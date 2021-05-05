package handlers;

import java.util.List;

/**
 * Interface, dass eine klare Schnittstelle zwischen den Handlern und der fuer die Datenbankverwaltung zustaendige Klasse
 * darstellt.
 *
 * @author Anonymous Student
 * @author Anonymous Student
 */
public interface DynamoDBManager {

	public void store(Appointment appointment);

	public void delete(Appointment appointment);

	public List<Appointment> getAllAppointments(String userId);

}
