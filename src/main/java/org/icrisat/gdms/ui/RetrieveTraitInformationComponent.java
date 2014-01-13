package org.icrisat.gdms.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.write.WriteException;

import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.retrieve.RetrieveQTL;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;


public class RetrieveTraitInformationComponent implements Component.Listener {

	private static final long serialVersionUID = 1L;

	private TabSheet _tabsheetForTrait;
	private Component buildTraitResultsComponent;
	private GDMSMain _mainHomePage;
	//private List<Integer> listOfQTLIdsByTrait;
	private List<QtlDetailElement> listOfQTLDetailsByQTLIDs;


	private Table _qtlTable;

	private Table _tableWithAllTraits;
	ManagerFactory factory=null;
	OntologyDataManager om;

	public RetrieveTraitInformationComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		om=factory.getOntologyDataManager();
	}

	/**
	 * 
	 * Building the entire Tabbed Component required for Trait data
	 * 
	 */
	public HorizontalLayout buildTabbedComponentForTrait() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();

		_tabsheetForTrait = new TabSheet();
		_tabsheetForTrait.setWidth("700px");

		Component buildTraitSearchComponent = buildMapSearchComponent();

		buildTraitResultsComponent = buildTraitResultsComponent();
		
		buildTraitSearchComponent.setSizeFull();
		buildTraitResultsComponent.setSizeFull();
		

		_tabsheetForTrait.addComponent(buildTraitSearchComponent);
		_tabsheetForTrait.addComponent(buildTraitResultsComponent);
		
		_tabsheetForTrait.getTab(1).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForTrait);

		return horizontalLayout;
	}


	private Component buildMapSearchComponent() {
		VerticalLayout searchTraitsLayout = new VerticalLayout();
		searchTraitsLayout.setCaption("Search");
		searchTraitsLayout.setMargin(true, true, true, true);
		searchTraitsLayout.setSpacing(true);

		Label lblSearch = new Label("Search Traits");
		lblSearch.setStyleName(Reindeer.LABEL_H2);
		searchTraitsLayout.addComponent(lblSearch);
		searchTraitsLayout.setComponentAlignment(lblSearch, Alignment.TOP_CENTER);

		Label lblMAPNames = new Label("Trait Names");
		lblMAPNames.setStyleName(Reindeer.LABEL_SMALL);

		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("500px");
		txtFieldSearch.setNullRepresentation("");

		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);

		//final GridLayout gridLayout = new GridLayout();
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		
		searchButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				String strSearchString = txtFieldSearch.getValue().toString();
				
				if (strSearchString.trim().equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a search string.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (false == strSearchString.endsWith("*")){
					if (false == strSearchString.equals(""))
						strSearchString = strSearchString + "*";
				}
				if(strSearchString.equals("*")) {
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				} else if(strSearchString.endsWith("*")) {
					strSearchString = strSearchString.substring(0, strSearchString.length() - 1);
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				} else {
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				}
				
				if (null == _tableWithAllTraits){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no Traits to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (null != _tableWithAllTraits && 0 == _tableWithAllTraits.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no Traits to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		
		
		HorizontalLayout layoutForTextSearch = new HorizontalLayout();
		layoutForTextSearch.setSpacing(true);
		layoutForTextSearch.addComponent(lblMAPNames);
		layoutForTextSearch.addComponent(txtFieldSearch);
		layoutForTextSearch.addComponent(searchButton);
		searchTraitsLayout.addComponent(layoutForTextSearch);
		searchTraitsLayout.setMargin(true, true, true, true);

		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				listOfQTLDetailsByQTLIDs = new ArrayList<QtlDetailElement>();
				String strTraitName = txtFieldSearch.getValue().toString();
				List<String> listOfTraitNames = new ArrayList<String>();

				if(false == strTraitName.equals("*") && false == strTraitName.endsWith("*")) {
					/*if(null != arrayOfCheckBoxes && 0 != arrayOfCheckBoxes.length) {
						for (int i = 0; i < arrayOfCheckBoxes.length; i++) {
							CheckBox checkBox = arrayOfCheckBoxes[i];
							if(checkBox.booleanValue()) {
								listOfTrailNames.add(checkBox.getCaption());
							}
						}
						if(0 != strTraitName.trim().length()) {
							if(false == listOfTrailNames.contains(strTraitName)) {
								listOfTrailNames.add(strTraitName);
							}
						}
					}*/
					
					int iNumOfQTLs = _tableWithAllTraits.size();
					for (int i = 0; i < iNumOfQTLs; i++) {
						Item item = _tableWithAllTraits.getItem(new Integer(i));
						Property itemProperty = item.getItemProperty("Select");
						CheckBox checkBox = (CheckBox) itemProperty.getValue();
						if (checkBox.booleanValue() == true) {
							String strSelectedQTL = item.getItemProperty("QTL Name").toString();
							listOfTraitNames.add(strSelectedQTL);
						}
					}
				}

					try {
						if (null != strTraitName && (false == strTraitName.equals(""))){

							if (strTraitName.equals("*")){
								getAllTraitDetails();
							} else if(strTraitName.endsWith("*")) {
								getAllTraitDetailsStartsWith(strTraitName);
							} else {
								for (String string : listOfTraitNames) {
									if(false == string.equals("*") && false == string.endsWith("*")) {
										RetrieveQTL retrieveQTL = new RetrieveQTL();
										List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(string);
										if(null != retrieveTrait) {
											listOfQTLDetailsByQTLIDs.addAll(retrieveTrait);
										}
									}
								}
							}

						} else {
							for (String string : listOfTraitNames) {
								if(false == string.equals("*") && false == string.endsWith("*")) {
									RetrieveQTL retrieveQTL = new RetrieveQTL();
									List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(string);
									if(null != retrieveTrait) {
										listOfQTLDetailsByQTLIDs.addAll(retrieveTrait);
									}
								}
							}
						}

					} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Trait Details.",  Notification.TYPE_ERROR_MESSAGE);
						return;
					}

					if (0 == listOfQTLDetailsByQTLIDs.size()){
						_mainHomePage.getMainWindow().getWindow().showNotification("No Traits to be displayed.",  Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				Component newTraitResultsPanel = buildTraitResultsComponent();
				_tabsheetForTrait.replaceComponent(buildTraitResultsComponent, newTraitResultsPanel);
				_tabsheetForTrait.requestRepaint();
				buildTraitResultsComponent = newTraitResultsPanel;
				_tabsheetForTrait.getTab(1).setEnabled(true);
				_tabsheetForTrait.setSelectedTab(1);
			}

			private void getAllTraitDetailsStartsWith(String strTraitName)
					throws MiddlewareQueryException {
				RetrieveQTL retrieveQTL = new RetrieveQTL();
				strTraitName = strTraitName.substring(0, strTraitName.length() - 1);
				QtlDetailElement retrieveTraitNameStartWith = retrieveQTL.retrieveTraitNameStartWith(strTraitName);
				if(null != retrieveTraitNameStartWith) {
					String strTrait = "";
					Integer iTraitId = retrieveTraitNameStartWith.getTraitId();
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					
					//List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(retrieveTraitNameStartWith.getTrait());
					List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(strTrait);
					if(null != retrieveTrait) {
						listOfQTLDetailsByQTLIDs.addAll(retrieveTrait);
					}
				}
			}

			private void getAllTraitDetails() throws MiddlewareQueryException {
				RetrieveQTL retrieveQTL = new RetrieveQTL();
				List<String> retrieveTraitNames = new ArrayList<String>();
				List<QtlDetailElement> retrieveQTLDetails = retrieveQTL.retrieveQTLDetails();
				for (QtlDetailElement qtlDetailElement : retrieveQTLDetails) {
					//String strTrait = qtlDetailElement.getTrait();
					String strTrait = "";
					Integer iTraitId = qtlDetailElement.getTraitId();
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(null != strTrait && false == retrieveTraitNames.contains(strTrait)) {
						retrieveTraitNames.add(strTrait);
					}
				}
				
				for (String string : retrieveTraitNames) {
					List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(string);
					if(null != retrieveTrait) {
						listOfQTLDetailsByQTLIDs.addAll(retrieveTrait);
					}
				}
			}
		});

		//searchTraitsLayout.addComponent(gridLayout);
		searchTraitsLayout.addComponent(horizontalLayout);
		
		searchTraitsLayout.addComponent(layoutForButton);
		searchTraitsLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);

		return searchTraitsLayout;
	}


	private Component buildTraitResultsComponent() {
		VerticalLayout verticalLayout = new VerticalLayout();
		
		VerticalLayout resultsLayout = new VerticalLayout();
		verticalLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);
		resultsLayout.setMargin(true);
		resultsLayout.setWidth("700px");
		
		int iNumOfTraitssFound = 0;
		if (null != listOfQTLDetailsByQTLIDs){
			iNumOfTraitssFound = listOfQTLDetailsByQTLIDs.size();
		}

		Label lblMAPsFound = new Label(iNumOfTraitssFound + " Trait's Found");
		lblMAPsFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayout.addComponent(lblMAPsFound);
		resultsLayout.setComponentAlignment(lblMAPsFound, Alignment.TOP_CENTER);

		if (0 != iNumOfTraitssFound){
			Table tableForMAPResults = buildmapTable();
			tableForMAPResults.setWidth("100%");
			tableForMAPResults.setPageLength(10);
			tableForMAPResults.setSelectable(true);
			tableForMAPResults.setColumnCollapsingAllowed(true);
			tableForMAPResults.setColumnReorderingAllowed(true);
			tableForMAPResults.setStyleName("strong");
			resultsLayout.addComponent(tableForMAPResults);
			resultsLayout.setComponentAlignment(tableForMAPResults, Alignment.MIDDLE_CENTER);
		}

		HorizontalLayout layoutForExportTypes = new HorizontalLayout();
		layoutForExportTypes.setSpacing(true);

		ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		excelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				List<String[]> listOfData = new ArrayList<String[]>();
				if (null != listOfQTLDetailsByQTLIDs){

					for (int i = 0; i < listOfQTLDetailsByQTLIDs.size(); i++){

						QtlDetailElement qtlDetailElement = listOfQTLDetailsByQTLIDs.get(i);

						final String strQTLName = qtlDetailElement.getQtlName();
						String strMapName = qtlDetailElement.getMapName();
						final String strChromosome = qtlDetailElement.getChromosome();
						final Float fMinPosition = qtlDetailElement.getMinPosition();
						final Float fMaxPosition = qtlDetailElement.getMaxPosition();
						
						//String strTrait = qtlDetailElement.getTrait();
						String strTrait = "";
						Integer iTraitId = qtlDetailElement.getTraitId();
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
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						String strExperiment = qtlDetailElement.getExperiment();
						String strLeftFlankingMarker = qtlDetailElement.getLeftFlankingMarker();
						String strRightFlankingMarker = qtlDetailElement.getRightFlankingMarker();
						Integer iEffect = qtlDetailElement.getEffect();
						Float fScoreValue = qtlDetailElement.getScoreValue();
						Float fRSquare = qtlDetailElement.getRSquare();
						String strInteractions = qtlDetailElement.getInteractions();
						String strTRName = qtlDetailElement.getTRName();
						String strOntology = qtlDetailElement.getOntology();

						String strCropOntologyLink = "http://www.cropontology.org/terms/" + strOntology + "/Harvest";
						Link linkCropOntologySite = new Link(strTrait, new ExternalResource(strCropOntologyLink));
						linkCropOntologySite.setTargetName("_blank");
						

						String strCMapLink = "http://cmap.icrisat.ac.in/cgi-bin/cmap_public/" + "feature_search?features=" +
								            strQTLName + "&order_by=&data_source=CMAP_PUBLIC";
						Link linkCMap = new Link("CMap", new ExternalResource(strCMapLink));
						linkCMap.setTargetName("_blank");

						
						listOfData.add(new String[] {strQTLName, strMapName, strChromosome, String.valueOf(fMinPosition), String.valueOf(fMaxPosition),
								strCropOntologyLink, strExperiment, strLeftFlankingMarker, strRightFlankingMarker, String.valueOf(iEffect), String.valueOf(fScoreValue), String.valueOf(fRSquare), 
								strInteractions, strTRName, strOntology, strCMapLink});
					}
					String[] strArrayOfColNames = {"QTl-NAME", "MAP-NAME", "CHROMOSOME", "MIN-POSITION", "MAX-POSITION",
							"TRAIT", "EXPERIMENT", "LM", "RM", "EFFECT", "SCORE-VALUE",
							"R-SQUARE", "INTERACTIONS", "TR-NAME", "ONTOLOGY", "VISUALIZE"};
					listOfData.add(0, strArrayOfColNames);
					ExportFileFormats exportFileFormats = new ExportFileFormats();
					try {
						exportFileFormats.exportMap(_mainHomePage, listOfData, "tmp");
					} catch (WriteException e) {
						
					} catch (IOException e) {
						
					}
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

			@Override
			public void buttonClick(ClickEvent event) {
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				exportFileFormats.exportToPdf(_qtlTable, _mainHomePage);
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
		//20131210: Tulasi --- Not displaying the PDF and Print buttons

		if (0 == iNumOfTraitssFound){
			//pdfButton.setEnabled(false);
			excelButton.setEnabled(false);
			//printButton.setEnabled(false);
		}
		resultsLayout.addComponent(layoutForExportTypes);
		resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);

		verticalLayout.addComponent(resultsLayout);
		return verticalLayout;
	}

	private Table buildmapTable() {
		_qtlTable = new Table();
		//_qtlTable.setStyleName("markertable");
		_qtlTable.setPageLength(10);
		_qtlTable.setSelectable(true);
		_qtlTable.setColumnCollapsingAllowed(true);
		_qtlTable.setColumnReorderingAllowed(true);

		String[] strArrayOfColNames = {"QTl-NAME", "MAP-NAME", "CHROMOSOME", "MIN-POSITION", "MAX-POSITION",
				"TRAIT", "EXPERIMENT", "LM", "RM", "EFFECT", "SCORE-VALUE",
				"R-SQUARE", "INTERACTIONS", "TR-NAME", "ONTOLOGY", "VISUALIZE"};


		for (int i = 0; i < strArrayOfColNames.length; i++){
			if (0 == i){
				_qtlTable.addContainerProperty(strArrayOfColNames[i], Button.class, null);
			} else if (5 == i || 15 == i){
				_qtlTable.addContainerProperty(strArrayOfColNames[i], Link.class, null);
			} else {
				_qtlTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
			}
			//_qtlTable.setColumnWidth(strArrayOfColNames[i], 30);
		}

		
		if (null != listOfQTLDetailsByQTLIDs){

			for (int i = 0; i < listOfQTLDetailsByQTLIDs.size(); i++){

				QtlDetailElement qtlDetailElement = listOfQTLDetailsByQTLIDs.get(i);

				final String strQTLName = qtlDetailElement.getQtlName();
				String strMapName = qtlDetailElement.getMapName();
				final String strChromosome = qtlDetailElement.getChromosome();
				final Float fMinPosition = qtlDetailElement.getMinPosition();
				final Float fMaxPosition = qtlDetailElement.getMaxPosition();
				
				//String strTrait = qtlDetailElement.getTrait();
				String strTrait = "";
				Integer iTraitId = qtlDetailElement.getTraitId();
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				String strExperiment = qtlDetailElement.getExperiment();
				String strLeftFlankingMarker = qtlDetailElement.getLeftFlankingMarker();
				String strRightFlankingMarker = qtlDetailElement.getRightFlankingMarker();
				Integer iEffect = qtlDetailElement.getEffect();
				Float fScoreValue = qtlDetailElement.getScoreValue();
				Float fRSquare = qtlDetailElement.getRSquare();
				String strInteractions = qtlDetailElement.getInteractions();
				String strTRName = qtlDetailElement.getTRName();
				String strOntology = qtlDetailElement.getOntology();

				Button qtlNameLink = new Button();
				qtlNameLink.setCaption(strQTLName);
				qtlNameLink.setStyleName(Reindeer.BUTTON_LINK);
				qtlNameLink.setDescription(strQTLName);
				qtlNameLink.addListener(new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						QtlDetailsDAO qtlDetailsDAO = new QtlDetailsDAO();
						qtlDetailsDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());

						String qtlName = strQTLName;
						String chromosome = strChromosome;
						int intMinValue = fMinPosition.intValue();
						int intMaxValue = fMaxPosition.intValue();

						try {

							List<Integer> markerIdsByQtl = qtlDetailsDAO.getMarkerIdsByQtl(qtlName, chromosome, intMinValue, intMaxValue);
							MarkerDAO markerDAO = new MarkerDAO();
							markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
							long lCntMarkersByIds = markerDAO.countMarkersByIds(markerIdsByQtl);
							List<Marker> listOfMarkersByIds = markerDAO.getMarkersByIds(markerIdsByQtl, 0, (int)lCntMarkersByIds);

							String strMarkers = "";

							if (null == listOfMarkersByIds){
								_mainHomePage.getMainWindow().getWindow().showNotification("Markers could not be obtained for the selected QTL", Notification.TYPE_ERROR_MESSAGE);
								return;
							} else if (0 == listOfMarkersByIds.size()){
								_mainHomePage.getMainWindow().getWindow().showNotification("There are no Markers the selected QTL", Notification.TYPE_ERROR_MESSAGE);
								return;
							} 

							for (int i = 0; i < listOfMarkersByIds.size(); i++){
								Marker marker = listOfMarkersByIds.get(i);
								strMarkers += marker.getMarkerName();
							}

							Window messageWindow = new Window("Marker Names");
							GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage, strMarkers);
							messageWindow.addComponent(gdmsMessageWindow);
							messageWindow.setWidth("400px");
							messageWindow.setBorder(Window.BORDER_NONE);
							messageWindow.setClosable(true);
							messageWindow.center();

							if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
								_mainHomePage.getMainWindow().addWindow(messageWindow);
							} 

						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
							return;
						}						
					}
				});

				String strCropOntologyLink = "http://www.cropontology.org/terms/" + strOntology + "/Harvest";
				Link linkCropOntologySite = new Link(strTrait, new ExternalResource(strCropOntologyLink));
				linkCropOntologySite.setTargetName("_blank");
				

				String strCMapLink = "http://cmap.icrisat.ac.in/cgi-bin/cmap_public/" + "feature_search?features=" +
						            strQTLName + "&order_by=&data_source=CMAP_PUBLIC";
				Link linkCMap = new Link("CMap", new ExternalResource(strCMapLink));
				linkCMap.setTargetName("_blank");

				
				_qtlTable.addItem(new Object[] {qtlNameLink, strMapName, strChromosome, fMinPosition, fMaxPosition,
						linkCropOntologySite, strExperiment, strLeftFlankingMarker, strRightFlankingMarker, iEffect, fScoreValue, fRSquare, 
						strInteractions, strTRName, strOntology, linkCMap}, new Integer(i));
			}
		}

		return _qtlTable;
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}
	
	/*private void buildOnLoad(final GridLayout gridLayout, String theSearchString) {
		gridLayout.removeAllComponents();
		gridLayout.setSpacing(true);
		RetrieveQTL retrieveQTL = new RetrieveQTL();
		List<String> retrieveTraitNames = new ArrayList<String>();
		try {
			List<QtlDetailElement> retrieveQTLDetails = retrieveQTL.retrieveQTLDetails();
			for (QtlDetailElement qtlDetailElement : retrieveQTLDetails) {
				//String strTrait = qtlDetailElement.getTrait();
				String strTrait = "";
				Integer iTraitId = qtlDetailElement.getTraitId();
				if (null != iTraitId){
					TraitDAO traitDAOLocal = new TraitDAO();
					traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
					Trait traitFromLocal;
					try {
						traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
						if (null != traitFromLocal){
							strTrait = traitFromLocal.getAbbreviation();
						} else {
							TraitDAO traitDAOCentral = new TraitDAO();
							traitDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
							Trait traitFromCentral = traitDAOCentral.getByTraitId(iTraitId);
							strTrait = traitFromCentral.getAbbreviation();
						}
					} catch (MiddlewareQueryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(null != strTrait && false == retrieveTraitNames.contains(strTrait)) {
					retrieveTraitNames.add(strTrait);
				}
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			return;
		}
		

		
		List<String> retrieveQTLNamesFinal = new ArrayList<String>();
		
		if(null != theSearchString && false == theSearchString.equals("*")) {
			//theSearchString = theSearchString.substring(0, theSearchString.length() - 1);
			for (int i = 0; i < retrieveTraitNames.size(); i++) {
				String string = retrieveTraitNames.get(i);
				if(true == string.startsWith(theSearchString)) {
					retrieveQTLNamesFinal.add(string);
				}
			}
		} else {
			for (int i = 0; i < retrieveTraitNames.size(); i++) {
				retrieveQTLNamesFinal.add(retrieveTraitNames.get(i));
			}
		}

		gridLayout.setColumns(1);
		int rowCount = retrieveQTLNamesFinal.size();
		if(rowCount == 0) {
			rowCount = 1;
		}
		gridLayout.setRows(rowCount);
		int iCounter = 0;
		arrayOfCheckBoxes = new CheckBox[retrieveQTLNamesFinal.size()];
		int i = 0;
		for (String string : retrieveQTLNamesFinal) {
			arrayOfCheckBoxes[iCounter] = new CheckBox(string);
			arrayOfCheckBoxes[iCounter].setImmediate(true);
			gridLayout.addComponent(arrayOfCheckBoxes[i], 0, i);
			iCounter++;
			i++;
		}
	}*/

	private void buildOnLoad(final HorizontalLayout horizontalLayout, String theSearchString) {
		horizontalLayout.removeAllComponents();
		horizontalLayout.setSpacing(true);
		RetrieveQTL retrieveQTL = new RetrieveQTL();
		List<String> retrieveTraitNames = new ArrayList<String>();
		try {
			List<QtlDetailElement> retrieveQTLDetails = retrieveQTL.retrieveQTLDetails();
			for (QtlDetailElement qtlDetailElement : retrieveQTLDetails) {
				//String strTrait = qtlDetailElement.getTrait();
				String strTrait = "";
				Integer iTraitId = qtlDetailElement.getTraitId();
				if (null != iTraitId){
					/*TraitDAO traitDAOLocal = new TraitDAO();
					traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
					String traitFromLocal="";;
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(null != strTrait && false == retrieveTraitNames.contains(strTrait)) {
					retrieveTraitNames.add(strTrait);
				}
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			return;
		}
		

		
		List<String> retrieveQTLNamesFinal = new ArrayList<String>();
		
		if(null != theSearchString && false == theSearchString.equals("*")) {
			//theSearchString = theSearchString.substring(0, theSearchString.length() - 1);
			for (int i = 0; i < retrieveTraitNames.size(); i++) {
				String string = retrieveTraitNames.get(i);
				if(true == string.startsWith(theSearchString)) {
					retrieveQTLNamesFinal.add(string);
				}
			}
		} else {
			for (int i = 0; i < retrieveTraitNames.size(); i++) {
				retrieveQTLNamesFinal.add(retrieveTraitNames.get(i));
			}
		}

		/*horizontalLayout.setColumns(1);
		int rowCount = retrieveQTLNamesFinal.size();
		if(rowCount == 0) {
			rowCount = 1;
		}
		horizontalLayout.setRows(rowCount);
		int iCounter = 0;
		arrayOfCheckBoxes = new CheckBox[retrieveQTLNamesFinal.size()];
		int i = 0;
		for (String string : retrieveQTLNamesFinal) {
			arrayOfCheckBoxes[iCounter] = new CheckBox(string);
			arrayOfCheckBoxes[iCounter].setImmediate(true);
			horizontalLayout.addComponent(arrayOfCheckBoxes[i], 0, i);
			iCounter++;
			i++;
		}*/
		
		if (0 < retrieveQTLNamesFinal.size()) {
			_tableWithAllTraits = new Table();
			_tableWithAllTraits.setSizeFull();
			_tableWithAllTraits.setPageLength(5);
			_tableWithAllTraits.setSelectable(false);
			_tableWithAllTraits.setColumnCollapsingAllowed(false);
			_tableWithAllTraits.setColumnReorderingAllowed(false);
			_tableWithAllTraits.setEditable(false);
			_tableWithAllTraits.setStyleName("strong");
			horizontalLayout.addComponent(_tableWithAllTraits);
			
			_tableWithAllTraits.addContainerProperty("Select", CheckBox.class, null);
			_tableWithAllTraits.addContainerProperty("Trait Name", String.class, null);
			_tableWithAllTraits.setColumnWidth("Select", 40);
			_tableWithAllTraits.setColumnWidth("Trait Name", 500);
			
			int i = 0;
			for (String strQTLName : retrieveQTLNamesFinal) {
				_tableWithAllTraits.addItem(new Object[]{new CheckBox(), strQTLName}, new Integer(i));
				i++;
			}
		}
	}
}
