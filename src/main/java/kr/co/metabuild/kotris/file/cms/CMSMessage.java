package kr.co.metabuild.kotris.file.cms;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kr.co.metabuild.kotris.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Formulate
 */
public class CMSMessage {
    private static final Logger logger = LoggerFactory.getLogger(CMSMessage.class);


    /**
     * 공통필드 생성
     *
     * @param msgid 메시지ID
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param resultCode 결과코드
     * @return 전문 공통부 byte[]
     */
    public static byte[] makeCommonField(String msgid, String orgCode, String srCode, String fileName, String resultCode) {
        String msg = Arrays.asList(ByteUtil.setNumber("", 4), ByteUtil.setString("FTE", 3), ByteUtil.setString(orgCode, 8)
                , ByteUtil.setNumber(msgid, 4), ByteUtil.setString(srCode, 1), ByteUtil.setString("E", 1), ByteUtil.setString(fileName, 8)
                , ByteUtil.setString(resultCode, 3)).toString();
        return msg.getBytes();
    }

    /**
     * 전문 길이부 설정
     * @param buff
     */
    private static void writeLength(ByteBuf buff) {
        long size = buff.readableBytes() - 4;
        buff.setBytes(0, ByteUtil.setNumber(size+"", 4));
    }

    /**
     * 업무개시요구(0600/001)
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param userId 사용자ID
     * @param password 비밀번호
     * @param sendDateStr 전송일자
     * @return
     */
    public static ByteBuf workStartRequest(String orgCode, String srCode, String fileName, String userId, String password, String sendDateStr) {
        ByteBuf sendData = Unpooled.buffer();
        sendData.writeBytes(makeCommonField("0600", orgCode, srCode, fileName, "000"));
        sendData.writeBytes(ByteUtil.setString(sendDateStr, 10)); // 전문전송일시 AN
        sendData.writeBytes(ByteUtil.setString("001", 3)); // 업무관리정보 N 001 : 업무개시
        sendData.writeBytes(ByteUtil.setString(userId, 20)); // 송신자명 A
        sendData.writeBytes(ByteUtil.setString(password, 16)); // 송신자 암호

        writeLength(sendData);

        return sendData;
    }


    /**
     * 파일송수신완료 지시(0600/003)
     *
     * @param orgCode
     * @param srCode
     * @param fileName
     * @param userId
     * @param password
     * @param sendDateStr
     * @return
     */
    public static ByteBuf fileSendCompleteRequest(String orgCode, String srCode, String fileName, String userId, String password, String sendDateStr) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0600", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setString(sendDateStr, 10)); // 전문전송일시 AN
        buff.writeBytes(ByteUtil.setString("003", 3)); // 업무관리정보 N 003 : 파일송수신완료 (송신할 파일 없음)
        buff.writeBytes(ByteUtil.setString(userId, 20)); // 송신자명 A
        buff.writeBytes(ByteUtil.setString(password, 16)); // 송신자 암호

        writeLength(buff);

        return buff;
    }



}
