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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;

import java.util.Collection;
import java.util.Collections;

import fr.putnami.pwt.core.editor.client.EditorCollection;
import fr.putnami.pwt.core.editor.client.EditorInput;
import fr.putnami.pwt.core.editor.client.EditorOutput;
import fr.putnami.pwt.core.editor.client.EditorValue;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.editor.client.Visitor;
import fr.putnami.pwt.core.editor.client.factory.InputFactory;
import fr.putnami.pwt.core.editor.client.factory.OutputFactory;
import fr.putnami.pwt.core.editor.client.helper.MessageHelper;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.editor.client.util.ValidationUtils;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.model.client.ModelDriver;
import fr.putnami.pwt.core.model.client.base.EditorModel;
import fr.putnami.pwt.core.model.client.base.EditorProvider;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.model.client.base.HasEditorProvider;
import fr.putnami.pwt.core.model.client.base.HasInputEditorFactory;
import fr.putnami.pwt.core.model.client.base.HasOutputEditorFactory;
import fr.putnami.pwt.core.model.client.exception.EditorModelNotInitializedException;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.model.ModelCollection;
import fr.putnami.pwt.core.model.client.visitor.ReadonlyVisitor;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.theme.client.IconFont;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent.Handler;
import fr.putnami.pwt.core.widget.client.util.AnchorUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputList<T> extends List
	implements EditorCollection<T>, EditorInput<Collection<T>>, EditorModel<T>, HasDrawable, HasEditorProvider,
	HasInputEditorFactory, HasOutputEditorFactory {

	private static final CssStyle STYLE_ITEM_CONTAINER = new SimpleStyle("list-element-container");
	private static final CssStyle STYLE_CLEAR = new SimpleStyle("clearfix");
	private static final CssStyle STYLE_CLOSE = new SimpleStyle("close");
	private static final CssStyle STYLE_ERROR = new SimpleStyle("has-error text-danger");

	private class NewListItem extends ListItem implements FocusHandler, Handler {

		private final Button<T> addButton;

		public NewListItem() {
			super();
			this.addButton = new Button<T>();
			this.addButton.setType(Button.Type.LINK);
			this.addButton.setSize(Button.Size.SMALL);
			this.addButton.setIconType(IconFont.ICON_ADD);
			this.add(this.addButton);
			this.setTabIndex(0);
			this.addButton.addButtonHandler(this);
			this.addFocusHandler(this);
		}

		@Override
		public void onFocus(FocusEvent event) {
			InternalListItem next = InputList.this.getEditorForTraversal(InputList.this.items.size());
			next.onFocus(null);
		}

		@Override
		public void onButtonAction(ButtonEvent event) {
			InternalListItem next = InputList.this.getEditorForTraversal(InputList.this.items.size());
			next.onFocus(null);
		}
	}

	private class InternalListItem extends ListItem implements EditorValue<T>, BlurHandler, FocusHandler {

		private final HandlerRegistrationCollection registrationCollection = new HandlerRegistrationCollection();

		private final Anchor<?> deleteButton = new Anchor<>("&times;");
		private final SimplePanel container = new SimplePanel();
		private final SimplePanel clear = new SimplePanel();
		private EditorOutput<T> output;
		private EditorInput<T> input;

		private T itemValue;

		private boolean focused = false;

		public InternalListItem() {
			super();
			this.resetFocusHandler();
			StyleUtils.addStyle(this.container, InputList.STYLE_ITEM_CONTAINER);
			StyleUtils.addStyle(this.deleteButton, InputList.STYLE_CLOSE);
			StyleUtils.addStyle(this.clear, InputList.STYLE_CLEAR);
			this.deleteButton.setTabIndex(-1);
			this.deleteButton.setLink(AnchorUtils.DUMMY_HREF);
			this.deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					InputList.this.removeEditor(InternalListItem.this);
				}
			});

			this.add(this.deleteButton);
			this.add(this.container);
			this.add(this.clear);
		}

		@Override
		public String getPath() {
			return "[" + InputList.this.items.indexOf(this) + "]";
		}

		public T flush() {
			if (this.input != null) {
				this.itemValue = this.input.flush();
			}
			return this.itemValue;
		}

		@Override
		public T getValue() {
			return this.itemValue;
		}

		@Override
		public void edit(T value) {
			this.itemValue = value;
			StyleUtils.toggleStyle(this.container, InputList.STYLE_ERROR, false);
			this.redraw();
		}

		@Override
		public LeafValueEditor<T> asEditor() {
			return new TakesValueEditorWrapper<T>(this);
		}

		@Override
		public void onBlur(BlurEvent event) {
			this.focused = false;
			if (this.input != null) {
				T val = this.input.flush();
				StyleUtils.toggleStyle(this.container, InputList.STYLE_ERROR, this.hasErrors());
				if (val == null && !this.hasErrors()) {
					InputList.this.removeEditor(this);
				} else {
					this.itemValue = val;
					this.output.edit(val);
				}
			}
			this.resetFocusHandler();
			this.redraw();
		}

		@Override
		public void onFocus(FocusEvent event) {
			if (this.input == null) {
				this.input = InputList.this.editorProvider.getEditorForTraversal(false, InputList.this.items.indexOf(this));
			}
			if (!this.hasErrors()) {
				this.input.edit(this.itemValue);
			}
			this.focused = true;
			this.resetFocusHandler();
			this.redraw();
		}

		private void resetFocusHandler() {
			this.registrationCollection.removeHandler();
			boolean hasError = this.hasErrors();
			if (!hasError && !this.focused) {
				this.setTabIndex(0);
				this.registrationCollection.add(this.addFocusHandler(this));
			} else if (hasError && !this.focused) {
				this.setTabIndex(-1);
				if (this.input instanceof HasFocusHandlers) {
					this.registrationCollection.add(((HasFocusHandlers) this.input).addFocusHandler(this));
				}
			} else {
				this.setTabIndex(-1);
				if (this.input instanceof HasBlurHandlers) {
					this.registrationCollection.add(((HasBlurHandlers) this.input).addBlurHandler(this));
				}
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						if (InternalListItem.this.input instanceof Focusable) {
							((Focusable) InternalListItem.this.input).setFocus(true);
						}
					}
				});
			}
		}

		@Override
		public void redraw() {
			if (this.output == null) {
				this.output = InputList.this.editorProvider.getEditorForTraversal(true, InputList.this.items.indexOf(this));
			}
			this.output.edit(this.itemValue);
			if (this.input != null && !this.hasErrors()) {
				this.input.edit(this.itemValue);
			}

			if (this.focused || this.hasErrors()) {
				this.container.setWidget(this.input);
			} else {
				this.container.setWidget(this.output);
			}
		}

		private Iterable<? extends Error> getErrors() {
			return this.input.getErrors();
		}

		private boolean hasErrors() {
			return this.input != null && this.input.hasErrors();
		}
	}

	private final java.util.List<InternalListItem> items = Lists.newArrayList();
	private final NewListItem nextItem = new NewListItem();

	private String path;
	private Model<T> model;
	private ModelDriver<Collection<T>> driver;
	private MessageHelper messageHelper;

	private OutputFactory outputFactory;
	private InputFactory inputFactory;

	private EditorProvider editorProvider;

	private Collection<Error> errors;
	private Collection<Validator<Collection<T>>> validators;

	private Collection<T> value;

	public InputList() {
		this.setType(Type.LIST);
		this.append(this.nextItem);
	}

	protected InputList(InputList<T> source) {
		this();
		this.path = source.path;
		this.model = source.model;
		this.messageHelper = source.messageHelper;
		this.outputFactory = source.outputFactory;
		this.inputFactory = source.inputFactory;

		if (source.validators != null) {
			for (Validator<Collection<T>> validator : source.validators) {
				this.addValidator(validator);
			}
		}
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputList<T>(this);
	}

	@Override
	public void initialize(Model<T> model, Visitor... visitors) {
		assert this.model == null : "model can not be set twice.";
		this.model = model;
		if (model instanceof ModelCollection) {
			this.driver = new ModelDriver<Collection<T>>((ModelCollection<T>) model);
		} else {
			this.driver = new ModelDriver<Collection<T>>(new ModelCollection<T>(List.class, model));
		}
		this.driver.setMessageHelper(this.messageHelper);
		this.driver.initialize(this, visitors);
		this.driver.accept(new ReadonlyVisitor(this, true, true));
	}

	@Override
	public <A extends EditorValue<T>> A getEditorForTraversal(int index) {
		InternalListItem editor = null;
		if (this.items.size() > index) {
			editor = this.items.get(index);
		} else {
			editor = new InternalListItem();
			this.items.add(editor);
			this.addListItem(editor);
			this.getElement().insertBefore(editor.getElement(), this.nextItem.getElement());
		}
		return (A) editor;
	}

	private void removeEditor(InternalListItem editor) {
		this.items.remove(editor);
		super.remove(editor);
	}

	@Override
	public void edit(Collection<T> value) {
		if (this.errors != null) {
			this.errors.clear();
			this.errors = null;
		}
		this.value = value;
		this.clear();
		this.items.clear();
		this.addListItem(this.nextItem);
		getDriverOrThrow().edit(value);
	}

	@Override
	public LeafValueEditor<Collection<T>> asEditor() {
		return new TakesValueEditorWrapper<Collection<T>>(this);
	}

	@Override
	public void add(IsWidget child) {
		boolean mustAdd = true;
		if (this.inputFactory == null && child instanceof InputFactory) {
			this.inputFactory = (InputFactory) child;
			mustAdd = false;
		}
		if (this.outputFactory == null && child instanceof OutputFactory) {
			this.outputFactory = (OutputFactory) child;
			mustAdd = false;
		}
		if (mustAdd) {
			this.append(child);
		}
	}

	@UiChild(tagname = "input")
	public void addInput(InputFactory inputFactory) {
		this.inputFactory = inputFactory;
	}

	@UiChild(tagname = "output")
	public void addOutput(OutputFactory outputFactory) {
		this.outputFactory = outputFactory;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public Collection<T> flush() {
		if (this.errors != null) {
			this.errors.clear();
			this.errors = null;
		}

		Collection<T> result = getDriverOrThrow().getModel().newInstance();
		for (InternalListItem itemEditor : this.items) {
			result.add(itemEditor.flush());
			if (itemEditor.hasErrors()) {
				if (this.errors == null) {
					this.errors = Lists.newArrayList();
				}
				Iterables.addAll(this.errors, itemEditor.getErrors());
			}
		}
		if (this.errors == null && this.validators != null) {
			this.errors = ValidationUtils.validate(this.validators, this, result);
		}
		if (this.errors != null) {
			result = this.value;
		}
		return result;
	}

	@Override
	public void setEditorProvider(EditorProvider provider) {
		this.editorProvider = provider;
	}

	@Override
	public void setMessageHelper(MessageHelper messageHelper) {
		this.messageHelper = messageHelper;
	}

	@Override
	public Model<T> getModel() {
		return this.model;
	}

	@Override
	public Collection<T> getValue() {
		return getDriverOrThrow().getValue();
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public OutputFactory getOutputFactory() {
		return this.outputFactory;
	}

	@Override
	public InputFactory getInputFactory() {
		return this.inputFactory;
	}

	@Override
	public boolean hasErrors() {
		return this.errors != null && !this.errors.isEmpty();
	}

	@Override
	public Iterable<Error> getErrors() {
		return this.errors == null ? Collections.<Error> emptyList() : Iterables.unmodifiableIterable(this.errors);
	}

	@Override
	public void addValidator(Validator<Collection<T>> validator) {
		if (this.validators == null) {
			this.validators = Lists.newArrayList();
		}
		this.validators.add(validator);
	}

	@Override
	public void redraw() {
		// NoOp
	}

	private ModelDriver<Collection<T>> getDriverOrThrow() {
		if (this.driver == null) {
			throw new EditorModelNotInitializedException(this);
		}
		return this.driver;
	}

}
