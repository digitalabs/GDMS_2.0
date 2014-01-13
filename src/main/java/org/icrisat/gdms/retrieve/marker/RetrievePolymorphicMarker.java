package org.icrisat.gdms.retrieve.marker;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.dao.gdms.MappingPopDAO;
import org.generationcp.middleware.dao.gdms.MarkerMetadataSetDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GidNidElement;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
import org.generationcp.middleware.pojos.gdms.ParentElement;
import org.hibernate.Session;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;

public class RetrievePolymorphicMarker {
	private Session centralSession = null;
	private Session localSession = null;
	private String strSelectedPolymorphicType;
	ManagerFactory factory=null;
	GenotypicDataManager genoManager;
	GermplasmDataManager manager;
	List<String> genotypeList=new ArrayList<String>();
	List<Integer> listofGids = new ArrayList<Integer>();
	public RetrievePolymorphicMarker() {
		localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		genoManager=factory.getGenotypicDataManager();
		manager=factory.getGermplasmDataManager();
	}

	public List<Name> getNames(String theSelectedGName, String strSelectedPolymorphicType) throws GDMSException {
		try {
			/*String strName = name;//.getNval();
			List<String> strNames = new ArrayList<String>();
			strNames.add(strName);*/
			
			genotypeList = new ArrayList<String>();
			listofGids = new ArrayList<Integer>();
			
			List<Integer> listOfDatasetIds = new ArrayList<Integer>();
			Integer gid = null;
			Integer nid = null;
			genotypeList.add(theSelectedGName);
			List<GidNidElement> results = manager.getGidAndNidByGermplasmNames(genotypeList);
			for(int r=0;r<results.size();r++){
				/*gid=gid+results.get(r).getGermplasmId()+",";	*/
				listofGids.add(results.get(r).getGermplasmId());
		}
			/*List<Integer> listofGids = new ArrayList<Integer>();
			listofGids.add(theSelectedGID);*/
			List<AccMetadataSetPK> accMetadataSets = genoManager.getGdmsAccMetadatasetByGid(listofGids, 0, 
	                (int) genoManager.countGdmsAccMetadatasetByGid(listofGids));
	        //System.out.println("testGetGdmsAccMetadatasetByGid() RESULTS: ");
	        for (AccMetadataSetPK accMetadataSet : accMetadataSets) {
	            //System.out.println("@@@@@@@@@@@@@@@@@@@@  :"+accMetadataSet.toString());
	            Integer datasetId = accMetadataSet.getDatasetId();
	            gid = accMetadataSet.getGermplasmId();
				nid = accMetadataSet.getNameId();
				if(false == listOfDatasetIds.contains(datasetId)) {
					listOfDatasetIds.add(datasetId);
				}
	            
	            
	        }
			/*List<AccMetadataSetPK> accMetaDataSetByGids = getAccMetaDataSetByGids(listofGids);
			List<Integer> listOfDatasetIds = new ArrayList<Integer>();
			Integer gid = null;
			Integer nid = null;
			for (AccMetadataSetPK accMetadataSetPK : accMetaDataSetByGids) {
				Integer datasetId = accMetadataSetPK.getDatasetId();
				gid = accMetadataSetPK.getGermplasmId();
				nid = accMetadataSetPK.getNameId();
				if(false == listOfDatasetIds.contains(datasetId)) {
					listOfDatasetIds.add(datasetId);
				}
			}*/
	        List<Integer> midsList= new ArrayList();
	        for(int d=0;d<listOfDatasetIds.size();d++){
	        	midsList=genoManager.getMarkerIdsByDatasetId(listOfDatasetIds.get(d));
	        }
			//List<Integer> markerMetadataSet = getMarkerMetadataSet(listOfDatasetIds, gid);
			
			List<Integer> nidByMarkerIdsAndDatasetIdsAndNotGIds = getNidByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIds, midsList, listofGids);
			for (int i = 0; i < nidByMarkerIdsAndDatasetIdsAndNotGIds.size(); i++) {
				Integer integer = nidByMarkerIdsAndDatasetIdsAndNotGIds.get(i);
				if(false == integer.equals(nid)) {
					nidByMarkerIdsAndDatasetIdsAndNotGIds.remove(i);
				}
			}
			
			List<Name> namesByNIds = getNamesByNIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
			//System.out.println("%%%%%%%%%%%%%  :"+namesByNIds);
			return namesByNIds;
			
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		//return new ArrayList<Name>();
	}

	private List<Name> getNamesByNIds(
			List<Integer> nidByMarkerIdsAndDatasetIdsAndNotGIds)
			throws MiddlewareQueryException {
		List<Name> listOfNames = new ArrayList<Name>();
		List<Name> localNamesByNIds = getLocalNamesByNIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		for (Name name : localNamesByNIds) {
			listOfNames.add(name);
		}
		List<Name> centralNamesByNIds = getCentralNamesByNIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		for (Name name : centralNamesByNIds) {
			listOfNames.add(name);
		}
		return listOfNames;
	}

	private List<Name> getCentralNamesByNIds(
			List<Integer> nidByMarkerIdsAndDatasetIdsAndNotGIds) throws MiddlewareQueryException {
		/*NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		return namesByNameIds;*/
		
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(centralSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		return namesByNameIds;
	}

	private List<Name> getLocalNamesByNIds(
			List<Integer> nidByMarkerIdsAndDatasetIdsAndNotGIds)
			throws MiddlewareQueryException {
		/*for (Integer integer : nidByMarkerIdsAndDatasetIdsAndNotGIds) {
			NameDAO nameDAO = new NameDAO();
			nameDAO.setSession(localSession);
			Name nameByNameId = nameDAO.getNameByNameId(integer);
			System.out.println(nameByNameId);
		}
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		return namesByNameIds;*/
		
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		return namesByNameIds;
	}

	private List<Integer> getNidByMarkerIdsAndDatasetIdsAndNotGIds(List<Integer> listOfDatasetIds, List<Integer> markerMetadataSet, List<Integer> listofGids) throws MiddlewareQueryException {
		List<Integer> listOfNids = new ArrayList<Integer>();
		/*Set<Integer> localNidByMarkerIdsandDatasetIdsAndNotGIds = getLocalNidByMarkerIdsandDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		for (Integer integer : localNidByMarkerIdsandDatasetIdsAndNotGIds) {
			listOfNids.add(integer);
		}
		Set<Integer> centralNidByMarkerIdsandDatasetIdsAndNotGIds = getCentralNidByMarkerIdsandDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		for (Integer integer : centralNidByMarkerIdsandDatasetIdsAndNotGIds) {
			listOfNids.add(integer);
		}*/
		
		List<Integer> centralNidByMarkerIdsandDatasetIdsAndNotGIds = getCentralNidByMarkerIdsandDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		for (Integer integer : centralNidByMarkerIdsandDatasetIdsAndNotGIds) {
			listOfNids.add(integer);
		}
		
		return listOfNids;
		
	}

	private List<Integer> getCentralNidByMarkerIdsandDatasetIdsAndNotGIds(
			List<Integer> listOfDatasetIds, List<Integer> markerMetadataSet,
			List<Integer> listofGids) throws MiddlewareQueryException {
		/*AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);*/
		//Set<Integer> nIdsByMarkerIdsAndDatasetIdsAndNotGIds = accMetadataSetDAO.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		//getNIdsByMarkerIdsAndDatasetIds(datasetIdList, markerIDList, 0, manager1.countNIdsByMarkerIdsAndDatasetIds(datasetIdList, markerIDList))
		
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl(localSession, centralSession);
		int countNIdsByMarkerIdsAndDatasetIds = genotypicDataManagerImpl.countNIdsByMarkerIdsAndDatasetIds(listOfDatasetIds, markerMetadataSet);
		List<Integer> nIdsByMarkerIdsAndDatasetIds2 = genotypicDataManagerImpl.getNIdsByMarkerIdsAndDatasetIds(listOfDatasetIds, markerMetadataSet, 0, countNIdsByMarkerIdsAndDatasetIds);
		
		return nIdsByMarkerIdsAndDatasetIds2;
		//return nIdsByMarkerIdsAndDatasetIdsAndNotGIds;
	}

	/*private Set<Integer> getLocalNidByMarkerIdsandDatasetIdsAndNotGIds(
			List<Integer> listOfDatasetIds, List<Integer> markerMetadataSet,
			List<Integer> listofGids) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		Set<Integer> nIdsByMarkerIdsAndDatasetIdsAndNotGIds = accMetadataSetDAO.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		return nIdsByMarkerIdsAndDatasetIdsAndNotGIds;
		Set<Integer> nIdsByMarkerIdsAndDatasetIds = accMetadataSetDAO.getNIdsByMarkerIdsAndDatasetIds(markerMetadataSet, listOfDatasetIds);
		return nIdsByMarkerIdsAndDatasetIds;
	}*/

	private List<Integer> getMarkerMetadataSet(List<Integer> listOfDatasetIds,
			Integer gid) throws MiddlewareQueryException {
		List<Integer> listOfMarkerMetadataSet = new ArrayList<Integer>();
		List<Integer> localMarkerMetadataSet = getLocalMarkerMetadataSet(listOfDatasetIds, gid);
		for (Integer integer : localMarkerMetadataSet) {
			listOfMarkerMetadataSet.add(integer);
		}
		
		List<Integer> centralMarkerMetadataSet = getCentralMarkerMetadataSet(listOfDatasetIds, gid);
		for (Integer integer : centralMarkerMetadataSet) {
			listOfMarkerMetadataSet.add(integer);
		}
		return listOfMarkerMetadataSet;
	}

	private List<Integer> getCentralMarkerMetadataSet(
			List<Integer> listOfDatasetIds, Integer gid) throws MiddlewareQueryException {
		MarkerMetadataSetDAO markerMetadataSetDAO = new MarkerMetadataSetDAO();
		markerMetadataSetDAO.setSession(centralSession);
		List<Integer> markersByGidAndDatasetIds = markerMetadataSetDAO.getMarkersByGidAndDatasetIds(gid, listOfDatasetIds, 0, (int) markerMetadataSetDAO.countAll());
		return markersByGidAndDatasetIds;
	}

	private List<Integer> getLocalMarkerMetadataSet(List<Integer> listOfDatasetIds,
			Integer gid) throws MiddlewareQueryException {
		MarkerMetadataSetDAO markerMetadataSetDAO = new MarkerMetadataSetDAO();
		markerMetadataSetDAO.setSession(localSession);
		List<Integer> markersByGidAndDatasetIds = markerMetadataSetDAO.getMarkersByGidAndDatasetIds(gid, listOfDatasetIds, 0, (int) markerMetadataSetDAO.countAll());
		return markersByGidAndDatasetIds;
	}

	private List<AccMetadataSetPK> getAccMetaDataSetByGids(List<Integer> listofGids)
			throws MiddlewareQueryException {
		List<AccMetadataSetPK> listOfAccMetadataSetDAO = new ArrayList<AccMetadataSetPK>();
		List<AccMetadataSetPK> localAccMetaDataSetByGids = getLocalAccMetaDataSetByGids(listofGids);
		for (AccMetadataSetPK accMetadataSetPK : localAccMetaDataSetByGids) {
			listOfAccMetadataSetDAO.add(accMetadataSetPK);
		}
		List<AccMetadataSetPK> centralAccMetaDataSetByGids = getCentralAccMetaDataSetByGids(listofGids);
		for (AccMetadataSetPK accMetadataSetPK : centralAccMetaDataSetByGids) {
			listOfAccMetadataSetDAO.add(accMetadataSetPK);
		}
		return listOfAccMetadataSetDAO;
		
	}

	private List<AccMetadataSetPK> getCentralAccMetaDataSetByGids(List<Integer> listofGids) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);
		List<AccMetadataSetPK> accMetadataSetByGids = accMetadataSetDAO.getAccMetadataSetByGids(listofGids, 0, (int)accMetadataSetDAO.countAll());
		return accMetadataSetByGids;
	}

	private List<AccMetadataSetPK> getLocalAccMetaDataSetByGids(List<Integer> listofGids)
			throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		List<AccMetadataSetPK> accMetadataSetByGids = accMetadataSetDAO.getAccMetadataSetByGids(listofGids, 0, (int)accMetadataSetDAO.countAll());
		return accMetadataSetByGids;
	}

	private List<Integer> getGIDAndNidByGermplasmNames(List<String> strNames)
			throws MiddlewareQueryException {
		List<Integer> listofGids = new ArrayList<Integer>();
		
		List<GidNidElement> localGidAndNidByGermplasmNames = getLocalGidAndNidByGermplasmNames(strNames);
		for (GidNidElement gidNidElement : localGidAndNidByGermplasmNames) {
			Integer germplasmId = gidNidElement.getGermplasmId();
			listofGids.add(germplasmId);
		}
		
		List<GidNidElement> centralGidAndNidByGermplasmNames = getCentralGidAndNidByGermplasmNames(strNames);
		for (GidNidElement gidNidElement : centralGidAndNidByGermplasmNames) {
			Integer germplasmId = gidNidElement.getGermplasmId();
			listofGids.add(germplasmId);
		}

		return listofGids;
	}

	private List<GidNidElement> getCentralGidAndNidByGermplasmNames(
			List<String> strNames) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(centralSession);
		List<GidNidElement> gidAndNidByGermplasmNames = nameDAO.getGidAndNidByGermplasmNames(strNames);
		return gidAndNidByGermplasmNames;
	}

	private List<GidNidElement> getLocalGidAndNidByGermplasmNames(
			List<String> strNames) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<GidNidElement> gidAndNidByGermplasmNames = nameDAO.getGidAndNidByGermplasmNames(strNames);
		return gidAndNidByGermplasmNames;
	}

	public List<Name> getNamesForRetrievePolymorphic() throws GDMSException {
		List<Integer> listOfParentsFromMappingPopulation = new ArrayList<Integer>();
		List<Integer> listofDatasetId = new ArrayList<Integer>();
		try {
			listofDatasetId = getDatasetIDs();
			//System.out.println("dataset IDS:"+listofDatasetId);
			listOfParentsFromMappingPopulation = getMappingPop();
			//System.out.println(".......parents:"+listOfParentsFromMappingPopulation);
			
			/** Commented by Kalyani on 23 OCT 2013 while testing the functionality   **/
			//List<Integer> niDsByDatasetIds = getAccMetadataSetByParentMappingPopulationAndDatasetId(listOfParentsFromMappingPopulation, listofDatasetId);
			
			List<Integer> niDsByDatasetIds = getAccMetaDatasetFromBothCentralAndLocal(listofDatasetId);
			
			List<Name> namesByNameIds = getNamesByNID(niDsByDatasetIds);
			
			return namesByNameIds;
		} catch (Throwable e) {
			//e.printStackTrace();
			throw new GDMSException(e.getMessage());
		}
		//return null;
	}

	private List<Name> getNamesByNID(List<Integer> niDsByDatasetIds)
			throws MiddlewareQueryException {
		List<Name> namesByNameIds = new ArrayList<Name>();
		List<Name> localNamesByNameIds = getLocalName(niDsByDatasetIds);
		for (Name name : localNamesByNameIds) {
			namesByNameIds.add(name);
		}
		List<Name> centralNamesByNameIds = getCentralName(niDsByDatasetIds);
		for (Name name : centralNamesByNameIds) {
			namesByNameIds.add(name);
		}
		return namesByNameIds;
	}

	private List<Name> getCentralName(List<Integer> niDsByDatasetIds) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(centralSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(niDsByDatasetIds);
		return namesByNameIds;
	}

	private List<Name> getLocalName(List<Integer> niDsByDatasetIds) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(niDsByDatasetIds);
		return namesByNameIds;
	}

	private List<Integer> getAccMetadataSetByParentMappingPopulationAndDatasetId(List<Integer> listOfParentsFromMappingPopulation, List<Integer> listofDatasetId) throws MiddlewareQueryException {
		List<Integer> niDsByDatasetIds = new ArrayList<Integer>();
		/*List<Integer> localNiDsByDatasetIds = getLocalAccMetadataSet(listOfParentsFromMappingPopulation, listofDatasetId);
		for (Integer integer : localNiDsByDatasetIds) {
			if(false == niDsByDatasetIds.contains(integer)) {
				niDsByDatasetIds.add(integer);
			}
		}
		List<Integer> centralNiDsByDatasetIds = getCentralAccMetadataSet(listOfParentsFromMappingPopulation, listofDatasetId);
		for (Integer integer : centralNiDsByDatasetIds) {
			if(false == niDsByDatasetIds.contains(integer)) {
				niDsByDatasetIds.add(integer);
			}
		}*/
		
		
		niDsByDatasetIds = getAccMetaDatasetFromBothCentralAndLocal(listofDatasetId);
		
		return niDsByDatasetIds;
	}

	private List<Integer> getAccMetaDatasetFromBothCentralAndLocal(
			List<Integer> listofDatasetId) throws MiddlewareQueryException {
		
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl(localSession, centralSession);
		long countDatasetIdsForMapping = 0l;
		//System.out.println("..............  strSelectedPolymorphicType=:"+strSelectedPolymorphicType+"    "+listofDatasetId);
		if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
			countDatasetIdsForMapping = genotypicDataManagerImpl.countDatasetIdsForMapping();
		} else {
			countDatasetIdsForMapping = genotypicDataManagerImpl.countDatasetIdsForFingerPrinting();
			//System.out.println("count=:"+countDatasetIdsForMapping);
		}
		List<Integer> nidsFromAccMetadatasetByDatasetIds = genotypicDataManagerImpl.getNidsFromAccMetadatasetByDatasetIds(listofDatasetId, 0, 500);
		//genotypicDataManagerImpl.countn
		//List<Integer> nidsFromAccMetadatasetByDatasetIds = genotypicDataManagerImpl.getNidsFromAccMetadatasetByDatasetIds(listofDatasetId, 0, (int)genotypicDataManagerImpl.countNidsFromAccMetadatasetByDatasetIds(listofDatasetId));
		//System.out.println("^^^^^^^^^^^^^^^^^^^^^:"+nidsFromAccMetadatasetByDatasetIds);
		return nidsFromAccMetadatasetByDatasetIds;
		
	}

	/*private List<Integer> getLocalAccMetadataSet(List<Integer> listOfParentsFromMappingPopulation, List<Integer> listofDatasetId) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		List<Integer> niDsByDatasetIds = accMetadataSetDAO.getNIDsByDatasetIds(listofDatasetId, listOfParentsFromMappingPopulation, 0, (int)accMetadataSetDAO.countAll());
		return niDsByDatasetIds;
		
	}
	
	private List<Integer> getCentralAccMetadataSet(List<Integer> listOfParentsFromMappingPopulation, List<Integer> listofDatasetId) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);
		List<Integer> niDsByDatasetIds = accMetadataSetDAO.getNIDsByDatasetIds(listofDatasetId, listOfParentsFromMappingPopulation, 0, (int)accMetadataSetDAO.countAll());
		return niDsByDatasetIds;
	}*/


	private List<Integer> getMappingPop()
			throws MiddlewareQueryException {
		List<Integer> listOfParentsFromMappingPopulation = new ArrayList<Integer>();
		List<ParentElement> localAllParentsFromMappingPopulation = getLocalMappingPop();
		List<ParentElement> centralAllParentsFromMappingPopulation = getCentralMappingPop();
		
		for (ParentElement parentElement : localAllParentsFromMappingPopulation) {
			if(false == listOfParentsFromMappingPopulation.contains(parentElement.getParentANId())) {
				listOfParentsFromMappingPopulation.add(parentElement.getParentANId());
			}
			if(false == listOfParentsFromMappingPopulation.contains(parentElement.getParentBGId())) {
				listOfParentsFromMappingPopulation.add(parentElement.getParentBGId());
			}
		}
		
		for (ParentElement parentElement : centralAllParentsFromMappingPopulation) {
			if(false == listOfParentsFromMappingPopulation.contains(parentElement.getParentANId())) {
				listOfParentsFromMappingPopulation.add(parentElement.getParentANId());
			}
			if(false == listOfParentsFromMappingPopulation.contains(parentElement.getParentBGId())) {
				listOfParentsFromMappingPopulation.add(parentElement.getParentBGId());
			}
		}
		return listOfParentsFromMappingPopulation;
	}

	private List<ParentElement> getCentralMappingPop() throws MiddlewareQueryException {
		MappingPopDAO mappingPopDAO = new MappingPopDAO();
		mappingPopDAO.setSession(centralSession);
		List<ParentElement> allParentsFromMappingPopulation = mappingPopDAO.getAllParentsFromMappingPopulation(0, (int)mappingPopDAO.countAll());
		return allParentsFromMappingPopulation;
	}

	private List<ParentElement> getLocalMappingPop()
			throws MiddlewareQueryException {
		MappingPopDAO mappingPopDAO = new MappingPopDAO();
		mappingPopDAO.setSession(localSession);
		List<ParentElement> allParentsFromMappingPopulation = mappingPopDAO.getAllParentsFromMappingPopulation(0, (int)mappingPopDAO.countAll());
		return allParentsFromMappingPopulation;
	}

	private List<Integer> getDatasetIDs() throws MiddlewareQueryException {
		List<Integer> listofDatasetId = new ArrayList<Integer>();
		
		if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
			listofDatasetId=genoManager.getDatasetIdsForMapping(0, (int)genoManager.countDatasetIdsForMapping());
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
		} else {
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForFingerPrinting(0, (int)datasetDAO.countAll());
			listofDatasetId=genoManager.getDatasetIdsForFingerPrinting(0, (int)genoManager.countDatasetIdsForFingerPrinting());
		}
		
		/*List<Integer> localDatasetIdsForMapping = getLocalDataset();
		List<Integer> centraldatasetIdsForMapping = getCentralDataset();
		for (Integer integerDatasetid : localDatasetIdsForMapping) {
			if(false == listofDatasetId.contains(integerDatasetid)) {
				listofDatasetId.add(integerDatasetid);
			}
		}
		
		for (Integer integerDatasetid : centraldatasetIdsForMapping) {
			if(false == listofDatasetId.contains(integerDatasetid)) {
				listofDatasetId.add(integerDatasetid);
			}
		}*/
		
		return listofDatasetId;
	}

	/*private List<Integer> getLocalDataset()
			throws MiddlewareQueryException {
		DatasetDAO datasetDAO = new DatasetDAO();
		datasetDAO.setSession(localSession);
		
		//20130830: Fix for Issue No: 70
		//List<Integer> datasetIdsForMapping = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
		
		List<Integer> datasetIdsForPolymorphic;
		System.out.println("strSelectedPolymorphicType=:"+strSelectedPolymorphicType);
		if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
			datasetIdsForPolymorphic =genoManager.get
		} else {
			datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForFingerPrinting(0, (int)datasetDAO.countAll());
		}
		//20130830: Fix for Issue No: 70
		System.out.println("datasetIdsForPolymorphic    LOCAL   :"+datasetIdsForPolymorphic);
		return datasetIdsForPolymorphic;
	}
	
	private List<Integer> getCentralDataset()
			throws MiddlewareQueryException {
		DatasetDAO datasetDAO = new DatasetDAO();
		datasetDAO.setSession(centralSession);
		
		//20130830: Fix for Issue No: 70
		//List<Integer> datasetIdsForMapping = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
		
		List<Integer> datasetIdsForPolymorphic;
		
		if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
			datasetIdsForPolymorphic=genoManager.getDatasetIdsForMapping(0, (int)genoManager.countDatasetIdsForMapping());
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
		} else {
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForFingerPrinting(0, (int)datasetDAO.countAll());
			datasetIdsForPolymorphic=genoManager.getDatasetIdsForFingerPrinting(0, (int)genoManager.countDatasetIdsForFingerPrinting());
		}
		//20130830: Fix for Issue No: 70
		
		return datasetIdsForPolymorphic;
	}*/

	public void setPolymorphicType(String theSelectedPolymorphicType) {
		strSelectedPolymorphicType = theSelectedPolymorphicType;
	}

}
