package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import junit.framework.TestCase;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test fuer: FallbackIntentHandler
 * @author Anonymous Student
 */
public class FallbackIntentHandlerTest extends TestCase {

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

    public void testCanHandle() {
        HandlerInput input = getHandlerInput("AMAZON.FallbackIntent", "UserA");
        FallbackIntentHandler handler = new FallbackIntentHandler();
        assertTrue(handler.canHandle(input));
    }

    public void testCantHandle() {
        HandlerInput input = getHandlerInput("AMAZON.StopIntent", "UserA");
        FallbackIntentHandler handler = new FallbackIntentHandler();
        assertFalse(handler.canHandle(input));
    }

    public void testHandle() {
        HandlerInput input = getHandlerInput("AMAZON.FallbackIntent", "userA");
        String want = "Tut mir Leid, das weiss ich leider nicht.";
        String have = getSpeechValue(new FallbackIntentHandler().handle(input));
        assertEquals(want, have);
    }
}