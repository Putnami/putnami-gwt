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
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.widget.client.base.AbstractComposite;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.PageChangeEvent;
import fr.putnami.pwt.core.widget.client.event.PageChangeEvent.Handler;
import fr.putnami.pwt.core.widget.client.event.PageChangeEvent.HasPageChangeHandlers;
import fr.putnami.pwt.core.widget.client.util.AnchorUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Pagination extends AbstractComposite implements HasPageChangeHandlers, HasDrawable {

	private static final CssStyle STYLE_ACTIVE = new SimpleStyle("active");
	private static final CssStyle STYLE_DISABLED = new SimpleStyle("disabled");

	private static final CssStyle PREVIOUS_STYLE = new SimpleStyle("previous");
	private static final CssStyle NEXT_STYLE = new SimpleStyle("next");

	public enum Size implements CssStyle {

		SMALL("pagination-sm"),
		DEFAULT(null),
		LARGE("pagination-lg");

		private final String style;

		private Size(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}

	}

	public enum Style implements CssStyle {
		PAGER("pager"),
		PAGINATION("pagination");

		private final String style;

		private Style(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}

	}

	private class Page extends Widget implements ClickHandler {
		private final int page;

		public Page(String label, int page) {
			setElement(Document.get().createLIElement());
			this.page = page;
			if (currentPage == page) {
				StyleUtils.addStyle(this, STYLE_ACTIVE);
			}
			else {
				addDomHandler(this, ClickEvent.getType());
			}
			AnchorElement anchor = Document.get().createAnchorElement();
			anchor.setHref(AnchorUtils.DUMMY_HREF);
			anchor.setInnerText(label);
			getElement().appendChild(anchor);
		}

		@Override
		public void onClick(ClickEvent event) {
			int oldPage = currentPage;
			setCurrentPage(page);
			if (oldPage != page) {
				EventBus.get().fireEventFromSource(new PageChangeEvent(page), Pagination.this);
			}
		}
	}

	private class NavigationPage extends Widget implements ClickHandler {

		private final boolean previous;

		public NavigationPage(boolean previous) {
			this.previous = previous;
			setElement(Document.get().createLIElement());
			addDomHandler(this, ClickEvent.getType());
			AnchorElement anchor = Document.get().createAnchorElement();
			anchor.setHref(AnchorUtils.DUMMY_HREF);
			anchor.setInnerHTML(previous ? "&laquo;" : "&raquo;");
			getElement().appendChild(anchor);
			StyleUtils.addStyle(this, previous ? PREVIOUS_STYLE : NEXT_STYLE);
		}

		@Override
		public void onClick(ClickEvent event) {
			if (previous) {
				currentStartPage -= Math.min(nbPageMax, nbPage);
			}
			else {
				currentStartPage += Math.min(nbPageMax, nbPage);
			}
			redrawFromCurrentStart();
		}
	}

	private final Container content = new Container(UListElement.TAG);

	private Size size = Size.DEFAULT;
	private Style style = Style.PAGINATION;

	private int pageSize = 10;
	private int currentPage = 0;
	private int nbPageMax = 10;
	private int nbPage;

	private int currentStartPage;

	public Pagination() {
		initWidget(content);
		setStyle(style);
		setSize(size);
	}

	protected Pagination(Pagination source) {
		super(source);

		this.pageSize = source.pageSize;
		this.currentPage = source.currentPage;

		initWidget(content);
		setStyle(source.style);
		setSize(source.size);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Pagination(this);
	}

	@Override
	public HandlerRegistration addPageChangeHandler(Handler handler) {
		return EventBus.get().addHandlerToSource(PageChangeEvent.TYPE, this, handler);
	}

	public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		StyleUtils.addStyle(this, style);
		this.style = style;
	}

	public Size getSize() {
		return size;
	}

	public void setSize(Size size) {
		StyleUtils.addStyle(this, size);
		this.size = size;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		redraw();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
		redraw();
	}

	public int getNbPageMax() {
		return nbPageMax;
	}

	public void setNbPageMax(int nbPageMax) {
		this.nbPageMax = nbPageMax;
		redraw();
	}

	public int getNbPage() {
		return nbPage;
	}

	public void setNbPage(int nbPage) {
		this.nbPage = nbPage;
		redraw();
	}

	@Override
	public void redraw() {
		int nbPageToDraw = Math.min(nbPageMax, nbPage);
		currentStartPage = currentPage - (nbPageToDraw / 2);
		redrawFromCurrentStart();
	}

	public void redrawFromCurrentStart() {
		content.clear();
		int nbPageToDraw = Math.min(nbPageMax, nbPage);
		currentStartPage = Math.max(0, currentStartPage);
		currentStartPage = Math.min(nbPage - nbPageToDraw, currentStartPage);
		if (currentStartPage > 0) {
			content.append(new NavigationPage(true));
		}
		for (int i = 0; i < nbPageToDraw; i++) {
			content.append(new Page("" + (currentStartPage + i + 1), currentStartPage + i));
		}
		if (currentStartPage + nbPageToDraw < nbPage) {
			content.append(new NavigationPage(false));
		}
	}

}
