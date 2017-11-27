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
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractComposite;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.event.PageChangeEvent;
import fr.putnami.pwt.core.widget.client.event.PageChangeEvent.Handler;
import fr.putnami.pwt.core.widget.client.event.PageChangeEvent.HasPageChangeHandlers;
import fr.putnami.pwt.core.widget.client.helper.PaginationHelper;
import fr.putnami.pwt.core.widget.client.util.AnchorUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Pagination extends AbstractComposite implements HasPageChangeHandlers, HasDrawable {

	private static final CssStyle STYLE_ACTIVE = new SimpleStyle("active");

	private static final CssStyle PREVIOUS_STYLE = new SimpleStyle("previous");
	private static final CssStyle NEXT_STYLE = new SimpleStyle("next");

	public enum Size implements CssStyle {

			SMALL("pagination-sm"),
			DEFAULT(null),
			LARGE("pagination-lg");

		private final String style;

		Size(String style) {
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

		Style(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private class Page extends Widget implements ClickHandler {
		private final int page;

		Page(String label, int page) {
			this.setElement(Document.get().createLIElement());
			this.page = page;
			if (Pagination.this.currentPage == page) {
				StyleUtils.addStyle(this, Pagination.STYLE_ACTIVE);
			} else {
				this.addDomHandler(this, ClickEvent.getType());
			}
			AnchorElement anchor = Document.get().createAnchorElement();
			anchor.setHref(AnchorUtils.DUMMY_HREF);
			anchor.setInnerText(label);
			this.getElement().appendChild(anchor);
		}

		@Override
		public void onClick(ClickEvent event) {
			int oldPage = Pagination.this.currentPage;
			Pagination.this.setCurrentPage(this.page);
			if (oldPage != this.page) {
				EventBus.get().fireEventFromSource(new PageChangeEvent(this.page), Pagination.this);
			}
		}
	}

	private class NavigationPage extends Widget implements ClickHandler {

		private final boolean previous;

		NavigationPage(boolean previous) {
			this.previous = previous;
			this.setElement(Document.get().createLIElement());
			this.addDomHandler(this, ClickEvent.getType());
			AnchorElement anchor = Document.get().createAnchorElement();
			anchor.setHref(AnchorUtils.DUMMY_HREF);
			anchor.setInnerHTML(previous ? "&laquo;" : "&raquo;");
			this.getElement().appendChild(anchor);
			StyleUtils.addStyle(this, previous ? Pagination.PREVIOUS_STYLE : Pagination.NEXT_STYLE);
		}

		@Override
		public void onClick(ClickEvent event) {
			if (this.previous) {
				Pagination.this.currentStartPage -= Math.min(Pagination.this.nbPageMax, Pagination.this.nbPage);
			} else {
				Pagination.this.currentStartPage += Math.min(Pagination.this.nbPageMax, Pagination.this.nbPage);
			}
			Pagination.this.redrawFromCurrentStart();
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

	private boolean hideOnSinglePage = true;
	private PaginationHelper<?> paginationHelper;

	public Pagination() {
		this.initWidget(this.content);
		this.setStyle(this.style);
		this.setSize(this.size);
	}

	protected Pagination(Pagination source) {
		super(source);

		this.pageSize = source.pageSize;
		this.currentPage = source.currentPage;

		this.initWidget(this.content);
		this.setStyle(source.style);
		this.setSize(source.size);
	}

	@Override
	public IsWidget cloneWidget() {
		return new Pagination(this);
	}

	@Override
	public HandlerRegistration addPageChangeHandler(Handler handler) {
		return EventBus.get().addHandlerToSource(PageChangeEvent.TYPE, this, handler);
	}

	public <T> PaginationHelper<T> getPaginationHelper() {
		if (paginationHelper == null) {
			paginationHelper = new PaginationHelper<>(this);
		}
		return (PaginationHelper<T>) paginationHelper;
	}

	public <T> void setPaginationHelper(PaginationHelper<T> paginationHelper) {
		this.paginationHelper = paginationHelper;
	}

	public Style getStyle() {
		return this.style;
	}

	public void setStyle(Style style) {
		StyleUtils.addStyle(this, style);
		this.style = style;
	}

	public Size getSize() {
		return this.size;
	}

	public void setSize(Size size) {
		StyleUtils.addStyle(this, size);
		this.size = size;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		this.redraw();
	}

	public int getCurrentPage() {
		return this.currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
		this.redraw();
	}

	public int getNbPageMax() {
		return this.nbPageMax;
	}

	public void setNbPageMax(int nbPageMax) {
		this.nbPageMax = nbPageMax;
		this.redraw();
	}

	public int getNbPage() {
		return this.nbPage;
	}

	public void setNbPage(int nbPage) {
		this.nbPage = nbPage;
		this.redraw();
	}

	public boolean isHideOnSinglePage() {
		return hideOnSinglePage;
	}

	public void setHideOnSinglePage(boolean hideOnSinglePage) {
		this.hideOnSinglePage = hideOnSinglePage;
	}

	@Override
	public void redraw() {
		int nbPageToDraw = Math.min(this.nbPageMax, this.nbPage);
		this.currentStartPage = this.currentPage - nbPageToDraw / 2;
		this.redrawFromCurrentStart();
	}

	public void redrawFromCurrentStart() {
		this.content.clear();
		this.setVisible(!hideOnSinglePage || this.nbPage > 1);
		int nbPageToDraw = Math.min(this.nbPageMax, this.nbPage);
		this.currentStartPage = Math.max(0, this.currentStartPage);
		this.currentStartPage = Math.min(this.nbPage - nbPageToDraw, this.currentStartPage);
		if (this.currentStartPage > 0) {
			this.content.append(new NavigationPage(true));
		}
		for (int i = 0; i < nbPageToDraw; i++) {
			this.content.append(new Page(String.valueOf(this.currentStartPage + i + 1), this.currentStartPage + i));
		}
		if (this.currentStartPage + nbPageToDraw < this.nbPage) {
			this.content.append(new NavigationPage(false));
		}
	}

}
