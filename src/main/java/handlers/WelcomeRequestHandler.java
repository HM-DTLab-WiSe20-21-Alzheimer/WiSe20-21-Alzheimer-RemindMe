package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handler fuer Intent: WelcomeIntent
 * @author Anonymous Student
 */
public class WelcomeRequestHandler implements RequestHandler {
	private static final String WELCOME_PHRASE = "/Phrases/WelcomeRequest/WELCOME_PHRASE.txt";
	@Override
	public boolean canHandle(HandlerInput handlerInput) {
		return handlerInput.matches(intentName("WelcomeIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput handlerInput) {
		return handlerInput.getResponseBuilder()
				.withSpeech((new PhraseProvider(WELCOME_PHRASE, Map.of()).get()))
				.build();
	}
}