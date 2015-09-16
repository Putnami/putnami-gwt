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

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.model.PropertyDescription;
import fr.putnami.pwt.core.serialization.ppc.shared.MarshallerRegistry;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;
import fr.putnami.pwt.core.serialization.ppc.shared.marshaller.AbstractMarshaller;
import fr.putnami.pwt.core.serialization.ppc.shared.marshaller.Marshaller;

public class ModelMarshaller<T> extends AbstractMarshaller<T> {

	private final Model<T> model;
	private final List<String> properties = Lists.newArrayList();
	private final MarshallerRegistry registry;

	public ModelMarshaller(Model<T> model, MarshallerRegistry registry) {
		this.registry = registry;
		this.model = model;
		for (String propertyName : model.getPropertyNames()) {
			PropertyDescription propDescription = model.getProperty(propertyName);

			boolean toAdd = !propDescription.isTransient()
				&& propDescription.isPublic()
				|| (propDescription.isHasGetter() && propDescription.isHasSetter());

			if (toAdd) {
				properties.add(propertyName);
			}
		}
		Collections.sort(properties);
	}

	@Override
	public void marshal(T bean, PpcWriter writer) {
		for (String property : properties) {
			PropertyDescription propDescription = model.getProperty(property);
			Object value = model.get(bean, property);
			Class propertyClass = propDescription.getClazz();
			if (boolean.class.equals(propertyClass)) {
				writer.write(Boolean.TRUE.equals(value));
			} else if (byte.class.equals(propertyClass)) {
				writer.write((byte) (value == null ? 0 : value));
			} else if (char.class.equals(propertyClass)) {
				writer.write((char) (value == null ? 0 : value));
			} else if (double.class.equals(propertyClass)) {
				writer.write((double) (value == null ?  0 :  value));
			} else if (float.class.equals(propertyClass)) {
				writer.write((float) (value == null ? 0 : value));
			} else if (int.class.equals(propertyClass)) {
				writer.write((int) (value == null ? 0 : value));
			} else if (long.class.equals(propertyClass)) {
				writer.write((long) (value == null ? 0 : value));
			} else if (short.class.equals(propertyClass)) {
				writer.write((short) (value == null ? 0 : value));
			} else if (value == null) {
				writer.writeNull();
			} else if (propDescription.isFinal()) {
				Marshaller<Object> marshaler = registry.findMarshaller(propertyClass);
				if (marshaler != null) {
					marshaler.marshal(value, writer);
				} else {
					writer.write(value);
				}
			} else {
				writer.write(value);
			}
		}
	}

	@Override
	public T unmarshal(PpcReader reader) {
		T instance = newInstance();
		for (String property : properties) {
			PropertyDescription propertyDescription = model.getProperty(property);
			if (propertyDescription.isHasSetter()) {
				Class propertyClass = propertyDescription.getClazz();
				if (boolean.class.equals(propertyClass)) {
					model.set(instance, property, reader.readBoolean());
				} else if (byte.class.equals(propertyClass)) {
					model.set(instance, property, reader.readByte());
				} else if (char.class.equals(propertyClass)) {
					model.set(instance, property, reader.readChar());
				} else if (double.class.equals(propertyClass)) {
					model.set(instance, property, reader.readDouble());
				} else if (float.class.equals(propertyClass)) {
					model.set(instance, property, reader.readFloat());
				} else if (int.class.equals(propertyClass)) {
					model.set(instance, property, reader.readInt());
				} else if (long.class.equals(propertyClass)) {
					model.set(instance, property, reader.readLong());
				} else if (short.class.equals(propertyClass)) {
					model.set(instance, property, reader.readShort());
				} else if (propertyDescription.isFinal()) {
					Marshaller<Object> marshaler = registry.findMarshaller(propertyClass);
					if (marshaler != null) {
						model.set(instance, property, marshaler.unmarshal(reader));
					} else {
						model.set(instance, property, reader.readObject());
					}
				} else {
					model.set(instance, property, reader.readObject());
				}
			}
		}
		return instance;
	}

	@Override
	public String getTypeName() {
		return model.getBeanType().getName();
	}

	@Override
	public Class<?> getType() {
		return model.getBeanType();
	}

	@Override
	public T newInstance() {
		return model.newInstance();
	}

	@Override
	public Integer writeInstanceId(PpcWriter writer, Integer instanceId) {
		writer.write((int) instanceId);
		return instanceId;
	}

	@Override
	public Integer readInstanceId(PpcReader reader) {
		return reader.readInt();
	}

	public static void register(MarshallerRegistry registry, Model model) {
		boolean registered = registry.register(new ModelMarshaller(model, registry));
		if (registered) {
			Model parentModel = model.getParentModel();
			if (parentModel != null) {
				register(registry, parentModel);
			}
			Model leafModel = model.getParentModel();
			if (leafModel != null) {
				register(registry, leafModel);
			}
			List<String> properties = Lists.newArrayList(model.getPropertyNames());
			for (String propertyName : properties) {
				PropertyDescription property = model.getProperty(propertyName);
				Model portertyModel = property.getModel();
				if (portertyModel != null) {
					register(registry, portertyModel);
				}
				if (property.getClazz().isEnum()) {
					registry.register(new EnumMarshaller(property.getClazz()));
				}
			}
		}
	}

}
