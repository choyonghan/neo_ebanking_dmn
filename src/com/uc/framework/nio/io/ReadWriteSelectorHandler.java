package com.uc.framework.nio.io;

public interface ReadWriteSelectorHandler extends SelectorHandler 
{  
  public void handleRead();
  public void handleWrite();  
}