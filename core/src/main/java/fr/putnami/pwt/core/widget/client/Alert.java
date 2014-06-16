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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.widget.client.base.AbstractHTMLPanel;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.AlertDismissEvent;
import fr.putnami.pwt.core.widget.client.event.AlertDismissEvent.HasAlertDismissHandlers;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Alert extends AbstractHTMLPanel implements HasDrawable, HasAlertDismissHandlers {

	private static final CssStyle STYLE_ALERT = new SimpleStyle("alert");
	private static final CssStyle STYLE_ALERT_DISMISSABLE = new SimpleStyle("alert-dismissable");

	private static final CssStyle STYLE_FADE = new SimpleStyle("fade");

	private static final CssStyle STYLE_VISIBLE = new SimpleStyle("in");

	public static final CssStyle STYLE_ALERT_LINK = new SimpleStyle("alert-link");

	private static final CssStyle STYLE_CLOSE = new SimpleStyle("close");

	public enum Type implements CssStyle {
		SUCCESS("alert-success"),
		INFO("alert-info"),
		WARNING("alert-warning"),
		DANGER("alert-danger");

		private final String style;

		private Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return style;
		}
	}

	private Type type = Type.INFO;

	private boolean dismissable;
	private Anchor<?> dismissButton;

	public Alert(String html) {
		super(DivElement.TAG, html);
		StyleUtils.addStyle(this, STYLE_ALERT);
		StyleUtils.addStyle(this, STYLE_FADE);
		StyleUtils.addStyle(this, STYLE_VISIBLE);
		setType(Type.INFO);
	}

	protected Alert(Alert source) {
		super(source);
		this.dismissable = source.dismissable;
		setType(source.type);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Alert(this);
	}

	public void hide() {
		StyleUtils.removeStyle(this, STYLE_VISIBLE);
		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

			@Override
			public boolean execute() {
				Alert.this.removeFromParent();
				EventBus.get().fireEventFromSource(new AlertDismissEvent(Alert.this), Alert.this);
				return false;
			}
		}, 150);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
		StyleUtils.addStyle(this, this.type);
	}

	public boolean isDismissable() {
		return dismissable;
	}

	public void setDismissable(boolean dismissable) {
		this.dismissable = dismissable;
		StyleUtils.toggleStyle(this, STYLE_ALERT_DISMISSABLE, dismissable);
		redraw();
	}

	@Override
	public void redraw() {
		if (dismissable && dismissButton == null) {
			insert(ensureDismissButton(), 0, true);
		}
		else if (dismissButton != null) {
			dismissButton.removeFromParent();
			dismissButton = null;
		}
	}

	private Anchor<?> ensureDismissButton() {
		if (dismissButton == null) {
			dismissButton = new Anchor("&times;");
			StyleUtils.addStyle(dismissButton, STYLE_CLOSE);
			dismissButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					hide();
				}

			});
		}
		return dismissButton;
	}

	@Override
	public HandlerRegistration addAlertDismissHandler(AlertDismissEvent.Handler handler) {
		return EventBus.get().addHandlerToSource(AlertDismissEvent.TYPE, this, handler);
	}

}
