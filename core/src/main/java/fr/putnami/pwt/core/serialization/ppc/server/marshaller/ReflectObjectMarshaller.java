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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.putnami.pwt.core.serialization.ppc.shared.MarshallerRegistry;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;
import fr.putnami.pwt.core.serialization.ppc.shared.SerializationException;
import fr.putnami.pwt.core.serialization.ppc.shared.marshaller.AbstractMarshaller;
import fr.putnami.pwt.core.serialization.ppc.shared.marshaller.Marshaller;

public class ReflectObjectMarshaller<T> extends AbstractMarshaller<T> {

	private interface PropertyMarshaller {
		String getPropertyName();

		void marshal(Object bean, PpcWriter writer);

		void unmarshal(Object bean, PpcReader reader);
	}

	public class GetterSetterMarshaller implements PropertyMarshaller {
		private final String propertyName;
		private final Method getterMethod;
		private final Method setterMethod;
		private final Class<?> propertyType;

		public GetterSetterMarshaller(String propertyName, Method getterMethod, Method setterMethod) {
			super();
			this.propertyName = propertyName;
			this.getterMethod = getterMethod;
			this.setterMethod = setterMethod;
			this.propertyType = getterMethod.getReturnType();
		}

		@Override
		public String getPropertyName() {
			return propertyName;
		}

		@Override
		public void marshal(Object bean, PpcWriter writer) {
			try {
				if (propertyType.isPrimitive()) {
					if (boolean.class.equals(propertyType)) {
						writer.write((boolean) getterMethod.invoke(bean));
					} else if (byte.class.equals(propertyType)) {
						writer.write((byte) getterMethod.invoke(bean));
					} else if (char.class.equals(propertyType)) {
						writer.write((char) getterMethod.invoke(bean));
					} else if (double.class.equals(propertyType)) {
						writer.write((double) getterMethod.invoke(bean));
					} else if (float.class.equals(propertyType)) {
						writer.write((float) getterMethod.invoke(bean));
					} else if (int.class.equals(propertyType)) {
						writer.write((int) getterMethod.invoke(bean));
					} else if (long.class.equals(propertyType)) {
						writer.write((long) getterMethod.invoke(bean));
					} else if (short.class.equals(propertyType)) {
						writer.write((short) getterMethod.invoke(bean));
					}
				} else {
					Object value = getterMethod.invoke(bean);
					if (value == null) {
						writer.writeNull();
					} else if (Modifier.isFinal(propertyType.getModifiers())) {
						Marshaller<Object> marshaler = (Marshaller<Object>) marshallers.findMarshaller(propertyType);
						marshaler.marshal(value, writer);
					} else {
						writer.write(value);
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new SerializationException("Fail to marshal " + bean, e);
			}
		}

		@Override
		public void unmarshal(Object instance, PpcReader reader) {
			try {
				if (propertyType.isPrimitive()) {
					if (boolean.class.equals(propertyType)) {
						setterMethod.invoke(instance, reader.readBoolean());
					} else if (byte.class.equals(propertyType)) {
						setterMethod.invoke(instance, reader.readByte());
					} else if (char.class.equals(propertyType)) {
						setterMethod.invoke(instance, reader.readChar());
					} else if (double.class.equals(propertyType)) {
						setterMethod.invoke(instance, reader.readDouble());
					} else if (float.class.equals(propertyType)) {
						setterMethod.invoke(instance, reader.readFloat());
					} else if (int.class.equals(propertyType)) {
						setterMethod.invoke(instance, reader.readInt());
					} else if (long.class.equals(propertyType)) {
						setterMethod.invoke(instance, reader.readLong());
					} else if (short.class.equals(propertyType)) {
						setterMethod.invoke(instance, reader.readShort());
					}
				} else if (Modifier.isFinal(propertyType.getModifiers())) {
					Marshaller<Object> marshaler = (Marshaller<Object>) marshallers.findMarshaller(propertyType);
					setterMethod.invoke(instance, marshaler.unmarshal(reader));
				} else {
					setterMethod.invoke(instance, reader.readObject());
				}
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				throw new SerializationException("Fail to unmarshal " + objectClass, e);
			}
		}
	}

	private class PublicFieldMarshaller implements PropertyMarshaller {
		private final Field field;

		public PublicFieldMarshaller(Field field) {
			this.field = field;
		}

		@Override
		public String getPropertyName() {
			return field.getName();
		}

		@Override
		public void marshal(Object bean, PpcWriter writer) {
			try {
				Class<?> type = field.getType();
				if (Modifier.isTransient(field.getModifiers())) {
					return;
				}
				if (type.isPrimitive()) {
					if (boolean.class.equals(type)) {
						writer.write(field.getBoolean(bean));
					} else if (byte.class.equals(type)) {
						writer.write(field.getByte(bean));
					} else if (char.class.equals(type)) {
						writer.write(field.getChar(bean));
					} else if (double.class.equals(type)) {
						writer.write(field.getDouble(bean));
					} else if (float.class.equals(type)) {
						writer.write(field.getFloat(bean));
					} else if (int.class.equals(type)) {
						writer.write(field.getInt(bean));
					} else if (long.class.equals(type)) {
						writer.write(field.getLong(bean));
					} else if (short.class.equals(type)) {
						writer.write(field.getShort(bean));
					}
				} else {
					Object value = field.get(bean);
					if (value == null) {
						writer.writeNull();
					} else if (Modifier.isFinal(type.getModifiers())) {
						Marshaller<Object> marshaler = (Marshaller<Object>) marshallers.findMarshaller(type);
						marshaler.marshal(value, writer);
					} else {
						writer.write(value);
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new SerializationException("Fail to marshal " + bean, e);
			}
		}

		@Override
		public void unmarshal(Object instance, PpcReader reader) {
			try {
				Class<?> type = field.getType();
				if (Modifier.isTransient(field.getModifiers())) {
					return;
				}
				if (type.isPrimitive()) {
					if (boolean.class.equals(type)) {
						field.setBoolean(instance, reader.readBoolean());
					} else if (byte.class.equals(type)) {
						field.setByte(instance, reader.readByte());
					} else if (char.class.equals(type)) {
						field.setChar(instance, reader.readChar());
					} else if (double.class.equals(type)) {
						field.setDouble(instance, reader.readDouble());
					} else if (float.class.equals(type)) {
						field.setFloat(instance, reader.readFloat());
					} else if (int.class.equals(type)) {
						field.setInt(instance, reader.readInt());
					} else if (long.class.equals(type)) {
						field.setLong(instance, reader.readLong());
					} else if (short.class.equals(type)) {
						field.setShort(instance, reader.readShort());
					}
				} else if (Modifier.isFinal(type.getModifiers())) {
					Marshaller<Object> marshaler = (Marshaller<Object>) marshallers.findMarshaller(type);
					Object value = marshaler.unmarshal(reader);
					field.set(instance, value);
				} else {
					field.set(instance, reader.readObject());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new SerializationException("Fail to unmarshal " + objectClass, e);
			}
		}
	}

	private Map<String, PropertyMarshaller> propertyMarshaller = Maps.newHashMap();
	private List<String> propertyNames = Lists.newArrayList();

	private final Class<T> objectClass;

	private final MarshallerRegistry marshallers;

	public ReflectObjectMarshaller(Class<T> objectClass, MarshallerRegistry marshallers) {
		this.objectClass = objectClass;
		this.marshallers = marshallers;

		try {
			for (Field field : objectClass.getFields()) {
				if (Modifier.isPublic(field.getModifiers()) ||
					!Modifier.isTransient(field.getModifiers())) {
					propertyMarshaller.put(field.getName(), new PublicFieldMarshaller(field));
					propertyNames.add(field.getName());
				}
			}

			Map<String, Method> setters = listSetters(objectClass);
			Map<String, Method> getters = listGetters(objectClass);

			for (Map.Entry<String, Method> entry : setters.entrySet()) {
				String propertyName = entry.getKey();
				Method setter = entry.getValue();
				Method getter = getters.get(propertyName);

				if (getter != null && setter != null && !propertyMarshaller.containsKey(propertyName)) {
					propertyMarshaller.put(propertyName, new GetterSetterMarshaller(propertyName, getter, setter));
					propertyNames.add(propertyName);
				}
			}
			Collections.sort(propertyNames);
		} catch (SecurityException e) {
			throw new SerializationException("Fail to initialise marshaller " + objectClass, e);
		}
	}

	@Override
	public T newInstance() {
		try {
			return objectClass.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
			throw new SerializationException("Fail to instanciate " + objectClass, e);
		}
	}

	@Override
	public void marshal(T object, PpcWriter writer) {
		for (String property : propertyNames) {
			propertyMarshaller.get(property).marshal(object, writer);
		}
	}

	@Override
	public T unmarshal(PpcReader reader) {
		T instance = newInstance();
		for (String property : propertyNames) {
			propertyMarshaller.get(property).unmarshal(instance, reader);
		}
		return instance;
	}

	@Override
	public String getTypeName() {
		return objectClass.getName();
	}

	@Override
	public Class<?> getType() {
		return objectClass;
	}

	private static Map<String, Method> listGetters(Class type) {
		Map<String, Method> map = Maps.newHashMap();
		for (Method method : type.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("get") || methodName.startsWith("is")
				&& method.getParameterTypes().length == 0
				&& !method.getReturnType().equals(void.class)) {

				String propertyName = methodName.replaceFirst("is", "");
				propertyName = propertyName.replaceFirst("get", "");
				String firstChar = propertyName.substring(0, 1);
				propertyName = propertyName.replaceFirst(firstChar, firstChar.toLowerCase());

				map.put(propertyName, method);
			}
		}
		return map;
	}

	private static Map<String, Method> listSetters(Class type) {
		Map<String, Method> map = Maps.newHashMap();
		for (Method method : type.getMethods()) {
			String methodName = method.getName();
			Class returnType = method.getReturnType();
			if (methodName.startsWith("set")
				&& method.getParameterTypes().length == 1
				&& Void.class.equals(returnType) || void.class.equals(returnType)) {

				String propertyName = methodName.replaceFirst("set", "");
				String firstChar = propertyName.substring(0, 1);
				propertyName = propertyName.replaceFirst(firstChar, firstChar.toLowerCase());

				map.put(propertyName, method);
			}
		}
		return map;
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

}
