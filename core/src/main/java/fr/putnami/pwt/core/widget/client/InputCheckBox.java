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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractInputChoice;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputCheckBox<T> extends AbstractInputChoice<T, List<T>> {

	public enum Type implements CssStyle {
			DEFAULT("checkbox"),
			INLINE("checkbox-inline");

		private final String style;

		Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private class MultiChoiceHandler implements ChoiceSelectionHandler<T, List<T>> {
		private List<T> selectedItems;

		@Override
		public void onItemClick(T item) {
			if (this.selectedItems == null) {
				this.selectedItems = new ArrayList<T>();
			}
			boolean selectItem = !this.selectedItems.contains(item);
			if (selectItem) {
				this.selectedItems.add(item);
			} else {
				this.selectedItems.remove(item);
				if (this.selectedItems.isEmpty()) {
					this.selectedItems = null;
				}
			}
			ValueChangeEvent.fire(InputCheckBox.this, this.selectedItems);
		}

		@Override
		public void setSelection(List<T> selection, boolean fireEvents) {
			List<T> oldSelection = this.selectedItems;
			if (oldSelection != null) {
				for (T item : oldSelection) {
					this.selectItem(item, false);
				}
			}

			this.selectedItems = selection;
			if (selection != null) {
				for (T item : selection) {
					this.selectItem(item, true);
				}
			}
			if (fireEvents) {
				ValueChangeEvent.fireIfNotEqual(InputCheckBox.this, oldSelection, selection);
			}
		}

		private void selectItem(T item, boolean selected) {
			CheckboxContainer currentSelection = InputCheckBox.this.itemsContainer.get(item);
			if (currentSelection != null) {
				currentSelection.setChecked(selected);
			}
		}

		@Override
		public List<T> getSelection() {
			return this.selectedItems;
		}
	}

	private class CheckboxContainer extends Composite implements ClickHandler {

		private final InputElement checkboxElement = InputElement.as(Document.get().createCheckInputElement());

		private T value;

		CheckboxContainer(T value) {
			this.value = value;
			Container newContainer = null;
			Container label = new Container(LabelElement.TAG);
			if (Type.INLINE.equals(InputCheckBox.this.type)) {
				newContainer = label;
			} else {
				newContainer = new Container();
				InputCheckBox.this.container.append(label);
			}
			this.initWidget(newContainer);
			label.getElement().appendChild(this.checkboxElement);
			label.getElement().appendChild(
				Document.get().createTextNode(" " + InputCheckBox.this.getChoiceRenderer().renderItem(value)));
			StyleUtils.addStyle(newContainer, InputCheckBox.this.type);
			newContainer.addDomHandler(this, ClickEvent.getType());
			InputCheckBox.this.itemsContainer.put(value, this);
		}

		@Override
		public void onClick(ClickEvent event) {
			InputCheckBox.this.getSelectionHandler().onItemClick(this.value);
		}

		public void setChecked(boolean checked) {
			this.checkboxElement.setChecked(checked);
		}
	}

	private ChoiceSelectionHandler<T, List<T>> selectionHandler = new MultiChoiceHandler();
	private ChoiceRenderer<T> choiceRenderer = new ChoiceRendererImpl();

	private final Container container;
	private final Map<T, CheckboxContainer> itemsContainer = Maps.newHashMap();
	private Type type = Type.DEFAULT;

	public InputCheckBox() {
		super(new Container());
		this.container = (Container) this.getWidget();
		this.setMultiple(true);
		this.endConstruct();
	}

	protected InputCheckBox(InputCheckBox<T> source) {
		super(new Container(), source);
		this.container = (Container) this.getWidget();
		this.endConstruct();
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputCheckBox<T>(this);
	}

	@Override
	protected void redrawInternal() {
		this.container.clear();
		this.itemsContainer.clear();
		for (T item : this.getOrderedItems()) {
			this.container.add(new CheckboxContainer(item));
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
	protected ChoiceSelectionHandler<T, List<T>> getSelectionHandler() {
		return this.selectionHandler;
	}

	@Override
	protected void setSelectionHandler(ChoiceSelectionHandler<T, List<T>> selectionHandler) {
		this.selectionHandler = selectionHandler;
	}

}
