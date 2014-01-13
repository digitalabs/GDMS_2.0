package org.icrisat.gdms.deletedata;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.ui.GDMSInfoDialog;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class DataDeletionAction {

	private GDMSMain _mainHomePage;
	
	GenotypicDataManager genoManager=null;
	ManagerFactory factory =null;
	
	public DataDeletionAction(GDMSMain mainHomePage) {
		_mainHomePage = mainHomePage;
	}


	public boolean deleteQTLInfoData(List<String> itemsToDelete) {
		if(null == itemsToDelete) {
			return false;
		}
		/*HibernateSessionProvider hibernateSessionProviderForLocal = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal();
		Session session = hibernateSessionProviderForLocal.getSession();
		Transaction beginTransaction = session.beginTransaction();*/
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		GenotypicDataManager genoManager=factory.getGenotypicDataManager();
		
		try {
			String strDisplayMessage = "Successfully deleted following QTL \n\n";
			int iCount = 1;
			for (String string : itemsToDelete) {
				strDisplayMessage += iCount++ + ". "+ string +"\n";
				List<DatasetElement> detailsByName=genoManager.getDatasetDetailsByDatasetName(string, Database.LOCAL);
				//List<DatasetElement> detailsByName = datasetDAO.getDetailsByName(string);
				Integer datasetID = 0;
				List<Integer> datasetIds=new ArrayList<Integer>();
				for (DatasetElement datasetElement : detailsByName) {
					datasetID = datasetElement.getDatasetId();
					datasetIds.add(datasetElement.getDatasetId());
				}
				
				List<Integer> qtlIds =genoManager.getQTLIdsByDatasetIds(datasetIds);
				
				genoManager.deleteQTLs(qtlIds, datasetID);
				
							
			}
			//beginTransaction.commit();
			Window messageWindow = new Window("Delete");
			GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage, strDisplayMessage);
			messageWindow.addComponent(gdmsMessageWindow);
			messageWindow.setWidth("400px");
			messageWindow.setBorder(Window.BORDER_NONE);
			messageWindow.setClosable(true);
			messageWindow.center();

			if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
				_mainHomePage.getMainWindow().addWindow(messageWindow);
			} 
			return true;
		} catch (Throwable th) {
			th.printStackTrace();
			/*if(null != beginTransaction) {
				beginTransaction.rollback();
			}*/
			String message = th.getMessage();
			Throwable cause = th.getCause();
			if(null != cause) {
				message += "<br>" + cause.getMessage();
			}
			_mainHomePage.getMainWindow().getWindow().showNotification(message,  Notification.TYPE_ERROR_MESSAGE);
		}
		return false;
	}

	
	public boolean deleteMapData(List<String> itemsToDelete) {
		if(null == itemsToDelete) {
			return false;
		}
		/*HibernateSessionProvider hibernateSessionProviderForLocal = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal();
		Session session = hibernateSessionProviderForLocal.getSession();
		Transaction beginTransaction = session.beginTransaction();*/
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		GenotypicDataManager genoManager=factory.getGenotypicDataManager();
		
		Integer mapid = 0;
		try {
			String strDisplayMessage = "Successfully deleted following Map \n\n";
			//System.out.println(".............   Maps data");
			int iCount = 1;
			for (String string : itemsToDelete) {
				strDisplayMessage += iCount++ + ". "+ string +"\n";
//				String[] strArr2=strArr1[2].split("!~!");
//				for(int d=0;d<strArr2.length;d++){
				/*MapDAO mapDAO = new MapDAO();
				mapDAO.setSession(session);*/
				
				mapid=genoManager.getMapIdByName(string);
				
				/*List<Map> datasetID = mapDAO.getAll();
			
				for (Map map : datasetID) {
					if(map.getMapName().equals(string)) {
						mapid = map.getMapId();
					}
				}*/
				//rs=stmt.executeQuery("select map_id from gdms_map where map_name='"+strArr2[d]+"'");
					//while(rs.next()){
						//datasetID=rs.getInt(1);						
					//}
				/*SQLQuery query = session.createSQLQuery("DELETE from gdms_markers_onmap WHERE map_id = " + mapid);
				int executeUpdate = query.executeUpdate();
				//	int del=stmtR.executeUpdate("delete from gdms_markers_onmap where map_id='"+datasetID+"'");
				
				query = session.createSQLQuery("DELETE from gdms_map WHERE map_id = " + mapid);
				int executeUpdate2 = query.executeUpdate();*/
				List<QtlDetails> qtls =genoManager.getQtlDetailsByMapId(mapid);
				if(qtls == null && qtls.size() == 0)				
					genoManager.deleteMaps(mapid);
				else
					return false;
				
				
				//int delDA=stmtR.executeUpdate("delete from gdms_map where map_id='"+datasetID+"'");	
//				}
			}
			//beginTransaction.commit();
			
			Window messageWindow = new Window("Delete");
			GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage, strDisplayMessage);
			messageWindow.addComponent(gdmsMessageWindow);
			messageWindow.setWidth("400px");
			messageWindow.setBorder(Window.BORDER_NONE);
			messageWindow.setClosable(true);
			messageWindow.center();

			if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
				_mainHomePage.getMainWindow().addWindow(messageWindow);
			} 
			return true;
		} catch (Throwable th) {
			th.printStackTrace();
			/*if(null != beginTransaction) {
				beginTransaction.rollback();
			}*/
			String message = th.getMessage();
			Throwable cause = th.getCause();
			if(null != cause) {
				message += "<br>" + cause.getMessage();
			}
			_mainHomePage.getMainWindow().getWindow().showNotification(message,  Notification.TYPE_ERROR_MESSAGE);
		}
//		}
		return false;
	}
	
	public boolean deleteGenotypingData(List<String> itemsToDelete) {
		if(null == itemsToDelete) {
			return false;
		}
		
		boolean bDataDeleted = true;
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		GenotypicDataManager genoManager=factory.getGenotypicDataManager();
		
		
		HibernateSessionProvider hibernateSessionProviderForLocal = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal();
		Session session = hibernateSessionProviderForLocal.getSession();
		Transaction beginTransaction = session.beginTransaction();
		
		String strDisplayMessage = "Successfully deleted following Genotyping \n\n";
		
		try {
			int iCount = 1;
			for (String string : itemsToDelete) {
				strDisplayMessage += iCount++ + ". "+ string +"\n";
				//			String[] strArr2=strArr1[0].split("!~!");
				//			for(int d=0;d<strArr2.length;d++){					
				/*List<DatasetElement> results = gdms.getDatasetDetailsByDatasetName(strArr2[d], Database.LOCAL);
		        System.out.println("RESULTS (testGetDatasetDetailsByDatasetName): " + results);*/
				/*DatasetDAO datasetDAO = new DatasetDAO();
				datasetDAO.setSession(session);
				*/
				List<DatasetElement> detailsByName =genoManager.getDatasetDetailsByDatasetName(string, Database.LOCAL);
				
				//List<DatasetElement> detailsByName = datasetDAO.getDetailsByName(string);
				String datasetType = null;
				Integer datasetID = null;
				for (DatasetElement datasetElement : detailsByName) {
					datasetID = datasetElement.getDatasetId();
					datasetType = datasetElement.getDatasetType();
				}
				//System.out.println("datasetID="+datasetID+"    datasetType:"+datasetType);
				if(datasetType.equalsIgnoreCase("SNP")){
										
					genoManager.deleteSNPGenotypingDatasets(datasetID);
					
					
					//int del3=stD.executeUpdate("delete from gdms_dataset where dataset_id='"+datasetID+"'");	
				}else if(datasetType.equalsIgnoreCase("SSR")){
									
					genoManager.deleteSSRGenotypingDatasets(datasetID);
					
					
					//int del3=stD.executeUpdate("delete from gdms_dataset where dataset_id='"+datasetID+"'");	
				}else if(datasetType.equalsIgnoreCase("DArT")){
					
					
					genoManager.deleteDArTGenotypingDatasets(datasetID);
					
					//int del3=stD.executeUpdate("delete from gdms_dataset where dataset_id='"+datasetID+"'");	
				}else if(datasetType.equalsIgnoreCase("mapping")){
					/*String exists="no";
					MarkerMetadataSetDAO markerMetadataSetDAO = new MarkerMetadataSetDAO();
					markerMetadataSetDAO.setSession(session);
					List<Integer> markerIdByDatasetId = markerMetadataSetDAO.getMarkerIdByDatasetId(datasetID);

					MarkerDAO markerDAO = new MarkerDAO();
					markerDAO.setSession(session);
					List<String> markerTypeByMarkerIds = markerDAO.getMarkerTypeByMarkerIds(markerIdByDatasetId);
					String marker_type = null;
					for (String string2 : markerTypeByMarkerIds) {
						marker_type = string2;
					}

					//rs1=stP.executeQuery("select distinct marker_type from gdms_marker where marker_id in(select marker_id from gdms_marker_metadataset where dataset_id="+datasetID+")");
					//while(rs1.next()){
					//System.out.println(rs1.getString(1));
					//marker_type=rs1.getString(1);
					//}
					MappingPopDAO mappingPopDAO = new MappingPopDAO();
					mappingPopDAO.setSession(session);
					List<ParentElement> parentsByDatasetId = mappingPopDAO.getParentsByDatasetId(datasetID);
					String mapping_type = null;
					for (ParentElement parentElement : parentsByDatasetId) {
						mapping_type = parentElement.getMappingType();
					}
					//rs3=stmtPD.executeQuery("select mapping_type from gdms_mapping_pop where dataset_id="+datasetID);
					//while(rs3.next()){
					//mapping_type=rs3.getString(1);
					//}
					if(mapping_type.equalsIgnoreCase("allelic")){
						if(marker_type.equalsIgnoreCase("snp")){
							CharValuesDAO charValuesDAO = new CharValuesDAO();
							charValuesDAO.setSession(session);
							List<AllelicValueWithMarkerIdElement> allelicValuesByDatasetId = charValuesDAO.getAllelicValuesByDatasetId(datasetID, 0, charValuesDAO.getAll().size());
							if(allelicValuesByDatasetId.size() > 0){
								exists="yes";
							}
							System.out.println(exists);
							if(exists.equalsIgnoreCase("yes")){
								System.out.println("if exists");
								SQLQuery query = session.createSQLQuery("DELETE from gdms_char_values WHERE dataset_id = " + datasetID);
								int executeUpdate = query.executeUpdate();
								//int delPD=stPD.executeUpdate("delete from gdms_char_values where dataset_id="+datasetID);
							}
						}else if((marker_type.equalsIgnoreCase("ssr"))||(marker_type.equalsIgnoreCase("DArT"))){
							AlleleValuesDAO alleleValuesDAO = new AlleleValuesDAO();
							alleleValuesDAO.setSession(session);
							List<AllelicValueWithMarkerIdElement> allelicValuesByDatasetId = alleleValuesDAO.getAllelicValuesByDatasetId(datasetID, 0, alleleValuesDAO.getAll().size());
							//rs2=stmtP.executeQuery("select * from gdms_allele_values where dataset_id="+datasetID);
							if(allelicValuesByDatasetId.size() > 0){
								exists="yes";
							}
							if(exists.equalsIgnoreCase("yes")){
								SQLQuery query = session.createSQLQuery("DELETE from gdms_allele_values WHERE dataset_id = " + datasetID);
								int executeUpdate = query.executeUpdate();
								//int delPD=stPD.executeUpdate("delete from gdms_allele_values where dataset_id="+datasetID);
							}
						}
					}
					SQLQuery query = session.createSQLQuery("DELETE from gdms_mapping_pop_values WHERE dataset_id = " + datasetID);
					int executeUpdate = query.executeUpdate();
					//int del=stmtR.executeUpdate("delete from gdms_mapping_pop_values where dataset_id='"+datasetID+"'");

					query = session.createSQLQuery("DELETE from gdms_mapping_pop WHERE dataset_id = " + datasetID);
					int executeUpdate1 = query.executeUpdate();
					//int delDA=stmtR.executeUpdate("delete from gdms_mapping_pop where dataset_id='"+datasetID+"'");		

					query = session.createSQLQuery("DELETE from gdms_dataset_users WHERE dataset_id = " + datasetID);
					int executeUpdate2 = query.executeUpdate();
					//int del1=st.executeUpdate("delete from gdms_dataset_users where dataset_id='"+datasetID+"'");

					query = session.createSQLQuery("DELETE from gdms_acc_metadataset WHERE dataset_id = " + datasetID);
					int executeUpdate3 = query.executeUpdate();
					//int del2=stR.executeUpdate("delete from gdms_acc_metadataset where dataset_id='"+datasetID+"'");

					query = session.createSQLQuery("DELETE from gdms_marker_metadataset WHERE dataset_id = " + datasetID);
					int executeUpdate4 = query.executeUpdate();
					//int del4=stDa.executeUpdate("delete from gdms_marker_metadataset where dataset_id='"+datasetID+"'");

					query = session.createSQLQuery("DELETE from gdms_dataset WHERE dataset_id = " + datasetID);
					int executeUpdate5 = query.executeUpdate();*/
					
					
					genoManager.deleteMappingPopulationDatasets(datasetID);
					
					
					//int del3=stD.executeUpdate("delete from gdms_dataset where dataset_id='"+datasetID+"'");	
				} else {
					strDisplayMessage = "Could not delete selected data. Unknown Dataset-Type.";
					bDataDeleted = false;
				}

			}
			//beginTransaction.commit();
			Window messageWindow = new Window("Delete");
			GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage, strDisplayMessage);
			messageWindow.addComponent(gdmsMessageWindow);
			messageWindow.setWidth("400px");
			messageWindow.setBorder(Window.BORDER_NONE);
			messageWindow.setClosable(true);
			messageWindow.center();

			if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
				_mainHomePage.getMainWindow().addWindow(messageWindow);
			} 
			//_mainHomePage.getMainWindow().getWindow().showNotification(strDisplayMessage,  Notification.TYPE_HUMANIZED_MESSAGE);
			return bDataDeleted;
		} catch (Throwable th) {
			th.printStackTrace();
			if(null != beginTransaction) {
				beginTransaction.rollback();
			}
			String message = th.getMessage();
			Throwable cause = th.getCause();
			if(null != cause) {
				message += "<br>" + cause.getMessage();
			}
			_mainHomePage.getMainWindow().getWindow().showNotification(message,  Notification.TYPE_ERROR_MESSAGE);
		}
		return false;
	}
}
