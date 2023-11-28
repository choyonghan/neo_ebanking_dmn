/**
 * 
 */
package com.uc.framework.nio.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import com.uc.framework.utils.MyMap;

/**
 * @author hotaep
 *
 */
public abstract interface Protocol 
{
	  public ByteBuffer	encode(MyMap map, Object[] args, int argc) throws IOException;
	  public ByteBuffer decode(ByteBuffer bBuffer, boolean received) throws IOException;
	  public boolean hasRemaining();
}
