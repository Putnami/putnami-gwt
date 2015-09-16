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

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import fr.putnami.pwt.core.error.client.ErrorManager;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.serialization.ppc.client.PpcClientSerializer;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;
import fr.putnami.pwt.core.serialization.ppc.shared.SerializationException;
import fr.putnami.pwt.core.service.client.error.ClientErrorHandler;
import fr.putnami.pwt.core.service.client.error.DefaultCommandExceptionErrorHandler;
import fr.putnami.pwt.core.service.client.error.ServerErrorHandler;
import fr.putnami.pwt.core.service.client.event.CommandRequestEvent;
import fr.putnami.pwt.core.service.client.event.CommandResponseEvent;
import fr.putnami.pwt.core.service.shared.domain.CommandDefinition;
import fr.putnami.pwt.core.service.shared.domain.CommandRequest;
import fr.putnami.pwt.core.service.shared.domain.CommandResponse;
import fr.putnami.pwt.core.service.shared.exception.CommandException;

public final class DefaultCommandController extends CommandController {

	private class ServiceCallback implements RequestCallback {

		private final List<Request> requests;
		private AsyncCallback<List<CommandResponse>> callback;

		public ServiceCallback(List<Request> requests, AsyncCallback<List<CommandResponse>> callback) {
			this.requests = requests;
			this.callback = callback;
		}

		@Override
		public void onResponseReceived(com.google.gwt.http.client.Request request, Response response) {
			PpcClientSerializer serializer = PpcClientSerializer.get();
			PpcReader reader = serializer.newReader();
			reader.prepare(response.getText());
			List<CommandResponse> responses = reader.readObject();
			onSuccess(responses);
		}

		@Override
		public void onError(com.google.gwt.http.client.Request request, Throwable exception) {
			onFailure(exception);
		}

		public void onSuccess(List<CommandResponse> responses) {
			for (CommandResponse response : responses) {
				for (Request request : this.requests) {
					if (request.requestId == response.getRequestId()) {
						if (!request.param.isQuiet()) {
							DefaultCommandController.this.fireEvent(new CommandResponseEvent(request.requestId, request.command,
								response));
						}
						if (response.getThrown() == null) {
							for (AsyncCallback requestCallback : request.param.getCallbacks()) {
								if (response.getResult().size() == 1) {
									requestCallback.onSuccess(response.getResult().get(0));
								} else {
									requestCallback.onSuccess(null);
								}
							}
						} else {
							boolean caught = false;
							for (AsyncCallback requestCallback : request.param.getCallbacks()) {
								try {
									requestCallback.onFailure(response.getThrown());
									caught = true;
								} catch (RuntimeException e) {
									// Exception not handled.
									continue;
								}
							}
							if (!caught) {
								GWT.reportUncaughtException(response.getThrown());
							}
						}
					}
				}
			}
			if (this.callback != null) {
				this.callback.onSuccess(responses);
			}
		}

		public void onFailure(Throwable caught) {
			if (caught instanceof StatusCodeException) {
				GWT.reportUncaughtException(caught);
			} else {
				for (Request request : this.requests) {
					if (request.param.getCallbacks().isEmpty()) {
						GWT.reportUncaughtException(caught);
					}
					for (AsyncCallback requestCallback : request.param.getCallbacks()) {
						requestCallback.onFailure(caught);
					}
				}
				if (this.callback != null) {
					this.callback.onFailure(caught);
				}
			}
		}
	}

	private static class Request {

		private long requestId;
		private CommandRequest command;
		private CommandParam param;
	}

	private static final String RPC_CONTENT_TYPE = "application/x-ppc; charset=utf-8";

	private static DefaultCommandController instance;

	private final String moduleBaseURL;
	private final String remoteServiceURL;

	private long requestIdSequence = 0;

	private boolean suspended = false;
	private Stack<Request> stack = new Stack<Request>();

	private DefaultCommandController() {
		this.moduleBaseURL = GWT.getHostPageBaseURL();
		this.remoteServiceURL = this.moduleBaseURL + "commandService";
		ErrorManager.get().registerErrorHandlers(new ClientErrorHandler(), new ServerErrorHandler(),
			new DefaultCommandExceptionErrorHandler());
	}

	@Override
	public CommandRequest invokeCommand(CommandDefinition commandDefinition, CommandParam commandParam) {

		long requestId = ++this.requestIdSequence;
		CommandRequest command = new CommandRequest();
		command.setRequestId(requestId);
		command.setCommandDefinition(commandDefinition);
		command.setArgs(commandParam.getParams());

		Request request = new Request();
		request.requestId = requestId;
		request.param = commandParam;
		request.command = command;

		if (this.suspended || request.param.isLazy()) {
			this.stack.push(request);
		} else {
			this.sendRequest(Lists.newArrayList(request), null);
		}

		return command;
	}

	@Override
	public int flush() {
		return this.flush(null);
	}

	@Override
	public int flush(AsyncCallback<List<CommandResponse>> callback) {
		try {
			int result = this.sendRequest(Lists.newArrayList(this.stack), callback);
			if (result == 0 && callback != null) {
				callback.onSuccess(Collections.<CommandResponse> emptyList());
			}
			return result;
		} finally {
			this.stack.clear();
		}
	}

	@Override
	public int countPendingRequest() {
		return this.stack.size();
	}

	@Override
	public boolean isSuspended() {
		return this.suspended;
	}

	@Override
	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
		if (!suspended) {
			this.flush();
		}
	}

	private int sendRequest(List<Request> requests, AsyncCallback<List<CommandResponse>> callback) {
		if (requests == null || requests.isEmpty()) {
			return 0;
		}
		try {
			List<CommandRequest> commands = Lists.newArrayList();

			for (Request request : requests) {
				commands.add(request.command);
				if (!request.param.isQuiet()) {
					this.fireEvent(new CommandRequestEvent(request.requestId, request.command));
				}
			}

			ServiceCallback serviceCallback = new ServiceCallback(requests, callback);

			PpcClientSerializer serializer = PpcClientSerializer.get();
			PpcWriter writer = serializer.newWriter();
			writer.write(commands);

			String payload = writer.flush();

			RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, this.remoteServiceURL);
			requestBuilder.setCallback(serviceCallback);
			requestBuilder.setHeader("Content-Type", RPC_CONTENT_TYPE);
			requestBuilder.setRequestData(payload);
			CsrfController.get().securize(requestBuilder);
			requestBuilder.send();

			return requests.size();
		} catch (SerializationException e) {
			throw new CommandException(e.getMessage());
		} catch (RequestException e) {
			throw new CommandException(e.getMessage());
		}
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		EventBus.get().fireEventFromSource(event, this);
	}

	@Override
	public HandlerRegistration addCommandRequestHandler(CommandRequestEvent.Handler handler) {
		return EventBus.get().addHandlerToSource(CommandRequestEvent.TYPE, this, handler);
	}

	@Override
	public HandlerRegistration addCommandResponseHandler(CommandResponseEvent.Handler handler) {
		return EventBus.get().addHandlerToSource(CommandResponseEvent.TYPE, this, handler);
	}

	public static DefaultCommandController get() {
		if (instance == null) {
			instance = new DefaultCommandController();
		}
		return instance;
	}

}
