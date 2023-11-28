/**
 * 
 */
package com.uc.framework.synchronization;

/**
 * @author hotaep
 *
 */
public class Semaphore implements Blockable 
{
	public static final int INVALID_INCREMENT = -1;
    
    public Semaphore(int initialValue, int maxValue) 
    {
        currentLocks = initialValue;
        maxLocks = maxValue;
    }
    
    public int waitForSingleObject() 
    {
        return this.waitForSingleObject(INFINITE);
    }
    
    public synchronized int waitForSingleObject(long ulTimeout) 
    {
        long t1, t2;
        if (currentLocks == maxLocks) {
            try {
                t1 = System.currentTimeMillis();
                wait(ulTimeout);
                t2 = System.currentTimeMillis() - t1;
                if (t2 >= ulTimeout)
                    return TIMEOUT;
            } catch (InterruptedException e) {
                return INTERRUPT;
            }
        }
        currentLocks++;
        return SUCCESS;
    }
    
    public synchronized int releaseSemaphore(int increment) 
    {
        if (currentLocks - increment >= 0) {
            currentLocks -= increment;
            for (int i=0; i < increment; i++)
                notify();
            return SUCCESS;
        }
        return INVALID_INCREMENT;
    }
    
    // private data members
    private int currentLocks;
    private int maxLocks;
}