package kr.co.metabuild.kotris.file.cms;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import kr.co.metabuild.kotris.config.AdaptorPropPrefix;
import kr.co.metabuild.kotris.exception.EAIException;
import kr.co.metabuild.kotris.exception.ResponseCode;
import kr.co.metabuild.kotris.file.cms.message.FileInfo;
import kr.co.metabuild.kotris.file.cms.message.Request;
import kr.co.metabuild.kotris.util.ByteUtil;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@ConfigurationProperties(prefix = AdaptorPropPrefix.CMS_ADAPTOR_PREFIX, ignoreUnknownFields = true)
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

    /**
     * 동기방식 일괄 파일 전송
     *
     * @param request CMS파일전송요청객체
     * @param fileFullPath 파일경로
     * @return String 응답코드
     * @throws Exception 예외발생
     */
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

            ByteBuf workStartResponse = null;
            while (!nextStep && recallCountNo0610 <= messageResendCount) {
                ByteBuf workStartRequest = CMSMessage.workStartRequest(orgCode, "R", "", request.getUserName(), request.getUserPassword(), sendDataStr);
                logger.debug("TransferType: {}, Request [{}], tx[{}] : {}", "OutBound", workStartRequest, "0600", "workStartRequest");
                conn.writeAndFlush(workStartRequest).awaitUninterruptibly();


                /********************************************************************************************
                 * . 업무 개시 응답[0610]
                 ********************************************************************************************/
                workStartResponse = responseQueue.poll(60, TimeUnit.SECONDS);
                if (workStartResponse == null) {
                    recallCountNo0610++;
                    logger.debug("[0610] Read Timeout 60초 발생");
                    logger.debug("[0600/001] 전문 재전송");
                    continue;
                } else {
                    nextStep = true;
                }
            }
            if (!nextStep) {
                throw new EAIException("Can not receive [0610] Message.");
            }

            String msgId = CMSExtractor.extractMessageId(workStartResponse);
            String resCode = CMSExtractor.extractResponseCode(workStartResponse);

            if (!msgId.contentEquals("0610")) {
                logger.error("Expected MessageId : 0610, Received MessageId {}", msgId);
                throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
            }

            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }

            nextStep = false;
            ByteBuf fileInfoRecvResponse = null;


            while (!nextStep && recallCountNo0640 <= messageResendCount) {


                /********************************************************************************************
                 * . 파일정보수신요청[0630]
                 ********************************************************************************************/
                ByteBuf fileInfoRecvRequest = CMSMessage.fileInfoRecvRequest(orgCode, "R", cmsFileName, dataframe, (int) file.length());
                conn.writeAndFlush(fileInfoRecvRequest).awaitUninterruptibly();


                /********************************************************************************************
                 * . 파일정보수신응답[0640]
                 ********************************************************************************************/
                fileInfoRecvResponse = responseQueue.poll(60, TimeUnit.SECONDS);
                if (fileInfoRecvResponse == null) {
                    recallCountNo0640++;
                    logger.debug("[0640] Read Timeout 60초 발생");
                    logger.debug("[0630] 전문 재전송");
                    continue;
                } else {
                    nextStep = true;
                }

            }

            if (!nextStep) {
                throw new EAIException("Can not receive [0640] Message.");
            }

            msgId = CMSExtractor.extractMessageId(fileInfoRecvResponse);
            resCode = CMSExtractor.extractResponseCode(fileInfoRecvResponse);

            long alRecvSize = CMSExtractor.extractFileInfoFileSize(fileInfoRecvResponse);
            int exchangeDataframe = CMSExtractor.extractFileInfoDataFrame(fileInfoRecvResponse);

            if (dataframe <= exchangeDataframe) {
                exchangedDataframe = dataframe;
            } else {
                exchangedDataframe = exchangeDataframe;
            }

            logger.debug("exchangedDataframe : {}", exchangedDataframe);

            if (alRecvSize > 0) {
                logger.debug("Already Received Bytes : {}", alRecvSize);
            }

            if (!msgId.contentEquals("0640")) {
                logger.error("Expected MessageId : 0640, Received MessageId {}", msgId);
                throw new EAIException("Expected MessageId : 0640, Received MessageId " + msgId);
            }

            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }

            // 기전송 유무를 판단해야 함
            boolean loop = true;
            long offset = 0;


            // (4096) - DATA_INDEX(43) + 4 ) = data length 전문정보에 들어갈 패킷사이즈는 4096이고 실제 패킷은
            // 4100이니 4를 더해준다
            int dataPacketSize = (exchangedDataframe + 4) -DATA_INDEX;
            logger.debug("data packet size : {}", dataPacketSize);

            long realFileSize = file.length();
            long sumFileSize = 0L;
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

                MultiKeyMap<Integer, Long> missNumAddrMap = new MultiKeyMap<>();
                int seq = 1;
                int block = 1;
                // Block loop
                while (loop) {
                    byte[] data = new byte[dataPacketSize];
                    int actualLength = 0;
                    raf.seek(offset);
                    actualLength = raf.read(data);
                    sumFileSize += actualLength;


                    // 추출한 데이터가 패킷길이보다 부족할때 또는 파일누적길이가 파일사이즈랑 동일할때 마지막단계인걸 안다. <결번요청/확인과정이 포함됨 Step>
                    // 결국 최종단계!!
                    if (actualLength < dataPacketSize || realFileSize == sumFileSize) {
                        byte[] actualBytes = new byte[actualLength];
                        System.arraycopy(data, 0, actualBytes, 0, actualLength);
                        data = actualBytes;

                        // 결번 요청을 위한 Offset 주소를 보관
                        missNumAddrMap.put(block, seq, offset);

                        if (alRecvSize < sumFileSize) {
                            ByteBuf dataBuf = CMSMessage.dataSend(orgCode, "R", cmsFileName, data, block, seq);
                            conn.writeAndFlush(dataBuf).awaitUninterruptibly();

                        } else {
                            // 데이터가 기수신한 만큼은 송신하지 않고 skip
                            logger.debug("0320 SendData block {}, seq {} Skipped", block, seq);
                        }

                        missDataSendProcess(cmsFileName, responseQueue, conn, orgCode, dataPacketSize, raf, missNumAddrMap, block, seq);
                        loop = false;

                    } else {
                        // 결번 요청을 위한 Offset 주소를 보관
                        missNumAddrMap.put(block, seq, offset);

                        // 이어보내기 구현 dataSkip
                        boolean dataSkip = false;

                        if (alRecvSize < sumFileSize) {
                            ByteBuf dataBuf = CMSMessage.dataSend(orgCode, "R", "", data, block, seq);
                            conn.writeAndFlush(dataBuf).awaitUninterruptibly();
                        } else {
                            // 데이터가 기수신한 만큼은 송신하지 않고 skip
                            dataSkip = true;
                        }

                        if (seq >= FINAL_SEQ) {
                            if (!dataSkip) {
                                missDataSendProcess(cmsFileName, responseQueue, conn, orgCode, dataPacketSize, raf, missNumAddrMap, block, seq);
                                seq = 1;
                                block++;
                            }
                        } else {
                            seq++;
                        }
                        offset += dataPacketSize;

                    }
                }
            }


            /********************************************************************************************
             * . 파일송신완료요청[0600]
             ********************************************************************************************/
            ByteBuf fileSendCompleteRequest = CMSMessage.fileSendCompleteRequest(orgCode, "R", cmsFileName, request.getUserName(), request.getUserPassword(), sendDataStr);
            conn.writeAndFlush(fileSendCompleteRequest).awaitUninterruptibly();


            /********************************************************************************************
             * . 파일송신완료응답[0610]
             ********************************************************************************************/
            ByteBuf fileSendCompleteResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            msgId = CMSExtractor.extractMessageId(fileSendCompleteResponse);
            resCode = CMSExtractor.extractResponseCode(fileSendCompleteResponse);
            if (!msgId.contentEquals("0610")) {
                logger.error("Expected MessageId : 0610, Received MessageId {}", msgId);
                throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
            }
            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
            }



            /********************************************************************************************
             * . 업무종료요청[0600]
             ********************************************************************************************/
            ByteBuf workEndRequest = CMSMessage.workEndRequest(orgCode, "R", cmsFileName, request.getUserName(), request.getUserPassword(), sendDataStr);
            conn.writeAndFlush(workEndRequest).awaitUninterruptibly();


            /********************************************************************************************
             * . 업무종료응답[0610]
             ********************************************************************************************/
            ByteBuf workEndResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            msgId = CMSExtractor.extractMessageId(workEndResponse);
            resCode = CMSExtractor.extractResponseCode(workEndResponse);
            if (!msgId.contentEquals("0610")) {
                logger.error("Expected MessageId : 0610, Received MessageId {}", msgId);
                throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
            }
            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }
            logger.debug("File Send Complete! FileName : {}", cmsFileName);

            if (conn.isActive()) {
                conn.close().awaitUninterruptibly();
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

    /**
     * 결번 확인 및 결번 데이터 송신 처리
     *
     * @param fileName 파일명
     * @param responseQueue 응답큐
     * @param conn 커넥션 채널
     * @param orgCode 기관코드
     * @param dataPacketSize 데이터 패킷 사이즈
     * @param raf 파일객체
     * @param missNumAddrMap 결번주소저장맵
     * @param block 블럭
     * @param seq 시퀀스
     * @return resCode 응답코드
     * @throws Exception 예외처리
     */
    private String missDataSendProcess(String fileName, BlockingDeque<ByteBuf> responseQueue, Channel conn, String orgCode, int dataPacketSize, RandomAccessFile raf, MultiKeyMap<Integer, Long> missNumAddrMap, int block, int seq) throws Exception {
        String resCode;
        String msgId;
        boolean missLoop = true;

        while (missLoop) {

            /********************************************************************************************
             * . 결번확인요청[0620] 송신
             ********************************************************************************************/

            int recallCountNo0300 = 0;
            boolean nextStep = false;
            ByteBuf sendMissDataConfirmResponse = null;
            while (!nextStep && recallCountNo0300 <= messageResendCount) {

                ByteBuf missConfirmBuf = CMSMessage.missDataConfirmRequest(orgCode, "R", fileName, block, seq);
                conn.writeAndFlush(missConfirmBuf).awaitUninterruptibly();


                /********************************************************************************************
                 * . 결번확인응답[0300] 수신
                 ********************************************************************************************/
                sendMissDataConfirmResponse = responseQueue.poll(60, TimeUnit.SECONDS);
                if (sendMissDataConfirmResponse == null) {
                    recallCountNo0300++;
                    logger.debug("[0300] Read TimeOut 60초 발생");
                    logger.debug("[0620] 전문재전송...");
                    continue;
                } else {
                    nextStep = true;
                }


            }

            if (!nextStep) {
                throw new EAIException("Can't Receive [0300] Message.", ResponseCode.RES_E02);
            }

            msgId = CMSExtractor.extractMessageId(sendMissDataConfirmResponse);
            resCode = CMSExtractor.extractResponseCode(sendMissDataConfirmResponse);
            if (!msgId.contentEquals("0300")) {
                logger.error("Expected MessageId : 0300, Received MessageId {}", msgId);
                throw new EAIException("Expected MessageId : 0300, Received MessageId " + msgId);
            }
            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                throw new EAIException("missDataSendProcess fail" + resCode, ResponseCode.RES_E02);
            }

            int resBlockNo = ByteUtil.extractInt(sendMissDataConfirmResponse, 32, 4);
            int resMissCount = ByteUtil.extractInt(sendMissDataConfirmResponse, 39, 3);

            // 결번이 한 건이라도 있으면 결번 송신 처리 시작
            if (resMissCount == 0) {
                // 결번이 발생하지 않았으면 탈출
                missLoop = false;
            } else {
                // 여기부터 결번 발생
                int finalSeq = ByteUtil.extractInt(sendMissDataConfirmResponse, 38, 3);
                String missCheckData = ByteUtil.extractString(sendMissDataConfirmResponse, 42, finalSeq);
                int[] checkSeq = { 1 };

                final RandomAccessFile missRaf = raf;

                missCheckData.chars().forEach(c -> {
                    if (c == '0') {

                        /********************************************************************************************
                         * . 결번 데이터 송신[0310] 송신
                         ********************************************************************************************/
                        long missOffset = missNumAddrMap.get(resBlockNo, checkSeq[0]);
                        byte[] missData = new byte[dataPacketSize];
                        int missActualLength = 0;

                        try {
                            missRaf.seek(missOffset);
                            missActualLength = missRaf.read(missData);
                            if (missActualLength < dataPacketSize) {
                                byte[] missActualBytes = new byte[missActualLength];
                                System.arraycopy(missData, 0, missActualBytes, 0, missActualLength);
                                missData = missActualBytes;
                            }

                            // 데이터 전송
                            ByteBuf missDataBuf = CMSMessage.missDataSend(orgCode, "R", fileName, missData, resBlockNo, checkSeq[0]);
                            conn.writeAndFlush(missDataBuf).awaitUninterruptibly();

                        } catch (IOException e) {
                            logger.error("Miss Data Extract Fail", e);
                        }
                    }
                    checkSeq[0]++;
                });
            }

        }
        return "000";
    }





    public String recvfileSync(Request request, List<String> completeFiles) throws Exception {
        int selectedRemotePort = Integer.parseInt(remotePort);
        String orgCode = request.getOrgCode();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        logger.debug("CMSAdaptor *: to {} : {} ...", remoteHost, remotePort);
        try {
            BlockingDeque<ByteBuf> responseQueue = new LinkedBlockingDeque<>();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new CMSInitializer(responseQueue));
            ChannelFuture cf = bootstrap.connect(remoteHost, selectedRemotePort).syncUninterruptibly();
            Channel conn = cf.channel();

            // 전문전송일시
            String sendDataStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss")); // ex) 0306131309

            int recallCountNo0610 = 0;
            int recallCountNo0630 = 0;
            boolean isComplete = false;
            while (!isComplete && recallCountNo0610 <= messageResendCount && recallCountNo0630 <= messageResendCount) {


                /********************************************************************************************
                 * . 업무개시요청[0600] 송신
                 ********************************************************************************************/
                ByteBuf workStartRequest = CMSMessage.workStartRequest(orgCode, "S", "", request.getUserName(), request.getUserPassword(), sendDataStr);
                conn.writeAndFlush(workStartRequest).awaitUninterruptibly();


                /********************************************************************************************
                 * . 업무개시응답[0610] 수신
                 ********************************************************************************************/
                ByteBuf workStartResponse = responseQueue.poll(60, TimeUnit.SECONDS);
                if (workStartResponse == null) {
                    recallCountNo0610++;
                    logger.debug("[0610] Read TimeOut 60초 발생");
                    logger.debug("[0600-001] 전문재전송 ...");
                    continue;
                }
                String msgId = CMSExtractor.extractMessageId(workStartResponse);
                String resCode = CMSExtractor.extractResponseCode(workStartResponse);
                if (!msgId.contentEquals("0610")) {
                    throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
                }
                if (!resCode.contentEquals("000")) {
                    logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                    return resCode;
                }

                boolean nextFile = true;
                while (nextFile) {
                    ByteBuf selectionBuf = responseQueue.poll(60, TimeUnit.SECONDS);

                    if (selectionBuf == null) {
                        logger.debug("[0630] Read TimeOut 60초 발생");
                        logger.debug("[0600-001] 전문재전송...");
                        recallCountNo0630++;
                        break;
                    }
                    msgId = CMSExtractor.extractMessageId(selectionBuf);

                    ByteBuf fileInfoRecvRequest = null;

                    switch (msgId) {
                        case "0630": {
                            fileInfoRecvRequest = selectionBuf;

                            /********************************************************************************************
                             * . 파일정보수신요청[0630] 송신
                             ********************************************************************************************/
                            break;
                        }
                        case "0600": {
                            /********************************************************************************************
                             * . 업무종료[0600/004] 송신
                             ********************************************************************************************/
                            ByteBuf workEndResponse = CMSMessage.workEndResponse(orgCode, "S", "", request.getUserName(), request.getUserPassword(), sendDataStr);
                            conn.writeAndFlush(workEndResponse).awaitUninterruptibly();
                            nextFile = false;
                            isComplete = true;
                            continue;
                        }
                        default:
                            break;
                    }


                    /********************************************************************************************
                     * . 파일정보수신요청[0630] 송신
                     ********************************************************************************************/
                    msgId = CMSExtractor.extractMessageId(fileInfoRecvRequest);
                    resCode = CMSExtractor.extractResponseCode(fileInfoRecvRequest);
                    if (!msgId.contentEquals("0630")) {
                        logger.error("Expected MessageId : 0630, Received MessageId " + msgId);
                        throw new EAIException("Expected MessageId : 0630, Received MessageId " + msgId);
                    }

                    if (!resCode.contentEquals("000")) {
                        logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                        return resCode;
                    }

                    String cmdFileName = CMSExtractor.extractFileInfoFileName(fileInfoRecvRequest);
                    long fileSize = CMSExtractor.extractFileInfoFileSize(fileInfoRecvRequest);
                    int dataframe = CMSExtractor.extractFileInfoDataFrame(fileInfoRecvRequest);
                    Path tmpRecvFile = Paths.get(recvTempFilePath, cmdFileName);

                    Path recvTempDir = Paths.get(recvTempFilePath);
                    if (!Files.exists(recvTempDir)) {
                        Files.createDirectory(recvTempDir);
                    }

                    // 이어받기 하지 말고 기존파일 삭제하고 처음부터 받자
                    long recvFileSize = 0L;
                    if (Files.notExists(tmpRecvFile)) {
                        Files.createFile(tmpRecvFile);
                    } else {
                        Files.delete(tmpRecvFile);
                    }

                    /********************************************************************************************
                     * . 파일정보수신요청[0640] 송신
                     ********************************************************************************************/
                    ByteBuf fileInfoRecvResponse = CMSMessage.fileInfoRecvResponse(orgCode, "S", cmdFileName, dataframe, recvFileSize);
                    conn.writeAndFlush(fileInfoRecvResponse).awaitUninterruptibly();

                    boolean recvLoop = true;
                    MultiKeyMap<Integer, byte[]> dataMap = new MultiKeyMap<>();

                    int finalBlockNo = 1;
                    int finalSeq = 1;

                    while (recvLoop) {

                        ByteBuf unknownData = responseQueue.poll(60, TimeUnit.SECONDS);

                        if (unknownData == null) {
                            logger.debug("Read TimeOut 180초 발생");
                            logger.debug("Process End ...");
                            return "reconnect";
                        }
                        msgId = CMSExtractor.extractMessageId(unknownData);

                        switch (msgId) {
                            case "0320": {
                                /********************************************************************************************
                                 * . 데이터송신[0320] 송신
                                 ********************************************************************************************/
                                byte[] data = CMSExtractor.extractDataSendData(unknownData);
                                int blockNo = CMSExtractor.extractDataSendBlockNo(unknownData);
                                int seq = CMSExtractor.extractDataSendSeq(unknownData);
                                dataMap.put(blockNo, seq, data);
                                break;
                            }
                            case "310": {
                                /********************************************************************************************
                                 * . 결번데이터송신[0310] 송신
                                 ********************************************************************************************/
                                byte[] data = CMSExtractor.extractDataSendData(unknownData);
                                int blockNo = CMSExtractor.extractDataSendBlockNo(unknownData);
                                int seq = CMSExtractor.extractDataSendSeq(unknownData);
                                dataMap.put(blockNo, seq, data);
                                break;
                            }
                            case "0620": {
                                /********************************************************************************************
                                 * . 결번확인요청[0620] 송신
                                 ********************************************************************************************/
                                finalBlockNo = CMSExtractor.extractDataSendBlockNo(unknownData);
                                finalSeq = CMSExtractor.extractDataSendSeq(unknownData);
                                StringBuilder missResult = new StringBuilder();
                                int missCount = 0;
                                for (int i = 1; i < finalSeq; i++) {
                                    if (dataMap.containsKey(finalBlockNo, i)) {
                                        missResult.append("1");
                                    } else {
                                        missResult.append("0");
                                        missCount++;
                                    }
                                }

                                /********************************************************************************************
                                 * . 결번확인응답[0300] 수신
                                 ********************************************************************************************/
                                ByteBuf missDataConfirmResponse = CMSMessage.missDataConfirmResponse(orgCode, "S", cmdFileName, finalBlockNo, finalSeq, missCount, missResult.toString());
                                conn.writeAndFlush(missDataConfirmResponse).awaitUninterruptibly();
                                break;
                            }
                            case "0600": {
                                /********************************************************************************************
                                 * . 데이터송신완료[0600] 송신
                                 ********************************************************************************************/
                                msgId = CMSExtractor.extractMessageId(unknownData);
                                resCode = CMSExtractor.extractResponseCode(unknownData);
                                if (!msgId.contentEquals("0600")) {
                                    logger.error("Expected MessageId : 0600, Received MessageId " + msgId);
                                    throw new EAIException("Expected MessageId : 0600, Received MessageId " + msgId);
                                }
                                if (!resCode.contentEquals("000")) {
                                    logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                                    return resCode;
                                }

                                String workAdminInfo = CMSExtractor.extractFileInfoFileName(unknownData);
                                switch (workAdminInfo) {
                                    case "002":
                                        logger.debug("nextFile Exist");
                                        break;
                                    case "003":
                                        logger.debug("FinalFile End ...");
                                        break;
                                    default:
                                        break;
                                }

                                // 파일을 저장하고
                                for (int i = 1; i <= finalBlockNo; i++) {
                                    int seq = 1;
                                    while (true) {
                                        byte[] data = dataMap.get(i, seq);
                                        if (data == null) {
                                            break;
                                        }
                                        Files.write(tmpRecvFile, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                                        seq++;
                                    }
                                }
                                logger.debug("Received FileSize : {}, RealFileSize : {}", fileSize, Files.size(tmpRecvFile));
                                /********************************************************************************************
                                 * . 데이터송신완료[0610] 송신
                                 ********************************************************************************************/
                                Path recvDir = Paths.get(recvFilePath);

                                if (!Files.exists(recvDir)) {
                                    Files.createDirectories(recvDir);
                                }

                                Files.move(tmpRecvFile, Paths.get(recvDir.toString(), tmpRecvFile.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                                ByteBuf fileSendCompleteResponse = CMSMessage.fileSendCompleteResponse(orgCode, "S", cmdFileName, request.getUserName(), request.getUserPassword(), sendDataStr, workAdminInfo);

                                completeFiles.add(cmdFileName);
                                conn.writeAndFlush(fileSendCompleteResponse).awaitUninterruptibly();
                                recvLoop = false;
                            }
                            default:
                                break;
                        }
                    }


                }


            }

            if (conn.isActive()) {
                conn.close().awaitUninterruptibly();
            }

            if (!isComplete) {
                throw new EAIException("Can not Receive Message ...", ResponseCode.RES_E02);
            }

            logger.debug("처리 완료");
            return "000";
        } catch (Exception e) {
            throw new EAIException("Can not Receive Message ...", ResponseCode.RES_E02, e);
        } finally {
            if (!workerGroup.isShutdown()) {
                workerGroup.shutdownGracefully();
            }
        }
    }


    public String recvfileSyncFileInfo(Request request, List<FileInfo> completeFiles) throws Exception {
        int selectedRemotePort = Integer.parseInt(remotePort);
        String orgCode = request.getOrgCode();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        logger.debug("CMSAdaptor *: to {} : {} ...", remoteHost, remotePort);
        try {
            BlockingQueue<ByteBuf> responseQueue = new LinkedBlockingQueue<>();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new CMSInitializer(responseQueue));
            ChannelFuture cf = bootstrap.connect(remoteHost, selectedRemotePort).syncUninterruptibly();
            Channel conn = cf.channel();

            // 전문전송일시
            String sendDataStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss")); // ex) 0306131309

            /********************************************************************************************
             * . 업무개시요청[0600] 송신
             ********************************************************************************************/
            ByteBuf workStartRequest = CMSMessage.workStartRequest(orgCode, "S", "", request.getUserName(), request.getUserPassword(), sendDataStr);
            conn.writeAndFlush(workStartRequest).awaitUninterruptibly();

            /********************************************************************************************
             * . 업무개시응답[0600] 수신
             ********************************************************************************************/
            ByteBuf workStartResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            if (workStartResponse == null) {
                throw new EAIException("[0610] Read TimeOut 60초 발생", ResponseCode.RES_E02);
            }
            String msgId = CMSExtractor.extractMessageId(workStartResponse);
            String resCode = CMSExtractor.extractResponseCode(workStartResponse);
            if (!msgId.contentEquals("0610")) {
                throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
            }
            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }

            boolean nextFile = true;
            while (nextFile) {

                ByteBuf selectionBuf = responseQueue.poll(60, TimeUnit.SECONDS);
                msgId = CMSExtractor.extractFileInfoFileName(selectionBuf);

                ByteBuf fileInfoRecvRequest = null;

                switch (msgId) {
                    case "0630": {
                        fileInfoRecvRequest = selectionBuf;

                        /********************************************************************************************
                         * . 파일정보수신요청[0630] 송신
                         ********************************************************************************************/
                        break;
                    }
                    case "0600": {

                        /********************************************************************************************
                         * . 업무종료요청[0600/004] 송신
                         ********************************************************************************************/
                        ByteBuf workEndResponse = CMSMessage.workEndResponse(orgCode, "S", "", request.getUserName(), request.getUserPassword(), sendDataStr);
                        conn.writeAndFlush(workEndResponse).awaitUninterruptibly();
                        nextFile = false;
                        continue;
                    }
                    default:
                        break;
                }

                /********************************************************************************************
                 * . 파일정보수신요청[0630] 송신
                 ********************************************************************************************/
                msgId = CMSExtractor.extractMessageId(fileInfoRecvRequest);
                resCode = CMSExtractor.extractResponseCode(fileInfoRecvRequest);
                if (!msgId.contentEquals("0630")) {
                    logger.error("Expected MessageId : 0630, Received MessageId " + msgId);
                    throw new EAIException("Expected MessageId : 0630, Received MessageId " + msgId);
                }

                if (!resCode.contentEquals("000")) {
                    logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                    return resCode;
                }

                String cmsFileName = CMSExtractor.extractFileInfoFileName(fileInfoRecvRequest);
                long fileSize = CMSExtractor.extractFileInfoFileSize(fileInfoRecvRequest);
                int dataframe = CMSExtractor.extractFileInfoDataFrame(fileInfoRecvRequest);
                Path tmpRecvFile = Paths.get(recvTempFilePath, cmsFileName);

                Path recvTempDir = Paths.get(recvTempFilePath);
                if (!Files.exists(recvTempDir)) {
                    Files.createDirectory(recvTempDir);
                }

                // 이어받기 하지 말고 기존파일 삭제하고 처음부터 받자
                long recvFileSize = 0L;
                if (Files.notExists(tmpRecvFile)) {
                    Files.createFile(tmpRecvFile);
                } else {
                    Files.delete(tmpRecvFile);
                }

                /********************************************************************************************
                 * . 파일정보수신요청[0640] 송신
                 ********************************************************************************************/
                ByteBuf fileInfoRecvResponse = CMSMessage.fileInfoRecvResponse(orgCode, "S", cmsFileName, dataframe, recvFileSize);
                conn.writeAndFlush(fileInfoRecvResponse).awaitUninterruptibly();

                boolean recvLoop = true;
                MultiKeyMap<Integer, byte[]> dataMap = new MultiKeyMap<>();

                int finalBlockNo = 1;
                int finalSeq = 1;

                while (recvLoop) {

                    ByteBuf unknownData = responseQueue.poll(60, TimeUnit.SECONDS);
                    msgId = CMSExtractor.extractMessageId(unknownData);

                    switch (msgId) {

                        case "0320": {

                            /********************************************************************************************
                             * . 데이터송신[0320] 송신
                             ********************************************************************************************/
                            byte[] data = CMSExtractor.extractDataSendData(unknownData);
                            int blockNo = CMSExtractor.extractDataSendBlockNo(unknownData);
                            int seq = CMSExtractor.extractDataSendSeq(unknownData);
                            dataMap.put(blockNo, seq, data);
                            break;
                        }
                        case "0310": {

                            /********************************************************************************************
                             * . 결번데이터송신[0310] 송신
                             ********************************************************************************************/
                            byte[] data = CMSExtractor.extractDataSendData(unknownData);
                            int blockNo = CMSExtractor.extractDataSendBlockNo(unknownData);
                            int seq = CMSExtractor.extractDataSendSeq(unknownData);
                            dataMap.put(blockNo, seq, data);
                            break;

                        }
                        case "0620": {
                            /********************************************************************************************
                             * . 결번확인응답[0620] 수신
                             ********************************************************************************************/
                            finalBlockNo = CMSExtractor.extractDataSendBlockNo(unknownData);
                            finalSeq = CMSExtractor.extractDataSendSeq(unknownData);
                            StringBuilder missResult = new StringBuilder();
                            int missCount = 0;
                            for (int i = 1; i <= finalSeq; i++) {
                                if (dataMap.containsKey(finalBlockNo, i)) {
                                    missResult.append("1");
                                } else {
                                    missResult.append("0");
                                    missCount++;
                                }
                            }

                            /********************************************************************************************
                             * . 결번확인요청[0300] 송신
                             ********************************************************************************************/
                            ByteBuf missDataConfirmResponse = CMSMessage.missDataConfirmResponse(orgCode, "S", cmsFileName, finalBlockNo, finalSeq, missCount, missResult.toString());
                            conn.writeAndFlush(missDataConfirmResponse).awaitUninterruptibly();
                            break;
                        }
                        case "0600": {

                            /********************************************************************************************
                             * . 송신완료요청[0600] 송신
                             ********************************************************************************************/
                            msgId = CMSExtractor.extractMessageId(unknownData);
                            resCode = CMSExtractor.extractResponseCode(unknownData);
                            if (!msgId.contentEquals("0600")) {
                                logger.error("Expected MessageId : 0600, Received MessageId " + msgId);
                                throw new EAIException("Expected MessageId : 0600, Received MessageId " + msgId);
                            }
                            if (!resCode.contentEquals("000")) {
                                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                                return resCode;
                            }

                            String workAdminInfo = CMSExtractor.extractWorkAdminInfo(unknownData);
                            switch (workAdminInfo) {
                                case "002":
                                    logger.debug("nextFile Exist");
                                    break;
                                case "003":
                                    logger.debug("FinalFile End ...");
                                    break;
                                default:
                                    break;
                            }

                            // 파일 저장
                            for (int i = 0; i < finalBlockNo; i++) {
                                int seq = 1;
                                while (true) {
                                    byte[] data = dataMap.get(i, seq);
                                    if (data == null) {
                                        break;
                                    }
                                    Files.write(tmpRecvFile, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                                    seq++;
                                }
                            }
                            logger.debug("Received FileSize : {}, RealFileSize : {}", fileSize, Files.size(tmpRecvFile));

                            /********************************************************************************************
                             * . 송신완료요청[0610] 송신
                             ********************************************************************************************/
                            Path recvDir = Paths.get(recvFilePath);

                            if (!Files.exists(recvDir)) {
                                Files.createDirectory(recvDir);
                            }

                            Files.move(tmpRecvFile, Paths.get(recvDir.toString(), tmpRecvFile.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);

                            long fsize = Files.size(Paths.get(recvDir.toString(), tmpRecvFile.getFileName().toString()));
                            ByteBuf fileSendCompleteResponse = CMSMessage.fileSendCompleteResponse(orgCode, "S", cmsFileName, request.getUserName(), request.getUserPassword(), sendDataStr);
                            FileInfo finfo = new FileInfo();
                            finfo.setName(cmsFileName);
                            finfo.setSize(fsize);
                            completeFiles.add(finfo);
                            recvLoop = false;

                        }

                        default:
                            break;

                    }
                }


            }


            if (conn.isActive()) {
                conn.close().awaitUninterruptibly();
            }

            logger.debug("처리 완료");
            return "000";
        } catch (Exception e) {
            throw new EAIException("Can not Receive Message ...", ResponseCode.RES_E02, e);
        } finally {
            if (!workerGroup.isShutdown()) {
                workerGroup.shutdownGracefully();
            }
        }

    }


    public String fileInquiry(Request request) throws Exception {

        int selectedRemotePort = Integer.parseInt(remotePort);

        String cmsFileName = request.getFileName();
        String yyyymmdd = request.getYyyymmdd();
        String orgCode = request.getOrgCode();

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        logger.debug("CMSAdaptor *: to {} : {} ...", remoteHost, remotePort);
        try {
            BlockingQueue<ByteBuf> responseQueue = new LinkedBlockingQueue<>();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new CMSInitializer(responseQueue));
            ChannelFuture cf = bootstrap.connect(remoteHost, selectedRemotePort).syncUninterruptibly();
            Channel conn = cf.channel();

            // 전문송신일시
            String sendDataStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss")); // ex) 0306131309

            /********************************************************************************************
             * . 업무개시요청[0600] 송신
             ********************************************************************************************/
            ByteBuf workStartRequest = CMSMessage.workStartRequest(orgCode, "R", "", request.getUserName(), request.getUserPassword(), sendDataStr);
            conn.writeAndFlush(workStartRequest).awaitUninterruptibly();

            /********************************************************************************************
             * . 업무개시응답[0610] 수신
             ********************************************************************************************/
            ByteBuf workStartResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            String msgId = CMSExtractor.extractMessageId(workStartResponse);
            String resCode = CMSExtractor.extractResponseCode(workStartResponse);

            if (!msgId.contentEquals("0610")) {
                logger.error("Expected MessageId : 0610, Received MessageId " + msgId);
                throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
            }

            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }


            /********************************************************************************************
             * . 파일조회요청[0400] 송신
             ********************************************************************************************/
            ByteBuf fileInquiryRequest = CMSMessage.fileInquiryRequest(orgCode, "R", cmsFileName, yyyymmdd);
            conn.writeAndFlush(fileInquiryRequest).awaitUninterruptibly();


            /********************************************************************************************
             * . 파일조회응답[0410] 수신
             ********************************************************************************************/
            ByteBuf fileInquiryResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            msgId = CMSExtractor.extractMessageId(fileInquiryResponse);
            resCode = CMSExtractor.extractResponseCode(fileInquiryResponse);
            String resultCode = CMSExtractor.extractFileInquiryResultCode(fileInquiryResponse);
            if (!msgId.contentEquals("0410")) {
                logger.error("Expected MessageId : 0410, Received MessageId " + msgId);
                throw new EAIException("Expected MessageId : 0410, Received MessageId " + msgId);
            }

            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }


            /********************************************************************************************
             * . 업무종료요청[0600] 송신
             ********************************************************************************************/
            ByteBuf workEndRequest = CMSMessage.workEndRequest(orgCode, "R", "", request.getUserName(), request.getUserPassword(), sendDataStr);
            conn.writeAndFlush(workEndRequest).awaitUninterruptibly();


            /********************************************************************************************
             * . 업무종료응답[0610] 수신
             ********************************************************************************************/
            ByteBuf workEndResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            msgId = CMSExtractor.extractMessageId(workEndResponse);
            resCode = CMSExtractor.extractResponseCode(workEndResponse);

            if (!msgId.contentEquals("0610")) {
                logger.error("Expected MessageId : 0610, Received MessageId " + msgId);
                throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
            }

            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }

            if (conn.isActive()) {
                conn.close().awaitUninterruptibly();
            }

            logger.debug("처리 완료");
            return resultCode;

        } catch (Exception e) {
            logger.error("Error : {}", e);
            throw new EAIException("Can not Connect ...", ResponseCode.RES_E02, e);
        }
    }


    public String fileCancel(Request request) throws Exception {

        int selectedRemotePort = Integer.parseInt(remotePort);

        String cmsFileName = request.getFileName();
        String yyyymmdd = request.getYyyymmdd();
        String orgCode = request.getOrgCode();

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        logger.debug("CMSAdaptor *: to {} : {} ...", remoteHost, remotePort);

        try {
            BlockingDeque<ByteBuf> responseQueue = new LinkedBlockingDeque<>();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new CMSInitializer(responseQueue));
            ChannelFuture cf = bootstrap.connect(remoteHost, selectedRemotePort).syncUninterruptibly();
            Channel conn = cf.channel();

            // 전문송신일시
            String sendDataStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss")); // ex) 0306131309


            /********************************************************************************************
             * . 업무개시요청[0600] 송신
             ********************************************************************************************/
            ByteBuf workStartRequest = CMSMessage.workStartRequest(orgCode, "R", "", request.getUserName(), request.getUserPassword(), sendDataStr);
            conn.writeAndFlush(workStartRequest).awaitUninterruptibly();


            /********************************************************************************************
             * . 업무개시응답[0600] 수신
             ********************************************************************************************/
            ByteBuf workStartResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            String msgId = CMSExtractor.extractMessageId(workStartResponse);
            String resCode = CMSExtractor.extractResponseCode(workStartResponse);

            if (!msgId.contentEquals("0610")) {
                logger.error("Expected MessageId : 0610, Received MessageId " + msgId);
                throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
            }

            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message : {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }


            /********************************************************************************************
             * . 파일조회요청[0400] 송신
             ********************************************************************************************/
            ByteBuf fileCancelRequest = CMSMessage.fileCancelRequest(orgCode, "R", cmsFileName, yyyymmdd);
            conn.writeAndFlush(fileCancelRequest).awaitUninterruptibly();


            /********************************************************************************************
             * . 파일조회응답[0410] 수신
             ********************************************************************************************/
            ByteBuf fileCancelResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            msgId = CMSExtractor.extractMessageId(fileCancelResponse);
            resCode = CMSExtractor.extractResponseCode(fileCancelResponse);
            String resultCode = CMSExtractor.extractFileInquiryResultCode(fileCancelResponse);

            if (!msgId.contentEquals("0510")) {
                logger.error("Expected MessageId : 0510, Received MessageId " + msgId);
                throw new EAIException("Expected MessageId : 0510, Received MessageId " + msgId);
            }

            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message :  {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }


            /********************************************************************************************
             * . 업무종료요청[0600] 송신
             ********************************************************************************************/
            ByteBuf workEndRequest = CMSMessage.workEndRequest(orgCode, "R", "", request.getUserName(), request.getUserPassword(), sendDataStr);
            conn.writeAndFlush(workEndRequest).awaitUninterruptibly();


            /********************************************************************************************
             * . 업무종료요청[0610] 수신
             ********************************************************************************************/
            ByteBuf workEndResponse = responseQueue.poll(60, TimeUnit.SECONDS);
            msgId = CMSExtractor.extractMessageId(workEndResponse);
            resCode = CMSExtractor.extractResponseCode(workEndResponse);

            if (!msgId.contentEquals("0610")) {
                logger.error("Expected MessageId : 0610, Received MessageId " + msgId);
                throw new EAIException("Expected MessageId : 0610, Received MessageId " + msgId);
            }

            if (!resCode.contentEquals("000")) {
                logger.error("Response Code : {}, Message : {}", resCode, resCodeMessage.get(resCode));
                return resCode;
            }

            if (conn.isActive()) {
                conn.close().awaitUninterruptibly();
            }

            logger.debug("처리 완료");
            return resultCode;

        } catch (Exception e) {
            logger.error("Can not Receive Message ...", ResponseCode.RES_E02, e);
            throw new EAIException("Can not Receive Message ...", ResponseCode.RES_E02, e);
        }
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
