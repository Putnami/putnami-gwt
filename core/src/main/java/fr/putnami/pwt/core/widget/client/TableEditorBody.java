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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.putnami.pwt.core.editor.client.EditorCollection;
import fr.putnami.pwt.core.editor.client.EditorInput;
import fr.putnami.pwt.core.editor.client.EditorOutput;
import fr.putnami.pwt.core.editor.client.EditorValue;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.editor.client.Visitor;
import fr.putnami.pwt.core.editor.client.helper.MessageHelper;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.model.client.ModelDriver;
import fr.putnami.pwt.core.model.client.base.EditorModel;
import fr.putnami.pwt.core.model.client.base.HasDriver;
import fr.putnami.pwt.core.model.client.exception.EditorModelNotInitializedException;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.model.ModelCollection;
import fr.putnami.pwt.core.model.client.visitor.ReadonlyVisitor;
import fr.putnami.pwt.core.widget.client.base.AbstractTableColumn;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public class TableEditorBody<T> extends TableBody<T>
	implements HasDriver<Collection<T>, ModelDriver<Collection<T>>>, EditorOutput<Collection<T>>,
	EditorInput<Collection<T>>, EditorCollection<T>, EditorModel<T> {

	private final List<AbstractTableColumn<?>> columns = Lists.newArrayList();

	private MessageHelper messageHelper;
	private Model<T> model;
	private ModelDriver<Collection<T>> driver;

	private boolean rowOrderDirty;

	@UiConstructor
	public TableEditorBody(String bodyId) {
		super(bodyId);
	}

	protected TableEditorBody(TableEditorBody<T> source) {
		super(source);
		for (AbstractTableColumn<?> col : source.columns) {
			this.addColumn(WidgetUtils.cloneWidget(col));
		}
	}

	@Override
	public IsWidget cloneWidget() {
		return new TableEditorBody<T>(this);
	}

	@Override
	public Model<T> getModel() {
		return this.model;
	}

	@Override
	public void setMessageHelper(MessageHelper messageHelper) {
		this.messageHelper = messageHelper;
	}

	@Override
	public void initialize(Model<T> model, Visitor... visitors) {
		assert this.model == null : "model can not be set twice.";
		this.model = model;
		this.driver = new ModelDriver<Collection<T>>(new ModelCollection<T>(List.class, model));
		this.driver.setMessageHelper(this.messageHelper);
		this.driver.initialize(this, visitors);
	}

	@Override
	public ModelDriver<Collection<T>> getDriver() {
		return this.driver;
	}

	@Override
	public void setDriver(ModelDriver<Collection<T>> driver) {
		// Nothing to do, initialised method must be call in order to collect contexts
	}

	@Override
	public void setReadonly(Boolean readonly) {
		super.setReadonly(readonly);
		if (this.driver != null) {
			this.driver.accept(new ReadonlyVisitor(this, readonly, true));
		}
	}

	@Override
	public void add(IsWidget w) {
		super.add(w);
		if (w instanceof AbstractTableColumn) {
			this.addColumn((AbstractTableColumn) w);
		}
	}

	private <A> void addColumn(AbstractTableColumn<A> column) {
		this.columns.add(column);
	}

	public <A> void removeColumn(AbstractTableColumn<A> column) {
		this.columns.remove(column);
	}

	@Override
	public Collection<T> flush() {
		// FIXME flush is not called from the drivers, but from TableEditor
		List<T> values = (List<T>) getDriverOrThrow().getValue();
		values.clear();
		for (TableRow<T> row : this.getRowList()) {
			values.add(row.getValue());
		}
		return values;
	}

	@Override
	public void edit(Collection<T> value) {
		resetRows();
		getDriverOrThrow().edit(value);
	}

	private void resetRows() {
		if (rowOrderDirty) {
			Collections.sort(this.getRowList());
		}
		List<TableRow<T>> rows = this.getRowList();
		int i = 0;
		for (TableRow<T> row : rows) {
			row.setVisible(false);
			if (rowOrderDirty) {
				insert(row, ++i, true);
			}
		}
		this.rowOrderDirty = false;
	}

	@Override
	public LeafValueEditor<Collection<T>> asEditor() {
		return new TakesValueEditorWrapper<Collection<T>>(this);
	}

	@Override
	public <A extends EditorValue<T>> A getEditorForTraversal(int index) {
		TableRow<T> row = null;
		if (this.getRowList().size() > index) {
			row = this.getRowList().get(index);
		}
		if (row == null) {
			row = new TableRow<T>();
			row.setIndex(index);
			for (AbstractTableColumn<?> column : this.columns) {
				row.append(column.createBodyCell());
			}
			row.setReadonly(this.getReadonly());
			row.setMessageHelper(this.messageHelper);
			row.setPath(Strings.nullToEmpty(this.getPath()) + "[" + index + "]");

			this.addRow(row);
		}
		row.setVisible(true);
		return (A) row;
	}

	public void switchRows(TableRow<T> first, TableRow<T> second) {
		List<T> values = null;
		if (this.getValue() instanceof List) {
			values = (List<T>) this.getValue();
		}
		List<TableRow<T>> rows = this.getRowList();
		int firstIndex = rows.indexOf(first);
		int secondIndex = rows.indexOf(second);
		T firstVal = first.getValue();
		T secondVal = second.getValue();

		if (firstIndex == secondIndex) {
			return;
		} else if (firstIndex < secondIndex) {
			rows.remove(firstIndex);
			rows.add(firstIndex, second);
			rows.remove(secondIndex);
			rows.add(secondIndex, first);

			if (values != null) {
				values.remove(firstIndex);
				values.add(firstIndex, secondVal);
				values.remove(secondIndex);
				values.add(secondIndex, firstVal);
			}

			this.insert(second, firstIndex, true);
		} else {
			rows.remove(secondIndex);
			rows.add(secondIndex, first);
			rows.remove(firstIndex);
			rows.add(firstIndex, second);

			if (values != null) {
				values.remove(secondIndex);
				values.add(secondIndex, firstVal);
				values.remove(firstIndex);
				values.add(firstIndex, secondVal);
			}

			this.insert(first, secondIndex, true);
		}
	}

	@Override
	public Collection<T> getValue() {
		return getDriverOrThrow().getValue();
	}

	@Override
	public boolean hasErrors() {
		return this.driver == null ? false : this.driver.hasErrors();
	}

	@Override
	public Iterable<Error> getErrors() {
		return this.driver == null ? Collections.<Error> emptyList() : this.driver.getErrors();
	}

	@Override
	public void addValidator(Validator<Collection<T>> validator) {
		// TODO check if the validator must not be added to the driver.
	}

	private ModelDriver<Collection<T>> getDriverOrThrow() {
		if (this.driver == null) {
			throw new EditorModelNotInitializedException(this);
		}
		return this.driver;
	}

	public void setRowOrderDirty(boolean rowOrderDirty) {
		this.rowOrderDirty = rowOrderDirty;
	}
}
