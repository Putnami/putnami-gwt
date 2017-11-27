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
package fr.putnami.pwt.plugin.code.client.token;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class SimpleToken<T extends TokenContent> implements Token<T> {

	public static final SimpleToken<?> UNDEFINED_TOKEN = SimpleToken.createUndefinedToken();

	public static <U extends TokenContent> SimpleToken<U> createEOFToken(int tokenStart) {
		return new SimpleToken<U>(TokenType.EOF, tokenStart);
	}

	public static <U extends TokenContent> SimpleToken<U> createUndefinedToken() {
		return new SimpleToken<U>(TokenType.UNDEFINED);
	}

	public static <U extends TokenContent> SimpleToken<U> createWhitespaceToken(int tokenStart,
		String strValue) {
		return new SimpleToken<U>(TokenType.WHITESPACE, tokenStart, strValue);
	}

	public static <U extends TokenContent> SimpleToken<U> createWhitespaceToken(int tokenStart,
		String strValue, U content) {
		return new SimpleToken<U>(TokenType.WHITESPACE, tokenStart, strValue, content);
	}

	public static <U extends TokenContent> SimpleToken<U> createNewlineToken(int tokenStart,
		String strValue) {
		return new SimpleToken<U>(TokenType.NEWLINE, tokenStart, strValue);
	}

	private enum TokenType {
			UNDEFINED,
			EOF,
			WHITESPACE,
			NEWLINE,
			OTHER;
	}

	private final TokenType type;
	private int tokenStart = -1;
	private String text;
	private T content;

	public SimpleToken(int tokenStart, String text) {
		this(TokenType.OTHER, tokenStart, text, null);
	}

	public SimpleToken(int tokenStart, String text, T content) {
		this(TokenType.OTHER, tokenStart, text, content);
	}

	private SimpleToken(TokenType type) {
		this.type = type;
		this.text = null;
	}

	private SimpleToken(TokenType type, int tokenStart) {
		this(type);
		this.tokenStart = tokenStart;
	}

	private SimpleToken(TokenType type, int tokenStart, String text) {
		this(type, tokenStart);
		this.text = text;
	}

	private SimpleToken(TokenType type, int tokenStart, String text, T content) {
		this(type, tokenStart, text);
		this.content = content;
	}

	@Override
	public boolean isEOF() {
		return this.type == TokenType.EOF;
	}

	@Override
	public boolean isNewLine() {
		return this.type == TokenType.NEWLINE;
	}

	@Override
	public boolean isUndefined() {
		return this.type == TokenType.UNDEFINED;
	}

	@Override
	public boolean isWhiteSpace() {
		return this.type == TokenType.WHITESPACE;
	}

	@Override
	public T getContent() {
		return this.content;
	}

	public void setContent(T content) {
		this.content = content;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public int getTokenLength() {
		if (this.text != null) {
			return this.text.length();
		}
		return 0;
	}

	@Override
	public int getTokenStart() {
		return this.tokenStart;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleToken) {
			return Objects.equal(this.type, ((SimpleToken<?>) other).type)
				&& Objects.equal(this.getTokenStart(), ((SimpleToken<?>) other).getTokenStart())
				&& Objects.equal(this.getText(), ((SimpleToken<?>) other).getText())
				&& Objects.equal(this.getContent(), ((SimpleToken<?>) other).getContent());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.type, this.tokenStart, this.text, this.content);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("type", this.type)
			.add("text", this.text)
			.toString();
	}
}
