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
package fr.putnami.pwt.core.widget.client.assist;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collection;

import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.Nav.LinkStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.AnchorUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.shared.assist.Oracle;
import fr.putnami.pwt.core.widget.shared.assist.Oracle.Suggestion;

public class ContentAssistAspect<T> {

	public interface SuggestionDisplay<T> {

		boolean isSuggestionListShowing();

		void showSuggestions(Oracle.Request request, IsWidget textInput,
			Collection<? extends Oracle.Suggestion<T>> suggestions,
			SuggestionCallback<T> suggestionCallback);

		void hideSuggestions();

		void moveSelectionDown();

		void moveSelectionUp();

		Oracle.Suggestion<T> getSelectedSelection();

		Widget getSuggestionWidget();
	}

	interface SuggestionCallback<T> {
		void onSuggestionSelected(Oracle.Suggestion<T> suggestion);
	}

	static class SuggestionItem<T> extends Widget {

		private final Oracle.Suggestion<T> suggestion;

		SuggestionItem(Oracle.Suggestion<T> suggestion, String displayedValue) {
			this.setElement(Document.get().createLIElement());
			this.suggestion = suggestion;
			AnchorElement anchor = Document.get().createAnchorElement();
			anchor.setHref(AnchorUtils.DUMMY_HREF);
			anchor.setInnerHTML(displayedValue);
			this.getElement().appendChild(anchor);
		}
	}

	static class DefaultContentAssistHandler<T> extends AbstractContentAssistHandler<T> {

		DefaultContentAssistHandler() {
			super(new SimpleOracle<T>());
		}

		@Override
		public String getQueryText(IsWidget textInput) {
			return "";
		}

		@Override
		public void handleSuggestionSelected(IsWidget textInput, Oracle.Suggestion<T> suggestion) {
			// DoNothing
		}

		@Override
		public ContentAssistHandler<T> copy() {
			return new DefaultContentAssistHandler<T>();
		}
	}

	class SuggestionDisplayImpl implements SuggestionDisplay<T> {

		private class DropdownMenu extends AbstractPanel {
			DropdownMenu() {
				super(UListElement.TAG);
				StyleUtils.addStyle(this, ContentAssistAspect.STYLE_MENU);
				StyleUtils.addStyle(this, ContentAssistAspect.STYLE_SCROLLABLE);
			}
		}

		private final DropdownMenu suggestionsContainer = new DropdownMenu();
		private final PopupPanel suggestionPopup;

		private SuggestionItem<T> selectedItem;

		private IsWidget lastTextInput = null;

		private boolean hideWhenEmpty = true;

		SuggestionDisplayImpl() {
			this.suggestionPopup = new PopupPanel(true, false);
			this.suggestionPopup.setPreviewingAllNativeEvents(true);
			StyleUtils.addStyle(this.suggestionPopup, ContentAssistAspect.STYLE_POPUP);
			FlowPanel dropdownContainer = new FlowPanel();
			StyleUtils.addStyle(dropdownContainer, ContentAssistAspect.STYLE_DROPDOWN);
			StyleUtils.addStyle(dropdownContainer, ContentAssistAspect.STYLE_OPEN);
			dropdownContainer.add(this.suggestionsContainer);
			this.suggestionPopup.setWidget(dropdownContainer);
		}

		@Override
		public void hideSuggestions() {
			this.suggestionPopup.hide();
			this.setSuggestionItemSelected(null);
		}

		@Override
		public boolean isSuggestionListShowing() {
			return this.suggestionPopup.isShowing();
		}

		@Override
		public Oracle.Suggestion<T> getSelectedSelection() {
			if (this.selectedItem != null) {
				return this.selectedItem.suggestion;
			}
			return null;
		}

		@Override
		public void moveSelectionDown() {
			if (this.isSuggestionListShowing() && this.selectedItem != null) {
				int currentIndex = this.suggestionsContainer.getWidgetIndex(this.selectedItem);
				if (this.suggestionsContainer.getWidgetCount() > currentIndex + 1) {
					this.setSuggestionItemSelected((SuggestionItem<T>) this.suggestionsContainer.getWidget(currentIndex + 1));
				}
			}
		}

		@Override
		public void moveSelectionUp() {
			if (this.isSuggestionListShowing() && this.selectedItem != null) {
				int currentIndex = this.suggestionsContainer.getWidgetIndex(this.selectedItem);
				if (currentIndex >= 1) {
					this.setSuggestionItemSelected((SuggestionItem<T>) this.suggestionsContainer.getWidget(currentIndex - 1));
				}
			}
		}

		@Override
		public Widget getSuggestionWidget() {
			return this.suggestionPopup;
		}

		private void setSuggestionItemSelected(SuggestionItem<T> newSelection) {
			if (this.selectedItem != null) {
				StyleUtils.removeStyle(this.selectedItem, LinkStyle.ACTIVE);
			}
			this.selectedItem = newSelection;
			if (newSelection != null) {
				StyleUtils.addStyle(this.selectedItem, LinkStyle.ACTIVE);
			}
			this.scrollToSelected();
		}

		private void scrollToSelected() {
			if (this.isSuggestionListShowing() && this.selectedItem != null) {
				this.selectedItem.getElement().scrollIntoView();
			}
		}

		@Override
		public void showSuggestions(Oracle.Request request, final IsWidget textInput,
			Collection<? extends Suggestion<T>> suggestions, final SuggestionCallback<T> callback) {

			boolean anySuggestions = suggestions != null && !suggestions.isEmpty();
			if (!anySuggestions && this.hideWhenEmpty) {
				this.hideSuggestions();
				return;
			}

			if (this.suggestionPopup.isAttached()) {
				this.suggestionPopup.hide();
			}

			this.suggestionsContainer.clear();

			SuggestionItem<T> selected = null;
			for (final Oracle.Suggestion<T> currentSuggestion : suggestions) {
				String display = highlighter.highlight(currentSuggestion.getValue(), request.getQuery());
				final SuggestionItem<T> suggestionItem = new SuggestionItem<T>(currentSuggestion, display);
				if (selected == null) {
					selected = suggestionItem;
				}
				if (this.selectedItem != null && currentSuggestion.equals(this.selectedItem.suggestion)) {
					selected = suggestionItem;
				}

				suggestionItem.addDomHandler(new MouseUpHandler() {
					@Override
					public void onMouseUp(MouseUpEvent event) {
						if (textInput instanceof Focusable) {
							((Focusable) textInput).setFocus(true);
						}
						SuggestionDisplayImpl.this.setSuggestionItemSelected(suggestionItem);
						callback.onSuggestionSelected(suggestionItem.suggestion);
					}
				}, MouseUpEvent.getType());

				this.suggestionsContainer.append(suggestionItem);
			}

			this.setSuggestionItemSelected(selected);

			if (this.lastTextInput != textInput) {
				if (this.lastTextInput != null) {
					this.suggestionPopup.removeAutoHidePartner(this.lastTextInput.asWidget().getElement());
				}
				this.lastTextInput = textInput;
				this.suggestionPopup.addAutoHidePartner(this.lastTextInput.asWidget().getElement());
			}

			this.suggestionPopup.showRelativeTo(this.lastTextInput.asWidget());
			this.scrollToSelected();
		}
	}

	private static final CssStyle STYLE_DROPDOWN = new SimpleStyle("dropdown");
	private static final CssStyle STYLE_SCROLLABLE = new SimpleStyle("scrollable-dropdown");
	private static final CssStyle STYLE_MENU = new SimpleStyle("dropdown-menu");
	private static final CssStyle STYLE_OPEN = new SimpleStyle("open");
	private static final CssStyle STYLE_POPUP = new SimpleStyle("dropdown-popup");

	protected IsWidget textInput;
	protected SuggestionDisplay<T> suggestionDisplay;
	protected ContentAssistHandler<T> assistHandler;

	private Oracle.Highlighter<T> highlighter = null;

	private Oracle.Callback<T> oracleCallback = new Oracle.Callback<T>() {
		@Override
		public void onSuggestionsReady(Oracle.Request request, Oracle.Response<T> response) {
			ContentAssistAspect.this.suggestionDisplay.showSuggestions(
				request,
				ContentAssistAspect.this.textInput,
				response.getSuggestions(),
				ContentAssistAspect.this.suggestionCallback);
		}
	};

	private SuggestionCallback<T> suggestionCallback = new SuggestionCallback<T>() {

		@Override
		public void onSuggestionSelected(Oracle.Suggestion<T> suggestion) {
			ContentAssistAspect.this.setNewSelection(suggestion);
		}
	};

	public ContentAssistAspect() {
		this(new DefaultContentAssistHandler<T>());
	}

	public ContentAssistAspect(ContentAssistHandler<T> assistHandler) {
		this.suggestionDisplay = new SuggestionDisplayImpl();
		this.assistHandler = assistHandler;
	}

	public Oracle.Highlighter<T> getHighlighter() {
		return highlighter;
	}

	public void setHighlighter(Oracle.Highlighter<T> highlighter) {
		this.highlighter = highlighter;
	}

	public void setInput(IsWidget input) {
		this.textInput = input;
		if (this.textInput != null) {
			this.addEventsToTextInput();
		}
	}

	public IsWidget getInput() {
		return this.textInput;
	}

	public Widget getSuggestionWidget() {
		return this.suggestionDisplay.getSuggestionWidget();
	}

	public ContentAssistHandler<T> getContentAssistHandler() {
		return this.assistHandler;
	}

	protected void showSuggestions(String query) {
		if (this.assistHandler.getOracle() != null) {
			this.assistHandler.getOracle().request(
				new Oracle.Request(query, this.assistHandler.getLimit()), this.oracleCallback);
		}
	}

	protected void refreshSuggestions() {
		this.showSuggestions(this.assistHandler.getQueryText(this.textInput));
	}

	protected void setNewSelection(Oracle.Suggestion<T> curSuggestion) {
		this.assistHandler.handleSuggestionSelected(this.textInput, curSuggestion);
		this.suggestionDisplay.hideSuggestions();
	}

	private void addEventsToTextInput() {
		class TextInputEventsHandler implements KeyDownHandler, KeyUpHandler {

			@Override
			public void onKeyDown(KeyDownEvent event) {
				boolean mustKillEvent = false;
				if (ContentAssistAspect.this.suggestionDisplay.isSuggestionListShowing()) {
					switch (event.getNativeKeyCode()) {
						case KeyCodes.KEY_DOWN:
							ContentAssistAspect.this.suggestionDisplay.moveSelectionDown();
							mustKillEvent = true;
							break;
						case KeyCodes.KEY_UP:
							ContentAssistAspect.this.suggestionDisplay.moveSelectionUp();
							mustKillEvent = true;
							break;
						case KeyCodes.KEY_ENTER:
						case KeyCodes.KEY_TAB:
							Oracle.Suggestion<T> suggestion = ContentAssistAspect.this.suggestionDisplay.getSelectedSelection();
							if (suggestion == null) {
								ContentAssistAspect.this.suggestionDisplay.hideSuggestions();
							} else {
								ContentAssistAspect.this.suggestionCallback.onSuggestionSelected(suggestion);
							}
							mustKillEvent = true;
							break;
						case KeyCodes.KEY_ESCAPE:
							ContentAssistAspect.this.suggestionDisplay.hideSuggestions();
							break;
						default:
							break;
					}
				}
				if (mustKillEvent) {
					event.preventDefault();
					event.stopPropagation();
				}
			}

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (ContentAssistAspect.this.suggestionDisplay.isSuggestionListShowing()) {
					switch (event.getNativeKeyCode()) {
						case KeyCodes.KEY_DOWN:
						case KeyCodes.KEY_UP:
						case KeyCodes.KEY_ENTER:
						case KeyCodes.KEY_TAB:
						case KeyCodes.KEY_ESCAPE:
							return;
						default:
							break;
					}
				}
				ContentAssistAspect.this.refreshSuggestions();
			}
		}

		TextInputEventsHandler handler = new TextInputEventsHandler();

		if (this.textInput instanceof HasKeyDownHandlers) {
			((HasKeyDownHandlers) this.textInput).addKeyDownHandler(handler);
		}
		if (this.textInput instanceof HasKeyUpHandlers) {
			((HasKeyUpHandlers) this.textInput).addKeyUpHandler(handler);
		}
	}

}
