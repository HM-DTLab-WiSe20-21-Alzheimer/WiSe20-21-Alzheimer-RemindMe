package helpers;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//Autor: Thorben Horn
public class TimeUtil2Test {
    //timestamps manually calculated/verified using https://www.epochconverter.com/

    private final static long TEST_EPOCH = 1609830000; // 2021-01-05, 08:00:00 Uhr DE
    private final static LocalDateTime TEST_LOCAL_DATE_TIME = LocalDateTime.of(2021,1,5,8,0);
    private final static String TEST_FORMATTED_DATE = "2021-01-05";
    private final static String TEST_FORMATTED_TIME = "08:00:00";
    private final static long TEST_START_OF_DAY_EPOCH = 1609801200; // 2021-01-05, 00:00:00 Uhr DE


    @Test
    public void testTimestampFromLocalDateTime() {
        TimeUtil2 timeUtil2 = TimeUtil2.make(TEST_LOCAL_DATE_TIME);
        assertEquals(TEST_EPOCH, timeUtil2.getTimestamp());
    }

    @Test
    public void testLocalDateTimeFromTimestamp() {
        TimeUtil2 timeUtil2 = TimeUtil2.make(TEST_EPOCH);
        assertEquals(TEST_LOCAL_DATE_TIME, timeUtil2.getLocalDateTime());
    }

    @Test
    public void testFormattedDateFromLocalDateTime() {
        TimeUtil2 timeUtil2 = TimeUtil2.make(TEST_EPOCH);
        assertEquals(TEST_FORMATTED_DATE, timeUtil2.getDate());
    }

    @Test
    public void testFormattedTimeFromLocalDateTime() {
        TimeUtil2 timeUtil2 = TimeUtil2.make(TEST_EPOCH);
        assertEquals(TEST_FORMATTED_TIME, timeUtil2.getTime());
    }

    @Test
    public void testStartOfDayTimestampFromLocalDateTimes() {
        for(int i = 0; i < 24; i++){
            TimeUtil2 timeUtil2 = TimeUtil2.make(TEST_LOCAL_DATE_TIME.withHour(i));
            assertEquals(TEST_START_OF_DAY_EPOCH, timeUtil2.getStartOfDay());
        }
    }

    @Test
    public void testTimestampFromLocalDateTimeNow() {
        long actual = TimeUtil2.make().getTimestamp()*1000;
        long expected = new Date().getTime();
        assertTrue(Math.abs(expected-actual) < 2000);
    }
}
