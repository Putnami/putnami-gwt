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
package fr.putnami.pwt.plugin.code.client.render;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import fr.putnami.pwt.plugin.code.client.aspect.CodeEditorAspect;
import fr.putnami.pwt.plugin.code.client.base.CodeEditorDriver;
import fr.putnami.pwt.plugin.code.client.output.CodeLineImpl;
import fr.putnami.pwt.plugin.code.client.token.CharacterScanner;
import fr.putnami.pwt.plugin.code.client.token.CharacterScannerImpl;
import fr.putnami.pwt.plugin.code.client.token.SimpleToken;
import fr.putnami.pwt.plugin.code.client.token.Token;
import fr.putnami.pwt.plugin.code.client.token.TokenContent;
import fr.putnami.pwt.plugin.code.client.token.TokenScanner;
import fr.putnami.pwt.plugin.code.client.token.TokenScannerImpl;
import fr.putnami.pwt.plugin.code.client.util.CharacterUtil;

public abstract class AbstractTextRendererAspect implements CodeEditorAspect {

	private boolean autoAddEOLToken = true;

	public AbstractTextRendererAspect() {
		// NoOp
	}

	public AbstractTextRendererAspect(boolean autoAddEOLToken) {
		this.autoAddEOLToken = autoAddEOLToken;
	}

	protected TokenScanner buildScanner() {
		CharacterScanner charScanner = new CharacterScannerImpl();
		TokenScanner tokenScanner = new TokenScannerImpl(charScanner);
		return tokenScanner;
	}

	@Override
	public void apply(CodeEditorDriver editorDriver) {
		List<Token<?>> tokenizedValue = Lists.newArrayList();

		if (!Strings.isNullOrEmpty(editorDriver.getValue())) {
			tokenizedValue = this.extractTokenList(editorDriver.getValue());

			if (this.autoAddEOLToken) {
				tokenizedValue = this.addEOLToken(editorDriver.getValue(), tokenizedValue);
			}
		}
		this.render(editorDriver, tokenizedValue);
	}

	protected abstract List<Token<?>> extractTokenList(String value);

	protected void render(CodeEditorDriver editorDriver, List<Token<?>> tokenizedValue) {
		editorDriver.getCodeOutput().startRender();
		CodeLineImpl codeLine = new CodeLineImpl();
		for (Token<?> tokenVal : tokenizedValue) {
			if (tokenVal.isNewLine()) {
				editorDriver.getCodeOutput().renderNextLine(codeLine);
				codeLine = new CodeLineImpl();
			} else {
				codeLine.addToken(tokenVal);
			}
		}
		editorDriver.getCodeOutput().renderNextLine(codeLine);
		editorDriver.getCodeOutput().endRender();
	}

	private List<Token<?>> addEOLToken(String value, List<Token<?>> tokenList) {
		List<Token<?>> resultTokenList = Lists.newArrayList();
		// add EOL Tokens
		RegExp regExp = RegExp.compile(CharacterUtil.END_OF_LINE_PATTERN, "g");
		MatchResult eolMatcher = regExp.exec(value);
		while (eolMatcher != null) {
			for (Iterator<Token<?>> it = tokenList.iterator(); it.hasNext();) {
				Token<?> currToken = it.next();
				if (currToken.getTokenStart() >= regExp.getLastIndex()) {
					// current token is after last EL match
					break;
				}
				if (this.getTokenEnd(currToken) <= eolMatcher.getIndex()) {
					// current token is before last EL match
					resultTokenList.add(currToken);
					it.remove();
				} else {
					// current token contains last EOL match
					it.remove();
					this.splitTokenAndAddEOL(tokenList, resultTokenList, it, regExp.getLastIndex(),
						eolMatcher, currToken);
					break;
				}
			}
			eolMatcher = regExp.exec(value);
		}
		resultTokenList.addAll(tokenList);
		return resultTokenList;
	}

	private void splitTokenAndAddEOL(List<Token<?>> tokenizedValue,
		List<Token<?>> resultTokenizedValue, Iterator<Token<?>> tokenIterator, int lastMatchEndIndex,
		MatchResult eolMatcher, Token<?> currToken) {

		Token<?> tonken = currToken;
		if (tonken.getTokenStart() == eolMatcher.getIndex()) {
			String eolStr = "";
			// MultiChar EOL
			while (this.getTokenEnd(tonken) < lastMatchEndIndex) {
				eolStr += tonken.getText();
				tonken = tokenIterator.next();
				tokenIterator.remove();
			}
			if (this.getTokenEnd(tonken) == lastMatchEndIndex) {
				eolStr += tonken.getText();
			} else {
				eolStr += tonken.getText().substring(0, lastMatchEndIndex - tonken.getTokenStart());
				// Nead to go out of the iterator (in the call) after because of
				// ConcurrentModificationException
				tokenizedValue.add(0, new SimpleToken<TokenContent>(lastMatchEndIndex, tonken.getText()
					.substring(lastMatchEndIndex - tonken.getTokenStart()), tonken.getContent()));
			}
			resultTokenizedValue.add(SimpleToken.createNewlineToken(eolMatcher.getIndex(), eolStr));
		} else {
			// We extract the first part of token and recall the method to go on the first condition
			resultTokenizedValue.add(new SimpleToken<TokenContent>(tonken.getTokenStart(), tonken
				.getText().substring(0, lastMatchEndIndex - tonken.getTokenStart() - 1), tonken
				.getContent()));
			tonken =
				new SimpleToken<TokenContent>(eolMatcher.getIndex(), tonken.getText().substring(
					lastMatchEndIndex - tonken.getTokenStart() - 1), tonken.getContent());
			this.splitTokenAndAddEOL(tokenizedValue, resultTokenizedValue, tokenIterator,
				lastMatchEndIndex, eolMatcher, tonken);
		}
	}

	public boolean getAutoAddEOLToken() {
		return this.autoAddEOLToken;
	}

	public void setAutoAddEOLToken(boolean autoAddEOLToken) {
		this.autoAddEOLToken = autoAddEOLToken;
	}

	@Override
	public List<AspectTrigger> trigerOn() {
		return Arrays.asList(AspectTrigger.EDIT, AspectTrigger.CHANGE);
	}

	protected int getTokenEnd(Token<?> token) {
		return token.getTokenStart() + token.getTokenLength();
	}
}
