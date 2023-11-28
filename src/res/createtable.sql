--------------------------------------------------------
--  DDL for Table 계좌소관정보
--------------------------------------------------------

  DROP TABLE "계좌소관정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "계좌소관정보" 
   (	"회계연도" VARCHAR2(4 BYTE), 
	"회계코드" VARCHAR2(4 BYTE), 
	"계좌소관" VARCHAR2(4 BYTE), 
	"계좌성격" VARCHAR2(1 BYTE), 
	"계좌번호" VARCHAR2(19 BYTE), 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 공지사항
--------------------------------------------------------

  DROP TABLE "공지사항" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "공지사항" 
   (	"자치단체코드" VARCHAR2(7 BYTE), 
	"등록순번" NUMBER(6,0), 
	"제목" VARCHAR2(200 BYTE), 
	"내용" VARCHAR2(4000 BYTE), 
	"등록일자" VARCHAR2(8 BYTE), 
	"종료일자" VARCHAR2(8 BYTE), 
	"첨부파일" VARCHAR2(200 BYTE), 
	"공지구분코드" VARCHAR2(1 BYTE), 
	"긴급공지여부" VARCHAR2(1 BYTE), 
	"조회수" VARCHAR2(8 BYTE), 
	"등록자ID" VARCHAR2(13 BYTE), 
	"등록일시" DATE, 
	"메뉴시스템구분코드" CHAR(2 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 공통코드
--------------------------------------------------------

  DROP TABLE "공통코드" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "공통코드" 
   (	"분류코드" CHAR(6 BYTE), 
	"코드" VARCHAR2(20 BYTE), 
	"분류명" VARCHAR2(40 BYTE), 
	"코드명" VARCHAR2(40 BYTE), 
	"코드설명" VARCHAR2(100 BYTE), 
	"코드사용여부" CHAR(1 BYTE) DEFAULT 'N', 
	"등록일자" CHAR(8 BYTE), 
	"등록자ID" CHAR(7 BYTE), 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 급여관리
--------------------------------------------------------

  DROP TABLE "급여관리" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "급여관리" 
   (	"회계연도" VARCHAR2(4 BYTE), 
	"예산과목코드" VARCHAR2(5 BYTE), 
	"예산과목명" VARCHAR2(50 BYTE), 
	"급여구분" VARCHAR2(1 BYTE), 
	"등록일시" DATE, 
	"입금명세구분" VARCHAR2(1 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "급여관리"."예산과목코드" IS '급여코드 : 10101,10102,10103,10104, 그외코드';
 
   COMMENT ON COLUMN "급여관리"."급여구분" IS '1:급여/수당';

--------------------------------------------------------
--  DDL for Table 부서정보
--------------------------------------------------------

  DROP TABLE "부서정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "부서정보" 
   (	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"GCC부서코드" VARCHAR2(7 BYTE), 
	"부서코드" VARCHAR2(7 BYTE), 
	"회계코드" VARCHAR2(3 BYTE), 
	"부서명" VARCHAR2(200 BYTE), 
	"자치단체명" VARCHAR2(200 BYTE), 
	"관서명" VARCHAR2(200 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 사업정보
--------------------------------------------------------

  DROP TABLE "사업정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "사업정보" 
   (	"회계연도" VARCHAR2(4 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"GCC부서코드" VARCHAR2(7 BYTE), 
	"부서코드" VARCHAR2(7 BYTE), 
	"회계코드" VARCHAR2(3 BYTE), 
	"정책사업코드" VARCHAR2(16 BYTE), 
	"단위사업코드" VARCHAR2(16 BYTE), 
	"세부사업코드" VARCHAR2(16 BYTE), 
	"자치단체명" VARCHAR2(200 BYTE), 
	"관서명" VARCHAR2(200 BYTE), 
	"부서명" VARCHAR2(200 BYTE), 
	"회계명" VARCHAR2(200 BYTE), 
	"정책사업명" VARCHAR2(200 BYTE), 
	"단위사업명" VARCHAR2(200 BYTE), 
	"세부사업명" VARCHAR2(200 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 사용자별_회계관직
--------------------------------------------------------

  DROP TABLE "사용자별_회계관직" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "사용자별_회계관직" 
   (	"회계연도" VARCHAR2(4 BYTE), 
	"회계구분코드" VARCHAR2(3 BYTE), 
	"회계관직코드" VARCHAR2(4 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"부서코드" VARCHAR2(7 BYTE), 
	"GCC부서코드" VARCHAR2(7 BYTE), 
	"자료구분" VARCHAR2(2 BYTE), 
	"사용자ID" VARCHAR2(13 BYTE), 
	"사용자명" VARCHAR2(200 BYTE), 
	"회계관직명" VARCHAR2(200 BYTE), 
	"승인권한여부" VARCHAR2(1 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 사용자정보
--------------------------------------------------------

  DROP TABLE "사용자정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "사용자정보" 
   (	"회계연도" VARCHAR2(4 BYTE), 
	"회계구분" VARCHAR2(3 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"부서코드" VARCHAR2(7 BYTE), 
	"GCC부서코드" VARCHAR2(7 BYTE), 
	"사용자ID" VARCHAR2(13 BYTE), 
	"패스워드" VARCHAR2(20 BYTE), 
	"사용자명" VARCHAR2(200 BYTE), 
	"승인권한여부" VARCHAR2(1 BYTE) DEFAULT 'N', 
	"시스템권한" VARCHAR2(2 BYTE), 
	"이전로그인일시" DATE, 
	"최종로그인일시" DATE, 
	"사용중지일자" DATE, 
	"비밀번호변경일자" DATE, 
	"비밀번호오류횟수" NUMBER(4,0), 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 세입세출외현금
--------------------------------------------------------

  DROP TABLE "세입세출외현금" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "세입세출외현금" 
   (	"자치단체코드" VARCHAR2(7 BYTE), 
	"현금대분류코드" VARCHAR2(2 BYTE), 
	"현금중분류코드" VARCHAR2(3 BYTE), 
	"현금대분류명" VARCHAR2(40 BYTE), 
	"현금중분류명" VARCHAR2(40 BYTE), 
	"사용유무" VARCHAR2(1 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "세입세출외현금"."자치단체코드" IS '자치단체코드';
 
   COMMENT ON COLUMN "세입세출외현금"."현금대분류코드" IS '현금대분류코드';
 
   COMMENT ON COLUMN "세입세출외현금"."현금중분류코드" IS '현금중분류코드';
 
   COMMENT ON COLUMN "세입세출외현금"."현금대분류명" IS '현금대분류명';
 
   COMMENT ON COLUMN "세입세출외현금"."현금중분류명" IS '현금중분류명';
 
   COMMENT ON COLUMN "세입세출외현금"."사용유무" IS '사용유무';
 
   COMMENT ON TABLE "세입세출외현금"  IS '세입세출외현금의 현금유형 및 종류에 대한 분류정보 저장';

--------------------------------------------------------
--  DDL for Table 엑셀계좌검증
--------------------------------------------------------

  DROP TABLE "엑셀계좌검증" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "엑셀계좌검증" 
   (	"요청일자" CHAR(8 BYTE), 
	"요청일련번호" NUMBER(8,0), 
	"요청건수" NUMBER(8,0) DEFAULT 0, 
	"정상건수" NUMBER(8,0) DEFAULT 0, 
	"오류건수" NUMBER(8,0) DEFAULT 0, 
	"거래번호" VARCHAR2(14 BYTE), 
	"파일명" VARCHAR2(50 BYTE), 
	"파일경로" VARCHAR2(200 BYTE), 
	"작업상태코드" VARCHAR2(2 BYTE), 
	"오류메시지" VARCHAR2(200 BYTE), 
	"처리시작일시" DATE DEFAULT SYSDATE, 
	"처리종료일시" DATE DEFAULT SYSDATE, 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"요청부서코드" VARCHAR2(7 BYTE), 
	"요청자ID" VARCHAR2(13 BYTE), 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "엑셀계좌검증"."요청일자" IS '요청일자';
 
   COMMENT ON COLUMN "엑셀계좌검증"."요청일련번호" IS '엑셀계좌검증일련번호 시퀀스 관리';
 
   COMMENT ON COLUMN "엑셀계좌검증"."요청건수" IS '요청건수';
 
   COMMENT ON COLUMN "엑셀계좌검증"."정상건수" IS '정상건수';
 
   COMMENT ON COLUMN "엑셀계좌검증"."오류건수" IS '오류건수';
 
   COMMENT ON COLUMN "엑셀계좌검증"."거래번호" IS '이뱅킹계좌검증 파일번호';
 
   COMMENT ON COLUMN "엑셀계좌검증"."파일명" IS '파일명';
 
   COMMENT ON COLUMN "엑셀계좌검증"."파일경로" IS '파일경로';
 
   COMMENT ON COLUMN "엑셀계좌검증"."작업상태코드" IS '10:처리중 20:처리완료 30:오류';
 
   COMMENT ON COLUMN "엑셀계좌검증"."오류메시지" IS '오류메시지';
 
   COMMENT ON COLUMN "엑셀계좌검증"."처리시작일시" IS '처리시작일시';
 
   COMMENT ON COLUMN "엑셀계좌검증"."처리종료일시" IS '처리종료일시';
 
   COMMENT ON COLUMN "엑셀계좌검증"."자치단체코드" IS '자치단체코드';
 
   COMMENT ON COLUMN "엑셀계좌검증"."요청부서코드" IS '요청부서코드';
 
   COMMENT ON COLUMN "엑셀계좌검증"."요청자ID" IS '요청자ID';
 
   COMMENT ON COLUMN "엑셀계좌검증"."등록일시" IS '등록일시';
 
   COMMENT ON TABLE "엑셀계좌검증"  IS '엑셀파일에 정리된 계좌에 대한 검증 결과를 저장';

--------------------------------------------------------
--  DDL for Table 영업일
--------------------------------------------------------

  DROP TABLE "영업일" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "영업일" 
   (	"기준일자" CHAR(8 BYTE), 
	"휴일명" VARCHAR2(100 BYTE), 
	"휴일영문명" VARCHAR2(100 BYTE), 
	"휴일여부" CHAR(1 BYTE) DEFAULT 'N', 
	"음력일자" CHAR(8 BYTE), 
	"주차수" NUMBER, 
	"요일구분코드" VARCHAR2(1 BYTE), 
	"휴일구분코드" VARCHAR2(1 BYTE), 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "영업일"."휴일여부" IS 'Y : 여
N : 부';

--------------------------------------------------------
--  DDL for Table 예외계좌정보
--------------------------------------------------------

  DROP TABLE "예외계좌정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "예외계좌정보" 
   (	"지급명령일자" VARCHAR2(8 BYTE), 
	"은행코드" VARCHAR2(3 BYTE), 
	"계좌번호" VARCHAR2(16 BYTE), 
	"예금주" VARCHAR2(200 BYTE), 
	"요청자" VARCHAR2(200 BYTE), 
	"부서코드" VARCHAR2(7 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 이뱅킹계좌검증
--------------------------------------------------------

  DROP TABLE "이뱅킹계좌검증" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "이뱅킹계좌검증" 
   (	"자치단체코드" VARCHAR2(7 BYTE), 
	"지출단계" CHAR(1 BYTE), 
	"지출번호구분" VARCHAR2(14 BYTE), 
	"지출순번" VARCHAR2(8 BYTE), 
	"검증순번" NUMBER, 
	"거래처명" VARCHAR2(200 BYTE), 
	"예금주" VARCHAR2(200 BYTE), 
	"은행" VARCHAR2(3 BYTE), 
	"계좌번호" VARCHAR2(20 BYTE), 
	"예금주주민번호" VARCHAR2(13 BYTE), 
	"요청상태" VARCHAR2(3 BYTE), 
	"검증결과" VARCHAR2(1 BYTE), 
	"오류사유" VARCHAR2(1000 BYTE), 
	"정상예금주" VARCHAR2(200 BYTE), 
	"요청일시" DATE, 
	"요청자ID" VARCHAR2(13 BYTE), 
	"검증일시" DATE, 
	"작업시스템코드" VARCHAR2(2 BYTE), 
	"작업상태코드" VARCHAR2(2 BYTE), 
	"거래번호" VARCHAR2(14 BYTE), 
	"등록일시" DATE DEFAULT SYSDATE, 
	"지급금액" NUMBER(17,0) DEFAULT 0, 
	"요청일자" VARCHAR2(8 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "이뱅킹계좌검증"."자치단체코드" IS '자치단체코드';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."지출단계" IS 'S:품의, C:원인행위, R:결의, P:지급명령';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."지출번호구분" IS 'S/C/R : 회계연도 + 회계구분 + 부서코드 P : 회계연도 + 회계구분 + 경비구분';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."지출순번" IS 'S/C/R : 각 단계별 SNO P : 지급명령번호';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."검증순번" IS '채주순번';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."거래처명" IS '거래처명';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."예금주" IS '예금주성명';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."은행" IS '은행코드 3자리';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."계좌번호" IS '-는 제외한 숫자만 입력';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."예금주주민번호" IS '예금주 주민번호(-는 제외) 예금주가 법인일 경우 법인번호 의사결정기관의 의사결정 후 값 입력';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."요청상태" IS 'R:검증의뢰상태 S:E뱅킹/E세출 검증 진행중 E:검증완료 이호조에서 요청시 R E뱅킹/E세출에서 S,E';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."검증결과" IS 'Y:정상계좌 N:오류계좌';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."오류사유" IS '오류사유';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."정상예금주" IS '정상예금주';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."요청일시" IS '요청일시';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."요청자ID" IS '요청자ID';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."검증일시" IS 'E뱅킹/E세출 시스템에서 검증결과 UPDT 한 일시';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."작업시스템코드" IS '01: 뱅킹 02: 세입금 03: 계정계';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."작업상태코드" IS '01:e-호조 자료수신,05:계좌검증요청,06:계좌검증완료,11:세입금 파일생성12:세입금 파일송신,21:계정계 파일송신,22:계정계 처리요청_전문,23:계정계 처리응답_전문,25:계정계 파일수신,26:b뱅킹 파일송신,31:b뱅킹 처리,32:e-호조 처리';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."거래번호" IS '거래번호';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."등록일시" IS '등록일시';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."지급금액" IS '지급금액';
 
   COMMENT ON COLUMN "이뱅킹계좌검증"."요청일자" IS '요청일자';
 
   COMMENT ON TABLE "이뱅킹계좌검증"  IS '이뱅킹 계좌검증 결과 및 오류내역을 저장';

--------------------------------------------------------
--  DDL for Table 입금명세
--------------------------------------------------------

  DROP TABLE "입금명세" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "입금명세" 
   (	"요청ID" VARCHAR2(2 BYTE), 
	"요청기관구분" VARCHAR2(2 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"부서코드" VARCHAR2(7 BYTE), 
	"회계연도" VARCHAR2(4 BYTE), 
	"회계코드" VARCHAR2(3 BYTE), 
	"자료구분" VARCHAR2(2 BYTE), 
	"지급명령등록번호" VARCHAR2(8 BYTE), 
	"입금일련번호" VARCHAR2(7 BYTE), 
	"거래일자" VARCHAR2(8 BYTE), 
	"기관구분" VARCHAR2(2 BYTE), 
	"지급명령번호" VARCHAR2(10 BYTE), 
	"입금유형" VARCHAR2(2 BYTE), 
	"입금은행코드" VARCHAR2(3 BYTE), 
	"입금계좌번호" VARCHAR2(16 BYTE), 
	"입금계좌예금주명" VARCHAR2(200 BYTE), 
	"입금명세" VARCHAR2(200 BYTE), 
	"입금금액" NUMBER(15,0), 
	"CMS번호" VARCHAR2(20 BYTE), 
	"자료수신여부" VARCHAR2(1 BYTE), 
	"자료수신일시" DATE, 
	"자료수신자명" VARCHAR2(20 BYTE), 
	"결과처리여부" VARCHAR2(1 BYTE), 
	"결과코드" VARCHAR2(7 BYTE), 
	"결과설명" VARCHAR2(80 BYTE), 
	"결과처리자명" VARCHAR2(20 BYTE), 
	"결과처리일시" DATE, 
	"재배정여부" VARCHAR2(1 BYTE), 
	"지급형태" VARCHAR2(2 BYTE), 
	"현금유형코드" VARCHAR2(2 BYTE), 
	"현금종류코드" VARCHAR2(3 BYTE), 
	"현금종류명" VARCHAR2(100 BYTE), 
	"등록일시" DATE DEFAULT SYSDATE, 
	"지급부서코드" VARCHAR2(7 BYTE), 
	"채주SMS수신번호" VARCHAR2(20 BYTE), 
	"압류방지코드" VARCHAR2(2 BYTE), 
	"세목코드참조여부" VARCHAR2(1 BYTE), 
	"세목코드" VARCHAR2(2 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "입금명세"."요청ID" IS 'LF:지방재정관리시스템';
 
   COMMENT ON COLUMN "입금명세"."요청기관구분" IS '01:일반회계, 02:특별회계, 03:공기업특별회계, 04:기금';
 
   COMMENT ON COLUMN "입금명세"."자치단체코드" IS '예)서울특별시:6110000 ,성동구:3030000, 접속시스템코드';
 
   COMMENT ON COLUMN "입금명세"."관서코드" IS '사업소코드예)남부도로관리사업소:0260 , 성동구보건소:0020';
 
   COMMENT ON COLUMN "입금명세"."부서코드" IS '부서코드';
 
   COMMENT ON COLUMN "입금명세"."회계연도" IS 'YYYY';
 
   COMMENT ON COLUMN "입금명세"."회계코드" IS '자치기관이 정의한 회계구분 코드(기관마다 다름)예) 100:일반회계, 201:주차장특별회계, 301:상수도사업특별회계';
 
   COMMENT ON COLUMN "입금명세"."자료구분" IS '10:일반지출(일반회계,특별회계)  ,20:일상경비 ,30:임시일상경비  ,40:도급경비,45:자금배정   ,46:자금환수   ,60:세입세출외현금지출  ,80:구청보조금교부용지출,90:일상경비교부용지출';
 
   COMMENT ON COLUMN "입금명세"."지급명령등록번호" IS 'E-HOJO 지급명령일련번호';
 
   COMMENT ON COLUMN "입금명세"."입금일련번호" IS '지급명령번호별 일련번호(0000001 ~ 9999999)';
 
   COMMENT ON COLUMN "입금명세"."거래일자" IS 'YYYYMMDD';
 
   COMMENT ON COLUMN "입금명세"."기관구분" IS '20:시비   30:구비';
 
   COMMENT ON COLUMN "입금명세"."지급명령번호" IS 'E-HOJO 지급명령번호';
 
   COMMENT ON COLUMN "입금명세"."입금유형" IS '10:계좌이체 , 20:대량이체 , 30:원천징수 , 40:고지서,50:CMS , 60:수표 , 80:보조금 , 99:현금(자료구분 일반지출(10), 일상경비(20) 경우만)';
 
   COMMENT ON COLUMN "입금명세"."입금은행코드" IS '입금은행코드';
 
   COMMENT ON COLUMN "입금명세"."입금계좌번호" IS '입금유형에 따라 필수, 옵션임 1.필수 : 10(계좌이체), 30(원천징수), 50(CMS), 80(보조금) 2.옵션 : 20(대량이체), 40(고지서), 60(수표), 99(현금)';
 
   COMMENT ON COLUMN "입금명세"."입금계좌예금주명" IS '입금계좌예금주명';
 
   COMMENT ON COLUMN "입금명세"."입금명세" IS '입금통장적요';
 
   COMMENT ON COLUMN "입금명세"."입금금액" IS '입금금액';
 
   COMMENT ON COLUMN "입금명세"."CMS번호" IS '입금유형 50(CMS)시만 SET';
 
   COMMENT ON COLUMN "입금명세"."자료수신여부" IS 'Y:수신 , N:미수신  (default=N), B:반려, D:삭제';
 
   COMMENT ON COLUMN "입금명세"."자료수신일시" IS '재무회계에서 수신 처리한 일시';
 
   COMMENT ON COLUMN "입금명세"."자료수신자명" IS '재무회계에서 수신 처리한 처리담당자 성명';
 
   COMMENT ON COLUMN "입금명세"."결과처리여부" IS 'Y:처리 , N:미처리  (default=N)';
 
   COMMENT ON COLUMN "입금명세"."결과코드" IS '0000:정상 , XXXX(임의의 숫자or문자):비정상';
 
   COMMENT ON COLUMN "입금명세"."결과설명" IS '오류시 ERROR 내용';
 
   COMMENT ON COLUMN "입금명세"."결과처리자명" IS '영업점 처리자';
 
   COMMENT ON COLUMN "입금명세"."결과처리일시" IS '영업점 처리일시 YYYYMMDD hh:mm:ss';
 
   COMMENT ON COLUMN "입금명세"."재배정여부" IS 'Y:재배정자료, N : 일반배정자료';
 
   COMMENT ON COLUMN "입금명세"."지급형태" IS 'Y:재배정자료, N : 일반배정자료';
 
   COMMENT ON COLUMN "입금명세"."현금유형코드" IS '세입세출외현금    현금유형코드 (대분류)';
 
   COMMENT ON COLUMN "입금명세"."현금종류코드" IS '세입세출외현금    현금종류코드 (중분류)';
 
   COMMENT ON COLUMN "입금명세"."현금종류명" IS '세입세출외현금    현금종류명';
 
   COMMENT ON COLUMN "입금명세"."등록일시" IS '등록일시';
 
   COMMENT ON COLUMN "입금명세"."지급부서코드" IS '예)도로보수과:1234567 , 보건행정과 :1234567,품의부서 su_ibuseo(일상경비), ibuseo(지출)';
 
   COMMENT ON COLUMN "입금명세"."채주SMS수신번호" IS '채주SMS수신번호';
 
   COMMENT ON COLUMN "입금명세"."압류방지코드" IS '압류방지코드';
 
   COMMENT ON COLUMN "입금명세"."세목코드참조여부" IS '세목코드참조여부';
 
   COMMENT ON COLUMN "입금명세"."세목코드" IS '세목코드';
 
   COMMENT ON TABLE "입금명세"  IS '지급이체 거래에서 입금관련(일반지출, 자금배정, 일상경비, 세입세출외현금) 명세 저장';

--------------------------------------------------------
--  DDL for Table 입금반려명세
--------------------------------------------------------

  DROP TABLE "입금반려명세" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "입금반려명세" 
   (	"요청ID" VARCHAR2(2 BYTE), 
	"요청기관구분" VARCHAR2(2 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"부서코드" VARCHAR2(7 BYTE), 
	"회계연도" VARCHAR2(4 BYTE), 
	"회계코드" VARCHAR2(3 BYTE), 
	"자료구분" VARCHAR2(2 BYTE), 
	"지급명령등록번호" VARCHAR2(8 BYTE), 
	"입금일련번호" VARCHAR2(7 BYTE), 
	"거래일자" VARCHAR2(8 BYTE), 
	"기관구분" VARCHAR2(2 BYTE), 
	"지급명령번호" VARCHAR2(8 BYTE), 
	"입금유형" VARCHAR2(2 BYTE), 
	"입금은행코드" VARCHAR2(3 BYTE), 
	"입금계좌번호" VARCHAR2(16 BYTE), 
	"입금계좌예금주명" VARCHAR2(200 BYTE), 
	"입금명세" VARCHAR2(200 BYTE), 
	"입금금액" NUMBER(15,0), 
	"CMS번호" VARCHAR2(20 BYTE), 
	"자료수신여부" VARCHAR2(1 BYTE), 
	"자료수신일시" DATE, 
	"자료수신자명" VARCHAR2(20 BYTE), 
	"결과처리여부" VARCHAR2(1 BYTE), 
	"결과코드" VARCHAR2(4 BYTE), 
	"결과설명" VARCHAR2(80 BYTE), 
	"결과처리자명" VARCHAR2(20 BYTE), 
	"결과처리일시" DATE, 
	"재배정여부" VARCHAR2(1 BYTE), 
	"삭제일자" DATE, 
	"삭제자ID" VARCHAR2(13 BYTE), 
	"삭제사유" VARCHAR2(200 BYTE), 
	"지급부서코드" VARCHAR2(7 BYTE), 
	"지급형태" VARCHAR2(2 BYTE), 
	"삭제이력순번" NUMBER(3,0), 
	"현금유형코드" VARCHAR2(2 BYTE), 
	"현금종류코드" VARCHAR2(3 BYTE), 
	"현금종류명" VARCHAR2(100 BYTE), 
	"등록일시" DATE, 
	"요구부서코드" VARCHAR2(7 BYTE), 
	"채주SMS수신번호" VARCHAR2(20 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "입금반려명세"."요청ID" IS 'LF:지방재정관리시스템';
 
   COMMENT ON COLUMN "입금반려명세"."요청기관구분" IS '01:일반회계, 02:특별회계, 03:공기업특별회계, 04:기금';
 
   COMMENT ON COLUMN "입금반려명세"."자치단체코드" IS '예)서울특별시:6110000 ,성동구:3030000, 접속시스템코드';
 
   COMMENT ON COLUMN "입금반려명세"."관서코드" IS '사업소코드 예)남부도로관리사업소:0260 , 성동구보건소:0020';
 
   COMMENT ON COLUMN "입금반려명세"."부서코드" IS '부서코드';
 
   COMMENT ON COLUMN "입금반려명세"."회계연도" IS 'YYYY';
 
   COMMENT ON COLUMN "입금반려명세"."회계코드" IS '자치기관이 정의한 회계구분 코드(기관마다 다름)예) 100:일반회계, 201:주차장특별회계, 301:상수도사업특별회계';
 
   COMMENT ON COLUMN "입금반려명세"."자료구분" IS '10:일반지출(일반회계,특별회계),20:일상경비 ,30:임시일상경비  ,40:도급경비,45:자금배정   ,46:자금환수   ,60:세입세출외현금지출  ,80:구청보조금교부용지출,90:일상경비교부용지출';
 
   COMMENT ON COLUMN "입금반려명세"."지급명령등록번호" IS 'E-HOJO지급명령일련번호';
 
   COMMENT ON COLUMN "입금반려명세"."입금일련번호" IS '지급명령번호별 일련번호(0000001 ~ 9999999)';
 
   COMMENT ON COLUMN "입금반려명세"."거래일자" IS 'YYYYMMDD';
 
   COMMENT ON COLUMN "입금반려명세"."기관구분" IS '20:시비   30:구비';
 
   COMMENT ON COLUMN "입금반려명세"."지급명령번호" IS '지급명령번호';
 
   COMMENT ON COLUMN "입금반려명세"."입금유형" IS '10:계좌이체 , 20:대량이체 , 30:원천징수 , 40:고지서,50:CMS , 60:수표 , 80:보조금 , 99:현금(자료구분 일반지출(10), 일상경비(20) 경우만)';
 
   COMMENT ON COLUMN "입금반려명세"."입금은행코드" IS '은행코드 3자리';
 
   COMMENT ON COLUMN "입금반려명세"."입금계좌번호" IS '입금유형에 따라 필수, 옵션임 1.필수 : 10(계좌이체), 30(원천징수), 50(CMS), 80(보조금) 2.옵션 : 20(대량이체), 40(고지서), 60(수표), 99(현금)';
 
   COMMENT ON COLUMN "입금반려명세"."입금계좌예금주명" IS '입금계좌예금주명';
 
   COMMENT ON COLUMN "입금반려명세"."입금명세" IS '입금통장적요';
 
   COMMENT ON COLUMN "입금반려명세"."입금금액" IS '입금금액';
 
   COMMENT ON COLUMN "입금반려명세"."CMS번호" IS '입금유형 50(CMS)시만 SET';
 
   COMMENT ON COLUMN "입금반려명세"."자료수신여부" IS 'Y:수신 , N:미수신  (default=N), B:반려, D:삭제';
 
   COMMENT ON COLUMN "입금반려명세"."자료수신일시" IS '재무회계에서 수신 처리한 일시';
 
   COMMENT ON COLUMN "입금반려명세"."자료수신자명" IS '재무회계에서 수신 처리한 처리담당자 성명';
 
   COMMENT ON COLUMN "입금반려명세"."결과처리여부" IS 'Y:처리 , N:미처리  (default=N)';
 
   COMMENT ON COLUMN "입금반려명세"."결과코드" IS '0000:정상 , XXXX(임의의 숫자or문자):비정상';
 
   COMMENT ON COLUMN "입금반려명세"."결과설명" IS '오류시 ERROR 내용';
 
   COMMENT ON COLUMN "입금반려명세"."결과처리자명" IS '영업점 처리자';
 
   COMMENT ON COLUMN "입금반려명세"."결과처리일시" IS '영업점 처리일시 YYYYMMDD hh:mm:ss';
 
   COMMENT ON COLUMN "입금반려명세"."재배정여부" IS 'Y:재배정자료, N : 일반배정자료';
 
   COMMENT ON COLUMN "입금반려명세"."삭제일자" IS '삭제일자';
 
   COMMENT ON COLUMN "입금반려명세"."삭제자ID" IS '삭제자ID';
 
   COMMENT ON COLUMN "입금반려명세"."삭제사유" IS '삭제사유';
 
   COMMENT ON COLUMN "입금반려명세"."지급부서코드" IS '예)도로보수과:1234567 , 보건행정과 :1234567,품의부서';
 
   COMMENT ON COLUMN "입금반려명세"."지급형태" IS '통상,집합,현금 구분';
 
   COMMENT ON COLUMN "입금반려명세"."삭제이력순번" IS '삭제이력순번';
 
   COMMENT ON COLUMN "입금반려명세"."현금유형코드" IS '세입세출외현금    현금유형코드 (대분류)';
 
   COMMENT ON COLUMN "입금반려명세"."현금종류코드" IS '세입세출외현금    현금종류코드 (중분류)';
 
   COMMENT ON COLUMN "입금반려명세"."현금종류명" IS '세입세출외현금    현금종류명';
 
   COMMENT ON COLUMN "입금반려명세"."등록일시" IS 'YYYYMMDD hh:mi:ss';
 
   COMMENT ON COLUMN "입금반려명세"."요구부서코드" IS '요구부서코드';
 
   COMMENT ON COLUMN "입금반려명세"."채주SMS수신번호" IS '채주SMS수신번호';
 
   COMMENT ON TABLE "입금반려명세"  IS '지급이체 거래에서 입금반려(일반지출, 자금배정, 일상경비, 세입세출외현금) 명세 저장';

--------------------------------------------------------
--  DDL for Table 자금관리
--------------------------------------------------------

  DROP TABLE "자금관리" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "자금관리" 
   (	"회계연도" NUMBER(4,0), 
	"등록순번" NUMBER(5,0), 
	"회계종류" VARCHAR2(2 BYTE), 
	"공금계좌" VARCHAR2(16 BYTE), 
	"예치종류" VARCHAR2(2 BYTE), 
	"예치이율" NUMBER(7,4), 
	"예치금액" NUMBER(15,0), 
	"예치계좌" VARCHAR2(16 BYTE), 
	"신규일자" VARCHAR2(8 BYTE), 
	"만기일자" VARCHAR2(8 BYTE), 
	"예치일수" NUMBER(5,0), 
	"해지일자" DATE, 
	"해지금액" NUMBER(15,0), 
	"예치상태" VARCHAR2(2 BYTE), 
	"등록일시" DATE, 
	"수정일시" DATE, 
	"등록자" VARCHAR2(20 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 전문순번
--------------------------------------------------------

  DROP TABLE "전문순번" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "전문순번" 
   (	"거래구분" VARCHAR2(2 BYTE), 
	"거래일자" VARCHAR2(8 BYTE), 
	"점번" VARCHAR2(3 BYTE), 
	"순번" NUMBER(8,0) DEFAULT 0, 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 점번정보
--------------------------------------------------------

  DROP TABLE "점번정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "점번정보" 
   (	"점번" VARCHAR2(3 BYTE), 
	"구청명" VARCHAR2(20 BYTE), 
	"관리영업점명" VARCHAR2(20 BYTE), 
	"모점" VARCHAR2(3 BYTE), 
	"모점명" VARCHAR2(20 BYTE), 
	"사업본부구분명" VARCHAR2(10 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"자치단체명" VARCHAR2(200 BYTE), 
	"링크명" VARCHAR2(20 BYTE), 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 지급계좌정보
--------------------------------------------------------

  DROP TABLE "지급계좌정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "지급계좌정보" 
   (	"순번" NUMBER(4,0), 
	"지출자금구분" VARCHAR2(2 BYTE), 
	"회계연도" VARCHAR2(4 BYTE), 
	"회계구분코드" VARCHAR2(3 BYTE), 
	"경비구분" VARCHAR2(3 BYTE), 
	"회계관직구분" VARCHAR2(3 BYTE), 
	"부서코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"은행코드" VARCHAR2(3 BYTE), 
	"계좌번호" VARCHAR2(30 BYTE), 
	"관리번호" VARCHAR2(2 BYTE), 
	"사용자명" VARCHAR2(200 BYTE), 
	"사용유무" VARCHAR2(1 BYTE), 
	"등록자ID" VARCHAR2(13 BYTE), 
	"변경자ID" VARCHAR2(13 BYTE), 
	"변경일자" DATE, 
	"비고" VARCHAR2(200 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 지급반려명세
--------------------------------------------------------

  DROP TABLE "지급반려명세" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "지급반려명세" 
   (	"요청ID" VARCHAR2(2 BYTE), 
	"요청기관구분" VARCHAR2(2 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"지급부서코드" VARCHAR2(7 BYTE), 
	"회계연도" VARCHAR2(4 BYTE), 
	"회계코드" VARCHAR2(3 BYTE), 
	"자료구분" VARCHAR2(2 BYTE), 
	"지급명령등록번호" VARCHAR2(8 BYTE), 
	"재배정여부" VARCHAR2(1 BYTE), 
	"삭제이력순번" NUMBER(3,0), 
	"거래일자" VARCHAR2(8 BYTE), 
	"기관구분" VARCHAR2(2 BYTE), 
	"지급명령번호" VARCHAR2(8 BYTE), 
	"요구부서코드" VARCHAR2(7 BYTE), 
	"정책사업코드" VARCHAR2(16 BYTE), 
	"단위사업코드" VARCHAR2(16 BYTE), 
	"세부사업코드" VARCHAR2(16 BYTE), 
	"분야코드" VARCHAR2(3 BYTE), 
	"부문코드" VARCHAR2(3 BYTE), 
	"예산과목코드" VARCHAR2(5 BYTE), 
	"출금은행코드" VARCHAR2(3 BYTE), 
	"출금계좌번호" VARCHAR2(16 BYTE), 
	"출금금액" NUMBER(15,0), 
	"출금명세" VARCHAR2(20 BYTE), 
	"요청정보" VARCHAR2(200 BYTE), 
	"입금총건수" NUMBER(5,0), 
	"요청일시" DATE, 
	"자치단체명" VARCHAR2(200 BYTE), 
	"관서명" VARCHAR2(200 BYTE), 
	"지급부서명" VARCHAR2(100 BYTE), 
	"회계명" VARCHAR2(200 BYTE), 
	"정책사업명" VARCHAR2(200 BYTE), 
	"단위사업명" VARCHAR2(200 BYTE), 
	"세부사업명" VARCHAR2(200 BYTE), 
	"분야명" VARCHAR2(50 BYTE), 
	"부문명" VARCHAR2(50 BYTE), 
	"예산과목명" VARCHAR2(50 BYTE), 
	"요구부서명" VARCHAR2(100 BYTE), 
	"지출원ID" VARCHAR2(13 BYTE), 
	"지출원담당자명" VARCHAR2(200 BYTE), 
	"지출원전화번호" VARCHAR2(30 BYTE), 
	"자료수신여부" VARCHAR2(1 BYTE), 
	"자료수신일시" DATE, 
	"자료수신자명" VARCHAR2(20 BYTE), 
	"결과처리여부" VARCHAR2(1 BYTE), 
	"결과코드" VARCHAR2(4 BYTE), 
	"결과설명" VARCHAR2(80 BYTE), 
	"결과처리자명" VARCHAR2(20 BYTE), 
	"결과처리일시" DATE, 
	"삭제일자" DATE, 
	"삭제자ID" VARCHAR2(13 BYTE), 
	"삭제사유" VARCHAR2(400 BYTE), 
	"지급형태" VARCHAR2(2 BYTE), 
	"현금유형코드" VARCHAR2(2 BYTE), 
	"현금종류코드" VARCHAR2(3 BYTE), 
	"현금종류명" VARCHAR2(100 BYTE), 
	"복지급여여부" VARCHAR2(1 BYTE), 
	"거래번호" VARCHAR2(14 BYTE), 
	"작업시스템코드" VARCHAR2(2 BYTE), 
	"작업상태코드" VARCHAR2(2 BYTE), 
	"처리건수" NUMBER(8,0), 
	"오류건수" NUMBER(8,0), 
	"처리금액" NUMBER(15,0), 
	"오류금액" NUMBER(15,0), 
	"등록일시" DATE, 
	"이체일자" VARCHAR2(8 BYTE), 
	"지급부서SMS발신번호" VARCHAR2(20 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "지급반려명세"."요청ID" IS 'LF:지방재정관리시스템';
 
   COMMENT ON COLUMN "지급반려명세"."요청기관구분" IS '01:일반회계, 02:특별회계, 03:공기업특별회계, 04:기금';
 
   COMMENT ON COLUMN "지급반려명세"."자치단체코드" IS '예)서울특별시:6110000 ,성동구:3030000, 접속시스템코드';
 
   COMMENT ON COLUMN "지급반려명세"."관서코드" IS '사업소코드 예)남부도로관리사업소:0260 , 성동구보건소:0020';
 
   COMMENT ON COLUMN "지급반려명세"."지급부서코드" IS '예)도로보수과:1234567 , 보건행정과 :1234567,품의부서';
 
   COMMENT ON COLUMN "지급반려명세"."회계연도" IS 'YYYY';
 
   COMMENT ON COLUMN "지급반려명세"."회계코드" IS '자치기관이 정의한 회계구분 코드(기관마다 다름)예) 100:일반회계, 201:주차장특별회계, 301:상수도사업특별회계';
 
   COMMENT ON COLUMN "지급반려명세"."자료구분" IS '10:일반지출(일반회계,특별회계),20:일상경비 ,30:임시일상경비  ,40:도급경비,45:자금배정   ,46:자금환수   ,60:세입세출외현금지출  ,80:구청보조금교부용지출,90:일상경비교부용지출';
 
   COMMENT ON COLUMN "지급반려명세"."지급명령등록번호" IS 'E-HOJO 지급명령일련번호';
 
   COMMENT ON COLUMN "지급반려명세"."재배정여부" IS 'Y:재배정자료, N : 일반배정자료';
 
   COMMENT ON COLUMN "지급반려명세"."삭제이력순번" IS '삭제이력순번';
 
   COMMENT ON COLUMN "지급반려명세"."거래일자" IS 'YYYYMMDD';
 
   COMMENT ON COLUMN "지급반려명세"."기관구분" IS '20:시비   30:구비';
 
   COMMENT ON COLUMN "지급반려명세"."지급명령번호" IS 'E-HOJO 지급명령번호';
 
   COMMENT ON COLUMN "지급반려명세"."요구부서코드" IS 'E-HOJO 품의요구부서코드(E-HOJO자체코드)';
 
   COMMENT ON COLUMN "지급반려명세"."정책사업코드" IS '정책사업코드';
 
   COMMENT ON COLUMN "지급반려명세"."단위사업코드" IS '단위사업코드';
 
   COMMENT ON COLUMN "지급반려명세"."세부사업코드" IS '세부사업코드';
 
   COMMENT ON COLUMN "지급반려명세"."분야코드" IS '분야코드';
 
   COMMENT ON COLUMN "지급반려명세"."부문코드" IS '부문코드';
 
   COMMENT ON COLUMN "지급반려명세"."예산과목코드" IS '일상경비일때 사용';
 
   COMMENT ON COLUMN "지급반려명세"."출금은행코드" IS 'default=031(대구은행)';
 
   COMMENT ON COLUMN "지급반려명세"."출금계좌번호" IS '숫자와 -만 허용';
 
   COMMENT ON COLUMN "지급반려명세"."출금금액" IS '입금명세 합계 금액과 일치';
 
   COMMENT ON COLUMN "지급반려명세"."출금명세" IS '출금통장적요(통장에 찍히는 내역)';
 
   COMMENT ON COLUMN "지급반려명세"."요청정보" IS '지출건명으로 사용';
 
   COMMENT ON COLUMN "지급반려명세"."입금총건수" IS '1 ~ 99999 N Type';
 
   COMMENT ON COLUMN "지급반려명세"."요청일시" IS 'YYYYMMDD hh:mm:ss';
 
   COMMENT ON COLUMN "지급반려명세"."자치단체명" IS '예)서울시청, 성동구청';
 
   COMMENT ON COLUMN "지급반려명세"."관서명" IS '예)남부도로관리사업소, 성동구보건소';
 
   COMMENT ON COLUMN "지급반려명세"."지급부서명" IS '예)도로보수과, 보건행정과';
 
   COMMENT ON COLUMN "지급반려명세"."회계명" IS '예)일반회계, 재정비촉진특별회계';
 
   COMMENT ON COLUMN "지급반려명세"."정책사업명" IS '정책사업명';
 
   COMMENT ON COLUMN "지급반려명세"."단위사업명" IS '단위사업명';
 
   COMMENT ON COLUMN "지급반려명세"."세부사업명" IS '세부사업명';
 
   COMMENT ON COLUMN "지급반려명세"."분야명" IS '예)일반공공행정, 공공질서 및 안전';
 
   COMMENT ON COLUMN "지급반려명세"."부문명" IS '예)입법 및 선거관기, 경찰, 보건의료';
 
   COMMENT ON COLUMN "지급반려명세"."예산과목명" IS '예)기본급, 직급보조비, 출연금, 국외자본이전';
 
   COMMENT ON COLUMN "지급반려명세"."요구부서명" IS '요구부서명칭';
 
   COMMENT ON COLUMN "지급반려명세"."지출원ID" IS '지출원ID';
 
   COMMENT ON COLUMN "지급반려명세"."지출원담당자명" IS '지출원담당자명';
 
   COMMENT ON COLUMN "지급반려명세"."지출원전화번호" IS '지출원전화번호';
 
   COMMENT ON COLUMN "지급반려명세"."자료수신여부" IS 'Y:수신 , N:미수신  (default=N), B:반려, D:삭제';
 
   COMMENT ON COLUMN "지급반려명세"."자료수신일시" IS '재무회계에서 수신 처리한 일시';
 
   COMMENT ON COLUMN "지급반려명세"."자료수신자명" IS '재무회계에서 수신 처리한 처리담당자 성명';
 
   COMMENT ON COLUMN "지급반려명세"."결과처리여부" IS 'Y:처리 , N:미처리  (default=N)';
 
   COMMENT ON COLUMN "지급반려명세"."결과코드" IS '0000:정상 , XXXX(임의의 숫자or문자):비정상';
 
   COMMENT ON COLUMN "지급반려명세"."결과설명" IS '오류시 ERROR 내용';
 
   COMMENT ON COLUMN "지급반려명세"."결과처리자명" IS '영업점 처리자';
 
   COMMENT ON COLUMN "지급반려명세"."결과처리일시" IS '영업점 처리일시 YYYYMMDD hh:mm:ss';
 
   COMMENT ON COLUMN "지급반려명세"."삭제일자" IS '삭제일자';
 
   COMMENT ON COLUMN "지급반려명세"."삭제자ID" IS '삭제자ID';
 
   COMMENT ON COLUMN "지급반려명세"."삭제사유" IS '삭제사유';
 
   COMMENT ON COLUMN "지급반려명세"."지급형태" IS '통상,집합,현금 구분';
 
   COMMENT ON COLUMN "지급반려명세"."현금유형코드" IS '세입세출외현금    현금유형코드 (대분류)';
 
   COMMENT ON COLUMN "지급반려명세"."현금종류코드" IS '세입세출외현금    현금종류코드 (중분류)';
 
   COMMENT ON COLUMN "지급반려명세"."현금종류명" IS '세입세출외현금    현금종류명';
 
   COMMENT ON COLUMN "지급반려명세"."복지급여여부" IS '지급처리건이 복지급여건이지의 여부';
 
   COMMENT ON COLUMN "지급반려명세"."거래번호" IS '거래번호';
 
   COMMENT ON COLUMN "지급반려명세"."작업시스템코드" IS '01: 뱅킹 02: 세입금 03: 계정계';
 
   COMMENT ON COLUMN "지급반려명세"."작업상태코드" IS '01:e-호조 자료수신,05:계좌검증요청,06:계좌검증완료,11:세입금 파일생성12:세입금 파일송신,21:계정계 파일송신,22:계정계 처리요청_전문,23:계정계 처리응답_전문,25:계정계 파일수신,26:b뱅킹 파일송신,31:b뱅킹 처리,32:e-호조 처리';
 
   COMMENT ON COLUMN "지급반려명세"."처리건수" IS '처리건수';
 
   COMMENT ON COLUMN "지급반려명세"."오류건수" IS '오류건수';
 
   COMMENT ON COLUMN "지급반려명세"."처리금액" IS '처리금액';
 
   COMMENT ON COLUMN "지급반려명세"."오류금액" IS '오류금액';
 
   COMMENT ON COLUMN "지급반려명세"."등록일시" IS '등록일시';
 
   COMMENT ON COLUMN "지급반려명세"."이체일자" IS '이체일자';
 
   COMMENT ON COLUMN "지급반려명세"."지급부서SMS발신번호" IS '지급부서SMS발신전화번호';
 
   COMMENT ON TABLE "지급반려명세"  IS '지급이체 거래에서 지급반려(일반지출, 자금배정, 일상경비, 세입세출외현금) 명세 저장';

--------------------------------------------------------
--  DDL for Table 지급원장
--------------------------------------------------------

  DROP TABLE "지급원장" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "지급원장" 
   (	"요청ID" VARCHAR2(2 BYTE), 
	"요청기관구분" VARCHAR2(2 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"지급부서코드" VARCHAR2(7 BYTE), 
	"회계연도" VARCHAR2(4 BYTE), 
	"회계코드" VARCHAR2(3 BYTE), 
	"자료구분" VARCHAR2(2 BYTE), 
	"지급명령등록번호" VARCHAR2(8 BYTE), 
	"거래일자" VARCHAR2(8 BYTE), 
	"기관구분" VARCHAR2(2 BYTE), 
	"지급명령번호" VARCHAR2(10 BYTE), 
	"요구부서코드" VARCHAR2(7 BYTE), 
	"정책사업코드" VARCHAR2(16 BYTE), 
	"단위사업코드" VARCHAR2(16 BYTE), 
	"세부사업코드" VARCHAR2(16 BYTE), 
	"분야코드" VARCHAR2(3 BYTE), 
	"부문코드" VARCHAR2(3 BYTE), 
	"예산과목코드" VARCHAR2(5 BYTE), 
	"출금은행코드" VARCHAR2(3 BYTE), 
	"출금계좌번호" VARCHAR2(16 BYTE), 
	"출금금액" NUMBER(15,0), 
	"출금명세" VARCHAR2(20 BYTE), 
	"요청정보" VARCHAR2(200 BYTE), 
	"입금총건수" NUMBER(5,0), 
	"요청일시" DATE, 
	"자치단체명" VARCHAR2(200 BYTE), 
	"관서명" VARCHAR2(200 BYTE), 
	"지급부서명" VARCHAR2(100 BYTE), 
	"회계명" VARCHAR2(200 BYTE), 
	"정책사업명" VARCHAR2(200 BYTE), 
	"단위사업명" VARCHAR2(200 BYTE), 
	"세부사업명" VARCHAR2(200 BYTE), 
	"분야명" VARCHAR2(50 BYTE), 
	"부문명" VARCHAR2(50 BYTE), 
	"예산과목명" VARCHAR2(50 BYTE), 
	"요구부서명" VARCHAR2(100 BYTE), 
	"지출원ID" VARCHAR2(13 BYTE), 
	"지출원담당자명" VARCHAR2(200 BYTE), 
	"지출원전화번호" VARCHAR2(30 BYTE), 
	"자료수신여부" VARCHAR2(1 BYTE), 
	"자료수신일시" DATE, 
	"자료수신자명" VARCHAR2(20 BYTE), 
	"결과처리여부" VARCHAR2(1 BYTE), 
	"결과코드" VARCHAR2(4 BYTE), 
	"결과설명" VARCHAR2(80 BYTE), 
	"결과처리자명" VARCHAR2(20 BYTE), 
	"결과처리일시" DATE, 
	"재배정여부" VARCHAR2(1 BYTE), 
	"지급형태" VARCHAR2(2 BYTE), 
	"현금유형코드" VARCHAR2(2 BYTE), 
	"현금종류코드" VARCHAR2(3 BYTE), 
	"현금종류명" VARCHAR2(100 BYTE), 
	"복지급여여부" VARCHAR2(1 BYTE), 
	"거래번호" VARCHAR2(14 BYTE), 
	"작업시스템코드" VARCHAR2(2 BYTE), 
	"작업상태코드" VARCHAR2(2 BYTE), 
	"처리건수" NUMBER(8,0) DEFAULT 0, 
	"오류건수" NUMBER(8,0) DEFAULT 0, 
	"처리금액" NUMBER(15,0) DEFAULT 0, 
	"오류금액" NUMBER(15,0) DEFAULT 0, 
	"등록일시" DATE DEFAULT SYSDATE, 
	"이체일자" VARCHAR2(8 BYTE), 
	"지급부서SMS발신번호" VARCHAR2(20 BYTE), 
	"별단계좌번호" VARCHAR2(20 BYTE), 
	"에러파일생성여부" VARCHAR2(1 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "지급원장"."요청ID" IS 'LF:지방재정관리시스템';
 
   COMMENT ON COLUMN "지급원장"."요청기관구분" IS '01:일반회계, 02:특별회계, 03:공기업특별회계, 04:기금';
 
   COMMENT ON COLUMN "지급원장"."자치단체코드" IS '예)대구광역시:6270000 ,성동구:3030000, 접속시스템코드';
 
   COMMENT ON COLUMN "지급원장"."관서코드" IS '사업소코드예)남부도로관리사업소:0260 , 성동구보건소:0020';
 
   COMMENT ON COLUMN "지급원장"."지급부서코드" IS '예)도로보수과:1234567 , 보건행정과 :1234567,품의부서';
 
   COMMENT ON COLUMN "지급원장"."회계연도" IS 'YYYY';
 
   COMMENT ON COLUMN "지급원장"."회계코드" IS '자치기관이 정의한 회계구분 코드(기관마다 다름)예) 100:일반회계, 201:주차장특별회계, 301:상수도사업특별회계';
 
   COMMENT ON COLUMN "지급원장"."자료구분" IS '10:일반지출(일반회계,특별회계),20:일상경비,30:임시일상경비,40:도급경비,45:자금배정,46:자금환수,60:세입세출외현금지출,80:구청보조금교부용지출,90:일상경비교부용지출';
 
   COMMENT ON COLUMN "지급원장"."지급명령등록번호" IS 'E-HOJO 지급명령일련번호';
 
   COMMENT ON COLUMN "지급원장"."거래일자" IS 'YYYYMMDD';
 
   COMMENT ON COLUMN "지급원장"."기관구분" IS '20:시비   30:구비';
 
   COMMENT ON COLUMN "지급원장"."지급명령번호" IS 'E-HOJO 지급명령번호';
 
   COMMENT ON COLUMN "지급원장"."요구부서코드" IS 'E-HOJO 품의요구부서코드(E-HOJO자체코드)';
 
   COMMENT ON COLUMN "지급원장"."정책사업코드" IS '정책사업코드';
 
   COMMENT ON COLUMN "지급원장"."단위사업코드" IS '단위사업코드';
 
   COMMENT ON COLUMN "지급원장"."세부사업코드" IS '세부사업코드';
 
   COMMENT ON COLUMN "지급원장"."분야코드" IS '분야코드';
 
   COMMENT ON COLUMN "지급원장"."부문코드" IS '부문코드';
 
   COMMENT ON COLUMN "지급원장"."예산과목코드" IS '일상경비일때 사용';
 
   COMMENT ON COLUMN "지급원장"."출금은행코드" IS 'default =031(대구은행) ';
 
   COMMENT ON COLUMN "지급원장"."출금계좌번호" IS '숫자와 -만 허용';
 
   COMMENT ON COLUMN "지급원장"."출금금액" IS '입금명세 합계 금액과 일치';
 
   COMMENT ON COLUMN "지급원장"."출금명세" IS '출금통장적요(통장에 찍히는 내역)';
 
   COMMENT ON COLUMN "지급원장"."요청정보" IS '지출건명으로 사용';
 
   COMMENT ON COLUMN "지급원장"."입금총건수" IS '1 ~ 99999 N Type';
 
   COMMENT ON COLUMN "지급원장"."요청일시" IS 'YYYYMM DD:hh:mm:ss';
 
   COMMENT ON COLUMN "지급원장"."자치단체명" IS '예)서울시청, 성동구청';
 
   COMMENT ON COLUMN "지급원장"."관서명" IS '예)남부도로관리사업소, 성동구보건소';
 
   COMMENT ON COLUMN "지급원장"."지급부서명" IS '예)도로보수과, 보건행정과';
 
   COMMENT ON COLUMN "지급원장"."회계명" IS '예)일반회계, 재정비촉진특별회계';
 
   COMMENT ON COLUMN "지급원장"."정책사업명" IS '정책사업명';
 
   COMMENT ON COLUMN "지급원장"."단위사업명" IS '단위사업명';
 
   COMMENT ON COLUMN "지급원장"."세부사업명" IS '세부사업명';
 
   COMMENT ON COLUMN "지급원장"."분야명" IS '예)일반공공행정, 공공질서 및 안전';
 
   COMMENT ON COLUMN "지급원장"."부문명" IS '예)입법 및 선거관기, 경찰, 보건의료';
 
   COMMENT ON COLUMN "지급원장"."예산과목명" IS '예)기본급, 직급보조비, 출연금, 국외자본이전';
 
   COMMENT ON COLUMN "지급원장"."요구부서명" IS '요구부서명칭';
 
   COMMENT ON COLUMN "지급원장"."지출원ID" IS '지출원ID';
 
   COMMENT ON COLUMN "지급원장"."지출원담당자명" IS '지출원담당자명';
 
   COMMENT ON COLUMN "지급원장"."지출원전화번호" IS '지출원전화번호';
 
   COMMENT ON COLUMN "지급원장"."자료수신여부" IS 'Y:수신, N:미수신(default=N), B:반려, D:삭제';
 
   COMMENT ON COLUMN "지급원장"."자료수신일시" IS '재무회계에서 수신처리한 일시';
 
   COMMENT ON COLUMN "지급원장"."자료수신자명" IS '재무획계에서 수신처리한 처리담당자 성명';
 
   COMMENT ON COLUMN "지급원장"."결과처리여부" IS 'Y:처리 , N:미처리  (default=N)';
 
   COMMENT ON COLUMN "지급원장"."결과코드" IS '0000:정상 , XXXX(임의의 숫자or문자):비정상';
 
   COMMENT ON COLUMN "지급원장"."결과설명" IS '오류시 ERROR 내용';
 
   COMMENT ON COLUMN "지급원장"."결과처리자명" IS '영업점 처리자';
 
   COMMENT ON COLUMN "지급원장"."결과처리일시" IS '영업점 처리일시 YYYYMMDD hh24:mm:ss';
 
   COMMENT ON COLUMN "지급원장"."재배정여부" IS 'Y:재배정자료, N : 일반배정자료';
 
   COMMENT ON COLUMN "지급원장"."지급형태" IS '통상,집합,현금 구분';
 
   COMMENT ON COLUMN "지급원장"."현금유형코드" IS '세입세출외현금 현금유형코드 (대분류)';
 
   COMMENT ON COLUMN "지급원장"."현금종류코드" IS '세입세출외현금 현금종류코드 (중분류)';
 
   COMMENT ON COLUMN "지급원장"."현금종류명" IS '세입세출외현금 현금종류명';
 
   COMMENT ON COLUMN "지급원장"."복지급여여부" IS '지급처리건이 복지급여건인지의 여부';
 
   COMMENT ON COLUMN "지급원장"."거래번호" IS '거래번호';
 
   COMMENT ON COLUMN "지급원장"."작업시스템코드" IS '01: b뱅킹 02: 세입금 03: 계정계';
 
   COMMENT ON COLUMN "지급원장"."작업상태코드" IS '01:e-호조 자료수신,05:계좌검증요청,06:계좌검증완료,11:세입금 파일생성12:세입금 파일송신,21:계정계 파일송신,22:계정계 처리요청_전문,23:계정계 처리응답_전문,25:계정계 파일수신,26:b뱅킹 파일송신,31:b뱅킹 처리,32:e-호조 처리';
 
   COMMENT ON COLUMN "지급원장"."처리건수" IS '처리건수';
 
   COMMENT ON COLUMN "지급원장"."오류건수" IS '오류건수';
 
   COMMENT ON COLUMN "지급원장"."처리금액" IS '처리금액';
 
   COMMENT ON COLUMN "지급원장"."오류금액" IS '오류금액';
 
   COMMENT ON COLUMN "지급원장"."등록일시" IS '등록일시';
 
   COMMENT ON COLUMN "지급원장"."이체일자" IS '이체일자';
 
   COMMENT ON COLUMN "지급원장"."지급부서SMS발신번호" IS '';
 
   COMMENT ON COLUMN "지급원장"."별단계좌번호" IS '지급오류건의 임시 입금계좌번호';
 
   COMMENT ON COLUMN "지급원장"."에러파일생성여부" IS '';
 
   COMMENT ON TABLE "지급원장"  IS '지급이체 거래에서 지급관련(일반지출, 자금배정, 일상경비, 세입세출외현금) 명세 저장';

--------------------------------------------------------
--  DDL for Table 지급원장_반려
--------------------------------------------------------

  DROP TABLE "지급원장_반려" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "지급원장_반려" 
   (	"요청ID" VARCHAR2(2 BYTE), 
	"요청기관구분" VARCHAR2(2 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"지급부서코드" VARCHAR2(7 BYTE), 
	"회계연도" VARCHAR2(4 BYTE), 
	"회계코드" VARCHAR2(3 BYTE), 
	"자료구분" VARCHAR2(2 BYTE), 
	"지급명령등록번호" VARCHAR2(8 BYTE), 
	"지급반려일련번호" NUMBER(3,0), 
	"거래일자" VARCHAR2(8 BYTE), 
	"기관구분" VARCHAR2(2 BYTE), 
	"지급명령번호" VARCHAR2(10 BYTE), 
	"요구부서코드" VARCHAR2(7 BYTE), 
	"정책사업코드" VARCHAR2(16 BYTE), 
	"단위사업코드" VARCHAR2(16 BYTE), 
	"세부사업코드" VARCHAR2(16 BYTE), 
	"분야코드" VARCHAR2(3 BYTE), 
	"부문코드" VARCHAR2(3 BYTE), 
	"예산과목코드" VARCHAR2(5 BYTE), 
	"출금은행코드" VARCHAR2(3 BYTE), 
	"출금계좌번호" VARCHAR2(16 BYTE), 
	"출금금액" NUMBER(15,0), 
	"출금명세" VARCHAR2(20 BYTE), 
	"요청정보" VARCHAR2(200 BYTE), 
	"입금총건수" NUMBER(5,0), 
	"요청일시" DATE, 
	"자치단체명" VARCHAR2(200 BYTE), 
	"관서명" VARCHAR2(200 BYTE), 
	"지급부서명" VARCHAR2(100 BYTE), 
	"회계명" VARCHAR2(200 BYTE), 
	"정책사업명" VARCHAR2(200 BYTE), 
	"단위사업명" VARCHAR2(200 BYTE), 
	"세부사업명" VARCHAR2(200 BYTE), 
	"분야명" VARCHAR2(50 BYTE), 
	"부문명" VARCHAR2(50 BYTE), 
	"예산과목명" VARCHAR2(50 BYTE), 
	"요구부서명" VARCHAR2(100 BYTE), 
	"지출원ID" VARCHAR2(13 BYTE), 
	"지출원담당자명" VARCHAR2(200 BYTE), 
	"지출원전화번호" VARCHAR2(30 BYTE), 
	"자료수신여부" VARCHAR2(1 BYTE), 
	"자료수신일시" DATE, 
	"자료수신자명" VARCHAR2(20 BYTE), 
	"결과처리여부" VARCHAR2(1 BYTE), 
	"결과코드" VARCHAR2(4 BYTE), 
	"결과설명" VARCHAR2(80 BYTE), 
	"결과처리자명" VARCHAR2(20 BYTE), 
	"결과처리일시" DATE, 
	"재배정여부" VARCHAR2(1 BYTE), 
	"지급형태" VARCHAR2(2 BYTE), 
	"현금유형코드" VARCHAR2(2 BYTE), 
	"현금종류코드" VARCHAR2(3 BYTE), 
	"현금종류명" VARCHAR2(100 BYTE), 
	"복지급여여부" VARCHAR2(1 BYTE), 
	"거래번호" VARCHAR2(14 BYTE), 
	"작업시스템코드" VARCHAR2(2 BYTE), 
	"작업상태코드" VARCHAR2(2 BYTE), 
	"처리건수" NUMBER(8,0) DEFAULT 0, 
	"오류건수" NUMBER(8,0) DEFAULT 0, 
	"처리금액" NUMBER(15,0) DEFAULT 0, 
	"오류금액" NUMBER(15,0) DEFAULT 0, 
	"등록일시" DATE DEFAULT SYSDATE, 
	"이체일자" VARCHAR2(8 BYTE), 
	"에러파일생성여부" VARCHAR2(1 BYTE) DEFAULT 'N', 
	"별단계좌번호" VARCHAR2(19 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

   COMMENT ON COLUMN "지급원장_반려"."요청ID" IS 'LF:지방재정관리시스템';
 
   COMMENT ON COLUMN "지급원장_반려"."요청기관구분" IS '01:일반회계, 02:특별회계, 03:공기업특별회계, 04:기금';
 
   COMMENT ON COLUMN "지급원장_반려"."자치단체코드" IS '예)서울특별시:6110000 ,성동구:3030000, 접속시스템코드';
 
   COMMENT ON COLUMN "지급원장_반려"."관서코드" IS '사업소코드 
예)남부도로관리사업소:0260 , 성동구보건소:0020
tbb_ibuseo';
 
   COMMENT ON COLUMN "지급원장_반려"."지급부서코드" IS '예)도로보수과:1234567 , 보건행정과 :1234567,품의부서
su_ibuseo(일상경비), ibuseo(지출)';
 
   COMMENT ON COLUMN "지급원장_반려"."회계연도" IS 'YYYY';
 
   COMMENT ON COLUMN "지급원장_반려"."회계코드" IS '자치기관이 정의한 회계구분 코드(기관마다 다름)
예) 100:일반회계, 201:주차장특별회계, 301:상수도사업특별회계';
 
   COMMENT ON COLUMN "지급원장_반려"."자료구분" IS '10:일반지출(일반회계,특별회계)  
20:일상경비 
30:임시일상경비  
40:도급경비
45:자금배정   
46:자금환수   
60:세입세출외현금지출  
80:구청보조금교부용지출
90:일상경비교부용지출';
 
   COMMENT ON COLUMN "지급원장_반려"."지급명령등록번호" IS 'E-HOJO 지급명령일련번호';
 
   COMMENT ON COLUMN "지급원장_반려"."지급반려일련번호" IS '1 ~ 99999 NUMBER Type';
 
   COMMENT ON COLUMN "지급원장_반려"."거래일자" IS 'YYYYMMDD';
 
   COMMENT ON COLUMN "지급원장_반려"."기관구분" IS '20:시비   30:구비';
 
   COMMENT ON COLUMN "지급원장_반려"."지급명령번호" IS 'E-HOJO 지급명령일련번호';
 
   COMMENT ON COLUMN "지급원장_반려"."요구부서코드" IS 'E-HOJO 품의요구부서코드(E-HOJO자체코드)';
 
   COMMENT ON COLUMN "지급원장_반려"."예산과목코드" IS '일상경비일때 사용';
 
   COMMENT ON COLUMN "지급원장_반려"."출금은행코드" IS 'default =20(우리은행) 일단두자리 주택사업특별회계=국민은행';
 
   COMMENT ON COLUMN "지급원장_반려"."출금계좌번호" IS '숫자와 -만 허용';
 
   COMMENT ON COLUMN "지급원장_반려"."출금금액" IS '입금명세 합계 금액과 일치';
 
   COMMENT ON COLUMN "지급원장_반려"."출금명세" IS '출금통장적요(통장에 찍히는 내역)';
 
   COMMENT ON COLUMN "지급원장_반려"."요청정보" IS '지출건명으로 사용';
 
   COMMENT ON COLUMN "지급원장_반려"."입금총건수" IS '1 ~ 99999 NUMBER Type';
 
   COMMENT ON COLUMN "지급원장_반려"."요청일시" IS 'YYYYMM DD:hh:mm:ss';
 
   COMMENT ON COLUMN "지급원장_반려"."자치단체명" IS '예)서울시청, 성동구청';
 
   COMMENT ON COLUMN "지급원장_반려"."관서명" IS '예)남부도로관리사업소, 성동구보건소';
 
   COMMENT ON COLUMN "지급원장_반려"."지급부서명" IS '예)도로보수과, 보건행정과';
 
   COMMENT ON COLUMN "지급원장_반려"."회계명" IS '예)일반회계, 재정비촉진특별회계';
 
   COMMENT ON COLUMN "지급원장_반려"."분야명" IS '예)일반공공행정, 공공질서 및 안전';
 
   COMMENT ON COLUMN "지급원장_반려"."부문명" IS '예)입법 및 선거관기, 경찰, 보건의료';
 
   COMMENT ON COLUMN "지급원장_반려"."예산과목명" IS '예)기본급, 직급보조비, 출연금, 국외자본이전';
 
   COMMENT ON COLUMN "지급원장_반려"."요구부서명" IS '요구부서명칭';
 
   COMMENT ON COLUMN "지급원장_반려"."자료수신여부" IS 'Y:수신 , N:미수신  (default=N), B:반려, D:삭제';
 
   COMMENT ON COLUMN "지급원장_반려"."자료수신일시" IS '재무회계에서 수신처리한 일시';
 
   COMMENT ON COLUMN "지급원장_반려"."자료수신자명" IS '재무획계에서 수신처리한 처리담당자 성명';
 
   COMMENT ON COLUMN "지급원장_반려"."결과처리여부" IS 'Y:처리 , N:미처리  (default=N)';
 
   COMMENT ON COLUMN "지급원장_반려"."결과코드" IS '0000:정상 , XXXX(임의의 숫자or문자):비정상';
 
   COMMENT ON COLUMN "지급원장_반려"."결과설명" IS '오류시 ERROR 내용';
 
   COMMENT ON COLUMN "지급원장_반려"."결과처리자명" IS '영업점 처리자';
 
   COMMENT ON COLUMN "지급원장_반려"."결과처리일시" IS '영업점 처리일시 YYYYMM DD:hh:mm:ss';
 
   COMMENT ON COLUMN "지급원장_반려"."재배정여부" IS 'Y:재배정자료, N : 일반배정자료';
 
   COMMENT ON COLUMN "지급원장_반려"."지급형태" IS '통상,집합,현금 구분';
 
   COMMENT ON COLUMN "지급원장_반려"."현금유형코드" IS '세입세출외현금    현금유형코드 (대분류)';
 
   COMMENT ON COLUMN "지급원장_반려"."현금종류코드" IS '세입세출외현금    현금종류코드 (중분류)';
 
   COMMENT ON COLUMN "지급원장_반려"."현금종류명" IS '세입세출외현금    현금종류명   ';
 
   COMMENT ON COLUMN "지급원장_반려"."복지급여여부" IS '지급처리건이 복지급여건이지의 여부';
 
   COMMENT ON COLUMN "지급원장_반려"."작업시스템코드" IS '01: b뱅킹 02: 세입금 03: 계정계';
 
   COMMENT ON COLUMN "지급원장_반려"."작업상태코드" IS '01:e-호조 자료수신
05:계좌검증요청
06:계좌검증완료
11:세입금 파일생성12:세입금 파일송신
21:계정계 파일송신
22:계정계 처리요청_전문
23:계정계 처리응답_전문
25:계정계 파일수신
26:b뱅킹 파일송신
31:b뱅킹 처리
32:e-호조 처리';

--------------------------------------------------------
--  DDL for Table 직인정보관리
--------------------------------------------------------

  DROP TABLE "직인정보관리" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "직인정보관리" 
   (	"회계연도" VARCHAR2(4 BYTE), 
	"회계구분코드" VARCHAR2(3 BYTE), 
	"자치단체코드" VARCHAR2(7 BYTE), 
	"관서코드" VARCHAR2(4 BYTE), 
	"부서코드" VARCHAR2(7 BYTE), 
	"GCC부서코드" VARCHAR2(7 BYTE), 
	"직인이미지" BLOB, 
	"등록일시" DATE, 
	"등록자ID" VARCHAR2(13 BYTE)
   )
  TABLESPACE "ICMSDATA" 
 LOB ("직인이미지") STORE AS BASICFILE (
  TABLESPACE "ICMSDATA" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION 
  NOCACHE LOGGING ) ;

--------------------------------------------------------
--  DDL for Table 질의응답
--------------------------------------------------------

  DROP TABLE "질의응답" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "질의응답" 
   (	"자치단체코드" VARCHAR2(7 BYTE), 
	"등록순번" NUMBER(8,0), 
	"상위등록번호" NUMBER(8,0), 
	"등록번호" NUMBER(8,0), 
	"등록레벨" NUMBER(4,0), 
	"제목" VARCHAR2(200 BYTE), 
	"내용" VARCHAR2(4000 BYTE), 
	"공지구분코드" VARCHAR2(1 BYTE), 
	"첨부파일" VARCHAR2(200 BYTE), 
	"조회수" NUMBER(10,0), 
	"등록자ID" VARCHAR2(13 BYTE), 
	"등록일시" DATE, 
	"FAQ여부" VARCHAR2(1 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 채주계좌정보
--------------------------------------------------------

  DROP TABLE "채주계좌정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "채주계좌정보" 
   (	"순번" NUMBER, 
	"은행코드" VARCHAR2(3 BYTE), 
	"계좌번호" VARCHAR2(30 BYTE), 
	"예금주" VARCHAR2(200 BYTE), 
	"조회후예금주" VARCHAR2(200 BYTE), 
	"자료등록일시" DATE, 
	"자료변경일시" DATE, 
	"사용유무" VARCHAR2(1 BYTE), 
	"자료수신일시" DATE, 
	"결과처리여부" VARCHAR2(1 BYTE), 
	"결과처리일시" DATE, 
	"결과코드" VARCHAR2(4 BYTE), 
	"결과설명" VARCHAR2(80 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 파일순번
--------------------------------------------------------

  DROP TABLE "파일순번" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "파일순번" 
   (	"거래구분" VARCHAR2(2 BYTE), 
	"거래일자" VARCHAR2(8 BYTE), 
	"점번" VARCHAR2(3 BYTE), 
	"순번" NUMBER(8,0) DEFAULT 0, 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 파일처리로그
--------------------------------------------------------

  DROP TABLE "파일처리로그" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "파일처리로그" 
   (	"거래일자" VARCHAR2(8 BYTE), 
	"거래번호" VARCHAR2(14 BYTE), 
	"자료수신여부" VARCHAR2(1 BYTE), 
	"자료수신일시" DATE, 
	"결과처리여부" VARCHAR2(1 BYTE), 
	"결과코드" VARCHAR2(4 BYTE), 
	"결과설명" VARCHAR2(200 BYTE), 
	"결과처리일시" DATE, 
	"작업시스템코드" VARCHAR2(2 BYTE), 
	"작업상태코드" VARCHAR2(2 BYTE), 
	"등록일시" DATE DEFAULT SYSDATE
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 한도관리
--------------------------------------------------------

  DROP TABLE "한도관리" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "한도관리" 
   (	"회계연도" VARCHAR2(4 BYTE), 
	"관서코드" VARCHAR2(50 BYTE), 
	"관서명" VARCHAR2(50 BYTE), 
	"한도금액" NUMBER(15,0), 
	"예비컬럼1" VARCHAR2(100 BYTE), 
	"예비컬럼2" VARCHAR2(100 BYTE), 
	"예비컬럼3" VARCHAR2(100 BYTE), 
	"예비컬럼4" VARCHAR2(100 BYTE), 
	"예비컬럼5" VARCHAR2(100 BYTE), 
	"등록일시" DATE, 
	"수정일시" DATE
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 회계정보
--------------------------------------------------------

  DROP TABLE "회계정보" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "회계정보" 
   (	"자치단체코드" VARCHAR2(7 BYTE), 
	"회계연도" VARCHAR2(4 BYTE), 
	"회계구분마스터코드" VARCHAR2(3 BYTE), 
	"회계코드" VARCHAR2(3 BYTE), 
	"자치단체명" VARCHAR2(200 BYTE), 
	"회계명" VARCHAR2(200 BYTE), 
	"회계구분마스터명" VARCHAR2(200 BYTE), 
	"사용유무" VARCHAR2(1 BYTE), 
	"금고구분명" VARCHAR2(30 BYTE), 
	"금고유무" VARCHAR2(1 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Table 회계코드맵핑
--------------------------------------------------------

  DROP TABLE "회계코드맵핑" CASCADE CONSTRAINTS PURGE;
  CREATE TABLE "회계코드맵핑" 
   (	"자치단체코드" VARCHAR2(7 BYTE), 
	"회계연도" VARCHAR2(4 BYTE), 
	"회계구분마스트코드" VARCHAR2(3 BYTE), 
	"회계구분코드" VARCHAR2(3 BYTE), 
	"요청기관구분" VARCHAR2(2 BYTE), 
	"회계구분" VARCHAR2(2 BYTE), 
	"관리용회계코드" VARCHAR2(4 BYTE), 
	"자치단체명" VARCHAR2(200 BYTE), 
	"회계명" VARCHAR2(200 BYTE), 
	"회계구분마스트명" VARCHAR2(200 BYTE), 
	"사용유무" VARCHAR2(1 BYTE), 
	"관리용회계코드명" VARCHAR2(200 BYTE)
   )
  TABLESPACE "ICMSDATA" ;

--------------------------------------------------------
--  DDL for Index 계좌소관정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "계좌소관정보_PK" ON "계좌소관정보" ("회계연도", "회계코드", "계좌소관", "계좌번호") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 공지사항_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "공지사항_PK" ON "공지사항" ("자치단체코드", "등록순번") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 공통코드_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "공통코드_PK" ON "공통코드" ("분류코드", "코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 급여관리_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "급여관리_PK" ON "급여관리" ("회계연도", "예산과목코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 부서정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "부서정보_PK" ON "부서정보" ("자치단체코드", "관서코드", "GCC부서코드", "부서코드", "회계코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 사업정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "사업정보_PK" ON "사업정보" ("회계연도", "자치단체코드", "관서코드", "GCC부서코드", "부서코드", "회계코드", "정책사업코드", "단위사업코드", "세부사업코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 사용자별_회계관직_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "사용자별_회계관직_PK" ON "사용자별_회계관직" ("회계연도", "회계구분코드", "회계관직코드", "자치단체코드", "관서코드", "부서코드", "GCC부서코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 사용자정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "사용자정보_PK" ON "사용자정보" ("회계연도", "회계구분", "자치단체코드", "관서코드", "부서코드", "GCC부서코드", "사용자ID") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 세입세출외현금_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "세입세출외현금_PK" ON "세입세출외현금" ("자치단체코드", "현금대분류코드", "현금중분류코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 엑셀계좌검증_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "엑셀계좌검증_PK" ON "엑셀계좌검증" ("요청일자", "요청일련번호") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 휴일정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "휴일정보_PK" ON "영업일" ("기준일자") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 예외계좌정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "예외계좌정보_PK" ON "예외계좌정보" ("지급명령일자", "은행코드", "계좌번호") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 이뱅킹계좌검증_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "이뱅킹계좌검증_PK" ON "이뱅킹계좌검증" ("자치단체코드", "지출단계", "지출번호구분", "지출순번", "검증순번") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index SYS_C0011151_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYS_C0011151" ON "입금명세" ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "입금일련번호", "재배정여부") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 입금명세_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "입금명세_PK" ON "입금명세" ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "입금일련번호", "재배정여부", "지급부서코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index SYS_C0011201_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "SYS_C0011201" ON "입금반려명세" ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "입금일련번호", "재배정여부", "지급부서코드", "삭제이력순번", "거래일자", "기관구분", "지급명령번호", "요구부서코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 자금관리_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "자금관리_PK" ON "자금관리" ("회계연도", "등록순번", "회계종류") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 전문순번_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "전문순번_PK" ON "전문순번" ("거래구분", "거래일자", "점번", "순번") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 점번정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "점번정보_PK" ON "점번정보" ("점번") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index IDX_점번정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "IDX_점번정보_PK" ON "점번정보" ("자치단체코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 지급계좌정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "지급계좌정보_PK" ON "지급계좌정보" ("순번", "지출자금구분") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 지급반려명세_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "지급반려명세_PK" ON "지급반려명세" ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "재배정여부", "삭제이력순번", "거래일자", "기관구분", "지급명령번호", "요구부서코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 지급원장_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "지급원장_PK" ON "지급원장" ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "재배정여부") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 지급원장_반려_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "지급원장_반려_PK" ON "지급원장_반려" ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "지급반려일련번호", "거래일자", "재배정여부") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 직인정보관리_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "직인정보관리_PK" ON "직인정보관리" ("회계연도", "자치단체코드", "관서코드", "부서코드", "회계구분코드", "GCC부서코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 질의응답_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "질의응답_PK" ON "질의응답" ("자치단체코드", "등록순번", "상위등록번호", "등록번호") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 채주계좌정보_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "채주계좌정보_PK" ON "채주계좌정보" ("순번", "은행코드", "계좌번호") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 파일순번_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "파일순번_PK" ON "파일순번" ("거래구분", "거래일자", "점번", "순번") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 파일처리로그_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "파일처리로그_PK" ON "파일처리로그" ("거래일자", "거래번호") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 한도관리_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "한도관리_PK" ON "한도관리" ("회계연도", "관서코드", "관서명") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 회계코드_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "회계코드_PK" ON "회계정보" ("자치단체코드", "회계연도", "회계구분마스터코드", "회계코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  DDL for Index 회계코드맵핑_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "회계코드맵핑_PK" ON "회계코드맵핑" ("자치단체코드", "회계연도", "회계구분", "관리용회계코드") 
  TABLESPACE "ICMSIDX" ;

--------------------------------------------------------
--  Constraints for Table 계좌소관정보
--------------------------------------------------------

  ALTER TABLE "계좌소관정보" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "계좌소관정보" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "계좌소관정보" MODIFY ("계좌소관" NOT NULL ENABLE);
 
  ALTER TABLE "계좌소관정보" MODIFY ("계좌성격" NOT NULL ENABLE);
 
  ALTER TABLE "계좌소관정보" MODIFY ("계좌번호" NOT NULL ENABLE);
 
  ALTER TABLE "계좌소관정보" MODIFY ("등록일시" NOT NULL ENABLE);
 
  ALTER TABLE "계좌소관정보" ADD PRIMARY KEY ("회계연도", "회계코드", "계좌소관", "계좌번호")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 공지사항
--------------------------------------------------------

  ALTER TABLE "공지사항" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "공지사항" MODIFY ("등록순번" NOT NULL ENABLE);
 
  ALTER TABLE "공지사항" ADD PRIMARY KEY ("자치단체코드", "등록순번")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 공통코드
--------------------------------------------------------

  ALTER TABLE "공통코드" MODIFY ("분류코드" NOT NULL ENABLE);
 
  ALTER TABLE "공통코드" MODIFY ("코드" NOT NULL ENABLE);
 
  ALTER TABLE "공통코드" ADD PRIMARY KEY ("분류코드", "코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 급여관리
--------------------------------------------------------

  ALTER TABLE "급여관리" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "급여관리" MODIFY ("예산과목코드" NOT NULL ENABLE);
 
  ALTER TABLE "급여관리" ADD PRIMARY KEY ("회계연도", "예산과목코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 부서정보
--------------------------------------------------------

  ALTER TABLE "부서정보" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "부서정보" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "부서정보" MODIFY ("GCC부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "부서정보" MODIFY ("부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "부서정보" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "부서정보" ADD PRIMARY KEY ("자치단체코드", "관서코드", "GCC부서코드", "부서코드", "회계코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 사업정보
--------------------------------------------------------

  ALTER TABLE "사업정보" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" MODIFY ("GCC부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" MODIFY ("부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" MODIFY ("정책사업코드" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" MODIFY ("단위사업코드" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" MODIFY ("세부사업코드" NOT NULL ENABLE);
 
  ALTER TABLE "사업정보" ADD PRIMARY KEY ("회계연도", "자치단체코드", "관서코드", "GCC부서코드", "부서코드", "회계코드", "정책사업코드", "단위사업코드", "세부사업코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 사용자별_회계관직
--------------------------------------------------------

  ALTER TABLE "사용자별_회계관직" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "사용자별_회계관직" MODIFY ("회계구분코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자별_회계관직" MODIFY ("회계관직코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자별_회계관직" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자별_회계관직" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자별_회계관직" MODIFY ("부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자별_회계관직" MODIFY ("GCC부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자별_회계관직" MODIFY ("자료구분" NOT NULL ENABLE);
 
  ALTER TABLE "사용자별_회계관직" ADD PRIMARY KEY ("회계연도", "회계구분코드", "회계관직코드", "자치단체코드", "관서코드", "부서코드", "GCC부서코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 사용자정보
--------------------------------------------------------

  ALTER TABLE "사용자정보" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "사용자정보" MODIFY ("회계구분" NOT NULL ENABLE);
 
  ALTER TABLE "사용자정보" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자정보" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자정보" MODIFY ("부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자정보" MODIFY ("GCC부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "사용자정보" MODIFY ("사용자ID" NOT NULL ENABLE);
 
  ALTER TABLE "사용자정보" MODIFY ("시스템권한" NOT NULL ENABLE);
 
  ALTER TABLE "사용자정보" ADD PRIMARY KEY ("회계연도", "회계구분", "자치단체코드", "관서코드", "부서코드", "GCC부서코드", "사용자ID")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 세입세출외현금
--------------------------------------------------------

  ALTER TABLE "세입세출외현금" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "세입세출외현금" MODIFY ("현금대분류코드" NOT NULL ENABLE);
 
  ALTER TABLE "세입세출외현금" MODIFY ("현금중분류코드" NOT NULL ENABLE);
 
  ALTER TABLE "세입세출외현금" ADD PRIMARY KEY ("자치단체코드", "현금대분류코드", "현금중분류코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 엑셀계좌검증
--------------------------------------------------------

  ALTER TABLE "엑셀계좌검증" MODIFY ("요청일자" NOT NULL ENABLE);
 
  ALTER TABLE "엑셀계좌검증" MODIFY ("요청일련번호" NOT NULL ENABLE);
 
  ALTER TABLE "엑셀계좌검증" MODIFY ("요청자ID" NOT NULL ENABLE);
 
  ALTER TABLE "엑셀계좌검증" ADD PRIMARY KEY ("요청일자", "요청일련번호")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 영업일
--------------------------------------------------------

  ALTER TABLE "영업일" ADD PRIMARY KEY ("기준일자")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;
 
  ALTER TABLE "영업일" MODIFY ("기준일자" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table 예외계좌정보
--------------------------------------------------------

  ALTER TABLE "예외계좌정보" MODIFY ("지급명령일자" NOT NULL ENABLE);
 
  ALTER TABLE "예외계좌정보" MODIFY ("은행코드" NOT NULL ENABLE);
 
  ALTER TABLE "예외계좌정보" MODIFY ("계좌번호" NOT NULL ENABLE);
 
  ALTER TABLE "예외계좌정보" ADD PRIMARY KEY ("지급명령일자", "은행코드", "계좌번호")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 이뱅킹계좌검증
--------------------------------------------------------

  ALTER TABLE "이뱅킹계좌검증" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("지출단계" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("지출번호구분" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("지출순번" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("검증순번" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("요청상태" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("요청일시" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("요청자ID" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("작업시스템코드" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("작업상태코드" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" MODIFY ("거래번호" NOT NULL ENABLE);
 
  ALTER TABLE "이뱅킹계좌검증" ADD PRIMARY KEY ("자치단체코드", "지출단계", "지출번호구분", "지출순번", "검증순번")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 입금명세
--------------------------------------------------------

  ALTER TABLE "입금명세" MODIFY ("요청ID" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("요청기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("자료구분" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("지급명령등록번호" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("입금일련번호" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("거래일자" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("입금유형" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("입금금액" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("자료수신여부" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("결과처리여부" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("재배정여부" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("지급형태" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("현금유형코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" MODIFY ("현금종류코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금명세" ADD PRIMARY KEY ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "입금일련번호", "재배정여부")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 입금반려명세
--------------------------------------------------------

  ALTER TABLE "입금반려명세" MODIFY ("요청ID" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("요청기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("자료구분" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("지급명령등록번호" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("입금일련번호" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("거래일자" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("지급명령번호" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("입금유형" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("입금금액" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("자료수신여부" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("결과처리여부" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("재배정여부" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("삭제일자" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("삭제자ID" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("삭제사유" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("지급부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("삭제이력순번" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" MODIFY ("요구부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "입금반려명세" ADD PRIMARY KEY ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "입금일련번호", "재배정여부", "지급부서코드", "삭제이력순번", "거래일자", "기관구분", "지급명령번호", "요구부서코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 자금관리
--------------------------------------------------------

  ALTER TABLE "자금관리" ADD PRIMARY KEY ("회계연도", "등록순번", "회계종류")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;
 
  ALTER TABLE "자금관리" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "자금관리" MODIFY ("등록순번" NOT NULL ENABLE);
 
  ALTER TABLE "자금관리" MODIFY ("회계종류" NOT NULL ENABLE);
 
  ALTER TABLE "자금관리" MODIFY ("예치종류" NOT NULL ENABLE);
 
  ALTER TABLE "자금관리" MODIFY ("예치이율" NOT NULL ENABLE);
 
  ALTER TABLE "자금관리" MODIFY ("예치금액" NOT NULL ENABLE);
 
  ALTER TABLE "자금관리" MODIFY ("신규일자" NOT NULL ENABLE);
 
  ALTER TABLE "자금관리" MODIFY ("만기일자" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table 전문순번
--------------------------------------------------------

  ALTER TABLE "전문순번" MODIFY ("거래구분" NOT NULL ENABLE);
 
  ALTER TABLE "전문순번" MODIFY ("거래일자" NOT NULL ENABLE);
 
  ALTER TABLE "전문순번" MODIFY ("점번" NOT NULL ENABLE);
 
  ALTER TABLE "전문순번" MODIFY ("순번" NOT NULL ENABLE);
 
  ALTER TABLE "전문순번" ADD PRIMARY KEY ("거래구분", "거래일자", "점번", "순번")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 점번정보
--------------------------------------------------------

  ALTER TABLE "점번정보" MODIFY ("점번" NOT NULL ENABLE);
 
  ALTER TABLE "점번정보" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "점번정보" ADD PRIMARY KEY ("점번")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 지급계좌정보
--------------------------------------------------------

  ALTER TABLE "지급계좌정보" MODIFY ("순번" NOT NULL ENABLE);
 
  ALTER TABLE "지급계좌정보" MODIFY ("지출자금구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급계좌정보" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "지급계좌정보" ADD PRIMARY KEY ("순번", "지출자금구분")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 지급반려명세
--------------------------------------------------------

  ALTER TABLE "지급반려명세" MODIFY ("요청ID" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("요청기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("지급부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("자료구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("지급명령등록번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("재배정여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("삭제이력순번" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("거래일자" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("지급명령번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("요구부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("출금은행코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("출금계좌번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("출금금액" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("입금총건수" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("자료수신여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("결과처리여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("삭제일자" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("삭제자ID" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" MODIFY ("삭제사유" NOT NULL ENABLE);
 
  ALTER TABLE "지급반려명세" ADD PRIMARY KEY ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "재배정여부", "삭제이력순번", "거래일자", "기관구분", "지급명령번호", "요구부서코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 지급원장
--------------------------------------------------------

  ALTER TABLE "지급원장" MODIFY ("요청ID" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("요청기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("지급부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("자료구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("지급명령등록번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("거래일자" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("지급명령번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("요구부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("출금은행코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("출금계좌번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("출금금액" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("입금총건수" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("자료수신여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("결과처리여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("재배정여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("지급형태" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("현금유형코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("현금종류코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" MODIFY ("거래번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장" ADD PRIMARY KEY ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "재배정여부")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 지급원장_반려
--------------------------------------------------------

  ALTER TABLE "지급원장_반려" ADD PRIMARY KEY ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "지급반려일련번호", "거래일자", "재배정여부")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;
 
  ALTER TABLE "지급원장_반려" MODIFY ("요청ID" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("요청기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("지급부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("자료구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("지급명령등록번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("지급반려일련번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("거래일자" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("지급명령번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("요구부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("출금은행코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("출금계좌번호" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("출금금액" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("입금총건수" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("자료수신여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("결과처리여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("재배정여부" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("지급형태" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("현금유형코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("현금종류코드" NOT NULL ENABLE);
 
  ALTER TABLE "지급원장_반려" MODIFY ("거래번호" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table 직인정보관리
--------------------------------------------------------

  ALTER TABLE "직인정보관리" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "직인정보관리" MODIFY ("회계구분코드" NOT NULL ENABLE);
 
  ALTER TABLE "직인정보관리" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "직인정보관리" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "직인정보관리" MODIFY ("부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "직인정보관리" MODIFY ("GCC부서코드" NOT NULL ENABLE);
 
  ALTER TABLE "직인정보관리" ADD PRIMARY KEY ("회계연도", "자치단체코드", "관서코드", "부서코드", "회계구분코드", "GCC부서코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 질의응답
--------------------------------------------------------

  ALTER TABLE "질의응답" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "질의응답" MODIFY ("등록순번" NOT NULL ENABLE);
 
  ALTER TABLE "질의응답" MODIFY ("상위등록번호" NOT NULL ENABLE);
 
  ALTER TABLE "질의응답" MODIFY ("등록번호" NOT NULL ENABLE);
 
  ALTER TABLE "질의응답" ADD PRIMARY KEY ("자치단체코드", "등록순번", "상위등록번호", "등록번호")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 채주계좌정보
--------------------------------------------------------

  ALTER TABLE "채주계좌정보" MODIFY ("순번" NOT NULL ENABLE);
 
  ALTER TABLE "채주계좌정보" MODIFY ("은행코드" NOT NULL ENABLE);
 
  ALTER TABLE "채주계좌정보" MODIFY ("계좌번호" NOT NULL ENABLE);
 
  ALTER TABLE "채주계좌정보" MODIFY ("결과처리여부" NOT NULL ENABLE);
 
  ALTER TABLE "채주계좌정보" ADD PRIMARY KEY ("순번", "은행코드", "계좌번호")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 파일순번
--------------------------------------------------------

  ALTER TABLE "파일순번" MODIFY ("거래구분" NOT NULL ENABLE);
 
  ALTER TABLE "파일순번" MODIFY ("거래일자" NOT NULL ENABLE);
 
  ALTER TABLE "파일순번" MODIFY ("점번" NOT NULL ENABLE);
 
  ALTER TABLE "파일순번" MODIFY ("순번" NOT NULL ENABLE);
 
  ALTER TABLE "파일순번" ADD PRIMARY KEY ("거래구분", "거래일자", "점번", "순번")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 파일처리로그
--------------------------------------------------------

  ALTER TABLE "파일처리로그" MODIFY ("거래일자" NOT NULL ENABLE);
 
  ALTER TABLE "파일처리로그" MODIFY ("거래번호" NOT NULL ENABLE);
 
  ALTER TABLE "파일처리로그" MODIFY ("자료수신여부" NOT NULL ENABLE);
 
  ALTER TABLE "파일처리로그" MODIFY ("결과처리여부" NOT NULL ENABLE);
 
  ALTER TABLE "파일처리로그" ADD PRIMARY KEY ("거래일자", "거래번호")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 한도관리
--------------------------------------------------------

  ALTER TABLE "한도관리" ADD PRIMARY KEY ("회계연도", "관서코드", "관서명")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;
 
  ALTER TABLE "한도관리" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "한도관리" MODIFY ("관서코드" NOT NULL ENABLE);
 
  ALTER TABLE "한도관리" MODIFY ("관서명" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table 회계정보
--------------------------------------------------------

  ALTER TABLE "회계정보" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "회계정보" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "회계정보" MODIFY ("회계구분마스터코드" NOT NULL ENABLE);
 
  ALTER TABLE "회계정보" MODIFY ("회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "회계정보" ADD PRIMARY KEY ("자치단체코드", "회계연도", "회계구분마스터코드", "회계코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Constraints for Table 회계코드맵핑
--------------------------------------------------------

  ALTER TABLE "회계코드맵핑" MODIFY ("자치단체코드" NOT NULL ENABLE);
 
  ALTER TABLE "회계코드맵핑" MODIFY ("회계연도" NOT NULL ENABLE);
 
  ALTER TABLE "회계코드맵핑" MODIFY ("회계구분마스트코드" NOT NULL ENABLE);
 
  ALTER TABLE "회계코드맵핑" MODIFY ("회계구분코드" NOT NULL ENABLE);
 
  ALTER TABLE "회계코드맵핑" MODIFY ("요청기관구분" NOT NULL ENABLE);
 
  ALTER TABLE "회계코드맵핑" MODIFY ("회계구분" NOT NULL ENABLE);
 
  ALTER TABLE "회계코드맵핑" MODIFY ("관리용회계코드" NOT NULL ENABLE);
 
  ALTER TABLE "회계코드맵핑" MODIFY ("관리용회계코드명" NOT NULL ENABLE);
 
  ALTER TABLE "회계코드맵핑" ADD PRIMARY KEY ("자치단체코드", "회계연도", "회계구분", "관리용회계코드")
  USING INDEX
  TABLESPACE "ICMSIDX"  ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table 입금명세
--------------------------------------------------------
  ALTER TABLE "입금명세" ADD FOREIGN KEY ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "재배정여부")
	  REFERENCES "지급원장" ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "재배정여부") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table 입금반려명세
--------------------------------------------------------
  ALTER TABLE "입금반려명세" ADD FOREIGN KEY ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "재배정여부", "삭제이력순번", "거래일자", "기관구분", "지급명령번호", "요구부서코드")
	  REFERENCES "지급반려명세" ("요청ID", "요청기관구분", "자치단체코드", "관서코드", "지급부서코드", "회계연도", "회계코드", "자료구분", "지급명령등록번호", "재배정여부", "삭제이력순번", "거래일자", "기관구분", "지급명령번호", "요구부서코드") ENABLE;

--------------------------------------------------------
--  DDL for Trigger TU_입금명세
--------------------------------------------------------
  CREATE OR REPLACE TRIGGER "TU_입금명세" after UPDATE on 입금명세 for each row
-- ERwin Builtin Mon Nov 07 14:09:20 2011
-- UPDATE trigger on 입금명세
declare numrows INTEGER;
begin
  /* ERwin Builtin Mon Nov 07 14:09:20 2011 */
  /* 지급원장 R/2 입금명세 ON CHILD UPDATE RESTRICT */
  /* ERWIN_RELATION:PARENT_OWNER="BX_USR", PARENT_TABLE="지급원장"
    CHILD_OWNER="BX_USR", CHILD_TABLE="입금명세"
    P2C_VERB_PHRASE="R/2", C2P_VERB_PHRASE="",
    FK_CONSTRAINT="R_1", FK_COLUMNS="요청ID""요청기관구분""자치단체코드""관？코드""지급부？코드""회계연도""
              회계코드""자료구분""지급명령등록번호""재배정여부" */
  select count(*) into numrows
    from 지급원장
    where
      /* %JoinFKPK(:%New,지급원장," = "," and") */
      :new.요청ID = 지급원장.요청ID and
      :new.요청기관구분 = 지급원장.요청기관구분 and
      :new.자치단체코드 = 지급원장.자치단체코드 and
      :new.관서코드 = 지급원장.관서코드 and
      :new.부서코드 = 지급원장.지급부서코드 and
      :new.회계연도 = 지급원장.회계연도 and
      :new.회계코드 = 지급원장.회계코드 and
      :new.자료구분 = 지급원장.자료구분 and
      :new.지급명령등록번호 = 지급원장.지급명령등록번호 and
      :new.재배정여부 = 지급원장.재배정여부;
  if (
    /* %NotnullFK(:%New," is not null and") */

    numrows = 0
  )
  then
    raise_application_error(
      -20007,
      'Cannot UPDATE 입금명세 because 지급원장 does not exist.'
    );
  end if;


-- ERwin Builtin Mon Nov 07 14:09:20 2011
end;
/
ALTER TRIGGER "TU_입금명세" ENABLE;

--------------------------------------------------------
--  DDL for Trigger TI_입금반려명세
--------------------------------------------------------
  CREATE OR REPLACE TRIGGER "TI_입금반려명세" after INSERT on 입금반려명세 for each row
-- ERwin Builtin Mon Nov 07 14:09:20 2011
-- INSERT trigger on 입금반려명세
declare numrows INTEGER;
begin
    /* ERwin Builtin Mon Nov 07 14:09:20 2011 */
    /* 지급반려명세 R/1 입금반려명세 ON CHILD INSERT RESTRICT */
    /* ERWIN_RELATION:PARENT_OWNER="", PARENT_TABLE="지급반려명세"
    CHILD_OWNER="BX_USR", CHILD_TABLE="입금반려명세"
    P2C_VERB_PHRASE="R/1", C2P_VERB_PHRASE="",
    FK_CONSTRAINT="R_3", FK_COLUMNS="요청ID""요청기관구분""자치단체코드""관？코드""지급부？코드""회계연도""
              회계코드""자료구분""지급명령등록번호""재배정여부""
              삭제이력순번""거래일자""기관구분""지급명령번호""
              요구부？코드" */
    select count(*) into numrows
      from 지급반려명세
      where
        /* %JoinFKPK(:%New,지급반려명세," = "," and") */
        :new.요청ID = 지급반려명세.요청ID and
        :new.요청기관구분 = 지급반려명세.요청기관구분 and
        :new.자치단체코드 = 지급반려명세.자치단체코드 and
        :new.관서코드 = 지급반려명세.관서코드 and
        :new.지급부서코드 = 지급반려명세.지급부서코드 and
        :new.회계연도 = 지급반려명세.회계연도 and
        :new.회계코드 = 지급반려명세.회계코드 and
        :new.자료구분 = 지급반려명세.자료구분 and
        :new.지급명령등록번호 = 지급반려명세.지급명령등록번호 and
        :new.재배정여부 = 지급반려명세.재배정여부 and
        :new.삭제이력순번 = 지급반려명세.삭제이력순번 and
        :new.거래일자 = 지급반려명세.거래일자 and
        :new.기관구분 = 지급반려명세.기관구분 and
        :new.지급명령번호 = 지급반려명세.지급명령번호 and
        :new.요구부서코드 = 지급반려명세.요구부서코드;
    if (
      /* %NotnullFK(:%New," is not null and") */

      numrows = 0
    )
    then
      raise_application_error(
        -20002,
        'Cannot INSERT 입금반려명세 because 지급반려명세 does not exist.'
      );
    end if;


-- ERwin Builtin Mon Nov 07 14:09:20 2011
end;
/
ALTER TRIGGER "TI_입금반려명세" ENABLE;

--------------------------------------------------------
--  DDL for Trigger TU_입금반려명세
--------------------------------------------------------
  CREATE OR REPLACE TRIGGER "TU_입금반려명세" after UPDATE on 입금반려명세 for each row
-- ERwin Builtin Mon Nov 07 14:09:20 2011
-- UPDATE trigger on 입금반려명세
declare numrows INTEGER;
begin
  /* ERwin Builtin Mon Nov 07 14:09:20 2011 */
  /* 지급반려명세 R/1 입금반려명세 ON CHILD UPDATE RESTRICT */
  /* ERWIN_RELATION:PARENT_OWNER="", PARENT_TABLE="지급반려명세"
    CHILD_OWNER="BX_USR", CHILD_TABLE="입금반려명세"
    P2C_VERB_PHRASE="R/1", C2P_VERB_PHRASE="",
    FK_CONSTRAINT="R_3", FK_COLUMNS="요청ID""요청기관구분""자치단체코드""관？코드""지급부？코드""회계연도""
              회계코드""자료구분""지급명령등록번호""재배정여부""
              삭제이력순번""거래일자""기관구분""지급명령번호""
              요구부？코드" */
  select count(*) into numrows
    from 지급반려명세
    where
      /* %JoinFKPK(:%New,지급반려명세," = "," and") */
      :new.요청ID = 지급반려명세.요청ID and
      :new.요청기관구분 = 지급반려명세.요청기관구분 and
      :new.자치단체코드 = 지급반려명세.자치단체코드 and
      :new.관서코드 = 지급반려명세.관서코드 and
      :new.지급부서코드 = 지급반려명세.지급부서코드 and
      :new.회계연도 = 지급반려명세.회계연도 and
      :new.회계코드 = 지급반려명세.회계코드 and
      :new.자료구분 = 지급반려명세.자료구분 and
      :new.지급명령등록번호 = 지급반려명세.지급명령등록번호 and
      :new.재배정여부 = 지급반려명세.재배정여부 and
      :new.삭제이력순번 = 지급반려명세.삭제이력순번 and
      :new.거래일자 = 지급반려명세.거래일자 and
      :new.기관구분 = 지급반려명세.기관구분 and
      :new.지급명령번호 = 지급반려명세.지급명령번호 and
      :new.요구부서코드 = 지급반려명세.요구부서코드;
  if (
    /* %NotnullFK(:%New," is not null and") */

    numrows = 0
  )
  then
    raise_application_error(
      -20007,
      'Cannot UPDATE 입금반려명세 because 지급반려명세 does not exist.'
    );
  end if;


-- ERwin Builtin Mon Nov 07 14:09:20 2011
end;
/

ALTER TRIGGER "TU_입금반려명세" ENABLE;
--------------------------------------------------------
--  DDL for Trigger TD_지급반려명세
--------------------------------------------------------
  CREATE OR REPLACE TRIGGER "TD_지급반려명세" after DELETE on 지급반려명세 for each row
-- ERwin Builtin Mon Nov 07 14:09:20 2011
-- DELETE trigger on 지급반려명세
declare numrows INTEGER;
begin
    /* ERwin Builtin Mon Nov 07 14:09:20 2011 */
    /* 지급반려명세 R/1 입금반려명세 ON PARENT DELETE RESTRICT */
    /* ERWIN_RELATION:PARENT_OWNER="", PARENT_TABLE="지급반려명세"
    CHILD_OWNER="BX_USR", CHILD_TABLE="입금반려명세"
    P2C_VERB_PHRASE="R/1", C2P_VERB_PHRASE="",
    FK_CONSTRAINT="R_3", FK_COLUMNS="요청ID""요청기관구분""자치단체코드""관？코드""지급부？코드""회계연도""
              회계코드""자료구분""지급명령등록번호""재배정여부""
              삭제이력순번""거래일자""기관구분""지급명령번호""
              요구부？코드" */
    select count(*) into numrows
      from 입금반려명세
      where
        /*  %JoinFKPK(입금반려명세,:%Old," = "," and") */
        입금반려명세.요청ID = :old.요청ID and
        입금반려명세.요청기관구분 = :old.요청기관구분 and
        입금반려명세.자치단체코드 = :old.자치단체코드 and
        입금반려명세.관서코드 = :old.관서코드 and
        입금반려명세.지급부서코드 = :old.지급부서코드 and
        입금반려명세.회계연도 = :old.회계연도 and
        입금반려명세.회계코드 = :old.회계코드 and
        입금반려명세.자료구분 = :old.자료구분 and
        입금반려명세.지급명령등록번호 = :old.지급명령등록번호 and
        입금반려명세.재배정여부 = :old.재배정여부 and
        입금반려명세.삭제이력순번 = :old.삭제이력순번 and
        입금반려명세.거래일자 = :old.거래일자 and
        입금반려명세.기관구분 = :old.기관구분 and
        입금반려명세.지급명령번호 = :old.지급명령번호 and
        입금반려명세.요구부서코드 = :old.요구부서코드;
    if (numrows > 0)
    then
      raise_application_error(
        -20001,
        'Cannot DELETE 지급반려명세 because 입금반려명세 exists.'
      );
    end if;


-- ERwin Builtin Mon Nov 07 14:09:20 2011
end;
/
ALTER TRIGGER "TD_지급반려명세" ENABLE;

--------------------------------------------------------
--  DDL for Trigger TU_지급반려명세
--------------------------------------------------------
  CREATE OR REPLACE TRIGGER "TU_지급반려명세" after UPDATE on 지급반려명세 for each row
-- ERwin Builtin Mon Nov 07 14:09:20 2011
-- UPDATE trigger on 지급반려명세
declare numrows INTEGER;
begin
  /* ERwin Builtin Mon Nov 07 14:09:20 2011 */
  /* 지급반려명세 R/1 입금반려명세 ON PARENT UPDATE RESTRICT */
  /* ERWIN_RELATION:PARENT_OWNER="", PARENT_TABLE="지급반려명세"
    CHILD_OWNER="BX_USR", CHILD_TABLE="입금반려명세"
    P2C_VERB_PHRASE="R/1", C2P_VERB_PHRASE="",
    FK_CONSTRAINT="R_3", FK_COLUMNS="요청ID""요청기관구분""자치단체코드""관？코드""지급부？코드""회계연도""
              회계코드""자료구분""지급명령등록번호""재배정여부""
              삭제이력순번""거래일자""기관구분""지급명령번호""
              요구부？코드" */
  if
    /* %JoinPKPK(:%Old,:%New," <> "," or ") */
    :old.요청ID <> :new.요청ID or
    :old.요청기관구분 <> :new.요청기관구분 or
    :old.자치단체코드 <> :new.자치단체코드 or
    :old.관서코드 <> :new.관서코드 or
    :old.지급부서코드 <> :new.지급부서코드 or
    :old.회계연도 <> :new.회계연도 or
    :old.회계코드 <> :new.회계코드 or
    :old.자료구분 <> :new.자료구분 or
    :old.지급명령등록번호 <> :new.지급명령등록번호 or
    :old.재배정여부 <> :new.재배정여부 or
    :old.삭제이력순번 <> :new.삭제이력순번 or
    :old.거래일자 <> :new.거래일자 or
    :old.기관구분 <> :new.기관구분 or
    :old.지급명령번호 <> :new.지급명령번호 or
    :old.요구부서코드 <> :new.요구부서코드
  then
    select count(*) into numrows
      from 입금반려명세
      where
        /*  %JoinFKPK(입금반려명세,:%Old," = "," and") */
        입금반려명세.요청ID = :old.요청ID and
        입금반려명세.요청기관구분 = :old.요청기관구분 and
        입금반려명세.자치단체코드 = :old.자치단체코드 and
        입금반려명세.관서코드 = :old.관서코드 and
        입금반려명세.지급부서코드 = :old.지급부서코드 and
        입금반려명세.회계연도 = :old.회계연도 and
        입금반려명세.회계코드 = :old.회계코드 and
        입금반려명세.자료구분 = :old.자료구분 and
        입금반려명세.지급명령등록번호 = :old.지급명령등록번호 and
        입금반려명세.재배정여부 = :old.재배정여부 and
        입금반려명세.삭제이력순번 = :old.삭제이력순번 and
        입금반려명세.거래일자 = :old.거래일자 and
        입금반려명세.기관구분 = :old.기관구분 and
        입금반려명세.지급명령번호 = :old.지급명령번호 and
        입금반려명세.요구부서코드 = :old.요구부서코드;
    if (numrows > 0)
    then
      raise_application_error(
        -20005,
        'Cannot UPDATE 지급반려명세 because 입금반려명세 exists.'
      );
    end if;
  end if;


-- ERwin Builtin Mon Nov 07 14:09:20 2011
end;
/
ALTER TRIGGER "TU_지급반려명세" ENABLE;

--------------------------------------------------------
--  DDL for Trigger TU_지급원장
--------------------------------------------------------
  CREATE OR REPLACE TRIGGER "TU_지급원장" after UPDATE on 지급원장 for each row
-- ERwin Builtin Mon Nov 07 14:09:20 2011
-- UPDATE trigger on 지급원장
declare numrows INTEGER;
begin
  /* ERwin Builtin Mon Nov 07 14:09:20 2011 */
  /* 지급원장 R/2 입금명세 ON PARENT UPDATE RESTRICT */
  /* ERWIN_RELATION:PARENT_OWNER="BX_USR", PARENT_TABLE="지급원장"
    CHILD_OWNER="BX_USR", CHILD_TABLE="입금명세"
    P2C_VERB_PHRASE="R/2", C2P_VERB_PHRASE="",
    FK_CONSTRAINT="R_1", FK_COLUMNS="요청ID""요청기관구분""자치단체코드""관？코드""지급부？코드""회계연도""
              회계코드""자료구분""지급명령등록번호""재배정여부" */
  if
    /* %JoinPKPK(:%Old,:%New," <> "," or ") */
    :old.요청ID <> :new.요청ID or
    :old.요청기관구분 <> :new.요청기관구분 or
    :old.자치단체코드 <> :new.자치단체코드 or
    :old.관서코드 <> :new.관서코드 or
    :old.지급부서코드 <> :new.지급부서코드 or
    :old.회계연도 <> :new.회계연도 or
    :old.회계코드 <> :new.회계코드 or
    :old.자료구분 <> :new.자료구분 or
    :old.지급명령등록번호 <> :new.지급명령등록번호 or
    :old.재배정여부 <> :new.재배정여부
  then
    select count(*) into numrows
      from 입금명세
      where
        /*  %JoinFKPK(입금명세,:%Old," = "," and") */
        입금명세.요청ID = :old.요청ID and
        입금명세.요청기관구분 = :old.요청기관구분 and
        입금명세.자치단체코드 = :old.자치단체코드 and
        입금명세.관서코드 = :old.관서코드 and
        입금명세.지급부서코드 = :old.지급부서코드 and
        입금명세.회계연도 = :old.회계연도 and
        입금명세.회계코드 = :old.회계코드 and
        입금명세.자료구분 = :old.자료구분 and
        입금명세.지급명령등록번호 = :old.지급명령등록번호 and
        입금명세.재배정여부 = :old.재배정여부;
    if (numrows > 0)
    then
      raise_application_error(
        -20005,
        'Cannot UPDATE 지급원장 because 입금명세 exists.'
      );
    end if;
  end if;


-- ERwin Builtin Mon Nov 07 14:09:20 2011
end;
/
ALTER TRIGGER "TU_지급원장" ENABLE;

--------------------------------------------------------
--  DDL for Trigger TD_지급원장
--------------------------------------------------------
  CREATE OR REPLACE TRIGGER "TD_지급원장" after DELETE on 지급원장 for each row
-- ERwin Builtin Mon Nov 07 14:09:20 2011
-- DELETE trigger on 지급원장
declare numrows INTEGER;
begin
    /* ERwin Builtin Mon Nov 07 14:09:20 2011 */
    /* 지급원장 R/2 입금명세 ON PARENT DELETE RESTRICT */
    /* ERWIN_RELATION:PARENT_OWNER="BX_USR", PARENT_TABLE="지급원장"
    CHILD_OWNER="BX_USR", CHILD_TABLE="입금명세"
    P2C_VERB_PHRASE="R/2", C2P_VERB_PHRASE="",
    FK_CONSTRAINT="R_1", FK_COLUMNS="요청ID""요청기관구분""자치단체코드""관？코드""지급부？코드""회계연도""
              회계코드""자료구분""지급명령등록번호""재배정여부" */
    select count(*) into numrows
      from 입금명세
      where
        /*  %JoinFKPK(입금명세,:%Old," = "," and") */
        입금명세.요청ID = :old.요청ID and
        입금명세.요청기관구분 = :old.요청기관구분 and
        입금명세.자치단체코드 = :old.자치단체코드 and
        입금명세.관서코드 = :old.관서코드 and
        입금명세.지급부서코드 = :old.지급부서코드 and
        입금명세.회계연도 = :old.회계연도 and
        입금명세.회계코드 = :old.회계코드 and
        입금명세.자료구분 = :old.자료구분 and
        입금명세.지급명령등록번호 = :old.지급명령등록번호 and
        입금명세.재배정여부 = :old.재배정여부;
    if (numrows > 0)
    then
      raise_application_error(
        -20001,
        'Cannot DELETE 지급원장 because 입금명세 exists.'
      );
    end if;


-- ERwin Builtin Mon Nov 07 14:09:20 2011
end;
/
ALTER TRIGGER "TD_지급원장" ENABLE;
