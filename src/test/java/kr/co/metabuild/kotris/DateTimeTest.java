package kr.co.metabuild.kotris;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTest {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeTest.class);
    @Test
    public void testDateTime() {
        String sendDataStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        logger.debug(sendDataStr); // 0306131309

    }
}
