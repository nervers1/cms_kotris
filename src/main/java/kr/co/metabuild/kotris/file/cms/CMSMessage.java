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
     * 업무개시요청(0600/001)
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param userId 사용자ID
     * @param password 비밀번호
     * @param sendDateStr 전송일자
     * @return ByteBuf
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
     * 파일송수신완료 요청(0600/003)
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param userId 사용자ID
     * @param password 비밀번호
     * @param sendDateStr 전송일자
     * @return ByteBuf
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

    
    /**
     * 파일송수신완료 응답(0610/003)
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param userId 사용자ID
     * @param password 비밀번호
     * @param sendDateStr 전송일자
     * @return ByteBuf
     */
    public static ByteBuf fileSendCompleteResponse(String orgCode, String srCode, String fileName, String userId, String password, String sendDateStr) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0610", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setString(sendDateStr, 10)); // 전문전송일시 AN
        buff.writeBytes(ByteUtil.setString("003", 3)); // 업무관리정보 N 003 : 파일송수신완료 (송신할 파일 없음)
        buff.writeBytes(ByteUtil.setString(userId, 20)); // 송신자명 A
        buff.writeBytes(ByteUtil.setString(password, 16)); // 송신자 암호
        writeLength(buff);
        return buff;
    }

    /**
     * 파일송수신완료 응답(0600/003)
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param userId 사용자ID
     * @param password 비밀번호
     * @param sendDateStr 전송일자
     * @param workAdminInfo 업무관리정보
     * @return ByteBuf
     */
    public static ByteBuf fileSendCompleteResponse(String orgCode, String srCode, String fileName, String userId, String password, String sendDateStr, String workAdminInfo) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0610", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setString(sendDateStr, 10)); // 전문전송일시 AN
        buff.writeBytes(ByteUtil.setString(workAdminInfo, 3)); // 업무관리정보 N 003 : 파일송수신완료 (송신할 파일 없음)
        buff.writeBytes(ByteUtil.setString(userId, 20)); // 송신자명 A
        buff.writeBytes(ByteUtil.setString(password, 16)); // 송신자 암호
        writeLength(buff);
        return buff;
    }


    /**
     * 업무종료지시(0600/004)
     *
     */
    public static ByteBuf workEndRequest(String orgCode, String srCode, String fileName, String userId, String password, String sendDateStr) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0600", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setString(sendDateStr, 10)); // 전문전송일시 AN
        buff.writeBytes(ByteUtil.setString("004", 3)); // 업무관리정보 N 004
        buff.writeBytes(ByteUtil.setString(userId, 20)); // 송신자명 A
        buff.writeBytes(ByteUtil.setString(password, 16)); // 송신자 암호
        writeLength(buff);
        return buff;
    }

    /**
     * 업무종료 응답(0600/004)
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param userId 사용자ID
     * @param password 비밀번호
     * @param sendDateStr 전송일자
     * @return ByteBuf
     */
    public static ByteBuf workEndResponse(String orgCode, String srCode, String fileName, String userId, String password, String sendDateStr) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0610", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setString(sendDateStr, 10)); // 전문전송일시 AN
        buff.writeBytes(ByteUtil.setString("004", 3)); // 업무관리정보 N 003 : 파일송수신완료 (송신할 파일 없음)
        buff.writeBytes(ByteUtil.setString(userId, 20)); // 송신자명 A
        buff.writeBytes(ByteUtil.setString(password, 16)); // 송신자 암호
        writeLength(buff);
        return buff;
    }

    /**
     * 파일정보 수신요청(0630)
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param dataframe 파일정보데이터
     * @param fileSize 파일사이즈
     * @return ByteBuf
     */
    public static ByteBuf fileInfoRecvRequest(String orgCode, String srCode, String fileName, int dataframe, long fileSize) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0630", orgCode, srCode, fileName, "000"));
        if (fileName.getBytes().length > 8) {
            fileName = fileName.substring(0, 8);
        }
        buff.writeBytes(ByteUtil.setString(fileName, 8)); // Filename
        buff.writeBytes(ByteUtil.setNumber(fileSize + "", 12)); // Filesize
        buff.writeBytes(ByteUtil.setString(dataframe + "", 4)); // byte
        writeLength(buff);
        return buff;
    }

    /**
     * 파일정보 수신요청(0640)
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param dataframe 파일정보데이터
     * @param fileSize 파일사이즈
     * @return ByteBuf
     */
    public static ByteBuf fileInfoRecvResponse(String orgCode, String srCode, String fileName, int dataframe, long fileSize) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0640", orgCode, srCode, fileName, "000"));

        buff.writeBytes(ByteUtil.setString(fileName, 8)); // Filename
        buff.writeBytes(ByteUtil.setNumber(fileSize + "", 12)); // Filesize
        buff.writeBytes(ByteUtil.setString(dataframe + "", 4)); // byte
        writeLength(buff);
        return buff;
    }

    /**
     * 0320 DATA 송신
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param data 데이터
     * @param block 블럭
     * @param seq 시퀀스
     * @return ByteBuf
     */
    public static ByteBuf dataSend(String orgCode, String srCode, String fileName, byte[] data, int block, int seq) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0320", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setNumber(block + "", 4)); // block-no
        buff.writeBytes(ByteUtil.setNumber(seq + "", 3)); // sequence-no
        buff.writeBytes(ByteUtil.setNumber(data.length + "", 4)); // data length
        buff.writeBytes(data); // 파일내역
        writeLength(buff);
        return buff;
    }

    /**
     * 0310 결번 DATA 송신
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param data 데이터
     * @param block 블럭
     * @param seq 시퀀스
     * @return ByteBuf
     */
    public static ByteBuf missDataSend(String orgCode, String srCode, String fileName, byte[] data, int block, int seq) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0310", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setNumber(block + "", 4)); // block-no
        buff.writeBytes(ByteUtil.setNumber(seq + "", 3)); // sequence-no
        buff.writeBytes(ByteUtil.setNumber(data.length + "", 4)); // miss data length
        buff.writeBytes(data); // 파일내역
        writeLength(buff);
        return buff;
    }

    /**
     * 0620 결번확인요청
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param block 블럭
     * @param seq 시퀀스
     * @return ByteBuf
     */
    public static ByteBuf missDataConfirmRequest(String orgCode, String srCode, String fileName, int block, int seq) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0620", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setNumber(block + "", 4)); // block-no
        buff.writeBytes(ByteUtil.setNumber(seq + "", 3)); // sequence-no
        writeLength(buff);
        return buff;
    }


    /**
     * 0300 결번확인응답
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param blockNo 블럭번호
     * @param finalSeq 최종시퀀스
     * @param missCount 결번갯수
     * @param missData 결번데이터
     * @return ByteBuf
     */
    public static ByteBuf missDataConfirmResponse(String orgCode, String srCode, String fileName, int blockNo, int finalSeq, int missCount, String missData) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0300", orgCode, srCode, fileName, "000"));
        buff.writeBytes(ByteUtil.setNumber(blockNo + "", 4)); // block-no
        buff.writeBytes(ByteUtil.setNumber(finalSeq + "", 3)); // finalseq
        buff.writeBytes(ByteUtil.setNumber(missCount + "", 3)); // misscount
        buff.writeBytes(missData.getBytes()); // missdata result
        writeLength(buff);
        return buff;
    }

    /**
     * 0400 파일조회요청
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param inquiryDate 요청일자
     * @return ByteBuf
     */
    public static ByteBuf fileInquiryRequest(String orgCode, String srCode, String fileName, String inquiryDate) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0400", orgCode, srCode, "", "000"));
        buff.writeBytes("001".getBytes()); // 조회종류
        buff.writeBytes(ByteUtil.setNumber(inquiryDate, 8)); // 조회대상일자
        buff.writeBytes(ByteUtil.setString(fileName, 8)); // Filename
        buff.writeBytes(ByteUtil.setString("", 3)); // 조회결과코드
        writeLength(buff);
        return buff;
    }

    /**
     * 파일취소요청
     *
     * @param orgCode 기관코드
     * @param srCode 요청응답구분코드
     * @param fileName 파일명
     * @param inquiryDate 요청일자
     * @return ByteBuf
     */
    public static ByteBuf fileCancelRequest(String orgCode, String srCode, String fileName, String inquiryDate) {
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(makeCommonField("0500", orgCode, srCode, "", "000"));
        buff.writeBytes(ByteUtil.setNumber(inquiryDate, 8)); // 파일처리일자
        buff.writeBytes(ByteUtil.setString(fileName, 8)); // Filename
        buff.writeBytes(ByteUtil.setString("", 3)); // 초기화결과코드
        writeLength(buff);
        return buff;
    }



}
