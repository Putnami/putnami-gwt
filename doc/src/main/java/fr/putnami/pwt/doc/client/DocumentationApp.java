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
package fr.putnami.pwt.doc.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import fr.putnami.pwt.core.error.client.ErrorManager;
import fr.putnami.pwt.core.error.client.widget.SimpleErrorDisplayer;
import fr.putnami.pwt.core.mvp.client.MvpController;
import fr.putnami.pwt.core.theme.client.CssLink;
import fr.putnami.pwt.core.theme.client.Theme;
import fr.putnami.pwt.core.theme.client.ThemeController;
import fr.putnami.pwt.doc.client.application.ApplicationConfig;
import fr.putnami.pwt.doc.client.application.DocumentationDisplay;
import fr.putnami.pwt.doc.client.application.error.ErrorConstants;
import fr.putnami.pwt.doc.client.application.error.UmbrellaExceptionHandler;
import fr.putnami.pwt.doc.client.page.binding.DataBindingPlace;
import fr.putnami.pwt.doc.client.page.bootstrap.BootstrapPlace;
import fr.putnami.pwt.doc.client.page.components.ComponentsPlace;
import fr.putnami.pwt.doc.client.page.download.DownloadPlace;
import fr.putnami.pwt.doc.client.page.errors.ErrorsPlace;
import fr.putnami.pwt.doc.client.page.form.FormsPlace;
import fr.putnami.pwt.doc.client.page.i18n.InternationalizationPlace;
import fr.putnami.pwt.doc.client.page.layout.LayoutsPlace;
import fr.putnami.pwt.doc.client.page.more.MorePlace;
import fr.putnami.pwt.doc.client.page.navigation.NavigationPlace;
import fr.putnami.pwt.doc.client.page.plugins.CodeEditorPlace;
import fr.putnami.pwt.doc.client.page.sample.addressbook.AddressBookPlace;
import fr.putnami.pwt.doc.client.page.sample.all.SamplesPlace;
import fr.putnami.pwt.doc.client.page.sample.table.ContactsTablePlace;
import fr.putnami.pwt.doc.client.page.server.ServerCallsPlace;
import fr.putnami.pwt.doc.client.page.soon.CommingSoonPlace;
import fr.putnami.pwt.doc.client.page.starting.GettingStartedPlace;
import fr.putnami.pwt.doc.client.page.table.TablesPlace;
import fr.putnami.pwt.doc.client.page.welcome.WelcomePlace;
import fr.putnami.pwt.plugin.ga.client.GoogleAnalytics;

public class DocumentationApp implements EntryPoint {

	@Override
	public void onModuleLoad() {
		if (redirectIfNotIndexPage()) {
			return;
		}
		RootPanel staticContent = RootPanel.get("pwt-static-content");
		if (staticContent != null && staticContent.getElement() != null) {
			staticContent.getElement().removeFromParent();
		}

		Theme theme = new Theme();
		theme.addLink(new CssLink("theme/doc/style/pwt-doc.css", 0));
		ThemeController.get().installTheme(theme);

		DocumentationDisplay display = new DocumentationDisplay();
		RootPanel.get().add(display);

		GoogleAnalytics.get(ApplicationConfig.ANALYTICS_TRACKER_ID).forceSSL(true);

		SimpleErrorDisplayer errorDisplayer = new SimpleErrorDisplayer();
		errorDisplayer.setConstants((ConstantsWithLookup) GWT.create(ErrorConstants.class));
		ErrorManager.get().setErrorDisplayer(errorDisplayer);
		ErrorManager.get().registerErrorHandler(new UmbrellaExceptionHandler());

		MvpController controller = MvpController.get();
		controller.setDisplay(display);

		controller.setDefaultPlace(WelcomePlace.INSTANCE);

		controller.registerActivity(WelcomePlace.INSTANCE);
		controller.registerActivity(GettingStartedPlace.INSTANCE);
		controller.registerActivity(BootstrapPlace.INSTANCE);
		controller.registerActivity(LayoutsPlace.INSTANCE);
		controller.registerActivity(ComponentsPlace.INSTANCE);
		controller.registerActivity(FormsPlace.INSTANCE);
		controller.registerActivity(TablesPlace.INSTANCE);
		controller.registerActivity(MorePlace.INSTANCE);
		controller.registerActivity(DataBindingPlace.INSTANCE);
		controller.registerActivity(InternationalizationPlace.INSTANCE);
		controller.registerActivity(NavigationPlace.INSTANCE);
		controller.registerActivity(ServerCallsPlace.INSTANCE);
		controller.registerActivity(ErrorsPlace.INSTANCE);
		controller.registerActivity(CodeEditorPlace.INSTANCE);
		controller.registerActivity(SamplesPlace.INSTANCE);
		controller.registerActivity(ContactsTablePlace.INSTANCE);
		controller.registerActivity(AddressBookPlace.INSTANCE);
		controller.registerActivity(CommingSoonPlace.INSTANCE);
		controller.registerActivity(DownloadPlace.INSTANCE);

		controller.handleCurrentHistory();
	}

	private boolean redirectIfNotIndexPage() {
		String params = "";
		String pageUrl = Window.Location.getHref();
		if (pageUrl.contains("?")) {
			params = Window.Location.getQueryString();
			pageUrl = pageUrl.replace(params, "");
		}
		if (pageUrl.endsWith(".html")) {
			String newUrl = GWT.getHostPageBaseURL().substring(0, GWT.getHostPageBaseURL().lastIndexOf("/"));
			Window.Location.replace(newUrl + params);
			return true;
		}
		return false;
	}
}
