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
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.GestureChangeEvent;
import com.google.gwt.event.dom.client.GestureChangeHandler;
import com.google.gwt.event.dom.client.GestureEndEvent;
import com.google.gwt.event.dom.client.GestureEndHandler;
import com.google.gwt.event.dom.client.GestureStartEvent;
import com.google.gwt.event.dom.client.GestureStartHandler;
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasAllGestureHandlers;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasAllTouchHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.impl.FocusImpl;

import fr.putnami.pwt.core.editor.client.EditorValue;
import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.util.AnchorUtils;
import fr.putnami.pwt.core.widget.client.util.HTMLUtils;

public class Anchor<T> extends AbstractPanel
	implements EditorValue<T>, CloneableWidget, HasAllDragAndDropHandlers, HasAllMouseHandlers, HasClickHandlers,
	HasDoubleClickHandlers, HasAllKeyHandlers, HasAllFocusHandlers, HasAllGestureHandlers, HasAllTouchHandlers {

	private static final FocusImpl FOCUS_IMPL = FocusImpl.getFocusImplForPanel();

	private T value;
	private String link;
    private String target;

	public Anchor() {
		super(AnchorElement.TAG);
		this.endConstruct();
	}

	public Anchor(String text) {
		this();
		this.setText(text);
	}

	public Anchor(Anchor<T> source) {
		super(source);
		this.endConstruct();
        if (source.link != null) {
            this.setLink(source.link);
        }
        this.setTarget(source.target);
		this.cloneSourceWidgets(source);
	}

	private void endConstruct() {
		this.setLinkAsDummy();
	}

	@Override
	public IsWidget cloneWidget() {
		return new Anchor<T>(this);
	}

	public void setText(String text) {
		this.getElement().removeAllChildren();
		this.getElement().appendChild(Document.get().createTextNode(HTMLUtils.unescapeHTML(text)));
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
		if (link == null) {
			this.getElement().removeAttribute("href");
		} else {
			AnchorElement.as(this.getElement()).setHref(link);
		}
	}

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
        if (target == null) {
            this.getElement().removeAttribute("target");
        } else {
            AnchorElement.as(this.getElement()).setTarget(target);
        }
    }

    public void setLinkAsDummy() {
		AnchorElement.as(this.getElement()).setHref(AnchorUtils.DUMMY_HREF);
	}

	@Override
	public void add(IsWidget child) {
		this.append(child);
	}

	@Override
	public void edit(T value) {
		this.value = value;
        if (this.link == null) {
            if (this.value instanceof String) {
                AnchorElement.as(this.getElement()).setHref((String) this.value);
            } else {
                setLinkAsDummy();
            }
        }
	}

	@Override
	public T getValue() {
		return this.value;
	}

	@Override
	public LeafValueEditor<T> asEditor() {
		return new TakesValueEditorWrapper<T>(this);
	}

	public int getTabIndex() {
		return Anchor.FOCUS_IMPL.getTabIndex(this.getElement());
	}

	public void setAccessKey(char key) {
		Anchor.FOCUS_IMPL.setAccessKey(this.getElement(), key);
	}

	public void setFocus(boolean focused) {
		if (focused) {
			Anchor.FOCUS_IMPL.focus(this.getElement());
		} else {
			Anchor.FOCUS_IMPL.blur(this.getElement());
		}
	}

	public void setTabIndex(int index) {
		Anchor.FOCUS_IMPL.setTabIndex(this.getElement(), index);
	}

	@Override
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return this.addDomHandler(handler, BlurEvent.getType());
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return this.addDomHandler(handler, ClickEvent.getType());
	}

	@Override
	public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
		return this.addDomHandler(handler, DoubleClickEvent.getType());
	}

	@Override
	public HandlerRegistration addDragEndHandler(DragEndHandler handler) {
		return this.addBitlessDomHandler(handler, DragEndEvent.getType());
	}

	@Override
	public HandlerRegistration addDragEnterHandler(DragEnterHandler handler) {
		return this.addBitlessDomHandler(handler, DragEnterEvent.getType());
	}

	@Override
	public HandlerRegistration addDragHandler(DragHandler handler) {
		return this.addBitlessDomHandler(handler, DragEvent.getType());
	}

	@Override
	public HandlerRegistration addDragLeaveHandler(DragLeaveHandler handler) {
		return this.addBitlessDomHandler(handler, DragLeaveEvent.getType());
	}

	@Override
	public HandlerRegistration addDragOverHandler(DragOverHandler handler) {
		return this.addBitlessDomHandler(handler, DragOverEvent.getType());
	}

	@Override
	public HandlerRegistration addDragStartHandler(DragStartHandler handler) {
		return this.addBitlessDomHandler(handler, DragStartEvent.getType());
	}

	@Override
	public HandlerRegistration addDropHandler(DropHandler handler) {
		return this.addBitlessDomHandler(handler, DropEvent.getType());
	}

	@Override
	public HandlerRegistration addFocusHandler(FocusHandler handler) {
		return this.addDomHandler(handler, FocusEvent.getType());
	}

	@Override
	public HandlerRegistration addGestureChangeHandler(GestureChangeHandler handler) {
		return this.addDomHandler(handler, GestureChangeEvent.getType());
	}

	@Override
	public HandlerRegistration addGestureEndHandler(GestureEndHandler handler) {
		return this.addDomHandler(handler, GestureEndEvent.getType());
	}

	@Override
	public HandlerRegistration addGestureStartHandler(GestureStartHandler handler) {
		return this.addDomHandler(handler, GestureStartEvent.getType());
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return this.addDomHandler(handler, KeyDownEvent.getType());
	}

	@Override
	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return this.addDomHandler(handler, KeyPressEvent.getType());
	}

	@Override
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return this.addDomHandler(handler, KeyUpEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return this.addDomHandler(handler, MouseDownEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return this.addDomHandler(handler, MouseMoveEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return this.addDomHandler(handler, MouseOutEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return this.addDomHandler(handler, MouseOverEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		return this.addDomHandler(handler, MouseUpEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
		return this.addDomHandler(handler, MouseWheelEvent.getType());
	}

	@Override
	public HandlerRegistration addTouchCancelHandler(TouchCancelHandler handler) {
		return this.addDomHandler(handler, TouchCancelEvent.getType());
	}

	@Override
	public HandlerRegistration addTouchEndHandler(TouchEndHandler handler) {
		return this.addDomHandler(handler, TouchEndEvent.getType());
	}

	@Override
	public HandlerRegistration addTouchMoveHandler(TouchMoveHandler handler) {
		return this.addDomHandler(handler, TouchMoveEvent.getType());
	}

	@Override
	public HandlerRegistration addTouchStartHandler(TouchStartHandler handler) {
		return this.addDomHandler(handler, TouchStartEvent.getType());
	}

}
