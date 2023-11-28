/**
 * 
 */
package com.uc.framework.synchronization;

/**
 * @author hotaep
 *
 */
public interface Blockable 
{
	public int	SUCCESS = 1;
	public int	INFINITE = -1;
	public int	TIMEOUT = 2;
	public int	INTERRUPT = 3;
	public int	NOTOWNED = 4;
	
	public int waitForSingleObject(long ulTimeout);
}
