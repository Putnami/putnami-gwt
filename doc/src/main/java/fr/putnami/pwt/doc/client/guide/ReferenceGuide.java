/**
 * This file is part of pwt-doc.
 *
 * pwt-doc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt-doc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt-doc.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.doc.client.guide;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.pwt.core.error.client.ErrorManager;
import fr.putnami.pwt.core.error.client.widget.SimpleErrorDisplayer;
import fr.putnami.pwt.core.theme.client.CssLink;
import fr.putnami.pwt.core.theme.client.Theme;
import fr.putnami.pwt.core.theme.client.ThemeController;
import fr.putnami.pwt.core.widget.client.GridColumn;
import fr.putnami.pwt.core.widget.client.Heading;
import fr.putnami.pwt.core.widget.client.NavSpy;
import fr.putnami.pwt.core.widget.client.binder.UiBinderLocalized;
import fr.putnami.pwt.doc.client.application.ApplicationConfig;
import fr.putnami.pwt.doc.client.application.HasTableOfContent;
import fr.putnami.pwt.doc.client.application.Page;
import fr.putnami.pwt.doc.client.application.error.ErrorConstants;
import fr.putnami.pwt.doc.client.application.error.UmbrellaExceptionHandler;
import fr.putnami.pwt.doc.client.page.binding.DataBindingView;
import fr.putnami.pwt.doc.client.page.bootstrap.BootstrapView;
import fr.putnami.pwt.doc.client.page.components.ComponentsView;
import fr.putnami.pwt.doc.client.page.errors.ErrorsView;
import fr.putnami.pwt.doc.client.page.form.FormView;
import fr.putnami.pwt.doc.client.page.i18n.InternationalizationView;
import fr.putnami.pwt.doc.client.page.layout.LayoutView;
import fr.putnami.pwt.doc.client.page.more.MoreView;
import fr.putnami.pwt.doc.client.page.navigation.NavigationView;
import fr.putnami.pwt.doc.client.page.plugins.CodeEditorView;
import fr.putnami.pwt.doc.client.page.server.ServerCallsView;
import fr.putnami.pwt.doc.client.page.soon.CommingSoonView;
import fr.putnami.pwt.doc.client.page.starting.GettingStartedView;
import fr.putnami.pwt.doc.client.page.table.TablesView;
import fr.putnami.pwt.doc.client.page.welcome.WelcomeView;
import fr.putnami.pwt.plugin.ga.client.GoogleAnalytics;

public class ReferenceGuide extends Composite implements EntryPoint {
	private final Logger LOGGER = Logger.getLogger(ReferenceGuide.class.getSimpleName());

	interface Binder extends UiBinderLocalized<Widget, ReferenceGuide> {
		Binder BINDER = GWT.create(Binder.class);
	}

	class Chapter {
		Chapter parent;
		List<Chapter> children;
		int level = 0;
		Heading heading;

		public Chapter(int offset, Heading heading) {
			super();
			this.level = offset;
			if (heading != null) {
				level += heading.getLevel();
			}
			this.heading = heading;
		}

		void addChildren(Chapter child) {
			if (children == null) {
				children = Lists.newArrayList();
			}
			child.parent = this;
			children.add(child);
		}

		boolean isDeeper(Chapter other) {
			return other.level > level;
		}

		boolean isBrother(Chapter other) {
			return other.level == level;
		}

		void draw(StringBuffer sb) {
			if (heading != null) {
				String name = "";
				Chapter cursor = this;
				Chapter parent = this.parent;
				int deep = 0;
				while (parent != null) {
					deep++;
					int index = parent.children.indexOf(cursor) + 1;
					name = index + "." + name;
					cursor = parent;
					parent = cursor.parent;
				}
				name += " " + heading.getText();
				heading.setHTML("<a name=\"" + name + "\" ></a>" + name);

				if (deep > MAX_TABLE_LEVEL) {
					return;
				}
				sb.append("<li><a href=\"#").append(name).append("\">").append(name).append("</a>\n");
			}
			if (children != null) {
				sb.append("<ul>\n");
				for (Chapter chapter : children) {
					chapter.draw(sb);
				}
				sb.append("</ul>\n");

			}
			if (heading != null) {
				sb.append("</li>\n");
			}
		}
	}

	private static final int MAX_TABLE_LEVEL = 3;

	Chapter root = new Chapter(0, null);
	Chapter current = root;

	@UiField
	GridColumn tableOfContent;
	@UiField
	GridColumn contentContainer;

	public ReferenceGuide() {
		initWidget(Binder.BINDER.createAndBindUi(this));

		Theme theme = new Theme();
		theme.addLink(new CssLink("theme/doc/style/pwt-doc.css", 0));
		ThemeController.get().installTheme(theme);

		SimpleErrorDisplayer errorDisplayer = new SimpleErrorDisplayer();
		errorDisplayer.setConstants((ConstantsWithLookup) GWT.create(ErrorConstants.class));
		ErrorManager.get().setErrorDisplayer(errorDisplayer);
		ErrorManager.get().registerErrorHandler(new UmbrellaExceptionHandler());

		GoogleAnalytics.get(ApplicationConfig.ANALYTICS_TRACKER_ID).trackPage();

		addHeading("Putnami Web Toolkit", 1);
		addContent(new WelcomeView(), 0);
		addContent(new GettingStartedView(), 0);
		addHeading("Look and feel", 1);
		addContent(new BootstrapView(), 1);
		addContent(new LayoutView(), 1);
		addContent(new ComponentsView(), 1);
		addContent(new FormView(), 1);
		addContent(new TablesView(), 1);
		addContent(new MoreView(), 1);
		addHeading("Framework", 1);
		addContent(new NavigationView(), 1);
		addContent(new DataBindingView(), 1);
		addContent(new InternationalizationView(), 1);
		addContent(new ServerCallsView(), 1);
		addContent(new ErrorsView(), 1);
		addHeading("Plugins", 1);
		addContent(new CodeEditorView(), 1);
		addContent(new CommingSoonView(), 0);

		StringBuffer tableContentBuffer = new StringBuffer();
		root.draw(tableContentBuffer);
		tableOfContent.getElement().setInnerHTML(tableContentBuffer.toString());
	}

	private void addHeading(String title, int level) {
		Heading heading = new Heading(level);
		heading.setText(title);
		contentContainer.add(heading);
		addChapter(heading, 0);
	}

	private void addContent(Widget content, int offset) {

		NavSpy subNav = null;
		if (content instanceof HasTableOfContent) {
			subNav = ((HasTableOfContent) content).getTableOfContent();
		}
		if (subNav != null) {
			for (Heading heading : subNav.getHeadings()) {
				addChapter(heading, offset);
			}
		}
		if (content instanceof Page) {
			contentContainer.add(((Page) content).header);
			contentContainer.add(((Page) content).content);
		}
		else {
			contentContainer.add(content);
		}
	}

	private void addChapter(Heading heading, int offset) {
		int level = offset + heading.getLevel();
		Chapter chapter = new Chapter(offset, heading);

		if (current.isBrother(chapter)) {
			current.parent.addChildren(chapter);
			current = chapter;
		}
		else if (current.isDeeper(chapter)) {
			current.addChildren(chapter);
			current = chapter;
		}
		else {
			while (!current.isBrother(chapter)) {
				current = current.parent;
			}
			if (current.isBrother(chapter)) {
				current.parent.addChildren(chapter);
				current = chapter;
			}
		}

	}

	@Override
	public void onModuleLoad() {
		RootPanel.get().add(this);

	}
}
