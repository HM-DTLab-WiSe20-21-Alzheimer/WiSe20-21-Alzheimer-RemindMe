package helpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Einfaches util fuer Koordination der Zeit.
 *
 * @author Anonymous Student
 */
public class TimeUtil {
	private static final ZoneId ZONE_GERMANY = ZoneId.of("Europe/Berlin");
	private final ZonedDateTime time;

	/**
	 * Erzeigt ein neues TimeUtil Objekt mit der aktuellen Zeit.
	 */
	public TimeUtil() {
		time = Instant.now().atZone(getZoneId());
	}

	/**
	 * Erzeugt ein neues TimeUtil Objekt mit der Zeit des Zeitstempels.
	 *
	 * @param timestamp der Zeitstempel.
	 */
	public TimeUtil(long timestamp) {
		time = Instant.ofEpochSecond(timestamp).atZone(getZoneId());
	}

	/**
	 * Erzeugt ein neues TimeUtil Objekt mit der Zeit des LocalDateTime Objekts.
	 *
	 * @param dateTime die Zeit.
	 */
	public TimeUtil(LocalDateTime dateTime) {
		time = dateTime.atZone(getZoneId());
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

	/**
	 * Gibt den UNIX Zeitstempel des TimeUtil Objekts zurueck.
	 *
	 * @return der UNIX Zeitstempel.
	 */
	public long getTimestamp() {
		return time.toInstant().atZone(ZoneOffset.UTC).toEpochSecond();
	}

	/**
	 * Gibt die vom TimeUtil verwendete ZoneId wieder.
	 *
	 * @return die ZoneId.
	 */
	public static ZoneId getZoneId() {
		return ZONE_GERMANY;
	}
}
