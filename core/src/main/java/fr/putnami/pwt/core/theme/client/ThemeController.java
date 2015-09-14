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
package fr.putnami.pwt.core.theme.client;

import com.google.gwt.core.client.GWT;

/**
 * The ThemeController manages the webapp theming.
 *
 * <p>
 * The default implementations are {@link DefaultThemeController}, or (
 * {@link DefaultIE8ThemeController} for IE8.
 * </p>
 * <p>
 * You can implement your own ThemeController by setting the below lines in your module .gwt.xml
 * </p>
 *
 * <pre>
 * &lt;replace-with class="you.package.CustomThemeController"&gt;
 * 	&lt;when-type-is class="fr.putnami.pwt.core.theme.client.ThemeController"/&gt;
 * &lt;/replace-with&gt;
 * </pre>
 *
 * @since 1.0
 */
public abstract class ThemeController {

	private static ThemeController instance;

	/**
	 * protected theme controller constructor.
	 * <p>
	 * This constructor is protected you must get the ThemeController via the static get() method.
	 * </p>
	 */
	protected ThemeController() {
	}

	/**
	 * Gets the default theme.
	 * <p>
	 * The default theme is initialized in
	 * {@link fr.putnami.pwt.core.widget.client.boot.BootstrapThemeLoader}
	 * </p>
	 *
	 * @return the default theme
	 */
	public abstract Theme getDefaultTheme();

	/**
	 * Install a theme.
	 * <ol>
	 * <li>Remove all css links from the html header</li>
	 * <li>Install all default theme css</li>
	 * <li>Install all current theme css</li>
	 * </ol>
	 *
	 * @param theme the theme
	 */
	public abstract void installTheme(Theme theme);

	/**
	 * Install default theme.
	 * <p>
	 * Remove the current theme then init the default theme.
	 * </p>
	 */
	public abstract void installDefaultTheme();

	/**
	 * Reset the current theme in the html header.
	 */
	public abstract void resetTheme();

	/**
	 * Gets the icon style name from the icon name.
	 * <p>
	 * For example, the default fontello as "icon-" prefix. so getInconStyle("plus") returns
	 * "icon-plus"
	 * </p>
	 *
	 * @param iconName the icon name
	 * @return the css icon style name
	 */
	public abstract CssStyle getIconStyle(String iconName);

	/**
	 * Gets the singleton.
	 * <p>
	 * The singleton is instantiated with GWT.create(ThemeController.class), so you can customize your
	 * implementation in your module .gwt.xml
	 * </p>
	 *
	 * @return the theme controller
	 */
	public static ThemeController get() {
		if (ThemeController.instance == null) {
			ThemeController.instance = GWT.create(ThemeController.class);
		}
		return ThemeController.instance;
	}

}
