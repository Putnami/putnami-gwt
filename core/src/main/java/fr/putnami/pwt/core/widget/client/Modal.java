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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractPanel;
import fr.putnami.pwt.core.widget.client.base.HasFooter;
import fr.putnami.pwt.core.widget.client.base.HasHeader;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.WidgetUtils;

public class Modal extends AbstractPanel implements HasOneWidget, CloneableWidget, HasDrawable {

	private static final CssStyle STYLE_MODAL = new SimpleStyle("modal");
	private static final CssStyle STYLE_MODAL_OPEN = new SimpleStyle("modal-open");
	private static final CssStyle STYLE_DIALOG = new SimpleStyle("modal-dialog");
	private static final CssStyle STYLE_CONTENT = new SimpleStyle("modal-content");
	private static final CssStyle STYLE_HEADER = new SimpleStyle("modal-header");
	private static final CssStyle STYLE_BODY = new SimpleStyle("modal-body");
	private static final CssStyle STYLE_FOOTER = new SimpleStyle("modal-footer");
	private static final CssStyle STYLE_BACKDROP = new SimpleStyle("modal-backdrop");

	private static final CssStyle STYLE_FADE = new SimpleStyle("fade");
	private static final CssStyle STYLE_VISIBLE = new SimpleStyle("in");
	private static final CssStyle STYLE_CLOSE = new SimpleStyle("close");

	private static class ModalBackdrop extends Widget {

		ModalBackdrop() {
			this.setElement(Document.get().createDivElement());
			StyleUtils.addStyle(this, Modal.STYLE_BACKDROP);
			StyleUtils.addStyle(this, Modal.STYLE_FADE);
		}

		public void show() {
			RootPanel.get().add(this);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					StyleUtils.addStyle(ModalBackdrop.this, Modal.STYLE_VISIBLE);
				}
			});
		}

		public void hide() {
			StyleUtils.removeStyle(ModalBackdrop.this, Modal.STYLE_VISIBLE);
			Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

				@Override
				public boolean execute() {
					RootPanel.get().remove(ModalBackdrop.this);
					return false;
				}
			}, 100);
		}
	}

	public enum Size implements CssStyle {
			SMALL("modal-sm"),
			DEFAULT(null),
			LARGE("modal-lg");

		private final String style;

		Size(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private static final ModalBackdrop MODAL_BACKDROP = new ModalBackdrop();

	private final Container headerContainer = new Container(DivElement.TAG);
	private final Container dialogContainer = new Container(DivElement.TAG);
	private final Container contentContainer = new Container(DivElement.TAG);
	private final Container bodyContainer = new Container(DivElement.TAG);

	private Widget containerWidget;

	private Anchor<?> dismissButton;
	private Header header;
	private Widget widget;
	private Footer footer;

	private boolean visible = false;
	private boolean dismissable;
	private String title;

	private Size size = Size.DEFAULT;

	public Modal() {
		super(DivElement.TAG);

		StyleUtils.addStyle(this, Modal.STYLE_MODAL);
		StyleUtils.addStyle(this, Modal.STYLE_FADE);

		this.append(this.dialogContainer);
		this.dialogContainer.append(this.contentContainer);

		StyleUtils.addStyle(this.dialogContainer, Modal.STYLE_DIALOG);
		StyleUtils.addStyle(this.contentContainer, Modal.STYLE_CONTENT);
		StyleUtils.addStyle(this.bodyContainer, Modal.STYLE_BODY);
	}

	protected Modal(Modal source) {
		super(source);

		this.visible = source.visible;
		this.dismissable = source.dismissable;
		this.title = source.title;

		this.setHeader(WidgetUtils.cloneWidget(this.header));
		this.setFooter(WidgetUtils.cloneWidget(this.footer));
		this.setWidget(WidgetUtils.cloneWidget(this.widget));
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public IsWidget cloneWidget() {
		return new Modal(this);
	}

	@Override
	public void add(IsWidget w) {
		if (w instanceof Header) {
			this.setHeader((Header) w);
			return;
		}
		if (w instanceof Footer) {
			this.setFooter((Footer) w);
			return;
		}
		if (w instanceof HasHeader) {
			this.setHeader(((HasHeader) w).getHeader());
		}
		if (w instanceof HasFooter) {
			this.setFooter(((HasFooter) w).getFooter());
		}
		this.setWidget(w);
	}

	public void setHeader(Header header) {
		if (header == null) {
			return;
		}
		assert this.header == null : "header may only be set once";
		this.header = header;
	}

	public void setFooter(Footer footer) {
		if (footer == null) {
			return;
		}
		assert this.footer == null : "footer may only be set once";
		this.footer = footer;
		StyleUtils.addStyle(footer, Modal.STYLE_FOOTER);
	}

	public void show() {
		this.ensureDismissButton();
		this.redraw();
		this.visible = true;

		Widget modal = getContainerWidget();

		if (modal.isAttached()) {
			modal.removeFromParent();
		}

		Modal.MODAL_BACKDROP.show();
		this.getElement().getStyle().setDisplay(Display.BLOCK);
		RootPanel rootPanel = RootPanel.get();
		rootPanel.add(modal);
		StyleUtils.addStyle(rootPanel, Modal.STYLE_MODAL_OPEN);

		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
			@Override
			public boolean execute() {
				StyleUtils.addStyle(Modal.this, Modal.STYLE_VISIBLE);
				return false;
			}
		}, 150);
	}

	public void hide() {
		final RootPanel rootPanel = RootPanel.get();

		this.visible = false;
		StyleUtils.removeStyle(Modal.this, Modal.STYLE_VISIBLE);
		StyleUtils.removeStyle(rootPanel, Modal.STYLE_MODAL_OPEN);

		Modal.MODAL_BACKDROP.hide();

		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
			@Override
			public boolean execute() {
				Modal.this.getElement().getStyle().clearDisplay();
				rootPanel.remove(getContainerWidget());
				return false;
			}
		}, 150);
	}

	public void toggleVisibility() {
		if (this.visible) {
			this.hide();
		} else {
			this.show();
		}
	}

	@Override
	public Widget getWidget() {
		return this.widget;
	}

	@Override
	public void setWidget(IsWidget w) {
		this.setWidget(w.asWidget());
	}

	@Override
	public void setWidget(Widget w) {
		this.widget = w;
		this.bodyContainer.append(this.widget);
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
		super.setTitle(title);
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	public void setDismissable(boolean dismissable) {
		this.dismissable = dismissable;
	}

	public Size getSize() {
		return this.size;
	}

	public void setSize(Size size) {
		this.size = size;
		StyleUtils.addStyle(this.contentContainer, this.size);
	}

	@Override
	public void redraw() {
		this.headerContainer.clear();

		if (this.dismissable) {
			this.headerContainer.append(this.ensureDismissButton());
		}
		if (this.header != null) {
			this.headerContainer.append(this.header);
		} else if (this.title != null) {
			Heading titleHeading = new Heading(4);
			titleHeading.setText(this.title);
			this.headerContainer.append(titleHeading);
		}

		if (this.headerContainer.getWidgetCount() > 0) {
			StyleUtils.addStyle(this.headerContainer, Modal.STYLE_HEADER);
		}

		this.contentContainer.clear();
		this.contentContainer.append(this.headerContainer);
		this.contentContainer.append(this.bodyContainer);
		this.contentContainer.append(this.footer);
	}

	private Widget getContainerWidget() {
		if (containerWidget == null) {
			if (getParent() instanceof HasWidgets) {
				containerWidget = this;
			} else if (getParent() instanceof Composite) {
				containerWidget = getParent();
			}
		}
		return containerWidget;
	}

	private Anchor<?> ensureDismissButton() {
		if (this.dismissable && this.dismissButton == null) {
			this.dismissButton = new Anchor<>("&times;");
			StyleUtils.addStyle(this.dismissButton, Modal.STYLE_CLOSE);
			this.dismissButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					Modal.this.hide();
				}

			});
		}
		return this.dismissButton;
	}
}
