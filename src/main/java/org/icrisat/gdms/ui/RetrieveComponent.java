package org.icrisat.gdms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.hibernate.Session;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.retrieve.GenotypingDataRetrieval;
import org.icrisat.gdms.retrieve.RetrieveMap;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.HeadingOne;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;


public class RetrieveComponent extends CustomComponent implements ItemClickListener  {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GDMSModel _gdmsModel;
	private VerticalLayout _buildRightSideRetrieveLayout;
	private VerticalLayout _buildLeftSideRetrieveLayout;
	private HierarchicalContainer hierarchicalContainer;
	private Tree treeForGDMSRetrieve;
	private String strRootNode;
	private String _strTitleString;
	private String _strItemSelected;
	private HeadingOne _lblRetrieveTitle;
	private HorizontalLayout _horizontalLayout;
	private Layout buildTableComponent;
	private Component currentComponent;
	private GDMSMain _mainHomePage;

	public RetrieveComponent(GDMSMain theMainHomePage){
		
		_gdmsModel = GDMSModel.getGDMSModel();
		_mainHomePage = theMainHomePage;

		//20131210: Tulasi
		if (null == _gdmsModel.getLoggedInUser()) {
			setEnabled(false);
		}
		//20131210: Tulasi
		
		_buildRightSideRetrieveLayout = buildRetrieveDataWindow();
		_buildLeftSideRetrieveLayout = buildRetrieveTreeComponent();

		_horizontalLayout = new HorizontalLayout();
		_horizontalLayout.addComponent(_buildLeftSideRetrieveLayout, 0);
		_horizontalLayout.addComponent(_buildRightSideRetrieveLayout, 1);

		VerticalLayout verticalLayoutForCompleteRetrievalData = new VerticalLayout();
		verticalLayoutForCompleteRetrievalData.addComponent(_horizontalLayout);

		setCaption("Retrieve");
		setCompositionRoot(verticalLayoutForCompleteRetrievalData);
		
	}

	private VerticalLayout buildRetrieveDataWindow() {
		VerticalLayout verticalLayoutRetrieve = new VerticalLayout();
		verticalLayoutRetrieve.setStyleName(Reindeer.LAYOUT_WHITE);

		CssLayout cssLayout = new CssLayout();
		cssLayout.setMargin(true);
		verticalLayoutRetrieve.addComponent(cssLayout);

		//_strTitleString = "Retrieving Data"; //20131205: Tulasi --- Modified the title string
		_strTitleString = "Data Retrieval";

		if (null != _gdmsModel.getMenuItemSelected()){
			String strMenuItemSelected = _gdmsModel.getMenuItemSelected().getText();
			if (false == strMenuItemSelected.equals("")){
				_strTitleString = "Retrieving Data - " + strMenuItemSelected;
			}
		}
		_lblRetrieveTitle = new HeadingOne(_strTitleString);
		verticalLayoutRetrieve.addComponent(_lblRetrieveTitle);

		if (null == _strItemSelected){
			buildTableComponent = buildRetrieveTableComponent();
			verticalLayoutRetrieve.addComponent(buildTableComponent);
		}
		
		currentComponent = buildTableComponent;

		return verticalLayoutRetrieve;
	}

	private Layout buildRetrieveTableComponent() {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		verticalLayout.setWidth("700px");
		verticalLayout.addStyleName(Reindeer.LAYOUT_WHITE);


		Table table = new Table();
		table.setWidth("100%");
		table.setPageLength(4);
		table.setSelectable(true);
		table.setColumnCollapsingAllowed(true);
		table.setColumnReorderingAllowed(true);

		table.setStyleName("strong");
		
		//20131205: Tulasi --- Commented the following Label component displaying the subtitle
		/*Label label = new Label("Retrieval Datasets", Label.CONTENT_XHTML);
		label.setStyleName(Reindeer.TABLE_STRONG);
		verticalLayout.addComponent(label);
		verticalLayout.setComponentAlignment(label, Alignment.TOP_CENTER);*/


		table.addContainerProperty("SNO", String.class, null);
		table.addContainerProperty("Datasets", String.class, null);
		/*table.addContainerProperty("Type", String.class, null);
		table.addContainerProperty("Size", String.class, null);*/


		if (this.isEnabled()) {
			ArrayList datasetIdsT=new ArrayList();
			GenotypingDataRetrieval genotypingDataRetrieval = new GenotypingDataRetrieval();
			try {
				List<Dataset> retrieveGenotyingDataRetrieval = genotypingDataRetrieval.retrieveGenotyingDataRetrieval();
				
				//20131210: Tulasi --- Displaying the Size data instead of count
				//HashMap<Integer, String> hashMapOfDatasetSize = genotypingDataRetrieval.retrieveDatasetSize();
				
				//Hashtable<String, Integer> htData = new Hashtable<String, Integer>();
				int iCounter = 1;
				for (Dataset dataset : retrieveGenotyingDataRetrieval) {
					String datasetName = dataset.getDatasetName();
					//String datasetType = dataset.getDatasetType();
					Integer iDatasetID = dataset.getDatasetId();
					//String strDatasetSize = hashMapOfDatasetSize.get(iDatasetID);
					
					
					//if (null != strDatasetSize) {
						//table.addItem(new Object[] {String.valueOf(iCounter), datasetName, datasetType, strDatasetSize}, new Integer(iCounter));
						table.addItem(new Object[] {String.valueOf(iCounter), datasetName, }, new Integer(iCounter));
						iCounter++;
					//}
					
				}
				/*int iCounter = 1;
				Iterator<String> iterator = htData.keySet().iterator();
				while(iterator.hasNext()) {
					String strKey = iterator.next();
					Integer integer = htData.get(strKey);
					String[] strSplit = strKey.split("~::::~");
					table.addItem(new Object[] {String.valueOf(iCounter), strSplit[0], strSplit[1], integer.toString()}, new Integer(iCounter));
					iCounter++;
				}*/
				//20131210: Tulasi --- Displaying the Dataset Size column data instead of count
				
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
	
			verticalLayout.addComponent(table);
			verticalLayout.setComponentAlignment(table, Alignment.MIDDLE_CENTER);
			
			//20131209: Tulasi --- Implemented code to display Map and QTL tables
			Table tableForMaps = getMapTableComponent();
			verticalLayout.addComponent(tableForMaps);
			verticalLayout.setComponentAlignment(tableForMaps, Alignment.MIDDLE_CENTER);
			
			
			Table tableForQTLs = getQTLTableComponent();
			verticalLayout.addComponent(tableForQTLs);
			verticalLayout.setComponentAlignment(tableForQTLs, Alignment.MIDDLE_CENTER);
			//20131209: Tulasi --- Implemented code to display Map and QTL tables
		}
		return verticalLayout;
	}

	//20131209: Tulasi --- Implemented code to display Map and QTL tables
		private Table getQTLTableComponent() {

			Table tableForQTLs = new Table();
			tableForQTLs.setWidth("100%");
			tableForQTLs.setPageLength(4);
			tableForQTLs.setSelectable(true);
			tableForQTLs.setColumnCollapsingAllowed(true);
			tableForQTLs.setColumnReorderingAllowed(true);
			tableForQTLs.setStyleName("strong");

			tableForQTLs.addContainerProperty("SNO", String.class, null);
			tableForQTLs.addContainerProperty("QTL-Name", String.class, null);
			tableForQTLs.addContainerProperty("Map-Name", String.class, null);
			tableForQTLs.addContainerProperty("Trait-Name", String.class, null);
			
			tableForQTLs.addContainerProperty("Linkage-Group", String.class, null);
			tableForQTLs.addContainerProperty("Min-Pos", String.class, null);
			tableForQTLs.addContainerProperty("Max-Pos", String.class, null);
			

			GenotypingDataRetrieval genotypingDataRetrieval = new GenotypingDataRetrieval();
			List<String> listOfQTLData = genotypingDataRetrieval.retrieveQTLData();

			if (null != listOfQTLData) {
				int iQTLCounter = 1;

				for (String strQTLData : listOfQTLData) {

					String[] strArrayQTLData = strQTLData.split("!~!");

					String strQTLName = strArrayQTLData[0];
					String strMapName = strArrayQTLData[1];
					String strTraitName = strArrayQTLData[2];
					
					String strLinkageGroup = strArrayQTLData[3];
					String strMinPos = strArrayQTLData[4];
					String strMaxPos = strArrayQTLData[5];

					tableForQTLs.addItem(new Object[] {String.valueOf(iQTLCounter), strQTLName, strMapName, strTraitName,
							strLinkageGroup, strMinPos, strMaxPos}, new Integer(iQTLCounter));
					iQTLCounter++;
				}
			}

			return tableForQTLs;
		}

		private Table getMapTableComponent() {

			Table tableWithAllMaps = null;
			
			RetrieveMap retrieveMarker = new RetrieveMap();
			List<MapDetailElement> retrieveMaps = new ArrayList<MapDetailElement>();
			try {
				retrieveMaps = retrieveMarker.retrieveMaps();
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				return null;
			}
			
			List<MappingData> retrieveMappingData = new ArrayList<MappingData>();
			try {
				retrieveMappingData = retrieveMarker.retrieveMappingData();
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
			
			final List<MapDetailElement> finalretrieveMarker2 = retrieveMaps;
			if(null != finalretrieveMarker2) {
				
				ArrayList<String> strDataM=new ArrayList<String>();
				for (MapDetailElement mapsDetails : finalretrieveMarker2) {
					String strMapUnit = mapsDetails.getMapType();
					/*for (MappingData mappingData : retrieveMappingData) {
						if(mappingData.getMapName().equals(mapsDetails.getMapName())) {
							strMapUnit = mappingData.getM.getMapUnit();
							break;
						}
					}*/
					strDataM.add(mapsDetails.getMarkerCount() + "!~!" + mapsDetails.getMaxStartPosition()+"!~!"+mapsDetails.getLinkageGroup()+"!~!"+mapsDetails.getMapName()+"!~!"+strMapUnit);
				}
				String[] strArr=strDataM.get(0).toString().split("!~!");
				
				String chr=strArr[3];
				int mCount=Integer.parseInt(strArr[0]);
				float distance=Float.parseFloat(strArr[1]);
				int mc=0;
				float d=0;
				List<ArrayList<String>> mapFinalList= new ArrayList<ArrayList<String>>();
				String mType="";
				for(int a=0;a<strDataM.size();a++){	
					String mapType="";
					String[] str1=strDataM.get(a).toString().split("!~!");		
					if(str1[3].equals(chr)){
						mc=mc+Integer.parseInt(str1[0]);
						d=d+Float.parseFloat(str1[1]);	
						mType=str1[4];
						if(a==(strDataM.size()-1)){
							mCount=mc;
							distance=d;
							mapType=mType;
							ArrayList<String> listOfData = new ArrayList<String>();
							listOfData.add(String.valueOf(mCount));
							listOfData.add(chr);
							listOfData.add(String.valueOf(distance));
							listOfData.add(mapType);
							mapFinalList.add(listOfData);								
						}
					}else if(!(str1[3].equals(chr))){							
						mCount=mc;
						distance=d;
						mapType=mType;
						ArrayList<String> listOfData = new ArrayList<String>();
						listOfData.add(String.valueOf(mCount));
						listOfData.add(chr);
						listOfData.add(String.valueOf(distance));
						listOfData.add(mapType);
						mapFinalList.add(listOfData);								
						mc=0;
						d=0;
						mType="";
						chr=str1[3];
						a=a-1;
					}						
				}

				List<ArrayList<String>> tempList = new ArrayList<ArrayList<String>>();
				
				for (ArrayList<String> arrayList : mapFinalList){
					tempList.add(arrayList);
				}
				if (0 < mapFinalList.size()) {
					
					tableWithAllMaps = new Table();
					tableWithAllMaps.setSizeFull();
					tableWithAllMaps.setPageLength(5);
					tableWithAllMaps.setSelectable(false);
					tableWithAllMaps.setColumnCollapsingAllowed(false);
					tableWithAllMaps.setColumnReorderingAllowed(false);
					tableWithAllMaps.setEditable(false);
					tableWithAllMaps.setStyleName("strong");
					
					tableWithAllMaps.addContainerProperty("SNO", String.class, null);	
					tableWithAllMaps.addContainerProperty("Map Name", String.class, null);
					tableWithAllMaps.addContainerProperty("Map Type", String.class, null);
					tableWithAllMaps.addContainerProperty("Map Length", String.class, null);

					int iMapCounter = 1;
					int i = 0;
					for (ArrayList<String> listOfData : mapFinalList) {
						String strChr = listOfData.get(1);
						String strDistance = listOfData.get(2);
						
						float parseFloat = Float.parseFloat(strDistance);
					    double roundOff = Math.round(parseFloat * 10.0)/10.0;
					    strDistance = String.valueOf(roundOff);
					    
						String strMapType = listOfData.get(3);
						
						//tableWithAllMaps.addItem(new Object[] {checkBox, strChr, strCount, strDistance, strMapType}, new Integer(i));
						tableWithAllMaps.addItem(new Object[] {String.valueOf(iMapCounter), strChr, strMapType, strDistance}, new Integer(i));
						//tableWithAllMaps.addItem(new Object[] {String.valueOf(iMapCounter), strChr, strMapType}, new Integer(i));
						iMapCounter++;
						i++;
					}
					tableWithAllMaps.requestRepaint();
					
				} else {
					_mainHomePage.getMainWindow().getWindow().showNotification("No Map/Maps exist with the given search string.", Notification.TYPE_ERROR_MESSAGE);
					return null;
				}
			}
			
			return tableWithAllMaps;
		}
		//20131209: Tulasi --- Implemented code to display Map and QTL tables
		
		
	private VerticalLayout buildRetrieveTreeComponent() {
		final VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setMargin(true, true, true, true);
		
		final Object[][] uploadTemplates = new Object[][]{
				new Object[]{"Genotyping Data", "Dataset", "Search", "Polymorphic Markers"},
				new Object[]{"Marker Information"},
				new Object[]{"Maps/QTLs", "Map", "QTL", "Trait"}
		};

		hierarchicalContainer = new HierarchicalContainer();
		treeForGDMSRetrieve = new Tree("GDMS Retrieve", hierarchicalContainer);

		strRootNode = "GDMS Retrieve";

		for (int i = 0; i < uploadTemplates.length; i++) {

			String strParentNode = (String) (uploadTemplates[i][0]);
			treeForGDMSRetrieve.addItem(strParentNode);
			treeForGDMSRetrieve.setParent(strParentNode, strRootNode);

			if (uploadTemplates[i].length == 1){
				treeForGDMSRetrieve.setChildrenAllowed(uploadTemplates[i], false);
			} else {

				for (int j = 1; j < uploadTemplates[i].length; j++) {
					String childNode = (String) uploadTemplates[i][j];

					treeForGDMSRetrieve.addItem(childNode);
					treeForGDMSRetrieve.setParent(childNode, strParentNode);

					if (childNode.equals("Search")){
						treeForGDMSRetrieve.setChildrenAllowed(childNode, true);
						
						treeForGDMSRetrieve.addItem("GID");
						treeForGDMSRetrieve.addItem("Germplasm Names");
						treeForGDMSRetrieve.addItem("Markers");
						//treeForGDMSRetrieve.addItem("Polymorphic Markers");
						
						treeForGDMSRetrieve.setParent("GID", childNode);
						treeForGDMSRetrieve.setParent("Germplasm Names", childNode);
						treeForGDMSRetrieve.setParent("Markers", childNode);
//						treeForGDMSRetrieve.setParent("Polymorphic Markers", childNode);	
						
					} else {
						treeForGDMSRetrieve.setChildrenAllowed(childNode, false);
					}
				}
				treeForGDMSRetrieve.expandItemsRecursively(strParentNode);
			}
		}    

		treeForGDMSRetrieve.addListener(this);

		Panel panelForTree = new Panel();
		panelForTree.setStyleName(Reindeer.LAYOUT_BLUE);
		panelForTree.addComponent(treeForGDMSRetrieve);

		verticalLayout.addComponent(panelForTree);

		return verticalLayout;
	}

	public void itemClick(ItemClickEvent event) {

		String strSelectedNode = "";
		Object openedItemId = null; //selected node
		Object initialSelectedItemId = null;

		if (event.getSource() == treeForGDMSRetrieve){
			openedItemId = event.getItemId();
			initialSelectedItemId = openedItemId;
			strSelectedNode = openedItemId.toString(); 
			while (!treeForGDMSRetrieve.isRoot(openedItemId)){

				Object parentNode = treeForGDMSRetrieve.getParent(openedItemId);
				openedItemId = parentNode;
			}
		}

		_strItemSelected = strSelectedNode;

		if (false == treeForGDMSRetrieve.hasChildren(initialSelectedItemId)){

			_strTitleString = "Data Retrieval - " + _strItemSelected;
			_lblRetrieveTitle.setValue(_strTitleString);

		}  else {
			_lblRetrieveTitle.setValue("Data Retrieval");
		}
		
		if (_strItemSelected.equals("Marker Information")){
			
			try {
				RetrieveMarkerInformationComponent modelOneRetrieveMarkerInformation = new RetrieveMarkerInformationComponent(_mainHomePage);
				HorizontalLayout tabbedComponentForMarker = modelOneRetrieveMarkerInformation.buildTabbedComponentForMarker();
				tabbedComponentForMarker.setWidth("90%");
				_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForMarker);
				currentComponent = tabbedComponentForMarker;
				
			} catch (GDMSException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
				return;
			} catch (MiddlewareQueryException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
				return;
			}
			
		} else if (_strItemSelected.equals("QTL")){
			RetrieveQTLInformationComponent modelOneQTLInformation = new RetrieveQTLInformationComponent(_mainHomePage);
			HorizontalLayout tabbedComponentForQTL = modelOneQTLInformation.buildTabbedComponentForQTL();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForQTL);
			currentComponent = tabbedComponentForQTL;
		} else if (_strItemSelected.equals("Map")){
			RetrieveMapInformationComponent modelOneMapInformation = new RetrieveMapInformationComponent(_mainHomePage);
			HorizontalLayout tabbedComponentForMap = modelOneMapInformation.buildTabbedComponentForMap();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForMap);
			currentComponent = tabbedComponentForMap;
		} else if (_strItemSelected.equals("Trait")){
			RetrieveTraitInformationComponent modelOneTraitInformation = new RetrieveTraitInformationComponent(_mainHomePage);
			HorizontalLayout tabbedComponentForTrait = modelOneTraitInformation.buildTabbedComponentForTrait();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForTrait);
			currentComponent = tabbedComponentForTrait;
		} else if (_strItemSelected.equals("Dataset")){
			RetrieveDatasetInformationComponent modelOneDatasetInformation = new RetrieveDatasetInformationComponent(_mainHomePage);
			HorizontalLayout tabbedComponentForDataset = modelOneDatasetInformation.buildTabbedComponentForQTL();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForDataset);
			currentComponent = tabbedComponentForDataset;
		} else if (_strItemSelected.equals("GID")){
			RetrieveGIDInformationComponent modelOneGIDInformation = new RetrieveGIDInformationComponent(_mainHomePage);
			HorizontalLayout tabbedComponentForGID = modelOneGIDInformation.buildTabbedComponentForGID();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForGID);
			currentComponent = tabbedComponentForGID;
		} else if (_strItemSelected.equals("Polymorphic Markers")){
			RetrievePolymorphicMarkerComponent modelOnePolymorphicMarkers = new RetrievePolymorphicMarkerComponent(_mainHomePage);
			HorizontalLayout tabbedComponentForPloymorphicMarker = modelOnePolymorphicMarkers.buildTabbedComponentForPolymorphicMarker();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForPloymorphicMarker);
			currentComponent = tabbedComponentForPloymorphicMarker;
		} else if (_strItemSelected.equals("Germplasm Names")){
			RetrieveGermplasmInformationComponent modelOneGermplasmNames = new RetrieveGermplasmInformationComponent(_mainHomePage);
			HorizontalLayout tabbedComponentForGermplasmNames = modelOneGermplasmNames.buildTabbedComponentForGermplasmNames();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForGermplasmNames);
			currentComponent = tabbedComponentForGermplasmNames;	
		} else if (_strItemSelected.equals("Markers")){
			RetrieveMarkersComponent retrieveMarkersComponent = new RetrieveMarkersComponent(_mainHomePage);
			HorizontalLayout tabbedComponentForMarkers = retrieveMarkersComponent.buildTabbedComponentForMarkers();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, tabbedComponentForMarkers);
			currentComponent = tabbedComponentForMarkers;
		}  /*else if (_strItemSelected.equals("Search") || _strItemSelected.equals("Maps/QTL")){
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, buildTableComponent);
			currentComponent = buildTableComponent;
		} */else if(_strItemSelected.equals("Genotyping Data") ||
				_strItemSelected.equals("Search") || _strItemSelected.equals("Maps/QTLs")) {
			buildTableComponent = buildRetrieveTableComponent();
			_buildRightSideRetrieveLayout.replaceComponent(currentComponent, buildTableComponent);
			currentComponent = buildTableComponent;
		}
		
		_buildRightSideRetrieveLayout.setMargin(false, true, true, true);
	}

}
