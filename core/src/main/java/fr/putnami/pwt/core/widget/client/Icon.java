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

import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.theme.client.ThemeController;
import fr.putnami.pwt.core.widget.client.base.AbstractWidget;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Icon extends AbstractWidget {

	private static final CssStyle STYLE_LIGHT = new SimpleStyle("light");

	public enum Color implements CssStyle {
			DEFAULT("text-default"),
			MUTED("text-muted"),
			INFO("text-info"),
			SUCCESS("text-success"),
			WARNING("text-warning"),
			DANGER("text-danger");

		private final String style;

		Color(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private static final String ICON_TAG_NAME = "i";

	private String type;
	private Color color;

	private boolean light;

	public Icon() {
		super(Icon.ICON_TAG_NAME);
	}

	protected Icon(Icon source) {
		super(source);
		this.setType(source.type);
		this.setColor(source.color);
		this.setLight(source.light);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Icon(this);
	}

	public void setType(String type) {
		StyleUtils.addStyle(this, ThemeController.get().getIconStyle(type));
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public Color getColor() {
		return this.color;
	}

	public void setColor(Color color) {
		StyleUtils.addStyle(this, color);
		this.color = color;
	}

	public boolean isLight() {
		return this.light;
	}

	public void setLight(boolean light) {
		StyleUtils.toggleStyle(this, Icon.STYLE_LIGHT, light);
		this.light = light;
	}
}
