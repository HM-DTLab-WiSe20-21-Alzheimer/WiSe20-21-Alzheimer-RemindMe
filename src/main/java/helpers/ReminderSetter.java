package helpers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Permissions;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.services.ServiceException;
import com.amazon.ask.model.services.reminderManagement.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static helpers.TimeUtil2.ZONE_GERMANY;

//Autor: Thorben Horn
public final class ReminderSetter {
	public static final String REMINDERS_API_PERMISSION = "alexa::alerts:reminders:skill:readwrite";
	public static final String ALERT_TOKEN_KEY = "DailyReminderAlertTokenKey";


	private static final String REMINDERS_API_REQUEST_PERMISSION_PHRASE = "/Phrases/ReminderSetter/REMINDERS_API_REQUEST_PERMISSION_TEXT.txt";


	private ReminderSetter(){}


	private static String addReminder(HandlerInput handlerInput, String content, LocalDateTime scheduledTime, Recurrence recurrence){//returns reminder alert token (null = failed to add reminder)
		Trigger trigger = Trigger.builder()
				.withType(TriggerType.SCHEDULED_ABSOLUTE)
				.withScheduledTime(scheduledTime)
				.withTimeZoneId(ZONE_GERMANY.getId())
				.withRecurrence(recurrence)
				.build();

		SpokenText spokenText = SpokenText.builder()
				.withText(content)
				.withLocale(handlerInput.getRequestEnvelope().getRequest().getLocale())
				.build();
		AlertInfoSpokenInfo alertInfoSpokenInfo = AlertInfoSpokenInfo.builder()
				.addContentItem(spokenText)
				.build();
		AlertInfo alertInfo = AlertInfo.builder()
				.withSpokenInfo(alertInfoSpokenInfo)
				.build();

		PushNotification pushNotification = PushNotification.builder()
				.withStatus(PushNotificationStatus.ENABLED)
				.build();

		ReminderRequest reminderRequest = ReminderRequest.builder()
				.withAlertInfo(alertInfo)
				.withRequestTime(OffsetDateTime.now())
				.withTrigger(trigger)
				.withPushNotification(pushNotification)
				.build();
		try {
			ReminderResponse reminderResponse = handlerInput.getServiceClientFactory().getReminderManagementService().createReminder(reminderRequest);
			return reminderResponse.getAlertToken();
		} catch (ServiceException e) {
			return null;
		}
	}

	public static String addReminder(HandlerInput handlerInput, String content, LocalDateTime scheduledTime){
		if(hasPermission(handlerInput)){
			return addReminder(handlerInput, content, scheduledTime, null);
		}
		return null;

	}

	public static String addDailyReminder(HandlerInput handlerInput, String content, LocalTime scheduledTime){//returns reminder alert token (null = failed to add reminder)
		if(hasPermission(handlerInput)){
			StringBuilder recurrenceRule = new StringBuilder()
					.append("FREQ=DAILY;BYHOUR=")
					.append(scheduledTime.getHour())
					.append(";BYMINUTE=")
					.append(scheduledTime.getMinute())
					.append(";BYSECOND=")
					.append(scheduledTime.getSecond())
					.append(";INTERVAL=1;");
			Recurrence recurrence = Recurrence.builder()
					.addRecurrenceRulesItem(recurrenceRule.toString())
					.build();
			LocalDateTime setTime = LocalDate.now().atTime(scheduledTime);
			return addReminder(handlerInput, content, setTime, recurrence);
		}
		return null;
	}

	public static boolean deleteReminder(HandlerInput handlerInput, String alertToken){//returns successful?
		if(hasPermission(handlerInput)){
			try {
				handlerInput.getServiceClientFactory().getReminderManagementService().deleteReminder(alertToken);
				return true;
			} catch (ServiceException e) {
				return false;
			}
		}
		return false;
	}

	private static boolean hasPermission(HandlerInput handlerInput) {
		Permissions permissions = handlerInput.getRequestEnvelope().getSession().getUser().getPermissions();
		return permissions != null && permissions.getConsentToken() != null;
	}

	/*returns true if it has added a permissionsrequest to the handlerInt (just return it and end your handler) or
	if false continue with your handler (you can add a reminder)*/
	public static Optional<Response> getPermission(HandlerInput handlerInput){
		if(!hasPermission(handlerInput)){
			List<String> requestedPermissions = new ArrayList<>();
			requestedPermissions.add(REMINDERS_API_PERMISSION);
			return handlerInput.getResponseBuilder()
					.withSpeech(new PhraseProvider(REMINDERS_API_REQUEST_PERMISSION_PHRASE, Map.of()).get())
					.withAskForPermissionsConsentCard(requestedPermissions)
					.build();
		}
		return Optional.empty();
	}

	public static String getAttribute(HandlerInput handlerInput, String key){//returns value for key, null if error -> couldnt retrieve (not empty string if error, because empty value for key is possible)
		Map<String, Slot> slots = ((IntentRequest) handlerInput.getRequestEnvelope().getRequest()).getIntent().getSlots();
		if(slots.containsKey(key)) {
			return slots.get(key).getValue();
		}
		return null;
	}

	public static boolean deleteDailyReminderReference(HandlerInput input){//returns true if successful, false if not
		if (dailyReminderExists(input)) {
			String alertToken = input.getAttributesManager().getPersistentAttributes().get(ALERT_TOKEN_KEY).toString();
			if (deleteReminder(input, alertToken)) {
				input.getAttributesManager().getPersistentAttributes().remove(ALERT_TOKEN_KEY);
				input.getAttributesManager().savePersistentAttributes();
				return true;
			}
			return false;
		}
		return true;
	}

	public static void createDailyReminderReference(HandlerInput input, String alertToken){
		input.getAttributesManager().getPersistentAttributes().put(ALERT_TOKEN_KEY, alertToken);
		input.getAttributesManager().savePersistentAttributes();
	}

	private static boolean dailyReminderExists(HandlerInput input){
		return input.getAttributesManager().getPersistentAttributes().containsKey(ALERT_TOKEN_KEY);
	}
}
