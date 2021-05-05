package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import helpers.PhraseProvider;
import helpers.ReminderSetter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Der Handler für den DeletEntryIntent.
 * Sucht nach einem Ereignis und entfernt ihn falls er existiert.
 *
 * @author Anonymous Student
 */
public class DeleteEntryIntentHandler implements RequestHandler {
    private static final String APPOINTMENT_NOT_FOUND = "/Phrases/DeleteEntry/APPOINTMENT_NOT_FOUND.txt";
    private static final String MULTIPLE_APPOINTMENTS_FOUND = "/Phrases/DeleteEntry/MULTIPLE_APPOINTMENTS_FOUND.txt";
    private static final String DELETE_APPOINTMENT = "/Phrases/DeleteEntry/DELETE_APPOINTMENT.txt";
    private static final RemindMeDynamoDB db = RemindMeDynamoDB.getInstance();

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("DeleteEntry"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        Optional<Response> permission = ReminderSetter.getPermission(input);
        if(permission.isPresent()){
            return permission;
        }

        String content = ReminderSetter.getAttribute(input, "content");
        String date = ReminderSetter.getAttribute(input, "date");
        String time = ReminderSetter.getAttribute(input, "time");

        String userId = input.getRequestEnvelope().getSession().getUser().getUserId();

        List<Appointment> appointments;
        LocalDateTime scheduledTime;

        scheduledTime = getLocalDateTime(date, time);
        appointments = getAppointmentsAtTime(db.getAllAppointments(userId), scheduledTime, content);

        String speechtext;
        switch (appointments.size()) {
            case 0:
                speechtext = new PhraseProvider(APPOINTMENT_NOT_FOUND, Map.of()).get();
                break;
            case 1:
                Appointment toDelete = appointments.get(0);

                for (String alertToken : toDelete.getAlertTokens()) {
                    if(alertToken != null){
                        ReminderSetter.deleteReminder(input, alertToken);
                    }
                }

                var phraseValues = toDelete.getPhraseValues();

                db.delete(toDelete);

                speechtext = new PhraseProvider(DELETE_APPOINTMENT, phraseValues).get();
                break;
            default:
                speechtext = new PhraseProvider(MULTIPLE_APPOINTMENTS_FOUND, Map.of()).get();
                break;
        }

        return input.getResponseBuilder()
                .withSpeech(speechtext)
                .withShouldEndSession(true)
                .build();
    }

    private List<Appointment> getAppointmentsAtTime(List<Appointment> appointments, LocalDateTime time, String content) {
        long timeEpoch = time.atZone(ZoneId.of("Europe/Berlin")).toInstant().getEpochSecond();

        return appointments.stream()
                .filter(appointment -> appointment.getTime() == timeEpoch)
                .filter(appointment -> appointment.getContent().equals(content))
                .collect(Collectors.toList());
    }

    private LocalDateTime getLocalDateTime(String date, String time) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        return localDate.atTime(LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME));
    }
}
