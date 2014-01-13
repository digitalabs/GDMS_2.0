package org.icrisat.gdms.ui;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.pojos.gdms.MappingData;

public class SortingMaps {

	public List<MappingData> sort(List<MappingData> theListOfAllMappingData) {
		if(1 >= theListOfAllMappingData.size()) {
			return theListOfAllMappingData;
		}
		MappingData pivotMappingData = theListOfAllMappingData.remove(theListOfAllMappingData.size()/2);
		
		return sort(theListOfAllMappingData, pivotMappingData);
	}
	
	
	public List<MappingData> sort(List<MappingData> theListOfAllMappingData, MappingData pivotMappingData) {
		List<MappingData> tempListOfAllMappingDataLess = new ArrayList<MappingData>();
		List<MappingData> tempListOfAllMappingDataGreat = new ArrayList<MappingData>();
		for (int i = 0; i < theListOfAllMappingData.size(); i++) {
				MappingData mappingData = theListOfAllMappingData.get(i);
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
	
	private List<MappingData> concatenate(List<MappingData> sort,
			MappingData pivotMappingData, List<MappingData> sort2) {
		List<MappingData> listOfMappingDataToReturn = new ArrayList<MappingData>();
		for (MappingData mappingData : sort) {
			listOfMappingDataToReturn.add(mappingData);
		}
		listOfMappingDataToReturn.add(pivotMappingData);
		for (MappingData mappingData : sort2) {
			listOfMappingDataToReturn.add(mappingData);
		}
		
		return listOfMappingDataToReturn;
	}

}
