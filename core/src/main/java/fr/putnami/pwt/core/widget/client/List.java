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

import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.OListElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class List extends AbstractPanel implements CloneableWidget {

	public enum Type {
			DEFAULT(null, LIElement.TAG, null),
			LIST("list-unstyled", LIElement.TAG, null),
			LIST_GROUP("list-group", LIElement.TAG, "list-group-item"),
			INLINE("list-inline", LIElement.TAG, null),
			LABEL("list-inline", LIElement.TAG, "label label-default");

		final String itemTag;
		final CssStyle listStyle;
		final CssStyle itemStyle;

		Type(String listStyle, String itemTag, String itemStyle) {
			this.itemTag = itemTag;
			this.listStyle = new SimpleStyle(listStyle);
			this.itemStyle = new SimpleStyle(itemStyle);
		}
	}

	private Type type = Type.DEFAULT;

	public List() {
		this(UListElement.TAG);
	}

	@UiConstructor
	public List(String tag) {
		super(OListElement.TAG.equals(tag) || UListElement.TAG.equals(tag) ? tag : UListElement.TAG);
		this.setType(this.type);
	}

	protected List(List source) {
		super(source);
		this.setType(source.type);
	}

	@Override
	public IsWidget cloneWidget() {
		return new List(this);
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
		if (this.type == null) {
			this.type = Type.DEFAULT;
		}
		if (this.getWidgetCount() > 0) {
			StyleUtils.addStyle(this, this.type.listStyle);
			for (Widget w : this.getChildren()) {
				if (w instanceof ListItem) {
					ListItem item = (ListItem) w;
					StyleUtils.addStyle(item, this.type.itemStyle);
					item.redraw();
				}
			}
		}
	}

	@Override
	public void add(IsWidget child) {
		if (child instanceof ListItem) {
			this.addListItem((ListItem) child);
		}
	}

	public void addListItem(ListItem child) {
		StyleUtils.addStyle(this, this.type.listStyle);
		StyleUtils.addStyle(child, this.type.itemStyle);
		child.redraw();
		this.append(child);
	}
}
