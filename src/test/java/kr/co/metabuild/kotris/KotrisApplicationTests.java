package kr.co.metabuild.kotris;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kr.co.metabuild.kotris.file.cms.CMSExtractor;
import kr.co.metabuild.kotris.util.CMSUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static kr.co.metabuild.kotris.util.CMSUtil.getFieldList;
import static kr.co.metabuild.kotris.util.CMSUtil.getMetaProp;

@Slf4j
@SpringBootTest
class KotrisApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(KotrisApplicationTests.class);


    @Test
    void contextLoads() {
    }

    @Test
    void printProperties() {
        String property = CMSUtil.getProperty("IFCOMM.name"); // 전문공통부 of properties/cms.properties
        System.out.println(property);
    }


    @Test
    void testProperties() {
        ByteBuf buf = Unpooled.buffer();
        String msgId = CMSExtractor.extractMessageId(buf);
    }

    @Test
    void printEnvironment() {
        CMSUtil.printEnv();
    }
    @Test
    void printProp() {
        CMSUtil.printProp("cms.remoteHost");
    }

    @Test
    public void PropertyUtilTest() {

        // 메타정보위치에서 프로퍼티 파일을 읽어온다 ( resources/meta/cms.properties 파일을 읽는 경우 파일명 "test"를 입력한다.
        String path = "cms";
        Map<String, Object> info = getMetaProp(path);

        // 전문정보를 추출한다.
        log.debug("============================[전문정보추출]================================================");
        info.forEach((k, v) -> {
            log.debug("ID: {} --> : {}", k, v);

        });

        // 메타정보를 맵 형태로 읽어온 경우 해당 필드 목록을 맵에서 추출한다.
        List<Map<String, Object>> if0310 = getFieldList(info, "IF0310");
        log.debug("============================[리스트를 출력한다]================================================");

        // 추출된 맵의 리스트를 출력한다.
        if0310.forEach(field -> {
            field.forEach((k, v) -> {
                log.debug("ID: {} --> : {}", k, v);
            });
        });

        log.debug("============================[Prop]================================================");
        Properties if0600 = CMSUtil.getInterfaceProp("if0600");
        log.debug(if0600.toString());

    }

}
