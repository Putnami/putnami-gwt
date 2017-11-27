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

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.Map;

import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractInputChoice;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputRadio<T> extends AbstractInputChoice<T, T> {

	public enum Type implements CssStyle {
			DEFAULT("radio"),
			INLINE("radio-inline");

		private final String style;

		Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private class SingleChoiceHandler implements ChoiceSelectionHandler<T, T> {
		private T selectedItem;

		@Override
		public void onItemClick(T item) {
			this.selectedItem = item;
			ValueChangeEvent.fire(InputRadio.this, this.selectedItem);
		}

		@Override
		public void setSelection(T selection, boolean fireEvents) {
			T oldValue = this.selectedItem;
			RadioContainer newSelection = InputRadio.this.itemsContainer.get(selection);
			if (newSelection != null) {
				newSelection.select();
			}
			this.selectedItem = selection;
			if (fireEvents) {
				ValueChangeEvent.fireIfNotEqual(InputRadio.this, oldValue, selection);
			}
		}

		@Override
		public T getSelection() {
			return this.selectedItem;
		}
	}

	private class RadioContainer extends Composite implements ClickHandler {

		private final InputElement radioElement = InputElement.as(Document.get().createRadioInputElement(
			"radio" + InputRadio.this.radioGroupNum));

		private T value;

		RadioContainer(T value) {
			this.value = value;
			Container newContainer = null;
			Container label = new Container(LabelElement.TAG);
			if (Type.INLINE.equals(InputRadio.this.type)) {
				newContainer = label;
			} else {
				newContainer = new Container();
				newContainer.append(label);
			}
			this.initWidget(newContainer);
			label.getElement().appendChild(this.radioElement);
			label.getElement().appendChild(
				Document.get().createTextNode(" " + InputRadio.this.getChoiceRenderer().renderItem(value)));
			StyleUtils.addStyle(newContainer, InputRadio.this.type);
			newContainer.addDomHandler(this, ClickEvent.getType());
			InputRadio.this.itemsContainer.put(value, this);
		}

		@Override
		public void onClick(ClickEvent event) {
			InputRadio.this.getSelectionHandler().onItemClick(this.value);
		}

		public void select() {
			this.radioElement.setChecked(true);
		}
	}

	private static long seq = 0;

	private final long radioGroupNum = InputRadio.incrementeAndGetSeq();

	private ChoiceSelectionHandler<T, T> selectionHandler = new SingleChoiceHandler();
	private ChoiceRenderer<T> choiceRenderer = new ChoiceRendererImpl();

	private final Container container;
	private final Map<T, RadioContainer> itemsContainer = Maps.newHashMap();
	private Type type = Type.DEFAULT;

	public InputRadio() {
		super(new Container());
		this.container = (Container) this.getWidget();
		this.setMultiple(true);
		this.endConstruct();
	}

	protected InputRadio(InputRadio<T> source) {
		super(new Container(), source);
		this.container = (Container) this.getWidget();
		this.endConstruct();
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputRadio<T>(this);
	}

	@Override
	protected void redrawInternal() {
		this.container.clear();
		this.itemsContainer.clear();
		for (T item : this.getOrderedItems()) {
			this.container.add(new RadioContainer(item));
		}
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	protected ChoiceRenderer<T> getChoiceRenderer() {
		return this.choiceRenderer;
	}

	@Override
	protected void setChoiceRenderer(ChoiceRenderer<T> choiceRenderer) {
		this.choiceRenderer = choiceRenderer;
	}

	@Override
	protected ChoiceSelectionHandler<T, T> getSelectionHandler() {
		return this.selectionHandler;
	}

	@Override
	protected void setSelectionHandler(ChoiceSelectionHandler<T, T> selectionHandler) {
		this.selectionHandler = selectionHandler;
	}

	static long incrementeAndGetSeq() {
		return ++InputRadio.seq;
	}

}
