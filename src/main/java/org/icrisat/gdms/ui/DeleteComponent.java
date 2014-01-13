package org.icrisat.gdms.ui;

import java.util.ArrayList;
import java.util.List;

import org.icrisat.gdms.deletedata.DataDeletionAction;
import org.icrisat.gdms.deletedata.DataDeletionRetrievalAction;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.HeadingOne;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;



public class DeleteComponent extends CustomComponent  implements ItemClickListener {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GDMSModel _gdmsModel;
	private VerticalLayout _buildRightSideDeleteLayout;
	private VerticalLayout _buildLeftSideDeleteLayout;
	//private String _strTreeNodeSelection;
	private Tree _treeForGDMSDelete;
	private HorizontalLayout _buildLayoutForSelectedNodes;
	private String _strRootNode;
	private String _strItemSelected;
	private String _strTitleString;
	private Button _btnDelete;
	private Table _tableData;
	private HeadingOne _lblDeleteTitle;
	private HierarchicalContainer _hierarchicalContainer;
	private Object openedItemId;
	private ArrayList<String> _dataList;
	private GDMSMain __mainHomePage;
	protected Object selectedItem;
	private Label _lblSubTitle;
	private Table _centralTableData;
	private Table _currentCentralTableData;
	private VerticalLayout verticalLayoutForTable;


	public DeleteComponent(GDMSMain gdmsMain){
		__mainHomePage = gdmsMain;
		_gdmsModel = GDMSModel.getGDMSModel();

		_buildRightSideDeleteLayout = buildDeleteDataWindow();
		_buildLeftSideDeleteLayout = buildDeleteTreeComponent();
		_buildLayoutForSelectedNodes = buildLayoutForSelectedNodes();

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addComponent(_buildLeftSideDeleteLayout, 0);
		horizontalLayout.addComponent(_buildRightSideDeleteLayout, 1);

		VerticalLayout verticalLayoutForCompleteDeleteData = new VerticalLayout();
		verticalLayoutForCompleteDeleteData.addComponent(_buildLayoutForSelectedNodes);
		verticalLayoutForCompleteDeleteData.addComponent(horizontalLayout);

		//20131205: Tulasi --- Changed the Caption for delete component from Delete to View
		//setCaption("Delete");
		setCaption("View");
		setCompositionRoot(verticalLayoutForCompleteDeleteData);
	}


	private VerticalLayout buildDeleteTreeComponent() {
		final VerticalLayout verticalLayoutForTree = new VerticalLayout();
		verticalLayoutForTree.setSpacing(true);
		verticalLayoutForTree.setMargin(true, true, true, true);


		final Object[][] uploadTemplates = new Object[][]{
				new Object[]{"Genotyping Data"}, 
				new Object[]{"Maps"},
				new Object[]{"QTLs"}
		};

		_hierarchicalContainer = new HierarchicalContainer();
		_treeForGDMSDelete = new Tree("GDMS Delete", _hierarchicalContainer);

		_strRootNode = "GDMS Delete";

		for (int i = 0; i < uploadTemplates.length; i++) {

			String strParentNode = (String) (uploadTemplates[i][0]);
			_treeForGDMSDelete.addItem(strParentNode);
			_treeForGDMSDelete.setParent(strParentNode, _strRootNode);

			if (uploadTemplates[i].length == 1){
				_treeForGDMSDelete.setChildrenAllowed(uploadTemplates[i], false);
			} else {
				for (int j = 1; j < uploadTemplates[i].length; j++) {
					String childNode = (String) uploadTemplates[i][j];

					_treeForGDMSDelete.addItem(childNode);

					_treeForGDMSDelete.setParent(childNode, strParentNode);

					_treeForGDMSDelete.setChildrenAllowed(childNode, false);
				}
				_treeForGDMSDelete.expandItemsRecursively(strParentNode);
			}
		}    

		_treeForGDMSDelete.setImmediate(true);
		_treeForGDMSDelete.addListener(this);
		_treeForGDMSDelete.addListener(new Property.ValueChangeListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				selectedItem = event.getProperty().getValue();
			}
		});

		Panel panelForTree = new Panel();
		panelForTree.setStyleName(Reindeer.LAYOUT_BLUE);
		panelForTree.addComponent(_treeForGDMSDelete);

		verticalLayoutForTree.addComponent(panelForTree);

		return verticalLayoutForTree;
	}


	private VerticalLayout buildDeleteDataWindow() {
		VerticalLayout verticalLayoutDelete = new VerticalLayout();
		verticalLayoutDelete.setSpacing(true);
		verticalLayoutDelete.setStyleName(Reindeer.LAYOUT_WHITE);

		CssLayout cssLayout = new CssLayout();
		cssLayout.setMargin(true);
		cssLayout.setWidth("100%");
		verticalLayoutDelete.addComponent(cssLayout);

		_strTitleString = "Deleting Data";

		if (null != _gdmsModel.getMenuItemSelected()){
			String strMenuItemSelected = _gdmsModel.getMenuItemSelected().getText();
			if (false == strMenuItemSelected.equals("")){
				_strTitleString = "Deleting Data - " + strMenuItemSelected;
			}
		}
		_lblDeleteTitle = new HeadingOne(_strTitleString);
		verticalLayoutDelete.addComponent(_lblDeleteTitle);

		Layout buildTableComponent = buildDeleteTableComponent();
		verticalLayoutDelete.addComponent(buildTableComponent);

		_btnDelete = new Button("Delete");
		_btnDelete.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				handleDeletion();
			}
		});
		verticalLayoutDelete.addComponent(_btnDelete);
		verticalLayoutDelete.setComponentAlignment(_btnDelete, Alignment.BOTTOM_CENTER);

		verticalLayoutDelete.setMargin(false, true, false, true);

		return verticalLayoutDelete;
	}

	private void handleDeletion() {
		if(null == _tableData) {
			return;
		}
		Container containerDataSource = _tableData.getContainerDataSource();
		if(null == containerDataSource) {
			return;
		}
		if(null == _dataList || 0 == _dataList.size()) {
			return;
		}
		
		List<String> itemsToDelete = new ArrayList<String>();
		int iCount = _dataList.size();
		for (int i = 0; i < iCount; i++) {
			Item item = containerDataSource.getItem(new Integer(i));
			if(null != item) {
				Property itemProperty = item.getItemProperty("Select");
				if(itemProperty.toString().equals("true")) {
					if(openedItemId.equals("Genotyping Data")) {
						String strValue = item.getItemProperty("Genotyping Dataset(s) from Local").toString();
						itemsToDelete.add(strValue);
					} else if(openedItemId.equals("Maps")) {
						String strValue = item.getItemProperty("Map(s) from Local").toString();
						itemsToDelete.add(strValue);
					} else if(openedItemId.equals("QTLs")) {
						String strValue = item.getItemProperty("QTL(s) from Local").toString();
						itemsToDelete.add(strValue);
					}
				}
			}
		}
		
		if(0 == itemsToDelete.size()) {
			return;
		}
		
		if(openedItemId.equals("Genotyping Data")) {
			DataDeletionAction dataDeletionAction = new  DataDeletionAction(__mainHomePage);
			if(dataDeletionAction.deleteGenotypingData(itemsToDelete)) {
				removeItemsFromTable(containerDataSource, iCount);
			}
		} else if(openedItemId.equals("Maps")) {
			DataDeletionAction dataDeletionAction = new  DataDeletionAction(__mainHomePage);
			if(dataDeletionAction.deleteMapData(itemsToDelete)) {
				removeItemsFromTable(containerDataSource, iCount);
			}
		} else if(openedItemId.equals("QTLs")) {
			DataDeletionAction dataDeletionAction = new  DataDeletionAction(__mainHomePage);
			if(dataDeletionAction.deleteQTLInfoData(itemsToDelete)) {
				removeItemsFromTable(containerDataSource, iCount);
			}
		}

		
	}


	private void removeItemsFromTable(Container containerDataSource, int iCount) {
		List<String> listToDelete = new ArrayList<String>();
		for (int i = 0; i < iCount; i++) {
			Item item = containerDataSource.getItem(new Integer(i));
			if(null != item) {
				Property itemProperty = item.getItemProperty("Select");
				if(itemProperty.toString().equals("true")) {
					if (null != item){
						_tableData.removeItem(new Integer(i));
						listToDelete.add(_dataList.get(i));
					}
				}
			}
		}
		for (String string : listToDelete) {
			_dataList.remove(string);
		}
		_lblSubTitle.setValue("Records found for " + _strItemSelected + " are: " + _dataList.size());
		_lblSubTitle.requestRepaint();
		_tableData.requestRepaint();
	}

	Layout buildDeleteTableComponent() {

		verticalLayoutForTable = new VerticalLayout();
		verticalLayoutForTable.setMargin(true);
		verticalLayoutForTable.setSpacing(true);
		verticalLayoutForTable.setWidth("700px");
		verticalLayoutForTable.addStyleName(Reindeer.LAYOUT_WHITE);

		_tableData = new Table();
		/*DataDeletionRetrievalAction dataDeletionRetrievalAction = new DataDeletionRetrievalAction();
		_dataList = new ArrayList<String>();
		_tableData.addContainerProperty("Select", CheckBox.class, null);
		_tableData.addContainerProperty("Genotyping Name", String.class, null);
		try {
			_dataList = dataDeletionRetrievalAction.getGenotypingDataList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		_tableData.removeAllItems();
		for(int i=0; i < _dataList.size(); i++) {
			_tableData.addItem(new Object[] {new CheckBox(), _dataList.get(i)}, new Integer(i));
		}
		_tableData.requestRepaint();*/
		
		openedItemId = "Genotyping Data";
		_strItemSelected = "Genotyping Data";
		
		int iTotalNumOfRecords = 0;
		if (this.isEnabled()) {
			updateCentralDataTable();
			iTotalNumOfRecords += _dataList.size(); //Adding the records from the central table
			
			updateTableData();
			iTotalNumOfRecords += _dataList.size(); //Adding the records from the local table
		}
		
		_tableData.setWidth("100%");
		_tableData.setPageLength(5);
		_tableData.setSelectable(false);
		_tableData.setColumnCollapsingAllowed(false);
		_tableData.setColumnReorderingAllowed(false);
		_tableData.setImmediate(true);
		_tableData.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				Object rowSelected = _tableData.getValue();
				if (null != rowSelected){
					_btnDelete.setEnabled(true);
				}
			}
		});


		_tableData.setStyleName("strong");
		_lblSubTitle = new Label("Select the delete.", Label.CONTENT_XHTML);
		_lblSubTitle.setStyleName(Reindeer.TABLE_STRONG);
		verticalLayoutForTable.addComponent(_lblSubTitle);
		verticalLayoutForTable.setComponentAlignment(_lblSubTitle, Alignment.TOP_CENTER);
		
		_strTitleString = "Data Delete - " + _strItemSelected;
		_lblDeleteTitle.setValue(_strTitleString);
		//_lblSubTitle.setValue("Records found for " + _strItemSelected + " are: 0");
		_lblSubTitle.setValue("Records found for " + _strItemSelected + " are: " + iTotalNumOfRecords);

//		_tableData.addContainerProperty("ID", String.class, null);
//		_tableData.addContainerProperty("Map Name", String.class, null);
//		_tableData.addContainerProperty("Markers", String.class, null);
//		_tableData.addContainerProperty("Map Length", String.class, null);
//		_tableData.addContainerProperty("Select", CheckBox.class, null);

//		OptionGroup optiongroup = new OptionGroup("Option Group");
//		Item addItem1 = optiongroup.addItem("1");
//		Item addItem2 = optiongroup.addItem("2");
//		Item addItem3 = optiongroup.addItem("3");

		
//		_tableData.addItem(new Object[] {"1", "ICGS 44 X ICG8 76", "82", "8314 eM", new CheckBox()}, new Integer(1));
//		_tableData.addItem(new Object[] {"2","ICGS 76 X CSGM 84-1", "119", "2208.0002 eM", new CheckBox()}, new Integer(2));
//		_tableData.addItem(new Object[] {"3", "TAG 24 X ICGV 86031", "191", "785.4 eM", new CheckBox()}, new Integer(3));
//		_tableData.addItem(new Object[] {"4", "TAG 24 X ICGV 86031", "191", "785.4 eM", new CheckBox()}, new Integer(4));
//		_tableData.addItem(new Object[] {"5", "TAG 24 X ICGV 86031", "191", "785.4 eM", new CheckBox()}, new Integer(5));
		
		_currentCentralTableData = _centralTableData;
		verticalLayoutForTable.addComponent(_currentCentralTableData);
		verticalLayoutForTable.setComponentAlignment(_currentCentralTableData, Alignment.MIDDLE_CENTER);
		verticalLayoutForTable.addComponent(_tableData);
		verticalLayoutForTable.setComponentAlignment(_tableData, Alignment.MIDDLE_CENTER);
		verticalLayoutForTable.setMargin(true, true, true, true);

		return verticalLayoutForTable;
	}
	
	private void updateCentralDataTable(){
		
		_centralTableData = new Table();
		_centralTableData.setWidth("100%");
		_centralTableData.setPageLength(4);
		_centralTableData.setSelectable(false);
		_centralTableData.setColumnCollapsingAllowed(false);
		_centralTableData.setColumnReorderingAllowed(false);
		_centralTableData.setEditable(false);
		_centralTableData.setStyleName("strong");
		
		_centralTableData.removeContainerProperty("ID");
		_centralTableData.removeContainerProperty("Map Name");
		_centralTableData.removeContainerProperty("Select");
		_centralTableData.removeContainerProperty("Genotyping Name");
		_centralTableData.removeContainerProperty("QTLs Name");
		
		DataDeletionRetrievalAction dataDeletionRetrievalAction = new DataDeletionRetrievalAction();
		_dataList = new ArrayList<String>();
		if(openedItemId.equals("Genotyping Data")) {
			try {
				//_centralTableData.addContainerProperty("Genotyping Name", String.class, null);
				_centralTableData.addContainerProperty("Genotyping Dataset(s) from Central", String.class, null);
				_dataList = dataDeletionRetrievalAction.getCentralGenotypingDataList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(openedItemId.equals("Maps")) {
			try {
				//centralTableData.addContainerProperty("Select", CheckBox.class, null);
				_centralTableData.addContainerProperty("Map(s) from Central", String.class, null);
				_dataList = dataDeletionRetrievalAction.getCentralMapsList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(openedItemId.equals("QTLs")) {
			try {
				//centralTableData.addContainerProperty("Select", CheckBox.class, null);
				_centralTableData.addContainerProperty("QTL(s) from Central", String.class, null);
				_dataList = dataDeletionRetrievalAction.getCentralQTLInfoList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		_centralTableData.removeAllItems();
		for(int i=0; i < _dataList.size(); i++) {
			_centralTableData.addItem(new Object[] {_dataList.get(i)}, new Integer(i));
		}
		_centralTableData.requestRepaint();
		verticalLayoutForTable.replaceComponent(_currentCentralTableData, _centralTableData);
		verticalLayoutForTable.requestRepaint();
		_currentCentralTableData = _centralTableData;
	}

	private HorizontalLayout buildLayoutForSelectedNodes() {

		final HorizontalLayout horizontalLayoutForSelectedNodes = new HorizontalLayout();
		horizontalLayoutForSelectedNodes.setSpacing(true);
		horizontalLayoutForSelectedNodes.setMargin(true, false, true, true);

//		_lblSelectedNodesTree = new Label(_strTreeNodeSelection, Label.CONTENT_XHTML);
//		_lblSelectedNodesTree.addListener(new RepaintRequestListener() {
//			private static final long serialVersionUID = 1L;
//
//			public void repaintRequested(RepaintRequestEvent event) {
//
//				_lblSelectedNodesTree = new Label(_strTreeNodeSelection, Label.CONTENT_XHTML);
//				_lblSelectedNodesTree.setStyleName(Reindeer.LABEL_SMALL);
//				_lblSelectedNodesTree.setHeight("14px");
//				horizontalLayoutForSelectedNodes.addComponent(_lblSelectedNodesTree);
//			}
//		});

		return horizontalLayoutForSelectedNodes;
	}


	public void itemClick(ItemClickEvent event) {

		String strSelectedNode = "";
		//String strParentNode = "";
		//String strDisplayTreeNodes = "";
		Object initialSelectedItemId = null;

		if (event.getSource() == _treeForGDMSDelete){
			openedItemId = event.getItemId();
			
			initialSelectedItemId = openedItemId;
			strSelectedNode = openedItemId.toString(); 
			while (!_treeForGDMSDelete.isRoot(openedItemId)){

				Object parentNode = _treeForGDMSDelete.getParent(openedItemId);
				//String strNode = parentNode.toString();
				//strParentNode += " > " + strNode; 
				openedItemId = parentNode;
			}
		}


		//strDisplayTreeNodes = _strRootNode + strParentNode + " > " + strSelectedNode;
		//_strTreeNodeSelection = strDisplayTreeNodes;

//		_lblSelectedNodesTree.setValue(_strTreeNodeSelection);
//		_lblSelectedNodesTree.requestRepaint();
		_strItemSelected = strSelectedNode;

		int iTotalNumOfRecords = 0;
		
		updateCentralDataTable();
		iTotalNumOfRecords += _dataList.size(); //Adding the records from the central table
		
		updateTableData();
		iTotalNumOfRecords += _dataList.size(); //Adding the records from the local table
		
		if (false == _treeForGDMSDelete.hasChildren(initialSelectedItemId)){
			_strTitleString = "Data Delete - " + _strItemSelected;
			_lblDeleteTitle.setValue(_strTitleString);
			//_lblSubTitle.setValue("Records found for " + _strItemSelected + " are: " + _dataList.size());
			_lblSubTitle.setValue("Records found for " + _strItemSelected + " are: " + iTotalNumOfRecords);
		}  else {
			_lblDeleteTitle.setValue("Data Delete");
			_lblSubTitle.setValue("Select the delete.");
		}
	}


	private void updateTableData() {
		_tableData.removeContainerProperty("ID");
		_tableData.removeContainerProperty("Map Name");
		_tableData.removeContainerProperty("Select");
		_tableData.removeContainerProperty("Genotyping Name");
		_tableData.removeContainerProperty("QTLs Name");
		_tableData.removeContainerProperty("Genotyping Dataset(s) from Local");
		_tableData.removeContainerProperty("Map(s) from Local");
		_tableData.removeContainerProperty("QTL(s) from Local");

		
		DataDeletionRetrievalAction dataDeletionRetrievalAction = new DataDeletionRetrievalAction();
		_dataList = new ArrayList<String>();
		if(openedItemId.equals("Genotyping Data")) {
			try {
				_tableData.addContainerProperty("Select", CheckBox.class, null);
				_tableData.addContainerProperty("Genotyping Dataset(s) from Local", String.class, null);
				_tableData.setColumnWidth("Select", 40);
				_tableData.setColumnWidth("Genotyping Name", 1000);
				//_tableData.setColumnExpandRatio("Select", 1.0f);
				//_tableData.setColumnExpandRatio("Genotyping Name", 2.0f);
				_dataList = dataDeletionRetrievalAction.getGenotypingDataList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(openedItemId.equals("Maps")) {
			try {
				_tableData.addContainerProperty("Select", CheckBox.class, null);
				_tableData.addContainerProperty("Map(s) from Local", String.class, null);
				_tableData.setColumnWidth("Select", 40);
				_tableData.setColumnWidth("Map Name", 1000);
				//_tableData.setColumnExpandRatio("Select", 1.0f);
				//_tableData.setColumnExpandRatio("Map Name", 2.0f);
				_dataList = dataDeletionRetrievalAction.getMapsList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(openedItemId.equals("QTLs")) {
			try {
				_tableData.addContainerProperty("Select", CheckBox.class, null);
				_tableData.addContainerProperty("QTL(s) from Local", String.class, null);
				_tableData.setColumnWidth("Select", 40);
				_tableData.setColumnWidth("QTLs Name", 1000);
				//_tableData.setColumnExpandRatio("Select", 1.0f);
				//_tableData.setColumnExpandRatio("QTLs Name", 2.0f);
				_dataList = dataDeletionRetrievalAction.getQTLInfoList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		_tableData.removeAllItems();
		for(int i=0; i < _dataList.size(); i++) {
			_tableData.addItem(new Object[] {new CheckBox(), _dataList.get(i)}, new Integer(i));
		}
		_tableData.requestRepaint();
	}

}
