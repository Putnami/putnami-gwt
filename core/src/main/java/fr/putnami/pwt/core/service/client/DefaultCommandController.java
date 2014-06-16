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
package fr.putnami.pwt.core.service.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter.ResponseReader;
import com.google.gwt.user.client.rpc.impl.RpcStatsContext;
import com.google.gwt.user.client.rpc.impl.Serializer;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.error.client.ErrorManager;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.service.client.error.ClientErrorHandler;
import fr.putnami.pwt.core.service.client.error.DefaultCommandExceptionErrorHandler;
import fr.putnami.pwt.core.service.client.error.ServerErrorHandler;
import fr.putnami.pwt.core.service.client.event.CommandRequestEvent;
import fr.putnami.pwt.core.service.client.event.CommandResponseEvent;
import fr.putnami.pwt.core.service.shared.domain.CommandDefinition;
import fr.putnami.pwt.core.service.shared.domain.CommandRequest;
import fr.putnami.pwt.core.service.shared.domain.CommandResponse;
import fr.putnami.pwt.core.service.shared.exception.CommandException;
import fr.putnami.pwt.core.service.shared.service.CommandService;

public final class DefaultCommandController extends CommandController {

	private class ServiceCallback extends CallbackAdapter<List<CommandResponse>> {

		private final List<Request> requests;
		private AsyncCallback<List<CommandResponse>> callback;

		public ServiceCallback(List<Request> requests, AsyncCallback<List<CommandResponse>> callback) {
			this.requests = requests;
			this.callback = callback;
		}

		@Override
		public void onSuccess(List<CommandResponse> responses) {
			for (CommandResponse response : responses) {
				for (Request request : this.requests) {
					if (request.requestId == response.getRequestId()) {
						if (!request.param.isQuiet()) {
							fireEvent(new CommandResponseEvent(request.requestId, request.command, response));
						}
						if (response.getThrown() == null) {
							for (AsyncCallback callback : request.param.getCallbacks()) {
								if (response.getResult().size() == 1) {
									callback.onSuccess(response.getResult().get(0));
								}
								else {
									callback.onSuccess(null);
								}
							}
						}
						else {
							boolean caught = false;
							for (AsyncCallback callback : request.param.getCallbacks()) {
								try {
									callback.onFailure(response.getThrown());
									caught = true;
								}
								catch (RuntimeException e) {
									// Exception not handled.
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

		@Override
		public void onFailure(Throwable caught) {
			if (caught instanceof StatusCodeException) {
				GWT.reportUncaughtException(caught);
			}
			else {
				for (Request request : this.requests) {
					if (request.param.getCallbacks().isEmpty()) {
						GWT.reportUncaughtException(caught);
					}
					for (AsyncCallback callback : request.param.getCallbacks()) {
						callback.onFailure(caught);
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

	private static final String RPC_CONTENT_TYPE = "text/x-gwt-rpc; charset=utf-8";
	private static final String REMOTE_SERVICE_INTERFACE_NAME = CommandService.class.getName();
	private static final String METHOD_NAME = "executeCommands";

	private static final DefaultCommandController INSTANCE = new DefaultCommandController();

	public static DefaultCommandController get() {
		return DefaultCommandController.INSTANCE;
	}

	private final RpcRequestBuilder rpcRequestBuilder = new RpcRequestBuilder();;
	private final String moduleBaseURL;
	private final String remoteServiceURL;

	private long requestIdSequence = 0;

	private boolean suspended = false;
	private Stack<Request> stack = new Stack<Request>();

	private DefaultCommandController() {
		this.moduleBaseURL = GWT.getModuleBaseURL();
		this.remoteServiceURL = this.moduleBaseURL + "commandService";
		ErrorManager.get().registerErrorHandlers(new ClientErrorHandler(), new ServerErrorHandler(), new DefaultCommandExceptionErrorHandler());
	}

	@Override
	public CommandRequest invokeCommand(CommandDefinition commandDefinition, CommandParam commanParam) {

		long requestId = ++this.requestIdSequence;
		CommandRequest command = new CommandRequest();
		command.setRequestId(requestId);
		command.setCommandDefinition(commandDefinition);
		command.setArgs(commanParam.getParams());

		Request request = new Request();
		request.requestId = requestId;
		request.param = commanParam;
		request.command = command;

		if (this.suspended || request.param.isLazy()) {
			this.stack.push(request);
		}
		else {
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
				callback.onSuccess(Collections.EMPTY_LIST);
			}
			return result;
		}
		finally {
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
		if (requests == null || requests.size() == 0) {
			return 0;
		}
		try {
			Collection<Serializer> serializers = Sets.newHashSet();
			List<CommandRequest> commands = Lists.newArrayList();

			for (Request request : requests) {
				serializers.add(request.param.getSerializer());
				commands.add(request.command);
				if (!request.param.isQuiet()) {
					fireEvent(new CommandRequestEvent(request.requestId, request.command));
				}
			}

			ServiceCallback serviceCallback = new ServiceCallback(requests, callback);
			CommandServiceCompositeSerializer compositeSerializer = new CommandServiceCompositeSerializer(serializers);

			SerializationStreamFactory streamFactory = new CommandSerializationStreamFactory(compositeSerializer, this.moduleBaseURL);
			SerializationStreamWriter streamWriter = streamFactory.createStreamWriter();

			streamWriter.writeString(REMOTE_SERVICE_INTERFACE_NAME);
			streamWriter.writeString(METHOD_NAME);
			streamWriter.writeInt(1);
			streamWriter.writeString(List.class.getName());
			streamWriter.writeObject(commands);

			String payload = streamWriter.toString();

			RpcStatsContext statsContext = new RpcStatsContext();

			RequestCallback responseHandler = new RequestCallbackAdapter<List<CommandResponse>>(
					streamFactory,
					METHOD_NAME,
					statsContext,
					serviceCallback, null, ResponseReader.OBJECT);

			this.rpcRequestBuilder.create(this.remoteServiceURL);
			this.rpcRequestBuilder.setCallback(responseHandler);
			this.rpcRequestBuilder.setContentType(RPC_CONTENT_TYPE);
			this.rpcRequestBuilder.setRequestData(payload);
			this.rpcRequestBuilder.setRequestId(statsContext.getRequestId());

			RequestBuilder rb = this.rpcRequestBuilder.finish();
			rb.send();

			return requests.size();
		}
		catch (SerializationException e) {
			throw new CommandException(e.getMessage());
		}
		catch (RequestException e) {
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
}
