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
public class StopDailyReminderHandlerTest {
    private static MockedStatic<ReminderSetter> mockReminderSetter;

    private static HandlerInput getHandlerInput(String intentName, String userId) {
        final var intent = Intent.builder().withName(intentName).build();
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
    }

    @Test
    public void testCanHandle() {
        HandlerInput input = getHandlerInput("StopDailyReminder", "userOne");
        assertTrue(new StopDailyReminderHandler().canHandle(input));
    }

    @Test
    public void testCantHandle() {
        HandlerInput input = getHandlerInput("Potato", "userOne");
        assertFalse(new StopDailyReminderHandler().canHandle(input));
    }

    @Test
    public void testNoPermissions() {
        HandlerInput input = getHandlerInput("StopDailyReminder", "someUser");
        Optional<Response> mockResponse = Optional.of(mock(Response.class));
        mockReminderSetter.when(() -> ReminderSetter.getPermission(any())).thenReturn(mockResponse);

        assertEquals(mockResponse, new StopDailyReminderHandler().handle(input));
        mockReminderSetter.verify(() -> ReminderSetter.getPermission(any()));
        mockReminderSetter.verify(never(),() -> ReminderSetter.deleteDailyReminderReference(any()));
    }

    @Test
    public void testSuccessfulDelete() {
        HandlerInput input = getHandlerInput("StopDailyReminder", "someUser");

        assertTrue(new StopDailyReminderHandler().handle(input).isPresent());
        mockReminderSetter.verify(() -> ReminderSetter.getPermission(any()));
        mockReminderSetter.verify(() -> ReminderSetter.deleteDailyReminderReference(any()));
    }

    @Test
    public void testUnsuccessfulDelete() {
        HandlerInput input = getHandlerInput("StopDailyReminder", "someUser");
        mockReminderSetter.when(() -> ReminderSetter.deleteDailyReminderReference(any())).thenReturn(false);

        assertTrue(new StopDailyReminderHandler().handle(input).isPresent());
        mockReminderSetter.verify(() -> ReminderSetter.getPermission(any()));
        mockReminderSetter.verify(() -> ReminderSetter.deleteDailyReminderReference(any()));
    }

    @After
    public void clearMocks() {
        mockReminderSetter.close();
    }
}
