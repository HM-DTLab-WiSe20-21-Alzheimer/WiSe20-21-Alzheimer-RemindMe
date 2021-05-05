package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import org.junit.Test;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Test fuer: CancelandStopIntentHandler
 * @author Anonymous Student
 */
public class CancelandStopIntentHandlerTest {

    private static HandlerInput getHandlerInput(String intentName, String userId) {
        final var intent = Intent.builder().withName(intentName).build();
        final var request = IntentRequest.builder().withIntent(intent).build();
        final var user = User.builder().withUserId(userId).build();
        final var session = Session.builder().withUser(user).build();
        final var envelope = RequestEnvelope.builder().withSession(session).withRequest(request).build();
        return HandlerInput.builder().withRequestEnvelope(envelope).build();
    }

    private static String getSpeechValue(Optional<Response> responseOptional) {
        assertTrue(responseOptional.isPresent());
        Response response = responseOptional.get();
        Pattern pattern = Pattern.compile("<speak>.*</speak>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response.getOutputSpeech().toString());
        assertTrue(matcher.find());
        String speechText = matcher.group(0).replaceAll("\\R( {4})|\\t", "\n");
        return speechText.substring(7, speechText.length() - 8);
    }

    @Test
    public void testCanHandleStopIntent() {
        HandlerInput input = getHandlerInput("AMAZON.StopIntent", "UserA");
        CancelandStopIntentHandler handler = new CancelandStopIntentHandler();
        assertTrue(handler.canHandle(input));
    }

    @Test
    public void testCanHandleCancelIntent() {
        HandlerInput input = getHandlerInput("AMAZON.CancelIntent", "UserA");
        CancelandStopIntentHandler handler = new CancelandStopIntentHandler();
        assertTrue(handler.canHandle(input));
    }

    @Test
    public void testCantHandle() {
        HandlerInput input = getHandlerInput("AMAZON.FallbackIntent", "UserA");
        CancelandStopIntentHandler handler = new CancelandStopIntentHandler();
        assertFalse(handler.canHandle(input));
    }

    @Test
    public void testHandleStopIntent() {
        HandlerInput input = getHandlerInput("AMAZON.StopIntent", "userA");
        String want = "Tut mir Leid, das weiss ich leider nicht.";
        String have = getSpeechValue(new CancelandStopIntentHandler().handle(input));
        assertEquals(want, have);
    }

    @Test
    public void testHandleCancelIntent() {
        HandlerInput input = getHandlerInput("AMAZON.CancelIntent", "userA");
        String want = "Tut mir Leid, das weiss ich leider nicht.";
        String have = getSpeechValue(new CancelandStopIntentHandler().handle(input));
        assertEquals(want, have);
    }



}