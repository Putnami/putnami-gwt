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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.event.DirtyEvent.Handler;
import fr.putnami.pwt.core.editor.client.helper.MessageHelper;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.model.client.base.HasMessageHelper;
import fr.putnami.pwt.core.model.client.base.HasPlaceholder;
import fr.putnami.pwt.core.widget.client.ButtonDropdown;
import fr.putnami.pwt.core.widget.client.DropdownHeader;
import fr.putnami.pwt.core.widget.client.NavLink;
import fr.putnami.pwt.core.widget.client.event.AskFocusEvent;
import fr.putnami.pwt.core.widget.client.event.ChangeEvent;
import fr.putnami.pwt.core.widget.client.helper.CompositeFocusHelper;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public abstract class AbstractInputSelect<T, U> extends AbstractInput<U> implements
		HasValueChangeHandlers<U>,
		ValueChangeHandler<U>,
		HasPlaceholder,
		HasMessageHelper,
		HasDrawable {

	private static final CssStyle STYLE_INPUT_SELECT = new SimpleStyle("input-select");

	private class ItemClickCommand implements ScheduledCommand {
		private T item;

		public ItemClickCommand(T item) {
			this.item = item;
		}

		@Override
		public void execute() {
			getSelectionHandler().onItemClick(item);
			AbstractInputSelect.this.setFocus(true);
		}
	}

	public interface ItemSelectionHandler<T, U> {

		void onItemClick(T item);

		void onHomeKeyDown();

		void onEndKeyDown();

		void onUpKeyDown();

		void onDownKeyDown();

		void setSelection(U selection, boolean fireEvents);

		U getSelection();
	}

	public interface SelectRenderer<T, U> {

		Renderer<T> getItemRenderer();

		void setItemRenderer(Renderer<T> renderer);

		Renderer<U> getSelectionRenderer();

		void setSelectionRenderer(Renderer<U> renderer);

		String renderItem(T item);

		String renderSelection(U selection);

		String getPlaceholder();

		void setPlaceholder(String placeholder);

		String getNullRender();

		void setNullRender(String nullRender);

	}

	private class SelectRendererImpl implements SelectRenderer<T, U> {

		private static final String DEFAULT_NULL_RENDER = "";

		private Renderer<T> itemRenderer;
		private Renderer<U> selectionRenderer;
		private String nullRender;
		private String placeholder;

		@Override
		public Renderer<T> getItemRenderer() {
			return itemRenderer;
		}

		@Override
		public void setItemRenderer(Renderer<T> renderer) {
			this.itemRenderer = renderer;
		}

		@Override
		public Renderer<U> getSelectionRenderer() {
			return selectionRenderer;
		}

		@Override
		public void setSelectionRenderer(Renderer<U> renderer) {
			this.selectionRenderer = renderer;
		}

		@Override
		public String renderItem(T item) {
			return render(item, itemRenderer);
		}

		@Override
		public String renderSelection(U selection) {
			return render(selection, selectionRenderer);
		}

		@Override
		public String getPlaceholder() {
			return placeholder;
		}

		@Override
		public void setPlaceholder(String placeholder) {
			this.placeholder = placeholder;
		}

		@Override
		public String getNullRender() {
			return nullRender;
		}

		@Override
		public void setNullRender(String nullRender) {
			this.nullRender = nullRender;
		}

		private <V> String render(V value, Renderer<V> renderer) {
			if (value == null) {
				return renderNull();
			}
			if (renderer != null) {
				return renderer.render(value);
			}
			return value.toString();
		}

		private String renderNull() {
			if (AbstractInputSelect.this.nullValueAllowed) {
				return this.nullRender != null ? this.nullRender : DEFAULT_NULL_RENDER;
			}
			else {
				return this.placeholder != null ? this.placeholder : DEFAULT_NULL_RENDER;
			}
		}
	}

	private final SelectRenderer<T, U> selectRenderer = new SelectRendererImpl();

	private HandlerRegistration valueChangeRegistration;

	private CompositeFocusHelper compositeFocusHelper;

	private MessageHelper messageHelper;

	private final Map<String, Collection<T>> itemsMap;
	private Map<T, NavLink> itemsLinks = Maps.newHashMap();
	private List<T> orderedItems = Lists.newArrayList();

	private ItemSelectionHandler<T, U> selectionHandler;

	private boolean nullValueAllowed = false;
	private boolean multiple = false;

	private final SimpleDropdown dropdown;

	private HandlerRegistration askFocusRegistration;

	private String htmlId;

	public AbstractInputSelect() {
		super(new ButtonDropdown());
		itemsMap = Maps.newLinkedHashMap();
		dropdown = (SimpleDropdown) getWidget();
		endConstruct();
	}

	protected AbstractInputSelect(AbstractInputSelect<T, U> source) {
		super(new ButtonDropdown(), source);
		dropdown = (SimpleDropdown) getWidget();
		endConstruct();
		messageHelper = source.messageHelper;
		multiple = source.multiple;
		nullValueAllowed = source.nullValueAllowed;
		selectionHandler = source.selectionHandler;

		itemsMap = source.itemsMap;

		setNullRender(source.selectRenderer.getNullRender());
		setItemRenderer(source.selectRenderer.getItemRenderer());
		setSelectionRenderer(source.selectRenderer.getSelectionRenderer());
		redraw();
	}

	@Override
	protected void endConstruct() {
		StyleUtils.removeStyle(this, STYLE_CONTROL);
		StyleUtils.removeStyle(this, ButtonDropdown.STYLE_BUTTON_GROUP);
		StyleUtils.addStyle(this, STYLE_INPUT_SELECT);

		compositeFocusHelper = CompositeFocusHelper.createFocusHelper(this, dropdown);
		dropdown.addAnchorStyle(STYLE_CONTROL);

		addValueChangeHandler(this);
		sinkEvents(Event.ONKEYDOWN);

		super.endConstruct();
	}

	public void setItems(Collection<T> items) {
		Map<String, Collection<T>> itemsMap = Maps.newHashMapWithExpectedSize(1);
		itemsMap.put(null, items);
		setItemsMap(itemsMap);
	}

	public void setItemsMap(Map<String, Collection<T>> itemsMap) {
		U currentValue = getValue();
		this.itemsMap.clear();
		this.itemsMap.putAll(itemsMap);
		edit(currentValue);
	}

	private void redrowDropdown() {

		this.dropdown.clear();
		this.itemsLinks.clear();
		this.orderedItems.clear();

		if (nullValueAllowed && !multiple) {
			addItem(null);
		}

		if (itemsMap != null) {
			for (Entry<String, Collection<T>> entry : itemsMap.entrySet()) {
				if (entry.getKey() != null) {
					dropdown.addMenuContent(new DropdownHeader(entry.getKey()));
				}
				addItemCollection(entry.getValue());
			}
		}

	}

	private void addItemCollection(Collection<T> items) {
		if (items != null) {
			for (T item : items) {
				addItem(item);
			}
		}
	}

	public void addItem(T item) {
		NavLink link = new NavLink(selectRenderer.renderItem(item), new ItemClickCommand(item));
		dropdown.addMenuContent(link);
		this.itemsLinks.put(item, link);
		this.orderedItems.add(item);
	}

	public void setNullRender(String nullRender) {
		selectRenderer.setNullRender(nullRender);
		renderLabel();
	}

	public void setNullValueAllowed(boolean nullValueAllowed) {
		this.nullValueAllowed = nullValueAllowed;
	}

	public boolean isNullValueAllowed() {
		return nullValueAllowed;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public void setItemRenderer(Renderer<T> renderer) {
		selectRenderer.setItemRenderer(renderer);
	}

	public void setSelectionRenderer(Renderer<U> renderer) {
		selectRenderer.setSelectionRenderer(renderer);
	}

	@Override
	public void setHtmlId(String htmlId) {
		this.htmlId = htmlId;
		dropdown.getAnchor().getElement().setId(htmlId);
		if (askFocusRegistration != null) {
			askFocusRegistration.removeHandler();
		}
		if (htmlId != null) {
			AskFocusEvent.Handler handler = new AskFocusEvent.Handler() {

				@Override
				public void onAskFocus(AskFocusEvent event) {
					if (Objects.equal(event.getHtmlId(), AbstractInputSelect.this.htmlId)) {
						setFocus(true);
					}
				}
			};
			askFocusRegistration = EventBus.get().addHandler(AskFocusEvent.TYPE, handler);
		}
	}

	@Override
	public String getHtmlId() {
		return dropdown.getAnchor().getElement().getId();
	}

	@Override
	public MessageHelper getMessageHelper() {
		return messageHelper;
	}

	@Override
	public void setMessageHelper(MessageHelper messageHelper) {
		this.messageHelper = messageHelper;
	}

	@Override
	public String getPlaceholder() {
		return selectRenderer.getPlaceholder();
	}

	@Override
	public void setPlaceholder(String placeholder) {
		selectRenderer.setPlaceholder(placeholder);
		renderLabel();
	}

	@Override
	public boolean isDirty() {
		return !Objects.equal(this.getValue(), this.getSelectedValue());
	}

	@Override
	public HandlerRegistration addDirtyHandler(Handler handler) {
		if (valueChangeRegistration == null) {
			valueChangeRegistration = addValueChangeHandler(new ChangeEvent<U>(AbstractInputSelect.this));
		}
		return super.addDirtyHandler(handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addValueChangeHandler(ValueChangeHandler<U> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	public U getSelectedValue() {
		return getSelectionHandler().getSelection();
	}

	@Override
	public U flush() {
		clearErrors();
		U value = getSelectedValue();
		validate(value);
		if (!hasErrors()) {
			setValue(value);
		}
		return getValue();
	}

	@Override
	public void edit(U value) {
		redrowDropdown();
		setValue(value);
		getSelectionHandler().setSelection(getValue(), false);
		renderLabel();
	}

	private void renderLabel() {
		dropdown.setLabel(selectRenderer.renderSelection(getValue()));
	}

	@Override
	public void onValueChange(ValueChangeEvent<U> event) {
		if (event != null && event.getSource() == this) {
			dropdown.setLabel(selectRenderer.renderSelection(event.getValue()));
		}
	}

	@Override
	public void redraw() {
		if (!itemsMap.isEmpty()) {
			setItemsMap(Maps.newLinkedHashMap(itemsMap));
		}
	}

	@Override
	public void setFocus(boolean focused) {
		dropdown.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		dropdown.setTabIndex(index);
	}

	@Override
	public int getTabIndex() {
		return dropdown.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		dropdown.setAccessKey(key);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addBlurHandler(BlurHandler handler) {
		return compositeFocusHelper.addBlurHandler(handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addFocusHandler(FocusHandler handler) {
		return compositeFocusHelper.addFocusHandler(handler);
	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);
		boolean mustKillEvent = false;
		switch (DOM.eventGetType(event)) {
		case Event.ONKEYDOWN:
			switch (event.getKeyCode()) {
			case KeyCodes.KEY_HOME:
				getSelectionHandler().onHomeKeyDown();
				mustKillEvent = true;
				break;
			case KeyCodes.KEY_END:
				getSelectionHandler().onEndKeyDown();
				mustKillEvent = true;
				break;
			case KeyCodes.KEY_UP:
				getSelectionHandler().onUpKeyDown();
				mustKillEvent = true;
				break;
			case KeyCodes.KEY_DOWN:
				getSelectionHandler().onDownKeyDown();
				mustKillEvent = true;
				break;
			}
			break;
		}
		if (mustKillEvent) {
			event.preventDefault();
			event.stopPropagation();
		}
	}

	protected SelectRenderer<T, U> getSelectRenderer() {
		return selectRenderer;
	}

	protected ItemSelectionHandler<T, U> getSelectionHandler() {
		assert selectionHandler != null : "SelectionHandler is not set !";
		return selectionHandler;
	}

	protected void setSelectionHandler(ItemSelectionHandler<T, U> selectionHandler) {
		this.selectionHandler = selectionHandler;
	}

	protected Map<T, NavLink> getItemsLinks() {
		return itemsLinks;
	}

	protected List<T> getOrderedItems() {
		return orderedItems;
	}

	protected SimpleDropdown getDropdown() {
		return dropdown;
	}

}
