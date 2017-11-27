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

import com.google.common.collect.Maps;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.shared.DateTimeFormatInfo;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

import java.util.Date;
import java.util.Map;

import fr.putnami.pwt.core.widget.client.base.AbstractInput;
import fr.putnami.pwt.core.widget.client.base.AbstractInputBox;
import fr.putnami.pwt.core.widget.client.constant.WidgetParams;
import fr.putnami.pwt.core.widget.client.helper.DateParser;
import fr.putnami.pwt.core.widget.client.helper.DateRenderer;
import fr.putnami.pwt.core.widget.client.mask.IntegerTokenHelper;
import fr.putnami.pwt.core.widget.client.mask.MaskValueBoxHelper;
import fr.putnami.pwt.core.widget.client.mask.MaskValueBoxHelper.TokenHelper;
import fr.putnami.pwt.core.widget.client.mask.RestrictedStringTokenHelpler;
import fr.putnami.pwt.core.widget.client.mask.StaticStringTokenHelper;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class InputDateBox extends AbstractInputBox<TextBox, Date> {

	private interface HelperFactory {
		TokenHelper newTokenHelper();
	}

	private static class NumericHelperFactory implements HelperFactory {
		String placeholder;
		String format;
		int defaultValue;
		int min;
		int max;
		int maxLenght;

		NumericHelperFactory(String placeholder, String format, int defaultValue, int min, int max, int maxLenght) {
			this.placeholder = placeholder;
			this.format = format;
			this.defaultValue = defaultValue;
			this.min = min;
			this.max = max;
			this.maxLenght = maxLenght;
		}

		@Override
		public TokenHelper newTokenHelper() {
			IntegerTokenHelper helper = new IntegerTokenHelper();
			helper.setDefaultValue(this.defaultValue);
			helper.setPlaceHolder(this.placeholder);
			helper.setMin(this.min);
			helper.setMax(this.max);
			helper.setMaxLenght(this.maxLenght);
			helper.setFormater(this.format);
			return helper;
		}
	}

	private static class StringHelperFactory implements HelperFactory {
		String[] restrictedValues;

		StringHelperFactory(String placeholder, String... restrictedValues) {
			this.restrictedValues = restrictedValues;
		}

		@Override
		public TokenHelper newTokenHelper() {
			return new RestrictedStringTokenHelpler(this.restrictedValues);
		}
	}

	private static final DateTimeFormatInfo DATE_TIME_FORMAT_INFO = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo();

	private static final Map<String, HelperFactory> TOKEN_HELPER_FACTORIES = Maps.newLinkedHashMap();

	static {
		// year
		InputDateBox.TOKEN_HELPER_FACTORIES.put("yyyy", new NumericHelperFactory("aaaa", "0000",
			new Date().getYear() + 1900, 0, 9999, 4));
		// month full
		InputDateBox.TOKEN_HELPER_FACTORIES.put("MMMM", new StringHelperFactory("mm", InputDateBox.DATE_TIME_FORMAT_INFO
			.monthsFull()));
		// day of week full (monday)
		InputDateBox.TOKEN_HELPER_FACTORIES.put("EEEE", new StringHelperFactory("j", InputDateBox.DATE_TIME_FORMAT_INFO
			.weekdaysFull()));
		// month abbr
		InputDateBox.TOKEN_HELPER_FACTORIES.put("MMM", new StringHelperFactory("mm", InputDateBox.DATE_TIME_FORMAT_INFO
			.monthsShort()));
		// month numeric
		InputDateBox.TOKEN_HELPER_FACTORIES.put("MM", new NumericHelperFactory("mm", "00", 1, 1, 12, 2));
		// day
		InputDateBox.TOKEN_HELPER_FACTORIES.put("dd", new NumericHelperFactory("jj", "00", 1, 1, 31, 2));
		// day of week abbr (mond.)
		InputDateBox.TOKEN_HELPER_FACTORIES.put("E", new StringHelperFactory("j", InputDateBox.DATE_TIME_FORMAT_INFO
			.weekdaysShort()));
		// day of week (monday :> 0)
		InputDateBox.TOKEN_HELPER_FACTORIES.put("c", new NumericHelperFactory("j", "00", 0, 0, 6, 2));

		// hour 1 - 12
		InputDateBox.TOKEN_HELPER_FACTORIES.put("hh", new NumericHelperFactory("hh", "00", 0, 1, 12, 2));
		// hour 0 - 23
		InputDateBox.TOKEN_HELPER_FACTORIES.put("HH", new NumericHelperFactory("hh", "00", 0, 0, 23, 2));
		// minute
		InputDateBox.TOKEN_HELPER_FACTORIES.put("mm", new NumericHelperFactory("mm", "00", 0, 0, 59, 2));
		// second
		InputDateBox.TOKEN_HELPER_FACTORIES.put("ss", new NumericHelperFactory("ss", "00", 0, 0, 59, 2));
		// fractional second
		InputDateBox.TOKEN_HELPER_FACTORIES.put("S", new NumericHelperFactory("S", "000", 0, 0, 999, 3));
		// am/pm marker
		InputDateBox.TOKEN_HELPER_FACTORIES.put("a", new StringHelperFactory("a", "am", "pm"));
	}

	private final WidgetParams params = WidgetParams.Util.get();

	private final MaskValueBoxHelper maskHelper;

	private String format;

	public InputDateBox() {
		super(new TextBox());
		StyleUtils.addStyle(this, AbstractInput.STYLE_CONTROL);

		this.maskHelper = new MaskValueBoxHelper(this.getInput());
		this.setFormat(this.params.inputDateSimpleFormat());
	}

	protected InputDateBox(InputDateBox source) {
		super(new TextBox(), source);
		StyleUtils.addStyle(this, AbstractInput.STYLE_CONTROL);

		this.maskHelper = new MaskValueBoxHelper(this.getInput());
		this.setFormat(source.format);
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputDateBox(this);
	}

	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
		this.setRenderer(new DateRenderer(format));
		this.setParser(new DateParser(format));
		this.maskHelper.reset();
		this.parseFormat(0);
	}

	private void parseFormat(int start) {
		if (start >= this.format.length()) {
			return;
		}
		for (Map.Entry<String, HelperFactory> entry : InputDateBox.TOKEN_HELPER_FACTORIES.entrySet()) {
			String pattern = entry.getKey();
			if (this.format.substring(start).startsWith(pattern)) {
				int nextStart = start + pattern.length();
				Character delimiter = null;
				if (nextStart < this.format.length()) {
					String nextFormat = this.format.substring(nextStart);
					delimiter = nextFormat.length() > 0 ? nextFormat.charAt(0) : null;
					nextStart++;
					for (String nextPattern : InputDateBox.TOKEN_HELPER_FACTORIES.keySet()) {
						if (nextFormat.startsWith(nextPattern)) {
							delimiter = null;
							nextStart--;
							break;
						}
					}
				}
				HelperFactory factory = entry.getValue();
				TokenHelper helper = factory.newTokenHelper();
				this.maskHelper.addTokenHelper(helper);
				if (delimiter != null) {
					this.maskHelper.addTokenHelper(new StaticStringTokenHelper(String.valueOf(delimiter), false));
				}

				this.parseFormat(nextStart);
				return;
			}
		}
	}

}
