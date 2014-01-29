package org.icrisat.gdms.upload.genotyping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.UserDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
import org.generationcp.middleware.pojos.gdms.AlleleValues;
import org.generationcp.middleware.pojos.gdms.DartDataRow;
import org.generationcp.middleware.pojos.gdms.DartValues;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.DatasetUsers;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.SNPDataRow;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;



public class DARTGenotype implements UploadMarker {

	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private HashMap<Integer, String> hmOfColIndexAndGermplasmName;
	private ArrayList<HashMap<String, String>> listOfDataInGIDsSheet;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private AccMetadataSet accMetadataSet;
	private MarkerMetadataSet markerMetadataSet;
	private DatasetUsers datasetUser;
	private AlleleValues alleleValues;
	private Dataset dataset;
	private Marker addedMarker;
	private DartValues dartValues;
	private Marker[] arrayOfMarkers;
	List<DartDataRow> listOfDArTDataRows; 
	ManagerFactory factory =null;
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    String notMatchingGIDS="";
    String notMatchingDataDB="";
    String notMatchingGIDSDB="";
    String notMatchingDataExists="";
    
    String strErrMsg="";
    
    GermplasmDataManager germManager;		
	GenotypicDataManager genoManager;
	
    
	static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();  
	
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			workbook = Workbook.getWorkbook(new File(strFileLocation));
			strSheetNames = workbook.getSheetNames();
		} catch (BiffException e) {
			throw new GDMSException("Error Reading DART Genotype Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading DART Genotype Sheet - " + e.getMessage());
		}
	}

	@Override
	public void validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("dart_source")){
			throw new GDMSException("DArT_Source Sheet Name Not Found");
		}

		if (false == strSheetNames[1].equalsIgnoreCase("dart_data")){
			throw new GDMSException("DArT_DataList Sheet Name Not Found");
		}

		if (false == strSheetNames[2].equalsIgnoreCase("dart_gids")){
			throw new GDMSException("DArT_DataList Sheet Name Not Found");
		}

		//check the template fields
		for(int i = 0; i < strSheetNames.length; i++){
			String strSName = strSheetNames[i].toString();

			if(strSName.equalsIgnoreCase("DArT_Source")) {
				Sheet sName = workbook.getSheet(strSName);

				String strTempColumnNames[] = {"Institute", "Principle investigator", "Dataset Name", "Dataset description", "Genus",
						"Species", "Remark"};

				for(int j = 0; j < strTempColumnNames.length; j++){
					String strColFromSheet = (String)sName.getCell(0, j).getContents().trim();
					if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
					}
				}															
			}

			if(strSName.equalsIgnoreCase("DArT_Data")){
				Sheet dataListSheet = workbook.getSheet(strSName);
				int intNoOfRows = dataListSheet.getRows();
				int intNoOfCols = dataListSheet.getColumns();
				String strTempColumnNames[] = {"CloneID", "MarkerName", "Q", "Reproducibility",
						"Call Rate", "PIC", "Discordance"};					 

				for(int j = 0; j < strTempColumnNames.length; j++){
					String strColFromSheet = (String)dataListSheet.getCell(j, 0).getContents().trim();
					if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException("Delete Empty Column " + strColFromSheet);
					}
				}

				for(int c = 0; c < 6; c++){
					for(int r = 1; r < intNoOfRows; r++){
						String value = (String)dataListSheet.getCell(c, r).getContents().trim();
						String strColumnName = (String)dataListSheet.getCell(c, 0).getContents().trim();
						if(value == null || value == ""){
							String strRowNumber = String.valueOf(dataListSheet.getCell(c, r).getRow()+1);	
							String strErrMsg = "This cell is empty at position " + strColumnName + strRowNumber+".";
							throw new GDMSException(strErrMsg);
						}
					}
				}

				for(int c = 7; c < intNoOfCols; c++){
					for(int r = 0; r < intNoOfRows; r++){
						String value = (String)dataListSheet.getCell(c, r).getContents().trim();
						if(value == null || value == ""){
							String strRowNumber = String.valueOf(dataListSheet.getCell(c, r).getRow()+1);	
							String strColumnName = (String)dataListSheet.getCell(c, 0).getContents().trim();
							String strErrMsg = "This cell is empty at position " + strColumnName + strRowNumber+".";
							throw new GDMSException(strErrMsg);
						}
					}
				}	

				hmOfColIndexAndGermplasmName = new HashMap<Integer, String>();
				for(int colIndex = 7; colIndex < intNoOfCols; colIndex++){
					//if(dataListSheet.getCell(colIndex, 0).getContents()!=null){
						String strMarkerName = dataListSheet.getCell(colIndex, 0).getContents().toString();
						if(strMarkerName != null){
							hmOfColIndexAndGermplasmName.put(colIndex, strMarkerName);
							for(int r = 0; r < intNoOfRows; r++){
								String strCellValue = (String)dataListSheet.getCell(colIndex, r).getContents().trim();
								if(strCellValue == null || strCellValue == ""){
									String strRowNumber = String.valueOf(dataListSheet.getCell(colIndex, r).getRow()+1);	
									String strErrMsg = "Please provide a value at cell position " + "[" + colIndex + ", " + strRowNumber + "] in DArT_GIDs sheet.";
									throw new GDMSException(strErrMsg);
								}
							}
						}
					//}
				}
			}
		}
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI() throws GDMSException {

		Sheet sourceSheet = workbook.getSheet(0);
		listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();

		HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();

		String strInstitute = sourceSheet.getCell(1, 0).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Institute.toString(), strInstitute);

		String strPrincipalInvestigator = sourceSheet.getCell(1, 1).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PrincipleInvestigator.toString(), strPrincipalInvestigator);

		String strDatasetName = sourceSheet.getCell(1, 2).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetName.toString(), strDatasetName);

		String strDatasetDescription = sourceSheet.getCell(1, 3).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetDescription.toString(), strDatasetDescription);

		String strGenus = sourceSheet.getCell(1, 4).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Genus.toString(), strGenus);

		String strSpecies = sourceSheet.getCell(1, 5).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Species.toString(), strSpecies);

		String strRemark = sourceSheet.getCell(1, 6).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Remark.toString(), strRemark);

		listOfDataInSourceSheet.add(hmOfDataInSourceSheet);

		//CloneID, MarkerName, Q, Reproducibility, Call Rate, PIC, Discordance followed by Marker-Names	
		Sheet dataSheet = workbook.getSheet(1);
		int iNumOfRowsInDataSheet = dataSheet.getRows();
		int iNumOfColumnsInDataSheet = dataSheet.getColumns();
		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();

		for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strCloneId = dataSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.CloneID.toString(), strCloneId);

			String strMarkerName = dataSheet.getCell(1, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.MarkerName.toString(), strMarkerName);

			String strQ = dataSheet.getCell(2, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Q.toString(), strQ);

			String strReproducibility = dataSheet.getCell(3, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Reproducibility.toString(), strReproducibility);

			String strCallRate = dataSheet.getCell(4, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.CallRate.toString(), strCallRate);

			String strPIC = dataSheet.getCell(5, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.PIC.toString(), strPIC);

			String strDiscordance = dataSheet.getCell(6, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Discordance.toString(), strDiscordance);

			//Inserting the Marker-Names and Marker-Values
			for (int cIndex = 7; cIndex < iNumOfColumnsInDataSheet; cIndex++){
				String strMName = hmOfColIndexAndGermplasmName.get(cIndex);
				String strMValue = dataSheet.getCell(cIndex, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(strMName, strMValue);
			}

			listOfDataInDataSheet.add(hmOfDataInDataSheet);
		}


		//GIDs
		//Germplasm_Name
		Sheet gidsSheet = workbook.getSheet(2);
		int iNumOfRowsInGIDsSheet = gidsSheet.getRows();
		listOfDataInGIDsSheet = new ArrayList<HashMap<String,String>>();
		for (int rIndex = 1; rIndex < iNumOfRowsInGIDsSheet; rIndex++) {
			HashMap<String, String> hmOfDataInGIDsSheet = new HashMap<String, String>();

			String strGIDs = gidsSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInGIDsSheet.put(UploadField.GIDs.toString(), strGIDs);

			String strGermplasmName = gidsSheet.getCell(1, rIndex).getContents().toString();
			hmOfDataInGIDsSheet.put(UploadField.GermplasmName.toString(), strGermplasmName);

			listOfDataInGIDsSheet.add(hmOfDataInGIDsSheet);
		}

	}

	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(ArrayList<HashMap<String, String>> theListOfSourceDataRows, ArrayList<HashMap<String, String>> listOfDataRows, ArrayList<HashMap<String, String>> listOfGIDRows) {
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

		String strReqColumnNamesInSource[] = {UploadField.Institute.toString(), 
				UploadField.DatasetName.toString(), UploadField.DatasetDescription.toString(), UploadField.Genus.toString()
		};

		HashMap<String, String> hmOfSourceColumnsAndValuesFromGUI = listOfDataRowsFromSourceTable.get(0);
		String strDatasetName = "";
		for(int j = 0; j < strReqColumnNamesInSource.length; j++){
			String strCol = strReqColumnNamesInSource[j];
			if (false == hmOfSourceColumnsAndValuesFromGUI.containsKey(strCol)){
				throw new GDMSException(strCol + " column not found in data DArT_Source table.");
			} else {
				//Institute, Principle-Investigator, Dataset-Name, Dataset-Description, Genus, Species, Remark
				if (strCol.equalsIgnoreCase(UploadField.Institute.toString()) || 
						strCol.equalsIgnoreCase(UploadField.DatasetName.toString()) ||
						strCol.equalsIgnoreCase(UploadField.DatasetDescription.toString()) || 
						strCol.equalsIgnoreCase(UploadField.Genus.toString())){
					String strValue = hmOfSourceColumnsAndValuesFromGUI.get(strCol);
					if (null == strValue || strValue.equals("")){
						throw new GDMSException("Please provide a value for " +  strCol + " column not found in data DArT_Source sheet.");
					}
					
					if (strCol.equalsIgnoreCase(UploadField.DatasetName.toString())){
						strDatasetName = strValue;
					}
				}
			}
		}															

		
		/**
		 * 20130826: Fix for Issue No: 60 - DArT Genotype Upload
		 * 
		 * Check for duplicate Dataset Name before uploading the DArT Genotype
		 * 
		 */
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		factory=GDMSModel.getGDMSModel().getManagerFactory();
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%  :"+GDMSModel.getGDMSModel().getLocalParams().);
		germManager = factory.getGermplasmDataManager();		
		genoManager=factory.getGenotypicDataManager();
		
	
			if(strDatasetName.trim().length() > 30){
				throw new GDMSException("Dataset Name value exceeds max char size.");
			}

			
			
	
		//20130826: End of fix for Issue No: 60 - DArT Genotype Upload

		String strReqColumnNamesInDataSheet[] = {UploadField.CloneID.toString(), UploadField.MarkerName.toString(),  UploadField.Q.toString(), 
				UploadField.Reproducibility.toString(), UploadField.CallRate.toString(), UploadField.PIC.toString(), UploadField.Discordance.toString()};					 

		for(int colIndex = 0; colIndex < strReqColumnNamesInDataSheet.length; colIndex++) {
			String strColName = strReqColumnNamesInDataSheet[colIndex];
			for (int rowIndex = 0; rowIndex < listOfDataRowsFromDataTable.size(); rowIndex++) {
				HashMap<String, String> hmOfDataColumnsAndValuesFromGUI = listOfDataRowsFromDataTable.get(rowIndex);
				if(false == hmOfDataColumnsAndValuesFromGUI.containsKey(strColName)){
					throw new GDMSException(strReqColumnNamesInSource[colIndex] + " Column Name Not Found");
				} else {
					String strValue = hmOfDataColumnsAndValuesFromGUI.get(strColName);
					if (null == strValue || strValue.equals("")){
						throw new GDMSException("Please provide a value for " +  strColName + " in data DArT_Data sheet at Row#: " + (rowIndex+1));
					}
				}
			}	
		}
		
		String strReqColumnNamesInGIDsSheet[] = {UploadField.GIDs.toString(), UploadField.GermplasmName.toString()};
		HashMap<String, String> hmOfGIDsAndGNamesFromGIDSheet = listOfGIDRowsFromGIDTableForDArT.get(0);
		for(int j = 0; j < strReqColumnNamesInGIDsSheet.length; j++){
			String strCol = strReqColumnNamesInGIDsSheet[j];
			if (false == hmOfGIDsAndGNamesFromGIDSheet.containsKey(strCol)){
				throw new GDMSException(strCol + " column not found in data DArT_GIDs table.");
			} 
		}	
	}


	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {

		/** reading from Data sheet of template **/
		int iRowCountInDataTable = listOfDataRowsFromDataTable.size();
		int iGermplasmNamesCount = 0;

		//Checking for the number of Germplasm names provided in the "DArT_Data" sheet
		HashMap<String, String> hashMapOfFirstDataRow = listOfDataRowsFromDataTable.get(0);
		int  iColCountInDataTable = hashMapOfFirstDataRow.size();
		for(int col = 8; col < iColCountInDataTable; col++){
			iGermplasmNamesCount = iGermplasmNamesCount + 1;				
		}

		//Checking for the number of GIDs provided in the "DArT_GIDs" sheet
		int iRowsInGIDsTable = listOfGIDRowsFromGIDTableForDArT.size();
		int iGIDsCount = 0;
		for(int r = 0; r < iRowsInGIDsTable; r++){					
			iGIDsCount = iGIDsCount + 1;
		}

		if(iGIDsCount != iGermplasmNamesCount){
			String strErrMsg = "Germplasms in DArT_GIDs sheet doesnot match with the Germplasm in DArT_Data sheet.";
			throw new GDMSException(strErrMsg);
		}


		//Building the list of GIDs from the DArT_GIDs
		//And building a HashMap of GIDs and GNames from the GID sheet
		Map<Integer, String> hashMapOfGIDAndGNameFromGIDTable = new HashMap<Integer, String>();
		Map<String, Integer> hashMapOfGNameAndGIDFromGIDTable = new HashMap<String, Integer>();
		ArrayList<Integer> listofGIDsFromGIDsTable = new ArrayList<Integer>();
		ArrayList<String> listofGNamessFromGNamesTable = new ArrayList<String>();
		if(iGIDsCount == iGermplasmNamesCount){
			for (int r = 0; r < iRowsInGIDsTable; r++){
				HashMap<String, String> hashMapOfGIDDataRow = listOfGIDRowsFromGIDTableForDArT.get(r);
				String strGID = hashMapOfGIDDataRow.get(UploadField.GIDs.toString()).toString();
				int iGID = Integer.parseInt(strGID);
				if (false == listofGIDsFromGIDsTable.contains(iGID)){
					listofGIDsFromGIDsTable.add(iGID);
				}
				String strGermplasmName = hashMapOfGIDDataRow.get(UploadField.GermplasmName.toString());
				if (false == listofGNamessFromGNamesTable.contains(strGermplasmName)){
					listofGNamessFromGNamesTable.add(strGermplasmName);
					hashMapOfGIDAndGNameFromGIDTable.put(iGID, strGermplasmName);
					
					hashMapOfGNameAndGIDFromGIDTable.put(strGermplasmName, iGID);
				}
				
			}
		}


		//Retrieving data from Names table using GIDs list
		/*GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		HibernateSessionProvider hibernateSessionProviderForLocal = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal();
		genotypicDataManagerImpl.setSessionProviderForLocal(hibernateSessionProviderForLocal);
		genotypicDataManagerImpl.setSessionProviderForCentral(null);
		*/
		/*
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%  :"+GDMSModel.getGDMSModel().getLocalParams().);
		GermplasmDataManager germManager = factory.getGermplasmDataManager();		
		GenotypicDataManager genoManager=factory.getGenotypicDataManager();
		
		*/

		List<Integer> listOfGermplasmIDsFromDB = new ArrayList<Integer>();
		List<Integer> listOfNIDsByGermplasmIds = null;
		ArrayList<String> listOfGermplasmNamesFromDB = null;
		HashMap<Integer, String> hashMapOfGIDandGNameFromDB = null;
		HashMap<Integer, Integer> hashMapOfGIDsandNIDsFromDB = null;
		HashMap<String, Integer> hashMapOfGNamesandGIDsFromDB = null;
		//System.out.println("listofGIDsFromGIDsTable=:"+listofGIDsFromGIDsTable);
		ArrayList gidsDBList = new ArrayList();
		ArrayList gNamesDBList = new ArrayList();
		try {

			hashMap.clear();
			for(int n=0;n<listofGNamessFromGNamesTable.size();n++){
				List<Germplasm> germplasmList = germManager.getGermplasmByName(listofGNamessFromGNamesTable.get(n).toString(), 0, new Long(germManager.countGermplasmByName(listofGNamessFromGNamesTable.get(n).toString(), Operation.EQUAL)).intValue(), Operation.EQUAL);
				for (Germplasm g : germplasmList) {
		        	if(!(gidsDBList.contains(g.getGid()))){
		        		gidsDBList.add(g.getGid());
		        		gNamesDBList.add(listofGNamessFromGNamesTable.get(n).toString());
		        		addValues(listofGNamessFromGNamesTable.get(n).toString(), g.getGid());					        		
		        	}				        	
		           //System.out.println("  " + g.getGid());
		        }
		        //System.out.println(n+":"+listofGNamessFromGNamesTable.get(n).toString()+"   "+hashMap.get(listofGNamessFromGNamesTable.get(n).toString()));
			}

		} catch (NumberFormatException e) {
			throw new GDMSException(e.getMessage());
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}

		if (0 == gidsDBList.size()) {
			strErrMsg = "The following GID(s) provided do not exist in the database. \n Please upload the relevant germplasm information to the GMS \n \t" + " \n Please verify the name(s) provided with the following GID(s) which do not match the name(s) present in the database: \n\t ";
			throw new GDMSException(strErrMsg);
		}
		int size=0;
		if(gidsDBList.size() > 0){
			for(int n=0;n<listofGNamessFromGNamesTable.size();n++){
     		   if(gNamesDBList.contains(listofGNamessFromGNamesTable.get(n))){
     			   if(!(hashMap.get(listofGNamessFromGNamesTable.get(n).toString()).contains(hashMapOfGNameAndGIDFromGIDTable.get(listofGNamessFromGNamesTable.get(n).toString())))){
     				   notMatchingData=notMatchingData+listofGNamessFromGNamesTable.get(n)+"   "+hashMapOfGNameAndGIDFromGIDTable.get(listofGNamessFromGNamesTable.get(n).toString())+"\n\t";
     				   
     				   notMatchingDataDB=notMatchingDataDB+listofGNamessFromGNamesTable.get(n)+"="+hashMap.get(listofGNamessFromGNamesTable.get(n))+"\t";
		        		   alertGN="yes";
     			   }
     		   }else{
     			   //int gid=GIDsMap.get(NamesList.get(n).toString());
     			   alertGID="yes";
     			   size=hashMap.size();
     			   notMatchingGIDS=notMatchingGIDS+listofGNamessFromGNamesTable.get(n).toString()+", ";
     		   }
     	   }	
		}

		if((alertGN.equals("yes"))&&(alertGID.equals("no"))){
     	   //String ErrMsg = "GID(s) ["+notMatchingGIDS.substring(0,notMatchingGIDS.length()-1)+"] of Germplasm(s) ["+notMatchingData.substring(0,notMatchingData.length()-1)+"] being assigned to ["+notMatchingDataExists.substring(0,notMatchingDataExists.length()-1)+"] \n Please verify the template ";
			strErrMsg = "Please verify the name(s) provided \t "+notMatchingData+" which do not match the GID(s) present in the database"+notMatchingDataDB;
			throw new GDMSException(strErrMsg);
        }
        if((alertGID.equals("yes"))&&(alertGN.equals("no"))){	        	   
     	   if(size==0){
     		  strErrMsg = "The Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook ";
     	   }else{
     		  strErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS;
     		   //ErrMsg = "Please verify the GID/Germplasm(s) provided as some of them do not exist in the database. \n Please upload germplasm information into GMS ";
     	   }	        	   
     	   //ErrMsg = "Please verify the following GID/Germplasm(s) doesnot exists. \n Upload germplasm Information into GMS \n\t"+notMatchingGIDS;
     	  throw new GDMSException(strErrMsg);
        }
		
        if((alertGID.equals("yes"))&&(alertGN.equals("yes"))){
        	strErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS+" \n Please verify the name(s) provided "+notMatchingData+" which do not match the GIDS(s) present in the database "+notMatchingDataDB;
     	  throw new GDMSException(strErrMsg); 
        }		
		/** Obtaining the list of Markers from the sheet */	
		List<String> listOfMarkerNamesFromTheDataSheet = new ArrayList<String>();
		for (int iRowCount = 0; iRowCount < iRowCountInDataTable; iRowCount++){
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(iRowCount);
			String strMarkerName = hashMapOfDataRow.get(UploadField.MarkerName.toString());
			listOfMarkerNamesFromTheDataSheet.add(strMarkerName);
		}


		/** Retrieving the Marker-IDs for the Markers given in the DataSheet-DArT_Data */
		/*MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		long countAll = 0;
		List<Marker> listOfMarkersFromDB = null;
		try {
			countAll = markerDAO.countAll();
			List<Integer> listOfMarkerIdsByMarkerNames =genoManager.getMarkerIdsByMarkerNames(listOfMarkerNamesFromTheDataSheet, 0, listOfMarkerNamesFromTheDataSheet.size(), Database.CENTRAL);
			
			
			List<Integer> listOfMarkerIdsByMarkerNames = genotypicDataManagerImpl.getMarkerIdsByMarkerNames(listOfMarkerNamesFromTheDataSheet, 0, (int)countAll, Database.LOCAL);
			if (null != listOfMarkerIdsByMarkerNames){
				listOfMarkersFromDB = genotypicDataManagerImpl.getMarkersByMarkerIds(listOfMarkerIdsByMarkerNames, 0, listOfMarkerIdsByMarkerNames.size());
			}
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}*/


		//Creating the DatasetUsers object first
		Integer iUserId = 0;
		HashMap<String, String> hashMapOfDataRowFromSourceTable = listOfDataRowsFromSourceTable.get(0);
		String strPrincipleInvestigator = hashMapOfDataRowFromSourceTable.get(UploadField.PrincipleInvestigator.toString());

		/*if (null == strPrincipleInvestigator || strPrincipleInvestigator.equals("")){
			WorkbenchDataManagerImpl workbenchDataManagerImpl = new WorkbenchDataManagerImpl(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
			WorkbenchRuntimeData workbenchRuntimeData;
			try {
				workbenchRuntimeData = workbenchDataManagerImpl.getWorkbenchRuntimeData();
				if (null != workbenchRuntimeData){
					iUserId = workbenchRuntimeData.getUserId();
				} else {
					User loggedInUser = GDMSModel.getGDMSModel().getLoggedInUser();
					iUserId = loggedInUser.getUserid();
				}
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
		} else {
*/
			UserDAO userDAO = new UserDAO();
			userDAO.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
			List<User> listOfAllUsers =  null;
			try {
				listOfAllUsers = userDAO.getAll();
				for (User user : listOfAllUsers){
					String strName = user.getName();
					if (strName.equals(strPrincipleInvestigator)){
						iUserId = user.getUserid();
						break;
					}
				}
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
		//}

		ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
		HashMap<Integer, String> hashMapOfGIDandGName = new HashMap<Integer, String>();
		//HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		HashMap<Integer, Integer> hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
		List<Marker> listOfMarkersFromDB = null;
		HashMap<String, Integer> hashMapOfMNamesAndMIDs = new HashMap<String, Integer>();
		
		HashMap<String, Integer> hashMapOfGNameandGIDdb = new HashMap<String, Integer>();
		
		Name names = null;
		ArrayList gidL=new ArrayList();
		List<Integer> nameIdsByGermplasmIds =new ArrayList();
		for(int n=0;n<listofGIDsFromGIDsTable.size();n++){
		try {
			names = germManager.getNameByGIDAndNval(Integer.parseInt(listofGIDsFromGIDsTable.get(n).toString()), listofGNamessFromGNamesTable.get(n).toString(), GetGermplasmByNameModes.STANDARDIZED);
			if(names==null){
				names=germManager.getNameByGIDAndNval(Integer.parseInt(listofGIDsFromGIDsTable.get(n).toString()), listofGNamessFromGNamesTable.get(n).toString(), GetGermplasmByNameModes.NORMAL);
			}	
			//System.out.println(",,,,,,,,,,,,,,,:"+names.getGermplasmId());
			if(!gidL.contains(names.getGermplasmId()))
            	gidL.add(names.getGermplasmId());
			listOfGermplasmNames.add(names.getNval());
			hashMapOfGIDandGName.put(names.getGermplasmId(), names.getNval());
			hashMapOfGIDsandNIDs.put(names.getGermplasmId(), names.getNid());
			hashMapOfGNameandGIDdb.put(names.getNval(), names.getGermplasmId());
			nameIdsByGermplasmIds.add(names.getNid());
			} catch (MiddlewareQueryException e1) {
				throw new GDMSException(e1.getMessage());
			}
		}
		int iUploadedMarkerCount = 0;
		arrayOfMarkers = new Marker[iGermplasmNamesCount];
		Integer iMarkerId = 0;

		//Dataset Fields
		String strDatasetName = hashMapOfDataRowFromSourceTable.get(UploadField.DatasetName.toString());
		String strDatasetDesc = hashMapOfDataRowFromSourceTable.get(UploadField.DatasetDescription.toString());
		String strDatasetType = "DArT";
		String strGenus = hashMapOfDataRowFromSourceTable.get(UploadField.Genus.toString());
		String strSpecies = hashMapOfDataRowFromSourceTable.get(UploadField.Species.toString());
		Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
		String strRemarks = ""; 
		String strDataType = "int"; 
		String strMissingData = null;
		String strMethod = null;
		String strScore = null;
		String strInstitute = hashMapOfDataRowFromSourceTable.get(UploadField.Institute.toString());
		String strEmail = null;
		String strPurposeOfStudy = null;
		// DatasetUser Fields
		Integer iDatasetId = 0; //Will be set/overridden by the function

		//dataset = new Dataset(iDatasetId, strDatasetName, strDatasetDesc, strDatasetType, strGenus, strSpecies, uploadTemplateDate, strRemarks, strDataType, strMissingData, strMethod, strScore, strInstitute, strPrincipleInvestigator, strEmail, strPurposeOfStudy);
		dataset = new Dataset();
		//dataset.setDatasetId(iDatasetId);
		dataset.setDatasetName(strDatasetName);
		dataset.setDatasetDesc(strDatasetDesc);
		dataset.setDatasetType(strDatasetType);
		dataset.setGenus(strGenus);
		dataset.setSpecies(strSpecies);
		dataset.setUploadTemplateDate(uploadTemplateDate);
		dataset.setRemarks(strRemarks);
		dataset.setDataType(strDataType);
		dataset.setMissingData(strMissingData);
		dataset.setMethod(strMethod);
		dataset.setScore(strScore);
		
		
					
		datasetUser = new DatasetUsers(iDatasetId, iUserId);
		//dataset = new Dataset(iDatasetId, strDatasetName, strDatasetDesc, strDatasetType, strGenus, strSpecies, uploadTemplateDate, strRemarks,
			//	strDataType, strMissingData, strMethod, strScore);    
		listOfDArTDataRows = new ArrayList<DartDataRow>();
		
		//System.out.println("hmOfColIndexAndGermplasmName:"+hmOfColIndexAndGermplasmName);
		//System.out.println("hashMapOfDataRowFromDataTable=:"+);
		for (int row = 0; row < iRowCountInDataTable; row++){
			
			HashMap<String, String> hashMapOfDataRowFromDataTable = listOfDataRowsFromDataTable.get(row);
			String strMarkerFromDArTDataTable = hashMapOfDataRowFromDataTable.get(UploadField.MarkerName.toString()).trim();
			/*for (int m = 0; m < listOfMarkersFromDB.size(); m++){
				String strMarkerName = listOfMarkersFromDB.get(m).getMarkerName();
				if (strMarkerName.equals(strMarkerFromDArTDataTable)){
					iMarkerId = listOfMarkersFromDB.get(m).getMarkerId();
					break;
				}
			}*/
			addedMarker= new Marker();
			addedMarker.setMarkerName(strMarkerFromDArTDataTable);
			addedMarker.setSpecies("groundnut");
			
			
			markerMetadataSet = new MarkerMetadataSet(null, 0);
			//System.out.println("strMarkerFromDArTDataTable=:"+strMarkerFromDArTDataTable);
			//HashMap<String, String> hashMapDataRowFromGIDTable = listOfGIDRowsFromGIDTableForDArT.get(row);
			
			int iNumOfColumnsRow = hashMapOfDataRowFromDataTable.size();
			//System.out.println("iNumOfColumnsRow=:"+iNumOfColumnsRow);
			//int iNumOfColumnsInDataRow=iNumOfColumnsRow-1;
			
			//System.out.println("iNumOfColumnsInDataRow=:"+iNumOfColumnsRow);
			
			for (int gNameIndex = 7; gNameIndex < iNumOfColumnsRow; gNameIndex++) {
				if(hmOfColIndexAndGermplasmName.get(gNameIndex) != null){
				String strGermplasmName = hmOfColIndexAndGermplasmName.get(gNameIndex).trim();
				//System.out.println("***********  :"+strGermplasmName);
				Integer iGId = hashMapOfGNameandGIDdb.get(strGermplasmName);
				Integer iNameId = hashMapOfGIDsandNIDs.get(iGId);
				
				//System.out.println("Marker=:"+strMarkerFromDArTDataTable+"     iGId=:"+iGId+"  nid=:"+iNameId+"   gname=:"+strGermplasmName+"   "+gNameIndex);
				

				// AlleleValues Additional Fields
				Integer iAnId = null;     //Will be set/overridden by the function
				String iAlleleBinValue = hashMapOfDataRowFromDataTable.get(strGermplasmName);
				String iAlleleRawValue = "";
				Integer iPeakHeight = null;


				// DartValues Additional Fields
				Integer iAdId = null;  //Will be set/overridden by the function

				String strCloneID = hashMapOfDataRowFromDataTable.get(UploadField.CloneID.toString());
				Integer iCloneId = Integer.parseInt(strCloneID);

				String strQValue = hashMapOfDataRowFromDataTable.get(UploadField.Q.toString());
				Float fQValue = Float.parseFloat(strQValue);

				String strRerpoducibility = hashMapOfDataRowFromDataTable.get(UploadField.Reproducibility.toString());
				Float fReproducibility = Float.parseFloat(strRerpoducibility);

				String strReproducibility = hashMapOfDataRowFromDataTable.get(UploadField.CallRate.toString());
				Float fCallRate = Float.parseFloat(strReproducibility);

				String strPIC = hashMapOfDataRowFromDataTable.get(UploadField.PIC.toString());
				Float fPicValue = Float.parseFloat(strPIC); 

				String strDiscordance = hashMapOfDataRowFromDataTable.get(UploadField.Discordance.toString());
				Float fDiscordance = Float.parseFloat(strDiscordance); 


				accMetadataSet = new AccMetadataSet(new AccMetadataSetPK(iDatasetId, iGId, iNameId)); 

				

				alleleValues = new AlleleValues();
				alleleValues.setAnId(iAnId);
				alleleValues.setgId(iGId);
				alleleValues.setAlleleBinValue(iAlleleBinValue);
				alleleValues.setAlleleRawValue(iAlleleBinValue);
				
				
				//alleleValues = new AlleleValues(iAnId, iDatasetId, iGId, iMarkerId, iAlleleBinValue, iAlleleRawValue);

				dartValues = new DartValues();
				dartValues.setAdId(iAdId);
				dartValues.setCloneId(iCloneId);
				dartValues.setqValue(fQValue);
				dartValues.setReproducibility(fReproducibility);
				dartValues.setCallRate(fCallRate);
				dartValues.setPicValue(fPicValue);
				dartValues.setDiscordance(fDiscordance);
				
				
				
				DartDataRow dartDataRow = new DartDataRow(addedMarker, accMetadataSet, markerMetadataSet, alleleValues, dartValues);
				listOfDArTDataRows.add(dartDataRow);
				

				}	
				
			}
			
		}
		saveDArTGenotype();			
	}

	protected void saveDArTGenotype() throws GDMSException {
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%  :"+GDMSModel.getGDMSModel().getLocalParams().);
			
		//GenotypicDataManager genoManager=factory.getGenotypicDataManager();
		

		try {
			genoManager.setDart(dataset, datasetUser, listOfDArTDataRows);
	
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading DArT Genotype");
		} catch (Throwable th){
			throw new GDMSException("Error uploading DArT Genotype", th);
		}
	}

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "Uploaded DArT Genotyping dataset";
		/*if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";

			for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strGID = arrayOfMarkers[i].getDbAccessionId();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName + " GID: " + strGID;
				strUploadInfo += strMarker + "\n";
			}
			strDataUploaded = "Uploaded SSR Genotype with following Marker(s): \n" + strUploadInfo;
		}*/

		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		return listOfDataInDataSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		return listOfDataInGIDsSheet;
	}
	
	
	private static void addValues(String key, Integer value){
		ArrayList<Integer> tempList = null;
		if(hashMap.containsKey(key)){
			tempList=hashMap.get(key);
			if(tempList == null)
				tempList = new ArrayList<Integer>();
			tempList.add(value);
		}else{
			tempList = new ArrayList();
			tempList.add(value);
		}
		hashMap.put(key,tempList);
	}
	

}
