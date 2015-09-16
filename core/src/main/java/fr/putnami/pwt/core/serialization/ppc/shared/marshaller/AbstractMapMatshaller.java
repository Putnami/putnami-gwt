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

import java.util.Map;
import java.util.Map.Entry;

import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;

public abstract class AbstractMapMatshaller<K, V, M extends Map<K, V>> extends AbstractMarshaller<M> {

	@Override
	public void marshal(M value, PpcWriter writer) {
		writer.write(value.size());
		for (Entry<K, V> entry : value.entrySet()) {
			writer.write(entry.getKey());
			writer.write(entry.getValue());
		}
	}

	@Override
	public M unmarshal(PpcReader reader) {
		int size = reader.readInt();
		M map = newInstance();
		for (int i = 0; i < size; i++) {
			K key = reader.readObject();
			V value = reader.readObject();
			map.put(key, value);
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
