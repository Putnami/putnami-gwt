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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.widget.client.util.HTMLUtils;

public class Text extends Widget implements
		HasText,
		CloneableWidget {

	private final com.google.gwt.dom.client.Text textElement;
	private String text;

	public Text() {
		this.textElement = Document.get().createTextNode("");
		setElement(textElement.<Element> cast());
	}

	public Text(String text) {
		this();
		setText(text);
	}

	public Text(Text source) {
		this();
		textElement.setData(source.text);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Text(this);
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public void setText(String text) {
		this.text = HTMLUtils.unescapeHTML(text);
		textElement.setData(this.text == null ? "" : this.text);
	}

}
