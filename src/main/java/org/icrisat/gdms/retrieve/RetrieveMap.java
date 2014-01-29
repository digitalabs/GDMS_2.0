package org.icrisat.gdms.retrieve;

import java.util.ArrayList;
import java.util.List;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.dao.gdms.MappingDataDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.hibernate.Session;
import org.icrisat.gdms.ui.common.GDMSModel;

public class RetrieveMap {

	private Session localSession;
	private Session centralSession;
	public RetrieveMap() {
		try{
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public List<MappingData> retrieveMappingData() throws MiddlewareQueryException {
		List<MappingData> mapDAO = getMappingDataDAO();
		return mapDAO;
	}
	
	
	private List<MappingData> getMappingDataDAO() throws MiddlewareQueryException {
		List<MappingData> listOfMappingData = new ArrayList<MappingData>();
		List<MappingData> localMappingDataDAO = getLocalMappingDataDAO();
		if(null != localMappingDataDAO) {
			listOfMappingData.addAll(localMappingDataDAO);
		}
		List<MappingData> centralMappingDataDAO = getCentralMappingDataDAO();
		if (null != centralMappingDataDAO) {
			listOfMappingData.addAll(centralMappingDataDAO);
		}
		return listOfMappingData;
	}

	private List<MappingData> getCentralMappingDataDAO() throws MiddlewareQueryException {
		MappingDataDAO mappingDataDAO = new MappingDataDAO();
		mappingDataDAO.setSession(centralSession);
		List<MappingData> all = mappingDataDAO.getAll();
		return all;
	}

	private List<MappingData> getLocalMappingDataDAO() throws MiddlewareQueryException {
		MappingDataDAO mappingDataDAO = new MappingDataDAO();
		mappingDataDAO.setSession(localSession);
		List<MappingData> all = mappingDataDAO.getAll();
		return all;
	}

	public List<MapDetailElement> retrieveMaps() throws MiddlewareQueryException {
		List<MapDetailElement> mapDAO = getMapDAO();
		return mapDAO;
	}
	private List<MapDetailElement> getMapDAO() throws MiddlewareQueryException {
		List<MapDetailElement> mapDetails = new ArrayList<MapDetailElement>();
		List<MapDetailElement> localMap = getLocalMap();
		if (null != localMap) {
			mapDetails.addAll(localMap);
		}
		List<MapDetailElement> centralMap = getCentralMap();
		if (null != centralMap) {
			mapDetails.addAll(centralMap);
		}
		return mapDetails;
	}
	private List<MapDetailElement> getCentralMap() throws MiddlewareQueryException {
		MapDAO mapDAO = new MapDAO();
		mapDAO.setSession(centralSession);
		List<MapDetailElement> allMapDetails = mapDAO.getAllMapDetails(0, (int)mapDAO.countAll());
		
		List<Map> listOfAllMaps = mapDAO.getAll();
		
		for (Map map : listOfAllMaps) {
			String mapName = map.getMapName();
			Long countMapDetailsByName = mapDAO.countMapDetailsByName(mapName);
			List<MapDetailElement> listOfAllMapDetailsByNameLocal = mapDAO.getMapDetailsByName(mapName, 0, countMapDetailsByName.intValue());
			
			for (MapDetailElement mapDetailElement : listOfAllMapDetailsByNameLocal) {
				if (false == allMapDetails.contains(mapDetailElement)) {
					allMapDetails.add(mapDetailElement);
				}
			}
		}
		
		return allMapDetails;
	}
	private List<MapDetailElement> getLocalMap() throws MiddlewareQueryException {
		MapDAO mapDAO = new MapDAO();
		mapDAO.setSession(localSession);
		List<MapDetailElement> allMapDetails = mapDAO.getAllMapDetails(0, (int)mapDAO.countAll());
		
		List<Map> listOfAllMaps = mapDAO.getAll();
		
		for (Map map : listOfAllMaps) {
			String mapName = map.getMapName();
			Long countMapDetailsByName = mapDAO.countMapDetailsByName(mapName);
			List<MapDetailElement> listOfAllMapDetailsByNameCentral = mapDAO.getMapDetailsByName(mapName, 0, countMapDetailsByName.intValue());
			
			for (MapDetailElement mapDetailElement : listOfAllMapDetailsByNameCentral) {
				if (false == allMapDetails.contains(mapDetailElement)) {
					allMapDetails.add(mapDetailElement);
				}
			}
		}
		
		return allMapDetails;
	}

}
