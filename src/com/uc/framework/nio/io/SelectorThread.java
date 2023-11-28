package com.uc.framework.nio.io;

import java.io.IOException;
import java.nio.channels.*;
import java.util.*;

import org.apache.log4j.Logger;

final public class SelectorThread implements Runnable 
{
	private Selector 		selector;
	private final Thread	selectorThread;
	private boolean			closeRequested = false;
	private final List<Runnable> 		pendingInvocations = new ArrayList<Runnable>(32);
	private Logger 			logger = null;
  
	public SelectorThread(Logger logger) throws IOException 
	{
		// Selector for incoming time requests
		this.logger = logger;
		selector = Selector.open();
		selectorThread = new Thread(this);
		logger.trace("SelectorThread started");
		selectorThread.start();
	}
  
	/**
	 * 접속종료 요구
	 */
	public void requestClose() 
	{
		logger.trace("requestClose in [" + Thread.currentThread().getName() + "]");
		closeRequested = true;
		// Nudges the selector.
		selector.wakeup();
	}
  
	/**
	 * 수행할 Ops
	 * @param channel
	 * @param interest
	 * @throws IOException
	 */
	public void addChannelInterestNow(SelectableChannel channel, int interest) throws IOException 
	{
		logger.trace("addChannelInterestNow in [" + Thread.currentThread().getName() + "]");
		if (Thread.currentThread() != selectorThread) {
//            throw new IOException("Method can only be called from selector thread");
            logger.error("메소드는 selector쓰레드에서만 호출가능합니다.");
            throw new IOException("메소드는 selector쓰레드에서만 호출가능합니다.");
		}
		SelectionKey sk = channel.keyFor(selector);
		changeKeyInterest(sk, sk.interestOps() | interest);
		logger.trace("addChannelInterestNow done");
	}
 
	/**
	 * 수행할 Ops
	 * @param channel
	 * @param interest
	 * @param errorHandler
	 */
	public void addChannelInterestLater(final SelectableChannel channel, final int interest, final CallbackErrorHandler errorHandler) 
	{
		logger.trace("addChannelInterestLater in [" + Thread.currentThread().getName() + "]");

		// Add a new runnable to the list of tasks to be executed in the selector thread
		logger.trace("SelectableChannel[" + channel.toString() +"]");
		invokeLater(new Runnable() {
			public void run() {
				try {
					addChannelInterestNow(channel, interest);
				} catch (IOException e) {
					errorHandler.handleError(e);
				}
			}
		});
		logger.trace("addChannelInterestLater done");
	}
  
	/**
	 * 삭제
	 * @param channel
	 * @param interest
	 * @throws IOException
	 */
	public void removeChannelInterestNow(SelectableChannel channel, int interest) throws IOException 
	{
		logger.trace("removeChannelInterestNow in [" + Thread.currentThread().getName() + "]");

		if (Thread.currentThread() != selectorThread) {
			throw new IOException("Method can only be called from selector thread");
		}
		SelectionKey sk = channel.keyFor(selector);
		changeKeyInterest(sk, sk.interestOps() & ~interest);
		logger.trace("removeChannelInterestNow done");
	}

	/**
	 * 삭제
	 * @param channel
	 * @param interest
	 * @param errorHandler
	 */
	public void removeChannelInterestLater(final SelectableChannel channel, final int interest, final CallbackErrorHandler errorHandler)  
	{
		logger.trace("removeChannelInterestLater in [" + Thread.currentThread().getName() + "]");

		invokeLater(new Runnable() {
			public void run() {
				try {
					removeChannelInterestNow(channel, interest);
				} catch (IOException e) {
					errorHandler.handleError(e);
				}
			}
		});
		logger.trace("removeChannelInterestLater done");
	}

	/**
	 * 변경
	 * @param sk
	 * @param newInterest
	 * @throws IOException
	 */
	private void changeKeyInterest(SelectionKey sk, int newInterest) throws IOException 
	{
    /* This method might throw two unchecked exceptions:
     * 1. IllegalArgumentException  - Should never happen. It is a bug if it happens
     * 2. CancelledKeyException - Might happen if the channel is closed while
     * a packet is being dispatched. 
     */
		logger.trace("changeKeyInterest in [" + Thread.currentThread().getName() + "]");

		try {
			sk.interestOps(newInterest);
		} catch (CancelledKeyException cke) {
			IOException ioe = new IOException("Failed to change channel interest.");
			ioe.initCause(cke);
			throw ioe;
		}
		logger.trace("changeKeyInterest done");
	}

	/**
	 * 채널등록
	 * @param channel
	 * @param selectionKeys
	 * @param handlerInfo
	 * @param errorHandler
	 */
	public void registerChannelLater(final SelectableChannel channel, final int selectionKeys, final SelectorHandler handlerInfo, final CallbackErrorHandler errorHandler) 
	{
		logger.trace("registerChannelLater in [" + Thread.currentThread().getName() + "]");

		invokeLater(new Runnable() {
			public void run() {
				try {
					logger.trace("channel[" + channel.toString() + "], selectionKeys[" + selectionKeys + "], handlerInfo[" + handlerInfo.toString() + "]");
					registerChannelNow(channel, selectionKeys, handlerInfo);
				} catch (IOException e) {
					errorHandler.handleError(e);
				}
			}
		});
		logger.trace("registerChannelLater done");
	}  

	/**
	 * 채널등록
	 */
	public void registerChannelNow(SelectableChannel channel, int selectionKeys, SelectorHandler handlerInfo) throws IOException 
	{
		logger.trace("registerChannelNow in [" + Thread.currentThread().getName() + "]");

		if (Thread.currentThread() != selectorThread) {
		    logger.error("registerChannelNow : Method can only be called from selector thread");
			throw new IOException("Method can only be called from selector thread");
		}
    
		if (!channel.isOpen()) { 
			logger.error("Channel is not open.");
			throw new IOException("Channel is not open.");
		}
    
		try {
			logger.trace("register = " + channel.toString());
			if (channel.isRegistered()) {
				SelectionKey sk = channel.keyFor(selector);
				assert sk != null : "Channel is already registered with other selector";        
				sk.interestOps(selectionKeys);
				Object previousAttach = sk.attach(handlerInfo);
				assert previousAttach != null;
			} else {  
				channel.configureBlocking(false);
				channel.register(selector, selectionKeys, handlerInfo);      
				logger.trace("registered channel. selector[" + selector.toString() + "]");
			}  
		} catch (Exception e) {
			IOException ioe = new IOException("Error registering channel.");
			ioe.initCause(e);
			logger.error("Error registering channel.");
			throw ioe;      
		}
		logger.trace("registerChannelNow done");
	}  
  
	/**
	 * 기동
	 * @param run
	 */
	public void invokeLater(Runnable run) 
	{
		synchronized (pendingInvocations) {
			pendingInvocations.add(run);
		}
		logger.trace("invokeLater called from [" + Thread.currentThread().getName() + "]");
		selector.wakeup();
		logger.trace("wakeup selector");
	}
  
	/**
	 * 기동
	 * @param task
	 * @throws InterruptedException
	 */
	public void invokeAndWait(final Runnable task) throws InterruptedException  
	{
            logger.trace("invokeAndWait");
		if (Thread.currentThread() == selectorThread) {
			// We are in the selector's thread. No need to schedule execution
			task.run();      
		} else {
			// Used to deliver the notification that the task is executed    
			final Object latch = new Object();
			synchronized (latch) {
				// Uses the invokeLater method with a newly created task 
				this.invokeLater(new Runnable() {
					public void run() {
						task.run();
						// Notifies
						latch.notify();
					}
				});
				// Wait for the task to complete.
				latch.wait();
			}
			// Ok, we are done, the task was executed. Proceed.
		}
        logger.trace("invokeAndWait done");
	}
  
	/**
	 * 보류중인 업무
	 * 
	 */
	private void doInvocations() 
	{
		synchronized (pendingInvocations) {
			for (int i = 0; i < pendingInvocations.size(); i++) {
				Runnable task = (Runnable) pendingInvocations.get(i);
//				logger.trace("지연된 작업들을 실행합니다. 작업[" + task.getClass().getName() + "]");
				task.run();
			}
			pendingInvocations.clear();
		}
	}

	/**
	 * 주 함수
	 */
	public void run() 
	{
            logger.trace("SelectorThread[" + Thread.currentThread().getName() + "] run");
		// Here's where everything happens. The select method will
		// return when any operations registered above have occurred, the
		// thread has been interrupted, etc.    
		while (true) {   
			// Execute all the pending tasks.
			doInvocations();
      
			// Time to terminate? 
			if (closeRequested) {
				return;
			}
			
			int selectedKeys = 0;
			try {
				selectedKeys = selector.select();
//				logger.trace("selector[" + selector.keys().toString() + "]");
			} catch (IOException ioe) {
				// Select should never throw an exception under normal 
				// operation. If this happens, print the error and try to 
				// continue working.
//				ioe.printStackTrace();
				logger.error("select error [" + ioe.getMessage());
				continue;
			}
      
			if (selectedKeys == 0) {
				// Go back to the beginning of the loop
				//    	  logger.trace("selectedKeys is null");
				continue;
			}
      
			logger.trace("select event occured...");

			// Someone is ready for IO, get the ready keys
			Iterator it = selector.selectedKeys().iterator();
			// Walk through the collection of ready keys and dispatch
			// any active event.
			while (it.hasNext()) {
                logger.trace("selectedKeys 있음");
				SelectionKey sk = (SelectionKey)it.next();
				it.remove();
				try {
					// Obtain the interest of the key
					int readyOps = sk.readyOps();
					// Disable the interest for the operation that is ready.
					// This prevents the same event from being raised multiple 
					// times.
					sk.interestOps(sk.interestOps() & ~readyOps);
					SelectorHandler handler = (SelectorHandler) sk.attachment();          
          
					// Some of the operations set in the selection key
					// might no longer be valid when the handler is executed. 
					// So handlers should take precautions against this 
					// possibility.
          
					// Check what are the interests that are active and
					// dispatch the event to the appropriate method.
					if (sk.isAcceptable()) {
						// A connection is ready to be completed
                        logger.trace("sk is acceptable");
						((AcceptSelectorHandler)handler).handleAccept(logger);
					} else if (sk.isConnectable()) {
						// A connection is ready to be accepted            
                        logger.trace("sk is connectable");
						((ConnectorSelectorHandler)handler).handleConnect();            
					} else {
						ReadWriteSelectorHandler rwHandler = (ReadWriteSelectorHandler)handler; 
						// Readable or writable              
						if (sk.isReadable()) {                
							// It is possible to read
                            logger.trace("sk is readable");
							rwHandler.handleRead();              
						}
            
						// Check if the key is still valid, since it might 
						// have been invalidated in the read handler 
						// (for instance, the socket might have been closed)
						if (sk.isValid() && sk.isWritable()) {
							// It is read to write
                            logger.trace("sk is wriable");
							rwHandler.handleWrite();                              
						}
					}
				} catch (Throwable t) {
					// No exceptions should be thrown in the previous block!
					// So kill everything if one is detected. 
					// Makes debugging easier.
				    logger.error("system exit: " + t);
					closeSelectorAndChannels();
					return;
				}
			}
		}
	}
    
	private void closeSelectorAndChannels() 
	{
		Set keys = selector.keys();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			SelectionKey key = (SelectionKey)iter.next();
			try {
				key.channel().close();
			} catch (IOException e) {
				// Ignore
			}
		}
		try {
			selector.close();
		} catch (IOException e) {
			// Ignore
		}
	}
}
