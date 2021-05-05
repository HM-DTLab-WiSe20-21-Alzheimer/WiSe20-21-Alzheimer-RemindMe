package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.LaunchRequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.Image;
import com.amazon.ask.model.ui.StandardCard;
import helpers.PhraseProvider;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.requestType;

/**
 * CustomLaunchRequestHandler
 * @author Anonymous Student
 */
public class CustomLaunchRequestHandler implements LaunchRequestHandler {
	private static final String LAUNCH_REQUEST_CARD_TITLE = "/Phrases/CustomLaunchRequestHandler/LAUNCH_REQUEST_CARD_TITLE_TEXT.txt";
	private static final String LAUNCH_REQUEST_CARD_TEXT = "/Phrases/CustomLaunchRequestHandler/LAUNCH_REQUEST_CARD_TEXT_TEXT.txt";
	private static final String LAUNCH_REQUEST_CARD_IMAGE = "/Phrases/CustomLaunchRequestHandler/LAUNCH_REQUEST_CARD_IMAGE_TEXT.txt";
	private static final String LAUNCH_REQUEST_PHRASE = "/Phrases/CustomLaunchRequestHandler/LAUNCH_REQUEST_PHRASE_TEXT.txt";


	@Override
	public boolean canHandle(HandlerInput input, LaunchRequest launchRequest) {
		return input.matches(requestType(LaunchRequest.class));
	}

	@Override
	public Optional<Response> handle(HandlerInput input, LaunchRequest launchRequest) {
		Image image = Image.builder().withLargeImageUrl(new PhraseProvider(LAUNCH_REQUEST_CARD_IMAGE, Map.of()).get()).build();
		StandardCard standardCard = StandardCard.builder()
				.withTitle(new PhraseProvider(LAUNCH_REQUEST_CARD_TITLE, Map.of()).get())
				.withText(new PhraseProvider(LAUNCH_REQUEST_CARD_TEXT, Map.of()).get())
				.withImage(image)
				.build();
		return input.getResponseBuilder()
				.withSpeech(new PhraseProvider(LAUNCH_REQUEST_PHRASE, Map.of()).get())
				.withCard(standardCard)
				.build();
	}
}