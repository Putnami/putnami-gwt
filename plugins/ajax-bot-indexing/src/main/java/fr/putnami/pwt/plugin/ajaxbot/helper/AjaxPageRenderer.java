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
package fr.putnami.pwt.plugin.ajaxbot.helper;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlInlineFrame;
import com.gargoylesoftware.htmlunit.html.HtmlItalic;
import com.gargoylesoftware.htmlunit.html.HtmlMeta;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlScript;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

public class AjaxPageRenderer {

	public static final String META_NAME_FRAGMENT = "fragment";
	public static final String META_CSRF = "_csrf";
	public static final String META_CSRF_HEADER = "_csrf_header";

	private String baseUrl;

	private List<String> anchors = Lists.newArrayList();

	public AjaxPageRenderer(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public List<String> getAnchors() {
		return anchors;
	}

	public String crawlPage(String queryString) throws IOException {
		anchors.clear();
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		new ConnectionFilter(webClient);

		HtmlPage page = webClient.getPage(baseUrl + queryString);
		List<DomNode> elementsToRemove = Lists.newArrayList();

		// Remove the meta tag containing the fragment
		for (DomElement elem : page.getHead().getElementsByTagName(HtmlMeta.TAG_NAME)) {
			HtmlMeta meta = (HtmlMeta) elem;
			if (META_NAME_FRAGMENT.equals(meta.getNameAttribute())) {
				elementsToRemove.add(meta);
			}
			if (META_CSRF.equals(meta.getNameAttribute())) {
				elementsToRemove.add(meta);
			}
			if (META_CSRF_HEADER.equals(meta.getNameAttribute())) {
				elementsToRemove.add(meta);
			}
		}
		for (DomElement elem : page.getElementsByTagName("pre")) {
			String classAttr = elem.getAttribute("class");
			if (classAttr != null && classAttr.contains("code-editor")) {
				String asText = elem.getTextContent();
				elem.setTextContent(asText);
			}
		}
		for (DomElement elem : page.getElementsByTagName("a")) {
			String href = elem.getAttribute("href");
			if (href != null && href.startsWith("#!")) {
				anchors.add(href);
			}
		}

		// GWT iframe are useless
		for (DomNode node : page.getBody().getChildren()) {
			if (HtmlInlineFrame.TAG_NAME.equals(node.getNodeName())) {
				// Remove the iframe
				elementsToRemove.add(node);
			}
		}

		// remove all javascript from the header, the bot will not try process them.
		for (DomElement elem : page.getHead().getElementsByTagName(HtmlScript.TAG_NAME)) {
			HtmlScript scriptElem = (HtmlScript) elem;
			if (scriptElem.getAttribute("src") != null) {
				elementsToRemove.add(scriptElem);
			}
		}
		// remove the i (icon) tags.
		elementsToRemove.addAll(page.getBody().getElementsByTagName(HtmlItalic.TAG_NAME));

		// Remove elements from dom
		for (DomNode elem : elementsToRemove) {
			elem.remove();
		}

		// Reset the html doctype
		String pageData =
			page.asXml().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<!DOCTYPE html>");

		return pageData;
	}

	class ConnectionFilter extends FalsifyingWebConnection {
		ConnectionFilter(WebClient webClient) throws IllegalArgumentException {
			super(webClient);
		}

		@Override
		public WebResponse getResponse(WebRequest request) throws IOException {
			WebResponse response = super.getResponse(request);
			if (response.getWebRequest().getUrl().toString().contains("www.google-analytics.com")) {
				return this.createWebResponse(response.getWebRequest(), "", "application/javascript", 200, "Ok");
			}
			if (response.getWebRequest().getUrl().toString().endsWith(".css")) {
				return this.createWebResponse(response.getWebRequest(), "", "text/css", 200, "Ok");
			}
			return super.getResponse(request);
		}
	}

}
