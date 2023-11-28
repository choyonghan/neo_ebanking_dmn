/**
 * 
 */
package com.uc.framework.utils;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author hotaep
 *
 */
public class MyMap extends LinkedHashMap<Object, Object> 
{
    public Object getAny(Object key)
    {       
        return super.get(key);
    }
    
    public Object setAny(Object key, Object value)
    {       
        return super.put(key, value);
    }
    
	public Object getMap(Integer key)
	{		
		return super.get(key);
	}

	public Object getMap(Long key)
	{		
		return super.get(key);
	}

	public Object getMap(String key)
	{		
		return super.get(key);
	}

    public String getString(String key)
    {       
        Object  value = super.get(key);
        if (value == null) {
            return "";
        } else {
            if (value instanceof String) {
                return value.toString().trim();
            } else if (value instanceof byte[]) {
                return new String((byte[])value).trim();                
            } else {
                return value.toString().trim();
            }
        }
    }

    public String getString(String key, String encoding)
    {       
        Object  value = super.get(key);
        if (value == null) {
            return "";
        } else {
            try {
	            if (value instanceof String) {
	            	byte[]	v = value.toString().getBytes(encoding);
	            	
	            	int i = v.length;
	            	while (i-- > 0 && (v[i] == 32 || v[i] == (byte)0xa1)) {
//	            		System.out.println("[" + v[i] + "]");
	            	}  
	            	byte[] output = new byte[i+1]; 
	            	System.arraycopy(v, 0, output, 0, i+1);	            	
	                return new String(output, encoding);
	            } else if (value instanceof byte[]) {
	            	byte[]	v = (byte[])value;
	            	
	            	int i = v.length;
	            	while (i-- > 0 && (v[i] == 32 || v[i] == (byte)0xa1)) {
//	            		System.out.println("[" + v[i] + "]");
	            	}  
	            	byte[] output = new byte[i+1]; 
	            	System.arraycopy(v, 0, output, 0, i+1);	            	
					return new String(output, encoding);
	            } else {
	                return new String(value.toString().trim().getBytes(encoding));
	            }
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}                
        }
    }

    public byte[] getBytes(String key)
    {       
        Object  value = super.get(key);
        if (value == null) {
            return null;
        } else {
            if (value instanceof String) {
                return value.toString().getBytes();
            } else if (value instanceof byte[]) {
                return (byte[])value;                
            } else {
                return value.toString().getBytes();
            }
        }
    }

    public byte[] getBytes(String key, String encoding)
    {       
        Object  value = super.get(key);
        if (value == null) {
            return null;
        } else {
            if (value instanceof String) {
                try {
					return value.toString().getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				return null;
				}
            } else if (value instanceof byte[]) {
                return (byte[])value;                
            } else {
                try {
					return value.toString().getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
            }
        }
    }

    public Long getLong(String key)
    {       
        Object  value = super.get(key);
        if (value == null) {
            return null;
        } else {
            if (value instanceof String) {
                return Long.parseLong(value.toString().trim());
            } else if (value instanceof byte[]) {
                return Long.parseLong(new String((byte[])value).trim());                
            } else if (value instanceof Long) {
                return (Long)value;
            } else if (value instanceof Integer) {
                return Long.getLong(value.toString().trim());
            } else {
                return Long.parseLong(value.toString().trim());
            }
        }
    }

    public Integer getInteger(String key)
    {       
        Object  value = super.get(key);
        if (value == null) {
            return null;
        } else {
            if (value instanceof String) {
                return Integer.parseInt(value.toString().trim());
            } else if (value instanceof byte[]) {
                return Integer.parseInt(new String((byte[])value).trim());                
            } else if (value instanceof Long) {
                return (Integer)value;
            } else if (value instanceof Integer) {
                return (Integer)value;
            } else {
                return Integer.parseInt(value.toString().trim());
            }
        }
    }

    public void setString(String key, Object value)
	{
        if (value instanceof String) {
            super.put(key, value.toString().trim());
        } else if (value instanceof byte[]) {
            super.put(key, new String((byte[])value));                
        } else if (value instanceof Long) {
            super.put(key, String.valueOf(value));
        } else if (value instanceof Integer) {
            super.put(key, String.valueOf(value));
        } else {
            super.put(key, value.toString());
        }
	}
		
	public void setBytes(String key, Object value)
	{
        if (value instanceof String) {
            super.put(key, value.toString().getBytes());
        } else if (value instanceof byte[]) {
            super.put(key, (byte[])value);                
        } else if (value instanceof Long) {
            super.put(key, String.valueOf(value).getBytes());
        } else if (value instanceof Integer) {
            super.put(key, String.valueOf(value).getBytes());
        } else {
            super.put(key, value.toString().getBytes());
        }
	}
		
	public void setByteBuffer(String key, Object value)
	{
        if (value instanceof String) {
            super.put(key, value.toString().getBytes());
        } else if (value instanceof byte[]) {
            super.put(key, (byte[])value);                
        } else if (value instanceof Long) {
            super.put(key, (Long)value);
        } else if (value instanceof Integer) {
            super.put(key, (Long)value);
        } else {
            super.put(key, value.toString().getBytes());
        }
	}
		
    public void setLong(String key, Object value)
    {
        if (value instanceof String) {
            super.put(key, Long.parseLong(value.toString()));
        } else if (value instanceof byte[]) {
            super.put(key, Long.parseLong(new String((byte[])value)));                
        } else if (value instanceof Long) {
            long    lVal = (Long)value;
            super.put(key, lVal);
        } else if (value instanceof Integer) {
        	int	iVal = (Integer)value;
            super.put(key, (long)iVal);
        } else {
            super.put(key, value);
        }
    }
        
    public void setInteger(String key, Object value)
    {
        if (value instanceof String) {
            super.put(key, Integer.parseInt(value.toString()));
        } else if (value instanceof byte[]) {
            super.put(key, Integer.parseInt(new String((byte[])value)));                
        } else if (value instanceof Long) {
            long    lVal = (Long)value;
            int     iToVal = 0;
            if (lVal > Integer.MAX_VALUE) {
                iToVal = Integer.MAX_VALUE;
            } else if (lVal < Integer.MIN_VALUE) {
                iToVal = Integer.MIN_VALUE;            
            } else {
                iToVal = (int)lVal;                       
            }
            super.put(key, iToVal);
        } else if (value instanceof Integer) {
            super.put(key, (Integer)value);
        } else {
            super.put(key, value);
        }
    }
        
    public void setMap(String key, Object value)
    {
        super.put(key, value);
    }

    public Set<Object> getKeyList()
	{
		return this.keySet();
	}
		
	public Set getEntryList()
	{		
		return this.entrySet();	
	}
		
	public Collection<Object> getValueList()
	{
		return this.values();
	}
		
	public Map getMaps()
	{
		return this;
	}
		  
	public Iterator<Object> iterator() 
	{
		return this.getKeyList().iterator();
	}

	public String getKeyByValue(String strName) 
	{
		Iterator<Object> it = this.iterator();
		
		for (; it.hasNext(); ) {
			String key = (String) it.next();
			
			if(getString(key).equals(strName)) 
				return key;
		}		
		return null;
	}
}
