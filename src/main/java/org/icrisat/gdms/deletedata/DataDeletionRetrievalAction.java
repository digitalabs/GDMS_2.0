package org.icrisat.gdms.deletedata;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.Map;
import org.hibernate.Session;
import org.icrisat.gdms.ui.common.GDMSModel;

public class DataDeletionRetrievalAction {

//	private List<String[]> buildMarkerOnLoad() {
//		List<String[]> listString = new ArrayList<String[]>();
//		RetrieveMap retrieveMarker = new RetrieveMap();
//		List<MapDetailElement> retrieveMaps = null;
//		try {
//			retrieveMaps = retrieveMarker.retrieveMaps();
//		} catch (MiddlewareQueryException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		List<MappingData> retrieveMappingData = null;
//		try {
//			retrieveMappingData = retrieveMarker.retrieveMappingData();
//		} catch (MiddlewareQueryException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		final List<MapDetailElement> finalretrieveMarker2 = retrieveMaps;
//		if(null != finalretrieveMarker2) {
//			int iCounter = 0;
//			
//			
//			ArrayList<String> strDataM=new ArrayList<String>();
//			for (MapDetailElement mapsDetails : finalretrieveMarker2) {
//				String strMapUnit = mapsDetails.getMapType();
//				Integer strMapId = 0;
//				for (MappingData mappingData : retrieveMappingData) {
//					if(mappingData.getMapName().equals(mapsDetails.getMapName())) {
//						strMapUnit = mappingData.getMapUnit();
//						strMapId = mappingData.getMapId();
//						break;
//					}
//				}
//				strDataM.add(mapsDetails.getMarkerCount()+"!~!"+mapsDetails.getMaxStartPosition()+"!~!"+mapsDetails.getLinkageGroup()+"!~!"+mapsDetails.getMapName()+"!~!"+strMapUnit + "!~!" + strMapId);
//			}
//			String[] strArr=strDataM.get(0).toString().split("!~!");
//			
//			String chr=strArr[3];
//			int mCount=Integer.parseInt(strArr[0]);
//			float distance=Float.parseFloat(strArr[1]);
//			int mc=0;
//			float d=0;
//			List<ArrayList<String>> mapFinalList= new ArrayList<ArrayList<String>>();
//			String mType="";
//			//System.out.println("strDataM.size()=:"+strDataM.size());
//			for(int a=0;a<strDataM.size();a++){	
//				String mapType="";
//				String[] str1=strDataM.get(a).toString().split("!~!");		
//				//System.out.println(" a="+a+" ,,,,markerCount="+str1[0]+"    ;startPosition="+str1[1]+"  ;LinkageGroup="+str1[2]+"  ;MapName="+str1[3]);
//				if(str1[3].equals(chr)){
//					mc=mc+Integer.parseInt(str1[0]);
//					d=d+Float.parseFloat(str1[1]);	
//					mType=str1[4];
//					//System.out.println("..mc="+mc+"   d:"+d);
//					if(a==(strDataM.size()-1)){
//						//System.out.println("IF in IF "+mapType);
//						mCount=mc;
//						distance=d;
//						mapType=mType;
//						ArrayList<String> listOfData = new ArrayList<String>();
//						listOfData.add(String.valueOf(mCount));
//						listOfData.add(chr);
//						listOfData.add(String.valueOf(distance));
//						listOfData.add(mapType);
//						listOfData.add(str1[5]);
//						listOfData.add(str1[3]);
//						mapFinalList.add(listOfData);								
//					}
//				}else if(!(str1[3].equals(chr))){							
//					mCount=mc;
//					distance=d;
//					mapType=mType;
//					//System.out.println("else IF in IF "+mapType+ ".... "+chr);
//					//mapFinalList.add(mCount+"!~!"+chr+"!~!"+distance+"!~!"+mapType+";;");
//					ArrayList<String> listOfData = new ArrayList<String>();
//					listOfData.add(String.valueOf(mCount));
//					listOfData.add(chr);
//					listOfData.add(String.valueOf(distance));
//					listOfData.add(mapType);
//					listOfData.add(str1[5]);
//					listOfData.add(str1[3]);
//					mapFinalList.add(listOfData);								
//					mc=0;
//					d=0;
//					mType="";
//					chr=str1[3];
//					a=a-1;
//				}						
//			}
//
//			
//			iCounter = 0;
//			int i = 0;
//			for (ArrayList<String> listOfData : mapFinalList) {
//				String strCount = listOfData.get(0);
//				String strChr = listOfData.get(1);
//				String strDistance = listOfData.get(2);
//				String strMapType = listOfData.get(3);
//				String strMapId = listOfData.get(4);
//				String strMapName = listOfData.get(5);
//				
//				String[] strValues = new String[4];
//				strValues[0] = strMapId;
//				strValues[1] = strMapName;
//				strValues[2] = strCount;
//				strValues[3] = strDistance;
//				listString.add(strValues);
//				iCounter++;
//				i++;
//			}
//		}
//		return listString;
//	}

	
	
	public ArrayList<String> getGenotypingDataList()
			throws Exception {
		ArrayList<String> gList = new ArrayList<String>();
		try{
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			List<Dataset> allDataset = datasetDAO.getAll();
			List<String> datasetNames = datasetDAO.getDatasetNames(0, allDataset.size());
			for (String string : datasetNames) {
				gList.add(string);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return gList;
	}
	
	public ArrayList<String> getCentralGenotypingDataList() throws Exception {
		ArrayList<String> gList = new ArrayList<String>();
		try{
			Session session =GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			List<Dataset> allDataset = datasetDAO.getAll();
			List<String> datasetNames = datasetDAO.getDatasetNames(0, allDataset.size());
			for (String string : datasetNames) {
				gList.add(string);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return gList;
	}
	
	public ArrayList<String> getMapsList()
			throws Exception {
		ArrayList<String> mList = new ArrayList<String>();
		try{
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();

			MapDAO mapDAO = new MapDAO();
			mapDAO.setSession(session);
			List<Map> allMaps = mapDAO.getAll();
			for (Map map2 : allMaps) {
				mList.add(map2.getMapName());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return mList;
	}
	
	public ArrayList<String> getCentralMapsList() throws Exception {
		ArrayList<String> mList = new ArrayList<String>();
		try{
			Session session =GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();

			MapDAO mapDAO = new MapDAO();
			mapDAO.setSession(session);
			List<Map> allMaps = mapDAO.getAll();
			for (Map map2 : allMaps) {
				mList.add(map2.getMapName());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return mList;
	}
	
	
	public ArrayList<String> getQTLInfoList() throws Exception {
		ArrayList<String> qList = new ArrayList<String>();
		try {
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			List<Dataset> allDataset = datasetDAO.getAll();

			for (Dataset dataset : allDataset) {
				if(dataset.getDatasetType().equalsIgnoreCase("qtl")) {
					qList.add(dataset.getDatasetName());
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return qList;
	}
	
	public ArrayList<String> getCentralQTLInfoList() throws Exception {
		ArrayList<String> qList = new ArrayList<String>();
		try {
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			List<Dataset> allDataset = datasetDAO.getAll();

			for (Dataset dataset : allDataset) {
				if(dataset.getDatasetType().equalsIgnoreCase("qtl")) {
					qList.add(dataset.getDatasetName());
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return qList;
	}
	
}
