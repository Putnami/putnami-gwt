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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class ButtonGroup extends AbstractPanel implements CloneableWidget {

	private static final CssStyle STYLE_JUSTIFIED = new SimpleStyle("btn-group-justified");

	public enum Type implements CssStyle {
			DEFAULT("btn-group"),
			VERTICAL("btn-group-vertical"),
			TOOLBAR("btn-toolbar");

		private final String style;

		Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	public enum Size implements CssStyle {
			X_SMALL("btn-group-xs"),
			SMALL("btn-group-sm"),
			DEFAULT(null),
			LARGE("btn-group-lg");

		private final String style;

		Size(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private Type type = Type.DEFAULT;
	private Size size = Size.DEFAULT;
	private boolean justified = false;

	public ButtonGroup() {
		super(DivElement.TAG);
		this.setSize(this.size);
		this.setType(this.type);
	}

	protected ButtonGroup(ButtonGroup source) {
		super(source);
		this.setSize(source.size);
		this.setType(source.type);
		this.setJustified(source.justified);
		this.cloneSourceWidgets(source);
	}

	@Override
	public IsWidget cloneWidget() {
		return new ButtonGroup(this);
	}

	@Override
	public void add(IsWidget child) {
		if (child instanceof Button) {
			this.append(child);
			((Button<?>) child).setSize(null);
		}
		if (child instanceof ButtonDropdown) {
			this.append(child);
			((ButtonDropdown) child).setSize(null);
		}
		if (child instanceof ButtonGroup) {
			this.append(child);
			((ButtonGroup) child).setSize(null);
		}
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
		StyleUtils.addStyle(this, this.type);
	}

	public Size getSize() {
		return this.size;
	}

	public void setSize(Size size) {
		this.size = size;
		StyleUtils.addStyle(this, this.size);
	}

	public boolean isJustified() {
		return this.justified;
	}

	public void setJustified(boolean justified) {
		this.justified = justified;
		StyleUtils.toggleStyle(this, ButtonGroup.STYLE_JUSTIFIED, justified);
	}

}
