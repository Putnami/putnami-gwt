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

import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.shared.DateTimeFormatInfo;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

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

	private static interface HelperFactory {
		TokenHelper newTokenHelper();

	}

	private static class NumericHelperFactory implements HelperFactory {
		String placeholder;
		String format;
		int defaultValue;
		int min;
		int max;
		int maxLenght;

		public NumericHelperFactory(String placeholder, String format, int defaultValue, int min, int max, int maxLenght) {
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
			helper.setDefaultValue(defaultValue);
			helper.setPlaceHolder(placeholder);
			helper.setMin(min);
			helper.setMax(max);
			helper.setMaxLenght(maxLenght);
			helper.setFormater(format);
			return helper;
		}

	}

	private static class StringHelperFactory implements HelperFactory {
		String[] restrictedValues;

		public StringHelperFactory(String placeholder, String... restrictedValues) {
			this.restrictedValues = restrictedValues;
		}

		@Override
		public TokenHelper newTokenHelper() {
			return new RestrictedStringTokenHelpler(restrictedValues);
		}

	}

	private static final DateTimeFormatInfo DATE_TIME_FORMAT_INFO = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo();

	private static final Map<String, HelperFactory> TOKEN_HELPER_FACTORIES = Maps.newLinkedHashMap();

	static {
		TOKEN_HELPER_FACTORIES.put("yyyy", new NumericHelperFactory("aaaa", "0000", new Date().getYear() + 1900, 0, 9999, 4)); // year
		TOKEN_HELPER_FACTORIES.put("MMMM", new StringHelperFactory("mm", DATE_TIME_FORMAT_INFO.monthsFull())); // month full
		TOKEN_HELPER_FACTORIES.put("EEEE", new StringHelperFactory("j", DATE_TIME_FORMAT_INFO.weekdaysFull())); // day of week full (monday)
		TOKEN_HELPER_FACTORIES.put("MMM", new StringHelperFactory("mm", DATE_TIME_FORMAT_INFO.monthsShort())); // month abbr
		TOKEN_HELPER_FACTORIES.put("MM", new NumericHelperFactory("mm", "00", 1, 1, 12, 2)); // month numeric
		TOKEN_HELPER_FACTORIES.put("dd", new NumericHelperFactory("jj", "00", 1, 1, 31, 2)); // day
		TOKEN_HELPER_FACTORIES.put("E", new StringHelperFactory("j", DATE_TIME_FORMAT_INFO.weekdaysShort())); // day of week abbr (mond.)
		TOKEN_HELPER_FACTORIES.put("c", new NumericHelperFactory("j", "00", 0, 0, 6, 2)); // day of week (monday :> 0)

		TOKEN_HELPER_FACTORIES.put("hh", new NumericHelperFactory("hh", "00", 0, 1, 12, 2)); // hour 1 - 12
		TOKEN_HELPER_FACTORIES.put("HH", new NumericHelperFactory("hh", "00", 0, 0, 23, 2)); // hour 0 - 23
		TOKEN_HELPER_FACTORIES.put("mm", new NumericHelperFactory("mm", "00", 0, 0, 59, 2)); // minute
		TOKEN_HELPER_FACTORIES.put("ss", new NumericHelperFactory("ss", "00", 0, 0, 59, 2)); // second
		TOKEN_HELPER_FACTORIES.put("S", new NumericHelperFactory("S", "000", 0, 0, 999, 3)); // fractional second
		TOKEN_HELPER_FACTORIES.put("a", new StringHelperFactory("a", "am", "pm")); // am/pm marker

	}

	private final MaskValueBoxHelper maskHelper;

	private String format;

	public InputDateBox() {
		super(new TextBox());
		StyleUtils.addStyle(this, STYLE_CONTROL);

		this.maskHelper = new MaskValueBoxHelper(getInput());
		setFormat(WidgetParams.Util.get().dateFormatSimple());
	}

	protected InputDateBox(InputDateBox source) {
		super(new TextBox(), source);
		StyleUtils.addStyle(this, STYLE_CONTROL);

		this.maskHelper = new MaskValueBoxHelper(getInput());
		setFormat(source.format);
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputDateBox(this);
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
		setRenderer(new DateRenderer(format));
		setParser(new DateParser(format));
		maskHelper.reset();
		parseFormat(0);
	}

	private void parseFormat(int start) {
		if (start >= format.length()) {
			return;
		}
		for (Map.Entry<String, HelperFactory> entry : TOKEN_HELPER_FACTORIES.entrySet()) {
			String pattern = entry.getKey();
			if (format.substring(start).startsWith(pattern)) {
				int nextStart = start + pattern.length();
				Character delimiter = null;
				if (nextStart < format.length()) {
					String nextFormat = format.substring(nextStart);
					delimiter = nextFormat.length() > 0 ? nextFormat.charAt(0) : null;
					nextStart++;
					for (String nextPattern : TOKEN_HELPER_FACTORIES.keySet()) {
						if (nextFormat.startsWith(nextPattern)) {
							delimiter = null;
							nextStart--;
							break;
						}
					}
				}
				HelperFactory factory = entry.getValue();
				TokenHelper helper = factory.newTokenHelper();
				maskHelper.addTokenHelper(helper);
				if (delimiter != null) {
					maskHelper.addTokenHelper(new StaticStringTokenHelper("" + delimiter, false));
				}

				parseFormat(nextStart);
				return;
			}
		}
	}

}
