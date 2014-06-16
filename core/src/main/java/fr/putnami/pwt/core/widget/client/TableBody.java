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
package fr.putnami.pwt.core.widget.client;

import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.model.client.base.HasReadonly;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.event.RowClickEvent;
import fr.putnami.pwt.core.widget.client.event.RowClickEvent.Handler;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public class TableBody<T> extends AbstractPanel implements
		HasReadonly,
		CloneableWidget,
		RowClickEvent.HasRowClickHandlers {

	private class ClickEventHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			Object source = event.getSource();
			if (source instanceof TableRow) {
				EventBus.get().fireEventFromSource(new RowClickEvent((TableRow) source), TableBody.this);
			}
		}

	}

	private final List<TableRow<T>> rows = Lists.newArrayList();

	private final String bodyId;

	private Boolean readonly;

	private ClickHandler clickEventHandler;
	private HandlerRegistrationCollection registrations;

	@UiConstructor
	public TableBody(String bodyId) {
		super(TableSectionElement.TAG_TBODY);
		this.bodyId = bodyId;
	}

	protected TableBody(TableBody<T> source) {
		super(source);
		this.bodyId = source.bodyId;
		this.clickEventHandler = source.clickEventHandler;
		for (TableRow<T> row : source.rows) {
			addRow(WidgetUtils.cloneWidget(row));
		}
	}

	@Override
	public IsWidget cloneWidget() {
		return new TableBody<T>(this);
	}

	@Override
	public Boolean getReadonly() {
		return readonly;
	}

	@Override
	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}

	public String getBodyId() {
		return bodyId;
	}

	public Iterable<TableRow<T>> getRows() {
		return Iterables.unmodifiableIterable(rows);
	}

	protected List<TableRow<T>> getRowList() {
		return rows;
	}

	@Override
	public void add(IsWidget w) {
		if (w instanceof TableRow) {
			this.addRow((TableRow) w);
		}
	}

	public void addRow(TableRow<T> row) {
		rows.add(row);
		append(row);
		if (clickEventHandler != null) {
			registrations.add(row.addClickHandler(clickEventHandler));
		}
	}

	@Override
	public HandlerRegistration addRowClickHandler(Handler handler) {
		if (clickEventHandler == null) {
			clickEventHandler = new ClickEventHandler();
		}
		if (registrations == null) {
			registrations = new HandlerRegistrationCollection();
		}
		registrations.add(EventBus.get().addHandlerToSource(RowClickEvent.TYPE, this, handler));
		return registrations;
	}

}
