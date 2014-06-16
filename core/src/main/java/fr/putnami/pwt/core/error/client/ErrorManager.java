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
package fr.putnami.pwt.core.error.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

public abstract class ErrorManager implements UncaughtExceptionHandler {

	private static ErrorManager INSTANCE;

	public static ErrorManager get() {
		if (INSTANCE == null) {
			INSTANCE = GWT.create(ErrorManager.class);
		}
		return INSTANCE;
	}

	public abstract void registerErrorHandlers(ErrorHandler... handlers);

	public abstract void registerErrorHandler(ErrorHandler handler);

	public abstract void setDefaultErrorHandler(ErrorHandler handler);

	public abstract List<ErrorHandler> getErrorHandlers();

	public abstract boolean hasErrorDisplayer();

	public abstract ErrorDisplayer getErrorDisplayer();

	public abstract void setErrorDisplayer(ErrorDisplayer errorDisplayer);

}
