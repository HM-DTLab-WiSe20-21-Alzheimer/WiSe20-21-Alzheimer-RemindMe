package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadNextEntryIntentHandlerTest {
	private static MockedStatic<RemindMeDynamoDB> mockStatic;

	private static HandlerInput getHandlerInput(String intentName, String userId) {
		final var intent = Intent.builder().withName(intentName).build();
		final var request = IntentRequest.builder().withIntent(intent).build();
		final var user = User.builder().withUserId(userId).build();
		final var session = Session.builder().withUser(user).build();
		final var envelope = RequestEnvelope.builder().withSession(session).withRequest(request).build();
		return HandlerInput.builder().withRequestEnvelope(envelope).build();
	}

	@BeforeClass
	public static void setupMockDB() {
		final var mockDB = mock(RemindMeDynamoDB.class);
		when(mockDB.getAllAppointments(Mockito.eq("userA"))).thenReturn(getAppointmentsA());

		mockStatic = mockStatic(RemindMeDynamoDB.class);
		mockStatic.when(RemindMeDynamoDB::getInstance).thenReturn(mockDB);
	}

	@AfterClass
	public static void destroyMockDB() {
		mockStatic.close();
	}

	private static List<Appointment> getAppointmentsA() {
		final var listAlerts = List.<String>of();
		return List.of(
				new Appointment(1577836800, "userA", "AppointmentA", "", listAlerts, 0),
				new Appointment(33139929600L, "userA", "AppointmentB", "", listAlerts, 0),
				new Appointment(33134745600L, "userA", "AppointmentC", "", listAlerts, 0),
				new Appointment(33137424000L, "userA", "AppointmentD", "", listAlerts, 0)
		);
	}

	private static List<Appointment> getAppointmentsB() {
		final var listAlerts = List.<String>of();
		return List.of(
				new Appointment(1577836860, "userB", "Appointment1", "", listAlerts, 0),
				new Appointment(33139929660L, "userB", "Appointment2", "", listAlerts, 0),
				new Appointment(33134745660L, "userB", "Appointment3", "something", listAlerts, 0),
				new Appointment(33137424060L, "userB", "Appointment4", "", listAlerts, 0)
		);
	}

	@Test
	public void test1() {
		final var have = ReadNextEntryIntentHandler.processAppointments(getAppointmentsA());
		assertTrue(have.contains("Appointment"));
	}

	@Test
	public void test2() {
		final var have = ReadNextEntryIntentHandler.processAppointments(List.of());
		assertFalse(have.contains("Appointment"));
	}

	@Test
	public void test3() {
		final var sut = new ReadNextEntryIntentHandler();
		final var handler = getHandlerInput("ReadNextEntry", "userA");
		final var have = sut.canHandle(handler);
		assertTrue(have);
	}

	@Test
	public void test4() {
		final var sut = new ReadNextEntryIntentHandler();
		final var have = sut.canHandle(getHandlerInput("SomeOtherIntent", "userA"));
		assertFalse(have);
	}

	@Test
	public void test5() {
		final var sut = new ReadNextEntryIntentHandler();
		final var have = sut.handle(getHandlerInput("ReadNextEntry", "userA"));
		assertTrue(have.isPresent());
	}


	@Test
	public void test6() {
		final var have = ReadNextEntryIntentHandler.processAppointments(getAppointmentsB());
		assertTrue(have.contains("something"));
	}
}