package kr.co.metabuild.kotris;

import kr.co.metabuild.kotris.util.ByteUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(ByteUtilTest.class);

    @Test
    public void testPad() {
        String a = "한글";
        logger.debug("[{}], {}", a, a.length);
        byte[] b = ByteUtil.setString(a, 10);
        logger.debug("[{}], {}", new String(b), b.length);
        byte[] c = ByteUtil.setString(a, 10);
        logger.debug("[{}], {}", new String(c), c.length);

        byte[] num1 = ByteUtil.setNumber(a, 10);
        logger.debug("[{}], {}", new String(num1), num1.length);

        String a1 = "한글한글한글한글한글한글";
        String a2 = "1234567890";
        logger.debug("[{}], {}", a1, a1.length);
        logger.debug("-----");
        byte[] b1 = ByteUtil.setString(a1, 10);
        logger.debug("[{}], {}", new String(b1), b1.length);
        byte[] b9 = ByteUtil.setString(a1, 40);
        logger.debug("[{}], {}", new String(b9), b9.length);
        byte[] b2 = ByteUtil.setNumber(a1, 40);
        logger.debug("[{}], {}", new String(b2), b2.length);
        byte[] b3 = ByteUtil.setNumber(a2, 40);
        logger.debug("[{}], {}", new String(b3), b3.length);
        byte[] b4 = ByteUtil.setString(a2, 40);
        logger.debug("[{}], {}", new String(b4), b4.length);
        byte[] b5 = ByteUtil.setString(a2, 9);
        logger.debug("[{}], {}", new String(b5), b5.length);

    }
}
