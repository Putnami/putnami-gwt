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

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

import java.util.Collection;

import fr.putnami.pwt.core.widget.client.assist.AbstractContentAssistHandler;
import fr.putnami.pwt.core.widget.client.assist.ContentAssistAspect;
import fr.putnami.pwt.core.widget.client.assist.ContentAssistHandler;
import fr.putnami.pwt.core.widget.client.assist.OracleWrapper;
import fr.putnami.pwt.core.widget.client.assist.SimpleOracle;
import fr.putnami.pwt.core.widget.client.base.AbstractInputBox;
import fr.putnami.pwt.core.widget.client.helper.CompositeFocusHelper;
import fr.putnami.pwt.core.widget.client.helper.StringParser;
import fr.putnami.pwt.core.widget.client.helper.ToStringRenderer;
import fr.putnami.pwt.core.widget.shared.assist.Oracle;

public class InputSuggest<T> extends AbstractInputBox<TextBox, T> {

	private final OracleWrapper<T> oracle;
	private ContentAssistHandler<T> assistHandler;
	private ContentAssistAspect<T> assistAspect;

	private CompositeFocusHelper compositeFocus;

	private T currentValue;

	private Oracle.Highlighter<T> highlighter = new Oracle.Highlighter<T>() {

		@Override
		public String highlight(T value, String query) {
			RegExp pattern = RegExp.compile("(" + query + ")", "ig");
			return pattern.replace(getRenderer().render(value), "<strong>$1</strong>");
		}
	};

	public InputSuggest() {
		super(new TextBox());
		this.oracle = new OracleWrapper<T>();
		this.setParser((Parser<T>) StringParser.get());
		this.setRenderer(ToStringRenderer.<T> get());
		this.init();
	}

	protected InputSuggest(InputSuggest<T> source) {
		super(new TextBox(), source);
		this.oracle = source.oracle;
		this.highlighter = source.highlighter;
		this.init();
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputSuggest<T>(this);
	}

	private void init() {
		this.assistHandler = new TextBoxContentAssistHandler(this.oracle);
		this.assistAspect = new ContentAssistAspect<T>(this.assistHandler);
		this.assistAspect.setInput(this.getInput());
		this.assistAspect.setHighlighter(highlighter);

		this.compositeFocus = CompositeFocusHelper.createFocusHelper(this, this.getInput());
		this.compositeFocus.addFocusPartner(this.assistAspect.getSuggestionWidget().getElement());
	}

	public Oracle.Highlighter<T> getHighlighter() {
		return highlighter;
	}

	public void setHighlighter(Oracle.Highlighter<T> highlighter) {
		this.highlighter = highlighter;
		this.assistAspect.setHighlighter(highlighter);
	}

	public void setSuggestions(Collection<T> suggestions) {
		SimpleOracle<T> wordSuggestOracle = new SimpleOracle<T>();
		wordSuggestOracle.addAll(suggestions);
		wordSuggestOracle.setRenderer(getRenderer());
		oracle.setDelagate(wordSuggestOracle);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void setRenderer(Renderer<T> renderer) {
		super.setRenderer(renderer);
		if (oracle.getDelegate() instanceof SimpleOracle) {
			SimpleOracle<T> simpleOracle = (SimpleOracle<T>) oracle.getDelegate();
			simpleOracle.setRenderer(renderer);
		}
	}

	public void setOracle(Oracle<T> oracle) {
		this.oracle.setDelagate(oracle);
	}

	public void setSuggestionsLimit(int limit) {
		this.assistHandler.setLimit(limit);
	}

	@Override
	public void setValue(T value) {
		super.setValue(value);
		currentValue = value;
	}

	@Override
	public T flush() {
		this.clearErrors();
		this.validate(currentValue);
		if (!this.hasErrors()) {
			this.setValue(currentValue);
		}
		return this.getValue();
	}

	@Override
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return this.compositeFocus.addBlurHandler(handler);
	}

	@Override
	public HandlerRegistration addFocusHandler(FocusHandler handler) {
		return this.compositeFocus.addFocusHandler(handler);
	}

	class TextBoxContentAssistHandler extends AbstractContentAssistHandler<T> {

		public TextBoxContentAssistHandler(Oracle<T> oracle) {
			super(oracle);
		}

		@Override
		public String getQueryText(IsWidget textInput) {
			TextBox input = (TextBox) textInput;
			return input.getText();
		}

		@Override
		public void handleSuggestionSelected(IsWidget textInput, Oracle.Suggestion<T> suggestion) {
			TextBox input = (TextBox) textInput;
			T value = suggestion.getValue();
			String replacementString = getRenderer().render(value);
			input.setText(replacementString);
			input.setCursorPos(replacementString.length());

			T oldValue = InputSuggest.this.currentValue;
			InputSuggest.this.currentValue = value;
			ValueChangeEvent.fireIfNotEqual(InputSuggest.this, oldValue, value);

		}
	}
}
