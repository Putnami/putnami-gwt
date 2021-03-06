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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.pwt.core.editor.client.EditorLabel;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractHover;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Popover extends AbstractHover implements EditorLabel {

	private static final CssStyle STYLE_POPOVER = new SimpleStyle("popover");
	private static final CssStyle STYLE_ARROW = new SimpleStyle("arrow");
	private static final CssStyle STYLE_TITLE = new SimpleStyle("popover-title");
	private static final CssStyle STYLE_INNER = new SimpleStyle("popover-content");
	private static final CssStyle STYLE_FADE = new SimpleStyle("fade");

	public static class PopoverContainer extends Widget {

		private DivElement container;

		public PopoverContainer() {
			this.container = Document.get().createDivElement();
			StyleUtils.addStyle(this.container, Popover.STYLE_POPOVER);
			StyleUtils.addStyle(this.container, Popover.STYLE_FADE);

			this.setElement(this.container);
		}

		private void reset() {
			this.container.removeAllChildren();
			DivElement arrow = Document.get().createDivElement();
			StyleUtils.addStyle(arrow, Popover.STYLE_ARROW);
			this.container.appendChild(arrow);
		}

		@Override
		public void setTitle(String title) {
			Element titleElement = Document.get().createElement("h3");
			this.container.appendChild(titleElement);
			StyleUtils.addStyle(titleElement, Popover.STYLE_TITLE);
			titleElement.setInnerText(title);
		}

		public void setContent(String content) {
			DivElement contentElement = Document.get().createDivElement();
			this.container.appendChild(contentElement);
			StyleUtils.addStyle(contentElement, Popover.STYLE_INNER);
			contentElement.setInnerText(content);
		}
	}

	private PopoverContainer popoverWidget;

	private String title;
	private String text;

	public Popover() {
		super();
	}

	protected Popover(Popover source) {
		super(source);
		this.title = source.title;
		this.text = source.text;
	}

	@Override
	public IsWidget cloneWidget() {
		return new Popover(this);
	}

	@Override
	protected Widget getHoverWidget() {
		if (this.popoverWidget == null) {
			this.popoverWidget = new PopoverContainer();
		}
		this.popoverWidget.reset();
		if (this.title != null) {
			this.popoverWidget.setTitle(this.title);
		}
		this.popoverWidget.setContent(this.text);
		return this.popoverWidget;
	}

	@Override
	public String[] getSuffix() {
		return new String[] {EditorLabel.LABEL_SUFFIX};
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getLabelKey() {
		return null;
	}

	@Override
	public boolean isLabelMandatory() {
		return false;
	}

}
