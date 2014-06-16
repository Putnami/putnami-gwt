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

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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

import fr.putnami.pwt.core.editor.client.EditorCollection;
import fr.putnami.pwt.core.editor.client.EditorInput;
import fr.putnami.pwt.core.editor.client.EditorOutput;
import fr.putnami.pwt.core.editor.client.EditorValue;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.editor.client.Visitor;
import fr.putnami.pwt.core.editor.client.factory.InputFactory;
import fr.putnami.pwt.core.editor.client.factory.OutputFactory;
import fr.putnami.pwt.core.editor.client.helper.MessageHelper;
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
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.model.ModelCollection;
import fr.putnami.pwt.core.model.client.visitor.ReadonlyVisitor;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.constant.IconType;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent.Handler;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputList<T> extends List implements
		EditorCollection<T>,
		EditorInput<Collection<T>>,
		EditorModel<T>,
		HasDrawable,
		HasEditorProvider,
		HasInputEditorFactory<T>,
		HasOutputEditorFactory<T>
{

	private static final CssStyle STYLE_ITEM_CONTAINER = new SimpleStyle("list-element-container");
	private static final CssStyle STYLE_CLEAR = new SimpleStyle("clearfix");
	private static final CssStyle STYLE_CLOSE = new SimpleStyle("close");
	private static final CssStyle STYLE_ERROR = new SimpleStyle("has-error text-danger");

	private class NewListItem extends ListItem implements HasDrawable, FocusHandler, Handler {

		private final Button<T> addButton;

		public NewListItem() {
			super();
			addButton = new Button<T>();
			addButton.setType(Button.Type.LINK);
			addButton.setSize(Button.Size.SMALL);
			addButton.setIconType(IconType.ADD);
			add(addButton);
			setTabIndex(0);
			addButton.addButtonHandler(this);
			addFocusHandler(this);
		}

		@Override
		public void onFocus(FocusEvent event) {
			InternalListItem next = getEditorForTraversal(items.size());
			next.onFocus(null);
		}

		@Override
		public void onButtonAction(ButtonEvent event) {
			InternalListItem next = getEditorForTraversal(items.size());
			next.onFocus(null);
		}

	}

	private class InternalListItem extends ListItem implements HasDrawable, EditorValue<T>, BlurHandler, FocusHandler {

		private final HandlerRegistrationCollection registrationCollection = new HandlerRegistrationCollection();

		private final Anchor deleteButton = new Anchor("&times;");
		private final SimplePanel container = new SimplePanel();
		private final SimplePanel clear = new SimplePanel();
		private EditorOutput<T> output;
		private EditorInput<T> input;

		private T value;

		private boolean focused = false;

		public InternalListItem() {
			super();
			resetFocusHandler();
			StyleUtils.addStyle(container, STYLE_ITEM_CONTAINER);
			StyleUtils.addStyle(deleteButton, STYLE_CLOSE);
			StyleUtils.addStyle(clear, STYLE_CLEAR);
			deleteButton.setTabIndex(-1);
			deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					removeEditor(InternalListItem.this);
				}
			});

			add(deleteButton);
			add(container);
			add(clear);
		}

		@Override
		public String getPath() {
			return "[" + items.indexOf(this) + "]";
		}

		public T flush() {
			if (input != null) {
				value = input.flush();
			}
			return value;
		}

		@Override
		public T getValue() {
			return value;
		}

		@Override
		public void edit(T value) {
			this.value = value;
			StyleUtils.toggleStyle(container, STYLE_ERROR, false);
			redraw();
		}

		@Override
		public void onBlur(BlurEvent event) {
			focused = false;
			if (input != null) {
				T value = input.flush();
				StyleUtils.toggleStyle(container, STYLE_ERROR, hasErrors());
				if (value == null && !hasErrors()) {
					removeEditor(this);
				}
				else {
					this.value = value;
					output.edit(value);
				}
			}
			resetFocusHandler();
			redraw();
		}

		@Override
		public void onFocus(FocusEvent event) {
			if (input == null) {
				input = editorProvider.getEditorForTraversal(false, items.indexOf(this));
			}
			if (!hasErrors()) {
				input.edit(value);
			}
			focused = true;
			resetFocusHandler();
			redraw();
		}

		private void resetFocusHandler() {
			registrationCollection.removeHandler();
			boolean hasError = hasErrors();
			if (!hasError && !focused) {
				setTabIndex(0);
				registrationCollection.add(addFocusHandler(this));
			}
			else if (hasError && !focused) {
				setTabIndex(-1);
				if (input instanceof HasFocusHandlers) {
					registrationCollection.add(((HasFocusHandlers) input).addFocusHandler(this));
				}
			}
			else {
				setTabIndex(-1);
				if (input instanceof HasBlurHandlers) {
					registrationCollection.add(((HasBlurHandlers) input).addBlurHandler(this));
				}
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						if (input instanceof Focusable) {
							((Focusable) input).setFocus(true);
						}
					}
				});
			}
		}

		@Override
		public void redraw() {
			if (output == null) {
				output = editorProvider.getEditorForTraversal(true, items.indexOf(this));
			}
			output.edit(value);
			if (input != null && !hasErrors()) {
				input.edit(value);
			}

			if (focused || hasErrors()) {
				container.setWidget(input);
			}
			else {
				container.setWidget(output);
			}

		}

		private Iterable<? extends Error> getErrors() {
			return input.getErrors();
		}

		private boolean hasErrors() {
			return input != null && input.hasErrors();
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
		setType(Type.LIST);
		append(nextItem);
	}

	protected InputList(InputList<T> source) {
		this();
		this.path = source.path;
		this.model = source.model;
		this.messageHelper = source.messageHelper;
		this.outputFactory = source.outputFactory;
		this.inputFactory = source.inputFactory;

		if (source.validators != null) {
			for (Validator validator : source.validators) {
				addValidator(validator);
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
		}
		else {
			this.driver = new ModelDriver<Collection<T>>(new ModelCollection<T>(List.class, model));
		}
		this.driver.setMessageHelper(messageHelper);
		this.driver.initialize(this, visitors);
		this.driver.accept(new ReadonlyVisitor(this, true, true));
	}

	@Override
	public <A extends EditorValue<T>> A getEditorForTraversal(int index) {
		InternalListItem editor = null;
		if (items.size() > index) {
			editor = items.get(index);
		}
		else {
			editor = new InternalListItem();
			items.add(editor);
			addListItem(editor);
			getElement().insertBefore(editor.getElement(), nextItem.getElement());
		}
		return (A) editor;
	}

	private void removeEditor(InternalListItem editor) {
		items.remove(editor);
		super.remove(editor);
	}

	@Override
	public void edit(Collection<T> value) {
		this.value = value;
		clear();
		items.clear();
		addListItem(nextItem);
		this.driver.edit(value);
	}

	@Override
	public void add(IsWidget child) {
		boolean mustAdd = true;
		if (inputFactory == null && child instanceof InputFactory) {
			this.inputFactory = (InputFactory) child;
			mustAdd = false;
		}
		if (outputFactory == null && child instanceof OutputFactory) {
			this.outputFactory = (OutputFactory) child;
			mustAdd = false;
		}
		if (mustAdd) {
			append(child);
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
		return path;
	}

	@Override
	public Collection<T> flush() {
		if (errors != null) {
			errors.clear();
			errors = null;
		}

		Collection<T> result = this.driver.getModel().newInstance();
		for (InternalListItem itemEditor : items) {
			result.add(itemEditor.flush());
			if (itemEditor.hasErrors()) {
				if (errors == null) {
					errors = Lists.newArrayList();
				}
				Iterables.addAll(errors, itemEditor.getErrors());
			}
		}
		if (errors == null && validators != null) {
			this.errors = ValidationUtils.validate(validators, this, result);
		}
		if (errors != null) {
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
		return model;
	}

	@Override
	public Collection<T> getValue() {
		return this.driver.getValue();
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public OutputFactory getOutputFactory() {
		return outputFactory;
	}

	@Override
	public InputFactory getInputFactory() {
		return inputFactory;
	}

	@Override
	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}

	@Override
	public Iterable<Error> getErrors() {
		return errors == null ? Collections.<Error> emptyList() : Iterables.unmodifiableIterable(errors);
	}

	@Override
	public void addValidator(Validator<Collection<T>> validator) {
		if (validators == null) {
			validators = Lists.newArrayList();
		}
		validators.add(validator);
	}

	@Override
	public void redraw() {
	}

}
