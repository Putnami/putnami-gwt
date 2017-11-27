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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

import fr.putnami.pwt.core.editor.client.EditorError;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractWidget;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class ErrorGroup extends AbstractWidget implements EditorError, HasDrawable {
	public enum Color implements CssStyle {

			PRIMARY("bg-primary"),
			SUCCESS("bg-success"),
			INFO("bg-info"),
			WARNING("bg-warning"),
			DANGER("bg-danger");

		private final String style;

		Color(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private String message;

	private Color color = Color.DANGER;

	private List<Error> errors = Lists.newArrayList();

	public ErrorGroup() {
		super(UListElement.TAG);
		this.setColor(this.color);
	}

	public ErrorGroup(ErrorGroup source) {
		super(source);
		this.setColor(source.color);
		this.setMessage(source.message);
	}

	@Override
	public IsWidget cloneWidget() {
		return new ErrorGroup(this);
	}

	public Color getColor() {
		return this.color;
	}

	public void setColor(Color color) {
		StyleUtils.addStyle(this, color);
		this.color = color;
	}

	public void setMessage(String message) {
		this.message = message;
		this.getElement().setInnerText(message);
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public void clearErrors() {
		this.errors.clear();
		this.getElement().removeAllChildren();
	}

	@Override
	public void displayErrors(Iterable<Error> errors) {
		this.errors.clear();
		Iterables.addAll(this.errors, errors);
	}

	public boolean hasError() {
		return this.getElement().getChildCount() > 0;
	}

	@Override
	public void redraw() {
		this.getElement().removeAllChildren();
		for (Error error : this.errors) {
			LIElement errorElement = Document.get().createLIElement();
			errorElement.setInnerText(error.getMessageKey());
			this.getElement().appendChild(errorElement);
		}
	}
}
