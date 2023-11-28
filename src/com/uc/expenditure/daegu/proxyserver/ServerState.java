 /**
 * 
 */
package com.uc.expenditure.daegu.proxyserver;

/**
 * @author hotaep
 *
 */
public enum ServerState 
{
    /*
     *      <- 업무개시요구    -        0600
     *      - 업무개시통보     ->   0610
     *      <- 파일정보수신요구 -       0630
     *      - 파일정보수신통보 ->       0640
     *      <- 데이터송신      -        0320
     *      <- 데이터송신      -        0320
     *      <- 결번확인요구    -        0620
     *      - 결번확인통보     ->   0300
     *      <- 결번데이터송신   -   0310
     *      <- 결번데이터송신   -   0310
     *      <- 파일송신완료요구 -       0600
     *      - 파일송신완료통보 ->       0610
     *      <- 업무종료요구    ->   0600
     *      - 업무종료통보     ->   0610
     */ 
    OFFLINE,
    STOPPED,    /* 업무개시전 */
    STARTING,   /* 업무개시요구 */
    STARTED,    /* 업무개시통보 */
    CREATING,   /* 파일 */
    CREATED,    /* 파일준비 */
    CREATED2,    /* 파일준비 */
    CHECKING,   /* 확인 */
    CHECKED,    /* */
    COMPLETING, /* */
    COMPLETED,  /* */
    STOPPING;   /* */
}
