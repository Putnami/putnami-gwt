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
package fr.putnami.pwt.core.serialization.ppc.server.marshaller;

import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;
import fr.putnami.pwt.core.serialization.ppc.shared.SerializationException;
import fr.putnami.pwt.core.serialization.ppc.shared.marshaller.AbstractMarshaller;

public class ReflectEnumMarshaller extends AbstractMarshaller<Enum> {

	private ClassLoader classLoader;

	public ReflectEnumMarshaller(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void marshal(Enum value, PpcWriter writer) {
		writer.write(value.getClass().getName());
		writer.write(value.toString());
	}

	@Override
	public Enum unmarshal(PpcReader reader) {
		String enumType = reader.readString();
		if (enumType == null) {
			return null;
		}
		String enumVal = reader.readString();
		if (enumVal == null) {
			return null;
		}

		try {
			Class enumClass = classLoader.loadClass(enumType);
			for (Object constant : enumClass.getEnumConstants()) {
				if (enumVal.equals(constant.toString())) {
					return (Enum) constant;
				}
			}
		} catch (ClassNotFoundException e) {
			throw new SerializationException("Fail to load " + enumType, e);
		}
		return null;
	}

	@Override
	public String getTypeName() {
		return "E";
	}

	@Override
	public Class<?> getType() {
		return Enum.class;
	}
}
