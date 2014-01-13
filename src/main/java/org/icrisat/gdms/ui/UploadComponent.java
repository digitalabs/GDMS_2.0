package org.icrisat.gdms.ui;



import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.HeadingOne;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;


public class UploadComponent extends CustomComponent implements ItemClickListener {

	private static final long serialVersionUID = 1L;

	private GDMSModel _gdmsModel;
	private VerticalLayout _buildRightSideUploadLayout;
	private VerticalLayout _buildLeftSideUploadLayout;
	private Tree _treeForGDMSUpload;
	private HierarchicalContainer _hierarchicalContainer;
	private String _strRootNode;
	private String _strItemSelected = "SSR Marker";
	private String _strTitleString = "Uploading Data";
	private HeadingOne _lblUploadTitle;
	private GDMSMain _mainHomePage;
	private Component _currentComponent;


	public UploadComponent(GDMSMain theMainHomePage){

		_gdmsModel = GDMSModel.getGDMSModel();
		_mainHomePage = theMainHomePage;

		_buildRightSideUploadLayout = buildUploadDataWindow();
		_buildLeftSideUploadLayout = buildUploadTreeComponent();

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addComponent(_buildLeftSideUploadLayout, 0);
		horizontalLayout.addComponent(_buildRightSideUploadLayout, 1);

		VerticalLayout verticalLayoutForCompleteUploadData = new VerticalLayout();
		verticalLayoutForCompleteUploadData.addComponent(horizontalLayout);
		verticalLayoutForCompleteUploadData.setCaption("Upload");

		setCaption("Upload");
		setCompositionRoot(verticalLayoutForCompleteUploadData);
	}

	public VerticalLayout buildUploadDataWindow() {

		VerticalLayout verticalLayoutUpdate = new VerticalLayout();
		verticalLayoutUpdate.setMargin(true);
		verticalLayoutUpdate.setStyleName(Reindeer.LAYOUT_WHITE);

		if (null != _gdmsModel.getMenuItemSelected()){
			_strItemSelected = _gdmsModel.getMenuItemSelected().getText();
			if (false == _strItemSelected.equals("")){
				_strTitleString = "Uploading Data - " + _strItemSelected;
			}
		}
		
		_lblUploadTitle = new HeadingOne(_strTitleString);
		_lblUploadTitle.setImmediate(true);
		verticalLayoutUpdate.addComponent(_lblUploadTitle);

		VerticalLayout verticalLayoutForUploadData = new VerticalLayout();
		verticalLayoutForUploadData.setSpacing(true);
		verticalLayoutForUploadData.setMargin(true, false, true, false);

		String strIntroPara1 = "<p>Data can be uploaded using provided templates. To upload, select Browse button and upload template data.";

		String strIntroPara2 = "<p>Please upload marker information before uploading Genotyping data.</p>";

		Label lblPara = new Label(strIntroPara1 + strIntroPara2, Label.CONTENT_XHTML);
		verticalLayoutUpdate.addComponent(lblPara);

		if (null != _strItemSelected || false == _strItemSelected.equals("")){
			
			//String strMarkerType = _strMarkerType.replace(" ", "");
			//UploadMarkerInformationComponent modelOneRetrieveMarkerInformation = new UploadMarkerInformationComponent(_mainHomePage, _strItemSelected);
			UploadMarkerInformationComponent uploadMarkerInformationComponent2 = new UploadMarkerInformationComponent(_mainHomePage, _strItemSelected);
			VerticalLayout tableForMarker;
			try {
				tableForMarker = uploadMarkerInformationComponent2.buildTableComponentForMarkerTemplate();
				verticalLayoutUpdate.replaceComponent(_currentComponent, tableForMarker);
				_currentComponent = tableForMarker;
			} catch (GDMSException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
			}
		}

		verticalLayoutUpdate.setMargin(true, true, true, true);

		return verticalLayoutUpdate;
	}



	private VerticalLayout buildUploadTreeComponent() {

		final VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setMargin(true);
		verticalLayout.setMargin(true, true, true, true);


		final Object[][] uploadTemplates = new Object[][]{
				new Object[]{"Marker Information", "SSR Marker", "SNP Marker", "CISR Marker", "CAP Marker"}, 
				new Object[]{"Genotyping Data", "SSR Genotype", "SNP Genotype", "DArt Genotype", "Mapping"},
				new Object[]{"Maps/QTLs", "Map", "QTL", "MTA"}
		};

		_hierarchicalContainer = new HierarchicalContainer();
		_treeForGDMSUpload = new Tree("GDMS Upload", _hierarchicalContainer);

		_strRootNode = "GDMS Upload";

		for (int i = 0; i < uploadTemplates.length; i++) {

			String strParentNode = (String) (uploadTemplates[i][0]);
			_treeForGDMSUpload.addItem(strParentNode);
			_treeForGDMSUpload.setParent(strParentNode, _strRootNode);

			if (uploadTemplates[i].length == 1){
				_treeForGDMSUpload.setChildrenAllowed(uploadTemplates[i], false);
			} else {

				for (int j = 1; j < uploadTemplates[i].length; j++) {
					String childNode = (String) uploadTemplates[i][j];

					_treeForGDMSUpload.addItem(childNode);
					_treeForGDMSUpload.setParent(childNode, strParentNode);

					if (childNode.equals("Mapping")){
						_treeForGDMSUpload.setChildrenAllowed(childNode, true);

						_treeForGDMSUpload.addItem("Allelic Data");
						_treeForGDMSUpload.addItem("ABH Data");

						_treeForGDMSUpload.setParent("Allelic Data", childNode);
						_treeForGDMSUpload.setParent("ABH Data", childNode);

						//20131206: Tulasi : Modified the tree to display separate nodes for Generic and KBio SNPs, also made the Allellic and ABH Data nodes as child nodes
						_treeForGDMSUpload.setChildrenAllowed("Allelic Data", false);
						_treeForGDMSUpload.setChildrenAllowed("ABH Data", false);

					} else if (childNode.equals("SNP Genotype")){ 
						_treeForGDMSUpload.setChildrenAllowed(childNode, true);

						_treeForGDMSUpload.addItem("KBio Science SNP");
						_treeForGDMSUpload.addItem("Generic SNP");

						_treeForGDMSUpload.setParent("KBio Science SNP", childNode);
						_treeForGDMSUpload.setParent("Generic SNP", childNode);
						
						_treeForGDMSUpload.setChildrenAllowed("KBio Science SNP", false);
						_treeForGDMSUpload.setChildrenAllowed("Generic SNP", false);

						//20131206: Tulasi : Modified the tree to display separate nodes for Generic and KBio SNPs
					} else {
						_treeForGDMSUpload.setChildrenAllowed(childNode, false);
					}
				}

				_treeForGDMSUpload.expandItemsRecursively(strParentNode);
			}
		}    

		_treeForGDMSUpload.addListener(this);

		Panel panelForTree = new Panel();
		panelForTree.setStyleName(Reindeer.LAYOUT_BLUE);
		panelForTree.addComponent(_treeForGDMSUpload);

		verticalLayout.addComponent(panelForTree);

		return verticalLayout;
	}


	public void itemClick(ItemClickEvent event) {
		String strSelectedNode = "";
		
		Object openedItemId = null; //selected node
		if (event.getSource() == _treeForGDMSUpload){
			openedItemId = event.getItemId();
			if (false == _treeForGDMSUpload.hasChildren(openedItemId)){
				strSelectedNode = openedItemId.toString();
				_strItemSelected = strSelectedNode;
				
				if (null != _strItemSelected || false == _strItemSelected.equals("")){
					
					/*String strSelectedDataType = _strItemSelected.replace(" ", "");
					UploadVariableFieldsDialog uploadVariableFieldsDialog = null;
					if (strSelectedDataType.equalsIgnoreCase("SNPGenotype") ||  strSelectedDataType.equalsIgnoreCase("AllelicData") || strSelectedDataType.equalsIgnoreCase("ABHData") ){
						uploadVariableFieldsDialog = new UploadVariableFieldsDialog(_mainHomePage, "Markers");
					} else if (strSelectedDataType.equalsIgnoreCase("DArTGenotype")){
						uploadVariableFieldsDialog = new UploadVariableFieldsDialog(_mainHomePage, "Germplasm-Names");
					}
					if (null != uploadVariableFieldsDialog) {
						Window messageWindow = new Window("Upload Message");
						messageWindow.addComponent(uploadVariableFieldsDialog);
						messageWindow.setWidth("500px");
						messageWindow.setBorder(Window.BORDER_NONE);
						messageWindow.setClosable(true);
						messageWindow.center();
						if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
							_mainHomePage.getMainWindow().addWindow(messageWindow);
						}
						messageWindow.setModal(true);
						messageWindow.setVisible(true);
						
						uploadVariableFieldsDialog.addListener(new UploadVariableFieldsListener() {
							
							@Override
							public void uploadVariableFields(ArrayList<String> theListOfVariableFields) {
								ArrayList<String> listOfVariableColumns = theListOfVariableFields;
								if (null != listOfVariableColumns && 0 != listOfVariableColumns.size()){
									UploadMarkerInformationComponent uploadMarkerInformationComponent2 = new UploadMarkerInformationComponent(_mainHomePage, _strItemSelected);
									uploadMarkerInformationComponent2.setListOfVariableFields(listOfVariableColumns);
									VerticalLayout tableForMarker;
									try {
										tableForMarker = uploadMarkerInformationComponent2.buildTableComponentForMarkerTemplate();
										_buildRightSideUploadLayout.replaceComponent(_currentComponent, tableForMarker);
										_currentComponent = tableForMarker;
									} catch (GDMSException e) {
										return;
									}
								}
							}
						});
						
					} else {*/
						UploadMarkerInformationComponent uploadMarkerInformationComponent2 = new UploadMarkerInformationComponent(_mainHomePage, _strItemSelected);
						VerticalLayout tableForMarker;
						try {
							tableForMarker = uploadMarkerInformationComponent2.buildTableComponentForMarkerTemplate();
							_buildRightSideUploadLayout.replaceComponent(_currentComponent, tableForMarker);
							_currentComponent = tableForMarker;
						} catch (GDMSException e) {
							//e.printStackTrace();
							return;
						}
						_strItemSelected = _strItemSelected + " Sample Template";
					//}
				}

			} else {
				strSelectedNode = "SSR Marker"; 
			}
		}

	}

}
