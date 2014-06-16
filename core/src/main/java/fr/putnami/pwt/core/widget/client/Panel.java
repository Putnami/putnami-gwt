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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.HasFooter;
import fr.putnami.pwt.core.widget.client.base.HasHeader;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent.Handler;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent.HasCollapseHandlers;
import fr.putnami.pwt.core.widget.client.helper.CollapseHelper;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public class Panel extends AbstractPanel implements
		CloneableWidget,
		HasOneWidget,
		HasDrawable,
		HasCollapseHandlers {

	private static final CssStyle STYLE_PANEL = new SimpleStyle("panel");
	private static final CssStyle STYLE_BODY = new SimpleStyle("panel-body");
	private static final CssStyle STYLE_HEADING = new SimpleStyle("panel-heading");
	private static final CssStyle STYLE_FOOTER = new SimpleStyle("panel-footer");

	public enum Color implements CssStyle {
		DEFAULT("panel-default"),
		PRIMARY("panel-primary"),
		SUCCESS("panel-success"),
		INFO("panel-info"),
		WARNING("panel-warning"),
		DANGER("panel-danger");

		private final String style;

		private Color(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return style;
		}
	}

	private final Container collapsePanel = new Container();
	private OneWidgetPanel bodyPanel;

	private Header header;
	private Footer footer;

	private Color color = Color.DEFAULT;

	private boolean collapsible;
	private Boolean collapse;
	private CollapseHelper collapseHelper;

	public Panel() {
		super(DivElement.TAG);
		endConstruct();
	}

	protected Panel(Panel source) {
		super(source);

		this.collapsible = source.collapsible;
		this.collapse = source.collapse;
		this.color = source.color;

		setHeader(WidgetUtils.cloneWidget(source.header));
		setFooter(WidgetUtils.cloneWidget(source.footer));
		setWidget(WidgetUtils.cloneWidget(source.getWidget()));

		endConstruct();
	}

	private void endConstruct() {
		StyleUtils.addStyle(this, STYLE_PANEL);
		StyleUtils.addStyle(this, color);

		redraw();
	}

	@Override
	public IsWidget cloneWidget() {
		return new Panel(this);
	}

	@Override
	public void add(IsWidget w) {
		if (w instanceof Header) {
			setHeader((Header) w);
		}
		else if (w instanceof Footer) {
			setFooter((Footer) w);
		}
		else {
			if (w instanceof HasHeader) {
				setHeader(((HasHeader) w).getHeader());
			}
			if (w instanceof HasFooter) {
				setFooter(((HasFooter) w).getFooter());
			}
			setWidget(w);
		}
	}

	@Override
	public Widget getWidget() {
		if (bodyPanel == null) {
			return null;
		}
		return bodyPanel.getWidget();
	}

	@Override
	public void setWidget(Widget w) {
		setWidget((IsWidget) w);
	}

	@Override
	public void setWidget(IsWidget w) {
		if (bodyPanel == null) {
			bodyPanel = new OneWidgetPanel();
			StyleUtils.addStyle(bodyPanel, STYLE_BODY);
			redraw();
		}
		bodyPanel.setWidget(w);
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		if (header == null) {
			return;
		}
		assert (this.header == null) : "heading may only be set once";
		this.header = header;
		StyleUtils.addStyle(header, STYLE_HEADING);
		redraw();
	}

	public Footer getFooter() {
		return footer;
	}

	public void setFooter(Footer footer) {
		if (footer == null) {
			return;
		}
		assert (this.footer == null) : "footer may only be set once";
		this.footer = footer;
		StyleUtils.addStyle(footer, STYLE_FOOTER);
		redraw();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		StyleUtils.addStyle(getElement(), this.color);
	}

	public void setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
		if (collapseHelper != null) {
			if (collapsible) {
				this.collapseHelper.enable();
			}
			else {
				this.collapseHelper.disable();
			}
		}
	}

	public Boolean getCollapse() {
		return collapse;
	}

	public void setCollapse(boolean collapse) {
		this.collapse = collapse;
		if (collapseHelper != null && collapseHelper.isEnabled()) {
			collapseHelper.doCollapse(collapse);
		}
	}

	public void toggleCollapse() {
		if (collapseHelper != null && collapseHelper.isEnabled()) {
			collapseHelper.toggleCollapse();
		}
	}

	@Override
	public HandlerRegistration addCollapseHandler(Handler handler) {
		return EventBus.get().addHandlerToSource(CollapseEvent.TYPE, this, handler);
	}

	@Override
	public void redraw() {
		clear();

		append(header);
		append(collapsePanel);

		collapsePanel.clear();
		collapsePanel.append(bodyPanel);
		collapsePanel.append(footer);

		ensureCollapseHelper();
	}

	public CollapseHelper ensureCollapseHelper() {
		if (header != null && collapseHelper == null) {
			boolean initialCollapse = collapse != null ? collapse : false;
			collapseHelper = CollapseHelper.apply(header, collapsePanel.getElement(), initialCollapse);
			setCollapsible(collapsible);
			if (collapse != null) {
				setCollapse(collapse);
			}
		}
		return collapseHelper;
	}
}
