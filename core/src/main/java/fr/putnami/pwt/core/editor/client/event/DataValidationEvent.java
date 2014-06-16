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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.event.client.EventBus;

public final class DataValidationEvent extends GwtEvent<DataValidationEvent.Handler> {

	public interface Handler extends EventHandler {

		void onDataValidationEvent(DataValidationEvent event);
	}

	public interface HasDataValidationHandlers extends HasHandlers {

		HandlerRegistration addValidationHandler(Handler handler);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final Editor editor;
	private final Object value;
	private List<Error> errors;

	private DataValidationEvent(Editor editor, Object value) {
		super();
		this.editor = editor;
		this.value = value;
	}

	@Override
	protected void dispatch(DataValidationEvent.Handler handler) {
		handler.onDataValidationEvent(this);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return DataValidationEvent.TYPE;
	}

	public Editor getEditor() {
		return editor;
	}

	public <T> T getValue() {
		return (T) value;
	}

	public void recordError(Error... errors) {
		if (this.errors == null) {
			this.errors = Lists.newArrayList();
		}
		for (Error error : errors) {
			this.errors.add(error);
		}
	}

	public List<Error> getErrors() {
		return errors == null ? Collections.EMPTY_LIST : errors;
	}

	public static DataValidationEvent fire(Editor editor, Object value) {
		DataValidationEvent event = new DataValidationEvent(editor, value);
		EventBus.get().fireEventFromSource(event, editor);
		return event;
	}
}
