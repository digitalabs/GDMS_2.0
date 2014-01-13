package org.icrisat.gdms.upload;


import org.generationcp.middleware.pojos.gdms.MappingPopValues;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerAlias;
import org.generationcp.middleware.pojos.gdms.MarkerDetails;
import org.generationcp.middleware.pojos.gdms.MarkerUserInfo;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.icrisat.gdms.common.GDMSException;


public abstract class AbstractUploadData {
	

	protected String strMarkerType;
	protected String strUploadType;
	protected String strFileLocation;
	protected Marker[] arrayOfMarkers;
	protected MarkerAlias[] arrayOfMarkerAlias;
	protected MarkerUserInfo[] arrayOfMarkerUserInfo;
	protected MarkerDetails[] arrayOfMarkerDetails;
	protected Qtl[] arrayOfQTLs;
	private MappingPopValues[] arrayOfMappingPopValues;
	public String strMapSelectedOnTheUI;
	

	protected abstract void readFile() throws GDMSException;
	
	protected abstract void validateData() throws GDMSException;
	
	protected abstract void createObjects() throws GDMSException;
	
	protected abstract void save() throws GDMSException;
	
	public void uploadSheet() throws GDMSException{
		
		readFile();
		
		validateData();
		
		createObjects();
	}
	
	public Marker[] getArrayOfUploadedMarkers(){
		return arrayOfMarkers;
	}

	public void setArrayOfMarkers(Marker[] arrayOfMarkers) {
		this.arrayOfMarkers = arrayOfMarkers;
	}

	public MarkerAlias[] getArrayOfUploadedMarkerAlias() {
		return arrayOfMarkerAlias;
	}

	public void setArrayOfMarkerAlias(MarkerAlias[] arrayOfMarkerAlias) {
		this.arrayOfMarkerAlias = arrayOfMarkerAlias;
	}

	public MarkerUserInfo[] getArrayOfUploadedMarkerUserInfo() {
		return arrayOfMarkerUserInfo;
	}

	public void setArrayOfMarkerUserInfo(MarkerUserInfo[] arrayOfMarkerUserInfo) {
		this.arrayOfMarkerUserInfo = arrayOfMarkerUserInfo;
	}

	public MarkerDetails[] getArrayOfUploadedMarkerDetails() {
		return arrayOfMarkerDetails;
	}

	public void setArrayOfMarkerDetails(MarkerDetails[] arrayOfMarkerDetails) {
		this.arrayOfMarkerDetails = arrayOfMarkerDetails;
	}

	public String getUploadType() {
		return strUploadType;
	}

	public void setUploadType(String theUploadType) {
		this.strUploadType = theUploadType;
	}

	public void setFileLocation(String theFileLocation) {
		this.strFileLocation = theFileLocation;
	}

	public String getFileLocation() {
		return strFileLocation;
	}

	public Qtl[] getArrayOfUploadedQTLs() {
		return arrayOfQTLs;
	}
	
	public MappingPopValues[] getArrayOfUploadedMappings() {
		return arrayOfMappingPopValues;
	}

	public String getMarkerType() {
		return strMarkerType;
	}

	public void setMarkerType(String theMarkerType) {
		this.strMarkerType = theMarkerType;
	}

	public void setMapForMarker(String theMapSelected) {
		strMapSelectedOnTheUI = theMapSelected;
	}
	
	public String getMapForMarker(){
		return strMapSelectedOnTheUI;
	}


}
