package com.zylman.twitter;

@SuppressWarnings("serial")
public class DatabaseException extends Exception {
	private Exception underlyingException = null;
	private String message = null;
	
	public DatabaseException(Exception underlyingException, String message) {
		this.underlyingException = underlyingException;
		this.message = message;
	}
	
	public DatabaseException(Exception underlyingException) {
		this(underlyingException, null);
	}
	
	public DatabaseException(String message) {
		this(null, message);
	}

	public Exception getUnderlyingException() {
		return underlyingException;
	}
	
	public String getMessage() {
		return message;
	}
}
