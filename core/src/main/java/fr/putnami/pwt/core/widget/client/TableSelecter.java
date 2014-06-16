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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.EditorValue;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.widget.client.base.AbstractTableCell;
import fr.putnami.pwt.core.widget.client.base.AbstractTableColumn;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.SelectionEvent;
import fr.putnami.pwt.core.widget.client.event.SelectionEvent.HasSelectionHandlers;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class TableSelecter<T> extends AbstractTableColumn<T> implements HasSelectionHandlers {

	private static final CssStyle STYLE_ROW_SELECTED = new SimpleStyle("info");

	public enum SelectionMode {
		COLUMN, ROW_CLICK, BOTH;
	}

	private class TDSelecter extends AbstractTableCell<T> implements
			ClickHandler,
			EditorValue<T>,
			HasDrawable {

		private final InputElement inputElem;

		private HandlerRegistration parentClickRegistration;
		private HandlerRegistration clickRegistration;

		private T value;

		public TDSelecter() {
			super(TableCellElement.TAG_TD);
			if (singleSelection) {
				inputElem = InputElement.as(DOM.createInputRadio(groupId));
			}
			else {
				inputElem = InputElement.as(DOM.createInputCheck());
			}
			getElement().appendChild(inputElem);
		}

		@Override
		public void setReadonly(Boolean readonly) {
			super.setReadonly(readonly);
			inputElem.setReadOnly(Boolean.TRUE.equals(readonly));
		}

		@Override
		public T getValue() {
			return value;
		}

		@Override
		public void edit(T value) {
			this.value = value;
			redraw();
		}

		@Override
		public void redraw() {
			inputElem.setChecked(selection.contains(this.value));
			inputElem.setReadOnly(Boolean.TRUE.equals(this.getReadonly()));
			StyleUtils.toggleStyle(getParent(), STYLE_ROW_SELECTED, selection.contains(this.value));
			switch (selectionMode) {
			case COLUMN:
				setVisible(true);
				if (clickRegistration == null) {
					clickRegistration = addDomHandler(this, ClickEvent.getType());
				}
				break;
			case ROW_CLICK:
				setVisible(false);
				if (parentClickRegistration == null) {
					parentClickRegistration = getParent().addDomHandler(this, ClickEvent.getType());
				}
				break;
			case BOTH:
				setVisible(true);
				if (clickRegistration == null) {
					clickRegistration = addDomHandler(this, ClickEvent.getType());
				}
				if (parentClickRegistration == null) {
					parentClickRegistration = getParent().addDomHandler(this, ClickEvent.getType());
				}
				break;
			}
		}

		@Override
		public void onClick(ClickEvent event) {
			boolean fire = false;
			if (event.getSource() == this) {
				fire = setSelected(this.value, inputElem.isChecked());
				event.stopPropagation();
			}
			else if (event.getSource() instanceof TableRow) {
				TableRow row = (TableRow) event.getSource();
				T clickedValue = (T) row.getValue();
				fire = setSelected(clickedValue, !isSelected(clickedValue));
			}

			if (fire) {
				EventBus.get().fireEventFromSource(new SelectionEvent(selection), TableSelecter.this);
			}
			redraw();
		}

	}

	private HandlerManager handlerManager;

	private final List<T> selection = Lists.newArrayList();
	private final List<TDSelecter> cells = Lists.newArrayList();

	private boolean singleSelection = false;
	private SelectionMode selectionMode = SelectionMode.COLUMN;
	private String groupId = Document.get().createUniqueId();

	public TableSelecter() {
	}

	protected TableSelecter(TableSelecter<T> source) {
		super(source);
		this.singleSelection = source.singleSelection;
		this.selectionMode = source.selectionMode;

		handlerManager = new HandlerManager(source.handlerManager, this);
		handlerManager.resetSinkEvents();
	}

	@Override
	public IsWidget cloneWidget() {
		return new TableSelecter<T>(this);
	}

	public boolean isSingleSelection() {
		return singleSelection;
	}

	public void setSingleSelection(boolean singleSelection) {
		this.singleSelection = singleSelection;
	}

	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(SelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}

	@Override
	public TableTH<T> createHeaderCell() {
		TableTH<T> headerCell = new TableTH<T>();
		if (selectionMode == SelectionMode.ROW_CLICK) {
			headerCell.setVisible(false);
		}
		return headerCell;
	}

	@Override
	public AbstractTableCell<T> createBodyCell() {
		TDSelecter cell = new TDSelecter();
		cells.add(cell);
		return cell;
	}

	public boolean isSelected(T object) {
		return selection.contains(object);
	}

	public List<T> getSelection() {
		return Collections.unmodifiableList(selection);
	}

	public boolean setSelected(T object, boolean selected) {
		boolean hasChanged = selected && !selection.contains(object);
		hasChanged |= !selected && selection.contains(object);
		if (singleSelection) {
			selection.clear();
		}
		else {
			selection.remove(object);
		}
		if (selected) {
			selection.add(object);
		}
		for (TDSelecter cell : cells) {
			cell.redraw();
		}
		return hasChanged;
	}

	@Override
	public HandlerRegistration addSelectionHandler(SelectionEvent.Handler handler) {
		return createHandlerManager().addHandler(SelectionEvent.TYPE, handler);
	}

	protected HandlerManager createHandlerManager() {
		if (handlerManager == null) {
			handlerManager = new HandlerManager(this);
		}
		return handlerManager;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		EventBus.get().fireEventFromSource(event, this);
	}

}
