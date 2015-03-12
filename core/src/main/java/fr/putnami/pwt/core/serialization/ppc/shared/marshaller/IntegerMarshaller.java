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

import com.google.gwt.thirdparty.guava.common.base.Strings;

import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;

public class IntegerMarshaller extends AbstractMarshaller<Integer> {

	@Override
	public void marshal(Integer value, PpcWriter writer) {
		writer.write(value.intValue());
	}

	@Override
	public Integer unmarshal(PpcReader reader) {
		String token = reader.next();
		if (Strings.isNullOrEmpty(token)) {
			return null;
		}
		return Integer.valueOf(token);
	}

	@Override
	public String getTypeName() {
		return "I";
	}

	@Override
	public Class<Integer> getType() {
		return Integer.class;
	}
}
