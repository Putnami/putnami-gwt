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
package fr.putnami.pwt.core.serialization.ppc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ibm.icu.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import fr.putnami.pwt.core.serialization.domain.BeanPublicFields;
import fr.putnami.pwt.core.serialization.domain.BeanSetters;
import fr.putnami.pwt.core.serialization.domain.Gender;
import fr.putnami.pwt.core.serialization.domain.Manager;
import fr.putnami.pwt.core.serialization.domain.Person;
import fr.putnami.pwt.core.serialization.ppc.server.PpcServerSerializer;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcSerializer;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;

public class PpcServerSerialisationTest {

	private static PpcSerializer serializer;

	@BeforeClass
	public static void beforeClass() {
		serializer = new PpcServerSerializer();
	}

	private PpcWriter createWriter() {
		return serializer.newWriter();
	}

	private PpcReader createReader(String payload) {
		PpcReader reader = serializer.newReader();
		reader.prepare(payload);
		return reader;
	}

	@Test
	public void testBoolean() {
		// write
		assertEquals("1", createWriter().write(true).flush());
		assertEquals("0", createWriter().write(false).flush());
		// read
		assertEquals(true, createReader("1").readBoolean());
		assertEquals(false, createReader("0").readBoolean());

		// Boolean object
		assertEquals("0|1|--|B", createWriter().write(Boolean.TRUE).flush());
		assertEquals(Boolean.FALSE, createReader("0|0|--|B").readObject());
	}

	@Test
	public void testByte() {
		// write
		assertEquals("127", createWriter().write(Byte.MAX_VALUE).flush());
		// read
		assertEquals(Byte.MAX_VALUE, createReader("127").readByte());

		// Byte object
		assertEquals("0|127|--|O", createWriter().write(new Byte(Byte.MAX_VALUE)).flush());
		assertEquals(new Byte(Byte.MIN_VALUE), createReader("0|-128|--|O").readObject());
	}

	@Test
	public void testChar() {
		// write
		assertEquals("a", createWriter().write('a').flush());
		assertEquals("a|b", createWriter().write('a').write('b').flush());
		// read
		assertEquals('a', createReader("a").readChar());
		PpcReader reader = createReader("a|b");
		assertEquals('a', reader.readChar());
		assertEquals('b', reader.readChar());

		// Byte object
		assertEquals("0|D|--|C", createWriter().write(new Character('D')).flush());
		assertEquals(new Character('C'), createReader("0|C|--|C").readObject());

	}

	@Test
	public void testFloat() {
		// write
		assertEquals("0.12", createWriter().write(0.12f).flush());
		assertEquals("0.12|225.13", createWriter().write(0.12f).write(225.13f).flush());
		// read
		assertEquals(0.1f, createReader("0.1").readFloat(), 0.01);
		PpcReader reader = createReader("0.155|0.144");
		assertEquals(0.155f, reader.readFloat(), 0.01);
		assertEquals(0.144f, reader.readFloat(), 0.01);

		// Float object
		assertEquals("0|0.25|--|F", createWriter().write(new Float(0.25)).flush());
		assertEquals(new Float(0.35), createReader("0|0.35|--|F").readObject());
	}

	@Test
	public void testInt() {
		// write
		assertEquals("12", createWriter().write(12).flush());
		assertEquals("12|13", createWriter().write(12).write(13).flush());
		// read
		assertEquals(1, createReader("1").readInt());
		PpcReader reader = createReader("1|2|3");
		assertEquals(1, reader.readInt());
		assertEquals(2, reader.readInt());

		// Float object
		assertEquals("0|25|--|I", createWriter().write(new Integer(25)).flush());
		assertEquals(new Integer(35), createReader("0|35|--|I").readObject());
	}

	@Test
	public void testLong() {
		// write
		assertEquals("12", createWriter().write(12L).flush());
		assertEquals("12|13", createWriter().write(12L).write(13L).flush());
		// read
		assertEquals(1, createReader("1").readLong());
		PpcReader reader = createReader("1|2|3");
		assertEquals(1L, reader.readLong());
		assertEquals(2L, reader.readLong());

		// Long object
		assertEquals("0|25|--|L", createWriter().write(new Long(25)).flush());
		assertEquals(new Long(35), createReader("0|35|--|L").readObject());
	}

	@Test
	public void testShort() {
		// write
		assertEquals("12", createWriter().write((short) 12).flush());
		assertEquals("12|13", createWriter().write((short) 12).write((short) 13).flush());
		// read
		assertEquals(1, createReader("1").readInt());
		PpcReader reader = createReader("1|2|3");
		assertEquals((short) 1, reader.readShort());
		assertEquals((short) 2, reader.readShort());
		assertEquals((short) 3, reader.readShort());

		// Short object
		assertEquals("0|-32768|--|SH", createWriter().write(new Short(Short.MIN_VALUE)).flush());
		assertEquals(new Short(Short.MIN_VALUE), createReader("0|-32768|--|SH").readObject());
	}

	@Test
	public void testNull() {
		// write
		assertEquals("", createWriter().write(null).flush());
		assertEquals("|", createWriter().write(null).write(null).flush());
		// read
		assertNull(createReader("").readObject());
	}

	@Test
	public void testString() {
		// write
		assertEquals("0|--|abc", createWriter().write("abc").flush());
		assertEquals("0|1|--|abc|def", createWriter().write("abc").write("def").flush());
		assertEquals("0|0|--|abc", createWriter().write("abc").write("abc").flush());
		// read
		assertEquals("foo", createReader("0|--|foo").readString());
		PpcReader reader = createReader("0|1|0|--|foo|bar");
		assertEquals("foo", reader.readString());
		assertEquals("bar", reader.readString());
		assertEquals("foo", reader.readString());
	}

	@Test
	public void testDate() throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		// write
		assertEquals("0|1426028400000|--|DT", createWriter().write(df.parse("20150311")).flush());
		// read
		assertEquals(df.parse("20150311"), createReader("0|1426028400000|--|DT").<Date> readObject());
	}

	@Test
	public void testBigDecimal() {
		// write
		assertEquals("0|1|--|BD|12.58", createWriter().write(new BigDecimal("12.58")).flush());
		// read
		assertEquals(new BigDecimal("012.58"), createReader("0|1|--|BD|12.58").<Date> readObject());
	}

	@Test
	public void testBigInteger() {
		// write
		assertEquals("0|1|--|BI|12", createWriter().write(new BigInteger("12")).flush());
		// read
		assertEquals(new BigInteger("12"), createReader("0|1|--|BI|12").<Date> readObject());
	}

	@Test
	public void testDouble() {
		// write
		assertEquals("0.12", createWriter().write(0.12d).flush());
		assertEquals("0.12|225.13", createWriter().write(0.12d).write(225.13d).flush());
		// read
		assertEquals(0.1d, createReader("0.1").readDouble(), 0.01);
		PpcReader reader = createReader("0.155|0.144");
		assertEquals(0.155d, reader.readFloat(), 0.01);
		assertEquals(0.144d, reader.readFloat(), 0.01);

		// Float object
		assertEquals("0|0.25|--|D", createWriter().write(new Double(0.25)).flush());
		assertEquals(new Double(0.35), createReader("0|0.35|--|D").readObject());
	}

	@Test
	public void testArrayList() {
		List<String> l = Lists.newArrayList("a", "b", "c");
		String serial = createWriter().write(l).flush();
		assertEquals("0|3|1|2|1|3|1|4|--|AL|S|a|b|c", serial);
		assertEquals(l, createReader(serial).readObject());
		assertEquals(ArrayList.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testLinkedList() {
		List<String> l = Lists.newLinkedList();
		l.add("a");
		l.add("b");
		l.add("a");

		String serial = createWriter().write(l).flush();
		assertEquals("0|3|1|2|1|3|1|2|--|LL|S|a|b", serial);
		assertEquals(l, createReader(serial).readObject());
		assertEquals(LinkedList.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testHashSet() {
		Set<String> l = Sets.newHashSet();
		l.add("a");
		l.add("b");
		l.add("a");

		String serial = createWriter().write(l).flush();
		assertEquals("0|2|1|2|1|3|--|HS|S|a|b", serial);
		assertEquals(l, createReader(serial).readObject());
		assertEquals(HashSet.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testLinkedHashSet() {
		Set<String> l = Sets.newLinkedHashSet();
		l.add("a");
		l.add("b");
		l.add("a");

		String serial = createWriter().write(l).flush();
		assertEquals("0|2|1|2|1|3|--|LHS|S|a|b", serial);
		assertEquals(l, createReader(serial).readObject());
		assertEquals(LinkedHashSet.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testTreeSet() {
		Set<String> l = Sets.newTreeSet();
		l.add("a");
		l.add("b");
		l.add("a");

		String serial = createWriter().write(l).flush();
		assertEquals("0|2|1|2|1|3|--|TS|S|a|b", serial);
		assertEquals(l, createReader(serial).readObject());
		assertEquals(TreeSet.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testVector() {
		Vector<String> l = new Vector<String>();
		l.add("a");
		l.add("b");
		l.add("a");

		String serial = createWriter().write(l).flush();
		assertEquals("0|3|1|2|1|3|1|2|--|VT|S|a|b", serial);
		assertEquals(l, createReader(serial).readObject());
		assertEquals(Vector.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testHashMap() {
		Map<Long, String> m = Maps.newHashMap();
		m.put(2L, "a");
		m.put(3L, "b");
		m.put(4L, "a");

		String serial = createWriter().write(m).flush();
		assertEquals("0|3|1|2|2|3|1|3|2|4|1|4|2|3|--|HM|L|S|a|b", serial);
		assertEquals(m, createReader(serial).readObject());
		assertEquals(HashMap.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testIdentityHashMap() {
		Map<Long, String> m = Maps.newIdentityHashMap();
		m.put(2L, "a");
		m.put(3L, "b");
		m.put(4L, "a");

		String serial = createWriter().write(m).flush();
		// FIXME assertEquals("0|3|1|3|2|3|1|4|2|4|1|2|2|4|--|IHM|L|S|b|a", serial);
		// FIXME assertEquals(m, createReader(serial).readObject());
		assertEquals(IdentityHashMap.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testLinkedHashMap() {
		Map<Long, String> m = Maps.newLinkedHashMap();
		m.put(2L, "a");
		m.put(3L, "b");
		m.put(4L, "a");

		String serial = createWriter().write(m).flush();
		assertEquals("0|3|1|2|2|3|1|3|2|4|1|4|2|3|--|LHM|L|S|a|b", serial);
		assertEquals(m, createReader(serial).readObject());
		assertEquals(LinkedHashMap.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testTreeMap() {
		Map<Long, String> m = Maps.newTreeMap();
		m.put(2L, "a");
		m.put(3L, "b");
		m.put(4L, "a");

		String serial = createWriter().write(m).flush();
		assertEquals("0|3|1|2|2|3|1|3|2|4|1|4|2|3|--|TM|L|S|a|b", serial);
		assertEquals(m, createReader(serial).readObject());
		assertEquals(TreeMap.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testEnum() {
		// write
		assertEquals("0|1|2|--|E|com.google.gwt.i18n.server.testing.Gender|MALE", createWriter().write(Gender.MALE).flush());
		// read
		assertEquals(Gender.MALE, createReader("0|1|2|--|E|com.google.gwt.i18n.server.testing.Gender|MALE")
			.<Gender> readObject());
	}

	@Test
	public void testVoid() {
		assertEquals(null, createReader("0|--|V").readObject());
	}

	@Test
	public void testCustomBeanPublicFields() throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

		BeanPublicFields bean = new BeanPublicFields();
		String serial = createWriter().write(bean).flush();
		BeanPublicFields read = createReader(serial).readObject();
		assertEquals(bean, read);

		bean = new BeanPublicFields();
		bean.booleanVal = true;
		bean.byteVal = Byte.MAX_VALUE;
		bean.charVal = 'A';
		bean.doubleVal = 12.4;
		bean.floatVal = 4.1f;
		bean.intVal = 12;
		bean.longVal = 62L;
		bean.shortVal = 223;
		bean.stringObject = "a";
		bean.dateObject = df.parse("20150311");
		bean.booleanObject = true;
		bean.byteObject = Byte.MAX_VALUE;
		bean.charObject = 'A';
		bean.doubleObject = 12.4;
		bean.floatObject = 4.1f;
		bean.intObject = 12;
		bean.longObject = 62L;
		bean.shortObject = 223;

		serial = createWriter().write(bean).flush();
		read = createReader(serial).readObject();
		assertEquals(bean, read);
	}

	@Test
	public void testCustomBeanSetters() throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

		BeanSetters bean = new BeanSetters();
		String serial = createWriter().write(bean).flush();
		BeanSetters read = createReader(serial).readObject();
		assertEquals(bean, read);

		bean = new BeanSetters();
		bean.setBooleanVal(true);
		bean.setByteVal(Byte.MAX_VALUE);
		bean.setCharVal('A');
		bean.setDoubleVal(12.4);
		bean.setFloatVal(4.1f);
		bean.setIntVal(12);
		bean.setLongVal(62L);
		bean.setShortVal((short) 223);
		bean.setStringObject("a");
		bean.setDateObject(df.parse("20150311"));
		bean.setBooleanObject(true);
		bean.setByteObject(Byte.MAX_VALUE);
		bean.setCharObject('A');
		bean.setDoubleObject(12.4);
		bean.setFloatObject(4.1f);
		bean.setIntObject(12);
		bean.setLongObject(62L);
		bean.setShortObject((short) 223);

		serial = createWriter().write(bean).flush();
		read = createReader(serial).readObject();
		assertEquals(bean, read);
	}

	@Test
	public void testComplexBean() {
		Manager bean = new Manager();
		String serial = createWriter().write(bean).flush();
		Manager read = createReader(serial).readObject();
		assertEquals(bean, read);

		Person p = new Person();
		p.setGender(Gender.FEMALE);
		p.setName("empl");

		bean = new Manager();
		bean.setName("man");
		bean.setGender(Gender.MALE);
		bean.setStaff(Lists.newArrayList(p));

		serial = createWriter().write(bean).flush();
		read = createReader(serial).readObject();
		assertEquals(bean, read);
	}
}
