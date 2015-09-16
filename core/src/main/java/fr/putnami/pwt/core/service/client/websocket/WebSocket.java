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
import com.google.gwt.core.client.JavaScriptObject;

import java.util.List;

public class WebSocket {

	public static interface WebSocketCallback {

		void onOpen(WebSocket webSocket);

		void onMessage(WebSocket webSocket, String message);

		void onError(WebSocket webSocket);

		void onClose(WebSocket webSocket);

	}

	public static native boolean isSupported()
	/*-{
		if ($wnd.WebSocket) {
			return true;
		}
		return false;
	}-*/;

	private String url;
	private JavaScriptObject jsSocket;
	private final WebSocketCallback callback;
	private List<String> messageStack;

	public WebSocket(final String url, final WebSocketCallback socketCallback) {
		assert url != null;
		assert socketCallback != null;
		assert isSupported();

		this.url = url;
		this.callback = socketCallback;
	}

	public String getURL() {
		return this.url;
	}

	public void open() {
		if (jsSocket == null) {
			jsSocket = nativeCreateWebSocket(this, url);
		}
	}
	public void send(String message) {
		open();
		if (getReadyState() != 1) {
			if (messageStack == null) {
				messageStack = Lists.newArrayList();
			}
			messageStack.add(message);
		}
		else {
			nativeSend(message);
		}
	}

	public void close() {
		nativeClose();
	}

	public int getBufferedAmount() {
		return nativeGetBufferedAmount();
	}

	public int getReadyState() {
		return nativeGetReadyState();
	}


	private void onOpen() {
		if (messageStack != null) {
			for (String message : messageStack) {
				send(message);
			}
		}
		callback.onOpen(this);
	}

	private void onMessage(String message) {
		callback.onMessage(this, message);
	}

	private void onError() {
		callback.onError(this);
	}

	private void onClose() {
		this.jsSocket = null;
		callback.onClose(this);
	}

	private native void nativeSend(String message)
	/*-{
		if (message == null) {
			return;
		}
		this.@fr.putnami.pwt.core.serialization.client.websocket.WebSocket::jsSocket.send(message);
	}-*/;

	private native void nativeClose()
	/*-{
		this.@fr.putnami.pwt.core.serialization.client.websocket.WebSocket::jsSocket.close();
	}-*/;

	private native int nativeGetBufferedAmount()
	/*-{
		return this.@fr.putnami.pwt.core.serialization.client.websocket.WebSocket::jsSocket.bufferedAmount;
	}-*/;

	private native int nativeGetReadyState()
	/*-{
		return this.@fr.putnami.pwt.core.serialization.client.websocket.WebSocket::jsSocket.readyState;
	}-*/;

	private native JavaScriptObject nativeCreateWebSocket(WebSocket webSocket, final String url)
	/*-{
		var socket = new WebSocket(url);

		socket.onopen = function() {
			webSocket.@fr.putnami.pwt.core.serialization.client.websocket.WebSocket::onOpen()();
		}

		socket.onclose = function() {
			webSocket.@fr.putnami.pwt.core.serialization.client.websocket.WebSocket::onClose()();
		}

		socket.onerror = function() {
			webSocket.@fr.putnami.pwt.core.serialization.client.websocket.WebSocket::onError()();
		}

		socket.onmessage = function(socketResponse) {
			if (socketResponse.data) {
				webSocket.@fr.putnami.pwt.core.serialization.client.websocket.WebSocket::onMessage(Ljava/lang/String;)(socketResponse.data);
			}
		}

		return socket;
	}-*/;


}