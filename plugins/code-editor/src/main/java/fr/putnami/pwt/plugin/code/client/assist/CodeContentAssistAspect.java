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
package fr.putnami.pwt.plugin.code.client.assist;

import com.google.gwt.user.client.ui.IsWidget;

import java.util.Arrays;
import java.util.List;

import fr.putnami.pwt.core.widget.client.assist.AbstractContentAssistHandler;
import fr.putnami.pwt.core.widget.client.assist.ContentAssistAspect;
import fr.putnami.pwt.core.widget.client.assist.ContentAssistHandler;
import fr.putnami.pwt.core.widget.client.assist.SimpleOracle;
import fr.putnami.pwt.core.widget.shared.assist.Oracle;
import fr.putnami.pwt.plugin.code.client.aspect.CodeEditorAspect;
import fr.putnami.pwt.plugin.code.client.base.CodeEditorDriver;
import fr.putnami.pwt.plugin.code.client.event.LiveValueChangeEvent;
import fr.putnami.pwt.plugin.code.client.input.CodeInput;

public class CodeContentAssistAspect extends ContentAssistAspect<String> implements CodeEditorAspect {

	public CodeContentAssistAspect() {
		this(new CodeDefaultContentAssistHandler());
	}

	public CodeContentAssistAspect(ContentAssistHandler<String> assistHandler) {
		super(assistHandler);
	}

	@Override
	protected void setNewSelection(Oracle.Suggestion<String> curSuggestion) {
		CodeInput codeInput = (CodeInput) this.getInput();
		String oldText = codeInput.getText();
		super.setNewSelection(curSuggestion);
		String newText = codeInput.getText();
		LiveValueChangeEvent.fireIfNotEqual(codeInput, oldText, newText);
	}

	@Override
	public void apply(CodeEditorDriver driver) {
		this.setInput(driver.getCodeInput());
	}

	@Override
	public List<AspectTrigger> trigerOn() {
		return Arrays.asList(AspectTrigger.INITALIZE);
	}

	@Override
	public CodeEditorAspect copy() {
		return new CodeContentAssistAspect(this.assistHandler.copy());
	}

	static class CodeDefaultContentAssistHandler extends AbstractContentAssistHandler<String> {

		CodeDefaultContentAssistHandler() {
			super(new SimpleOracle<String>());
		}

		@Override
		public String getQueryText(IsWidget textInput) {
			CodeInput codeInput = (CodeInput) textInput;
			return codeInput.getText().substring(0, codeInput.getCursorPosition());
		}

		@Override
		public void handleSuggestionSelected(IsWidget textInput, Oracle.Suggestion<String> suggestion) {
			CodeInput codeInput = (CodeInput) textInput;
			String oldText = codeInput.getText();
			String replacementString = suggestion.getValue();
			String newText = replacementString + oldText.substring(codeInput.getCursorPosition(), oldText.length());
			codeInput.setText(newText);
			codeInput.setCursorPosition(replacementString.length());
		}

		@Override
		public ContentAssistHandler<String> copy() {
			return new CodeDefaultContentAssistHandler();
		}
	}
}
