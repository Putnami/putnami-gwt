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

import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.editor.client.EditorLabel;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasHtmlFor;
import fr.putnami.pwt.core.widget.client.base.AbstractWidget;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.AskFocusEvent;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Label extends AbstractWidget implements EditorLabel, HasHtmlFor, HasText {

	private static final CssStyle STYLE_CONTROL_LABEL = new SimpleStyle("control-label");

	private final LabelElement labelElement;

	private String path;
	private String text;

	private String htmlFor;

	private HandlerRegistration clickRegistration;

	public Label() {
		super(LabelElement.TAG);
		labelElement = LabelElement.as(getElement());
		StyleUtils.addStyle(this, STYLE_CONTROL_LABEL);
	}

	private Label(Label source) {
		super(source);
		labelElement = LabelElement.as(getElement());
		StyleUtils.addStyle(this, STYLE_CONTROL_LABEL);
		this.path = source.path;
		setText(source.text);
		if (source.htmlFor != null) {
			setHtmlFor(source.htmlFor);
		}
	}

	@Override
	public IsWidget cloneWidget() {
		return new Label(this);
	}

	@Override
	public void setText(String text) {
		this.text = text;
		this.getElement().setInnerText(text);
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public void setHtmlFor(String htmlFor) {
		this.htmlFor = htmlFor;
		this.labelElement.setHtmlFor(htmlFor);
		ensureLabelEvent();
	}

	private void ensureLabelEvent() {
		if (clickRegistration != null) {
			clickRegistration.removeHandler();
		}
		if (htmlFor != null) {
			clickRegistration = addDomHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					EventBus.get().fireEvent(new AskFocusEvent(htmlFor));
				}
			}, ClickEvent.getType());
		}
	}

	@Override
	public String getHtmlFor() {
		return this.htmlFor;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getLabelKey() {
		return null;
	}

	@Override
	public boolean isLabelMandatory() {
		return true;
	}

	@Override
	public String[] getSuffix() {
		return new String[] {
				EditorLabel.LABEL_SUFFIX, EditorLabel.EMPTY_SUFFIX
		};
	}
}
