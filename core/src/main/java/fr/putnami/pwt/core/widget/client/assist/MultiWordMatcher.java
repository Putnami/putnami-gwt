/**
 * This file is part of pwt.
 * <p>
 * pwt is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * pwt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with pwt. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.widget.client.assist;

import com.google.common.base.Splitter;
import com.google.gwt.text.shared.Renderer;

public class MultiWordMatcher<T> implements Matcher<T> {

	private static final char WHITESPACE_CHAR = ' ';

	private Splitter spitter = Splitter.on(WHITESPACE_CHAR).trimResults().omitEmptyStrings();

	private boolean caseSensitive = true;
	private Renderer<T> renderer;
	private char[] whitespaceChars;

	public MultiWordMatcher() {
	}

	public MultiWordMatcher(Renderer<T> renderer, char[] whitespaceChars) {
		this.renderer = renderer;
		this.whitespaceChars = whitespaceChars;
	}

	public Renderer<T> getRenderer() {
		return renderer;
	}

	public void setRenderer(Renderer<T> renderer) {
		this.renderer = renderer;
	}

	public char[] getWhitespaceChars() {
		return whitespaceChars;
	}

	public void setWhitespaceChars(char[] whitespaceChars) {
		this.whitespaceChars = whitespaceChars;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	@Override
	public int match(T value, String query) {
		String normalizedSuggestion = normalize(renderer.render(value));
		String normalizedQuery = normalize(query);

		spitter.split(normalizedSuggestion);
		if (normalizedSuggestion.equals(normalizedQuery)) {
			return 10;
		}
		if (normalizedSuggestion.startsWith(normalizedQuery)) {
			return 9;
		}
		for (String word : spitter.split(normalizedSuggestion)) {
			if (word.equals(normalizedQuery)) {
				return 5;
			}
			if (word.startsWith(normalizedQuery)) {
				return 4;
			}
			if (word.contains(normalizedQuery)) {
				return 3;
			}
		}
		return 0;
	}

	private String normalize(String value) {
		String result = value;
		if (caseSensitive && result != null) {
			result = result.toLowerCase();
		}
		if (whitespaceChars != null) {
			for (char ignore : whitespaceChars) {
				result = value.replace(ignore, WHITESPACE_CHAR);
			}
		}
		return result;
	}
}
