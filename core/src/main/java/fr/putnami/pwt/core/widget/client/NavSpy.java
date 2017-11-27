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
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;
import java.util.Map;

import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractComposite;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class NavSpy extends AbstractComposite implements HasDrawable {

	private static final CssStyle STYLE_NAV_SPY = new SimpleStyle("nav-spy");

	private class NavWidget extends NavLink implements ScheduledCommand {
		private final NavWidget parentNav;
		private final int level;
		private Nav subNavContainer;

		NavWidget(NavWidget parentNav, int level) {
			this.parentNav = parentNav;
			this.level = level;
		}

		NavWidget(NavWidget parentNav, final Element heading) {
			super(heading.getInnerHTML(), new ScheduledCommand() {
				@Override
				public void execute() {
					int top = NavSpy.this.getElementTop(heading) - NavSpy.this.spyOffset;
					if (NavSpy.this.isBodyScrollWidget()) {
						Window.scrollTo(Document.get().getScrollLeft(), top);
					} else {
						NavSpy.this.scrollWidget.getElement().setScrollTop(top);
					}
				}
			});

			this.parentNav = parentNav;
			this.level = NavSpy.this.getLevel(heading);
		}

		private NavWidget addToSubNav(NavWidget subNav) {
			this.getSubNavContainer().append(subNav);
			return subNav;
		}

		public Nav getSubNavContainer() {
			if (this.subNavContainer == null) {
				this.subNavContainer = new Nav();
				this.append(this.subNavContainer);
			}
			return this.subNavContainer;
		}

		@Override
		public void execute() {
			// NoOp
		}
	}

	private final SimplePanel content = new SimplePanel();

	private final List<Element> headings = Lists.newArrayList();
	private final Map<Element, NavWidget> navs = Maps.newHashMap();

	private boolean refreshing = false;
	private HandlerRegistration scrollRegistration;
	private Widget scrollWidget;

	private int spyOffset;

	private Widget headingContainer;
	private String spyName;

	private final RepeatingCommand refreshCommand = new RepeatingCommand() {

		@Override
		public boolean execute() {
			NavSpy.this.refreshActive();
			return false;
		}
	};

	public NavSpy() {
		this.initWidget(this.content);
		StyleUtils.addStyle(this, NavSpy.STYLE_NAV_SPY);
	}

	protected NavSpy(NavSpy source) {
		super(source);
		this.initWidget(this.content);
	}

	@Override
	public IsWidget cloneWidget() {
		return new NavSpy(this);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		this.registerScrollHandler();
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		if (this.scrollRegistration != null) {
			this.scrollRegistration.removeHandler();
		}
	}

	public Widget getHeadingContainer() {
		return this.headingContainer;
	}

	public void setHeadingContainer(Widget target) {
		this.headingContainer = target;
	}

	public String getSpyName() {
		return this.spyName;
	}

	public void setSpyName(String spyName) {
		this.spyName = spyName;
	}

	public void setScrollWidget(IsWidget scrollWidget) {
		if (scrollWidget != null) {
			this.scrollWidget = scrollWidget.asWidget();
		} else {
			this.scrollWidget = null;
		}
		this.registerScrollHandler();
	}

	public void setSpyOffset(int spyOffset) {
		this.spyOffset = spyOffset;
	}

	@Override
	public void redraw() {
		if (this.headingContainer == null) {
			this.headingContainer = RootPanel.get();
		}
		this.headings.clear();
		this.collectHeadings(this.headingContainer.getElement(), this.headings);

		int lowestNavLevel = 6;
		this.navs.clear();
		this.content.clear();
		for (Element heading : this.headings) {
			int level = this.getLevel(heading);
			if (level < lowestNavLevel) {
				lowestNavLevel = level;
			}
		}

		NavWidget currentNav = new NavWidget(null, lowestNavLevel - 1);
		this.content.add(currentNav.getSubNavContainer());
		for (Element heading : this.headings) {
			int level = this.getLevel(heading);
			while (currentNav.level >= level && currentNav.parentNav != null) {
				currentNav = currentNav.parentNav;
			}
			if (currentNav.level < level - 1) {
				for (int i = currentNav.level; i < level; i++) {
					NavWidget newNav = new NavWidget(currentNav, i);
					currentNav.append(newNav);
					currentNav = newNav;
				}
			}

			currentNav = currentNav.addToSubNav(new NavWidget(currentNav, heading));
			this.navs.put(heading, currentNav);
		}
	}

	private void registerScrollHandler() {
		if (this.scrollRegistration != null) {
			this.scrollRegistration.removeHandler();
		}
		if (this.isBodyScrollWidget()) {
			this.scrollRegistration = Window.addWindowScrollHandler(new Window.ScrollHandler() {

				@Override
				public void onWindowScroll(Window.ScrollEvent event) {
					NavSpy.this.scheduleRefresh();
				}
			});
		} else {
			this.scrollRegistration = this.scrollWidget.addDomHandler(new ScrollHandler() {

				@Override
				public void onScroll(ScrollEvent event) {
					NavSpy.this.scheduleRefresh();
				}
			}, ScrollEvent.getType());
		}
	}

	private void scheduleRefresh() {
		if (!this.refreshing) {
			this.refreshing = true;
			Scheduler.get().scheduleFixedDelay(this.refreshCommand, 250);
		}
	}

	private int getElementTop(Element heading) {
		if (this.isBodyScrollWidget()) {
			return heading.getAbsoluteTop();
		}
		return heading.getOffsetTop() - this.scrollWidget.getElement().getOffsetTop();
	}

	private boolean isBodyScrollWidget() {
		return this.scrollWidget == null;
	}

	private void refreshActive() {
		if (this.navs.isEmpty()) {
			// Not displayed NavSpy
			return;
		}
		int scrollTop, scrollHeight, maxScroll;

		if (this.isBodyScrollWidget()) {
			scrollTop = Document.get().getScrollTop() + this.spyOffset;
			scrollHeight = Document.get().getScrollHeight();
			maxScroll = scrollHeight - Document.get().getClientHeight();
		} else {
			scrollTop = this.scrollWidget.getElement().getScrollTop() + this.spyOffset;
			scrollHeight = this.scrollWidget.getElement().getScrollHeight();
			maxScroll = scrollHeight - this.scrollWidget.getElement().getClientHeight();
		}

		Element activeHeading = null;
		if (scrollTop >= maxScroll && !this.headings.isEmpty()) {
			activeHeading = this.headings.get(this.headings.size() - 1);
		} else {
			for (Element heading : this.headings) {
				int top = this.getElementTop(heading) - scrollTop;
				if (activeHeading == null || top <= 0) {
					activeHeading = heading;
				}
				if (top > 0) {
					break;
				}
			}
		}

		for (NavWidget nav : this.navs.values()) {
			nav.setActive(false);
		}
		if (activeHeading != null) {
			NavWidget navActive = this.navs.get(activeHeading);
			navActive.setActive(true);
			while (navActive.parentNav != null) {
				navActive = navActive.parentNav;
				navActive.setActive(true);
			}
		}
		this.refreshing = false;
	}

	private int getLevel(Element element) {
		return Heading.HEADING_TAGS.indexOf(element.getTagName().toLowerCase()) + 1;
	}

	private void collectHeadings(Element element, List<Element> headings) {
		for (int i = 0; i < element.getChildCount(); i++) {
			Node node = element.getChild(i);
			if (node instanceof Element) {
				Element child = (Element) node;
				String tagName = child.getTagName();
				if (tagName != null && Heading.HEADING_TAGS.contains(tagName.toLowerCase())) {
					if (this.spyName != null && this.spyName.equals(child.getAttribute(Heading.ATTRIBUTE_DATA_SUMMARY))) {
						headings.add(child);
					}
				} else {
					this.collectHeadings(child, headings);
				}
			}
		}
	}

}
