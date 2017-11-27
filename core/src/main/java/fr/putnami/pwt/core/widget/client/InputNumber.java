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

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.constants.NumberConstants;
import com.google.gwt.text.client.DoubleParser;
import com.google.gwt.text.client.DoubleRenderer;
import com.google.gwt.text.client.IntegerParser;
import com.google.gwt.text.client.IntegerRenderer;
import com.google.gwt.text.client.LongParser;
import com.google.gwt.text.client.LongRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

import fr.putnami.pwt.core.editor.client.validator.MinValidator;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.widget.client.base.AbstractInputBox;
import fr.putnami.pwt.core.widget.client.helper.BigDecimalParser;
import fr.putnami.pwt.core.widget.client.helper.BigDecimalRenderer;
import fr.putnami.pwt.core.widget.client.helper.BigIntegerParser;
import fr.putnami.pwt.core.widget.client.helper.BigIntegerRenderer;
import fr.putnami.pwt.core.widget.client.helper.FloatParser;
import fr.putnami.pwt.core.widget.client.helper.FloatRenderer;
import fr.putnami.pwt.core.widget.client.mask.IntegerTokenHelper;
import fr.putnami.pwt.core.widget.client.mask.MaskValueBoxHelper;
import fr.putnami.pwt.core.widget.client.mask.StaticStringTokenHelper;

public class InputNumber<N extends Number> extends AbstractInputBox<TextBox, N> {

	public enum NumberType {
			FLOAT(FloatRenderer.get(), FloatParser.get()),
			DOUBLE(DoubleRenderer.instance(), DoubleParser.instance()),
			BIG_DECIMAL(BigDecimalRenderer.get(), BigDecimalParser.get()),
			INTEGER(IntegerRenderer.instance(), IntegerParser.instance()),
			LONG(LongRenderer.instance(), LongParser.instance()),
			BIG_INTEGER(BigIntegerRenderer.get(), BigIntegerParser.get());

		private final Renderer<? extends Number> renderer;
		private final Parser<? extends Number> parser;

		NumberType(Renderer<? extends Number> renderer, Parser<? extends Number> parser) {
			this.renderer = renderer;
			this.parser = parser;
		}
	}

	protected static final NumberConstants NUMBER_CONSTANTS = LocaleInfo.getCurrentLocale().getNumberConstants();

	protected final MaskValueBoxHelper maskHelper;

	private NumberType type;
	private boolean signed = true;

	@UiConstructor
	public InputNumber(NumberType type) {
		super(new TextBox());
		this.type = type;
		this.maskHelper = new MaskValueBoxHelper(this.getInput());
		this.reset();
	}

	protected InputNumber(InputNumber<N> source) {
		super(new TextBox(), source);
		this.type = source.type;
		this.signed = source.signed;
		this.maskHelper = new MaskValueBoxHelper(this.getInput());
		this.reset();
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputNumber<N>(this);
	}

	public NumberType getNumberType() {
		return this.type;
	}

	public void setNumberType(NumberType numberType) {
		this.type = numberType;
		this.reset();
	}

	public boolean isSigned() {
		return this.signed;
	}

	public void setSigned(boolean signed) {
		this.signed = signed;
		this.reset();
	}

	@Override
	public void addValidator(Validator<N> validator) {
		super.addValidator(validator);
		if (this.signed && validator instanceof MinValidator) {
			MinValidator minValidator = (MinValidator) validator;
			if (minValidator.getMin() >= 0) {
				this.signed = false;
				this.reset();
			}
		}
	}

	protected void reset() {
		if (this.type == null) {
			this.type = NumberType.LONG;
		}

		this.setRenderer((Renderer<N>) this.type.renderer);
		this.setParser((Parser<N>) this.type.parser);

		this.maskHelper.reset();
		if (this.signed) {
			this.maskHelper.addTokenHelper(new StaticStringTokenHelper(InputNumber.NUMBER_CONSTANTS.minusSign(), true));
		}
		this.maskHelper.addTokenHelper(new IntegerTokenHelper(0, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, "0"));

		switch (this.type) {
			case FLOAT:
			case DOUBLE:
			case BIG_DECIMAL:
				this.maskHelper.addTokenHelper(new StaticStringTokenHelper(InputNumber.NUMBER_CONSTANTS.decimalSeparator(),
					true, ',', '.'));
				this.maskHelper.addTokenHelper(new IntegerTokenHelper(0, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, "0"));
				break;
			default:
				break;
		}
	}

}
