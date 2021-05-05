package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;
import helpers.ReminderSetter;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static helpers.ReminderSetter.deleteDailyReminderReference;

//Autor: Thorben Horn
public class StopDailyReminderHandler  implements RequestHandler {
    public static final String SUCCESSFUL_STOP_DAILY_REMINDER_PHRASE = "/Phrases/StopDailyReminder/SUCCESSFUL_STOP_DAILY_REMINDER_TEXT.txt";
    public static final String UNSUCCESSFUL_STOP_DAILY_REMINDER_PHRASE = "/Phrases/StopDailyReminder/UNSUCCESSFUL_STOP_DAILY_REMINDER_TEXT.txt";

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(intentName("StopDailyReminder"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        Optional<Response> permissionResponse = ReminderSetter.getPermission(input);
        if(permissionResponse.isPresent()){
            return permissionResponse;
        }

        final var phrasePath = deleteDailyReminderReference(input) ? SUCCESSFUL_STOP_DAILY_REMINDER_PHRASE : UNSUCCESSFUL_STOP_DAILY_REMINDER_PHRASE;
        return input.getResponseBuilder()
            .withSpeech(new PhraseProvider(phrasePath, Map.of()).get())
            .build();
    }


}
