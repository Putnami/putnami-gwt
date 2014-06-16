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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.shared.DateTimeFormatInfo;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import fr.putnami.pwt.core.editor.client.EditorInput;
import fr.putnami.pwt.core.editor.client.EditorLeaf;
import fr.putnami.pwt.core.editor.client.Error;
import fr.putnami.pwt.core.editor.client.util.ValidationUtils;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.model.client.base.HasHtmlId;
import fr.putnami.pwt.core.widget.client.base.CssStyle;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.constant.WidgetParams;
import fr.putnami.pwt.core.widget.client.event.AskFocusEvent;
import fr.putnami.pwt.core.widget.client.util.KeyEventUtils;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputDatePicker extends FocusWidget implements
		EditorLeaf,
		EditorInput<Date>,
		HasHtmlId,
		HasDrawable,
		HasValue<Date>,
		HasValueChangeHandlers<Date> {

	private static final CssStyle STYLE_DATEPICKER = new SimpleStyle("datepicker");
	private static final CssStyle STYLE_POPUP = new SimpleStyle("datepicker-popup");
	private static final CssStyle STYLE_HEADER = new SimpleStyle("datepicker-header");
	private static final CssStyle STYLE_MONTH_PICKER_BUTTON = new SimpleStyle("month-picker-button");
	private static final CssStyle STYLE_MONTH_PICKER = new SimpleStyle("month-picker");
	private static final CssStyle STYLE_MONTH_PAGER = new SimpleStyle("month-pager");
	private static final CssStyle STYLE_MONTH_PREVIOUS = new SimpleStyle("month-previous");
	private static final CssStyle STYLE_MONTH_NEXT = new SimpleStyle("month-next");
	private static final CssStyle STYLE_YEAR_BUTTON = new SimpleStyle("year-button");
	private static final CssStyle STYLE_CALENDAR_PICKER = new SimpleStyle("calendar-picker");
	private static final CssStyle STYLE_MUTED = new SimpleStyle("muted");
	private static final CssStyle STYLE_SELECTED = new SimpleStyle("selected");
	private static final CssStyle STYLE_TODAY = new SimpleStyle("today");
	private static final CssStyle STYLE_SHOW = new SimpleStyle("in");

	public enum Mode {
		CALENDAR, MONTH;
	}

	private static final CssStyle STYLE_FADE = new SimpleStyle("fade");

	private static final String ATTRIBUTE_DATA_DATE = "data-date";
	private static final String ATTRIBUTE_DATA_CURSOR = "data-cursor";
	private static final String ATTRIBUTE_DATA_YEAR = "data-year";

	private static final int DAYS_IN_WEEK = 7;

	private static final int YEAR_OFFSET = 1900;

	private static final DateTimeFormatInfo DATE_TIME_FORMAT_INFO = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo();
	private static final String[] DAYS = DATE_TIME_FORMAT_INFO.weekdaysShortStandalone();

	private static final DateTimeFormat MONTH_YEAR_FORMAT = DateTimeFormat.getFormat(WidgetParams.Util.get().monthYearFull());
	private static final DateTimeFormat MONTH_ABBR_FORMAT = DateTimeFormat.getFormat(WidgetParams.Util.get().monthAbbr());

	private static final DateTimeFormat ATTRIBUTE_DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");

	private final DivElement datepickerHeader = Document.get().createDivElement();

	private final DivElement monthPickerButton = Document.get().createDivElement();

	private final UListElement monthPagerUl = Document.get().createULElement();
	private final LIElement pagePreviusMonthLi = Document.get().createLIElement();
	private final LIElement pageTodayLi = Document.get().createLIElement();
	private final LIElement pageNextMonthLi = Document.get().createLIElement();

	private final TableElement calendarTable = Document.get().createTableElement();
	private final TableSectionElement calendatBody = Document.get().createTBodyElement();

	private final DivElement monthPicker = Document.get().createDivElement();
	private final DivElement monthPickerInner = Document.get().createDivElement();
	private final UListElement monthPickerUlMonthElement = Document.get().createULElement();

	private HandlerRegistration popupBlurHandler;

	private Mode mode = Mode.CALENDAR;

	private Date today;
	private Date cursor;
	private Date value;

	private String path;

	private List<Error> errors = Lists.newArrayList();
	private Collection<Validator<Date>> validators;
	private String htmlId;
	private com.google.web.bindery.event.shared.HandlerRegistration askFocusRegistration;

	public InputDatePicker() {
		super(Document.get().createDivElement());

		StyleUtils.addStyle(this, STYLE_DATEPICKER);

		getElement().appendChild(datepickerHeader);
		StyleUtils.addStyle(datepickerHeader, STYLE_HEADER);

		/* month selecter */
		datepickerHeader.appendChild(monthPickerButton);
		StyleUtils.addStyle(monthPickerButton, STYLE_MONTH_PICKER_BUTTON);

		/* pagination */
		datepickerHeader.appendChild(monthPagerUl);
		StyleUtils.addStyle(monthPagerUl, STYLE_MONTH_PAGER);
		createLi(pagePreviusMonthLi, STYLE_MONTH_PREVIOUS, "&#9668;");
		createLi(pageTodayLi, STYLE_TODAY, "&#9673;");
		createLi(pageNextMonthLi, STYLE_MONTH_NEXT, "&#9658;");

		monthPagerUl.appendChild(pagePreviusMonthLi);
		monthPagerUl.appendChild(pageTodayLi);
		monthPagerUl.appendChild(pageNextMonthLi);

		/* Calendar Picker */
		getElement().appendChild(monthPicker);
		monthPicker.appendChild(monthPickerInner);
		StyleUtils.addStyle(monthPicker, STYLE_MONTH_PICKER);

		/* Calendar Picker */
		getElement().appendChild(calendarTable);
		StyleUtils.addStyle(calendarTable, STYLE_CALENDAR_PICKER);

		/* DayPicker Header */
		TableSectionElement head = Document.get().createTHeadElement();
		TableRowElement headRow = Document.get().createTRElement();
		calendarTable.appendChild(head);
		head.appendChild(headRow);
		for (int i = DATE_TIME_FORMAT_INFO.firstDayOfTheWeek(); i < DAYS.length + 1; i++) {
			TableCellElement th = Document.get().createTHElement();
			th.setInnerText(DAYS[i % DAYS.length]);
			headRow.appendChild(th);
		}
		/* DayPicker Body */
		calendarTable.appendChild(calendatBody);

		today = ATTRIBUTE_DATE_FORMAT.parse(ATTRIBUTE_DATE_FORMAT.format(new Date()));
		setValue(today);

		Event.sinkEvents(getElement(), Event.ONKEYDOWN);
		Event.sinkEvents(monthPickerButton, Event.ONCLICK);
		Event.sinkEvents(pagePreviusMonthLi, Event.ONCLICK);
		Event.sinkEvents(pageTodayLi, Event.ONCLICK);
		Event.sinkEvents(pageNextMonthLi, Event.ONCLICK);

		redraw();
	}

	public InputDatePicker(InputDatePicker source) {
		this();
		today = source.today;
		cursor = source.cursor;
		path = source.path;
		validators = source.validators;
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputDatePicker(this);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Date getValue() {
		return value;
	}

	@Override
	public void setValue(Date value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Date value, boolean fireEvents) {
		if (fireEvents) {
			ValueChangeEvent.fireIfNotEqual(this, this.value, value);
			setFocus(true);
		}
		if (value == null) {
			this.value = null;
			this.cursor = new Date(today.getTime());
		}
		else {
			this.value = new Date(value.getTime());
			this.cursor = new Date(value.getTime());
		}
		redraw();
	}

	@Override
	public void onBrowserEvent(Event event) {
		int type = event.getTypeInt();
		Element target = event.getCurrentTarget();
		if (type == Event.ONCLICK) {
			String dataDate = target.getAttribute(ATTRIBUTE_DATA_DATE);
			String dataCursor = target.getAttribute(ATTRIBUTE_DATA_CURSOR);
			String dataYear = target.getAttribute(ATTRIBUTE_DATA_YEAR);
			if (dataDate != null && dataDate.length() > 0) {
				mode = Mode.CALENDAR;
				setValue(ATTRIBUTE_DATE_FORMAT.parse(dataDate), true);
			}
			else if (dataCursor != null && dataCursor.length() > 0) {
				mode = Mode.CALENDAR;
				cursor = ATTRIBUTE_DATE_FORMAT.parse(dataCursor);
				redraw();
			}
			else if (dataYear != null && dataYear.length() > 0) {
				openMonthOfYear(Integer.valueOf(dataYear));
			}
			else if (target == monthPickerButton) {
				if (mode != Mode.MONTH) {
					mode = Mode.MONTH;
				}
				else {
					mode = Mode.CALENDAR;
				}
				redraw();
			}
			event.stopPropagation();
			event.preventDefault();
		}
		else if (type == Event.ONKEYDOWN) {
			handleKeyPress(event.getKeyCode());
			event.stopPropagation();
			event.preventDefault();
		}
		else {
			super.onBrowserEvent(event);
		}
	}

	@Override
	public void redraw() {
		if (mode == Mode.CALENDAR) {
			redrawCalendarPicker();
		}
		else if (mode == Mode.MONTH) {
			redrawMonthPicker();
		}

	}

	private void redrawMonthPicker() {
		monthPicker.getStyle().setWidth(calendarTable.getClientWidth(), Unit.PX);
		calendarTable.getStyle().setDisplay(Display.NONE);
		monthPicker.getStyle().clearDisplay();

		int currentYear = cursor.getYear() + YEAR_OFFSET;
		if (monthPickerInner.getChildCount() == 0) {

			for (int year = currentYear - 100; year < currentYear + 100; year++) {
				DivElement yearDiv = Document.get().createDivElement();
				yearDiv.setInnerText("" + year);
				StyleUtils.addStyle(yearDiv, STYLE_YEAR_BUTTON);
				Event.sinkEvents(yearDiv, Event.ONCLICK);
				monthPickerInner.appendChild(yearDiv);
				yearDiv.setAttribute(ATTRIBUTE_DATA_YEAR, "" + year);
			}
		}
		openMonthOfYear(cursor.getYear() + YEAR_OFFSET);
	}

	private void openMonthOfYear(int year) {
		String yearString = "" + year;
		monthPickerUlMonthElement.removeFromParent();
		for (int i = 0; i < monthPickerInner.getChildCount(); i++) {
			Element child = (Element) monthPickerInner.getChild(i);
			if (yearString.equals(child.getAttribute(ATTRIBUTE_DATA_YEAR))) {
				monthPickerInner.insertAfter(monthPickerUlMonthElement, child);
				Date monthButtonDate = new Date(cursor.getTime());
				monthButtonDate.setYear(year - YEAR_OFFSET);
				if (monthPickerUlMonthElement.getChildCount() == 0) {
					for (int month = 0; month < 12; month++) {
						LIElement monthElement = Document.get().createLIElement();
						monthPickerUlMonthElement.appendChild(monthElement);
						Event.sinkEvents(monthElement, Event.ONCLICK);
						monthButtonDate.setMonth(month);
						monthElement.setInnerText(MONTH_ABBR_FORMAT.format(monthButtonDate));
					}
				}
				for (int month = 0; month < 12; month++) {
					LIElement monthElement = (LIElement) monthPickerUlMonthElement.getChild(month);
					monthButtonDate.setMonth(month);
					monthElement.setAttribute(ATTRIBUTE_DATA_CURSOR, ATTRIBUTE_DATE_FORMAT.format(monthButtonDate));
				}
				monthPicker.setScrollTop(child.getOffsetTop());
				break;
			}

		}
	}

	private void redrawCalendarPicker() {
		monthPicker.getStyle().setDisplay(Display.NONE);
		calendarTable.getStyle().clearDisplay();

		calendatBody.removeAllChildren();

		int firstDayOfWeek = DATE_TIME_FORMAT_INFO.firstDayOfTheWeek();
		int lastDayOfWeek = (firstDayOfWeek + DAYS_IN_WEEK) % DAYS_IN_WEEK;

		/* Display month */
		monthPickerButton.setInnerHTML(MONTH_YEAR_FORMAT.format(cursor) + "<span class=\"caret\"></span>");

		Date lastMonth = new Date(cursor.getTime());
		CalendarUtil.addMonthsToDate(lastMonth, -1);
		pagePreviusMonthLi.setAttribute(ATTRIBUTE_DATA_CURSOR, ATTRIBUTE_DATE_FORMAT.format(lastMonth));
		pageTodayLi.setAttribute(ATTRIBUTE_DATA_CURSOR, ATTRIBUTE_DATE_FORMAT.format(today));
		Date nextMonth = new Date(cursor.getTime());
		CalendarUtil.addMonthsToDate(nextMonth, 1);
		pageNextMonthLi.setAttribute(ATTRIBUTE_DATA_CURSOR, ATTRIBUTE_DATE_FORMAT.format(nextMonth));

		/* Draw daypicker */
		Date dateToDrow = new Date(cursor.getTime());
		int selectedMonth = dateToDrow.getMonth();
		int firstMonthToDisplay = (selectedMonth + 11) % 12;
		int lastMonthToDisplay = (selectedMonth + 13) % 12;
		do {
			CalendarUtil.addDaysToDate(dateToDrow, -1);
		}
		while (firstMonthToDisplay != dateToDrow.getMonth() || dateToDrow.getDay() != firstDayOfWeek);

		// drow calendarTable
		TableRowElement headRow = null;
		while (dateToDrow.getMonth() != lastMonthToDisplay || dateToDrow.getDay() != lastDayOfWeek
				|| (dateToDrow.getDate() == 1 && dateToDrow.getDay() == firstDayOfWeek)) {
			if (headRow == null || dateToDrow.getDay() == firstDayOfWeek) {
				headRow = Document.get().createTRElement();
				calendatBody.appendChild(headRow);
			}
			TableCellElement td = Document.get().createTDElement();
			headRow.appendChild(td);
			DivElement div = Document.get().createDivElement();
			td.appendChild(div);
			div.setInnerText("" + dateToDrow.getDate());
			div.setAttribute(ATTRIBUTE_DATA_DATE, ATTRIBUTE_DATE_FORMAT.format(dateToDrow));
			if (dateToDrow.getMonth() != selectedMonth) {
				StyleUtils.addStyle(td, STYLE_MUTED);
			}
			if (dateToDrow.equals(cursor)) {
				StyleUtils.addStyle(td, STYLE_SELECTED);
			}
			if (today.equals(dateToDrow)) {
				StyleUtils.addStyle(td, STYLE_TODAY);
			}
			Event.sinkEvents(div, Event.ONCLICK);

			CalendarUtil.addDaysToDate(dateToDrow, 1);
		}
	}

	private boolean handleKeyPress(int keyCode) {
		if (KeyEventUtils.isModifierKeyDown(Event.getCurrentEvent())) {
			return false;
		}
		boolean handleKey = false;
		switch (keyCode) {
		case KeyCodes.KEY_LEFT:
			CalendarUtil.addDaysToDate(cursor, -1);
			handleKey = true;
			break;
		case KeyCodes.KEY_RIGHT:
			CalendarUtil.addDaysToDate(cursor, 1);
			handleKey = true;
			break;
		case KeyCodes.KEY_UP:
			CalendarUtil.addDaysToDate(cursor, -7);
			handleKey = true;
			break;
		case KeyCodes.KEY_DOWN:
			CalendarUtil.addDaysToDate(cursor, 7);
			handleKey = true;
			break;
		case KeyCodes.KEY_PAGEUP:
			CalendarUtil.addMonthsToDate(cursor, -1);
			handleKey = true;
			break;
		case KeyCodes.KEY_PAGEDOWN:
			CalendarUtil.addMonthsToDate(cursor, 1);
			handleKey = true;
			break;
		case KeyCodes.KEY_ENTER:
			setValue(cursor, true);
			handleKey = true;
			break;
		case KeyCodes.KEY_ESCAPE:
			setValue(value);
			setFocus(false);
			handleKey = true;
			break;
		}
		if (handleKey) {
			redraw();
		}

		return handleKey;
	}

	private void createLi(LIElement liElement, CssStyle style, String text) {
		StyleUtils.addStyle(liElement, style);
		liElement.setInnerHTML(text);
	}

	public void popup(Widget container, Widget relativeTo) {
		setVisible(true);
		StyleUtils.addStyle(this, STYLE_POPUP);
		RootPanel.get().add(this);

		Element positioningElement = getElement();
		Element relativeElement = relativeTo.getElement();

		int targetHeight = relativeElement.getOffsetHeight();
		int targetTop = relativeElement.getAbsoluteTop();

		int positioningWidth = positioningElement.getOffsetWidth();
		int targetRight = relativeElement.getAbsoluteRight();

		Style elementStyle = positioningElement.getStyle();
		elementStyle.setPosition(Position.ABSOLUTE);
		elementStyle.setLeft(targetRight - positioningWidth, Unit.PX);
		elementStyle.setTop(targetTop + targetHeight, Unit.PX);

		StyleUtils.addStyle(this, STYLE_FADE);
		StyleUtils.addStyle(this, STYLE_SHOW);

		setFocus(true);

		if (popupBlurHandler == null) {
			popupBlurHandler = addBlurHandler(new BlurHandler() {

				@Override
				public void onBlur(BlurEvent event) {
					hide();
				}
			});
		}
	}

	public void hide() {
		StyleUtils.removeStyle(getElement(), STYLE_SHOW);
		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

			@Override
			public boolean execute() {
				setVisible(false);
				RootPanel.get().remove(InputDatePicker.this);
				return false;
			}
		}, 200);
	}

	public void togglePopup(Widget container, Widget relativeTo) {
		if (!isVisible() || !isAttached()) {
			popup(container, relativeTo);
		}
		else {
			hide();
		}
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Date flush() {
		errors.clear();
		this.errors.addAll(ValidationUtils.validate(validators, this, this.value));
		return getValue();
	}

	@Override
	public void edit(Date value) {
		setValue(value);
	}

	@Override
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	@Override
	public Iterable<Error> getErrors() {
		return Iterables.unmodifiableIterable(errors);
	}

	@Override
	public void addValidator(Validator<Date> validator) {
		if (validators == null) {
			validators = Lists.newArrayList();
		}
		validators.add(validator);
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void setHtmlId(String htmlId) {
		this.htmlId = htmlId;
		if (askFocusRegistration != null) {
			askFocusRegistration.removeHandler();
		}
		if (htmlId != null) {
			AskFocusEvent.Handler handler = new AskFocusEvent.Handler() {

				@Override
				public void onAskFocus(AskFocusEvent event) {
					if (Objects.equal(event.getHtmlId(), InputDatePicker.this.htmlId)) {
						setFocus(true);
					}
				}
			};
			askFocusRegistration = EventBus.get().addHandler(AskFocusEvent.TYPE, handler);
		}
	}

	@Override
	public String getHtmlId() {
		return this.htmlId;
	}

}
