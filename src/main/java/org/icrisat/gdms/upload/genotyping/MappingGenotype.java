package org.icrisat.gdms.upload.genotyping;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.UserDAO;
import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.MarkerMetadataSetDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.DartValues;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.DatasetUsers;
import org.generationcp.middleware.pojos.gdms.MappingABHRow;
import org.generationcp.middleware.pojos.gdms.MappingAllelicSNPRow;
import org.generationcp.middleware.pojos.gdms.MappingAllelicSSRDArTRow;
import org.generationcp.middleware.pojos.gdms.MappingPop;
import org.generationcp.middleware.pojos.gdms.MappingPopValues;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSetPK;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.ExcelSheetColumnName;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;


public class MappingGenotype implements UploadMarker {

	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private HashMap<Integer, String> hmOfColIndexAndMarkerName;
	private AccMetadataSet accMetadataSet;
	private MarkerMetadataSet markerMetadataSet;
	private DatasetUsers datasetUser;
	private MappingPop mappingPop;
	private MappingPopValues mappingPopValues;
	private Dataset dataset;
	private Marker addedMarker;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private MappingPopValues[] arrayOfMappingPopValues;
	private ManagerFactory factory;
	private GenotypicDataManager genoManager;
	private AlleleValues alleleValues;
	private CharValues charValues;
	private GermplasmDataManager manager;
	private DartValues dartValues;
	
	List<MappingAllelicSSRDArTRow> listOfMPSSRDataRows; 
	List<MappingAllelicSNPRow> listOfMPSNPDataRows; 
	List<MappingABHRow> listOfMPABHDataRows; 
	
	 ArrayList markersList=new ArrayList();
	String notMatchingDataDB="";
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    int size=0;
    String notMatchingGIDS="";
    
	String ErrMsg ="";
	Integer iDatasetId = null;

	
	int gid=0;
	int nid=0;
	
	String strMapDataDescription = "";
	String strScore = null;
	String strInstitute = null;
	String strEmail = null;
	String strPurposeOfStudy = null;
	
	int map_id=0;
	String strMappingType="";
	Integer iMpId = null;  //Will be set/overridden by the function
	String strMapCharValue = "-";
    static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
	
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			workbook = Workbook.getWorkbook(new File(strFileLocation));
			strSheetNames = workbook.getSheetNames();
		} catch (BiffException e) {
			throw new GDMSException("Error Reading Mapping Genotype Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading Mapping Genotype Sheet - " + e.getMessage());
		}
	}

	@Override
	public void validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("mapping_source")){
			throw new GDMSException("Mapping_Source Sheet Name Not Found");
		}

		if (false == strSheetNames[1].equalsIgnoreCase("mapping_datalist")){
			throw new GDMSException("Mapping_DataList Sheet Name Not Found");
		}

		//check the template fields
		for(int i = 0; i < strSheetNames.length; i++){

			String strSName = strSheetNames[i].toString();

			if(strSName.equalsIgnoreCase("Mapping_Source")) {
				Sheet sourceSheet = workbook.getSheet(strSName);
				int iNumOfRowsinSourceSheet = sourceSheet.getRows();

				String strArrayOfSourceColumns[] = {"Institute", "Principle investigator", "Email contact", "Dataset Name", "Dataset description", 
						"Genus", "Species", "Population ID", "Parent A GID", "Parent A", 
						"Parent B GID", "Parent B", "Population Size", "Population Type",
						"Purpose of the study", "Scoring Scheme",
						"Missing Data", "Creation Date", "Remark"};

				//Checking if all the columns are present
				for(int j = 0; j < strArrayOfSourceColumns.length; j++){
					String strColFromSheet = (String)sourceSheet.getCell(0, j).getContents().trim();
					if(!strArrayOfSourceColumns[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException(strArrayOfSourceColumns[j] + " Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException(strArrayOfSourceColumns[j] + " Column Name Not Found");
					}
				}	

				//Checking if values have been provided for the required columns
				for(int r = 0; r < iNumOfRowsinSourceSheet; r++){
					String strColumnName = sourceSheet.getCell(0, r).getContents().trim();
					if (strColumnName.equalsIgnoreCase("Institute") || strColumnName.equals("Principle investigator") || strColumnName.equals("Dataset Name") ||
							strColumnName.equalsIgnoreCase("Dataset description") || strColumnName.equalsIgnoreCase("Genus") || strColumnName.equalsIgnoreCase("Species") ||
							strColumnName.equalsIgnoreCase("Population ID") || strColumnName.equalsIgnoreCase("Parent A GID") || strColumnName.equalsIgnoreCase("Parent A") ||
							strColumnName.equalsIgnoreCase("Parent B GID") || strColumnName.equalsIgnoreCase("Parent B") || strColumnName.equalsIgnoreCase("Purpose of the study") ||
							strColumnName.equalsIgnoreCase("Missing Data") || strColumnName.equalsIgnoreCase("Creation Date")) {

						String strFieldValue = sourceSheet.getCell(1, r).getContents().trim();

						if(strFieldValue == null || strFieldValue == ""){
							throw new GDMSException("Please provide value for Required Field " + strColumnName + " at cell position [1," + r + "]");
						}
					}
				}
			}

			if (strSName.equalsIgnoreCase("Mapping_DataList")) {
				Sheet dataListSheet = workbook.getSheet(strSName);
				int intNoOfRows = dataListSheet.getRows();
				int intNoOfCols = dataListSheet.getColumns();
				String strArrayOfReqColumnNames[] = {"Alias", "GID", "Line"};	

				//Checking if all the columns are present in the DataList
				for(int j = 0; j < strArrayOfReqColumnNames.length; j++){
					String strColFromSheet = (String)dataListSheet.getCell(j, 0).getContents().trim();
					if(!strArrayOfReqColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException(strArrayOfReqColumnNames[j] + " Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException("Delete Empty Column at position " + j);
					}
				}

				//Checking if GIDs and Lines have been provided in all the rows
				for(int r = 1; r < intNoOfRows; r++){
					String strGIDalue = (String)dataListSheet.getCell(1, r).getContents().trim();
					if(strGIDalue == null || strGIDalue == ""){
						String strRowNumber = String.valueOf(dataListSheet.getCell(1, r).getRow()+1);	
						String strErrMsg = "Please provide a value at cell position " + "[1" + ", " + strRowNumber + "] in Mapping_DataList sheet.";
						throw new GDMSException(strErrMsg);
					}
					String strLine = (String)dataListSheet.getCell(2, r).getContents().trim();
					if(strLine == null || strLine == ""){
						String strRowNumber = String.valueOf(dataListSheet.getCell(2, r).getRow()+1);	
						String strErrMsg = "Please provide a value at cell position " + "[2" + ", " + strRowNumber + "] in Mapping_DataList sheet.";
						throw new GDMSException(strErrMsg);
					}
				}

				hmOfColIndexAndMarkerName = new HashMap<Integer, String>();
				for(int colIndex = 3; colIndex < intNoOfCols; colIndex++){
					String strMarkerName = dataListSheet.getCell(colIndex, 0).getContents().toString();
					hmOfColIndexAndMarkerName.put(colIndex, strMarkerName);
					for(int r = 0; r < intNoOfRows; r++){
						String strCellValue = (String)dataListSheet.getCell(colIndex, r).getContents().trim();
						if(strCellValue == null || strCellValue == ""){
							String strRowNumber = String.valueOf(dataListSheet.getCell(colIndex, r).getRow()+1);	
							String strErrMsg = "Please provide a value at cell position " + "[" + colIndex + ", " + strRowNumber + "] in Mapping_DataList sheet.";
							throw new GDMSException(strErrMsg);
						}
					}
				}
			}			 
		}
	}


	@Override
	public void createObjectsToBeDisplayedOnGUI() throws GDMSException {

		//Creating a ArrayList of HashMap of fields and values from Mapping_Source sheet
		Sheet sourceSheet = workbook.getSheet(0);
		listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();

		HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();

		String strInstitue = sourceSheet.getCell(1, 0).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Institute.toString(), strInstitue);

		String strPrincipalInvestigator = sourceSheet.getCell(1, 1).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PrincipleInvestigator.toString(), strPrincipalInvestigator);

		String strEmailContact = sourceSheet.getCell(1, 2).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.EmailContact.toString(), strEmailContact);

		String strDatasetName = sourceSheet.getCell(1, 3).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetName.toString(), strDatasetName);

		String strDatasetDescription = sourceSheet.getCell(1, 4).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetDescription.toString(), strDatasetDescription);

		String strGenus = sourceSheet.getCell(1, 5).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Genus.toString(), strGenus);

		String strSpecies = sourceSheet.getCell(1, 6).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Species.toString(), strSpecies);

		String strPopulationID = sourceSheet.getCell(1, 7).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PopulationID.toString(), strPopulationID);

		String strParentAGID = sourceSheet.getCell(1, 8).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ParentAGID.toString(), strParentAGID);

		String strParentA = sourceSheet.getCell(1, 9).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ParentA.toString(), strParentA);

		String strParentBGID = sourceSheet.getCell(1, 10).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ParentBGID.toString(), strParentBGID);

		String strParentB = sourceSheet.getCell(1, 11).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ParentB.toString(), strParentB);

		String strPopulationSize = sourceSheet.getCell(1, 12).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PopulationSize.toString(), strPopulationSize);

		String strPopulationType = sourceSheet.getCell(1, 13).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PopulationType.toString(), strPopulationType);

		String strPurposeOfTheStudy = sourceSheet.getCell(1, 14).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PurposeOfTheStudy.toString(), strPurposeOfTheStudy);

		String strScoringScheme = sourceSheet.getCell(1, 15).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ScoringScheme.toString(), strScoringScheme);

		String strMissingData = sourceSheet.getCell(1, 16).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.MissingData.toString(), strMissingData);

		String strCreationDate = sourceSheet.getCell(1, 17).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.CreationDate.toString(), strCreationDate);

		String strRemark = sourceSheet.getCell(1, 18).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Remark.toString(), strRemark);

		listOfDataInSourceSheet.add(hmOfDataInSourceSheet);


		//Creating a ArrayList of HashMap of fields and values from Mapping_DataList sheet
		Sheet dataSheet = workbook.getSheet(1);
		int iNumOfRowsInDataSheet = dataSheet.getRows();
		int iNumOfColumnsInDataSheet = dataSheet.getColumns();
		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();

		for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strAliasValue = dataSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Alias.toString(), strAliasValue);

			String strGIDValue = dataSheet.getCell(1, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.GID.toString(), strGIDValue);

			String strLineValue = dataSheet.getCell(2, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Line.toString(), strLineValue);

			//Inserting the Marker-Names and Marker-Values
			for (int cIndex = 3; cIndex < iNumOfColumnsInDataSheet; cIndex++){
				String strMarkerName = hmOfColIndexAndMarkerName.get(cIndex);
				String strMarkerValue = dataSheet.getCell(cIndex, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(strMarkerName, strMarkerValue);
			}

			listOfDataInDataSheet.add(hmOfDataInDataSheet);
		}

	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet(){
		return listOfDataInDataSheet;
	}

	@Override
	public void setDataToBeUploded(ArrayList<HashMap<String, String>> theListOfSourceDataRows, ArrayList<HashMap<String, String>> listOfDataRows, ArrayList<HashMap<String, String>> listOfGIDRows) {
		listOfDataRowsFromSourceTable = theListOfSourceDataRows;
		listOfDataRowsFromDataTable = listOfDataRows;
		listOfGIDRowsFromGIDTableForDArT = listOfGIDRows;
	}

	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void upload() throws GDMSException {
		validateData();
		createObjectsToBeSavedToDB();
	}

	@Override
	public void validateData() throws GDMSException {

		//Checking for Column-Names and Values provided in the Mapping_Source table on the GUI
		String strColsInSourceSheet[] = {UploadField.Institute.toString(), UploadField.PrincipleInvestigator.toString(), 
				UploadField.DatasetName.toString(), UploadField.DatasetDescription.toString(), 
				UploadField.Genus.toString(), UploadField.Species.toString(), UploadField.PopulationID.toString(),
				UploadField.ParentAGID.toString(), UploadField.ParentA.toString(), UploadField.ParentBGID.toString(),
				UploadField.ParentB.toString(),
				UploadField.PurposeOfTheStudy.toString(),
				UploadField.MissingData.toString()};

		HashMap<String, String> hmOfSourceColumnsAndValuesFromGUI = listOfDataRowsFromSourceTable.get(0);
		for(int j = 0; j < strColsInSourceSheet.length; j++){
			String strCol = strColsInSourceSheet[j];
			if (false == hmOfSourceColumnsAndValuesFromGUI.containsKey(strCol)){
				throw new GDMSException(strCol + " column not found in data Mapping_Source table.");
			} else {
				//Institute, Principle-Investigator, Dataset-Name, Dataset-Description, Genus, 
				//Species, Population-ID, Parent-A-GID,  Parent-A, Parent-B-GID, Parent-B,
				//Purpose-Of-The-Study, Missing-Data, Creation-Date, 
				if (strCol.equalsIgnoreCase(UploadField.Institute.toString()) || 
						strCol.equalsIgnoreCase(UploadField.PrincipleInvestigator.toString()) || 
						strCol.equalsIgnoreCase(UploadField.DatasetName.toString()) ||
						strCol.equalsIgnoreCase(UploadField.DatasetDescription.toString()) || 
						strCol.equalsIgnoreCase(UploadField.Genus.toString()) ||  
						strCol.equalsIgnoreCase(UploadField.Species.toString()) || 
						strCol.equalsIgnoreCase(UploadField.PopulationID.toString()) || 
						strCol.equalsIgnoreCase(UploadField.ParentAGID.toString()) || 
						strCol.equalsIgnoreCase(UploadField.ParentA.toString()) || 
						strCol.equalsIgnoreCase(UploadField.ParentBGID.toString()) ||
						strCol.equalsIgnoreCase(UploadField.ParentB.toString()) || 
						strCol.equalsIgnoreCase(UploadField.PurposeOfTheStudy.toString()) ||
						strCol.equalsIgnoreCase(UploadField.MissingData.toString()) ||
						strCol.equalsIgnoreCase(UploadField.CreationDate.toString())){
					String strValue = hmOfSourceColumnsAndValuesFromGUI.get(strCol);
					if (null == strValue || strValue.equals("")){
						throw new GDMSException("Please provide a value for " +  strCol + " in data Mapping_Source sheet.");
					}
				}
			}
		}

		//Checking for Column-Names and Values provided in the Mapping_DataList table on the GUI
		String[] strColsInDataSheet = {UploadField.Alias.toString(), UploadField.GID.toString(), 
				UploadField.Line.toString()};

		for(int cIndex = 0; cIndex < strColsInDataSheet.length; cIndex++){
			String strCol = strColsInDataSheet[cIndex];
			for (int rIndex = 0; rIndex < listOfDataRowsFromDataTable.size(); rIndex++){
				HashMap<String, String> hmOfDataColumnsAndValuesFromGUI = listOfDataRowsFromDataTable.get(rIndex);
				if (false == hmOfDataColumnsAndValuesFromGUI.containsKey(strCol)){
					throw new GDMSException(strCol + " column not found in data Mapping_DataList table.");
				} else {
					//GID, Line
					if (strCol.equalsIgnoreCase(UploadField.GID.toString()) || 
							strCol.equalsIgnoreCase(UploadField.Line.toString())){
						String strValue = hmOfDataColumnsAndValuesFromGUI.get(strCol);
						if (null == strValue || strValue.equals("")){
							throw new GDMSException("Please provide a value for " +  strCol + " column in data Mapping_DataList sheet.");
						}
					}
				}
			}	
		}
	}


	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%  :"+GDMSModel.getGDMSModel().getLocalParams().);
		manager = factory.getGermplasmDataManager();
		genoManager=factory.getGenotypicDataManager();
		
		int strPopulationSize =0;
		Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
		String strMapSelectedOnTheUI = GDMSModel.getGDMSModel().getMapSelected();
		String strMarkerForMap = GDMSModel.getGDMSModel().getMarkerForMap();
		
		HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		//Institute, Principle investigator, Email contact, Dataset Name, Dataset description, Genus, Species, Population ID, Parent A GID, 
		//Parent A, Parent B GID, Parent B, Population Size, Population Type, Purpose of the study, 
		//Scoring Scheme, Missing Data, Creation Date, Remark
		String dataset_type="mapping";
		HashMap<String, String> hmOfSourceFieldsAndValues = listOfDataRowsFromSourceTable.get(0);
		
		Sheet sheetDataList=workbook.getSheet(1);
		Sheet sheetSource = workbook.getSheet(0);
		int rowCount=sheetDataList.getRows();
		int colCount=sheetDataList.getColumns();
		
		//String strInstitute = hmOfSourceFieldsAndValues.get(UploadField.Institute.toString());
		/*String strPrincipleInvestigator = hmOfSourceFieldsAndValues.get(UploadField.PrincipleInvestigator.toString());
		//String strEmailContact = hmOfSourceFieldsAndValues.get(UploadField.EmailContact.toString());
		String strDatasetName = hmOfSourceFieldsAndValues.get(UploadField.DatasetName.toString());
		String strDatasetDescription = hmOfSourceFieldsAndValues.get(UploadField.DatasetDescription.toString());
		String strGenus = hmOfSourceFieldsAndValues.get(UploadField.Genus.toString());
		String strSpecies = hmOfSourceFieldsAndValues.get(UploadField.Species.toString());
		//String strPopulationID = hmOfSourceFieldsAndValues.get(UploadField.PopulationID.toString());
		String strParentAGID = hmOfSourceFieldsAndValues.get(UploadField.ParentAGID.toString());
		String strParentA = hmOfSourceFieldsAndValues.get(UploadField.ParentA.toString());
		String strParentBGID = hmOfSourceFieldsAndValues.get(UploadField.ParentBGID.toString());
		String strParentB = hmOfSourceFieldsAndValues.get(UploadField.ParentB.toString());
		if(hmOfSourceFieldsAndValues.get(UploadField.ParentB.toString())=="")
			strPopulationSize=0;
		else
			strPopulationSize = Integer.parseInt(hmOfSourceFieldsAndValues.get(UploadField.PopulationSize.toString()));
		String strPopulationType = hmOfSourceFieldsAndValues.get(UploadField.PopulationType.toString());
		String strPurposeOfTheStudy = hmOfSourceFieldsAndValues.get(UploadField.PurposeOfTheStudy.toString());
		String strScoringScheme = hmOfSourceFieldsAndValues.get(UploadField.ScoringScheme.toString());
		String strMissingData = hmOfSourceFieldsAndValues.get(UploadField.MissingData.toString());
		String strCreationDate = hmOfSourceFieldsAndValues.get(UploadField.CreationDate.toString());
		String strRemark = hmOfSourceFieldsAndValues.get(UploadField.Remark.toString());*/
		
		String strPrincipleInvestigator = sheetSource.getCell(1,1).getContents().trim();
		//String strEmailContact = hmOfSourceFieldsAndValues.get(UploadField.EmailContact.toString());
		String strDatasetName =sheetSource.getCell(1,3).getContents().trim();
		String strDatasetDescription = sheetSource.getCell(1,4).getContents().trim();
		String strGenus = sheetSource.getCell(1,5).getContents().trim();
		String strSpecies = sheetSource.getCell(1,6).getContents().trim();
		//String strPopulationID = hmOfSourceFieldsAndValues.get(UploadField.PopulationID.toString());
		String strParentAGID =sheetSource.getCell(1,8).getContents().trim();
		String strParentA = sheetSource.getCell(1,9).getContents().trim();
		String strParentBGID =sheetSource.getCell(1,10).getContents().trim();
		String strParentB = sheetSource.getCell(1,11).getContents().trim();
		if(sheetSource.getCell(1,12).getContents().trim() =="")
			strPopulationSize=0;
		else
			strPopulationSize = Integer.parseInt(sheetSource.getCell(1,12).getContents().trim().toString());
		String strPopulationType = sheetSource.getCell(1,13).getContents().trim();
		String strPurposeOfTheStudy = sheetSource.getCell(1,14).getContents().trim();
		String strScoringScheme = sheetSource.getCell(1,15).getContents().trim();
		String strMissingData = sheetSource.getCell(1,16).getContents().trim();
		String strCreationDate =sheetSource.getCell(1,17).getContents().trim();
		String strRemark =sheetSource.getCell(1,18).getContents().trim();
		
		
		
		

		//Assigning the User Id value based on the Principle investigator's value in the Mapping_Source sheet
		Integer iUserId = 0;
		if (null == strPrincipleInvestigator || strPrincipleInvestigator.equals("")){
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
			UserDAO userDAO = new UserDAO();
			userDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
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
		}
		
		String strMapSelected =  "";

		if (null != strMapSelectedOnTheUI){
			strMapSelected = strMapSelectedOnTheUI;  
		}
		Integer iMapId = 0;
		
		
		
		if(!(strMapSelected.isEmpty())){
			
			try{
				long mapId = genoManager.getMapIdByName(strMapSelected);
				iMapId = (int)mapId;
			}catch (MiddlewareQueryException e) {
				throw new GDMSException("Error uploading Map");
			}
			//20131111: Tulasi - Used the above code to get Map Id for the Map selected on the GUI for both Allelic and ABH data
		}else{
			iMapId=0;
		}

		String strMarkerTypeSelected = GDMSModel.getGDMSModel().getMarkerForMap(); //SSR/SNP/DArT 
		String mappingType = GDMSModel.getGDMSModel().getMappingType();
		
		if(mappingType.equalsIgnoreCase("AllelicData"))		
			strMappingType="allelic";
		else
			strMappingType="abh";
		
			
		String gids1="";
		int gidsCount=0;
		String marker="";
		String marker_type="";
		if (sheetDataList==null){
				System.out.println("Empty Sheet");		
		}else{
			int intNR=sheetDataList.getRows();
			int intColRowEmpty=0;
			//int rows=sheetDataList.getRows();
			for(int i=0;i<intNR;i++){
				Cell c=sheetDataList.getCell(0,i);
				String s=c.getContents();
				if(!s.equals("")){
					intColRowEmpty=intColRowEmpty + 1;
					
				}
			}
			
			
			
			for (int a=3;a<colCount;a++){				
				markersList.add(sheetDataList.getCell(a,0).getContents().trim());		
				marker = marker +"'"+ sheetDataList.getCell(a,0).getContents().trim().toString()+"',";
			}
			String exists="";
			ArrayList pGidsList=new ArrayList();
			ArrayList pGNamesList=new ArrayList();
			ArrayList pNamesList=new ArrayList();
			HashMap<String, Integer> GIDsMapP = new HashMap<String, Integer>();
			if(strMappingType.equalsIgnoreCase("allelic")){
				marker_type=strMarkerTypeSelected;
				int dataset=0;
				String parentA=sheetSource.getCell(1,9).getContents().trim();
				int parentA_GID=Integer.parseInt(sheetSource.getCell(1,8).getContents().trim().toString());
				String parentB=sheetSource.getCell(1,11).getContents().trim();
				int parentB_GID=Integer.parseInt(sheetSource.getCell(1,10).getContents().trim().toString());
				String parentGids=parentA_GID+","+parentB_GID;
				if(!(pGidsList.contains(parentA_GID))){
					pGidsList.add(parentA_GID);
					pGNamesList.add(parentA_GID+","+parentA);
					pNamesList.add(parentA);
					GIDsMapP.put(parentA,parentA_GID);	
				}
				if(!(pGidsList.contains(parentB_GID))){
					pGidsList.add(parentB_GID);
					pGNamesList.add(parentB_GID+","+parentB);
					pNamesList.add(parentB);
					GIDsMapP.put(parentB,parentB_GID);
				}
				//System.out.println("**************:"+pGidsList);
				SortedMap mapP = new TreeMap();
	            List lstgermpNameP = new ArrayList();
	           // manager = factory.getGermplasmDataManager();
				List<Name> names = null;
				/*for(int n=0;n<pGidsList.size();n++){
					names = manager.getNamesByGID(Integer.parseInt(pGidsList.get(n).toString()), null, null);
					for (Name name : names) {					
						 lstgermpNameP.add(name.getGermplasmId());
						 mapP.put(name.getGermplasmId(), name.getNval());	
						 addValues(name.getGermplasmId(), name.getNval().toLowerCase());	
			        }
				}*/
				ArrayList gidsDBList = new ArrayList();
				ArrayList gNamesDBList = new ArrayList();
				hashMap.clear();
				try{
					for(int n=0;n<pNamesList.size();n++){
						List<Germplasm> germplasmList = manager.getGermplasmByName(pNamesList.get(n).toString(), 0, new Long(manager.countGermplasmByName(pNamesList.get(n).toString(), Operation.EQUAL)).intValue(), Operation.EQUAL);
						for (Germplasm g1 : germplasmList) {
				        	if(!(gidsDBList.contains(g1.getGid()))){
				        		gidsDBList.add(g1.getGid());
				        		gNamesDBList.add(pNamesList.get(n).toString());
				        		addValues(pNamesList.get(n).toString(), g1.getGid());					        		
				        	}				        	
				           //System.out.println("  " + g.getGid());
				        }
				        //System.out.println(n+":"+gnamesList.get(n).toString()+"   "+hashMap.get(gnamesList.get(n).toString()));
					}
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				//System.out.println("....mapP="+mapP);
				if(gNamesDBList.size()==0){
		        	   alertGID="yes";
		        	   size=0;
		           }
				int gidToCompare=0;
		           String gNameToCompare="";
		           //String gNameFromMap="";
		           ArrayList gNameFromMap=new ArrayList();
		           if(gNamesDBList.size()>0){					           
			           for(int n=0;n<pNamesList.size();n++){
		        		   if(gNamesDBList.contains(pNamesList.get(n))){
		        			   if(!(hashMap.get(pNamesList.get(n).toString()).contains(GIDsMapP.get(pNamesList.get(n).toString())))){
		        				   notMatchingData=notMatchingData+pNamesList.get(n)+"   "+GIDsMapP.get(pNamesList.get(n).toString())+"\n\t";
		        				   notMatchingDataDB=notMatchingDataDB+pNamesList.get(n)+"="+hashMap.get(pNamesList.get(n))+"\t";
				        		   alertGN="yes";
		        			   }
		        		   }else{
		        			   //int gid=GIDsMap.get(gnamesList.get(n).toString());
		        			   alertGID="yes";
			        		   size=hashMap.size();
			        		   notMatchingGIDS=notMatchingGIDS+pNamesList.get(n).toString()+", ";
		        		   }
		        	   }
		           }
		           if((alertGN.equals("yes"))&&(alertGID.equals("no"))){
		        	   //String ErrMsg = "GID(s) ["+notMatchingGIDS.substring(0,notMatchingGIDS.length()-1)+"] of Germplasm(s) ["+notMatchingData.substring(0,notMatchingData.length()-1)+"] being assigned to ["+notMatchingDataExists.substring(0,notMatchingDataExists.length()-1)+"] \n Please verify the template ";
		        	   ErrMsg = "Please verify the name(s) provided \t "+notMatchingData+" which do not match the GID(s) present in the database"+notMatchingDataDB;
		        	 
		        	   throw new GDMSException(ErrMsg);
		           }
		           if((alertGID.equals("yes"))&&(alertGN.equals("no"))){	        	   
		        	   if(size==0){
		        		   ErrMsg = "The Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook ";
		        	   }else{
		        		   ErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS;
		        		   //ErrMsg = "Please verify the GID/Germplasm(s) provided as some of them do not exist in the database. \n Please upload germplasm information into GMS ";
		        	   }	        	   
		        	   //ErrMsg = "Please verify the following GID/Germplasm(s) doesnot exists. \n Upload germplasm Information into GMS \n\t"+notMatchingGIDS;
		        	   throw new GDMSException(ErrMsg);
		           }
				
		           if((alertGID.equals("yes"))&&(alertGN.equals("yes"))){
		        	   ErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS+" \n Please verify the name(s) provided "+notMatchingData+" which do not match the GIDS(s) present in the database "+notMatchingDataDB;
		        	   throw new GDMSException(ErrMsg);
		           }
				
				
				List datasetIdsL=new ArrayList();
				List datasetIdsC=new ArrayList();
				List datasetIdsList=new ArrayList();
				try{
					AccMetadataSetDAO accDAOLocal = new AccMetadataSetDAO();
					accDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
					List<AccMetadataSetPK> dataset_Ids= accDAOLocal.getAccMetadataSetByGids(pGidsList, 0, (int)accDAOLocal.countAccMetadataSetByGids(pGidsList));
					for (AccMetadataSetPK name : dataset_Ids){
						//System.out.println(name.getDatasetId());
						if(!(datasetIdsL.contains(name.getDatasetId())))
							datasetIdsL.add(name.getDatasetId());
					}
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				try{
					AccMetadataSetDAO accDAOCentral = new AccMetadataSetDAO();
					accDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
					List<AccMetadataSetPK> dataset_IdsC= accDAOCentral.getAccMetadataSetByGids(pGidsList, 0, (int)accDAOCentral.countAccMetadataSetByGids(pGidsList));
					for (AccMetadataSetPK name : dataset_IdsC){
						//System.out.println(name.getDatasetId());
						if(!(datasetIdsC.contains(name.getDatasetId())))
							datasetIdsC.add(name.getDatasetId());
					}
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				if(datasetIdsL.size()>0){
					for(int dl=0;dl<datasetIdsL.size();dl++){
						if(!(datasetIdsList.contains(datasetIdsL.get(dl))))
							datasetIdsList.add(datasetIdsL.get(dl));
					}
				}
				if(datasetIdsC.size()>0){
					for(int dc=0;dc<datasetIdsC.size();dc++){
						if(!(datasetIdsList.contains(datasetIdsC.get(dc))))
							datasetIdsList.add(datasetIdsC.get(dc));
					}
				}
				if(datasetIdsList.size()==0)
					exists="no";
				try{
				List<Integer> markerIdsC = genoManager.getMarkerIdsByMarkerNames(markersList, 0, markersList.size(),Database.CENTRAL);
				List<Integer> markerIdsL = genoManager.getMarkerIdsByMarkerNames(markersList, 0, markersList.size(),Database.LOCAL);
				
				List<Integer> markerIdsList=new ArrayList();
				if(markerIdsL.size()>0){
					for(int ml=0;ml<markerIdsL.size();ml++){
						if(!(markerIdsList.contains(markerIdsL.get(ml))))
							markerIdsList.add(markerIdsL.get(ml));
					}
				}
				if(markerIdsC.size()>0){
					for(int mc=0;mc<markerIdsC.size();mc++){
						if(!(markerIdsList.contains(markerIdsC.get(mc))))
							markerIdsList.add(markerIdsC.get(mc));
					}
				}
				List markerFromDBFinal=new ArrayList();
				//genoManager.get.get
				MarkerMetadataSetDAO marmetaDAOCentral= new MarkerMetadataSetDAO();
				marmetaDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
				for(int d=0;d<datasetIdsList.size();d++){
					List<Integer> markerFromDBC= marmetaDAOCentral.getMarkerIdByDatasetId(Integer.parseInt(datasetIdsList.get(d).toString()));
					for(int c=0;c<markerFromDBC.size();c++){
						if(!markerFromDBFinal.contains(Integer.parseInt(markerFromDBC.get(c).toString())))
							markerFromDBFinal.add(Integer.parseInt(markerFromDBC.get(c).toString()));
					}
				}
				MarkerMetadataSetDAO marmetaDAOLocal= new MarkerMetadataSetDAO();
				marmetaDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
				for(int d=0;d<datasetIdsList.size();d++){
					List<Integer> markerFromDBL= marmetaDAOLocal.getMarkerIdByDatasetId(Integer.parseInt(datasetIdsList.get(d).toString()));
					for(int l=0;l<markerFromDBL.size();l++){
						if(!markerFromDBFinal.contains(Integer.parseInt(markerFromDBL.get(l).toString())))
							markerFromDBFinal.add(Integer.parseInt(markerFromDBL.get(l).toString()));
					}
				}
				
				for(int m=0;m<markerIdsList.size();m++){
					if(markerFromDBFinal.contains(Integer.parseInt(markerIdsList.get(m).toString())))
						exists="yes";
					else
						exists="no";
				}
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				/*
				rs1=st1.executeQuery("select * from gdms_marker_metadataset where dataset_id="+dataset+" and marker_id in(select marker_id from gdms_marker where marker_name in("+marker.substring(0,marker.length()-1)+") order by marker_id)");	
				if(rs1.next())
					exists="yes";
				else
					exists="no";*/
				//System.out.println("2 exists="+exists);
				if(exists.equalsIgnoreCase("no")){
					if((!(sheetDataList.getCell(2,1).getContents().trim().toString().equals(parentA)))&&(!(sheetDataList.getCell(2,2).getContents().trim().toString().equals(parentB)))){
						 String strRowNumber1 = String.valueOf(sheetDataList.getCell(2, 1).getRow()+1);	
						 String strRowNumber2 = String.valueOf(sheetDataList.getCell(2, 2).getRow()+1);	
						 ErrMsg = "Please provide Parents Information first followed by population in Mapping_DataList sheet.\n  The row position is "+strRowNumber1+" & "+strRowNumber2;
						 throw new GDMSException(ErrMsg);
					}
					
				}
			}
			
			String parentGids=sheetSource.getCell(1,8).getContents().trim()+","+sheetSource.getCell(1,10).getContents().trim();
			String gidsForQuery = "";
			HashMap<String, Integer> GIDsMap = new HashMap<String, Integer>();
			String gNames="";
			ArrayList gidsAList=new ArrayList();
			ArrayList gidNamesList=new ArrayList();
			ArrayList NamesList=new ArrayList();
			//System.out.println("rowCount=:"+rowCount);
			for(int r=1;r<rowCount;r++){	
				gidsForQuery = gidsForQuery + sheetDataList.getCell(1,r).getContents().trim()+",";
				gNames=gNames+"'"+sheetDataList.getCell(2,r).getContents().trim()+"',";
				gids1=gids1+sheetDataList.getCell(1,r).getContents().trim()+"!~!"+sheetDataList.getCell(2,r).getContents().trim()+",";
				GIDsMap.put(sheetDataList.getCell(2,r).getContents().trim(),Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim()));
				
				if(!gidNamesList.contains(Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim())))
					gidNamesList.add(Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim())+","+sheetDataList.getCell(2,r).getContents().trim());
				
				if(!gidsAList.contains(Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim())))
					gidsAList.add(Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim()));
				
				if(!NamesList.contains(sheetDataList.getCell(2,r).getContents().trim()))
					NamesList.add(sheetDataList.getCell(2,r).getContents().trim());
				
				
				gidsCount=gidsCount+1;
			}
			String gidsO="";
			int gnCount=0;
			
			for(int gn=1;gn<rowCount;gn++){					
				gids1=gids1+sheetDataList.getCell(2,gn).getContents().trim()+",";
				gnCount=gnCount+1;
			}
			int s=0;
			//String fGids="";
			ArrayList fGids=new ArrayList();
			String gidsRet="";
			
			//HashMap<Integer, String> GIDsMap = new HashMap<Integer, String>();
			/** arranging gid's with respect to germplasm name in order to insert into allele_values table */
			if(gidsCount==gnCount){			
				
	            gidsForQuery=gidsForQuery.substring(0, gidsForQuery.length()-1);
	           
	            SortedMap map = new TreeMap();
	            List lstgermpName = new ArrayList();
	            manager = factory.getGermplasmDataManager();
				List<Name> names = null;
				
	            ArrayList gidsDBList = new ArrayList();
				ArrayList gNamesDBList = new ArrayList();
				hashMap.clear();
				//System.out.println("NamesList*******************:"+NamesList);
				try{
					for(int n=0;n<NamesList.size();n++){
						List<Germplasm> germplasmList = manager.getGermplasmByName(NamesList.get(n).toString(), 0, new Long(manager.countGermplasmByName(NamesList.get(n).toString(), Operation.EQUAL)).intValue(), Operation.EQUAL);
						for (Germplasm g1 : germplasmList) {
				        	if(!(gidsDBList.contains(g1.getGid()))){
				        		gidsDBList.add(g1.getGid());
				        		gNamesDBList.add(NamesList.get(n).toString());
				        		addValues(NamesList.get(n).toString(), g1.getGid());					        		
				        	}				        	
				           //System.out.println(" **************** " + g1.getGid()+"  "+NamesList.get(n));
				        }
				        //System.out.println(n+":"+gnamesList.get(n).toString()+"   "+hashMap.get(gnamesList.get(n).toString()));
					}
					//System.out.println("......"+map.size()+".........  map="+map);
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
	          
	            /*
	            System.out.println("lstgermpName="+lstgermpName);*/			           
	           if(gNamesDBList.size()==0){
	        	   alertGID="yes";
	        	   size=0;
	           }
	          
	           int gidToCompare=0;
	           String gNameToCompare="";
	           String gNameFromMap="";
	           /*System.out.println("gidNamesList="+gNamesDBList);
	           System.out.println("gidNamesList="+hashMap);
	           System.out.println("NamesList=:"+NamesList);
	           System.out.println("GIDsMap:+"+GIDsMap);*/
	           if(gNamesDBList.size()>0){
		           
		           for(int n=0;n<NamesList.size();n++){
	        		   if(gNamesDBList.contains(NamesList.get(n))){
	        			   if(!(hashMap.get(NamesList.get(n).toString()).contains(GIDsMap.get(NamesList.get(n).toString())))){
	        				   notMatchingData=notMatchingData+NamesList.get(n)+"   "+GIDsMap.get(NamesList.get(n).toString())+"\n\t";
	        				   notMatchingDataDB=notMatchingDataDB+NamesList.get(n)+"="+hashMap.get(NamesList.get(n))+"\t";
			        		   alertGN="yes";
	        			   }
	        		   }else{
	        			   //int gid=GIDsMap.get(gnamesList.get(n).toString());
	        			   alertGID="yes";
		        		   size=hashMap.size();
		        		   notMatchingGIDS=notMatchingGIDS+NamesList.get(n).toString()+", ";
	        		   }
	        	   }
	           }
	           if((alertGN.equals("yes"))&&(alertGID.equals("no"))){
	        	   //String ErrMsg = "GID(s) ["+notMatchingGIDS.substring(0,notMatchingGIDS.length()-1)+"] of Germplasm(s) ["+notMatchingData.substring(0,notMatchingData.length()-1)+"] being assigned to ["+notMatchingDataExists.substring(0,notMatchingDataExists.length()-1)+"] \n Please verify the template ";
	        	   ErrMsg = "Please verify the name(s) provided \t "+notMatchingData+" which do not match the GID(s) present in the database"+notMatchingDataDB;
	        	   throw new GDMSException(ErrMsg);	 
	           }
	           if((alertGID.equals("yes"))&&(alertGN.equals("no"))){	        	   
	        	   if(size==0){
	        		   ErrMsg = "The Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook ";
	        	   }else{
	        		   ErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS;
	        		   //ErrMsg = "Please verify the GID/Germplasm(s) provided as some of them do not exist in the database. \n Please upload germplasm information into GMS ";
	        	   }	        	   
	        	   //ErrMsg = "Please verify the following GID/Germplasm(s) doesnot exists. \n Upload germplasm Information into GMS \n\t"+notMatchingGIDS;
	        	   throw new GDMSException(ErrMsg);
	           }
			
	           if((alertGID.equals("yes"))&&(alertGN.equals("yes"))){
	        	   ErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS+" \n Please verify the name(s) provided "+notMatchingData+" which do not match the GIDS(s) present in the database "+notMatchingDataDB;
	        	   throw new GDMSException(ErrMsg); 
	           }
			
			}
			 List lstMarkers = new ArrayList();
			String markersForQuery="";
			/** retrieving maximum marker id from 'marker' table of database **/
			
			
			int parentA_nid=0;
			int parentB_nid=0;
			
			int parentAGid=Integer.parseInt(sheetSource.getCell(1,8).getContents().trim());
			String parentA=sheetSource.getCell(1,9).getContents().trim();
			
			int parentBGid=Integer.parseInt(sheetSource.getCell(1,10).getContents().trim());
			String parentB=sheetSource.getCell(1,11).getContents().trim();
			try{
			Name namesPA = null;
			namesPA=manager.getNameByGIDAndNval(parentAGid, parentA, GetGermplasmByNameModes.STANDARDIZED);
			if(namesPA==null){
				namesPA=manager.getNameByGIDAndNval(parentAGid, parentA, GetGermplasmByNameModes.NORMAL);
			}
			
			parentA_nid=namesPA.getNid();
			
			
			Name namesPB = null;
			namesPB=manager.getNameByGIDAndNval(parentBGid, parentB, GetGermplasmByNameModes.STANDARDIZED);
			if(namesPB==null){
				namesPB=manager.getNameByGIDAndNval(parentBGid, parentB, GetGermplasmByNameModes.NORMAL);
			}
			parentB_nid=namesPB.getNid();
			
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			
			if(strDatasetName.length()>30){
				ErrMsg = "Dataset Name value exceeds max char size.";
				throw new GDMSException(ErrMsg);
			}
			
			
			String purposeOfStudy=sheetSource.getCell(1,14).getContents().trim();
			/*scoringScheme=sheetSource.getCell(1,15).getContents().trim();
			missingData=sheetSource.getCell(1,16).getContents().trim();	*/			
			boolean dFormat=isValidDate(sheetSource.getCell(1,17).getContents().trim());
			if(dFormat==false){
				ErrMsg = "Creation Date should be in yyyy-mm-dd format";
				throw new GDMSException(ErrMsg);
			}else{
				 uploadTemplateDate = uploadTemplateDate;
			}
			
			
			//remarks=sheetSource.getCell(1,18).getContents().trim();
			dataset=new Dataset();
			
			
			dataset.setDatasetName(strDatasetName);
			dataset.setDatasetDesc(strDatasetDescription);
			dataset.setDatasetType(dataset_type);
			dataset.setGenus(strGenus);
			dataset.setSpecies(strSpecies);
			dataset.setUploadTemplateDate(uploadTemplateDate);
			dataset.setRemarks(strRemark);
			dataset.setDataType("mapping");
			dataset.setMissingData(strMissingData);
			//dataset.setMethod(strMethod);
			dataset.setInstitute(strInstitute);
			dataset.setPrincipalInvestigator(strPrincipleInvestigator);
			dataset.setEmail(strEmail);
			dataset.setPurposeOfStudy(strPurposeOfStudy);
			
			
			datasetUser = new DatasetUsers(iDatasetId, iUserId);

			///System.out.println("strMappingType:"+strMappingType);
			int iPopulationSize = strPopulationSize;
			mappingPop = new MappingPop();
			mappingPop.setMappingType(strMappingType);
			mappingPop.setParentANId(parentA_nid);
			mappingPop.setParentBGId(parentB_nid);
			mappingPop.setPopulationSize(iPopulationSize);
			mappingPop.setPopulationType(strPopulationType);
			mappingPop.setMapDataDescription(strMapDataDescription);
			mappingPop.setScoringScheme(strScoringScheme);
			mappingPop.setMapId(iMapId);
			
			
			Integer iACId = null;
			SortedMap mapN = new TreeMap();
			//System.out.println(",,,,,,,,,,,,,,,,,gNames="+gNames);
			ArrayList finalList =new ArrayList();
			ArrayList gidL=new ArrayList();
			
			/**
			 * getting nids with gid and nval for inserting into gdms_acc_metadataset table			
			*/
	       try{
			Name names = null;
			for(int n=0;n<gidsAList.size();n++){
				/*names = manager.getNameByGIDAndNval(Integer.parseInt(gidsAList.get(n).toString()), NamesList.get(n).toString());
				if(!gidL.contains(names.getGermplasmId()))
	            	gidL.add(names.getGermplasmId());
	            mapN.put(names.getGermplasmId(), names.getNid());*/
				//names = manager.getNameByGIDAndNval(Integer.parseInt(gidsAList.get(n).toString()), NamesList.get(n).toString(), GetGermplasmByNameModes.STANDARDIZED);
				names = manager.getNameByGIDAndNval(Integer.parseInt(gidsAList.get(n).toString()), NamesList.get(n).toString(), GetGermplasmByNameModes.STANDARDIZED);
				if(names==null){
					names=manager.getNameByGIDAndNval(Integer.parseInt(gidsAList.get(n).toString()), NamesList.get(n).toString(), GetGermplasmByNameModes.NORMAL);
				}
				if(!gidL.contains(names.getGermplasmId()))
	            	gidL.add(names.getGermplasmId());
	            mapN.put(names.getGermplasmId(), names.getNid());
				
			}
			
	       } catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
	        for(int a=0;a<gidsAList.size();a++){
	        	int gid1=Integer.parseInt(gidsAList.get(a).toString());
	        	if(gidL.contains(gid1)){
	        		finalList.add(gid1+"~!~"+mapN.get(gid1));	
	        	}
	        }
            
	       // System.out.println("finalList="+finalList);
	        
	       
			//String markersList="";
			
            ArrayList gids=new ArrayList();
            String gidsList="";
            int rows=0;
            int cols=0;
            if(strMappingType.equalsIgnoreCase("allelic")){
            	rows=3;
            }else{
            	rows=1;
            }
            for (int l=rows;l<rowCount;l++){				
				gids.add(sheetDataList.getCell(1,l).getContents().trim());		
				gidsList = gidsList +"'"+ sheetDataList.getCell(1,l).getContents().trim().toString()+"',";
			}
            
           
            String charData="";
            listOfMPABHDataRows = new ArrayList<MappingABHRow>();
    		listOfMPSSRDataRows = new ArrayList<MappingAllelicSSRDArTRow>();
    		listOfMPSNPDataRows = new ArrayList<MappingAllelicSNPRow>();
			if(strMappingType.equalsIgnoreCase("allelic")){
				if(marker_type.equalsIgnoreCase("snp")){
					//intAC_ID=uptMId.getMaxIdValue("ac_id","gdms_char_values",session);	
					String[] pGids=parentGids.split(",");
					int mcount=0;
					int gcount=0;
					
					for(int r=1;r<3;r++){
						mcount=0;
						gid=Integer.parseInt(pGids[gcount]);
						nid=Integer.parseInt(mapN.get(gid).toString());
						accMetadataSet = new AccMetadataSet(iDatasetId, gid, nid);
						for(int c=3;c<colCount;c++){
							String strMarkerName=sheetDataList.getCell(c,0).getContents().trim();
							addedMarker= new Marker();
							addedMarker.setMarkerName(strMarkerName);
							addedMarker.setSpecies("groundnut");
							
							
							//charValues = new CharValues();
							charValues = new CharValues();
							charValues.setAcId(iACId);
							
							                 
							String charStr=sheetDataList.getCell(c,r).getContents().trim();
							//System.out.println(charStr.length());
							if(charStr.length()>2){
								if(charStr.contains(":")){
									String str1="";
									String str2="";
									//String charStr=str.get(s);
									str1=charStr.substring(0, charStr.length()-2);
									str2=charStr.substring(2, charStr.length());
									charData=str1+"/"+str2;
								}else if(charStr.contains("/")){
									charData=charStr;
								}else{
									ExcelSheetColumnName escn =  new ExcelSheetColumnName();
									String strColName = escn.getColumnName(sheetDataList.getCell(c, r).getColumn());							
																				
									 ErrMsg = "Data not submitted to the database. \n Please check the allele value("+charStr+") given at cell position "+strColName+(sheetDataList.getCell(c, r).getRow()+1)+"\n Heterozygote data representation should be either : or /";
									 /*request.getSession().setAttribute("indErrMsg", ErrMsg);
									 return "ErrMsg";*/
									 throw new GDMSException(ErrMsg);
									
								}
							}else if(charStr.length()==2){
								String str1="";
								String str2="";
								//String charStr=str.get(s);
								str1=charStr.substring(0, charStr.length()-1);
								str2=charStr.substring(1);
								charData=str1+"/"+str2;
								
							}else if(charStr.length()==1){
								if(charStr.equalsIgnoreCase("A")){
									charData="A/A";	
								}else if(charStr.equalsIgnoreCase("C")){	
									charData="C/C";
								}else if(charStr.equalsIgnoreCase("G")){
									charData="G/G";
								}else if(charStr.equalsIgnoreCase("T")){
									charData="T/T";
								}else{
									charData=charStr;
								}
							}							
							
							charValues.setgId(Integer.parseInt(pGids[gcount]));
							charValues.setCharValue(charData);							
							
							mcount++;
							
							
							
						}
						gcount++;
					}
				}else if((marker_type.equalsIgnoreCase("ssr"))||(marker_type.equalsIgnoreCase("DArT"))){
					Integer intAnID = null;
					String[] pGids=parentGids.split(",");
					int mcount=0;
					int gcount=0;
					
					for(int r=1;r<3;r++){
						mcount=0;
						for(int c=3;c<colCount;c++){
							alleleValues = new AlleleValues();
							alleleValues.setAnId(intAnID);
							//alleleValues.setDatasetId(iNewDatasetId);
							charData=sheetDataList.getCell(c,r).getContents().trim();
							alleleValues.setgId(Integer.parseInt(pGids[gcount]));
							alleleValues.setAlleleBinValue(charData);
							
							mcount++;
							
							
						}
						gcount++;
					}
				}
								
			}	
			int gi=0;
			//mp_id=mp_id+1;
			//mp_id=mp_id-1;
			String strData1="";
			for(int i=rows;i<rowCount;i++){	
				//String[] insGids=fGids.split(",");
				int m=0;
				for(int j=3;j<colCount;j++){
					mappingPopValues = new MappingPopValues();
					mappingPopValues.setMpId(iMpId);
					
					strData1=sheetDataList.getCell(j,i).getContents();
					//System.out.println("strData1.length()=:"+strData1.length());
					if((strMappingType.equalsIgnoreCase("allelic"))&&(marker_type.equalsIgnoreCase("snp"))){
					
						if(strData1.length()>2){
							String charStr=strData1;
							if(charStr.contains(":")){
								String str1="";
								String str2="";
								//String charStr=str.get(s);
								str1=charStr.substring(0, charStr.length()-2);
								str2=charStr.substring(2, charStr.length());
								charData=str1+"/"+str2;
							}else if(charStr.contains("/")){
								charData=charStr;
							}else{
								//String errVal=charStr;
								ExcelSheetColumnName escn =  new ExcelSheetColumnName();
								String strColName = escn.getColumnName(sheetDataList.getCell(j, i).getColumn());							
																			
								 ErrMsg = "Data not submitted to the database. \n Please check the allele value("+charStr+") given at cell position "+strColName+(sheetDataList.getCell(j, i).getRow()+1)+"\n Heterozygote data representation should be either : or /";
								 throw new GDMSException(ErrMsg);
							}
							
						}else if(strData1.length()==2){
							String str1="";
							String str2="";
							String charStr=strData1;
							str1=charStr.substring(0, charStr.length()-1);
							str2=charStr.substring(1);
							charData=str1+"/"+str2;
							//System.out.println(".....:"+str.get(s).substring(1));
						}else if(strData1.length()==1){
							if(strData1.equalsIgnoreCase("A")){
								charData="A/A";	
							}else if(strData1.equalsIgnoreCase("C")){	
								charData="C/C";
							}else if(strData1.equalsIgnoreCase("G")){
								charData="G/G";
							}else if(strData1.equalsIgnoreCase("T")){
								charData="T/T";
							}else{
								charData=strData1;
							}							
						}
					}else{
						charData=sheetDataList.getCell(j,i).getContents();
					}
					
					mappingPopValues.setMapCharValue(charData);
					mappingPopValues.setGid(Integer.parseInt(gids.get(gi).toString()));
					
																
					//g++;
					//mp_id++;
					//mp_id--;
					m++;
									
				}
				m=0;
				gi++;
				gid=Integer.parseInt(gids.get(gi).toString());
				nid=Integer.parseInt(mapN.get(gid).toString());
				accMetadataSet = new AccMetadataSet(iDatasetId, gid, nid);
				
				//g=0;
				//m=0;
			}
			
			
			
			markerMetadataSet = new MarkerMetadataSet(null, 0);		
			if(strMappingType.equalsIgnoreCase("ABH")){
				MappingABHRow mappingPopABHDataRow = new MappingABHRow(addedMarker, accMetadataSet, markerMetadataSet, mappingPopValues);
				listOfMPABHDataRows.add(mappingPopABHDataRow);
			}else if(strMappingType.equalsIgnoreCase("Allelic")){
				if((strMarkerTypeSelected.equalsIgnoreCase("SSR"))||(strMarkerTypeSelected.equalsIgnoreCase("DArT"))){				
					MappingAllelicSSRDArTRow mappingSSRDArTDataRow = new MappingAllelicSSRDArTRow(addedMarker, accMetadataSet, markerMetadataSet, mappingPopValues, alleleValues, dartValues);
					listOfMPSSRDataRows.add(mappingSSRDArTDataRow);
				}else if(strMarkerTypeSelected.equalsIgnoreCase("SNP")){
					MappingAllelicSNPRow mappingSNPDataRows= new MappingAllelicSNPRow(addedMarker, accMetadataSet, markerMetadataSet, mappingPopValues, charValues);
					listOfMPSNPDataRows.add(mappingSNPDataRows);
				}
			}
			
		}
		
		
		if(strMappingType.equalsIgnoreCase("ABH")){
			
			saveMappingABH();
		}else if(strMappingType.equalsIgnoreCase("Allelic")){
			if((strMarkerTypeSelected.equalsIgnoreCase("SSR"))||(strMarkerTypeSelected.equalsIgnoreCase("DArT"))){
				saveMappingAllelicSSRDArT();
			}else if(strMarkerTypeSelected.equalsIgnoreCase("SNP")){
				saveMappingAllelicSNP();
			}
		}
		
		
		


	}

	protected void saveMappingABH() throws GDMSException {
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		genotypicDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
		genotypicDataManagerImpl.setSessionProviderForCentral(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral());
		try {
			genotypicDataManagerImpl.setMappingABH(dataset, datasetUser, mappingPop, listOfMPABHDataRows);
			//genotypicDataManagerImpl.setMappingData(accMetadataSet, markerMetadataSet, datasetUser, mappingPop, mappingPopValues, dataset);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Mapping Genotype");
		} catch (Throwable th){
			throw new GDMSException("Error uploading Mapping Genotype", th);
		}
	}

	
	protected void saveMappingAllelicSSRDArT() throws GDMSException {
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		genotypicDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
        genotypicDataManagerImpl.setSessionProviderForCentral(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral());
		try {
			genotypicDataManagerImpl.setMappingAllelicSSRDArT(dataset, datasetUser, mappingPop, listOfMPSSRDataRows);
			//genotypicDataManagerImpl.setMappingData(accMetadataSet, markerMetadataSet, datasetUser, mappingPop, mappingPopValues, dataset);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Mapping Genotype");
		} catch (Throwable th){
			throw new GDMSException("Error uploading Mapping Genotype", th);
		}
	}
	
	
	protected void saveMappingAllelicSNP() throws GDMSException {
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		genotypicDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
        genotypicDataManagerImpl.setSessionProviderForCentral(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral());
		try {
			genotypicDataManagerImpl.setMappingAllelicSNP(dataset, datasetUser, mappingPop, listOfMPSNPDataRows);
			//genotypicDataManagerImpl.setMappingData(accMetadataSet, markerMetadataSet, datasetUser, mappingPop, mappingPopValues, dataset);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Mapping Genotype");
		} catch (Throwable th){
			throw new GDMSException("Error uploading Mapping Genotype", th);
		}
	}
	
	

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}


	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		if (null != arrayOfMappingPopValues && arrayOfMappingPopValues.length > 0){
			String strUploadInfo = "";
			for (int i = 0; i < arrayOfMappingPopValues.length; i++){
				Integer iMpId = arrayOfMappingPopValues[i].getMpId();
				String strGID = String.valueOf(arrayOfMappingPopValues[i].getGid());
				String strMarkerId = String.valueOf(arrayOfMappingPopValues[i].getMarkerId());
				String strMappingGenotype = "Map: " + iMpId + " GID: " + strGID +
						" Marker-Id: " + strMarkerId;
				strUploadInfo += strMappingGenotype + "\n";
			}
			strDataUploaded = "Uploaded Mapping Genotype(s): \n" + strUploadInfo;
		}
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isValidDate(String inDate) {

		if (inDate == null)
			return false;

		//set the format to use as a constructor argument
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		if (inDate.trim().length() != dateFormat.toPattern().length())
			return false;

		dateFormat.setLenient(false);

		try {
			//parse the inDate parameter
			dateFormat.parse(inDate.trim());
		}
		catch (ParseException pe) {
			return false;
		}
		return true;
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
