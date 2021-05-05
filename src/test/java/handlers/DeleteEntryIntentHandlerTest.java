package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import helpers.ReminderSetter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Anonymous Student
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteEntryIntentHandlerTest {
    private static final long CURRENT_TIME = 1_609_941_600; // 6 Jan 2021, 15 Uhr

    private static MockedStatic<RemindMeDynamoDB> mockRMDDb;
    private static RemindMeDynamoDB mockDB;
    private static MockedStatic<ReminderSetter> mockReminderSetter;

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
        when(mockDB.getAllAppointments(Mockito.eq("userNone"))).thenReturn(getAppointmentsNone());
        when(mockDB.getAllAppointments(Mockito.eq("userOne"))).thenReturn(getAppointmentsOne());

        mockRMDDb = mockStatic(RemindMeDynamoDB.class);
        mockRMDDb.when(RemindMeDynamoDB::getInstance).thenReturn(mockDB);

        mockReminderSetter = mockStatic(ReminderSetter.class);
        mockReminderSetter.when(() -> ReminderSetter.getPermission(any())).thenReturn(Optional.empty());
        mockReminderSetter.when(() -> ReminderSetter.getAttribute(any(), any())).thenCallRealMethod();
        mockReminderSetter.when(() -> ReminderSetter.deleteReminder(any(), any())).thenReturn(true);

    }

    private static List<Appointment> getAppointmentsNone() {
        return List.of();
    }

    private static List<Appointment> getAppointmentsOne() {
        final var listAlerts = List.of("12341", "12341", "12341");
        return List.of(
                new Appointment(CURRENT_TIME, "userOne", "AppointmentA", "", listAlerts, 0)
        );
    }

    @Test
    public void testDeleteNone() {
        String want = "Ich konnte das Ereignis nicht finden.";
        Map<String, Slot> slots = Map.of(
                "content", Slot.builder().withName("content").withValue("AppointmentA").build(),
                "date", Slot.builder().withName("date").withValue("2021-01-06").build(),
                "time", Slot.builder().withName("time").withValue("15:00:00").build()
        );
        HandlerInput input = getHandlerInput("DeleteEntry", "userNone", slots);
        String have = getSpeechValue(new DeleteEntryIntentHandler().handle(input));
        assertEquals(want, have);
    }

    @Test
    public void testDeleteOne() {
        String want = "Ich habe das Ereignis AppointmentA, am <say-as interpret-as=\"date\">2021-01-06</say-as> um <say-as interpret-as=\"time\">15:00:00</say-as> gelöscht.";
        Map<String, Slot> slots = Map.of(
                "content", Slot.builder().withName("content").withValue("AppointmentA").build(),
                "date", Slot.builder().withName("date").withValue("2021-01-06").build(),
                "time", Slot.builder().withName("time").withValue("15:00:00").build()
        );
        HandlerInput input = getHandlerInput("DeleteEntry", "userOne", slots);
        String have = getSpeechValue(new DeleteEntryIntentHandler().handle(input));
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

    @After
    public void clearMocks() {
        mockRMDDb.close();

        mockReminderSetter.close();
    }
}
