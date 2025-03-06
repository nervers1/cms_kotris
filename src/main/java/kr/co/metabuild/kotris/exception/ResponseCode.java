package kr.co.metabuild.kotris.exception;

/**
 * 일괄처리 응답코드
 */
public enum ResponseCode {
    RES_000("000", "정상"),
    RES_001("001", "전문길이 오류"),
    RES_002("002", "수신측 응답 및 처리불가"),
    RES_090("090", "시스템장애"),
    RES_310("310", "송신자명 오류"),
    RES_320("320", "송신자 암호 오류"),
    RES_630("630", "기전송 완료"),
    RES_631("631", "해당기관 미등록 업무"),
    RES_632("632", "비정상 파일명"),
    RES_633("633", "비정상 BYTE 수"),
    RES_800("800", "FORMAT 오류"),
    RES_999("999", "기타 오류"),
    RES_E01("E01", "파일명오류"),
    RES_E02("E02", "외부Connection실패"),
    RES_E03("E03", "Record길이오류"),
    RES_E04("E04", "파일미등록오류"),
    RES_NON("NON", "전송자료없음"),
    RES_SPACE("   ", "응답전문이 아닌 경우");

    private final String code;
    private final String desc;

    private ResponseCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
