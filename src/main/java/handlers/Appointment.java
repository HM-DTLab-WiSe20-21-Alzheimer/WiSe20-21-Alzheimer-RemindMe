package handlers;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import helpers.TimeUtil2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Kapselt alle fuer ein Appointment wichtigen Informationen und entspricht damit genau einer Abbildung eines Eintrages
 * der RemindMeV2 Tabelle. Appointment kann daher mit einem DynamoDBMapper zum Speichern und Abfragen der Datenbank
 * genutzt werden.
 *
 * @author Anonymous Student
 * @author Anonymous Student
 */
@DynamoDBTable(tableName = "RemindMeV2")
public class Appointment {
	private long time;
	private String userId;
	private String content;
	private String thingsToBring;
	private List<String> alertTokens;
	private long expirationDate;

	private static final String TIME_KEY = "time";
	private static final String CONTENT_KEY = "content";
	private static final String THINGS_TO_BRING_KEY = "thingsToBring";
	private static final String DATE_KEY = "date";

	public Appointment() {
	}

	public Appointment(long time, String userId, String content, String thingsToBring, List<String> alertTokens, long expirationDate) {
		this.time = time;
		this.userId = userId;
		this.content = content;
		this.thingsToBring = thingsToBring == null ? "" : thingsToBring;
		this.alertTokens = alertTokens;
		this.expirationDate = expirationDate;
	}

	@DynamoDBIgnore
	public Map<String, String> getPhraseValues() {
		TimeUtil2 timeUtil = TimeUtil2.make(getTime());
		HashMap<String, String> map = new HashMap<>();
		map.put(TIME_KEY, timeUtil.getTime());
		map.put(CONTENT_KEY, getContent());
		map.put(THINGS_TO_BRING_KEY, getThingsToBring());
		map.put(DATE_KEY, timeUtil.getDate());
		return map;
	}

	@DynamoDBHashKey(attributeName = "Time")
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@DynamoDBAttribute(attributeName = "Content")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@DynamoDBAttribute(attributeName = "ThingsToBring")
	public String getThingsToBring() {
		return thingsToBring == null ? "" : thingsToBring;
	}

	public void setThingsToBring(String thingsToBring) {
		this.thingsToBring = thingsToBring == null ? "" : thingsToBring;
	}

	@DynamoDBHashKey(attributeName = "UserId")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@DynamoDBAttribute(attributeName = "AlertTokens")
	public List<String> getAlertTokens() {
		return alertTokens;
	}

	public void setAlertTokens(List<String> alertTokens) {
		this.alertTokens = alertTokens;
	}

	@DynamoDBAttribute(attributeName = "ExpirationDate")
	public long getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(long expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Appointment that = (Appointment) o;
		return getTime() == that.getTime() &&
				getExpirationDate() == that.getExpirationDate() &&
				Objects.equals(getUserId(), that.getUserId()) &&
				Objects.equals(getContent(), that.getContent()) &&
				Objects.equals(getThingsToBring(), that.getThingsToBring()) &&
				Objects.equals(getAlertTokens(), that.getAlertTokens());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getTime(), getUserId(), getContent(), getThingsToBring(), getAlertTokens(), getExpirationDate());
	}
}
