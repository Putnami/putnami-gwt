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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import java.util.List;

import fr.putnami.pwt.core.serialization.ppc.shared.marshaller.Marshaller;
import fr.putnami.pwt.core.serialization.ppc.shared.util.PpcUtils;

public class SimpleWriter implements PpcWriter {

	private boolean closed = false;
	private boolean hasContent = false;
	private final StringBuffer sb = new StringBuffer();
	private final List<String> strings = Lists.newArrayList();

	private final BiMap<Object, Integer> cache = HashBiMap.create();

	private final MarshallerRegistry marshallers;

	public SimpleWriter(MarshallerRegistry marshallers) {
		this.marshallers = marshallers;
	}

	@Override
	public PpcWriter write(boolean value) {
		append(value ? 1 : 0);
		return this;
	}

	@Override
	public PpcWriter write(byte value) {
		append(value);
		return this;
	}

	@Override
	public PpcWriter write(char value) {
		append(value);
		return this;
	}

	@Override
	public PpcWriter write(float value) {
		append(value);
		return this;
	}

	@Override
	public PpcWriter write(double value) {
		append(value);
		return this;
	}

	@Override
	public PpcWriter write(int value) {
		append(value);
		return this;
	}

	@Override
	public PpcWriter write(long value) {
		append(value);
		return this;
	}

	@Override
	public PpcWriter write(short value) {
		append(value);
		return this;
	}

	@Override
	public PpcWriter write(String value) {
		if (value == null) {
			append(null);
		} else {
			long index = strings.indexOf(value);
			if (index == -1) {
				strings.add(value);
				index = strings.indexOf(value);
			}
			write(index);
		}
		return this;
	}

	@Override
	public <O> PpcWriter write(O value) {
		if (value == null) {
			append(null);
			return this;
		}

		Class<O> valueClass = (Class<O>) value.getClass();

		Marshaller<O> marshaller = marshallers.findMarshaller(valueClass);

		if (marshaller == null) {
			throw new SerializationException("Can not resolve marsahller for " + valueClass);
		}

		Integer index = cache.get(value);

		if (index == null) {
			index = cache.size();
			if (marshaller.writeType(this, index)) {
				cache.put(value, index);
			}
			marshaller.marshal(value, this);
		} else {
			marshaller.writeType(this, index);
		}
		return this;
	}

	@Override
	public PpcWriter writeNull() {
		return write(null);
	}

	@Override
	public String flush() {
		if (!strings.isEmpty()) {
			append(PpcUtils.SEPARATOR_STRINGS);
			for (String string : strings) {
				append(string);
			}
		}
		closed = true;
		return sb.toString();
	}

	private void append(Object o) {
		if (closed) {
			throw new RuntimeException("Can not write on closed serializer.");
		}
		if (hasContent) {
			sb.append(PpcUtils.SEPARATOR);
		}
		if (o instanceof String) {
			sb.append(PpcUtils.encodeString((String) o));
		} else if (o != null) {
			sb.append("" + o);
		}
		hasContent = true;
	}
}
