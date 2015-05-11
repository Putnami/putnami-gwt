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

import com.google.common.base.Strings;

import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;

public class DoubleMarshaller extends AbstractMarshaller<Double> {

	@Override
	public void marshal(Double value, PpcWriter writer) {
		writer.write(value.doubleValue());
	}

	@Override
	public Double unmarshal(PpcReader reader) {
		String token = reader.next();
		if (Strings.isNullOrEmpty(token)) {
			return null;
		}
		return Double.valueOf(token);
	}

	@Override
	public String getTypeName() {
		return "D";
	}

	@Override
	public Class<?> getType() {
		return Double.class;
	}
}
