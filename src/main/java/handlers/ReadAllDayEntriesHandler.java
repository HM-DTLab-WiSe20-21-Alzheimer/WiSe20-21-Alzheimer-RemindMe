package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.TimeUtil2;

import java.util.List;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handler fuer Intent: ReadAllDayEntries
 * Gibt alle Termine aus, die ab dem jetzigen Zeitpunkt
 * bis zum Ende des Tages noch anstehen
 *
 * @author Anonymous Student
 * @author Anonymous Student
 */
public class ReadAllDayEntriesHandler implements RequestHandler {

	private static final String NO_APPOINTMENTS = "/Phrases/ReadAllDayEntries/None";
	private static final String ONE_APP_WITHOUT_THINGS = "/Phrases/ReadAllDayEntries/OneNoThings";
	private static final String ONE_APP_WITH_THINGS = "/Phrases/ReadAllDayEntries/OneWithThings";
	private static final String MULTIPLE_APP_HEADER = "/Phrases/ReadAllDayEntries/MultipleHeader";
	private static final String SINGLE_APP_WHITHOUT_THINGS = "/Phrases/ReadAllDayEntries/SingleAppointmentNoThings";
	private static final String SINGLE_APP_WITH_THINGS = "/Phrases/ReadAllDayEntries/SingleAppointmentWithThings";

	@Override
	public boolean canHandle(HandlerInput handlerInput) {
		return handlerInput.matches(intentName("ReadAllDayEntries"));
	}

	@Override
	public Optional<Response> handle(HandlerInput handlerInput) {

		String userId = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
		String speechText = processInput(userId);

		return handlerInput.getResponseBuilder()
				.withSpeech(speechText)
				.withShouldEndSession(true)
				.build();
	}

	/**
	 * Gibt zu einer UserId die entsprechenden heute noch anstehenden Termine formatiert als "Speech" Text zurueck.
	 *
	 * @param userId Id des Users dessen Termine zu formatieren sind.
	 * @return Formatierter String mit Terminen.
	 */
	private static String processInput(String userId) {
		final long timeNow = TimeUtil2.make().getTimestamp();
		final long startOfDay = TimeUtil2.make().getStartOfDay();
		final long startOfNextDay = startOfDay + (24 * 60 * 60);

		return getAppointmentsFormatted(ReadAnyDayAllEntriesHandler.getAppointmentList(userId, timeNow, startOfNextDay));
	}

	/**
	 * Gibt die Appointments als String in einem, fuer unseren Skill einheitlich, festgeleten Format zurueck.
	 *
	 * @param appointments List mit den zu formatierenden Appointments.
	 * @return Formetierter String.
	 */
	 static String getAppointmentsFormatted(List<Appointment> appointments) {
		 return ReadAnyDayAllEntriesHandler.getString(appointments, NO_APPOINTMENTS, ONE_APP_WITHOUT_THINGS, ONE_APP_WITH_THINGS, MULTIPLE_APP_HEADER, SINGLE_APP_WITH_THINGS, SINGLE_APP_WHITHOUT_THINGS);
	 }


}
