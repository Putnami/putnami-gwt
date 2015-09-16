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

import java.util.Collection;

import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;

public abstract class AbstractCollectionMatshaller<C extends Collection> extends AbstractMarshaller<C> {

	@Override
	public void marshal(C value, PpcWriter writer) {
		writer.write(value.size());
		for (Object o : value) {
			writer.write(o);
		}
	}

	@Override
	public C unmarshal(PpcReader reader) {
		int size = reader.readInt();
		C collect = newInstance();
		for (int i = 0; i < size; i++) {
			Object value = reader.readObject();
			collect.add(value);
		}
		return collect;
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
