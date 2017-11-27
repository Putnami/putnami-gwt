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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.Nav.Style;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public class TabPanel extends AbstractPanel implements CloneableWidget {

	public static final CssStyle STYLE_TAB_CONTENT = new SimpleStyle("tab-content");

	private class TabNavigationCommand implements ScheduledCommand {

		private final TabPanelContent containerSource;

		TabNavigationCommand(TabPanelContent container) {
			this.containerSource = container;
		}

		@Override
		public void execute() {
			TabPanel.this.setActivePane(this.containerSource);
		}
	}

	private final Container tabPaneContainer = new Container();
	private final Nav tabContainer = new Nav();
	private final List<TabPanelContent> tabPaneList = Lists.newArrayList();

	public TabPanel() {
		super(DivElement.TAG);
		this.endConstruct();
		this.setStyle(Style.TABS);
	}

	protected TabPanel(TabPanel source) {
		super(source);
		this.endConstruct();
		for (TabPanelContent content : source.tabPaneList) {
			this.addTabPanelContent(WidgetUtils.cloneWidget(content));
		}
		this.setStyle(source.tabContainer.getStyle());
	}

	private void endConstruct() {
		this.append(this.tabContainer);
		this.append(this.tabPaneContainer);
		StyleUtils.addStyle(this.tabPaneContainer, TabPanel.STYLE_TAB_CONTENT);
	}

	@Override
	public IsWidget cloneWidget() {
		return new TabPanel(this);
	}

	@Override
	public void add(IsWidget child) {
		if (child instanceof TabPanelContent) {
			this.addTabPanelContent((TabPanelContent) child);
		}
	}

	private void addTabPanelContent(TabPanelContent item) {
		NavLink link = item.getTabLink();
		assert link != null : "TabPanelContent have to contain an tabLink";
		this.tabPaneList.add(item);
		this.tabPaneContainer.append(item);
		this.tabContainer.addNavContent(item.getTabLink());
		link.setCommand(new TabNavigationCommand(item));
		link.setActive(item.isActive());
	}

	public void setActivePane(final TabPanelContent source) {
		for (TabPanelContent tabPane : this.tabPaneList) {
			if (!tabPane.equals(source)) {
				tabPane.setActive(false);
			}
		}
		source.getTabLink().setActive(true);
		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

			@Override
			public boolean execute() {
				source.setActive(true);
				return false;
			}
		}, 150);
	}

	public void setStyle(Style style) {
		this.tabContainer.setStyle(style);
	}

}
