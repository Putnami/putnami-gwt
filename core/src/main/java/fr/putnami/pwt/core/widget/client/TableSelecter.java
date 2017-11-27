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
package fr.putnami.pwt.core.widget.client;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.Collections;
import java.util.List;

import fr.putnami.pwt.core.editor.client.EditorValue;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractTableCell;
import fr.putnami.pwt.core.widget.client.base.AbstractTableColumn;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.SelectionEvent;
import fr.putnami.pwt.core.widget.client.event.SelectionEvent.HasSelectionHandlers;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class TableSelecter<T> extends AbstractTableColumn<T> implements HasSelectionHandlers {

	private static final CssStyle STYLE_TABLE_SELECTER = new SimpleStyle("table-selecter");
	private static final CssStyle STYLE_ROW_CLICKABLE = new SimpleStyle("clickable");
	private static final CssStyle STYLE_ROW_SELECTED = new SimpleStyle("info");

	public enum SelectionMode {
			COLUMN,
			ROW_CLICK,
			BOTH;
	}

	private class TDSelecter extends AbstractTableCell<T> implements ClickHandler, EditorValue<T>, HasDrawable {

		private final InputElement inputElem;

		private HandlerRegistration parentClickRegistration;
		private HandlerRegistration clickRegistration;

		private T value;

		TDSelecter() {
			super(TableCellElement.TAG_TD);
			if (TableSelecter.this.singleSelection) {
				this.inputElem = InputElement.as(DOM.createInputRadio(TableSelecter.this.groupId));
			} else {
				this.inputElem = InputElement.as(DOM.createInputCheck());
			}
			this.getElement().appendChild(this.inputElem);
		}

		@Override
		public void setReadonly(Boolean readonly) {
			super.setReadonly(readonly);
			this.inputElem.setReadOnly(Boolean.TRUE.equals(readonly));
		}

		@Override
		public T getValue() {
			return this.value;
		}

		@Override
		public void edit(T value) {
			this.value = value;
			this.redraw();
		}

		@Override
		public LeafValueEditor<T> asEditor() {
			return new TakesValueEditorWrapper<T>(this);
		}

		@Override
		public void redraw() {
			this.inputElem.setChecked(TableSelecter.this.selection.contains(this.value));
			this.inputElem.setReadOnly(Boolean.TRUE.equals(this.getReadonly()));
			this.inputElem.setDisabled(!TableSelecter.this.enable);
			StyleUtils.toggleStyle(this.getParent(), TableSelecter.STYLE_ROW_SELECTED, TableSelecter.this.selection
				.contains(this.value));
			switch (TableSelecter.this.selectionMode) {
				case COLUMN:
					StyleUtils.toggleStyle(this.getParent(), TableSelecter.STYLE_ROW_CLICKABLE, false);
					if (this.clickRegistration == null) {
						this.clickRegistration = this.addDomHandler(this, ClickEvent.getType());
					}
					break;
				case ROW_CLICK:
					StyleUtils.toggleStyle(this.getParent(), TableSelecter.STYLE_ROW_CLICKABLE, TableSelecter.this.enable);
					if (this.parentClickRegistration == null) {
						this.parentClickRegistration = this.getParent().addDomHandler(this, ClickEvent.getType());
					}
					break;
				case BOTH:
					StyleUtils.toggleStyle(this.getParent(), TableSelecter.STYLE_ROW_CLICKABLE, TableSelecter.this.enable);
					if (this.clickRegistration == null) {
						this.clickRegistration = this.addDomHandler(this, ClickEvent.getType());
					}
					if (this.parentClickRegistration == null) {
						this.parentClickRegistration = this.getParent().addDomHandler(this, ClickEvent.getType());
					}
					break;
				default:
					break;
			}
		}

		@Override
		public void onClick(ClickEvent event) {
			if (!TableSelecter.this.enable) {
				return;
			}
			boolean fire = false;
			if (event.getSource() == this) {
				fire = TableSelecter.this.setSelected(this.value, this.inputElem.isChecked());
				event.stopPropagation();
			} else if (event.getSource() instanceof TableRow) {
				TableRow<T> row = (TableRow<T>) event.getSource();
				T clickedValue = row.getValue();
				fire = TableSelecter.this.setSelected(clickedValue, !TableSelecter.this.isSelected(clickedValue));
			}

			if (fire) {
				TableSelecter.this.fireEvent(new SelectionEvent(TableSelecter.this.selection));
			}
			this.redraw();
		}
	}

	private HandlerManager handlerManager;

	private final List<T> selection = Lists.newArrayList();
	private final List<TDSelecter> cells = Lists.newArrayList();

	private boolean singleSelection = false;
	private SelectionMode selectionMode = SelectionMode.COLUMN;
	private String groupId = Document.get().createUniqueId();
	private boolean enable = true;

	public TableSelecter() {
		this.setType(Type.ACTION);
	}

	protected TableSelecter(TableSelecter<T> source) {
		super(source);
		this.singleSelection = source.singleSelection;
		this.selectionMode = source.selectionMode;

		this.handlerManager = new HandlerManager(source.handlerManager, this);
		this.handlerManager.resetSinkEvents();
	}

	@Override
	public IsWidget cloneWidget() {
		return new TableSelecter<T>(this);
	}

	public boolean isSingleSelection() {
		return this.singleSelection;
	}

	public void setSingleSelection(boolean singleSelection) {
		this.singleSelection = singleSelection;
	}

	public SelectionMode getSelectionMode() {
		return this.selectionMode;
	}

	public void setSelectionMode(SelectionMode selectionMode) {
		this.selectionMode = selectionMode;
		if (selectionMode == SelectionMode.ROW_CLICK) {
			this.setColumnVisibility(ColumnVisibility.HIDE);
		}
	}

	@Override
	public TableTH<T> doCreateHeaderCell() {
		TableTH<T> headerCell = new TableTH<T>();
		StyleUtils.addStyle(headerCell, TableSelecter.STYLE_TABLE_SELECTER);
		return headerCell;
	}

	@Override
	public AbstractTableCell<T> doCreateBodyCell() {
		TDSelecter cell = new TDSelecter();
		StyleUtils.addStyle(cell, TableSelecter.STYLE_TABLE_SELECTER);
		if (this.selectionMode == SelectionMode.ROW_CLICK) {
			cell.setVisible(false);
		}
		this.cells.add(cell);
		return cell;
	}

	public boolean isSelected(T object) {
		return this.selection.contains(object);
	}

	public List<T> getSelection() {
		return Collections.unmodifiableList(this.selection);
	}

	public boolean setSelected(T object, boolean selected) {
		boolean hasChanged = selected && !this.selection.contains(object);
		hasChanged |= !selected && this.selection.contains(object);
		if (this.singleSelection) {
			this.selection.clear();
		} else {
			this.selection.remove(object);
		}
		if (selected) {
			this.selection.add(object);
		}
		this.redraw();
		return hasChanged;
	}

	@Override
	public HandlerRegistration addSelectionHandler(SelectionEvent.Handler handler) {
		return this.createHandlerManager().addHandler(SelectionEvent.TYPE, handler);
	}

	protected HandlerManager createHandlerManager() {
		if (this.handlerManager == null) {
			this.handlerManager = new HandlerManager(this);
		}
		return this.handlerManager;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (this.handlerManager != null) {
			this.handlerManager.fireEvent(event);
		}
	}

	public void clearSelection() {
		this.selection.clear();
		this.redraw();
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
		this.redraw();
	}

	public boolean isEnable() {
		return this.enable;
	}

	private void redraw() {
		for (TDSelecter cell : this.cells) {
			cell.redraw();
		}
	}
}
