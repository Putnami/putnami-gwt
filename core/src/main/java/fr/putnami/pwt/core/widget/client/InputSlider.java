/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with pwt. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.widget.client;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.List;

import fr.putnami.pwt.core.editor.client.event.DirtyEvent;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent.Handler;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractHover.Trigger;
import fr.putnami.pwt.core.widget.client.base.AbstractInput;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.ChangeEvent;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputSlider<T> extends AbstractInput<T> implements ValueChangeHandler<T> {

	private static final CssStyle STYLE_BACKGROUNG = new SimpleStyle("input-slider-background");
	private static final CssStyle STYLE_HANDLE = new SimpleStyle("input-slider-handle");

	private class DragHandler {

		private boolean dragging;

		public void onMouseUp(Event event) {
			if (this.dragging) {
				this.dragging = false;
				Event.releaseCapture(InputSlider.this.getElement());
			}
		}

		public void onMouseMove(Event event) {
			if (this.dragging) {
				InputSlider.this.handleWidget.moveHandleToPosition(this.getRelativeX(event));
			}
		}

		public void onMouseDown(Event event) {
			this.dragging = true;
			Event.setCapture(InputSlider.this.getElement());
			this.killEvent(event);
			InputSlider.this.handleWidget.moveHandleToPosition(this.getRelativeX(event));
		}

		public void onMouseWheel(Event event) {
			this.killEvent(event);
			int velocityY = event.getMouseWheelVelocityY();
			if (velocityY > 0) {
				InputSlider.this.handleWidget.moveRight(1);
			} else {
				InputSlider.this.handleWidget.moveLeft(1);
			}
		}

		private int getRelativeX(Event event) {
			int clientX = event.getClientX();
			if ((DOM.eventGetType(event) & Event.TOUCHEVENTS) != 0) {
				clientX = event.getTouches().get(0).getClientX();
			}
			return clientX - InputSlider.this.backgroundBar.getAbsoluteLeft()
				+ InputSlider.this.backgroundBar.getScrollLeft()
				+ InputSlider.this.backgroundBar.getOwnerDocument().getScrollLeft();
		}

		private void killEvent(Event event) {
			event.preventDefault();
			event.stopPropagation();
		}
	}

	private class KeyHandler implements KeyDownHandler {

		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (!InputSlider.this.isStateValid()) {
				return;
			}
			int paddingValue = 1;
			if (event.isControlKeyDown()) {
				paddingValue = InputSlider.this.items.size() / 5;
			}
			switch (event.getNativeKeyCode()) {
				case KeyCodes.KEY_HOME:
					InputSlider.this.handleWidget.moveStart();
					this.killEvent(event);
					break;
				case KeyCodes.KEY_END:
					InputSlider.this.handleWidget.moveEnd();
					this.killEvent(event);
					break;
				case KeyCodes.KEY_SPACE:
					InputSlider.this.handleWidget.moveMiddle();
					this.killEvent(event);
					break;
				case KeyCodes.KEY_LEFT:
					InputSlider.this.handleWidget.moveLeft(paddingValue);
					this.killEvent(event);
					break;
				case KeyCodes.KEY_RIGHT:
					InputSlider.this.handleWidget.moveRight(paddingValue);
					this.killEvent(event);
					break;
				default:
					break;
			}
		}

		private void killEvent(DomEvent<?> event) {
			event.preventDefault();
			event.stopPropagation();
		}
	}

	private class HandleWidget extends FocusWidget {

		private boolean focused = false;
		private int valueIndex = -1;

		HandleWidget() {
			super(DOM.createDiv());
			StyleUtils.addStyle(this, AbstractInput.STYLE_CONTROL);
			StyleUtils.addStyle(this, InputSlider.STYLE_HANDLE);

			super.addBlurHandler(new BlurHandler() {

				@Override
				public void onBlur(BlurEvent event) {
					HandleWidget.this.focused = false;
				}
			});

			super.addFocusHandler(new FocusHandler() {

				@Override
				public void onFocus(FocusEvent event) {
					HandleWidget.this.focused = true;
				}
			});
		}

		public T getValue() {
			if (this.valueIndex == -1) {
				return null;
			}
			return InputSlider.this.items.get(this.valueIndex);
		}

		public void setValue(T value, boolean fireEvent) {
			this.moveHandleToIndex(InputSlider.this.items.indexOf(value), fireEvent);
		}

		public void moveRight(int padNumber) {
			int newIndex = padNumber;
			if (this.valueIndex > -1) {
				newIndex += this.valueIndex;
			}
			this.moveHandleToIndex(newIndex, true);
		}

		public void moveLeft(int padNumber) {
			int newIndex = this.valueIndex - padNumber;
			this.moveHandleToIndex(newIndex, true);
		}

		public void moveMiddle() {
			this.moveHandleToIndex(InputSlider.this.items.size() / 2 - 1, true);
		}

		public void moveEnd() {
			this.moveHandleToIndex(InputSlider.this.items.size() - 1, true);
		}

		public void moveStart() {
			this.moveHandleToIndex(0, true);
		}

		public void moveHandleToIndex(int index, boolean fireEvent) {
			T oldValue = this.getValue();

			int newIndex = Math.min(InputSlider.this.items.size() - 1, Math.max(0, index));
			this.valueIndex = newIndex;
			double position = newIndex * 100d / (InputSlider.this.items.size() - 1d);
			this.setLeftPct(position);

			if (fireEvent) {
				ValueChangeEvent.fireIfNotEqual(InputSlider.this, oldValue, this.getValue());
			}
		}

		public void moveHandleToPosition(int relativeXPosition) {
			double idx =
				(double) (InputSlider.this.items.size() - 1) * (double) relativeXPosition
					/ InputSlider.this.backgroundBar.getClientWidth();
			this.moveHandleToIndex((int) Math.round(idx), true);
		}

		private void setLeftPct(double left) {
			double leftPosition = Math.min(100, Math.max(0, left));
			this.getElement().getStyle().setLeft(leftPosition, Unit.PCT);
		}
	}

	private final SimplePanel contentPanel;

	private final Element backgroundBar = DOM.createDiv();
	private final HandleWidget handleWidget = new HandleWidget();

	private final Popover popover = new Popover();

	private final KeyHandler keyHandler = new KeyHandler();
	private final DragHandler dragHandler = new DragHandler();

	private final List<T> items = Lists.newArrayList();

	private Renderer<T> labelRenderer;
	private HandlerRegistration valueChangeRegistration;

	public InputSlider() {
		super(new SimplePanel());
		this.contentPanel = (SimplePanel) this.getWidget();
		this.endConstruct();
	}

	protected InputSlider(InputSlider<T> source) {
		super(new SimplePanel(), source);
		this.contentPanel = (SimplePanel) this.getWidget();
		this.endConstruct();
		this.labelRenderer = source.labelRenderer;
		this.items.addAll(source.items);
	}

	@Override
	protected void endConstruct() {
		this.getElement().appendChild(this.backgroundBar);
		StyleUtils.addStyle(this.backgroundBar, InputSlider.STYLE_BACKGROUNG);
		this.contentPanel.setWidget(this.handleWidget);

		this.popover.add(this.handleWidget);
		this.popover.setTrigger(Trigger.FOCUS);

		this.handleWidget.addKeyDownHandler(this.keyHandler);

		this.addValueChangeHandler(this);

		this.sinkEvents(Event.MOUSEEVENTS | Event.ONMOUSEWHEEL | Event.TOUCHEVENTS);

		super.endConstruct();
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputSlider<T>(this);
	}

	public void setItems(List<T> items) {
		this.items.clear();
		if (items != null) {
			this.items.addAll(items);
		}
	}

	@Override
	public boolean isDirty() {
		return !Objects.equal(this.getValue(), this.handleWidget.getValue());
	}

	@Override
	public T flush() {
		this.clearErrors();
		T value = this.handleWidget.getValue();
		this.validate(value);
		if (!this.hasErrors()) {
			this.setValue(value);
		}
		return this.getValue();
	}

	@Override
	public void edit(T value) {
		this.clearErrors();
		this.setValue(value);
		if (this.isStateValid()) {
			this.handleWidget.setValue(value, false);
			this.popover.setText(this.render(value));
		}
	}

	@Override
	public HandlerRegistration addDirtyHandler(Handler handler) {
		if (this.valueChangeRegistration == null) {
			this.valueChangeRegistration = this.addValueChangeHandler(new ChangeEvent<T>(InputSlider.this));
		}
		return EventBus.get().addHandlerToSource(DirtyEvent.TYPE, this, handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
		return this.addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void onValueChange(ValueChangeEvent<T> event) {
		this.popover.setText(this.render(event.getValue()));
		this.popover.show();
	}

	private String render(T value) {
		if (value == null) {
			return null;
		}
		if (this.labelRenderer != null) {
			return this.labelRenderer.render(value);
		}
		return value.toString();
	}

	@Override
	public void setHtmlId(String htmlId) {
		this.handleWidget.getElement().setId(htmlId);
	}

	@Override
	public String getHtmlId() {
		return this.handleWidget.getElement().getId();
	}

	public void setLabelRenderer(Renderer<T> labelRenderer) {
		this.labelRenderer = labelRenderer;
	}

	private boolean isStateValid() {
		return this.items != null && this.items.size() > 1;
	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);
		if (!this.isStateValid()) {
			return;
		}
		switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEDOWN:
			case Event.ONTOUCHSTART:
				this.handleWidget.setFocus(true);
				this.dragHandler.onMouseDown(event);
				break;
			case Event.ONMOUSEUP:
			case Event.ONTOUCHEND:
				this.dragHandler.onMouseUp(event);
				break;
			case Event.ONMOUSEMOVE:
			case Event.ONTOUCHMOVE:
				this.dragHandler.onMouseMove(event);
				break;
			case Event.ONMOUSEWHEEL:
				if (this.handleWidget.focused) {
					this.dragHandler.onMouseWheel(event);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addFocusHandler(FocusHandler handler) {
		return this.handleWidget.addFocusHandler(handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addBlurHandler(BlurHandler handler) {
		return this.handleWidget.addBlurHandler(handler);
	}

	@Override
	public int getTabIndex() {
		return this.handleWidget.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		this.handleWidget.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		this.handleWidget.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		this.handleWidget.setTabIndex(index);
	}

}
