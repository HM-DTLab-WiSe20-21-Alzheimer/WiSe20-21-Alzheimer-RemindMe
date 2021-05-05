package handlers;

import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;

public class SimpleAlexaSkillStreamHandler extends SkillStreamHandler {

	public SimpleAlexaSkillStreamHandler() {
		super(Skills.standard()
				.addRequestHandler(new WelcomeRequestHandler())
				.addRequestHandler(new CancelandStopIntentHandler())
				.addRequestHandler(new FallbackIntentHandler())
				.addRequestHandler(new HelpIntentHandler())
				.addRequestHandler(new ReadAllDayEntriesHandler())
				.addRequestHandler(new ReadNextEntryIntentHandler())
				.addRequestHandler(new ReadCurrentEntryHandler())
				.addRequestHandler(new CustomLaunchRequestHandler())
				.addRequestHandler(new AddEntryHandler())
				.addRequestHandler(new ReadAnyDayAllEntriesHandler())
				.addRequestHandler(new StartDailyReminderHandler())
				.addRequestHandler(new StopDailyReminderHandler())
				.addRequestHandler(new DeleteEntryIntentHandler())
				.withTableName("DailyReminder")
				.withAutoCreateTable(true)
				.build());
	}
}