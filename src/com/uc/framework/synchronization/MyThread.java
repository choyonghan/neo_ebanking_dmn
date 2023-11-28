/**
 * 
 */
package com.uc.framework.synchronization;

import java.util.Date;

/**
 * @author hotaep
 *
 */
public class MyThread extends Thread 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
        MyThread thrd = new MyThread();
        thrd.start();
        try {
            Thread.sleep(10000);    // sleep for 10 seconds
        } catch (InterruptedException e) { }
        thrd.stop();
        System.exit(0);
	}

    public void run() 
    {
        while (true) {
            System.out.println("It is now " + new Date());
            try {
                Thread.sleep(1000); // sleep for 1 second
            } catch (InterruptedException e) { }
        }
    }
	
}
