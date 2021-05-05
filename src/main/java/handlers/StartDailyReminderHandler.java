package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;
import helpers.ReminderSetter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static helpers.ReminderSetter.*;

//Autor: Thorben Horn
public class StartDailyReminderHandler implements RequestHandler {
	public static final String DAILY_REMINDER_MESSAGE_PHRASE = "/Phrases/StartDailyReminder/DAILY_REMINDER_MESSAGE_TEXT.txt";
	public static final String SUCCESSFUL_START_DAILY_REMINDER_PHRASE = "/Phrases/StartDailyReminder/SUCCESSFUL_START_DAILY_REMINDER_TEXT.txt";
	public static final String UNSUCCESSFUL_START_DAILY_REMINDER_PHRASE = "/Phrases/StartDailyReminder/UNSUCCESSFUL_START_DAILY_REMINDER_TEXT.txt";
	public static final String UNSUCCESSFUL_DELETE_LAST_REMINDER_PHRASE = "/Phrases/StartDailyReminder/UNSUCCESSFUL_DELETE_LAST_REMINDER_TEXT.txt";
	public static final String MISSING_VITAL_ARGUMENT_PHRASE = "/Phrases/StartDailyReminder/MISSING_VITAL_ARGUMENT_TEXT.txt";



	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("StartDailyReminder"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		Optional<Response> permissionResponse = ReminderSetter.getPermission(input);
		if(permissionResponse.isPresent()){
			return permissionResponse;
		}

		String time = ReminderSetter.getAttribute(input, "time");
		if(time == null){
			return input.getResponseBuilder()
				.withSpeech(new PhraseProvider(MISSING_VITAL_ARGUMENT_PHRASE, Map.of()).get())
				.build();
		}

		if(!deleteDailyReminderReference(input)){
			return input.getResponseBuilder()
				.withSpeech(new PhraseProvider(UNSUCCESSFUL_DELETE_LAST_REMINDER_PHRASE, Map.of()).get())
				.build();
		}

		LocalTime scheduledTime = buildTime(time);

		String alertToken = ReminderSetter.addDailyReminder(input, new PhraseProvider(DAILY_REMINDER_MESSAGE_PHRASE, Map.of()).get(), scheduledTime);
		if(alertToken != null && !alertToken.equals("")){
			createDailyReminderReference(input, alertToken);
			return input.getResponseBuilder()
					.withSpeech(new PhraseProvider(SUCCESSFUL_START_DAILY_REMINDER_PHRASE, Map.of()).get())
					.build();
		} else {
			return input.getResponseBuilder()
					.withSpeech(new PhraseProvider(UNSUCCESSFUL_START_DAILY_REMINDER_PHRASE, Map.of()).get())
					.build();
		}
	}

	private LocalTime buildTime(String time) {
		return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
	}
}