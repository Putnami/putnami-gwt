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
package fr.putnami.pwt.core.widget.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.widget.client.TableEditorBody;
import fr.putnami.pwt.core.widget.client.TableRow;

public class RowOrderChangeEvent extends GwtEvent<RowOrderChangeEvent.Handler> {

	public interface Handler extends EventHandler {

		void onRowOrderChange(RowOrderChangeEvent event);
	}

	public interface HasRowOrderChangeHandlers extends HasHandlers {

		HandlerRegistration addRowOrderChangeHandler(Handler handler);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final TableEditorBody<?> source;

	public RowOrderChangeEvent(TableEditorBody<?> source) {
		super();
		this.source = source;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return RowOrderChangeEvent.TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onRowOrderChange(this);
	}

	@Override
	public TableEditorBody<?> getSource() {
		return this.source;
	}

	public Iterable<? extends TableRow<?>> getRows() {
		if (this.source != null) {
			return this.source.getRows();
		}
		return null;
	}
}
