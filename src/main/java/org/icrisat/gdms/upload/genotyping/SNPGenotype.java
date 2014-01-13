package org.icrisat.gdms.upload.genotyping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.generationcp.middleware.dao.UserDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
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
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.Dataset;
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


public class SNPGenotype  implements  UploadMarker {

	private String strFileLocation;
	private String strPI;
	private String strDatasetName;
	private String strDatasetDescription;
	private String strGenus;
	private String strSpecies;
	private String strMissingData;
	private int iGIDCount;
	private String[] strArrayOfGIDs;
	private int iGCount;
	private String[] strArrayOfGenotypes;
	private ArrayList<String> listOfGIDsAndGNamesFromFile;
	private ArrayList<String> listOfGIDsFromTheFile;
	private ArrayList<String> listOfGNames;
	private AccMetadataSet accMetadataSet;
	private AccMetadataSet accMetadataSet1;
	private MarkerMetadataSet markerMetadataSet;
	private DatasetUsers datasetUser;
	private CharValues charValues;
	private Marker marker;
	
	private Marker addedMarker;
	private Dataset dataset;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private ArrayList<HashMap<String, String>> listOfDataInSourceLines;
	private ArrayList<HashMap<String, String>> listOfDataInDataLines;
	private ArrayList<String> listOfMarkersFromTheSheet = new ArrayList<String>();
	private String strInstitute;
	private String strEmail;
	private String strInchargePerson;
	private String strPurposeOfStudy;
	private String strCreationDate;
	private List<List<String>> listOfGenoData = new ArrayList<List<String>>();
	private HashMap<Integer, String> hashMapOfGIDsAndGNamesFromFile;
	BufferedReader bReader = null;
	private Marker[] arrayOfMarkers;
	
	List<SNPDataRow> listOfSNPDataRows; 
	GenotypicDataManager manager1=null;
	
	ArrayList<String> listOfMarkerNamesFromSourceTable = new ArrayList<String>();
	
	String strMarkerType="SNP";
	ManagerFactory factory =null;
	
	String notMatchingDataDB="";
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    int size=0;
    String notMatchingGIDS="";
    
        
    
    static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
	@Override
	public void readExcelFile() throws GDMSException {
		try {			
			bReader = new BufferedReader(new FileReader(strFileLocation));
		} catch (FileNotFoundException e) {
			throw new GDMSException(e.getMessage());
		} 
	}

	@Override
	public void validateDataInExcelSheet() throws GDMSException {

		String strLine = "";
		try {
			
			
			while ((strLine = bReader.readLine()) != null) {
				String[] strArrayOfTokens = strLine.split("\t");	
				int iNumOfTokens = strArrayOfTokens.length;		

				if(strLine.startsWith("Institute")){
					strInstitute = strArrayOfTokens[1];
				}
				if(strLine.startsWith("PI")){
					if(iNumOfTokens == 2) {
						strPI = strArrayOfTokens[1];
					}
					if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the PI ");
					}
					if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line PI");
					}
				}
				if(strLine.startsWith("Email")){
					strEmail = strArrayOfTokens[1];
				}
				if(strLine.startsWith("Incharge_Person")){
					strInchargePerson = strArrayOfTokens[1];
				}
				if(strLine.startsWith("Dataset_Name")){
					if(iNumOfTokens == 2){
						strDatasetName = strArrayOfTokens[1];
					}else if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the Dataset Name ");
					}else if(iNumOfTokens>2){ 	
						throw new GDMSException("There are extra tabs at line Dataset Name");
					}
				}
				if(strLine.startsWith("Purpose_Of_Study")){
					strPurposeOfStudy = strArrayOfTokens[1];
				}
				if(strLine.startsWith("Dataset_Description")){
					if(iNumOfTokens == 2) {
						strDatasetDescription = strArrayOfTokens[1];		
					} else if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the Description");
					} else if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line Description");
					}
				}

				if(strLine.startsWith("Genus")){
					if(iNumOfTokens == 2){
						strGenus = strArrayOfTokens[1];	
					} else if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the Genus");
					} else if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line Genus");
					}
				}	
				if(strLine.startsWith("Species")){
					if(iNumOfTokens == 2) {
						strSpecies = strArrayOfTokens[1];
					} else if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the Species");
					} else if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line Species");
					}
				}				
				if(strLine.startsWith("Missing_Data")){
					if(iNumOfTokens == 2) {
						strMissingData = strArrayOfTokens[1];
					} else if(iNumOfTokens==1){
						throw new GDMSException("Please provide the Missing_Data");
					} else if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line Missing_Data");
					}
				}
				if(strLine.startsWith("Creation_Date")){
					strCreationDate = strArrayOfTokens[1];
				}
				//System.out.println("iNumOfTokens=:"+iNumOfTokens+"   "+strArrayOfTokens);
				if((strArrayOfTokens[0].startsWith("gid's") && (iNumOfTokens >= 2))){					
					iNumOfTokens = strArrayOfTokens.length;
					iGIDCount = iNumOfTokens;
					for(int g = 1; g < iNumOfTokens; g++){						
						strArrayOfGIDs = strArrayOfTokens;							
					}					
				}
				if((strArrayOfTokens[0].startsWith("Marker\\Genotype")) && (iNumOfTokens >= 2)){	
					iNumOfTokens = strArrayOfTokens.length;
					iGCount = iNumOfTokens;
					for(int g = 1; g < iNumOfTokens; g++){
						strArrayOfGenotypes = strArrayOfTokens;							
					}					
				}
				if((!(strArrayOfTokens[0].equals("Marker\\Genotype"))) && (!(strArrayOfTokens[0].equals("gid's"))) && (iNumOfTokens > 2)){
					listOfGenoData.add(Arrays.asList(strArrayOfTokens)); 			
					listOfMarkersFromTheSheet.add(strArrayOfTokens[0]);
				}	
			}

			if(iGIDCount < iGCount){
				throw new GDMSException("The number of GIDs is less than the number of Germplasm names provided");
			}else if(iGCount < iGIDCount){
				throw new GDMSException("The number of GIDs is more than the number of Germplasm names provided");
			}

			listOfGIDsAndGNamesFromFile = new ArrayList<String>();
			hashMapOfGIDsAndGNamesFromFile = new HashMap<Integer, String>();
			listOfGIDsFromTheFile = new ArrayList<String>();
			listOfGNames = new ArrayList<String>();

			if (null != strArrayOfGIDs){
				for(int d = 1; d < strArrayOfGIDs.length; d++){	  
					if(!listOfGIDsAndGNamesFromFile.contains(Integer.parseInt(strArrayOfGIDs[d]))){
						listOfGIDsAndGNamesFromFile.add(Integer.parseInt(strArrayOfGIDs[d]) + "," + strArrayOfGenotypes[d]);
						hashMapOfGIDsAndGNamesFromFile.put(Integer.parseInt(strArrayOfGIDs[d]), strArrayOfGenotypes[d]);
					}
					
					if(!listOfGIDsFromTheFile.contains(Integer.parseInt(strArrayOfGIDs[d])))
						listOfGIDsFromTheFile.add(strArrayOfGIDs[d]);
					
					listOfGNames.add(strArrayOfGenotypes[d]);
				}
			}

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI() throws GDMSException {

		listOfDataInSourceLines = new ArrayList<HashMap<String,String>>();
		listOfDataInDataLines = new ArrayList<HashMap<String,String>>();

		HashMap<String, String> hashMapOfSourceFields = new HashMap<String, String>();
		hashMapOfSourceFields.put(UploadField.Institute.toString(), strInstitute);
		hashMapOfSourceFields.put(UploadField.PI.toString(), strPI);
		hashMapOfSourceFields.put(UploadField.Email.toString(), strEmail);
		hashMapOfSourceFields.put(UploadField.InchargePerson.toString(), strInchargePerson);
		hashMapOfSourceFields.put(UploadField.DatasetName.toString(), strDatasetName);
		hashMapOfSourceFields.put(UploadField.PurposeOfTheStudy.toString(), strPurposeOfStudy);
		hashMapOfSourceFields.put(UploadField.DatasetDescription.toString(), strDatasetDescription);
		hashMapOfSourceFields.put(UploadField.Genus.toString(), strGenus);
		hashMapOfSourceFields.put(UploadField.Species.toString(), strSpecies);
		hashMapOfSourceFields.put(UploadField.MissingData.toString(), strMissingData);
		hashMapOfSourceFields.put(UploadField.CreationDate.toString(), strCreationDate);
		listOfDataInSourceLines.add(hashMapOfSourceFields);

		
		for (int i = 0; i < listOfGIDsFromTheFile.size(); i++){
			
			HashMap<String, String> hashMapOfGenoDataLine = new HashMap<String, String>();
			
			String strGID = listOfGIDsFromTheFile.get(i);
			hashMapOfGenoDataLine.put(UploadField.GID.toString(), listOfGIDsFromTheFile.get(i));
			
			String strGName = hashMapOfGIDsAndGNamesFromFile.get(Integer.parseInt(strGID));
			hashMapOfGenoDataLine.put(UploadField.Genotype.toString(), strGName);
			
			for (int j = 0; j < listOfMarkersFromTheSheet.size(); j++){
				String strMarkerName = listOfMarkersFromTheSheet.get(j);
				for (int k = 0; k < listOfGenoData.size(); k++){
					List<String> list = listOfGenoData.get(k);
					if (list.contains(strMarkerName)){
						String strMName = list.get(0);
						if (strMName.equals(strMarkerName)) {
							String string = list.get(i+1);
							hashMapOfGenoDataLine.put(strMarkerName, string);
							break;
						}
					}
				}
			}
			
			listOfDataInDataLines.add(hashMapOfGenoDataLine);
		}

	}



	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(ArrayList<HashMap<String, String>> theListOfSourceDataRows, ArrayList<HashMap<String, String>> listOfDataRows,ArrayList<HashMap<String, String>> listOfGIDRows) {
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
		
		ArrayList<Integer> listOfGIDFromTable = new ArrayList<Integer>();
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++) {
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			String strGID = hashMapOfDataRow.get(UploadField.GID.toString());
			listOfGIDFromTable.add(Integer.parseInt(strGID));
		}
		
		if (0 == listOfGIDFromTable.size()){
			throw new GDMSException("Please provide list of GIDs to be uploaded.");
		}
		

		listOfMarkerNamesFromSourceTable = new ArrayList<String>();
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
			HashMap<String,String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			Iterator<String> iterator = hashMapOfDataRow.keySet().iterator();
			while(iterator.hasNext()){
				String strMarkerNameFromSourceTable = iterator.next();
				if (false == (strMarkerNameFromSourceTable.equals(UploadField.GID.toString()) ||
                    strMarkerNameFromSourceTable.equals(UploadField.Genotype.toString()))){
					if (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable)){
						listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
					}
				}
			}
		}

		if (0 == listOfMarkerNamesFromSourceTable.size()) {
			throw new GDMSException("Please provide list of MarkerNames to be uploaded.");
		}
		
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {	
		
		HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%  :"+GDMSModel.getGDMSModel().getLocalParams().);
		GermplasmDataManager manager = factory.getGermplasmDataManager();
		GenotypicDataManager manager1=factory.getGenotypicDataManager();
		ArrayList<Integer> listOfGIDFromTable = new ArrayList<Integer>();
		ArrayList<String> listOfGNamesFromTable = new ArrayList<String>();
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++) {
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			String strGID = hashMapOfDataRow.get(UploadField.GID.toString());
			if(!(listOfGIDFromTable.contains(strGID))){
				listOfGIDFromTable.add(Integer.parseInt(strGID));
				listOfGNamesFromTable.add(hashMapOfDataRow.get(UploadField.Genotype.toString()));
			}
			hashMapOfGNameandGID.put(hashMapOfDataRow.get(UploadField.Genotype.toString()), Integer.parseInt(strGID));
			
			//System.out.println("germplasm name=:"+hashMapOfDataRow.get(UploadField.Genotype.toString())+"      gid=:"+hashMapOfDataRow.get(UploadField.GID.toString()));
		}

		ArrayList<String> listOfMarkerNamesFromSourceTable = new ArrayList<String>();
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
			HashMap<String,String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			Iterator<String> iterator = hashMapOfDataRow.keySet().iterator();
			while(iterator.hasNext()){
				String strMarkerNameFromSourceTable = iterator.next();
				if (false == (strMarkerNameFromSourceTable.equals(UploadField.GID.toString()) ||
                    strMarkerNameFromSourceTable.equals(UploadField.Genotype.toString()))){
					if( (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable))&&(strMarkerNameFromSourceTable != "SNo")){
						listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
					}
				}
			}
		}
		ArrayList gidsDBList = new ArrayList();
		ArrayList gNamesDBList = new ArrayList();
		hashMap.clear();
		for(int n=0;n<listOfGNamesFromTable.size();n++){
			try{
				//System.out.println("........:"+listOfGNamesFromTable.get(n).toString());
				//List<Germplasm> germplasmList = germplasmDataManagerImpl.getGermplasmByName(listOfGNamesProvided.get(n).toString(), 0, new Long(germplasmDataManagerImpl.countGermplasmByName(listOfGNamesProvided.get(n).toString(), Operation.EQUAL)).intValue(), Operation.EQUAL);
				List<Germplasm> germplasmList = manager.getGermplasmByName(listOfGNamesFromTable.get(n).toString(), 0, new Long(manager.countGermplasmByName(listOfGNamesFromTable.get(n).toString(), Operation.EQUAL)).intValue(), Operation.EQUAL);
				for (Germplasm g : germplasmList) {
					//System.out.println("Checking  ==:"+g.getGid()+"   "+listOfGNamesFromTable.get(n));
				
		        	if(!(gidsDBList.contains(g.getGid()))){
		        		gidsDBList.add(g.getGid());
		        		gNamesDBList.add(listOfGNamesFromTable.get(n).toString());
		        		addValues(listOfGNamesFromTable.get(n).toString(), g.getGid());					        		
		        	}				        	
		           //System.out.println("  " + g.getGid());
		        }
				
			} catch (MiddlewareQueryException e1) {
				throw new GDMSException(e1.getMessage());
			}
			
	        //System.out.println(n+":"+gnamesList.get(n).toString()+"   "+hashMap.get(gnamesList.get(n).toString()));
		}
		
		
		
		if(gNamesDBList.size()>0){
			for(int n=0;n<listOfGNamesFromTable.size();n++){
	 		   if(gNamesDBList.contains(listOfGNamesFromTable.get(n))){
	 			   if(!(hashMap.get(listOfGNamesFromTable.get(n).toString()).contains(hashMapOfGNameandGID.get(listOfGNamesFromTable.get(n).toString())))){
	 				   notMatchingData=notMatchingData+listOfGNamesFromTable.get(n)+"   "+hashMapOfGNameandGID.get(listOfGNamesFromTable.get(n).toString())+"\n\t";
	 				   notMatchingDataDB=notMatchingDataDB+listOfGNamesFromTable.get(n)+"="+hashMap.get(listOfGNamesFromTable.get(n))+"\t";
		        		   alertGN="yes";
	 			   }
	 		   }else{
	 			   //int gid=GIDsMap.get(gnamesList.get(n).toString());
	 			   alertGID="yes";
	     		   size=hashMap.size();
	     		   notMatchingGIDS=notMatchingGIDS+listOfGNamesFromTable.get(n).toString()+", ";
	 		   }
	 	   }
	    }
	    if((alertGN.equals("yes"))&&(alertGID.equals("no"))){
	 	   //String ErrMsg = "GID(s) ["+notMatchingGIDS.substring(0,notMatchingGIDS.length()-1)+"] of Germplasm(s) ["+notMatchingData.substring(0,notMatchingData.length()-1)+"] being assigned to ["+notMatchingDataExists.substring(0,notMatchingDataExists.length()-1)+"] \n Please verify the template ";
	    	throw new GDMSException("Please verify the name(s) provided \t "+notMatchingData+" which do not match the GID(s) present in the database"+notMatchingDataDB);	 	   
	    }
	    if((alertGID.equals("yes"))&&(alertGN.equals("no"))){	        	   
	 	   if(size==0){
	 		   //ErrMsg = "The GIDs provided do not exist in the database. \n Please upload the relevant germplasm information to the GMS ";
	 		  throw new GDMSException("The Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook ");
	 	   }else{
	 		  throw new GDMSException("The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS);
	 		   //ErrMsg = "Please verify the GID/Germplasm(s) provided as some of them do not exist in the database. \n Please upload germplasm information into GMS ";
	 	   } 	   
	    }
		
	    if((alertGID.equals("yes"))&&(alertGN.equals("yes"))){
	    	throw new GDMSException("The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS+" \n Please verify the name(s) provided "+notMatchingData+" which do not match the GIDS(s) present in the database "+notMatchingDataDB);	 	  
	    }
		
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		HibernateSessionProvider hibernateSessionProviderForLocal = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal();
		genotypicDataManagerImpl.setSessionProviderForLocal(hibernateSessionProviderForLocal);
		genotypicDataManagerImpl.setSessionProviderForCentral(null);
		ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
		HashMap<Integer, String> hashMapOfGIDandGName = new HashMap<Integer, String>();
		//HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		HashMap<Integer, Integer> hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
		List<Marker> listOfMarkersFromDB = null;
		HashMap<String, Integer> hashMapOfMNamesAndMIDs = new HashMap<String, Integer>();
		Name names = null;
		ArrayList gidL=new ArrayList();
		List<Integer> nameIdsByGermplasmIds =new ArrayList();
		for(int n=0;n<listOfGIDFromTable.size();n++){
		try {
			names = manager.getNameByGIDAndNval(Integer.parseInt(listOfGIDFromTable.get(n).toString()), listOfGNamesFromTable.get(n).toString(), GetGermplasmByNameModes.STANDARDIZED);
			if(names==null){
				names=manager.getNameByGIDAndNval(Integer.parseInt(listOfGIDFromTable.get(n).toString()), listOfGNamesFromTable.get(n).toString(), GetGermplasmByNameModes.NORMAL);
			}	
			//System.out.println(",,,,,,,,,,,,,,,:"+names.getGermplasmId());
			if(!gidL.contains(names.getGermplasmId()))
            	gidL.add(names.getGermplasmId());
			listOfGermplasmNames.add(names.getNval());
			hashMapOfGIDandGName.put(names.getGermplasmId(), names.getNval());
			hashMapOfGIDsandNIDs.put(names.getGermplasmId(), names.getNid());
			hashMapOfGNameandGID.put(names.getNval(), names.getGermplasmId());
			nameIdsByGermplasmIds.add(names.getNid());
			} catch (MiddlewareQueryException e1) {
				throw new GDMSException(e1.getMessage());
			}
		}
		
			
			/** 
			 * 20130813
			 */
			if (null == nameIdsByGermplasmIds){
				throw new GDMSException("Error retrieving list of NIDs for given GIDs. Please provide valid GIDs.");
			} 
			
			if (0 == nameIdsByGermplasmIds.size()){
				throw new GDMSException("Name IDs do not exist for given GIDs. Please provide valid GIDs.");
			}
			//20130813
			
			
			try{
			MarkerDAO markerDAO = new MarkerDAO();
			markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
			long countAll = markerDAO.countAll();
			
			List<Integer> listOfMarkerIdsByMarkerNames = markerDAO.getIdsByNames(listOfMarkerNamesFromSourceTable, 0, (int)countAll);
			listOfMarkersFromDB = markerDAO.getMarkersByIds(listOfMarkerIdsByMarkerNames, 0, (int)countAll);
			
			/** 
			 * 20130813
			 *//*
			if (null == listOfMarkersFromDB){
				throw new GDMSException("Error retrieving list of Markers for given Marker-Names. Please provide valid Marker-Names.");
			} 
			
			if (0 == listOfMarkersFromDB.size()){
				throw new GDMSException("Markers do not exist for given Marker-Names. Please provide valid Marker-Names.");
			}
			//20130813
*/			
			for (Marker marker : listOfMarkersFromDB){
				String strMarkerName = marker.getMarkerName();
				Integer markerId = marker.getMarkerId();
				if (false == hashMapOfMNamesAndMIDs.containsKey(strMarkerName)){
					hashMapOfMNamesAndMIDs.put(strMarkerName, markerId);
				}
			}
		} catch (NumberFormatException e) {
			throw new GDMSException(e.getMessage());
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}

		//Integer iDatasetId = 0; //Will be set/overridden by the function
		String strDataType = "char"; 
		Date uploadTemplateDate = new Date(System.currentTimeMillis());
		String strCharValue = "CV";
		String strDatasetType = "SNP";
		String method = null;
		String strScore = null;
		Integer iACId = null;
		String strRemarks = "";

		HashMap<String, String> hashMapOfSourceDataRow = listOfDataRowsFromSourceTable.get(0);
		
		String strPIFromSourceTable = hashMapOfSourceDataRow.get(UploadField.PI.toString());
		String strDatasetNameFromSourceTable = hashMapOfSourceDataRow.get(UploadField.DatasetName.toString());
		String strDatasetDescFromSourceTable = hashMapOfSourceDataRow.get(UploadField.DatasetDescription.toString());
		String strGenusFromSourceTable = hashMapOfSourceDataRow.get(UploadField.Genus.toString());
		String strSpeciesFromSourceTable = hashMapOfSourceDataRow.get(UploadField.Species.toString());
		String strMissingDataFromSourceTable = hashMapOfSourceDataRow.get(UploadField.MissingData.toString());
		int marker_id=0;
		Database instance = Database.LOCAL;
		/*try{
			long lastId = manager1.getLastId(instance, GdmsTable.GDMS_MARKER);
			//System.out.println("testGetLastId(" + GdmsTable.GDMS_MARKER + ") in " + instance + " = " + lastId);
			marker_id=(int)lastId;
			
		}catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Genotyping Data");
		} */
		ArrayList finalList =new ArrayList();
		Integer iUserId = 0;
		if (null == strPIFromSourceTable || strPIFromSourceTable.equals("")){
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
			userDAO.setSession(hibernateSessionProviderForLocal.getSession());
			List<User> listOfAllUsers =  null;
			try {
				listOfAllUsers = userDAO.getAll();
				for (User user : listOfAllUsers){
					String strName = user.getName();
					if (strName.equals(strPIFromSourceTable)){
						iUserId = user.getUserid();
						break;
					}
				}
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
		}

		
		int iUploadedMarkerCount = 0;
		
		//System.out.println("listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
		
		int iNumOfMarkers = listOfMarkerNamesFromSourceTable.size();
		int iNumOfGIDs = listOfGIDFromTable.size();
		arrayOfMarkers = new Marker[iNumOfMarkers*iNumOfGIDs];
		Integer iDatasetId = 0;

		
		dataset = new Dataset();
		//dataset.setDatasetId(iDatasetId);
		dataset.setDatasetName(strDatasetNameFromSourceTable);
		dataset.setDatasetDesc(strDatasetDescFromSourceTable);
		dataset.setDatasetType(strDatasetType);
		dataset.setGenus(strGenusFromSourceTable);
		dataset.setSpecies(strSpeciesFromSourceTable);
		dataset.setUploadTemplateDate(uploadTemplateDate);
		dataset.setRemarks(strRemarks);
		dataset.setDataType(strDataType);
		dataset.setMissingData(strMissingDataFromSourceTable);
		dataset.setMethod(method);
		dataset.setScore(strScore);
		
		
		datasetUser = new DatasetUsers(iDatasetId, iUserId);
		
		
		listOfSNPDataRows = new ArrayList<SNPDataRow>();
		
		//System.out.println("listOfMarkerNamesFromSourceTable:"+listOfMarkerNamesFromSourceTable);
		
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
			
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			//System.out.println("hashMapOfDataRow:"+hashMapOfDataRow);
			//System.out.println("hashMapOfDataRow.get(UploadField.GID.toString()):"+hashMapOfDataRow.get(UploadField.GID.toString()));
						
			String strGID = hashMapOfDataRow.get(UploadField.GID.toString());
			
			//System.out.println("hashMapOfGIDsandNIDs.get(Integer.parseInt(strGID)):"+hashMapOfGIDsandNIDs.get(Integer.parseInt(strGID)));
			
			Integer iNameId = hashMapOfGIDsandNIDs.get(Integer.parseInt(strGID));
			int datasetId = -10;
			accMetadataSet1 = new AccMetadataSet(new AccMetadataSetPK(datasetId, Integer.parseInt(strGID), iNameId));	
			
			//System.out.println("hashMapOfDataRow.get(UploadField.Genotype.toString()):"+hashMapOfDataRow.get(UploadField.Genotype.toString()));
			String strGenotype = hashMapOfDataRow.get(UploadField.Genotype.toString());
			//System.out.println("nids=:"+hashMapOfGIDsandNIDs);
			//System.out.println(strGID+"   "+hashMapOfGIDsandNIDs.get(Integer.parseInt(strGID)));
			//Integer iNameId = hashMapOfGIDsandNIDs.get(Integer.parseInt(strGID));
			
		
			
			
			//System.out.println("Gid=:"+strGID);
			for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {		
				
					strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));
					
					String strMarkerName = listOfMarkerNamesFromSourceTable.get(j);		
					//System.out.println("Nid=:"+ hashMapOfGIDsandNIDs.get(Integer.parseInt(strGID))  +"GID=:"+strGID+"marker:"+strMarkerName +" Allele value="+ strCharValue);
					int MID=0;
					
					addedMarker= new Marker();
					addedMarker.setMarkerName(strMarkerName);
					addedMarker.setSpecies("groundnut");
																				
					//markerMetadataSet = new MarkerMetadataSet(new MarkerMetadataSetPK(datasetId, MID));
					markerMetadataSet = new MarkerMetadataSet(null, 0);
					

					charValues = new CharValues();
					charValues.setAcId(iACId);
					charValues.setgId(Integer.parseInt(strGID));
					charValues.setCharValue(strCharValue);
					
				
					SNPDataRow snpDataRow = new SNPDataRow(addedMarker, accMetadataSet1, markerMetadataSet, charValues);
					listOfSNPDataRows.add(snpDataRow);
						
				}
			
			
		}
		
		saveSNPGenotype();
	}

	protected void saveSNPGenotype() throws GDMSException {
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		manager1=factory.getGenotypicDataManager();
		try {
			//manager1.setSNP(accMetadataSet1, markerMetadataSet, datasetUser, charValues, dataset, addedMarker);
			//genotypicDataManagerImpl.setSNP(accMetadataSet, markerMetadataSet, datasetUser, charValues, dataset);
			
			/*setSNP(Dataset dataset,
		               DatasetUsers datasetUser,
		               List<SNPDataRow> rows)
		               throws MiddlewareQueryException*/
			
			//20131214: Tulasi
			manager1.setSNP(dataset, datasetUser, listOfSNPDataRows);
		               
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading SNP Genotype");
		} catch (Throwable th){
			throw new GDMSException("Error uploading SNP Genotype", th);
		}
	}

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	public String getDataUploaded() {
		//System.out.println("arrayOfMarkers:"+arrayOfMarkers);
		String strDataUploaded = "";
		/*if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";

			for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strGID = arrayOfMarkers[i].getDbAccessionId();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName + " GID: " + strGID;
				strUploadInfo += strMarker + "\n";
			}*/
			strDataUploaded = "Uploaded SNP Genotyping dataset \n " +listOfMarkerNamesFromSourceTable;
		//}
		
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceLines;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		return listOfDataInDataLines;
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
	

}
