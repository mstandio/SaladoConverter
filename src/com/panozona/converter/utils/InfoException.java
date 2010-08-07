package com.panozona.converter.utils;

/**
 *
 * @author Marek
 */
public class InfoException extends Exception{
    public InfoException() {}  
    public InfoException(String msg) { super(msg); }  
    public InfoException(Throwable cause) { super(cause); }  
    public InfoException(String msg, Throwable cause) { super(msg, cause); }
}
