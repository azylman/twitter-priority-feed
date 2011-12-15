package com.zylman.twitter;

@SuppressWarnings("serial")
public class TwitterException extends Exception {
	private Exception underlyingException = null;
	private String message = null;
	
	public TwitterException(Exception underlyingException, String message) {
		this.underlyingException = underlyingException;
		this.message = message;
	}
	
	public TwitterException(Exception underlyingException) {
		this(underlyingException, null);
	}
	
	public TwitterException(String message) {
		this(null, message);
	}

	public Exception getUnderlyingException() {
		return underlyingException;
	}
	
	public String getMessage() {
		return message;
	}
}
