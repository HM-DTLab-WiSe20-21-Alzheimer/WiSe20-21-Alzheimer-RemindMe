package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;
import helpers.TimeUtil2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handler für den ReadCurrentEntry Intent.
 * Liest alle Termine die in einer halben Stunde, in zwei Stunden,
 * und in vier Stunden stattfinden.
 *
 * @author Anonymous Student
 */
public class ReadCurrentEntryHandler implements RequestHandler {
	private static final int THIRTY_MINUTES = 30 * 60;
	private static final int ONE_HOUR = THIRTY_MINUTES * 2;
	private static final int TWO_HOURS = ONE_HOUR * 2;
	private static final int FOUR_HOURS = TWO_HOURS * 2;
	private static final int TIME_VARIATION = 5 * 60;

	private static final String PHRASE_NO_APP = "/Phrases/ReadCurrentEntry/NoAppointments.txt";
	private static final String PHRASE_ONE_NO_THINGS = "/Phrases/ReadCurrentEntry/OneAppointment.txt";
	private static final String PHRASE_ONE_WHITH_THINGS = "/Phrases/ReadCurrentEntry/OneAppointmentWithThings.txt";
	private static final String PHRASE_MULT_HEADER = "/Phrases/ReadCurrentEntry/MultipleAppointmentsHeader.txt";
	private static final String PHRASE_SINGLE_APP_WITH_THINGS = "/Phrases/ReadCurrentEntry/SingleAppointmentWithThings.txt";
	private static final String PHRASE_SINGLE_APP_NO_THINGS = "/Phrases/ReadCurrentEntry/SingleAppointmentNoThings.txt";


	@Override
	public boolean canHandle(HandlerInput input) { return input.matches(intentName("ReadCurrentEntry")); }

	@Override
	public Optional<Response> handle(HandlerInput handlerInput) {
		final String userId = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();

		final long timeNow = TimeUtil2.make().getTimestamp();
		final List<Appointment> toRemind = getAllAppointmentsAtTime(userId, timeNow);
		final String speechText = appointmentsToString(toRemind == null ? List.of() : toRemind);

		return handlerInput.getResponseBuilder()
				.withSpeech(speechText)
				.withShouldEndSession(true)
				.build();
	}

	/**
	 * Gibt eine einheitliche String Repräsentation der Termine zurück.
	 * @param appointments 	Liste mit den zu formatierenden Terminen.
	 * @return				String Repräsentation der Termine.
	 */
	private static String appointmentsToString(List<Appointment> appointments) {
		final StringBuilder builder = new StringBuilder();

		switch (appointments.size()) {
			case 0:
				builder.append(new PhraseProvider(PHRASE_NO_APP, Map.of()).get());
				break;
			case 1:
				Appointment appointment = appointments.get(0);
				final String template = appointment.getThingsToBring().isEmpty() ? PHRASE_ONE_NO_THINGS : PHRASE_ONE_WHITH_THINGS;
				builder.append(new PhraseProvider(template, appointment.getPhraseValues()).get());
				break;
			default:
				builder.append(new PhraseProvider(PHRASE_MULT_HEADER, Map.of("count", String.valueOf(appointments.size()))).get());
				for (Appointment app: appointments) {
					String phrase = app.getThingsToBring().isEmpty() ? PHRASE_SINGLE_APP_NO_THINGS: PHRASE_SINGLE_APP_WITH_THINGS;
					builder.append(System.lineSeparator())
							.append(" - ")
							.append(new PhraseProvider(phrase, app.getPhraseValues()).get());
				}
		}
		return builder.toString();
	}

	/**
	 * Sucht in allen Terminen nach Terminen, die in einer bestimmten Zeit, innerhalb einer
	 * bestimmten Zeitspanne stattfinden.
	 * @param userId		UserId des Benutzers, dessen Termine durchsucht werden.
	 * @param timeNow		Der Zeitpunkt ab dem die Zeitspanne berechnet wird.
	 * @return				Liste von Terminen die an einer bestimmten Zeit stattfinden.
	 */
	private static List<Appointment> getAllAppointmentsAtTime(String userId, long timeNow) {
		Predicate<Appointment> inThirtyMinutes 	= appointment -> isAtTime(timeNow + THIRTY_MINUTES, 	appointment.getTime());
		Predicate<Appointment> inTwoHours 		= appointment -> isAtTime(timeNow + TWO_HOURS, 		appointment.getTime());
		Predicate<Appointment> inFourHours 		= appointment -> isAtTime(timeNow + FOUR_HOURS, 		appointment.getTime());

		RemindMeDynamoDB db = RemindMeDynamoDB.getInstance();
		return db.getAllAppointments(userId).stream()
				.filter(inThirtyMinutes.or(inTwoHours).or(inFourHours))
				.sorted((appOne, appTwo) -> (int) (appOne.getTime() - appTwo.getTime()))
				.collect(Collectors.toList());
	}

	/**
	 * Ermittelt ob sich ein Zeitpunkt innerhalb einer bestimmten Zeitspanne liegt.
	 * @param time			Ein bestimmter Zeitpunkt.
	 * @param timeAt		Zeitpunkt des Termins.
	 * @return				true wenn sich der Termin innerhalb der Zeitspanne befindet.
	 */
	private static boolean isAtTime(long time, long timeAt) {
		return timeAt >= time - TIME_VARIATION && timeAt <= time + TIME_VARIATION;
	}
}
