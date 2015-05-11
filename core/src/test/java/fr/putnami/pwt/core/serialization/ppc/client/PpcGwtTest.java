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
package fr.putnami.pwt.core.serialization.ppc.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.serialization.ppc.shared.AbstractPpcTest;
import fr.putnami.pwt.core.serialization.ppc.shared.BeanPublicFields;
import fr.putnami.pwt.core.serialization.ppc.shared.BeanSetters;
import fr.putnami.pwt.core.serialization.ppc.shared.Gender;
import fr.putnami.pwt.core.serialization.ppc.shared.Manager;
import fr.putnami.pwt.core.serialization.ppc.shared.MarshallerRegistry;
import fr.putnami.pwt.core.serialization.ppc.shared.Person;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcSerializer;

public class PpcGwtTest extends AbstractPpcTest {

	public interface ManagerModel extends Model<Manager> {
	}
	public interface PersonModel extends Model<Person> {
	}
	public interface BeanPublicFieldsModel extends Model<BeanPublicFields> {
	}
	public interface BeanSettersModel extends Model<BeanSetters> {
	}

	private PpcSerializer serializer;

	@Override
	protected Date parseDate(String text) {
		DateTimeFormat df = DateTimeFormat.getFormat("yyyyMMdd");
		return df.parse(text);
	}

	@Override
	protected PpcSerializer getSerializer() {
		if (serializer == null) {
			serializer = new PpcClientSerializer();

			Model<Person> personModel = GWT.create(PersonModel.class);
			Model<Manager> managerModel = GWT.create(ManagerModel.class);
			Model<BeanPublicFields> beanPublicFieldsModel = GWT.create(BeanPublicFieldsModel.class);
			Model<BeanSetters> beanSettersModel = GWT.create(BeanSettersModel.class);

			MarshallerRegistry registry = serializer.getMarshallerRegistry();
			registry.register(new ModelMarshaller<Person>(personModel, registry));
			registry.register(new ModelMarshaller<Manager>(managerModel, registry));
			registry.register(new ModelMarshaller<BeanPublicFields>(beanPublicFieldsModel, registry));
			registry.register(new ModelMarshaller<BeanSetters>(beanSettersModel, registry));
			registry.register(new EnumMarshaller<Gender>(Gender.class));
		}
		return serializer;
	}
}
