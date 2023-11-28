package com.uc.framework.nio.io;

import org.apache.log4j.Logger;

public interface AcceptSelectorHandler extends SelectorHandler 
{
	public void handleAccept(Logger logger);
}
