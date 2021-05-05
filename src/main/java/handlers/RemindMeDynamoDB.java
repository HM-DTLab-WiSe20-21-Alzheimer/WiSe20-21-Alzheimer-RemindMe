package handlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import java.util.List;

/**
 * Eine Klasse deren Object standard methoden zum interagieren mit der DynamoDB Datenbank zur verfügung stellt.
 *
 * @author Anonymous Student
 * @author Anonymous Student
 */
public class RemindMeDynamoDB implements DynamoDBManager {
	private static RemindMeDynamoDB instance;
	private final DynamoDBMapper dbMapper;

	private RemindMeDynamoDB() {
		final var dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
		this.dbMapper = new DynamoDBMapper(dynamoDB);
	}

	private DynamoDBMapper getDbMapper() {
		return this.dbMapper;
	}

	@Override
	public void store(Appointment appointment) {
		getDbMapper().save(appointment);
	}

	@Override
	public void delete(Appointment appointment) {
		getDbMapper().delete(appointment);
	}

	@Override
	public List<Appointment> getAllAppointments(String userId) {
		final var queryFilter = new Appointment();
		queryFilter.setUserId(userId);

		final var query = new DynamoDBQueryExpression<Appointment>()
				.withHashKeyValues(queryFilter);
		return getDbMapper().query(Appointment.class, query);
	}

	public static RemindMeDynamoDB getInstance() {
		if(instance == null) {
			instance = new RemindMeDynamoDB();
		}
		return instance;
	}
}