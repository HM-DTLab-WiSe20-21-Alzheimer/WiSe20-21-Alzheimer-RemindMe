package helpers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import com.amazon.ask.model.services.ServiceClientFactory;
import com.amazon.ask.model.services.ServiceException;
import com.amazon.ask.model.services.reminderManagement.ReminderManagementServiceClient;
import com.amazon.ask.model.services.reminderManagement.ReminderResponse;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static helpers.ReminderSetter.ALERT_TOKEN_KEY;
import static helpers.ReminderSetter.REMINDERS_API_PERMISSION;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


//Autor: Thorben Horn
//This is bad, but I want to sleep now
public class ReminderSetterTest {

    private static HandlerInput getHandlerInput(Map<String, Slot> slots, String consentToken) {
        final var intent = Intent.builder().withName("AddEntryHandler").withSlots(slots).build();
        final var request = IntentRequest.builder().withIntent(intent).build();
        final var permissions = Permissions.builder().withConsentToken(consentToken).build();
        final var user = User.builder().withUserId("someUser").withPermissions(permissions).build();
        final var session = Session.builder().withUser(user).build();
        final var envelope = RequestEnvelope.builder().withSession(session).withRequest(request).build();
        return HandlerInput.builder().withRequestEnvelope(envelope).build();
    }

    private static HandlerInput getHandlerInput(Map<String, Slot> slots, String consentToken, ServiceClientFactory serviceClientFactory) {
        final var intent = Intent.builder().withName("AddEntryHandler").withSlots(slots).build();
        final var request = IntentRequest.builder().withIntent(intent).build();
        final var permissions = Permissions.builder().withConsentToken(consentToken).build();
        final var user = User.builder().withUserId("someUser").withPermissions(permissions).build();
        final var session = Session.builder().withUser(user).build();
        final var envelope = RequestEnvelope.builder().withSession(session).withRequest(request).build();
        return HandlerInput.builder().withRequestEnvelope(envelope).withServiceClientFactory(serviceClientFactory).build();
    }

    @Test
    public void testAddReminder() {
        ReminderResponse mockReminderResponse = mock(ReminderResponse.class);
        ServiceClientFactory mockServiceClientFactory = mock(ServiceClientFactory.class);
        ReminderManagementServiceClient mockReminderManagementServiceClient = mock(ReminderManagementServiceClient.class);
        when(mockServiceClientFactory.getReminderManagementService()).thenReturn(mockReminderManagementServiceClient);
        when(mockReminderManagementServiceClient.createReminder(any())).thenReturn(mockReminderResponse);
        when(mockReminderResponse.getAlertToken()).thenReturn("1234");
        HandlerInput input = getHandlerInput(Map.of(),null, mockServiceClientFactory);

        assertNull(ReminderSetter.addReminder(input, "1234", LocalDateTime.now()));

        input = getHandlerInput(Map.of(),REMINDERS_API_PERMISSION, mockServiceClientFactory);
        assertEquals("1234", ReminderSetter.addReminder(input,"1234", LocalDateTime.now()));

        when(mockReminderManagementServiceClient.createReminder(any())).thenThrow(new ServiceException("", 1, List.of(), null));
        assertNull(ReminderSetter.addReminder(input,"1234", LocalDateTime.now()));
    }

    @Test
    public void testDailyAddReminder() {
        ReminderResponse mockReminderResponse = mock(ReminderResponse.class);
        ServiceClientFactory mockServiceClientFactory = mock(ServiceClientFactory.class);
        ReminderManagementServiceClient mockReminderManagementServiceClient = mock(ReminderManagementServiceClient.class);
        when(mockServiceClientFactory.getReminderManagementService()).thenReturn(mockReminderManagementServiceClient);
        when(mockReminderManagementServiceClient.createReminder(any())).thenReturn(mockReminderResponse);
        when(mockReminderResponse.getAlertToken()).thenReturn("1234");
        HandlerInput input = getHandlerInput(Map.of(),null, mockServiceClientFactory);

        assertNull(ReminderSetter.addDailyReminder(input, "1234", LocalTime.now()));

        input = getHandlerInput(Map.of(),REMINDERS_API_PERMISSION, mockServiceClientFactory);
        assertEquals("1234", ReminderSetter.addDailyReminder(input,"1234", LocalTime.now()));

        when(mockReminderManagementServiceClient.createReminder(any())).thenThrow(new ServiceException("", 1, List.of(), null));
        assertNull(ReminderSetter.addDailyReminder(input,"1234", LocalTime.now()));
    }

    @Test
    public void testDeleteReminder() {
        ServiceClientFactory mockServiceClientFactory = mock(ServiceClientFactory.class);
        ReminderManagementServiceClient mockReminderManagementServiceClient = mock(ReminderManagementServiceClient.class);
        when(mockServiceClientFactory.getReminderManagementService()).thenReturn(mockReminderManagementServiceClient);
        assertTrue(ReminderSetter.deleteReminder(getHandlerInput(Map.of(),REMINDERS_API_PERMISSION, mockServiceClientFactory),"1234"));

        when(mockServiceClientFactory.getReminderManagementService()).thenThrow(new ServiceException("", 1, List.of(), null));
        assertFalse(ReminderSetter.deleteReminder(getHandlerInput(Map.of(),REMINDERS_API_PERMISSION, mockServiceClientFactory),"1234"));

        assertFalse(ReminderSetter.deleteReminder(getHandlerInput(Map.of(),null),"1234"));
    }

    @Test
    public void testGetPermission() {
        assertTrue(ReminderSetter.getPermission(getHandlerInput(Map.of(),null)).isPresent());
        assertTrue(ReminderSetter.getPermission(getHandlerInput(Map.of(),REMINDERS_API_PERMISSION)).isEmpty());
    }

    @Test
    public void testGetAttribute() {
        Map<String, Slot> slots = Map.of("time", Slot.builder().withName("time").withValue("16:00:00").build());
        HandlerInput input = getHandlerInput(slots,null);
        assertEquals("16:00:00", ReminderSetter.getAttribute(input,"time"));
    }

    @Test
    public void testGetNonExistingAttribute() {
        HandlerInput input = getHandlerInput(Map.of(),null);
        assertNull(ReminderSetter.getAttribute(input, "time"));
    }

    @Test
    public void testDailyReminderExists() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        map.put(ALERT_TOKEN_KEY, "1234");
        AttributesManager mockAttributesManager = mock(AttributesManager.class);
        HandlerInput mockHandlerInput = mock(HandlerInput.class);
        when(mockHandlerInput.getAttributesManager()).thenReturn(mockAttributesManager);
        when(mockAttributesManager.getPersistentAttributes()).thenReturn(map);

        Method method = ReminderSetter.class.getDeclaredMethod("dailyReminderExists",HandlerInput.class);
        method.setAccessible(true);

        boolean result = (Boolean) method.invoke(null, mockHandlerInput);
        assertTrue(result);
        map.clear();
        result = (Boolean) method.invoke(null, mockHandlerInput);
        assertFalse(result);
    }


    @Test
    public void testDeleteDailyReminderReference() {
        Map<String, Object> map = new HashMap<>();
        AttributesManager mockAttributesManager = mock(AttributesManager.class);
        HandlerInput mockHandlerInput = mock(HandlerInput.class);
        when(mockHandlerInput.getAttributesManager()).thenReturn(mockAttributesManager);
        when(mockAttributesManager.getPersistentAttributes()).thenReturn(map);

        assertTrue(ReminderSetter.deleteDailyReminderReference(mockHandlerInput));
    }


    @Test
    public void testCreateDailyReminderReference() {
        Map<String, Object> map = new HashMap<>();
        AttributesManager mockAttributesManager = mock(AttributesManager.class);
        HandlerInput mockHandlerInput = mock(HandlerInput.class);
        when(mockHandlerInput.getAttributesManager()).thenReturn(mockAttributesManager);
        when(mockAttributesManager.getPersistentAttributes()).thenReturn(map);

        ReminderSetter.createDailyReminderReference(mockHandlerInput, "1234");

        verify(mockAttributesManager).getPersistentAttributes();
        verify(mockAttributesManager).savePersistentAttributes();
        assertTrue(map.containsValue("1234"));
    }
}
