/**
 * 
 */
package com.uc.framework.nio.io;

/**
 * @author hotaep
 *
 */
public enum EventType
{
    DISCONNECTED,
    CONNECTED,
    CLIENTACCEPTED,
    CLIENTDISCONNECTED,
    PACKETARRIVED,
    PACKETSENT,
    CONNECTIONFAILED,
    PACKETSENTFAILED,
    SOCKETERROR,
    SOCKETEXCEPTION,
    TIMEREXPIRED,
}
