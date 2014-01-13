package org.icrisat.gdms.common;

public enum UploadType {
	
	SSRMarker("SSRMarker"),
	SNPMarker("SNPMarker"),
	CAPMarker("CAPMarker"),
	CISRMarker("CISRMarker"),
	SSRGenotype("SSRGenotype"),
	SNPGenotype("SNPGenotype"), 
	DARTGenotype("DARTGenotype"),
	Map("Map"), 
	QTL("QTL"), 
	AllelicData("AllelicData"),
	ABHData("ABHData"),
	;
	
	
	private String strMarkerType;
	
	private UploadType(String theMarkerType){
		strMarkerType = theMarkerType;
	}
	
	public String toString(){
		return strMarkerType;
	}
}
	
