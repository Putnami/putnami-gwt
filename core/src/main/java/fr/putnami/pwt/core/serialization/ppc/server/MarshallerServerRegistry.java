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
package fr.putnami.pwt.core.serialization.ppc.server;

import java.io.Serializable;

import fr.putnami.pwt.core.serialization.ppc.client.EnumMarshaller;
import fr.putnami.pwt.core.serialization.ppc.server.marshaller.ReflectArrayMarshaller;
import fr.putnami.pwt.core.serialization.ppc.server.marshaller.ReflectObjectMarshaller;
import fr.putnami.pwt.core.serialization.ppc.shared.SerializationException;
import fr.putnami.pwt.core.serialization.ppc.shared.base.AbstractMarshallerRegistry;
import fr.putnami.pwt.core.serialization.ppc.shared.marshaller.Marshaller;

public class MarshallerServerRegistry extends AbstractMarshallerRegistry {

	public MarshallerServerRegistry() {
		registerDefault();
	}

	@Override
	public <T> Marshaller<T> findMarshaller(Class<T> clazz) {
		Marshaller<T> marshaller = super.findMarshaller(clazz);
		if (marshaller == null) {
			if (clazz.isEnum()) {
				marshaller = new EnumMarshaller<>(clazz);
			} else if (Serializable.class.isAssignableFrom(clazz)) {
				marshaller = new ReflectObjectMarshaller<T>(clazz, this);
			}
			if (marshaller == null) {
				throw new SerializationException(clazz + " does not implement Serializable.");
			}
			synchronized (registry) {
				register(marshaller);
			}
		}
		return marshaller;
	}

	@Override
	public <T> Marshaller<T> findMarshaller(String className) {
		Marshaller<T> marshaller = null;
		if (className.indexOf('[') == 0) {
			String targetClassName = className.substring(1);
			Marshaller<T> targetMarshaller = findMarshaller(targetClassName);
			marshaller = newArrayMarshaller(targetMarshaller.getType(), targetMarshaller);
		} else {
			marshaller = (Marshaller<T>) registry.get(className);
		}
		if (marshaller == null) {
			try {
				Class clazz = Class.forName(className);
				return findMarshaller(clazz);
			} catch (ClassNotFoundException e) {
				// no op
			}
		}
		if (marshaller == null) {
			throw new SerializationException(className + " doesnt have any marshaller.");
		}
		return marshaller;
	}

	@Override
	protected Marshaller newArrayMarshaller(Class targetClass, Marshaller<?> marchaller) {
		return new ReflectArrayMarshaller(targetClass, marchaller);
	}
}
