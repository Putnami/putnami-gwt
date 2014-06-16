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

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Text;
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
import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.widget.client.base.AbstractWidget;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.constant.IconType;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent.Handler;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent.HasButtonHandlers;
import fr.putnami.pwt.core.widget.client.util.AnchorUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Button<T> extends AbstractWidget implements
		HasHTML,
		EditorValue<T>,
		EditorLabel,
		HasButtonHandlers,
		HasAllFocusHandlers,
		Focusable {

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

		private Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return style;
		}
	}

	public enum Size implements CssStyle {
		X_SMALL("btn-xs"),
		SMALL("btn-sm"),
		DEFAULT(null),
		LARGE("btn-lg");

		private final String style;

		private Size(String style) {
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
				((Button) source).fireEvent(new ButtonEvent((Button) source));
			}
		}

	}

	private static final FocusImpl focusImpl = FocusImpl.getFocusImplForPanel();

	private final AnchorElement element;

	private Type type = Type.DEFAULT;
	private Size size = Size.DEFAULT;
	private boolean block = false;
	private boolean disabled = false;
	private boolean active = false;

	private IconType iconType;

	private String name;
	private String text;

	private T value;

	private ClickEventHandler clickEventHandler;
	private HandlerRegistrationCollection registrations;
	private Integer tabIndex;

	public Button() {
		super(AnchorElement.TAG);

		element = AnchorElement.as(getElement());
		endConstruct();
		setType(Type.DEFAULT);
	}

	public Button(Button<T> source) {
		super(source);

		element = AnchorElement.as(getElement());
		endConstruct();

		setName(source.name);
		setText(source.text);
		setType(source.type);
		setIconType(source.iconType);
		setSize(source.size);
		setBlock(source.block);
		setDisabled(source.disabled);

		if (source.tabIndex != null) {
			setTabIndex(source.tabIndex);
		}
	}

	private void endConstruct() {
		element.setHref(AnchorUtils.DUMMY_HREF);

		StyleUtils.addStyle(this, STYLE_BUTTON);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Button<T>(this);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
		StyleUtils.addStyle(this, this.type);
	}

	public Size getSize() {
		return size;
	}

	public void setSize(Size size) {
		this.size = size;
		StyleUtils.addStyle(this, this.size);
	}

	public boolean isBlock() {
		return block;
	}

	public void setBlock(boolean block) {
		StyleUtils.toggleStyle(this, STYLE_BLOCK, block);
		this.block = block;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		StyleUtils.toggleStyle(this, STYLE_DISABLED, disabled);
		this.disabled = disabled;
	}

	@Override
	public void onBrowserEvent(Event event) {
		if (!disabled) {
			super.onBrowserEvent(event);
		}
	}

	public IconType getIconType() {
		return iconType;
	}

	public void setIconType(IconType iconType) {
		this.iconType = iconType;
		resetInner();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
		resetInner();
	}

	@Override
	public void setHTML(String html) {
		this.text = html;
		resetInner();
	}

	@Override
	public String getHTML() {
		return text;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		StyleUtils.toggleStyle(this, STYLE_ACTIVE, active);
		this.active = active;
	}

	@Override
	public String getLabelKey() {
		return name;
	}

	@Override
	public String[] getSuffix() {
		return new String[] {
				EditorLabel.BUTTON_SUFFIX
		};
	}

	@Override
	public boolean isLabelMandatory() {
		return true;
	}

	private void resetInner() {
		if (iconType == null) {
			iconType = IconType.get(name);
		}

		element.removeAllChildren();
		if (iconType != null) {
			Icon icon = new Icon();
			icon.setType(iconType);

			element.appendChild(icon.getElement());
		}
		if (type != Type.ICON && text != null) {
			Text textElem = Document.get().createTextNode(text);
			element.appendChild(textElem);
		}
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void edit(T value) {
		this.value = value;
	}

	@Override
	public HandlerRegistration addButtonHandler(final Handler handler) {
		registerClickHandler(this);
		registrations.add(addHandler(handler, ButtonEvent.TYPE));
		return registrations;
	}

	private void registerClickHandler(Button<T> b) {
		if (clickEventHandler == null) {
			clickEventHandler = new ClickEventHandler();
		}
		if (registrations == null) {
			registrations = new HandlerRegistrationCollection();
		}
		registrations.add(b.addDomHandler(clickEventHandler, ClickEvent.getType()));
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addFocusHandler(FocusHandler handler) {
		return addDomHandler(handler, FocusEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addBlurHandler(BlurHandler handler) {
		return addDomHandler(handler, BlurEvent.getType());
	}

	@Override
	public int getTabIndex() {
		return focusImpl.getTabIndex(getElement());
	}

	@Override
	public void setAccessKey(char key) {
		focusImpl.setAccessKey(getElement(), key);
	}

	@Override
	public void setFocus(boolean focused) {
		if (focused) {
			focusImpl.focus(getElement());
		}
		else {
			focusImpl.blur(getElement());
		}
	}

	@Override
	public void setTabIndex(int index) {
		this.tabIndex = index;
		focusImpl.setTabIndex(getElement(), index);
	}

}
