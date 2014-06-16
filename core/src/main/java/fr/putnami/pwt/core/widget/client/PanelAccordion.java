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

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent;
import fr.putnami.pwt.core.widget.client.helper.CollapseHelper;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class PanelAccordion extends AbstractPanel implements CloneableWidget {

	private static final CssStyle STYLE_PANEL_GROUP = new SimpleStyle("panel-group");

	private final CollapseEvent.Handler openPanelHandler = new CollapseEvent.Handler() {
		@Override
		public void onCollapse(CollapseEvent event) {
			if (!event.isCollapsed()) {
				for (CollapseHelper helper : collapseHelpers) {
					if (helper != event.getSource()) {
						helper.collapse();
					}
				}
			}
		}
	};

	private final List<CollapseHelper> collapseHelpers = Lists.newArrayList();
	private final HandlerRegistrationCollection registrations = new HandlerRegistrationCollection();

	public PanelAccordion() {
		super(DivElement.TAG);
		StyleUtils.addStyle(this, STYLE_PANEL_GROUP);
	}

	protected PanelAccordion(PanelAccordion source) {
		super(source);
		cloneSourceWidgets(source);
		for (Widget child : getChildren()) {
			if (child instanceof Panel) {
				registerCollapseHelper((Panel) child);
			}
		}
	}

	@Override
	public IsWidget cloneWidget() {
		return new PanelAccordion(this);
	}

	private void registerCollapseHelper(Panel panel) {
		CollapseHelper helper = panel.ensureCollapseHelper();
		if (helper != null) {
			collapseHelpers.add(helper);
			panel.setCollapsible(true);
			if (panel.getCollapse() == null) {
				panel.setCollapse(true);
			}
			registrations.add(helper.addCollapseHandler(openPanelHandler));
		}
	}

	public void addPanel(Panel childPanel) {
		assert childPanel.getHeader() != null : "Only Panel with Header can be append to a PanelAccordion";
		registerCollapseHelper(childPanel);
		append(childPanel);
	}

	@Override
	public void add(IsWidget child) {
		if (child instanceof Panel) {
			addPanel((Panel) child);
		}
	}
}
