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
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.List;
import java.util.Map;

import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.service.client.CommandController;
import fr.putnami.pwt.core.service.client.CommandParam;
import fr.putnami.pwt.core.service.client.CommandSerializationStreamFactory;
import fr.putnami.pwt.core.service.client.event.CommandRequestEvent;
import fr.putnami.pwt.core.service.client.event.CommandResponseEvent;
import fr.putnami.pwt.core.service.client.websocket.WebSocket.WebSocketCallback;
import fr.putnami.pwt.core.service.shared.domain.CommandDefinition;
import fr.putnami.pwt.core.service.shared.domain.CommandRequest;
import fr.putnami.pwt.core.service.shared.domain.CommandResponse;
import fr.putnami.pwt.core.service.shared.exception.CommandException;
import fr.putnami.pwt.core.service.shared.service.CommandService;

public class WebSocketCommandController extends CommandController implements WebSocketCallback {

	private static final String METHOD_NAME = "executeCommands";
	private static final String REMOTE_SERVICE_INTERFACE_NAME = CommandService.class.getName();

	private static class Request {
		private long requestId;
		private CommandRequest command;
		private CommandParam param;
	}

	private static WebSocketCommandController instance;

	private WebSocket socket;
	private final String moduleBaseURL;
	private long requestIdSequence = 0;
	private Map<Serializer, SerializationStreamFactory> streamFactories = Maps.newHashMap();

	public WebSocketCommandController() {
		String baseUrl = GWT.getHostPageBaseURL();
		baseUrl = baseUrl.replaceFirst("http://", "ws://");
		baseUrl = baseUrl.replaceFirst("https://", "ws://");
		this.moduleBaseURL = baseUrl + "/commandSocket";
		socket = new WebSocket(this.moduleBaseURL, this);
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

		this.sendRequest(request);

		return command;
	}

	private void sendRequest(Request request) {
		try {
			if (!request.param.isQuiet()) {
				this.fireEvent(new CommandRequestEvent(request.requestId, request.command));
			}

			SerializationStreamFactory streamFactory = streamFactories.get(request.param.getSerializer());
			if (streamFactory == null) {
				streamFactory = new CommandSerializationStreamFactory(request.param.getSerializer(), this.moduleBaseURL);
				streamFactories.put(request.param.getSerializer(), streamFactory);
			}
			List<CommandRequest> commands = Lists.newArrayList(request.command);

			SerializationStreamWriter streamWriter = streamFactory.createStreamWriter();

			streamWriter.writeString(WebSocketCommandController.REMOTE_SERVICE_INTERFACE_NAME);
			streamWriter.writeString(WebSocketCommandController.METHOD_NAME);
			streamWriter.writeInt(1);
			streamWriter.writeString(List.class.getName());
			streamWriter.writeObject(commands);

			String payload = streamWriter.toString();

			socket.send(payload);
		} catch (SerializationException e) {
			throw new CommandException(e.getMessage());
		}
	}

	@Override
	public int countPendingRequest() {
		return 0;
	}

	@Override
	public int flush() {
		return 0;
	}

	@Override
	public int flush(AsyncCallback<List<CommandResponse>> callback) {
		return 0;
	}

	@Override
	public boolean isSuspended() {
		return false;
	}

	@Override
	public void setSuspended(boolean suspended) {
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

	public static WebSocketCommandController get() {
		if (instance == null) {
			instance = new WebSocketCommandController();
		}
		return instance;
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(WebSocket webSocket, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(WebSocket webSocket) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClose(WebSocket webSocket) {
		// TODO Auto-generated method stub

	}

}
