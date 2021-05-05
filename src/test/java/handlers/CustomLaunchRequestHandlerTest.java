package handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import junit.framework.TestCase;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Test fuer CustomLaunchRequestHandler
 * @author Anonymous Student
 */
public class CustomLaunchRequestHandlerTest {

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
    public void testCanHandle() {
        CustomLaunchRequestHandler handler = new CustomLaunchRequestHandler();
        LaunchRequest launchRequest = LaunchRequest.builder().build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder().withRequest(launchRequest).build();
        HandlerInput input = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        assertTrue(handler.canHandle(input));
    }

    @Test
    public void testCantHandle() {
        HandlerInput input = getHandlerInput("AMAZON.CancelIntent", "UserA");
        CustomLaunchRequestHandler handler = new CustomLaunchRequestHandler();
        assertFalse(handler.canHandle(input));
    }

    @Test
    public void testHandle() {
        LaunchRequest sessionEndedRequest = LaunchRequest.builder().build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder().withRequest(sessionEndedRequest).build();
        HandlerInput input = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        assertTrue(new CustomLaunchRequestHandler().handle(input).isPresent());
    }
}