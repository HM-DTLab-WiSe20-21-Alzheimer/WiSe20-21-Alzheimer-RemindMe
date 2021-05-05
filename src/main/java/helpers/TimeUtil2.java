package helpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

//Author Thorben Horn, and others
public class TimeUtil2 {
	public static final ZoneId ZONE_GERMANY = ZoneId.of("Europe/Berlin");
	private final ZonedDateTime time;

	private TimeUtil2(LocalDateTime localDateTime) {
		time = localDateTime.atZone(ZONE_GERMANY);
	}

	private TimeUtil2(long timestamp) {
		time = Instant.ofEpochSecond(timestamp).atZone(ZONE_GERMANY);
	}

	public static TimeUtil2 make(LocalDateTime localDateTime) {
		return new TimeUtil2(localDateTime);
	}

	public static TimeUtil2 make() {
		return new TimeUtil2(LocalDateTime.now(ZONE_GERMANY));
	}

	public static TimeUtil2 make(long timestamp) {
		return new TimeUtil2(timestamp);
	}

	public long getTimestamp() {
		return time.toInstant().atZone(ZoneOffset.UTC).toEpochSecond();
	}

	public LocalDateTime getLocalDateTime() {
		return time.toLocalDateTime();
	}

	/**
	 * Gibt den Beginn des Tages des TimeUtil Objekts als UNIX Zeitstempel zurueck.
	 *
	 * @return der UNIX Zeitstempel.
	 */
	public long getStartOfDay() {
		return time.toLocalDate().atStartOfDay(ZONE_GERMANY).toEpochSecond();
	}

	/**
	 * Gibt die gespeicherte Uhrzeit des TimeUtils im ISO Format zurueck.
	 *
	 * @return die Uhrzeit im ISO Format.
	 */
	public String getTime() {
		return DateTimeFormatter.ISO_LOCAL_TIME.format(time);
	}

	/**
	 * Gibt das gespeichete Datum des TimeUtils im ISO Format zurueck.
	 *
	 * @return das Datum im ISO Format.
	 */
	public String getDate() {
		return DateTimeFormatter.ISO_LOCAL_DATE.format(time);
	}
}
