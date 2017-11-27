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
package fr.putnami.pwt.core.widget.client.base;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.text.ParseException;

import fr.putnami.pwt.core.editor.client.event.DirtyEvent;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent.Handler;
import fr.putnami.pwt.core.editor.client.util.ValidationUtils;
import fr.putnami.pwt.core.editor.client.validator.SizeValidator;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.model.client.base.HasPlaceholder;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.event.ChangeEvent;
import fr.putnami.pwt.core.widget.client.helper.ToStringRenderer;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public abstract class AbstractInputBox<T extends TextBoxBase, I> extends AbstractInput<I>
	implements HasPlaceholder, HasEnabled, HasChangeHandlers {

	public enum Size implements CssStyle {

			DEFAULT(null),
			LARGE("input-lg"),
			SMALL("input-sm");

		private final String style;

		Size(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private class KeyUpDirtyHandler implements KeyUpHandler {

		@Override
		public void onKeyUp(KeyUpEvent event) {
			boolean fireDirty = false;
			if (AbstractInputBox.this.currentStringValue == null) {
				fireDirty = !Strings.isNullOrEmpty(AbstractInputBox.this.input.getText());
			} else {
				fireDirty = !AbstractInputBox.this.currentStringValue.equals(AbstractInputBox.this.input.getText());
			}
			if (fireDirty) {
				DirtyEvent.fire(AbstractInputBox.this);
			}
		}
	}

	private static final String ERROR_PARSING = "inputParsing";

	private final T input;

	private HandlerRegistration valueChangeRegistration;
	private HandlerRegistration keyupChangeRegistration;

	private String inputType;

	private Parser<I> parser;
	private Renderer<I> renderer = ToStringRenderer.<I> get();
	private String placeholder;

	private Size size;

	private String currentStringValue;

	public AbstractInputBox(T input) {
		super(input);
		this.input = input;
	}

	protected AbstractInputBox(T input, AbstractInputBox<T, I> source) {
		super(input, source);
		this.input = input;
		this.setPlaceholder(source.placeholder);
		this.parser = source.parser;
		this.renderer = source.renderer;

		this.setSize(source.size);
		this.endConstruct();
	}

	public Parser<I> getParser() {
		return this.parser;
	}

	public void setParser(Parser<I> parser) {
		this.parser = parser;
	}

	public Renderer<I> getRenderer() {
		return this.renderer;
	}

	public void setRenderer(Renderer<I> renderer) {
		this.renderer = renderer;
	}

	public Size getSize() {
		return this.size;
	}

	public void setSize(Size size) {
		this.size = size;
		StyleUtils.addStyle(this, size);
	}

	public T getInput() {
		return this.input;
	}

	public String getType() {
		return this.inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
		this.getElement().setAttribute("type", inputType);
	}

	@Override
	public String getPlaceholder() {
		return this.placeholder;
	}

	@Override
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
		if (placeholder == null) {
			this.getElement().removeAttribute(HasPlaceholder.PLACEHOLDER_ATTRIBUTE);
		} else {
			this.getElement().setAttribute(HasPlaceholder.PLACEHOLDER_ATTRIBUTE, placeholder);
		}
	}

	@Override
	public boolean isDirty() {
		return !Objects.equal(this.getValue(), this.input.getValue());
	}

	@Override
	public HandlerRegistration addDirtyHandler(Handler handler) {
		if (this.valueChangeRegistration == null) {
			this.valueChangeRegistration = this.input.addValueChangeHandler(new ChangeEvent<String>(this));
		}
		if (this.keyupChangeRegistration == null) {
			this.keyupChangeRegistration = this.input.addKeyUpHandler(new KeyUpDirtyHandler());
		}
		return super.addDirtyHandler(handler);
	}

	@Override
	public I flush() {
		this.clearErrors();
		String strValue = this.getInput().getValue();
		I value = this.getValue();
		try {
			value = this.parser.parse(strValue);
			this.validate(value);
			if (!this.hasErrors()) {
				this.setValue(value);
			}
		} catch (ParseException e) {
			this.addError(ValidationUtils.createError(this, AbstractInputBox.ERROR_PARSING, this.getValue(), strValue));
		}
		return this.getValue();
	}

	@Override
	public void edit(I value) {
		this.edit(value, false);
	}

	@Override
	public void setValue(I value) {
		super.setValue(value);
		this.currentStringValue = this.renderer.render(value);
	}

	public void edit(I value, boolean fireChangeEvents) {
		this.clearErrors();
		this.setValue(value);
		this.input.setValue(currentStringValue, fireChangeEvents);
	}

	@Override
	public void addValidator(Validator<I> validator) {
		super.addValidator(validator);
		if (validator instanceof SizeValidator) {
			SizeValidator<String> sizeValidator = (SizeValidator<String>) validator;
			if (sizeValidator.getMax() > 0) {
				this.setMaxLength(sizeValidator.getMax());
			}
		}
	}

	public void setMaxLength(int maxLength) {
		InputElement.as(this.input.getElement()).setMaxLength(maxLength);
	}

	public int getMaxLength() {
		return InputElement.as(this.input.getElement()).getMaxLength();
	}

	@Override
	public boolean isEnabled() {
		return this.input.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.input.setEnabled(enabled);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return this.input.addChangeHandler(handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addValueChangeHandler(ValueChangeHandler<I> handler) {
		return this.input.addHandler(handler, ValueChangeEvent.getType());
	}

}
