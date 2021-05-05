package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import helpers.ReminderSetter;
import helpers.TimeUtil2;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Anonymous Student
 */
public class AddEntryHandlerTest {
	//timestamps manually calculated/verified using https://www.epochconverter.com/

	private static final long CURRENT_TIME = 1609830000; // 2021-01-05, 8:00:00 Uhr DE
	private static MockedStatic<RemindMeDynamoDB> mockRMDDb;
	private static RemindMeDynamoDB mockDB;
	private static MockedStatic<TimeUtil2> mockTimeUtil2;
	private static MockedStatic<ReminderSetter> mockReminderSetter;
	private static final int SEVEN_DAY_OFFSET = 7 * 24 * 3600;

	private static HandlerInput getHandlerInput(String intentName, String userId, Map<String, Slot> slots) {
		final var intent = Intent.builder().withName(intentName).withSlots(slots).build();
		final var request = IntentRequest.builder().withIntent(intent).build();
		final var user = User.builder().withUserId(userId).build();
		final var session = Session.builder().withUser(user).build();
		final var envelope = RequestEnvelope.builder().withSession(session).withRequest(request).build();
		return HandlerInput.builder().withRequestEnvelope(envelope).build();
	}

	@Before
	public void setupMocks() {
		mockDB = mock(RemindMeDynamoDB.class);
		mockRMDDb = mockStatic(RemindMeDynamoDB.class);
		mockRMDDb.when(RemindMeDynamoDB::getInstance).thenReturn(mockDB);

		TimeUtil2 timeUtil2 = TimeUtil2.make(CURRENT_TIME);
		mockTimeUtil2 = mockStatic(TimeUtil2.class);
		mockTimeUtil2.when(TimeUtil2::make).thenReturn(timeUtil2);
		mockTimeUtil2.when(() -> TimeUtil2.make(any(LocalDateTime.class))).thenCallRealMethod();

		mockReminderSetter = mockStatic(ReminderSetter.class);
		mockReminderSetter.when(() -> ReminderSetter.getPermission(any())).thenReturn(Optional.empty());
		mockReminderSetter.when(() -> ReminderSetter.addReminder(any(), any(), any())).thenReturn("1234");
		mockReminderSetter.when(() -> ReminderSetter.getAttribute(any(), any())).thenCallRealMethod();
		mockReminderSetter.when(() -> ReminderSetter.deleteReminder(any(), any())).thenReturn(true);
	}

	@Test
	public void testCanHandle() {
		HandlerInput input = getHandlerInput("AddEntry", "userOne", Map.of());
		assertTrue(new AddEntryHandler().canHandle(input));
	}

	@Test
	public void testCantHandle() {
		HandlerInput input = getHandlerInput("Potato", "userOne", Map.of());
		assertFalse(new AddEntryHandler().canHandle(input));
	}


	@Test
	public void testNoPermissions() {
		Map<String, Slot> slots = Map.of();
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);
		Optional<Response> mockResponse = Optional.of(mock(Response.class));
		mockReminderSetter.when(() -> ReminderSetter.getPermission(any())).thenReturn(mockResponse);

		assertEquals(mockResponse, new AddEntryHandler().handle(input));
		mockReminderSetter.verify(() -> ReminderSetter.getPermission(any()));
		verify(mockDB, never()).store(any());
	}

	@Test
	public void testWithExistingAppointment() {
		Map<String, Slot> slots = Map.of("thingsToBring", Slot.builder().withName("thingsToBring").withValue("eine Zahnbürste und eine Flasche Wasser").build(),
				"content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build(),
				"time", Slot.builder().withName("time").withValue("16:00:00").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);Appointment want = new Appointment(CURRENT_TIME + 8 * 3600, "someUser", "Zahnarzt", "eine Zahnbürste und eine Flasche Wasser", List.of("1234", "1234", "1234"), CURRENT_TIME + 8 * 3600 + 7 * 24 * 3600);
		Appointment anotherAppointment = new Appointment(CURRENT_TIME + 7 * 3601, "someUser", "Zahnarzt1", "", List.of("12341", "12341", "12341"), CURRENT_TIME + 7 * 3601 + 7 * 24 * 3600);
		mockRMDDb.when(() -> mockDB.getAllAppointments(any())).thenReturn(List.of(anotherAppointment));

		assertTrue(new AddEntryHandler().handle(input).isPresent());
		verify(mockDB).store(any());
	}

	@Test
	public void testWithExistingOverlappingAppointment() {
		Map<String, Slot> slots = Map.of("content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build(),
				"time", Slot.builder().withName("time").withValue("15:00:00").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);
		Appointment want = new Appointment(CURRENT_TIME + 7 * 3600, "someUser", "Zahnarzt", "", List.of("1234", "1234", "1234"), CURRENT_TIME + 7 * 3600 + 7 * 24 * 3600);
		Appointment anotherAppointment = new Appointment(CURRENT_TIME + 7 * 3601, "someUser", "Zahnarzt1", "", List.of("12341", "12341", "12341"), CURRENT_TIME + 7 * 3601 + 7 * 24 * 3600);
		mockRMDDb.when(() -> mockDB.getAllAppointments(any())).thenReturn(List.of(want,anotherAppointment));

		assertTrue(new AddEntryHandler().handle(input).isPresent());
		verify(mockDB, never()).store(any());
	}

	@Test
	public void testWithEmptyAttribute() {
		Map<String, Slot> slots = Map.of("content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);

		assertTrue(new AddEntryHandler().handle(input).isPresent());
		verify(mockDB, never()).store(any());
	}

	@Test
	public void testSuccessfulEntry() {
		Map<String, Slot> slots = Map.of("thingsToBring", Slot.builder().withName("thingsToBring").withValue("eine Zahnbürste und eine Flasche Wasser").build(),
				"content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build(),
				"time", Slot.builder().withName("time").withValue("16:00:00").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);
		Appointment want = new Appointment(CURRENT_TIME + 8 * 3600, "someUser", "Zahnarzt", "eine Zahnbürste und eine Flasche Wasser", List.of("1234", "1234", "1234"), CURRENT_TIME + 8 * 3600 + 7 * 24 * 3600);

		assertTrue(new AddEntryHandler().handle(input).isPresent());
		verify(mockDB).store(want);
		mockReminderSetter.verify(times(3),() -> ReminderSetter.addReminder(any(), any(), any()));
	}

	@Test
	public void testOnlyTwoReminders() {
		Map<String, Slot> slots = Map.of("thingsToBring", Slot.builder().withName("thingsToBring").withValue("eine Zahnbürste und eine Flasche Wasser").build(),
				"content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build(),
				"time", Slot.builder().withName("time").withValue("11:00:00").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);

		assertTrue(new AddEntryHandler().handle(input).isPresent());
		mockReminderSetter.verify(times(2),() -> ReminderSetter.addReminder(any(), any(), any()));
	}

	@Test
	public void testZeroReminders() {
		Map<String, Slot> slots = Map.of("thingsToBring", Slot.builder().withName("thingsToBring").withValue("eine Zahnbürste und eine Flasche Wasser").build(),
				"content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build(),
				"time", Slot.builder().withName("time").withValue("07:00:00").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);

		assertTrue(new AddEntryHandler().handle(input).isPresent());
		mockReminderSetter.verify(never(),() -> ReminderSetter.addReminder(any(), any(), any()));
		mockReminderSetter.verify(never(),() -> ReminderSetter.deleteReminder(any(), any()));
	}

	@Test
	public void testRemindersAPIFail() {
		Map<String, Slot> slots = Map.of("thingsToBring", Slot.builder().withName("thingsToBring").withValue("eine Zahnbürste und eine Flasche Wasser").build(),
				"content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build(),
				"time", Slot.builder().withName("time").withValue("16:00:00").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);
		mockReminderSetter.when(() -> ReminderSetter.addReminder(any(), any(), any())).thenReturn(null,"1234","1234");

		assertTrue(new AddEntryHandler().handle(input).isPresent());
		mockReminderSetter.verify(times(3),() -> ReminderSetter.addReminder(any(), any(), any()));
		mockReminderSetter.verify(times(2),() -> ReminderSetter.deleteReminder(any(), any()));
	}

	@Test
	public void testGetThingsToBringEmpty() {
		Map<String, Slot> slots = Map.of("content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build(),
				"time", Slot.builder().withName("time").withValue("16:00:00").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);
		assertTrue(new AddEntryHandler().handle(input).isPresent());
		Appointment want = new Appointment(CURRENT_TIME + 8 * 3600, "someUser", "Zahnarzt", "", List.of("1234", "1234", "1234"), CURRENT_TIME + 8 * 3600 + 7 * 24 * 3600);
		verify(mockDB).store(want);
	}

	@Test
	public void testGetThingsToBringNo() {
		Map<String, Slot> slots = Map.of("thingsToBring", Slot.builder().withName("thingsToBring").withValue("nein").build(),
				"content", Slot.builder().withName("content").withValue("Zahnarzt").build(),
				"date", Slot.builder().withName("date").withValue("2021-01-05").build(),
				"time", Slot.builder().withName("time").withValue("16:00:00").build());
		HandlerInput input = getHandlerInput("AddEntryHandler", "someUser", slots);
		Optional<Response> response = new AddEntryHandler().handle(input);
		assertTrue(response.isPresent());
		Appointment want = new Appointment(CURRENT_TIME + 8 * 3600, "someUser", "Zahnarzt", "nein", List.of("1234", "1234", "1234"), CURRENT_TIME + 8 * 3600 + 7 * 24 * 3600);
		verify(mockDB, never()).store(want);
	}


	@After
	public void clearMocks() {
		mockRMDDb.close();
		mockTimeUtil2.close();
		mockReminderSetter.close();
	}
}
