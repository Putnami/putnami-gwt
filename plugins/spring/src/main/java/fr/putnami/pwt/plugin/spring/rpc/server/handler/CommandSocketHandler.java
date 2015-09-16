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
package fr.putnami.pwt.plugin.spring.rpc.server.handler;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

import fr.putnami.pwt.core.serialization.ppc.server.PpcServerSerializer;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcSerializer;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;
import fr.putnami.pwt.core.service.server.service.CommandSerializationPolicy;
import fr.putnami.pwt.core.service.shared.domain.CommandRequest;
import fr.putnami.pwt.core.service.shared.domain.CommandResponse;
import fr.putnami.pwt.core.service.shared.service.CommandService;

public class CommandSocketHandler extends TextWebSocketHandler
	implements SerializationPolicyProvider {

	private static final PpcSerializer serializer = new PpcServerSerializer();

	private final Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	private CommandService commandService;

	public CommandSocketHandler() {
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		super.handleTransportError(session, exception);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {

			String payload = message.getPayload();
			PpcReader reader = serializer.newReader();
			reader.prepare(payload);
			List<CommandRequest> commands = Lists.newArrayList(reader.<CommandRequest> readObject());
			List<CommandResponse> responses = commandService.executeCommands(commands);
			PpcWriter writer = serializer.newWriter();
			writer.write(responses.get(0));
			String responsePayload = writer.flush();
			session.sendMessage(new TextMessage(responsePayload.getBytes()));
		} catch (Exception e) {
			this.logger.error("Request processing failed", e);
			throw Throwables.propagate(e);
		}
	}

	@Override
	public SerializationPolicy getSerializationPolicy(String moduleBaseURL, String serializationPolicyStrongName) {
		return CommandSerializationPolicy.get();
	}

}