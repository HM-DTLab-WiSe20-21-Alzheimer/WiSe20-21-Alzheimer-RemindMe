package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handler fuer Intent: AMAZON.HelpIntent
 * @author Anonymous Student
 */
public class HelpIntentHandler implements RequestHandler {
	private static final String WELCOME_PHRASE = "/Phrases/HelpIntent/WELCOME_PHRASE";
	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.HelpIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		return input.getResponseBuilder()
				.withSpeech(new PhraseProvider(WELCOME_PHRASE, Map.of()).get())
				.withReprompt(new PhraseProvider(WELCOME_PHRASE, Map.of()).get())
				.build();
	}
}