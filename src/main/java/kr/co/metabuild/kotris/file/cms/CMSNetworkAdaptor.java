package kr.co.metabuild.kotris.file.cms;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import kr.co.metabuild.kotris.exception.EAIException;
import kr.co.metabuild.kotris.exception.ResponseCode;
import kr.co.metabuild.kotris.file.cms.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class CMSNetworkAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(CMSNetworkAdaptor.class);

    private boolean enabled;
    @NotNull(message = "properties 파일에  \" + AdaptorPropPrefix.CMS_ADAPTOR_PREFIX + \".remote-host 등록하여주세요")
    private String remoteHost;
    @NotNull(message = "properties 파일에 \" + AdaptorPropPrefix.CMS_ADAPTOR_PREFIX + \".remote-port 등록하여주세요")
    private String remotePort;
    private int dataframe = 4096;
    private boolean hexMode = false;
    private String recvTempFilePath;
    private String recvFilePath;
    private String sendTempFilePath;

    static final int FINAL_SEQ = 100;
    static final int FINAL_Block = 9999;
    static final int DATA_INDEX = 43;

    private int exchangedDataframe;


    public static LinkedHashMap<String, String> LOGGING_POISON_INSTANCE = new LinkedHashMap<>();
    LinkedBlockingDeque<Map<String, String>> loggingQueue = new LinkedBlockingDeque<>();

    public static Map<String, String> resCodeMessage = new HashMap<>();
    public static Map<String, String> fileInquiryMessage = new HashMap<>();
    public static Map<String, String> fileCancelMessage = new HashMap<>();

    static {
        resCodeMessage.put("000", "정상");
        resCodeMessage.put("090", "시스템 장애 - 비밀번호 오류횟수 초과");
        resCodeMessage.put("310", "송신자명 오류");
        resCodeMessage.put("320", "송신자 암호 오류");
        resCodeMessage.put("630", "기전송 완료");
        resCodeMessage.put("631", "미등록 업무");
        resCodeMessage.put("632", "비정상 파일명 - 파일명의 MMDD가 휴일인 경우");
        resCodeMessage.put("633", "비정상 전문 BYTE 수");
        resCodeMessage.put("634", "파일송신 가능 시간/일자 완료 - 참가기관 앞 파일 처리 완료");
        resCodeMessage.put("635", "EI13파일 검증완료 전 EB13파일 수신");
        resCodeMessage.put("800", "FORMAT 오류");

        fileInquiryMessage.put("000", "정상");
        fileInquiryMessage.put("400", "처리중(센터에서 파일 검증 처리중. 일정시간 후 재조회 필요)");
        fileInquiryMessage.put("411", "송신중단(미완료파일)");
        fileInquiryMessage.put("412", "MAC 검증값 오류");
        fileInquiryMessage.put("413", "데이터 오류(FORMAT 오류 등)");
        fileInquiryMessage.put("414", "데이터 변형 오류");
        fileInquiryMessage.put("415", "조회대상파일 존재하지 않음");

        fileCancelMessage.put("000", "취소/초기화 성공");
        fileCancelMessage.put("511", "취소/초기화 대상파일 존재하지 않음(수신된 적 없는 파일)");
        fileCancelMessage.put("512", "기 취소/초기화된 파일");
        fileCancelMessage.put("513", "재전송이 불가한 기 취소/초기화 파일");
        fileCancelMessage.put("514", "취소/초기화 불가한 파일종류");
        fileCancelMessage.put("515", "취소/초기화 요청 마감시한 경과");
        fileCancelMessage.put("516", "연계파일 존재");
        fileCancelMessage.put("599", "기타 사유로 인한 취소/초기화 실패");
    }

    private int loggingTaskCount = 3;
    private String loggingBasePath;
    private int messageResendCount = 3;

    public String sendFileSync(Request request, String fileFullPath) throws Exception {

        String cmsFileName = request.getFileName();

        File file = new File(fileFullPath);
        // 파일미등록오류
        if (!file.exists()) { throw new EAIException(ResponseCode.RES_E04.getDesc(), ResponseCode.RES_E04);}

        // 포트
        int selectedRemotePort = Integer.parseInt(remotePort);
        // 기관코드
        String orgCode = request.getOrgCode();

        // 이벤트루프 그룹지정
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        logger.debug("CMS Adapter *: to {} : {} ...", remoteHost, selectedRemotePort);
        try {
            // 블로킹 큐 선언
            BlockingDeque<ByteBuf> responseQueue = new LinkedBlockingDeque<>();
            // 부트스트랩
            Bootstrap b = new Bootstrap();
            b.group(workerGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new CMSInitializer(responseQueue));
            ChannelFuture cf = b.connect(remoteHost, selectedRemotePort).syncUninterruptibly();
            Channel conn = cf.channel();

            // 전문송신일시
            String sendDataStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss")); // ex) 0306131309

            /********************************************************************************************
             * . 업무 개시 요청[0600/001]
             ********************************************************************************************/
            int recallCountNo0610 = 0;
            int recallCountNo0640 = 0;
            boolean nextStep = false;

            ByteBuf sendData = null;
            while (!nextStep && recallCountNo0610 <= messageResendCount) {
//                ByteBuf workStartRequest =
            }






        } catch (Exception e) {
            // 외부 Connection 실패
            throw new EAIException(ResponseCode.RES_E02.getDesc(), ResponseCode.RES_E02);
        } finally {
            if (!workerGroup.isShutdown()) {
                workerGroup.shutdownGracefully();
            }
        }

        return "000";
    }


    private String missDataSendProcess() throws Exception {
        return null;
    }


    public String recvfileSync() throws Exception {
        return null;
    }


    public String recvfileSyncFileInfo() throws Exception {
        return null;
    }


    public String fileInquiry() throws Exception {
        return null;
    }


    public String fileCancel() throws Exception {
        return null;
    }


    public void start() {
        // TODO:
/*
        service.setLoggingBasePath(loggingBasePath);
        service.setLoggingTaskCount(loggingTaskCount);
        service.start();
*/
    }


    public void stop() {

        // 서비스 중지
        // TODO:
        // service.stop()

        // 로깅 큐에 인스턴스 추가
        for (int i = 0; i < loggingTaskCount; i++) {
            loggingQueue.add(LOGGING_POISON_INSTANCE);
        }
        logger.debug("Stop.. ");
    }



}
