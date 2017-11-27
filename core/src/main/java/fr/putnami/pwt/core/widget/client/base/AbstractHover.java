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
package fr.putnami.pwt.core.widget.client.base;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import java.util.Iterator;

import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.EditorComposite;
import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public abstract class AbstractHover implements IsWidget, HasWidgets, HasOneWidget, CloneableWidget, EditorComposite {

	public enum Visibility implements CssStyle {

			SHOW("in"),
			HIDE(null),
			TOGGLE(null);

		private String name;

		Visibility(String name) {
			this.name = name;
		}

		@Override
		public String get() {
			return this.name;
		}
	}

	public enum Placement implements CssStyle {
			TOP("top"),
			BOTTOM("bottom"),
			LEFT("left"),
			RIGHT("right");

		private String name;

		Placement(String name) {
			this.name = name;
		}

		@Override
		public String get() {
			return this.name;
		}
	}

	public enum Trigger {
			HOVER,
			FOCUS,
			MANUAL;
	}

	protected class Handler implements FocusHandler, BlurHandler, MouseOverHandler, MouseOutHandler {

		@Override
		public void onMouseOver(MouseOverEvent event) {
			if (AbstractHover.this.visibilityChange == Visibility.HIDE) {
				AbstractHover.this.schedule(AbstractHover.this.getShowDelay(), Visibility.SHOW);
			}
		}

		@Override
		public void onMouseOut(MouseOutEvent event) {
			if (AbstractHover.this.visibilityChange == Visibility.SHOW) {
				AbstractHover.this.schedule(AbstractHover.this.getHideDelay(), Visibility.HIDE);
			}
		}

		@Override
		public void onFocus(FocusEvent event) {
			if (AbstractHover.this.visibilityChange == Visibility.HIDE) {
				AbstractHover.this.schedule(AbstractHover.this.getShowDelay(), Visibility.SHOW);
			}
		}

		@Override
		public void onBlur(BlurEvent event) {
			if (AbstractHover.this.visibilityChange == Visibility.SHOW) {
				AbstractHover.this.schedule(AbstractHover.this.getHideDelay(), Visibility.HIDE);
			}
		}
	}

	private String path;

	private Placement placement = Placement.TOP;
	private Trigger trigger = Trigger.HOVER;

	private int showDelayInMilliseconds = 0;
	private int hideDelayInMilliseconds = 0;

	private Visibility visibilityChange = Visibility.HIDE;

	private Widget widget;

	private final HandlerRegistrationCollection registrations = new HandlerRegistrationCollection();
	private final Handler triggerEventHandler = new Handler();

	public AbstractHover() {
	}

	protected AbstractHover(AbstractHover source) {
		this.showDelayInMilliseconds = source.showDelayInMilliseconds;
		this.hideDelayInMilliseconds = source.hideDelayInMilliseconds;
		this.placement = source.placement;

		this.setWidget(WidgetUtils.cloneWidget(source.widget));
		this.setTrigger(source.trigger);
	}

	@Override
	public String getPath() {
		return this.path == null ? Path.ROOT_PATH : this.path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void setWidget(IsWidget w) {
		this.setWidget(w == null ? null : w.asWidget());
	}

	@Override
	public Widget getWidget() {
		return this.widget;
	}

	@Override
	public Widget asWidget() {
		return this.getWidget();
	}

	@Override
	public void setWidget(Widget w) {
		if (w == this.widget) {
			return;
		}
		this.widget = w;
		if (this.widget == null) {
			return;
		}
		this.bindHandlers();
	}

	public void add(IsWidget w) {
		this.add(w.asWidget());
	}

	@Override
	public void add(Widget w) {
		if (this.getWidget() != null) {
			throw new IllegalStateException("can only contain one child widget");
		}
		this.setWidget(w);
	}

	@Override
	public void clear() {
		this.widget = null;
	}

	@Override
	public Iterator<Widget> iterator() {
		return Lists.newArrayList(this.widget).iterator();
	}

	@Override
	public boolean remove(Widget w) {
		if (this.widget != w) {
			return false;
		}
		this.widget = null;
		return true;
	}

	public void setPlacement(Placement placement) {
		this.placement = placement;
	}

	public Placement getPlacement() {
		return this.placement;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
		this.bindHandlers();
	}

	public Trigger getTrigger() {
		return this.trigger;
	}

	public void setShowDelay(int delayInMilliseconds) {
		this.showDelayInMilliseconds = delayInMilliseconds;
	}

	public int getShowDelay() {
		return this.showDelayInMilliseconds;
	}

	public void setHideDelay(int delayInMilliseconds) {
		this.hideDelayInMilliseconds = delayInMilliseconds;
	}

	public int getHideDelay() {
		return this.hideDelayInMilliseconds;
	}

	public void show() {
		this.changeVisibility(Visibility.SHOW);
	}

	public void hide() {
		this.changeVisibility(Visibility.HIDE);
	}

	public void toggleVisibility() {
		this.changeVisibility(Visibility.TOGGLE);
	}

	public void changeVisibility(Visibility visibilityChange) {
		Visibility order = visibilityChange;
		if (order == Visibility.TOGGLE) {
			order = this.visibilityChange == Visibility.HIDE ? Visibility.SHOW : Visibility.HIDE;
		}
		final Element toDisplayElement = this.getHoverWidget().getElement();
		final Element target = this.getWidget().getElement();
		final Element parent = target.getParentElement();
		if (parent == null) {
			return;
		}
		switch (order) {
			case SHOW:
				parent.insertAfter(toDisplayElement, target);
				toDisplayElement.getStyle().setDisplay(Display.BLOCK);
				this.resetPosition(toDisplayElement, this.getWidget(), this.placement);
				StyleUtils.addStyle(this.getHoverWidget(), this.placement);
				StyleUtils.addStyle(toDisplayElement, Visibility.SHOW);
				break;
			case HIDE:
				StyleUtils.removeStyle(toDisplayElement, Visibility.SHOW);
				Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

					@Override
					public boolean execute() {
						toDisplayElement.getStyle().clearDisplay();
						toDisplayElement.removeFromParent();
						return false;
					}
				}, 200);
				break;
			default:
				break;
		}
		this.visibilityChange = order;
	}

	private void bindHandlers() {
		if (this.widget == null) {
			return;
		}

		this.registrations.removeHandler();
		switch (this.getTrigger()) {
			case FOCUS:
				this.registrations.add(this.widget.addDomHandler(this.triggerEventHandler, FocusEvent.getType()));
				this.registrations.add(this.widget.addDomHandler(this.triggerEventHandler, BlurEvent.getType()));
				break;
			case HOVER:
				this.registrations.add(this.widget.addDomHandler(this.triggerEventHandler, MouseOverEvent.getType()));
				this.registrations.add(this.widget.addDomHandler(this.triggerEventHandler, MouseOutEvent.getType()));
				break;
			case MANUAL:
				break;
			default:
				break;
		}
	}

	private void schedule(int delay, final Visibility visibility) {
		if (delay > 0) {
			Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

				@Override
				public boolean execute() {
					AbstractHover.this.changeVisibility(visibility);
					return false;
				}
			}, delay);
		} else {
			this.changeVisibility(visibility);
		}
	}

	private void resetPosition(Element toPositionedElement, Widget relativeTo, Placement placement) {
		Element relativeElement = relativeTo.getElement();

		com.google.gwt.dom.client.Style elementStyle = toPositionedElement.getStyle();
		int tooltipWidth = toPositionedElement.getOffsetWidth();
		int tooltipHeight = toPositionedElement.getOffsetHeight();

		int targetWidth = relativeElement.getOffsetWidth();
		int targetHeight = relativeElement.getOffsetHeight();
		int targetTop = relativeElement.getOffsetTop();
		int targetLeft = relativeElement.getOffsetLeft();

		elementStyle.setPosition(Position.ABSOLUTE);
		switch (placement) {
			case TOP:
				elementStyle.setLeft(targetLeft + targetWidth / 2 - tooltipWidth / 2, Unit.PX);
				elementStyle.setTop(targetTop - tooltipHeight, Unit.PX);
				break;
			case BOTTOM:
				elementStyle.setLeft(targetLeft + targetWidth / 2 - tooltipWidth / 2, Unit.PX);
				elementStyle.setTop(targetTop + targetHeight, Unit.PX);
				break;
			case LEFT:
				elementStyle.setLeft(targetLeft - tooltipWidth, Unit.PX);
				elementStyle.setTop(targetTop + targetHeight / 2 - tooltipHeight / 2, Unit.PX);
				break;
			case RIGHT:
				elementStyle.setLeft(targetLeft + targetWidth, Unit.PX);
				elementStyle.setTop(targetTop + targetHeight / 2 - tooltipHeight / 2, Unit.PX);
				break;
			default:
				break;
		}
	}

	@Override
	public Iterable<Editor> getEditors() {
		return Lists.newArrayList((Editor) this.getWidget());
	}

	protected abstract Widget getHoverWidget();

}
