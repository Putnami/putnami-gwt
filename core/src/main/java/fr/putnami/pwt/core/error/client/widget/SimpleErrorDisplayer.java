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

import java.util.MissingResourceException;

import com.google.common.base.CaseFormat;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.logging.impl.StackTracePrintStream;

import fr.putnami.pwt.core.error.client.ErrorDisplayer;
import fr.putnami.pwt.core.service.shared.exception.CommandException;

public class SimpleErrorDisplayer implements ErrorDisplayer {

	private static final String TITLE_SUFFIX = "Title";
	private static final String MESSAGE_SUFFIX = "Message";

	private ErrorDisplay display = new ErrorDisplay();

	private ConstantsWithLookup constants;

	public void setConstants(ConstantsWithLookup constants) {
		this.constants = constants;
	}

	@Override
	public void display(Throwable error, Severity severity) {
		String message = getMessage(error, MESSAGE_SUFFIX, error.getMessage());
		display(message, error, severity);
	}

	@Override
	public void display(String message, Throwable error, Severity severity) {
		String title = getMessage(error, TITLE_SUFFIX, error.getClass().getSimpleName());
		display(title, message, error, severity);
	}

	@Override
	public void display(String title, String message, Throwable error, Severity severity) {
		display(title, message, getDetailString(error), severity);
	}

	@Override
	public void display(String title, String message, String details, Severity severity) {
		display.addErrorAlert(new ErrorAlert(title, message, details, severity));
	}

	private String getDetailString(Throwable error) {
		StringBuilder sb = new StringBuilder();
		if (error instanceof CommandException) {
			sb.append(((CommandException) error).getCauseMessage()).append(" : \n");
			sb.append(((CommandException) error).getCauseStackTrace());
		}
		else {
			sb.append(error.getMessage()).append(" : \n");
			error.printStackTrace(new StackTracePrintStream(sb));
		}
		return sb.toString();
	}

	private String getMessage(Throwable error, String suffix, String defaultMessage) {
		if (constants == null) {
			return defaultMessage;
		}
		try {
			String className = error.getClass().getSimpleName();
			if (error instanceof CommandException) {
				className = ((CommandException) error).getCauseSimpleClassName();
			}
			String methodName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, className) + suffix;
			return constants.getString(methodName);
		}
		catch (MissingResourceException exc) {
			return defaultMessage;
		}
	}
}
