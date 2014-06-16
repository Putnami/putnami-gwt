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
package fr.putnami.pwt.core.widget.client.factory;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import fr.putnami.pwt.core.model.client.factory.EditorFactoryManager;
import fr.putnami.pwt.core.widget.client.InputBoolean;
import fr.putnami.pwt.core.widget.client.InputDate;
import fr.putnami.pwt.core.widget.client.InputList;
import fr.putnami.pwt.core.widget.client.InputNumber;
import fr.putnami.pwt.core.widget.client.InputNumber.NumberType;
import fr.putnami.pwt.core.widget.client.InputSelect;
import fr.putnami.pwt.core.widget.client.InputText;
import fr.putnami.pwt.core.widget.client.OutputBoolean;
import fr.putnami.pwt.core.widget.client.OutputDate;
import fr.putnami.pwt.core.widget.client.OutputEnum;
import fr.putnami.pwt.core.widget.client.OutputList;
import fr.putnami.pwt.core.widget.client.OutputNumber;
import fr.putnami.pwt.core.widget.client.OutputText;

public class DefaultEditorFactoryManager extends EditorFactoryManager {

	public DefaultEditorFactoryManager() {
		// Register labelFactory
		setLabelFactory(LabelEditorFactory.INSTANCE);
		// Register tooltipFactory
		setTooltipFactory(TooltipEditorFactory.INSTANCE);

		// Register inputFactories
		{
			registerInputFactory(String.class, new InputText());

			registerInputFactory(Double.class, new InputNumber<Double>(NumberType.DOUBLE));
			registerInputFactory(double.class, new InputNumber<Double>(NumberType.DOUBLE));
			registerInputFactory(Float.class, new InputNumber<Float>(NumberType.FLOAT));
			registerInputFactory(float.class, new InputNumber<Float>(NumberType.FLOAT));
			registerInputFactory(Integer.class, new InputNumber<Integer>(NumberType.INTEGER));
			registerInputFactory(int.class, new InputNumber<Integer>(NumberType.INTEGER));
			registerInputFactory(Long.class, new InputNumber<Long>(NumberType.LONG));
			registerInputFactory(long.class, new InputNumber<Long>(NumberType.LONG));

			registerInputFactory(Date.class, new InputDate());

			registerInputFactory(Boolean.class, new InputBoolean());
			registerInputFactory(boolean.class, new InputBoolean());

			registerInputFactory(Enum.class, new InputSelect());

			registerInputFactory(List.class, new InputList());
			registerInputFactory(Collection.class, new InputList());
		}
		// Register outputFactories
		{
			registerOutputFactory(String.class, new OutputText());

			registerOutputFactory(Double.class, new OutputNumber());
			registerOutputFactory(double.class, new OutputNumber());
			registerOutputFactory(Float.class, new OutputNumber());
			registerOutputFactory(float.class, new OutputNumber());
			registerOutputFactory(Integer.class, new OutputNumber());
			registerOutputFactory(int.class, new OutputNumber());
			registerOutputFactory(Long.class, new OutputNumber());
			registerOutputFactory(long.class, new OutputNumber());

			registerOutputFactory(Date.class, new OutputDate());

			registerOutputFactory(Boolean.class, new OutputBoolean());
			registerOutputFactory(boolean.class, new OutputBoolean());

			registerOutputFactory(Enum.class, new OutputEnum());

			registerOutputFactory(List.class, new OutputList());
			registerOutputFactory(Collection.class, new OutputList());
		}
	}
}
