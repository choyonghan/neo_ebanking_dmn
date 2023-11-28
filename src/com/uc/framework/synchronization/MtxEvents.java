/**
 * 
 */
package com.uc.framework.synchronization;

/**
 * @author hotaep
 *
 */
public class MtxEvents extends BaseMutex implements Blockable 
{

	public MtxEvents(boolean bManual, boolean binitSignaled) 
	{
		super(bManual, binitSignaled);
		// TODO Auto-generated constructor stub
	}

	public int waitForSingleObject() 
	{
        return super.waitForSingleObject(INFINITE);
    }

	public synchronized int waitForSingleObject(long ulTimeout) 
	{
        return super.waitForSingleObject(ulTimeout);
    }
	
    public synchronized void setEvent() 
    {
        signal();
    }
    
    public synchronized void resetEvent() 
    {
        reset();
    }
    
    public synchronized void pulseEvent() 
    {
        signal();
        reset();
    }
}
