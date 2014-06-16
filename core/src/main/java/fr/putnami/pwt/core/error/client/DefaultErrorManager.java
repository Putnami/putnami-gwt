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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;

public class DefaultErrorManager extends ErrorManager {

	private static final DefaultErrorManager INSTANCE = new DefaultErrorManager();

	public static DefaultErrorManager get() {
		return INSTANCE;
	}

	private static final Comparator<ErrorHandler> PRIORITY_COMPARATOR = new Comparator<ErrorHandler>() {

		@Override
		public int compare(ErrorHandler o1, ErrorHandler o2) {
			if (o1 == o2) {
				return 0;
			}
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}
			return Integer.compare(o2.getPriority(), o1.getPriority());// Reverse order
		}
	};

	private final class DefaultErrorHandler extends AbstractErrorHandler {
		private final Logger LOGGER = Logger.getLogger(DefaultErrorHandler.class.getSimpleName());

		@Override
		public boolean handle(Throwable error) {
			LOGGER.log(Level.SEVERE, error.getMessage(), error);
			return false;
		}
	}

	private final List<ErrorHandler> errorHandlers = Lists.newArrayList();
	private ErrorHandler defaultHandler = new DefaultErrorHandler();

	private ErrorDisplayer errorDisplayer;

	private DefaultErrorManager() {
		GWT.setUncaughtExceptionHandler(this);
	}

	public void registerErrorHandlers(ErrorHandler... handlers) {
		if (handlers != null) {
			for (ErrorHandler handler : handlers) {
				registerErrorHandler(handler);
			}
		}
	}

	public void registerErrorHandler(ErrorHandler handler) {
		if (handler != null) {
			errorHandlers.add(handler);
			Collections.sort(errorHandlers, PRIORITY_COMPARATOR);
		}
	}

	public void setDefaultErrorHandler(ErrorHandler handler) {
		assert handler != null : "The default Error Handler should not be null";
		this.defaultHandler = handler;
	}

	public List<ErrorHandler> getErrorHandlers() {
		return errorHandlers;
	}

	@Override
	public void onUncaughtException(Throwable throwable) {
		for (ErrorHandler handler : errorHandlers) {
			if (handler.handle(throwable)) {
				return;
			}
		}
		defaultHandler.handle(throwable);
	}

	public boolean hasErrorDisplayer() {
		return errorDisplayer != null;
	}

	public ErrorDisplayer getErrorDisplayer() {
		return errorDisplayer;
	}

	public void setErrorDisplayer(ErrorDisplayer errorDisplayer) {
		this.errorDisplayer = errorDisplayer;
	}

}
