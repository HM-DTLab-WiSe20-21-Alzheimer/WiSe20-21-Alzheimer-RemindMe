package handlers;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.junit.Test;
import sun.misc.Unsafe;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class RemindMeDynamoDBTest {
	private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
		final var f = Unsafe.class.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		return (Unsafe)f.get(null);
	}

	@Test
	public void test1() throws InstantiationException, NoSuchFieldException, IllegalAccessException {
		final var sut = (RemindMeDynamoDB) getUnsafe().allocateInstance(RemindMeDynamoDB.class);
		final var mockDB = spy(sut);
		final var mockMapper = mock(DynamoDBMapper.class);
		final var field = mockDB.getClass().getDeclaredField("dbMapper");
		field.setAccessible(true);
		field.set(mockDB, mockMapper);
		mockDB.getAllAppointments("");
		verify(mockMapper).query(any(), any());
	}

	@Test
	public void test2() throws InstantiationException, NoSuchFieldException, IllegalAccessException {
		final var sut = (RemindMeDynamoDB) getUnsafe().allocateInstance(RemindMeDynamoDB.class);
		final var mockDB = spy(sut);
		final var mockMapper = mock(DynamoDBMapper.class);
		final var field = mockDB.getClass().getDeclaredField("dbMapper");
		field.setAccessible(true);
		field.set(mockDB, mockMapper);
		mockDB.store(null);
		verify(mockMapper).save(any());
	}

}