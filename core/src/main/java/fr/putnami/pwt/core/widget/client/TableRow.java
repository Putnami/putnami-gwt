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

import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.Collections;

import fr.putnami.pwt.core.editor.client.EditorInput;
import fr.putnami.pwt.core.editor.client.EditorLeaf;
import fr.putnami.pwt.core.editor.client.EditorOutput;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.editor.client.Visitor;
import fr.putnami.pwt.core.editor.client.helper.MessageHelper;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.model.client.ModelDriver;
import fr.putnami.pwt.core.model.client.base.EditorModel;
import fr.putnami.pwt.core.model.client.base.HasReadonly;
import fr.putnami.pwt.core.model.client.exception.EditorModelNotInitializedException;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.visitor.ReadonlyVisitor;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.AbstractTableCell;

public class TableRow<T> extends AbstractPanel
	implements EditorOutput<T>, EditorInput<T>, EditorLeaf, EditorModel<T>, HasReadonly, HasClickHandlers, Comparable<TableRow<T>> {

	private MessageHelper messageHelper;
	private Model<T> model;
	private ModelDriver<T> driver;

	private int index;

	private Boolean readonly;

	public TableRow() {
		super(TableRowElement.TAG);
	}

	protected TableRow(TableRow<T> source) {
		super(source);
		this.cloneSourceWidgets(source);
	}

	@Override
	public IsWidget cloneWidget() {
		return new TableRow<T>(this);
	}

	@Override
	public void add(IsWidget child) {
		if (child instanceof AbstractTableCell || child instanceof TableTH) {
			this.append(child);
		}
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
		this.driver = new ModelDriver<T>(model);
		this.driver.setMessageHelper(this.messageHelper);
		this.driver.initialize(this, visitors);
	}

	@Override
	public Boolean getReadonly() {
		return this.readonly;
	}

	@Override
	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
		if (this.driver != null) {
			this.driver.accept(new ReadonlyVisitor(this, readonly, true));
		}
	}

	@Override
	public T flush() {
		return getDriverOrThrow().flush();
	}

	@Override
	public void edit(T value) {
		getDriverOrThrow().edit(value);
	}

	@Override
	public LeafValueEditor<T> asEditor() {
		return new TakesValueEditorWrapper<T>(this);
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public T getValue() {
		return getDriverOrThrow().getValue();
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return this.addDomHandler(handler, ClickEvent.getType());
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
	public void addValidator(Validator<T> validator) {
		// TODO check if the validator must not be added to the driver.
	}

	private ModelDriver<T> getDriverOrThrow() {
		if (this.driver == null) {
			throw new EditorModelNotInitializedException(this);
		}
		return this.driver;
	}

	@Override
	public int compareTo(TableRow<T> o) {
		return Integer.compare(index, o.index);
	}
}
