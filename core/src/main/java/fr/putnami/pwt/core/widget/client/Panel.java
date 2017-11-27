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

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.List;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.HasFooter;
import fr.putnami.pwt.core.widget.client.base.HasHeader;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent.Handler;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent.HasCollapseHandlers;
import fr.putnami.pwt.core.widget.client.helper.CollapseHelper;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public class Panel extends AbstractPanel implements CloneableWidget, HasOneWidget, HasDrawable, HasCollapseHandlers {

	private static final CssStyle STYLE_PANEL = new SimpleStyle("panel");
	private static final CssStyle STYLE_BODY = new SimpleStyle("panel-body");
	private static final CssStyle STYLE_HEADING = new SimpleStyle("panel-heading");
	private static final CssStyle STYLE_COMMANDS = new SimpleStyle("panel-commands");
	private static final CssStyle STYLE_FOOTER = new SimpleStyle("panel-footer");

	public enum Color implements CssStyle {
			DEFAULT("panel-default"),
			PRIMARY("panel-primary"),
			SUCCESS("panel-success"),
			INFO("panel-info"),
			WARNING("panel-warning"),
			DANGER("panel-danger");

		private final String style;

		Color(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private final List<IsWidget> commands = Lists.newArrayList();

	private final Container collapsePanel = new Container();
	private OneWidgetPanel bodyPanel;
	private Table<?> table;

	private Container commandsContainer;
	private Header header;
	private Footer footer;

	private Color color = Color.DEFAULT;

	private boolean collapsible;
	private Boolean collapse;
	private CollapseHelper collapseHelper;

	public Panel() {
		super(DivElement.TAG);
		this.endConstruct();
	}

	protected Panel(Panel source) {
		super(source);

		this.collapsible = source.collapsible;
		this.collapse = source.collapse;
		this.color = source.color;

		this.setHeader(WidgetUtils.cloneWidget(source.header));
		this.setFooter(WidgetUtils.cloneWidget(source.footer));
		this.setWidget(WidgetUtils.cloneWidget(source.getWidget()));

		for (IsWidget command : source.commands) {
			if (command instanceof CloneableWidget) {
				this.commands.add(((CloneableWidget) command).cloneWidget());
			}
		}

		this.endConstruct();
	}

	private void endConstruct() {
		StyleUtils.addStyle(this, Panel.STYLE_PANEL);
		StyleUtils.addStyle(this, this.color);

		this.redraw();
	}

	@Override
	public IsWidget cloneWidget() {
		return new Panel(this);
	}

	@Override
	public void add(IsWidget w) {
		if (w instanceof Header) {
			this.setHeader((Header) w);
		} else if (w instanceof Footer) {
			this.setFooter((Footer) w);
		} else {
			if (w instanceof HasHeader) {
				this.setHeader(((HasHeader) w).getHeader());
			}
			if (w instanceof HasFooter) {
				this.setFooter(((HasFooter) w).getFooter());
			}
			this.setWidget(w);
		}
	}

	@UiChild(tagname = "command")
	public void addCommand(IsWidget w) {
		this.commands.add(w);
	}

	@Override
	public Widget getWidget() {
		if (this.bodyPanel == null) {
			return null;
		}
		return this.bodyPanel.getWidget();
	}

	@Override
	public void setWidget(Widget w) {
		this.setWidget((IsWidget) w);
	}

	@Override
	public void setWidget(IsWidget w) {
		if (w instanceof Table) {
			this.table = (Table<?>) w;
			this.redraw();
		} else {
			if (this.bodyPanel == null) {
				this.bodyPanel = new OneWidgetPanel();
				StyleUtils.addStyle(this.bodyPanel, Panel.STYLE_BODY);
				this.redraw();
			}
			this.bodyPanel.setWidget(w);
		}
	}

	public Header getHeader() {
		return this.header;
	}

	public void setHeader(Header header) {
		if (header == null) {
			return;
		}
		assert this.header == null : "heading may only be set once";
		this.header = header;
		StyleUtils.addStyle(header, Panel.STYLE_HEADING);
		this.redraw();
	}

	public Footer getFooter() {
		return this.footer;
	}

	public void setFooter(Footer footer) {
		if (footer == null) {
			return;
		}
		assert this.footer == null : "footer may only be set once";
		this.footer = footer;
		StyleUtils.addStyle(footer, Panel.STYLE_FOOTER);
		this.redraw();
	}

	public Color getColor() {
		return this.color;
	}

	public void setColor(Color color) {
		this.color = color;
		StyleUtils.addStyle(this.getElement(), this.color);
	}

	public void setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
		if (this.collapseHelper != null) {
			if (collapsible) {
				this.collapseHelper.enable();
			} else {
				this.collapseHelper.disable();
			}
		}
	}

	public Boolean getCollapse() {
		return this.collapse;
	}

	public void setCollapse(boolean collapse) {
		this.collapse = collapse;
		if (this.collapseHelper != null && this.collapseHelper.isEnabled()) {
			this.collapseHelper.doCollapse(collapse);
		}
	}

	public void toggleCollapse() {
		if (this.collapseHelper != null && this.collapseHelper.isEnabled()) {
			this.collapseHelper.toggleCollapse();
		}
	}

	@Override
	public HandlerRegistration addCollapseHandler(Handler handler) {
		return EventBus.get().addHandlerToSource(CollapseEvent.TYPE, this, handler);
	}

	@Override
	public void redraw() {
		this.clear();

		this.append(this.header);
		this.append(this.collapsePanel);

		this.collapsePanel.clear();
		this.collapsePanel.append(this.bodyPanel);
		this.collapsePanel.append(this.table);
		this.collapsePanel.append(this.footer);

		if (this.header != null && !this.commands.isEmpty()) {
			if (this.commandsContainer == null) {
				this.commandsContainer = new Container();
				StyleUtils.addStyle(this.commandsContainer, Panel.STYLE_COMMANDS);
				this.header.add(this.commandsContainer);
			}
			for (IsWidget command : this.commands) {
				this.commandsContainer.add(command);
			}
		}
		this.ensureCollapseHelper();
	}

	public CollapseHelper ensureCollapseHelper() {
		if (this.header != null && this.collapseHelper == null) {
			boolean initialCollapse = this.collapse != null ? this.collapse : false;
			this.collapseHelper = CollapseHelper.apply(this.header, this.collapsePanel.getElement(), initialCollapse);
			this.setCollapsible(this.collapsible);
			if (this.collapse != null) {
				this.setCollapse(this.collapse);
			}
		}
		return this.collapseHelper;
	}
}
