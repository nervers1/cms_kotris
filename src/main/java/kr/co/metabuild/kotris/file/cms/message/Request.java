package kr.co.metabuild.kotris.file.cms.message;

import java.util.Objects;

/**
 * CMSRequest - CMS 요청
 */
public class Request {

    /**
     * HEADER
     */
    // 연계_인터페이스
    String lnkInterfaceId;
    // 연계_트렌젝션
    String lnkTransactionId;
    // 연계_패턴
    String lnkPattern;
    // 연계_목적지기관
    String lnkTrgOrg;
    // 연계_이용기관(시스템)
    String lnkSrcOrg;


    /**
     * BODY
     */
    // 파일 바이너리(base64)
    String data;
    // 파일명
    String fileName;
    // 로그인 사용자명
    String userName;
    // 로그인 패스워드
    String userPassword;
    // 이용기관코드
    String orgCode;
    // 송수신 구분
    String srFlag;
    // 요청날짜(취소/조회 전용)
    String yyyymmdd;


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

    public String getLnkPattern() {
        return lnkPattern;
    }

    public void setLnkPattern(String lnkPattern) {
        this.lnkPattern = lnkPattern;
    }

    public String getLnkTrgOrg() {
        return lnkTrgOrg;
    }

    public void setLnkTrgOrg(String lnkTrgOrg) {
        this.lnkTrgOrg = lnkTrgOrg;
    }

    public String getLnkSrcOrg() {
        return lnkSrcOrg;
    }

    public void setLnkSrcOrg(String lnkSrcOrg) {
        this.lnkSrcOrg = lnkSrcOrg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getSrFlag() {
        return srFlag;
    }

    public void setSrFlag(String srFlag) {
        this.srFlag = srFlag;
    }

    public String getYyyymmdd() {
        return yyyymmdd;
    }

    public void setYyyymmdd(String yyyymmdd) {
        this.yyyymmdd = yyyymmdd;
    }


    /**
     * Equals & HashCode
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Request)) return false;
        Request request = (Request) o;
        return Objects.equals(getLnkInterfaceId(), request.getLnkInterfaceId()) && Objects.equals(getLnkTransactionId(), request.getLnkTransactionId()) && Objects.equals(getLnkPattern(), request.getLnkPattern()) && Objects.equals(getLnkTrgOrg(), request.getLnkTrgOrg()) && Objects.equals(getLnkSrcOrg(), request.getLnkSrcOrg()) && Objects.equals(getData(), request.getData()) && Objects.equals(getFileName(), request.getFileName()) && Objects.equals(getUserName(), request.getUserName()) && Objects.equals(getUserPassword(), request.getUserPassword()) && Objects.equals(getOrgCode(), request.getOrgCode()) && Objects.equals(getSrFlag(), request.getSrFlag()) && Objects.equals(getYyyymmdd(), request.getYyyymmdd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLnkInterfaceId(), getLnkTransactionId(), getLnkPattern(), getLnkTrgOrg(), getLnkSrcOrg(), getData(), getFileName(), getUserName(), getUserPassword(), getOrgCode(), getSrFlag(), getYyyymmdd());
    }


    /**
     * toString
     */
    @Override
    public String toString() {
        return "Request{" +
                "lnkInterfaceId='" + lnkInterfaceId + '\'' +
                ", lnkTransactionId='" + lnkTransactionId + '\'' +
                ", lnkPattern='" + lnkPattern + '\'' +
                ", lnkTrgOrg='" + lnkTrgOrg + '\'' +
                ", lnkSrcOrg='" + lnkSrcOrg + '\'' +
                ", data='" + data + '\'' +
                ", fileName='" + fileName + '\'' +
                ", userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", orgCode='" + orgCode + '\'' +
                ", srFlag='" + srFlag + '\'' +
                ", yyyymmdd='" + yyyymmdd + '\'' +
                '}';
    }
}
