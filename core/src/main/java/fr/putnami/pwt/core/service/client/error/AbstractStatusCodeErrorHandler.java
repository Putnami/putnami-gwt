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

import java.util.MissingResourceException;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.user.client.rpc.StatusCodeException;

import fr.putnami.pwt.core.error.client.AbstractErrorHandler;

public abstract class AbstractStatusCodeErrorHandler extends AbstractErrorHandler {

	private final ConstantsWithLookup constants;
	private final String constantsPrefix;

	public AbstractStatusCodeErrorHandler(ConstantsWithLookup constants, String constantsPrefix) {
		this.constants = constants;
		this.constantsPrefix = constantsPrefix;
	}

	@Override
	public boolean handle(Throwable error) {
		if (!(error instanceof StatusCodeException)) {
			return false;
		}
		StatusCodeException statusCodeExc = (StatusCodeException) error;
		if (handlesCode(statusCodeExc.getStatusCode())) {
			return internalHandle(statusCodeExc);
		}
		return false;
	}

	protected String getErrorMessage(StatusCodeException exception) {
		try {
			return constants.getString(constantsPrefix + exception.getStatusCode());
		}
		catch (MissingResourceException exc) {
			return exception.getStatusText();
		}
	}

	protected ConstantsWithLookup getConstants() {
		return constants;
	}

	protected abstract boolean handlesCode(int statusCode);

	protected abstract boolean internalHandle(StatusCodeException error);

}
