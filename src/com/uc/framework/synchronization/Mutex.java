/**
 * 
 */
package com.uc.framework.synchronization;

/**
 * @author hotaep
 *
 */
public class Mutex extends BaseMutex implements Blockable 
{
	public Mutex(boolean bInitialOwner)
	{
		super(false, !bInitialOwner);
	    if (bInitialOwner) {
	    	count = 1;
	        thrd = Thread.currentThread();
	    } else {
	    	count = 0;
	        thrd = null;
	    }
	}

	public int waitForSingleObject() 
	{
		return this.waitForSingleObject(INFINITE);
	}
	
	public synchronized int waitForSingleObject(long ulTimeout) 
	{
		// if the mutex is not owned by any other thread
		if (count == 0) {
			count = 1;
			thrd = Thread.currentThread();
			return super.waitForSingleObject(ulTimeout);
		}
		// Otherwise, check to see if the current owner is the same thread 
		// calling now; if so, grant immediate access
		if (thrd == Thread.currentThread()) {
			count++;
			return SUCCESS;
		}
		// Otherwise block and wait for the current owner to relinquish mutex
	    int rc = super.waitForSingleObject(ulTimeout);
	    if (rc == SUCCESS) {
	        count = 1;
	        thrd = Thread.currentThread();
	    }
	    return rc;
	}
	
	public synchronized int releaseMutex() 
	{
	    if (thrd != null)
	        if (thrd == Thread.currentThread()) {
	            count--;
	            if (count == 0) {
	                thrd = null;
	                super.signal();
	            }
	            return SUCCESS;
	        }
	    return NOTOWNED;
	}
	// private data members
	private int    count;
	private Thread thrd;
}
