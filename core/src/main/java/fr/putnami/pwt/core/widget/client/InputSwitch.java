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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.Map;

import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractInputChoice;
import fr.putnami.pwt.core.widget.client.base.AbstractInputSelect;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputSwitch<T> extends AbstractInputChoice<T, T> {

	private static final CssStyle STYLE_CHOICE_SLIDER = new SimpleStyle("input-switch");
	private static final CssStyle STYLE_ITEM = new SimpleStyle("switch-item");
	private static final CssStyle STYLE_CHOICE_ITEM_SLIDER = new SimpleStyle("switch-item-slider");
	private static final CssStyle STYLE_ACTIVE = new SimpleStyle("switch-item-active");

	private class SwitchHandler implements ChoiceSelectionHandler<T, T> {
		private T selectedItem;

		@Override
		public void onItemClick(T item) {
			this.selectedItem = item;
			ValueChangeEvent.fire(InputSwitch.this, this.selectedItem);
			InputSwitch.this.resetSlider(item);
		}

		@Override
		public void setSelection(T selection, boolean fireEvents) {
			T oldValue = this.selectedItem;
			InputSwitch.this.resetSlider(selection);
			this.selectedItem = selection;
			if (fireEvents) {
				ValueChangeEvent.fireIfNotEqual(InputSwitch.this, oldValue, selection);
			}
		}

		@Override
		public T getSelection() {
			return this.selectedItem;
		}
	}

	private class SwitchItem extends Container implements ClickHandler {

		private T value;

		SwitchItem(T value) {
			this.value = value;
			StyleUtils.addStyle(this, InputSwitch.STYLE_ITEM);
			this.getElement().setInnerHTML(InputSwitch.this.getChoiceRenderer().renderItem(value));
			this.addDomHandler(this, ClickEvent.getType());
		}

		@Override
		public void onClick(ClickEvent event) {
			InputSwitch.this.getSelectionHandler().onItemClick(this.value);
		}
	}

	private ChoiceSelectionHandler<T, T> selectionHandler = new SwitchHandler();
	private ChoiceRenderer<T> choiceRenderer = new ChoiceRendererImpl();

	private final Container container;
	private final Container slider;
	private final Map<T, SwitchItem> items = Maps.newHashMap();

	public InputSwitch() {
		super(new Container());
		this.container = (Container) this.getWidget();
		this.endConstruct();
		this.slider = new Container();
		StyleUtils.addStyle(this.slider, InputSwitch.STYLE_CHOICE_ITEM_SLIDER);
		StyleUtils.addStyle(this, InputSwitch.STYLE_CHOICE_SLIDER);
	}

	protected InputSwitch(AbstractInputSelect<T, T> source) {
		super(new Container(), source);
		this.container = (Container) this.getWidget();
		this.slider = new Container();
		StyleUtils.addStyle(this.slider, InputSwitch.STYLE_CHOICE_ITEM_SLIDER);
		this.endConstruct();
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputSwitch<T>();
	}

	@Override
	protected void redrawInternal() {
		this.container.clear();
		this.items.clear();
		this.container.add(this.slider);

		for (T item : this.getOrderedItems()) {
			SwitchItem choiceItem = new SwitchItem(item);
			this.container.add(choiceItem);
			this.items.put(item, choiceItem);
		}
		this.resetSlider(this.getValue());
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

	private void resetSlider(T value) {
		for (SwitchItem item : this.items.values()) {
			StyleUtils.removeStyle(item, InputSwitch.STYLE_ACTIVE);
		}
		final SwitchItem item = this.items.get(value);
		if (item != null) {

			StyleUtils.addStyle(item, InputSwitch.STYLE_ACTIVE);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					Element itemElement = item.getElement();
					InputSwitch.this.slider.getElement().getStyle().setWidth(itemElement.getClientWidth(), Unit.PX);
					InputSwitch.this.slider.getElement().getStyle().setMarginLeft(itemElement.getOffsetLeft() - 5, Unit.PX);
				}
			});
		}
	}

}
