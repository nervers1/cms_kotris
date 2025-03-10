package kr.co.metabuild.kotris.file.cms;

import io.netty.buffer.ByteBuf;
import kr.co.metabuild.kotris.util.ByteUtil;
import kr.co.metabuild.kotris.util.CMSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ByteBuf를 입력받아 전문의 필드내용을 String 형태로 추출한다.
 * @author Formulate
 *
 *
 */
public class CMSExtractor {

    private static final Logger logger = LoggerFactory.getLogger(CMSExtractor.class);

    // 전체 cms.properties 를 읽어서
    private static final Map<String, Object> cms = CMSUtil.getMetaProp("cms");
    // extrator 부분만 목록으로 추출한다.
    private static final List<Map<String, Object>> fields = CMSUtil.getFieldList(cms, "extractor");
    // 목록에서 필드 정보를 추출하여 Map형태로 반환한다.
    private static Map<String, Object> getFieldMap(List<Map<String, Object>> fields, String fieldName) {
        AtomicReference<Map<String, Object>> result = new AtomicReference<>();
        fields.forEach(field -> {
            String nm = (String)field.get("name");
            if (fieldName.equalsIgnoreCase(nm)) {
                result.set(field);
            }
        });
        return result.get();
    }

    public static String extractMessageId (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "messageId");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractString(bb, offset, length);
    }

    public static String extractWorkCode (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "workCode");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractString(bb, offset, length);
    }

    public static String extractResponseCode (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "responseCode");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractString(bb, offset, length);
    }
    public static String extractWorkAdminInfo (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "workAdminInfo");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractString(bb, offset, length);
    }
    public static String extractFileInquiryResultCode (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "fileInquiryResultCode");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractString(bb, offset, length);
    }
    public static String extractFileCancelResultCode (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "fileCancelResultCode");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractString(bb, offset, length);
    }
    public static String extractFileName (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "fileName");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractString(bb, offset, length);
    }
    public static String extractFileInfoFileName (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "fileInfoFileName");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractString(bb, offset, length);
    }
    public static long extractFileInfoFileSize (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "fileInfoFileSize");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractLong(bb, offset, length);
    }
    public static int extractFileInfoDataFrame (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "fileInfoDataFrame");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractInt(bb, offset, length);
    }
    public static int extractDataSendSeq (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "dataSendSeq");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractInt(bb, offset, length);
    }
    public static int extractDataSendBlockNo (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "dataSendBlockNo");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractInt(bb, offset, length);
    }
    public static byte[] extractDataSendData (ByteBuf bb) {
        Map<String, Object> fieldMap = getFieldMap(fields, "dataSendData");
        int offset = Integer.parseInt((String)fieldMap.get("offset"));
        int length = Integer.parseInt((String)fieldMap.get("length"));
        return ByteUtil.extractBytes(bb, offset, length);
    }
}
