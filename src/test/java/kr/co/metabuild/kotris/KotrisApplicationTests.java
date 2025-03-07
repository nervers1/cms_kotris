package kr.co.metabuild.kotris;

import kr.co.metabuild.kotris.util.CMSUtil;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KotrisApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(KotrisApplicationTests.class);


    @Test
    void contextLoads() {
    }

    @Test
    void printProperties() {
        String property = CMSUtil.getProperty("cms.remoteHost");
        System.out.println(property);
    }


}
