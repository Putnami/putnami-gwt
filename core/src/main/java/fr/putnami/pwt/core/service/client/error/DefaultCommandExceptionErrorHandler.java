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

import fr.putnami.pwt.core.error.client.ErrorDisplayer.Severity;
import fr.putnami.pwt.core.error.client.ErrorManager;
import fr.putnami.pwt.core.service.shared.exception.CommandException;

public class DefaultCommandExceptionErrorHandler extends AbstractCommandExceptionErrorHandler {

	@Override
	protected boolean internalHandle(CommandException error) {
		if (ErrorManager.get().hasErrorDisplayer()) {
			ErrorManager.get().getErrorDisplayer().display(error, Severity.DANGER);
		}
		return true;
	}

	@Override
	public int getPriority() {
		return LOW_PRIORITY;
	}

}
