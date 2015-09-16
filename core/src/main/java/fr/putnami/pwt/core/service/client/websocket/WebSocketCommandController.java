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
package fr.putnami.pwt.core.service.client.websocket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.serialization.ppc.client.PpcClientSerializer;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;
import fr.putnami.pwt.core.serialization.ppc.shared.SerializationException;
import fr.putnami.pwt.core.service.client.CommandController;
import fr.putnami.pwt.core.service.client.CommandParam;
import fr.putnami.pwt.core.service.client.event.CommandRequestEvent;
import fr.putnami.pwt.core.service.client.event.CommandResponseEvent;
import fr.putnami.pwt.core.service.client.websocket.WebSocket.WebSocketCallback;
import fr.putnami.pwt.core.service.shared.domain.CommandDefinition;
import fr.putnami.pwt.core.service.shared.domain.CommandRequest;
import fr.putnami.pwt.core.service.shared.domain.CommandResponse;
import fr.putnami.pwt.core.service.shared.exception.CommandException;

public class WebSocketCommandController extends CommandController implements WebSocketCallback {

	private static WebSocketCommandController instance;

	public static WebSocketCommandController get() {
		if (instance == null) {
			instance = new WebSocketCommandController();
		}
		return instance;
	}

	private static class Request {
		private long requestId;
		private CommandParam param;
		private CommandRequest command;
	}


	private WebSocket socket;
	private final String moduleBaseURL;
	private long requestIdSequence = 0;

	private Map<Long, Request> pendingRequest = Maps.newHashMap();
	private boolean suspended = false;
	private Stack<Request> stack = new Stack<Request>();
	private AsyncCallback<List<CommandResponse>> callback;

	public WebSocketCommandController() {
		String baseUrl = GWT.getHostPageBaseURL();
		baseUrl = baseUrl.replaceFirst("http://", "ws://");
		baseUrl = baseUrl.replaceFirst("https://", "ws://");
		this.moduleBaseURL = baseUrl + "/commandSocket";
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

		if (!commandParam.isQuiet()) {
			this.fireEvent(new CommandRequestEvent(requestId, command));
		}

		sendRequest(request);
		return command;
	}

	private void sendRequest(Request request) {
		try {
			PpcClientSerializer serializer = PpcClientSerializer.get();
			PpcWriter writer = serializer.newWriter();
			writer.write(request.command);
			String payload = writer.flush();

			pendingRequest.put(request.requestId, request);

			if (socket == null) {
				socket = new WebSocket(this.moduleBaseURL, this);
			}
			socket.send(payload);
		} catch (SerializationException e) {
			throw new CommandException(e.getMessage());
		}
	}

	@Override
	public int countPendingRequest() {
		return this.stack.size();
	}

	@Override
	public int flush() {
		return this.flush(null);
	}

	@Override
	public int flush(AsyncCallback<List<CommandResponse>> callback) {
		try {
			for (Request request : stack) {
				sendRequest(request);
			}
			if (stack.isEmpty() && callback != null) {
				callback.onSuccess(Collections.<CommandResponse> emptyList());
			} else {
				this.callback = callback;
			}
			return stack.size();
		} finally {
			this.stack.clear();
		}
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	@Override
	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
		if (!suspended) {
			this.flush();
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

	@Override
	public void onOpen(WebSocket webSocket) {
		GWT.log("onOpen");
	}

	@Override
	public void onMessage(WebSocket webSocket, String message) {
		PpcClientSerializer serializer = PpcClientSerializer.get();
		PpcReader reader = serializer.newReader().prepare(message);
		CommandResponse response = reader.readObject();

		Request request = pendingRequest.remove(response.getRequestId());

		if (!request.param.isQuiet()) {
			fireEvent(new CommandResponseEvent(request.requestId, request.command, response));
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

		if (pendingRequest.isEmpty() && callback != null) {
			callback.onSuccess(Lists.newArrayList(response));
		}
	}

	@Override
	public void onError(WebSocket webSocket) {
		GWT.log("onError");
	}

	@Override
	public void onClose(WebSocket webSocket) {
		socket = null;
	}

}
