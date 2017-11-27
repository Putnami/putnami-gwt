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

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.mvp.client.MvpController;
import fr.putnami.pwt.core.mvp.client.event.StartActivityEvent;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.theme.client.IconFont;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.helper.CollapseHelper;
import fr.putnami.pwt.core.widget.client.util.AnchorUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public class Navbar extends AbstractPanel implements CloneableWidget {

	private static final CssStyle STYLE_NAVBAR = new SimpleStyle("navbar");
	private static final CssStyle STYLE_BRAND = new SimpleStyle("navbar-brand");
	private static final CssStyle STYLE_NAV = new SimpleStyle("nav navbar-nav");
	private static final CssStyle STYLE_HEADER = new SimpleStyle("navbar-header");
	private static final CssStyle STYLE_TOGGLE = new SimpleStyle("navbar-toggle");
	private static final CssStyle STYLE_NAV_BUTTON = new SimpleStyle("navbar-btn");
	private static final CssStyle STYLE_NAVBAR_LEFT = new SimpleStyle("navbar-left");
	private static final CssStyle STYLE_NAVBAR_RIGHT = new SimpleStyle("navbar-right");
	private static final CssStyle STYLE_COLLAPSE = new SimpleStyle("navbar-collapse");
	private static final CssStyle STYLE_TEXT_MUTED = new SimpleStyle("text-muted");

	public enum ContainerType implements CssStyle {
			RESPONSIVE("container"),
			FLUID("container-fluid");

		private final String style;

		ContainerType(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	public enum Type implements CssStyle {

			DEFAULT("navbar-default"),
			INVERSE("navbar-inverse");

		private final String style;

		Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	public enum Position implements CssStyle {
			DEFAULT(null),
			FIXED_TOP("navbar-fixed-top"),
			FIXED_BOTTOM("navbar-fixed-bottom"),
			STATIC_TOP("navbar-static-top");

		private final String style;

		Position(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private class NavbarButton extends Widget implements CloneableWidget {

		NavbarButton() {
			this.setElement(Document.get().createAnchorElement());
			StyleUtils.addStyle(this, Navbar.STYLE_TOGGLE);
			StyleUtils.addStyle(this, Navbar.STYLE_TEXT_MUTED);
			AnchorElement.as(this.getElement()).setHref(AnchorUtils.DUMMY_HREF);
			this.getElement().appendChild(this.createIcon());
		}

		@Override
		public IsWidget cloneWidget() {
			return new NavbarButton();
		}

		public void setCollapseContainer(Container collapseContainer) {
			Navbar.this.collapseHelper = CollapseHelper.apply(this, collapseContainer.getElement(), true);
		}

		private Element createIcon() {
			Icon icon = new Icon();
			icon.setType(IconFont.ICON_MENU);
			return icon.getElement();
		}
	}

	private final Container headerContainer = new Container();
	private final Container collapseContainer = new Container();
	private final Container contentContainer = new Container();

	private ContainerType containerType = ContainerType.FLUID;
	private Type type = Type.DEFAULT;
	private Position position = Position.DEFAULT;
	private CollapseHelper collapseHelper;

	private StartActivityEvent.Handler collapseHandler = new StartActivityEvent.Handler() {

		@Override
		public void onStartActivity(StartActivityEvent event) {
			if (Navbar.this.collapseHelper != null) {
				Navbar.this.collapseHelper.collapse();
			}
		}
	};

	public Navbar() {
		super(DivElement.TAG);
		this.endConstruct();
	}

	protected Navbar(Navbar source) {
		super(source);
		this.type = source.type;
		this.position = source.position;
		this.containerType = source.containerType;
		for (Widget collapseWidget : source.collapseContainer) {
			this.collapseContainer.append(WidgetUtils.cloneWidget(collapseWidget));
		}
		this.endConstruct();
	}

	private void endConstruct() {
		this.append(this.contentContainer);
		this.contentContainer.append(this.headerContainer);
		this.contentContainer.append(this.collapseContainer);

		NavbarButton button = new NavbarButton();
		button.setCollapseContainer(this.collapseContainer);
		this.headerContainer.append(button);

		StyleUtils.addStyle(this, Navbar.STYLE_NAVBAR);
		StyleUtils.addStyle(this.headerContainer, Navbar.STYLE_HEADER);
		StyleUtils.addStyle(this.collapseContainer, Navbar.STYLE_COLLAPSE);

		this.setContainerType(this.containerType);
		this.setType(this.type);
		this.setPosition(this.position);

		MvpController.get().addStartActivityHandler(this.collapseHandler);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Navbar(this);
	}

	@UiChild(tagname = "Brand")
	public void addBrand(Widget w) {
		StyleUtils.addStyle(Widget.asWidgetOrNull(w), Navbar.STYLE_BRAND);
		this.headerContainer.add(w);
	}

	@UiChild(tagname = "left")
	public void addLeft(IsWidget w) {
		StyleUtils.addStyle(Widget.asWidgetOrNull(w), Navbar.STYLE_NAVBAR_LEFT);
		this.add(w);
	}

	@UiChild(tagname = "right")
	public void addRight(IsWidget w) {
		StyleUtils.addStyle(Widget.asWidgetOrNull(w), Navbar.STYLE_NAVBAR_RIGHT);
		this.add(w);
	}

	@Override
	public void add(IsWidget w) {
		if (w instanceof Nav) {
			this.updateNavStyle((Nav) w);
		}
		if (w instanceof Button || w instanceof ButtonGroup) {
			StyleUtils.addStyle((Widget) w, Navbar.STYLE_NAV_BUTTON);
		}
		this.collapseContainer.append(w);
	}

	public void setType(Type type) {
		this.type = type;
		StyleUtils.addStyle(this.getElement(), this.type);
	}

	public void setPosition(Position position) {
		this.position = position;
		StyleUtils.addStyle(this.getElement(), this.position);
	}

	public void setContainerType(ContainerType containerType) {
		this.containerType = containerType;
		StyleUtils.addStyle(this.contentContainer, containerType);
	}

	private void updateNavStyle(Nav nav) {
		StyleUtils.addStyle(nav, Navbar.STYLE_NAV);
		nav.setStyle(null);
	}
}
