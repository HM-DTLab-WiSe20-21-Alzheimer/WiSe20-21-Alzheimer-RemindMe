package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import helpers.TimeUtil2;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

public class ReadAnyDayAllEntriesHandlerTest {

    private static final long CURRENT_TIME = 1_609_941_600; // 06.01.2021 15 Uhr
    private static final long APPOINTMENT_TIME = 1_610_114_400; // 08.01.2021 15 Uhr
    private static MockedStatic<TimeUtil2> mockTimeUtil;
    private static MockedStatic<RemindMeDynamoDB> mockRMDDb;

    private static HandlerInput getHandlerInput(String intentName, String userId, Map<String, Slot> slots) {
        final var intent = Intent.builder().withName(intentName).withSlots(slots).build();
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

        mockRMDDb = mockStatic(RemindMeDynamoDB.class);
        mockRMDDb.when(RemindMeDynamoDB::getInstance).thenReturn(mockDB);

        final TimeUtil2 timeUtil = TimeUtil2.make(CURRENT_TIME);
        mockTimeUtil = mockStatic(TimeUtil2.class);
        mockTimeUtil.when(TimeUtil2::make).thenReturn(timeUtil);
        mockTimeUtil.when(() -> TimeUtil2.make(anyLong())).thenCallRealMethod();
        mockTimeUtil.when(() -> TimeUtil2.make(any(LocalDateTime.class))).thenCallRealMethod();
    }

    private static List<Appointment> getAppointmentsNone() {
        return List.of();
    }

    private static List<Appointment> getAppointmentsOne() {
        final var listAlerts = List.<String>of();
        return List.of(
                new Appointment(APPOINTMENT_TIME, "userOne", "AppointmentA", "", listAlerts, 0)
        );
    }

    @Test
    public void testCanHandle() {
        Map<String, Slot> slots = Map.of(
                "content", Slot.builder().withName("content").withValue("AppointmentA").build(),
                "date", Slot.builder().withName("date").withValue("2021-01-08").build()
        );
        HandlerInput input = getHandlerInput("ReadAnyDayAllEntries", "userOne", slots);
        ReadAnyDayAllEntriesHandler handler = new ReadAnyDayAllEntriesHandler();
        Assert.assertTrue(handler.canHandle(input));
    }

    @Test
    public void testCantHandle() {
        Map<String, Slot> slots = Map.of(
                "content", Slot.builder().withName("content").withValue("AppointmentA").build(),
                "date", Slot.builder().withName("date").withValue("2021-01-08").build()
        );
        HandlerInput input = getHandlerInput("ReadAnyDayAllEntries", "userOne", slots);
        ReadAllDayEntriesHandler handler = new ReadAllDayEntriesHandler();
        Assert.assertFalse(handler.canHandle(input));
    }

    @Test
    public void testNoEntries() {
        String want = "Es sind keine Ereignisse vorhanden.";
        Map<String, Slot> slots = Map.of(
                "content", Slot.builder().withName("content").withValue("AppointmentA").build(),
                "date", Slot.builder().withName("date").withValue("2021-01-06").build()
        );
        HandlerInput input = getHandlerInput("ReadAnyDayAllEntries", "userNone", slots);
        String have = getSpeechValue(new ReadAnyDayAllEntriesHandler().handle(input));
        assertEquals(want, have);
    }

    @Test
    public void testOneEntryWithThings() {
        String want = "Es ist nur das Ereignis AppointmentA um <say-as interpret-as=\"time\">15:00:00</say-as> eingetragen.";
        Map<String, Slot> slots = Map.of(
                "content", Slot.builder().withName("content").withValue("AppointmentA").build(),
                "date", Slot.builder().withName("date").withValue("2021-01-08").build()
        );
        HandlerInput input = getHandlerInput("ReadAnyDayAllEntries", "userOne", slots);
        String have = getSpeechValue(new ReadAnyDayAllEntriesHandler().handle(input));
        assertEquals(want, have);
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