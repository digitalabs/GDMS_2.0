package org.icrisat.gdms.upload.genotyping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

import org.generationcp.middleware.dao.UserDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
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
import org.generationcp.middleware.pojos.gdms.AlleleValues;
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.DartDataRow;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.DatasetUsers;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerInfo;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.SSRDataRow;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;

import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.common.GDMSModel;


import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;


public class SSRGenotype implements  UploadMarker {

	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	//private AccMetadataSet accMetadataSet;
	//private Marker marker;
	
	private Marker addedMarker;
	private DatasetBean dataset;
	private AccessionMetaDataBean accMetadataSet;
	private MarkerMetadataSet markerMetadataSet;
	private GenotypeUsersBean datasetUser;
	private IntArrayBean alleleValues;
	
	private Session localSession;
	private Session centralSession;
	
	private Session session;
	/*
	private Session sessionL;
	private Session sessionC;
	*/
	/*private Marker addedMarker; 
	private Dataset dataset;
	private MarkerMetadataSet markerMetadataSet;
	private DatasetUsers datasetUser;*/
	private Marker[] arrayOfMarkers;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	
	List<SSRDataRow> listOfSSRTDataRows; 
	int intDataOrderIndex =0;
	ManagerFactory factory =null;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	CheckNumericDatatype cnd = new CheckNumericDatatype();
	String notMatchingDataDB="";
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    String notMatchingGIDS="";
    int size=0;
    String ErrMsg="";
    String strMarkerType="SSR";
    
    int iDatasetId = 0;
    int intRMarkerId = 1;
    int maxMid=0;
	int mid=0;
    private Transaction tx;
    
	static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			workbook = Workbook.getWorkbook(new File(strFileLocation));
			strSheetNames = workbook.getSheetNames();
		} catch (BiffException e) {
			throw new GDMSException("Error Reading SSR Genotype Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading SSR Genotype Sheet - " + e.getMessage());
		}
	}

	@Override
	public void validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("SSR_Source")){
			throw new GDMSException("SSR_Source Sheet Name Not Found");
		}

		if (false == strSheetNames[1].equalsIgnoreCase("SSR_Data List")){
			throw new GDMSException("SSR_Data List Sheet Name Not Found");
		}

		//check the template fields in source sheet
		for(int i = 0; i < strSheetNames.length; i++){
			String strSheetName = strSheetNames[i].toString();
			if(strSheetName.equalsIgnoreCase("SSR_Source")){
				Sheet sName = workbook.getSheet(strSheetName);
				String strTempColumnNames[] = {"Institute", " Principle investigator", "Dataset Name", "Dataset description", "Genus", "Species", "Missing Data", "Remark"};
				for(int j = 0; j < strTempColumnNames.length; j++){
					String strColFromSheet = (String)sName.getCell(0, j).getContents().trim();
					if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException("Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException("Delete Empty Rows");
					}
				}															
			}


			//SSR_DataList fields validation
			if(strSheetName.equalsIgnoreCase("SSR_Data List")){
				Sheet sName = workbook.getSheet(strSheetName);
				String strTempColumnNames[] = {"GID", "Accession", "Marker", "Gel/Run", "Dye", "Called Allele", "Raw Data", "Quality", "Height", "Volume", "Amount"};
				for(int j = 0; j < strTempColumnNames.length; j++){
					String strColFromSheet = (String)sName.getCell(j, 0).getContents().trim();
					if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException("Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException("Delete Empty Columns");
					}
				}
			}
		}

		//check the required fields in SSR_Source;
		for(int i = 0; i < strSheetNames.length; i++){
			Sheet sName = workbook.getSheet(i);
			int intNoOfRows = sName.getRows();
			if(strSheetNames[i].equalsIgnoreCase("SSR_Source")){
				for(int j = 0; j < intNoOfRows; j++){
					String strFieldsName = sName.getCell(0, j).getContents().trim();
					if(strFieldsName.equalsIgnoreCase("Institute") || strFieldsName.equalsIgnoreCase("Dataset Name") || strFieldsName.equalsIgnoreCase("Dataset description") || strFieldsName.equalsIgnoreCase("genus") || strFieldsName.equalsIgnoreCase("missing data")){
						String strFieldValue = sName.getCell(1, j).getContents().trim();
						if(strFieldValue == null || strFieldValue == ""){
							throw new GDMSException("Please provide values for Required Fields");
						}
					}
				}
			}

			int intNoOfColumns = sName.getColumns();
			//GID, Accession, Marker and Amount fields from ssr_data list.
			if(strSheetNames[i].equalsIgnoreCase("SSR_Data List")){
				for(int col = 0; col < intNoOfColumns; col++) {
					String strFieldName = sName.getCell(col, 0).getContents().trim();
					if(strFieldName.equalsIgnoreCase("GID") || strFieldName.equalsIgnoreCase("Accession") || 
							strFieldName.equalsIgnoreCase("Marker") || strFieldName.equalsIgnoreCase("Amount")){
						for (int row = 1; row < intNoOfRows; row++){
							String strFieldValue = sName.getCell(col, row).getContents().trim();

							if(strFieldValue == null || strFieldValue == ""){
								throw new GDMSException("Please provide value for " + strFieldName + " at cell position [" + col + "," + row + "]");
							}

							if(strFieldName.equalsIgnoreCase("Amount")){
								float parseFloat = Float.parseFloat(strFieldValue);
								if (0 > parseFloat || 1 < parseFloat){
									throw new GDMSException("Amount value must be greater than 0 and less than 1 at cell position [" + col + "," + row + "]");
								}
							}
						}
					}
				}
			} // end Data List validation
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
		
		String strMissingData = sourceSheet.getCell(1, 6).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.MissingData.toString(), strMissingData);

		String strRemark = sourceSheet.getCell(1, 7).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Remark.toString(), strRemark);

		listOfDataInSourceSheet.add(hmOfDataInSourceSheet);


		Sheet dataSheet = workbook.getSheet(1);
		int iNumOfRowsInDataSheet = dataSheet.getRows();
		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();

		for (int i = 1; i < iNumOfRowsInDataSheet; i++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strGID = dataSheet.getCell(0, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.GID.toString(), strGID);

			String strAccession = dataSheet.getCell(1, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Accession.toString(), strAccession);

			String strMarker = dataSheet.getCell(2, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Marker.toString(), strMarker);

			String strGelOrRun = dataSheet.getCell(3, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.GelOrRun.toString(), strGelOrRun);

			String strDye = dataSheet.getCell(4, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Dye.toString(), strDye);

			String strCalledAllele = dataSheet.getCell(5, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.CalledAllele.toString(), strCalledAllele);

			String strRawData = dataSheet.getCell(6, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.RawData.toString(), strRawData);

			String strQuality = dataSheet.getCell(7, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Quality.toString(), strQuality);

			String strHeight = dataSheet.getCell(8, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Height.toString(), strHeight);

			String strVolume = dataSheet.getCell(9, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Volume.toString(), strVolume);

			String strAmount = dataSheet.getCell(10, i).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Amount.toString(), strAmount);

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

		String strColsInSourceTable[] = {UploadField.Institute.toString(), UploadField.PrincipleInvestigator.toString(), 
				UploadField.DatasetName.toString(), UploadField.DatasetDescription.toString(), 
				UploadField.Genus.toString(), UploadField.Species.toString(), 
				UploadField.MissingData.toString(), UploadField.Remark.toString()};
		
		HashMap<String, String> hmOfSourceColumnsAndValuesFromGUI = listOfDataRowsFromSourceTable.get(0);
		
		for(int j = 0; j < strColsInSourceTable.length; j++){
			String strCol = strColsInSourceTable[j];
			if (false == hmOfSourceColumnsAndValuesFromGUI.containsKey(strCol)){
				throw new GDMSException(strCol + " column not found in data SSR_Source sheet.");
			} else {
				//Institute, Dataset-Name, Dataset-Description, Genus, Missing-Data are required columns
				if (strCol.equalsIgnoreCase(UploadField.Institute.toString()) || strCol.equalsIgnoreCase(UploadField.DatasetName.toString()) ||
						strCol.equalsIgnoreCase(UploadField.DatasetDescription.toString()) || strCol.equalsIgnoreCase(UploadField.Genus.toString()) ||  
						strCol.equalsIgnoreCase(UploadField.MissingData.toString())) {
					String strValue = hmOfSourceColumnsAndValuesFromGUI.get(strCol);
					if (null == strValue || strValue.equals("")){
						throw new GDMSException("Please provide a value for " +  strCol + " column in data SSR_Source sheet.");
					}
				}
			}
		}

		String strColsInDataTable[] = {UploadField.GID.toString(), UploadField.Accession.toString(), UploadField.Marker.toString(), 
				UploadField.GelOrRun.toString(), UploadField.Dye.toString(), UploadField.CalledAllele.toString(), 
				UploadField.RawData.toString(), UploadField.Quality.toString(), UploadField.Height.toString(), 
				UploadField.Volume.toString(), UploadField.Amount.toString()};
		
		for (int i = 0; i < strColsInDataTable.length; i++){
			String strCol = strColsInDataTable[i];
			for (int j = 0; j < listOfDataRowsFromDataTable.size(); j++){
				HashMap<String, String> hmOfDataColumnsAndValuesFromGUI = listOfDataRowsFromDataTable.get(j);
				
				if (false == hmOfDataColumnsAndValuesFromGUI.containsKey(strCol)){
					throw new GDMSException(strCol + " column not found in data SSR_Data List table.");
				}else {
					//GID, Accession, Marker, Amount are required columns
					if (strCol.equalsIgnoreCase(UploadField.GID.toString()) || strCol.equalsIgnoreCase(UploadField.Accession.toString()) ||
							strCol.equalsIgnoreCase(UploadField.Marker.toString()) || strCol.equalsIgnoreCase(UploadField.Amount.toString())) {
						String strValue = hmOfDataColumnsAndValuesFromGUI.get(strCol);
						if (null == strValue || strValue.equals("")){
							throw new GDMSException("Please provide a value for " +  strCol + " column in data SSR_Data List table.");
						}
					}
				}
				if(strCol.equalsIgnoreCase(UploadField.Amount.toString())){
					String strValue = hmOfDataColumnsAndValuesFromGUI.get(strCol);
					float parseFloat = Float.parseFloat(strValue);
					if (0 > parseFloat || 1 < parseFloat){
						throw new GDMSException("Amount value must be greater than 0 and less than 1");
					}
				}
			}
		}
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {		
		ArrayList<Integer> listOfGIDsProvided = new ArrayList<Integer>();
		ArrayList<String> listOfGNamesProvided = new ArrayList<String>();		
		ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
		ArrayList<String> listOfMarkersProvided = new ArrayList<String>();
		HashMap<Integer, String> hashMapOfGIDandGName = new HashMap<Integer, String>();
		//HashMap<Integer, Integer> hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();		
		HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();	
		String curAcc ="";String preAcc = "";String strPreAmount="";
		
		/*localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		
		sessionL=localSession.getSessionFactory().openSession();	
        sessionC=centralSession.getSessionFactory().openSession();
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		manager = factory.getGermplasmDataManager();
		genoManager=factory.getGenotypicDataManager();	*/	
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
		
			manager = factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
	
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
		List<String> listGIDs = new ArrayList<String>();
		List<String> listGNames = new ArrayList<String>();
		
		//String species=sheetSource.getCell(1,5).getContents().trim();
		 String markersForQuery="";
         ArrayList markerList = new ArrayList();
		
		String strMarCheck = listOfDataRowsFromDataTable.get(0).get(UploadField.Marker.toString());
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
			//HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);			
			String strAmount=listOfDataRowsFromDataTable.get(i).get(UploadField.Amount.toString());
			float fltAmount = Float.parseFloat(strAmount);			
			if(strMarCheck == listOfDataRowsFromDataTable.get(i).get(UploadField.Marker.toString())){
				if((fltAmount == 0.0) || (fltAmount == 1.0)){					
					listGNames.add(listOfDataRowsFromDataTable.get(i).get(UploadField.Accession.toString()));
					listGIDs.add(listOfDataRowsFromDataTable.get(i).get(UploadField.GID.toString()));
						//System.out.println("Acc:01="+(String)sheetDataList.getCell(0,i).getContents().trim());
				}else{
					
					listGNames.add(listOfDataRowsFromDataTable.get(i).get(UploadField.Accession.toString()));
					listGIDs.add(listOfDataRowsFromDataTable.get(i).get(UploadField.GID.toString()));
						//System.out.println("contains="+(String)sheetDataList.getCell(0,i).getContents().trim());
					//}else{
					if(i>1){
						curAcc = listOfDataRowsFromDataTable.get(i).get(UploadField.GID.toString());
						preAcc = listOfDataRowsFromDataTable.get(i-1).get(UploadField.GID.toString());//(String)sheetDataList.getCell(0,i-1).getContents().trim();
						//String strPreAmount=(String)sheetDataList.getCell(9,i-1).getContents().trim();
						strPreAmount=listOfDataRowsFromDataTable.get(i-1).get(UploadField.Amount.toString());//(String)sheetDataList.getCell(10,i-1).getContents().trim().toString();
						//System.out.println("&&&&&&&&&&&&&&&&&&&&   strPreAmount="+strPreAmount);
					}else if (i==1){
						curAcc = listOfDataRowsFromDataTable.get(i).get(UploadField.GID.toString());
						preAcc = listOfDataRowsFromDataTable.get(i+1).get(UploadField.GID.toString());
						//String strPreAmount=(String)sheetDataList.getCell(9,i-1).getContents().trim();
						strPreAmount=listOfDataRowsFromDataTable.get(i+1).get(UploadField.Amount.toString());
						
					}
						double fltPreAmount = Float.parseFloat(strPreAmount);
													
						int fltA=0;
							for(int r=1;r<25;r++){
								double f = fltAmount*r;
								
								//MaxIdValue rt = new MaxIdValue();
								double fltRB=roundThree(f);
								if((fltRB>=0.900 && fltRB<=0.999))
									fltRB=Math.round(f);
								
								if(fltRB==1.000){
									//System.out.println("fltRB==1.000="+fltRB+"Rvalue="+r);
									fltA=r;
									r=25;
								}
							}							
						if(fltA!=0){
							i=i+fltA-1;								
						}					
				}	
			}else{					
				//strMarCheck=(String) sheetDataList.getCell(1,i).getContents().trim();
				strMarCheck=listOfDataRowsFromDataTable.get(i).get(UploadField.Marker.toString());
				i=i-1;
			}
			if(!markerList.contains(strMarCheck))
				markerList.add(strMarCheck);
			
		}
		
			/*for(int m1=1;m1<intDataListRowCount;m1++){
				//markersForQuery=markersForQuery+"'"+sheetDataList.getCell(2,m1).getContents().trim()+"',";
				if(!markerList.contains(sheetDataList.getCell(2,m1).getContents().trim()))
					markerList.add(sheetDataList.getCell(2,m1).getContents().trim());
			}*/
		/*Sheet sheetSource = workbook.getSheet(0);
		Sheet sheetDataList = workbook.getSheet(1);
		int intDataListRowCount=sheetDataList.getRows();*/
		/*List<String> listGIDs = new ArrayList<String>();
		List<String> listGNames = new ArrayList<String>();
		String strMarCheck = (String) sheetDataList.getCell(2,1).getContents().trim();*/
		//System.out.println("4:"+sheetSource.getCell(1,4).getContents()+"     5:"+sheetSource.getCell(1,5).getContents());
		
				
		String gidsString="";
		ArrayList gidsList = new ArrayList();
		ArrayList gnamesList = new ArrayList();
		for(int g1=0;g1<listGIDs.size();g1++){
			if(!gidsList.contains(listGIDs.get(g1)))
				gidsList.add(listGIDs.get(g1));
		}
		//System.out.println("listGIDs="+listGIDs);
		//System.out.println("gidsList="+gidsList);
		for(int g2=0;g2<listGNames.size();g2++){
			if(!gnamesList.contains(listGNames.get(g2)))
				gnamesList.add(listGNames.get(g2));
		}
		//System.out.println(gnamesList.size()+"    "+gidsList.size());
		int gCount=gnamesList.size();
		int gidCount=gidsList.size();
		if(gidCount<gCount){
			throw new GDMSException("The number of GIDs is less than the number of Germplasm names provided");
		}else if(gCount<gidCount){
			throw new GDMSException("The number of GIDs is more than the number of Germplasm names provided");
			
		}
		
		
			//String gidsForQuery = "";
			ArrayList gidsForQuery=new ArrayList();
			String gNames="";
			HashMap<Integer, String> GIDsMap = new HashMap<Integer, String>();
			HashMap<String, Integer> GIDsMap1 = new HashMap<String, Integer>();
			ArrayList gidNamesList=new ArrayList();
			 //SortedMap GIDsMap = new TreeMap();
            for(int d=0;d<gidsList.size();d++){	               
            	//gidsForQuery = gidsForQuery + gidsList.get(d)+",";
            	
            	gNames=gNames+"'"+gnamesList.get(d).toString()+"',";
            	if(!gidNamesList.contains(Integer.parseInt(gidsList.get(d).toString())))
					gidNamesList.add(Integer.parseInt(gidsList.get(d).toString())+","+gnamesList.get(d).toString());
            	
            	
            	GIDsMap.put((Integer.parseInt(gidsList.get(d).toString())), gnamesList.get(d).toString());
            	GIDsMap1.put(gnamesList.get(d).toString(),(Integer.parseInt(gidsList.get(d).toString())));
            }
            
            Map<Object, String> sortedMap = new TreeMap<Object, String>(GIDsMap);
           
            SortedMap gidsmap = new TreeMap();
            List lstgermpName = new ArrayList();
            
			List<Name> names = null;
		
            ArrayList gidsDBList = new ArrayList();
			ArrayList gNamesDBList = new ArrayList();
			hashMap.clear();
			for(int n=0;n<gnamesList.size();n++){
				try{
				List<Germplasm> germplasmList = manager.getGermplasmByName(gnamesList.get(n).toString(), 0, new Long(manager.countGermplasmByName(gnamesList.get(n).toString(), Operation.EQUAL)).intValue(), Operation.EQUAL);
				for (Germplasm g : germplasmList) {
					//System.out.println("Checking  ==:"+g.getGid()+"   "+gnamesList.get(n));
				
		        	if(!(gidsDBList.contains(g.getGid()))){
		        		gidsDBList.add(g.getGid());
		        		gNamesDBList.add(gnamesList.get(n).toString());
		        		addValues(gnamesList.get(n).toString(), g.getGid());					        		
		        	}				        	
		          
		        }
				} catch (MiddlewareQueryException e1) {
					throw new GDMSException(e1.getMessage());
				}
			}
          
           if(gNamesDBList.size()==0){
        	   alertGID="yes";
        	   size=0;
           }
            
          
          // System.out.println(markerList.size()+"  markers="+markerList);
           for(int ml=0;ml<markerList.size();ml++){
        	   markersForQuery=markersForQuery+"'"+markerList.get(ml)+"',";
           }
           markersForQuery=markersForQuery.substring(0, markersForQuery.length()-1);
           List newListL=new ArrayList();
			List newListC=new ArrayList();
			//try {	
			Object obj=null;
			Object objL=null;
			Iterator itListC=null;
			Iterator itListL=null;
			//genoManager.getMar
			
			List lstMarkers = new ArrayList();
			HashMap<String, Object> markersMap = new HashMap<String, Object>();	
           String strQuerry="select distinct marker_id, marker_name from gdms_marker where Lower(marker_name) in ("+markersForQuery.toLowerCase()+")";
			
			//sessionC=centralSession.getSessionFactory().openSession();			
			SQLQuery queryC=centralSession.createSQLQuery(strQuerry);	
			queryC.addScalar("marker_id",Hibernate.INTEGER);	
			queryC.addScalar("marker_name",Hibernate.STRING);	
			newListC=queryC.list();			
			itListC=newListC.iterator();			
			while(itListC.hasNext()){
				obj=itListC.next();
				if(obj!=null){	
					Object[] strMareO= (Object[])obj;
		        	//System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
					lstMarkers.add(strMareO[1].toString());
					markersMap.put(strMareO[1].toString(), strMareO[0]);
					
				}
			}
					

			//sessionL=localSession.getSessionFactory().openSession();			
			SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
			queryL.addScalar("marker_id",Hibernate.INTEGER);	
			queryL.addScalar("marker_name",Hibernate.STRING);	       
			newListL=queryL.list();
			itListL=newListL.iterator();			
			while(itListL.hasNext()){
				objL=itListL.next();
				if(objL!=null)	{			
					Object[] strMareO= (Object[])objL;
					//System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
					if(!lstMarkers.contains(strMareO[1].toString())){
	            		lstMarkers.add(strMareO[1].toString());	            		
	            		markersMap.put(strMareO[1].toString(), strMareO[0]);	
					}
				}
			}
			
           
           
           
           int gidToCompare=0;
           String gNameToCompare="";
          // String gNameFromMap="";
           ArrayList gNameFromMap=new ArrayList();
           //System.out.println("gidNamesList="+gidNamesList);
           
           if(gNamesDBList.size()>0){	           
	           for(int n=0;n<gnamesList.size();n++){
        		   if(gNamesDBList.contains(gnamesList.get(n))){
        			   if(!(hashMap.get(gnamesList.get(n).toString()).contains(GIDsMap1.get(gnamesList.get(n).toString())))){
        				   notMatchingData=notMatchingData+gnamesList.get(n)+"   "+GIDsMap1.get(gnamesList.get(n).toString())+"\n\t";
        				   notMatchingDataDB=notMatchingDataDB+gnamesList.get(n)+"="+hashMap.get(gnamesList.get(n))+"\t";
		        		   alertGN="yes";
        			   }
        		   }else{
        			   //int gid=GIDsMap.get(gnamesList.get(n).toString());
        			   alertGID="yes";
	        		   size=hashMap.size();
	        		   notMatchingGIDS=notMatchingGIDS+gnamesList.get(n).toString()+", ";
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
        		   //ErrMsg = "The GIDs provided do not exist in the database. \n Please upload the relevant germplasm information to the GMS ";
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
          		
           SortedMap map = new TreeMap();
           SortedMap finalMarkersMap = new TreeMap();
          
	
		SortedMap hashMapOfGIDsandNIDs = new TreeMap();
		//System.out.println(",,,,,,,,,,,,,,,,,gNames="+gNames);
		ArrayList finalList =new ArrayList();
		ArrayList gidL=new ArrayList();
		
		Name name = null;
		
		for(int n=0;n<gidsList.size();n++){
			try{
				name = manager.getNameByGIDAndNval(Integer.parseInt(gidsList.get(n).toString()), gnamesList.get(n).toString(), GetGermplasmByNameModes.STANDARDIZED);
				if(name==null){
					name=manager.getNameByGIDAndNval(Integer.parseInt(gidsList.get(n).toString()), gnamesList.get(n).toString(), GetGermplasmByNameModes.NORMAL);
				}
				if(!gidL.contains(name.getGermplasmId()))
	            	gidL.add(name.getGermplasmId());
				hashMapOfGIDsandNIDs.put(name.getGermplasmId(), name.getNid());
			} catch (MiddlewareQueryException e1) {
				throw new GDMSException(e1.getMessage());
			}
		}
		//System.out.println("mapN=:"+mapN);
       
        for(int a=0;a<gidsList.size();a++){
        	int gid1=Integer.parseInt(gidsList.get(a).toString());
        	if(gidL.contains(gid1)){
        		finalList.add(gid1+"~!~"+hashMapOfGIDsandNIDs.get(gid1));	
        	}
        }
        //System.out.println("******************  "+finalList);
		
        HashMap<String, String> hmOfSourceFieldsAndValues = listOfDataRowsFromSourceTable.get(0);
		String strInstitute = hmOfSourceFieldsAndValues.get(UploadField.Institute.toString());
		String strPrincipleInvestigator = hmOfSourceFieldsAndValues.get(UploadField.PrincipleInvestigator.toString());
		String strDatasetName = hmOfSourceFieldsAndValues.get(UploadField.DatasetName.toString());
		if(strDatasetName.length()>30){
			ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException(ErrMsg); 
		}
		
		String strDatasetDescription = hmOfSourceFieldsAndValues.get(UploadField.DatasetDescription.toString());
		String strGenus = hmOfSourceFieldsAndValues.get(UploadField.Genus.toString());
		String strSpecies = hmOfSourceFieldsAndValues.get(UploadField.Species.toString());
		String strMissingData = hmOfSourceFieldsAndValues.get(UploadField.MissingData.toString());
		String strRemark = hmOfSourceFieldsAndValues.get(UploadField.Remark.toString());

		//Creating the DatasetUsers object first
		Integer iUserId = 0;
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

		} else {*/
			UserDAO userDAO = new UserDAO();
			userDAO.setSession(localSession);
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

		try{
			List<DatasetElement> results =genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.CENTRAL);
			if(results.isEmpty()){			
				results =genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.LOCAL);
				if(results.size()>0)
					throw new GDMSException("Dataset Name already exists.");
			}else 
				throw new GDMSException("Dataset Name already exists.");
		
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		if(strDatasetName.length()>30){
			//ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException("Dataset Name value exceeds max char size.");
		}
		
		int intAnID=0;
		Integer iMarkerId = 0;
		String strDatasetType = "SSR";
		String strDataType = "int";
		Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
		String strRemarks = ""; 
		String strMethod = null;
		String strScore = null;
		int iUploadedMarkerCount = 0;
		int iNumOfMarkers = listOfMarkersProvided.size();
		arrayOfMarkers = new Marker[iNumOfMarkers];
		Integer iNewDatasetId = null; //Test class says iNewDatasetId must be zero

		long datasetLastId = 0;
		 long lastId = 0;
		 
		//int iNumOfMarkers = listOfMarkerNamesFromSourceTable.size();
		//int iNumOfGIDs = listOfGIDFromTable.size();
		//arrayOfMarkers = new Marker[iNumOfMarkers*iNumOfGIDs];
		int iDatasetId = 0;
		 tx=localSession.beginTransaction();
		 
		 /** retrieving maximum marker id from 'marker' table of database **/
			try{
				lastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MARKER);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			maxMid=(int)lastId; 
		 
			String mon="";
			Calendar cal = new GregorianCalendar();
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			if(month>=10) 
				mon=String.valueOf(month+1);
			else 
				mon="0"+(month+1);
			  
			 String curDate=year+"-"+mon+"-"+day;
			
			 
			 try{
				 datasetLastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_DATASET);
				}catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				int intDatasetId=(int)datasetLastId;
				
				iDatasetId=intDatasetId-1;
		 
				long lastIdMPId=0;
				try{
					lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_ALLELE_VALUES);
				}catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				int maxCHid=(int)lastIdMPId;
				intAnID=maxCHid-1;
		 
		 
		
		
		dataset = new DatasetBean();
		dataset.setDataset_id(iDatasetId);
		dataset.setDataset_name(strDatasetName);
		dataset.setDataset_desc(strDatasetDescription);
		dataset.setDataset_type(strDatasetType);
		dataset.setGenus(strGenus);
		dataset.setSpecies(strSpecies);
		dataset.setUpload_template_date(curDate);	
		dataset.setRemarks(strRemarks);
		dataset.setDatatype(strDataType);
		dataset.setMissing_data(strMissingData);
		localSession.save(dataset);
		
		datasetUser = new GenotypeUsersBean();
		datasetUser.setDataset_id(iDatasetId);
		datasetUser.setUser_id(iUserId);
		localSession.save(datasetUser);
		
		for(int a=0;a<finalList.size();a++){	
        	String[] strList=finalList.get(a).toString().split("~!~");
        	accMetadataSet=new AccessionMetaDataBean();					
			//******************   GermplasmTemp   *********************//*	
        	accMetadataSet.setDataset_id(iDatasetId);
        	accMetadataSet.setGid(Integer.parseInt(strList[0].toString()));
        	accMetadataSet.setNid(Integer.parseInt(strList[1].toString()));
			
        	localSession.save(accMetadataSet);
			
			if (a % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
        
        }
		 ArrayList mids=new ArrayList();
         
         HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
       
		for(int f=0; f<markerList.size();f++){
			MarkerInfoBean mib=new MarkerInfoBean();
			if(lstMarkers.contains(markerList.get(f))){
				intRMarkerId=(Integer)(markersMap.get(markerList.get(f)));							
				mids.add(intRMarkerId);
				finalHashMapMarkerAndIDs.put(markerList.get(f).toString(), intRMarkerId);
			}else{
				//maxMid=maxMid+1;
				maxMid=maxMid-1;
				intRMarkerId=maxMid;
				finalHashMapMarkerAndIDs.put(markerList.get(f).toString(), intRMarkerId);
				mids.add(intRMarkerId);	
				mib.setMarkerId(intRMarkerId);
				mib.setMarker_type("SNP");
				mib.setMarker_name(markerList.get(f).toString());
				//mib.setCrop(sheetSource.getCell(1,5).getContents());
				mib.setSpecies(strSpecies);
				
				localSession.save(mib);
				if (f % 1 == 0){
					localSession.flush();
					localSession.clear();
				}
			}
			
			
		}
		
		//listOfSSRTDataRows = new ArrayList<SSRDataRow>();
		
		String marker="";
		int m=0;				
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
			//System.out.println(".................."+listOfDataRowsFromDataTable.get(i));
			//MarkerInfoBean mib=new MarkerInfoBean();
			//marker=sheetDataList.getCell(2,i).getContents().trim();					
			marker=listOfDataRowsFromDataTable.get(i).get(UploadField.Marker.toString());
			String strGID = listGIDs.get(m);
						
			//AccMetadataSet
			int iGID = Integer.parseInt(strGID);				
			
			
			alleleValues = new IntArrayBean();
			IntArrayCompositeKey cack = new IntArrayCompositeKey();
			
			//**************** writing to char_values tables........
			cack.setDataset_id(iDatasetId);
			cack.setAn_id(intAnID);
			alleleValues.setComKey(cack);
						
			alleleValues.setGid(Integer.parseInt(strGID));			
			
			
			String strV = listOfDataRowsFromDataTable.get(i).get(UploadField.CalledAllele.toString());//(String)sheetDataList.getCell(5,i).getContents().trim();
			String strRV = listOfDataRowsFromDataTable.get(i).get(UploadField.RawData.toString());//(String)sheetDataList.getCell(6,i).getContents().trim();
			String strAmountVal = listOfDataRowsFromDataTable.get(i).get(UploadField.Amount.toString());//(String)sheetDataList.getCell(10,i).getContents().trim();
			int intAlleleBinValues = 0;
			float intAlleleRawValues = 0;
			if(cnd.isInteger(strV)){
				intAlleleBinValues = Integer.parseInt(listOfDataRowsFromDataTable.get(i).get(UploadField.CalledAllele.toString()));
			}else{
				String str=listOfDataRowsFromDataTable.get(i).get(UploadField.CalledAllele.toString());
				/*if(str.equalsIgnoreCase("?")){
					intAlleleBinValues=999999999;
				}else{
					intAlleleBinValues=88888888;
				}*/
			}
			
			if(cnd.isFloat(strRV)){
				intAlleleRawValues = Float.parseFloat(listOfDataRowsFromDataTable.get(i).get(UploadField.RawData.toString()));
			}else{
				String str=listOfDataRowsFromDataTable.get(i).get(UploadField.RawData.toString());
				/*if(str.equalsIgnoreCase("?")){
					intAlleleRawValues=999999999;
				}else{
					intAlleleRawValues=88888888;
				}*/
			}
			//check the amount value and insert the data into database 
			//without using amount value
			
				if((strAmountVal.equals("1"))||(strAmountVal.equals("0"))){					
					String strValue = intAlleleBinValues+"/"+intAlleleBinValues;
					String strRValue = intAlleleRawValues+"/"+intAlleleRawValues;
					
					
					alleleValues.setAllele_bin_value(strValue);					
					alleleValues.setAllele_raw_value(strRValue);												
				}else{
					
					String strValue1="";
					String strRValue1="";
					
					//amout value 
					String strA = listOfDataRowsFromDataTable.get(i).get(UploadField.Amount.toString());//(String)sheetDataList.getCell(10,i).getContents().trim();
					int intAmoutVal = 0;
					for(int l=1;l<17;l++){
						Float val;
						val = Float.parseFloat(strA) * l;
						if(val >= 0.9){
							intAmoutVal = l;
							break;
						}
					}
					intAmoutVal = intAmoutVal +i;
					//System.out.println("....intAmoutVal:"+intAmoutVal);
					for(int n=i;n<intAmoutVal;n++){
						String strV1 = listOfDataRowsFromDataTable.get(i).get(UploadField.CalledAllele.toString());
						String strRV1 = listOfDataRowsFromDataTable.get(i).get(UploadField.RawData.toString());
						int intAlleleBinValues1 = 0;
						float intAlleleRawValues1 = 0;
						if(cnd.isInteger(strV1)){
							intAlleleBinValues1 = Integer.parseInt(listOfDataRowsFromDataTable.get(i).get(UploadField.CalledAllele.toString()));
						}else{
							String str=listOfDataRowsFromDataTable.get(i).get(UploadField.CalledAllele.toString());
							/*if(str.equalsIgnoreCase("?")){
								intAlleleBinValues1=999999999;
							}else{
								intAlleleBinValues1=88888888;
							}*/
						}
						
						if(cnd.isFloat(strRV1)){
							intAlleleRawValues1 = Float.parseFloat(listOfDataRowsFromDataTable.get(i).get(UploadField.RawData.toString()));
						}else{
							String str=listOfDataRowsFromDataTable.get(i).get(UploadField.RawData.toString());
							/*if(str.equalsIgnoreCase("?")){
								intAlleleRawValues1=999999999;
							}else{
								intAlleleRawValues1=88888888;
							}*/
						}			
						
						strValue1 = strValue1+intAlleleBinValues1+"/";
						strRValue1 = strRValue1+intAlleleRawValues1+"/";
						i++;
						//marker=sheetDataList.getCell(2,n).getContents().trim();
						marker=listOfDataRowsFromDataTable.get(i).get(UploadField.Marker.toString());
					}
					i--;
					
					//System.out.println(".............:"+strValue1+"   "+(strValue1.length()-1)+"   "+marker);
					
					
					strValue1=strValue1.substring(0, strValue1.length()-1);
					strRValue1=strRValue1.substring(0, strRValue1.length()-1);	
					
					alleleValues.setAllele_bin_value(strValue1);					
					alleleValues.setAllele_raw_value(strRValue1);	
					
				/*	alleleValues.setAlleleBinValue(strValue1);				
					alleleValues.setAlleleRawValue(strRValue1);			*/			
				}
				//intDataOrderIndex++;
				//intDataOrderIndex--;
				alleleValues.setMarker_id(Integer.parseInt(finalHashMapMarkerAndIDs.get(marker).toString()));
				localSession.save(alleleValues);
				
				if (i % 1 == 0){
					localSession.flush();
					localSession.clear();
				}
				intAnID--;
				m++;
									
				//SSRDataRow> listOfSSRTDataRows
				/*SSRDataRow ssrDataRow = new SSRDataRow(addedMarker, accMetadataSet, markerMetadataSet, alleleValues);
				listOfSSRTDataRows.add(ssrDataRow);*/
				
		}
		
		for(int m1=0;m1<mids.size();m1++){					
			//System.out.println("gids doesnot Exists    :"+lstgermpName+"   "+gids[l]);
			MarkerMetaDataBean mdb=new MarkerMetaDataBean();					
			//******************   GermplasmTemp   *********************//*	
			mdb.setDataset_id(iDatasetId);
			mdb.setMarker_id(Integer.parseInt(mids.get(m1).toString()));
			
			localSession.save(mdb);
			if (m1 % 1 == 0){
				localSession.flush();
                localSession.clear();
			}			
		}
			
		//saveSSRGenotype();
				
		tx.commit();
		//}
		
		
	}

	/*private void saveSSRGenotype() throws GDMSException {
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		genotypicDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
		genotypicDataManagerImpl.setSessionProviderForCentral(null);
		 
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		genoManager=factory.getGenotypicDataManager();
		try {
			genoManager.setSSR(dataset, datasetUser, listOfSSRTDataRows);
			//genoManager.setSSR(accMetadataSet, markerMetadataSet, datasetUser, alleleValues, dataset);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading SSR Genotype");
		} catch (Throwable th){
			throw new GDMSException("Error uploading SSR Genotype", th);
		}
	}*/

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		//if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";

			/*for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strGID = arrayOfMarkers[i].getDbAccessionId();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName + " GID: " + strGID;
				strUploadInfo += strMarker + "\n";
			}*/
			strDataUploaded = "Uploaded SSR Genotype ";
		//}
		
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
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
	public double roundThree(double in){		
		return Math.round(in*1000.0)/1000.0;
	}
	
}
