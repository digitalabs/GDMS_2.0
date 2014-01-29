package org.icrisat.gdms.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jxl.write.WriteException;

import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.MarkerDetailsDAO;
import org.generationcp.middleware.dao.gdms.MarkerUserInfoDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerDetails;
import org.generationcp.middleware.pojos.gdms.MarkerUserInfo;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.retrieve.marker.RetrieveMarker;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;


public class RetrieveMarkerInformationComponent implements Component.Listener {


	private static final long serialVersionUID = 1L;


	private TabSheet _tabsheetForMarkers;
	private Component buildConditionsPanel;
	private Component buildResultsPanel;
	private String strMarkerField = "";
	private String strCondition = "";
	private String strValue = "";
	private int iConditonCnt = 0;
	private RetrieveMarker retrieveMarker;
	private ComboBox selectFieldName;
	private ComboBox selectFieldValue;
	private ComboBox containsField;
	private ArrayList<String> listOfSelectedFields = new ArrayList<String>();
	private Table conditionsTable;
	private MarkerDAO markerDAOLocal;
	private ArrayList<Item> listOfSelectedCriteria;
	private MarkerUserInfoDAO markerUserInfoDAOLocal;
	private MarkerDetailsDAO markerDetailsDAOLocal;
	private GDMSMain _mainHomePage;
	protected String strMarkerType;
	private Session localSession;
	private Session centralSession;
	private MarkerDAO markerDAOCentral;
	private MarkerUserInfoDAO markerUserInfoDAOCentral;
	private MarkerDetailsDAO markerDetailsDAOCentral;


	//private Table _markerTable;


	private ArrayList<Marker> _finalListOfMarkers;


	private ArrayList<MarkerUserInfo> _finalListOfMarkerInfo;


	private ArrayList<MarkerDetails> _finalListOfMarkerDetails;


	private Table[] arrayOfTables;




	private ArrayList<String> finalListOfMarkerTypes;


	protected int iTableCounter;
	
	ManagerFactory factory;
	ManagerFactory factoryD;
	
	GermplasmDataManager germManager;
	GenotypicDataManager genoManager;
	protected File orderFormForPlymorphicMarkers;

	public RetrieveMarkerInformationComponent(GDMSMain theMainHomePage) throws GDMSException{

		_mainHomePage = theMainHomePage;
		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			/*if(localSession==null){
				localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionFactoryForLocal().getCurrentSession();
			}*/
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			/*if(centralSession==null){
				centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionFactoryForCentral().getCurrentSession();
			}*/
			
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		/*localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		*/
		markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);

		markerUserInfoDAOLocal = new MarkerUserInfoDAO();
		markerUserInfoDAOLocal.setSession(localSession);
		markerUserInfoDAOCentral = new MarkerUserInfoDAO();
		markerUserInfoDAOCentral.setSession(centralSession);

		markerDetailsDAOLocal = new MarkerDetailsDAO();
		markerDetailsDAOLocal.setSession(localSession);
		markerDetailsDAOCentral = new MarkerDetailsDAO();
		markerDetailsDAOCentral.setSession(centralSession);

	}


	public HorizontalLayout buildTabbedComponentForMarker() throws GDMSException, MiddlewareQueryException {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("50%");

		_tabsheetForMarkers = new TabSheet();
		_tabsheetForMarkers.setWidth("50%");

		buildConditionsPanel = buildMarkerConditionsComponent();
		buildConditionsPanel.addListener(this);
		buildConditionsPanel.setWidth("50%");
		
		buildResultsPanel = buildMarkerResultsComponent();
		buildResultsPanel.addListener(this);
		buildResultsPanel.setWidth("50%");

		_tabsheetForMarkers.addComponent(buildConditionsPanel);
		_tabsheetForMarkers.addComponent(buildResultsPanel);
		
		_tabsheetForMarkers.getTab(1).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForMarkers);

		return horizontalLayout;
	}


	private Component buildMarkerConditionsComponent() {
		VerticalLayout finalConditionsLayout = new VerticalLayout();
		finalConditionsLayout.setCaption("Conditions");
		finalConditionsLayout.setMargin(true, true, true, true);

		Label lblSearchConditions = new Label("Select Fields And Set Conditions");
		lblSearchConditions.setStyleName(Reindeer.LABEL_H2);
		finalConditionsLayout.addComponent(lblSearchConditions);
		finalConditionsLayout.setComponentAlignment(lblSearchConditions, Alignment.TOP_CENTER);

		final ArrayList<String> listOfSelectionValues = new ArrayList<String>();
		selectFieldValue = new ComboBox();
		Object itemId3 = selectFieldValue.addItem();
		selectFieldValue.setItemCaption(itemId3, "Select Value");
		selectFieldValue.setValue(itemId3);
		selectFieldValue.setImmediate(true);
		selectFieldValue.setNullSelectionAllowed(false);
		selectFieldValue.setNewItemsAllowed(true);

		selectFieldName = new ComboBox();
		Object itemId1 = selectFieldName.addItem();
		selectFieldName.setItemCaption(itemId1, "Select Field");
		selectFieldName.setValue(itemId1);
		selectFieldName.setImmediate(true);
		selectFieldName.setNullSelectionAllowed(false);


		/*String[] strArrayOfFields = {"Marker-ID", "Marker-Name", "Marker-Type", "Principal-Investigator", "Contact",
				"Institute", "Reference", "Species", "Accession-ID", "Genotype", 
				"Assay-Type", "Motif", "No-of-Repeats", "Motif-Type", "Sequence", "Sequence-Length",
				"Min-Allele", "Max-Allele", "SSR-Number", "Forward-Primer", "Reverse-Primer", "Product-Size",
				"Forward-Primer-Temperature", "Reverse-Primer-Temperature", "Annealing-Temperature",
				"Elongation-Temperature", "Fragment-Size-Expected", "Fragment-Size-Observed", "Amplification"};*/
		//final ArrayList<String> listOfSelectionValues = new ArrayList<String>();
		final String[] strArrayOfFields = {"Marker-ID", "Marker-Name", "Marker-Type",
				"Accession-ID", "Genotype", "Annealing-Temperature", "Amplification"};
		for (int i = 0; i < strArrayOfFields.length; i++){
			selectFieldName.addItem(strArrayOfFields[i]);
		}
		Property.ValueChangeListener listener1 = new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				//try {
					if (null == selectFieldName.getValue()){
						return;
					}
					
					if (0 < listOfSelectionValues.size()){
						/*for (String string : listOfSelectionValues) {
							listOfSelectionValues.remove(string);
						}*/
						ArrayList<String> tempList = new ArrayList<String>();
						for (String string : listOfSelectionValues) {
							tempList.add(string);
						}
						for (String string : tempList) {
							listOfSelectionValues.remove(string);
						}
						//System.out.println(listOfSelectionValues.size());
					}
					
					
					String strSelectedField = selectFieldName.getValue().toString();

					selectFieldValue.removeAllItems();
					Object itemId3 = selectFieldValue.addItem();
					selectFieldValue.setItemCaption(itemId3, "Select Value");
					selectFieldValue.setValue(itemId3);

					//ArrayList<String> listOfSelectionValues = new ArrayList<String>();
					
					if (strSelectedField.equals("Marker-Name")){			

						List<String> listOfAllMarkerTypes = new ArrayList<String>();
						try {
							List<String> markerTypes = genoManager.getAllMarkerTypes(0, 15);	
							for (int m=0;m<markerTypes.size();m++){
								if(!listOfAllMarkerTypes.contains(markerTypes.get(m)))
									listOfAllMarkerTypes.add(markerTypes.get(m));
							}
							

						} catch (MiddlewareQueryException e) {
							//throw new GDMSException(e.getMessage());
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}
						
						
						for (String strMarkerType : listOfAllMarkerTypes) {
							try {
								markerDAOLocal = new MarkerDAO();
								
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
								
								markerDAOLocal.setSession(localSession);
								markerDAOCentral = new MarkerDAO();
								markerDAOCentral.setSession(centralSession);
							
								
								long countAllLocal = markerDAOLocal.countAll();
								List<String> markerNamesByMarkerTypeLocal = markerDAOLocal.getMarkerNamesByMarkerType(strMarkerType, 0, (int)countAllLocal);
								long countAllCentral = markerDAOCentral.countAll();
								List<String> markerNamesByMarkerTypeCentral = markerDAOCentral.getMarkerNamesByMarkerType(strMarkerType, 0, (int)countAllCentral);
								
								for (String strMarkerName : markerNamesByMarkerTypeLocal){
									if (false == listOfSelectionValues.contains(strMarkerName)){
										listOfSelectionValues.add(strMarkerName);
									}
								}
								for (String strMarkerName : markerNamesByMarkerTypeCentral){
									if (false == listOfSelectionValues.contains(strMarkerName)){
										listOfSelectionValues.add(strMarkerName);
									}
								}
							} catch (MiddlewareQueryException e) {
								//throw new GDMSException(e.getMessage());
								//_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
								e.printStackTrace();
								return;
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						
						
					} else if (strSelectedField.equals("Marker-Type")){

						try {	
							
							
							
							List<String> markerTypes = genoManager.getAllMarkerTypes(0, 15);							
							for (int m=0;m<markerTypes.size();m++){
								if(!listOfSelectionValues.contains(markerTypes.get(m)))
									listOfSelectionValues.add(markerTypes.get(m));
							}
							
							listOfSelectedFields.add("Marker-Type");

						} catch (MiddlewareQueryException e) {
							//throw new GDMSException(e.getMessage());
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}catch(Exception e){
							e.printStackTrace();
						}

					} else if (strSelectedField.equals("Marker-ID")){

						try {
							long countAll = markerDAOLocal.countAll();
							List<Marker> listOfAllMarkersLocal = markerDAOLocal.getAll(0, (int)countAll);

							long countAll2 = markerDAOCentral.countAll();
							List<Marker> listOfAllMarkersCentral = markerDAOCentral.getAll(0, (int)countAll2);
							
							for (Marker marker : listOfAllMarkersLocal){
								listOfSelectionValues.add(String.valueOf(marker.getMarkerId()));
							}
							for (Marker marker : listOfAllMarkersCentral){
								listOfSelectionValues.add(String.valueOf(marker.getMarkerId()));
							}
							listOfSelectedFields.add("Marker-ID");

						} catch (MiddlewareQueryException e) {
							//throw new GDMSException(e.getMessage());
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}

					} else if (strSelectedField.equals("Accession-ID")){
						try {

							long countAllDBAccessionIDs = markerDAOLocal.countAllDbAccessionIds();
							List<String> listOfAllDBAccessionIDsLocal = markerDAOLocal.getAllDbAccessionIds(0, (int)countAllDBAccessionIDs);
							long countAllDBAccessionIDs2 = markerDAOCentral.countAllDbAccessionIds();
							List<String> listOfAllDBAccessionIDsCentral = markerDAOCentral.getAllDbAccessionIds(0, (int)countAllDBAccessionIDs2);
							
							for (String strAccessionID : listOfAllDBAccessionIDsLocal){
								listOfSelectionValues.add(strAccessionID);
							}
							for (String strAccessionID : listOfAllDBAccessionIDsCentral){
								listOfSelectionValues.add(strAccessionID);
							}
							listOfSelectedFields.add("Accession-ID");

						} catch (MiddlewareQueryException e) {
							//throw new GDMSException(e.getMessage());
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}

					
					for (int i = 0; i < listOfSelectionValues.size(); i++){
						selectFieldValue.addItem(listOfSelectionValues.get(i));
					}

					/*} catch (GDMSException e) {
					e.printStackTrace();
				}*/
			}
		};
		selectFieldName.addListener(listener1);


		containsField = new ComboBox();
		Object itemId2 = containsField.addItem();
		//20131206: Tulasi --- Modifed the caption for ContainsFields and added "Contains" as one of the options for the conditions
		//containsField.setItemCaption(itemId2, "Contains");
		containsField.setItemCaption(itemId2, "Set Condition");
		containsField.setValue(itemId2);
		final String[] containsFields = new String[] {
				"Contains", "Equals", "Not Equals", "Less Than", "Less Equal", "Greater Than", "Greater Equal"};
		for (int i = 0; i < containsFields.length; i++){
			containsField.addItem(containsFields[i]);
		}
		containsField.setImmediate(true);


		HorizontalLayout horizontalLayoutForSelectComponents = new HorizontalLayout();
		horizontalLayoutForSelectComponents.addComponent(selectFieldName);
		horizontalLayoutForSelectComponents.addComponent(containsField);
		horizontalLayoutForSelectComponents.addComponent(selectFieldValue);
		horizontalLayoutForSelectComponents.setWidth("500px");
		horizontalLayoutForSelectComponents.setMargin(true, false, false, false);
		finalConditionsLayout.addComponent(horizontalLayoutForSelectComponents);


		conditionsTable = new Table();
		conditionsTable.setWidth("100%");
		conditionsTable.setPageLength(10);
		conditionsTable.setSelectable(true);
		conditionsTable.setColumnCollapsingAllowed(true);
		//20130619: Not allowing reordering
		conditionsTable.setColumnReorderingAllowed(false);
		conditionsTable.setStyleName("strong");

		String[] strArrayOfColNames = {"S.NO#:", "FIELD-NAME", "CONDITION", "FIELD-VALUE"};
		for (int i = 0; i < strArrayOfColNames.length; i++){
			conditionsTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
		}

		Button btnAdd = new Button("Add");
		btnAdd.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if (null != selectFieldName.getValue()){
					strMarkerField = selectFieldName.getValue().toString();
				}
				
				boolean bValidField = false;
				for (int i = 0; i < strArrayOfFields.length; i++){
					if (strMarkerField.equals(strArrayOfFields[i])){
						bValidField = true;
						break;
					}
				}
				if (!bValidField){
					strMarkerField = "";
				}
				
				int size = conditionsTable.size();
				for (int i = 0; i < size; i++){
					Item item = conditionsTable.getItem(i);
					String strFieldName = item.getItemProperty("FIELD-NAME").toString();
					if (strFieldName.equals(strMarkerField)){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select criteria with new field for new condition.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}


				if (null != containsField.getValue()){
					Object value = containsField.getValue();
					if (false == value.equals(1)){
						strCondition = value.toString();
					} else {
						strCondition = "Equals";
					}
				} else {
					strCondition = "Equals";
				}

				
				if (null != selectFieldValue.getValue()){
					if (strMarkerField.equals("Marker-Type")){
						strMarkerType = selectFieldValue.getValue().toString();
						strValue = selectFieldValue.getValue().toString();
					} else {
						strValue = selectFieldValue.getValue().toString();
					}
					
					if (strValue.equals("*") || strValue.endsWith("*")) {
						strValue = selectFieldValue.getValue().toString();
					} else if (!listOfSelectionValues.contains(strValue)){
						strValue = "";
					} 
				}
				
				//boolean bValidCriteria = true;
				if (strMarkerField.equals("")) {
					//bValidCriteria = false;
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select a valid Field from the Select-Field list.", Notification.TYPE_ERROR_MESSAGE);
				} else if (strCondition.equals("")) {
					//bValidCriteria = false;
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select a valid Condition from the Contains list.", Notification.TYPE_ERROR_MESSAGE);
				} else if (strValue.equals("")) {
					//bValidCriteria = false;
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select a valid Condition from the Select-Value list.", Notification.TYPE_ERROR_MESSAGE);
				} else {

				//if (bValidCriteria){
					iConditonCnt += 1;
					conditionsTable.addItem(new Object[] {String.valueOf(iConditonCnt), strMarkerField, strCondition, strValue},  new Integer(iConditonCnt-1));
					
					listOfSelectedFields.add(strMarkerField);
					selectFieldName.removeItem(strMarkerField);
					selectFieldName.setValue("Select Field");
					selectFieldName.setImmediate(true);
				}
			}
		});


		Button btnDelete = new Button("Delete");
		btnDelete.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				Object rowId = conditionsTable.getValue(); // get the selected rows id
				if (null!= rowId){
					Container containerDataSource = conditionsTable.getContainerDataSource();
					Item item = containerDataSource.getItem(rowId);
					if (null != item){
						String strMarkerField = item.getItemProperty("FIELD-NAME").toString();
						conditionsTable.removeItem(rowId);
						iConditonCnt -= 1;
						selectFieldName.addItem(strMarkerField);
					}
				}
			}
		});

		HorizontalLayout horizontalLayoutForButtons = new HorizontalLayout();
		horizontalLayoutForButtons.setSpacing(true);
		horizontalLayoutForButtons.addComponent(btnAdd);
		horizontalLayoutForButtons.addComponent(btnDelete);
		horizontalLayoutForButtons.setWidth("150px");
		horizontalLayoutForButtons.setMargin(true, false, true, false);

		listOfSelectedCriteria = new ArrayList<Item>();
		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				listOfSelectedCriteria = new ArrayList<Item>();

				for (int i = 0; i < iConditonCnt; i++){
					Item item = conditionsTable.getItem(new Integer(i));
					listOfSelectedCriteria.add(item);
				}
				//System.out.println("**************:"+listOfSelectedCriteria);
				try {
					Component newMarkerResultsPanel = buildMarkerResultsComponent();
					_tabsheetForMarkers.replaceComponent(buildResultsPanel, newMarkerResultsPanel);
					_tabsheetForMarkers.requestRepaint();
					buildResultsPanel = newMarkerResultsPanel;
					//20131206: Tulasi --- Set the next tab enabled to true to move to the next tab after clicking next
					_tabsheetForMarkers.getTab(1).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(1);

				} catch (GDMSException e) {
					e.printStackTrace();
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				} catch (MiddlewareQueryException e) {
					e.printStackTrace();
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});


		finalConditionsLayout.addComponent(horizontalLayoutForButtons);
		finalConditionsLayout.addComponent(conditionsTable);
		finalConditionsLayout.addComponent(layoutForButton);
		finalConditionsLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);

		return finalConditionsLayout;
	}



	protected List<String> getSelectionValues() throws GDMSException {

		List<String> listOfSelectableValues = new ArrayList<String>(); 

		if (strMarkerField.equals("MARKER_TYPE")){
			listOfSelectableValues = retrieveMarker.retrieveAllMarkerTypes();
		}

		return listOfSelectableValues;
	}


	private Component buildMarkerResultsComponent() throws GDMSException, MiddlewareQueryException {
		VerticalLayout verticalLayout = new VerticalLayout();
		VerticalLayout resultsLayout = new VerticalLayout();
		verticalLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true);
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);
		resultsLayout.setWidth("700px");
		resultsLayout.addStyleName(Reindeer.LAYOUT_WHITE);
		
		Table[] arrayOfTablesForMarkerResults = buildMarkerTable();
		//System.out.println("arrayOfTablesForMarkerResults  &&&&&&&&&&&&&&&&  :"+arrayOfTablesForMarkerResults);
		//System.out.println("Length:"+arrayOfTablesForMarkerResults.length);
		if (null != arrayOfTablesForMarkerResults && arrayOfTablesForMarkerResults.length > 0){
			/*System.out.println("arrayOfTablesForMarkerResults  &&&&&&&&&&&&&&&&  :"+arrayOfTablesForMarkerResults);
			System.out.println("Length:"+arrayOfTablesForMarkerResults.length);*/
			
			for (int i = 0; i < arrayOfTablesForMarkerResults.length; i++) {
				
				final int j = i;
				
				arrayOfTablesForMarkerResults[i].setWidth("100%");
				arrayOfTablesForMarkerResults[i].setPageLength(10);
				arrayOfTablesForMarkerResults[i].setSelectable(true);
				arrayOfTablesForMarkerResults[i].setColumnCollapsingAllowed(true);
				arrayOfTablesForMarkerResults[i].setColumnReorderingAllowed(false);
				arrayOfTablesForMarkerResults[i].setStyleName("strong");
				
				int size = arrayOfTablesForMarkerResults[i].size();
				String strMarkerType = "";
				if (0 < finalListOfMarkerTypes.size()){
					//System.out.println("IF   &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
					strMarkerType = finalListOfMarkerTypes.get(i).toString();
					if (0 < size) {
						resultsLayout.addComponent(new Label("There are " + size +  " "  + strMarkerType + " markers in the below table."));
					}

				} else {
					//System.out.println("ELSE  ********************************************");
					//resultsLayout.addComponent(new Label("There are " + size + " markers in the below table."));
					//System.out.println("ELSE  ********************************************");
					if (0 < size) {
						resultsLayout.addComponent(new Label("There are " + size + " markers in the below table."));
					}
				}
				System.out.println("%%%%%%%%%%%%%%%%%%   :"+arrayOfTablesForMarkerResults[0]);
				if (0 < size) {
					resultsLayout.addComponent(arrayOfTablesForMarkerResults[i]);
					resultsLayout.setComponentAlignment(arrayOfTablesForMarkerResults[i], Alignment.MIDDLE_CENTER);
					
					ThemeResource themeResource = new ThemeResource("images/excel.gif");
					Button excelButton = new Button();
					excelButton.setIcon(themeResource);
					excelButton.setStyleName(Reindeer.BUTTON_LINK);
					excelButton.setDescription(strMarkerType + "EXCEL Format");
					
					if (strMarkerType.equalsIgnoreCase("snp")) {
						
						HorizontalLayout layoutForExportTypes = new HorizontalLayout();
						layoutForExportTypes.setSpacing(true);
						
						layoutForExportTypes.addComponent(excelButton);
						
						ThemeResource themeResource1 = new ThemeResource("images/LGC_Genomics.gif");
						Button kbioButton = new Button();
						kbioButton.setIcon(themeResource1);
						kbioButton.setStyleName(Reindeer.BUTTON_LINK);
						kbioButton.setDescription("LGC Genomics Order form");
						layoutForExportTypes.addComponent(kbioButton);
						kbioButton.addListener(new ClickListener() {
							private static final long serialVersionUID = 1L;
							public void buttonClick(ClickEvent event) {
								ExportFileFormats exportFileFormats = new ExportFileFormats();
								String mType="SNP";
								ArrayList <String> markersForKBio=new ArrayList();	
								try{
									//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
									factory=GDMSModel.getGDMSModel().getManagerFactory();
									/*
									localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
									centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
									
									//om=factory.getOntologyDataManager();
*/									genoManager=factory.getGenotypicDataManager();
								}catch (Exception e){
									e.printStackTrace();
								}
								
								
								try {
									ArrayList<String> snpMarkers=(ArrayList<String>) genoManager.getMarkerNamesByMarkerType(mType, 0, (int)genoManager.countMarkerNamesByMarkerType(mType));
									if(!(snpMarkers.isEmpty())){
										if(!(snpMarkers.isEmpty())){
											File kbioOrderFormFile = exportFileFormats.exportToKBio(snpMarkers, _mainHomePage);
											FileResource fileResource = new FileResource(kbioOrderFormFile, _mainHomePage);
											//_mainHomePage.getMainWindow().getWindow().open(fileResource, "KBio Order Form", true);
											_mainHomePage.getMainWindow().getWindow().open(fileResource, "_self");
										}
									}else{
										_mainHomePage.getMainWindow().getWindow().showNotification("No SNP Marker(s) to create KBio Order form", Notification.TYPE_ERROR_MESSAGE);
										return;
									}
									
								} catch (Exception e) {
									_mainHomePage.getMainWindow().getWindow().showNotification("Error generating KBioOrder Form", Notification.TYPE_ERROR_MESSAGE);
									return;
								}
								
								//exportFileFormats.exportToKBio(_markerTable, _mainHomePage);
							}
						});
						
						resultsLayout.addComponent(layoutForExportTypes);
						resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.BOTTOM_RIGHT);
						
					} else {
						resultsLayout.addComponent(excelButton);
						resultsLayout.setComponentAlignment(excelButton, Alignment.BOTTOM_RIGHT);
					}
					
					excelButton.addListener(new ClickListener() {
						private static final long serialVersionUID = 1L;
						public void buttonClick(ClickEvent event) {
							exportToExcel(j);
						}
					});
				}
				//System.out.println("%%%%%%%%%%%%%%%%%%   :"+arrayOfTablesForMarkerResults[0]);
				//resultsLayout.addComponent(arrayOfTablesForMarkerResults[i]);
				//resultsLayout.setComponentAlignment(arrayOfTablesForMarkerResults[i], Alignment.MIDDLE_CENTER);
			}
		}

		

		/*ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		layoutForExportTypes.addComponent(excelButton);
		excelButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				exportToExcel();
			}
		});*/


		//20131210: Tulasi --- Not displaying the PDF and Print buttons
		/*themeResource = new ThemeResource("images/pdf.gif");
		Button pdfButton = new Button();
		pdfButton.setIcon(themeResource);
		pdfButton.setStyleName(Reindeer.BUTTON_LINK);
		pdfButton.setDescription("PDF Format");
		layoutForExportTypes.addComponent(pdfButton);
		pdfButton.addListener(new ClickListener() {
			/**
			 * 
			 *//*
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				exportFileFormats.exportToPdf(_markerTable, _mainHomePage);
			}
		});	*/		
		
		//20131206: Added a new button to export the data in KBio format 
		//TODO: Have to add the required icon
		/*ThemeResource themeResource = new ThemeResource("images/pdf.gif");
		Button kbioButton = new Button();
		kbioButton.setIcon(themeResource);
		kbioButton.setStyleName(Reindeer.BUTTON_LINK);
		kbioButton.setDescription("KBio Format");
		layoutForExportTypes.addComponent(kbioButton);
		kbioButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				//exportFileFormats.exportToKBio(_markerTable, _mainHomePage);
			}
		});*/
		//20131206: Added a new button to export the data in KBio format 		
	
		//20131210: Tulasi --- Not displaying the PDF and Print buttons
		/*themeResource = new ThemeResource("images/print.gif");
		Button printButton = new Button();
		printButton.setIcon(themeResource);
		printButton.setStyleName(Reindeer.BUTTON_LINK);
		printButton.setDescription("Print Format");
		layoutForExportTypes.addComponent(printButton);
		printButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
			}
		});*/
		
		if (null != arrayOfTablesForMarkerResults && 0 == arrayOfTablesForMarkerResults.length){
			//excelButton.setEnabled(false);
			/*pdfButton.setEnabled(false);
			printButton.setEnabled(false);*/
		} else {
			//excelButton.setEnabled(true);
			/*pdfButton.setEnabled(true);
			printButton.setEnabled(true);*/
		}

		//resultsLayout.addComponent(layoutForExportTypes);
		//resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);

		verticalLayout.addComponent(resultsLayout);
		return verticalLayout;
	}
	
	
	private void exportToExcel(int theTableIndex) {
		if(null == _finalListOfMarkers || 0 == _finalListOfMarkers.size()) {
			return;
		}

		String strFileName = "tmp";

		ArrayList<String[]> listToExport = new ArrayList<String[]>();
		
		if (null != _finalListOfMarkers) {

			String strMarkerType = finalListOfMarkerTypes.get(theTableIndex);

			String[] strArrayOfColNames = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT",
					"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
					"ASSAY-TYPE", "MOTIF", "NO-OF-REPEATS", "MOTIF-TYPE", "SEQUENCE", "SEQUENCE-LENGTH",
					"MIN-ALLELE", "MAX-ALLELE", "SSR-NUMBER", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
					"FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
					"ELONGATION-TEMPERATURE", "FRAGMENT-SIZE-EXPECTED", "FRAGMENT-SIZE-OBSERVED", "AMPLIFICATION"};

			if (null != strMarkerType) {

				if (strMarkerType.equalsIgnoreCase("snp")) {

					String[] strSNParray = {"MARKER-ID", "MARKER-NAME", "ALIAS","MARKER_TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
							"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
							"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
							"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
							"ANNEALING-TEMPERATURE",
					"POSITION-ON-REFERENCE-SEQUENCE"};

					strArrayOfColNames = strSNParray;

				} else if (strMarkerType.equalsIgnoreCase("cap")) {

					String[] strCAParray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
							"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
							"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
							"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
							"RESTRICTED ENZYME FOR ASSAY", "ANNEALING-TEMPERATURE",
					"POSITION-ON-REFERENCE-SEQUENCE"};

					strArrayOfColNames = strCAParray;

				} else if (strMarkerType.equalsIgnoreCase("cisr")) {

					String[] strCISRarray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "MARKER-TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT",
							"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
							"ASSAY-TYPE", "REPEAT", "NO-OF-REPEATS", "SEQUENCE", "SEQUENCE-LENGTH",
							"MIN-ALLELE", "MAX-ALLELE", "SIZE-OF-REPEAT-MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
							"PRIMER-LENGTH", "FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
							"FRAGMENT-SIZE-EXPECTED", "AMPLIFICATION"};

					strArrayOfColNames = strCISRarray;
				}

			}


			for (int i = 0; i < arrayOfTables[theTableIndex].size(); i++) {

				Item item = arrayOfTables[theTableIndex].getItem(i);

				String[] strArrayOfRowData = new String[strArrayOfColNames.length];

				for (int jCol = 0; jCol < strArrayOfColNames.length; jCol++) {
					
					if (null != item) {
						Property propertyFieldName = item.getItemProperty(strArrayOfColNames[jCol]);
						if (null != propertyFieldName) {
							strArrayOfRowData[jCol] = propertyFieldName.toString();
						} else {
							strArrayOfRowData[jCol] = "";
						}
					}
				}

				listToExport.add(strArrayOfRowData);
			}
		}

		if(0 == listToExport.size()) {
			_mainHomePage.getMainWindow().getWindow().showNotification("No Maps to export",  Notification.TYPE_ERROR_MESSAGE);
			return;
		}


		String strMarkerType = finalListOfMarkerTypes.get(theTableIndex);

		String[] strArrayOfColNames = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT",
				"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
				"ASSAY-TYPE", "MOTIF", "NO-OF-REPEATS", "MOTIF-TYPE", "SEQUENCE", "SEQUENCE-LENGTH",
				"MIN-ALLELE", "MAX-ALLELE", "SSR-NUMBER", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
				"FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
				"ELONGATION-TEMPERATURE", "FRAGMENT-SIZE-EXPECTED", "FRAGMENT-SIZE-OBSERVED", "AMPLIFICATION"};

		if (null != strMarkerType) {

			if (strMarkerType.equalsIgnoreCase("snp")) {

				String[] strSNParray = {"MARKER-ID", "MARKER-NAME", "ALIAS","MARKER_TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
						"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
						"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
						"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
						"ANNEALING-TEMPERATURE",
				"POSITION-ON-REFERENCE-SEQUENCE"};

				strArrayOfColNames = strSNParray;

			} else if (strMarkerType.equalsIgnoreCase("cap")) {

				String[] strCAParray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
						"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
						"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
						"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
						"RESTRICTED ENZYME FOR ASSAY", "ANNEALING-TEMPERATURE",
				"POSITION-ON-REFERENCE-SEQUENCE"};

				strArrayOfColNames = strCAParray;

			} else if (strMarkerType.equalsIgnoreCase("cisr")) {

				String[] strCISRarray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "MARKER-TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT",
						"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
						"ASSAY-TYPE", "REPEAT", "NO-OF-REPEATS", "SEQUENCE", "SEQUENCE-LENGTH",
						"MIN-ALLELE", "MAX-ALLELE", "SIZE-OF-REPEAT-MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
						"PRIMER-LENGTH", "FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
						"FRAGMENT-SIZE-EXPECTED", "AMPLIFICATION"};

				strArrayOfColNames = strCISRarray;
			}
		}

		listToExport.add(0, strArrayOfColNames);

		ExportFileFormats exportFileFormats = new ExportFileFormats();
		try {
			exportFileFormats.exportMap(_mainHomePage, listToExport, strFileName);
		} catch (WriteException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
		} catch (IOException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
		}

	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}

	private Table[] buildMarkerTable() throws GDMSException, MiddlewareQueryException {		
		
		ArrayList<Marker> tempListOfMarkers = null;
		ArrayList<MarkerUserInfo> tempListOfMarkerInfo = null;
		ArrayList<MarkerDetails> tempListOfMarkerDetails = null;

		_finalListOfMarkers = null;
		boolean bStartedFiltering = false;
		//System.out.println(listOfSelectedCriteria+" &&  size="+listOfSelectedCriteria.size());
		if (null != listOfSelectedCriteria && listOfSelectedCriteria.size() > 0){

			_finalListOfMarkers = new ArrayList<Marker>();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			
			markerDAOLocal = new MarkerDAO();
			markerDAOLocal.setSession(localSession);
			markerDAOCentral = new MarkerDAO();
			markerDAOCentral.setSession(centralSession);
			markerUserInfoDAOLocal = new MarkerUserInfoDAO();
			markerUserInfoDAOLocal.setSession(localSession);
			markerUserInfoDAOCentral = new MarkerUserInfoDAO();
			markerUserInfoDAOCentral.setSession(centralSession);

			markerDetailsDAOLocal = new MarkerDetailsDAO();
			markerDetailsDAOLocal.setSession(localSession);
			markerDetailsDAOCentral = new MarkerDetailsDAO();
			markerDetailsDAOCentral.setSession(centralSession);
			
			List<Marker> listOfAllMarkersLocal = markerDAOLocal.getAll();
			List<Marker> listOfAllMarkersCentral = markerDAOCentral.getAll();
			
			for (Marker marker : listOfAllMarkersLocal){
				if (false == _finalListOfMarkers.contains(marker)){
					_finalListOfMarkers.add(marker);
				}
			}
			for (Marker marker : listOfAllMarkersCentral){
				if (false == _finalListOfMarkers.contains(marker)){
					_finalListOfMarkers.add(marker);
				}
			}
			
			tempListOfMarkers = new ArrayList<Marker>();
			tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
			tempListOfMarkerDetails = new ArrayList<MarkerDetails>();

			_finalListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
			_finalListOfMarkerDetails = new ArrayList<MarkerDetails>();
		}


		//"Marker-ID", "Marker-Name", "Marker-Type", "Accession-ID", "Genotype", "Annealing-Temperature", "Amplification"
		//"S.NO#:", "FIELD-NAME", "CONDITION", "FIELD-VALUE"
		for (int i = 0; i < listOfSelectedCriteria.size(); i++){

			Item item = listOfSelectedCriteria.get(i);

			Property propertyFieldName = item.getItemProperty("FIELD-NAME");
			Property propertyCondition = item.getItemProperty("CONDITION");
			Property propertyFieldValue = item.getItemProperty("FIELD-VALUE");
			//System.out.println("field selected=:"+propertyFieldName+"   condition selected=:"+propertyCondition+"  value selected=:"+propertyFieldValue);
			if (propertyFieldName.toString().equals("Marker-Type")){
				for (int j = 0; j < _finalListOfMarkers.size(); j++){
					Marker marker = _finalListOfMarkers.get(j);
					Integer markerId = marker.getMarkerId();
					String strCondition = propertyCondition.toString();
					
					if (strCondition.equals("Equals")){
						if (marker.getMarkerType().equals(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)) {
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)) {
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)) {
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}  

					} else if (strCondition.equals("Not Equals")){
						if (false == marker.getMarkerType().equals(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					}
				}
				if (bStartedFiltering){
					_finalListOfMarkers = tempListOfMarkers;
					_finalListOfMarkerInfo = tempListOfMarkerInfo;
					_finalListOfMarkerDetails = tempListOfMarkerDetails;
					
					tempListOfMarkers = new ArrayList<Marker>();
					tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
					tempListOfMarkerDetails = new ArrayList<MarkerDetails>();
				}
			} 


			if (propertyFieldName.toString().equals("Marker-Name")){
				//System.out.println("_finalListOfMarkers=:"+_finalListOfMarkers);
				for (int j = 0; j < _finalListOfMarkers.size(); j++){
					Marker marker = _finalListOfMarkers.get(j);
					Integer markerId = marker.getMarkerId();
					String strCondition = propertyCondition.toString();
					//System.out.println("marker id=:"+markerId+"   marker name=:"+marker);
					if (strCondition.equals("Equals")){
						
						if (marker.getMarkerName().toString().toLowerCase().equals(propertyFieldValue.toString().toLowerCase())){
							//System.out.println("marker id=:"+markerId+"   marker name=:"+marker);
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							//System.out.println("*****************:"+markerUserInfoDAOLocal.getById(markerId, true));
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)) {
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)) {
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}

							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)) {
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
							//System.out.println("tempListOfMarkerInfo:"+tempListOfMarkerInfo);
							
							//System.out.println("tempListOfMarkerDetails:"+tempListOfMarkerDetails);
						} else if (propertyFieldValue.toString().equals("*")) {
							//20130829: Fix for Issue No: 68
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							List<MarkerUserInfo> listOfAllMarkerUserInfoLocal = markerUserInfoDAOLocal.getAll();
							if (null != listOfAllMarkerUserInfoLocal && false == tempListOfMarkerInfo.contains(listOfAllMarkerUserInfoLocal)){
								tempListOfMarkerInfo.addAll(listOfAllMarkerUserInfoLocal);
							}
							List<MarkerUserInfo> listOfAllMarkerUserInfoCentral = markerUserInfoDAOCentral.getAll();
							if (null != listOfAllMarkerUserInfoCentral && false == tempListOfMarkerInfo.contains(listOfAllMarkerUserInfoCentral)){
								tempListOfMarkerInfo.addAll(listOfAllMarkerUserInfoCentral);
							}
							
							List<MarkerDetails> listOfAllMarkerDetailsLocal = markerDetailsDAOLocal.getAll();
							if (null != listOfAllMarkerDetailsLocal && false == tempListOfMarkerDetails.contains(listOfAllMarkerDetailsLocal)){
								tempListOfMarkerDetails.addAll(listOfAllMarkerDetailsLocal);
							}
							List<MarkerDetails> listOfAllMarkerDetailsCentral = markerDetailsDAOCentral.getAll();
							if (null != listOfAllMarkerDetailsCentral && false == tempListOfMarkerDetails.contains(listOfAllMarkerDetailsCentral)){
								tempListOfMarkerDetails.addAll(listOfAllMarkerDetailsCentral);
							}
						} //20130829: Fix for Issue No: 68
						
					} else if (strCondition.equals("Not Equals")){
						if (false == marker.getMarkerName().equals(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)) {
								tempListOfMarkers.add(marker);
							}
							
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)) {
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}

							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}
					}  else if (strCondition.equals("Contains")) {

						//20131206: Tulasi --- Added code to display Markers based on initial string given by the user
						
						String strFieldValue = propertyFieldValue.toString();
						String strInitialChars = "";
						if (strFieldValue.endsWith("*")) {
							int indexOfAstrisk = strFieldValue.indexOf("*");
							strInitialChars = strFieldValue.substring(0, indexOfAstrisk);
						}

						if (false == strInitialChars.equals("")) {
							if (marker.getMarkerName().startsWith(strInitialChars)) {

								if (false == tempListOfMarkers.contains(marker)){
									tempListOfMarkers.add(marker);
								}

								if (!bStartedFiltering){
									bStartedFiltering = true;
								}
								MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
								if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)) {
									tempListOfMarkerInfo.add(markerInfoByIdLocal);
								}
								MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
								if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)) {
									tempListOfMarkerInfo.add(markerInfoByIdCentral);
								}

								MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
								if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)) {
									tempListOfMarkerDetails.add(markerDetailsIDLocal);
								}
								MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
								if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)) {
									tempListOfMarkerDetails.add(markerDetailsIDCentral);
								}
							}
						}
					}
					//20131206: Tulasi --- Added code to display Markers based on initial string given by the user
					
				}
				if (bStartedFiltering){
					_finalListOfMarkers = tempListOfMarkers;
					_finalListOfMarkerInfo = tempListOfMarkerInfo;
					_finalListOfMarkerDetails = tempListOfMarkerDetails;

					tempListOfMarkers = new ArrayList<Marker>();
					tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
					tempListOfMarkerDetails = new ArrayList<MarkerDetails>();
				}
			}


			if (propertyFieldName.toString().equals("Marker-ID")){
				for (int j = 0; j < _finalListOfMarkers.size(); j++){
					Marker marker = _finalListOfMarkers.get(j);
					Integer markerId = marker.getMarkerId();
					String strCondition = propertyCondition.toString();
					if (strCondition.equals("Equals")){
						if (String.valueOf(marker.getMarkerId()).equals(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}

							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsICentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsICentral && false == tempListOfMarkerDetails.contains(markerDetailsICentral)){
								tempListOfMarkerDetails.add(markerDetailsICentral);
							}
						} 
					} else if (strCondition.equals("Not Equals")){
						if (false == String.valueOf(marker.getMarkerId()).equals(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}

							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					} else if (strCondition.equals("Less Than")){
						if (marker.getMarkerId() < Integer.parseInt(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}
					} else if (strCondition.equals("Less Equal")){
						if (marker.getMarkerId() <= Integer.parseInt(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}
					} else if (strCondition.equals("Greater Than")){
						if (marker.getMarkerId() > Integer.parseInt(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}
					} else if (strCondition.equals("Greater Equal")){
						if (marker.getMarkerId() >= Integer.parseInt(propertyFieldValue.toString())){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}
					} 
				}
				if (bStartedFiltering){
					_finalListOfMarkers = tempListOfMarkers;
					_finalListOfMarkerInfo = tempListOfMarkerInfo;
					_finalListOfMarkerDetails = tempListOfMarkerDetails;

					tempListOfMarkers = new ArrayList<Marker>();
					tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
					tempListOfMarkerDetails = new ArrayList<MarkerDetails>();
				}
			}


			if (propertyFieldName.toString().equals("Genotype")){
				for (int j = 0; j < _finalListOfMarkers.size(); j++){
					Marker marker = _finalListOfMarkers.get(j);
					Integer markerId = marker.getMarkerId();
					String strCondition = propertyCondition.toString();
					if (strCondition.equals("Equals")){
						if (marker.getGenotype().equals(propertyFieldValue)){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					} else if (strCondition.equals("Not Equals")){
						if (false == marker.getGenotype().equals(propertyFieldValue)){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					}
				}
				if (bStartedFiltering){
					_finalListOfMarkers = tempListOfMarkers;
					_finalListOfMarkerInfo = tempListOfMarkerInfo;
					_finalListOfMarkerDetails = tempListOfMarkerDetails;

					tempListOfMarkers = new ArrayList<Marker>();
					tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
					tempListOfMarkerDetails = new ArrayList<MarkerDetails>();
				}
			}


			if (propertyFieldName.toString().equals("Accession-ID")){
				for (int j = 0; j < _finalListOfMarkers.size(); j++){
					Marker marker = _finalListOfMarkers.get(j);
					Integer markerId = marker.getMarkerId();
					String strCondition = propertyCondition.toString();
					if (strCondition.equals("Equals")){
						if (marker.getDbAccessionId().equals(propertyFieldValue)){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
							
						} 
					} else if (strCondition.equals("Not Equals")){
						if (false == marker.getDbAccessionId().equals(propertyFieldValue)){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					}
				}
				if (bStartedFiltering){
					_finalListOfMarkers = tempListOfMarkers;
					_finalListOfMarkerInfo = tempListOfMarkerInfo;
					_finalListOfMarkerDetails = tempListOfMarkerDetails;

					tempListOfMarkers = new ArrayList<Marker>();
					tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
					tempListOfMarkerDetails = new ArrayList<MarkerDetails>();
				}
			}


			if (propertyFieldName.toString().equals("Annealing-Temperature")){
				for (int j = 0; j < _finalListOfMarkers.size(); j++){
					Marker marker = _finalListOfMarkers.get(j);
					Integer markerId = marker.getMarkerId();
					String strCondition = propertyCondition.toString();
					if (strCondition.equals("Equals")){
						if (marker.getAnnealingTemp().equals(propertyFieldValue)){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					} else if (strCondition.equals("Not Equals")){
						if (false == marker.getAnnealingTemp().equals(propertyFieldValue)){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					}
					
				}
				if (bStartedFiltering){
					_finalListOfMarkers = tempListOfMarkers;
					_finalListOfMarkerInfo = tempListOfMarkerInfo;
					_finalListOfMarkerDetails = tempListOfMarkerDetails;

					tempListOfMarkers = new ArrayList<Marker>();
					tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
					tempListOfMarkerDetails = new ArrayList<MarkerDetails>();
				}
			}


			if (propertyFieldName.toString().equals("Amplification")){
				for (int j = 0; j < _finalListOfMarkers.size(); j++){
					Marker marker = _finalListOfMarkers.get(j);
					Integer markerId = marker.getMarkerId();
					String strCondition = propertyCondition.toString();
					if (strCondition.equals("Equals")){
						if (marker.getAmplification().equals(propertyFieldValue)){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					} else if (strCondition.equals("Not Equals")){
						if (false == marker.getAmplification().equals(propertyFieldValue)){
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)){
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)){
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						} 
					}
				}
				if (bStartedFiltering){
					_finalListOfMarkers = tempListOfMarkers;
					_finalListOfMarkerInfo = tempListOfMarkerInfo;
					_finalListOfMarkerDetails = tempListOfMarkerDetails;

					tempListOfMarkers = new ArrayList<Marker>();
					tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
					tempListOfMarkerDetails = new ArrayList<MarkerDetails>();
				}
			}
		}		
		
		if (null != _finalListOfMarkers) {
			
			if (0 == _finalListOfMarkers.size()){
				_mainHomePage.getMainWindow().getWindow().showNotification("No markers for the selected criteria.",  Notification.TYPE_HUMANIZED_MESSAGE);
				return null;
			}

			finalListOfMarkerTypes = new ArrayList<String>();
			if (1 < _finalListOfMarkers.size()) {
				for (int i = 0; i < _finalListOfMarkers.size(); i++) {
					Marker marker = _finalListOfMarkers.get(i);
					String markerType = marker.getMarkerType();
					if (false == finalListOfMarkerTypes.contains(markerType)){
						finalListOfMarkerTypes.add(markerType);
					}
				}
				arrayOfTables = new Table[finalListOfMarkerTypes.size()];
			} else {
				arrayOfTables = new Table[1];
			}

			for (int iTableCntr = 0; iTableCntr < arrayOfTables.length; iTableCntr++) {
				
				String strMarkerType = null;
				if (1 <= arrayOfTables.length){
					strMarkerType = finalListOfMarkerTypes.get(iTableCntr);
				} 
				
				arrayOfTables[iTableCntr] = new Table();
				arrayOfTables[iTableCntr].setStyleName("markertable");
				arrayOfTables[iTableCntr].setWidth("50%");
				arrayOfTables[iTableCntr].setPageLength(10);
				arrayOfTables[iTableCntr].setSelectable(true);
				arrayOfTables[iTableCntr].setColumnCollapsingAllowed(true);
				arrayOfTables[iTableCntr].setColumnReorderingAllowed(true);

				String[] strArrayOfColNames = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT",
						"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
						"ASSAY-TYPE", "MOTIF", "NO-OF-REPEATS", "MOTIF-TYPE", "SEQUENCE", "SEQUENCE-LENGTH",
						"MIN-ALLELE", "MAX-ALLELE", "SSR-NUMBER", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
						"FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
						"ELONGATION-TEMPERATURE", "FRAGMENT-SIZE-EXPECTED", "FRAGMENT-SIZE-OBSERVED", "AMPLIFICATION"};
				
				if (null != strMarkerType) {
					
					if (strMarkerType.equalsIgnoreCase("snp")) {
						
						//"Marker Name","Alias (comma separated for multiple names)","Crop","Genotype","Ploidy","GID","Principal Investigator","Contact","Institute","Incharge Person","Assay Type",
						//"Forward Primer","Reverse Primer","Product Size","Expected Product Size","Position on Refrence Sequence","Motif","Annealing Temperature","Sequence","Reference"
						
						//Marker Name	Alias (comma separated for multiple names)	Crop	Genotype	Ploidy	GID	Principal Investigator	Contact	Institute	Incharge Person	Assay Type	Forward Primer	Reverse Primer	Product Size	Expected Product Size	Position on Refrence Sequence	Motif	Annealing Temperature	Sequence	Reference
						
						String[] strSNParray = {"MARKER-ID", "MARKER-NAME", "ALIAS","MARKER_TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
								"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
								"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
								"ANNEALING-TEMPERATURE",
						"POSITION-ON-REFERENCE-SEQUENCE"};
						
						strArrayOfColNames = strSNParray;
						
					} else if (strMarkerType.equalsIgnoreCase("cap")) {
						
						//Marker Name	Primer ID	Alias (comma separated for multiple names)	Crop	Genotype	Ploidy	GID	Principal Investigator	Contact	Institute	Incharge Person	Assay Type	Forward Primer	Reverse Primer	Product Size	Expected Product Size	Restriction enzyme for assay	Position on Refrence Sequence	Motif	Annealing Temperature	Sequence	Reference	Remarks
						
						String[] strCAParray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
								"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
								"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
								"RESTRICTED ENZYME FOR ASSAY", "ANNEALING-TEMPERATURE",
						"POSITION-ON-REFERENCE-SEQUENCE"};
						
						strArrayOfColNames = strCAParray;
						
					} else if (strMarkerType.equalsIgnoreCase("cisr")) {
						
						//Marker Name	Primer ID	Alias (comma separated for multiple names)	Crop	Genotype	Ploidy	GID	Principal Investigator	Contact	Institute	Incharge Person	Assay Type	Repeat	No of Repeats	Sequence	Sequence Length	Min Allele	Max Allele	Size of Repeat Motif	Forward Primer	Reverse Primer	Product Size	
						//Primer Length	Forward Primer Temperature	Reverse Primer 
						//Temperature	Annealing Temperature	Fragment Size Expected	Amplification	Reference	Remarks
						
						String[] strCISRarray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "MARKER-TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
								"ASSAY-TYPE", "REPEAT", "NO-OF-REPEATS", "SEQUENCE", "SEQUENCE-LENGTH",
								"MIN-ALLELE", "MAX-ALLELE", "SIZE-OF-REPEAT-MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
								"PRIMER-LENGTH", "FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
								"FRAGMENT-SIZE-EXPECTED", "AMPLIFICATION"};
						
						strArrayOfColNames = strCISRarray;
					}
				}
				
				

				for (int i = 0; i < strArrayOfColNames.length; i++){
					arrayOfTables[iTableCntr].addContainerProperty(strArrayOfColNames[i], String.class, null);
				}
				
				ArrayList<MarkerUserInfo> tempfinalListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
				if (null != _finalListOfMarkerInfo && 0 != _finalListOfMarkerInfo.size()){
					tempfinalListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
					
					for (int i = 0; i < _finalListOfMarkerInfo.size(); i++) {
						if (null != _finalListOfMarkerInfo.get(i)) {
							tempfinalListOfMarkerInfo.add(_finalListOfMarkerInfo.get(i));
						}
					}
				}
				
				ArrayList<MarkerDetails> tempfinalListOfMarkerDetails = new ArrayList<MarkerDetails>();
				if (null != _finalListOfMarkerDetails && 0 != _finalListOfMarkerDetails.size()){
					tempfinalListOfMarkerDetails = new ArrayList<MarkerDetails>();
					
					for (int i = 0; i < _finalListOfMarkerDetails.size(); i++) {
						if (null != _finalListOfMarkerDetails.get(i)) {
							tempfinalListOfMarkerDetails.add(_finalListOfMarkerDetails.get(i));
						}
					}
				}
				
				
				for (int i = 0; i < _finalListOfMarkers.size(); i++){
					
					Marker marker = _finalListOfMarkers.get(i);
					
					if (null != strMarkerType) {
						if (false == strMarkerType.equals(marker.getMarkerType())){
							continue;
						}
					}
					
					//20131206: Tulasi
					MarkerUserInfo markerUserInfo = null;
					if (null != tempfinalListOfMarkerInfo && 0 != tempfinalListOfMarkerInfo.size()){
						for (int j = 0; j < tempfinalListOfMarkerInfo.size(); j++) {
							if (null != tempfinalListOfMarkerInfo.get(j)) {
								MarkerUserInfo markerUserInfo2 = tempfinalListOfMarkerInfo.get(j);
								if (markerUserInfo2.getMarkerId() == marker.getMarkerId()) {
									markerUserInfo = markerUserInfo2;
									break;
								}
							}
						}
						
					}
					
					MarkerDetails markerDetails = null;
					if (null != tempfinalListOfMarkerDetails && 0 != tempfinalListOfMarkerDetails.size()) {
						for (int j = 0; j < tempfinalListOfMarkerDetails.size(); j++) {
							if (null != tempfinalListOfMarkerDetails.get(j)) {
								MarkerDetails markerDetails2 = tempfinalListOfMarkerDetails.get(j);
								if (markerDetails2.getMarkerId() == marker.getMarkerId()) {
									markerDetails = markerDetails2;
									break;
								}
							}
						}
					}
					//20131206: Tulasi
					
					String markerId = "";
					if (null != marker.getMarkerId()){
						markerId = String.valueOf(marker.getMarkerId());
					}
					String markerName = marker.getMarkerName();
					String markerType = marker.getMarkerType();
					String reference = marker.getReference();
					String species = marker.getSpecies();
					String dbAccessionId = marker.getDbAccessionId();
					String genotype = marker.getGenotype();
					String assayType = marker.getAssayType();
					String motif = marker.getMotif();
					String forwardPrimer = marker.getForwardPrimer();
					String reversePrimer = marker.getReversePrimer();
					String productSize = marker.getProductSize();
					
					String annealingTemp = "";
					if (null != marker.getAnnealingTemp()) {
						annealingTemp = String.valueOf(marker.getAnnealingTemp());
					}
					
					String amplification = marker.getAmplification();
					String ploidy = marker.getPloidy();
					String primerID = marker.getPrimerId();
					
					String principalInvestigator = "";
					String contact = "";
					String institute = "";
					if (null != markerUserInfo){
						principalInvestigator = markerUserInfo.getPrincipalInvestigator();
						contact = markerUserInfo.getContact();
						institute = markerUserInfo.getInstitute();
					}
					
					String elongationTemp = "0.0";
					String fragmentSizeExpected = "";
					String fragmentSizeObserved = "0";
					String forwardPrimerTemp = "0.0";
					String reversePrimerTemp = "0.0";
					String noOfRepeats = "0";
					String motifType = "";
					String sequence = "";
					String sequenceLength = "0";
					String minAllele = "0";
					String maxAllele = "0";
					String ssrNr = "0";
					String positionOnReferenceSequence = "0";
					String expectedproductsize = "0";
					String restrictedenzymeforassay = "";
					if (null != markerDetails){
						
						if (null != markerDetails.getElongationTemp()) {
							elongationTemp = String.valueOf(markerDetails.getElongationTemp());
						}
						
						if (null != markerDetails.getFragmentSizeExpected()) {
							fragmentSizeExpected = String.valueOf(markerDetails.getFragmentSizeExpected());
						}
						
						if (null != markerDetails.getFragmentSizeObserved()) {
							fragmentSizeObserved = String.valueOf(markerDetails.getFragmentSizeObserved());
						}
						
						if (null != markerDetails.getForwardPrimerTemp()){
							forwardPrimerTemp = String.valueOf(markerDetails.getForwardPrimerTemp());
						}
						
						if (null != markerDetails.getReversePrimerTemp()) {
							reversePrimerTemp = String.valueOf(markerDetails.getReversePrimerTemp());
						}
						
						if (null != markerDetails.getNoOfRepeats()) {
							noOfRepeats = String.valueOf(markerDetails.getNoOfRepeats());
						}
						
						motifType = markerDetails.getMotifType();
						sequence = markerDetails.getSequence();
						
						if (null != markerDetails.getSequenceLength()){
							sequenceLength = String.valueOf(markerDetails.getSequenceLength());
						}
						
						if (null != markerDetails.getMinAllele()){
							minAllele = String.valueOf(markerDetails.getMinAllele());
						}
						
						if (null != markerDetails.getMaxAllele()) {
							maxAllele = String.valueOf(markerDetails.getMaxAllele());
						}
						
						if (null != markerDetails.getSsrNr()) {
							ssrNr = String.valueOf(markerDetails.getSsrNr());
						}
						
						if (null != markerDetails.getPositionOnReferenceSequence()){
							positionOnReferenceSequence = String.valueOf(markerDetails.getPositionOnReferenceSequence());
						}
						
						if (null != markerDetails.getExpectedProductSize()) {
							expectedproductsize = String.valueOf(markerDetails.getExpectedProductSize());
						}
						restrictedenzymeforassay = markerDetails.getRestrictionEnzymeForAssay();
					}
					
					//20131216: Tulasi
					String alias = "";
					String inchargeperson = "";
					String repeat = "";
					String sizeOfRepeatMotif = "";
					String primerLength = "";
					strMarkerType=markerType;
					if (strMarkerType.equalsIgnoreCase("ssr") || strMarkerType.equalsIgnoreCase("ua")) {
						
						String[] strDataArray = {markerId, markerName, markerType, principalInvestigator,
								contact, institute, reference, species, dbAccessionId,
								genotype, assayType, motif, noOfRepeats, motifType,
								sequence, sequenceLength, minAllele, maxAllele, 
								ssrNr, forwardPrimer, reversePrimer, productSize,
								forwardPrimerTemp, reversePrimerTemp, annealingTemp,
								elongationTemp, fragmentSizeExpected, fragmentSizeObserved, amplification
						};
								
						if (null != strDataArray) {
							arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
						}
						
					} else if (strMarkerType.equalsIgnoreCase("snp")) { 
						
						/*String[] strSNParray = {"MARKER-ID", "MARKER-NAME", "ALIAS","MARKER_TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
								"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
								"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
								"ANNEALING-TEMPERATURE",
						"POSITION-ON-REFERENCE-SEQUENCE"};*/
						
						String[] strDataArray = {markerId, markerName, alias, markerType, principalInvestigator,
							contact, inchargeperson, institute, reference, species, dbAccessionId,
							genotype, ploidy, assayType, sequence, motif, forwardPrimer,
							reversePrimer, productSize, expectedproductsize, annealingTemp, positionOnReferenceSequence
					    };
						
						if (null != strDataArray) {
							arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
						}
						
					} else if (strMarkerType.equalsIgnoreCase("cap")) { 
						
						/*String[] strCAParray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
								"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
								"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
								"RESTRICTED ENZYME FOR ASSAY", "ANNEALING-TEMPERATURE",
						"POSITION-ON-REFERENCE-SEQUENCE"};*/
						
						String[] strDataArray = {markerId, markerName, primerID, alias, principalInvestigator,
								contact, inchargeperson, institute, reference, species, dbAccessionId,
								genotype, ploidy, assayType, sequence, motif, forwardPrimer,
								reversePrimer, productSize, expectedproductsize, restrictedenzymeforassay,
								annealingTemp
						};
						
						if (null != strDataArray) {
							arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
						}
						
					} else if (strMarkerType.equalsIgnoreCase("cisr")) { 

						/*String[] strCISRarray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "MARKER-TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
								"ASSAY-TYPE", "REPEAT", "NO-OF-REPEATS", "SEQUENCE", "SEQUENCE-LENGTH",
								"MIN-ALLELE", "MAX-ALLELE", "SIZE-OF-REPEAT-MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
								"PRIMER-LENGTH", "FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
								"FRAGMENT-SIZE-EXPECTED", "AMPLIFICATION"};*/
						String[] strDataArray = {markerId, markerName, primerID, markerType, principalInvestigator,
								contact, institute, reference, species, dbAccessionId,
								genotype, assayType, repeat, noOfRepeats,
								sequence, sequenceLength, minAllele, maxAllele, 
								sizeOfRepeatMotif, forwardPrimer, reversePrimer, productSize,
								primerLength, forwardPrimerTemp, reversePrimerTemp, annealingTemp, fragmentSizeExpected, amplification
						};
						
						if (null != strDataArray) {
							arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
						}

					}
				}
			}
			
		}
		//System.out.println("....:"+arrayOfTables);
		return arrayOfTables;
	}

}
