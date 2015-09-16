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
package fr.putnami.pwt.core.service.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public class CommandParam {
	private final boolean lazy;
	private final boolean quiet;
	private final List params;
	private final List callbacks;

	public CommandParam(boolean lazy, boolean quiet, List params, List callbacks) {
		super();
		this.lazy = lazy;
		this.quiet = quiet;
		this.params = params;
		this.callbacks = callbacks;
	}

	public boolean isLazy() {
		return this.lazy;
	}

	public boolean isQuiet() {
		return this.quiet;
	}

	public List getParams() {
		return this.params;
	}

	public List<AsyncCallback> getCallbacks() {
		return this.callbacks;
	}

}
