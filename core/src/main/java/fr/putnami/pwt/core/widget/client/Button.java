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

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Text;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.EditorLabel;
import fr.putnami.pwt.core.editor.client.EditorValue;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractWidget;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent.Handler;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent.HasButtonHandlers;
import fr.putnami.pwt.core.widget.client.util.AnchorUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Button<T> extends AbstractWidget
	implements HasHTML, EditorValue<T>, EditorLabel, HasButtonHandlers, HasAllFocusHandlers, Focusable {

	private static final CssStyle STYLE_BUTTON = new SimpleStyle("btn");
	private static final CssStyle STYLE_BLOCK = new SimpleStyle("btn-block");
	private static final CssStyle STYLE_ACTIVE = new SimpleStyle("active");
	private static final CssStyle STYLE_DISABLED = new SimpleStyle("disabled");

	public enum Type implements CssStyle {
			DEFAULT("btn-default"),
			LINK("btn-link"),
			SUCCESS("btn-success"),
			PRIMARY("btn-primary"),
			INFO("btn-info"),
			WARNING("btn-warning"),
			DANGER("btn-danger"),
			ICON("btn-icon");

		private final String style;

		Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	public enum Size implements CssStyle {
			X_SMALL("btn-xs"),
			SMALL("btn-sm"),
			DEFAULT(null),
			LARGE("btn-lg");

		private final String style;

		Size(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private class ClickEventHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			Object source = event.getSource();
			event.stopPropagation();
			if (source instanceof Button) {
				((Button<?>) source).fireEvent(new ButtonEvent((Button<?>) source));
			}
		}
	}

	private static final FocusImpl FOCUS_IMPL = FocusImpl.getFocusImplForPanel();

	private final AnchorElement element;

	private Type type = Type.DEFAULT;
	private Size size = Size.DEFAULT;
	private boolean block = false;
	private boolean disabled = false;
	private boolean active = false;

	private String iconType;

	private String name;
	private String text;

	private T value;

	private ClickEventHandler clickEventHandler;
	private HandlerRegistrationCollection registrations;
	private Integer tabIndex;

	public Button() {
		super(AnchorElement.TAG);

		this.element = AnchorElement.as(this.getElement());
		this.endConstruct();
		this.setType(Type.DEFAULT);
	}

	public Button(Button<T> source) {
		super(source);

		this.element = AnchorElement.as(this.getElement());
		this.endConstruct();

		this.setName(source.name);
		this.setText(source.text);
		this.setType(source.type);
		this.setIconType(source.iconType);
		this.setSize(source.size);
		this.setBlock(source.block);
		this.setDisabled(source.disabled);

		if (source.tabIndex != null) {
			this.setTabIndex(source.tabIndex);
		}
	}

	private void endConstruct() {
		this.element.setHref(AnchorUtils.DUMMY_HREF);

		StyleUtils.addStyle(this, Button.STYLE_BUTTON);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Button<T>(this);
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
		StyleUtils.addStyle(this, this.type);
	}

	public Size getSize() {
		return this.size;
	}

	public void setSize(Size size) {
		this.size = size;
		StyleUtils.addStyle(this, this.size);
	}

	public boolean isBlock() {
		return this.block;
	}

	public void setBlock(boolean block) {
		StyleUtils.toggleStyle(this, Button.STYLE_BLOCK, block);
		this.block = block;
	}

	public boolean isDisabled() {
		return this.disabled;
	}

	public void setDisabled(boolean disabled) {
		StyleUtils.toggleStyle(this, Button.STYLE_DISABLED, disabled);
		this.disabled = disabled;
	}

	@Override
	public void onBrowserEvent(Event event) {
		if (!this.disabled) {
			super.onBrowserEvent(event);
		}
	}

	public String getIconType() {
		return this.iconType;
	}

	public void setIconType(String iconType) {
		this.iconType = iconType;
		this.resetInner();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
		this.resetInner();
	}

	@Override
	public void setHTML(String html) {
		this.text = html;
		this.resetInner();
	}

	@Override
	public String getHTML() {
		return this.text;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		StyleUtils.toggleStyle(this, Button.STYLE_ACTIVE, active);
		this.active = active;
	}

	@Override
	public String getLabelKey() {
		return this.name;
	}

	@Override
	public String[] getSuffix() {
		return new String[] {EditorLabel.BUTTON_SUFFIX};
	}

	@Override
	public boolean isLabelMandatory() {
		return true;
	}

	private void resetInner() {
		this.element.removeAllChildren();
		if (this.iconType != null) {
			Icon icon = new Icon();
			icon.setType(this.iconType);

			this.element.appendChild(icon.getElement());
		}
		if (this.type != Type.ICON && this.text != null) {
			Text textElem = Document.get().createTextNode(this.text);
			this.element.appendChild(textElem);
		}
	}

	@Override
	public T getValue() {
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
	public HandlerRegistration addButtonHandler(final Handler handler) {
		this.registerClickHandler(this);
		this.registrations.add(this.addHandler(handler, ButtonEvent.TYPE));
		return this.registrations;
	}

	private void registerClickHandler(Button<T> b) {
		if (this.clickEventHandler == null) {
			this.clickEventHandler = new ClickEventHandler();
		}
		if (this.registrations == null) {
			this.registrations = new HandlerRegistrationCollection();
		}
		this.registrations.add(b.addDomHandler(this.clickEventHandler, ClickEvent.getType()));
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addFocusHandler(FocusHandler handler) {
		return this.addDomHandler(handler, FocusEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addBlurHandler(BlurHandler handler) {
		return this.addDomHandler(handler, BlurEvent.getType());
	}

	@Override
	public int getTabIndex() {
		return Button.FOCUS_IMPL.getTabIndex(this.getElement());
	}

	@Override
	public void setAccessKey(char key) {
		Button.FOCUS_IMPL.setAccessKey(this.getElement(), key);
	}

	@Override
	public void setFocus(boolean focused) {
		if (focused) {
			Button.FOCUS_IMPL.focus(this.getElement());
		} else {
			Button.FOCUS_IMPL.blur(this.getElement());
		}
	}

	@Override
	public void setTabIndex(int index) {
		this.tabIndex = index;
		Button.FOCUS_IMPL.setTabIndex(this.getElement(), index);
	}

}
