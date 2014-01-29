package org.icrisat.gdms.ui;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.pojos.gdms.MapInfo;

public class SortingMaps {

	public List<MapInfo> sort(List<MapInfo> theListOfAllMappingData) {
		if(1 >= theListOfAllMappingData.size()) {
			return theListOfAllMappingData;
		}
		MapInfo pivotMappingData = theListOfAllMappingData.remove(theListOfAllMappingData.size()/2);
		
		return sort(theListOfAllMappingData, pivotMappingData);
	}
	
	
	public List<MapInfo> sort(List<MapInfo> theListOfAllMappingData, MapInfo pivotMappingData) {
		List<MapInfo> tempListOfAllMappingDataLess = new ArrayList<MapInfo>();
		List<MapInfo> tempListOfAllMappingDataGreat = new ArrayList<MapInfo>();
		for (int i = 0; i < theListOfAllMappingData.size(); i++) {
			MapInfo mappingData = theListOfAllMappingData.get(i);
				if(0 > mappingData.getLinkageGroup().compareTo(pivotMappingData.getLinkageGroup())) {
					tempListOfAllMappingDataLess.add(mappingData);
				} else if(0 < mappingData.getLinkageGroup().compareTo(pivotMappingData.getLinkageGroup())) {
					tempListOfAllMappingDataGreat.add(mappingData);
				} else if(0 == mappingData.getLinkageGroup().compareTo(pivotMappingData.getLinkageGroup())) {
					if(mappingData.getStartPosition() > pivotMappingData.getStartPosition()) {
						tempListOfAllMappingDataGreat.add(mappingData);
					} else if(mappingData.getStartPosition() < pivotMappingData.getStartPosition()) {
						tempListOfAllMappingDataLess.add(mappingData);
					} else if(mappingData.getStartPosition() == pivotMappingData.getStartPosition()) {
						if(0 >= mappingData.getMapName().compareTo(pivotMappingData.getMapName())) {
							tempListOfAllMappingDataLess.add(mappingData);
						} else if(0 < mappingData.getMapName().compareTo(pivotMappingData.getMarkerName())) {
							tempListOfAllMappingDataGreat.add(mappingData);
						} 
					}
				}
		}
		return concatenate(sort(tempListOfAllMappingDataLess), pivotMappingData, sort(tempListOfAllMappingDataGreat));
	}
	
	private List<MapInfo> concatenate(List<MapInfo> sort,
			MapInfo pivotMappingData, List<MapInfo> sort2) {
		List<MapInfo> listOfMappingDataToReturn = new ArrayList<MapInfo>();
		for (MapInfo mappingData : sort) {
			listOfMappingDataToReturn.add(mappingData);
		}
		listOfMappingDataToReturn.add(pivotMappingData);
		for (MapInfo mappingData : sort2) {
			listOfMappingDataToReturn.add(mappingData);
		}
		
		return listOfMappingDataToReturn;
	}

}
