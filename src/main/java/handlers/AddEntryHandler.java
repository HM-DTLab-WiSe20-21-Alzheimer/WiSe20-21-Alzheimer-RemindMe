package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;
import helpers.ReminderSetter;
import helpers.TimeUtil2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Kuemmert sich um den AddEntry Intent und fuegt dabei sowohl den Termin der Datenbank hinzu als auch drei Erinnerungen
 * der Reminder Api.
 *
 * @author Anonymous Student
 * debugging: Andreas Urlberger, Thorben Horn
 */
public class AddEntryHandler implements RequestHandler {
	public static final String IN30MIN_REMINDER_MESSAGE_PHRASE = "/Phrases/AddEntry/IN30MIN_REMINDER_MESSAGE_TEXT.txt";
	public static final String IN2H_REMINDER_MESSAGE_PHRASE = "/Phrases/AddEntry/IN2H_REMINDER_MESSAGE_TEXT.txt";
	public static final String IN4H_REMINDER_MESSAGE_PHRASE = "/Phrases/AddEntry/IN4H_REMINDER_MESSAGE_TEXT.txt";

	public static final String SUCCESSFUL_ADDED_ENTRY_PHRASE = "/Phrases/AddEntry/SUCCESSFUL_ADDED_ENTRY_TEXT.txt";
	public static final String UNSUCCESSFUL_ADDED_ENTRY_PHRASE = "/Phrases/AddEntry/UNSUCCESSFUL_ADDED_ENTRY_TEXT.txt";
	public static final String UNSUCCESSFUL_BECAUSE_SAME_TIME_ENTRY_PHRASE = "/Phrases/AddEntry/UNSUCCESSFUL_BECAUSE_SAME_TIME_ENTRY_TEXT.txt";
	public static final String MISSING_VITAL_ARGUMENT_PHRASE = "/Phrases/AddEntry/MISSING_VITAL_ARGUMENT_TEXT.txt";
	public static final String UNSUCCESSFUL_BECAUSE_SAME_TIME_ENTRY_KEY= "existingAppointmentName";



	private static final Set<String> noSynonyms = Set.of("nein", "ne", "jetzt nicht", "nö", "nein danke", "nein möchte ich nicht", "nein will ich nicht", "nein jetzt nicht");


	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AddEntry"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		Optional<Response> permissionResponse = ReminderSetter.getPermission(input);
		if(permissionResponse.isPresent()){
			return permissionResponse;
		}

		String content = ReminderSetter.getAttribute(input, "content");
		String date = ReminderSetter.getAttribute(input, "date");
		String time = ReminderSetter.getAttribute(input, "time");
		if(content == null || date == null || time == null){
			return input.getResponseBuilder()
					.withSpeech(new PhraseProvider(MISSING_VITAL_ARGUMENT_PHRASE, Map.of()).get())
					.build();
		}

		LocalDateTime scheduledDateTime = buildDateTime(date,time);


		RemindMeDynamoDB remindMeDynamoDB = RemindMeDynamoDB.getInstance();
		String userId = input.getRequestEnvelope().getSession().getUser().getUserId();
		String thingsToBring = getThingsToBring(input);
		long dateTimeEpoch = TimeUtil2.make(scheduledDateTime).getTimestamp();
		long expirationDate = TimeUtil2.make(scheduledDateTime.plusDays(7)).getTimestamp();

		String sameTimeAppointment = checkForAppointment(dateTimeEpoch, remindMeDynamoDB, userId);
		if (!sameTimeAppointment.equals("")){
			return input.getResponseBuilder()
				.withSpeech(new PhraseProvider(UNSUCCESSFUL_BECAUSE_SAME_TIME_ENTRY_PHRASE, Map.of(UNSUCCESSFUL_BECAUSE_SAME_TIME_ENTRY_KEY, sameTimeAppointment)).get())
				.build();
		}

		List<String> alertTokens = new ArrayList<>();
		LocalDateTime nowTime = TimeUtil2.make().getLocalDateTime();
		if(nowTime.isBefore(scheduledDateTime.minusMinutes(30))){
			alertTokens.add(ReminderSetter.addReminder(input, new PhraseProvider(IN30MIN_REMINDER_MESSAGE_PHRASE, Map.of()).get(), scheduledDateTime.minusMinutes(30)));
		}
		if(nowTime.isBefore(scheduledDateTime.minusHours(2))){
			alertTokens.add(ReminderSetter.addReminder(input, new PhraseProvider(IN2H_REMINDER_MESSAGE_PHRASE, Map.of()).get(), scheduledDateTime.minusHours(2)));
		}
		if(nowTime.isBefore(scheduledDateTime.minusHours(4))){
			alertTokens.add(ReminderSetter.addReminder(input, new PhraseProvider(IN4H_REMINDER_MESSAGE_PHRASE, Map.of()).get(), scheduledDateTime.minusHours(4)));
		}

		if(!alertTokens.contains(null) && !alertTokens.contains("") && !alertTokens.isEmpty()) {
			remindMeDynamoDB.store(new Appointment(dateTimeEpoch, userId, content, thingsToBring, alertTokens, expirationDate));
			return input.getResponseBuilder()
					.withSpeech(new PhraseProvider(SUCCESSFUL_ADDED_ENTRY_PHRASE, Map.of()).get())
					.build();
		} else {
			for (String alertToken : alertTokens) {
				if(alertToken != null){
					ReminderSetter.deleteReminder(input, alertToken);
				}
			}
			return input.getResponseBuilder()
					.withSpeech(new PhraseProvider(UNSUCCESSFUL_ADDED_ENTRY_PHRASE, Map.of()).get())
					.build();
		}
	}

	private LocalDateTime buildDateTime(String date, String time) {
		LocalDate scheduledDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
		LocalTime scheduledTime = LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
		return scheduledDate.atTime(scheduledTime);
	}


	/**
	 * Gibt einen String mit den Dingen die der Nutzer mitnehmen moechte zurueck.
	 *
	 * @param input HandlerInput aus dem die Slot Daten ausgelesen werden koennen.
	 * @return String mit Dingen die der Nutzer mitnehmen moechte, leer wenn der Nutzer keine Dinge mitnehmen will.
	 */
	private String getThingsToBring(HandlerInput input) {
		String thingsToBring = ReminderSetter.getAttribute(input, "thingsToBring");
		if (thingsToBring == null || thingsToBring.isEmpty() || noSynonyms.contains(thingsToBring.toLowerCase()))
			thingsToBring = "";

		return thingsToBring;
	}

	private String checkForAppointment(long dateTimeEpoch, RemindMeDynamoDB remindMeDynamoDB, String userId){
		List<Appointment> allAppointments = remindMeDynamoDB.getAllAppointments(userId);
		if (allAppointments != null && !allAppointments.isEmpty()) {
			for (Appointment appointment: allAppointments) {
				if(appointment.getTime() == dateTimeEpoch){
					return appointment.getContent();
				}
			}
		}
		return "";
	}
}