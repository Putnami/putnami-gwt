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

import fr.putnami.pwt.core.error.client.AbstractErrorHandler;
import fr.putnami.pwt.core.service.shared.exception.CommandException;

public abstract class AbstractCommandExceptionErrorHandler extends AbstractErrorHandler {

	@Override
	public boolean handle(Throwable error) {
		if (!(error instanceof CommandException)) {
			return false;
		}
		CommandException commandExc = (CommandException) error;
		return internalHandle(commandExc);
	}

	protected abstract boolean internalHandle(CommandException error);

}
