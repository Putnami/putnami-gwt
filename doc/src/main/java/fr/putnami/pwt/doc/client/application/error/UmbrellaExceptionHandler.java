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
package fr.putnami.pwt.doc.client.application.error;

import com.google.gwt.event.shared.UmbrellaException;

import fr.putnami.pwt.core.error.client.AbstractErrorHandler;
import fr.putnami.pwt.core.error.client.ErrorDisplayer.Severity;
import fr.putnami.pwt.core.error.client.ErrorManager;

public class UmbrellaExceptionHandler extends AbstractErrorHandler {

	@Override
	public boolean handle(Throwable error) {
		if (ErrorManager.get().hasErrorDisplayer()) {
			displayThrowable(error);
		}
		return true;
	}

	private void displayThrowable(Throwable error) {
		if (error instanceof UmbrellaException) {
			for (Throwable cause : ((UmbrellaException) error).getCauses()) {
				displayThrowable(cause);
			}
		}
		else {
			ErrorManager.get().getErrorDisplayer().display(error, Severity.DANGER);
		}
	}
}
