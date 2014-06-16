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
package fr.putnami.pwt.core.widget.client.base;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.EditorInput;
import fr.putnami.pwt.core.editor.client.EditorLeaf;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent.Handler;
import fr.putnami.pwt.core.editor.client.util.ValidationUtils;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasHtmlId;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public abstract class AbstractInput<I> extends AbstractComposite implements
		DirtyEvent.HasDirtyHandlers,
		EditorInput<I>,
		EditorLeaf,
		HasHtmlId,
		Focusable,
		HasValueChangeHandlers<I>,
		HasAllFocusHandlers,
		HasClickHandlers,
		HasDoubleClickHandlers,
		HasAllDragAndDropHandlers,
		HasAllGestureHandlers,
		HasAllKeyHandlers,
		HasAllMouseHandlers,
		HasAllTouchHandlers
{

	public static CssStyle STYLE_CONTROL = new SimpleStyle("form-control");

	String ATTRIBUTE_TYPE = "type";

	private static final FocusImpl focusImpl = FocusImpl.getFocusImplForPanel();

	private String path;

	private Collection<Error> errors;
	private Collection<Validator<I>> validators;

	private I value;

	private String htmlId;

	private Character accessKey;
	private Integer tabIndex;

	public AbstractInput(Widget widget) {
		initWidget(widget);
	}

	protected AbstractInput(Widget widget, AbstractInput<I> source) {
		super(source);
		initWidget(widget);
		this.path = source.path;
		this.htmlId = source.htmlId;

		if (source.tabIndex != null) {
			setTabIndex(source.tabIndex);
		}
		if (source.accessKey != null) {
			setAccessKey(source.accessKey);
		}

		if (source.validators != null) {
			for (Validator<I> validator : source.validators) {
				addValidator(validator);
			}
		}
	}

	protected void endConstruct() {
		setHtmlId(htmlId);
	}

	@Override
	protected void initWidget(Widget widget) {
		super.initWidget(widget);
		StyleUtils.addStyle(this, STYLE_CONTROL);
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public void setHtmlId(String htmlId) {
		this.htmlId = htmlId;
		if (htmlId != null) {
			getElement().setId(htmlId);
		}
	}

	@Override
	public String getHtmlId() {
		return this.htmlId;

	}

	@Override
	public HandlerRegistration addDirtyHandler(Handler handler) {
		return EventBus.get().addHandlerToSource(DirtyEvent.TYPE, this, handler);
	}

	@Override
	public I getValue() {
		return this.value;
	}

	public void setValue(I value) {
		this.value = value;
	}

	@Override
	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}

	@Override
	public Iterable<Error> getErrors() {
		return errors == null ? Collections.<Error> emptyList() : Iterables.unmodifiableIterable(errors);
	}

	protected void clearErrors() {
		if (errors != null) {
			errors.clear();
			errors = null;
		}
	}

	protected void addError(Error error) {
		if (this.errors == null) {
			this.errors = Lists.newArrayList();
		}
		this.errors.add(error);

	}

	@Override
	public void addValidator(Validator<I> validator) {
		if (validators == null) {
			validators = Lists.newArrayList();
		}
		validators.add(validator);
	}

	protected void validate(I value) {
		Collection<Error> errors = ValidationUtils.validate(validators, this, value);
		for (Error error : errors) {
			addError(error);
		}
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

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addBlurHandler(BlurHandler handler) {
		return addDomHandler(handler, BlurEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addClickHandler(ClickHandler handler) {
		return addDomHandler(handler, ClickEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
		return addDomHandler(handler, DoubleClickEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addDragEndHandler(DragEndHandler handler) {
		return addBitlessDomHandler(handler, DragEndEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addDragEnterHandler(DragEnterHandler handler) {
		return addBitlessDomHandler(handler, DragEnterEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addDragHandler(DragHandler handler) {
		return addBitlessDomHandler(handler, DragEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addDragLeaveHandler(DragLeaveHandler handler) {
		return addBitlessDomHandler(handler, DragLeaveEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addDragOverHandler(DragOverHandler handler) {
		return addBitlessDomHandler(handler, DragOverEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addDragStartHandler(DragStartHandler handler) {
		return addBitlessDomHandler(handler, DragStartEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addDropHandler(DropHandler handler) {
		return addBitlessDomHandler(handler, DropEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addFocusHandler(FocusHandler handler) {
		return addDomHandler(handler, FocusEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addGestureChangeHandler(GestureChangeHandler handler) {
		return addDomHandler(handler, GestureChangeEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addGestureEndHandler(GestureEndHandler handler) {
		return addDomHandler(handler, GestureEndEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addGestureStartHandler(GestureStartHandler handler) {
		return addDomHandler(handler, GestureStartEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return addDomHandler(handler, KeyDownEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return addDomHandler(handler, KeyPressEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return addDomHandler(handler, KeyUpEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return addDomHandler(handler, MouseDownEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return addDomHandler(handler, MouseMoveEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return addDomHandler(handler, MouseOutEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return addDomHandler(handler, MouseOverEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		return addDomHandler(handler, MouseUpEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
		return addDomHandler(handler, MouseWheelEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addTouchCancelHandler(TouchCancelHandler handler) {
		return addDomHandler(handler, TouchCancelEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addTouchEndHandler(TouchEndHandler handler) {
		return addDomHandler(handler, TouchEndEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addTouchMoveHandler(TouchMoveHandler handler) {
		return addDomHandler(handler, TouchMoveEvent.getType());
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addTouchStartHandler(TouchStartHandler handler) {
		return addDomHandler(handler, TouchStartEvent.getType());
	}

	@Override
	public int getTabIndex() {
		return focusImpl.getTabIndex(getElement());
	}

	@Override
	public void setAccessKey(char key) {
		this.accessKey = key;
		focusImpl.setAccessKey(getElement(), key);
	}

	public char getAccessKey() {
		return accessKey;
	}

}
