package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import helpers.ReminderSetter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//Autor: Thorben Horn
public class StartDailyReminderHandlerTest {
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
        mockReminderSetter = mockStatic(ReminderSetter.class);
        mockReminderSetter.when(() -> ReminderSetter.getPermission(any())).thenReturn(Optional.empty());
        mockReminderSetter.when(() -> ReminderSetter.deleteDailyReminderReference(any())).thenReturn(true);
        mockReminderSetter.when(() -> ReminderSetter.getAttribute(any(), any())).thenCallRealMethod();
        mockReminderSetter.when(() -> ReminderSetter.addDailyReminder(any(), any(), any())).thenReturn("1234");
    }

    @Test
    public void testCanHandle() {
        HandlerInput input = getHandlerInput("StartDailyReminder", "userOne", Map.of());
        assertTrue(new StartDailyReminderHandler().canHandle(input));
    }

    @Test
    public void testCantHandle() {
        HandlerInput input = getHandlerInput("Potato", "userOne", Map.of());
        assertFalse(new StartDailyReminderHandler().canHandle(input));
    }

    @Test
    public void testNoPermissions() {
        HandlerInput input = getHandlerInput("StartDailyReminder", "someUser", Map.of());
        Optional<Response> mockResponse = Optional.of(mock(Response.class));
        mockReminderSetter.when(() -> ReminderSetter.getPermission(any())).thenReturn(mockResponse);

        assertEquals(mockResponse, new StartDailyReminderHandler().handle(input));
        mockReminderSetter.verify(() -> ReminderSetter.getPermission(any()));
    }

    @Test
    public void testWithEmptyAttribute() {
        HandlerInput input = getHandlerInput("StartDailyReminder", "someUser", Map.of());

        assertTrue(new StartDailyReminderHandler().handle(input).isPresent());
        mockReminderSetter.verify(never(),() -> ReminderSetter.deleteDailyReminderReference(any()));
    }

    @Test
    public void testUnsuccessfulDelete() {
        Map<String, Slot> slots = Map.of("time", Slot.builder().withName("time").withValue("08:00:00").build());
        HandlerInput input = getHandlerInput("StartDailyReminder", "someUser", slots);
        mockReminderSetter.when(() -> ReminderSetter.deleteDailyReminderReference(any())).thenReturn(false);

        assertTrue(new StartDailyReminderHandler().handle(input).isPresent());
        mockReminderSetter.verify(never(),() -> ReminderSetter.addDailyReminder(any(),any(),any()));
    }

    @Test
    public void testSuccessfulNewDailyReminder() {
        Map<String, Slot> slots = Map.of("time", Slot.builder().withName("time").withValue("08:00:00").build());
        HandlerInput input = getHandlerInput("StartDailyReminder", "someUser", slots);

        assertTrue(new StartDailyReminderHandler().handle(input).isPresent());
        mockReminderSetter.verify(() -> ReminderSetter.addDailyReminder(any(),any(),any()));
        mockReminderSetter.verify(() -> ReminderSetter.createDailyReminderReference(any(),any()));
    }

    @Test
    public void testUnsuccessfulNewDailyReminder() {
        Map<String, Slot> slots = Map.of("time", Slot.builder().withName("time").withValue("08:00:00").build());
        HandlerInput input = getHandlerInput("StartDailyReminder", "someUser", slots);
        mockReminderSetter.when(() -> ReminderSetter.addDailyReminder(any(), any(), any())).thenReturn("");

        assertTrue(new StartDailyReminderHandler().handle(input).isPresent());
        mockReminderSetter.verify(() -> ReminderSetter.addDailyReminder(any(),any(),any()));
        mockReminderSetter.verify(never(),() -> ReminderSetter.createDailyReminderReference(any(),any()));
    }

    @After
    public void clearMocks() {
        mockReminderSetter.close();
    }
}
