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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

import fr.putnami.pwt.core.error.client.ErrorDisplayer.Severity;
import fr.putnami.pwt.core.widget.client.Alert;
import fr.putnami.pwt.core.widget.client.Alert.Type;
import fr.putnami.pwt.core.widget.client.Button;
import fr.putnami.pwt.core.widget.client.binder.UiBinderLocalized;
import fr.putnami.pwt.core.widget.client.constant.IconType;
import fr.putnami.pwt.core.widget.client.event.CollapseEvent;
import fr.putnami.pwt.core.widget.client.helper.CollapseHelper;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class ErrorAlert implements CollapseEvent.Handler {

	interface Binder extends UiBinderLocalized<Alert, ErrorAlert> {
		Binder BINDER = GWT.create(Binder.class);
	}

	interface Constants extends ConstantsWithLookup {

		@DefaultStringValue("Detail")
		String detailButton();
	}

	@UiField(provided = true)
	Constants constants = GWT.create(Constants.class);

	@UiField
	Alert alert;

	@UiField
	Element titleOutput;
	@UiField
	Label messageOutput;
	@UiField
	PreElement detailOutput;
	@UiField
	Button<?> detailButton;

	private CollapseHelper collapseHelper;

	public ErrorAlert(String title, String message, String errorDetails, Severity severity) {
		Binder.BINDER.createAndBindUi(this);
		StyleUtils.addStyle(detailButton, Alert.STYLE_ALERT_LINK);
		titleOutput.setInnerSafeHtml(SafeHtmlUtils.fromString(title));
		messageOutput.setText(message);
		detailOutput.setInnerSafeHtml(SafeHtmlUtils.fromString(errorDetails));
		alert.setType(getAlertTypeFromSeverity(severity));
		collapseHelper = CollapseHelper.apply(detailButton, detailOutput, true);
		collapseHelper.addCollapseHandler(this);
	}

	private Type getAlertTypeFromSeverity(Severity severity) {
		Type result = Type.DANGER;
		if (severity != null) {
			result = Type.valueOf(severity.toString());
		}
		return result;
	}

	@Override
	public void onCollapse(CollapseEvent event) {
		if (event.isCollapsed()) {
			detailButton.setIconType(IconType.ANGLE_DOUBLE_RIGHT);
		}
		else {
			detailButton.setIconType(IconType.ANGLE_DOUBLE_DOWN);
		}
	}

	public Alert getAlert() {
		return alert;
	}
}
