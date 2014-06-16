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
package fr.putnami.pwt.core.editor.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.event.client.EventBus;

public final class FlushSuccessEvent extends GwtEvent<FlushSuccessEvent.Handler> {

	public interface Handler extends EventHandler {

		void onFlushEvent(FlushSuccessEvent event);
	}

	public interface HasFlushSuccessHandlers extends HasHandlers {

		HandlerRegistration addFlushHandler(Handler handler);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final Editor editor;
	private final Object value;

	private FlushSuccessEvent(Editor editor, Object value) {
		super();
		this.editor = editor;
		this.value = value;
	}

	@Override
	protected void dispatch(FlushSuccessEvent.Handler handler) {
		handler.onFlushEvent(this);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return FlushSuccessEvent.TYPE;
	}

	public Editor getEditor() {
		return editor;
	}

	public <T> T getValue() {
		return (T) value;
	}

	public static void fire(Editor editor, Object value) {
		EventBus.get().fireEventFromSource(new FlushSuccessEvent(editor, value), editor);
	}
}
