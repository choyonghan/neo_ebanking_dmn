package com.uc.framework.nio.io;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.ListIterator;
import org.apache.log4j.Logger;
import com.uc.framework.nio.handlers.TimerListener;

public class TimerThread extends Thread
{
	private static Logger	logger = Logger.getLogger(TimerThread.class);

    private static class TimerRequest
    {
        final long time;
        final long currtime;
        final TimerListener target;
        final Object		handle;

        TimerRequest(int timeout, TimerListener target, Object handle)
        {
            if (timeout <= 0) {
                throw new IllegalArgumentException("Invalid timeout parameter "
                        + timeout);
            }
            this.currtime = System.currentTimeMillis();
            this.time = this.currtime + (timeout);
            this.target = target;
            this.handle = handle;
        }
    }

    private static TimerThread instance;
    private final LinkedList<TimerRequest> timerList = new LinkedList<TimerRequest>();
    private long currTimeout;
    private long nextTimeout;

    /**
     * Singleton getter.
     */
    public static synchronized TimerThread getInstance()
    {
        if (instance == null) {
        	logger.info("getInstance time !!: 1");
            instance = new TimerThread();
            instance.start();
        }
        logger.info("instance.getClass() :: "+instance.getClass());
        return instance;
    }

    public TimerThread()
    {
        super("jTDS TimerThread");
        this.setDaemon(true);
    }

    public void run()
    {
    	logger.info("--run start--");
        synchronized (timerList) {
            while (true) {
            	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss.SSS");
            	logger.info("thisStats()-nextTimeout: " + this.getState() + " : " + nextTimeout + " : " + simpleDateFormat.format(nextTimeout));
            	logger.info("thisStats()-currTimeout : " + this.getState() + " : " + currTimeout + " : " + simpleDateFormat.format(currTimeout));

            	try {
                    try {
                    	logger.info("time wait before");
                    	timerList.wait(nextTimeout == 0 ? 0 : nextTimeout - currTimeout);
                    	logger.info("time wait after");
                    } catch (IllegalArgumentException ex) {
                        logger.info("IllegalArgumentException : " + ex);
                    }

                    long time = System.currentTimeMillis();

                   logger.info("timerList.size before : " + timerList.size());
                    while (!timerList.isEmpty()) {
                        TimerRequest t = (TimerRequest) timerList.getFirst();
                        if (t.time > time) {
                        	logger.info("time !!");
                            //break; // No timers have expired
                        }
                        logger.info("***timerExpired***");
                        t.target.timerExpired(t.handle);
                        logger.info("***removeFirst***");
                        timerList.removeFirst();
                    }

                    logger.info("timerList.size after : " + timerList.size());
                    updateNextTimeout();
                } catch (InterruptedException e) {
                	logger.info("##ERROR## : " + e);
                }
            }
        }
    }

    public Object setTimer(int timeout, TimerListener l, Object handle)
    {
        TimerRequest t = new TimerRequest(timeout, l, handle);
        synchronized (timerList) {
            if (timerList.isEmpty()) {
                timerList.add(t);
            } else {
                TimerRequest crt = (TimerRequest) timerList.getLast();
                if (t.time >= crt.time) {
                    timerList.addLast(t);
                } else {
                    for (ListIterator li = timerList.listIterator(); li.hasNext(); ) {
                    	crt = (TimerRequest) li.next();
                    	logger.info("#crt.time : "+ crt.time + " t.time : "  + t.time);
                    	if (t.time < crt.time) {
                    		logger.info("#crt.time : "+ crt.time + " t.time : "  + t.time);
                            li.previous();
                            li.add(t);
                            break;
                        }
                    }
                }
            }

            logger.info("***timerList.size()*** : " + timerList.size() + " : " + timeout);
            try {
            	 if (timerList.getFirst() == t) {
                     nextTimeout = t.time;
                     currTimeout = t.currtime;
                     logger.info("CHECK : " + l.getClass() +  " : " + this.getState());
                     logger.info("this.isInterrupted()-before : " + l.getClass() + " : " + this.isInterrupted());
                     this.interrupt();
                     logger.info("this.isInterrupted()-after : " + l.getClass() + " : " + this.isInterrupted());
                 }else {
                	 logger.info("CHECKERROR : " + l.getClass() +  " : " + this.getState());
                 }
            }catch(Exception e) {
            	logger.info("ERROR : " + e);
            }
        }

        return t;
    }

    public boolean cancelTimer(Object handle)
    {
        TimerRequest t = (TimerRequest) handle;

        synchronized (timerList) {
            boolean result = timerList.remove(t);
            if (nextTimeout == t.time) {
                updateNextTimeout();
            }
            return result;
        }
    }

    public boolean hasExpired(Object handle)
    {
        TimerRequest t = (TimerRequest) handle;

        synchronized (timerList) {
            return !timerList.contains(t);
        }
    }

    private void updateNextTimeout()
    {
    	currTimeout = timerList.isEmpty() ? 0
                : ((TimerRequest) timerList.getFirst()).currtime;

        nextTimeout = timerList.isEmpty() ? 0
                : ((TimerRequest) timerList.getFirst()).time;
    }
}