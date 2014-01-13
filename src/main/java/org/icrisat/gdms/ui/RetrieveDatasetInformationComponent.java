package org.icrisat.gdms.ui;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.dao.gdms.MappingPopDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionPerThreadProvider;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.hibernate.HibernateUtil;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.AllelicValueWithMarkerIdElement;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MappingPop;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.ParentElement;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.FileDownloadResource;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.OptionWindowForFlapjackMap;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class RetrieveDatasetInformationComponent implements Component.Listener {
	private static final long serialVersionUID = 1L;

	private TabSheet _tabsheetForDataset;
	private Component _buildDatasetResultComponent;
	private Component _buildDatasetFormatComponent;
	private GDMSMain _mainHomePage;
	protected String strDatasetID;
	protected String strDatasetName;
	private String strSelectedMap;
	private String strSelectedColumn;
	protected String strSelectedFormat;
	HashMap<Integer, String> hashMapOfMapIDsAndNames = new HashMap<Integer, String>(); 
	private Session localSession;
	private Session centralSession;
	private ArrayList<Map> listOfAllMaps;
	private HashMap<String, Integer> hmOfMapNameAndMapId;
	protected File matrixFileForDatasetRetrieval;
	protected String strDatasetType;
	private ArrayList<Integer> listOfParentGIDs;
	protected Integer iSelectedMapId;
	public File generatedTextFile;
	public File generatedMapFile;
	public File generatedDatFile;
	protected List<File> listOfmatrixTextFileDataSSRDataset;
	int markersCount=0;
	ManagerFactory factory=null;
	GermplasmDataManager germManager;
	GenotypicDataManager genoManager;
	List<AllelicValueWithMarkerIdElement> alleleValues;
	ArrayList intAlleleValues=new ArrayList();
	
	private static WorkbenchDataManager workbenchDataManager;
	private static HibernateUtil hibernateUtil;
	 
	private TreeMap<Integer, String> sortedMapOfGIDsAndGNames;
	
	int parentANid=0;
	int parentBNid=0;
	
	int parentAGid=0;
	int parentBGid=0;
	String mappingType="";
	String parentsListToWrite="";
	private HashMap<Integer, String> parentsGIDsNames;
	
	HashMap<Object, String> IBWFProjects= new HashMap<Object, String>();
	 
	 String bPath="";
     String opPath="";
    
     //System.out.println(",,,,,,,,,,,,,  :"+bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1));
     String pathWB="";
     
	    String dbNameL="";
	    
	protected String strSelectedMappingType = "";
	
	public RetrieveDatasetInformationComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		try{
			factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			germManager=factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
			
			//hibernateUtil = new HibernateUtil(workbenchDb.getHost(), workbenchDb.getPort(), workbenchDb.getDbName(), workbenchDb.getUsername(), workbenchDb.getPassword());
			hibernateUtil = new HibernateUtil(GDMSModel.getGDMSModel().getWorkbenchParams());
			HibernateSessionProvider sessionProvider = new HibernateSessionPerThreadProvider(hibernateUtil.getSessionFactory());
			workbenchDataManager = new WorkbenchDataManagerImpl(sessionProvider);
		}catch (FileNotFoundException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}catch (IOException ei) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}catch (URISyntaxException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * 
	 * Building the entire Tabbed Component required for Dataset
	 * 
	 */
	public HorizontalLayout buildTabbedComponentForQTL() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		_tabsheetForDataset = new TabSheet();
		//_tabsheetForDataset.setSizeFull();
		_tabsheetForDataset.setWidth("700px");

		Component buildDatasetDataSetComponent = buildDatasetDataSetComponent();
		_buildDatasetFormatComponent = buildDatasetFormatComponent();
		_buildDatasetResultComponent = buildDatasetResultComponent();
		
		buildDatasetDataSetComponent.setSizeFull();
		_buildDatasetFormatComponent.setSizeFull();
		_buildDatasetResultComponent.setSizeFull();
		
		_tabsheetForDataset.addComponent(buildDatasetDataSetComponent);
		_tabsheetForDataset.addComponent(_buildDatasetFormatComponent);
		_tabsheetForDataset.addComponent(_buildDatasetResultComponent);

		
		_tabsheetForDataset.getTab(1).setEnabled(false);
		_tabsheetForDataset.getTab(2).setEnabled(false);
		
		horizontalLayout.addComponent(_tabsheetForDataset);

		return horizontalLayout;
	}

	private Component buildDatasetDataSetComponent() {
		VerticalLayout datasetLayout = new VerticalLayout();
		datasetLayout.setCaption("Dataset");
		datasetLayout.setSpacing(true);
		datasetLayout.setSizeFull();
		datasetLayout.setMargin(true, true, true, true);

		Panel panelForTable = new Panel();
		panelForTable.setWidth("680px");

		final Table tableForDatasetResults = buildDatasetDetailsTable();
		datasetLayout.addComponent(tableForDatasetResults);
		datasetLayout.setComponentAlignment(tableForDatasetResults, Alignment.MIDDLE_CENTER);

		
		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				
				Object rowID = tableForDatasetResults.getValue();
				if (null != rowID){
					Container containerDataSource = tableForDatasetResults.getContainerDataSource();
					Item item = containerDataSource.getItem(rowID);
					
					if (null != item){
						Dataset dataset = listOfAllDatasets.get((Integer) rowID);
						
						//strDatasetID = item.getItemProperty("DATASET-ID").toString();
						strDatasetName = item.getItemProperty("DATASET-NAME").toString();
						
						
						try{
							//20131207:   Kalyani as datasetid is not shown its returning null
							List<DatasetElement> resultsL =genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.LOCAL);
							//System.out.println("from Local resultsL=:"+resultsL);
							if(resultsL.isEmpty()){
								//System.out.println("if null");
								resultsL = genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.CENTRAL);
							}
							for (DatasetElement result : resultsL){
					        	//System.out.println("  " + result.getDatasetId());
					        	strDatasetID=result.getDatasetId().toString();
					        }
							
						
						}catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
						
						//20131112: Tulasi --- Made the following change to get the Dataset type from the list of Datasets 
						//obtained initially 
						//strDatasetType = item.getItemProperty("DATASET-TYPE").toString();
						strDatasetType = dataset.getDatasetType();
						
						//System.out.println("Dataset selected in the table: " + strDatasetID + " --- " + strDatasetName + " --- " + strDatasetType);
						
						VerticalLayout newFormatComponent = (VerticalLayout) buildDatasetFormatComponent();
						_tabsheetForDataset.replaceComponent(_buildDatasetFormatComponent, newFormatComponent);
						_buildDatasetFormatComponent = newFormatComponent;
						_buildDatasetFormatComponent.requestRepaint();
						_tabsheetForDataset.getTab(1).setEnabled(true);
						_tabsheetForDataset.setSelectedTab(_buildDatasetFormatComponent);
						_tabsheetForDataset.requestRepaint();
					}else {
						if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
							_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click next.", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}
				} else {
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});

		datasetLayout.addComponent(layoutForButton);
		datasetLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);

		return datasetLayout;
	}

	private Table buildDatasetDetailsTable() {
		Table tableForDatasetDetails = new Table();
		tableForDatasetDetails.setWidth("650px");
		tableForDatasetDetails.setSelectable(true);
		tableForDatasetDetails.setColumnCollapsingAllowed(true);
		tableForDatasetDetails.setColumnReorderingAllowed(false);
		tableForDatasetDetails.setStyleName("strong");

		/*String[] strArrayOfColNames = {"DATASET-ID", "DATASET-NAME", "DATASET-DESC", "DATASET-TYPE",
                "GENUS", "SPECIES", "DATATYPE"};*/
		
		/*String[] strArrayOfColNames = {"DATASET-ID", "DATASET-NAME", "DATASET-DESC", "SPECIES"};*/
		
		//20131206: Tulasi --- Modified the columns to be displayed
		String[] strArrayOfColNames = {"DATASET-NAME", "DATASET-DESC", "DATASET-TYPE", "DATASET SIZE(Genotypes x Markers)"};
		
		for (int i = 0; i < strArrayOfColNames.length; i++){
			tableForDatasetDetails.addContainerProperty(strArrayOfColNames[i], String.class, null);
			tableForDatasetDetails.setColumnWidth(strArrayOfColNames[i], 135);
		}
		
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
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return null;
		}
		
		
		listOfAllDatasets = new ArrayList<Dataset>();
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
		// 20131209 kalyani added code to show the dataset size
		
		ArrayList datasetIdsList=new ArrayList();
		 HashMap<Integer, String> datasetSize=new HashMap<Integer, String>();
		
		
		for (int i = 0; i < listOfAllDatasets.size(); i++){
			Dataset dataset = listOfAllDatasets.get(i);			
			datasetIdsList.add(dataset.getDatasetId());					
			try{
				int markerCount=(int)genoManager.countMarkersFromMarkerMetadatasetByDatasetIds(datasetIdsList);
				int nidsCount=(int)genoManager.countNidsFromAccMetadatasetByDatasetIds(datasetIdsList);				
				String size=nidsCount+" x "+markerCount;
				datasetSize.put(Integer.parseInt(dataset.getDatasetId().toString()), size);
			} catch (MiddlewareQueryException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
				return null;
			}
			datasetIdsList.clear();
		}
		
		for (int i = 0; i < listOfAllDatasets.size(); i++){

			Dataset dataset = listOfAllDatasets.get(i);			
			datasetIdsList.add(dataset.getDatasetId());
			String strDatasetName = dataset.getDatasetName();
			String strDatasetDesc = dataset.getDatasetDesc();
			String strDatasetType = dataset.getDatasetType();
			String strCount = "0";
			//System.out.println("datasetIdsList=:"+datasetIdsList);			
			//String strGenus = dataset.getGenus();
			//String strSpecies = dataset.getSpecies();
			//String strDataType = dataset.getDataType();
			
			//"DATASET-ID", "DATASET-NAME", "DATASET-DESC", "DATASET-TYPE",
            //"GENUS", "SPECIES", "DATATYPE", "SELECT"
			/*tableForDatasetDetails.addItem(new Object[] {datasetId, strDatasetName, strDatasetDesc, strDatasetType,
					strGenus, strSpecies, strDataType}, new Integer(i));*/
			
			/*tableForDatasetDetails.addItem(new Object[] {datasetId, strDatasetName, strDatasetDesc, strSpecies}, new Integer(i));*/
			
			//20131206: Tulasi --- Modified the data being displayed
			strCount=datasetSize.get(dataset.getDatasetId());
			
			tableForDatasetDetails.addItem(new Object[] {strDatasetName, strDatasetDesc, strDatasetType, strCount}, new Integer(i));
			
		}
		return tableForDatasetDetails;
	}

	private Component buildDatasetFormatComponent() {
		try{
			hibernateUtil = new HibernateUtil(GDMSModel.getGDMSModel().getWorkbenchParams());
			HibernateSessionProvider sessionProvider = new HibernateSessionPerThreadProvider(hibernateUtil.getSessionFactory());
			workbenchDataManager = new WorkbenchDataManagerImpl(sessionProvider);
		}catch (FileNotFoundException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			//return;
		}catch (IOException ei) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			//return;
		}catch (URISyntaxException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			//return;
		}
		//20131212: Tulasi --- Implemented code to display Allelic and ABH options, 
				//if the dataset selected on the first tab is of Mapping-Allelic type
				strMappingType = "";
				if (null != strDatasetType && strDatasetType.equalsIgnoreCase("Mapping")) {
					
					try {
						
						MappingPopDAO mappingPopDAOLocal = new MappingPopDAO();
						mappingPopDAOLocal.setSession(localSession);
						MappingPop mappingPopByIdLocal = mappingPopDAOLocal.getById(Integer.parseInt(strDatasetID));
						if (null != mappingPopByIdLocal) {
							strMappingType = mappingPopByIdLocal.getMappingType();
						}
						
						if (strMappingType.equals("")) {
							MappingPopDAO mappingPopDAOCentral = new MappingPopDAO();
							mappingPopDAOCentral.setSession(centralSession);
							MappingPop mappingPopByIdCentral = mappingPopDAOCentral.getById(Integer.parseInt(strDatasetID));
							if (null != mappingPopByIdCentral) {
								strMappingType = mappingPopByIdCentral.getMappingType();
							}
						}
						
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MiddlewareQueryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				final OptionGroup optionGroupForMappingType = new OptionGroup();
				optionGroupForMappingType.setMultiSelect(false);
				optionGroupForMappingType.addStyleName("horizontal");
				optionGroupForMappingType.addItem("Allelic");
				optionGroupForMappingType.addItem("ABH");
				optionGroupForMappingType.setImmediate(true);
				
				Label lblMappingType = new Label("Mapping Type: ");
				lblMappingType.setStyleName(Reindeer.LABEL_SMALL);
				
				HorizontalLayout horizLayoutForMappingType = new HorizontalLayout();
				horizLayoutForMappingType.setSpacing(true);
				horizLayoutForMappingType.addComponent(lblMappingType);
				horizLayoutForMappingType.addComponent(optionGroupForMappingType);
				//20131212: Tulasi --- Implemented code to display Allelic and ABH options, if the dataset selected on the first tab is of Mapping-Allelic type
				
		/**
		 * 
		 * Title label on the top
		 * 
		 */
		Label lblTitle = new Label("Choose Data Export Format");
		lblTitle.setStyleName(Reindeer.LABEL_H2);

		/**
		 * 
		 * Building the left side components and layout
		 * 
		 */
		VerticalLayout layoutForGenotypingMatrixFormat = new VerticalLayout();
		layoutForGenotypingMatrixFormat.setSpacing(true);
		layoutForGenotypingMatrixFormat.setMargin(true, true, true, true);
		
		Label lblColumn = new Label("Identify a Column");
		lblColumn.setStyleName(Reindeer.LABEL_SMALL);
		
		final OptionGroup optionGroupForColumn = new OptionGroup();
		optionGroupForColumn.setMultiSelect(false);
		optionGroupForColumn.addStyleName("horizontal");
		optionGroupForColumn.addItem("GIDs");
		optionGroupForColumn.addItem("Germplasm Names");
		optionGroupForColumn.setImmediate(true);
		optionGroupForColumn.setEnabled(false);
		
		HorizontalLayout horizLayoutForColumns = new HorizontalLayout();
		horizLayoutForColumns.setSpacing(true);
		horizLayoutForColumns.addComponent(lblColumn);
		horizLayoutForColumns.addComponent(optionGroupForColumn);
		
		chbMatrix = new CheckBox();
		chbMatrix.setCaption("Genotyping X Marker Matrix");
		chbMatrix.setHeight("25px");
		chbMatrix.setImmediate(true);
		layoutForGenotypingMatrixFormat.addComponent(chbMatrix);
		chbMatrix.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) chbMatrix.getValue()){
					chbFlapjack.setValue(false);
					optionGroupForColumn.setEnabled(false);
					selectMap.setEnabled(false);
				} else {
					chbMatrix.setValue(false);
				}
			}
		});
		


		ThemeResource themeResourceMatrix = new ThemeResource("images/Matrix.jpg");
		Embedded matrixImage = new Embedded(null, themeResourceMatrix);
		matrixImage.setWidth("240px");
		matrixImage.setHeight("180px");
		CssLayout cssLayoutMatrix = new CssLayout();
		cssLayoutMatrix.addComponent(matrixImage);
		layoutForGenotypingMatrixFormat.addComponent(cssLayoutMatrix);


		selectMap = new ComboBox();
		selectMap.setWidth("200px");
		Object itemId = selectMap.addItem();
		selectMap.setItemCaption(itemId, "Select Map");
		selectMap.setValue(itemId);
		selectMap.setNullSelectionAllowed(false);
		selectMap.setImmediate(true);
		selectMap.setEnabled(false);
		selectMap.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				strSelectedMap = selectMap.getValue().toString();
				iSelectedMapId = hmOfMapNameAndMapId.get(strSelectedMap);
			}
		});
		
		
		MapDAO mapDAOLocal = new MapDAO();
		mapDAOLocal.setSession(localSession);
		MapDAO mapDAOCentral = new MapDAO();
		mapDAOCentral.setSession(centralSession);
		listOfAllMaps = new ArrayList<Map>();
		hmOfMapNameAndMapId = new HashMap<String, Integer>();
		try {
			
			List<Map> listOfAllMapsLocal = mapDAOLocal.getAll();
			List<Map> listOfAllMapsCentral = mapDAOCentral.getAll();
			
			for (Map map: listOfAllMapsLocal){
				if (false == listOfAllMaps.contains(map)){
					listOfAllMaps.add(map);
					hmOfMapNameAndMapId.put(map.getMapName(), map.getMapId());
				}
			}
			for (Map map: listOfAllMapsCentral){
				if (false == listOfAllMaps.contains(map)){
					listOfAllMaps.add(map);
					hmOfMapNameAndMapId.put(map.getMapName(), map.getMapId());
				}
			}
			
			if (null != listOfAllMaps){
				for (int i = 0; i < listOfAllMaps.size(); i++){
					Map map = listOfAllMaps.get(i);
					selectMap.addItem(map.getMapName());
				}
			}
			
		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			return null;
		}
		chbFlapjack = new CheckBox();
		chbFlapjack.setCaption("Flapjack");
		chbFlapjack.setHeight("25px");
		chbFlapjack.setImmediate(true);
		chbFlapjack.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) chbFlapjack.getValue()){
					chbMatrix.setValue(false);
					chbFlapjack.setValue(true);
					optionGroupForColumn.setEnabled(true);
					selectMap.setEnabled(true);
				} else {
					chbFlapjack.setValue(false);
					optionGroupForColumn.setEnabled(false);
					selectMap.setEnabled(false);
				}
			}
		});
		
		final OptionGroup optionGroupForFormat = new OptionGroup();
		optionGroupForFormat.setMultiSelect(false);
		optionGroupForFormat.setStyleName("horizontal");
		optionGroupForFormat.addItem(chbMatrix);
		optionGroupForFormat.addItem(chbFlapjack);
		optionGroupForFormat.setEnabled(false);
		optionGroupForFormat.setImmediate(true);
		chbFlapjack.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				if (chbFlapjack.getValue().toString().equals("true")){
					selectMap.setEnabled(true);
					optionGroupForColumn.setEnabled(true);
				} else {
					selectMap.setEnabled(false);
					optionGroupForColumn.setEnabled(false);
				}
			}
		});

		
		HorizontalLayout topHorizLayoutForFlapjack = new HorizontalLayout();
		topHorizLayoutForFlapjack.setSizeFull();
		topHorizLayoutForFlapjack.setSpacing(true);
		topHorizLayoutForFlapjack.addComponent(chbFlapjack);
		topHorizLayoutForFlapjack.addComponent(selectMap);

		ThemeResource themeResourceFlapjack = new ThemeResource("images/flapjack.png");
		Embedded flapjackImage = new Embedded(null, themeResourceFlapjack);
		flapjackImage.setWidth("200px");
		flapjackImage.setHeight("200px");
		CssLayout cssLayoutFlapjack = new CssLayout();
		cssLayoutFlapjack.addComponent(flapjackImage);

		VerticalLayout layoutForFlapjackFormat = new VerticalLayout();
		layoutForFlapjackFormat.setSpacing(true);
		layoutForFlapjackFormat.setSizeFull();
		layoutForFlapjackFormat.setMargin(true, true, true, true);
		layoutForFlapjackFormat.addComponent(topHorizLayoutForFlapjack);
		layoutForFlapjackFormat.addComponent(horizLayoutForColumns);
		layoutForFlapjackFormat.addComponent(cssLayoutFlapjack);

		/**
		 * 
		 * Building the Next button panel at the bottom of the layout
		 * 
		 */
		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			private boolean bGenerateFlapjack;
			private boolean dataToBeExportedBuiltSuccessfully;
			
			public void buttonClick(ClickEvent event) {


				dataToBeExportedBuiltSuccessfully = false;
				
				//20131212: Tulasi --- Implemented code to make Mapping Type as a mandatory selection
				//if the Dataset row selected on the the first tab is of Mapping-Allelic type
				if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click Next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (strDatasetType.equalsIgnoreCase("mapping") && strMappingType.equalsIgnoreCase("allelic")) {
					Object mappingTypeValue = optionGroupForMappingType.getValue();
					if (null != mappingTypeValue){
						strSelectedMappingType = mappingTypeValue.toString();
					} else {
						//strSelectedMappingType = "";
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required mapping type for the selected Mapping-Allelic Dataset.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}
				//20131212: Tulasi --- Implemented code to make Mapping Type as a mandatory selection
				//if the Dataset row selected on the the first tab is of Mapping-Allelic type
				if (chbMatrix.getValue().toString().equals("true")){
					strSelectedFormat = "Matrix";
					//System.out.println("Format Selected: " + strSelectedFormat);

					/*if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click next.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}*/

					try {
						retrieveDatasetDetailsForMatrixFormat();
						dataToBeExportedBuiltSuccessfully = true;
					} catch (GDMSException e1) {
						dataToBeExportedBuiltSuccessfully = false;
						_mainHomePage.getMainWindow().getWindow().showNotification(e1.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					//System.out.println("intAlleleValues="+intAlleleValues);
					if (dataToBeExportedBuiltSuccessfully) {
						ExportFileFormats exportFileFormats = new ExportFileFormats();
						try {
							if ((strDatasetType.equalsIgnoreCase("SSR"))||(strDatasetType.equalsIgnoreCase("DArT"))){
								if (markersCount > 252){
									listOfmatrixTextFileDataSSRDataset = exportFileFormats.MatrixTextFileDataSSRDataset(_mainHomePage, listOfAllelicValueWithMarkerIdElements, listOfNIDs, listOfAllMarkers, hmOfNIDAndNVal, hmOfMIdAndMarkerName);
								} else {
									matrixFileForDatasetRetrieval = exportFileFormats.MatrixForSSRDataset(_mainHomePage, listOfAllelicValueWithMarkerIdElements, listOfNIDs, listOfAllMarkers, hmOfNIDAndNVal, hmOfMIdAndMarkerName);
								}
							}
							/*if (strDatasetType.equalsIgnoreCase("DArT")){
								matrixFileForDatasetRetrieval = exportFileFormats.MatrixForDArtDataset(_mainHomePage, listOfAllelicValueElements, listOfNIDs, listOfAllMarkers, hmOfNIDAndNVal);
							}*/
							if (strDatasetType.equalsIgnoreCase("SNP")){
								matrixFileForDatasetRetrieval = exportFileFormats.MatrixForSNPDataset(_mainHomePage, listOfAllelicValueWithMarkerIdElements, listOfNIDs, listofMarkerNamesForSNP, hmOfNIDAndNVal, hmOfMIdAndMarkerName);
							}
							if (strDatasetType.equalsIgnoreCase("mapping")){
								matrixFileForDatasetRetrieval = exportFileFormats.MatrixForMappingDataset(_mainHomePage, intAlleleValues, parentsListToWrite, listOfNIDs, listofMarkerNamesForSNP, sortedMapOfGIDsAndGNames, hmOfMIdAndMarkerName);
							}

						} catch (GDMSException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error generating Matrix file", Notification.TYPE_ERROR_MESSAGE);
							return;
						}

						//System.out.println("Received the generated Matrix file.");
					}

				}  else if (chbFlapjack.getValue().toString().equals("true")){

					/*if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click Next.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}*/

					if ("true".equals(chbFlapjack.getValue().toString())){
						strSelectedFormat = "Flapjack";
					}

					Object mapValue = selectMap.getValue();
					if (mapValue instanceof Integer){
						Integer itemId = (Integer)mapValue;
						if (itemId.equals(1)){
							strSelectedMap = "";
						} 
					} else {
						String strMapSelected = mapValue.toString();
						boolean bIsValidMap = false;
						if (null != listOfAllMaps){
							for (int i = 0; i < listOfAllMaps.size(); i++){
								Map map = listOfAllMaps.get(i);
								if (map.getMapName().equals(strMapSelected)){
									bIsValidMap = true;
									break;
								}
							}
						}	
						if (bIsValidMap){
							strSelectedMap = strMapSelected;
						}
					}

					Object value = optionGroupForColumn.getValue();
					if (null != value){
						strSelectedColumn = value.toString();
					} else {
						strSelectedColumn = "";
					}
					//System.out.println("Selected Map: " + strSelectedMap + " --- " + "Selected Column: " + strSelectedColumn);


					if (strSelectedMap.equals("")){
						/*_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;*/
						//20131211: Tulasi --- Implemented code to ask the user to generate Flapjack with or without Map
						bGenerateFlapjack = false;
						OptionWindowForFlapjackMap optionWindowForFlapjackMap = new OptionWindowForFlapjackMap();
						final Window messageWindow = new Window("Require Map");
						if (null != optionWindowForFlapjackMap) {
							messageWindow.setContent(optionWindowForFlapjackMap);
							messageWindow.setWidth("500px");
							messageWindow.setClosable(true);
							messageWindow.center();
							
							if (false == _mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
								_mainHomePage.getMainWindow().addWindow(messageWindow);
							}
							messageWindow.setModal(true);
							messageWindow.setVisible(true);
						}
						
						optionWindowForFlapjackMap.addMapOptionListener(new MapOptionsListener() {

							@Override
							public void isMapRequiredOption(boolean bMapRequired) {
								if (bMapRequired) {
									_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
									bGenerateFlapjack = false;
									
									if (_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
										_mainHomePage.getMainWindow().removeWindow(messageWindow);
									}
									
								} else {
									bGenerateFlapjack = true;
									
									if (_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
										_mainHomePage.getMainWindow().removeWindow(messageWindow);
									}
									if (strSelectedColumn.equals("")){
										_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
										return;
									} 
									RetrieveDataForFlapjack retrieveDataForFlapjack = new RetrieveDataForFlapjack(_mainHomePage);
									retrieveDataForFlapjack.setGenotypingType("Dataset");
									retrieveDataForFlapjack.setDatasetName(strDatasetName);
									retrieveDataForFlapjack.setDatasetID(strDatasetID);
									retrieveDataForFlapjack.setDatasetType(strDatasetType);
									retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
									retrieveDataForFlapjack.setExportType(strSelectedColumn);
									retrieveDataForFlapjack.retrieveFlapjackData();
									dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();
									
									if (dataToBeExportedBuiltSuccessfully){
										generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
										generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
										generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
										
										Component newDatasetResultsPanel = buildDatasetResultComponent();
										_tabsheetForDataset.replaceComponent(_buildDatasetResultComponent, newDatasetResultsPanel);
										_tabsheetForDataset.requestRepaint();
										_buildDatasetResultComponent = newDatasetResultsPanel;
										_tabsheetForDataset.getTab(2).setEnabled(true);
										_tabsheetForDataset.setSelectedTab(2);
									}
									
								}
							}
							
						});
					} else if (strSelectedColumn.equals("")){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;
					} else {
						if (false == bGenerateFlapjack) {
							bGenerateFlapjack = true;
						}
					}


					/*if (strSelectedColumn.equals("")){
						//Please mention whether to write gids or germplasm name to the data file for flapjack						
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}*/


					//System.out.println("retrieveDatasetDetailsForMatrixFormat():");
					//System.out.println("DatasetName: " + strDatasetName + " --- " + "Dataset-ID: " + strDatasetID + " --- " +
							//"Dataset-Type:" + strDatasetType);
					if (bGenerateFlapjack) {
						RetrieveDataForFlapjack retrieveDataForFlapjack = new RetrieveDataForFlapjack(_mainHomePage);
						retrieveDataForFlapjack.setGenotypingType("Dataset");
						retrieveDataForFlapjack.setDatasetName(strDatasetName);
						retrieveDataForFlapjack.setDatasetID(strDatasetID);
						retrieveDataForFlapjack.setDatasetType(strDatasetType);
						retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
						retrieveDataForFlapjack.setExportType(strSelectedColumn);
						retrieveDataForFlapjack.retrieveFlapjackData();
						dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();
						//System.out.println(" ********************    dataToBeExportedBuiltSuccessfully=:"+dataToBeExportedBuiltSuccessfully);
						if (dataToBeExportedBuiltSuccessfully){
							generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
							generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
							generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
						}
					}
				}
				///  ************************************************************************************************************************
				
				if (dataToBeExportedBuiltSuccessfully){
					Component newDatasetResultsPanel = buildDatasetResultComponent();
					_tabsheetForDataset.replaceComponent(_buildDatasetResultComponent, newDatasetResultsPanel);
					_tabsheetForDataset.requestRepaint();
					_buildDatasetResultComponent = newDatasetResultsPanel;
					_tabsheetForDataset.getTab(2).setEnabled(true);
					_tabsheetForDataset.setSelectedTab(2);
				}

				if (null == strSelectedFormat || strSelectedFormat.equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required Export format type.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}


				if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});

		HorizontalLayout horizontalLayoutForTwoFormats = new HorizontalLayout();
		horizontalLayoutForTwoFormats.setSpacing(true);
		horizontalLayoutForTwoFormats.setSizeFull();
		horizontalLayoutForTwoFormats.addComponent(layoutForGenotypingMatrixFormat);
		horizontalLayoutForTwoFormats.addComponent(layoutForFlapjackFormat);

		
		/**
		 * 
		 * Building the final vertical layout for the Format tab
		 * 
		 */
		VerticalLayout completeFormatLayout = new VerticalLayout();
		completeFormatLayout.setCaption("Format");
		completeFormatLayout.setSpacing(true);
		completeFormatLayout.setSizeFull();
		completeFormatLayout.setMargin(true, true, true, true);
		

		//20131212: Tulasi --- Implemented code to make Mapping Type as a mandatory selection
		//if the Dataset row selected on the the first tab is of Mapping-Allelic type
		if (null != strDatasetType) {
			if (strDatasetType.equalsIgnoreCase("Mapping") && strMappingType.equalsIgnoreCase("allelic")) {
				completeFormatLayout.addComponent(horizLayoutForMappingType);
				completeFormatLayout.setComponentAlignment(horizLayoutForMappingType, Alignment.TOP_LEFT);
			}
		}
		//20131212: Tulasi --- Implemented code to make Mapping Type as a mandatory selection
		//if the Dataset row selected on the the first tab is of Mapping-Allelic type

		completeFormatLayout.addComponent(lblTitle);
		completeFormatLayout.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		completeFormatLayout.addComponent(horizontalLayoutForTwoFormats);
		completeFormatLayout.addComponent(layoutForButton);
		completeFormatLayout.setComponentAlignment(layoutForButton, Alignment.BOTTOM_CENTER);
		
		if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
			btnNext.setEnabled(false);
			chbMatrix.setEnabled(false);
			chbFlapjack.setEnabled(false);
			optionGroupForFormat.setEnabled(false);
		} else {
			btnNext.setEnabled(true);
			chbMatrix.setEnabled(true);
			chbFlapjack.setEnabled(true);
			optionGroupForFormat.setEnabled(true);
		}


		return completeFormatLayout;
	}

	protected void retrieveDatasetDetailsForMatrixFormat() throws GDMSException {
		
		/*System.out.println("retrieveDatasetDetailsForMatrixFormat():");
		System.out.println("DatasetName: " + strDatasetName + " --- " + "Dataset-ID: " + strDatasetID + " --- " +
		                   "Dataset-Type:" + strDatasetType);*/
		
		ArrayList<Integer> listOfDatasetID = new ArrayList<Integer>();
		listOfDatasetID.add(Integer.parseInt(strDatasetID));
		listofMarkerNamesForSNP = new ArrayList<String>();
		
		if (strDatasetType.equalsIgnoreCase("mapping")){
			
			MappingPopDAO mappingPopDAOLocal = new MappingPopDAO();
			mappingPopDAOLocal.setSession(localSession);
			MappingPopDAO mappingPopDAOCentral = new MappingPopDAO();
			mappingPopDAOCentral.setSession(centralSession);
			listOfAllParents = new ArrayList<ParentElement>();
			try {
				List<ParentElement> results =genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID));
				for (ParentElement parentElement : results){
					parentANid=parentElement.getParentANId();
					parentBNid=parentElement.getParentBGId();
					mappingType=parentElement.getMappingType();
				}
				Name namesA = null;
				Name namesB = null;						
				parentsGIDsNames= new HashMap<Integer, String>();
				
				namesA=germManager.getGermplasmNameByID(parentANid);
				parentAGid=namesA.getGermplasmId();
				parentsGIDsNames.put(namesA.getGermplasmId(), namesA.getNval());
				parentsListToWrite=parentsListToWrite+parentAGid+";;"+namesA.getNval()+"!~!";
				
				
				namesB=germManager.getGermplasmNameByID(parentBNid);
				parentBGid=namesB.getGermplasmId();
				parentsGIDsNames.put(namesB.getGermplasmId(), namesB.getNval());
				parentsListToWrite=parentsListToWrite+parentBGid+";;"+namesB.getNval()+"!~!";
				 
				List<Integer> markers= genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
				for(int m=0; m<markers.size(); m++){
					intAlleleValues.add(parentAGid+"!~!"+markers.get(m)+"!~!"+"A");
				}
				for(int m=0; m<markers.size(); m++){
					intAlleleValues.add(parentBGid+"!~!"+markers.get(m)+"!~!"+"B");
				}
				List<ParentElement> listOfParentsByDatasetIdLocal = mappingPopDAOLocal.getParentsByDatasetId(Integer.parseInt(strDatasetID));
				List<ParentElement> listOfParentsByDatasetIdCentral = mappingPopDAOCentral.getParentsByDatasetId(Integer.parseInt(strDatasetID));
				if (null != listOfParentsByDatasetIdLocal){
					for (ParentElement parentElement : listOfParentsByDatasetIdLocal){
						if (false == listOfAllParents.contains(parentElement)){
							listOfAllParents.add(parentElement);
						}
					}
				}
				if (null != listOfParentsByDatasetIdCentral){
					for (ParentElement parentElement : listOfParentsByDatasetIdCentral){
						if (false == listOfAllParents.contains(parentElement)){
							listOfAllParents.add(parentElement);
						}
					}
				}
			} catch (NumberFormatException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving ParentElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving ParentElements for the selected Dataset";
				throw new GDMSException(strErrMessage);
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving ParentElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving ParentElements for the selected Dataset";
				throw new GDMSException(strErrMessage);
			}
			
			listOfParentGIDs = new ArrayList<Integer>();
			
			for (ParentElement parentElement : listOfAllParents){
				Integer parentAGId = parentElement.getParentANId();
				Integer parentBGId = parentElement.getParentBGId();
				
				if (false == listOfParentGIDs.contains(parentAGId)){
					listOfParentGIDs.add(parentAGId);
				}
				if (false == listOfParentGIDs.contains(parentBGId)){
					listOfParentGIDs.add(parentBGId);
				}
			}
			
			
		}
		
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		/**
		 * commented by Kalyani on 22 nov 2013 
		 */
	
		/*MarkerMetadataSetDAO markerMetadataSetDAOLocal = new MarkerMetadataSetDAO();
		markerMetadataSetDAOLocal.setSession(localSession);
		MarkerMetadataSetDAO markerMetadataSetDAOCentral = new MarkerMetadataSetDAO();
		markerMetadataSetDAOCentral.setSession(localSession);*/
		
		List<Integer> listOfAllMIDsForSelectedDatasetID = new ArrayList<Integer>();
		hmOfMIdAndMarkerName = new HashMap<Integer, String>();
		try {
			/**
			 * commented by Kalyani on 22 nov 2013 
			 */
			
			/*List<Integer> listOfMarkerIDsLocal = markerMetadataSetDAOLocal.getMarkerIdByDatasetId(Integer.parseInt(strDatasetID));
			List<Integer> listOfMarkerIDsCentral = markerMetadataSetDAOCentral.getMarkerIdByDatasetId(Integer.parseInt(strDatasetID));
			if (null != listOfMarkerIDsLocal){
				for (Integer iMID : listOfMarkerIDsLocal){
					if (false == listOfAllMIDsForSelectedDatasetID.contains(iMID)){
						listOfAllMIDsForSelectedDatasetID.add(iMID);
					}
				}
			}
			if (null != listOfMarkerIDsCentral) {
				for (Integer iMID : listOfMarkerIDsCentral){
					if (false == listOfAllMIDsForSelectedDatasetID.contains(iMID)){
						listOfAllMIDsForSelectedDatasetID.add(iMID);
					}
				}
			}*/
			ArrayList LocalList=new ArrayList();
			ArrayList centralList=new ArrayList();
			listOfAllMarkers = new ArrayList<MarkerIdMarkerNameElement>();
			listOfAllMIDsForSelectedDatasetID=genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
			//genoManager.getMarkerNamesByMarkerIds(listOfAllMIDsForSelectedDatasetID);
			List<MarkerIdMarkerNameElement> markerNames = genoManager.getMarkerNamesByMarkerIds(listOfAllMIDsForSelectedDatasetID);
	        //System.out.println("testGetMarkerNamesByMarkerIds(" + listOfAllMIDsForSelectedDatasetID + ") RESULTS: ");
			markersCount=markerNames.size();
	        for (MarkerIdMarkerNameElement e : markerNames) {
	            //System.out.println(e.getMarkerId() + " : " + e.getMarkerName()+"    "+markerNames.size());
	            listOfAllMarkers.add(e);
	            listofMarkerNamesForSNP.add(e.getMarkerName().toString());
	            hmOfMIdAndMarkerName.put(e.getMarkerId(), e.getMarkerName().toString());
	        }
			
		} catch (NumberFormatException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker IDs for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
			String strErrMessage = "Error Retrieving Marker IDs for selected Dataset";
			throw new GDMSException(strErrMessage);
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker IDs for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
			String strErrMessage = "Error Retrieving Marker IDs for selected Dataset";
			throw new GDMSException(strErrMessage);
		}
		
		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(localSession);
		List<Integer> listOfGIDs = new ArrayList<Integer>();
		listOfNIDs = new ArrayList<Integer>();
		String mappingType="";
		hmOfNIDAndNVal = new HashMap<Integer, String>();
			if (strDatasetType.equalsIgnoreCase("mapping")){
				List<Integer> parentNIDs = new ArrayList<Integer>();
				try {
					//System.out.println("               <<<<<<<<<<<<<   :"+genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID)));
					 List<ParentElement> results = genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID));
					 //System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  :"+results.get(0).getParentANId());
					// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  :"+results.get(0).getParentBGId());
					// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  :"+results.get(0).getMappingType());
					 parentNIDs.add(results.get(0).getParentANId());
					 parentNIDs.add(results.get(0).getParentBGId());
					 mappingType=results.get(0).getMappingType();
					 Name names = null;
						
						for(int n=0;n<parentNIDs.size();n++){
							
							names=germManager.getGermplasmNameByID(Integer.parseInt(parentNIDs.get(n).toString()));
							//System.out.println(names.getNval()+","+names.getGermplasmId());
							//if(!germNames.contains(names.getNval()+","+names.getGermplasmId()))
								listOfNIDs.add(names.getGermplasmId());
								hmOfNIDAndNVal.put(names.getGermplasmId(),names.getNval());
								
						}
				     //   System.out.println("testGetParentsByDatasetId(" + datasetId + ") RESULTS: " + results);
				}catch (MiddlewareQueryException e) {
					//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving NIDs for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
					String strErrMessage = "Error Retrieving NIDs for selected Dataset";
					throw new GDMSException(strErrMessage);
				}
			}
				
			//System.out.println("hmOfNIDAndNVal=:"+hmOfNIDAndNVal);
			
		try {
			/**
			 * commented by Kalyani on 22 nov 2013 
			 */
			/*long countAll = accMetadataSetDAOLocal.countAll();
			List<Integer> niDsByDatasetIdsLocal = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetID, listOfGIDs, 0, (int)countAll);
			long countAll2 = accMetadataSetDAOCentral.countAll();
			List<Integer> niDsByDatasetIdsCentral = accMetadataSetDAOCentral.getNIDsByDatasetIds(listOfDatasetID, listOfGIDs, 0, (int)countAll2);
			if (null != niDsByDatasetIdsLocal){
				for (Integer nid : niDsByDatasetIdsLocal){
					if (false == listOfNIDs.contains(nid)){
						listOfNIDs.add(nid);
					}
				}
			}
			if (null != niDsByDatasetIdsCentral){
				for (Integer nid : niDsByDatasetIdsCentral){
					if (false == listOfNIDs.contains(nid)){
						listOfNIDs.add(nid);
					}
				}
			}*/
			nidsList=genoManager.getNidsFromAccMetadatasetByDatasetIds(listOfDatasetID, 0, (int)(genoManager.countNidsFromAccMetadatasetByDatasetIds(listOfDatasetID)));
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving NIDs for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
			String strErrMessage = "Error Retrieving NIDs for selected Dataset";
			throw new GDMSException(strErrMessage);
		}
		
		try {			
			Name names = null;
			
			for(int n=0;n<nidsList.size();n++){
				
				names=germManager.getGermplasmNameByID(Integer.parseInt(nidsList.get(n).toString()));
				//if(!germNames.contains(names.getNval()+","+names.getGermplasmId()))
					listOfNIDs.add(names.getGermplasmId());
					hmOfNIDAndNVal.put(names.getGermplasmId(),names.getNval());
					
			}
			
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names by NIds for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
			String strErrMessage = "Error Retrieving Names by NIds for selected Dataset";
			throw new GDMSException(strErrMessage);
		}
		//System.out.println("**********************  listOfNIDs="+listOfNIDs);	
		sortedMapOfGIDsAndGNames = new TreeMap<Integer, String>();
		Set<Integer> gidKeySet = hmOfNIDAndNVal.keySet();
		Iterator<Integer> gidIterator = gidKeySet.iterator();
		while (gidIterator.hasNext()) {
			Integer gid = gidIterator.next();
			String gname = hmOfNIDAndNVal.get(gid);
			sortedMapOfGIDsAndGNames.put(gid, gname);
		}
		if ((strDatasetType.equalsIgnoreCase("SSR"))||(strDatasetType.equalsIgnoreCase("DArT"))){
			listOfAllelicValueWithMarkerIdElements = new ArrayList<AllelicValueWithMarkerIdElement>();
			try {			
				List<AllelicValueWithMarkerIdElement> allelicValues = genoManager.getAllelicValuesFromAlleleValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromAlleleValuesByDatasetId(Integer.parseInt(strDatasetID)));
				//System.out.println(allelicValues.size());
				for(AllelicValueWithMarkerIdElement results : allelicValues) {
					listOfAllelicValueWithMarkerIdElements.add(results);
					
		        }
				
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving AllelicValues for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving AllelicValues for selected Dataset";
				throw new GDMSException(strErrMessage);
			}		
		} else if (strDatasetType.equalsIgnoreCase("SNP")){			
			listOfAllelicValueWithMarkerIdElements = new ArrayList<AllelicValueWithMarkerIdElement>();
			try {
				
				List<AllelicValueWithMarkerIdElement> charValues = genoManager.getAllelicValuesFromCharValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromCharValuesByDatasetId(Integer.parseInt(strDatasetID)));
				
				for(AllelicValueWithMarkerIdElement results : charValues) {
					listOfAllelicValueWithMarkerIdElements.add(results);
		        }
				
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving AllelicValueWithMarkerIdElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving AllelicValueWithMarkerIdElements for the selected Dataset";
				throw new GDMSException(strErrMessage);
			}
		} else if (strDatasetType.equalsIgnoreCase("mapping")){
			
			listOfAllelicValueWithMarkerIdElements = new ArrayList<AllelicValueWithMarkerIdElement>();
			//System.out.println("&&&&&&&&&&&&&&&&&&&&&&&  MAPPING &&&&&&&&&&&&&&&&&&&&&&&&");
			try {				
				alleleValues = genoManager.getAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID)));
				for(AllelicValueWithMarkerIdElement results : alleleValues) {
					//listOfAllAllelicValuesForSSRandDArtDatasetType.add(results);
					intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
		        }
				//System.out.println(intAlleleValues);
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Mapping AllelicValueElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving Mapping AllelicValueElements for the selected Dataset";
				throw new GDMSException(strErrMessage);
			}
		}
		
	}

	private Component buildDatasetResultComponent() {
		VerticalLayout resultsLayout = new VerticalLayout();
		resultsLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);

		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println("buildDatasetResultComponent(): " + absoluteFile);
		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("WEB-INF")) {
				fileExport = file;
				break;
			}
		}
		final String strAbsolutePath = fileExport.getAbsolutePath();
		//System.out.println(">>>>>" + strAbsolutePath);
				
		
		Button btnAllFlapjackFiles = new Button("View All Generated Files");
		btnAllFlapjackFiles.setStyleName(Reindeer.BUTTON_LINK);
		btnAllFlapjackFiles.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				
				FileOutputStream fos;
				try {
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hh:mm");
					String strDate = sdf.format(new Date());
					
					File zipFile = new File(strAbsolutePath + "\\" + "Flapjack-Files-" + strDate + ".zip");
					fos = new FileOutputStream(zipFile);
		    		ZipOutputStream zos = new ZipOutputStream(fos);
		    		
		    		FileResource fileResource = new FileResource(generatedTextFile, _mainHomePage);
		    		ZipEntry ze1 = new ZipEntry(fileResource.getFilename());
		    		zos.putNextEntry(ze1);
		    		FileInputStream in = new FileInputStream(generatedTextFile);
		    		while (-1 != in.read()) {
		    			zos.write(in.read());
		    		}

		    		
		    		FileResource fileResource2 = new FileResource(generatedDatFile, _mainHomePage);
		    		ZipEntry ze2 = new ZipEntry(fileResource2.getFilename());
		    		zos.putNextEntry(ze2);
		    		in = new FileInputStream(generatedDatFile);
		    		while (-1 != in.read()) {
		    			zos.write(in.read());
		    		}
		    		
		    		FileResource fileResource3 = new FileResource(generatedMapFile, _mainHomePage);
		    		ZipEntry ze3 = new ZipEntry(fileResource3.getFilename());
		    		zos.putNextEntry(ze3);
		    		in = new FileInputStream(generatedMapFile);
		    		while (-1 != in.read()) {
		    			zos.write(in.read());
		    		}
		    		
		    		in.close();
		    		zos.closeEntry();
		    		zos.close();
		    		
		    		FileResource zipFileResource = new FileResource(zipFile, _mainHomePage);
					_mainHomePage.getMainWindow().getWindow().open(zipFileResource, "_blank", true);
					
				} catch (FileNotFoundException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		
		final String strFJVisualizeLink = strAbsolutePath + "\\" + "flapjackrun.bat";
		//System.out.println(strFJVisualizeLink);
		Button btnVisualizeFJ = new Button("Visualize in Flapjack");
		btnVisualizeFJ.setStyleName(Reindeer.BUTTON_LINK);
		btnVisualizeFJ.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				//System.out.println("Trying to execute the flapjackrun.bat file.");
				
				String[] cmd = {"cmd.exe", "/c", "start", "\""+"flapjack"+"\"", strFJVisualizeLink};
				Runtime rt = Runtime.getRuntime();	
				try {
					rt.exec(cmd);
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error occurred while trying to create Flapjack.flapjack project.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		
		
		Link gtpExcelFileLink = new Link("Download Genotypic Matrix file", new ExternalResource(""));
		if (null != listOfmatrixTextFileDataSSRDataset){
			if(null != listOfmatrixTextFileDataSSRDataset && 0 < listOfmatrixTextFileDataSSRDataset.size()) {
				if(1 <= listOfmatrixTextFileDataSSRDataset.size()) {
					gtpExcelFileLink = new Link("Download Genotypic Matrix file", new FileDownloadResource(
							listOfmatrixTextFileDataSSRDataset.get(0), _mainHomePage.getMainWindow().getWindow().getApplication()));
				}
			} 
		} else if (null != matrixFileForDatasetRetrieval){
			gtpExcelFileLink = new Link("Download Genotypic Matrix file", new FileDownloadResource(
					matrixFileForDatasetRetrieval, _mainHomePage.getMainWindow().getWindow().getApplication()));
		}
		gtpExcelFileLink.setTargetName("_blank");
		
		
		HorizontalLayout layoutForExportTypes = new HorizontalLayout();
		layoutForExportTypes.setSpacing(true);

		ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		excelButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				if(null != listOfmatrixTextFileDataSSRDataset && 0 < listOfmatrixTextFileDataSSRDataset.size()) {
					if(1 <= listOfmatrixTextFileDataSSRDataset.size()) {
						FileResource fileResource = new FileResource(listOfmatrixTextFileDataSSRDataset.get(0), _mainHomePage);
						_mainHomePage.getMainWindow().getWindow().open(fileResource, "_self");
					}
				}
			}
		});
		if (null == strSelectedFormat) {
			layoutForExportTypes.addComponent(excelButton);
		}


		//System.out.println("Selected Format: buildDatasetResultComponent(): " + strSelectedFormat);
		//20131216: Added link to download Similarity Matrix File
		final String strSMVisualizeLink = strAbsolutePath + "\\" + "flapjackMatrix.bat";
		Button similarityMatrixButton = new Button("Show Similarity Matrix");
		similarityMatrixButton.setStyleName(Reindeer.BUTTON_LINK);
		similarityMatrixButton.setDescription("Similarity Matrix File");
		similarityMatrixButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				File similarityMatrixFile = new File(""); //TODO: Have to provide the File location
				String[] cmd = {"cmd.exe", "/c", "start", "\""+"flapjack"+"\"", strSMVisualizeLink};
				Runtime rtSM = Runtime.getRuntime();
				try {
					rtSM.exec(cmd);
				
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error occurred while trying to create Similarity Matrix.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				/*FileResource fileResource = new FileResource(similarityMatrixFile, _mainHomePage);
				_mainHomePage.getMainWindow().getWindow().open(fileResource, "Similarity Matrix File", true);*/
			}
		});
		//20131216: Added link to download Similarity Matrix File
		if (null != strSelectedFormat){
			if (strSelectedFormat.equals("Flapjack")){				
				resultsLayout.addComponent(btnAllFlapjackFiles);
				resultsLayout.addComponent(btnVisualizeFJ);
				resultsLayout.addComponent(similarityMatrixButton);
			} else if (strSelectedFormat.equals("Matrix")) {
				if (null != matrixFileForDatasetRetrieval && true == matrixFileForDatasetRetrieval.toString().endsWith(".xls")){
					resultsLayout.addComponent(gtpExcelFileLink);
				} else {
					layoutForExportTypes.addComponent(excelButton, 0);
					resultsLayout.addComponent(layoutForExportTypes);
					resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
				}
			}
		} else {
			excelButton.setEnabled(false);
			//pdfButton.setEnabled(false);
			//printButton.setEnabled(false);
			resultsLayout.addComponent(layoutForExportTypes);
			resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		}
		return resultsLayout;
	}


	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}


	private ArrayList<AllelicValueWithMarkerIdElement> listOfAllelicValueWithMarkerIdElements;
	private HashMap<Integer, String> hmOfNIDAndNVal;
	private ArrayList<Integer> listOfNIDs;
	List<Integer> nidsList=null;
	private ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers;
	private HashMap<Integer, String> hmOfMIdAndMarkerName;
	private ArrayList<AllelicValueElement> listOfAllelicValueElements;
	private ArrayList<String> listofMarkerNamesForSNP;
	private ArrayList<ParentElement> listOfAllParents;

	private CheckBox chbMatrix;

	private CheckBox chbFlapjack;

	private ComboBox selectMap;
	
	private List<Dataset> listOfAllDatasets;
	
	private String strMappingType;
	
}
