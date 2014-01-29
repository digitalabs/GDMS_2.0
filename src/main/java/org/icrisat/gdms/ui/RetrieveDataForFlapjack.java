package org.icrisat.gdms.ui;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.AlleleValuesDAO;
import org.generationcp.middleware.dao.gdms.MappingDataDAO;
import org.generationcp.middleware.dao.gdms.MappingPopDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.MarkerMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.QtlDAO;
import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.AllelicValueWithMarkerIdElement;
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.MapInfo;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.MappingPopValues;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.ParentElement;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
import org.generationcp.middleware.pojos.gdms.QtlDetailsPK;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFlapjackFileFormats;
import org.icrisat.gdms.common.ExportFlapjackFileFormatsGermplasmRetrieval;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.ui.Window.Notification;

public class RetrieveDataForFlapjack {

	List<QtlDetailElement> listOfQtlDetailElementByQtlIds = null;
	private String strDatasetName;
	private String strDatasetID;
	private String strDatasetType = "";
	private String strGenotypingType;
	private Session localSession;
	private Session centralSession;
	private List<Integer> listOfMarkerIdsForGivenDatasetID;
	private List<String> listOfMarkerTypeByMarkerID;
	private String strMarkerType;
	private ArrayList<Integer> listOfParentAGIDs;
	private ArrayList<Integer> listOfParentBGIDs;
	private String strMappingType;
	private List<ParentElement> listOfParentsByDatasetId;
	private List<Integer> listOfAllParentGIDs;
	private ArrayList<Integer> listOfNIDsForAllelicMappingType;
	private List<Integer> listOfDatasetIDs;
	private GDMSMain _mainHomePage;
	private HashMap<Integer, String> hmOfGIdsAndNval;
	private HashMap<String, Integer> hmOfNvalAndGIds;
	private List<Integer> markersL=new ArrayList();;
	private ArrayList<Integer> listOfGIDs;
	private ArrayList<Integer> listOfNIDs;
	private ArrayList<Marker> listOfAllMarkersForGivenDatasetID;
	private HashMap<Integer, String> hmOfMIDandMNames;
	
	private HashMap<Integer, String> hmOfQtlPosition;
	
	private HashMap<String, Integer> hmOfQtlNameId;
	
	private ArrayList<String> listOfMarkerNames;
	private ArrayList<AllelicValueWithMarkerIdElement> listOfAllelicValuesForMappingType;
	private ArrayList<AllelicValueWithMarkerIdElement> listOfAllAllelicValuesForSSRandDArtDatasetType;
	private ArrayList<AllelicValueWithMarkerIdElement> listOfAllAllelicValuesForSNPDatasetType;
	private String strSelectedMap;
	private String strSelectedExportType;
	private TreeMap<Integer, String> sortedMapOfGIDsAndGNames;
	private TreeMap<String, Integer> sortedMapOfGNamesAndGIDs;
	private TreeMap<Integer, String> sortedMapOfMIDsAndMNames;
	private ArrayList<Integer> listOfGIDsToBeExported;
	private ArrayList<String> listOfGNamesToBeExported;
	private Integer iMapId;
	private boolean bQTLExists;
	private ArrayList<QtlDetailElement> listOfAllQTLDetails;
	private ArrayList listOfAllMapInfo;
	private ArrayList<MappingData> listOfAllMappingData;
	private ArrayList<AllelicValueElement> listIfAllelicValueElements;
	private boolean bFlapjackDataBuiltSuccessfully;
	private HashMap<Integer, String> hmOfQtlIdandName;
	private File generatedTextFile;
	private File generatedMapFile;
	private File generatedDatFile;
	private ArrayList<String> listOfGermplasmNamesSelectedForGermplasmRetrieval;
	private ArrayList<String> listOfMarkersForGivenGermplasmRetrieval;
	private ArrayList<Integer> listOfGIDsProvidedForGermplasmRetrieval;
	private ArrayList<Integer> listOfNIDsForGivenGIDs;
	//private ArrayList<AllelicValueElement> listOfAllelicValueElementsForGermplasmNames;
	private ArrayList<Integer> listOfMIDsForGivenGermplasmRetrieval;
	private ArrayList<MappingPopValues> listOfAllMappingPopValuesForGermplasmRetrieval;
	private ArrayList<CharValues> listOfAllCharValuesForGermplasmRetrieval;
	private ArrayList<AllelicValueElement> listOfAllelicValueElementsForGermplasmRetrievals;
	private HashMap<Integer, String> hmOfSelectedMIDandMNames2;
	
	HashMap<Integer, HashMap<String, Object>> mapEx = new HashMap<Integer, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
	HashMap marker = new HashMap();
	
	
	
	//List<Integer> markers=new ArrayList();;
	int parentANid=0;
	int parentBNid=0;
	
	int parentAGid=0;
	int parentBGid=0;
	
	private HashMap<Integer, String> parentsGIDsNames;
	
	ArrayList listOfMarkersinMap;
	
	
	List<AllelicValueWithMarkerIdElement> allelicValues;
	ArrayList intAlleleValues=new ArrayList();
	List<MapInfo> results ;
	ManagerFactory factory=null;
	GenotypicDataManager genoManager;
	GermplasmDataManager germManager;
	public RetrieveDataForFlapjack(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			genoManager=factory.getGenotypicDataManager();
			germManager=factory.getGermplasmDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void setGenotypingType(String theGenotypingType) {
		strGenotypingType = theGenotypingType;
	}

	public void setDatasetName(String theDatasetName) {
		strDatasetName = theDatasetName;
	}

	public void setDatasetID(String theDatasetID) {
		strDatasetID = theDatasetID;
	}

	public void setDatasetType(String theDatasetType) {
		strDatasetType = theDatasetType;
	}

	public void setMapSelected(String theSelectedMap, Integer theMapID) {
		strSelectedMap = theSelectedMap;
		iMapId = theMapID;
	}

	public void setExportType(String theSelectedColumn) {
		strSelectedExportType = theSelectedColumn;
	}


	public void retrieveFlapjackData() {

		bFlapjackDataBuiltSuccessfully = false;
		////System.out.println("strGenotypingType=:"+strGenotypingType);
		if (strGenotypingType.equalsIgnoreCase("Dataset")){

			////System.out.println("RetrieveDataForFlapjack --- retrieveFlapjackData():");
			////System.out.println("DatasetName: " + strDatasetName + " --- " + "Dataset-ID: " + strDatasetID + " --- " +
//					"Dataset-Type:" + strDatasetType);

			listOfDatasetIDs = new ArrayList<Integer>();
			listOfDatasetIDs.add(Integer.parseInt(strDatasetID));

			/**
			 * Retrieving the list of all markers for the Dataset selected
			 */
			try {

				retrieveListOfMarkerIdsForGivenDatasetID();

				retrieveMarkerTypeByMarkerID();
				markersL=new ArrayList();
				if (strDatasetType.equalsIgnoreCase("mapping")){

					retrieveParentAandParentBGIDs();
					////System.out.println("strMappingType=   :"+strMappingType);
					if (strMappingType.equalsIgnoreCase("allelic")){
						retrieveNIDsUsingTheParentGIDsList();
					} 

					retrieveNIDsUsingDatasetID();
					markersL= genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
					
					/*if (strMappingType.equalsIgnoreCase("allelic")){
						retrieveParentGIDsAndGNamesForAllelicType();						
					} else {*/
						Name namesA = null;
						Name namesB = null;						
						parentsGIDsNames= new HashMap<Integer, String>();
						
						namesA=germManager.getGermplasmNameByID(parentANid);
						parentAGid=namesA.getGermplasmId();
						parentsGIDsNames.put(namesA.getGermplasmId(), namesA.getNval());
						
						
						namesB=germManager.getGermplasmNameByID(parentBNid);
						parentBGid=namesB.getGermplasmId();
						parentsGIDsNames.put(namesB.getGermplasmId(), namesB.getNval());
						 
						
						for(int m=0; m<markersL.size(); m++){
							intAlleleValues.add(parentAGid+"!~!"+markersL.get(m)+"!~!"+"A");
						}
						for(int m=0; m<markersL.size(); m++){
							intAlleleValues.add(parentBGid+"!~!"+markersL.get(m)+"!~!"+"B");
						}
						
						retrieveGermplasmNamesByGIDs();
					//}

					if(strMappingType.equalsIgnoreCase("allelic")){
						retrieveAllelicValuesBasedOnMarkerType();
					}

				} else{
					//If strDatasetType is not equal to mapping 
					////System.out.println("Dataset Type is not mapping.");
					markersL= genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
					retrieveGermplasmNamesByGIDs();
					retrieveNIDsFor_SSR_SNP_DArt_DataTypes();
				}
				hmOfMIDandMNames = new HashMap<Integer, String>();
				listOfMarkerNames = new ArrayList<String>();
				listOfMarkersForGivenGermplasmRetrieval=new ArrayList<String>();
				List<MarkerIdMarkerNameElement> markerNames =genoManager.getMarkerNamesByMarkerIds(markersL);
				for (MarkerIdMarkerNameElement e : markerNames) {
		            //Debug.println(0, e.getMarkerId() + " : " + e.getMarkerName());
					if(!listOfMarkerNames.contains(e.getMarkerName())){
						listOfMarkersForGivenGermplasmRetrieval.add(e.getMarkerName());
						listOfMarkerNames.add(e.getMarkerName());
						hmOfMIDandMNames.put(e.getMarkerId(), e.getMarkerName());
					}
		        }
				//Trying to retrieve Germplasm Names with the listOfGIDs obtained till now
				retrieveGermplasmNames();

				//////System.out.println("select marker_id, marker_name from marker where marker_id in("+ mid.substring(0,mid.length()-1) +") order by marker_id ASC");
				//rsM=stmtM.executeQuery("select marker_id, marker_name from gdms_marker where marker_id in("+ mid.substring(0,mid.length()-1) +") order by marker_id ASC");
				//retrieveMarkersForMarkerIDs();

				if(strDatasetType.equalsIgnoreCase("SNP")){
					retrieveValuesForSNPDatasetType();
				}else if((strDatasetType.equalsIgnoreCase("SSR"))||(strDatasetType.equalsIgnoreCase("DArT"))){
					retrieveValuesForSSRandDArtDatasetType();
				}else if(strDatasetType.equalsIgnoreCase("mapping")){
					retrieveValuesForMappingDatasetType();
				}

				sortedMapOfGIDsAndGNames = new TreeMap<Integer, String>();
				Set<Integer> gidKeySet = hmOfGIdsAndNval.keySet();
				Iterator<Integer> gidIterator = gidKeySet.iterator();
				while (gidIterator.hasNext()) {
					Integer gid = gidIterator.next();
					String gname = hmOfGIdsAndNval.get(gid);
					if (strDatasetType.equalsIgnoreCase("mapping")){
						String ParentA=parentsGIDsNames.get(parentAGid);
						String ParentB=parentsGIDsNames.get(parentBGid);
						
						sortedMapOfGIDsAndGNames.put(parentAGid, ParentA);
						sortedMapOfGIDsAndGNames.put(parentBGid, ParentB);
						
					}
					sortedMapOfGIDsAndGNames.put(gid, gname);
				}
				////System.out.println("Size of Sorted Map of GIDs and GNames: " + sortedMapOfGIDsAndGNames.size()+"   "+sortedMapOfGIDsAndGNames);


				sortedMapOfMIDsAndMNames = new TreeMap<Integer, String>();
				Set<Integer> midKeySet = hmOfMIDandMNames.keySet();
				Iterator<Integer> midIterator = midKeySet.iterator();
				while (midIterator.hasNext()) {
					Integer mid = midIterator.next();
					String mname = hmOfMIDandMNames.get(mid);
					sortedMapOfMIDsAndMNames.put(mid, mname);
				}
				////System.out.println("Size of Sorted Map of MIDs and MNames: " + sortedMapOfMIDsAndMNames.size());

				//listOfGIDsToBeExported to be used if exporting based on GIDs
				if (strSelectedExportType.equalsIgnoreCase("GIDs")){
					listOfGIDsToBeExported = new ArrayList<Integer>();
					
					if (strDatasetType.equalsIgnoreCase("mapping")){
						if (false == listOfGIDsToBeExported.contains(parentAGid)){
							listOfGIDsToBeExported.add(parentAGid);
						}
						if (false == listOfGIDsToBeExported.contains(parentBGid)){
							listOfGIDsToBeExported.add(parentBGid);
						}
					}
					
					Iterator<Integer> itrSortedMapGIDs = sortedMapOfGIDsAndGNames.keySet().iterator();
					while (itrSortedMapGIDs.hasNext()){
						Integer iGID = itrSortedMapGIDs.next();
						if (false == listOfGIDsToBeExported.contains(iGID)){
							listOfGIDsToBeExported.add(iGID);
						}
					}
				} else if (strSelectedExportType.equalsIgnoreCase("Germplasm Names")){
					listOfGNamesToBeExported = new ArrayList<String>();
					Iterator<Integer> itrSortedMapGIDs = sortedMapOfGIDsAndGNames.keySet().iterator();
					while (itrSortedMapGIDs.hasNext()){
						Integer iGID = itrSortedMapGIDs.next();
						String strGName = hmOfGIdsAndNval.get(iGID);
						if (strDatasetType.equalsIgnoreCase("mapping")){
							String ParentA=parentsGIDsNames.get(parentAGid);
							String ParentB=parentsGIDsNames.get(parentBGid);
							if (false == listOfGNamesToBeExported.contains(ParentA)){
								listOfGNamesToBeExported.add(ParentA);
							}
							if (false == listOfGNamesToBeExported.contains(ParentB)){
								listOfGNamesToBeExported.add(ParentB);
							}
						}
						if((false == listOfGNamesToBeExported.contains(strGName))&&(strGName!=null)){
							listOfGNamesToBeExported.add(strGName);
						}
					}
				}

				retrieveMapDataForFlapjack();

				retrieveQTLDataForFlapjack();
				////System.out.println("listOfGIDsToBeExported=:"+listOfGNamesToBeExported);

				bFlapjackDataBuiltSuccessfully = true;

				ExportFlapjackFileFormats exportFlapjackFileFormats = new ExportFlapjackFileFormats();
				//////System.out.println("intAlleleValues=:"+intAlleleValues);
				if (strSelectedExportType.equalsIgnoreCase("GIDs")){
					if (strDatasetType.equalsIgnoreCase("SNP")){
						exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
					} else if (strDatasetType.equalsIgnoreCase("SSR") || strDatasetType.equalsIgnoreCase("DArt")){
						exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId,hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
					} else if (strDatasetType.equalsIgnoreCase("Mapping")){
						exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId,hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
					}
				} else if (strSelectedExportType.equalsIgnoreCase("Germplasm Names")) {
					if (strDatasetType.equalsIgnoreCase("SNP")){
						exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, sortedMapOfGIDsAndGNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
					} else if (strDatasetType.equalsIgnoreCase("SSR") || strDatasetType.equalsIgnoreCase("DArt")){
						exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, sortedMapOfGIDsAndGNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
					} else if (strDatasetType.equalsIgnoreCase("Mapping")){
						exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, sortedMapOfGIDsAndGNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId,hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
					}
				}
				generatedTextFile = exportFlapjackFileFormats.getGeneratedTextFile();
				generatedMapFile = exportFlapjackFileFormats.getGeneratedMapFile();
				generatedDatFile = exportFlapjackFileFormats.getGeneratedDatFile();
			} catch (NumberFormatException e) {
				bFlapjackDataBuiltSuccessfully = false;
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given DatasetID", Notification.TYPE_ERROR_MESSAGE);
				return;
			} catch (MiddlewareQueryException e) {
				bFlapjackDataBuiltSuccessfully = false;
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given DatasetID", Notification.TYPE_ERROR_MESSAGE);
				return;
			} catch (GDMSException ge){
				bFlapjackDataBuiltSuccessfully = false;
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given DatasetID. " + ge.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
				return;
			}

		} else {

			
				if (strGenotypingType.equalsIgnoreCase("Germplasm Names") || strGenotypingType.equalsIgnoreCase("GIDs") ||
						strGenotypingType.equalsIgnoreCase("Markers")){
					markersL=new ArrayList();
					try {
						
						retrieveNIDsForGivenGIDs();

						/*retrieveAlleleValuesForGermplasmRetrieval();

						retrieveCharValuesGermplasmRetrieval();

						retrieveMappingPopValuesForGermplasmRetrieval();*/

						//retrieveGermplasmNames();

						//retrieveMarkersForMarkerIDs();
						
						
						retrieveDataForFlapjackFormat();
						

						sortedMapOfGIDsAndGNames = new TreeMap<Integer, String>();
						Set<Integer> gidKeySet = hmOfGIdsAndNval.keySet();
						Iterator<Integer> gidIterator = gidKeySet.iterator();
						while (gidIterator.hasNext()) {
							Integer gid = gidIterator.next();
							String gname = hmOfGIdsAndNval.get(gid);
							sortedMapOfGIDsAndGNames.put(gid, gname);
						}
						TreeMap<String, Integer> sortedMapOfGNamesAndGIDs = new TreeMap<String,Integer>(hmOfNvalAndGIds);
						
						/*sortedMapOfGNamesAndGIDs = new TreeMap<String, Integer>();
						Set<String> gidKeySetN = hmOfNvalAndGIds.keySet();
						Iterator<Integer> gidIteratorN = gidKeySetN.iterator();
						while (gidIteratorN.hasNext()) {
							Integer gid = gidIteratorN.next();
							String gname = hmOfGIdsAndNval.get(gid);
							sortedMapOfGIDsAndGNames.put(gid, gname);
						}*/
						
						////System.out.println("Size of Sorted Map of GIDs and GNames: " + sortedMapOfGIDsAndGNames.size());


						sortedMapOfMIDsAndMNames = new TreeMap<Integer, String>();
						Set<Integer> midKeySet = hmOfMIDandMNames.keySet();
						Iterator<Integer> midIterator = midKeySet.iterator();
						while (midIterator.hasNext()) {
							Integer mid = midIterator.next();
							String mname = hmOfMIDandMNames.get(mid);
							sortedMapOfMIDsAndMNames.put(mid, mname);
						}
						////System.out.println("Size of Sorted Map of MIDs and MNames: " + sortedMapOfMIDsAndMNames.size());

						//listOfGIDsToBeExported to be used if exporting based on GIDs
						if (strSelectedExportType.equalsIgnoreCase("GIDs")){
							listOfGIDsToBeExported = new ArrayList<Integer>();
							
							Iterator<Integer> itrSortedMapGIDs = sortedMapOfGIDsAndGNames.keySet().iterator();
							while (itrSortedMapGIDs.hasNext()){
								Integer iGID = itrSortedMapGIDs.next();
								if (false == listOfGIDsToBeExported.contains(iGID)){
									listOfGIDsToBeExported.add(iGID);
								}
							}
						} else if (strSelectedExportType.equalsIgnoreCase("Germplasm Names")){
							listOfGNamesToBeExported = new ArrayList<String>();
							Iterator<Integer> itrSortedMapGIDs = sortedMapOfGIDsAndGNames.keySet().iterator();
							while (itrSortedMapGIDs.hasNext()){
								Integer iGID = itrSortedMapGIDs.next();
								String strGName = hmOfGIdsAndNval.get(iGID);
								if (false == listOfGNamesToBeExported.contains(strGName)){
									listOfGNamesToBeExported.add(strGName);
								}
							}
						}
						////System.out.println("listOfMIDsForGivenGermplasmRetrieval=:"+listOfMIDsForGivenGermplasmRetrieval);
						markersL=new ArrayList();
						////System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  :"+listOfMarkersForGivenGermplasmRetrieval);
						if(null != listOfMIDsForGivenGermplasmRetrieval){
							////System.out.println("null *********************************************** mlist");
							for(int m=0; m<listOfMIDsForGivenGermplasmRetrieval.size();m++){
								if(listOfMIDsForGivenGermplasmRetrieval.get(m)!=null)
								markersL.add(listOfMIDsForGivenGermplasmRetrieval.get(m));
							}
						}
						////System.out.println("markersL=:"+markersL.size()+"   "+markersL);
						if(markersL.isEmpty()){
							////System.out.println("if m empty ");
							List<Integer> markerIds =genoManager.getMarkerIdsByMarkerNames(listOfMarkersForGivenGermplasmRetrieval, 0, listOfMarkersForGivenGermplasmRetrieval.size(), Database.CENTRAL);
							List<Integer> markerIdsL =genoManager.getMarkerIdsByMarkerNames(listOfMarkersForGivenGermplasmRetrieval, 0, listOfMarkersForGivenGermplasmRetrieval.size(), Database.LOCAL);
							if(!(markerIds.isEmpty())){
								////System.out.println("M from central not empty");
								for(int m=0; m<markerIds.size();m++){
									markersL.add(markerIds.get(m));
								}
							}
							if(!(markerIdsL.isEmpty())){
								////System.out.println("MLK not Empty");
								for(int ml=0; ml<markerIdsL.size();ml++){
									markersL.add(markerIdsL.get(ml));
								}
							}
							
						}
						////System.out.println("markers=:"+markersL);
						retrieveMapDataForFlapjack();

						retrieveQTLDataForFlapjack();

						bFlapjackDataBuiltSuccessfully = true;

						ExportFlapjackFileFormatsGermplasmRetrieval exportFlapjackFileFormats = new ExportFlapjackFileFormatsGermplasmRetrieval();
						
						if (strSelectedExportType.equalsIgnoreCase("GIDs")){
							exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkersForGivenGermplasmRetrieval, sortedMapOfMIDsAndMNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx);
							
						} else if (strSelectedExportType.equalsIgnoreCase("Germplasm Names")) {
							//exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkersForGivenGermplasmRetrieval, sortedMapOfMIDsAndMNames, sortedMapOfGNamesAndGIDs, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx);
							exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkersForGivenGermplasmRetrieval, sortedMapOfMIDsAndMNames, sortedMapOfGNamesAndGIDs, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx);
							
						}
						
						
						generatedTextFile = exportFlapjackFileFormats.getGeneratedTextFile();
						generatedMapFile = exportFlapjackFileFormats.getGeneratedMapFile();
						generatedDatFile = exportFlapjackFileFormats.getGeneratedDatFile();

					} catch (MiddlewareQueryException e) {
						bFlapjackDataBuiltSuccessfully = false;
						_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given Germplasm Names", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}/* else if (strGenotypingType.equalsIgnoreCase("GIDs")){
					//TODO: Build data for Marker Flapjack

				}*/
			//}
		}
	}




	/*private void retrieveAlleleValuesForGermplasmRetrieval() throws MiddlewareQueryException {
		//ResultSet rsa=stmt1.executeQuery("select count(*) from gdms_allele_values where gid in ("+gid+")");

		AlleleValuesDAO alleleValuesDAOLocal = new AlleleValuesDAO();
		alleleValuesDAOLocal.setSession(localSession);
		AlleleValuesDAO alleleValuesDAOCentral = new AlleleValuesDAO();
		alleleValuesDAOCentral.setSession(centralSession);

		long countAlleleValuesByGidsLocal = alleleValuesDAOLocal.countAlleleValuesByGids(listOfGIDs);
		long countAlleleValuesByGidsCentral = alleleValuesDAOCentral.countAlleleValuesByGids(listOfGIDs);

		if (0 == countAlleleValuesByGidsLocal && 0 == countAlleleValuesByGidsCentral){
			return;
		}

		"SELECT distinct gdms_allele_values.gid,gdms_allele_values.allele_bin_value,gdms_marker.marker_name FROM gdms_allele_values,gdms_marker WHERE gdms_allele_values.marker_id=gdms_marker.marker_id AND gdms_allele_values.gid IN ("+gid+") AND gdms_allele_values.marker_id IN (SELECT marker_id FROM gdms_marker WHERE marker_name IN ("+mlist1.substring(0,mlist1.length()-1)+")) ORDER BY gdms_allele_values.gid, gdms_marker.marker_name");
		rsDet=st.executeQuery("SELECT distinct gdms_allele_values.gid,gdms_allele_values.allele_bin_value,gdms_marker.marker_name"+
				" FROM gdms_allele_values,gdms_marker WHERE gdms_allele_values.marker_id=gdms_marker.marker_id"+
				" AND gdms_allele_values.gid IN ("+gid+") AND gdms_allele_values.marker_id IN (SELECT marker_id FROM gdms_marker WHERE marker_name IN ("+mlist1.substring(0,mlist1.length()-1)+")) ORDER BY gdms_allele_values.gid, gdms_marker.marker_name");

		listOfAllelicValueElementsForGermplasmRetrievals = new ArrayList<AllelicValueElement>();

		List<AllelicValueElement> listOfAlleleValuesLocal = alleleValuesDAOLocal.getIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countAlleleValuesByGidsLocal);
		List<AllelicValueElement> listOfAlleleValuesCentral = alleleValuesDAOCentral.getIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countAlleleValuesByGidsCentral);

		for (AllelicValueElement allelicValueElement : listOfAlleleValuesLocal){
			String markerName = allelicValueElement.getMarkerName();
			if (listOfMarkersForGivenGermplasmRetrieval.contains(markerName)){
				if (false == listOfAllelicValueElementsForGermplasmRetrievals.contains(allelicValueElement)){
					listOfAllelicValueElementsForGermplasmRetrievals.add(allelicValueElement);
				}
			}
		}
		for (AllelicValueElement allelicValueElement : listOfAlleleValuesCentral){
			String markerName = allelicValueElement.getMarkerName();
			if (listOfMarkersForGivenGermplasmRetrieval.contains(markerName)){
				if (false == listOfAllelicValueElementsForGermplasmRetrievals.contains(allelicValueElement)){
					listOfAllelicValueElementsForGermplasmRetrievals.add(allelicValueElement);
				}
			}
		}

	}*/


	private void retrieveDataForFlapjackFormat() throws MiddlewareQueryException {
		ArrayList glist = new ArrayList();
		ArrayList midslist = new ArrayList();
		String data="";
		////System.out.println("listOfMarkersSelected=:"+listOfMarkersForGivenGermplasmRetrieval);
		////System.out.println("listOfGIDsSelected:"+listOfGIDs);
		
		Name names = null;
		hmOfNvalAndGIds=new HashMap<String, Integer>();
		hmOfGIdsAndNval=new HashMap<Integer, String>();
		try{
			for(int n=0;n<listOfNIDs.size();n++){
				names=germManager.getGermplasmNameByID(Integer.parseInt(listOfNIDs.get(n).toString()));
				
				hmOfGIdsAndNval.put(names.getGermplasmId(), names.getNval());
				hmOfNvalAndGIds.put(names.getNval(), names.getGermplasmId());
			}
			
		}catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving list of AllelicValueElement for the selected GIDs and markers required for FLAPJACK Format", Notification.TYPE_ERROR_MESSAGE);
			return;
			
		}
		
		
		
		try {
			List<AllelicValueElement> allelicValues =genoManager.getAllelicValuesByGidsAndMarkerNames(listOfGIDs, listOfMarkersForGivenGermplasmRetrieval);
			
			////System.out.println(" allelicValues =:"+allelicValues);		
			marker = new HashMap();
			if (null != allelicValues){
				for (AllelicValueElement allelicValueElement : allelicValues){
					if(!(midslist.contains(allelicValueElement.getMarkerName())))
						midslist.add(allelicValueElement.getMarkerName());
					
					data=data+allelicValueElement.getGid()+"~!~"+allelicValueElement.getData()+"~!~"+allelicValueElement.getMarkerName()+"!~!";
					markerAlleles.put(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerName(), allelicValueElement.getData());
					
					if(!(glist.contains(allelicValueElement.getGid())))
						glist.add(allelicValueElement.getGid());					
				}
				
				List markerKey = new ArrayList();
				markerKey.addAll(markerAlleles.keySet());
				for(int g=0; g<glist.size(); g++){
					for(int i=0; i<markerKey.size();i++){
						 if(!(mapEx.get(Integer.parseInt(glist.get(g).toString()))==null)){
							 marker = (HashMap)mapEx.get(Integer.parseInt(glist.get(g).toString()));
						 }else{
						marker = new HashMap();
						 }
						 if(Integer.parseInt(glist.get(g).toString())==Integer.parseInt(markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!")))){
							 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
							 mapEx.put(Integer.parseInt(glist.get(g).toString()),(HashMap)marker);
						 }						
					}	
				}				
			}
			
		}catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving list of AllelicValueElement for the selected GIDs required for Matrix format.", Notification.TYPE_ERROR_MESSAGE);
				//String strErrMsg = "Error retrieving list of AllelicValueElement for the selected GIDs required for Matrix format.";
				//throw new Exception(strErrMsg);
				
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving list of AllelicValueElement for the selected GIDs and markers required for FLAPJACK Format", Notification.TYPE_ERROR_MESSAGE);
				return;
				
			}
		
	}
	
	
	/*private void retrieveCharValuesGermplasmRetrieval() throws MiddlewareQueryException {
		//ResultSet rsc=stmt2.executeQuery("select count(*) from gdms_char_values where gid in("+gid+")");

		"SELECT DISTINCT gdms_char_values.gid,gdms_char_values.char_value as data,gdms_marker.marker_name"+
		" FROM gdms_char_values,gdms_marker WHERE gdms_char_values.marker_id=gdms_marker.marker_id"+
		" AND gdms_char_values.gid IN ("+gid+") AND gdms_char_values.marker_id IN (SELECT marker_id FROM gdms_marker WHERE marker_name IN ("+mlist1.substring(0,mlist1.length()-1)+")) ORDER BY gdms_char_values.gid, gdms_marker.marker_name"


		CharValuesDAO charValuesDAOLocal = new CharValuesDAO();
		charValuesDAOLocal.setSession(localSession);
		CharValuesDAO charValuesDAOCentral = new CharValuesDAO();
		charValuesDAOCentral.setSession(centralSession);

		long countCharValuesByGidsLocal = charValuesDAOLocal.countCharValuesByGids(listOfGIDs);
		long countCharValuesByGidsCentral = charValuesDAOCentral.countCharValuesByGids(listOfGIDs);

		if (0 == countCharValuesByGidsLocal && 0 == countCharValuesByGidsCentral){
			return;
		}

		List<CharValues> listOfCharValuesLocal = charValuesDAOLocal.getAll();
		List<CharValues> listOfCharValuesCentral = charValuesDAOCentral.getAll();

		listOfAllCharValuesForGermplasmRetrieval = new ArrayList<CharValues>();
		for (CharValues charValues : listOfCharValuesLocal){
			Integer gid = charValues.getgId();
			Integer markerId = charValues.getMarkerId();
			if (listOfGIDsProvidedForGermplasmRetrieval.contains(gid)){
				if (listOfMIDsForGivenGermplasmRetrieval.contains(markerId)){
					if (false == listOfAllCharValuesForGermplasmRetrieval.contains(charValues)){
						listOfAllCharValuesForGermplasmRetrieval.add(charValues);
					}
				}
			}
		}
		for (CharValues charValues : listOfCharValuesCentral){
			Integer gid = charValues.getgId();
			Integer markerId = charValues.getMarkerId();
			if (listOfGIDsProvidedForGermplasmRetrieval.contains(gid)){
				if (listOfMIDsForGivenGermplasmRetrieval.contains(markerId)){
					if (false == listOfAllCharValuesForGermplasmRetrieval.contains(charValues)){
						listOfAllCharValuesForGermplasmRetrieval.add(charValues);
					}
				}
			}
		}
	}


	private void retrieveMappingPopValuesForGermplasmRetrieval() throws MiddlewareQueryException {
		//ResultSet rsMap=stmtM.executeQuery("select count(*) from gdms_mapping_pop_values where gid in("+gid+")");

		"SELECT DISTINCT gdms_mapping_pop_values.gid,gdms_mapping_pop_values.map_char_value as data,gdms_marker.marker_name"+
		" FROM gdms_mapping_pop_values,gdms_marker WHERE gdms_mapping_pop_values.marker_id=gdms_marker.marker_id "+
		" AND gdms_mapping_pop_values.gid IN ("+gid+") AND gdms_mapping_pop_values.marker_id IN (SELECT marker_id FROM gdms_marker WHERE marker_name IN ("+mlist1.substring(0,mlist1.length()-1)+")) ORDER BY gdms_mapping_pop_values.gid, gdms_marker.marker_name"

		MappingPopValuesDAO mappingPopValuesDAOLocal = new MappingPopValuesDAO();
		mappingPopValuesDAOLocal.setSession(localSession);
		MappingPopValuesDAO mappingPopValuesDAOCentral = new MappingPopValuesDAO();
		mappingPopValuesDAOCentral.setSession(centralSession);

		List<MappingPopValues> listOfAllMappingPopValuesLocal = mappingPopValuesDAOLocal.getAll();
		List<MappingPopValues> listOfAllMappingPopValuesCentral = mappingPopValuesDAOCentral.getAll();

		listOfAllMappingPopValuesForGermplasmRetrieval = new ArrayList<MappingPopValues>();

		for (MappingPopValues mappingPopValues : listOfAllMappingPopValuesLocal){
			Integer gid = mappingPopValues.getGid();
			Integer markerId = mappingPopValues.getMarkerId();
			if (listOfGIDsProvidedForGermplasmRetrieval.contains(gid)){
				if (listOfMIDsForGivenGermplasmRetrieval.contains(markerId)){
					if (false == listOfAllMappingPopValuesForGermplasmRetrieval.contains(mappingPopValues)){
						listOfAllMappingPopValuesForGermplasmRetrieval.add(mappingPopValues);
					}
				}
			}
		}
		for (MappingPopValues mappingPopValues : listOfAllMappingPopValuesCentral){
			Integer gid = mappingPopValues.getGid();
			Integer markerId = mappingPopValues.getMarkerId();
			if (listOfGIDsProvidedForGermplasmRetrieval.contains(gid)){
				if (listOfMIDsForGivenGermplasmRetrieval.contains(markerId)){
					if (false == listOfAllMappingPopValuesForGermplasmRetrieval.contains(mappingPopValues)){
						listOfAllMappingPopValuesForGermplasmRetrieval.add(mappingPopValues);
					}
				}
			}
		}

	}
*/
	private void retrieveNIDsForGivenGIDs() throws MiddlewareQueryException {
		//rs=stmt.executeQuery("select nid from gdms_acc_metadataset where gid in ("+gid+") order by gid");
		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);

		List<Integer> listOfNameIdsByGermplasmIdsLocal = accMetadataSetDAOLocal.getNameIdsByGermplasmIds(listOfGIDs);
		List<Integer> listOfNameIdsByGermplasmIdsCentral = accMetadataSetDAOCentral.getNameIdsByGermplasmIds(listOfGIDs);

		listOfNIDs = new ArrayList<Integer>();

		for (Integer iNID : listOfNameIdsByGermplasmIdsLocal){
			if (false == listOfNIDs.contains(iNID)){
				listOfNIDs.add(iNID);
			}
		}
		for (Integer iNID : listOfNameIdsByGermplasmIdsCentral){
			if (false == listOfNIDs.contains(iNID)){
				listOfNIDs.add(iNID);
			}
		}
	}


	public File getGeneratedTextFile() {
		return generatedTextFile;
	}

	public File getGeneratedMapFile() {
		return generatedMapFile;
	}

	public File getGeneratedDatFile() {
		return generatedDatFile;
	}

	private void retrieveAllelicValuesBasedOnMarkerType() throws MiddlewareQueryException {

		listIfAllelicValueElements = new ArrayList<AllelicValueElement>();
		List<AllelicValueElement> listOfAlleleValuesLocal = new ArrayList<AllelicValueElement>();
		List<AllelicValueElement> listOfAlleleValuesCentral = new ArrayList<AllelicValueElement>();
		if (strMarkerType.equalsIgnoreCase("SSR") || strMarkerType.equalsIgnoreCase("DArT")){

			//"select gid,marker_id, allele_bin_value from gdms_allele_values where gid in("+pgids.substring(0,pgids.length()-1)+") and marker_id in("+mid.substring(0,mid.length()-1)+") order by gid, marker_id"
			AlleleValuesDAO alleleValuesDAOLocal = new AlleleValuesDAO();
			alleleValuesDAOLocal.setSession(localSession);
			AlleleValuesDAO alleleValuesDAOCentral = new AlleleValuesDAO();
			alleleValuesDAOCentral.setSession(centralSession);

			long countIntAlleleValuesLocal = alleleValuesDAOLocal.countIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs);
			listOfAlleleValuesLocal = alleleValuesDAOLocal.getIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countIntAlleleValuesLocal);

			long countIntAlleleValuesCentral = alleleValuesDAOCentral.countIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs);
			listOfAlleleValuesCentral = alleleValuesDAOCentral.getIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countIntAlleleValuesCentral);
		} else if (strMarkerType.equalsIgnoreCase("SNP")){

			//"select gid,marker_id, char_value from gdms_char_values where gid in("+pgids.substring(0,pgids.length()-1)+") and marker_id in("+mid.substring(0,mid.length()-1)+") order by gid, marker_id"
			AlleleValuesDAO alleleValuesDAOLocal = new AlleleValuesDAO();
			alleleValuesDAOLocal.setSession(localSession);
			AlleleValuesDAO alleleValuesDAOCentral = new AlleleValuesDAO();
			alleleValuesDAOCentral.setSession(centralSession);

			long countIntAlleleValuesLocal = alleleValuesDAOLocal.countCharAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs);
			listOfAlleleValuesLocal = alleleValuesDAOLocal.getCharAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countIntAlleleValuesLocal);

			long countIntAlleleValuesCentral = alleleValuesDAOCentral.countCharAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs);
			listOfAlleleValuesCentral = alleleValuesDAOCentral.getCharAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countIntAlleleValuesCentral);

		}

		for (AllelicValueElement allelicValueElement : listOfAlleleValuesLocal){
			if (false == listIfAllelicValueElements.contains(allelicValueElement)){
				listIfAllelicValueElements.add(allelicValueElement);
			}
		}
		for (AllelicValueElement allelicValueElement : listOfAlleleValuesCentral){
			if (false == listIfAllelicValueElements.contains(allelicValueElement)){
				listIfAllelicValueElements.add(allelicValueElement);
			}
		}

		////System.out.println("Size of list of list of AllelicValueElements for " + strMarkerType + " type are : " + 
				//listIfAllelicValueElements.size());


		for (AllelicValueElement allelicValueElement : listIfAllelicValueElements){
			Integer gid = allelicValueElement.getGid();
			String markerName = allelicValueElement.getMarkerName();
			String alleleBinValue = allelicValueElement.getAlleleBinValue();
			String data = allelicValueElement.getData();
		}

	}

	private void retrieveParentGIDsAndGNamesForAllelicType() throws MiddlewareQueryException {

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);

		List<Integer> listOfNameIdsByGermplasmIdsLocal = accMetadataSetDAOLocal.getNameIdsByGermplasmIds(listOfAllParentGIDs);
		List<Integer> listOfNameIdsByGermplasmIdsCentral = accMetadataSetDAOCentral.getNameIdsByGermplasmIds(listOfAllParentGIDs);

		if (null == listOfGIDs){
			listOfGIDs = new ArrayList<Integer>();
		}

		for (Integer iGID : listOfNameIdsByGermplasmIdsLocal){
			if (false == listOfGIDs.contains(iGID)){
				listOfGIDs.add(iGID);
			}
		}
		for (Integer iGID : listOfNameIdsByGermplasmIdsCentral){
			if (false == listOfGIDs.contains(iGID)){
				listOfGIDs.add(iGID);
			}
		}

		retrieveGermplasmNames(); //Adds GIDs and GNames to hmOfGIdsAndNval

	}

	private void retrieveQTLDataForFlapjack() throws MiddlewareQueryException {
		//ResultSet rsMap=st.executeQuery("select qtl_id from gdms_qtl_details where map_id =(select map_id from gdms_map where map_name ='"+mapName+"')");
		QtlDetailsDAO qtlDetailsDAOLocal = new QtlDetailsDAO();
		qtlDetailsDAOLocal.setSession(localSession);
		QtlDetailsDAO qtlDetailsDAOCentral = new QtlDetailsDAO();
		qtlDetailsDAOCentral.setSession(centralSession);

		List<QtlDetails> listOfAllQTLsLocal = qtlDetailsDAOLocal.getAll();
		List<QtlDetails> listOfAllQTLsCentral = qtlDetailsDAOCentral.getAll();
		listOfAllQTLDetails = new ArrayList<QtlDetailElement>();
		ArrayList<Integer> listOfAllQTLIDs = new ArrayList<Integer>();

		hmOfQtlPosition = new HashMap<Integer, String>();
		
		//hmOfQtlIdName = new HashMap<QtlDetailsPK, String>();
		
		ArrayList QTLNames=new ArrayList();
		////System.out.println("...............iMapId:"+iMapId);
		
		
		for (QtlDetails qtlDetails : listOfAllQTLsLocal){
			QtlDetailsPK qtlPK = qtlDetails.getId();
			Integer mapId = qtlPK.getMapId();
			if (mapId.equals(iMapId)){
				/*if (false == listOfAllQTLDetails.contains(qtlDetails)) {
					listOfAllQTLDetails.add(qtlDetails);
				}*/
				//QTLNames.add(qtlDetails.getId(), qtlDetails.getPosition().toString());
				
				
				Integer qtlId = qtlPK.getQtlId();
				if (false == listOfAllQTLIDs.contains(qtlId)){
					hmOfQtlPosition.put(qtlId, qtlDetails.getPosition().toString());
					listOfAllQTLIDs.add(qtlId);
				}
			}
		}
		for (QtlDetails qtlDetails : listOfAllQTLsCentral){
			QtlDetailsPK qtlPK = qtlDetails.getId();
			Integer mapId = qtlPK.getMapId();
			if (mapId.equals(iMapId)){
				/*if (false == listOfAllQTLDetails.contains(qtlDetails)) {
					listOfAllQTLDetails.add(qtlDetails);
				}*/
				qtlDetails.getPosition();
				Integer qtlId = qtlPK.getQtlId();
				if (false == listOfAllQTLIDs.contains(qtlId)){
					hmOfQtlPosition.put(qtlId, qtlDetails.getPosition().toString());
					listOfAllQTLIDs.add(qtlId);
				}
			}
		}

		////System.out.println("listOfAllQTLDetails=:"+listOfAllQTLDetails);
		
		QtlDAO qtlDAOLocal = new QtlDAO();
		qtlDAOLocal.setSession(localSession);
		QtlDAO qtlDAOCentral = new QtlDAO();
		qtlDAOCentral.setSession(centralSession);

		List<Qtl> listOfAllQtlsLocal = qtlDAOLocal.getAll();
		List<Qtl> listOfAllQtlsCentral = qtlDAOCentral.getAll();

		hmOfQtlIdandName = new HashMap<Integer, String>();
		hmOfQtlNameId = new HashMap<String, Integer>();
		
		for (Integer iQtlId : listOfAllQTLIDs){
			for (Qtl qtlLocal : listOfAllQtlsLocal){
				Integer qtlId = qtlLocal.getQtlId();
				if (iQtlId.equals(qtlId)){
					String qtlName = qtlLocal.getQtlName();
					if (false == hmOfQtlIdandName.containsKey(qtlId)){
						hmOfQtlNameId.put(qtlName, qtlId);
						hmOfQtlIdandName.put(qtlId, qtlName);
					}
				}
			}

			for (Qtl qtlCentral : listOfAllQtlsCentral){
				Integer qtlId = qtlCentral.getQtlId();
				if (iQtlId.equals(qtlId)){
					String qtlName = qtlCentral.getQtlName();
					if (false == hmOfQtlIdandName.containsKey(qtlId)){
						hmOfQtlNameId.put(qtlName, qtlId);
						hmOfQtlIdandName.put(qtlId, qtlName);
					}
					
				
				}
			}
		}
		//getAllelicValuesByGidsAndMarkerNames
		

		listOfQtlDetailElementByQtlIds = genoManager.getQtlByQtlIds(listOfAllQTLIDs, 0, (int)listOfAllQTLIDs.size());
	//}
		////System.out.println(",,,,,,,,,,,,,,,,,,,:"+listOfAllQTLDetails);
	
	if (null != listOfQtlDetailElementByQtlIds) {
		for (QtlDetailElement qtlDetailElement : listOfQtlDetailElementByQtlIds) {
			String strMapName = qtlDetailElement.getMapName();
			String strTRName = qtlDetailElement.getTRName();
			
			String strChromosome = qtlDetailElement.getChromosome();
			String strMinPosition = String.valueOf(qtlDetailElement.getMinPosition().floatValue());
			String strMaxPosition = String.valueOf(qtlDetailElement.getMaxPosition().floatValue());
			
			/*
			String strQTLData = strQtlName + "!~!" + strMapName + "!~!" + strTRName + "!~!" + strChromosome +
					               "!~!" +  strMinPosition + "!~!" + strMaxPosition ;
			arrayListOfQTLRetrieveDataLocal.add(qtlDetailElement);*/
			////System.out.println(qtlDetailElement);
			listOfAllQTLDetails.add(qtlDetailElement);
		}
	}
		
		
		
		/*for (QtlDetails qtlDetails : listOfAllQTLsLocal){
			QtlDetailsPK qtlPK = qtlDetails.getId();
			Integer mapId = qtlPK.getMapId();
			if (mapId.equals(iMapId)){
				Integer qtlId = qtlPK.getQtlId();
				if (false == listOfAllQtlIDs.contains(qtlId)) {
					listOfAllQtlIDs.add(qtlId);
				}
			}
		}
		for (QtlDetails qtlDetails : listOfAllQTLsCentral){
			QtlDetailsPK qtlPK = qtlDetails.getId();
			Integer mapId = qtlPK.getMapId();
			if (mapId.equals(iMapId)){
				Integer qtlId = qtlPK.getQtlId();
				if (false == listOfAllQtlIDs.contains(qtlId)) {
					listOfAllQtlIDs.add(qtlId);
				}
			}
		}*/

		if (listOfAllQTLDetails.size() > 0){
			bQTLExists = true;
			/*for (Integer iQtlID : listOfAllQtlIDs){
				QtlDetails qtlDetailsLocal = qtlDetailsDAOLocal.getById(iQtlID, false);
				QtlDetails qtlDetailsCentral = qtlDetailsDAOCentral.getById(iQtlID, false);
				if (false == listOfAllQTLDetails.contains(qtlDetailsLocal)){
					listOfAllQTLDetails.add(qtlDetailsLocal);
				}
				if (false == listOfAllQTLDetails.contains(qtlDetailsCentral)){
					listOfAllQTLDetails.add(qtlDetailsLocal);
				}
			}*/



		} else {
			bQTLExists = false;
		}
		//Error generating Flapjack.txt file

	}

	private void retrieveMapDataForFlapjack() throws MiddlewareQueryException {
		////System.out.println("//////////////////////////    retrieveMapDataForFlapjack    ///////////////////////////// "+strSelectedMap);
		/*MappingDataDAO mappingDataDAOLocal = new MappingDataDAO();
		mappingDataDAOLocal.setSession(localSession);
		MappingDataDAO mappingDataDAOCentral = new MappingDataDAO();
		mappingDataDAOCentral.setSession(centralSession);*/
		listOfAllMapInfo=new ArrayList();
		listOfMarkersinMap=new ArrayList();
		if(strSelectedMap.isEmpty()){
			////System.out.println("...........................   MAP NOT SELECTED   ...........................");
			////System.out.println("markersL.size():"+markersL.size());
			for(int m=0; m<listOfMarkersForGivenGermplasmRetrieval.size(); m++){				
				listOfAllMapInfo.add(listOfMarkersForGivenGermplasmRetrieval.get(m)+"!~!"+"unmapped"+"!~!"+"0");				
			}			
		        
		}else{
			////System.out.println("map selected ");			
			 results = genoManager.getMapInfoByMapName(strSelectedMap, Database.CENTRAL);
		     if(results == null) {
		    	 results = genoManager.getMapInfoByMapName(strSelectedMap, Database.LOCAL);
		     }
			 
		     listOfAllMapInfo = new ArrayList();
			 ////System.out.println("testGetMapInfoByMapName(mapName=" + strSelectedMap + ") RESULTS size: " + results.size());
	        for (MapInfo mapInfo : results){
	        	listOfMarkersinMap.add(mapInfo.getMapName().toLowerCase());	        	
	        	//////System.out.println(".................mapInfo."+results);
	        	listOfAllMapInfo.add(mapInfo.getMarkerName()+"!~!"+mapInfo.getLinkageGroup()+"!~!"+mapInfo.getStartPosition());
	        }	
	        
	        for(int m=0; m<listOfMarkersForGivenGermplasmRetrieval.size(); m++){
	        	if(!(listOfMarkersinMap.contains(listOfMarkersForGivenGermplasmRetrieval.get(m).toString().toLowerCase()))){
	        		//listOfAllMapInfo.add(sortedMapOfMIDsAndMNames.get(listOfMarkersForGivenGermplasmRetrieval.get(m))+"!~!"+"unmapped"+"!~!"+"0");		
	        		listOfAllMapInfo.add(listOfMarkersForGivenGermplasmRetrieval.get(m)+"!~!"+"unmapped"+"!~!"+"0");
	        	}
			}
	        
		}
		
	}


	private void retrieveValuesForSNPDatasetType() throws NumberFormatException, MiddlewareQueryException {
		int iDatasetId = Integer.parseInt(strDatasetID);		
		////System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS  :"+iDatasetId+"   "+strDatasetID);
		allelicValues=new ArrayList();
		allelicValues =genoManager.getAllelicValuesFromCharValuesByDatasetId(iDatasetId, 0, (int)genoManager.countAllelicValuesFromCharValuesByDatasetId(iDatasetId));
		for(AllelicValueWithMarkerIdElement results : allelicValues) {
			//listOfAllAllelicValuesForSSRandDArtDatasetType.add(results);
			intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
        }
		//System.out.println("Size of Allelic Value Elements for " +  strDatasetType + " Datatype: " + intAlleleValues.size());
	}

	private void retrieveValuesForSSRandDArtDatasetType() throws MiddlewareQueryException {
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		allelicValues=new ArrayList();
		int iDatasetId = Integer.parseInt(strDatasetID);
		//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS  :"+iDatasetId+"   "+strDatasetID);
		allelicValues =genoManager.getAllelicValuesFromAlleleValuesByDatasetId(iDatasetId, 0, (int)genoManager.countAllelicValuesFromAlleleValuesByDatasetId(iDatasetId));
		for(AllelicValueWithMarkerIdElement results : allelicValues) {
			//listOfAllAllelicValuesForSSRandDArtDatasetType.add(results);
			intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
        }
		//System.out.println("Size of Allelic Value Elements for " +  strDatasetType + " Datatype: " + intAlleleValues.size()+"   :"+intAlleleValues);
	}

	private void retrieveValuesForMappingDatasetType() throws MiddlewareQueryException {
		////System.out.println("select gid, marker_id, map_char_value from gdms_mapping_pop_values where dataset_id="+datasetId+" ORDER BY gid, marker_id ASC");
		//rsD=st.executeQuery("select gid, marker_id, map_char_value from gdms_mapping_pop_values where dataset_id="+datasetId+" ORDER BY gid, marker_id ASC");

		listOfAllelicValuesForMappingType = new ArrayList<AllelicValueWithMarkerIdElement>();

		int iDatasetId = Integer.parseInt(strDatasetID);
		allelicValues=new ArrayList();
		allelicValues =genoManager.getAllelicValuesFromMappingPopValuesByDatasetId(iDatasetId, 0, (int)genoManager.countAllelicValuesFromMappingPopValuesByDatasetId(iDatasetId));
		for(AllelicValueWithMarkerIdElement results : allelicValues) {
			//listOfAllAllelicValuesForSSRandDArtDatasetType.add(results);
			intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
        }
		//System.out.println("Size of Allelic Value Elements for " +  strDatasetType + " Datatype: " + intAlleleValues.size());
	}
	
	
	
	
	

	private void retrieveMarkersForMarkerIDs() throws MiddlewareQueryException {
		MarkerDAO markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		MarkerDAO markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);

		hmOfMIDandMNames = new HashMap<Integer, String>();
		listOfMarkerNames = new ArrayList<String>();


		long countMarkersByIdsLocal = markerDAOLocal.countMarkersByIds(listOfMarkerIdsForGivenDatasetID);
		List<Marker> listOfMarkersByIdsLocal = markerDAOLocal.getMarkersByIds(listOfMarkerIdsForGivenDatasetID, 0, (int)countMarkersByIdsLocal);

		long countMarkersByIdsCentral = markerDAOCentral.countMarkersByIds(listOfMarkerIdsForGivenDatasetID);
		List<Marker> listOfMarkersByIdsCentral = markerDAOCentral.getMarkersByIds(listOfMarkerIdsForGivenDatasetID, 0, (int)countMarkersByIdsCentral);

		listOfAllMarkersForGivenDatasetID = new ArrayList<Marker>();

		for (Marker marker : listOfMarkersByIdsLocal){
			if (false == listOfAllMarkersForGivenDatasetID.contains(marker)){
				listOfAllMarkersForGivenDatasetID.add(marker);
			}
		}
		for (Marker marker : listOfMarkersByIdsCentral){
			if (false == listOfAllMarkersForGivenDatasetID.contains(marker)){
				listOfAllMarkersForGivenDatasetID.add(marker);
			}
		}

		//System.out.println("Size of List of all Markers obtained for given Dataset-ID: " + listOfAllMarkersForGivenDatasetID.size());

		for (Marker marker : listOfAllMarkersForGivenDatasetID){
			Integer markerId = marker.getMarkerId();
			String markerName = marker.getMarkerName();

			if (false == hmOfMIDandMNames.containsKey(markerId)){
				hmOfMIDandMNames.put(markerId, markerName);
			}

			if (false == listOfMarkerNames.contains(markerName)){
				listOfMarkerNames.add(markerName);
			}
		}

		//System.out.println("Size of list of all Marker-Names: " + listOfMarkerNames.size());
		//System.out.println("Size of Hashmap of MIDs and MNames: " + hmOfMIDandMNames.size());
	}

	private void retrieveGermplasmNames() throws MiddlewareQueryException {
		NameDAO nameDAOLocal = new NameDAO();
		nameDAOLocal.setSession(localSession);
		NameDAO nameDAOCentral = new NameDAO();
		nameDAOCentral.setSession(centralSession);

		if (null == hmOfGIdsAndNval){
			hmOfGIdsAndNval = new HashMap<Integer, String>();
		}

		List<Name> listOfNamesByNameIdsLocal = nameDAOLocal.getNamesByNameIds(listOfGIDs);
		List<Name> listOfNamesByNameIdsCentral = nameDAOCentral.getNamesByNameIds(listOfGIDs);

		for (Name name : listOfNamesByNameIdsLocal){
			Integer nid = name.getNid();
			if (false == hmOfGIdsAndNval.containsKey(nid)){
				String nval = name.getNval();
				//hmOfGIdsAndNval.put(nid, nval);
				hmOfGIdsAndNval.put(name.getGermplasmId(), nval);
			}
		}

		for (Name name : listOfNamesByNameIdsCentral){
			Integer nid = name.getNid();
			if (false == hmOfGIdsAndNval.containsKey(nid)){
				String nval = name.getNval();
				hmOfGIdsAndNval.put(name.getGermplasmId(), nval);
			}
		}

		//System.out.println("Size of Germplasm Mapping - hashmap: " + hmOfGIdsAndNval.size());
	}

	private void retrieveNIDsFor_SSR_SNP_DArt_DataTypes() throws MiddlewareQueryException {
		//"SELECT nid from gdms_acc_metadataset where dataset_id="+datasetId
		//List<Integer> 	getNIDsByDatasetIds(List<Integer> datasetIds, List<Integer> gids, int start, int numOfRows)

		listOfGIDs = new ArrayList<Integer>();

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);


		long countAll = accMetadataSetDAOLocal.countAll();
		List<Integer> niDsByDatasetIds = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetIDs, new ArrayList<Integer>(), 0, (int)countAll);
		List<Integer> niDsByDatasetIds2 = accMetadataSetDAOCentral.getNIDsByDatasetIds(listOfDatasetIDs, new ArrayList<Integer>(), 0, (int)countAll);
		for (Integer iNid : niDsByDatasetIds){
			if (false == listOfGIDs.contains(iNid)){
				listOfGIDs.add(iNid);
			}
		}
		for (Integer iNid : niDsByDatasetIds2){
			if (false == listOfGIDs.contains(iNid)){
				listOfGIDs.add(iNid);
			}
		}

		//System.out.println("Size of List Of NIDs for given Dataset-Id: " + listOfGIDs.size());
	}

	private void retrieveGermplasmNamesByGIDs() throws MiddlewareQueryException {
		/*NameDAO nameDAOLocal = new NameDAO();
		nameDAOLocal.setSession(localSession);
		NameDAO nameDAOCentral = new NameDAO();
		nameDAOCentral.setSession(centralSession);

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);*/
		hmOfGIdsAndNval = new HashMap<Integer, String>();
		List<Integer> nids =genoManager.getNidsFromAccMetadatasetByDatasetIds(listOfDatasetIDs, 0, (int)genoManager.countNidsFromAccMetadatasetByDatasetIds(listOfDatasetIDs));
		Name names = null;
		for(int n=0;n<nids.size();n++){
			names=germManager.getGermplasmNameByID(nids.get(n));
			hmOfGIdsAndNval.put(names.getGermplasmId(), names.getNval());
		}
		
		
		

	}

	private void retrieveNIDsUsingDatasetID() throws MiddlewareQueryException {

		//"SELECT nid from gdms_acc_metadataset where dataset_id="+datasetId+" and gid not in("+parentsNames+") order by nid"
		//Set<Integer> 	getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(List<Integer> datasetIds, List<Integer> markerIds, List<Integer> gIds)

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);

		listOfGIDs = new ArrayList<Integer>();
		Set<Integer> nIdsByMarkerIdsAndDatasetIdsAndNotGIdsLocal = accMetadataSetDAOLocal.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIDs, listOfMarkerIdsForGivenDatasetID, listOfAllParentGIDs, 0, (int)accMetadataSetDAOLocal.countNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIDs, listOfMarkerIdsForGivenDatasetID, listOfAllParentGIDs));
		Set<Integer> nIdsByMarkerIdsAndDatasetIdsAndNotGIdsCentral = accMetadataSetDAOCentral.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIDs, listOfMarkerIdsForGivenDatasetID, listOfAllParentGIDs, 0, (int)accMetadataSetDAOLocal.countNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIDs, listOfMarkerIdsForGivenDatasetID, listOfAllParentGIDs));

		Iterator<Integer> iteratorLocal = nIdsByMarkerIdsAndDatasetIdsAndNotGIdsLocal.iterator();
		while (iteratorLocal.hasNext()){
			Integer next = iteratorLocal.next();
			if (false == listOfGIDs.contains(next)){
				listOfGIDs.add(next);
			}
		}
		Iterator<Integer> iteratorCentral = nIdsByMarkerIdsAndDatasetIdsAndNotGIdsCentral.iterator();
		while (iteratorCentral.hasNext()){
			Integer next = iteratorCentral.next();
			if (false == listOfGIDs.contains(next)){
				listOfGIDs.add(next);
			}
		}
	}

	private void retrieveNIDsUsingTheParentGIDsList() throws MiddlewareQueryException {
		listOfNIDsForAllelicMappingType = new ArrayList<Integer>();

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);

		//List<Integer> getNIDsByDatasetIds(List<Integer> datasetIds, List<Integer> gids, int start, int numOfRows) 

		long countAccMetadataSetByParentAGids1 = accMetadataSetDAOLocal.countAccMetadataSetByGids(listOfParentAGIDs);
		List<Integer> listOfNIDsByParentAGIDLocal = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetIDs, listOfParentAGIDs, 0, (int)countAccMetadataSetByParentAGids1);
		for (Integer iNid : listOfNIDsByParentAGIDLocal){
			if (false == listOfNIDsForAllelicMappingType.contains(iNid)){
				listOfNIDsForAllelicMappingType.add(iNid);
			}
		}

		long countAccMetadataSetByParentAGids2 = accMetadataSetDAOCentral.countAccMetadataSetByGids(listOfParentAGIDs);
		List<Integer> listOfNIDsByParentAGIDCentral = accMetadataSetDAOCentral.getNIDsByDatasetIds(listOfDatasetIDs, listOfParentAGIDs, 0, (int)countAccMetadataSetByParentAGids2);
		for (Integer iNid : listOfNIDsByParentAGIDCentral){
			if (false == listOfNIDsForAllelicMappingType.contains(iNid)){
				listOfNIDsForAllelicMappingType.add(iNid);
			}
		}

		long countAccMetadataSetByParentBGids1 = accMetadataSetDAOLocal.countAccMetadataSetByGids(listOfParentBGIDs);
		List<Integer> listOfNIDsByParentBGIDsLocal = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetIDs, listOfParentBGIDs, 0, (int)countAccMetadataSetByParentBGids1);
		for (Integer iNid : listOfNIDsByParentBGIDsLocal){
			if (false == listOfNIDsForAllelicMappingType.contains(iNid)){
				listOfNIDsForAllelicMappingType.add(iNid);
			}
		}

		long countAccMetadataSetByParentBGids2 = accMetadataSetDAOLocal.countAccMetadataSetByGids(listOfParentBGIDs);
		List<Integer> listOfNIDsByParentBGIDsCentral = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetIDs, listOfParentBGIDs, 0, (int)countAccMetadataSetByParentBGids2);
		for (Integer iNid : listOfNIDsByParentBGIDsCentral){
			if (false == listOfNIDsForAllelicMappingType.contains(iNid)){
				listOfNIDsForAllelicMappingType.add(iNid);
			}
		}
	}

	private void retrieveListOfMarkerIdsForGivenDatasetID() throws NumberFormatException, MiddlewareQueryException {

		listOfMarkerIdsForGivenDatasetID = new ArrayList<Integer>();

		MarkerMetadataSetDAO markerMetadatsSetDAOLocal = new MarkerMetadataSetDAO();
		markerMetadatsSetDAOLocal.setSession(localSession);
		MarkerMetadataSetDAO markerMetadataSetDAOCentral = new MarkerMetadataSetDAO();
		markerMetadataSetDAOCentral.setSession(centralSession);

		List<Integer> markerIdsByDatasetIdLocal = markerMetadatsSetDAOLocal.getMarkerIdByDatasetId(Integer.parseInt(strDatasetID));
		List<Integer> markerIdsByDatasetIdCentral = markerMetadataSetDAOCentral.getMarkerIdByDatasetId(Integer.parseInt(strDatasetID));

		for (Integer iMID : markerIdsByDatasetIdLocal){
			if (false == listOfMarkerIdsForGivenDatasetID.contains(iMID)){
				listOfMarkerIdsForGivenDatasetID.add(iMID);
			}
		}

		for (Integer iMID : markerIdsByDatasetIdCentral){
			if (false == listOfMarkerIdsForGivenDatasetID.contains(iMID)){
				listOfMarkerIdsForGivenDatasetID.add(iMID);
			}
		}
	}


	private void retrieveMarkerTypeByMarkerID() throws MiddlewareQueryException, GDMSException {
		listOfMarkerTypeByMarkerID = new ArrayList<String>();

		MarkerDAO markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		MarkerDAO markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);

		List<String> listOfMarkerTypeByMarkerIdsLocal = markerDAOLocal.getMarkerTypeByMarkerIds(listOfMarkerIdsForGivenDatasetID);
		List<String> listOfMarkerTypeByMarkerIdsCentral = markerDAOCentral.getMarkerTypeByMarkerIds(listOfMarkerIdsForGivenDatasetID);

		for (String markerType : listOfMarkerTypeByMarkerIdsLocal){
			if (false == listOfMarkerTypeByMarkerID.contains(markerType)){
				listOfMarkerTypeByMarkerID.add(markerType);
			}
		}
		for (String markerType : listOfMarkerTypeByMarkerIdsCentral){
			if (false == listOfMarkerTypeByMarkerID.contains(markerType)){
				listOfMarkerTypeByMarkerID.add(markerType);
			}
		}
		
		if (0 == listOfMarkerTypeByMarkerID.size()){
			throw new GDMSException("Marker Type could not be obtained");
		}
		
		strMarkerType = listOfMarkerTypeByMarkerID.get(0);
	}

	private void retrieveParentAandParentBGIDs() throws NumberFormatException, MiddlewareQueryException, GDMSException {
		listOfParentsByDatasetId = new ArrayList<ParentElement>();

		MappingPopDAO mappingPopDAOLocal = new MappingPopDAO();
		mappingPopDAOLocal.setSession(localSession);
		MappingPopDAO mappingPopDAOCentral = new MappingPopDAO();
		mappingPopDAOCentral.setSession(centralSession);

		List<ParentElement> parentsByDatasetIdLocal = mappingPopDAOLocal.getParentsByDatasetId(Integer.parseInt(strDatasetID));
		List<ParentElement> parentsByDatasetIdCentral = mappingPopDAOCentral.getParentsByDatasetId(Integer.parseInt(strDatasetID));
		
		List<ParentElement> results = genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID));
		for (ParentElement parentElement : results){
			////System.out.println(parentElement.getParentANId()+"   "+parentElement.getParentBGId());
			parentANid=parentElement.getParentANId();			
			parentBNid=parentElement.getParentBGId();
		}
		

		for (ParentElement parentElement : parentsByDatasetIdLocal){
			if (false == listOfParentsByDatasetId.contains(parentElement)){
				listOfParentsByDatasetId.add(parentElement);
			}
		}
		for (ParentElement parentElement : parentsByDatasetIdCentral){
			if (false == listOfParentsByDatasetId.contains(parentElement)){
				listOfParentsByDatasetId.add(parentElement);
			}
		}

		listOfParentAGIDs = new ArrayList<Integer>();
		listOfParentBGIDs = new ArrayList<Integer>();
		listOfAllParentGIDs = new ArrayList<Integer>(); 

		for (ParentElement parentElement : listOfParentsByDatasetId){
			listOfParentAGIDs.add(parentElement.getParentANId());
			listOfParentBGIDs.add(parentElement.getParentBGId());
		}

		for (Integer parentAGID : listOfParentAGIDs){
			if (false == listOfAllParentGIDs.contains(parentAGID)){
				listOfAllParentGIDs.add(parentAGID);
			}
		}

		for (Integer parentBGID : listOfParentBGIDs){
			if (false == listOfAllParentGIDs.contains(parentBGID)){
				listOfAllParentGIDs.add(parentBGID);
			}
		}
		
		if (0 == listOfParentsByDatasetId.size()){
			throw new GDMSException("Mapping Type could not be obtained");
		}
		strMappingType = listOfParentsByDatasetId.get(0).getMappingType();
	}

	public boolean isFlapjackDataBuiltSuccessfully() {
		return bFlapjackDataBuiltSuccessfully;
	}

	public void setListOfGermplasmsProvided(ArrayList<String> theListOfGermplasmNamesSelected) {
		listOfGermplasmNamesSelectedForGermplasmRetrieval = theListOfGermplasmNamesSelected;
	}

	public void setListOfMarkersSelected(ArrayList<String> theListOfMarkersSelected) {
		listOfMarkersForGivenGermplasmRetrieval = theListOfMarkersSelected;
	}

	public void setListOfGIDsSelected(ArrayList<Integer> theListOfGIDsSelected) {
		listOfGIDsProvidedForGermplasmRetrieval = theListOfGIDsSelected;
		listOfGIDs = theListOfGIDsSelected;
	}

	public void setListOfMIDsSelected(ArrayList<Integer> listOfAllMIDsSelected) {
		listOfMIDsForGivenGermplasmRetrieval = listOfAllMIDsSelected;
	}

	public void setHashmapOfSelectedMIDsAndMNames(HashMap<Integer, String> hmOfSelectedMIDandMNames) {
		hmOfMIDandMNames = hmOfSelectedMIDandMNames;
	}

	public void setHashmapOfSelectedGIDsAndGNames(
			HashMap<Integer, String> hmOfSelectedGIDsAndGNames) {
		hmOfGIdsAndNval = hmOfSelectedGIDsAndGNames;
	}

}
