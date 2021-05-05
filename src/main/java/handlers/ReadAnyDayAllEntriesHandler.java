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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handler fuer Intent: ReadAnyDayAllEntries
 * Gibt alle Termine aus, die an einem
 * bestimmten Tag anstehen bzw. waren
 *
 * @author Anonymous Student
 */
public class ReadAnyDayAllEntriesHandler implements RequestHandler {

    private static final String PHRASE_NONE = "/Phrases/ReadAnyDayAllEntries/None";
    private static final String PHRASE_ONE_NO_THINGS = "/Phrases/ReadAnyDayAllEntries/OneNoThings";
    private static final String PHRASE_ONE_WITH_THINGS = "/Phrases/ReadAnyDayAllEntries/OneWithThings";
    private static final String PHRASE_MULTIPLE_HEADER = "/Phrases/ReadAnyDayAllEntries/MultipleHeader";
    private static final String PHRASE_SINGLE_APPOINTMENT_NO_THINGS = "/Phrases/ReadAnyDayAllEntries/SingleAppointmentNoThings";
    private static final String PHRASE_SINGLE_APPOINTMENT_WITH_THINGS = "/Phrases/ReadAnyDayAllEntries/SingleAppointmentWithThings";

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(intentName("ReadAnyDayAllEntries"));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        String userId = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        String date = ReminderSetter.getAttribute(handlerInput, "date");

        String speechText = processInput(userId, date);

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withShouldEndSession(true)
                .build();
    }

    private static String processInput(String userId, String date) {
        LocalDateTime startTime = buildDateTime(date);
        TimeUtil2 time = TimeUtil2.make(startTime);
        long startDayEpoch = time.getTimestamp();
        long endDayEpoch = TimeUtil2.make(startTime.plusDays(1)).getTimestamp();

        List<Appointment> appointments = getAppointmentList(userId, startDayEpoch, endDayEpoch);

        return getAppointmentsFormatted(appointments);
    }

    static List<Appointment> getAppointmentList(String userId, long startDayEpoch, long endDayEpoch) {
        RemindMeDynamoDB dynamoDB = RemindMeDynamoDB.getInstance();
        return dynamoDB.getAllAppointments(userId).stream()
                .filter(appointment -> appointment.getTime() >= startDayEpoch)
                .filter(appointment -> appointment.getTime() < endDayEpoch)
                .sorted((one, other) -> (int) (one.getTime() - other.getTime()))
                .collect(Collectors.toList());
    }


    private static LocalDateTime buildDateTime(String date) {
        LocalDate scheduledDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalTime scheduledTime = LocalTime.parse("00:00:00", DateTimeFormatter.ISO_LOCAL_TIME);
        return scheduledDate.atTime(scheduledTime);
    }

    /**
     * Gibt die Appointments als String in einem, fuer unseren Skill einheitlich, festgeleten Format zurueck.
     *
     * @param appointments List mit den zu formatierenden Appointments.
     * @return Formetierter String.
     */
    static String getAppointmentsFormatted(List<Appointment> appointments) {
        return getString(appointments, PHRASE_NONE, PHRASE_ONE_NO_THINGS, PHRASE_ONE_WITH_THINGS, PHRASE_MULTIPLE_HEADER, PHRASE_SINGLE_APPOINTMENT_WITH_THINGS, PHRASE_SINGLE_APPOINTMENT_NO_THINGS);
    }

    static String getString(List<Appointment> appointments, String phraseNone, String phraseOneNoThings, String phraseOneWithThings, String phraseMultipleHeader, String phraseSingleAppointmentWithThings, String phraseSingleAppointmentNoThings) {
        StringBuilder builder = new StringBuilder();
        if (appointments.isEmpty()) {
            builder.append(new PhraseProvider(phraseNone, Map.of()).get());
        } else if (appointments.size() == 1) {
            final Appointment appointment = appointments.get(0);
            if (appointment.getThingsToBring().isEmpty())
                builder.append(new PhraseProvider(phraseOneNoThings, appointment.getPhraseValues()).get());
            else
                builder.append(new PhraseProvider(phraseOneWithThings, appointment.getPhraseValues()).get());
        } else {
            builder.append(new PhraseProvider(phraseMultipleHeader, Map.of("count", String.valueOf(appointments.size()))).get());
            for (Appointment elem : appointments) {
                String phrase = phraseSingleAppointmentWithThings;
                if (elem.getThingsToBring().isEmpty())
                    phrase = phraseSingleAppointmentNoThings;
                builder.append(System.lineSeparator()).append(" - ")
                        .append(new PhraseProvider(phrase, elem.getPhraseValues()).get());
            }
        }
        return builder.toString();
    }
}

