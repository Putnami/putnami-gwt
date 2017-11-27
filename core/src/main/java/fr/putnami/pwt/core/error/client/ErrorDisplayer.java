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
 * The Interface ErrorDisplayer.
 *
 * @since 1.0
 */
public interface ErrorDisplayer {

	/**
	 * Error Severity.
	 */
	enum Severity {
			INFO,
			WARNING,
			DANGER;
	}

	/**
	 * Display the error.
	 *
	 * @param error the error to display
	 * @param severity the severity of the error
	 */
	void display(Throwable error, Severity severity);

	/**
	 * Display the error.
	 *
	 * @param message the message to display
	 * @param error the error to display
	 * @param severity the severity of the error
	 */
	void display(String message, Throwable error, Severity severity);

	/**
	 * Display the error.
	 *
	 * @param title the title of the error
	 * @param message the message of the error
	 * @param error the error to display
	 * @param severity the severity of the error
	 */
	void display(String title, String message, Throwable error, Severity severity);

	/**
	 * Display the error.
	 *
	 * @param title the title of the error
	 * @param message the message of the error
	 * @param details the details of the error
	 * @param severity the severity of the error
	 */
	void display(String title, String message, String details, Severity severity);

}
