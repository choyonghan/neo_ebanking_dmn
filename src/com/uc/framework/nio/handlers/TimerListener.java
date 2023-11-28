/**
 * 
 */
package com.uc.framework.nio.handlers;

/**
 * @author hotaep
 *
 */
public abstract interface TimerListener
{
    /**
     * Event to be fired when the timeout expires.
     */
    public void timerExpired(Object handle);
}
