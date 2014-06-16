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
package fr.putnami.pwt.core.widget.client.base;

import java.util.Iterator;

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

import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.EditorComposite;
import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public abstract class AbstractHover implements IsWidget, HasWidgets, HasOneWidget, CloneableWidget, EditorComposite {

	public enum Visibility implements CssStyle {

		SHOW("in"),
		HIDE(null),
		TOGGLE(null);

		private String name;

		private Visibility(String name) {
			this.name = name;
		}

		@Override
		public String get() {
			return name;
		}
	}

	public enum Placement implements CssStyle {
		TOP("top"),
		BOTTOM("bottom"),
		LEFT("left"),
		RIGHT("right");

		private String name;

		private Placement(String name) {
			this.name = name;
		}

		@Override
		public String get() {
			return this.name;
		}
	}

	public static enum Trigger {
		HOVER,
		FOCUS,
		MANUAL;
	}

	protected class Handler implements FocusHandler, BlurHandler, MouseOverHandler, MouseOutHandler {

		@Override
		public void onMouseOver(MouseOverEvent event) {
			if (visibilityChange == Visibility.HIDE) {
				schedule(getShowDelay(), Visibility.SHOW);
			}
		}

		@Override
		public void onMouseOut(MouseOutEvent event) {
			if (visibilityChange == Visibility.SHOW) {
				schedule(getHideDelay(), Visibility.HIDE);
			}
		}

		@Override
		public void onFocus(FocusEvent event) {
			if (visibilityChange == Visibility.HIDE) {
				schedule(getShowDelay(), Visibility.SHOW);
			}
		}

		@Override
		public void onBlur(BlurEvent event) {
			if (visibilityChange == Visibility.SHOW) {
				schedule(getHideDelay(), Visibility.HIDE);
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
		showDelayInMilliseconds = source.showDelayInMilliseconds;
		hideDelayInMilliseconds = source.hideDelayInMilliseconds;
		placement = source.placement;

		setWidget(WidgetUtils.cloneWidget(source.widget));
		setTrigger(source.trigger);
	}

	@Override
	public String getPath() {
		return path == null ? Path.ROOT_PATH : path;
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
		return widget;
	}

	@Override
	public Widget asWidget() {
		return getWidget();

	}

	@Override
	public void setWidget(Widget w) {
		if (w == widget) {
			return;
		}
		widget = w;
		if (widget == null) {
			return;
		}
		bindHandlers();
	}

	public void add(IsWidget w) {
		this.add(w.asWidget());
	}

	@Override
	public void add(Widget w) {
		if (getWidget() != null) {
			throw new IllegalStateException("can only contain one child widget");
		}
		this.setWidget(w);
	}

	@Override
	public void clear() {
		widget = null;
	}

	@Override
	public Iterator<Widget> iterator() {
		return Lists.newArrayList(widget).iterator();
	}

	@Override
	public boolean remove(Widget w) {
		if (widget != w) {
			return false;
		}
		widget = null;
		return true;
	}

	public void setPlacement(Placement placement) {
		this.placement = placement;
	}

	public Placement getPlacement() {
		return placement;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
		bindHandlers();
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setShowDelay(int delayInMilliseconds) {
		showDelayInMilliseconds = delayInMilliseconds;
	}

	public int getShowDelay() {
		return showDelayInMilliseconds;
	}

	public void setHideDelay(int delayInMilliseconds) {
		hideDelayInMilliseconds = delayInMilliseconds;
	}

	public int getHideDelay() {
		return hideDelayInMilliseconds;
	}

	public void show() {
		changeVisibility(Visibility.SHOW);
	}

	public void hide() {
		changeVisibility(Visibility.HIDE);
	}

	public void toggleVisibility() {
		changeVisibility(Visibility.TOGGLE);
	}

	public void changeVisibility(Visibility visibilityChange) {
		Visibility order = visibilityChange;
		if (order == Visibility.TOGGLE) {
			order = this.visibilityChange == Visibility.HIDE ? Visibility.SHOW : Visibility.HIDE;
		}
		final Element toDisplayElement = getHoverWidget().getElement();
		final Element target = getWidget().getElement();
		switch (order) {
		case SHOW:
			target.getParentElement().insertAfter(toDisplayElement, target);
			toDisplayElement.getStyle().setDisplay(Display.BLOCK);
			resetPosition(toDisplayElement, getWidget(), placement);
			StyleUtils.addStyle(getHoverWidget(), placement);
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
		if (widget == null) {
			return;
		}

		registrations.removeHandler();
		switch (getTrigger()) {
		case FOCUS:
			registrations.add(widget.addDomHandler(triggerEventHandler, FocusEvent.getType()));
			registrations.add(widget.addDomHandler(triggerEventHandler, BlurEvent.getType()));
			break;
		case HOVER:
			registrations.add(widget.addDomHandler(triggerEventHandler, MouseOverEvent.getType()));
			registrations.add(widget.addDomHandler(triggerEventHandler, MouseOutEvent.getType()));
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
					changeVisibility(visibility);
					return false;
				}
			}, delay);
		}
		else {
			changeVisibility(visibility);
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
			elementStyle.setLeft(targetLeft + (targetWidth / 2) - (tooltipWidth / 2), Unit.PX);
			elementStyle.setTop(targetTop - tooltipHeight, Unit.PX);
			break;
		case BOTTOM:
			elementStyle.setLeft(targetLeft + (targetWidth / 2) - (tooltipWidth / 2), Unit.PX);
			elementStyle.setTop(targetTop + targetHeight, Unit.PX);
			break;
		case LEFT:
			elementStyle.setLeft(targetLeft - tooltipWidth, Unit.PX);
			elementStyle.setTop(targetTop + (targetHeight / 2) - (tooltipHeight / 2), Unit.PX);
			break;
		case RIGHT:
			elementStyle.setLeft(targetLeft + targetWidth, Unit.PX);
			elementStyle.setTop(targetTop + (targetHeight / 2) - (tooltipHeight / 2), Unit.PX);
			break;

		}
	}

	@Override
	public Iterable<Editor> getEditors() {
		return Lists.newArrayList((Editor) getWidget());
	}

	protected abstract Widget getHoverWidget();

}
