 /**
 * 
 */
package com.uc.expenditure.daegu.server;

/**
 * @author hotaep
 *
 */
public enum ClientState 
{
	/*
	 * 은행송신 모듈
	 * 센터                  은행
	 *      <- 업무개시요구    -		0600
	 *      - 업무개시통보     ->	0610
	 *      - 파일정보수신요구 ->		0630
	 *      <- 파일정보수신보고 -		0640
	 *      - 데이터송신       ->	0320
	 *      - 데이터송신       ->	0320
	 *      - 결번확인지시     ->	0620
	 *      <- 결번확인보고     -	0300
	 *      - 결번데이터송신   ->	0310
	 *      - 결번데이터송신   ->	0310
	 *      - 파일송신완료지시 ->		0600
	 *      <- 파일송신완료보고 -		0610
	 *      - 업무종료지시     ->	0600
	 *      <- 업무종료보고    -		0610
	 */
	OFFLINE,
	STOPPED,	/* 업무개시전 */
	STARTING,	/* 업무개시요구 */
	STARTED,	/* 업무개시통보 */
	CREATING,	/* 파일 */
    CREATED,    /* 파일준비 */
    CREATED2,    /* 파일준비 */
	CHECKING,	/* 확인 */
	CHECKED,	/* */
	COMPLETING,	/* */
	COMPLETED,	/* */
	STOPPING,	/* */
	PRESTOPPED,	/* 업무개시전 */
	POSTSTOPPED,
	POSTSTOPPED2;
}
