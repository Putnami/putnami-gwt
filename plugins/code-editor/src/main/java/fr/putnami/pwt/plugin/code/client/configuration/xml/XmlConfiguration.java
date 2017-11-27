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
package fr.putnami.pwt.plugin.code.client.configuration.xml;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;

import java.util.List;

import fr.putnami.pwt.plugin.code.client.aspect.CodeEditorAspect;
import fr.putnami.pwt.plugin.code.client.configuration.CodeEditorConfiguration;
import fr.putnami.pwt.plugin.code.client.render.CssRendererTokenContent;
import fr.putnami.pwt.plugin.code.client.render.PartitionnedTextRendererAspect;
import fr.putnami.pwt.plugin.code.client.token.TokenContent;
import fr.putnami.pwt.plugin.code.client.token.evaluator.MultiLineTokenEvaluator;
import fr.putnami.pwt.plugin.code.client.token.evaluator.RegExpWordMatcher;
import fr.putnami.pwt.plugin.code.client.token.evaluator.SingleLineTokenEvaluator;
import fr.putnami.pwt.plugin.code.client.token.evaluator.WordDetector;
import fr.putnami.pwt.plugin.code.client.token.evaluator.WordsTokenEvaluator;

public final class XmlConfiguration implements CodeEditorConfiguration {

	public static final XmlConfiguration XML_CONFIGURATION = new XmlConfiguration();

	private List<CodeEditorAspect> aspects = Lists.newArrayList();

	private enum XmlPartitionTokenContent implements TokenContent {
		INNER_TAG;
	}

	static class XMLTagWordDetector implements WordDetector {

		public static final WordDetector INSTANCE = new XMLTagWordDetector();

		@Override
		public boolean isWordStart(char c) {
			return c == '<' || c == '/' || c == '>';
		}

		@Override
		public boolean isWordPart(char c) {
			if (c == '/' || c == '>' || c == ':' || c == '.' || c == '-') {
				return true;
			}
			return CharMatcher.JAVA_LETTER_OR_DIGIT.matches(c);
		}
	}

	static class XMLStartTagDetector extends RegExpWordMatcher {
		XMLStartTagDetector(TokenContent tokenContent) {
			super(tokenContent, "\\<(/)?[a-zA-Z0-9\\:\\.\\-]*((/)?\\>)?");
		}
	}

	static class XMLEndTagDetector extends RegExpWordMatcher {
		XMLEndTagDetector(TokenContent tokenContent) {
			super(tokenContent, "(/)?\\>");
		}
	}

	private XmlConfiguration() {
		PartitionnedTextRendererAspect renderAspect = new PartitionnedTextRendererAspect(true);
		// Detect and Render Xml Comment
		renderAspect.registerPartitionner(new MultiLineTokenEvaluator("<!--", "-->",
			new CssRendererTokenContent("code-editor-xml-comment"), (char) 0, true));
		// Detect and Render Xml Head or CDATA
		renderAspect.registerPartitionner(new MultiLineTokenEvaluator("<!", ">",
			new CssRendererTokenContent("code-editor-xml-doctype"), '\\', true));
		// Detect Xml tag
		renderAspect.registerPartitionner(new MultiLineTokenEvaluator("<", ">",
			XmlPartitionTokenContent.INNER_TAG, (char) 0, true));

		// Detect and render start and end Xml tag
		WordsTokenEvaluator tagTokenEvaluator =
			new WordsTokenEvaluator(XMLTagWordDetector.INSTANCE, null);
		tagTokenEvaluator.addWordMatcher(new XMLStartTagDetector(
			new CssRendererTokenContent("code-editor-xml-tag")));
		tagTokenEvaluator.addWordMatcher(
			new XMLEndTagDetector(new CssRendererTokenContent("code-editor-xml-tag")));
		// Detect and render inner Tag word token
		WordsTokenEvaluator wordTokenEvaluator =
			new WordsTokenEvaluator(new CssRendererTokenContent("code-editor-xml-attribute"));
		// Detect and render inner Tag Strings
		SingleLineTokenEvaluator stringTokenEvaluator =
			new SingleLineTokenEvaluator("\"", "\"", new CssRendererTokenContent("code-editor-xml-attribute-value"), '\\');

		// Register inner tag detectors and renderers.
		renderAspect.registerPartitionScanners(XmlPartitionTokenContent.INNER_TAG,
			stringTokenEvaluator, tagTokenEvaluator, wordTokenEvaluator);
		this.aspects.add(renderAspect);
	}

	@Override
	public List<CodeEditorAspect> getAspects() {
		return this.aspects;
	}
}
