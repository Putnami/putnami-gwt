/**
 * This file is part of pwt-code-editor.
 *
 * pwt-code-editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt-code-editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt-code-editor.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.plugin.code.client;

import com.google.common.base.Objects;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.event.DirtyEvent.Handler;
import fr.putnami.pwt.core.model.client.base.HasPlaceholder;
import fr.putnami.pwt.core.widget.client.assist.ContentAssistAspect;
import fr.putnami.pwt.core.widget.client.base.AbstractInput;
import fr.putnami.pwt.core.widget.client.event.ChangeEvent;
import fr.putnami.pwt.core.widget.client.helper.CompositeFocusHelper;
import fr.putnami.pwt.core.widget.client.util.HTMLUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.plugin.code.client.aspect.CodeEditorAspect;
import fr.putnami.pwt.plugin.code.client.base.CodeEditor;
import fr.putnami.pwt.plugin.code.client.base.CodeEditorDriver;
import fr.putnami.pwt.plugin.code.client.base.CodeEditorDriverImpl;
import fr.putnami.pwt.plugin.code.client.configuration.CodeEditorConfiguration;
import fr.putnami.pwt.plugin.code.client.input.CodeInput;
import fr.putnami.pwt.plugin.code.client.input.CodeInputImpl;
import fr.putnami.pwt.plugin.code.client.output.CodeOutput;
import fr.putnami.pwt.plugin.code.client.output.CodeOutputImpl;

public class InputCode extends AbstractInput<String> implements
		CodeEditor,
		HasPlaceholder,
		FocusHandler,
		BlurHandler,
		HasHTML {

	private final FlowPanel content;

	private final CodeInput codeInput = new CodeInputImpl();
	private final CodeOutput codeOutput = new CodeOutputImpl();
	private final CodeEditorDriver codeDriver = new CodeEditorDriverImpl(codeInput, codeOutput);

	private HandlerRegistration valueChangeRegistration;

	private CompositeFocusHelper compositeFocus;

	public InputCode() {
		super(new FlowPanel());
		content = (FlowPanel) getWidget();
		endConstruct();
	}

	protected InputCode(InputCode source) {
		super(new FlowPanel(), source);
		content = (FlowPanel) getWidget();
		endConstruct();
		if (source != null) {
			codeDriver.applyConfiguration(source.codeDriver);
		}
	}

	@Override
	protected void endConstruct() {
		content.add(codeInput);
		content.add(codeOutput);
		// Hook to replace Input by output on blur
		setTabIndex(0);
		codeInput.asWidget().setVisible(false);
		compositeFocus = CompositeFocusHelper.createFocusHelper(codeInput, codeInput);
		addFocusHandler(this);
		compositeFocus.addBlurHandler(this);

		StyleUtils.removeStyle(this, STYLE_CONTROL);
		StyleUtils.addStyle(codeInput.asWidget(), STYLE_CONTROL);

		super.endConstruct();
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputCode(this);
	}

	@Override
	public String getPlaceholder() {
		return codeInput.getPlaceholder();
	}

	@Override
	public void setPlaceholder(String placeholder) {
		codeInput.setPlaceholder(placeholder);
	}

	@Override
	public boolean isDirty() {
		return !Objects.equal(this.getValue(), codeDriver.getValue());
	}

	@Override
	public HandlerRegistration addDirtyHandler(Handler handler) {
		if (valueChangeRegistration == null) {
			// Hook to prevent blur on click on suggestion popup
			valueChangeRegistration = codeInput.addValueChangeHandler(new ChangeEvent<String>(InputCode.this));
		}
		return super.addDirtyHandler(handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return codeInput.addValueChangeHandler(handler);
	}

	@Override
	public String flush() {
		String value = codeDriver.flush();
		validate(value);
		if (!hasErrors()) {
			setValue(value);
		}
		return getValue();
	}

	@Override
	public void edit(String value) {
		setValue(value);
		codeDriver.edit(value);
	}

	@Override
	public void applyConfiguration(CodeEditorConfiguration configuration) {
		codeDriver.applyConfiguration(configuration);
		for (CodeEditorAspect aspect : codeDriver.getAspects()) {
			if (aspect instanceof ContentAssistAspect) {
				compositeFocus.addFocusPartner(((ContentAssistAspect) aspect).getSuggestionWidget().getElement());
			}
		}
	}

	@Override
	public void setConfiguration(CodeEditorConfiguration configuration) {
		codeDriver.setConfiguration(configuration);
	}

	@Override
	public void onFocus(FocusEvent event) {
		codeInput.asWidget().getElement().getStyle().setHeight(codeOutput.asWidget().getElement().getOffsetHeight(), Unit.PX);
		codeInput.asWidget().setVisible(true);
		codeOutput.asWidget().setVisible(false);
		codeInput.setFocus(true);
	}

	@Override
	public void onBlur(BlurEvent event) {
		codeInput.asWidget().setVisible(false);
		codeOutput.asWidget().setVisible(true);

	}

	@Override
	public String getText() {
		return getValue();
	}

	@Override
	public void setText(String text) {
		edit(text);
	}

	@Override
	public String getHTML() {
		return getText();
	}

	/**
	 * To be use only by UIBinder (provide escaped HTML)
	 */
	@Override
	public void setHTML(String html) {
		String htmlToEdit = HTMLUtils.unescapeHTML(html);
		setText(htmlToEdit);
	}
}
