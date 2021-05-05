package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import junit.framework.TestCase;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test fuer: WelcomeRequestHandler
 * @author Anonymous Student
 */
public class WelcomeRequestHandlerTest extends TestCase {

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
        HandlerInput input = getHandlerInput("WelcomeIntent", "UserA");
        WelcomeRequestHandler handler = new WelcomeRequestHandler();
        assertTrue(handler.canHandle(input));
    }

    public void testCantHandle() {
        HandlerInput input = getHandlerInput("AMAZON.StopIntent", "UserA");
        WelcomeRequestHandler handler = new WelcomeRequestHandler();
        assertFalse(handler.canHandle(input));
    }

    public void testHandle() {
        HandlerInput input = getHandlerInput("AMAZON.WelcomeIntent", "userA");
        String want = "Hi, ich bin <lang xml:lang=\"en-US\">Remind-Me</lang>. Ich helfe dir deine Ereignisse zu notieren und erinnere dich rechtzeitig an diese.";
        String have = getSpeechValue(new WelcomeRequestHandler().handle(input));
        assertEquals(want, have);
    }
}