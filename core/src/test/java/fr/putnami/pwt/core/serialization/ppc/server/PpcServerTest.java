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

import com.ibm.icu.text.SimpleDateFormat;

import java.text.ParseException;
import java.util.Date;

import fr.putnami.pwt.core.serialization.ppc.shared.AbstractPpcTest;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcSerializer;

public class PpcServerTest extends AbstractPpcTest {

	private static PpcSerializer serializer;

	@Override
	public boolean isPureJava() {
		return true;
	}

	@Override
	protected PpcSerializer getSerializer() {
		if (serializer == null) {
			serializer = new PpcServerSerializer();
		}
		return serializer;
	}

	@Override
	protected Date parseDate(String text) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		try {
			return df.parse(text);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
