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
package fr.putnami.pwt.core.widget.client.base;

import java.util.Collections;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.EditorInput;
import fr.putnami.pwt.core.editor.client.EditorLeaf;
import fr.putnami.pwt.core.editor.client.EditorOutput;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.editor.client.Visitor;
import fr.putnami.pwt.core.editor.client.event.DataValidationEvent;
import fr.putnami.pwt.core.editor.client.event.DataValidationEvent.HasDataValidationHandlers;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent.Handler;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent.HasDirtyHandlers;
import fr.putnami.pwt.core.editor.client.event.FlushErrorEvent;
import fr.putnami.pwt.core.editor.client.event.FlushErrorEvent.HasFlushErrorHandlers;
import fr.putnami.pwt.core.editor.client.event.FlushSuccessEvent;
import fr.putnami.pwt.core.editor.client.event.FlushSuccessEvent.HasFlushSuccessHandlers;
import fr.putnami.pwt.core.editor.client.event.ResetDisplayEvent;
import fr.putnami.pwt.core.editor.client.event.ResetDisplayEvent.HasResetDisplayHandlers;
import fr.putnami.pwt.core.editor.client.helper.MessageHelper;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.ModelDriver;
import fr.putnami.pwt.core.model.client.base.EditorModel;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.model.client.base.HasDriver;
import fr.putnami.pwt.core.model.client.base.HasReadonly;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.visitor.ReadonlyVisitor;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public abstract class AbstractForm<T> extends AbstractHTMLPanel implements
		EditorLeaf,
		EditorModel<T>,
		EditorOutput<T>,
		EditorInput<T>,
		HasReadonly,
		HasDrawable,
		HasDriver<T, ModelDriver<T>>,
		HasFormType,
		HasDirtyHandlers,
		HasFlushSuccessHandlers,
		HasFlushErrorHandlers,
		HasResetDisplayHandlers,
		HasDataValidationHandlers
{

	public enum Type implements CssStyle {

		BASIC("form"),
		INLINE("form-inline"),
		HORIZONTAL("form-horizontal");

		private final String style;

		private Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return style;
		}

	}

	private MessageHelper messageHelper;
	private Model<T> model;
	private ModelDriver<T> driver;

	private Boolean readonly;

	private Type type;

	public AbstractForm(String tag, String html) {
		super(tag, html);
	}

	protected AbstractForm(AbstractForm<T> source) {
		super(source);
		this.messageHelper = source.messageHelper;
		this.readonly = source.readonly;
		this.type = source.type;
	}

	@Override
	public void addAndReplaceElement(Widget widget, com.google.gwt.user.client.Element toReplace) {
		refreshType(widget);
		super.addAndReplaceElement(widget, toReplace);
	}

	@Override
	public ModelDriver<T> getDriver() {
		return driver;
	}

	@Override
	public void setDriver(ModelDriver<T> driver) {
		// Nothing to do, initialised method must be call in order to collect contexts
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
		this.driver.setMessageHelper(messageHelper);
		this.driver.initialize(this, visitors);
		this.driver.accept(new ReadonlyVisitor(this, readonly, false));
	}

	@Override
	public T flush() {
		T value = this.driver.flush();
		return value;
	}

	@Override
	public void edit(T object) {
		this.driver.edit(object);
	}

	@Override
	public Model<T> getModel() {
		return this.model;
	}

	@Override
	public Boolean getReadonly() {
		return readonly;
	}

	@Override
	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
		if (this.driver != null) {
			this.driver.accept(new ReadonlyVisitor(this, readonly, false));
		}
	}

	@Override
	public T getValue() {
		return driver.getValue();
	}

	public boolean hasError() {
		return driver.hasErrors();
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void setType(Type type) {
		this.type = type;
		StyleUtils.addStyle(this, type);
		Set<Widget> children = WidgetUtils.listChildren(this);
		for (Widget w : children) {
			refreshType(w);
		}
	}

	@Override
	public boolean hasErrors() {
		return driver == null ? false : driver.hasErrors();
	}

	@Override
	public Iterable<Error> getErrors() {
		return driver == null ? Collections.EMPTY_LIST : driver.getErrors();
	}

	@Override
	public void addValidator(Validator<T> validator) {
	}

	@Override
	public void redraw() {
	}

	private void refreshType(Widget w) {
		if (type != null && w instanceof HasFormType && ((HasFormType) w).getType() == null) {
			((HasFormType) w).setType(type);
		}
	}

	@Override
	public boolean isDirty() {
		return driver != null && driver.isDirty();
	}

	@Override
	public HandlerRegistration addDirtyHandler(Handler handler) {
		return EventBus.get().addHandlerToSource(DirtyEvent.TYPE, this, handler);
	}

	@Override
	public HandlerRegistration addFlushHandler(FlushSuccessEvent.Handler handler) {
		return EventBus.get().addHandlerToSource(FlushSuccessEvent.TYPE, this, handler);
	}

	@Override
	public HandlerRegistration addFlushErrorHandler(FlushErrorEvent.Handler handler) {
		return EventBus.get().addHandlerToSource(FlushErrorEvent.TYPE, this, handler);
	}

	@Override
	public HandlerRegistration addResetDisplayHandler(ResetDisplayEvent.Handler handler) {
		return EventBus.get().addHandlerToSource(ResetDisplayEvent.TYPE, this, handler);
	}

	@Override
	public HandlerRegistration addValidationHandler(DataValidationEvent.Handler handler) {
		return EventBus.get().addHandlerToSource(DataValidationEvent.TYPE, this, handler);
	}
}
