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

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.OListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Carousel extends AbstractPanel implements ClickHandler, MouseOverHandler, MouseOutHandler {

	private static final CssStyle STYLE_CAROUSEL = new SimpleStyle("carousel");
	private static final CssStyle STYLE_CAROUSEL_INDICATORS = new SimpleStyle("carousel-indicators");

	private static final CssStyle STYLE_CAROUSEL_INNER = new SimpleStyle("carousel-inner");

	private static final CssStyle STYLE_CAROUSEL_CONTROL = new SimpleStyle("carousel-control");

	private enum ItemType implements CssStyle {
		DEFAULT(null),
		ACTIVE("active"),
		NEXT("next"),
		PREVIOUS("prev");

		private final String style;

		private ItemType(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return style;
		}
	}

	private enum LeftRightType implements CssStyle {
		LEFT("left"),
		RIGHT("right");

		private final String style;

		private LeftRightType(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return style;
		}
	}

	private class IndicatorItem extends ListItem implements ClickHandler {
		private final int itemIndex;

		public IndicatorItem(int itemIndex) {
			this.itemIndex = itemIndex;
			addClickHandler(this);
		}

		@Override
		public void onClick(ClickEvent event) {
			goTo(itemIndex);
		}
	}

	private final Container carouselInner = new Container();
	private final fr.putnami.pwt.core.widget.client.List carouselIndicators = new fr.putnami.pwt.core.widget.client.List(OListElement.TAG);
	private final Anchor<?> leftButton = new Anchor();
	private final Anchor<?> rightButton = new Anchor();

	private Timer autoPlayTimer = new Timer() {

		@Override
		public void run() {
			next();
		}
	};
	private boolean pauseOnHover = true;
	private boolean autoPlay = false;
	private int timerDelay = 5000;
	private HandlerRegistrationCollection handlerRegistrations = new HandlerRegistrationCollection();

	private boolean sliding = false;

	private final List<CarouselItem> carouselItems = Lists.newArrayList();
	private final List<IndicatorItem> carouselItemsIndicators = Lists.newArrayList();
	private int currentIndex = 0;

	public Carousel() {
		super(DivElement.TAG);
		append(carouselIndicators);
		append(carouselInner);
		append(leftButton);
		append(rightButton);
		StyleUtils.addStyle(this, STYLE_CAROUSEL);
		StyleUtils.addStyle(carouselInner, STYLE_CAROUSEL_INNER);
		StyleUtils.addStyle(carouselIndicators, STYLE_CAROUSEL_INDICATORS);
		StyleUtils.addStyle(leftButton, STYLE_CAROUSEL_CONTROL);
		StyleUtils.addStyle(leftButton, LeftRightType.LEFT);
		StyleUtils.addStyle(rightButton, STYLE_CAROUSEL_CONTROL);
		StyleUtils.addStyle(rightButton, LeftRightType.RIGHT);
		leftButton.addClickHandler(this);
		rightButton.addClickHandler(this);
		leftButton.getElement().setInnerHTML("<i class=\"icon-prev\"/>");
		rightButton.getElement().setInnerHTML("<i class=\"icon-next\"/>");

	}

	@Override
	protected void onUnload() {
		super.onUnload();
		autoPlayTimer.cancel();
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		setAutoPlay(autoPlay);
	}

	public void previous() {
		goTo(currentIndex - 1);
	}

	public void next() {
		goTo(currentIndex + 1);
	}

	public void goTo(int itemIndex) {
		if (itemIndex == currentIndex || sliding) {
			return;
		}
		sliding = true;
		final int newItemIndex;
		if (itemIndex < 0) {
			newItemIndex = carouselItems.size() - 1;
		}
		else if (itemIndex >= carouselItems.size()) {
			newItemIndex = 0;
		}
		else {
			newItemIndex = itemIndex;
		}

		StyleUtils.addStyle(carouselItemsIndicators.get(currentIndex), ItemType.DEFAULT);

		boolean goLeft = newItemIndex > currentIndex;
		final CarouselItem curentItem = carouselItems.get(currentIndex);
		final CarouselItem newItem = carouselItems.get(newItemIndex);

		StyleUtils.addStyle(newItem, goLeft ? ItemType.NEXT : ItemType.PREVIOUS);
		// hook to force width evaluation (cf original bootstrap JS file)
		carouselItems.get(newItemIndex).getOffsetWidth();
		StyleUtils.addStyle(newItem, goLeft ? LeftRightType.LEFT : LeftRightType.RIGHT);
		StyleUtils.addStyle(curentItem, goLeft ? LeftRightType.LEFT : LeftRightType.RIGHT);

		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

			@Override
			public boolean execute() {
				StyleUtils.removeStyle(curentItem, ItemType.DEFAULT);
				StyleUtils.removeStyle(newItem, ItemType.ACTIVE);
				StyleUtils.addStyle(curentItem, ItemType.DEFAULT);
				StyleUtils.addStyle(newItem, ItemType.ACTIVE);
				StyleUtils.cleanEnumStyle(curentItem.getElement(), LeftRightType.class);
				StyleUtils.cleanEnumStyle(newItem.getElement(), LeftRightType.class);
				StyleUtils.addStyle(carouselItemsIndicators.get(newItemIndex), ItemType.ACTIVE);
				currentIndex = newItemIndex;
				sliding = false;
				return false;
			}
		}, 600);

	}

	@Override
	public void add(IsWidget child) {
		if (child instanceof CarouselItem) {
			addItem((CarouselItem) child);
		}
	}

	private void addItem(CarouselItem item) {
		IndicatorItem indicator = new IndicatorItem(carouselItems.size());
		carouselIndicators.addListItem(indicator);
		carouselItemsIndicators.add(indicator);
		carouselInner.add(item);
		carouselItems.add(item);
		if (carouselItems.size() == 1) {
			StyleUtils.addStyle(indicator, ItemType.ACTIVE);
			StyleUtils.addStyle(item, ItemType.ACTIVE);
		}
	}

	public void setAutoPlay(boolean autoPlay) {
		this.autoPlay = autoPlay;
		handlerRegistrations.removeHandler();
		if (autoPlay) {
			autoPlayTimer.scheduleRepeating(timerDelay);
			handlerRegistrations.add(addDomHandler(this, MouseOverEvent.getType()));
			handlerRegistrations.add(addDomHandler(this, MouseOutEvent.getType()));
		}
		else {
			autoPlayTimer.cancel();
		}
	}

	public void setPauseOnHover(boolean pauseOnHover) {
		this.pauseOnHover = pauseOnHover;
	}

	public void setTimerDelay(int timerDelay) {
		this.timerDelay = timerDelay;
	}

	public void setDisplayNavButtons(boolean displayNavButtons) {
		if (displayNavButtons) {
			if (leftButton.getParent() == null && rightButton.getParent() == null) {
				append(leftButton);
				append(rightButton);
			}
		}
		else {
			leftButton.removeFromParent();
			rightButton.removeFromParent();
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		if (leftButton.equals(event.getSource())) {
			previous();
		}
		else if (rightButton.equals(event.getSource())) {
			next();
		}
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		if (pauseOnHover && autoPlay) {
			autoPlayTimer.cancel();
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		if (pauseOnHover && autoPlay) {
			autoPlayTimer.scheduleRepeating(timerDelay);
		}
	}

}
