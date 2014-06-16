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
package fr.putnami.pwt.core.error.client.widget;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

import fr.putnami.pwt.core.widget.client.Alert;
import fr.putnami.pwt.core.widget.client.Modal;
import fr.putnami.pwt.core.widget.client.binder.UiBinderLocalized;
import fr.putnami.pwt.core.widget.client.event.AlertDismissEvent;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent;

public class ErrorDisplay implements AlertDismissEvent.Handler {

	interface Binder extends UiBinderLocalized<Modal, ErrorDisplay> {
		Binder BINDER = GWT.create(Binder.class);
	}

	interface Constants extends ConstantsWithLookup {

		@DefaultStringValue("Ooops an error occured !")
		String errorDialogTitle();

		@DefaultStringValue("Continue")
		String continueButton();

		@DefaultStringValue("Reload")
		String reloadButton();
	}

	private List<Alert> displayedErrors = Lists.newArrayList();

	@UiField(provided = true)
	Constants constants = GWT.create(Constants.class);

	@UiField
	Modal modal;

	@UiField
	FlowPanel modalContent;

	private boolean showing;

	public ErrorDisplay() {
		Binder.BINDER.createAndBindUi(this);
	}

	@Override
	public void onAlertDismiss(AlertDismissEvent event) {
		displayedErrors.remove(event.getSource());
		if (displayedErrors.isEmpty()) {
			this.hide();
		}
	}

	public void addErrorAlert(ErrorAlert errorAlert) {
		Alert alert = errorAlert.getAlert();
		alert.addAlertDismissHandler(this);
		displayedErrors.add(alert);
		modalContent.add(alert);
		show();
	}

	@UiHandler("reloadButton")
	void onReload(ButtonEvent event) {
		Window.Location.reload();
	}

	@UiHandler("continueButton")
	void onContinue(ButtonEvent event) {
		hide();
	}

	public void hide() {
		modal.hide();
		modalContent.clear();
		displayedErrors.clear();
		showing = false;
	}

	public void show() {
		if (!showing) {
			RootPanel.get().add(modal);
			modal.show();
		}
		showing = true;
	}

}
