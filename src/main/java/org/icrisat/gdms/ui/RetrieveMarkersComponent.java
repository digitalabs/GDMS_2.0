package org.icrisat.gdms.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.AlleleValuesDAO;
import org.generationcp.middleware.dao.gdms.CharValuesDAO;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.dao.gdms.MappingDataDAO;
import org.generationcp.middleware.dao.gdms.MappingPopDAO;
import org.generationcp.middleware.dao.gdms.MappingPopValuesDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.QtlDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.GermplasmMarkerElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.MappingPopValues;
import org.generationcp.middleware.pojos.gdms.MappingValueElement;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.MarkerNameElement;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.retrieve.RetrieveQTL;
import org.icrisat.gdms.ui.common.FileDownloadResource;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.OptionWindowForFlapjackMap;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
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
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;


public class RetrieveMarkersComponent  implements Component.Listener {

	private static final long serialVersionUID = 1L;

	private GDMSMain _mainHomePage;
	private TabSheet _tabsheetForMarkers;
	private Component buildMarkerResultComponent;
	private Component buildMarkerGermplasmComponent;
	private Component buildFormatComponent;
	private FileUploadComponent uploadComponent;
	private TextArea textArea;
	private CheckBox chbMatrix;
	private CheckBox chbFlapjack;
	private final String MATRIX_FORMAT = "Genotyping X Marker Matrix"; 
	private final String FLAPJACK_FORMAT = "Flapjack";
	private ArrayList<String> listGermplasmsEnteredInTheSearchField;
	private Object strSelectedFormat;
	private ArrayList<String> listOfGermplasmsByMarkers;
	protected ArrayList<String> listOfMarkersProvided;
	protected ArrayList<String> listGermplasmsSelected;
	private Session localSession;
	private Session centralSession;
	private ArrayList<Integer> listOfGIDs;
	private MarkerDAO markerDAOLocal;
	private MarkerDAO markerDAOCentral;
	private ArrayList<Integer> listOfMarkerIds;
	private AlleleValuesDAO alleleValuesDAOLocal;
	private AlleleValuesDAO alleleValuesDAOCentral;
	private HashMap<String, Integer> hmOfGNamesAndGids;
	private ArrayList<Integer> listOfGIDsSelected;
	private ArrayList<GermplasmMarkerElement> listOfGermplasmMarkerElements;
	private ArrayList<AllelicValueElement> listOfAllelicValueElements;
	private ArrayList<MappingValueElement> listOfMappingValueElements;
	private ArrayList<String> listOfMarkerNameElement;
	private File matrixFile;
	private HashMap<Integer, String> hmOfGIDsAndGermplamsSelected;
	
	HashMap<Integer, HashMap<String, Object>> mapEx = new HashMap<Integer, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
	HashMap marker = new HashMap();
	
	
	private String _strSeletedFlapjackType = null;
	private List<File> generateFlagjackFiles;
	protected String strSelectedMap;
	protected String strSelectedColumn;
	ManagerFactory factory=null;
	OntologyDataManager om;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	private Button markersSampleFile;
	public RetrieveMarkersComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		om=factory.getOntologyDataManager();
		manager = factory.getGermplasmDataManager();
		genoManager=factory.getGenotypicDataManager();
	}


	public HorizontalLayout buildTabbedComponentForMarkers() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		_tabsheetForMarkers = new TabSheet();
		//_tabsheetForMarkers.setSizeFull();
		_tabsheetForMarkers.setWidth("700px");
		
		Component buildGIDComponent = buildMarkersComponent();

		buildMarkerGermplasmComponent = buildMarkerGermplasmComponent();

		buildFormatComponent = buildFormatComponent();

		buildMarkerResultComponent = buildResultComponent();

		buildGIDComponent.setSizeFull();
		buildMarkerGermplasmComponent.setSizeFull();
		buildFormatComponent.setSizeFull();
		buildMarkerResultComponent.setSizeFull();
		
		_tabsheetForMarkers.addComponent(buildGIDComponent);
		_tabsheetForMarkers.addComponent(buildMarkerGermplasmComponent);
		_tabsheetForMarkers.addComponent(buildFormatComponent);
		_tabsheetForMarkers.addComponent(buildMarkerResultComponent);
		
		_tabsheetForMarkers.getTab(1).setEnabled(false);
		_tabsheetForMarkers.getTab(2).setEnabled(false);
		_tabsheetForMarkers.getTab(3).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForMarkers);

		return horizontalLayout;
	}


	private Component buildMarkersComponent() {
		VerticalLayout layoutForGIDTab = new VerticalLayout();
		layoutForGIDTab.setCaption("Markers");
		layoutForGIDTab.setSpacing(true);
		layoutForGIDTab.setSizeFull();
		layoutForGIDTab.setMargin(true, true, true, true);

		VerticalLayout verticalLayoutOne = new VerticalLayout();
		Label label1 = new Label("Upload text file with desired Markers");
		label1.setStyleName(Reindeer.LABEL_H2);

		label1.setStyleName(Reindeer.LABEL_H2);
		markersSampleFile = new Button("Sample File");
		markersSampleFile.setImmediate(true);
		markersSampleFile.setStyleName(Reindeer.BUTTON_LINK);
		markersSampleFile.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
                
				WebApplicationContext ctx = (WebApplicationContext) _mainHomePage.getContext();
                String strTemplateFolderPath = ctx.getHttpSession().getServletContext().getRealPath("\\VAADIN\\themes\\gdmstheme\\Templates");
                //System.out.println("Folder-Path: " + strTemplateFolderPath);
                
				//String strMarkerType = _strMarkerType.replace(" ", "");
				String strFileName = "Markers.txt";
				
				File strFileLoc = new File(strTemplateFolderPath + "\\" + strFileName);
				FileResource fileResource = new FileResource(strFileLoc, _mainHomePage);
				//_mainHomePage.getMainWindow().getWindow().open(fileResource, "_blank", true);
				
				if (strFileName.endsWith(".txt")) {
					_mainHomePage.getMainWindow().getWindow().open(fileResource, "GIDs", true);
				} 				
			}
			
		});
		
		
		uploadComponent = new FileUploadComponent(_mainHomePage, "Marker Name");
		uploadComponent.setWidth("90%");
		//uploadComponent.init("basic");
		//20131205: Tulasi --- Modified the code to display the Markers read from the text file in the TextArea and then proceed to the next tab upon clicking Next
		uploadComponent.registerListener(new FileUploadListener() {
			@Override
			public void updateLocation(String absolutePath) {
				List<String> listOfMarkesFromTextFile = uploadComponent.getListOfMarkers();
				if (null != listOfMarkesFromTextFile) {
					String strListOfMarkersFromTextFile = "";
					for (String strMarker : listOfMarkesFromTextFile) {
						strListOfMarkersFromTextFile += String.valueOf(strMarker) + ",";
					}
					if (0 != strListOfMarkersFromTextFile.trim().length()) {
						strListOfMarkersFromTextFile = strListOfMarkersFromTextFile.substring(0, strListOfMarkersFromTextFile.length()-1);
						textArea.setValue(strListOfMarkersFromTextFile);
					}
				}
			}
		});
		//20131205: Tulasi --- Modified the code to display the GIDs read from the text file in the TextArea and then proceed to the next tab upon clicking Next
		
		verticalLayoutOne.addComponent(label1);
		verticalLayoutOne.addComponent(uploadComponent);
		
		verticalLayoutOne.addComponent(markersSampleFile);
		verticalLayoutOne.setComponentAlignment(markersSampleFile, Alignment.BOTTOM_CENTER);
		
		VerticalLayout verticalLayoutTwo = new VerticalLayout();
		verticalLayoutTwo.setSpacing(true);
		Label label2 = new Label("Enter Markers separated by commas");
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
					listOfMarkersProvided = obtainListOfMarkersInTextArea();
				} else {
					listOfMarkersProvided = uploadComponent.getListOfMarkers();
				}
				
				if (null == listOfMarkersProvided || 0 == listOfMarkersProvided.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please provide the list of Markers.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				boolean bObtainedDataRequiredForNextTab = false;
				try {
					retrieveTheGermplasmNames();
					
					if (null == listOfGermplasmsByMarkers || 0 == listOfGermplasmsByMarkers.size()) {
						bObtainedDataRequiredForNextTab = false;
						_mainHomePage.getMainWindow().getWindow().showNotification("Germplasm Names required for next tab could not be obtained. Please provide valid Markers", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
					
					bObtainedDataRequiredForNextTab = true;
				} catch (GDMSException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Germplasm Names required for next tab could not be obtained", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				/*MarkerDAO markerDAOLocal = new MarkerDAO();
				markerDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
				MarkerDAO markerDAOCentral = new MarkerDAO();
				markerDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());

				listOfGermplasmsByMarkers = new ArrayList<String>();
				try {
					List<GermplasmMarkerElement> germplasmNamesByMarkerNamesLocal = markerDAOLocal.getGermplasmNamesByMarkerNames(listOfMarkersProvided);
					List<GermplasmMarkerElement> germplasmNamesByMarkerNamesCentral = markerDAOCentral.getGermplasmNamesByMarkerNames(listOfMarkersProvided);
					for (GermplasmMarkerElement germplasmMarkerElement : germplasmNamesByMarkerNamesLocal){
						String germplasmName = germplasmMarkerElement.getGermplasmName();
						if (false == listOfGermplasmsByMarkers.contains(germplasmName)){
							listOfGermplasmsByMarkers.add(germplasmName);
						}
					}
					for (GermplasmMarkerElement germplasmMarkerElement : germplasmNamesByMarkerNamesCentral){
						String germplasmName = germplasmMarkerElement.getGermplasmName();
						if (false == listOfGermplasmsByMarkers.contains(germplasmName)){
							listOfGermplasmsByMarkers.add(germplasmName);
						}
					}
					
					if (0 == listOfGermplasmsByMarkers.size()){
						_mainHomePage.getMainWindow().getWindow().showNotification("No Genotyping data for the provided marker(s)", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
				} catch (MiddlewareQueryException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Germplasm Names for Markers provided", Notification.TYPE_ERROR_MESSAGE);
					return;
				}*/
				
				if (bObtainedDataRequiredForNextTab){
					VerticalLayout newMarkerGermplasmComponent = (VerticalLayout) buildMarkerGermplasmComponent();
					_tabsheetForMarkers.replaceComponent(buildMarkerGermplasmComponent, newMarkerGermplasmComponent);
					buildMarkerGermplasmComponent = newMarkerGermplasmComponent;
					buildMarkerGermplasmComponent.requestRepaint();
					_tabsheetForMarkers.getTab(1).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(buildMarkerGermplasmComponent);
					_tabsheetForMarkers.requestRepaint();
				} else {
					_mainHomePage.getMainWindow().getWindow().showNotification("Germplasm Names required for next tab could not be obtained", Notification.TYPE_ERROR_MESSAGE);
					return;
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


	protected void retrieveTheGermplasmNames() throws GDMSException {
		
		listOfGIDs = new ArrayList<Integer>();
		
		//exportFileFormats.Matrix(listOfGIDs, listOfMarkerElementsFromDB, listOfMarkersProvided, listOfAllelicValues, listGermplasmsSelected, listOfMappingValues);

		markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);
		listOfMarkerIds = new ArrayList<Integer>();
		try {

			long countAll = markerDAOLocal.countAll();
			List<Integer> listOfMarkerIdsFromLocal = markerDAOLocal.getIdsByNames(listOfMarkersProvided, 0, (int)countAll);
			long countAll2 = markerDAOCentral.countAll();
			List<Integer> listOfMarkerIDsFromCentral = markerDAOCentral.getIdsByNames(listOfMarkersProvided, 0, (int)countAll2);

			for (Integer iMarkerID : listOfMarkerIdsFromLocal){
				if (false == listOfMarkerIds.contains(iMarkerID)){
					listOfMarkerIds.add(iMarkerID);
				}
			}
			for (Integer iMarkerID : listOfMarkerIDsFromCentral){
				if (false == listOfMarkerIds.contains(iMarkerID)){
					listOfMarkerIds.add(iMarkerID);
				}
			}
			
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker-IDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("Error Retrieving Marker-IDs for the Markers provided");
		}
	
		
		alleleValuesDAOLocal = new AlleleValuesDAO();
		alleleValuesDAOLocal.setSession(localSession);
		alleleValuesDAOCentral = new AlleleValuesDAO();
		alleleValuesDAOCentral.setSession(centralSession);
		for (int i = 0; i < listOfMarkerIds.size(); i++){
			Integer iMarkerID = listOfMarkerIds.get(i);
			try {
				
				long countGIDsByMarkerId = alleleValuesDAOLocal.countGIDsByMarkerId(iMarkerID);
				List<Integer> giDsByMarkerId = alleleValuesDAOLocal.getGIDsByMarkerId(iMarkerID, 0, (int)countGIDsByMarkerId);
				for (Integer iGID : giDsByMarkerId){
					if (false == listOfGIDs.contains(iGID)){
						listOfGIDs.add(iGID);
					}
				}
				
				long countGIDsByMarkerId2 = alleleValuesDAOCentral.countGIDsByMarkerId(iMarkerID);
			    List<Integer> giDsByMarkerId2 = alleleValuesDAOCentral.getGIDsByMarkerId(iMarkerID, 0, (int)countGIDsByMarkerId2);
				for (Integer iGID : giDsByMarkerId2){
					if (false == listOfGIDs.contains(iGID)){
						listOfGIDs.add(iGID);
					}
				}
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving GIDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
				//return;
				throw new GDMSException("Error Retrieving GIDs for the Markers provided");
			}
		}
		
		CharValuesDAO charValuesDAOLocal = new CharValuesDAO();
		charValuesDAOLocal.setSession(localSession);
		CharValuesDAO charValuesDAOCentral = new CharValuesDAO();
		charValuesDAOCentral.setSession(centralSession);
		for (int i = 0; i < listOfMarkerIds.size(); i++){
			Integer iMarkerID = listOfMarkerIds.get(i);
			try {
				
				long countGIDsByMarkerId = charValuesDAOLocal.countGIDsByMarkerId(iMarkerID);
				List<Integer> giDsByMarkerId = charValuesDAOLocal.getGIDsByMarkerId(iMarkerID, 0, (int)countGIDsByMarkerId);
				for (Integer iGID : giDsByMarkerId){
					if (false == listOfGIDs.contains(iGID)){
						listOfGIDs.add(iGID);
					}
				}
				
				long countGIDsByMarkerId2 = charValuesDAOCentral.countGIDsByMarkerId(iMarkerID);
			    List<Integer> giDsByMarkerId2 = charValuesDAOCentral.getGIDsByMarkerId(iMarkerID, 0, (int)countGIDsByMarkerId2);
				for (Integer iGID : giDsByMarkerId2){
					if (false == listOfGIDs.contains(iGID)){
						listOfGIDs.add(iGID);
					}
				}
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving GIDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
				//return;
				throw new GDMSException("Error Retrieving GIDs for the Markers provided");
			}
		}
		
		
		MappingPopValuesDAO mappingPopValuesDAOLocal = new MappingPopValuesDAO();
		mappingPopValuesDAOLocal.setSession(localSession);
		MappingPopValuesDAO mappingPopValuesDAOCentral = new MappingPopValuesDAO();
		mappingPopValuesDAOCentral.setSession(localSession);		
		for (int i = 0; i < listOfMarkerIds.size(); i++){
			Integer iMarkerID = listOfMarkerIds.get(i);
			try {
				
				long countGIDsByMarkerId = mappingPopValuesDAOLocal.countGIDsByMarkerId(iMarkerID);
				List<Integer> giDsByMarkerId = mappingPopValuesDAOLocal.getGIDsByMarkerId(iMarkerID, 0, (int)countGIDsByMarkerId);
				for (Integer iGID : giDsByMarkerId){
					if (false == listOfGIDs.contains(iGID)){
						listOfGIDs.add(iGID);
					}
				}
				
				long countGIDsByMarkerId2 = mappingPopValuesDAOCentral.countGIDsByMarkerId(iMarkerID);
			    List<Integer> giDsByMarkerId2 = mappingPopValuesDAOCentral.getGIDsByMarkerId(iMarkerID, 0, (int)countGIDsByMarkerId2);
				for (Integer iGID : giDsByMarkerId2){
					if (false == listOfGIDs.contains(iGID)){
						listOfGIDs.add(iGID);
					}
				}
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving GIDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
				//return;
				throw new GDMSException("Error Retrieving GIDs for the Markers provided");
			}
		}
		
		if (0 == listOfGIDs.size()){
			//_mainHomePage.getMainWindow().getWindow().showNotification("No Genotyping data for the provided marker(s)", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("No Genotyping data for the provided marker(s)");
		}
		
		
		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);
		ArrayList<Integer> listOfNameIDs = new ArrayList<Integer>();
		try {
			List<Integer> nameIdsByGermplasmIds = accMetadataSetDAOLocal.getNameIdsByGermplasmIds(listOfGIDs);
			List<Integer> nameIdsByGermplasmIds2 = accMetadataSetDAOCentral.getNameIdsByGermplasmIds(listOfGIDs);
			
			for (Integer iNid : nameIdsByGermplasmIds){
				if (false == listOfNameIDs.contains(iNid)){
					listOfNameIDs.add(iNid);
				}
			}
			for (Integer iNid : nameIdsByGermplasmIds2){
				if (false == listOfNameIDs.contains(iNid)){
					listOfNameIDs.add(iNid);
				}
			}
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names for the GIDs", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("Error Retrieving Names for the GIDs");
		}
		
		
		NameDAO nameDAOLocal = new NameDAO();
		nameDAOLocal.setSession(localSession);
		NameDAO nameDAOCentral = new NameDAO();
		nameDAOCentral.setSession(centralSession);
		ArrayList<Name> listOfAllNames = new ArrayList<Name>();
		try {
			List<Name> namesByNameIds = nameDAOLocal.getNamesByNameIds(listOfNameIDs);
			List<Name> namesByNameIds2 = nameDAOCentral.getNamesByNameIds(listOfNameIDs);

			for (Name name : namesByNameIds){
				if (false == listOfAllNames.contains(name)){
					listOfAllNames.add(name);
				}
			}
			for (Name name : namesByNameIds2){
				if (false == listOfAllNames.contains(name)){
					listOfAllNames.add(name);
				}
			}
			
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names for the GIDs", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("Error Retrieving Names for the GIDs");
		}
		
		hmOfGNamesAndGids = new HashMap<String, Integer>();
		if (0 != listOfAllNames.size()){
			listOfGermplasmsByMarkers = new ArrayList<String>();
			for (Name name : listOfAllNames){
				String nval = name.getNval();
				Integer germplasmId = name.getGermplasmId();
				listOfGermplasmsByMarkers.add(nval);
				hmOfGNamesAndGids.put(nval, germplasmId);
			}
		} else {
			//_mainHomePage.getMainWindow().getWindow().showNotification("There are not Lines for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("There are not Lines for the Markers provided");
		}
		
	}


	protected ArrayList<String> obtainListOfMarkersInTextArea() {
		ArrayList<String> listOfMarkers = new ArrayList<String>();
		String strTextWithMarkers = textArea.getValue().toString();
		String[] arrayOfMarkers = strTextWithMarkers.split(",");
		for (int i = 0; i < arrayOfMarkers.length; i++){
			String strMarker = arrayOfMarkers[i].trim();
			listOfMarkers.add(strMarker);
		}
		return listOfMarkers;
	}


	private Component buildMarkerGermplasmComponent() {
		VerticalLayout layoutForGIDMarkerTab = new VerticalLayout();
		layoutForGIDMarkerTab.setCaption("Germplasms");
		layoutForGIDMarkerTab.setSpacing(true);
		layoutForGIDMarkerTab.setSizeFull();
		layoutForGIDMarkerTab.setMargin(true, true, true, true);

		Label lblTitle = new Label("Select the Germplasms from the list");
		if (null != listOfGermplasmsByMarkers){
			if (0 < listOfGermplasmsByMarkers.size()){
				lblTitle = new Label("Select the Germplasms from the list of " + listOfGermplasmsByMarkers.size());
			}
		}
		lblTitle.setStyleName(Reindeer.LABEL_H2);


		int iNumOfMarkers = 0;
		if (null != listOfGermplasmsByMarkers && 0 < listOfGermplasmsByMarkers.size()){
			iNumOfMarkers = listOfGermplasmsByMarkers.size();
		}
		
		final TwinColSelect selectForGermplasms = new TwinColSelect();
		selectForGermplasms.setLeftColumnCaption("All Germplasms");
		selectForGermplasms.setRightColumnCaption("Selected Germplasms");
		//selectForGermplasms.setNullSelectionAllowed(false);
		//selectForGermplasms.setInvalidAllowed(false);
		//select.setWidth("500px");
		if (null != listOfGermplasmsByMarkers && 0 < listOfGermplasmsByMarkers.size()){
			for (String strGermplasm : listOfGermplasmsByMarkers) {
				selectForGermplasms.addItem(strGermplasm);
			}
		}
		selectForGermplasms.setRows(20);
		selectForGermplasms.setColumns(25);
		
		selectForGermplasms.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                	TwinColSelect colSelect = (TwinColSelect)event.getProperty();
                	Object value = colSelect.getValue();
                	Set<String> hashSet = (Set<String>) value;
                	listGermplasmsSelected = new ArrayList<String>();
                	for (String string : hashSet) {
                		listGermplasmsSelected.add(string);
					}
                }
            }
        });
		selectForGermplasms.setImmediate(true);
		
		
		HorizontalLayout horizLytForSelectComponent = new HorizontalLayout();
		horizLytForSelectComponent.setSizeFull();
		horizLytForSelectComponent.setSpacing(true);
		horizLytForSelectComponent.setMargin(true);
		horizLytForSelectComponent.addComponent(selectForGermplasms);


		final CheckBox chbSelectAll = new CheckBox("Select All");
		chbSelectAll.setImmediate(true);
		chbSelectAll.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				selectForGermplasms.setValue(listOfGermplasmsByMarkers);
			}
		});


		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("300px");
		txtFieldSearch.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;

			public void textChange(TextChangeEvent event) {
				if (null == listGermplasmsEnteredInTheSearchField) {
					listGermplasmsEnteredInTheSearchField = new ArrayList<String>();
				}
				
				if (null != txtFieldSearch.getValue()){
					String strGermplasmFromTextField = txtFieldSearch.getValue().toString();
					if (strGermplasmFromTextField.endsWith("*")){
						int indexOf = strGermplasmFromTextField.indexOf('*');
						String substring = strGermplasmFromTextField.substring(0, indexOf);
						
						for (String strGName : listOfGermplasmsByMarkers){
							//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
							if (strGName.toUpperCase().startsWith(substring) ||
								strGName.toLowerCase().startsWith(substring)) {
								listGermplasmsEnteredInTheSearchField.add(strGName);
							}
						}
					} else if (strGermplasmFromTextField.trim().equals("*")) {
						listGermplasmsEnteredInTheSearchField.addAll(listOfGermplasmsByMarkers);
					}
					selectForGermplasms.setValue(listGermplasmsEnteredInTheSearchField);
				}
			}
			
		});
		

		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);
		searchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {

				listGermplasmsEnteredInTheSearchField = new ArrayList<String>();
				
				if (null != txtFieldSearch.getValue()){
					String strGermplasmFromTextField = txtFieldSearch.getValue().toString();
					if (strGermplasmFromTextField.endsWith("*")){
						int indexOf = strGermplasmFromTextField.indexOf('*');
						String substring = strGermplasmFromTextField.substring(0, indexOf);
						
						for (String strGName : listOfGermplasmsByMarkers){
							//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
							if (strGName.toLowerCase().startsWith(substring) ||
								strGName.toUpperCase().startsWith(substring)) {
								listGermplasmsEnteredInTheSearchField.add(strGName);
							}
						}
					} else if (strGermplasmFromTextField.trim().equals("*")) {
						listGermplasmsEnteredInTheSearchField.addAll(listOfGermplasmsByMarkers);
					}
					selectForGermplasms.setValue(listGermplasmsEnteredInTheSearchField);
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

				listGermplasmsSelected = new ArrayList<String>();
				Object value2 = selectForGermplasms.getValue();
            	Set<String> hashSet = (Set<String>) value2;
            	for (String string : hashSet) {
            		listGermplasmsSelected.add(string);
				}
		
				//retrieveDataForSelectedGermplasms();
				/*if (null == listGermplasmsSelected){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be exported and click Next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}*/
            	
            	if (0 == listGermplasmsSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be exported and click Next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (0 != listGermplasmsSelected.size()) {
					Component newFormatComponent = buildFormatComponent();
					_tabsheetForMarkers.replaceComponent(buildFormatComponent, newFormatComponent);
					_tabsheetForMarkers.requestRepaint();
					buildFormatComponent = newFormatComponent;
					_tabsheetForMarkers.getTab(2).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(buildFormatComponent);
				} else {
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be exported and click Next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
			}
		});
		
		if (0 == iNumOfMarkers){
			chbSelectAll.setEnabled(false);
			txtFieldSearch.setEnabled(false);
			searchButton.setEnabled(false);
			btnNext.setEnabled(false);
			selectForGermplasms.setEnabled(false);
		} else {
			chbSelectAll.setEnabled(true);
			txtFieldSearch.setEnabled(true);
			searchButton.setEnabled(true);
			btnNext.setEnabled(true);
			selectForGermplasms.setEnabled(true);
		}

		layoutForGIDMarkerTab.addComponent(lblTitle);
		layoutForGIDMarkerTab.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		layoutForGIDMarkerTab.addComponent(horizontalLayout);
		layoutForGIDMarkerTab.addComponent(horizLytForSelectComponent);
		layoutForGIDMarkerTab.addComponent(horizontalLayoutForButton);
		layoutForGIDMarkerTab.setComponentAlignment(horizontalLayoutForButton, Alignment.MIDDLE_CENTER);

		return layoutForGIDMarkerTab;
	}


	protected void retrieveDataForSelectedGermplasms() throws GDMSException {
		
		//exportFileFormats.Matrix(listOfGIDs, listOfMarkerNameElement, listOfMarkersProvided, listOfAllelicValueElements, listOfGermplasmMarkerElements, listOfMappingValueElements);
		
	    listOfGIDsSelected = new ArrayList<Integer>();
		hmOfGIDsAndGermplamsSelected = new HashMap<Integer, String>();
		for (String strGermplasmSelected : listGermplasmsSelected){
			Integer iGID = hmOfGNamesAndGids.get(strGermplasmSelected);
			listOfGIDsSelected.add(iGID);
			hmOfGIDsAndGermplamsSelected.put(iGID, strGermplasmSelected);
		}
		
		//Retrieving list of MarkerNameElements
		listOfMarkerNameElement = new ArrayList<String>();
		try {
			
			
			List<MarkerIdMarkerNameElement> markerNames=genoManager.getMarkerNamesByMarkerIds(listOfMarkerIds);
			for (MarkerIdMarkerNameElement markerNameElement : markerNames){
				if (false == listOfMarkerNameElement.contains(markerNameElement.getMarkerName())){
					listOfMarkerNameElement.add(markerNameElement.getMarkerName());
				}
			}
			
			
			
			
			/*List<MarkerNameElement> markerNamesByGIds = markerDAOLocal.getMarkerNamesByGIds(listOfGIDsSelected);
			List<MarkerNameElement> markerNamesByGIds2 = markerDAOCentral.getMarkerNamesByGIds(listOfGIDsSelected);
			
			for (MarkerNameElement markerNameElement : markerNamesByGIds){
				if (false == listOfMarkerNameElement.contains(markerNameElement)){
					listOfMarkerNameElement.add(markerNameElement);
				}
			}
			for (MarkerNameElement markerNameElement : markerNamesByGIds2){
				if (false == listOfMarkerNameElement.contains(markerNameElement)){
					listOfMarkerNameElement.add(markerNameElement);
				}
			}*/
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving list of MarkerNameElements required for Matrix format.");
		}
		
		ArrayList glist = new ArrayList();
		ArrayList midslist = new ArrayList();
		String data="";
		
		try{
			
			List<AllelicValueElement> allelicValues =genoManager.getAllelicValuesByGidsAndMarkerNames(listOfGIDsSelected, listOfMarkerNameElement);
			
			//System.out.println(" allelicValues =:"+allelicValues);		
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
			String strErrMsg = "Error retrieving list of AllelicValueElement for the selected GIDs required for Matrix format.";
			throw new GDMSException(strErrMsg);
		}
		
		
		//Retrieving list of MarkerNameElements
		/*MappingPopDAO mappingPopDAOLocal = new MappingPopDAO();
		mappingPopDAOLocal.setSession(localSession);
		MappingPopDAO mappingPopDAOCentral = new MappingPopDAO();
		mappingPopDAOCentral.setSession(centralSession);
		listOfMappingValueElements = new ArrayList<MappingValueElement>();
		try {
			List<MappingValueElement> listFromLocalDB = mappingPopDAOLocal.getMappingValuesByGidAndMarkerIds(listOfGIDsSelected, listOfMarkerIds);
			List<MappingValueElement> listFromCentralDB = mappingPopDAOCentral.getMappingValuesByGidAndMarkerIds(listOfGIDsSelected, listOfMarkerIds);
			
			for (MappingValueElement mappingValueElement : listFromLocalDB){
				if (false == listOfMappingValueElements.contains(mappingValueElement)){
					listOfMappingValueElements.add(mappingValueElement);
				}
			}
			for (MappingValueElement mappingValueElement : listFromCentralDB){
				if (false == listOfMappingValueElements.contains(mappingValueElement)){
					listOfMappingValueElements.add(mappingValueElement);
				}
			}
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving list of MappingValueElements required for Matrix format.");
		}
		
		
		//Retrieving list of AllelicValueElements
		listOfAllelicValueElements = new ArrayList<AllelicValueElement>();
		try {
			long lCountMappingAlleleValues = alleleValuesDAOLocal.countMappingAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDsSelected);
			List<AllelicValueElement> listFromLocalDB = alleleValuesDAOLocal.getMappingAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDsSelected, 0, (int)lCountMappingAlleleValues);
			long lCountMappingAlleleValues2 = alleleValuesDAOCentral.countMappingAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDsSelected);
			List<AllelicValueElement> listFromCentralDB = alleleValuesDAOCentral.getMappingAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDsSelected, 0, (int)lCountMappingAlleleValues2);

			for (AllelicValueElement allelicValueElement: listFromLocalDB){
				if (false == listOfAllelicValueElements.contains(allelicValueElement)){
					listOfAllelicValueElements.add(allelicValueElement);
				}
			}
			for (AllelicValueElement allelicValueElement: listFromCentralDB){
				if (false == listOfAllelicValueElements.contains(allelicValueElement)){
					listOfAllelicValueElements.add(allelicValueElement);
				}
			}
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving list of AllelicValueElement required for Matrix format.");
		}
		
		//Retrieving list of GermplasmMarkerElement
		listOfGermplasmMarkerElements = new ArrayList<GermplasmMarkerElement>();
		try {
			List<GermplasmMarkerElement> listFromLocalDB = markerDAOLocal.getGermplasmNamesByMarkerNames(listOfMarkersProvided);
			List<GermplasmMarkerElement> listFromCentralDB = markerDAOLocal.getGermplasmNamesByMarkerNames(listOfMarkersProvided);
			
			for (GermplasmMarkerElement germplasmMarkerElement : listFromLocalDB){
				if (false == listOfGermplasmMarkerElements.contains(germplasmMarkerElement)){
					listOfGermplasmMarkerElements.add(germplasmMarkerElement);
				}
			}
			for (GermplasmMarkerElement germplasmMarkerElement : listFromCentralDB){
				if (false == listOfGermplasmMarkerElements.contains(germplasmMarkerElement)){
					listOfGermplasmMarkerElements.add(germplasmMarkerElement);
				}
			}
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving list of GermplasmMarkerElement required for Matrix format.");
		}*/
		
	}


	private Component buildFormatComponent() {
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
		optionGroupForColumn.addListener(new Property.ValueChangeListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
                _strSeletedFlapjackType  = String.valueOf(event.getProperty().getValue());
			}
		});
		HorizontalLayout horizLayoutForColumns = new HorizontalLayout();
		horizLayoutForColumns.setSpacing(true);
		horizLayoutForColumns.addComponent(lblColumn);
		horizLayoutForColumns.addComponent(optionGroupForColumn);
		
		final ComboBox selectMap = new ComboBox();
		Object itemId = selectMap.addItem();
		selectMap.setItemCaption(itemId, "Select Map");
		selectMap.setValue(itemId);
		selectMap.setNullSelectionAllowed(false);
		selectMap.setImmediate(true);
		selectMap.setEnabled(false);
		optionGroupForColumn.setEnabled(false);
		
		final ArrayList<String> arrayListOfMapNames = new ArrayList<String>();
		HashMap<String, Integer> hmOfMapNameAndID = new HashMap<String, Integer>();

		if (null != listGermplasmsSelected && 0 != listGermplasmsSelected.size()){
			MapDAO mapDAOLocal = new MapDAO();
			mapDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
			MapDAO mapDAOCentral = new MapDAO();
			mapDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());			

			try {
				List<Map> listofAllMapsFromLocal = mapDAOLocal.getAll();
				List<Map> listOfAllMapsFromCentral = mapDAOCentral.getAll();

				if (null != listofAllMapsFromLocal){
					for (Map map : listofAllMapsFromLocal){
						String mapName = map.getMapName();
						Integer mapId = map.getMapId();
						
						if (false == arrayListOfMapNames.contains(mapName)){
							arrayListOfMapNames.add(mapName);
							hmOfMapNameAndID.put(mapName, mapId);
						}
					}
				}

				if (null != listOfAllMapsFromCentral){
					for (Map map : listOfAllMapsFromCentral){
						String mapName = map.getMapName();
						Integer mapId = map.getMapId();
						
						if (false == arrayListOfMapNames.contains(mapName)){
							arrayListOfMapNames.add(mapName);
							hmOfMapNameAndID.put(mapName, mapId);
						}
					}
				}

			} catch (MiddlewareQueryException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Maps from the database", Notification.TYPE_ERROR_MESSAGE);
				return null;
			}

		}

		for (String strMapName : arrayListOfMapNames){
			selectMap.addItem(strMapName);
		}

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
			private boolean bGenerateFlapjack = false;
			boolean dataToBeExportedBuiltSuccessfully = false;
			
			public void buttonClick(ClickEvent event) {

				if (null == listOfMarkersProvided || 0 == listOfMarkersProvided.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Markers to be displayed in the required format.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (null == listGermplasmsSelected || 0 == listGermplasmsSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be displayed in the required format.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (chbMatrix.getValue().toString().equals("true")){
					strSelectedFormat = "Matrix";
					
						
						try {
							retrieveDataForSelectedGermplasms();
							dataToBeExportedBuiltSuccessfully = true;
						} catch (GDMSException e1) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e1.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
							return;
						}
						
						if (dataToBeExportedBuiltSuccessfully) {
							//System.out.println("Format Selected: " + strSelectedFormat);
							ExportFileFormats exportFileFormats = new ExportFileFormats();
							try{
								matrixFile = exportFileFormats.Matrix(_mainHomePage, listOfGIDsSelected, listOfMarkersProvided, hmOfGIDsAndGermplamsSelected, mapEx);
							} catch (GDMSException e) {
								_mainHomePage.getMainWindow().getWindow().showNotification("Error generating the Matrix File", Notification.TYPE_ERROR_MESSAGE);
								return;
							}
							//matrixFile = exportFileFormats.MatrixDataSNP(_mainHomePage, listOfGIDsSelected, listOfMarkersProvided, listOfGermplasmMarkerElements, mapEx, hmOfGIDsAndGermplamsSelected);
							//System.out.println("Received the generated Matrix file.");
						}
						
					
					
				} else if (chbFlapjack.getValue().toString().equals("true")){
					strSelectedFormat = "Flapjack";
					//System.out.println("Format Selected: " + strSelectedFormat);
					
					Object mapValue = selectMap.getValue();
					if (mapValue instanceof Integer){
						Integer itemId = (Integer)mapValue;
						if (itemId.equals(1)){
							strSelectedMap = "";
						} 
					} else {
						String strMapSelected = mapValue.toString();
						if (null != arrayListOfMapNames){
							if (arrayListOfMapNames.contains(strMapSelected)){
								strSelectedMap = strMapSelected;
							}
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
										dataToBeExportedBuiltSuccessfully = false;
										_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
										return;
									}
									
									generateFlagjackFiles = generateFlagjackFiles(strSelectedMap, _strSeletedFlapjackType);
									if (null != generateFlagjackFiles){
										dataToBeExportedBuiltSuccessfully = true;
									}
									
									if (dataToBeExportedBuiltSuccessfully){
										Component newResultComponent = buildResultComponent();
										_tabsheetForMarkers.replaceComponent(buildMarkerResultComponent, newResultComponent);
										buildMarkerResultComponent.requestRepaint();
										buildMarkerResultComponent = newResultComponent;
										_tabsheetForMarkers.getTab(3).setEnabled(true);
										_tabsheetForMarkers.setSelectedTab(buildMarkerResultComponent);
									}
									
								}
							}
							
						});
						dataToBeExportedBuiltSuccessfully = false;
						/*_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;*/
					} else if (strSelectedColumn.equals("")){
						dataToBeExportedBuiltSuccessfully = false;
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;
					} else {
						if (false == bGenerateFlapjack) {
							bGenerateFlapjack = true;
						}
					}
					if (bGenerateFlapjack) {
						generateFlagjackFiles = generateFlagjackFiles(strSelectedMap, _strSeletedFlapjackType);
						if (null != generateFlagjackFiles){
							dataToBeExportedBuiltSuccessfully = true;
						}
					}
				}

				if (null == strSelectedFormat || strSelectedFormat.equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required format.",  Notification.TYPE_ERROR_MESSAGE);
					return;
				}  

				if (dataToBeExportedBuiltSuccessfully){
					Component newResultComponent = buildResultComponent();
					_tabsheetForMarkers.replaceComponent(buildMarkerResultComponent, newResultComponent);
					buildMarkerResultComponent.requestRepaint();
					buildMarkerResultComponent = newResultComponent;
					_tabsheetForMarkers.getTab(3).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(buildMarkerResultComponent);
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

		completeFormatLayout.addComponent(lblTitle);
		completeFormatLayout.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		completeFormatLayout.addComponent(horizontalLayoutForTwoFormats);
		completeFormatLayout.addComponent(layoutForButton);
		completeFormatLayout.setComponentAlignment(layoutForButton, Alignment.BOTTOM_CENTER);

		OptionGroup optiongroup = new OptionGroup();
		optiongroup.addItem(chbMatrix);
		optiongroup.addItem(chbFlapjack);
		optiongroup.setMultiSelect(false);

		
		if (null != listGermplasmsSelected && 0 != listGermplasmsSelected.size()){
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

	private List<File> generateFlagjackFiles(String theMap, String theSeletedFlapjackType2) {
		generateFlagjackFiles = null;
		if(null == listGermplasmsSelected) {
			return null;
		}
		
		boolean bGIDs = false;
		boolean bGerm = false;
		if(null != theSeletedFlapjackType2) {
			if(theSeletedFlapjackType2.startsWith("GID")) {
				bGIDs = true;
			} else {
				bGerm = true;
			}
		}
		String strMap = theMap;
		//List<String> listOfSelectedItems = new ArrayList<String>();
		List<Integer> listOfGIDs = new ArrayList<Integer>();
		for (int i = 0; i < listGermplasmsSelected.size(); i++) {
			String strMarkerName = listGermplasmsSelected.get(i);
			Integer integerGID = hmOfGNamesAndGids.get(strMarkerName);
			listOfGIDs.add(integerGID);
		}
		
		//listGermplasmsSelected
		try {
			List<Integer> listOfNewGids = new ArrayList<Integer>();
			List<String> listOfNVals = new ArrayList<String>();
			List<String> list = new ArrayList<String>();
			Hashtable<String, Integer> gListExp1 = new Hashtable<String, Integer>();
			List<Integer> gListExp = new ArrayList<Integer>();
			SortedMap<Integer, String> mapN = new TreeMap<Integer, String>();
			List<Integer> nidfromAccMetadataset = getNidfromAccMetadataset(listOfGIDs);
			//GET_ALLELE_COUNT_BY_GID
			long iCountOfAlleleCountByGID = getAlleleCountByGID(listOfGIDs);
			//GET_CHAR_COUNT_BY_GID
			long lGetCharCountByGID = getCharCountByGID(listOfGIDs);
			//GET_MAPPING_COUNT_BY_GID
			long lGetMappingCountByGID = getMappingCountByGID(listOfGIDs);
			List<AllelicValueElement> finalMappingValues = new ArrayList<AllelicValueElement>();
			List<AllelicValueElement> finalMappingValues2 = new ArrayList<AllelicValueElement>();
			
			List<AllelicValueElement> alleleValuesByMarkerNameElement = new ArrayList<AllelicValueElement>();
			if(0 < iCountOfAlleleCountByGID) {
				alleleValuesByMarkerNameElement = getMarkerData(listOfGIDs, listOfMarkersProvided);
				finalMappingValues.addAll(alleleValuesByMarkerNameElement);
			}
			
			List<AllelicValueElement> charValuesByMarkerNameElement = new ArrayList<AllelicValueElement>();
			if(0 < lGetCharCountByGID) {
				charValuesByMarkerNameElement = getMarkerData(listOfGIDs, listOfMarkersProvided);
				finalMappingValues.addAll(charValuesByMarkerNameElement); 
			}
			
			List<AllelicValueElement> mappingValuesByMarkerNameElement = new ArrayList<AllelicValueElement>();
			if(0 < lGetMappingCountByGID) {
				mappingValuesByMarkerNameElement = getMarkerData(listOfGIDs, listOfMarkersProvided);
				finalMappingValues.addAll(mappingValuesByMarkerNameElement);
				
				List<Integer> idsByNames = getMarkerIds();
				
				List<MappingValueElement> mappingPopValuesByGidsAndMarkerIds = getMappingPopValuesByGidsAndMarkerIds(listOfGIDs, idsByNames);
				
				boolean isExisting = false;
				
				Integer parentAGid = null; 
				Integer parentBGid = null;
				String strMappingType = "";
				String strMarkerType = "";
				List<Integer> listOfParents = new ArrayList<Integer>();
				for (MappingValueElement mappingValueElement : mappingPopValuesByGidsAndMarkerIds) {
					if(false == isExisting) {
						for (Integer integer : listOfGIDs) {
							if(mappingValueElement.getParentAGid().equals(integer) && mappingValueElement.getParentBGid().equals(integer)) {
								isExisting = true;
								parentAGid = mappingValueElement.getParentAGid();
								parentBGid = mappingValueElement.getParentBGid();
								break;
							}
						}
					}
					if(false == listOfParents.contains(mappingValueElement.getParentAGid())) {
						listOfParents.add(mappingValueElement.getParentAGid());
					}
					if(false == listOfParents.contains(mappingValueElement.getParentBGid())) {
						listOfParents.add(mappingValueElement.getParentBGid());
					}
					strMappingType = mappingValueElement.getMappingType();
					strMarkerType = mappingValueElement.getMarkerType();
				}
				
				if(isExisting) {
					if(true == (null != parentAGid && null != parentBGid)) {
						if(false == listOfGIDs.contains(parentAGid)) {
							listOfGIDs.add(parentAGid);
						}
						if(false == listOfGIDs.contains(parentBGid)) {
							listOfGIDs.add(parentBGid);
						}
					}
					
					List<Integer> nameIdsByGermplasmIds = getAccMetaDataSet(listOfParents);
					
						List<Name> namesByNameIds = getName(nameIdsByGermplasmIds);
						for (Name name : namesByNameIds) {
							
							if(false == listOfGIDs.contains(name.getGermplasmId())) {
								listOfGIDs.add(name.getGermplasmId());
							}
						}
						
				}
				
				List<AllelicValueElement> markerData = new ArrayList<AllelicValueElement>();
				
				if(strMappingType.equalsIgnoreCase("allelic")) {
					if(strMarkerType.equalsIgnoreCase("snp")){
						//"SELECT DISTINCT gdms_char_values.gid,gdms_char_values.char_value AS DATA,gdms_marker.marker_name 
						//FROM gdms_char_values,gdms_marker 
						//WHERE gdms_char_values.marker_id=gdms_marker.marker_id  
						//AND 
						//gdms_char_values.gid IN ("+parents+") 
						//AND 
						//gdms_char_values.marker_id IN (SELECT marker_id FROM gdms_marker WHERE marker_name IN ("+mlist1.substring(0,mlist1.length()-1)+")) ORDER BY gdms_char_values.gid, gdms_marker.marker_name");
						markerData = getMarkerData(listOfParents, listOfMarkersProvided);
						
					}else if((strMarkerType.equalsIgnoreCase("ssr"))||(strMarkerType.equalsIgnoreCase("DArT"))){
						//SELECT DISTINCT gdms_allele_values.gid,gdms_allele_values.allele_bin_value AS DATA,gdms_marker.marker_name 
						//FROM gdms_allele_values,gdms_marker WHERE gdms_allele_values.marker_id=gdms_marker.marker_id  AND gdms_allele_values.gid 
						//IN ("+parents+") AND gdms_allele_values.marker_id IN (SELECT marker_id FROM gdms_marker WHERE marker_name IN 
						//("+mlist1.substring(0,mlist1.length()-1)+")) ORDER BY gdms_allele_values.gid, gdms_marker.marker_name");
						if(0 < iCountOfAlleleCountByGID) {
							markerData = getMarkerData(listOfParents, listOfMarkersProvided);
						}
					}
					for (AllelicValueElement allelicValueElement : markerData) {
						finalMappingValues2.add(allelicValueElement);
					}
					
				}
				
				List<Name> listOfNames = getNames(nidfromAccMetadataset);
				for (Name name : listOfNames) {
					Integer germplasmId = name.getGermplasmId();
					if(false == listOfNewGids.contains(germplasmId)) {
						listOfNewGids.add(germplasmId);
					}
					
					if(bGerm ) {
						if(false == listOfNVals.contains(name.getNval())) {
							listOfNVals.add(name.getNval());
						}
						gListExp1.put(name.getNval(), name.getGermplasmId());
					} else {
						if(!(gListExp.contains(name.getGermplasmId())))
							gListExp.add(name.getGermplasmId());
					}
					mapN.put(name.getGermplasmId(), name.getNval());	
				}
				
				//System.out.println();
			}
			
			for (AllelicValueElement allelicValueElementallelic : finalMappingValues2) {
				for (AllelicValueElement allelicValueElementOld : finalMappingValues) {
					if(allelicValueElementallelic.getGid().equals(allelicValueElementOld.getGid())
							&& allelicValueElementallelic.getMarkerName().equals(allelicValueElementOld.getMarkerName())) {
						finalMappingValues.remove(allelicValueElementOld);
						finalMappingValues.add(allelicValueElementallelic);
					}
				}
			}

			
			for (AllelicValueElement allelicValueElement : finalMappingValues) {
				if(listOfGIDs.contains(allelicValueElement.getGid())) {
					if (bGerm) {
						list.add(mapN.get(allelicValueElement.getGid())+","+allelicValueElement.getMarkerName()+","+allelicValueElement.getData());	
						
					} else {
						//list.add(allelicValueElement.getGid()+","+allelicValueElement.getMarkerName()+","+allelicValueElement.getData());
					}
				}
			}
			
			
			String mapData = "";
			List<String> markersInMap = new ArrayList<String>();
			List<MappingData> mappingData = getMappingData(strMap);
			for (MappingData mapInfo : mappingData) {
				mapData=mapData+mapInfo.getMarkerName()+"!~!"+mapInfo.getLinkageGroup()+"!~!"+mapInfo.getStartPosition()+"~~!!~~";
				if(!markersInMap.contains(mapInfo.getMarkerName()))
					markersInMap.add(mapInfo.getMarkerName());
			}
			
			
			for (String mapInfo : listOfMarkersProvided) {
				if(false == markersInMap.contains(mapInfo)) {
					mapData=mapData+mapInfo+"!~!"+"unmapped"+"!~!"+"0"+"~~!!~~";
				}
			}
			
			//get QTl ids list details using list of gids
			List<Integer> listOfMapIds = getListOfMapIds(strMap);
			
			List<QtlDetails> qtlIdsListByListOfMapIds = getQTLIdsListByListOfMapIds(listOfMapIds);
			boolean bQTLPresent = false;
			if(null != qtlIdsListByListOfMapIds && 0 != qtlIdsListByListOfMapIds.size()) {
				bQTLPresent = true;
			}
			List<Qtl> qtlDetails = getQTLDAO(qtlIdsListByListOfMapIds);
			
			List<File> createDatFile = createDatFile(listOfMarkersProvided, listOfGIDs, bGIDs, qtlIdsListByListOfMapIds, qtlDetails, mapData, finalMappingValues, list, bQTLPresent);
			
			return createDatFile;
		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error while generating flapjack files.",  Notification.TYPE_ERROR_MESSAGE);
			return  null;
		}
		//return new ArrayList<File>();
	}


	private List<Name> getName(List<Integer> nameIdsByGermplasmIds)
			throws MiddlewareQueryException {
		List<Name> listToReturn = new ArrayList<Name>();
		List<Name> localName = getLocalName(nameIdsByGermplasmIds);
		if(null != localName) {
			listToReturn.addAll(localName);
		}
		List<Name> centralName = getCentralName(nameIdsByGermplasmIds);
		if(null != centralName) {
			listToReturn.addAll(centralName);
		}
		return listToReturn;
	}


	private List<Name> getCentralName(List<Integer> nameIdsByGermplasmIds) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(centralSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nameIdsByGermplasmIds);
		return namesByNameIds;
	}


	private List<Name> getLocalName(List<Integer> nameIdsByGermplasmIds)
			throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nameIdsByGermplasmIds);
		return namesByNameIds;
	}


	private List<Integer> getAccMetaDataSet(List<Integer> listOfParents)
			throws MiddlewareQueryException {
		List<Integer> listToReturn = new ArrayList<Integer>();
		List<Integer> localAccMetaDataSet = getLocalAccMetaDataSet(listOfParents);
		if(null != localAccMetaDataSet) {
			listToReturn.addAll(localAccMetaDataSet);
		}
		List<Integer> centralAccMetaDataSet = getCentralAccMetaDataSet(listOfParents);
		if(null != centralAccMetaDataSet) {
			for (Integer integer : centralAccMetaDataSet) {
				if(false == listToReturn.contains(integer)) {
					listToReturn.add(integer);
				}
			}
		}
		return listToReturn;
	}


	private List<Integer> getLocalAccMetaDataSet(List<Integer> listOfParents)
			throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		List<Integer> nameIdsByGermplasmIds = accMetadataSetDAO.getNameIdsByGermplasmIds(listOfParents);
		return nameIdsByGermplasmIds;
	}
	private List<Integer> getCentralAccMetaDataSet(List<Integer> listOfParents)
			throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);
		List<Integer> nameIdsByGermplasmIds = accMetadataSetDAO.getNameIdsByGermplasmIds(listOfParents);
		return nameIdsByGermplasmIds;
	}


	protected List<File> createDatFile(ArrayList<String> listOfMarkersProvided2, List<Integer> listOfGIDs2, 
			boolean isGIDSelected, List<QtlDetails> qtlIdsListByListOfMapIds, List<Qtl> qtlDetails, String mapData, List<AllelicValueElement> finalMappingValues, List<String> list, boolean bQTLPresent) {
		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println(absoluteFile);


		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		List<File> listOfFiles = new ArrayList<File>();
		String strAbsolutePath = fileExport.getAbsolutePath();
		//System.out.println(">>>>>" + strAbsolutePath);
		
		String strFJDatFileLink = strAbsolutePath + "\\Flapjack.dat";
		Link fjDatFileLink = new Link("View Flapjack.dat file", new ExternalResource(strFJDatFileLink));
		fjDatFileLink.setTargetName("_blank");
		File fileDatFile = new File(strFJDatFileLink);
		listOfFiles.add(fileDatFile);
		FileWriter flapjackdat;
		try {
			flapjackdat = new FileWriter(fileDatFile);
			
			BufferedWriter fjackdat = new BufferedWriter(flapjackdat);
			for (String string : listOfMarkersProvided2) {
				fjackdat.write("\t" + string);
			}
			
			if(false == isGIDSelected) {
//				fjackdat.write("\n");
				for (String strValue : list) {
					String[] strValues = strValue.split(",");
					fjackdat.write("\n" + strValues[0]);
					fjackdat.write("\t" + strValues[2]);
				}
			} else {
				for (Integer integer : listOfGIDs2) {
					fjackdat.write("\n" + integer);

					for (String string : listOfMarkersProvided2) {
						boolean bWrote = false;
						for (AllelicValueElement allelicValueElement : finalMappingValues) {
							if(false == allelicValueElement.getGid().equals(integer) && false == allelicValueElement.getMarkerName().equals(string)) {
								continue;
							}
							bWrote = true;
							String finalData = allelicValueElement.getData();
							fjackdat.write("\t"+finalData);
						}
						if(false == bWrote) {
							fjackdat.write("\t");
						}
					}
				}
			}
			fjackdat.close();
			
			//write map file.
			String strFlapjackMap = fileExport+("//")+"/Flapjack.map";
			File fileFlapjackMap = new File(strFlapjackMap);
			listOfFiles.add(fileFlapjackMap);
			FileWriter flapjackmapstream = new FileWriter(fileFlapjackMap);
			BufferedWriter fjackmap = new BufferedWriter(flapjackmapstream);
			String[] mData=mapData.split("~~!!~~");
			
			for(int m=0;m<mData.length;m++){		
				String[] strMData=mData[m].split("!~!");
				fjackmap.write(strMData[0]);
				fjackmap.write("\t");
				fjackmap.write(strMData[1]);
				fjackmap.write("\t");
				fjackmap.write(strMData[2]);
				fjackmap.write("\n");		
			}
			fjackmap.close();
			
			
			//write qtl
			String strFlapjackTxt = fileExport+("//")+"/Flapjack.txt";
			File fileFlapjackTxt = new File(strFlapjackTxt);
			listOfFiles.add(fileFlapjackTxt);
			FileWriter flapjackQTLstream = new FileWriter(fileFlapjackTxt);
			BufferedWriter fjackQTL = new BufferedWriter(flapjackQTLstream);
			if(bQTLPresent){
				//String[] qtlData=qtlData.split("~~!!~~");
				fjackQTL.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tfavallele\tFlanking markers in original publication\teffect");
				fjackQTL.write("\n");
				for (QtlDetails qtlDetails2 : qtlIdsListByListOfMapIds) {
					String strQTL = "";
					for (Qtl qtl : qtlDetails) {
						if(true == qtl.getQtlId().equals(qtlDetails2.getId().getQtlId())) {
							strQTL = qtl.getQtlName();
							break;
						}
					}
					fjackQTL.write(strQTL);
					fjackQTL.write("\t");

					//Chromosome
					String strChromosome = qtlDetails2.getLinkageGroup();
					fjackQTL.write(strChromosome);
					fjackQTL.write("\t");

					//Position
					String strPosition = String.valueOf(qtlDetails2.getPosition());
					fjackQTL.write(strPosition);
					fjackQTL.write("\t");

					//Minimum
					String strMinimum = String.valueOf(qtlDetails2.getMinPosition());
					fjackQTL.write(strMinimum);
					fjackQTL.write("\t");

					//Maximum
					String strMaximum = String.valueOf(qtlDetails2.getMaxPosition());
					fjackQTL.write(strMaximum);
					fjackQTL.write("\t");

					//Trait
					//String strTrait = qtlDetails2.getTrait();
					String strTrait = "";
					Integer iTraitId = qtlDetails2.getTraitId();
					if (null != iTraitId){
						/*TraitDAO traitDAOLocal = new TraitDAO();
						traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
						String traitFromLocal="";
						try {
							/*traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
							if (null != traitFromLocal){
								strTrait = traitFromLocal.getAbbreviation();
							} else {
								TraitDAO traitDAOCentral = new TraitDAO();
								traitDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
								Trait traitFromCentral = traitDAOCentral.getByTraitId(iTraitId);
								strTrait = traitFromCentral.getAbbreviation();
							}*/
							traitFromLocal=om.getStandardVariable(iTraitId).getName();
						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Trait objects", Notification.TYPE_ERROR_MESSAGE);
							return null;
						}
					}

					fjackQTL.write(strTrait);
					fjackQTL.write("\t");

					//Experiment
					String strExperiment = qtlDetails2.getExperiment();
					fjackQTL.write(strExperiment);
					fjackQTL.write("\t");

					//Trait Group
					String strTraitGroup = qtlDetails2.getExperiment();
					fjackQTL.write(strTraitGroup);
					fjackQTL.write("\t");

					//LOD
					String strLOD = String.valueOf(qtlDetails2.getScoreValue());
					fjackQTL.write(strLOD);
					fjackQTL.write("\t");

					//R2
					String strR2 = String.valueOf(qtlDetails2.getrSquare());
					fjackQTL.write(strR2);
					fjackQTL.write("\t");

					//favallele
					String strfavallele = qtlDetails2.getExperiment();
					fjackQTL.write(strfavallele);
					fjackQTL.write("\t");

					//Flanking markers in original publication
					String strFlankingmarkersinoriginalpublication = "";
					if(qtlDetails2.getLeftFlankingMarker().equals(qtlDetails2.getRightFlankingMarker())) {
						strFlankingmarkersinoriginalpublication = qtlDetails2.getLeftFlankingMarker();
					} else {
						strFlankingmarkersinoriginalpublication = qtlDetails2.getLeftFlankingMarker() + "/" + qtlDetails2.getRightFlankingMarker();
					}
					fjackQTL.write(strFlankingmarkersinoriginalpublication);
					fjackQTL.write("\t");

					//effect
					String streffect = String.valueOf(qtlDetails2.getEffect());
					fjackQTL.write(streffect);
					fjackQTL.write("\n");

				}
				fjackQTL.close();
			}
			
		} catch (IOException e) {
			return null;
		}
		
		return listOfFiles;
	}

	private List<MappingData> getMappingData(String strMap) throws MiddlewareQueryException {
		List<MappingData> listToReturn = new ArrayList<MappingData>();
		List<MappingData> localMappingData = getLocalMappingData(strMap);
		if(null != localMappingData) {
			listToReturn.addAll(localMappingData);
		}
		List<MappingData> centralMappingData = getCentralMappingData(strMap);
		if(null != centralMappingData) {
			listToReturn.addAll(centralMappingData);
		}
		return listToReturn;
	}


	private List<MappingData> getCentralMappingData(String strMap) throws MiddlewareQueryException {
		//return getMapInfoByMapName(strMap, centralSession);
		List<MappingData> listToReturn = new ArrayList<MappingData>();
		MappingDataDAO mappingDataDAO = new MappingDataDAO();
		mappingDataDAO.setSession(centralSession);
		List<MappingData> list = mappingDataDAO.getAll();
		for (MappingData mappingData : list) {
			if(mappingData.getMapName().equals(strMap)) {
				listToReturn.add(mappingData);
			}
		}
		return listToReturn;
	}


	private List<MappingData> getLocalMappingData(String strMap) throws MiddlewareQueryException {
		//return getMapInfoByMapName(strMap, localSession);
		List<MappingData> listToReturn = new ArrayList<MappingData>();
		MappingDataDAO mappingDataDAO = new MappingDataDAO();
		mappingDataDAO.setSession(localSession);
		List<MappingData> list = mappingDataDAO.getAll();
		for (MappingData mappingData : list) {
			if(mappingData.getMapName().equals(strMap)) {
				listToReturn.add(mappingData);
			}
		}
		return listToReturn;
	}

	private List<Name> getNames(List<Integer> nidfromAccMetadataset) throws MiddlewareQueryException {
		List<Name> listToReturn = new ArrayList<Name>();
		List<Name> localNames = getLocalNames(nidfromAccMetadataset);
		if(null != localNames) {
			listToReturn.addAll(localNames);
		}
		List<Name> centralNames = getCentralNames(nidfromAccMetadataset);
		if(null != centralNames) {
			listToReturn.addAll(centralNames);
		}
		return listToReturn;
	}


	private List<Name> getLocalNames(List<Integer> nidfromAccMetadataset) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		return nameDAO.getNamesByNameIds(nidfromAccMetadataset);
	}


	private List<Name> getCentralNames(List<Integer> nidfromAccMetadataset) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(centralSession);
		return nameDAO.getNamesByNameIds(nidfromAccMetadataset);
	}


	private List<MappingValueElement> getMappingPopValuesByGidsAndMarkerIds(List<Integer> listOfGids,
			List<Integer> idsByNames) throws MiddlewareQueryException {
		
		List<MappingValueElement> listToReturn = new ArrayList<MappingValueElement>();
		
		List<MappingValueElement> localMappingPopValuesByGidsAndMarkerIds = getLocalMappingPopValuesByGidsAndMarkerIds(listOfGids, idsByNames);
		if(null != localMappingPopValuesByGidsAndMarkerIds) {
			listToReturn.addAll(localMappingPopValuesByGidsAndMarkerIds);
		}
		List<MappingValueElement> centralMappingPopValuesByGidsAndMarkerIds = getCentralMappingPopValuesByGidsAndMarkerIds(listOfGids, idsByNames);
		if(null != centralMappingPopValuesByGidsAndMarkerIds) {
			listToReturn.addAll(centralMappingPopValuesByGidsAndMarkerIds);
		}
		return listToReturn;
	}


	private List<MappingValueElement> getCentralMappingPopValuesByGidsAndMarkerIds(
			List<Integer> listOfGids2, List<Integer> idsByNames) throws MiddlewareQueryException {
		MappingPopDAO mappingPopDAO = new MappingPopDAO();
		mappingPopDAO.setSession(localSession);
		return mappingPopDAO.getMappingValuesByGidAndMarkerIds(listOfGids2, idsByNames);
	}


	private List<MappingValueElement> getLocalMappingPopValuesByGidsAndMarkerIds(
			List<Integer> listOfGids, List<Integer> idsByNames)
			throws MiddlewareQueryException {
		MappingPopDAO mappingPopDAO = new MappingPopDAO();
		mappingPopDAO.setSession(localSession);
		return mappingPopDAO.getMappingValuesByGidAndMarkerIds(listOfGids, idsByNames);
	}


	private List<Integer> getMarkerIds() throws MiddlewareQueryException {
		List<Integer> listToReturn = new ArrayList<Integer>();
		List<Integer> localMarkerNames = getLocalMarkerIds();
		if(null != localMarkerNames) {
			listToReturn.addAll(localMarkerNames);
		}
		List<Integer> centralMarkerName = getCentralIdsName();
		if(null != centralMarkerName) {
			listToReturn.addAll(centralMarkerName);
		}
		return listToReturn;
	}
	


	private List<Integer> getCentralIdsName() throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(centralSession);
		List<Integer> idsByNames = markerDAO.getIdsByNames(listOfMarkersProvided, 0, (int)markerDAO.countAll());
		return idsByNames;
	}


	private List<Integer> getLocalMarkerIds() throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(localSession);
		List<Integer> idsByNames = markerDAO.getIdsByNames(listOfMarkersProvided, 0, (int)markerDAO.countAll());
		return idsByNames;
	}
	

	private List<AllelicValueElement> getMarkerData(List<Integer> listOfGIDs2,
			List<String> listOfMarkerName) throws MiddlewareQueryException {
		List<AllelicValueElement> listToReturn = new ArrayList<AllelicValueElement>();
		
		List<AllelicValueElement> localMarkerData = getLocalMarkerData(listOfGIDs2, listOfMarkerName);
		if(null != localMarkerData) {
			listToReturn.addAll(localMarkerData);
		}
		List<AllelicValueElement> centralMarkerData = getCentralMarkerData(listOfGIDs2, listOfMarkerName);
		if(null != centralMarkerData) {
			listToReturn.addAll(centralMarkerData);
		}
		return listToReturn;
	}


	private List<AllelicValueElement> getCentralMarkerData(List<Integer> listOfGIDs2,
			List<String> listOfMarkerName) throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(centralSession);
		List<AllelicValueElement> allelicValuesByGidsAndMarkerNames = markerDAO.getAllelicValuesByGidsAndMarkerNames(listOfGIDs2, listOfMarkerName);
		return allelicValuesByGidsAndMarkerNames;
	}


	private List<AllelicValueElement> getLocalMarkerData(
			List<Integer> listOfGIDs2, List<String> listOfMarkerName)
			throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(localSession);
		List<AllelicValueElement> allelicValuesByGidsAndMarkerNames = markerDAO.getAllelicValuesByGidsAndMarkerNames(listOfGIDs2, listOfMarkerName);
		return allelicValuesByGidsAndMarkerNames;
	}


	private long getMappingCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		long localMappingCountByGID = getLocalMappingCountByGID(listOfGIDs2);
		long centralMappingCountByGID = getCentralMappingCountByGID(listOfGIDs2);
		return localMappingCountByGID + centralMappingCountByGID;
	}


	private long getLocalMappingCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		try {
		 SQLQuery query = localSession.createSQLQuery(MappingPopValues.GET_MAPPING_COUNT_BY_GID);
         query.setParameterList("gIdList", listOfGIDs2);
         BigInteger mappingCount = (BigInteger) query.uniqueResult();
         return mappingCount.intValue();
		} catch (HibernateException e) {
            throw new MiddlewareQueryException("Error with getLocalMappingCountByGID(gids=" + listOfGIDs2 + ") query from MappingPopValues: " + e.getMessage(), e);
        }
	}


	private long getCentralMappingCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		try {
			 SQLQuery query = centralSession.createSQLQuery(MappingPopValues.GET_MAPPING_COUNT_BY_GID);
	         query.setParameterList("gIdList", listOfGIDs2);
	         BigInteger mappingCount = (BigInteger) query.uniqueResult();
	         return mappingCount.intValue();
			} catch (HibernateException e) {
	            throw new MiddlewareQueryException("Error with getLocalMappingCountByGID(gids=" + listOfGIDs2 + ") query from MappingPopValues: " + e.getMessage(), e);
	        }
		}


	private long getCharCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		long localCharCountByGID = getLocalCharCountByGID(listOfGIDs2);
		long centralCharCountByGID = getCentralCharCountByGID(listOfGIDs2);
		return localCharCountByGID + centralCharCountByGID;
	}


	private long getCentralCharCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		CharValuesDAO charValuesDAO = new CharValuesDAO();
		charValuesDAO.setSession(centralSession);
		return charValuesDAO.countCharValuesByGids(listOfGIDs2);
	}


	private long getLocalCharCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		CharValuesDAO charValuesDAO = new CharValuesDAO();
		charValuesDAO.setSession(localSession);
		return charValuesDAO.countCharValuesByGids(listOfGIDs2);
	}


	private long getAlleleCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		long localAlleleCountByGID = getLocalAlleleCountByGID(listOfGIDs2);
		long centralAlleleCountByGID = getCentralAlleleCountByGID(listOfGIDs2);
		return localAlleleCountByGID + centralAlleleCountByGID;
	}


	private long getLocalAlleleCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		AlleleValuesDAO alleleValuesDAO = new AlleleValuesDAO();
		alleleValuesDAO.setSession(localSession);
		return alleleValuesDAO.countAlleleValuesByGids(listOfGIDs2);
	}


	private long getCentralAlleleCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		AlleleValuesDAO alleleValuesDAO = new AlleleValuesDAO();
		alleleValuesDAO.setSession(centralSession);
		return alleleValuesDAO.countAlleleValuesByGids(listOfGIDs2);
	}


	private List<Qtl> getQTLDAO(List<QtlDetails> qtlIdsListByListOfMapIds) throws MiddlewareQueryException {
		List<Integer> listOfQLTIds = new ArrayList<Integer>();
		for (QtlDetails qtlDetails : qtlIdsListByListOfMapIds) {
			if(false == listOfQLTIds.contains(qtlDetails.getId().getQtlId())) {
				listOfQLTIds.add(qtlDetails.getId().getQtlId());
			}
		}
		return getQTLDAOByQTLIds(listOfQLTIds);
	}


	private List<Qtl> getQTLDAOByQTLIds(List<Integer> listOfQLTIds) throws MiddlewareQueryException {
		List<Qtl> listOfQTLDetailElement = new ArrayList<Qtl>();
		List<Qtl> localQTLDAO = getLocalQTLDAO(listOfQLTIds);
		if(null != localQTLDAO) {
			listOfQTLDetailElement.addAll(localQTLDAO);
		}
		List<Qtl> centralQTLDAO = getCentralQTLDAO(listOfQLTIds);
		if(null != centralQTLDAO) {
			listOfQTLDetailElement.addAll(centralQTLDAO);
		}
		return listOfQTLDetailElement;
	}


	private List<Qtl> getLocalQTLDAO(List<Integer> listOfQLTIds) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		List<Qtl> listToReturn = new ArrayList<Qtl>();
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			for (Integer integer : listOfQLTIds) {
				if(qtl.getDatasetId().equals(integer)) {
					listToReturn.add(qtl);
				}
			}
		}
		
		return listToReturn;
	}


	private List<Qtl> getCentralQTLDAO(List<Integer> listOfQLTIds) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		List<Qtl> listToReturn = new ArrayList<Qtl>();
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			for (Integer integer : listOfQLTIds) {
				if(qtl.getDatasetId().equals(integer)) {
					listToReturn.add(qtl);
				}
			}
		}
		
		return listToReturn;
	}


	private List<QtlDetails> getQTLIdsListByListOfMapIds(List<Integer> listOfMapIds) throws MiddlewareQueryException {
		List<QtlDetails> listOfQTLIds = new ArrayList<QtlDetails>();
		RetrieveQTL retrieveQTL = new RetrieveQTL();
		List<QtlDetails> retrieveQTLDetailsWithQTLDetailsPK = retrieveQTL.retrieveQTLDetailsWithQTLDetailsPK();
		for (QtlDetails qtlDetails : retrieveQTLDetailsWithQTLDetailsPK) {
			for (Integer integer : listOfMapIds) {
				if(qtlDetails.getId().getMapId().equals(integer)) {
					boolean bFound = false;
					for (QtlDetails qtlDetails2 : listOfQTLIds) {
						if(qtlDetails2.getId().getQtlId().equals(qtlDetails.getId().getQtlId())) {
							bFound = true;
						}
					}
					if(false == bFound) {
						listOfQTLIds.add(qtlDetails);
					}
				}
			}
		}
		return listOfQTLIds;
	}


	private List<Integer> getListOfMapIds(String strMap) throws MiddlewareQueryException {
		List<Integer> listOfMapIds = new ArrayList<Integer>();
		List<Map> mapDAO = getMapDAO();
		for (Map map : mapDAO) {
			if(map.getMapName().equals(strMap)) {
				if(false == listOfMapIds.contains(map.getMapId())) {
					listOfMapIds.add(map.getMapId());
				}
			}
		}
		return listOfMapIds;
	}


	private List<Map> getMapDAO() throws MiddlewareQueryException {
		List<Map> listToReturn = new ArrayList<Map>();
		List<Map> localMapDAO = getLocalMapDAO();
		if(null != localMapDAO) {
			listToReturn.addAll(localMapDAO);
		}
		List<Map> centralMapDAO = getCentralMapDAO();
		if(null != centralMapDAO) {
			listToReturn.addAll(centralMapDAO);
		}
		return listToReturn;
	}


	private List<Map> getCentralMapDAO() throws MiddlewareQueryException {
		MapDAO mapDAO = new MapDAO();
		mapDAO.setSession(centralSession);
		List<Map> all = mapDAO.getAll();
		return all;
	}


	private List<Map> getLocalMapDAO() throws MiddlewareQueryException {
		MapDAO mapDAO = new MapDAO();
		mapDAO.setSession(localSession);
		List<Map> all = mapDAO.getAll();
		return all;
	}

	
	private List<Integer> getNidfromAccMetadataset(List<Integer> integerGID) throws MiddlewareQueryException {
		List<Integer> listOfNids = new ArrayList<Integer>();
		List<Integer> localMetadataSetDAO = getLocalMetadataSetDAO(integerGID);
		if(null != localMetadataSetDAO) {
			for (Integer integer : localMetadataSetDAO) {
				if(false == listOfNids.contains(integer)) {
					listOfNids.add(integer);
				}
			}
		}
		List<Integer> centralMetadataSetDAO = getCentralMetadataSetDAO(integerGID);
		if(null != centralMetadataSetDAO) {
			for (Integer integer : centralMetadataSetDAO) {
				if(false == listOfNids.contains(integer)) {
					listOfNids.add(integer);
				}
			}
		}
		return listOfNids;
	}


	private List<Integer> getCentralMetadataSetDAO(List<Integer> integerGID)
			throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);
		return accMetadataSetDAO.getNameIdsByGermplasmIds(integerGID);
	}


	private List<Integer> getLocalMetadataSetDAO(List<Integer> integerGID) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		return accMetadataSetDAO.getNameIdsByGermplasmIds(integerGID);
	}


	private Component buildResultComponent() {
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
		    		
		    		File flapjackDatFile = generateFlagjackFiles.get(0);
		    		FileResource fileResource2 = new FileResource(flapjackDatFile, _mainHomePage);
		    		ZipEntry ze2 = new ZipEntry(fileResource2.getFilename());
		    		zos.putNextEntry(ze2);
		    		FileInputStream in = new FileInputStream(flapjackDatFile);
		    		while (-1 != in.read()) {
		    			zos.write(in.read());
		    		}
		    		
		    		File flapjackMapFile = generateFlagjackFiles.get(1);
		    		FileResource fileResource3 = new FileResource(flapjackMapFile, _mainHomePage);
		    		ZipEntry ze3 = new ZipEntry(fileResource3.getFilename());
		    		zos.putNextEntry(ze3);
		    		in = new FileInputStream(flapjackMapFile);
		    		while (-1 != in.read()) {
		    			zos.write(in.read());
		    		}

		    		File flapjackTextFile = generateFlagjackFiles.get(2);
		    		FileResource fileResource = new FileResource(flapjackTextFile, _mainHomePage);
		    		ZipEntry ze1 = new ZipEntry(fileResource.getFilename());
		    		zos.putNextEntry(ze1);
		    		in = new FileInputStream(flapjackTextFile);
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
//		final String strFJVisualizeLink = "D:/GDMSTwo/GDMS/WebContent/FileExports/flapjackrun.bat";
		Button btnVisualizeFJ = new Button("Visualize in Flapjack");
		btnVisualizeFJ.setStyleName(Reindeer.BUTTON_LINK);
		btnVisualizeFJ.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				File flapjackTextFile = new File(strFJVisualizeLink);
				//System.out.println(flapjackTextFile.exists());
				//String[] cmd = {"cmd.exe", "/c", "start", "\""+"flapjack"+"\"", strFJVisualizeLink};
				String[] cmd = {"cmd.exe", "/c", "start", strFJVisualizeLink};
				Runtime rt = Runtime.getRuntime();
				try {
					rt.exec(cmd);
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error occurred while trying to create Flapjack.flapjack project.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});

		
		/*String strGenotypicMatrixFile = _gdmsModel.getGenotypicMatrixFileName();
		String strGTPExcelFileLink = strAbsolutePath + strGenotypicMatrixFile;
		Link gtpExcelFileLink = new Link("Download Genotypic Matrix file", new ExternalResource(strGTPExcelFileLink));
		gtpExcelFileLink.setTargetName("_blank");*/
		
		Button btnDownloadMatrixFile = new Button("Download Matrix File");
		btnDownloadMatrixFile.setStyleName(Reindeer.BUTTON_LINK);
		btnDownloadMatrixFile.addListener(new Button.ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				_mainHomePage.getMainWindow().getWindow().open(new FileDownloadResource(
				matrixFile, _mainHomePage.getMainWindow().getWindow().getApplication()));
			}
		});


		HorizontalLayout layoutForExportTypes = new HorizontalLayout();
		layoutForExportTypes.setSpacing(true);

		ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		excelButton.addListener(this);
		if (null == strSelectedFormat) {
			layoutForExportTypes.addComponent(excelButton);
		}

		//20131210: Tulasi --- Not displaying the PDF and Print buttons
		/*themeResource = new ThemeResource("images/pdf.gif");
		Button pdfButton = new Button();
		pdfButton.setIcon(themeResource);
		pdfButton.setStyleName(Reindeer.BUTTON_LINK);
		pdfButton.setDescription("PDF Format");
		pdfButton.addListener(this);
		layoutForExportTypes.addComponent(pdfButton);

		themeResource = new ThemeResource("images/print.gif");
		Button printButton = new Button();
		printButton.setIcon(themeResource);
		printButton.setStyleName(Reindeer.BUTTON_LINK);
		printButton.setDescription("Print Format");
		printButton.addListener(this);
		layoutForExportTypes.addComponent(printButton);*/ 
		//20131210: Tulasi --- Not displaying the PDF and Print buttons

		//20131216: Added link to download Similarity Matrix File
		Button similarityMatrixButton = new Button("Similarity Matrix File");
		similarityMatrixButton.setStyleName(Reindeer.BUTTON_LINK);
		similarityMatrixButton.setDescription("Similarity Matrix File");
		similarityMatrixButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				File similarityMatrixFile = new File(""); //TODO: Have to provide the File location
				FileResource fileResource = new FileResource(similarityMatrixFile, _mainHomePage);
				_mainHomePage.getMainWindow().getWindow().open(fileResource, "Similarity Matrix File", true);
			}
		});
		//20131216: Added link to download Similarity Matrix File
				
		if (null != strSelectedFormat){
			if (strSelectedFormat.equals("Flapjack")){
				if(null != generateFlagjackFiles) {
					//resultsLayout.addComponent(btnFJDat);
					//resultsLayout.addComponent(btnFJMap);
					//resultsLayout.addComponent(btnFJText);
					resultsLayout.addComponent(btnAllFlapjackFiles);
					resultsLayout.addComponent(btnVisualizeFJ);
					resultsLayout.addComponent(similarityMatrixButton);
				}
			} else if (strSelectedFormat.equals("Matrix")) {
				resultsLayout.addComponent(btnDownloadMatrixFile);
			}
		} else {
			excelButton.setEnabled(false);
			//pdfButton.setEnabled(false);
			//printButton.setEnabled(false);
		}
		if(false == (null != strSelectedFormat && strSelectedFormat.equals("Flapjack"))) {
			resultsLayout.addComponent(layoutForExportTypes);
			resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		}
		
		return resultsLayout;
	}


	protected File createFlapjackTextFile() {
		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println(absoluteFile);

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		String strAbsolutePath = fileExport.getAbsolutePath();
		//System.out.println(">>>>>" + strAbsolutePath);

		String strFJTextFileLink = strAbsolutePath + "/Flapjack.txt";
		Link fjTextFileLink = new Link("View Flapjack.txt file", new ExternalResource(strFJTextFileLink));
		fjTextFileLink.setTargetName("_blank");

		return null;
	}


	protected File createFlapjackMapFile() {
		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println(absoluteFile);

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}

		String strAbsolutePath = fileExport.getAbsolutePath();
		//System.out.println(">>>>>" + strAbsolutePath);

		String strFJMapFileLink = strAbsolutePath + "/Flapjack.map";
		Link fjMapFileLink = new Link("View Flapjack.map file", new ExternalResource(strFJMapFileLink));
		fjMapFileLink.setTargetName("_blank");

		return null;
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}
}
