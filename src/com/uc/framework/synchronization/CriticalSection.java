package com.uc.framework.synchronization;

public class CriticalSection extends BaseMutex {

	public CriticalSection()
	{
		super(false, true);
	}
	
	public synchronized void enterCriticalSection()
	{
		while (waitForSingleObject(Blockable.INFINITE) != Blockable.SUCCESS);
	}
	
	public synchronized void leaveCriticalSection()
	{
		signal();
	}
}
