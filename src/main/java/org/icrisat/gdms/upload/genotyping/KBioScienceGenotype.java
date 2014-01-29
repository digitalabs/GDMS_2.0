package org.icrisat.gdms.upload.genotyping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.DatasetUsers;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.SNPDataRow;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;

public class KBioScienceGenotype implements  UploadMarker {
	
	private String strFileLocation;
	//private Workbook workbook;
	//private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();
	
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private ArrayList<HashMap<String, String>> listOfDataInSourceLines;
	private ArrayList<HashMap<String, String>> listOfDataInDataLines;
	
	
	private HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();
	private BufferedReader bReader;
	private HashMap<Integer, String> hmOfColIndexAndGermplasmName;
	private HashMap<String, String> hmOfData;
	private int iDataRowIndex;
	
	private Marker addedMarker;
	private DatasetBean dataset;
	private AccessionMetaDataBean accMetadataSet;
	//private AccMetadataSet accMetadataSet1;
	private MarkerMetadataSet markerMetadataSet;
	private GenotypeUsersBean datasetUser;
	private CharValues charValues;
	
	private Session localSession;
	private Session centralSession;
	
	private Session session;
	
	/*private Session sessionL;
	private Session sessionC;
*/
	ManagerFactory factory =null;
	GenotypicDataManager genoManager;
	List<SNPDataRow> listOfSNPDataRows; 
	
	GermplasmDataManager manager ;
	String notMatchingDataDB="";
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    int size=0;
    String notMatchingGIDS="";
    int iDatasetId = 0;
    int maxMid=0;
	int mid=0;
    private Transaction tx;
    ArrayList finalList =new ArrayList();
	int iUploadedMarkerCount = 0;
	String charData="";
	 int intRMarkerId = 1;
	
	static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
	
	//20131209: Tulasi --- Implemented data to be displayed from the template file and display on the GUI
	
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
		boolean bDataStarts = false;
		
		try {
			while ((strLine = bReader.readLine()) != null) {
				
				if (strLine.startsWith("Project number")) {
					//3System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");	
					
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.ProjectNumber.toString(), strArrayOfTokens[1]);
					}
				} else if (strLine.startsWith("Order number")) {
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.OrderNumber.toString(), strArrayOfTokens[1]);
					}
				} else if (strLine.startsWith("Plates")) {
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.Plates.toString(), strArrayOfTokens[1]);
					}
				}
				
				if(strLine.startsWith("DNA\\Assay") || strLine.startsWith("DNA \\ Assay") || strLine.startsWith("Sample Name")) {					
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");	
					
					hmOfColIndexAndGermplasmName = new HashMap<Integer, String>();
					for(int iColIndex = 0; iColIndex < strArrayOfTokens.length; iColIndex++){
						String strMarkerName = strArrayOfTokens[iColIndex];
						hmOfColIndexAndGermplasmName.put(iColIndex, strMarkerName);
					}					
					bDataStarts = true;					
				} else {					
					if (null == hmOfData) {
						hmOfData = new HashMap<String, String>();
						listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();
					}
					
					if (bDataStarts) {
						//System.out.println(strLine);
						String[] strArrayOfTokens = strLine.split(",");
						
						HashMap<String, String> hmOfData = new HashMap<String, String>();
						String strDNAName = strArrayOfTokens[0]; 
						hmOfData.put(UploadField.DNA.toString(), strDNAName);
						
						for(int iColIndex = 1; iColIndex < strArrayOfTokens.length; iColIndex++){
							String strValue = strArrayOfTokens[iColIndex];
							String strDNA = hmOfColIndexAndGermplasmName.get(iColIndex);
							hmOfData.put(strDNA, strValue);
						}
						
						listOfDataInDataSheet.add(hmOfData);
					}
				}
			}
			
			listOfDataInSourceSheet.add(hmOfDataInSourceSheet);
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}	
		
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI() throws GDMSException {
		
		/*Sheet kbioScienceSheet = workbook.getSheet(0);
		listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();
		
		HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();

		String strKBioScienceGridReport = kbioScienceSheet.getCell(1, 0).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.KBioSciencesGridReport.toString(), strKBioScienceGridReport);

		String strGridVersion = kbioScienceSheet.getCell(1, 1).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.GridVersion.toString(), strGridVersion);

		String strProjectNumber = kbioScienceSheet.getCell(1, 3).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ProjectNumber.toString(), strProjectNumber);

		String strOrderNumber = kbioScienceSheet.getCell(1, 4).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.OrderNumber.toString(), strOrderNumber);

		String strPlates = kbioScienceSheet.getCell(1, 5).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Plates.toString(), strPlates);
		
		listOfDataInSourceSheet.add(hmOfDataInSourceSheet);
		
		int iNumOfColumnsInDataSheet = kbioScienceSheet.getColumns();
		int iNumOfRowsInDataSheet = kbioScienceSheet.getRows();
		HashMap<Integer, String> hmOfColIndexAndGermplasmName = new HashMap<Integer, String>();
		int colIndex = 1;
		for(colIndex = 1; colIndex < iNumOfColumnsInDataSheet; colIndex++){
			String strMarkerName = kbioScienceSheet.getCell(colIndex, 7).getContents().toString();
			hmOfColIndexAndGermplasmName.put(colIndex, strMarkerName);
		}
		
		int rowIndex = 8;
		HashMap<Integer, String> hmOfRowIndexAndDNAName = new HashMap<Integer, String>();
		for(rowIndex = 8; rowIndex < iNumOfRowsInDataSheet; rowIndex++){
			String strDNAName = kbioScienceSheet.getCell(0, rowIndex).getContents().toString();
			hmOfRowIndexAndDNAName.put(rowIndex, strDNAName);
		}

		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();

		for (int rIndex = 8; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strDNAName = kbioScienceSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.DNA.toString(), strDNAName);
			
			//Inserting the Marker-Names and Marker-Values
			for (int cIndex = 1; cIndex < iNumOfColumnsInDataSheet; cIndex++){
				String strMName = hmOfColIndexAndGermplasmName.get(cIndex);
				String strMValue = kbioScienceSheet.getCell(cIndex, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(strMName, strMValue);
			}

			listOfDataInDataSheet.add(hmOfDataInDataSheet);
		}*/
		
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
		// TODO Auto-generated method stub
		/*System.out.println("theListOfSourceDataRows:"+theListOfSourceDataRows);
		System.out.println("listOfDataRows:"+listOfDataRows);
		System.out.println("listOfGIDRows:"+listOfGIDRows);
		*/
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
		// TODO Auto-generated method stub
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		
		String strDatasetSelected = GDMSModel.getGDMSModel().getDatasetSelected();
		String strGermplasmSelected = GDMSModel.getGDMSModel().getGermplasmSelected();
		// TODO Auto-generated method stub
		/*localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		
		*/
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		//sessionL=localSession.getSessionFactory().openSession();	
        //sessionC=centralSession.getSessionFactory().openSession();
		
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%  :"+GDMSModel.getGDMSModel().getLocalParams().);
		manager = factory.getGermplasmDataManager();
		genoManager=factory.getGenotypicDataManager();
		//GermplasmListManager listM = factory.getGermplasmListManager();
		HashMap<String, Integer> hashMapOfEntryIDandGID = new HashMap<String, Integer>();
		HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		
		ArrayList<Integer> listOfGIDFromTable = new ArrayList<Integer>();
		ArrayList<String> listOfGNamesFromTable = new ArrayList<String>();
		
		tx=localSession.beginTransaction();
		int list_Id=0;
		int listID_C=0;
		int listID_L=0;
		ArrayList listEntries=new ArrayList();
		System.out.println("strGermplasmSelected:"+strGermplasmSelected);
		//try{
		
			String strQuerry="select listid from listnms where listname='"+strGermplasmSelected+"'";
			
			
			
			System.out.println(strQuerry);
			//ArrayList<String> listOfGermplasmLists = new ArrayList<String>();
			 int list_id=0;
			
			List newListL=new ArrayList();
			
			List listData=new ArrayList();
			
			List newListC=new ArrayList();
			//try {	
			Object obj=null;
			Object objL=null;
			Iterator itListC=null;
			Iterator itListL=null;
			
			
			//listOfGermplasmLists.clear();
			
			//sessionL=localSession.getSessionFactory().openSession();			
			SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
			queryL.addScalar("listid",Hibernate.INTEGER);	  
			
			newListL=queryL.list();
			itListL=newListL.iterator();			
			while(itListL.hasNext()){
				objL=itListL.next();
				if(objL!=null)
					list_id=Integer.parseInt(objL.toString());					
			}
				
				
			if(list_id==0){
				//sessionC=centralSession.getSessionFactory().openSession();			
				SQLQuery queryC=centralSession.createSQLQuery(strQuerry);		
				queryC.addScalar("listid",Hibernate.INTEGER);;	
				newListC=queryC.list();			
				itListC=newListC.iterator();			
				while(itListC.hasNext()){
					obj=itListC.next();
					if(obj!=null)		
						list_id=Integer.parseInt(obj.toString());				
				}
					
			
			}	
			
			
		/*List<GermplasmList> listsC = listM.getGermplasmListByName(strGermplasmSelected, 0, 5, Operation.EQUAL, Database.CENTRAL);
		System.out.println("testGetGermplasmListByName(" + listsC + ") RESULTS: ");
		for (GermplasmList list : listsC) {
			System.out.println("  " + list);
			listID_C=list.getId();
	    }
		List<GermplasmList> listsL = listM.getGermplasmListByName(strGermplasmSelected, 0, 5, Operation.EQUAL, Database.LOCAL);
		System.out.println("testGetGermplasmListByName(" + listsL + ") RESULTS: ");
		for (GermplasmList list : listsL) {
			System.out.println("  " + list.getId()+"   "+list.getType());
			listID_L=list.getId();
	    }
		if(listID_C != 0)
			list_Id=listID_C;
		else if(listID_L != 0)
			list_Id=listID_L;*/
		//list_Id=list_id;
		System.out.println("list_Id=:"+list_id);
		String querryListData="SELECT entryid, desig, gid FROM listdata WHERE listid="+list_id+" order by entryid";
		System.out.println(querryListData);
		if(list_id>0)
			session=centralSession.getSessionFactory().openSession();		
		else
			session=localSession.getSessionFactory().openSession();			
		SQLQuery query=session.createSQLQuery(querryListData);		
		query.addScalar("entryid",Hibernate.INTEGER);	  
		query.addScalar("desig",Hibernate.STRING);	  
		query.addScalar("gid",Hibernate.INTEGER);	  
		
		listData=query.list();
		for(int w=0;w<listData.size();w++){
        	Object[] strMareO= (Object[])listData.get(w);
           System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]+"   "+strMareO[2]);
        	
        	 	listEntries.add(strGermplasmSelected+"-" + strMareO[0].toString());
	            listOfGNamesFromTable.add(strMareO[1].toString());
	            listOfGIDFromTable.add(Integer.parseInt(strMareO[2].toString()));
	            hashMapOfGNameandGID.put(strMareO[1].toString(), Integer.parseInt(strMareO[2].toString()));
	            hashMapOfEntryIDandGID.put(strGermplasmSelected+"-" + strMareO[0].toString(), Integer.parseInt(strMareO[2].toString()));
 		}
		
		//System.out.println("hashMapOfGNameandGID=:"+hashMapOfGNameandGID);
		String nonExistingListItem="";
		String nonExistingListItems="no";
		String markers ="";
		ArrayList<String> listOfMarkerNamesFromSourceTable = new ArrayList<String>();
		ArrayList<String> listOfDNANamesFromSourceTable = new ArrayList<String>();
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
			HashMap<String,String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			
			listOfDNANamesFromSourceTable.add(hashMapOfDataRow.get(UploadField.DNA.toString()));
			Iterator<String> iterator = hashMapOfDataRow.keySet().iterator();
			while(iterator.hasNext()){
				String strMarkerNameFromSourceTable = iterator.next();
				
				if (false == (strMarkerNameFromSourceTable.equals(UploadField.DNA.toString()) )){
					if( (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable))&&(strMarkerNameFromSourceTable != "SNo")){
						listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
						markers = markers +"'"+ strMarkerNameFromSourceTable+"',";
					}
				}
			}
		}
		//if(listOfDNANamesFromSourceTable.size()!=listEntries.size()){
			for(int k=0;k<listOfDNANamesFromSourceTable.size();k++){
				if(!(listEntries.contains(listOfDNANamesFromSourceTable.get(k)))){
					 nonExistingListItem=nonExistingListItem+listOfDNANamesFromSourceTable.get(k)+"\n";
					 nonExistingListItems="yes";
				}
			}
			if(nonExistingListItems.equalsIgnoreCase("yes")){
				throw new GDMSException("Please verify the List Entries provided doesnot exist in the database\t "+nonExistingListItem );
			}
		//}
						
			List newMarkersListL=new ArrayList();
			List newMarkersListC=new ArrayList();
			//try {	
			Object objM=null;
			Object objML=null;
			Iterator itListMC=null;
			Iterator itListML=null;
			//genoManager.getMar
			
			List lstMarkers = new ArrayList();
			String markersForQuery="";
			/** retrieving maximum marker id from 'marker' table of database **/
			//int maxMarkerId=uptMId.getMaxIdValue("marker_id","gdms_marker",session);
			
			HashMap<String, Object> markersMap = new HashMap<String, Object>();	
			markersForQuery=markers.substring(0, markers.length()-1);
			
			String strQuerryM="select distinct marker_id, marker_name from gdms_marker where Lower(marker_name) in ("+markersForQuery.toLowerCase()+")";
			
			//sessionC=centralSession.getSessionFactory().openSession();			
			SQLQuery queryMC=centralSession.createSQLQuery(strQuerryM);	
			queryMC.addScalar("marker_id",Hibernate.INTEGER);	
			queryMC.addScalar("marker_name",Hibernate.STRING);	
			newMarkersListC=queryMC.list();			
			itListMC=newMarkersListC.iterator();			
			while(itListMC.hasNext()){
				objM=itListMC.next();
				if(objM!=null){	
					Object[] strMareO= (Object[])objM;
		        	//System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
					lstMarkers.add(strMareO[1].toString());
					markersMap.put(strMareO[1].toString(), strMareO[0]);
					
				}
			}
					

			//sessionL=localSession.getSessionFactory().openSession();			
			SQLQuery queryML=localSession.createSQLQuery(strQuerryM);		
			queryML.addScalar("marker_id",Hibernate.INTEGER);	
			queryML.addScalar("marker_name",Hibernate.STRING);      
			newMarkersListL=queryML.list();
			itListML=newMarkersListL.iterator();			
			while(itListML.hasNext()){
				objML=itListML.next();
				if(objML!=null)	{			
					Object[] strMareO= (Object[])objML;
					//System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
					if(!lstMarkers.contains(strMareO[1].toString())){
	            		lstMarkers.add(strMareO[1].toString());	            		
	            		markersMap.put(strMareO[1].toString(), strMareO[0]);	
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
		
		
		System.out.println(hashMap);
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
		
		/*GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		HibernateSessionProvider hibernateSessionProviderForLocal = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal();
		genotypicDataManagerImpl.setSessionProviderForLocal(hibernateSessionProviderForLocal);
		genotypicDataManagerImpl.setSessionProviderForCentral(null);*/
		ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
		HashMap<Integer, String> hashMapOfGIDandGName = new HashMap<Integer, String>();
		//HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		HashMap<Integer, Integer> hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
		List<Marker> listOfMarkersFromDB = null;
		//HashMap<String, Integer> hashMapOfMNamesAndMIDs = new HashMap<String, Integer>();
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
		
		for(int a=0;a<listOfGIDFromTable.size();a++){
        	int gid1=Integer.parseInt(listOfGIDFromTable.get(a).toString());
        	if(gidL.contains(gid1)){
        		finalList.add(gid1+"~!~"+hashMapOfGIDsandNIDs.get(gid1));	
        	}
        }
		
			System.out.println("hashMapOfGIDsandNIDs:"+hashMapOfGIDsandNIDs);
			/** 
			 * 20130813
			 */
			if (null == nameIdsByGermplasmIds){
				throw new GDMSException("Error retrieving list of NIDs for given GIDs. Please provide valid GIDs.");
			} 
			
			if (0 == nameIdsByGermplasmIds.size()){
				throw new GDMSException("Name IDs do not exist for given GIDs. Please provide valid GIDs.");
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
		//String strDatasetNameFromSourceTable = hashMapOfSourceDataRow.get(UploadField.DatasetName.toString());
		String strDatasetDescFromSourceTable = hashMapOfSourceDataRow.get(UploadField.DatasetDescription.toString());
		String strGenusFromSourceTable = "Groundnut";
		String strSpeciesFromSourceTable = "Groundnut";
		String strMissingDataFromSourceTable = "-";
		int marker_id=0;
		Database instance = Database.LOCAL;
		 long datasetLastId = 0;
		 long lastId = 0;
		
		Integer iUserId = 0;
		/*if (null == strPIFromSourceTable || strPIFromSourceTable.equals("")){
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
					if (strName.equals(strPIFromSourceTable)){
						iUserId = user.getUserid();
						break;
					}
				}
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
		//}
		
		//System.out.println("listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
		
		try{
			List<DatasetElement> results =genoManager.getDatasetDetailsByDatasetName(strDatasetSelected, Database.CENTRAL);
			if(results.isEmpty()){			
				results =genoManager.getDatasetDetailsByDatasetName(strDatasetSelected, Database.LOCAL);
				if(results.size()>0)
					throw new GDMSException("Dataset Name already exists.");
			}else 
				throw new GDMSException("Dataset Name already exists.");
		
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		if(strDatasetSelected.length()>30){
			//ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException("Dataset Name value exceeds max char size.");
		}
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
				lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_CHAR_VALUES);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			int maxCHid=(int)lastIdMPId;
			iACId=maxCHid;
	 
	 
		
		
		
		int iNumOfMarkers = listOfMarkerNamesFromSourceTable.size();
		int iNumOfGIDs = listOfGIDFromTable.size();
		//arrayOfMarkers = new Marker[iNumOfMarkers*iNumOfGIDs];

		System.out.println("strDataType=:"+strDataType);
		dataset = new DatasetBean();
		dataset.setDataset_id(iDatasetId);
		dataset.setDataset_name(strDatasetSelected);
		dataset.setDataset_desc(strDatasetDescFromSourceTable);
		dataset.setDataset_type(strDatasetType);
		dataset.setGenus(strGenusFromSourceTable);
		dataset.setSpecies(strSpeciesFromSourceTable);
		dataset.setUpload_template_date(curDate);
		dataset.setRemarks(strRemarks);
		dataset.setDatatype(strDataType);
		dataset.setMissing_data(strMissingDataFromSourceTable);
		dataset.setMethod(method);
		dataset.setScore(strScore);
		localSession.save(dataset);
		
		datasetUser = new GenotypeUsersBean();
		datasetUser.setDataset_id(iDatasetId);
		datasetUser.setUser_id(iUserId);
		localSession.save(datasetUser);
		
		for(int i=0;i<finalList.size();i++){	
        	String[] strList=finalList.get(i).toString().split("~!~");
        	accMetadataSet=new AccessionMetaDataBean();					
			//******************   GermplasmTemp   *********************//*	
        	accMetadataSet.setDataset_id(iDatasetId);
        	accMetadataSet.setGid(Integer.parseInt(strList[0].toString()));
        	accMetadataSet.setNid(Integer.parseInt(strList[1].toString()));
			
        	localSession.save(accMetadataSet);
			
			if (i % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
        
        }
		 ArrayList mids=new ArrayList();
         
         HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
       
		for(int f=0; f<listOfMarkerNamesFromSourceTable.size();f++){
			MarkerInfoBean mib=new MarkerInfoBean();
			if(lstMarkers.contains(listOfMarkerNamesFromSourceTable.get(f))){
				intRMarkerId=(Integer)(markersMap.get(listOfMarkerNamesFromSourceTable.get(f)));							
				mids.add(intRMarkerId);
				finalHashMapMarkerAndIDs.put(listOfMarkerNamesFromSourceTable.get(f).toString(), intRMarkerId);
			}else{
				//maxMid=maxMid+1;
				maxMid=maxMid-1;
				intRMarkerId=maxMid;
				finalHashMapMarkerAndIDs.put(listOfMarkerNamesFromSourceTable.get(f).toString(), intRMarkerId);
				mids.add(intRMarkerId);	
				mib.setMarkerId(intRMarkerId);
				mib.setMarker_type("SNP");
				mib.setMarker_name(listOfMarkerNamesFromSourceTable.get(f).toString());
				//mib.setCrop(sheetSource.getCell(1,5).getContents());
				mib.setSpecies(strSpeciesFromSourceTable);
				
				localSession.save(mib);
				if (f % 1 == 0){
					localSession.flush();
					localSession.clear();
				}
			}
			
			
		}
		
		
		
		
		System.out.println("hashMapOfEntryIDandGID=:"+hashMapOfEntryIDandGID);
		//listOfSNPDataRows = new ArrayList<SNPDataRow>();
		System.out.println("listOfDataRowsFromDataTable=:"+listOfDataRowsFromDataTable.size());
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){			
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);	
			
			String strGID = hashMapOfEntryIDandGID.get(hashMapOfDataRow.get(UploadField.DNA.toString())).toString();
			iACId--;	
			for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {					
				strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));					
				String strMarkerName = listOfMarkerNamesFromSourceTable.get(j);							
				CharArrayBean charValues=new CharArrayBean();
				CharArrayCompositeKey cack = new CharArrayCompositeKey();
				
				//**************** writing to char_values tables........
				cack.setDataset_id(iDatasetId);
				cack.setAc_id(iACId);
				charValues.setComKey(cack);
				charValues.setGid(Integer.parseInt(strGID));
				
				if(strCharValue.length()>2){
					String charStr=strCharValue;
					if(charStr.contains(":")){
						String str1="";
						String str2="";
						//String charStr=strCharValue;
						str1=charStr.substring(0, charStr.length()-2);
						str2=charStr.substring(2, charStr.length());
						charData=str1+"/"+str2;
					}else if(charStr.contains("/")){
						charData=charStr;
					}else if((charStr.equalsIgnoreCase("DUPE"))||(charStr.equalsIgnoreCase("BAD"))){
						charData="?";
					}else{
						throw new GDMSException("Heterozygote data representation should be either : or /"+charStr);
						/* ErrMsg = "Heterozygote data representation should be either : or /"+charStr;
						 request.getSession().setAttribute("indErrMsg", ErrMsg);
						 return "ErrMsg";*/	 
					}
					
				}else if(strCharValue.length()==2){
					String str1="";
					String str2="";
					String charStr=strCharValue;
					str1=charStr.substring(0, charStr.length()-1);
					str2=charStr.substring(1);
					charData=str1+"/"+str2;
					//System.out.println(".....:"+strCharValue.substring(1));
				}else if(strCharValue.length()==1){
					if(strCharValue.equalsIgnoreCase("A")){
						charData="A/A";	
					}else if(strCharValue.equalsIgnoreCase("C")){	
						charData="C/C";
					}else if(strCharValue.equalsIgnoreCase("G")){
						charData="G/G";
					}else if(strCharValue.equalsIgnoreCase("T")){
						charData="T/T";
					}else{
						charData=strCharValue;
					}							
				}			
				
				charValues.setChar_value(charData);
				charValues.setMarker_id(Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName).toString()));
			
				localSession.save(charValues);
				
				if (j % 1 == 0){
					localSession.flush();
					localSession.clear();
				}
				iACId--;
			}
			
			
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
		
		tx.commit();
	
	}

	
	/*protected void saveSNPGenotype() throws GDMSException {
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		genoManager=factory.getGenotypicDataManager();
		try {
			//genoManager.setSNP(accMetadataSet1, markerMetadataSet, datasetUser, charValues, dataset, addedMarker);
			//genotypicDataManagerImpl.setSNP(accMetadataSet, markerMetadataSet, datasetUser, charValues, dataset);
			
			setSNP(Dataset dataset,
		               DatasetUsers datasetUser,
		               List<SNPDataRow> rows)
		               throws MiddlewareQueryException
			
			//20131214: Tulasi
			genoManager.setSNP(dataset, datasetUser, listOfSNPDataRows);
		             
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading SNP Genotype");
		} catch(Exception e1) {
			e1.printStackTrace();
			
		}catch (Throwable th){
			throw new GDMSException("Error uploading SNP Genotype", th);
		} 
		
	}*/
	
	
	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
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
				String strGID = arrayOfMarkers[i].getDbAccessionId();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName + " GID: " + strGID;
				strUploadInfo += strMarker + "\n";
			}*/
			//strDataUploaded = "Uploaded SNP Genotyping dataset \n " +listOfMarkerNamesFromSourceTable;
		strDataUploaded = "Uploaded SNP Genotyping dataset";
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
