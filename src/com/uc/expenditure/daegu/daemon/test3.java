package com.uc.expenditure.daegu.daemon;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class test3 {

    public static void main(String[] args) {

        FileInputStream fis=null;
        FileOutputStream fos=null;
        BufferedInputStream bis=null;
        byte[] buffer=new byte[470];
        try {
           fis=new FileInputStream("c:/data/CO1003_20231101093646993");
           bis = new BufferedInputStream(fis);
           while(true) {
              int readedByte=bis.read(buffer);
               if(readedByte == -1)break;
              System.out.println(new String(buffer,0,readedByte,"euc-kr"));
              String  str1 = new String(buffer,0,readedByte,"euc-kr");
              String  aaa = str1.substring(0,6);
               aaa = str1.substring(6,8);
             // System.out.println(aaa);
           }
        }catch(Exception e) {
           e.printStackTrace();
        }finally {
           try {
              if(fis!=null)fis.close();
              if(fos!=null)fos.close();
              if(bis!=null)bis.close();
           }catch(Exception e) {}
        }
    }
}
