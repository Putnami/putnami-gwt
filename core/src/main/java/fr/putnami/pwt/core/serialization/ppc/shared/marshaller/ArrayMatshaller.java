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
package fr.putnami.pwt.core.serialization.ppc.shared.marshaller;

import com.google.common.collect.Lists;

import java.lang.reflect.Array;
import java.util.List;

import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;
import fr.putnami.pwt.core.serialization.ppc.shared.util.PpcUtils;

public class ArrayMatshaller extends AbstractMarshaller<Object> {

	private Marshaller<Object> targetMarshaler;
	private Class<?> targetClass;

	public ArrayMatshaller(Class arrayClass, Marshaller marshaller) {
		this.targetMarshaler = marshaller;
		this.targetClass = arrayClass;
	}

	@Override
	public void marshal(Object value, PpcWriter writer) {
		List collect = toList(value);
		writer.write(collect.size());
		for (Object o : collect) {
			writer.write(o);
		}
	}

	@Override
	public Object unmarshal(PpcReader reader) {
		int size = reader.readInt();
		List collect = Lists.newArrayList();
		for (int i = 0; i < size; i++) {
			Object value = reader.readObject();
			collect.add(value);
		}
		return toArray(collect);
	}

	private Object toArray(List collect) {
		if (!targetClass.isPrimitive()) {
			Object[] arr = (Object[]) Array.newInstance(targetClass, collect.size());
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = collect.get(i);
			}
			return arr;
		} else if (boolean.class.equals(targetClass)) {
			boolean[] arr = new boolean[collect.size()];
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = (boolean) collect.get(i);
			}
			return arr;
		} else if (byte.class.equals(targetClass)) {
			byte[] arr = new byte[collect.size()];
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = (byte) collect.get(i);
			}
			return arr;
		} else if (char.class.equals(targetClass)) {
			char[] arr = new char[collect.size()];
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = (char) collect.get(i);
			}
			return arr;
		} else if (double.class.equals(targetClass)) {
			double[] arr = new double[collect.size()];
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = (double) collect.get(i);
			}
			return arr;
		} else if (float.class.equals(targetClass)) {
			float[] arr = new float[collect.size()];
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = (float) collect.get(i);
			}
			return arr;
		} else if (int.class.equals(targetClass)) {
			int[] arr = new int[collect.size()];
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = (int) collect.get(i);
			}
			return arr;
		} else if (long.class.equals(targetClass)) {
			long[] arr = new long[collect.size()];
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = (long) collect.get(i);
			}
			return arr;
		} else if (short.class.equals(targetClass)) {
			short[] arr = new short[collect.size()];
			for (int i = 0; i < collect.size(); i++) {
				arr[i] = (short) collect.get(i);
			}
			return arr;
		}
		return null;
	}

	private List toList(Object value) {
		List list = Lists.newArrayList();
		if (boolean.class.equals(targetClass)) {
			boolean[] arr = (boolean[]) value;
			for (boolean v : arr) {
				list.add(v);
			}
		} else if (byte.class.equals(targetClass)) {
			byte[] arr = (byte[]) value;
			for (byte v : arr) {
				list.add(v);
			}
		} else if (char.class.equals(targetClass)) {
			char[] arr = (char[]) value;
			for (char v : arr) {
				list.add(v);
			}
		} else if (double.class.equals(targetClass)) {
			double[] arr = (double[]) value;
			for (double v : arr) {
				list.add(v);
			}
		} else if (float.class.equals(targetClass)) {
			float[] arr = (float[]) value;
			for (float v : arr) {
				list.add(v);
			}
		} else if (int.class.equals(targetClass)) {
			int[] arr = (int[]) value;
			for (int v : arr) {
				list.add(v);
			}
		} else if (long.class.equals(targetClass)) {
			long[] arr = (long[]) value;
			for (long v : arr) {
				list.add(v);
			}
		} else if (short.class.equals(targetClass)) {
			short[] arr = (short[]) value;
			for (short v : arr) {
				list.add(v);
			}
		} else {
			Object[] arr = (Object[]) value;
			for (Object v : arr) {
				list.add(v);
			}
		}
		return list;
	}

	@Override
	public boolean writeType(PpcWriter writer, Integer id) {
		writer.write(getTypeName() + PpcUtils.SEPARATOR_TYPE_REF + id);
		return true;
	}

	@Override
	public String getTypeName() {
		return "[" + targetMarshaler.getTypeName();
	}

	@Override
	public Class<?> getType() {
		return targetClass;
	}
}
