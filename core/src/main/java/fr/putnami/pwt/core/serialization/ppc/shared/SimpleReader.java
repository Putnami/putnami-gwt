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
package fr.putnami.pwt.core.serialization.ppc.shared;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.List;

import fr.putnami.pwt.core.serialization.ppc.shared.marshaller.Marshaller;
import fr.putnami.pwt.core.serialization.ppc.shared.util.PpcUtils;

public class SimpleReader implements PpcReader {

	private List<String> strings;
	private int indexStringsDelimiter;
	private int cursor;

	private BiMap<Object, Integer> cache = HashBiMap.create();

	private final MarshallerRegistry marshallers;

	public SimpleReader(MarshallerRegistry marshallers) {
		this.marshallers = marshallers;
	}

	@Override
	public PpcReader prepare(String payload) {
		strings = Splitter.on("|").splitToList(payload);
		indexStringsDelimiter = strings.indexOf("--");
		cursor = 0;
		return this;
	}

	@Override
	public boolean readBoolean() {
		String bool = next();
		return "1".equals(bool);
	}

	@Override
	public byte readByte() {
		return Byte.valueOf(next());
	}

	@Override
	public char readChar() {
		return next().charAt(0);
	}

	@Override
	public float readFloat() {
		return Float.parseFloat(next());
	}

	@Override
	public double readDouble() {
		return Double.parseDouble(next());
	}

	@Override
	public int readInt() {
		return Integer.parseInt(next());
	}

	@Override
	public long readLong() {
		return Long.parseLong(next());
	}

	@Override
	public short readShort() {
		return Short.parseShort(next());
	}

	@Override
	public String readString() {
		String token = next();
		if (Strings.isNullOrEmpty(token)) {
			return null;
		}
		int index = Integer.parseInt(token);
		return getString(index);
	}

	@Override
	public <O> O readObject() {
		String token = next();
		if (Strings.isNullOrEmpty(token)) {
			return null;
		}
		int index = Integer.parseInt(token);

		String objectRef = getString(index);
		Object val = null;

		String className = PpcUtils.extractClassFromRef(objectRef);
		Integer instanceId = PpcUtils.extractInstanceIdFromRef(objectRef);

		if (instanceId != null) {
			val = cache.inverse().get(instanceId);
		}

		if (val != null) {
			return (O) val;
		}
		Marshaller<O> marshaller = marshallers.findMarshaller(className);
		O value = marshaller.unmarshal(this);
		if (instanceId != null) {
			cache.put(value, instanceId);
		}
		return value;
	}

	@Override
	public String next() {
		return strings.get(cursor++);
	}

	private String getString(int stringIndex) {
		int index = stringIndex;
		index += indexStringsDelimiter + 1;
		String value = strings.get(index);
		value = PpcUtils.decodeString(value);
		return value;
	}

}
