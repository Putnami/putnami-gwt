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

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.Date;

import fr.putnami.pwt.core.editor.client.event.DirtyEvent;
import fr.putnami.pwt.core.editor.client.event.DirtyEvent.Handler;
import fr.putnami.pwt.core.model.client.base.HasPlaceholder;
import fr.putnami.pwt.core.theme.client.IconFont;
import fr.putnami.pwt.core.widget.client.Button.Type;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent;
import fr.putnami.pwt.core.widget.client.helper.CompositeFocusHelper;

public class InputDate extends InputGroup<Date>
	implements DirtyEvent.HasDirtyHandlers, HasAllFocusHandlers, Focusable, HasPlaceholder {

	private final InputDateBox dateBox = new InputDateBox();
	private final Button<Date> calendarButton = new Button<Date>();
	private final Button<Date> resetButton = new Button<Date>();

	private InputDatePicker datePicker;

	private CompositeFocusHelper compositeFocusHelper;

	private Date value;

	public InputDate() {
		this.endConstruct();
	}

	protected InputDate(InputDate source) {
		super(source, false);
		this.endConstruct();
		this.setFormat(source.getFormat());
		this.setPlaceholder(source.getPlaceholder());
	}

	protected void endConstruct() {
		this.calendarButton.setType(Type.ICON);
		this.calendarButton.setIconType(IconFont.ICON_CALENDAR);
		this.calendarButton.addButtonHandler(new ButtonEvent.Handler() {
			@Override
			public void onButtonAction(ButtonEvent event) {
				InputDate.this.toggleDatePicker();
			}

		});
		this.resetButton.setType(Type.ICON);
		this.resetButton.setIconType(IconFont.ICON_CANCEL);
		this.resetButton.addButtonHandler(new ButtonEvent.Handler() {
			@Override
			public void onButtonAction(ButtonEvent event) {
				InputDate.this.edit(InputDate.this.getValue());
			}
		});

		this.append(this.dateBox);
		this.addAddon(this.resetButton);
		this.addAddon(this.calendarButton);
		this.compositeFocusHelper = CompositeFocusHelper.createFocusHelper(this, this.dateBox);
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputDate(this);
	}

	public String getFormat() {
		return this.dateBox.getFormat();
	}

	public void setFormat(String format) {
		this.dateBox.setFormat(format);
	}

	@Override
	public boolean isDirty() {
		return this.dateBox.isDirty();
	}

	@Override
	public Date flush() {
		this.value = this.dateBox.flush();
		return this.value;
	}

	@Override
	public void edit(Date value) {
		edit(value, false);
	}

	public void edit(Date value, boolean fireEvents) {
		this.value = value;
		this.dateBox.edit(value, fireEvents);
		if (this.datePicker != null) {
			this.datePicker.hide();
		}
	}

	@Override
	public HandlerRegistration addDirtyHandler(Handler handler) {
		return this.dateBox.addDirtyHandler(handler);
	}

	@Override
	public String getPlaceholder() {
		return this.dateBox.getPlaceholder();
	}

	@Override
	public void setPlaceholder(String placeholder) {
		this.dateBox.setPlaceholder(placeholder);
	}

	@Override
	public Date getValue() {
		return this.value;
	}

	private void toggleDatePicker() {
		if (this.datePicker == null) {
			this.datePicker = new InputDatePicker();
			this.datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {

				@Override
				public void onValueChange(ValueChangeEvent<Date> event) {
					InputDate.this.edit(event.getValue(), true);
					InputDate.this.dateBox.setFocus(true);
				}
			});
			this.compositeFocusHelper.addFocusPartner(this.datePicker.getElement());
		}
		this.datePicker.togglePopup(this, this.calendarButton);
		try {
			this.datePicker.setValue(this.flush());
		} catch (IllegalArgumentException e) {
			this.datePicker.setValue(new Date());
		}
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addFocusHandler(FocusHandler handler) {
		return this.compositeFocusHelper.addFocusHandler(handler);
	}

	@Override
	public com.google.gwt.event.shared.HandlerRegistration addBlurHandler(BlurHandler handler) {
		return this.compositeFocusHelper.addBlurHandler(handler);
	}

	@Override
	public int getTabIndex() {
		return this.dateBox.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		this.dateBox.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		this.dateBox.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		this.dateBox.setTabIndex(index);
	}

}
