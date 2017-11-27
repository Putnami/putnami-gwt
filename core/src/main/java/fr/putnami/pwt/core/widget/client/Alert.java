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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractForm;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.AlertDismissEvent;
import fr.putnami.pwt.core.widget.client.event.AlertDismissEvent.HasAlertDismissHandlers;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Alert<T> extends AbstractForm<T> implements HasAlertDismissHandlers {

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

		Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private Type type = Type.INFO;

	private boolean dismissable;
	private Anchor<?> dismissButton;

	public Alert(String html) {
		super(DivElement.TAG, html);
		StyleUtils.addStyle(this, Alert.STYLE_ALERT);
		StyleUtils.addStyle(this, Alert.STYLE_FADE);
		StyleUtils.addStyle(this, Alert.STYLE_VISIBLE);
		this.setType(Type.INFO);
	}

	public Alert(Type type, String message) {
		this("");
		this.setType(type);
		this.getElement().setInnerHTML(message);
	}

	protected Alert(Alert<T> source) {
		super(source);
		this.dismissable = source.dismissable;
		this.setType(source.type);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Alert<T>(this);
	}

	public void hide() {
		StyleUtils.removeStyle(this, Alert.STYLE_VISIBLE);
		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

			@Override
			public boolean execute() {
				Alert.this.removeFromParent();
				Alert.this.fireEvent(new AlertDismissEvent(Alert.this));
				return false;
			}
		}, 150);
	}

	@Override
	public void edit(T object) {
		super.edit(object);
		StyleUtils.addStyle(this, Alert.STYLE_VISIBLE);
	}

	@Override
	public LeafValueEditor<T> asEditor() {
		return new TakesValueEditorWrapper<T>(this);
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
		StyleUtils.addStyle(this, this.type);
	}

	public boolean isDismissable() {
		return this.dismissable;
	}

	public void setDismissable(boolean dismissable) {
		this.dismissable = dismissable;
		this.redraw();
	}

	@Override
	public void redraw() {
		StyleUtils.toggleStyle(this, Alert.STYLE_ALERT_DISMISSABLE, this.dismissable);
		if (this.dismissable) {
			if (this.dismissButton == null) {
				this.insert(this.ensureDismissButton(), 0, true);
			}
		} else if (this.dismissButton != null) {
			this.dismissButton.removeFromParent();
			this.dismissButton = null;
		}
	}

	private Anchor<?> ensureDismissButton() {
		if (this.dismissButton == null) {
			this.dismissButton = new Anchor<>("&times;");
			StyleUtils.addStyle(this.dismissButton, Alert.STYLE_CLOSE);
			this.dismissButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					Alert.this.hide();
				}

			});
		}
		return this.dismissButton;
	}

	@Override
	public HandlerRegistration addAlertDismissHandler(AlertDismissEvent.Handler handler) {
		return this.addHandler(handler, AlertDismissEvent.TYPE);
	}

}
