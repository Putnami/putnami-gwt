/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.widget.client;

import java.util.List;

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
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.event.DirtyEvent;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent.Handler;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.widget.client.base.AbstractHover.Trigger;
import fr.putnami.pwt.core.widget.client.base.AbstractInput;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.ChangeEvent;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputSlider<T> extends AbstractInput<T> implements
		HasValueChangeHandlers<T>,
		ValueChangeHandler<T> {

	private static final CssStyle STYLE_BACKGROUNG = new SimpleStyle("input-slider-background");
	private static final CssStyle STYLE_HANDLE = new SimpleStyle("input-slider-handle");

	private class DragHandler {

		private boolean dragging;

		public void onMouseUp(Event event) {
			if (this.dragging) {
				this.dragging = false;
				Event.releaseCapture(InputSlider.this.getElement());
				handleWidget.moveHandleToPosition(getRelativeX(event));
			}
		}

		public void onMouseMove(Event event) {
			if (dragging) {
				handleWidget.moveHandleToPosition(getRelativeX(event));
			}
		}

		public void onMouseDown(Event event) {
			this.dragging = true;
			Event.setCapture(InputSlider.this.getElement());
			killEvent(event);
			handleWidget.moveHandleToPosition(getRelativeX(event));
		}

		public void onMouseWheel(Event event) {
			killEvent(event);
			int velocityY = event.getMouseWheelVelocityY();
			if (velocityY > 0) {
				InputSlider.this.handleWidget.moveRight(1);
			}
			else {
				InputSlider.this.handleWidget.moveLeft(1);
			}
		}

		private int getRelativeX(Event event) {
			int clientX = event.getClientX();
			if ((DOM.eventGetType(event) & Event.TOUCHEVENTS) != 0) {
				clientX = event.getTouches().get(0).getClientX();
			}
			return clientX - InputSlider.this.backgroundBar.getAbsoluteLeft() + InputSlider.this.backgroundBar.getScrollLeft() +
					InputSlider.this.backgroundBar.getOwnerDocument().getScrollLeft();
		}

		private void killEvent(Event event) {
			event.preventDefault();
			event.stopPropagation();
		}

	}

	private class KeyHandler implements KeyDownHandler {

		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (!isStateValid()) {
				return;
			}
			int paddingValue = 1;
			if (event.isControlKeyDown()) {
				paddingValue = InputSlider.this.items.size() / 5;
			}
			switch (event.getNativeKeyCode()) {
			case KeyCodes.KEY_HOME:
				InputSlider.this.handleWidget.moveStart();
				killEvent(event);
				break;
			case KeyCodes.KEY_END:
				InputSlider.this.handleWidget.moveEnd();
				killEvent(event);
				break;
			case KeyCodes.KEY_SPACE:
				InputSlider.this.handleWidget.moveMiddle();
				killEvent(event);
				break;
			case KeyCodes.KEY_LEFT:
				InputSlider.this.handleWidget.moveLeft(paddingValue);
				killEvent(event);
				break;
			case KeyCodes.KEY_RIGHT:
				InputSlider.this.handleWidget.moveRight(paddingValue);
				killEvent(event);
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

		public HandleWidget() {
			super(DOM.createDiv());
			StyleUtils.addStyle(this, STYLE_CONTROL);
			StyleUtils.addStyle(this, STYLE_HANDLE);

			addBlurHandler(new BlurHandler() {

				@Override
				public void onBlur(BlurEvent event) {
					focused = false;
				}
			});

			addFocusHandler(new FocusHandler() {

				@Override
				public void onFocus(FocusEvent event) {
					focused = true;
				}
			});
		}

		public T getValue() {
			if (valueIndex == -1) {
				return null;
			}
			return InputSlider.this.items.get(valueIndex);
		}

		public void setValue(T value, boolean fireEvent) {
			moveHandleToIndex(InputSlider.this.items.indexOf(value), fireEvent);
		}

		public void moveRight(int padNumber) {
			int newIndex = padNumber;
			if (valueIndex > -1) {
				newIndex += valueIndex;
			}
			moveHandleToIndex(newIndex, true);
		}

		public void moveLeft(int padNumber) {
			int newIndex = valueIndex - padNumber;
			moveHandleToIndex(newIndex, true);
		}

		public void moveMiddle() {
			moveHandleToIndex(InputSlider.this.items.size() / 2 - 1, true);
		}

		public void moveEnd() {
			moveHandleToIndex(InputSlider.this.items.size() - 1, true);
		}

		public void moveStart() {
			moveHandleToIndex(0, true);
		}

		public void moveHandleToIndex(int index, boolean fireEvent) {
			T oldValue = this.getValue();

			int newIndex = Math.min(InputSlider.this.items.size() - 1, Math.max(0, index));
			this.valueIndex = newIndex;
			double position = newIndex * 100d / (InputSlider.this.items.size() - 1d);
			setLeftPct(position);

			if (fireEvent) {
				ValueChangeEvent.fireIfNotEqual(InputSlider.this, oldValue, this.getValue());
			}
		}

		public void moveHandleToPosition(int relativeXPosition) {
			double idx = (double) (InputSlider.this.items.size() - 1) * (double) relativeXPosition
					/ InputSlider.this.backgroundBar.getClientWidth();
			moveHandleToIndex((int) Math.round(idx), true);
		}

		private void setLeftPct(double left) {
			left = Math.min(100, Math.max(0, left));
			getElement().getStyle().setLeft(left, Unit.PCT);
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
		contentPanel = (SimplePanel) getWidget();
		endConstruct();
	}

	protected InputSlider(InputSlider<T> source) {
		super(new SimplePanel(), source);
		contentPanel = (SimplePanel) getWidget();
		endConstruct();
		labelRenderer = source.labelRenderer;
		items.addAll(source.items);
	}

	@Override
	protected void endConstruct() {
		getElement().appendChild(backgroundBar);
		StyleUtils.addStyle(backgroundBar, STYLE_BACKGROUNG);
		contentPanel.setWidget(handleWidget);

		popover.add(handleWidget);
		popover.setTrigger(Trigger.FOCUS);

		handleWidget.addKeyDownHandler(keyHandler);

		addValueChangeHandler(this);

		sinkEvents(Event.MOUSEEVENTS | Event.ONMOUSEWHEEL | Event.TOUCHEVENTS);

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
		return !Objects.equal(getValue(), handleWidget.getValue());
	}

	@Override
	public T flush() {
		T value = handleWidget.getValue();
		validate(value);
		if (!hasErrors()) {
			setValue(value);
		}
		return getValue();
	}

	@Override
	public void edit(T value) {
		setValue(value);
		if (isStateValid()) {
			handleWidget.setValue(value, false);
			popover.setText(render(value));
		}
	}

	@Override
	public HandlerRegistration addDirtyHandler(Handler handler) {
		if (valueChangeRegistration == null) {
			valueChangeRegistration = addValueChangeHandler(new ChangeEvent<T>(InputSlider.this));
		}
		return EventBus.get().addHandlerToSource(DirtyEvent.TYPE, this, handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void onValueChange(ValueChangeEvent<T> event) {
		popover.setText(render(event.getValue()));
		popover.show();
	}

	private String render(T value) {
		if (value == null) {
			return null;
		}
		if (labelRenderer != null) {
			return labelRenderer.render(value);
		}
		return value.toString();
	}

	@Override
	public void setHtmlId(String htmlId) {
		handleWidget.getElement().setId(htmlId);
	}

	@Override
	public String getHtmlId() {
		return handleWidget.getElement().getId();

	}

	public void setLabelRenderer(Renderer<T> labelRenderer) {
		this.labelRenderer = labelRenderer;
	}

	private boolean isStateValid() {
		return items != null && items.size() > 1;
	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);
		if (!isStateValid()) {
			return;
		}
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEDOWN:
		case Event.ONTOUCHSTART:
			handleWidget.setFocus(true);
			dragHandler.onMouseDown(event);
			break;
		case Event.ONMOUSEUP:
		case Event.ONTOUCHEND:
			dragHandler.onMouseUp(event);
			break;
		case Event.ONMOUSEMOVE:
		case Event.ONTOUCHMOVE:
			dragHandler.onMouseMove(event);
			break;
		case Event.ONMOUSEWHEEL:
			if (handleWidget.focused) {
				dragHandler.onMouseWheel(event);
			}
			break;
		}
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addFocusHandler(FocusHandler handler) {
		return handleWidget.addFocusHandler(handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addBlurHandler(BlurHandler handler) {
		return handleWidget.addBlurHandler(handler);
	}

	@Override
	public int getTabIndex() {
		return handleWidget.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		handleWidget.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		handleWidget.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		handleWidget.setTabIndex(index);
	}

}
