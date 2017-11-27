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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.editor.client.EditorOutput;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractWidget;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class OutputProgressBar<T extends Number> extends AbstractWidget implements EditorOutput<T> {

	private static final String ROLE_PROGRESSBAR = "progressbar";

	private static final String ATT_ARIA_VALUE = "aria-valuenow";
	private static final String ATT_ARIA_MIN = "aria-valuemin";
	private static final String ATT_ARIA_MAX = "aria-valuemax";

	private static final CssStyle STYLE_PROGRESS = new SimpleStyle("progress");
	private static final CssStyle STYLE_PROGRESSBAR = new SimpleStyle("progress-bar");
	private static final CssStyle STYLE_STRIPED = new SimpleStyle("progress-striped");
	private static final CssStyle STYLE_ANIMATED = new SimpleStyle("active");

	public enum Color implements CssStyle {
			DEFAULT(null),
			SUCCESS("progress-bar-success"),
			INFO("progress-bar-info"),
			WARNING("progress-bar-warning"),
			DANGER("progress-bar-danger");

		private final String style;

		Color(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	final DivElement progressBarElement = Document.get().createDivElement();

	private String format = "{1}%";
	private boolean displayValue = false;

	private Color color;
	private boolean striped = false;
	private boolean animated = false;

	private int min = 0;
	private int max = 100;

	private T value;

	public OutputProgressBar() {
		super(DivElement.TAG);
		StyleUtils.addStyle(this, OutputProgressBar.STYLE_PROGRESS);
		this.endConstruct();
	}

	protected OutputProgressBar(OutputProgressBar<T> source) {
		super(source);

		this.max = source.max;
		this.min = source.min;
		this.displayValue = source.displayValue;
		this.format = source.format;
		this.setColor(source.color);
		this.setStriped(source.striped);
		this.setAnimated(source.animated);

		this.endConstruct();
	}

	private void endConstruct() {
		this.getElement().appendChild(this.progressBarElement);
		this.progressBarElement.setAttribute("role", OutputProgressBar.ROLE_PROGRESSBAR);
		StyleUtils.addStyle(this.progressBarElement, OutputProgressBar.STYLE_PROGRESSBAR);
	}

	@Override
	public IsWidget cloneWidget() {
		return new OutputProgressBar<T>(this);
	}

	@Override
	public T getValue() {
		return this.value;
	}

	@Override
	public void edit(T value) {
		this.setValue(value);
	}

	@Override
	public LeafValueEditor<T> asEditor() {
		return new TakesValueEditorWrapper<T>(this);
	}

	public void setValue(T value) {
		this.value = value;
		double val = 0;
		if (value == null) {
			val = this.min;
		} else {
			val = value.doubleValue();
		}
		if (val > this.max) {
			val = this.max;
		} else if (val < this.min) {
			val = this.min;
		}

		this.progressBarElement.setAttribute(OutputProgressBar.ATT_ARIA_VALUE, value + "");
		double percent = 100 * (val - this.min) / (this.max - this.min);
		this.progressBarElement.getStyle().setProperty("width", percent, Unit.PCT);

		NumberFormat formatter = NumberFormat.getFormat("#.##");

		String stringToDisplay = this.format;
		stringToDisplay = RegExp.compile("\\{0\\}").replace(stringToDisplay, formatter.format(val));
		stringToDisplay = RegExp.compile("\\{1\\}").replace(stringToDisplay, formatter.format(percent));
		stringToDisplay = RegExp.compile("\\{2\\}").replace(stringToDisplay, formatter.format(this.min));
		stringToDisplay = RegExp.compile("\\{3\\}").replace(stringToDisplay, formatter.format(this.max));

		this.progressBarElement.removeAllChildren();
		if (this.displayValue) {
			this.progressBarElement.setInnerText(stringToDisplay);
		} else {
			SpanElement reader = Document.get().createSpanElement();
			reader.setInnerText(stringToDisplay);
			reader.addClassName("sr-only");
			this.progressBarElement.appendChild(reader);
		}
	}

	public int getMin() {
		return this.min;
	}

	public void setMin(int min) {
		this.min = min;
		this.progressBarElement.setAttribute(OutputProgressBar.ATT_ARIA_MIN, min + "");
	}

	public int getMax() {
		return this.max;
	}

	public void setMax(int max) {
		this.max = max;
		this.progressBarElement.setAttribute(OutputProgressBar.ATT_ARIA_MAX, max + "");
	}

	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean isDisplayValue() {
		return this.displayValue;
	}

	public void setDisplayValue(boolean displayValue) {
		this.displayValue = displayValue;
	}

	public Color getColor() {
		return this.color;
	}

	public void setColor(Color color) {
		this.color = color;
		StyleUtils.addStyle(this.progressBarElement, color);
	}

	public boolean isStriped() {
		return this.striped;
	}

	public void setStriped(boolean striped) {
		this.striped = striped;
		StyleUtils.toggleStyle(this, OutputProgressBar.STYLE_STRIPED, striped);
	}

	public boolean isAnimated() {
		return this.animated;
	}

	public void setAnimated(boolean animated) {
		this.animated = animated;
		if (animated) {
			this.setStriped(true);
		}
		StyleUtils.toggleStyle(this, OutputProgressBar.STYLE_ANIMATED, animated);
	}

}
