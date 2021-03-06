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
package fr.putnami.pwt.plugin.code.client.configuration.java;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.widget.client.assist.AbstractContentAssistHandler;
import fr.putnami.pwt.core.widget.client.assist.SimpleOracle;
import fr.putnami.pwt.core.widget.shared.assist.Oracle;
import fr.putnami.pwt.plugin.code.client.input.CodeInput;

public class JavaKeywordAssistHandler extends AbstractContentAssistHandler<String> {

	public JavaKeywordAssistHandler() {
		super(new SimpleOracle<String>());
		SimpleOracle<String> oracle = (SimpleOracle<String>) this.getOracle();
		oracle.addAll(Lists.newArrayList("abstract", "assert", "boolean", "break", "byte", "case",
			"catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum",
			"extends", "false", "final", "finally", "float", "for", "goto", "if", "implements",
			"import", "instanceof", "int", "interface", "long", "native", "new", "null", "package",
			"private", "protected", "public", "return", "short", "static", "strictfp", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void",
			"volatile", "while"));
	}

	@Override
	public String getQueryText(IsWidget textInput) {
		CodeInput codeInput = (CodeInput) textInput;
		String currentText = codeInput.getText();
		int cursorPos = codeInput.getCursorPosition();
		return currentText.substring(
			this.getCurrentTokenStartIndex(currentText, cursorPos), cursorPos).trim();
	}

	@Override
	public void handleSuggestionSelected(IsWidget textInput, Oracle.Suggestion<String> suggestion) {
		CodeInput codeInput = (CodeInput) textInput;
		String currentText = codeInput.getText();
		int cursorPos = codeInput.getCursorPosition();
		int tokenStartIndex = this.getCurrentTokenStartIndex(currentText, cursorPos);
		String replacementString = suggestion.getValue();
		String newText =
			currentText.substring(0, tokenStartIndex) + replacementString
				+ currentText.substring(codeInput.getCursorPosition(), currentText.length());
		int newCursorPos = tokenStartIndex + replacementString.length();
		codeInput.setText(newText);
		codeInput.setCursorPosition(newCursorPos);
	}

	private int getCurrentTokenStartIndex(String text, int cursorPos) {
		int startIndex = cursorPos;
		while (startIndex > 0) {
			if (!CharMatcher.JAVA_LETTER_OR_DIGIT.matches(text.charAt(startIndex - 1))) {
				return startIndex;
			}
			startIndex--;
		}
		return startIndex;
	}
}
