package org.icrisat.gdms.upload.maporqtl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.dao.UserDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.generationcp.middleware.pojos.gdms.QtlDataRow;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.genotyping.DatasetBean;
import org.icrisat.gdms.upload.genotyping.GenotypeUsersBean;
import org.icrisat.gdms.upload.genotyping.QTLBean;
import org.icrisat.gdms.upload.genotyping.QTLDetailsBean;
import org.icrisat.gdms.upload.marker.UploadField;

public class QTLUpload implements UploadMarker {

	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private GenotypeUsersBean datasetUser;
	private DatasetBean dataset;
	private QTLDetailsBean qtlDetails;
	private QTLBean qtl;
	
	//private Qtl[] arrayOfQTLs;
	
	
	private Session localSession;
	private Session centralSession;
	
	//private Session session;
	private Transaction tx;
	/*
	private Session sessionL;
	private Session sessionC;
*/
	ManagerFactory factory;
    GenotypicDataManager genoManager;
    OntologyDataManager om;
    List<QtlDataRow> listOfQTLDataRows; 
    WorkbenchDataManager  workbenchManager;
    Integer mapId =0;
    int qtlId=0;
    Integer iDatasetId = null; //Will be set/overridden by the function
	
	String strDatasetType = "QTL";
	Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
	String strDataType = "int"; 
	String strMissingData = null;
	String strMethod = null;
	String strScore = null;
	SortedMap mapTraits = new TreeMap();
	//First checking for the Map Name provided exists in the Database (gdms_map table) or not
	String strMapNameFromTemplate = "";
    ArrayList traitsList=new ArrayList();
    List traitsQList=new ArrayList();
    
    long lastId =0;
    String traits="";
    
    SQLQuery query;
    
    ArrayList traitsComList=new ArrayList();
    
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			workbook = Workbook.getWorkbook(new File(strFileLocation));
			strSheetNames = workbook.getSheetNames();
		} catch (BiffException e) {
			throw new GDMSException("Error Reading QTL Upload Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading QTL Upload Sheet - " + e.getMessage());
		}
	}

	@Override
	public void validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("qtl_source")){
			throw new GDMSException("QTL_Source Sheet Name Not Found");
		}

		if (false == strSheetNames[1].equalsIgnoreCase("qtl_data")){
			throw new GDMSException("QTL_DataList Sheet Name Not Found");
		}


		//check the template fields
		for(int i = 0; i < strSheetNames.length; i++){

			String strSName = strSheetNames[i].toString();

			if(strSName.equalsIgnoreCase("QTL_Source")){

				Sheet qtlSourceSheet = workbook.getSheet(strSName);
				//String strArrayOfReqColNames[] = {"Institute", "Principle investigator", "Dataset Name", "Dataset description", "Genus", "Species", "Remark"};
				String strArrayOfReqColNames[] = {"Institute", "Principle investigator", "Dataset Name", "Dataset description", "Genus", "Method", "Score", "Species", "Remark"};

				for(int j = 0; j < strArrayOfReqColNames.length; j++){
					String strColNameFromTemplate = (String)qtlSourceSheet.getCell(0, j).getContents().trim();
					if(!strArrayOfReqColNames[j].toLowerCase().contains(strColNameFromTemplate.toLowerCase())){
						throw new GDMSException("Column " + strArrayOfReqColNames[j].toLowerCase() + " not found");
					}
					if(strColNameFromTemplate == null || strColNameFromTemplate == ""){
						throw new GDMSException("Delete empty column " + strColNameFromTemplate);
					}
				}	

				//After checking for the required columns, have  to verify if the values have been provided for
				//all the required columns.
				//That is value at cell positon(1, n) should not be null or empty

				//Checking for value at Row#:0 Institute
				String strInstitue = qtlSourceSheet.getCell(1, 0).getContents().trim().toString();
				if (null == strInstitue){
					throw new GDMSException("Please provide the value for Institute at position (1, 0) in QTL_Source sheet of the template.");
				} else if (strInstitue.equals("")){
					throw new GDMSException("Please provide the value for Institute at position (1, 0) in QTL_Source sheet of the template.");
				}

				//Checking for value at Row#:2 Dataset Name
				String strDatasetName = qtlSourceSheet.getCell(1, 2).getContents().trim().toString();
				if (null == strDatasetName){
					throw new GDMSException("Please provide the value for Dataset Name at position (1, 2) in QTL_Source sheet of the template.");
				} else if (strDatasetName.equals("")){
					throw new GDMSException("Please provide the value for Dataset Name at position (1, 2) in QTL_Source sheet of the template.");
				}

				//Checking for value at Row#:3 Dataset Description
				String strDatasetDescription = qtlSourceSheet.getCell(1, 3).getContents().trim().toString();
				if (null == strDatasetDescription){
					throw new GDMSException("Please provide the value for Dataset Description at position (1, 3) in QTL_Source sheet of the template.");
				} else if (strDatasetDescription.equals("")){
					throw new GDMSException("Please provide the value for Dataset Description at position (1, 3) in QTL_Source sheet of the template.");
				}

				//Checking for value at Row#:4 Genus
				String strGenus = qtlSourceSheet.getCell(1, 4).getContents().trim().toString();
				if (null == strGenus){
					throw new GDMSException("Please provide the value for Genus at position (1, 4) in QTL_Source sheet of the template.");
				} else if (strGenus.equals("")){
					throw new GDMSException("Please provide the value for Genus at position (1, 4) in QTL_Source sheet of the template.");
				}

			}

			//SSR_DataList fields validation
			if(strSName.equalsIgnoreCase("QTL_Data")){

				Sheet sheetQTLData = workbook.getSheet(strSName);
				/*String strArrayOfRequiredColumnNames[] = {"Name", "Chromosome", "Map-Name", "Position", "Pos-Min",
						"Pos-Max", "Trait-ID", "Experiment", "CLEN", "LFM",
						"RFM", "Effect", "LOD", "R2", "Interactions"};*/
				String strArrayOfRequiredColumnNames[] = {"Name", "Chromosome", "Map-Name", "Position", "Pos-Min",
						"Pos-Max", "Trait", "Experiment", "CLEN", "LFM",
						"RFM", "Effect", "SE additive", "High value parent", "High value allele", "Low value parent", "Low value allele", "Score (e.g., LOD/-log10 (p))", "R2", "Interactions"};

				for(int j = 0; j < strArrayOfRequiredColumnNames.length; j++){
					String strColNamesFromDataSheet = (String)sheetQTLData.getCell(j, 0).getContents().trim();
					if(!strArrayOfRequiredColumnNames[j].toLowerCase().contains(strColNamesFromDataSheet.toLowerCase())){
						throw new GDMSException("column " + strColNamesFromDataSheet + " not found.");
					}
					if(strColNamesFromDataSheet == null || strColNamesFromDataSheet == ""){
						throw new GDMSException("Delete column " + strColNamesFromDataSheet);
					}
				}


				int iNumOfRows = sheetQTLData.getRows();

				for (int r = 1; r < iNumOfRows; r++){

					//0 --- Name	
					String strName = sheetQTLData.getCell(0, r).getContents().trim().toString();
					if (strName.equals("")){
						String strErrMsg = "Please provide value in Name column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//1 --- Chromosome	
					String strChromosome = sheetQTLData.getCell(1, r).getContents().trim().toString();
					if (strChromosome.equals("")){
						String strErrMsg = "Please provide value in Chromosome column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//2 --- Map-Name	
					String strMapName = sheetQTLData.getCell(2, r).getContents().trim().toString();
					if (strMapName.equals("")){
						String strErrMsg = "Please provide value in Map-Name column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//3 --- Position	
					String strPosition = sheetQTLData.getCell(3, r).getContents().trim().toString();
					if (strPosition.equals("")){
						String strErrMsg = "Please provide value in Position column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//4 --- Pos-Min	
					String strMinPos = sheetQTLData.getCell(4, r).getContents().trim().toString();
					if (strMinPos.equals("")){
						String strErrMsg = "Please provide value in Pos-Min column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//5 --- Pos-Max	
					String strMaxPos = sheetQTLData.getCell(5, r).getContents().trim().toString();
					if (strMaxPos.equals("")){
						String strErrMsg = "Please provide value in Pos-Max column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//6 --- Trait	
					String strTrait = sheetQTLData.getCell(6, r).getContents().trim().toString();
					
					if(!traitsList.contains(strTrait)){
						traitsList.add(strTrait);
						traits=traits+"'"+strTrait+"',";
					}
					if (strTrait.equals("")){
						String strErrMsg = "Please provide value in Trait-ID column at row:" + r;
						throw new GDMSException(strErrMsg);
					} /*else {
						try {
							Integer.parseInt(strTrait);
						} catch (NumberFormatException nfe){
							String strErrMsg = "Please provide a valid numeric value in Trait-ID column at row:" + r;
							throw new GDMSException(strErrMsg);
						}
					}*/
					

					//7 --- Experiment	
					String strExperiment = sheetQTLData.getCell(7, r).getContents().trim().toString();
					/*if (strExperiment.equals("")){
						String strErrMsg = "Please provide value in Experiment column at row:" + r;
						throw new GDMSException(strErrMsg);
					}*/

					//9 --- LFM	
					String strLFM = sheetQTLData.getCell(9, r).getContents().trim().toString();
					if (strLFM.equals("")){
						String strErrMsg = "Please provide value in LFM column at row:" + r;
						throw new GDMSException(strErrMsg);
					}


					//10 --- RFM	
					String strRFM = sheetQTLData.getCell(10, r).getContents().trim().toString();
					if (strRFM.equals("")){
						String strErrMsg = "Please provide value in RFM column at row:" + r;
						throw new GDMSException(strErrMsg);
					}


					//11 --- Effect	
					String strEffect = sheetQTLData.getCell(11, r).getContents().trim().toString();
					if (strEffect.equals("")){
						String strErrMsg = "Please provide value in Effect column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//12 --- LOD	
					String strLOD = sheetQTLData.getCell(12, r).getContents().trim().toString();
					/*if (strLOD.equals("")){
						String strErrMsg = "Please provide value in LOD column at row:" + r;
						throw new GDMSException(strErrMsg);
					}*/

					//13 --- R2
					String strR2 = sheetQTLData.getCell(13, r).getContents().trim().toString();
					if (strR2.equals("")){
						String strErrMsg = "Please provide value in R2 column at row:" + r;
						throw new GDMSException(strErrMsg);
					}
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

		String strMethod = sourceSheet.getCell(1, 5).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Method.toString(), strMethod);
		
		String strScore = sourceSheet.getCell(1, 6).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Score.toString(), strScore);
		
		String strSpecies = sourceSheet.getCell(1, 7).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Species.toString(), strSpecies);

		String strRemark = sourceSheet.getCell(1, 8).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Remark.toString(), strRemark);

		listOfDataInSourceSheet.add(hmOfDataInSourceSheet);

		//CloneID, MarkerName, Q, Reproducibility, Call Rate, PIC, Discordance followed by Marker-Names	
		Sheet dataSheet = workbook.getSheet(1);
		int iNumOfRowsInDataSheet = dataSheet.getRows();
		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();

		for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strName = dataSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Name.toString(), strName);

			String strChromosome = dataSheet.getCell(1, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Chromosome.toString(), strChromosome);

			String strMapName = dataSheet.getCell(2, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.MapName.toString(), strMapName);

			String strPosition = dataSheet.getCell(3, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Position.toString(), strPosition);

			String strPosMin = dataSheet.getCell(4, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.PosMin.toString(), strPosMin);

			String strPosMax = dataSheet.getCell(5, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.PosMax.toString(), strPosMax);

			String strTrait = dataSheet.getCell(6, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.TraitID.toString(), strTrait);

			String strExperiment = dataSheet.getCell(7, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Experiment.toString(), strExperiment);

			String strCLEN = dataSheet.getCell(8, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Clen.toString(), strCLEN);

			String strLFM = dataSheet.getCell(9, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.LFM.toString(), strLFM);

			String strRFM = dataSheet.getCell(10, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.RFM.toString(), strRFM);

			String strEffect = dataSheet.getCell(11, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Effect.toString(), strEffect);
			
			String strSEAdditive = dataSheet.getCell(12, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.additive.toString(), strSEAdditive);

			String strHVParent = dataSheet.getCell(13, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.HighParent.toString(), strHVParent);
			
			String strHVAllele = dataSheet.getCell(14, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.HighValueAllele.toString(), strHVAllele);
			
			String strLVParent = dataSheet.getCell(15, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.LowParent.toString(), strLVParent);
			
			String strLVAllele = dataSheet.getCell(16, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.LowAllele.toString(), strLVAllele);
			


			String strLOD = dataSheet.getCell(17, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.LOD.toString(), strLOD);

			String strR2 = dataSheet.getCell(18, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.R2.toString(), strR2);

			String strInteractions = dataSheet.getCell(19, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Interactions.toString(), strInteractions);

			listOfDataInDataSheet.add(hmOfDataInDataSheet);
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
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			genoManager=factory.getGenotypicDataManager();
	
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		/*HibernateSessionProvider hibernateSessionProviderForLocal = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal();
		//germplasmDataManagerImpl.setSessionProviderForLocal(hibernateSessionProviderForLocal);
		localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		genoManager=factory.getGenotypicDataManager();
		sessionL=localSession.getSessionFactory().openSession();	
        sessionC=centralSession.getSessionFactory().openSession();	
		*/
		//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
		validateData();
		createObjectsToBeSavedToDB();
	}

	@Override
	public void validateData() throws GDMSException {
		/*localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		genoManager=factory.getGenotypicDataManager();
		sessionL=localSession.getSessionFactory().openSession();	
        sessionC=centralSession.getSessionFactory().openSession();*/
		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			genoManager=factory.getGenotypicDataManager();
	
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		String strArrayOfReqColNames[] = {UploadField.Institute.toString(), UploadField.DatasetName.toString(), UploadField.DatasetDescription.toString(), 
				UploadField.Genus.toString()};

		HashMap<String, String> hashMapOfFieldsAndValuesFromSource = listOfDataRowsFromSourceTable.get(0);
		for(int j = 0; j < strArrayOfReqColNames.length; j++){
			//System.out.println( strArrayOfReqColNames[j]);
			String strReqCol = strArrayOfReqColNames[j];
			if(false == hashMapOfFieldsAndValuesFromSource.containsKey(strReqCol)){
				throw new GDMSException("Column " + strArrayOfReqColNames[j].toLowerCase() + " not found in QTL_Source sheet.");
			} else {
				String strReqColValue = hashMapOfFieldsAndValuesFromSource.get(strReqCol);
				if (null == strReqColValue){
					throw new GDMSException("Please provide the value for " +  strReqCol  + " in QTL_Source sheet of the template.");
				} else if (strReqColValue.equals("")){
					throw new GDMSException("Please provide the value for " +  strReqCol  + " in QTL_Source sheet of the template.");
				}
			}
		}	


		/*String strArrayOfRequiredColumnNames[] = {UploadField.Name.toString(), UploadField.Chromosome.toString(), UploadField.MapName.toString(), 
				UploadField.Position.toString(), UploadField.PosMin.toString(), UploadField.PosMax.toString(),
				UploadField.TraitID.toString(), UploadField.Experiment.toString(), UploadField.LFM.toString(),
				UploadField.RFM.toString(), UploadField.Effect.toString(), UploadField.LOD.toString(), UploadField.R2.toString()};
*/
		String strArrayOfRequiredColumnNames[] = {UploadField.Name.toString(), UploadField.Chromosome.toString(), UploadField.MapName.toString(), 
				UploadField.Position.toString(), UploadField.PosMin.toString(), UploadField.PosMax.toString(),
				UploadField.TraitID.toString(),  UploadField.LFM.toString(),
				UploadField.RFM.toString(), UploadField.Effect.toString(), UploadField.LOD.toString(), UploadField.R2.toString()};
		ArrayList qtlList=new ArrayList();
		String QTLs="";
		for(int j = 0; j < listOfDataRowsFromDataTable.size(); j++){
			String strReqColInDataSheet = strArrayOfRequiredColumnNames[j];
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(j);
			System.out.println(hashMapOfDataRow.get(UploadField.Name.toString()));
			if(false == hashMapOfDataRow.containsKey(strReqColInDataSheet)){
				throw new GDMSException("Column " + strArrayOfReqColNames[j] + " not found in QTL_Data sheet.");
			} else {
				String strReqColValue = hashMapOfDataRow.get(strReqColInDataSheet);
				if (null == strReqColValue){
					throw new GDMSException("Please provide the value for " +  strReqColInDataSheet  + " in QTL_Data sheet of the template.");
				} else if (strReqColValue.equals("")){
					throw new GDMSException("Please provide the value for " +  strReqColInDataSheet  + " in QTL_Data sheet of the template.");
				}
			}
			qtlList.add(hashMapOfDataRow.get(UploadField.Name.toString()));
			QTLs=QTLs+"'"+hashMapOfDataRow.get(UploadField.Name.toString())+"',";
		}
		//String strQTLQuerry="select * from gdms_qtl where qtl_name in ("+QTLs.substring(0, QTLs.length()-1)+")";
		String existingQTLs="";
		String QTLexistance="no";
		for(int q=0;q<qtlList.size();q++){
			try{
				//System.out.println("<<<<  :"+genoManager.countQtlByName(qtlList.get(q).toString()));
				//System.out.println("..........  :"+genoManager.countQtlIdByName(qtlList.get(q).toString()));
				int qtlCount=(int)genoManager.countQtlByName(qtlList.get(q).toString());
				if(qtlCount>0){
					existingQTLs=existingQTLs+qtlList.get(q).toString()+",";
					QTLexistance="yes";
				}
			} catch (MiddlewareQueryException e) {
				//throw new GDMSException(e.getMessage());
				e.printStackTrace();
			}
		}
		
		if(QTLexistance.equalsIgnoreCase("yes")){
			throw new GDMSException("Following QTL(s) already exists. Please check. \n"+existingQTLs);

		}
		
		/*query=sessionC.createSQLQuery(strQTLQuerry);
		
		query.addScalar("cvterm_id",Hibernate.INTEGER);	
		query.addScalar("name",Hibernate.STRING);	
		traitsQList=query.list();
		
		rsQC=stCen.executeQuery("select * from gdms_qtl where qtl_name in ("+QTLs+")");
		while(rsQC.next()){
			result2.add(rsQC.getString(2));
		}
		while(rsQL.next()){
			result2.add(rsQL.getString(2));
		}
		if(result2.size()>0){
			ErrMsg = "Following QTL(s) already exists. Please check. \n"+result2;
			request.getSession().setAttribute("indErrMsg", ErrMsg);							
			return "ErrMsg";
		}*/
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		System.out.println("!!!!!@@@@@@@@@@@@@@@@@@@@@#############################$$$$$$$$$$$$$$$$$$$$$$$%%%%%%%%%%%%%%%%%%%%%%%%^^^^^^^^^^^^^^^^^&&&&&&&&&&&&&&&&");
		System.out.println("traitsList:"+traitsList);
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			genoManager=factory.getGenotypicDataManager();
	
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		String strQuerry="SELECT cvterm_id, name FROM cvterm WHERE NAME IN ("+traits.substring(0,traits.length()-1)+") AND cv_id=1010";
		
		//GermplasmDataManagerImpl germplasmDataManagerImpl = new GermplasmDataManagerImpl();
		
		int iNumOfQTLDataRowsFromDataTable = listOfDataRowsFromDataTable.size();
		/*try{
			System.out.println("Workbench user =:"+workbenchManager.getWorkbenchRuntimeData().getUserId());
		} catch (MiddlewareQueryException e) {
			//throw new GDMSException(e.getMessage());
			e.printStackTrace();
		}*/
		//om=factory.getNewOntologyDataManager();
		mapTraits = new TreeMap();
        List retTraits = new ArrayList();
		
        /*try{
        	// System.out.println(" ......................    traitList=:"+om.getAllStandardVariables());
	        for(int t=0;t<traitsList.size();t++){
	       	
	       	 Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(traitsList.get(t).toString());
				//assertTrue(standardVariables.size() == 1);
				for (StandardVariable stdVar : standardVariables) {
					System.out.println(stdVar.getId()+"   "+stdVar.getNameSynonyms()+"   "+stdVar.getName());
					traitsComList.add(stdVar.getId());
					retTraits.add(stdVar.getName());
					mapTraits.put(stdVar.getName(), stdVar.getId());
					tids=tids+stdVar.getId()+",";
					tidsCount++;
				}
	        }
		} catch (MiddlewareQueryException e) {
			//throw new GDMSException(e.getMessage());
			e.printStackTrace();
		}*/
        
        
        
        query=centralSession.createSQLQuery(strQuerry);			
		if(query.list().isEmpty())				
			query=localSession.createSQLQuery(strQuerry);	
		
		query.addScalar("cvterm_id",Hibernate.INTEGER);	
		query.addScalar("name",Hibernate.STRING);	
		traitsQList=query.list();
		System.out.println("traitsQList=:"+traitsQList);
		for(int w=0;w<traitsQList.size();w++){
        	Object[] strMareO= (Object[])traitsQList.get(w);
        	//System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
        	mapTraits.put(strMareO[1], strMareO[0]);
        	 	
 		}
		
		HashMap<String, String> hashMapOfSourceDataFields = listOfDataRowsFromSourceTable.get(0);
		
		//Assigning the User Id value based on the Principle investigator's value in the QTL_Source sheet
		Integer iUserId = 0;
		String strPrincipleInvestigator = hashMapOfSourceDataFields.get(UploadField.PrincipleInvestigator.toString());
		String strDatasetName = hashMapOfSourceDataFields.get(UploadField.DatasetName.toString());
		String strDatasetDesc = hashMapOfSourceDataFields.get(UploadField.DatasetDescription.toString());
		String strGenus = hashMapOfSourceDataFields.get(UploadField.Genus.toString());
		String strSpecies = hashMapOfSourceDataFields.get(UploadField.Species.toString()); 
		String strRemarks = hashMapOfSourceDataFields.get(UploadField.Remark.toString());
		String strInstitute = hashMapOfSourceDataFields.get(UploadField.Institute.toString());
		String strEmail = null;
		String strPurposeOfStudy = null;
		String strMethod=hashMapOfSourceDataFields.get(UploadField.Method.toString());
		String strScore=hashMapOfSourceDataFields.get(UploadField.Score.toString());
		
		/*if (null == strPrincipleInvestigator || strPrincipleInvestigator.equals("")) {
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
			lastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_DATASET);
		}catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		int intDatasetId=(int)lastId;
		
		iDatasetId=intDatasetId-1;
		
		//arrayOfQTLs = new Qtl[iNumOfQTLDataRowsFromDataTable];
		int iUploadedQTLCtr = 0;
		
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
		 

			int intMaxVal=0;
			Object obj=null;
			Iterator itList=null;
			List listValues=null;
			Query query=localSession.createSQLQuery("select min(qtl_id) from gdms_qtl");
			
			listValues=query.list();
			itList=listValues.iterator();
						
			while(itList.hasNext()){
				obj=itList.next();
				if(obj!=null)
					intMaxVal=Integer.parseInt(obj.toString());
			}
			
			qtlId=intMaxVal-1;
		 
		 //session = HibernateSessionFactory.currentSession();
		 tx=localSession.beginTransaction();
		
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
		 
		dataset = new DatasetBean();
		dataset.setDataset_id(iDatasetId);
		dataset.setDataset_name(strDatasetName);
		dataset.setDataset_desc(strDatasetDesc);
		dataset.setDataset_type(strDatasetType);
		dataset.setGenus(strGenus);
		dataset.setSpecies(strSpecies);
		dataset.setUpload_template_date(curDate);
		dataset.setRemarks(strRemarks);
		dataset.setDatatype(strDataType);
		dataset.setMissing_data(strMissingData);
		dataset.setMethod(strMethod);
		dataset.setScore(strScore);
		dataset.setInstitute(strInstitute);
		dataset.setPrincipal_investigator(strPrincipleInvestigator);
		dataset.setEmail(strEmail);
		dataset.setPurpose_of_study(strPurposeOfStudy);
		localSession.save(dataset);
		
		//System.out.println(iDatasetId+","+ iUserId);
		datasetUser = new GenotypeUsersBean();
		datasetUser.setDataset_id(iDatasetId);
		datasetUser.setUser_id(iUserId);
		//iDatasetId, iUserId);
		localSession.save(datasetUser);
		
		
		
		
		//listOfQTLDataRows  = new ArrayList<QtlDataRow>();
		for (int i = 0; i < iNumOfQTLDataRowsFromDataTable; i++){
			
			HashMap<String, String> hashMapOfDataRowFromDataTable = listOfDataRowsFromDataTable.get(i);
			
			String strMapNameFromTemplate = hashMapOfDataRowFromDataTable.get(UploadField.MapName.toString());
			Integer iMapId = 1;
			boolean bMapExists = false;
			
			try {
				mapId = genoManager.getMapIdByName(strMapNameFromTemplate);
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
				System.out.println("map id=:"+mapId);
			if(mapId!=0){
				iMapId =mapId;
				bMapExists = true;
				//break;
			}
			
			if (false == bMapExists){
				String strErrMsg = "Map does not exists.\nPlease Upload the corresponding Map";
				throw new GDMSException(strErrMsg);
			}
			
			Integer iQTLId = null; //Will be set/overridden by the function, testSetQTL() of TestGenotypicDataManagerImpl class
			
			String strQTLName = hashMapOfDataRowFromDataTable.get(UploadField.Name.toString());
			String strLinkageGroup = hashMapOfDataRowFromDataTable.get(UploadField.Chromosome.toString());
			
			String strPosition = hashMapOfDataRowFromDataTable.get(UploadField.Position.toString());
			Float fPosition = new Float(0.0);
			if (null != strPosition){
				fPosition = Float.parseFloat(strPosition);
			}
			
			String strPosMin = hashMapOfDataRowFromDataTable.get(UploadField.PosMin.toString());
			Float fMinPosition = new Float(0.0);
			if (null != strPosMin){
				fMinPosition = Float.parseFloat(strPosMin);
			}
			
			String strPosMax = hashMapOfDataRowFromDataTable.get(UploadField.PosMax.toString());
			Float fMaxPosition = new Float(0.0);
			if (null != fMaxPosition){
				fMaxPosition = Float.parseFloat(strPosMax);
			}
			
			
			String strTrait = hashMapOfDataRowFromDataTable.get(UploadField.TraitID.toString());
			//Integer traitId = Integer.parseInt(strTrait);
			
			System.out.println("strTrait:"+strTrait);
			
			String strExperiment = hashMapOfDataRowFromDataTable.get(UploadField.Experiment.toString());
			System.out.println("strExperiment:"+strExperiment);
			
			String strClen = hashMapOfDataRowFromDataTable.get(UploadField.Clen.toString());
			Float fClen = 0f;
			if (false == strClen.equals("")){
				fClen = Float.parseFloat(strClen);
			}
			
			String strLeftFlankingMarker = hashMapOfDataRowFromDataTable.get(UploadField.LFM.toString());
			String strRightFlankingMarker = hashMapOfDataRowFromDataTable.get(UploadField.RFM.toString());
			
			String strEffect = hashMapOfDataRowFromDataTable.get(UploadField.Effect.toString());
			Float fEffect = 0f;
			if (false == strEffect.equals("")){
				fEffect = Float.parseFloat(strEffect);
			}
			System.out.println("fEffect:"+fEffect);
			
			
			String strSEAdditive = hashMapOfDataRowFromDataTable.get(UploadField.additive.toString());
			System.out.println("strSEAdditive:"+strSEAdditive);
			String strHVParent = hashMapOfDataRowFromDataTable.get(UploadField.HighParent.toString()); 
			System.out.println("strHVParent=:"+strHVParent);
			
			String strHVAllele = hashMapOfDataRowFromDataTable.get(UploadField.HighValueAllele.toString());
			System.out.println("strHVAllele=:"+strHVAllele);
			
			String strLVParent = hashMapOfDataRowFromDataTable.get(UploadField.LowParent.toString());
			System.out.println("strLVParent=:"+strLVParent);
			
			String strLVAllele = hashMapOfDataRowFromDataTable.get(UploadField.LowAllele.toString());
			
			System.out.println("strLVAllele=:"+strLVAllele);
			
			String strR2 = hashMapOfDataRowFromDataTable.get(UploadField.R2.toString());
			System.out.println("strR2=:"+strR2);
			System.out.println("R2:"+strR2);
			
			Float fRSquare = 0f;
			if (false == strR2.equals("")){
				fRSquare = Float.parseFloat(strR2);
			}
			
			String strLOD = hashMapOfDataRowFromDataTable.get(UploadField.LOD.toString());
			Float fScoreValue = 0f;
			if (false == strLOD.equals(strLOD)){
				fScoreValue = Float.parseFloat(strLOD);
			}
			
			
			String strInteractions = hashMapOfDataRowFromDataTable.get(UploadField.Interactions.toString());
			
			/*String strSEAdditive = null; 
			String strHVParent = null; 
			String strHVAllele = null; 
			String strLVParent = null;
			String strLVAllele = null;*/
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("DatasetDetails:"+iDatasetId+","+strDatasetName+","+strDatasetDesc+","+strDatasetType+","+strGenus+","+strSpecies+","+uploadTemplateDate+","+strRemarks+","+strDataType+","+ strMissingData+","+strMethod+","+strScore+","+strInstitute+","+strPrincipleInvestigator+","+strEmail+","+ strPurposeOfStudy);
			//dataset = new Dataset(iDatasetId, strDatasetName, strDatasetDesc, strDatasetType, strGenus, strSpecies, uploadTemplateDate, strRemarks,
			//strDataType, strMissingData, strMethod, strScore);
			
			
			//qtlDetails = new QtlDetails(iQTLId, iMapId, fMinPosition, fMaxPosition, strTrait, strExperiment, fEffect,
					//fScoreValue, fRSquare, strLinkageGroup, strInteractions, strLeftFlankingMarker,
					//strRightFlankingMarker, fPosition, fClen, strSEAdditive, strHVParent, strHVAllele, strLVParent, strLVAllele);
			
			//genoManager.getLastId(Database.LOCAL, GdmsTable.)
			
			qtl = new QTLBean();
			qtl.setDataset_id(iDatasetId);
			qtl.setQtl_id(qtlId);
			qtl.setQtl_name(strQTLName);
			localSession.save(qtl);
			//iQTLId, strQTLName, iDatasetId);
			System.out.println("mapTraits:"+mapTraits);
			System.out.println("QTL Details:"+qtlId+","+iMapId+","+fMinPosition+","+fMaxPosition+","+strExperiment+","+fEffect+","+fScoreValue+","+fRSquare+","+strLinkageGroup+","+strInteractions+","+strLeftFlankingMarker+","+strRightFlankingMarker+","+fPosition+","+fClen+","+strSEAdditive+","+strHVParent+","+strHVAllele+","+strLVParent+","+strLVAllele);
			
			qtlDetails = new QTLDetailsBean();
			qtlDetails.setQtl_id(qtlId);
			qtlDetails.setPosition(fPosition);
			qtlDetails.setMin_position(fMinPosition);
			qtlDetails.setMax_position(fMaxPosition);
			qtlDetails.setTid(Integer.parseInt(mapTraits.get(strTrait).toString()));
			qtlDetails.setExperiment(strExperiment);
			qtlDetails.setEffect(fEffect);
			qtlDetails.setScore_value(fScoreValue);
			qtlDetails.setR_square(fRSquare);
			qtlDetails.setLinkage_group(strLinkageGroup);
			qtlDetails.setInteractions(strInteractions);
			qtlDetails.setLeft_flanking_marker(strLeftFlankingMarker);
			qtlDetails.setRight_flanking_marker(strRightFlankingMarker);
			qtlDetails.setClen(fClen);
			qtlDetails.setSe_additive(strSEAdditive);
			qtlDetails.setHv_parent(strHVParent);
			qtlDetails.setHv_allele(strHVAllele);
			qtlDetails.setLv_parent(strLVParent);
			qtlDetails.setLv_allele(strLVAllele);
			
			localSession.save(qtlDetails);
			qtlId--;
			if (i % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			//arrayOfQTLs[iUploadedQTLCtr++] = qtl;
			/*QtlDataRow qtlDataRow = new QtlDataRow(qtl, qtlDetails);//addedMarker, accMetadataSet, markerMetadataSet, alleleValues, dartValues);
			listOfQTLDataRows.add(qtlDataRow);*/
			
		}
		//saveQTL();
		tx.commit();
	}
	
	/*protected void saveQTL() throws GDMSException {
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		genoManager=factory.getGenotypicDataManager();
		try {
			//genoManager.setQTL(datasetUser, dataset, qtlDetails, qtl);
			genoManager.setQTL(dataset, datasetUser,listOfQTLDataRows);
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading QTL Middleware Exception");
		} catch (Throwable th){
			throw new GDMSException("Error uploading QTL", th);
		}
	}*/

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		//if (null != arrayOfQTLs && arrayOfQTLs.length > 0){
			String strUploadInfo = "";

			/*for (int i = 0; i < arrayOfQTLs.length; i++){
				Integer iQTLId = arrayOfQTLs[i].getQtlId();
				String strQTLName = arrayOfQTLs[i].getQtlName();
				String strQTL = "QTL: " + iQTLId + ": " + strQTLName;
				strUploadInfo += strQTL + "\n";
			}*/

			strDataUploaded = "Uploaded QTL(s): \n";
		//}	
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
		// TODO Auto-generated method stub
		return null;
	}

}
