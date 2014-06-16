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
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.widget.client.base.AbstractTableCell;
import fr.putnami.pwt.core.widget.client.base.AbstractTableColumn;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.constant.IconType;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class TableOrder<T> extends AbstractTableColumn<T> {

	private static final CssStyle STYLE_ROW_DRAGING = new SimpleStyle("success");

	private static final CssStyle STYLE_NO_SELECTION = new SimpleStyle("no-selection");

	private class TDHandle extends AbstractTableCell<T> {

		private final Icon dragIcon;

		public TDHandle() {
			super(TableCellElement.TAG_TD);
			dragIcon = new Icon();
			dragIcon.setType(IconType.DRAG);
			dragIcon.addDomHandler(mouseHandler, MouseDownEvent.getType());
			dragIcon.addDomHandler(mouseHandler, ClickEvent.getType());
			append(dragIcon);
		}

	}

	private class Handler implements MouseDownHandler, MouseOverHandler, MouseUpHandler, ClickHandler {

		private TableEditorBody<T> body;
		private TableRow<T> hoverRow;
		private TableRow<T> selectedRow;
		private Map<TableRow<T>, Integer> rows = Maps.newHashMap();

		private HandlerRegistration upRegistration;
		private HandlerRegistrationCollection overRegistration;

		@Override
		public void onClick(ClickEvent event) {
			event.stopPropagation();
		}

		@Override
		public void onMouseDown(MouseDownEvent event) {
			onMouseUp(null);

			Icon dragIcon = (Icon) event.getSource();
			selectedRow = (TableRow<T>) dragIcon.getParent().getParent();
			body = (TableEditorBody<T>) selectedRow.getParent();

			int i = 0;
			overRegistration = new HandlerRegistrationCollection();
			upRegistration = RootPanel.get().addDomHandler(this, MouseUpEvent.getType());
			for (TableRow row : body.getRows()) {
				rows.put(row, i++);
				overRegistration.add(row.addDomHandler(this, MouseOverEvent.getType()));
			}
			disableTextSelection(true);
			RootPanel.get().getElement().getStyle().setCursor(Style.Cursor.MOVE);
			StyleUtils.addStyle(selectedRow, STYLE_ROW_DRAGING);
		}

		@Override
		public void onMouseUp(MouseUpEvent event) {
			if (upRegistration != null) {
				upRegistration.removeHandler();
			}
			if (overRegistration != null) {
				overRegistration.removeHandler();
			}
			if (selectedRow != null) {
				StyleUtils.removeStyle(selectedRow, STYLE_ROW_DRAGING);
			}
			upRegistration = null;
			overRegistration = null;

			body = null;
			hoverRow = null;
			selectedRow = null;
			disableTextSelection(false);
			RootPanel.get().getElement().getStyle().clearCursor();
			if (event != null) {
				event.stopPropagation();
			}
		}

		@Override
		public void onMouseOver(MouseOverEvent event) {
			if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
				hoverRow = (TableRow) event.getSource();
				body.switchRows(hoverRow, selectedRow);
			}
			event.preventDefault();
		}
	}

	private final Handler mouseHandler = new Handler();
	private final List<TDHandle> cells = Lists.newArrayList();

	public TableOrder() {

	}

	protected TableOrder(TableOrder<T> source) {
		super(source);
	}

	@Override
	public IsWidget cloneWidget() {
		return new TableOrder<T>(this);
	}

	@Override
	public TableTH<T> createHeaderCell() {
		TableTH<T> headerCell = new TableTH<T>();
		return headerCell;
	}

	@Override
	public AbstractTableCell<T> createBodyCell() {
		TDHandle cell = new TDHandle();
		cells.add(cell);
		return cell;
	}

	public void disableTextSelection(boolean disable) {
		Element rootPanelElement = RootPanel.get().getElement();
		StyleUtils.toggleStyle(rootPanelElement, STYLE_NO_SELECTION, disable);
		disableTextSelectInternal(rootPanelElement, disable);
	}

	private native static void disableTextSelectInternal(Element e, boolean disable)
	/*-{ 
	    if (disable) { 
	      e.ondrag = function () { return false; }; 
	      e.onselectstart = function () { return false; }; 
	    } else { 
	      e.ondrag = null; 
	      e.onselectstart = null; 
	    } 
	  }-*/;

}
