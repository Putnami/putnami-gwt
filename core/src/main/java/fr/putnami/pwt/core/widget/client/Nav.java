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

import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Nav extends AbstractPanel implements CloneableWidget {

	public enum LinkStyle implements CssStyle {

			DISABLED("disabled"),
			ACTIVE("active");

		private final String style;

		LinkStyle(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	public enum Style implements CssStyle {
			DEFAULT(null),
			TABS("nav-tabs"),
			PILLS("nav-pills"),
			PILLS_STACKED("nav-pills nav-stacked"),
			TABS_JUSTIFIED("nav-tabs nav-justified"),
			PILLS_JUSTIFIED("nav-pills nav-justified");

		private final String style;

		Style(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private static final CssStyle STYLE_NAV = new SimpleStyle("nav");

	public interface IsNavContent extends IsWidget {

		void setActive(boolean active);

		boolean isActive();
	}

	private Style style = Style.DEFAULT;

	public Nav() {
		super(UListElement.TAG);
		StyleUtils.addStyle(this, Nav.STYLE_NAV);
	}

	protected Nav(Nav source) {
		super(source);
		this.setStyle(source.style);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Nav(this);
	}

	@Override
	public void add(IsWidget w) {
		this.append(w);
	}

	public void addNavContent(Nav.IsNavContent navContent) {
		this.append(navContent);
	}

	public void setStyle(Style style) {
		this.style = style;
		StyleUtils.addStyle(this.getElement(), this.style);
	}

	public Style getStyle() {
		return this.style;
	}

}
