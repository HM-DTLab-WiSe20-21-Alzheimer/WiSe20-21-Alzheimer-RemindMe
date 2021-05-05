package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import helpers.TimeUtil2;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author Anonymous Student
 * @author Anonymous Student
 */
@RunWith(Parameterized.class)
public class ReadAllDayEntriesIntentHandlerTest {

	private static final long CURRENT_TIME = 1_609_941_600; // 15 Uhr
	private static MockedStatic<TimeUtil2> mockTimeUtil;
	private static MockedStatic<RemindMeDynamoDB> mockRMDDb;

	private final String message;
	private final String user;

	private static HandlerInput getHandlerInput(String intentName, String userId) {
		final var intent = Intent.builder().withName(intentName).build();
		final var request = IntentRequest.builder().withIntent(intent).build();
		final var user = User.builder().withUserId(userId).build();
		final var session = Session.builder().withUser(user).build();
		final var envelope = RequestEnvelope.builder().withSession(session).withRequest(request).build();
		return HandlerInput.builder().withRequestEnvelope(envelope).build();
	}

	@BeforeClass
	public static void setupMocks() {
		final var mockDB = mock(RemindMeDynamoDB.class);
		when(mockDB.getAllAppointments(Mockito.eq("userNone"))).thenReturn(getAppointmentsNone());
		when(mockDB.getAllAppointments(Mockito.eq("userOne"))).thenReturn(getAppointmentsOne());
		when(mockDB.getAllAppointments(Mockito.eq("userOneNoThings"))).thenReturn(getAppointmentsOneNoThings());
		when(mockDB.getAllAppointments(Mockito.eq("userMultiple"))).thenReturn(getAppointmentsMultiple());

		mockRMDDb = mockStatic(RemindMeDynamoDB.class);
		mockRMDDb.when(RemindMeDynamoDB::getInstance).thenReturn(mockDB);

		final TimeUtil2 timeUtil = TimeUtil2.make(CURRENT_TIME);
		mockTimeUtil = mockStatic(TimeUtil2.class);
		mockTimeUtil.when(TimeUtil2::make).thenReturn(timeUtil);
		mockTimeUtil.when(() -> TimeUtil2.make(anyLong())).thenCallRealMethod();
	}

	@Parameterized.Parameters
	public static List<String[]> userOutput() {
		return Arrays.asList(new String[][] {
				{"Du hast heute keine Ereignisse mehr.",
						"userNone"},
				{"Du hast heute nur das Ereignis AppointmentA um <say-as interpret-as=\"time\">15:00:00</say-as>.",
						"userOneNoThings"},
				{"Du hast heute nur das Ereignis AppointmentA um <say-as interpret-as=\"time\">15:00:00</say-as>, du wolltest something, something2 mitnehmen.",
						"userOne"},
				{"Du hast heute noch die 4 folgenden Ereignisse:\n" +
						" - Um <say-as interpret-as=\"time\">18:00:00</say-as> hast du das Ereignis AppointmentA.\n" +
						" - Um <say-as interpret-as=\"time\">19:00:00</say-as> hast du das Ereignis AppointmentB, du wolltest something mitnehmen.\n" +
						" - Um <say-as interpret-as=\"time\">19:30:00</say-as> hast du das Ereignis AppointmentC, du wolltest something und something2 mitnehmen.\n" +
						" - Um <say-as interpret-as=\"time\">20:00:00</say-as> hast du das Ereignis AppointmentD, du wolltest something, something2 und something3 mitnehmen.",
						"userMultiple"}
		});
	}

	public ReadAllDayEntriesIntentHandlerTest(String msg, String userName) {
		this.message = msg;
		this.user = userName;
	}

	private static List<Appointment> getAppointmentsNone() {
		return List.of();
	}

	private static List<Appointment> getAppointmentsOne() {
		final var listAlerts = List.<String>of();
		return List.of(
				new Appointment(CURRENT_TIME, "userOne", "AppointmentA", "something, something2", listAlerts, 0)
		);
	}

	private static List<Appointment> getAppointmentsOneNoThings() {
		final var listAlerts = List.<String>of();
		return List.of(
				new Appointment(CURRENT_TIME, "userOneNoThings", "AppointmentA", "", listAlerts, 0)
		);
	}

	private static List<Appointment> getAppointmentsMultiple() {
		final var listAlerts = List.<String>of();
		return List.of(
				new Appointment(CURRENT_TIME + 3 * 3600, "userMultiple", "AppointmentA", "", listAlerts, 0),
				new Appointment(CURRENT_TIME + 4 * 3600, "userMultiple", "AppointmentB", "something", listAlerts, 0),
				new Appointment(CURRENT_TIME + 4 * 3600 + 1800, "userMultiple", "AppointmentC", "something und something2", listAlerts, 0),
				new Appointment(CURRENT_TIME + 5 * 3600, "userMultiple", "AppointmentD", "something, something2 und something3", listAlerts, 0)
		);
	}

	@Test
	public void testHandlerOutput() {
		String want = message;
		HandlerInput input = getHandlerInput("ReadAllDayEntries", user);
		String have = getSpeechValue(new ReadAllDayEntriesHandler().handle(input));
		assertEquals(want, have);
	}

	@Test
	public void testCanHandle() {
		HandlerInput input = getHandlerInput("ReadAllDayEntries", "userOne");
		ReadAllDayEntriesHandler handler = new ReadAllDayEntriesHandler();
		assertTrue(handler.canHandle(input));
	}

	@Test
	public void testCanHandle2() {
		HandlerInput input = getHandlerInput("ReadNextEntry", "userOne");
		ReadAllDayEntriesHandler handler = new ReadAllDayEntriesHandler();
		assertFalse(handler.canHandle(input));
	}

	private static String getSpeechValue(Optional<Response> responseOptional) {
		assertTrue(responseOptional.isPresent());
		Response response = responseOptional.get();
		Pattern pattern = Pattern.compile("<speak>.*</speak>", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(response.getOutputSpeech().toString());
		assertTrue(matcher.find());
		String speechText = matcher.group(0).replaceAll("\\R( {4})|\\t", "\n");
		return speechText.substring(7, speechText.length() - 8);
	}

	@AfterClass
	public static void clearMocks() {
		mockTimeUtil.close();
		mockRMDDb.close();
	}
}
