package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;
import org.apache.logging.log4j.util.Strings;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Der Handler fuer den ReadNextEntry Intent.
 * Liest den naechsten Termin vor falls einer existiert.
 * Falls nicht wird eine entsprechende Nachricht wiedergegeben.
 *
 * @author Anonymous Student
 */
public class ReadNextEntryIntentHandler implements RequestHandler {
	private static final String PHRASE_NO_ENTRY = "/Phrases/ReadNextEntry/ReadNextEntryPhraseNoEntry.txt";
	private static final String PHRASE_DEFAULT = "/Phrases/ReadNextEntry/ReadNextEntryPhraseDefault.txt";
	private static final String PHRASE_THINGS_TO_BRING = "/Phrases/ReadNextEntry/ReadNextEntryPhraseDefaultThingsToBring.txt";
	private final DynamoDBManager dynamoDB = RemindMeDynamoDB.getInstance();

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ReadNextEntry"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		final var userId = input.getRequestEnvelope().getSession().getUser().getUserId();
		final var appointments = dynamoDB.getAllAppointments(userId);
		final var phrase = processAppointments(appointments);
		return input.getResponseBuilder()
				.withSpeech(phrase)
				.withShouldEndSession(true)
				.build();
	}

	static String processAppointments(List<Appointment> appointments) {
		final var phrasePath = appointments.isEmpty() ? PHRASE_NO_ENTRY : PHRASE_DEFAULT;
		final var nextAppointment = appointments.stream()
				.filter(x -> x.getTime() >= Instant.now().getEpochSecond())
				.min(Comparator.comparingLong(Appointment::getTime))
				.orElse(new Appointment());

		final var replacements = nextAppointment.getPhraseValues();
		final var phrase = new PhraseProvider(phrasePath, replacements).get();
		final var builder = new StringBuilder(phrase);
		if(!Strings.isBlank(nextAppointment.getThingsToBring())) {
			final var thingsToBring = new PhraseProvider(PHRASE_THINGS_TO_BRING, replacements).get();
			builder.append(" ").append(thingsToBring);		}
		return builder.toString();
	}
}
