package org.icrisat.gdms.retrieve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.hibernate.Session;
import org.icrisat.gdms.ui.common.GDMSModel;

public class GenotypingDataRetrieval {

	private Session centralSession;
	private Session localSession;
	ManagerFactory factory=null;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	List<Dataset> resultC;
	List<Dataset> resultL;
	public GenotypingDataRetrieval() {
		localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		manager = factory.getGermplasmDataManager();
		genoManager=factory.getGenotypicDataManager();
	}
	
	public List<Dataset> retrieveGenotyingDataRetrieval() throws MiddlewareQueryException {
		
		List<Dataset> listToReturn = new ArrayList<Dataset>();
		List<Dataset> localDataset = getLocalDataset();
		if(null != localDataset) {
			listToReturn.addAll(localDataset);
		}
		List<Dataset> centralDataset = getCentralDataset();
		if(null != centralDataset) {
			listToReturn.addAll(centralDataset);
		}
		//System.out.println("$$$$$$$$$$$$$$$$$$   :"+listToReturn);
		return listToReturn;
	}

	private List<Dataset> getCentralDataset() throws MiddlewareQueryException {
		ArrayList strDatasetID=new ArrayList();
		List<String> results=genoManager.getDatasetNames(0, (int)genoManager.countDatasetNames(Database.CENTRAL), Database.CENTRAL);
		for(int r=0;r<results.size();r++){
			List<DatasetElement> datasetIds =genoManager.getDatasetDetailsByDatasetName(results.get(r), Database.CENTRAL);
			for (DatasetElement result : datasetIds){		       	
		       	strDatasetID.add(result.getDatasetId());
		    }
		}	
		//System.out.println(genoManager.getDatasetDetailsByDatasetIds(strDatasetID));
		try{
			if(strDatasetID.size()>0){
				resultC = genoManager.getDatasetDetailsByDatasetIds(strDatasetID);
			}
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultC;
	}

	private List<Dataset> getLocalDataset() throws MiddlewareQueryException {
		ArrayList strDatasetID=new ArrayList();
		List<String> results=genoManager.getDatasetNames(0, (int)genoManager.countDatasetNames(Database.LOCAL), Database.LOCAL);
		for(int r=0;r<results.size();r++){
			List<DatasetElement> datasetIds =genoManager.getDatasetDetailsByDatasetName(results.get(r), Database.LOCAL);
			for (DatasetElement result : datasetIds){
		       	strDatasetID.add(result.getDatasetId());
		    }
		}
		try{
			if(strDatasetID.size()>0){
				resultL = genoManager.getDatasetDetailsByDatasetIds(strDatasetID);
			}
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultL;
	}

	//20131209: Tulasi --- Implemented code to retrieve all data related to QTLs to be displayed in the default Retrieve tab table
	public ArrayList<String> retrieveQTLData() {
		
		ArrayList<String> listToReturn = new ArrayList<String>();
		
		ArrayList<String> localQTLs = getLocalQTLs();
		if(null != localQTLs) {
			listToReturn.addAll(localQTLs);
		}
		
		ArrayList<String> centralQTLs = getCentralQTLs();
		if(null != centralQTLs) {
			listToReturn.addAll(centralQTLs);
		}
		
		return listToReturn;
	}

	private ArrayList<String> getLocalQTLs() {
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		genotypicDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
		
		
		ArrayList<String> arrayListOfQTLRetrieveDataLocal = null;
		
		try {
			
			long countAllQtl = genotypicDataManagerImpl.countAllQtl();
			List<Qtl> listOfAllQtls = genotypicDataManagerImpl.getAllQtl(0, (int)countAllQtl);
			
			arrayListOfQTLRetrieveDataLocal = new ArrayList<String>();
			
			for (Qtl qtl : listOfAllQtls) {
				
				String strQtlName = qtl.getQtlName();
				
				long countQtlIdByName = genotypicDataManagerImpl.countQtlIdByName(strQtlName);
				List<Integer> listOfAllQtlIdsByName = genotypicDataManagerImpl.getQtlIdByName(strQtlName, 0, (int)countQtlIdByName);
				
				List<QtlDetailElement> listOfQtlDetailElementByQtlIds = null;
				if (null != listOfAllQtlIdsByName) {
					long countQtlByQtlIds = genotypicDataManagerImpl.countQtlByQtlIds(listOfAllQtlIdsByName);
					listOfQtlDetailElementByQtlIds = genotypicDataManagerImpl.getQtlByQtlIds(listOfAllQtlIdsByName, 0, (int)countQtlByQtlIds);
				}
				
				if (null != listOfQtlDetailElementByQtlIds) {
					for (QtlDetailElement qtlDetailElement : listOfQtlDetailElementByQtlIds) {
						String strMapName = qtlDetailElement.getMapName();
						String strTRName = qtlDetailElement.getTRName();
						
						String strChromosome = qtlDetailElement.getChromosome();
						String strMinPosition = String.valueOf(qtlDetailElement.getMinPosition().floatValue());
						String strMaxPosition = String.valueOf(qtlDetailElement.getMaxPosition().floatValue());
						
						
						String strQTLData = strQtlName + "!~!" + strMapName + "!~!" + strTRName + "!~!" + strChromosome +
								               "!~!" +  strMinPosition + "!~!" + strMaxPosition ;
						arrayListOfQTLRetrieveDataLocal.add(strQTLData);
					}
				}                                  
			}
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		
		return arrayListOfQTLRetrieveDataLocal;
	}

	private ArrayList<String> getCentralQTLs() {
		GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl();
		genotypicDataManagerImpl.setSessionProviderForCentral(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral());
		
		ArrayList<String> arrayListOfQTLRetrieveDataCentral = null;
		
		try {
			
			long countAllQtl = genotypicDataManagerImpl.countAllQtl();
			List<Qtl> listOfAllQtls = genotypicDataManagerImpl.getAllQtl(0, (int)countAllQtl);
			
			arrayListOfQTLRetrieveDataCentral = new ArrayList<String>();
			
			for (Qtl qtl : listOfAllQtls) {
				
				String strQtlName = qtl.getQtlName();
				
				long countQtlIdByName = genotypicDataManagerImpl.countQtlIdByName(strQtlName);
				List<Integer> listOfAllQtlIdsByName = genotypicDataManagerImpl.getQtlIdByName(strQtlName, 0, (int)countQtlIdByName);
				
				List<QtlDetailElement> listOfQtlDetailElementByQtlIds = null;
				if (null != listOfAllQtlIdsByName) {
					long countQtlByQtlIds = genotypicDataManagerImpl.countQtlByQtlIds(listOfAllQtlIdsByName);
					listOfQtlDetailElementByQtlIds = genotypicDataManagerImpl.getQtlByQtlIds(listOfAllQtlIdsByName, 0, (int)countQtlByQtlIds);
				}
				
				if (null != listOfQtlDetailElementByQtlIds) {
					for (QtlDetailElement qtlDetailElement : listOfQtlDetailElementByQtlIds) {
						String strMapName = qtlDetailElement.getMapName();
						String strTRName = qtlDetailElement.getTRName();
						
						String strChromosome = qtlDetailElement.getChromosome();
						String strMinPosition = String.valueOf(qtlDetailElement.getMinPosition().floatValue());
						String strMaxPosition = String.valueOf(qtlDetailElement.getMaxPosition().floatValue());
						
						String strQTLData = strQtlName + "!~!" + strMapName + "!~!" + strTRName + "!~!" + strChromosome +
								               "!~!" +  strMinPosition + "!~!" + strMaxPosition ;
						arrayListOfQTLRetrieveDataCentral.add(strQTLData);
					}
				}
			}
			
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return arrayListOfQTLRetrieveDataCentral;
	}
	//20131209: Tulasi --- Implemented code to retrieve all data related to QTLs to be displayed in the default Retrieve tab table
	
	//20131210: Tulasi --- Implemented method to retrieve Dataset Size
	public HashMap<Integer, String> retrieveDatasetSize() {
		
		ManagerFactory factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		GenotypicDataManager genoManager = factory.getGenotypicDataManager();

		DatasetDAO datasetDAOForLocal = new DatasetDAO();
		datasetDAOForLocal.setSession(localSession);

		DatasetDAO datasetDAOForCentral = new DatasetDAO();
		datasetDAOForCentral.setSession(centralSession);

		List<Dataset> listOfAllDatasetsFromLocalDB = null;
		List<Dataset> listOfAllDatasetsFromCentralDB = null;

		try {
			
			
			listOfAllDatasetsFromLocalDB = datasetDAOForLocal.getAll();
			listOfAllDatasetsFromCentralDB = datasetDAOForCentral.getAll();
			
			
			
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<Dataset> listOfAllDatasets = new ArrayList<Dataset>();
		if (null != listOfAllDatasetsFromLocalDB && 0 != listOfAllDatasetsFromLocalDB.size()){
			for (Dataset dataset : listOfAllDatasetsFromLocalDB){
				if (false == "QTL".equalsIgnoreCase(dataset.getDatasetType().toString())){
					listOfAllDatasets.add(dataset);
				}
			}
		}

		if (null != listOfAllDatasetsFromCentralDB && 0 != listOfAllDatasetsFromCentralDB.size()){
			for (Dataset dataset : listOfAllDatasetsFromCentralDB){
				if (false == "QTL".equalsIgnoreCase(dataset.getDatasetType().toString())){
					listOfAllDatasets.add(dataset);
				}
			}
		}
		
		ArrayList<Integer> datasetIdsList = new ArrayList<Integer>();
		HashMap<Integer, String> datasetSize = new HashMap<Integer, String>();

		for (int i = 0; i < listOfAllDatasets.size(); i++){
			Dataset dataset = listOfAllDatasets.get(i);			
			datasetIdsList.add(dataset.getDatasetId());	
			
			try {
				
				int markerCount = (int)genoManager.countMarkersFromMarkerMetadatasetByDatasetIds(datasetIdsList);
				int nidsCount=(int)genoManager.countNidsFromAccMetadatasetByDatasetIds(datasetIdsList);	
				
				String size = nidsCount+" x "+markerCount;
				
				datasetSize.put(Integer.parseInt(dataset.getDatasetId().toString()), size);
				
				datasetIdsList.clear();
				
			} catch (MiddlewareQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return datasetSize;
	}
	//20131210: Tulasi --- Implemented method to retrieve Dataset Size
}
