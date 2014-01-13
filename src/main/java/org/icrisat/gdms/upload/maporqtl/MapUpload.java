package org.icrisat.gdms.upload.maporqtl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerInfo;
import org.generationcp.middleware.pojos.gdms.MarkerOnMap;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;

public class MapUpload implements UploadMarker {

	private String strFileLocation;
	private Workbook workbook;
	private Sheet sheetMapDetails;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceRowsFromSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataRowsFromSheet;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private Marker marker;
	private MarkerOnMap markerOnMap;
	private Map map;
	private Marker[] arrayOfMarkers;
	private MarkerOnMap[] arrayOfMarkersOnMap;
	private Map[] arrayOfMaps;

	String strMapName ="";
	List chList = new ArrayList();
	int iNumOfRowsFromDataTable =0;
	
	ManagerFactory factory;
    GenotypicDataManager genoManager;
	
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			workbook = Workbook.getWorkbook(new File(strFileLocation));
			sheetMapDetails = workbook.getSheet(0);
			strSheetNames = workbook.getSheetNames();
		} catch (BiffException e) {
			throw new GDMSException("Error Reading Map Upload Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading Map Upload Sheet - " + e.getMessage());
		}
	}

	
	@Override
	public void validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("map")){
			throw new GDMSException("Map Sheet Name Not Found");
		}

		Sheet mapSheet = workbook.getSheet(strSheetNames[0]);
		String strArrayOfReqColNames[] = {"Map Name", "Map Description", "Crop", "Map Unit"};

		for(int j = 0; j < strArrayOfReqColNames.length; j++){
			String strColFromSheet = (String)mapSheet.getCell(0, j).getContents().trim();
			if(!strArrayOfReqColNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
				throw new GDMSException(strColFromSheet + " column not found");
			}
			if(strColFromSheet == null || strColFromSheet == ""){
				throw new GDMSException("Delete empty column " + strColFromSheet);
			}
		}	

		String strArrayOfReqCols2[] = {"Marker Name", "Linkage Group", "Position"};
		int intNoOfRows = mapSheet.getRows();
		for(int j = 0; j < strArrayOfReqCols2.length; j++){
			String strColNameFromSheet = (String)mapSheet.getCell(j, 5).getContents().trim();
			if(!strArrayOfReqCols2[j].toLowerCase().contains(strColNameFromSheet.toLowerCase())){
				throw new GDMSException(strColNameFromSheet + " column name not found.");
			}
			if(strColNameFromSheet==null || strColNameFromSheet==""){
				throw new GDMSException(strColNameFromSheet + " information required.");
			}
		}


		for(int j = 7; j < intNoOfRows; j++){
			String strMFieldValue = mapSheet.getCell(0, j).getContents().trim();
			String strLGFieldValue = mapSheet.getCell(1, j).getContents().trim();
			String strPosition = mapSheet.getCell(2, j).getContents().trim();
			if(strMFieldValue.equals("") && !strLGFieldValue.equals("")){
				throw new GDMSException("Marker Name at row " +  j + " is required field");
			}
			if(!strMFieldValue.equals("") && strLGFieldValue.equals("")){
				throw new GDMSException("Linkage Group at row " + j + " is required field");
			}
			if(!strMFieldValue.equals("") && strPosition.equals("")){
				throw new GDMSException("Position at row " + j + " is required field");
			}
			if(strMFieldValue.equals("") && strLGFieldValue.equals("") && strPosition.equals("")){
				String strRowNumber = String.valueOf(mapSheet.getCell(1, j).getRow()+1);								 
				String strErrMsg = "There is an empty row at position " + strRowNumber + ".\nPlease delete it.";
				throw new GDMSException(strErrMsg);
			}							 
		}
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI() throws GDMSException {
		
		listOfDataInSourceRowsFromSheet = new ArrayList<HashMap<String,String>>();

		HashMap<String, String> hmOfDataInSourceRows = new HashMap<String, String>();

		String strMapName = sheetMapDetails.getCell(1, 0).getContents().toString();
		hmOfDataInSourceRows.put(UploadField.MapName.toString(), strMapName);

		String strMapDescription = sheetMapDetails.getCell(1, 1).getContents().toString();
		hmOfDataInSourceRows.put(UploadField.MapDescription.toString(), strMapDescription);

		String strCrop = sheetMapDetails.getCell(1, 2).getContents().toString();
		hmOfDataInSourceRows.put(UploadField.Crop.toString(), strCrop);

		String strMapUnit = sheetMapDetails.getCell(1, 3).getContents().toString();
		hmOfDataInSourceRows.put(UploadField.MapUnit.toString(), strMapUnit);
		
		listOfDataInSourceRowsFromSheet.add(hmOfDataInSourceRows);

		
		int iNumOfRows = sheetMapDetails.getRows();
		
		listOfDataInDataRowsFromSheet = new ArrayList<HashMap<String,String>>();

		for(int i = 6; i < iNumOfRows; i++){
			
			HashMap<String, String> hashMapOfMapDataRow = new HashMap<String, String>();

			String strMarkerName = (String)sheetMapDetails.getCell(0, i).getContents().trim();
			hashMapOfMapDataRow.put(UploadField.MarkerName.toString(), strMarkerName);
			
			String strLinkageGroup = (String)sheetMapDetails.getCell(1, i).getContents().trim();
			hashMapOfMapDataRow.put(UploadField.LinkageGroup.toString(), strLinkageGroup);
			
			String strPosition = (String)sheetMapDetails.getCell(2, i).getContents().trim();
			hashMapOfMapDataRow.put(UploadField.Position.toString(), strPosition);
			
			listOfDataInDataRowsFromSheet.add(hashMapOfMapDataRow);
		}
	}

	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(
			ArrayList<HashMap<String, String>> theListOfSourceDataRows,
			ArrayList<HashMap<String, String>> listOfDataRows,
			ArrayList<HashMap<String, String>> listOfGIDRows) {
		listOfDataRowsFromSourceTable = theListOfSourceDataRows;
		listOfDataRowsFromDataTable = listOfDataRows;
		listOfGIDRowsFromGIDTableForDArT = listOfGIDRows;		
	}

	@Override
	public void upload() throws GDMSException {
		validateData();
		createObjectsToBeSavedToDB();
	}

	@Override
	public void validateData() throws GDMSException {

		String strArrayOfReqColNames[] = {UploadField.MapName.toString(), UploadField.Crop.toString(), 
				UploadField.MapUnit.toString()};

		HashMap<String, String> hashMapOfSourceFieldsAndValues = listOfDataRowsFromSourceTable.get(0);
		for(int j = 0; j < strArrayOfReqColNames.length; j++) {
			String strReqSourceCol = strArrayOfReqColNames[j];
			if(false == hashMapOfSourceFieldsAndValues.containsKey(strReqSourceCol)){
				throw new GDMSException(strReqSourceCol + " column not found");
			} else {
				String strReqColValue = hashMapOfSourceFieldsAndValues.get(strReqSourceCol);
				if (null == strReqColValue || strReqColValue.equals("")){
					throw new GDMSException("Please provide value for " + strReqSourceCol);
				}
			}
		}	
		

		for(int j = 0; j < listOfDataRowsFromDataTable.size(); j++){
			
			HashMap<String, String> hashMapDataRows = listOfDataRowsFromDataTable.get(j);
			
			String strMFieldValue = hashMapDataRows.get(UploadField.MarkerName.toString());
			String strLGFieldValue = hashMapDataRows.get(UploadField.LinkageGroup.toString());
			String strPosition = hashMapDataRows.get(UploadField.Position.toString());
			
			if(strMFieldValue.equals("") && !strLGFieldValue.equals("")) {
				throw new GDMSException("Marker Name at row " +  (j+1) + " is required field");
			}
			if(!strMFieldValue.equals("") && strLGFieldValue.equals("")) {
				throw new GDMSException("Linkage Group at row " + (j+1) + " is required field");
			}
			if(!strMFieldValue.equals("") && strPosition.equals("")) {
				throw new GDMSException("Position at row " + (j+1) + " is required field");
			}
		}
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		genoManager=factory.getGenotypicDataManager();
		int marker_id=0;
		Database instance = Database.LOCAL;
		try{
			long lastId = genoManager.getLastId(instance, GdmsTable.GDMS_MARKER);
			//System.out.println("testGetLastId(" + GdmsTable.GDMS_MARKER + ") in " + instance + " = " + lastId);
			marker_id=(int)lastId;
		}catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Map");
		} 
		/** 
		 *  20131108: Following code added by Tulasi: 
		 *  Setting DatasetID of the Dataset selected on the UI
		 *  to mpId of Map object  
		 * 
		 */
		String datasetSelectedOnTheUI = GDMSModel.getGDMSModel().getDatasetSelected();
		DatasetDAO datasetDAO = new DatasetDAO();
		datasetDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		
		Integer datasetId = null;
		try {
			List<DatasetElement> listOfDetailsByName = datasetDAO.getDetailsByName(datasetSelectedOnTheUI);
			
			if (null != listOfDetailsByName) {
				DatasetElement datasetElement = listOfDetailsByName.get(0);
				datasetId = datasetElement.getDatasetId();
			}
			
		} catch (MiddlewareQueryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//20131108: Setting DatasetID of the Dataset selected on the UI to mpId of Map object  
		
		iNumOfRowsFromDataTable = listOfDataRowsFromDataTable.size();

		ArrayList<String> listOfMarkersFromDataTable = new ArrayList<String>();
		for(int mCount = 0; mCount < iNumOfRowsFromDataTable; mCount++){
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(mCount);
			String strMarkerName = hashMapOfDataRow.get(UploadField.MarkerName.toString());
			listOfMarkersFromDataTable.add(strMarkerName);
		}

		 HashMap<String, Object> markersMap = new HashMap<String, Object>();
         //ArrayList lstMarIdNames=new ArrayList();
         List lstMarkers = new ArrayList();
		//System.out.println("listOfMarkersFromDataTable=:"+listOfMarkersFromDataTable);
		for(int m=0;m<listOfMarkersFromDataTable.size();m++){
			try{
				List<MarkerInfo> results = genoManager.getMarkerInfoByMarkerName(listOfMarkersFromDataTable.get(m), 0, 1);				
		        for (MarkerInfo e : results) {		           
		            lstMarkers.add(e.getMarkerName());
	            	markersMap.put(e.getMarkerName(), e.getMarkerId());
		        }				
			}catch (MiddlewareQueryException e) {
				throw new GDMSException("Error uploading Map");
			} 
		}
		
		

		arrayOfMarkersOnMap = new MarkerOnMap[iNumOfRowsFromDataTable];
		int iUploadedMarkerOnMapCount = 0;
		
		arrayOfMaps = new Map[iNumOfRowsFromDataTable];
		int iUploadedMapCount = 0;
		
		HashMap<String, String> hashMapOfDataFromSourceTable = listOfDataRowsFromSourceTable.get(0);
		
		/**
		 * 20131126: Fix for Issue No: 63: Map is getting uploaded many time.
		 * 
		 * Adding the Map first and then get the Map-ID and use it
		 * upload the Markers in the Map
		 * 
		 */
		strMapName = hashMapOfDataFromSourceTable.get(UploadField.MapName.toString());
		String strMapUnit = hashMapOfDataFromSourceTable.get(UploadField.MapUnit.toString());
		String strMapType = "genetic";
		/**
		 * below lines of code by kalyani 
		 */
		
		String mUnit="";
		if (strMapUnit.equalsIgnoreCase("cm")){
			strMapType="genetic";
			mUnit="cM";
		}else if (strMapUnit.equalsIgnoreCase("bp")){
			strMapType="sequence\\physical";
			mUnit="bp";
		}else{
			//String ErrMsg = "Error : Invalid Map Unit at cell position B4";
			throw new GDMSException("Error : Invalid Map Unit at cell position B4");
		}
		
		/* commented by kalyani
		if (strMapUnit.equalsIgnoreCase("cm")){
			strMapType = "sequence\\physical";
		}*/
		String strMapDescription = UploadField.MapDescription.toString();
		
		/*GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		genotypicDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
		genotypicDataManagerImpl.setSessionProviderForCentral(null);*/
		
		map = new Map();
		map.setMapName(strMapName);
		map.setMapType(strMapType);
		map.setMapUnit(strMapUnit);
		map.setMapDesc(strMapDescription);
		//map.setMpId(0);   //Commented on 20131108: By Tulasi
		map.setMpId(datasetId); //Added on 20131108: By Tulasi
		Integer iMapID = null;
		try {
			iMapID = genoManager.addMap(map);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Map");
		} 
		Integer iMapId = iMapID;
		Integer iMpId = 0;

		/**
		 * 20130826: End of Fix for Issue No: 63: Map is getting uploaded many times
		 * 
		 */
		
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		for(int i = 0; i < iNumOfRowsFromDataTable; i++){

			HashMap<String, String> hashMapOfDataFromDataTable = listOfDataRowsFromDataTable.get(i);
			
			// Marker Fields
			Integer iMarkerId = null; // Value will be set/overriden by the function 
			String strMarkerType = "UA";
			String strSpecies = hashMapOfDataFromSourceTable.get(UploadField.Crop.toString());
			String strMarkerName = hashMapOfDataFromDataTable.get(UploadField.MarkerName.toString());
			String strLinkageGroup = hashMapOfDataFromDataTable.get(UploadField.LinkageGroup.toString());
			
			// MarkerOnMap Fields
			String strPosition = hashMapOfDataFromDataTable.get(UploadField.Position.toString());
			Float fStartPosition = Float.parseFloat(strPosition);
			Float fEndPosition = Float.parseFloat(strPosition);
			//System.out.println("******************************");
			//System.out.println(strMarkerName+"    "+markersMap.get(strMarkerName)+"    "+lstMarkers.contains(strMarkerName));
			
			if(lstMarkers.contains(strMarkerName)){
				iMarkerId=(Integer)(markersMap.get(strMarkerName));
				/*mids.put(strMarkerName, intRMarkerId);
				midsList.add(intRMarkerId);*/
				
			}else{
				marker_id=marker_id-1;
				iMarkerId=marker_id;
				marker = new Marker();
				marker.setMarkerId(iMarkerId);
				marker.setMarkerType(strMarkerType);
				marker.setMarkerName(strMarkerName);
				marker.setSpecies(strSpecies);
				//marker.setAnnealingTemp(new Float(0));
				
				Integer iMarkerID = null;
				try {
					genoManager.addGDMSMarker(marker);
				} catch (MiddlewareQueryException e) {
					throw new GDMSException("Error uploading Map");
				} 
				Integer IMarkerId = iMarkerID;
				Integer iMarId = 0;
				
			}
			if(!chList.contains(strLinkageGroup))
				chList.add(strLinkageGroup);
			//System.out.println(iMapId+", "+iMarkerId+", "+fStartPosition+", "+fEndPosition+", "+strLinkageGroup);
			//markerOnMap = new MarkerOnMap(iMapId, iMarkerId, fStartPosition, fEndPosition, strMapUnit, strLinkageGroup);
			markerOnMap = new MarkerOnMap(iMapId, iMarkerId, fStartPosition, fEndPosition, strLinkageGroup);
			try {
				genoManager.addMarkerOnMap(markerOnMap);
			} catch (MiddlewareQueryException e) {
				throw new GDMSException("Error uploading Map");
			} 
			//map = new Map(iMapId, strMapName, strMapType, iMpId);
			//map = new Map(iMapId, strMapName, strMapType, iMpId, strMapDescription, strMapUnit);

			//saveMap();
			
			int iMarkersOnMapCount = iUploadedMarkerOnMapCount;
			arrayOfMarkersOnMap[iMarkersOnMapCount] = markerOnMap;
			iUploadedMarkerOnMapCount += 1;
			
			int iMapCount = iUploadedMapCount;
			arrayOfMaps[iMapCount] = map;
			iUploadedMapCount += 1;
			//System.out.println("####################################################################################");
		}
		//System.out.println("listOfMarkersFromDataTable-:"+listOfMarkersFromDataTable);
		//GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		//genotypicDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
		/*List<Marker> listOfMarkersFromDB = null;
		List listOfMarkerIdsByMarkerNames=null;
		try {
			List<Integer> listOfMarkerIdsByMarkerNamesL = genoManager.getMarkerIdsByMarkerNames(listOfMarkersFromDataTable, 0, (int)listOfMarkersFromDataTable.size(), Database.LOCAL);
			List<Integer> listOfMarkerIdsByMarkerNamesC = genoManager.getMarkerIdsByMarkerNames(listOfMarkersFromDataTable, 0, (int)listOfMarkersFromDataTable.size(), Database.CENTRAL);
			
			for (int i = 0; i < listOfMarkerIdsByMarkerNamesL.size(); i++){				
				listOfMarkerIdsByMarkerNames.add(listOfMarkersFromDB.get(i).getMarkerName().toCharArray());
			}
			
			for (int i = 0; i < listOfMarkerIdsByMarkerNamesC.size(); i++){
				listOfMarkerIdsByMarkerNames.add(listOfMarkersFromDB.get(i).getMarkerName().toCharArray());
			}
			
			System.out.println("listOfMarkerIdsByMarkerNames=:"+listOfMarkerIdsByMarkerNames);
			listOfMarkersFromDB = genoManager.getMarkersByMarkerIds(listOfMarkerIdsByMarkerNames, 0, listOfMarkerIdsByMarkerNames.size());
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}

		arrayOfMarkers = new Marker[listOfMarkersFromDB.size()];
		for (int i = 0; i < listOfMarkersFromDB.size(); i++){
			Marker addedMarker = new Marker();
			addedMarker.setMarkerId(listOfMarkersFromDB.get(i).getMarkerId());
			addedMarker.setMarkerName(listOfMarkersFromDB.get(i).getMarkerName());
			arrayOfMarkers[i] = addedMarker;
		}*/
		
	}

	protected void saveMap() throws GDMSException {
		/*GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		genotypicDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
		genotypicDataManagerImpl.setSessionProviderForCentral(null);*/
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		genoManager=factory.getGenotypicDataManager();
		try {
			genoManager.setMaps(marker, markerOnMap, map);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Map");
		} catch (Throwable th){
			throw new GDMSException("Error uploading Map", th);
		}
	}
	
	@Override
	public void setListOfColumns(	ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		/*if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";

			for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName;
				strUploadInfo += strMarker + "\n";
			}
			strDataUploaded = "Uploaded MAP(s): \n" + strUploadInfo;
		}*/	
		
/*		if (null != arrayOfMarkersOnMap && arrayOfMarkersOnMap.length > 0){
			String strUploadInfo = "";

			for (int i = 0; i < arrayOfMarkersOnMap.length; i++){
				
				Integer iMapId = arrayOfMarkersOnMap[i].getMapId();
				String strMapName = "";
				for (int j = 0; j < arrayOfMaps.length; j++){
					Integer iMapID = new Integer(arrayOfMaps[j].getMapId());
					if (iMapID.equals(iMapId)){
						strMapName = arrayOfMaps[j].getMapName();
					}
				}
				
				
				Integer iMarkerId = arrayOfMarkersOnMap[i].getMarkerId();
				String strMarkerName = "";
				for (int j = 0; j < arrayOfMarkers.length; j++){
					Integer iMarkerID = new Integer(arrayOfMarkers[j].getMarkerId());
					if (iMarkerID.equals(iMarkerId)){
						strMarkerName = arrayOfMarkers[j].getMarkerName();
					}
				}
				
				String strMapInfo = "Map-ID: " +  iMapId +  " Map-Name: " + strMapName + " Marker: " + iMarkerId + ": " + strMarkerName;
				strUploadInfo += strMapInfo + "\n";
			}
			strDataUploaded = "Uploaded MAP(s): \n" + strUploadInfo;
		}*/
		
		strDataUploaded="Uploaded MAP '"+ strMapName+"' with "+iNumOfRowsFromDataTable+" marker(s) on "+chList.size()+" chromosome(s)";
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceRowsFromSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		return listOfDataInDataRowsFromSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
	}

}
