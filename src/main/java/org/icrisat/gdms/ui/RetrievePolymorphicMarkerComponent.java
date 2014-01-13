package org.icrisat.gdms.ui;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jxl.write.WriteException;

import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.dao.gdms.MappingDataDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.MarkerOnMapDAO;
import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GidNidElement;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.MarkerOnMap;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.retrieve.marker.RetrievePolymorphicMarker;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;



public class RetrievePolymorphicMarkerComponent implements Component.Listener {

	private static final long serialVersionUID = 1L;
	private TabSheet _tabsheetForPolymorphicMarkers;
	private Component buildPolymorphicMapComponent;
	private GDMSMain _mainHomePage;
	protected ArrayList<String> listofMarkers;
	
	protected List<String> markersList;
	
	private Component polymorphicResultComponent;
	private HashMap<String, Integer> hmOfMap = new HashMap<String, Integer>();
	private String strSelectedMapName;
	private Integer iSelectedMapId;
	private ArrayList<MappingData> finalListOfMappingData;
	private HashMap<Integer, String> hmOfMarkerIDAndQtlTrait;
	private ArrayList<String> listofTraits;
	private CheckBox[] arrayOfCheckBoxes;
	private Table _tableForMarkerResults;
	private List<Name> listOfNamesForLine1;
	private List<Name> listOfNamesForLine2;
	private CheckBox checkBox;
	protected ArrayList<String> listOfMarkersSelected;
	private OptionGroup optiongroup;
	protected String strSelectedPolymorphicType;
	private ArrayList missingList;
	ManagerFactory factory=null;
	OntologyDataManager om;
	GenotypicDataManager genoManager;
	
	ArrayList geno1=new ArrayList();
	ArrayList geno2=new ArrayList();
	ArrayList mark1=new ArrayList();
	ArrayList mark2=new ArrayList();
	ArrayList ch1=new ArrayList();
	ArrayList ch2=new ArrayList();
	
	ArrayList mList1=new ArrayList();
	ArrayList mList2=new ArrayList();
	ArrayList mFinalList=new ArrayList();
	String strLine1 ="";
	String strLine2 ="";
	
	protected File orderFormForPlymorphicMarkers;
	
	static HashMap<Integer, ArrayList<String>> hashMap = new HashMap<Integer,  ArrayList<String>>();
	
	
	private HashMap<Integer, String> hmOfSelectedMIDandMNames;
	
	public RetrievePolymorphicMarkerComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		om=factory.getOntologyDataManager();
		genoManager=factory.getGenotypicDataManager();
	}

	/**
	 * 
	 * Building the entire Tabbed Component required for Polymorphic Marker
	 * 
	 */
	
	
	public HorizontalLayout buildTabbedComponentForPolymorphicMarker() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		_tabsheetForPolymorphicMarkers = new TabSheet();
		//_tabsheetForPolymorphicMarkers.setSizeFull();
		_tabsheetForPolymorphicMarkers.setWidth("700px");

		Component polymorphicSelectLinesComponent = buildPolymorphicSelectLinesComponent();

		polymorphicResultComponent = buildPolymorphicResultComponent();

		buildPolymorphicMapComponent = buildPolymorphicMapComponent();
		
		polymorphicSelectLinesComponent.setSizeFull();
		polymorphicResultComponent.setSizeFull();
		buildPolymorphicMapComponent.setSizeFull();
		
		_tabsheetForPolymorphicMarkers.addComponent(polymorphicSelectLinesComponent);
		_tabsheetForPolymorphicMarkers.addComponent(polymorphicResultComponent);
		_tabsheetForPolymorphicMarkers.addComponent(buildPolymorphicMapComponent);
		
		
		_tabsheetForPolymorphicMarkers.getTab(1).setEnabled(false);
		_tabsheetForPolymorphicMarkers.getTab(2).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForPolymorphicMarkers);

		return horizontalLayout;
	}


	private Component buildPolymorphicSelectLinesComponent() {
		VerticalLayout selectLinesLayout = new VerticalLayout();
		selectLinesLayout.setCaption("Select Lines");
		selectLinesLayout.setMargin(true, true, true, true);
		
		final ComboBox selectMarker1 = new ComboBox();
		Object itemId1 = selectMarker1.addItem();
		selectMarker1.setItemCaption(itemId1, "Select Line-1");
		selectMarker1.setValue(itemId1);
		selectMarker1.setNullSelectionAllowed(false);
		selectMarker1.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		selectMarker1.setImmediate(true);
		
		
		final RetrievePolymorphicMarker retrievePolymorphicMarker = new RetrievePolymorphicMarker();
		optiongroup = new OptionGroup();
		optiongroup.setMultiSelect(false);
		optiongroup.addStyleName("horizontal");
		optiongroup.addItem("Finger Printing");
		optiongroup.addItem("Mapping");
		optiongroup.setImmediate(true);
		optiongroup.addListener(new Component.Listener() {
			private static final long serialVersionUID = 1L;
			public void componentEvent(Event event) {
				Object value = optiongroup.getValue();
				strSelectedPolymorphicType = value.toString();
				retrievePolymorphicMarker.setPolymorphicType(strSelectedPolymorphicType);
				
				try {
					listOfNamesForLine1 = retrievePolymorphicMarker.getNamesForRetrievePolymorphic();
					//System.out.println("......................  :"+listOfNamesForLine1);
					
					if (null != selectMarker1 && 0 != selectMarker1.getItemIds().size()) {
						selectMarker1.removeAllItems();
						Object itemId1 = selectMarker1.addItem();
						selectMarker1.setItemCaption(itemId1, "Select Line-1");
						selectMarker1.setValue(itemId1);
					}
					
					if (null != listOfNamesForLine1){
						for (Name name : listOfNamesForLine1) {
							selectMarker1.addItem(name.getNval());
						}
					}
					
				} catch (GDMSException e1) {
					//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names for Line1", Notification.TYPE_ERROR_MESSAGE);
					//return null;
				}
			}
		});
		optiongroup.select(0);
		
		
		Label lblPolymorphicType = new Label("Polymorphic Type:");
		lblPolymorphicType.setStyleName(Reindeer.LABEL_H2);

		HorizontalLayout horizLytForOptionGroup = new HorizontalLayout();
		horizLytForOptionGroup.addComponent(lblPolymorphicType);
		horizLytForOptionGroup.addComponent(optiongroup);
		horizLytForOptionGroup.setWidth("500px");
		horizLytForOptionGroup.setMargin(true, false, true, true);
		
		Label lblSearchConditions = new Label("Search Conditions");
		lblSearchConditions.setStyleName(Reindeer.LABEL_H2);
		selectLinesLayout.addComponent(lblSearchConditions);
		selectLinesLayout.setComponentAlignment(lblSearchConditions, Alignment.TOP_CENTER);
		selectLinesLayout.addComponent(horizLytForOptionGroup);
		
		
		Label lblAnd = new Label("and");
		lblAnd.setStyleName(Reindeer.LABEL_H2);

		/*final ComboBox selectMarker1 = new ComboBox();
		Object itemId1 = selectMarker1.addItem();
		selectMarker1.setItemCaption(itemId1, "Select Line-1");
		selectMarker1.setValue(itemId1);
		selectMarker1.setNullSelectionAllowed(false);
		selectMarker1.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);

		selectMarker1.setImmediate(true);*/
		/*final RetrievePolymorphicMarker retrievePolymorphicMarker = new RetrievePolymorphicMarker();
		retrievePolymorphicMarker.setPolymorphicType(strSelectedPolymorphicType);
		try {
			listOfNamesForLine1 = retrievePolymorphicMarker.getNamesForRetrievePolymorphic();
		} catch (GDMSException e1) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names for Line1", Notification.TYPE_ERROR_MESSAGE);
			//return null;
		}*/

		if (null != listOfNamesForLine1){
			for (Name name : listOfNamesForLine1) {
				selectMarker1.addItem(name.getNval());
			}
		}
		
	
		final ComboBox selectMarker2 = new ComboBox();
		Object itemId2 = selectMarker2.addItem();
		selectMarker2.setItemCaption(itemId2, "Select Line-2");
		selectMarker2.setValue(itemId2);
		selectMarker2.setNullSelectionAllowed(false);
		selectMarker2.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		selectMarker1.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				String strSelectedNameValue = "";
				if (null != selectMarker1.getValue()) {
					strSelectedNameValue = selectMarker1.getValue().toString();
				}
				Integer iGermplasmId = 0;

				if (null != listOfNamesForLine2) {
					listOfNamesForLine2.clear();
				}
				
				for (Name name : listOfNamesForLine1) {
				
					if (name.getNval().equals(strSelectedNameValue)){
						iGermplasmId = name.getGermplasmId();
						/*System.out.println("   ,,,,,,,,,,,,,   line 1 selected:"+iGermplasmId);
						System.out.println("%%%%%%%%%%%%%%%%%%%%%% option seleted:"+strSelectedPolymorphicType);*/
						try {
							//listOfNamesForLine2 = retrievePolymorphicMarker.getNames(iGermplasmId);
							listOfNamesForLine2 = retrievePolymorphicMarker.getNames(strSelectedNameValue, strSelectedPolymorphicType);
							break;
						} catch (GDMSException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names for Line2", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}
				}
				
				if (null != selectMarker2 && 0 != selectMarker2.getItemIds().size()) {
					selectMarker2.removeAllItems();
					Object itemId2 = selectMarker2.addItem();
					selectMarker2.setItemCaption(itemId2, "Select Line-2");
					selectMarker2.setValue(itemId2);
				}
				
				if (null != listOfNamesForLine2){
					for (Name name2 : listOfNamesForLine2) {
						String nval = name2.getNval();
						if (false == nval.equals(strSelectedNameValue)) {
							selectMarker2.addItem(name2.getNval());
						}
					}
				}
			}
		});

		
		HorizontalLayout horizontalLayoutForSelectComponents = new HorizontalLayout();
		horizontalLayoutForSelectComponents.addComponent(selectMarker1);
		horizontalLayoutForSelectComponents.addComponent(lblAnd);
		horizontalLayoutForSelectComponents.addComponent(selectMarker2);
		horizontalLayoutForSelectComponents.setWidth("500px");
		horizontalLayoutForSelectComponents.setMargin(true, false, true, true);
		selectLinesLayout.addComponent(horizontalLayoutForSelectComponents);
		
		
		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		selectLinesLayout.addComponent(layoutForButton);
		selectLinesLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				
				if (null == optiongroup.getValue()) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select a valid polymorphic type.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
					
				strSelectedPolymorphicType = optiongroup.getValue().toString();
				//System.out.println("polymorphic type="+strSelectedPolymorphicType);
				ArrayList<String> listOfGermplasmNames = new ArrayList<String>();

				strLine1 = selectMarker1.getValue().toString();
				boolean bValidLine1 = false;
				if (null != listOfNamesForLine1){
					for (Name name : listOfNamesForLine1){
						String nval = name.getNval();
						if (nval.equals(strLine1)){
							bValidLine1 = true;
						}
					}
				}
				if (bValidLine1){
					listOfGermplasmNames.add(strLine1);
				}

				
				strLine2 = selectMarker2.getValue().toString();
				boolean bValidLine2 = false;
				if (null != listOfNamesForLine2){
					for (Name name : listOfNamesForLine2){
						String nval = name.getNval();
						if (nval.equals(strLine2)){
							bValidLine2 = true;
						}
					}
				}
				if (bValidLine2){
					listOfGermplasmNames.add(strLine2);
				}
				
				if (0 == listOfGermplasmNames.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select valid lines to be viewed on Map and click Next", Notification.TYPE_ERROR_MESSAGE);
					return;
				} else if (2 != listOfGermplasmNames.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select valid lines to be viewed on Map and click Next", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				/*GermplasmDataManagerImpl germplasmDataManagerImpl = new GermplasmDataManagerImpl();
				germplasmDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
				germplasmDataManagerImpl.setSessionProviderForCentral(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral());*/
				GermplasmDataManager manager = factory.getGermplasmDataManager();
				GenotypicDataManager genoManager=factory.getGenotypicDataManager();
			
				
				boolean bDataRetrievedForNextTab = false;
				try {

					List<GidNidElement> gidAndNidByGermplasmNames = manager.getGidAndNidByGermplasmNames(listOfGermplasmNames);
					ArrayList<Integer> listofGIDs = new ArrayList<Integer>();
					if (null != gidAndNidByGermplasmNames){
						for (GidNidElement gidNidElement : gidAndNidByGermplasmNames){
							if (false == listofGIDs.contains(gidNidElement.getGermplasmId())){
								listofGIDs.add(gidNidElement.getGermplasmId());
							}
						}
					}

					/**
					 * commented by kalyani
					 */
					
					/*
					AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
					accMetadataSetDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
					long countAccMetadataSetByGids = accMetadataSetDAOLocal.countAccMetadataSetByGids(listofGIDs);
					List<AccMetadataSetPK> accMetadataSetByGidsLocal = accMetadataSetDAOLocal.getAccMetadataSetByGids(listofGIDs, 0, (int)countAccMetadataSetByGids);


					AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
					accMetadataSetDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
					long countAccMetadataSetByGids2 = accMetadataSetDAOCentral.countAccMetadataSetByGids(listofGIDs);
					List<AccMetadataSetPK> accMetadataSetByGidsCentral = accMetadataSetDAOCentral.getAccMetadataSetByGids(listofGIDs, 0, (int)countAccMetadataSetByGids2);
 					*/
					/***
					 * added by kalyani
					 * 
					 */
					List<AccMetadataSetPK> accMetadataSetByGids = genoManager.getGdmsAccMetadatasetByGid(listofGIDs, 0, (int)genoManager.countGdmsAccMetadatasetByGid(listofGIDs));
					//genoManager.get
					ArrayList<Integer> listOfNids = new ArrayList<Integer>();
					
					if (null != accMetadataSetByGids){
						for (AccMetadataSetPK accMetadataSetPK : accMetadataSetByGids){
							Integer nameId = accMetadataSetPK.getNameId();
							if (false == listOfNids.contains(nameId)){
								listOfNids.add(nameId);
							}
						}
					}
					Name names = null;
					String gids="";
					//System.out.println(",,,,,,,,,,,,,,,,,,,,  :"+nidList);
					for(int n=0;n<listOfNids.size();n++){
						names=manager.getGermplasmNameByID(Integer.parseInt(listOfNids.get(n).toString()));
						gids=gids+names.getGermplasmId()+",";
					}
					ArrayList QTLList=new ArrayList();
					QTLList.add(1);
					
					/*QtlDAO qtlDetailsDAOCentral = new QtlDAO();
					qtlDetailsDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
					
					try {
						//List<QtlDetailElement> listOfAllQtlDetailsCentral = qtlDetailsDAOCentral.getQtlDetailsByQTLIDs(QTLList, 0, 2);
						System.out.println("%%%%%%%%%%%%%%%%%%%%%%%   :"+qtlDetailsDAOCentral.getQtlDetailsByQTLIDs(QTLList, 0, 2));
						
						
					} catch (MiddlewareQueryException e1) {
						_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Qtl Detail objects", Notification.TYPE_ERROR_MESSAGE);
						return;
					}*/
					
					//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%   :"+genoManager.getQtlDetailsByQTLIDs(QTLList, 0, 12));
					gids = gids.substring(0,gids.length()-1);
					//System.out.println("gids=:"+gids);
					String[] gidsO=gids.split(",");
					int gid1=Integer.parseInt(gidsO[0]);
					int gid2=Integer.parseInt(gidsO[1]);
					ArrayList markersListforQ=new ArrayList();
					
					/*if (null != accMetadataSetByGidsLocal){
						for (AccMetadataSetPK accMetadataSetPK : accMetadataSetByGidsLocal){
							Integer nameId = accMetadataSetPK.getNameId();
							if (false == listOfNids.contains(nameId)){
								listOfNids.add(nameId);
							}
						}
					}

					if (null != accMetadataSetByGidsCentral) {
						for (AccMetadataSetPK accMetadataSetPK : accMetadataSetByGidsCentral){
							Integer nameId = accMetadataSetPK.getNameId();
							if (false == listOfNids.contains(nameId)){
								listOfNids.add(nameId);
							}
						}
					}*/


					/*MarkerDAO markerDAOForLocal = new MarkerDAO();
					markerDAOForLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
					List<MarkerNameElement> markerNamesByGIdsLocal = markerDAOForLocal.getMarkerNamesByGIds(listofGIDs);


					MarkerDAO markerDAOForCentral = new MarkerDAO();
					markerDAOForCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
					List<MarkerNameElement> markerNamesByGIdsCentral = markerDAOForCentral.getMarkerNamesByGIds(listofGIDs);

					listofMarkers = new ArrayList<String>();
					if (null != markerNamesByGIdsLocal) {
						for (MarkerNameElement markerNameElement : markerNamesByGIdsLocal){
							String markerName = markerNameElement.getMarkerName();
							if (false == listofMarkers.contains(markerName)){
								listofMarkers.add(markerName);
							}
						}
					}
					
					if (null != markerNamesByGIdsCentral) {
						for (MarkerNameElement markerNameElement : markerNamesByGIdsCentral){
							String markerName = markerNameElement.getMarkerName();
							if (false == listofMarkers.contains(markerName)){
								listofMarkers.add(markerName);
							}
						}
					}
					 */
					/**
					 * added by kalyani on 12-11-2013
					 * 
					 */
					List<AllelicValueElement> mappingAlleleValuesForPolymorphicMarkersRetrieval = null;
					//System.out.println("listofGIDs=:"+listofGIDs);
					ArrayList<Integer> listOfDatasetIDs = new ArrayList<Integer>();
					ArrayList<String> listOfAlleleBinValue = new ArrayList<String>();
					
					
					
					if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
						mappingAlleleValuesForPolymorphicMarkersRetrieval =genoManager.getMappingAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs, 0, (int)genoManager.countMappingAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs));
						if (null != mappingAlleleValuesForPolymorphicMarkersRetrieval) {
							for  (AllelicValueElement allelicValueElement : mappingAlleleValuesForPolymorphicMarkersRetrieval){
								Integer datasetId = allelicValueElement.getDatasetId();
								String alleleBinValue = allelicValueElement.getAlleleBinValue();
								//System.out.println("allelicValueElement=:"+allelicValueElement);
								listOfDatasetIDs.add(datasetId);
								if(! markersListforQ.contains(allelicValueElement.getMarkerName())){
									markersListforQ.add(allelicValueElement.getMarkerName());
								}
								listOfAlleleBinValue.add(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerName()+"!~!"+allelicValueElement.getData());
								addValues(allelicValueElement.getGid(), allelicValueElement.getMarkerName());	
							}
						}
					}else{
						int charCount=(int)genoManager.countCharAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs);
						int alleleCount=(int)genoManager.countIntAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs);
						if(charCount>0){
							mappingAlleleValuesForPolymorphicMarkersRetrieval=genoManager.getCharAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs, 0,  (int) genoManager.countCharAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs));
							if (null != mappingAlleleValuesForPolymorphicMarkersRetrieval) {
								for  (AllelicValueElement allelicValueElement : mappingAlleleValuesForPolymorphicMarkersRetrieval){
									Integer datasetId = allelicValueElement.getDatasetId();
									String alleleBinValue = allelicValueElement.getAlleleBinValue();
									//System.out.println("allelicValueElement=:"+allelicValueElement);
									listOfDatasetIDs.add(datasetId);
									if(! markersListforQ.contains(allelicValueElement.getMarkerName())){
										markersListforQ.add(allelicValueElement.getMarkerName());
									}
									listOfAlleleBinValue.add(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerName()+"!~!"+allelicValueElement.getData());
									addValues(allelicValueElement.getGid(), allelicValueElement.getMarkerName());	
								}
							}
						}
						if(alleleCount>0){
							mappingAlleleValuesForPolymorphicMarkersRetrieval=genoManager.getIntAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs, 0, (int)genoManager.countIntAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs));
							if (null != mappingAlleleValuesForPolymorphicMarkersRetrieval) {
								for  (AllelicValueElement allelicValueElement : mappingAlleleValuesForPolymorphicMarkersRetrieval){
									Integer datasetId = allelicValueElement.getDatasetId();
									String alleleBinValue = allelicValueElement.getAlleleBinValue();
									//System.out.println("allelicValueElement=:"+allelicValueElement);
									listOfDatasetIDs.add(datasetId);
									if(! markersListforQ.contains(allelicValueElement.getMarkerName())){
										markersListforQ.add(allelicValueElement.getMarkerName());
									}
									listOfAlleleBinValue.add(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerName()+"!~!"+allelicValueElement.getData());
									addValues(allelicValueElement.getGid(), allelicValueElement.getMarkerName());	
								}
							}
						}
						
						//mappingAlleleValuesForPolymorphicMarkersRetrieval =genoManager.get.getMappingAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs, 0, (int)genoManager.countMappingAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs));
					}
					
					
					//System.out.println(".......:"+hashMap);
					mList1=hashMap.get(gid1);
					mList2=hashMap.get(gid2);
					/*System.out.println("mList1=:"+mList1);
					System.out.println("mList2=:"+mList2);*/
					//System.out.println("copmmon Markers="+mFinalList);
					 int s=0;
					
					 if((hashMap.get(gid1).size())>(hashMap.get(gid2).size())){					
						for(int ml=0; ml<mList2.size();ml++){
							if(mList1.contains(mList2.get(ml))){
								if(!(mFinalList.contains(mList2.get(ml))))
									mFinalList.add(mList2.get(ml));
							}
						}					
					}else if((hashMap.get(gid1).size())<(hashMap.get(gid2).size())){
						for(int ml=0; ml<mList1.size();ml++){
							if(mList2.contains(mList1.get(ml))){
								if(!(mFinalList.contains(mList1.get(ml))))
								mFinalList.add(mList1.get(ml));
							}
						}
					}else if((hashMap.get(gid1).size())==(hashMap.get(gid2).size())){
						for(int ml=0; ml<mList2.size();ml++){
							if(mList1.contains(mList2.get(ml))){
								if(!(mFinalList.contains(mList2.get(ml))))
									mFinalList.add(mList2.get(ml));
							}
						}
					}
					 geno1.clear();geno2.clear();mark1.clear(); mark2.clear();ch1.clear();ch2.clear();
					 for(int c=0;c<listOfAlleleBinValue.size();c++){	
						 ///System.out.println(".............:"+chVal.get(c));
						 String arr[]=new String[3];
							StringTokenizer stz = new StringTokenizer(listOfAlleleBinValue.get(c).toString(), "!~!");
				    		//arrList6 = new String[stz.countTokens()];
				    		int i1=0;				  
				    		while(stz.hasMoreTokens()){				    			
				    			arr[i1] = stz.nextToken();
				    			i1++;
				    		}
						//str1=chVal.get(c).toString().split("!~!");
						if((Integer.parseInt(arr[0])==(gid1))&&(mFinalList.contains(arr[1]))){
							geno1.add(arr[0]);
							mark1.add(arr[1]);
							ch1.add(arr[2]);					
						}else if((Integer.parseInt(arr[0])==(gid2))&&(mFinalList.contains(arr[1]))){
							geno2.add(arr[0]);
							mark2.add(arr[1]);
							ch2.add(arr[2]);
						}			
					}
					
					/*System.out.println("CH1:"+ch1);
					System.out.println("CH2:"+ch2);
					System.out.println("Mark1=:"+mark1);
					System.out.println("Mark2=:"+mark2);
					System.out.println("Geno1=:"+geno1);*/
					missingList=new ArrayList();
					listofMarkers = new ArrayList<String>();
					//markersList= new ArrayList();
						for(int k=0;k<geno1.size();k++){
							if((!(ch2.get(k).equals("N")||ch2.get(k).equals("?")||ch2.get(k).equals("-")))&&(!(ch1.get(k).equals("N")||ch1.get(k).equals("?")||ch1.get(k).equals("-")))&&(!(ch1.get(k).equals(ch2.get(k))))){
								if (false == listofMarkers.contains(mark1.get(k).toString())){
								//if(!listofMarkers.contains(mark1.get(k).toString())){
									listofMarkers.add(mark1.get(k).toString());
									//markersList.add(mark1.get(k).toString());
									/*markers=markers+"'"+mark1.get(k)+"',";	
									markerCount++;*/
								}
							}
							if((ch1.get(k).equals("?"))||(ch2.get(k).equals("?"))||(ch1.get(k).equals("N"))||(ch2.get(k).equals("N"))||(ch1.get(k).equals("-"))||(ch2.get(k).equals("-"))){
								if(!missingList.contains(mark1.get(k))){
									missingList.add(mark1.get(k));
								}
							}
						}
					 
						//System.out.println("listofMarkers:"+listofMarkers);
					bDataRetrievedForNextTab = true;
					
				} catch (MiddlewareQueryException e) {
					bDataRetrievedForNextTab = false;
					_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving list of NIDs and GIDs using Germplasm names.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				if (bDataRetrievedForNextTab){
					Component newPolymorphicResultComponent = buildPolymorphicResultComponent();
					_tabsheetForPolymorphicMarkers.replaceComponent(polymorphicResultComponent, newPolymorphicResultComponent);
					polymorphicResultComponent.requestRepaint();
					polymorphicResultComponent = newPolymorphicResultComponent;
					_tabsheetForPolymorphicMarkers.getTab(1).setEnabled(true);
					_tabsheetForPolymorphicMarkers.setSelectedTab(polymorphicResultComponent);
				}
			}
		});
		
		return selectLinesLayout;
	}


	private Component buildPolymorphicResultComponent() {
		VerticalLayout resultsLayoutForPolymorphicMarkers = new VerticalLayout();
		resultsLayoutForPolymorphicMarkers.setCaption("Results");
		resultsLayoutForPolymorphicMarkers.setSpacing(true);
		resultsLayoutForPolymorphicMarkers.setMargin(true, true, true, true);

		int iNumOfMarkers = 0;
		if (null != listofMarkers){
			iNumOfMarkers = listofMarkers.size();
		}

		Label lblPolymorphicMarkersFound = new Label(iNumOfMarkers + "  Markers are Polymorphic between '"+strLine1+"' and '"+strLine2+"'");
		lblPolymorphicMarkersFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayoutForPolymorphicMarkers.addComponent(lblPolymorphicMarkersFound);
		resultsLayoutForPolymorphicMarkers.setComponentAlignment(lblPolymorphicMarkersFound, Alignment.TOP_CENTER);
		
		
		//20131112: Tulasi --- Created the tableForMissingMarkers component to display the list of Markers with missing data
		
		int iNumOfMissingMarkers = 0;
		if (null != missingList){
			iNumOfMissingMarkers = missingList.size();
		}
		
		Label lblMissingMarkersFound = new Label(iNumOfMissingMarkers + "  markers are with missing data.");
		lblMissingMarkersFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayoutForPolymorphicMarkers.addComponent(lblMissingMarkersFound);
		resultsLayoutForPolymorphicMarkers.setComponentAlignment(lblMissingMarkersFound, Alignment.TOP_CENTER);
		
		ListSelect listComponentForMissingMarkers = new ListSelect();
		
		if (null != missingList){
			
			//20131205: Tulasi --- Displaying the list of missing markers in a ListSelect component instead of a table
			Object itemId1 = listComponentForMissingMarkers.addItem();
			listComponentForMissingMarkers.setItemCaption(itemId1, "Missing Markers");
			listComponentForMissingMarkers.setValue(itemId1);
			for (Object objMissingMarker : missingList) {
				String strMissingMarker = (String) objMissingMarker;
				listComponentForMissingMarkers.addItem(strMissingMarker);
			}
			listComponentForMissingMarkers.setColumns(10);
			listComponentForMissingMarkers.setNewItemsAllowed(false);
			listComponentForMissingMarkers.setWidth("100%");
			listComponentForMissingMarkers.setNullSelectionAllowed(false);
			listComponentForMissingMarkers.setImmediate(true);
			//20131205: Tulasi --- Displaying the list of missing markers in a ListSelect component instead of a table
			
		}
		//20131112: Tulasi --- Created the tableForMissingMarkers component to display the list of Markers with missing data

		final TwinColSelect selectForMarkers = new TwinColSelect();
		selectForMarkers.setLeftColumnCaption("All Markers");
		selectForMarkers.setRightColumnCaption("Selected Markers");
		selectForMarkers.setNullSelectionAllowed(false);
		selectForMarkers.setInvalidAllowed(false);
		if (null != listofMarkers) {
			for (String strMarker : listofMarkers) {
				selectForMarkers.addItem(strMarker);
			}
		}
		selectForMarkers.setRows(20);
		selectForMarkers.setColumns(25);
		
		selectForMarkers.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                	TwinColSelect colSelect = (TwinColSelect)event.getProperty();
                	Object value = colSelect.getValue();
                	Set<String> hashSet = (Set<String>) value;
                	
                	if (null == listOfMarkersSelected){
                		listOfMarkersSelected = new ArrayList<String>();
                	}
                	
                	for (String string : hashSet) {
						listOfMarkersSelected.add(string);
					}
                	//System.out.println(hashSet);
                }
            }
        });
		selectForMarkers.setImmediate(true);
		//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   :"+listOfMarkersSelected);
		
		HorizontalLayout horizLytForSelectComponent = new HorizontalLayout();
		horizLytForSelectComponent.setSizeFull();
		horizLytForSelectComponent.setSpacing(true);
		horizLytForSelectComponent.setMargin(true);
		horizLytForSelectComponent.addComponent(selectForMarkers);
		
		
		final ComboBox selectMap = new ComboBox();
		Object itemId = selectMap.addItem();
		selectMap.setItemCaption(itemId, "Select Map");
		selectMap.setValue(itemId);
		selectMap.setNullSelectionAllowed(false);
		selectMap.setImmediate(true);

		MapDAO mapDAOLocal = new MapDAO();
		mapDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		
		MapDAO mapDAOCentral = new MapDAO();
		mapDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());

		final ArrayList<Map> listOfAllMaps = new ArrayList<Map>();
		List<Integer> markerIds=new ArrayList();
		try {
			/*if (null != listofMarkers) {
				 List<Integer> markerIdsC = genoManager.getMarkerIdsByMarkerNames(listofMarkers, 0, listofMarkers.size(), Database.CENTRAL);
				 List<Integer> markerIdsL = genoManager.getMarkerIdsByMarkerNames(listofMarkers, 0, listofMarkers.size(), Database.LOCAL);
				if(!(markerIdsC.isEmpty())){
					for(int m=0; m<markerIdsC.size(); m++){					
						markerIds.add(markerIdsC.get(m));
					}
				}
				if(!(markerIdsL.isEmpty())){
					for(int ml=0;ml<markerIdsL.size(); ml++){
						markerIds.add(markerIdsL.get(ml));
					}
				}
				System.out.println("markerIds   ,,,,,,,,,,,,,,,,,,,,   :"+markerIds);
				System.out.println("MAP WITH Marker count=:"+genoManager.getMapAndMarkerCountByMarkers(markerIds));
				List<MapDetailElement> details = genoManager.getMapAndMarkerCountByMarkers(markerIds));
		        if (details != null && details.size() > 0) {
		            //Debug.println(0, "FOUND " + details.size() + " records");
		            for (MapDetailElement detail : details) {
		                Debug.println(0, detail.getMapName() + " - " + detail.getMarkerCount());
		            }
		        } else {
		            Debug.println(0, "NO RECORDS FOUND");
		        }
				
				
			
			}*/
			
			List<Map> listOfAllMapsFromLocal = mapDAOLocal.getAll();
			List<Map> listOfAllMapsFromCentral = mapDAOCentral.getAll();
			
			if (null != listOfAllMapsFromLocal){
				for (Map map : listOfAllMapsFromLocal){
					if (false == listOfAllMaps.contains(map)){
						listOfAllMaps.add(map);
					}
				}
			}
			if (null != listOfAllMapsFromCentral){
				for (Map map : listOfAllMapsFromCentral){
					if (false == listOfAllMaps.contains(map)){
						listOfAllMaps.add(map);
					}
				}
			}
			
		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Maps",  Notification.TYPE_ERROR_MESSAGE);
			return null;
		}

		if (null != listOfAllMaps){
			for (int i = 0; i < listOfAllMaps.size(); i++){
				Map map = listOfAllMaps.get(i);
				String mapName = map.getMapName();
				Integer mapId = map.getMapId();
				selectMap.addItem(mapName);
				if (false == hmOfMap.containsKey(mapName)){
					hmOfMap.put(mapName, mapId);
				}
			}
		}

		
		//VerticalLayout layoutForButton = new VerticalLayout();
		HorizontalLayout layoutForButton = new HorizontalLayout();
		Button btnViewOnMap = new Button("View On Map");
		btnViewOnMap.addListener(new Button.ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				strSelectedMapName = selectMap.getValue().toString();
				iSelectedMapId = hmOfMap.get(strSelectedMapName);
				
				boolean bValidMapSelected = false;
				if (null == strSelectedMapName){
					
				} else {
					if (null != listOfAllMaps){
						for (int i = 0; i < listOfAllMaps.size(); i++){
							Map map = listOfAllMaps.get(i);
							String mapName = map.getMapName();
							selectMap.addItem(mapName);
							if (strSelectedMapName.equals(mapName)){
								bValidMapSelected = true;
								break;
							}
						}
					}
				}
				
				if (!bValidMapSelected){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select a valid Map to display the markers.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				//System.out.println("listOfMarkersSelected=:"+listOfMarkersSelected);
				if (null != listOfMarkersSelected){
					retrieveMappingDataBetweenLines();
					//System.out.println("@@@@@@@@@@@@@@@  finalListOfMappingData:"+finalListOfMappingData);
				}

				if (null != finalListOfMappingData){
					if (0 == finalListOfMappingData.size()){
						_mainHomePage.getMainWindow().getWindow().showNotification("Mapping Data could not be retrieved for selected Markers.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}
				
				Component newPolymorphicMapComponent = buildPolymorphicMapComponent();
				_tabsheetForPolymorphicMarkers.replaceComponent(buildPolymorphicMapComponent, newPolymorphicMapComponent);
				buildPolymorphicMapComponent.requestRepaint();
				buildPolymorphicMapComponent = newPolymorphicMapComponent;
				_tabsheetForPolymorphicMarkers.getTab(2).setEnabled(true);
				_tabsheetForPolymorphicMarkers.setSelectedTab(buildPolymorphicMapComponent);
			}
		});		
		
		if (0 == selectForMarkers.size()){
			btnViewOnMap.setEnabled(false);
			selectMap.setEnabled(false);
		}

		layoutForButton.addComponent(btnViewOnMap);
		layoutForButton.setComponentAlignment(btnViewOnMap, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		
		//20131206: Tulasi --- Added a new button for KBio order form
		//Button btnViewKBioOrderForm = new Button("View KBio Order Form");
		Button btnViewKBioOrderForm = new Button("Create KBio Order Form");
		btnViewKBioOrderForm.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				// 20131512 : Kalyani added to create kbio order form
				String mType="SNP";
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				ArrayList <String> markersForKBio=new ArrayList();		
				try {
					List<String> snpMarkers=genoManager.getMarkerNamesByMarkerType(mType, 0, (int)genoManager.countMarkerNamesByMarkerType(mType));
					if(!(snpMarkers.isEmpty())){
						for(int m=0; m<listOfMarkersSelected.size();m++){
							if(snpMarkers.contains(listOfMarkersSelected.get(m))){
								markersForKBio.add(listOfMarkersSelected.get(m));
							}
						}

						if(!(markersForKBio.isEmpty())){

							orderFormForPlymorphicMarkers=exportFileFormats.exportToKBio(markersForKBio, _mainHomePage);
							FileResource fileResource = new FileResource(orderFormForPlymorphicMarkers, _mainHomePage);
							//_mainHomePage.getMainWindow().getWindow().open(fileResource, "", true);
							_mainHomePage.getMainWindow().getWindow().open(fileResource, "KBio Order Form", true);
						}
						
						else{
							_mainHomePage.getMainWindow().getWindow().showNotification("Selected Marker(s) are not SNPs", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}else{
						_mainHomePage.getMainWindow().getWindow().showNotification("No SNP Marker(s) to create KBio Order form", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
				} catch (Exception e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error generating KBioOrder Form", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		

		/*Link gtpExcelFileLink = new Link("Download KBio Order Form", new ExternalResource(""));

		 if (null != orderFormForPlymorphicMarkers){
			gtpExcelFileLink = new Link("Download KBio Order Form", new FileDownloadResource(
					orderFormForPlymorphicMarkers, _mainHomePage.getMainWindow().getWindow().getApplication()));
		}
		gtpExcelFileLink.setTargetName("_blank");*/
		
		layoutForButton.addComponent(btnViewKBioOrderForm);
		layoutForButton.setComponentAlignment(btnViewKBioOrderForm, Alignment.MIDDLE_CENTER);
		layoutForButton.setSpacing(true);
		//20131206: Tulasi --- Added a new button for KBio order form
		
		resultsLayoutForPolymorphicMarkers.addComponent(horizLytForSelectComponent);
		resultsLayoutForPolymorphicMarkers.addComponent(selectMap);
		resultsLayoutForPolymorphicMarkers.addComponent(layoutForButton);
		resultsLayoutForPolymorphicMarkers.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);
		
		//20131112: Tulasi --- Added the tableForMissingMarkers component for the final results layout
		resultsLayoutForPolymorphicMarkers.addComponent(lblMissingMarkersFound);
		resultsLayoutForPolymorphicMarkers.setComponentAlignment(lblMissingMarkersFound, Alignment.MIDDLE_CENTER);
		resultsLayoutForPolymorphicMarkers.addComponent(listComponentForMissingMarkers);
		resultsLayoutForPolymorphicMarkers.setComponentAlignment(listComponentForMissingMarkers, Alignment.MIDDLE_CENTER);
		//20131112:  Tulasi --- Added the tableForMissingMarkers component for the final results layout
		
		if (null == listofMarkers){
			selectMap.setEnabled(false);
			btnViewOnMap.setEnabled(false);
			selectForMarkers.setEnabled(false);
			listComponentForMissingMarkers.setEnabled(false);
		} else {
			selectMap.setEnabled(true);
			btnViewOnMap.setEnabled(true);
			selectForMarkers.setEnabled(true);
			listComponentForMissingMarkers.setEnabled(true);
		}

		return resultsLayoutForPolymorphicMarkers;
	}


	private Component buildPolymorphicMapComponent() {
		VerticalLayout resultsLayoutForPolymorphicMaps = new VerticalLayout();
		resultsLayoutForPolymorphicMaps.setCaption("Map");
		resultsLayoutForPolymorphicMaps.setSpacing(true);
		resultsLayoutForPolymorphicMaps.setMargin(true, true, true, true);

		/*if (null != listofMarkers){
			retrieveMappingDataBetweenLines();
		}*/
		/*if (null != listOfMarkersSelected){
			retrieveMappingDataBetweenLines();
		}*/

		int iTotalPolymorphicMarkers = 0;
		int iMarkersOnMap = 0;
		if (null != finalListOfMappingData){
			//iTotalPolymorphicMarkers = listofMarkers.size();
			iTotalPolymorphicMarkers = listOfMarkersSelected.size();
			iMarkersOnMap = finalListOfMappingData.size();
		}


		Label lblMapsFound = new Label("Out of  " +  iTotalPolymorphicMarkers + "  polymorphic markers only  " + iMarkersOnMap  + "  are on Map");
		lblMapsFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayoutForPolymorphicMaps.addComponent(lblMapsFound);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(lblMapsFound, Alignment.TOP_CENTER);
		
		
		_tableForMarkerResults = new Table();
		_tableForMarkerResults.setWidth("100%");
		_tableForMarkerResults.setPageLength(10);
		_tableForMarkerResults.setSelectable(true);
		_tableForMarkerResults.setColumnCollapsingAllowed(false);
		_tableForMarkerResults.setColumnReorderingAllowed(false);
		_tableForMarkerResults.setStyleName("strong");


		_tableForMarkerResults.addContainerProperty("", CheckBox.class, null);
		_tableForMarkerResults.addContainerProperty("Marker", String.class, null);
		_tableForMarkerResults.addContainerProperty("Map", String.class, null);
		_tableForMarkerResults.addContainerProperty("Chromosome", String.class, null);
		_tableForMarkerResults.addContainerProperty("Position", String.class, null);
		_tableForMarkerResults.addContainerProperty("Trait", String.class, null);
	
		ArrayList<MappingData> arrayListOfSortedData = sortFinalListOfMappingData();
		//ArrayList<MappingData> arrayListOfSortedData = sortFinalListOfMappingData();
		
		if (null != finalListOfMappingData){
			_tableForMarkerResults.setEnabled(true);
			arrayOfCheckBoxes = new CheckBox[finalListOfMappingData.size()];
			for (int i = 0; i < finalListOfMappingData.size(); i++){

				MappingData mappingData = finalListOfMappingData.get(i);

				String strLinkageGroup = mappingData.getLinkageGroup();
				//Integer mapId = mappingData.getMapId();
				String strMapName = mappingData.getMapName();
				//String mapUnit = mappingData.getMapUnit();
				Integer markerId = mappingData.getMarkerId();
				String markerName = mappingData.getMarkerName();
				float startPosition = mappingData.getStartPosition();
				String strQtlTrait = hmOfMarkerIDAndQtlTrait.get(markerId);
				arrayOfCheckBoxes[i] = new CheckBox();
				_tableForMarkerResults.addItem(new Object[] {arrayOfCheckBoxes[i], markerName, strMapName, strLinkageGroup, startPosition, strQtlTrait}, new Integer(i));
			}
		} else {
			_tableForMarkerResults.setEnabled(false);
		}
		checkBox = new CheckBox();
		checkBox.setCaption("Check Trait(s)");
		checkBox.setImmediate(true);
		checkBox.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) checkBox.getValue()){
					if (null != finalListOfMappingData){
						for (int i = 0; i < finalListOfMappingData.size(); i++){
							MappingData mappingData = finalListOfMappingData.get(i);
							Integer markerId = mappingData.getMarkerId();
							String strQtlTrait = hmOfMarkerIDAndQtlTrait.get(markerId);
							
							if (null != strQtlTrait){
								if (false == strQtlTrait.equals("")){
									arrayOfCheckBoxes[i].setValue(true);
								}
							}
						}
					}
				}
			}
		});
		
		final ComboBox selectTrait = new ComboBox();
		Object itemId1 = selectTrait.addItem();
		selectTrait.setItemCaption(itemId1, "Select Trait");
		selectTrait.setValue(itemId1);
		selectTrait.setNullSelectionAllowed(false);
		if (null != listofTraits){
			for (String strTrait : listofTraits){
				selectTrait.addItem(strTrait);
			}
		}

		//Building the top panel
		HorizontalLayout topHorizontalLayout = new HorizontalLayout();
		topHorizontalLayout.setSpacing(true);

		Label lblBinSize = new Label("Bin Size");
		lblBinSize.setStyleName(Reindeer.LABEL_SMALL);
		topHorizontalLayout.addComponent(lblBinSize);

		final TextField txtBinSize = new TextField();
		txtBinSize.setMaxLength(4);
		txtBinSize.setImmediate(true);
		txtBinSize.setDescription("Bin Size can be max of 4 digits");
		txtBinSize.setTextChangeEventMode(TextChangeEventMode.EAGER);
		final Hashtable<String, String> htTraitList = new Hashtable<String, String>();		
		String regexp = "[0-9.]{1,4}";
		final RegexpValidator regexpValidator = new RegexpValidator(regexp, "Not a number");
		regexpValidator.setErrorMessage("Must be a number of max 4 digits");
		txtBinSize.setNullRepresentation("");
		txtBinSize.addValidator(regexpValidator);
		final Property.ValueChangeListener valueChangelistener = new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				Object value = event.getProperty().getValue();
				//System.out.println("Value Change listener: " + value);
				String strValue = value.toString(); 
				if (0 == Integer.parseInt(strValue)){
					uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
					return;
				}
				double dData = 0.0;
				try {
					dData = Double.parseDouble(strValue);
				} catch(Throwable th) {
					uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
					return;
				}
				handleChecks(txtBinSize, checkBox, selectTrait, htTraitList);
			}
			
		};
		txtBinSize.addListener(valueChangelistener);
		VerticalLayout layoutForBinSize = new VerticalLayout();
		layoutForBinSize.addComponent(txtBinSize);
		layoutForBinSize.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			public void layoutClick(LayoutClickEvent event) {
		        if (event.getChildComponent() == txtBinSize) {
		            //System.out.println("clicked the TextField");
		            String strValue = txtBinSize.getValue().toString(); 
					if (0 == Integer.parseInt(strValue)){
						uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
						return;
					}
					double dData = 0.0;
					try {
						dData = Double.parseDouble(strValue);
					} catch(Throwable th) {
						//_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a valid real value.", Notification.TYPE_ERROR_MESSAGE);
						uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
						return;
					}
					handleChecks(txtBinSize, checkBox, selectTrait, htTraitList);
		        }
		    }
			
		});
		topHorizontalLayout.addComponent(txtBinSize);

		Label lblCM = new Label("cM");
		lblCM.setStyleName(Reindeer.LABEL_SMALL);
		topHorizontalLayout.addComponent(lblCM);

		topHorizontalLayout.addComponent(selectTrait);
		topHorizontalLayout.addComponent(checkBox);


		resultsLayoutForPolymorphicMaps.addComponent(topHorizontalLayout);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(topHorizontalLayout, Alignment.TOP_CENTER);

		resultsLayoutForPolymorphicMaps.addComponent(_tableForMarkerResults);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(_tableForMarkerResults, Alignment.MIDDLE_CENTER);
		
		HorizontalLayout layoutForExportTypes = new HorizontalLayout();
		layoutForExportTypes.setSpacing(true);
		ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		layoutForExportTypes.addComponent(excelButton);
		excelButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				exportToExcel();
			}
		});
		
		Button btnViewKBioOrderForm = new Button("Create KBio Order Form");
		btnViewKBioOrderForm.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				// 20131512 : Kalyani added to create kbio order form
				String mType="SNP";
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				ArrayList <String> markersForKBio=new ArrayList();		
				try {
					List<String> snpMarkers=genoManager.getMarkerNamesByMarkerType(mType, 0, (int)genoManager.countMarkerNamesByMarkerType(mType));
					if(!(snpMarkers.isEmpty())){
						for(int m=0; m<listOfMarkersSelected.size();m++){
							if(snpMarkers.contains(listOfMarkersSelected.get(m))){
								markersForKBio.add(listOfMarkersSelected.get(m));
							}
						}
						if(!(markersForKBio.isEmpty())) {
							orderFormForPlymorphicMarkers=exportFileFormats.exportToKBio(markersForKBio, _mainHomePage);
							FileResource fileResource = new FileResource(orderFormForPlymorphicMarkers, _mainHomePage);
							_mainHomePage.getMainWindow().getWindow().open(fileResource, "", true);
						}
						
						else{
							_mainHomePage.getMainWindow().getWindow().showNotification("Selected Marker(s) are not SNPs", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}else{
						_mainHomePage.getMainWindow().getWindow().showNotification("No SNP Marker(s) to create KBio Order form", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
				} catch (Exception e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error generating KBioOrder Form", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		layoutForExportTypes.addComponent(btnViewKBioOrderForm);
		
		//20131210: Tulasi --- Not displaying the PDF and Print buttons
		/*themeResource = new ThemeResource("images/pdf.gif");
		Button pdfButton = new Button();
		pdfButton.setIcon(themeResource);
		pdfButton.setStyleName(Reindeer.BUTTON_LINK);
		pdfButton.setDescription("PDF Format");
		layoutForExportTypes.addComponent(pdfButton);
		pdfButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				exportFileFormats.exportToPdf(_tableForMarkerResults, _mainHomePage);
			}
		});*/
		
		//20131206: Added a new button to export the data in KBio format 
		//TODO: Have to add the required icon
		//themeResource = new ThemeResource("images/pdf.gif");
		Button kbioButton = new Button();
		kbioButton.setIcon(themeResource);
		kbioButton.setStyleName(Reindeer.BUTTON_LINK);
		kbioButton.setDescription("KBio Format");
		//layoutForExportTypes.addComponent(kbioButton);
		kbioButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				//exportFileFormats.exportToKBio(listOfMarkersSelected, _mainHomePage);
				
				
				if(null == finalListOfMappingData || 0 == finalListOfMappingData.size()) {
					return;
				}
				List<String[]> listToExport = new ArrayList<String[]>();
				
				for (int i = 0; i < finalListOfMappingData.size(); i++){

					MappingData mappingData = finalListOfMappingData.get(i);					
					String markerName = mappingData.getMarkerName();					
					
					arrayOfCheckBoxes[i] = new CheckBox();
					listToExport.add(new String[] {markerName});
				}
				//System.out.println("listToExport=:"+listToExport);
				
				String mType="SNP";
				//ExportFileFormats exportFileFormats = new ExportFileFormats();
				ArrayList <String> markersForKBio=new ArrayList();		
				try {
					List<String> snpMarkers=genoManager.getMarkerNamesByMarkerType(mType, 0, (int)genoManager.countMarkerNamesByMarkerType(mType));
					if(!(snpMarkers.isEmpty())){
						for(int m=0; m<listOfMarkersSelected.size();m++){
							if(snpMarkers.contains(listOfMarkersSelected.get(m))){
								markersForKBio.add(listOfMarkersSelected.get(m));
							}
						}
						if(!(markersForKBio.isEmpty())) {
							orderFormForPlymorphicMarkers=exportFileFormats.exportToKBio(markersForKBio, _mainHomePage);

							FileResource fileResource = new FileResource(orderFormForPlymorphicMarkers, _mainHomePage);
							_mainHomePage.getMainWindow().getWindow().open(fileResource, "", true);
						}else{

							_mainHomePage.getMainWindow().getWindow().showNotification("Selected Marker(s) are not SNPs", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}else{
						_mainHomePage.getMainWindow().getWindow().showNotification("No SNP Marker(s) to create KBio Order form", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
				} catch (Exception e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error generating KBioOrder Form", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		//20131206: Added a new button to export the data in KBio format 
		
		//20131210: Tulasi --- Not displaying the PDF and Print buttons
		/*themeResource = new ThemeResource("images/print.gif");
		Button printButton = new Button();
		printButton.setIcon(themeResource);
		printButton.setStyleName(Reindeer.BUTTON_LINK);
		printButton.setDescription("Print Format");
		layoutForExportTypes.addComponent(printButton);
		printButton.addListener(new ClickListener() {
			/**
			 * 
			 *//*
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
			}
		});*/
		
		if (0 == _tableForMarkerResults.size()){
			txtBinSize.setEnabled(false);
			selectTrait.setEnabled(false);
			checkBox.setEnabled(false);
			excelButton.setEnabled(false);
			//pdfButton.setEnabled(false);
			//printButton.setEnabled(false);
		} else {
			txtBinSize.setEnabled(true);
			selectTrait.setEnabled(true);
			checkBox.setEnabled(true);
			excelButton.setEnabled(true);
			//pdfButton.setEnabled(true);
			//printButton.setEnabled(true);
		}
		
		resultsLayoutForPolymorphicMaps.addComponent(layoutForExportTypes);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		
		return resultsLayoutForPolymorphicMaps;
	}
	private ArrayList<MappingData> sortFinalListOfMappingData() {
		
		if (null != finalListOfMappingData) {
			
			Collections.sort(finalListOfMappingData, new Comparator<MappingData>() {

				@Override
				public int compare(MappingData o1, MappingData o2) {
					/*//if (0 == o1.getMarkerName().compareTo(o2.getMarkerName())) {

						if (0 == o1.getLinkageGroup().compareTo(o2.getLinkageGroup())) {

							float startPosition = o1.getStartPosition();
							float startPosition2 = o2.getStartPosition();

							if (startPosition <= startPosition2){
								return 0;
							} else {
								return 1;
							}
						}
					//}
					 */					

					return o1.getLinkageGroup().compareTo(o2.getLinkageGroup());
				}
			});
		}
		
		
		
		/*if (null != finalListOfMappingData) {
			
			Collections.sort(finalListOfMappingData, new Comparator<MappingData>() {

				@Override
				public int compare(MappingData o1, MappingData o2) {
					// TODO Auto-generated method stub
					return o1.getLinkageGroup().compareTo(o2.getLinkageGroup());
				}
			});
		}
		
		if (null != finalListOfMappingData) {
			
			Collections.sort(finalListOfMappingData, new Comparator<MappingData>() {

				@Override
				public int compare(MappingData o1, MappingData o2) {
					// TODO Auto-generated method stub
					float startPosition = o1.getStartPosition();
					float startPosition2 = o2.getStartPosition();
					
					if (startPosition <= startPosition2){
						return 0;
					} else {
						return 1;
					}
							
				}
			});
		}*/
		
		
		return null;
	}
	

	private void handleChecks(TextField txtBinSize, CheckBox checkBox,
			ComboBox selectTrait, Hashtable<String, String> htTraitList) {
		Object value = txtBinSize.getValue();
		if(null == value) {
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			return;
		}
		String strValue = value.toString();
		if (0 == strValue.trim().length()){
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			return;
		}
		double dData = 0.0;
		try {
			dData = Double.parseDouble(strValue);
		} catch(Throwable th) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a valid real value.", Notification.TYPE_ERROR_MESSAGE);
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			return;
		}
		if(null == finalListOfMappingData) {
			return;
		}
		for (int i = 0; i < finalListOfMappingData.size(); i++){
			MappingData mappingData = finalListOfMappingData.get(i);
			MappingData mappingDataNext = null;
			if((i + 1) < finalListOfMappingData.size()) {
				mappingDataNext = finalListOfMappingData.get(i+1);
			}
			if(null == mappingDataNext) {
				continue;
			}
			float startPosition = mappingData.getStartPosition();
			float startPositionNext = mappingDataNext.getStartPosition();
			if(0 == dData || dData <= (startPositionNext - startPosition)) {
				if(0 == dData) {
					arrayOfCheckBoxes[i].setValue(true);
				} else {
					handleCheckBox(checkBox, selectTrait, 0, htTraitList);
				}
				arrayOfCheckBoxes[i+1].setValue(true);
			} else {
				if(checkBox.booleanValue()) {
					handleCheckBox(checkBox, selectTrait, i + 1, htTraitList);
				} else {
					arrayOfCheckBoxes[i+1].setValue(false);
				}
			}
		}
	}
	
	private void uncheckAllCheckBox(CheckBox checkBox,
			ComboBox selectTrait, Hashtable<String, String> htTraitList) {
		if(null == finalListOfMappingData) {
			return;
		}
		if(0 == finalListOfMappingData.size()) {
			return;
		}
		
		for (int i = 0; i < finalListOfMappingData.size(); i++) {
			handleCheckBox(checkBox, selectTrait, i, htTraitList);
		}
		
	}
	
	private void handleCheckBox(CheckBox checkBox, ComboBox selectTrait, int i,
			Hashtable<String, String> htTraitList) {
		if(checkBox.booleanValue()) {
			handlechecks(selectTrait, i, htTraitList);
		} else {
			arrayOfCheckBoxes[i].setValue(false);
		}
	}

	private void handlechecks(ComboBox selectTrait, int i,
			Hashtable<String, String> htTraitList) {
		if(null == finalListOfMappingData) {
			return;
		}
		if(0 == finalListOfMappingData.size()) {
			return;
		}

		if(i >= finalListOfMappingData.size()) {
			return;
		}

		/*if(null != htTraitList) {
			QtlDetails qtlDetails = htTraitList.get(markerId + markerName);
			if(null != qtlDetails) {}
		}*/
		
		
		for (String strQtlTrait : listofTraits) {
			
			if (false == checkBox.booleanValue()){
				break;
			}
			
			if (null != strQtlTrait && 0 != strQtlTrait.length()){
				Object value = selectTrait.getValue();
				if(null != value) {
					if(value instanceof Set) {
						Set set = (Set) value;
						List<String> listOfSelectedTraits = new ArrayList<String>();
						for (Object object : set) {
							listOfSelectedTraits.add(object.toString());
						}
						for (int j = 0; j < listOfSelectedTraits.size(); j++) {
							if(null != htTraitList && htTraitList.containsKey(listOfSelectedTraits.get(j))) {
								arrayOfCheckBoxes[i].setValue(true);
							}
						}
					} else {
						if(strQtlTrait.equals(value.toString())) {
							arrayOfCheckBoxes[i].setValue(true);
						}
					}
				} else {
					arrayOfCheckBoxes[i].setValue(false);
				}
			} else {
				arrayOfCheckBoxes[i].setValue(false);
			}
			
		}
		
	}

	private void exportToExcel() {
		if(null == finalListOfMappingData || 0 == finalListOfMappingData.size()) {
			return;
		}
		List<String[]> listToExport = new ArrayList<String[]>();
		String strFileName = "tmp";
		for (int i = 0; i < finalListOfMappingData.size(); i++){

			MappingData mappingData = finalListOfMappingData.get(i);

			String strLinkageGroup = mappingData.getLinkageGroup();
			//Integer mapId = mappingData.getMapId();
			String strMapName = mappingData.getMapName();
			//String mapUnit = mappingData.getMapUnit();
			Integer markerId = mappingData.getMarkerId();
			String markerName = mappingData.getMarkerName();
			float startPosition = mappingData.getStartPosition();
			String strQtlTrait = hmOfMarkerIDAndQtlTrait.get(markerId);
			arrayOfCheckBoxes[i] = new CheckBox();
			listToExport.add(new String[] {markerName, strMapName, strLinkageGroup, String.valueOf(startPosition), strQtlTrait});
		}
		
		if(0 == listToExport.size()) {
			_mainHomePage.getMainWindow().getWindow().showNotification("No Maps to export",  Notification.TYPE_ERROR_MESSAGE);
			return;
		}

		String[] strValues = new String[] {"MARKER", "MAP", "CHROMOSOME", "POSITION", "TRAIT"};
		listToExport.add(0, strValues);
		
		
		ExportFileFormats exportFileFormats = new ExportFileFormats();
		try {
			exportFileFormats.exportMap(_mainHomePage, listToExport, strFileName);
		} catch (WriteException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
		} catch (IOException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
		}
	}


	private void retrieveMappingDataBetweenLines() {
		
		
		
		MarkerDAO markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		MarkerDAO markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());

		MarkerOnMapDAO markerOnMapDAOLocal = new MarkerOnMapDAO();
		markerOnMapDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		MarkerOnMapDAO markerOnMapDAOCentral = new MarkerOnMapDAO();
		markerOnMapDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());

		ArrayList<Integer> listOfAllMarkerIDs = new ArrayList<Integer>();
		ArrayList<MarkerOnMap> listOfAllMarkerOnMaps = new ArrayList<MarkerOnMap>();

		try {
			long countAll = markerDAOLocal.countAll();
			//List<Integer> listOfMarkerIDsByNamesLocal = markerDAOLocal.getIdsByNames(listofMarkers, 0, (int)countAll);
			List<Integer> listOfMarkerIDsByNamesLocal = markerDAOLocal.getIdsByNames(listOfMarkersSelected, 0, (int)countAll);
			long countAll2 = markerDAOCentral.countAll();
			//List<Integer> listOfMarkerIDsByNamesCentral = markerDAOCentral.getIdsByNames(listofMarkers, 0, (int)countAll2);
			List<Integer> listOfMarkerIDsByNamesCentral = markerDAOCentral.getIdsByNames(listOfMarkersSelected, 0, (int)countAll2);
			if (null != listOfMarkerIDsByNamesLocal){
				for (Integer iMarkerID : listOfMarkerIDsByNamesLocal){
					if (false == listOfAllMarkerIDs.contains(iMarkerID)){
						listOfAllMarkerIDs.add(iMarkerID);
					}
				}
			}
			if (null != listOfMarkerIDsByNamesCentral) {
				for (Integer iMarkerID : listOfMarkerIDsByNamesCentral){
					if (false == listOfAllMarkerIDs.contains(iMarkerID)){
						listOfAllMarkerIDs.add(iMarkerID);
					}
				}
			}
			System.out.println("listOfAllMarkerIDs..............:"+listOfAllMarkerIDs);
			/*List<MapInfo> results=null ;
			

			List<MapInfo> resultsC = genoManager.getMapInfoByMapName(strSelectedMapName, Database.CENTRAL);
			List<MapInfo> ResultsL= genoManager.getMapInfoByMapName(strSelectedMapName, Database.LOCAL);
			if(!(resultsC.isEmpty())){
				System.out.println("IF C not Null");
				// results=genoManager.getMapInfoByMapName(strSelectedMapName, Database.LOCAL);
	        //Debug.println(0, "testGetMapInfoByMapName(mapName=" + mapName + ") RESULTS size: " + results.size());
		        for (MapInfo mapInfoC : resultsC){
		            System.out.println("^^^^^^^^^^^^^^  :"+mapInfoC);
		            results.add(mapInfoC);
		        }
			}
			
			if(!(ResultsL.isEmpty())){
				System.out.println("IF L not null");
				for (MapInfo mapInfoL : ResultsL){
		            System.out.println("^^^^^^^^^^^^^^  :"+mapInfoL);
		            results.add(mapInfoL);
		        }
			}
			for (MapInfo mapInfo : results){
	            System.out.println("^^^^^^^^^^^^^^  :"+mapInfo);
	            
	        }*/
			
			
			//genoManager.getMapDetailsByName(strSelectedMapName, arg1, arg2)
			
			
			
			List<MarkerOnMap> listOfAllMarkerOnMapsLocal = markerOnMapDAOLocal.getAll();
			List<MarkerOnMap> listOfAllMarkerOnMapsCentral = markerOnMapDAOCentral.getAll();
			if (null != listOfAllMarkerOnMapsLocal) {
				for (MarkerOnMap markerOnMap : listOfAllMarkerOnMapsLocal){
					//if (iSelectedMapId.equals(markerOnMap.getMapId())){
					
					if (false == listOfAllMarkerOnMaps.contains(markerOnMap)){
						listOfAllMarkerOnMaps.add(markerOnMap);
					}
					//}
				}
			}
			if (null != listOfAllMarkerOnMapsCentral){
				for (MarkerOnMap markerOnMap : listOfAllMarkerOnMapsCentral){
					//if (iSelectedMapId.equals(markerOnMap.getMapId())){
						if (false == listOfAllMarkerOnMaps.contains(markerOnMap)){
							listOfAllMarkerOnMaps.add(markerOnMap);
						}
					//}
				}
			}
		} catch (MiddlewareQueryException e1) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Marker details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}


		ArrayList<MarkerOnMap> finalListOfMarkersOnMap = new ArrayList<MarkerOnMap>();
		for (int i = 0; i < listOfAllMarkerIDs.size(); i++){
			Integer iMarkerID = listOfAllMarkerIDs.get(i);
			for (int j = 0; j <  listOfAllMarkerOnMaps.size(); j++){
				MarkerOnMap markerOnMap = listOfAllMarkerOnMaps.get(j);
			
				if (iMarkerID.equals(markerOnMap.getMarkerId())){
					finalListOfMarkersOnMap.add(markerOnMap);						
				}
			}
		}
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%   &&&&&&&&&&&&&&&&&&   %%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		//genoManager

		MappingDataDAO mappingDataDAOLocal = new MappingDataDAO();
		mappingDataDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		MappingDataDAO mappingDataDAOCentral = new MappingDataDAO();
		mappingDataDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
		ArrayList<MappingData> listOfAllMappingData = new ArrayList<MappingData>();
		ArrayList<MappingData> finalListOfMappingDataForSelectedMap = new ArrayList<MappingData>();
		try {

			List<MappingData> listOfAllMappingDataLocal = mappingDataDAOLocal.getAll();
			List<MappingData> listOfAllMappingDataCentral = mappingDataDAOCentral.getAll();
			
			if (null != listOfAllMappingDataCentral){
				//System.out.println("listOfAllMappingDataCentral=:"+listOfAllMappingDataCentral);
				for (MappingData mappingData : listOfAllMappingDataCentral){
					//if (iSelectedMapId.equals(mappingData.getMapId())) {
					if (false == listOfAllMappingData.contains(mappingData)){
						listOfAllMappingData.add(mappingData);
					}
					//}
				}
			}
			if (null != listOfAllMappingDataLocal) {
				//System.out.println("listOfAllMappingDataLocal:"+listOfAllMappingDataLocal);
				for (MappingData mappingData : listOfAllMappingDataLocal){
					//if (iSelectedMapId.equals(mappingData.getMapId())) {
					if (false == listOfAllMappingData.contains(mappingData)){
						
						listOfAllMappingData.add(mappingData);
					}
					//}
				}
			}
			//System.out.println("iSelectedMapId=:"+iSelectedMapId+"   listOfAllMappingData=:"+listOfAllMappingData);
			for (MappingData mappingData : listOfAllMappingData){
				//System.out.println(iSelectedMapId+"=="+ mappingData.getMapId());
				if (iSelectedMapId == Integer.parseInt(mappingData.getMapId().toString())){
					//System.out.println("######################");
					finalListOfMappingDataForSelectedMap.add(mappingData);
					
				}
				
			}
			//System.out.println("*********************:"+finalListOfMappingDataForSelectedMap);
			//genoManager.getMap
			/*List<MapInfo> results = genoManager.getMapInfoByMapName(mapName, Database.CENTRAL);
	        Debug.println(0, "testGetMapInfoByMapName(mapName=" + mapName + ") RESULTS size: " + results.size());
	        for (MapInfo mapInfo : results){
	            Debug.println(0, mapInfo.toString());
	            mapInfo.get
	        }*/
			
			
			//System.out.println("finalListOfMappingDataForSelectedMap:"+finalListOfMappingDataForSelectedMap);
			
		} catch (MiddlewareQueryException e1) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Mapping Data objects", Notification.TYPE_ERROR_MESSAGE);
			return;
		}

		finalListOfMappingData = new ArrayList<MappingData>();
		
		List<String> markersOnMap=new ArrayList();
		
		
		
		for (MarkerOnMap markerOnMap : finalListOfMarkersOnMap){
			Integer markerIdOnMap = markerOnMap.getMarkerId();
			//System.out.println("markerIdOnMap=:"+markerIdOnMap);
			for (MappingData mappingData : finalListOfMappingDataForSelectedMap){
				if (markerIdOnMap.equals(mappingData.getMarkerId())){
					//if (iSelectedMapId.equals(mappingData.getMapId())) {
					markersOnMap.add(mappingData.getMarkerName());
					if (false == finalListOfMappingData.contains(mappingData)){
						finalListOfMappingData.add(mappingData);
					}
					//}
				}
			}
		}
		//System.out.println("finalListOfMappingData=:"+finalListOfMappingData);
		/*Integer markerId, String linkageGroup, Float startPosition, 
        String mapUnit, String mapName, String markerName, Integer mapId*/
		QtlDetailsDAO qtlDetailsDAOLocal = new QtlDetailsDAO();
		qtlDetailsDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		QtlDetailsDAO qtlDetailsDAOCentral = new QtlDetailsDAO();
		qtlDetailsDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
		ArrayList<QtlDetails> listOfAllQtlDetails = new ArrayList<QtlDetails>();
		try {
			List<QtlDetails> listOfAllQtlDetailsLocal = qtlDetailsDAOLocal.getAll();
			List<QtlDetails> listOfAllQtlDetailsCentral = qtlDetailsDAOCentral.getAll();
			if (null != listOfAllQtlDetailsLocal) {
				for (QtlDetails qtlDetails : listOfAllQtlDetailsLocal){
					if (false == listOfAllQtlDetails.contains(qtlDetails)){
						listOfAllQtlDetails.add(qtlDetails);
					}
				}
			}
			if (null != listOfAllQtlDetailsCentral) { 
				for (QtlDetails qtlDetails : listOfAllQtlDetailsCentral){
					if (false == listOfAllQtlDetails.contains(qtlDetails)){
						listOfAllQtlDetails.add(qtlDetails);
					}
				}
			}
		} catch (MiddlewareQueryException e1) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Qtl Detail objects", Notification.TYPE_ERROR_MESSAGE);
			return;
		}

		hmOfMarkerIDAndQtlTrait = new HashMap<Integer, String>();
		listofTraits = new ArrayList<String>();
		for (MappingData mappingData : finalListOfMappingData){
			String linkageGroup = mappingData.getLinkageGroup();
			float startPosition = mappingData.getStartPosition();
			Integer markerId = mappingData.getMarkerId();
			
			for (QtlDetails qtlDetails : listOfAllQtlDetails){
				String lgFromQTL = qtlDetails.getLinkageGroup();
				
				if (linkageGroup.equals(lgFromQTL)){
					Float minPosition = qtlDetails.getMinPosition();
					Float maxPosition = qtlDetails.getMaxPosition();
					
					if ((minPosition <= startPosition) && (startPosition <= maxPosition)){
						//String trait = qtlDetails.getTrait();
						String trait = "";
						Integer iTraitId = qtlDetails.getTraitId();
						if (null != iTraitId){
							/*TraitDAO traitDAOLocal = new TraitDAO();
							traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
							String traitFromLocal="";
							try {
								/*traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
								if (null != traitFromLocal){
									trait = traitFromLocal.getAbbreviation();
								} else {
									TraitDAO traitDAOCentral = new TraitDAO();
									traitDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
									Trait traitFromCentral = traitDAOCentral.getByTraitId(iTraitId);
									trait = traitFromCentral.getAbbreviation();
								}*/
								traitFromLocal=om.getStandardVariable(iTraitId).getName();
								
							} catch (MiddlewareQueryException e) {
								_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Traits.", Notification.TYPE_ERROR_MESSAGE);
								return;
							}
						}
						hmOfMarkerIDAndQtlTrait.put(markerId, trait);
						listofTraits.add(trait);
					}
				}
			}
		}
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}
	private static void addValues(int key, String value){
		ArrayList<String> tempList = null;
		if(hashMap.containsKey(key)){
			tempList=hashMap.get(key);
			if(tempList == null)
				tempList = new ArrayList<String>();
			tempList.add(value);
		}else{
			tempList = new ArrayList();
			tempList.add(value);
		}
		hashMap.put(key,tempList);
	}

}


