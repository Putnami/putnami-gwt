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
package fr.putnami.pwt.core.widget.client.helper;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent.Handler;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent.HasCollapseHandlers;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class CollapseHelper implements ClickHandler, HasCollapseHandlers {

	public static CollapseHelper apply(Widget toggleWidget, Element collapsableElement) {
		return new CollapseHelper(toggleWidget, collapsableElement);
	}

	public static CollapseHelper apply(Widget toggleWidget, Element collapsableElement, boolean initialCollapse) {
		CollapseHelper helper = apply(toggleWidget, collapsableElement);
		helper.setInitialCollapse(initialCollapse);
		return helper;
	}

	private static final CssStyle STYLE_COLLAPSING = new SimpleStyle("collapsing");
	private static final CssStyle STYLE_COLLAPSE = new SimpleStyle("collapse");
	private static final CssStyle STYLE_VISIBLE = new SimpleStyle("in");

	private Element collapsableElement;
	private Widget toggleWidget;

	private boolean collapsed;
	private boolean enabled = true;

	public CollapseHelper(Widget toggleWidget, Element collapsableElement) {
		this.toggleWidget = toggleWidget;
		this.collapsableElement = collapsableElement;
		StyleUtils.addStyle(collapsableElement, STYLE_COLLAPSE);
		toggleWidget.addDomHandler(this, ClickEvent.getType());
		setInitialCollapse(collapsed);
	}

	public void enable() {
		this.enabled = true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void disable() {
		this.enabled = false;
	}

	public void collapse() {
		doCollapse(true);
	}

	public void expand() {
		doCollapse(false);
	}

	public void toggleCollapse() {
		doCollapse(!collapsed);
	}

	@Override
	public void onClick(ClickEvent event) {
		if (enabled) {
			toggleCollapse();
		}
	}

	private void setInitialCollapse(boolean collapse) {
		this.collapsed = collapse;
		collapsableElement.getStyle().clearHeight();
		StyleUtils.removeStyle(collapsableElement, STYLE_COLLAPSING);
		StyleUtils.toggleStyle(collapsableElement, STYLE_VISIBLE, !collapse);
	}

	public void doCollapse(final boolean collapse) {
		if (collapse != this.collapsed) {
			EventBus.get().fireEventFromSource(new CollapseEvent(CollapseHelper.this, collapse), CollapseHelper.this);

			collapsableElement.getStyle().setHeight(collapsableElement.getOffsetHeight(), Unit.PX);

			StyleUtils.removeStyle(collapsableElement, STYLE_COLLAPSE);
			StyleUtils.removeStyle(collapsableElement, STYLE_VISIBLE);
			StyleUtils.addStyle(collapsableElement, STYLE_COLLAPSING);

			final int endHeight = collapse ? 0 : collapsableElement.getScrollHeight();

			Scheduler.get().scheduleDeferred(new ScheduledCommand() {

				@Override
				public void execute() {
					collapsableElement.getStyle().setHeight(endHeight, Unit.PX);
				}
			});

			Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

				@Override
				public boolean execute() {
					collapsableElement.getStyle().clearHeight();
					StyleUtils.removeStyle(collapsableElement, STYLE_COLLAPSING);
					StyleUtils.addStyle(collapsableElement, STYLE_COLLAPSE);
					StyleUtils.toggleStyle(collapsableElement, STYLE_VISIBLE, !collapse);
					return false;
				}
			}, 350);
			this.collapsed = collapse;
		}
	}

	@Override
	public HandlerRegistration addCollapseHandler(Handler handler) {
		return EventBus.get().addHandlerToSource(CollapseEvent.TYPE, this, handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		EventBus.get().fireEventFromSource(event, this);
	}
}
