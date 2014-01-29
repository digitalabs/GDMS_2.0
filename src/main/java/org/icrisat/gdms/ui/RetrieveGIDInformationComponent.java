package org.icrisat.gdms.ui;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.AlleleValuesDAO;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.MarkerNameElement;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.FileDownloadResource;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.OptionWindowForFlapjackMap;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
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
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;


public class RetrieveGIDInformationComponent implements Component.Listener {

	private static final long serialVersionUID = 1L;

	private TabSheet _tabsheetForGID;
	private CheckBox chbMatrix;
	private CheckBox chbFlapjack;
	private final String MATRIX_FORMAT = "Genotyping X Marker Matrix"; 
	private final String FLAPJACK_FORMAT = "Flapjack";
	private Component buildGIDResultComponent;
	private Component buildGIDMarkerComponent;
	private Component buildGIDFormatComponent;
	private GDMSMain _mainHomePage;
	private FileUploadComponent uploadComponent;
	private TextArea textArea; 
	protected String strSelectedFormat;
	protected ArrayList<Integer> listOfGIDsProvided;
	protected String strSelectedMap;
	protected String strSelectedColumn;
	private List<MarkerIdMarkerNameElement> listOfMarkerNameElementsByGIDs;
	protected HashMap<String, Integer> hashMapOfMapNamesAndIDs;
	private ArrayList<String> listOfMarkersSelected;
	private Session centralSession;
	private Session localSession;
	private ArrayList<Map> listOfAllMaps;
	private HashMap<String, Integer> hmOfMapNameAndMapId;
	protected Integer iSelectedMapId;
	private ArrayList<String> listOfAllGermplasmNamesForGIDsProvided;
	private List<AllelicValueElement> listOfAllelicValueElements;
	private HashMap<Integer, String> hmOfGIDAndGName;
	protected File matrixFileForGIDRetrieval;
	protected boolean dataToBeExportedBuiltSuccessfully;
	protected File generatedTextFile;
	protected File generatedMapFile;
	protected File generatedDatFile;
	private HashMap<String, Integer> hmOfAllMarkerNamesAndMarkerId;
	protected ArrayList<Integer> listOfMIDsSelected;
	protected HashMap<Integer, String> hmOfSelectedMIDandMNames;
	String realPath="";
	
	HashMap<Integer, HashMap<String, Object>> mapEx = new HashMap<Integer, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
	HashMap marker = new HashMap();
	ArrayList datasetIDs=new ArrayList();
	ArrayList nList= new ArrayList();
	protected File pdfFile;

	private Button gidsSampleFile;
	
	ManagerFactory factory=null;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	
	public RetrieveGIDInformationComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			manager = factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
		

	/**
	 * 
	 * Building the entire Tabbed Component required for Dataset
	 * 
	 */
	public HorizontalLayout buildTabbedComponentForGID() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		_tabsheetForGID = new TabSheet();
		//_tabsheetForGID.setSizeFull();
		_tabsheetForGID.setWidth("700px");

		Component buildGIDComponent = buildGIDGIDComponent();

		buildGIDMarkerComponent = buildGIDMarkerComponent();

		buildGIDFormatComponent = buildGIDFormatComponent();

		buildGIDResultComponent = buildGIDResultComponent();
		
		buildGIDComponent.setSizeFull();
		buildGIDMarkerComponent.setSizeFull();
		buildGIDFormatComponent.setSizeFull();
		buildGIDResultComponent.setSizeFull();

		_tabsheetForGID.addComponent(buildGIDComponent);
		_tabsheetForGID.addComponent(buildGIDMarkerComponent);
		_tabsheetForGID.addComponent(buildGIDFormatComponent);
		_tabsheetForGID.addComponent(buildGIDResultComponent);
		
		_tabsheetForGID.getTab(1).setEnabled(false);
		_tabsheetForGID.getTab(2).setEnabled(false);
		_tabsheetForGID.getTab(3).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForGID);

		return horizontalLayout;
	}


	private Component buildGIDGIDComponent() {
		VerticalLayout layoutForGIDTab = new VerticalLayout();
		layoutForGIDTab.setCaption("GID");
		layoutForGIDTab.setSpacing(true);
		layoutForGIDTab.setSizeFull();
		layoutForGIDTab.setMargin(true, true, true, true);

		VerticalLayout verticalLayoutOne = new VerticalLayout();
		Label label1 = new Label("Upload text file with desired GIDs");
		
			
		label1.setStyleName(Reindeer.LABEL_H2);
		gidsSampleFile = new Button("Sample File");
		gidsSampleFile.setImmediate(true);
		gidsSampleFile.setStyleName(Reindeer.BUTTON_LINK);
		gidsSampleFile.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
                
				WebApplicationContext ctx = (WebApplicationContext) _mainHomePage.getContext();
                String strTemplateFolderPath = ctx.getHttpSession().getServletContext().getRealPath("\\VAADIN\\themes\\gdmstheme\\Templates");
                System.out.println("Folder-Path: " + strTemplateFolderPath);
                
				//String strMarkerType = _strMarkerType.replace(" ", "");
				String strFileName = "GIDs.txt";
				
				File strFileLoc = new File(strTemplateFolderPath + "\\" + strFileName);
				FileResource fileResource = new FileResource(strFileLoc, _mainHomePage);
				//_mainHomePage.getMainWindow().getWindow().open(fileResource, "_blank", true);
				
				if (strFileName.endsWith(".txt")) {
					_mainHomePage.getMainWindow().getWindow().open(fileResource, "GIDs", true);
				} 				
			}
			
		});
		

		uploadComponent = new FileUploadComponent(_mainHomePage, "GID");
		//20131205: Tulasi --- Modified the code to display the GIDs read from the text file in the TextArea and then proceed to the next tab upon clicking Next
		//uploadComponent.init("basic");
		uploadComponent.setWidth("90%");
		uploadComponent.registerListener(new FileUploadListener() {
			@Override
			public void updateLocation(String absolutePath) {
				List<Integer> listOfGIDs = uploadComponent.getListOfGIDs();
				if (null != listOfGIDs) {
					String strListOfGIDsFromTextFile = "";
					for (Integer iGID : listOfGIDs) {
						strListOfGIDsFromTextFile += String.valueOf(iGID) + ",";
					}
					if (0 != strListOfGIDsFromTextFile.trim().length()) {
						strListOfGIDsFromTextFile = strListOfGIDsFromTextFile.substring(0, strListOfGIDsFromTextFile.length()-1);
						textArea.setValue(strListOfGIDsFromTextFile);
					}
				}
			}
		});
		//20131205: Tulasi --- Modified the code to display the GIDs read from the text file in the TextArea and then proceed to the next tab upon clicking Next

		
		verticalLayoutOne.addComponent(label1);
		verticalLayoutOne.addComponent(uploadComponent);

		VerticalLayout verticalLayoutTwo = new VerticalLayout();
		verticalLayoutTwo.setSpacing(true);
		verticalLayoutOne.addComponent(gidsSampleFile);
		verticalLayoutOne.setComponentAlignment(gidsSampleFile, Alignment.BOTTOM_CENTER);
		
		Label label2 = new Label("Enter GIDs separated by commas");
		label2.setStyleName(Reindeer.LABEL_H2);

		textArea = new TextArea();
		//textArea.setWidth("200px");
		textArea.setWidth("90%");
		layoutForGIDTab.addComponent(textArea);

		Button btnNext = new Button("Next");
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {

				if (false == textArea.getValue().toString().equals("")){
					try {
						listOfGIDsProvided = (ArrayList<Integer>) obtainListOfGIDsInTextArea();
					} catch (GDMSException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				} else {
					listOfGIDsProvided = (ArrayList<Integer>) uploadComponent.getListOfGIDs();
				}


				if (null == listOfGIDsProvided || 0 == listOfGIDsProvided.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please provide the GIDs required to be exported.", Notification.TYPE_ERROR_MESSAGE);
					return;
				} else {
					try {
						retrieveGNamesForGivenGIDs();
						if (null == listOfAllGermplasmNamesForGIDsProvided || 0 == listOfAllGermplasmNamesForGIDsProvided.size()){
							_mainHomePage.getMainWindow().getWindow().showNotification("Germplasm Names could not be retrieved for the given GID(s). Please provide valid GID(s).", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					} catch (GDMSException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					if (null != listOfAllGermplasmNamesForGIDsProvided && 0 < listOfAllGermplasmNamesForGIDsProvided.size()){
						retrieveMarkersForGIDsProvided();
						if (null == listOfMarkerNameElementsByGIDs || 0 == listOfMarkerNameElementsByGIDs.size()){
							_mainHomePage.getMainWindow().getWindow().showNotification("Markers could not be retrieved for the given GID(s). Please provide valid GID(s).", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}

					VerticalLayout newGIDMarkerComponent = (VerticalLayout) buildGIDMarkerComponent();
					_tabsheetForGID.replaceComponent(buildGIDMarkerComponent, newGIDMarkerComponent);
					buildGIDMarkerComponent = newGIDMarkerComponent;
					buildGIDMarkerComponent.requestRepaint();
					_tabsheetForGID.getTab(1).setEnabled(true);
					_tabsheetForGID.setSelectedTab(buildGIDMarkerComponent);
					_tabsheetForGID.requestRepaint();
				} 

			}
		});

		verticalLayoutTwo.addComponent(label2);
		verticalLayoutTwo.addComponent(textArea);
		verticalLayoutTwo.addComponent(btnNext);

		layoutForGIDTab.addComponent(verticalLayoutOne);
		layoutForGIDTab.addComponent(verticalLayoutTwo);

		return layoutForGIDTab;
	}



	protected void retrieveGNamesForGivenGIDs() throws GDMSException {
		/*NameDAO nameDAOLocal = new NameDAO();
		nameDAOLocal.setSession(localSession);
		NameDAO nameDAOCentral = new NameDAO();
		nameDAOCentral.setSession(centralSession);*/
		listOfAllGermplasmNamesForGIDsProvided = new ArrayList<String>();
		hmOfGIDAndGName = new HashMap<Integer, String>();
		datasetIDs= new ArrayList();
		nList= new ArrayList();
		try {
			//manager.getGermplasmNameByID(arg0)
			//List<Integer> nList = genoManager.getNameIdsByGermplasmIds(listOfGIDsProvided);
			Name names = null;
			List<AccMetadataSetPK> accMetadataSets = genoManager.getGdmsAccMetadatasetByGid(listOfGIDsProvided, 0, (int) genoManager.countGdmsAccMetadatasetByGid(listOfGIDsProvided));
	       // Debug.println(0, "testGetGdmsAccMetadatasetByGid() RESULTS: ");
	        for (AccMetadataSetPK accMetadataSet : accMetadataSets) {
	            //Debug.println(0, accMetadataSet.toString());
	        	nList.add(accMetadataSet.getNameId());
	        	datasetIDs.add(accMetadataSet.getDatasetId());
	        }
			for(int n=0;n<nList.size();n++){
				names=manager.getGermplasmNameByID(Integer.parseInt(nList.get(n).toString()));
				String nval = names.getNval();
				Integer germplasmId = names.getGermplasmId();
				if (false == listOfAllGermplasmNamesForGIDsProvided.contains(nval)){
					listOfAllGermplasmNamesForGIDsProvided.add(nval);
					hmOfGIDAndGName.put(germplasmId, nval);
				}
				
			}
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving Names for selected GIDs");
		}

	}

	private Component buildGIDMarkerComponent() {
		VerticalLayout layoutForGIDMarkerTab = new VerticalLayout();
		layoutForGIDMarkerTab.setCaption("Marker");
		layoutForGIDMarkerTab.setSpacing(true);
		layoutForGIDMarkerTab.setSizeFull();
		layoutForGIDMarkerTab.setMargin(true, true, true, true);

		Label lblTitle = new Label("Select the Markers from the list");
		lblTitle.setStyleName(Reindeer.LABEL_H2);

		if (null != listOfMarkerNameElementsByGIDs && 0 < listOfMarkerNameElementsByGIDs.size()){
			lblTitle = new Label("Select from the list of " + listOfMarkerNameElementsByGIDs.size() + " markers");
		}

		final TwinColSelect selectForMarkers = new TwinColSelect();
		selectForMarkers.setLeftColumnCaption("All Markers");
		selectForMarkers.setRightColumnCaption("Selected Markers");
		//selectForMarkers.setNullSelectionAllowed(false);
		//selectForMarkers.setInvalidAllowed(false);
		if (null != listOfMarkerNameElementsByGIDs && 0 < listOfMarkerNameElementsByGIDs.size()){
			for (MarkerIdMarkerNameElement markerNameElement : listOfMarkerNameElementsByGIDs) {
				selectForMarkers.addItem(markerNameElement.getMarkerName());
			}
		}
		selectForMarkers.setRows(20);
		selectForMarkers.setColumns(25);
		selectForMarkers.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				
				if (event.getProperty().getValue() != null) {
					TwinColSelect colSelect = (TwinColSelect)event.getProperty();
					Object value2 = colSelect.getValue();
					Set<String> hashSet = (Set<String>) value2;
					listOfMarkersSelected = new ArrayList<String>();
					for (String string : hashSet) {
						listOfMarkersSelected.add(string);
					}
				}
			}
		});
		selectForMarkers.setImmediate(true);		

		HorizontalLayout horizLytForSelectComponent = new HorizontalLayout();
		horizLytForSelectComponent.setSizeFull();
		horizLytForSelectComponent.setSpacing(true);
		horizLytForSelectComponent.setMargin(true);
		horizLytForSelectComponent.addComponent(selectForMarkers);


		final CheckBox chbSelectAll = new CheckBox("Select All");
		chbSelectAll.setImmediate(true);
		chbSelectAll.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				ArrayList<String> listOfAllMarkerNames = new ArrayList<String>();
				if (null != listOfMarkerNameElementsByGIDs && 0 < listOfMarkerNameElementsByGIDs.size()){
					for (MarkerIdMarkerNameElement markerNameElement : listOfMarkerNameElementsByGIDs) {
						listOfAllMarkerNames.add(markerNameElement.getMarkerName());
					}
				}
				selectForMarkers.setValue(listOfAllMarkerNames);
			}
		});


		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("300px");
		txtFieldSearch.setImmediate(true);
		txtFieldSearch.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void textChange(TextChangeEvent event) {
				String strMarkerNames1 = txtFieldSearch.getValue().toString();
				
				System.out.println(strMarkerNames1);
				
				if (null == listOfMarkersSelected) {
					listOfMarkersSelected = new ArrayList<String>();
				}
				
				if (strMarkerNames1.endsWith("*")) {
					int indexOf = strMarkerNames1.indexOf('*');
					String substring = strMarkerNames1.substring(0, indexOf);

					if (null != listOfMarkerNameElementsByGIDs && 0 < listOfMarkerNameElementsByGIDs.size()){
						for (MarkerIdMarkerNameElement markerNameElement : listOfMarkerNameElementsByGIDs) {
							selectForMarkers.addItem(markerNameElement.getMarkerName());
							//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
							if (markerNameElement.getMarkerName().toLowerCase().startsWith(substring) || 
							    markerNameElement.getMarkerName().toUpperCase().startsWith(substring)) {
								listOfMarkersSelected.add(markerNameElement.getMarkerName());
							}
						}
					}
				} else if (strMarkerNames1.trim().equals("*")) {
					ArrayList<String> listOfAllMarkerNames = new ArrayList<String>();
					if (null != listOfMarkerNameElementsByGIDs && 0 < listOfMarkerNameElementsByGIDs.size()){
						for (MarkerIdMarkerNameElement markerNameElement : listOfMarkerNameElementsByGIDs) {
							listOfAllMarkerNames.add(markerNameElement.getMarkerName());
						}
					}
					listOfMarkersSelected.addAll(listOfAllMarkerNames);
				}
				selectForMarkers.setValue(listOfMarkersSelected);
			}
		});

		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);
		searchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				
				if (null != txtFieldSearch.getValue()){
					String strMarkerNames = txtFieldSearch.getValue().toString();
					
					if (null == listOfMarkersSelected) {
						listOfMarkersSelected = new ArrayList<String>();
					}
					
					if (strMarkerNames.endsWith("*")) {
						int indexOf = strMarkerNames.indexOf('*');
						String substring = strMarkerNames.substring(0, indexOf);

						if (null != listOfMarkerNameElementsByGIDs && 0 < listOfMarkerNameElementsByGIDs.size()){
							for (MarkerIdMarkerNameElement markerNameElement : listOfMarkerNameElementsByGIDs) {
								selectForMarkers.addItem(markerNameElement.getMarkerName());
								//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
								if (markerNameElement.getMarkerName().toLowerCase().startsWith(substring) || 
									markerNameElement.getMarkerName().toUpperCase().startsWith(substring)) {
									listOfMarkersSelected.add(markerNameElement.getMarkerName());
								}
							}
						}
					} else if (strMarkerNames.trim().equals("*")) {
						ArrayList<String> listOfAllMarkerNames = new ArrayList<String>();
						if (null != listOfMarkerNameElementsByGIDs && 0 < listOfMarkerNameElementsByGIDs.size()){
							for (MarkerIdMarkerNameElement markerNameElement : listOfMarkerNameElementsByGIDs) {
								listOfAllMarkerNames.add(markerNameElement.getMarkerName());
							}
						}
						listOfMarkersSelected.addAll(listOfAllMarkerNames);
					}
					selectForMarkers.setValue(listOfMarkersSelected);
				}
			}
		});


		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(false, true, true, false);
		horizontalLayout.addComponent(chbSelectAll);
		horizontalLayout.setComponentAlignment(chbSelectAll, Alignment.TOP_LEFT);
		horizontalLayout.addComponent(txtFieldSearch);
		horizontalLayout.addComponent(searchButton);


		HorizontalLayout horizontalLayoutForButton = new HorizontalLayout();
		Button btnNext = new Button("Next");
		horizontalLayoutForButton.addComponent(btnNext);
		horizontalLayoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		horizontalLayoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				listOfMIDsSelected = new ArrayList<Integer>();
				hmOfSelectedMIDandMNames = new HashMap<Integer, String>();
				
				/*if (null == listOfMarkersSelected){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Markers.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}*/
				
				//Creating a new list of Markers from the right side container of the TwinColumnSelect component
				listOfMarkersSelected = new ArrayList<String>();
				Object value2 = selectForMarkers.getValue();
				Set<String> hashSet = (Set<String>) value2;
				for (String string : hashSet) {
					listOfMarkersSelected.add(string);
				}

				if (0 == listOfMarkersSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Markers.", Notification.TYPE_ERROR_MESSAGE);
					return;
				} else {
					
					for (String strMarker : listOfMarkersSelected){
						Integer iMarkerID = hmOfAllMarkerNamesAndMarkerId.get(strMarker);
						if (false == listOfMIDsSelected.contains(iMarkerID)){
							listOfMIDsSelected.add(iMarkerID);
						}
						if (false == hmOfSelectedMIDandMNames.containsKey(iMarkerID)){
							hmOfSelectedMIDandMNames.put(iMarkerID, strMarker);
						}
					}

					VerticalLayout newGIDFormatComponent = (VerticalLayout) buildGIDFormatComponent();
					_tabsheetForGID.replaceComponent(buildGIDFormatComponent, newGIDFormatComponent);
					buildGIDFormatComponent = newGIDFormatComponent;
					buildGIDFormatComponent.requestRepaint();
					_tabsheetForGID.getTab(2).setEnabled(true);
					_tabsheetForGID.setSelectedTab(buildGIDFormatComponent);
					_tabsheetForGID.requestRepaint();
				}
			}
		});
System.out.println("hmOfSelectedMIDandMNames:"+hmOfSelectedMIDandMNames);
		if(null == listOfMarkerNameElementsByGIDs || 0 == listOfMarkerNameElementsByGIDs.size()){
			chbSelectAll.setEnabled(false);
			searchButton.setEnabled(false);
			txtFieldSearch.setEnabled(false);
			btnNext.setEnabled(false);
			selectForMarkers.setEnabled(false);
		} else {
			chbSelectAll.setEnabled(true);
			searchButton.setEnabled(true);
			txtFieldSearch.setEnabled(true);
			btnNext.setEnabled(true);
			selectForMarkers.setEnabled(true);
		}

		layoutForGIDMarkerTab.addComponent(lblTitle);
		layoutForGIDMarkerTab.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		layoutForGIDMarkerTab.addComponent(horizontalLayout);
		layoutForGIDMarkerTab.addComponent(horizLytForSelectComponent);
		layoutForGIDMarkerTab.addComponent(horizontalLayoutForButton);
		layoutForGIDMarkerTab.setComponentAlignment(horizontalLayoutForButton, Alignment.MIDDLE_CENTER);

		return layoutForGIDMarkerTab;
	}

	private void retrieveMarkersForGIDsProvided() {	
		/*MarkerDAO markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		MarkerDAO markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);*/
		listOfMarkerNameElementsByGIDs = new ArrayList<MarkerIdMarkerNameElement>();
		ArrayList<String> listOfAllMarkerNames = new ArrayList<String>();
		try {
			ArrayList markerIDsList=new ArrayList();
			//genoManager.getMarkersByGidAndDatasetIds(listOfGIDsProvided, datasetIDs, arg2, arg3)
			for(int d=0; d<datasetIDs.size();d++){
				List<Integer> results = genoManager.getMarkerIdsByDatasetId(Integer.parseInt(datasetIDs.get(d).toString()));
				for(int r=0;r<results.size();r++){
					markerIDsList.add(Integer.parseInt(results.get(r).toString()));
				}
			}
			List<MarkerIdMarkerNameElement> listOfMarkerNames=genoManager.getMarkerNamesByMarkerIds(markerIDsList);
			hmOfAllMarkerNamesAndMarkerId = new HashMap<String, Integer>();
			/*List<MarkerNameElement> listOfMarkerNamesByGIdsLocal = markerDAOLocal.getMarkerNamesByGIds(listOfGIDsProvided);
			List<MarkerNameElement> listOfMarkerNamesByGIdsCentral = markerDAOCentral.getMarkerNamesByGIds(listOfGIDsProvided);*/
			for (MarkerIdMarkerNameElement markerNameElement : listOfMarkerNames){
				if (false == listOfMarkerNameElementsByGIDs.contains(markerNameElement)){
					listOfMarkerNameElementsByGIDs.add(markerNameElement);
					listOfAllMarkerNames.add(markerNameElement.getMarkerName());
				}
				if (false == hmOfAllMarkerNamesAndMarkerId.containsKey(markerNameElement.getMarkerName())){
					hmOfAllMarkerNamesAndMarkerId.put(markerNameElement.getMarkerName(), markerNameElement.getMarkerId());
				}
			}
			/*for (MarkerNameElement markerNameElement : listOfMarkerNamesByGIdsCentral){
				if (false == listOfMarkerNameElementsByGIDs.contains(markerNameElement)){
					listOfMarkerNameElementsByGIDs.add(markerNameElement);
					listOfAllMarkerNames.add(markerNameElement.getMarkerName());
				}
			}
*/
/*
			if (0 != listOfMarkerNameElementsByGIDs.size()){

				hmOfAllMarkerNamesAndMarkerId = new HashMap<String, Integer>();

				long countAllLocal = markerDAOLocal.countAll();
				List<Integer> listOfMarkerIdsByNamesLocal = markerDAOLocal.getIdsByNames(listOfAllMarkerNames, 0, (int)countAllLocal);

				long countAllCentral = markerDAOCentral.countAll();
				List<Integer> listOfMarkerIdsByNamesCentral = markerDAOCentral.getIdsByNames(listOfAllMarkerNames, 0, (int)countAllCentral);

				long countMarkersByIdsLocal = markerDAOLocal.countMarkersByIds(listOfMarkerIdsByNamesLocal);
				List<Marker> listOfMarkersByIdsLocal = markerDAOLocal.getMarkersByIds(listOfMarkerIdsByNamesLocal, 0, (int)countMarkersByIdsLocal); 

				long countMarkersByIdsCentral = markerDAOLocal.countMarkersByIds(listOfMarkerIdsByNamesCentral);
				List<Marker> listOfMarkersByIdsCentral = markerDAOCentral.getMarkersByIds(listOfMarkerIdsByNamesCentral, 0, (int)countMarkersByIdsCentral);

				for (Marker marker : listOfMarkersByIdsLocal){
					Integer markerId = marker.getMarkerId();
					String markerName = marker.getMarkerName();
					if (false == hmOfAllMarkerNamesAndMarkerId.containsKey(markerName)){
						hmOfAllMarkerNamesAndMarkerId.put(markerName, markerId);
					}
				}
				for (Marker marker : listOfMarkersByIdsCentral){
					Integer markerId = marker.getMarkerId();
					String markerName = marker.getMarkerName();
					if (false == hmOfAllMarkerNamesAndMarkerId.containsKey(markerName)){
						hmOfAllMarkerNamesAndMarkerId.put(markerName, markerId);
					}
				}
			}*/

		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			return;
		}


	}

	private Component buildGIDFormatComponent() {
		/**
		 * 
		 * Title label on the top
		 * 
		 */
		Label lblTitle = new Label("Choose Data Export Format");
		lblTitle.setStyleName(Reindeer.LABEL_H2);

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

		/**
		 * 
		 * Building the left side components and layout
		 * 
		 */
		VerticalLayout layoutForGenotypingMatrixFormat = new VerticalLayout();
		layoutForGenotypingMatrixFormat.setSpacing(true);
		layoutForGenotypingMatrixFormat.setMargin(true, true, true, true);

		chbMatrix = new CheckBox();
		chbMatrix.setCaption(MATRIX_FORMAT);
		chbMatrix.setHeight("25px");
		chbMatrix.setImmediate(true);
		chbMatrix.setEnabled(false);
		layoutForGenotypingMatrixFormat.addComponent(chbMatrix);
		chbMatrix.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) chbMatrix.getValue()){
					chbFlapjack.setValue(false);
					chbMatrix.setValue(true);

					ThemeResource themeResourceMatrix = new ThemeResource("images/Matrix.jpg");
					Embedded matrixImage = new Embedded(null, themeResourceMatrix);
					matrixImage.setWidth("240px");
					matrixImage.setHeight("180px");
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


		/**
		 * 
		 * Building the right side components and layout
		 * 
		 */
		chbFlapjack = new CheckBox();
		chbFlapjack.setCaption(FLAPJACK_FORMAT);
		chbFlapjack.setHeight("25px");
		chbFlapjack.setImmediate(true);
		chbFlapjack.setEnabled(false);
		final ComboBox selectMap = new ComboBox();
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
					//selectMap.removeAllItems();
				}
			}
		});
		/*chbFlapjack.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) chbFlapjack.getValue()){
					chbMatrix.setValue(false);
					chbFlapjack.setValue(true);

					ThemeResource themeResourceFlapjack = new ThemeResource("images/flapjack.png");
					Embedded flapjackImage = new Embedded(null, themeResourceFlapjack);
					flapjackImage.setWidth("200px");
					flapjackImage.setHeight("200px");
				}
			}
		});*/


		/*MapDAO mapDAOLocal = new MapDAO();
		mapDAOLocal.setSession(localSession);
		MapDAO mapDAOCentral = new MapDAO();
		mapDAOCentral.setSession(centralSession);*/
		listOfAllMaps = new ArrayList<Map>();
		hmOfMapNameAndMapId = new HashMap<String, Integer>();
		try {

			List<Map> listOfAllMapsLocal = genoManager.getAllMaps(0, (int)genoManager.countAllMaps(Database.LOCAL), Database.LOCAL);
			List<Map> listOfAllMapsCentral = genoManager.getAllMaps(0, (int)genoManager.countAllMaps(Database.CENTRAL), Database.CENTRAL);

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
			_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Maps",  Notification.TYPE_ERROR_MESSAGE);
			return null;
		}

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
			public void buttonClick(ClickEvent event) {

				if (null == listOfGIDsProvided || 0 == listOfGIDsProvided.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be displayed in the required format.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				if (null == listOfMarkersSelected || 0 == listOfMarkersSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Markers to be displayed in the required format.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				if (chbMatrix.getValue().toString().equals("true")){
					strSelectedFormat = "Matrix";
					//System.out.println("Format Selected: " + strSelectedFormat);

					try {
						retrieveGIDDataForMatrixFormat();
						//allele_values
						//char_values
						//mapping_pop_values
						//GET_ALLELE_COUNT_BY_GID

						//GET_CHAR_COUNT_BY_GID
						//long lGetCharCountByGID = getCharCountByGID(listOfGIDsProvided);
						//GET_MAPPING_COUNT_BY_GID
						//long lGetMappingCountByGID = getMappingCountByGID(listOfGIDsProvided);

						ExportFileFormats exportFileFormats = new ExportFileFormats();
						//matrixFileForGIDRetrieval = exportFileFormats.MatrixForGIDRetrieval(_mainHomePage, listOfGIDs, listOfMarkersSelected, listOfAllGermplasmNamesForSelectedGIDs, listOfAllelicValueElements, hmOfGIDAndGName);
						try {
							/*if(listOfMarkersSelected.size() > 252) {
								List<File> matrixTxt = exportFileFormats.MatrixTxt(_mainHomePage, (ArrayList<Integer>)listOfGIDsProvided, listOfMarkersSelected, hmOfGIDAndGName, (ArrayList<AllelicValueElement>) listOfAllelicValueElements);
								matrixFileForGIDRetrieval = matrixTxt.get(0);
								if(matrixTxt.size() == 2) {
									pdfFile = matrixTxt.get(1);
								}
							} else {*/
								matrixFileForGIDRetrieval = exportFileFormats.Matrix(_mainHomePage, (ArrayList<Integer>)listOfGIDsProvided, listOfMarkersSelected, hmOfGIDAndGName, mapEx);
							//}
							dataToBeExportedBuiltSuccessfully = true;
						} catch (GDMSException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error generating the Matrix File", Notification.TYPE_ERROR_MESSAGE);
							return;
						}

						//System.out.println("Received the generated Matrix file.");

					} catch (GDMSException e1) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e1.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
						return;
					}

				} else if (chbFlapjack.getValue().toString().equals("true")) {

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
									System.out.println("Trying to retrieve Data required for Flapjack format."+listOfMarkersSelected);
									System.out.println("Trying to retrieve Data required for Flapjack format."+listOfMIDsSelected);
									RetrieveDataForFlapjack retrieveDataForFlapjack = new RetrieveDataForFlapjack(_mainHomePage);
									retrieveDataForFlapjack.setGenotypingType("GIDs");
									retrieveDataForFlapjack.setListOfGermplasmsProvided(listOfAllGermplasmNamesForGIDsProvided);
									retrieveDataForFlapjack.setListOfMarkersSelected(listOfMarkersSelected);
									retrieveDataForFlapjack.setListOfGIDsSelected((ArrayList<Integer>) listOfGIDsProvided);
									retrieveDataForFlapjack.setListOfMIDsSelected(listOfMIDsSelected);
									retrieveDataForFlapjack.setHashmapOfSelectedMIDsAndMNames(hmOfSelectedMIDandMNames);
									retrieveDataForFlapjack.setHashmapOfSelectedGIDsAndGNames(hmOfGIDAndGName);
									retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
									retrieveDataForFlapjack.setExportType(strSelectedColumn);
									retrieveDataForFlapjack.retrieveFlapjackData();
									dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();

									if (dataToBeExportedBuiltSuccessfully){
										generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
										generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
										generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
										
										Component newGIDResultComponent = buildGIDResultComponent();
										_tabsheetForGID.replaceComponent(buildGIDResultComponent, newGIDResultComponent);
										_tabsheetForGID.requestRepaint();
										buildGIDResultComponent = newGIDResultComponent;
										_tabsheetForGID.getTab(3).setEnabled(true);
										_tabsheetForGID.setSelectedTab(buildGIDResultComponent);
									}
								}
							}
							
						});
						/*_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;*/
					} else if (strSelectedColumn.equals("")){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;
					} else {
						if (false == bGenerateFlapjack) {
							bGenerateFlapjack = true;
						}
					}


					strSelectedFormat = "Matrix";

					if ("true".equals(chbFlapjack.getValue().toString())){
						strSelectedFormat = "Flapjack";
					}

					

					RetrieveDataForFlapjack retrieveDataForFlapjack = new RetrieveDataForFlapjack(_mainHomePage);
					retrieveDataForFlapjack.setGenotypingType("GIDs");
					retrieveDataForFlapjack.setListOfGermplasmsProvided(listOfAllGermplasmNamesForGIDsProvided);
					retrieveDataForFlapjack.setListOfMarkersSelected(listOfMarkersSelected);
					retrieveDataForFlapjack.setListOfGIDsSelected((ArrayList<Integer>) listOfGIDsProvided);
					retrieveDataForFlapjack.setListOfMIDsSelected(listOfMIDsSelected);
					retrieveDataForFlapjack.setHashmapOfSelectedMIDsAndMNames(hmOfSelectedMIDandMNames);
					retrieveDataForFlapjack.setHashmapOfSelectedGIDsAndGNames(hmOfGIDAndGName);
					retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
					retrieveDataForFlapjack.setExportType(strSelectedColumn);
					retrieveDataForFlapjack.retrieveFlapjackData();
					dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();

					if (dataToBeExportedBuiltSuccessfully){
						generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
						generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
						generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
					}
				}

				if (null == strSelectedFormat || strSelectedFormat.equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required format.",  Notification.TYPE_ERROR_MESSAGE);
					return;
				} 

				if (dataToBeExportedBuiltSuccessfully){
					Component newGIDResultComponent = buildGIDResultComponent();
					_tabsheetForGID.replaceComponent(buildGIDResultComponent, newGIDResultComponent);
					_tabsheetForGID.requestRepaint();
					buildGIDResultComponent = newGIDResultComponent;
					_tabsheetForGID.getTab(3).setEnabled(true);
					_tabsheetForGID.setSelectedTab(buildGIDResultComponent);
				}
			}
			private long getAlleleCountByGID(
					ArrayList<Integer> listOfGIDs2) throws MiddlewareQueryException {
				long localAlleleCountByGID = getLocalAlleleCountByGID(listOfGIDs2);
				long centralAlleleCountByGID = getCentralAlleleCountByGID(listOfGIDs2);
				return localAlleleCountByGID + centralAlleleCountByGID;
			}
			private long getCentralAlleleCountByGID(
					ArrayList<Integer> listOfGIDs2) throws MiddlewareQueryException {
				AlleleValuesDAO alleleValuesDAO = new AlleleValuesDAO();
				alleleValuesDAO.setSession(localSession);
				return alleleValuesDAO.countAlleleValuesByGids(listOfGIDs2);
			}
			private long getLocalAlleleCountByGID(ArrayList<Integer> listOfGIDs2) throws MiddlewareQueryException {
				AlleleValuesDAO alleleValuesDAO = new AlleleValuesDAO();
				alleleValuesDAO.setSession(centralSession);
				return alleleValuesDAO.countAlleleValuesByGids(listOfGIDs2);
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

		completeFormatLayout.addComponent(lblTitle);
		completeFormatLayout.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		completeFormatLayout.addComponent(horizontalLayoutForTwoFormats);
		completeFormatLayout.addComponent(layoutForButton);
		completeFormatLayout.setComponentAlignment(layoutForButton, Alignment.BOTTOM_CENTER);

		OptionGroup optiongroup = new OptionGroup();
		optiongroup.addItem(chbMatrix);
		optiongroup.addItem(chbFlapjack);
		optiongroup.setMultiSelect(false);

		if (null != listOfMarkersSelected && 0 != listOfMarkersSelected.size()){
			btnNext.setEnabled(true);
			chbMatrix.setEnabled(true);
			chbFlapjack.setEnabled(true);
			optiongroup.setEnabled(true);
		} else {
			btnNext.setEnabled(false);
			chbMatrix.setEnabled(false);
			chbFlapjack.setEnabled(false);
			optiongroup.setEnabled(false);
		}
		
		return completeFormatLayout;
	}


	protected void retrieveGIDDataForMatrixFormat() throws GDMSException {
		//Matrix format requires the following lists
		//listOfMarkersSelected -- obtained when user clicks on Next button in buildGIDMarkerComponent 
		//listOfGIDsSelected selected -- also obtained when user clicks on Next button in buildGIDMarkerComponent 

		//Based on which we have to obtain the listOfGermplasmNames and list of AlleleValueElement

		/*AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);*/
		ArrayList<Integer> listOfNidsForSelectedGIDs = new ArrayList<Integer>();
		try {			
			List<AccMetadataSetPK> accMetadataSets = genoManager.getGdmsAccMetadatasetByGid(listOfGIDsProvided, 0, (int) genoManager.countGdmsAccMetadatasetByGid(listOfGIDsProvided));
		       // Debug.println(0, "testGetGdmsAccMetadatasetByGid() RESULTS: ");
		        for (AccMetadataSetPK accMetadataSet : accMetadataSets) {
		            //Debug.println(0, accMetadataSet.toString());
		        	listOfNidsForSelectedGIDs.add(accMetadataSet.getNameId());
		        	//datasetIDs.add(accMetadataSet.getDatasetId());
		        }
			/*List<Integer> nameIdsByGermplasmIdsLocal = accMetadataSetDAOLocal.getNameIdsByGermplasmIds(listOfGIDsProvided);
			List<Integer> nameIdsByGermplasmIdsCentral = accMetadataSetDAOCentral.getNameIdsByGermplasmIds(listOfGIDsProvided);

			for (Integer iGID : nameIdsByGermplasmIdsLocal){
				if (false == listOfNidsForSelectedGIDs.contains(iGID)){
					listOfNidsForSelectedGIDs.add(iGID);
				}
			}
			for (Integer iGID : nameIdsByGermplasmIdsCentral){
				if (false == listOfNidsForSelectedGIDs.contains(iGID)){
					listOfNidsForSelectedGIDs.add(iGID);
				}
			}*/
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving Names for selected GIDs");
		}

		/*NameDAO nameDAOLocal = new NameDAO();
		nameDAOLocal.setSession(localSession);
		NameDAO nameDAOCentral = new NameDAO();
		nameDAOCentral.setSession(centralSession);*/
		listOfAllGermplasmNamesForGIDsProvided = new ArrayList<String>();
		hmOfGIDAndGName = new HashMap<Integer, String>();
		try {
			Name names = null;
			for(int n=0;n<listOfNidsForSelectedGIDs.size();n++){
				names=manager.getGermplasmNameByID(Integer.parseInt(listOfNidsForSelectedGIDs.get(n).toString()));
				String nval = names.getNval();
				Integer germplasmId = names.getGermplasmId();
				if (false == listOfAllGermplasmNamesForGIDsProvided.contains(nval)){
					listOfAllGermplasmNamesForGIDsProvided.add(nval);
					hmOfGIDAndGName.put(germplasmId, nval);
				}
				/*if(!gidsList.contains(names.getGermplasmId()))
					gidsList.add(names.getGermplasmId());
				if(!genotypesList.contains(names.getNval()))
					genotypesList.add(names.getNval());	*/
			}
			/*List<Name> namesByNameIdsLocal = nameDAOLocal.getNamesByNameIds(listOfNidsForSelectedGIDs);
			List<Name> namesByNameIdsCentral = nameDAOCentral.getNamesByNameIds(listOfNidsForSelectedGIDs);

			for (Name name : namesByNameIdsLocal){
				String nval = name.getNval();
				Integer germplasmId = name.getGermplasmId();
				if (false == listOfAllGermplasmNamesForGIDsProvided.contains(nval)){
					listOfAllGermplasmNamesForGIDsProvided.add(nval);
					hmOfGIDAndGName.put(germplasmId, nval);
				}
			}
			for (Name name : namesByNameIdsCentral){
				String nval = name.getNval();
				Integer germplasmId = name.getGermplasmId();
				if (false == listOfAllGermplasmNamesForGIDsProvided.contains(nval)){
					listOfAllGermplasmNamesForGIDsProvided.add(nval);
					hmOfGIDAndGName.put(germplasmId, nval);
				}
			}*/
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving Names for selected GIDs");
		}


		AlleleValuesDAO alleleValuesDAOLocal = new AlleleValuesDAO();
		alleleValuesDAOLocal.setSession(localSession);
		AlleleValuesDAO alleleValuesDAOCentral = new AlleleValuesDAO();
		alleleValuesDAOCentral.setSession(centralSession);
		listOfAllelicValueElements = new ArrayList<AllelicValueElement>();
		
		
		ArrayList glist = new ArrayList();
		ArrayList midslist = new ArrayList();
		String data="";
		try {
			
			List<AllelicValueElement> allelicValues =genoManager.getAllelicValuesByGidsAndMarkerNames(listOfGIDsProvided, listOfMarkersSelected);
			
			System.out.println(" allelicValues =:"+allelicValues);		
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
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving list of AllelicValueElement for selected GIDs");
		}
	}

	/*private List<AllelicValueElement> getAlleleValues() throws MiddlewareQueryException {
		List<AllelicValueElement> listToReturn = new ArrayList<AllelicValueElement>();
		List<AllelicValueElement> localAlleleValues = getLocalAlleleValues();
		if(null != listToReturn) {
			listToReturn.addAll(localAlleleValues);
		}
		List<AllelicValueElement> centralAlleleValues = getCentralAlleleValues();
		if(null != listToReturn) {
			listToReturn.addAll(centralAlleleValues);
		}
		
		System.out.println("Gids Retrieval  :"+listToReturn);
		
		return listToReturn;
	}

	private List<AllelicValueElement> getCentralAlleleValues() throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(centralSession);
		return markerDAO.getAllelicValuesByGidsAndMarkerNames(listOfGIDsProvided, listOfMarkersSelected);
	}

	private List<AllelicValueElement> getLocalAlleleValues() throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(localSession);
		return markerDAO.getAllelicValuesByGidsAndMarkerNames(listOfGIDsProvided, listOfMarkersSelected);
	}
*/
	private Component buildGIDResultComponent() {
		VerticalLayout resultsLayout = new VerticalLayout();
		resultsLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);

		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println(absoluteFile);


		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("WEB-INF")) {
				fileExport = file;
				break;
			}
		}

		final String strAbsolutePath = fileExport.getAbsolutePath();
				
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
		realPath=_mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory().toString();
		Button btnVisualizeFJ = new Button("Visualize in Flapjack");
		btnVisualizeFJ.setStyleName(Reindeer.BUTTON_LINK);
		btnVisualizeFJ.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				//System.out.println("Trying to execute the flapjackrun.bat file.");
				/*String[] cmd = {"cmd.exe", "/c", "start", strFJVisualizeLink};
				Runtime rt = Runtime.getRuntime();*/
				File fexists=new File(realPath+"/Flapjack/Flapjack.flapjack");
				if(fexists.exists()) { fexists.delete(); 
				//System.out.println("proj exists and deleted");
				}
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



		Button btnDownloadMatrixFile = new Button("Download Matrix text File");
		btnDownloadMatrixFile.setStyleName(Reindeer.BUTTON_LINK);
		btnDownloadMatrixFile.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				_mainHomePage.getMainWindow().getWindow().open(new FileDownloadResource(
						matrixFileForGIDRetrieval, _mainHomePage.getMainWindow().getWindow().getApplication()));
			}
		});


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
				if(listOfMarkersSelected.size() > 252) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Column size exceeded maximum size. Please export the file as text file.", Notification.TYPE_ERROR_MESSAGE);
					return;
				} else {
					_mainHomePage.getMainWindow().getWindow().open(new FileDownloadResource(
							matrixFileForGIDRetrieval, _mainHomePage.getMainWindow().getWindow().getApplication()));
				}
			}
		});
		layoutForExportTypes.addComponent(excelButton);

		//20131210: Tulasi --- Not displaying the PDF and Print buttons
		/*themeResource = new ThemeResource("images/pdf.gif");
		Button pdfButton = new Button();
		pdfButton.setIcon(themeResource);
		pdfButton.setStyleName(Reindeer.BUTTON_LINK);
		pdfButton.setDescription("PDF Format");
		pdfButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				if(listOfMarkersSelected.size() > 252) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Column size exceeded maximum size. Please export the file as text file.", Notification.TYPE_ERROR_MESSAGE);
					return;
				} else {
					_mainHomePage.getMainWindow().getWindow().open(new FileDownloadResource(
							pdfFile, _mainHomePage.getMainWindow().getWindow().getApplication()));
				}
			}
		});
		layoutForExportTypes.addComponent(pdfButton);

		themeResource = new ThemeResource("images/print.gif");
		Button printButton = new Button();
		printButton.setIcon(themeResource);
		printButton.setStyleName(Reindeer.BUTTON_LINK);
		printButton.setDescription("Print Format");
		printButton.addListener(this);
		layoutForExportTypes.addComponent(printButton);*/

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
				/*File similarityMatrixFile = new File(""); //TODO: Have to provide the File location
				FileResource fileResource = new FileResource(similarityMatrixFile, _mainHomePage);
				_mainHomePage.getMainWindow().getWindow().open(fileResource, "Similarity Matrix File", true);*/
			}
		});
		//20131216: Added link to download Similarity Matrix File
		if (null != strSelectedFormat){
			if (strSelectedFormat.equals("Flapjack")){
				//resultsLayout.addComponent(btnFJDat);
				//resultsLayout.addComponent(btnFJMap);
				//resultsLayout.addComponent(btnFJText);
				resultsLayout.addComponent(btnAllFlapjackFiles);
				resultsLayout.addComponent(btnVisualizeFJ);
				resultsLayout.addComponent(similarityMatrixButton);
			} else if (strSelectedFormat.equals("Matrix")) {
			}
		} else {
			excelButton.setEnabled(false);
			/*pdfButton.setEnabled(false);
			printButton.setEnabled(false);*/
			resultsLayout.addComponent(layoutForExportTypes);
			resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		}
		if (null != strSelectedFormat){
			if (false == strSelectedFormat.equals("Flapjack")){
				if(listOfMarkersSelected.size() > 252) {
					resultsLayout.addComponent(btnDownloadMatrixFile);
				}
				resultsLayout.addComponent(layoutForExportTypes);
				resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
			}
		}

		return resultsLayout;
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}

	public List<Integer> obtainListOfGIDsInTextArea() throws GDMSException{
		List<Integer> listOfGIDs = new ArrayList<Integer>();
		String strTextWithGIDs = textArea.getValue().toString();
		String[] arrayOfGIDs = strTextWithGIDs.split(",");
		for (int i = 0; i < arrayOfGIDs.length; i++){
			try {
				String strGID = arrayOfGIDs[i];
				int iGID = Integer.parseInt(strGID);
				listOfGIDs.add(iGID);
			} catch (NumberFormatException nfe){
				throw new GDMSException(nfe.getMessage());
			}
		}
		return listOfGIDs;
	}



}
