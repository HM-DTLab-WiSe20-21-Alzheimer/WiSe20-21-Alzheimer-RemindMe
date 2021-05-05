package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handler fuer Intent: AMAZON.StopIntent und AMAZON.CancelIntent
 * @author Anonymous Student
 */
public class CancelandStopIntentHandler implements RequestHandler {
	private static final String SORRY_PHRASE ="/Phrases/CancelandStopIntent/SORRY_PHRASE";

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.StopIntent").or(intentName("AMAZON.CancelIntent")));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		return input.getResponseBuilder()
				.withSpeech(new PhraseProvider(SORRY_PHRASE, Map.of()).get())
				.withShouldEndSession(true)
				.build();
	}
}