package org.icrisat.gdms.common;

public class GDMSException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String strExceptionMessage;
	
	public GDMSException(String theExceptionMessage) {
		super(theExceptionMessage);
		strExceptionMessage = theExceptionMessage;
	}

	public String getExceptionMessage() {
		return strExceptionMessage;
	}

	public void setExceptionMessage(String strExceptionMessage) {
		this.strExceptionMessage = strExceptionMessage;
	}
	
	public GDMSException(String theExceptionMessage, Throwable th) {
		super(theExceptionMessage, th);
		strExceptionMessage = theExceptionMessage;
	}
	

}
