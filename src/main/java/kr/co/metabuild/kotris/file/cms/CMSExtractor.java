package kr.co.metabuild.kotris.file.cms;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ByteBuf를 입력받아 전문의 필드내용을 String 형태로 추출한다.
 * @author Formulate
 *
 *
 */
public class CMSExtractor {

    private static final Logger logger = LoggerFactory.getLogger(CMSExtractor.class);

    public static String extractMessageId(ByteBuf bb) {
        int offset;
        int length;
        return null;
    }
}
