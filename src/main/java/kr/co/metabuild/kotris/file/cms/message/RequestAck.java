package kr.co.metabuild.kotris.file.cms.message;

import java.util.List;
import java.util.Objects;

/**
 * CMSRequestAck - CMS요청 응답
 */
public class RequestAck {

    /**
     * HEADER
     */
    // 연계_인터페이스
    String lnkInterfaceId;
    // 연계_트랜젝션
    String lnkTransactionId;
    // 연계_처리결과_코드
    String lnkResultCd;
    // 연계_처리결과 메시지
    String lnkResultMsg;
    // 연계_처리시간
    String lnkResultDt;

    /**
     * Body
     */
    // CMS 처리결과
    String cmsResultCd;
    // CMS 처리결과 메시지
    String cmsResultMsg;
    // CMS수신완료 파일 목록
    List<String> recvCompleteFiles;


    /**
     * Getter & Setter
     */
    public String getLnkInterfaceId() {
        return lnkInterfaceId;
    }

    public void setLnkInterfaceId(String lnkInterfaceId) {
        this.lnkInterfaceId = lnkInterfaceId;
    }

    public String getLnkTransactionId() {
        return lnkTransactionId;
    }

    public void setLnkTransactionId(String lnkTransactionId) {
        this.lnkTransactionId = lnkTransactionId;
    }

    public String getLnkResultCd() {
        return lnkResultCd;
    }

    public void setLnkResultCd(String lnkResultCd) {
        this.lnkResultCd = lnkResultCd;
    }

    public String getLnkResultMsg() {
        return lnkResultMsg;
    }

    public void setLnkResultMsg(String lnkResultMsg) {
        this.lnkResultMsg = lnkResultMsg;
    }

    public String getLnkResultDt() {
        return lnkResultDt;
    }

    public void setLnkResultDt(String lnkResultDt) {
        this.lnkResultDt = lnkResultDt;
    }

    public String getCmsResultCd() {
        return cmsResultCd;
    }

    public void setCmsResultCd(String cmsResultCd) {
        this.cmsResultCd = cmsResultCd;
    }

    public String getCmsResultMsg() {
        return cmsResultMsg;
    }

    public void setCmsResultMsg(String cmsResultMsg) {
        this.cmsResultMsg = cmsResultMsg;
    }

    public List<String> getRecvCompleteFiles() {
        return recvCompleteFiles;
    }

    public void setRecvCompleteFiles(List<String> recvCompleteFiles) {
        this.recvCompleteFiles = recvCompleteFiles;
    }


    /**
     * Equals & HashCode
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RequestAck)) return false;
        RequestAck that = (RequestAck) o;
        return Objects.equals(getLnkInterfaceId(), that.getLnkInterfaceId()) && Objects.equals(getLnkTransactionId(), that.getLnkTransactionId()) && Objects.equals(getLnkResultCd(), that.getLnkResultCd()) && Objects.equals(getLnkResultMsg(), that.getLnkResultMsg()) && Objects.equals(getLnkResultDt(), that.getLnkResultDt()) && Objects.equals(getCmsResultCd(), that.getCmsResultCd()) && Objects.equals(getCmsResultMsg(), that.getCmsResultMsg()) && Objects.equals(getRecvCompleteFiles(), that.getRecvCompleteFiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLnkInterfaceId(), getLnkTransactionId(), getLnkResultCd(), getLnkResultMsg(), getLnkResultDt(), getCmsResultCd(), getCmsResultMsg(), getRecvCompleteFiles());
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return "RequestAck{" +
                "lnkInterfaceId='" + lnkInterfaceId + '\'' +
                ", lnkTransactionId='" + lnkTransactionId + '\'' +
                ", lnkResultCd='" + lnkResultCd + '\'' +
                ", lnkResultMsg='" + lnkResultMsg + '\'' +
                ", lnkResultDt='" + lnkResultDt + '\'' +
                ", cmsResultCd='" + cmsResultCd + '\'' +
                ", cmsResultMsg='" + cmsResultMsg + '\'' +
                ", recvCompleteFiles=" + recvCompleteFiles +
                '}';
    }
}
