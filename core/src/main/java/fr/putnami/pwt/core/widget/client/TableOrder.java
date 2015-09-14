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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.List;

import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.theme.client.IconFont;
import fr.putnami.pwt.core.widget.client.base.AbstractTableCell;
import fr.putnami.pwt.core.widget.client.base.AbstractTableColumn;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.RowOrderChangeEvent;
import fr.putnami.pwt.core.widget.client.util.SelectionUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class TableOrder<T> extends AbstractTableColumn<T> implements RowOrderChangeEvent.HasRowOrderChangeHandlers {

	private static final CssStyle STYLE_TABLE_ORDER = new SimpleStyle("table-order");
	private static final CssStyle STYLE_ROW_DRAGING = new SimpleStyle("success");
	private static final CssStyle STYLE_NO_SELECTION = new SimpleStyle("no-selection");

	private class TDHandle extends AbstractTableCell<T> {

		private final Icon dragIcon;

		public TDHandle() {
			super(TableCellElement.TAG_TD);
			this.dragIcon = new Icon();
			this.dragIcon.setType(IconFont.ICON_DRAG);
			this.dragIcon.addDomHandler(TableOrder.this.mouseHandler, MouseDownEvent.getType());
			this.dragIcon.addDomHandler(TableOrder.this.mouseHandler, ClickEvent.getType());
			this.append(this.dragIcon);
		}
	}

	private class Handler implements MouseDownHandler, MouseOverHandler, MouseUpHandler, ClickHandler {

		private TableEditorBody<T> body;
		private TableRow<T> hoverRow;
		private TableRow<T> selectedRow;
		private Iterable<TableRow<T>> rows;

		private HandlerRegistration upRegistration;
		private HandlerRegistrationCollection overRegistration;

		@Override
		public void onClick(ClickEvent event) {
			event.stopPropagation();
		}

		@Override
		public void onMouseDown(MouseDownEvent event) {
			this.onMouseUp(null);

			Icon dragIcon = (Icon) event.getSource();
			this.selectedRow = (TableRow<T>) dragIcon.getParent().getParent();
			this.body = (TableEditorBody<T>) this.selectedRow.getParent();

			this.overRegistration = new HandlerRegistrationCollection();
			this.upRegistration = RootPanel.get().addDomHandler(this, MouseUpEvent.getType());
			this.rows = Lists.newArrayList(this.body.getRows());
			for (TableRow<T> row : this.rows) {
				this.overRegistration.add(row.addDomHandler(this, MouseOverEvent.getType()));
			}
			TableOrder.this.disableTextSelection(true);
			RootPanel.get().getElement().getStyle().setCursor(Style.Cursor.MOVE);
			StyleUtils.addStyle(this.selectedRow, TableOrder.STYLE_ROW_DRAGING);
		}

		@Override
		public void onMouseUp(MouseUpEvent event) {
			if (this.upRegistration != null) {
				this.upRegistration.removeHandler();
			}
			if (this.overRegistration != null) {
				this.overRegistration.removeHandler();
			}
			if (this.selectedRow != null) {
				StyleUtils.removeStyle(this.selectedRow, TableOrder.STYLE_ROW_DRAGING);
			}
			this.upRegistration = null;
			this.overRegistration = null;

			if (event != null) {
				event.stopPropagation();
				if (!Iterables.elementsEqual(this.rows, this.body.getRows())) {
					EventBus.get().fireEventFromSource(new RowOrderChangeEvent(this.body), TableOrder.this);
					this.body.setRowOrderDirty(true);
				}
			}

			this.body = null;
			this.hoverRow = null;
			this.selectedRow = null;
			TableOrder.this.disableTextSelection(false);

			RootPanel.get().getElement().getStyle().clearCursor();
		}

		@Override
		public void onMouseOver(MouseOverEvent event) {
			if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
				this.hoverRow = (TableRow<T>) event.getSource();
				this.body.switchRows(this.hoverRow, this.selectedRow);
			}
			event.preventDefault();
		}
	}

	private final Handler mouseHandler = new Handler();
	private final List<TDHandle> cells = Lists.newArrayList();

	public TableOrder() {
		this.setColumnVisibility(ColumnVisibility.HIDE_READONLY);
		this.setType(Type.ACTION);
	}

	protected TableOrder(TableOrder<T> source) {
		super(source);
	}

	@Override
	public IsWidget cloneWidget() {
		return new TableOrder<T>(this);
	}

	@Override
	public TableTH<T> doCreateHeaderCell() {
		TableTH<T> headerCell = new TableTH<T>();
		StyleUtils.addStyle(headerCell, TableOrder.STYLE_TABLE_ORDER);
		return headerCell;
	}

	@Override
	public AbstractTableCell<T> doCreateBodyCell() {
		TDHandle cell = new TDHandle();
		StyleUtils.addStyle(cell, TableOrder.STYLE_TABLE_ORDER);
		this.cells.add(cell);
		return cell;
	}

	public void disableTextSelection(boolean disable) {
		Element rootPanelElement = RootPanel.get().getElement();
		StyleUtils.toggleStyle(rootPanelElement, TableOrder.STYLE_NO_SELECTION, disable);
		SelectionUtils.disableTextSelectInternal(rootPanelElement, disable);
	}

	@Override
	public com.google.web.bindery.event.shared.HandlerRegistration addRowOrderChangeHandler(RowOrderChangeEvent.Handler handler) {
		return EventBus.get().addHandlerToSource(RowOrderChangeEvent.TYPE, this, handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		EventBus.get().fireEvent(event);
	}
}
