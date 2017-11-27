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
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collections;

import fr.putnami.pwt.core.editor.client.EditorInput;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputGroup<T> extends AbstractPanel implements EditorInput<T> {

	private static final CssStyle STYLE_INPUT_GROUP_ADDON = new SimpleStyle("input-group-addon");
	private static final CssStyle STYLE_INPUT_GROUP_BUTTON = new SimpleStyle("input-group-btn");

	public enum Style implements CssStyle {
			UNSTYLED(null),
			DEFAULT("input-group");

		private final String style;

		Style(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	public enum Size implements CssStyle {
			SMALL("input-group-sm"),
			DEFAULT(null),
			LARGE("input-group-lg");

		private final String style;

		Size(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private Style style = Style.DEFAULT;
	private Size size = Size.DEFAULT;
	private T value;

	public InputGroup() {
		super(DivElement.TAG);
		this.setStyle(this.style);
		this.setSize(this.size);
	}

	protected InputGroup(InputGroup<T> source, boolean cloneSourceWidgets) {
		super(source);
		this.setStyle(source.style);
		this.setSize(source.size);
		if (cloneSourceWidgets) {
			this.cloneSourceWidgets(source);
		}
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputGroup<T>(this, true);
	}

	@Override
	public T getValue() {
		return this.value;
	}

	@Override
	public T flush() {
		return this.value;
	}

	@Override
	public void edit(T value) {
		this.value = value;
	}

	@Override
	public LeafValueEditor<T> asEditor() {
		return new TakesValueEditorWrapper<T>(this);
	}

	@Override
	public boolean hasErrors() {
		return false;
	}

	@Override
	public Iterable<Error> getErrors() {
		return Collections.<Error> emptyList();
	}

	public Size getSize() {
		return this.size;
	}

	public void setSize(Size size) {
		this.size = size;
		StyleUtils.addStyle(this, size);
	}

	public Style getStyle() {
		return this.style;
	}

	public void setStyle(Style style) {
		this.style = style;
		StyleUtils.addStyle(this, style);
	}

	@Override
	public void addValidator(Validator<T> validator) {
		// no validator required for input group
	}

	@Override
	public void add(IsWidget child) {
		this.addInput(child);
	}

	@Override
	public void add(Widget child) {
		this.addInput(child);
	}

	@UiChild(tagname = "input")
	public void addInput(IsWidget child) {
		this.append(child);
		this.addEditor(child);
	}

	@UiChild(tagname = "addon")
	public void addAddon(Widget addon) {
		this.append(addon);
		this.addEditor(addon);
		StyleUtils.addStyle(addon, InputGroup.STYLE_INPUT_GROUP_ADDON);
	}

	@UiChild(tagname = "button")
	public void addButton(Widget button) {
		this.append(button);
		this.addEditor(button);
		StyleUtils.addStyle(button, InputGroup.STYLE_INPUT_GROUP_BUTTON);
	}

}
