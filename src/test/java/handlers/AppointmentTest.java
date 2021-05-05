package handlers;

import helpers.TimeUtil2;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Anonymous Student
 */
public class AppointmentTest {

	@Test
	public void testGetterSetter() {
		long time = 1234;
		String userId = "user1234";
		String content = "Zahnarzt";
		String thingsToBring = "Zahnbürste";
		List<String> alertTokens = List.of("1234", "1234");
		long expirationDate = 1000;

		Appointment app = new Appointment();
		app.setTime(time);
		app.setUserId(userId);
		app.setContent(content);
		app.setThingsToBring(thingsToBring);
		app.setAlertTokens(alertTokens);
		app.setExpirationDate(expirationDate);

		assertEquals(time, app.getTime());
		assertEquals(userId, app.getUserId());
		assertEquals(content, app.getContent());
		assertEquals(thingsToBring, app.getThingsToBring());
		assertEquals(alertTokens, app.getAlertTokens());
		assertEquals(expirationDate, app.getExpirationDate());
	}

	@Test
	public void testConstructor() {
		long time = 1234;
		String userId = "user1234";
		String content = "Zahnarzt";
		String thingsToBring = "Zahnbürste";
		List<String> alertTokens = List.of("1234", "1234");
		long expirationDate = 1000;

		Appointment app = new Appointment(time, userId, content, thingsToBring, alertTokens, expirationDate);

		assertEquals(time, app.getTime());
		assertEquals(userId, app.getUserId());
		assertEquals(content, app.getContent());
		assertEquals(thingsToBring, app.getThingsToBring());
		assertEquals(alertTokens, app.getAlertTokens());
		assertEquals(expirationDate, app.getExpirationDate());
	}

	@Test
	public void testGetPhraseValues() {
		long time = 1234;
		String userId = "user1234";
		String content = "Zahnarzt";
		String thingsToBring = "Zahnbürste";
		List<String> alertTokens = List.of("1234", "1234");
		long expirationDate = 1000;

		Appointment app = new Appointment(time, userId, content, thingsToBring, alertTokens, expirationDate);

		Map<String, String> want = new HashMap<>();
		TimeUtil2 timeUtil = TimeUtil2.make(time);
		want.put("time", timeUtil.getTime());
		want.put("content", content);
		want.put("thingsToBring", thingsToBring);
		want.put("date", timeUtil.getDate());
		Map<String, String> have = app.getPhraseValues();

		assertEquals(want, have);
	}

	@Test
	public void testEquals() {
		long time = 1234;
		String userId = "user1234";
		String content = "Zahnarzt";
		String thingsToBring = "Zahnbürste";
		List<String> alertTokens = List.of("1234", "1234");
		long expirationDate = 1000;

		Appointment app = new Appointment(time, userId, content, thingsToBring, alertTokens, expirationDate);
		Appointment app2 = new Appointment(time, userId, content, thingsToBring, alertTokens, expirationDate);
		assertEquals(app, app2);
	}

	@Test
	public void testHashCode() {
		long time = 1234;
		String userId = "user1234";
		String content = "Zahnarzt";
		String thingsToBring = "Zahnbürste";
		List<String> alertTokens = List.of("1234", "1234");
		long expirationDate = 1000;
		Appointment app1 = new Appointment(time, userId, content, thingsToBring, alertTokens, expirationDate);
		Appointment app2 = new Appointment(time, userId, content, thingsToBring, alertTokens, expirationDate);

		assertEquals(app1.hashCode(), app2.hashCode());
	}

	@Test
	public void testHashCode2() {
		long time = 1234;
		String userId = "user1234";
		String content = "Zahnarzt";
		String thingsToBring = "Zahnbürste";
		List<String> alertTokens = List.of("1234", "1234");
		long expirationDate = 1000;
		Appointment app1 = new Appointment(time, userId, content, thingsToBring, alertTokens, expirationDate);
		Appointment app2 = new Appointment(12345, userId, content, thingsToBring, alertTokens, expirationDate);

		assertNotEquals(app1.hashCode(), app2.hashCode());
	}
}
