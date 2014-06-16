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

import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.event.client.EventBus;

public final class FlushErrorEvent extends GwtEvent<FlushErrorEvent.Handler> {

	public interface Handler extends EventHandler {

		void onFlushErrorEvent(FlushErrorEvent event);
	}

	public interface HasFlushErrorHandlers extends HasHandlers {

		HandlerRegistration addFlushErrorHandler(Handler handler);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final Editor editor;
	private final Object valueEdited;
	private final Object valueFlushed;
	private final List<Error> errors;

	private FlushErrorEvent(Editor editor, Object valueEdited, Object valueFlushed, List<Error> errors) {
		super();
		this.editor = editor;
		this.valueEdited = valueEdited;
		this.valueFlushed = valueFlushed;
		this.errors = errors;
	}

	@Override
	protected void dispatch(FlushErrorEvent.Handler handler) {
		handler.onFlushErrorEvent(this);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return FlushErrorEvent.TYPE;
	}

	public <T> T getValueEdited() {
		return (T) valueEdited;
	}

	public <T> T getValueFlushed() {
		return (T) valueFlushed;
	}

	public List<Error> getErrors() {
		return errors;
	}

	public Editor getEditor() {
		return editor;
	}

	public static void fire(Editor editor, Object valueEdited, Object valueFlushed, List<Error> errors) {
		EventBus.get().fireEventFromSource(new FlushErrorEvent(editor, valueEdited, valueFlushed, errors), editor);
	}
}
