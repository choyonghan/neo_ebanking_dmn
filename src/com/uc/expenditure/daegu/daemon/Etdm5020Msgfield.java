/**
 *  주시스템명 : 전북은행 e-세출 시스템
 *  업  무  명 : 공통
 *  기  능  명 : 세입정보 전문을 읽어서 DB 생성
 *  클래스  ID : Etdm1800
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자      소속      일자            Tag           내용
 * ------------------------------------------------------------------------
 *  신상훈       다산시스템(주)      2015.11.XX         %01%         최초작성
 *
 */

package com.uc.expenditure.daegu.daemon;

import com.uc.framework.utils.*;

public class Etdm5020Msgfield extends Etmd_WorkField {

    public Etdm5020Msgfield() {

        super();

        // 반복부-header_tailer(540)
        // body length(173)
        this.addRepetField("입금일련번호", 7, "C");       // 입금일련번호 (ASIS vchar7 -> TOBE number 20)
        this.addRepetField("입금은행코드", 3, "C");       // 입금은행코드
        this.addRepetField("입금유형", 2, "C");       // 입금유형
        this.addRepetField("입금계좌번호", 16, "C");       // 입금계좌번호
        this.addRepetField("입금계좌예금주명", 40, "C");       // 입금계좌예금주명
        this.addRepetField("입금적요", 20, "C");       // 입금적요
        this.addRepetField("이체금액", 11, "H");       // 이체금액
        this.addRepetField("거래구분", 2, "C");       // 거래구분
        this.addRepetField("cms번호", 20, "C");       // cms번호
        this.addRepetField("압류방지코드", 2, "C");       // 압류방지코드
        this.addRepetField("처리여부", 1, "C");       // 처리여부
        this.addRepetField("이체처리불능코드", 7, "C");       // 이체처리불능코드
        this.addRepetField("이체처리불능내용", 42, "C");       // 이체처리불능내용
    }
}
