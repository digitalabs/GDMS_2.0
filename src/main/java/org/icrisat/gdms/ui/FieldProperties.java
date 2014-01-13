package org.icrisat.gdms.ui;

public class FieldProperties {
	
	private String strFieldName;
	private String strIsReq;
	private String strMinLen;
	private String strMaxLen;
	
	public FieldProperties(String theFieldName, String isReq, String theMinLen,
			String theMaxLen) {
		strFieldName = theFieldName;
		strIsReq = isReq;
		strMinLen = theMinLen;
		strMaxLen = theMaxLen;
	}

	public String getFieldName() {
		return strFieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.strFieldName = fieldName;
	}
	
	public String getIsReq() {
		return strIsReq;
	}
	
	public void setIsReq(String isReq) {
		this.strIsReq = isReq;
	}
	
	public String getMinLen() {
		return strMinLen;
	}
	
	public void setMinLen(String minLen) {
		this.strMinLen = minLen;
	}
	
	public String getMaxLen() {
		return strMaxLen;
	}
	
	public void setMaxLen(String maxLen) {
		this.strMaxLen = maxLen;
	}

}
