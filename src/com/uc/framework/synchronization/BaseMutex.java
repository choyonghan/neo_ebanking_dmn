/**
 * 
 */
package com.uc.framework.synchronization;

/**
 * @author hotaep
 *
 */

public abstract class BaseMutex 
{
	public BaseMutex(boolean bManual, boolean binitSignaled) 
	{
        this.bSignaled = binitSignaled;
        this.bAuto = !bManual;
    }
    
	protected synchronized int waitForSingleObject(long ulTimeout) 
	{
		// 시그널 자동 초기화이면 초기화를 먼저 한다.
        if (bSignaled) {
            if (bAuto)
                reset();
            return Blockable.SUCCESS;
        }
        long t1, t2;
        try {
            t1 = System.currentTimeMillis();		// 현재시각
            wait(ulTimeout);						// 주어진 시간만큼 지연
            t2 = System.currentTimeMillis() - t1;	// 현재시각
        } catch (InterruptedException e) {
            return Blockable.INTERRUPT;
        }
        if (t2 >= ulTimeout)
            return Blockable.TIMEOUT;
        if (bAuto)
            reset();
        return Blockable.SUCCESS;
    }
	
	/**
	 *  시그널 송신
	 */
    protected synchronized void signal() 
    {
        bSignaled = true;
        if (bAuto)
            notify();
        else
            notifyAll();
    }
    
    /**
     * 시그널 초기화
     */
    protected synchronized void reset() {
        bSignaled = false;
    }
    
    // 지역변수 선언
    //----------------------------------------------
    boolean bSignaled;
    boolean bAuto;
}