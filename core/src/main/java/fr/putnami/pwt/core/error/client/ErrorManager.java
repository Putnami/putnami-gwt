/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with pwt. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.error.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

import java.util.List;

/**
 * The Class ErrorManager manages with the top level uncaught exceptions.
 * <p>
 * Containing a registry of {@link fr.putnami.pwt.core.error.client.ErrorHandler}, it delegates the
 * uncaugth exception to the right handler. It submits the error to each handler while the error is
 * not handled.
 * </p>
 * <strong>Note:</strong> You can use your own implementation by overriding it in your .gwt.xml
 *
 * <pre>
 * &lt;replace-with class="{your instance}"&gt;
 * 	&lt;when-type-is class="fr.putnami.pwt.core.error.client.ErrorManager"&gt;
 * &lt;/replace-with&gt;
 * </pre>
 *
 *
 * @since 1.0
 */
public abstract class ErrorManager implements UncaughtExceptionHandler {

	private static ErrorManager instance;

	/**
	 * Get the singleton.
	 * <p>
	 * The singleton is created thanks to GWT.create(ErrorManager.class). So you can use your own
	 * implementation by overring it in your .gwt.xml
	 * </p>
	 *
	 * <pre>
	 * &lt;replace-with class="{your instance}"&gt;
	 * 	&lt;when-type-is class="fr.putnami.pwt.core.error.client.ErrorManager"&gt;
	 * &lt;/replace-with&gt;
	 * </pre>
	 *
	 * @return the error manager
	 */
	public static ErrorManager get() {
		if (ErrorManager.instance == null) {
			ErrorManager.instance = GWT.create(ErrorManager.class);
		}
		return ErrorManager.instance;
	}

	/**
	 * Register handlers.
	 *
	 * @param handlers the handlers
	 */
	public abstract void registerErrorHandlers(ErrorHandler... handlers);

	/**
	 * Register handler.
	 *
	 * @param handler the handler
	 */
	public abstract void registerErrorHandler(ErrorHandler handler);

	/**
	 * Sets the default error handler.
	 *
	 * @param handler the new default error handler
	 */
	public abstract void setDefaultErrorHandler(ErrorHandler handler);

	/**
	 * Gets the handlers.
	 *
	 * @return the error handlers
	 */
	public abstract List<ErrorHandler> getErrorHandlers();

	/**
	 * Checks for error displayer.
	 *
	 * @return true, if got one
	 */
	public abstract boolean hasErrorDisplayer();

	/**
	 * Gets the error displayer.
	 *
	 * @return the error displayer
	 */
	public abstract ErrorDisplayer getErrorDisplayer();

	/**
	 * Sets the error displayer.
	 *
	 * @param errorDisplayer the new error displayer
	 */
	public abstract void setErrorDisplayer(ErrorDisplayer errorDisplayer);

	/**
	 * Removes the error handler.
	 *
	 * @param errorHandler the error handler
	 */
	public abstract void removeErrorHandler(ErrorHandler errorHandler);

}
