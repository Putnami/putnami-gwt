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
package fr.putnami.pwt.core.widget.client.assist;

import com.google.common.collect.Lists;
import com.google.gwt.text.shared.Renderer;

import java.util.List;

import fr.putnami.pwt.core.service.client.CallbackAdapter;
import fr.putnami.pwt.core.widget.shared.assist.Oracle;

public abstract class AsyncOracle<T> extends AbstractOracle<T> {

	public static class AsyncOracleCallback<T> extends CallbackAdapter<Oracle.Response<T>> {

		private Oracle.Request request;
		private Oracle.Callback<T> callback;

		public AsyncOracleCallback(Oracle.Request request, Oracle.Callback<T> callback) {
			this.request = request;
			this.callback = callback;
		}

		@Override
		public void onSuccess(Oracle.Response<T> response) {
			callback.onSuggestionsReady(request, response);
		}
	}

	private class DelegateCallback implements Oracle.Callback<T> {

		private final Oracle.Callback<T> delegate;

		DelegateCallback(Oracle.Callback<T> callback) {
			super();
			this.delegate = callback;
		}

		@Override
		public void onSuggestionsReady(Oracle.Request request, Oracle.Response<T> response) {
			lastRequest = request;
			lastResponse = response;
			this.delegate.onSuggestionsReady(request, response);
		}
	}

	private Oracle.Request lastRequest;
	private Oracle.Response<T> lastResponse;

	public AsyncOracle() {
		setQueryLengthToRequest(2);
	}

	public AsyncOracle(Renderer<T> renderer) {
		this();
		setRenderer(renderer);
	}

	public Oracle.Request getLastRequest() {
		return lastRequest;
	}

	public Oracle.Response<T> getLastResponse() {
		return lastResponse;
	}

	@Override
	public void doRequest(Oracle.Request request, Oracle.Callback<T> callback) {
		String query = request.getQuery();
		if (lastResponse != null && !lastResponse.hasMoreSuggestions()
			&& query != null && query.startsWith(lastRequest.getQuery())) {

			List<Suggestion<T>> suggestions = Lists.newArrayList();

			for (Suggestion<T> suggestion : lastResponse.getSuggestions()) {
				T value = suggestion.getValue();
				int relevance = getMatcher().match(value, request.getQuery());
				if (relevance > 0) {
					suggestions.add(newSuggestion(query, value, relevance));
				}
			}
			Response<T> response = new Response<T>(suggestions);
			callback.onSuggestionsReady(request, response);
		} else {
			asyncRequest(request, new DelegateCallback(callback));
		}
	}

	public abstract void asyncRequest(Oracle.Request request, Oracle.Callback<T> callback);

}
