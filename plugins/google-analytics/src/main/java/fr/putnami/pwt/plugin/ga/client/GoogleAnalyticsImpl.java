/**
 * This file is part of pwt-google-analytics.
 *
 * pwt-google-analytics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt-google-analytics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt-google-analytics.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.plugin.ga.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ScriptElement;

import fr.putnami.pwt.core.error.client.AbstractErrorHandler;
import fr.putnami.pwt.core.error.client.ErrorHandler;
import fr.putnami.pwt.core.error.client.ErrorManager;
import fr.putnami.pwt.core.mvp.client.MvpController;
import fr.putnami.pwt.core.mvp.client.event.StartActivityEvent;

public class GoogleAnalyticsImpl extends GoogleAnalytics implements StartActivityEvent.Handler {

	private static final String SCRIPT_URL = "//www.google-analytics.com/analytics.js";

	private static boolean isInit = false;
	private ErrorHandler errorHandler;

	private void loadAnalyticsScript() {
		ScriptElement script = Document.get().createScriptElement();
		script.setSrc(SCRIPT_URL);
		script.setType("text/javascript");
		script.setAttribute("async", "true");

		Element firstScript = Document.get().getElementsByTagName("script").getItem(0);
		firstScript.getParentNode().insertBefore(script, firstScript);
	}

	private void initScript() {
		if (isInit) {
			return;
		}
		isInit = true;
		createGaObject();
		loadAnalyticsScript();
	}

	@Override
	protected void initialize(String account) {
		initialize(account, "auto");
	}

	@Override
	protected void initialize(String account, String domain) {
		initScript();
		MvpController.get().addStartActivityHandler(this);
		createTracker(account, domain);
	}

	@Override
	public void onStartActivity(StartActivityEvent event) {
		String placeToken = MvpController.get().getToken(event.getPlace());
		trackPage("/#" + placeToken);
	}

	@Override
	public void handleUncaughtException(boolean enable) {
		if (enable) {
			if (errorHandler == null) {
				errorHandler = new AbstractErrorHandler() {
					@Override
					public boolean handle(Throwable error) {
						trackException(error.getMessage(), false);
						return false;
					}
				};
			}
			ErrorManager.get().registerErrorHandler(errorHandler);
		}
		else {
			ErrorManager.get().removeErrorHandler(errorHandler);
		}
	}

	private native void createGaObject()
	/*-{
	  	// store the name of the Analytics object
		$wnd['GoogleAnalyticsObject'] = 'ga';

		//Init the queu function
		$wnd.ga = $wnd.ga || function(){
			($wnd.ga.q = $wnd.ga.q || []).push(arguments)
		}
		//Init the time
		$wnd.ga.l = 1 * new Date();
	}-*/;

	private native void createTracker(String account, String domain)
	/*-{
		$wnd.ga('create', account, domain);
	}-*/;

	@Override
	public native void forceSSL(boolean force)
	/*-{
	    $wnd.ga('set', 'forceSSL', force);
	}-*/;

	@Override
	public native void displayfeatures()
	/*-{
	    $wnd.ga('require', 'displayfeatures');
	}-*/;

	@Override
	public native void trackPage()
	/*-{
	    $wnd.ga('send', 'pageview');
	}-*/;

	@Override
	public native void trackPage(String pageName)
	/*-{
	    $wnd.ga('send', 'pageview', pageName);
	}-*/;

	@Override
	public native void trackEvent(String category, String action)
	/*-{
	   $wnd.ga('send', 'event', category, action);
	}-*/;

	@Override
	public native void trackEvent(String category, String action, String label)
	/*-{
	   $wnd.ga('send', 'event', category, action, label);
	}-*/;

	@Override
	public native void trackEvent(String category, String action, String label, int value)
	/*-{
	   $wnd.ga('send', 'event', category, action, label, value);
	}-*/;


	@Override
	public native void trackException(String description, boolean fatal)
	/*-{
   	$wnd.ga('send', 'exception', {
   		'exDescription': description,
  			'exFatal': fatal
   	});
	}-*/;

	@Override
	public native void trackSocial(String socialNetwork, String socialAction, String socialtarget)
	/*-{
		$wnd.ga('send', 'social', socialNetwork, socialAction, socialtarget, {
			'page': page
		});
	}-*/;

}
