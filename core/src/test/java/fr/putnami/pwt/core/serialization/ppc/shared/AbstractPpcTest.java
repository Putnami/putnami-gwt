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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.junit.client.GWTTestCase;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
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

public abstract class AbstractPpcTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "fr.putnami.pwt.core.serialization.ppc.Ppc";
	}

	protected abstract Date parseDate(String text);

	protected abstract PpcSerializer getSerializer();

	private PpcWriter createWriter() {
		return getSerializer().newWriter();
	}

	private PpcReader createReader(String payload) {
		PpcReader reader = getSerializer().newReader();
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
		assertEquals("0|1|--|Z", createWriter().write(Boolean.TRUE).flush());
		assertEquals(Boolean.FALSE, createReader("0|0|--|Z").readObject());
	}

	@Test
	public void testByte() {
		// write
		assertEquals("127", createWriter().write(Byte.MAX_VALUE).flush());
		// read
		assertEquals(Byte.MAX_VALUE, createReader("127").readByte());

		// Byte object
		assertEquals("0|127|--|B", createWriter().write((Byte) Byte.MAX_VALUE).flush());
		assertEquals(Byte.MIN_VALUE, createReader("0|-128|--|B").readObject());
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
		assertEquals("0|D|--|C", createWriter().write((Character) 'D').flush());
		assertEquals('C', createReader("0|C|--|C").readObject());
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
		assertEquals("0|25|--|I", createWriter().write((Integer) 25).flush());
		assertEquals(Integer.valueOf(35), createReader("0|35|--|I").readObject());
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
		assertEquals("0|25|--|J", createWriter().write((Long) 25L).flush());
		assertEquals(Long.valueOf(35), createReader("0|35|--|J").readObject());
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
		assertEquals("0|-32768|--|S", createWriter().write(new Short(Short.MIN_VALUE)).flush());
		assertEquals(new Short(Short.MIN_VALUE), createReader("0|-32768|--|S").readObject());
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
	public void testDate() {
		// write
		assertEquals("0|1426028400000|--|DT", createWriter().write(parseDate("20150311")).flush());
		// read
		assertEquals(parseDate("20150311"), createReader("0|1426028400000|--|DT").<Date> readObject());
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
	public void testVoid() {
		assertEquals(null, createReader("0|--|V").readObject());
	}

	@Test
	public void testEnum() {
		// write
		assertEquals("0|1|2|--|E|fr.putnami.pwt.core.serialization.ppc.shared.Gender|MALE",
				createWriter().write(Gender.MALE).flush());
		// read
		assertEquals(Gender.MALE,
				createReader("0|1|2|--|E|fr.putnami.pwt.core.serialization.ppc.shared.Gender|MALE")
				.<Gender> readObject());
	}

	@Test
	public void testArray() {
		String serial = null;

		String[] strings = {"a", "b", "a"};
		serial = createWriter().write(strings).flush();
		assertEquals("0|0|3|1|2|1|3|1|2|--|[X|X|a|b", serial);
		assertArrayEquals(strings, createReader(serial).readObject());

		Boolean[] bBooleans = {true, false, null};
		serial = createWriter().write(bBooleans).flush();
		assertEquals("0|0|3|1|1|1|0||--|[Z|Z", serial);
		assertArrayEquals(bBooleans, createReader(serial).readObject());

		Date[] dates = {parseDate("20150311")};
		serial = createWriter().write(dates).flush();
		assertEquals("0|0|1|1|1426028400000|--|[DT|DT", serial);
		assertArrayEquals(dates, createReader(serial).readObject());

		Gender[] enums = {Gender.MALE};
		serial = createWriter().write(enums).flush();
		assertEquals("0|0|1|1|2|3|--|[E|E|fr.putnami.pwt.core.serialization.ppc.shared.Gender|MALE", serial);
		assertArrayEquals(enums, createReader(serial).readObject());

		boolean[] booleans = {true, false};
		serial = createWriter().write(booleans).flush();
		assertEquals("0|0|2|1|1|1|0|--|[z|Z", serial);
		assertArrayEquals(booleans, createReader(serial).readObject());

		byte[] bytes = {12, 39};
		serial = createWriter().write(bytes).flush();
		assertEquals("0|0|2|1|12|1|39|--|[b|B", serial);
		assertArrayEquals(bytes, createReader(serial).readObject());

		char[] chars = {'e'};
		serial = createWriter().write(chars).flush();
		assertEquals("0|0|1|1|e|--|[c|C", serial);
		assertArrayEquals(chars, createReader(serial).readObject());

		double[] doubles = {1};
		serial = createWriter().write(doubles).flush();
		assertEquals("0|0|1|1|1.0|--|[d|D", serial);
		assertArrayEquals(doubles, createReader(serial).readObject());

		float[] floats = {1};
		serial = createWriter().write(floats).flush();
		assertEquals("0|0|1|1|1.0|--|[f|F", serial);
		assertArrayEquals(floats, createReader(serial).readObject());

		int[] ints = {1};
		serial = createWriter().write(ints).flush();
		assertEquals("0|0|1|1|1|--|[i|I", serial);
		assertArrayEquals(ints, createReader(serial).readObject());

		long[] longs = {1};
		serial = createWriter().write(longs).flush();
		assertEquals("0|0|1|1|1|--|[j|J", serial);
		assertArrayEquals(longs, createReader(serial).readObject());

		short[] shorts = {1};
		serial = createWriter().write(shorts).flush();
		assertEquals("0|0|1|1|1|--|[s|S", serial);
		assertArrayEquals(shorts, createReader(serial).readObject());
	}

	private void assertArrayEquals(Object expected, Object actual) {
		assertEquals(expected.getClass(), actual.getClass());
		assertEquals(toList(expected), toList(actual));
	}

	private List toList(Object value) {
		Class<?> targetClass = value.getClass().getComponentType();

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

	@Test
	public void testArrayList() {
		List<String> l = Lists.newArrayList("a", "c");
		String serial = createWriter().write(l).flush();
		assertEquals("0|0|2|1|2|1|3|--|AL|X|a|c", serial);
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
		assertEquals("0|0|3|1|2|1|3|1|2|--|LL|X|a|b", serial);
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
		assertEquals("0|0|2|1|2|1|3|--|LHS|X|a|b", serial);
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
		assertEquals("0|0|2|1|2|1|3|--|TS|X|a|b", serial);
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
		assertEquals("0|0|3|1|2|1|3|1|2|--|VT|X|a|b", serial);
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
		assertEquals("0|0|3|1|2|2|3|1|3|2|4|1|4|2|3|--|HM|J|X|a|b", serial);
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
		assertEquals("0|0|3|1|2|2|3|1|3|2|4|1|4|2|3|--|LHM|J|X|a|b", serial);
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
		assertEquals("0|0|3|1|2|2|3|1|3|2|4|1|4|2|3|--|TM|J|X|a|b", serial);
		assertEquals(m, createReader(serial).readObject());
		assertEquals(TreeMap.class, createReader(serial).readObject().getClass());
	}

	@Test
	public void testCustomBeanPublicFields() {
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
		bean.dateObject = parseDate("20150311");
		bean.booleanObject = true;
		bean.byteObject = Byte.MAX_VALUE;
		bean.charObject = 'A';
		bean.doubleObject = 12.4;
		bean.floatObject = 4.1f;
		bean.intObject = 12;
		bean.longObject = 62L;
		bean.shortObject = 223;

		serial = createWriter().write(bean).flush();
		assertEquals("0|0|1|1|127|127|A|A|1|1426028400000|12.4|12.4|4.1|4.1|12|12|62|62|223|223|2|"
			+ "--|fr.putnami.pwt.core.serialization.ppc.shared.BeanPublicFields|DT|a", serial);
		read = createReader(serial).readObject();
		assertEquals(bean, read);
	}

	@Test
	public void testCustomBeanSetters() {
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
		bean.setDateObject(parseDate("20150311"));
		bean.setBooleanObject(true);
		bean.setByteObject(Byte.MAX_VALUE);
		bean.setCharObject('A');
		bean.setDoubleObject(12.4);
		bean.setFloatObject(4.1f);
		bean.setIntObject(12);
		bean.setLongObject(62L);
		bean.setShortObject((short) 223);

		serial = createWriter().write(bean).flush();
		assertEquals("0|0|1|1|127|127|A|A|1|1426028400000|12.4|12.4|4.1|4.1|12|12|62|62|223|223|2|--"
			+ "|fr.putnami.pwt.core.serialization.ppc.shared.BeanSetters|DT|a", serial);
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
		bean.setStaff(Lists.newArrayList(p, p));

		serial = createWriter().write(bean).flush();
		assertEquals(
			"0|0|1|2|3|4|1|2|5|2|1|6|7|5|2|--|fr.putnami.pwt.core.serialization.ppc.shared.Manager"
				+ "|fr.putnami.pwt.core.serialization.ppc.shared.Gender|MALE|man|AL"
				+ "|fr.putnami.pwt.core.serialization.ppc.shared.Person|FEMALE|empl",
			serial);
		read = createReader(serial).readObject();
		assertEquals(bean, read);
	}

	@Test
	public void testComplexSameObject() {
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
		bean.setStaff(Lists.newArrayList(p, p));

		serial = createWriter().write(bean).flush();
		assertEquals(
			"0|0|1|2|3|4|1|2|5|2|1|6|7|5|2|"
				+ "--|fr.putnami.pwt.core.serialization.ppc.shared.Manager|fr.putnami.pwt.core.serialization.ppc.shared.Gender"
				+ "|MALE|man|AL|fr.putnami.pwt.core.serialization.ppc.shared.Person|FEMALE|empl"
			, serial);
		read = createReader(serial).readObject();
		assertEquals(bean, read);
		assertSame(read.getStaff().get(0), read.getStaff().get(1));
	}


}
