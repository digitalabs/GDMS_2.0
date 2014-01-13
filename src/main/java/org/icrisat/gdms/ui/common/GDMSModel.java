package org.icrisat.gdms.ui.common;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.pojos.User;
import org.icrisat.gdms.common.GDMSException;

import com.vaadin.ui.MenuBar.MenuItem;


public class GDMSModel {
	
	private static GDMSModel _gdmsModel;
	private static boolean instance_flag;
	private MenuItem _menuItemSelected;
	private User loggedInUser;
	private DatabaseConnectionParameters localParams;
	private ManagerFactory factory;
	private DatabaseConnectionParameters centralParams;
	private DatabaseConnectionParameters workbenchParams;
	private String strMapSelected;
	private String strMarkerForMap;
	private String strGenotypicMatrixFile;
	private String strMappingType;
	private ArrayList<String> listOfTemplateDataColumns;
	private String strDatasetSelected;
	private String strGermplasmSelected;

	
	private GDMSModel() throws GDMSException{
		initLocalDatabaseConnectionParameters();
		try{			
		
			initManagerFactoryForUploadingMarker();
		}catch (ConfigException e) {
			e.printStackTrace();
			//_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			//return null;
		}
	}

	public static GDMSModel getGDMSModel() {
		if (!instance_flag){
			try {
				_gdmsModel = new GDMSModel();
			} catch (GDMSException e) {
				e.printStackTrace();
				// TODO Will have to handle it
			}
			instance_flag = true;
		}
		return _gdmsModel;
	}

	public void finalize(){
		instance_flag = false;
	}

	public void setMenuItemSelected(MenuItem theMenuItemSelected){
		_menuItemSelected = theMenuItemSelected;
	}
	
	public MenuItem getMenuItemSelected(){
		return _menuItemSelected;
	}

	public void setLoggedInUser(User theLoggedInUser) {
		loggedInUser = theLoggedInUser;
	}
	
	public User getLoggedInUser(){
		return loggedInUser;
	}
	
	private void initLocalDatabaseConnectionParameters() throws GDMSException{

		Properties prop = new Properties();

		ClassLoader classLoader = GDMSModel.class.getClassLoader();
		InputStream resourceAsStream = classLoader.getResourceAsStream("DatabaseConfig.properties");

		//load a properties file
		try {
			prop.load(resourceAsStream);
			
			String strLocalHost = prop.getProperty("local.host");
			String strLocalPort = prop.getProperty("local.port");
			String strLocalDBName = prop.getProperty("local.dbname");
			String strLocalUserName = prop.getProperty("local.username");
			String strLocalPassword = prop.getProperty("local.password");
			
			localParams = new DatabaseConnectionParameters(strLocalHost, strLocalPort, strLocalDBName, strLocalUserName, strLocalPassword);
			
			String LocalHost = prop.getProperty("central.host");
			String LocalPort = prop.getProperty("central.port");
			String LocalDBName = prop.getProperty("central.dbname");
			String LocalUserName = prop.getProperty("central.username");
			String LocalPassword = prop.getProperty("central.password");
			
			centralParams = new DatabaseConnectionParameters(LocalHost, LocalPort, LocalDBName, LocalUserName, LocalPassword);
			
			String WorkbenchHost = prop.getProperty("workbench.host");
			String WorkbenchPort = prop.getProperty("workbench.port");
			String WorkbenchDBName = prop.getProperty("workbench.dbname");
			String WorkbenchUserName = prop.getProperty("workbench.username");
			String WorkbenchPassword = prop.getProperty("workbench.password");
			
			workbenchParams = new DatabaseConnectionParameters(WorkbenchHost, WorkbenchPort, WorkbenchDBName, WorkbenchUserName, WorkbenchPassword);
			
			
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}

	}
	
	private void initManagerFactoryForUploadingMarker(){
		factory = new ManagerFactory(localParams, centralParams);
	}
	
	public HibernateSessionProvider getHibernateSessionProviderForLocal(){
		return factory.getSessionProviderForLocal();
	}
	
	public HibernateSessionProvider getHibernateSessionProviderForCentral(){
		return factory.getSessionProviderForCentral();
	}
	
	public DatabaseConnectionParameters getLocalParams(){
		return localParams;
	}
	
	public DatabaseConnectionParameters getCentralParams(){
		return centralParams;
	}

	
	public DatabaseConnectionParameters getWorkbenchParams(){
		return workbenchParams;
	}

	public void setMapSelected(String theMapSelected) {
		strMapSelected = theMapSelected;
	}
	
	public String getMapSelected(){	
		return strMapSelected;
	}
	

	public String getMarkerForMap() {
		return strMarkerForMap;
	}
	
	public String getMappingType(){
		return strMappingType;
	}

	public void setMarkerForMap(String strMappingType, String strSelectedMarkerType) {
		this.strMappingType = strMappingType;
		this.strMarkerForMap = strSelectedMarkerType;
	}

	public void setGenotypicMatrixFileName(String theFileName) {
		strGenotypicMatrixFile = theFileName;
	}
	
	public String getGenotypicMatrixFileName(){
		return strGenotypicMatrixFile;
	}

	public void setListOfVariableColumns(ArrayList<String> thelistOfTemplateDataColumns) {
		listOfTemplateDataColumns = thelistOfTemplateDataColumns;
	}
	
	public ArrayList<String> getListOfVariableColumns(){
		return listOfTemplateDataColumns;
	}

	public void setDatasetSelected(String theSelecteDataset) {
		strDatasetSelected = theSelecteDataset;
	}
	
	public String getDatasetSelected(){
		return strDatasetSelected;
	}
	
	public void setGermplasmSelected(String theGermplasmName) {
		strGermplasmSelected = theGermplasmName;
	}

	public String getGermplasmSelected() {
		return strGermplasmSelected;
	}

}
