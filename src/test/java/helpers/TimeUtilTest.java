package helpers;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Testklasse fuer TimeUtil.
 *
 * @author Anonymous Student
 */
public class TimeUtilTest {
	@Test
	public void test1() {
		final var sut = new TimeUtil();
		final var have = sut.getTimestamp();
		final var want = Instant.now().getEpochSecond();
		assertEquals(want, have);
	}

	@Test
	public void test2() {
		final var sut = new TimeUtil(0);
		final var have = sut.getDate();
		final var want = "1970-01-01";
		assertEquals(want, have);
	}

	@Test
	public void test3() {
		final var sut = new TimeUtil(0);
		final var have = sut.getTime();
		final var want = "01:00:00";
		assertEquals(want, have);
	}

	@Test
	public void test4() {
		final var sut = new TimeUtil(0);
		final var have = sut.getTimestamp();
		final var want = 0;
		assertEquals(want, have);
	}

	@Test
	public void test5() {
		final var sut = new TimeUtil(1577836800);
		final var have = sut.getDate();
		final var want = "2020-01-01";
		assertEquals(want, have);
	}

	@Test
	public void test6() {
		final var sut = new TimeUtil(1577836800);
		final var have = sut.getTime();
		final var want = "01:00:00";
		assertEquals(want, have);
	}

	@Test
	public void test7() {
		final var have = TimeUtil.getZoneId();
		assertNotNull(have);
	}

	@Test
	public void test8() {
		final var dateTime = LocalDateTime.parse("2021-01-01T00:00:00");
		final var sut = new TimeUtil(dateTime);
		final var want = "2021-01-01";
		assertEquals(want, sut.getDate());
	}

	@Test
	public void test9() {
		final var dateTime = LocalDateTime.parse("2021-01-01T00:00:00");
		final var sut = new TimeUtil(dateTime);
		final var want = "00:00:00";
		assertEquals(want, sut.getTime());
	}

}