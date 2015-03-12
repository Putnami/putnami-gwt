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
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.google.gwt.user.server.rpc.impl.DequeMap;
import com.google.gwt.user.server.rpc.impl.SerializabilityUtil;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import fr.putnami.pwt.core.service.server.service.CommandSerializationPolicy;
import fr.putnami.pwt.core.service.shared.service.CommandService;

public class CommandSocketHandler extends TextWebSocketHandler
	implements SerializationPolicyProvider {

	private final Log logger = LogFactory.getLog(this.getClass());

	private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS =
		new HashMap<Class<?>, Class<?>>();
	private static final HashMap<String, Class<?>> TYPE_NAMES;

	static {
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Boolean.class, Boolean.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Byte.class, Byte.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Character.class, Character.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Double.class, Double.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Float.class, Float.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Integer.class, Integer.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Long.class, Long.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Short.class, Short.TYPE);

		TYPE_NAMES = new HashMap<String, Class<?>>();
		TYPE_NAMES.put("Z", boolean.class);
		TYPE_NAMES.put("B", byte.class);
		TYPE_NAMES.put("C", char.class);
		TYPE_NAMES.put("D", double.class);
		TYPE_NAMES.put("F", float.class);
		TYPE_NAMES.put("I", int.class);
		TYPE_NAMES.put("J", long.class);
		TYPE_NAMES.put("S", short.class);
	}

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

			ClassLoader classLoader = getClass().getClassLoader();

			ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(classLoader, this);
			streamReader.prepareToRead(payload);

			RpcToken rpcToken = null;
			if (streamReader.hasFlags(AbstractSerializationStream.FLAG_RPC_TOKEN_INCLUDED)) {
				// Read the RPC token
				rpcToken = (RpcToken) streamReader.deserializeValue(RpcToken.class);
			}

			// Read the name of the RemoteService interface
			String serviceIntfName = maybeDeobfuscate(streamReader, streamReader.readString());

			SerializationPolicy serializationPolicy = streamReader.getSerializationPolicy();
			Class<?> serviceIntf;
			try {
				serviceIntf = getClassFromSerializedName(serviceIntfName, classLoader);
				if (!RemoteService.class.isAssignableFrom(serviceIntf)) {
					// The requested interface is not a RemoteService interface
					throw new IncompatibleRemoteServiceException(
						"Blocked attempt to access interface '" + printTypeName(serviceIntf)
							+ "', which doesn't extend RemoteService; this is either misconfiguration or a hack attempt");
				}
			} catch (ClassNotFoundException e) {
				throw new IncompatibleRemoteServiceException("Could not locate requested interface '"
					+ serviceIntfName + "' in default classloader", e);
			}

			String serviceMethodName = streamReader.readString();

			int paramCount = streamReader.readInt();
			if (paramCount > streamReader.getNumberOfTokens()) {
				throw new IncompatibleRemoteServiceException("Invalid number of parameters");
			}
			Class<?>[] parameterTypes = new Class[paramCount];

			for (int i = 0; i < parameterTypes.length; i++) {
				String paramClassName = maybeDeobfuscate(streamReader, streamReader.readString());

				try {
					parameterTypes[i] = getClassFromSerializedName(paramClassName, classLoader);
				} catch (ClassNotFoundException e) {
					throw new IncompatibleRemoteServiceException("Parameter " + i
						+ " of is of an unknown type '" + paramClassName + "'", e);
				}
			}

			Method method = serviceIntf.getMethod(serviceMethodName, parameterTypes);

			// The parameter types we have are the non-parameterized versions in the
			// RPC stream. For stronger message verification, get the parameterized
			// types from the method declaration.
			Type[] methodParameterTypes = method.getGenericParameterTypes();
			DequeMap<TypeVariable<?>, Type> resolvedTypes = new DequeMap<TypeVariable<?>, Type>();

			TypeVariable<Method>[] methodTypes = method.getTypeParameters();
			for (TypeVariable<Method> methodType : methodTypes) {
				SerializabilityUtil.resolveTypes(methodType, resolvedTypes);
			}

			Object[] parameterValues = new Object[parameterTypes.length];
			for (int i = 0; i < parameterValues.length; i++) {
				parameterValues[i] = streamReader.deserializeValue(parameterTypes[i],
					methodParameterTypes[i], resolvedTypes);
			}

			RPCRequest rpcRequest = new RPCRequest(method, parameterValues, rpcToken, serializationPolicy, streamReader
				.getFlags());
			// RPCRequest rpcRequest = RPC.decodeRequest(payload, CommandService.class, this);
			String responsePayload =
				RPC.invokeAndEncodeResponse(commandService, rpcRequest.getMethod(), rpcRequest.getParameters(),
					rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
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

	private static Class<?> getClassFromSerializedName(String serializedName, ClassLoader classLoader)
		throws ClassNotFoundException {
		Class<?> value = TYPE_NAMES.get(serializedName);
		if (value != null) {
			return value;
		}

		return Class.forName(serializedName, false, classLoader);
	}

	private static String maybeDeobfuscate(ServerSerializationStreamReader streamReader, String name)
		throws SerializationException {
		int index;
		if (streamReader.hasFlags(AbstractSerializationStream.FLAG_ELIDE_TYPE_NAMES)) {
			SerializationPolicy serializationPolicy = streamReader.getSerializationPolicy();
			if (!(serializationPolicy instanceof TypeNameObfuscator)) {
				throw new IncompatibleRemoteServiceException(
					"RPC request was encoded with obfuscated type names, "
						+ "but the SerializationPolicy in use does not implement "
						+ TypeNameObfuscator.class.getName());
			}

			String maybe = ((TypeNameObfuscator) serializationPolicy).getClassNameForTypeId(name);
			if (maybe != null) {
				return maybe;
			}
		} else if ((index = name.indexOf('/')) != -1) {
			return name.substring(0, index);
		}
		return name;
	}

	private static String printTypeName(Class<?> type) {
		if (type.equals(Integer.TYPE)) {
			return "int";
		} else if (type.equals(Long.TYPE)) {
			return "long";
		} else if (type.equals(Short.TYPE)) {
			return "short";
		} else if (type.equals(Byte.TYPE)) {
			return "byte";
		} else if (type.equals(Character.TYPE)) {
			return "char";
		} else if (type.equals(Boolean.TYPE)) {
			return "boolean";
		} else if (type.equals(Float.TYPE)) {
			return "float";
		} else if (type.equals(Double.TYPE)) {
			return "double";
		}

		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			return printTypeName(componentType) + "[]";
		}

		return type.getName().replace('$', '.');
	}

}