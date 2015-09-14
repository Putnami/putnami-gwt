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

/**
 * The ErrorHandler is responsible to handle or not the exception.
 * <p>
 * You can register your handler implementation in {@link ErrorManager}
 * </p>
 * <strong>Register a handler</strong>
 * <pre>
 * ErrorManager.get().registerErrorHandler(handler);
 * </pre>
 * <strong>Simple handler</strong>
 *
 * <pre>
 * public class SimpleHandler implements ErrorHandler {
 * 	public int getPriority() {
 * 		return DEFAULT_PRIORITY;
 * 	}
 *
 * 	public boolean handle(Throwable error) {
 * 		Window.alert(error.getMessage());
 * 		return true;
 * 	}
 * }
 * </pre>
 *
 * @since 1.0
 */
public interface ErrorHandler {

	int DEFAULT_PRIORITY = 0;
	int LOWER_PRIORITY = Integer.MIN_VALUE;
	int LOW_PRIORITY = -1000;
	int HIGH_PRIORITY = 1000;
	int HIGHER_PRIORITY = Integer.MAX_VALUE;

	/**
	 * Handle the error. If the handler can manage the exception, it must return true to stop the
	 * chaining, otherwise false to follow the chain.
	 *
	 * @param error to handle
	 * @return true, if error handled
	 * @see ErrorManager
	 */
	boolean handle(Throwable error);

	/**
	 * The priority of the handler within the {@link ErrorManager}.
	 * <p>
	 * Higher the priority is, Sooner the handler will be used
	 * </p>
	 *
	 * @return int the priority of the handler
	 */
	int getPriority();

}
