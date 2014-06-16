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
package fr.putnami.pwt.core.service.client.error;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.StatusCodeException;

import fr.putnami.pwt.core.error.client.ErrorDisplayer.Severity;
import fr.putnami.pwt.core.error.client.ErrorManager;
import fr.putnami.pwt.core.service.client.error.constant.ServerErrorConstants;

public class ServerErrorHandler extends AbstractStatusCodeErrorHandler {

	private static final String SERVER_ERROR_PREFIX = "serverError";

	public ServerErrorHandler() {
		super((ServerErrorConstants) GWT.create(ServerErrorConstants.class), SERVER_ERROR_PREFIX);
	}

	@Override
	protected boolean handlesCode(int statusCode) {
		return (statusCode >= 500 && statusCode < 600);
	}

	@Override
	protected boolean internalHandle(StatusCodeException error) {
		if (ErrorManager.get().hasErrorDisplayer()) {
			String title = ((ServerErrorConstants) getConstants()).serverErrorTitle();
			ErrorManager.get().getErrorDisplayer().display(title, getErrorMessage(error), error, Severity.DANGER);
		}
		return true;
	}
}
