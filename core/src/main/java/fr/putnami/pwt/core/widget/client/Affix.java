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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public class Affix extends OneWidgetPanel {

	public enum Affixed implements CssStyle {
		AFFIX("affix"),
		TOP("affix-top"),
		BOTTOM("affix-bottom");

		private final String style;

		private Affixed(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}

	}

	private final HandlerRegistrationCollection handlerRegistrationCollection = new HandlerRegistrationCollection();
	private final ScrollHandler scrollHandler = new ScrollHandler() {

		@Override
		public void onWindowScroll(ScrollEvent event) {
			resetPosistion();
		}
	};
	private final ResizeHandler resizeHandler = new ResizeHandler() {

		@Override
		public void onResize(ResizeEvent event) {
			reset();
		}
	};

	private Affixed affixed = Affixed.TOP;

	private int clientHeigth = -1;
	private int pinnedOffset = -1;
	private int offsetWidth = -1;
	private int offsetHeight = -1;

	private int layerIndex = 1000;

	private int offsetTop = 0;
	private int offsetBottom = 0;
	private int fixBottom = Integer.MIN_VALUE;

	public Affix() {
		super(DivElement.TAG);
		StyleUtils.toggleStyle(Affix.this, affixed, true);
	}

	protected Affix(Affix source) {
		super(source);

		this.layerIndex = source.layerIndex;
		this.offsetTop = source.offsetTop;
		this.offsetBottom = source.offsetBottom;
		this.fixBottom = source.fixBottom;

		setWidget(WidgetUtils.cloneWidget(source.getWidget()));
	}

	@Override
	public IsWidget cloneWidget() {
		return new Affix(this);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				reset();
				handlerRegistrationCollection.add(Window.addWindowScrollHandler(scrollHandler));
				handlerRegistrationCollection.add(Window.addResizeHandler(resizeHandler));
			}
		});
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		handlerRegistrationCollection.removeHandler();
	}

	public int getLayerIndex() {
		return layerIndex;
	}

	public void setLayerIndex(int zIndex) {
		this.layerIndex = zIndex;
		getElement().getStyle().setZIndex(layerIndex);
	}

	public int getOffsetTop() {
		return offsetTop;
	}

	public void setOffsetTop(int offsetTop) {
		this.offsetTop = offsetTop;
	}

	public int getOffsetBottom() {
		return offsetBottom;
	}

	public void setOffsetBottom(int offsetBottom) {
		this.offsetBottom = offsetBottom;
	}

	public int getFixBottom() {
		return fixBottom;
	}

	public void setFixBottom(int fixBottom) {
		this.fixBottom = fixBottom;
	}

	public void resetPosistion() {
		if (!isVisible()) {
			return;
		}
		int scrollTop = Window.getScrollTop();
		int docHeigth = Document.get().getScrollHeight();

		getElement().getStyle().clearHeight();
		this.offsetHeight = getElement().getClientHeight();

		int top = pinnedOffset - scrollTop - offsetTop;
		int bottom = docHeigth - scrollTop - offsetBottom - offsetTop - offsetHeight;
		if (bottom <= 0 || fixBottom != Integer.MIN_VALUE) {
			toggleAffix(Affixed.BOTTOM);
		}
		else if (top >= 0) {
			toggleAffix(Affixed.TOP);
		}
		else {
			toggleAffix(Affixed.AFFIX);
		}
	}

	protected void toggleAffix(Affixed affix) {
		Element e = getElement();
		Style style = e.getStyle();

		if (this.affixed != affix) {
			clearElementStyle();
			this.affixed = affix;
			StyleUtils.addStyle(e, this.affixed);
		}

		switch (affix) {
		case AFFIX:
			style.setTop(offsetTop, Unit.PX);
			style.setWidth(offsetWidth, Unit.PX);
			style.setHeight(offsetHeight, Unit.PX);
			style.setZIndex(layerIndex);
			e.getParentElement().getStyle().setPaddingTop(offsetHeight, Unit.PX);
			break;
		case BOTTOM:
			int docHeigth = Document.get().getScrollHeight();
			int scrollTop = Window.getScrollTop();

			int bottom = offsetBottom - (docHeigth - scrollTop - clientHeigth);
			if (fixBottom != Integer.MIN_VALUE) {
				bottom = Math.max(bottom, fixBottom);
			}
			style.setPosition(Position.FIXED);
			style.setBottom(bottom, Unit.PX);
			style.setWidth(offsetWidth, Unit.PX);
			style.setHeight(offsetHeight, Unit.PX);
			style.setZIndex(layerIndex);
			break;
		default:
			break;
		}
	}

	protected void reset() {
		Element e = getElement();

		StyleUtils.addStyle(e, Affixed.TOP);
		clearElementStyle();

		this.clientHeigth = Window.getClientHeight();
		this.pinnedOffset = e.getAbsoluteTop();
		this.offsetWidth = e.getClientWidth();

		StyleUtils.addStyle(e, this.affixed);

		resetPosistion();
	}

	private void clearElementStyle() {
		Element e = getElement();
		Style style = e.getStyle();

		style.clearPosition();
		style.clearTop();
		style.clearBottom();
		style.clearWidth();
		style.clearHeight();
		style.clearZIndex();
		e.getParentElement().getStyle().clearPaddingTop();
	}
}
