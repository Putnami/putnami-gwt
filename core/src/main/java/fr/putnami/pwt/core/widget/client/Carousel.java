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

import java.util.List;

import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
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

		ItemType(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private enum LeftRightType implements CssStyle {
			LEFT("left"),
			RIGHT("right");

		private final String style;

		LeftRightType(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private class IndicatorItem extends ListItem implements ClickHandler {
		private final int itemIndex;

		IndicatorItem(int itemIndex) {
			this.itemIndex = itemIndex;
			this.addClickHandler(this);
		}

		@Override
		public void onClick(ClickEvent event) {
			Carousel.this.goTo(this.itemIndex);
		}
	}

	private final Container carouselInner = new Container();
	private final fr.putnami.pwt.core.widget.client.List carouselIndicators = new fr.putnami.pwt.core.widget.client.List(
		OListElement.TAG);
	private final Anchor<?> leftButton = new Anchor<>();
	private final Anchor<?> rightButton = new Anchor<>();
	private Timer autoPlayTimer = new Timer() {

		@Override
		public void run() {
			Carousel.this.next();
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
		this.append(this.carouselIndicators);
		this.append(this.carouselInner);
		this.append(this.leftButton);
		this.append(this.rightButton);
		StyleUtils.addStyle(this, Carousel.STYLE_CAROUSEL);
		StyleUtils.addStyle(this.carouselInner, Carousel.STYLE_CAROUSEL_INNER);
		StyleUtils.addStyle(this.carouselIndicators, Carousel.STYLE_CAROUSEL_INDICATORS);
		StyleUtils.addStyle(this.leftButton, Carousel.STYLE_CAROUSEL_CONTROL);
		StyleUtils.addStyle(this.leftButton, LeftRightType.LEFT);
		StyleUtils.addStyle(this.rightButton, Carousel.STYLE_CAROUSEL_CONTROL);
		StyleUtils.addStyle(this.rightButton, LeftRightType.RIGHT);
		this.leftButton.addClickHandler(this);
		this.rightButton.addClickHandler(this);
		this.leftButton.getElement().setInnerHTML("<i class=\"icon-prev\"/>");
		this.rightButton.getElement().setInnerHTML("<i class=\"icon-next\"/>");
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		this.autoPlayTimer.cancel();
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		this.setAutoPlay(this.autoPlay);
	}

	public void previous() {
		this.goTo(this.currentIndex - 1);
	}

	public void next() {
		this.goTo(this.currentIndex + 1);
	}

	public void goTo(int itemIndex) {
		if (itemIndex == this.currentIndex || this.sliding) {
			return;
		}
		this.sliding = true;
		final int newItemIndex;
		if (itemIndex < 0) {
			newItemIndex = this.carouselItems.size() - 1;
		} else if (itemIndex >= this.carouselItems.size()) {
			newItemIndex = 0;
		} else {
			newItemIndex = itemIndex;
		}

		StyleUtils.addStyle(this.carouselItemsIndicators.get(this.currentIndex), ItemType.DEFAULT);

		boolean goLeft = newItemIndex > this.currentIndex;
		final CarouselItem curentItem = this.carouselItems.get(this.currentIndex);
		final CarouselItem newItem = this.carouselItems.get(newItemIndex);

		StyleUtils.addStyle(newItem, goLeft ? ItemType.NEXT : ItemType.PREVIOUS);
		// hook to force width evaluation (cf original bootstrap JS file)
		this.carouselItems.get(newItemIndex).getOffsetWidth();
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
				StyleUtils.addStyle(Carousel.this.carouselItemsIndicators.get(newItemIndex), ItemType.ACTIVE);
				Carousel.this.currentIndex = newItemIndex;
				Carousel.this.sliding = false;
				return false;
			}
		}, 600);
	}

	@Override
	public void add(IsWidget child) {
		if (child instanceof CarouselItem) {
			this.addItem((CarouselItem) child);
		}
	}

	private void addItem(CarouselItem item) {
		IndicatorItem indicator = new IndicatorItem(this.carouselItems.size());
		this.carouselIndicators.addListItem(indicator);
		this.carouselItemsIndicators.add(indicator);
		this.carouselInner.add(item);
		this.carouselItems.add(item);
		if (this.carouselItems.size() == 1) {
			StyleUtils.addStyle(indicator, ItemType.ACTIVE);
			StyleUtils.addStyle(item, ItemType.ACTIVE);
		}
	}

	public void setAutoPlay(boolean autoPlay) {
		this.autoPlay = autoPlay;
		this.handlerRegistrations.removeHandler();
		if (autoPlay) {
			this.autoPlayTimer.scheduleRepeating(this.timerDelay);
			this.handlerRegistrations.add(this.addDomHandler(this, MouseOverEvent.getType()));
			this.handlerRegistrations.add(this.addDomHandler(this, MouseOutEvent.getType()));
		} else {
			this.autoPlayTimer.cancel();
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
			if (this.leftButton.getParent() == null && this.rightButton.getParent() == null) {
				this.append(this.leftButton);
				this.append(this.rightButton);
			}
		} else {
			this.leftButton.removeFromParent();
			this.rightButton.removeFromParent();
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		if (this.leftButton.equals(event.getSource())) {
			this.previous();
		} else if (this.rightButton.equals(event.getSource())) {
			this.next();
		}
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		if (this.pauseOnHover && this.autoPlay) {
			this.autoPlayTimer.cancel();
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		if (this.pauseOnHover && this.autoPlay) {
			this.autoPlayTimer.scheduleRepeating(this.timerDelay);
		}
	}

}
