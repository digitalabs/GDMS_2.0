package org.icrisat.gdms.ui;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jxl.Workbook;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.Map;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.FileDownloadResource;
import org.icrisat.gdms.ui.common.GDMSFileChooser;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.UploadTableFields;
import org.icrisat.gdms.ui.common.UploadVariableFieldsListener;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.genotyping.DARTGenotype;
import org.icrisat.gdms.upload.genotyping.KBioScienceGenotype;
import org.icrisat.gdms.upload.genotyping.MappingABH;
import org.icrisat.gdms.upload.genotyping.MappingAllelic;
import org.icrisat.gdms.upload.genotyping.SNPGenotype;
import org.icrisat.gdms.upload.genotyping.SSRGenotype;
import org.icrisat.gdms.upload.maporqtl.MTAUpload;
import org.icrisat.gdms.upload.maporqtl.MapUpload;
import org.icrisat.gdms.upload.maporqtl.QTLUpload;
import org.icrisat.gdms.upload.marker.CAPMarker;
import org.icrisat.gdms.upload.marker.CISRMarker;
import org.icrisat.gdms.upload.marker.SNPMarker;
import org.icrisat.gdms.upload.marker.SSRMarker;
import org.icrisat.gdms.upload.marker.UploadField;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class UploadMarkerInformationComponent  implements Component.Listener {

	private static final long serialVersionUID = 1L;

	private GDMSModel _gdmsModel;
	private GDMSMain _mainHomePage;
	private String _strMarkerType;
	private ArrayList<FieldProperties> listOfSourceColumnFields;
	private Button btnAdd;
	private Table tableForSourceTemplateFields;
	//private int iRowCount = 0;
	private Button btnDelete;
	protected ArrayList<HashMap<String, String>> sourceDataListToBeDisplayedInTheTable;
	private Button btnUpload;
	private Layout layoutForMarkerTableComponent;
	private UploadMarker uploadMarker;
	private ArrayList<FieldProperties> listOfDataColumnFields;
	private ArrayList<FieldProperties> listOfAdditionalGIDsColumnFieldsForDArT;
	private String strSourceSheetTitle;
	private String strDataSheetTitle;
	private String strGIDsSheetTitleForDArT;
	private Table tableForDataTemplateFields;
	private Table tableForGIDsTemplateFieldsForDArTGenotype;
	private ArrayList<HashMap<String, String>> dataListToBeDisplayedInTheTable;
	private ArrayList<HashMap<String, String>> dataListFromAdditionalGIDsSheetForDArTGenotype;
	private TabSheet tabsheetForMarkerTemplates;
	private int iSelectedTab;
	private ArrayList<String> listOfVariableDataColumns;
	private ArrayList<String> listOfVariableColumnsToBeDeleted;
	private OptionGroup optiongroup;
	private ComboBox comboBoxForMap;
	private Button btnAddColumns;
	private int iNumOfTabs;
	private Button btnDeleteColumns;
	private ComboBox comboBoxForDataset;
	private Button btnDownloadMarker;
	private Session localSession;
	private Session centralSession;
	private TextField txtFieldForDatasetName;
	private Session sessionL;
	private Session sessionC;
	private ComboBox comboBoxForGermplasm;
	ManagerFactory factory =null;
	
	GermplasmListManager list;
	GermplasmDataManager germManager;
	GenotypicDataManager genoManager;
	
	public UploadMarkerInformationComponent(GDMSMain theMainHomePage, String theMarkerType){
		_gdmsModel = GDMSModel.getGDMSModel();
		_mainHomePage = theMainHomePage;
		_strMarkerType = theMarkerType;
		
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			list = factory.getGermplasmListManager();
			//germManager=factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
			//System.out.println("777777777777777777777  :"+GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchSetting().getInstallationDirectory());
			//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%   :"+GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId());
			//System.out.println(",,,,,,,,,,,,,,,  :"+GDMSModel.getGDMSModel().getWorkbenchDataManager().getLastOpenedProject(1);
			createMarkerToBeUploaded();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public VerticalLayout buildTableComponentForMarkerTemplate() throws GDMSException {

		getMarkerFields();

		final VerticalLayout verticalLayout = new VerticalLayout();

		layoutForMarkerTableComponent = buildTabbedComponentForTemplate();
		final TabSheetListener tabSheetListener = new TabSheetListener();

		Label lblSelectedType = new Label("<u>" + _strMarkerType + "</u>" , Label.CONTENT_XHTML);
		lblSelectedType.setStyleName(Reindeer.LABEL_H2);

		btnAdd = new Button("Add Row");
		btnAdd.setImmediate(true);
		btnAdd.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				Tab sourceTab = tabsheetForMarkerTemplates.getTab(0);
				Tab dataTab = null;
				if (null != listOfDataColumnFields){
					dataTab = tabsheetForMarkerTemplates.getTab(1);
				}
				Tab gidTab = null;
				if (null != listOfAdditionalGIDsColumnFieldsForDArT){
					gidTab = tabsheetForMarkerTemplates.getTab(2);
				}
				Component selectedTab = tabsheetForMarkerTemplates.getSelectedTab();
				iSelectedTab = 0;
				if (selectedTab.getCaption().equals(sourceTab.getCaption())){
					iSelectedTab = 0;
				} else if (selectedTab.getCaption().equals(dataTab.getCaption())){
					iSelectedTab = 1;
				} else if (selectedTab.getCaption().equals(gidTab.getCaption())){
					iSelectedTab = 2;
				}

				if (0 == iSelectedTab){
					String[] arrayEmptyColumns = new String[listOfSourceColumnFields.size()];
					int size = tableForSourceTemplateFields.size();
					int iRowIndex = size + 1;
					String strEmptyColumns = "" + iRowIndex;
					arrayEmptyColumns[0] = strEmptyColumns;
					tableForSourceTemplateFields.setEditable(true);
					for (int i = 1; i < listOfSourceColumnFields.size(); i++){
						String strFieldName = listOfSourceColumnFields.get(i).getFieldName();
						tableForSourceTemplateFields.addContainerProperty(strFieldName, String.class, "");
						arrayEmptyColumns[i] = new String("");
					}
					Object[] array = tableForSourceTemplateFields.getItemIds().toArray();
					if (array.length >= 1) {
						Integer iLastRowID = (Integer) array[array.length - 1];
						iRowIndex = iLastRowID.intValue() + 1;
					} else {
						iRowIndex = 1;
					}
					Object addItem = tableForSourceTemplateFields.addItem(arrayEmptyColumns, new Integer(iRowIndex));
					if (null != addItem){
						System.out.println("New-Item added.");
					}
					tableForSourceTemplateFields.requestRepaint();
					tableForSourceTemplateFields.setTableFieldFactory(new TableFieldFactory() {
						private static final long serialVersionUID = 1L;
						public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
							boolean rowIsEditable = true; // check itemId if row should be editable
							if(rowIsEditable) {
								return DefaultFieldFactory.get().createField(container, itemId, propertyId, uiContext);
							}
							return null;
						}
					});

				} else if (1 == iSelectedTab) {

					String[] arrayEmptyColumns = new String[listOfDataColumnFields.size()];
					int size = tableForDataTemplateFields.size();
					int iRowIndex = size + 1;
					String strEmptyColumns = "" + iRowIndex;
					tableForDataTemplateFields.setEditable(true);
					arrayEmptyColumns[0] = strEmptyColumns;
					for (int i = 1; i < listOfDataColumnFields.size(); i++){
						String strFieldName = listOfDataColumnFields.get(i).getFieldName();
						tableForDataTemplateFields.addContainerProperty(strFieldName, String.class, "");
						arrayEmptyColumns[i] = new String("");
					}
					Object[] array = tableForDataTemplateFields.getItemIds().toArray();
					if (array.length >= 1) {
						Integer iLastRowID = (Integer) array[array.length - 1];
						iRowIndex = iLastRowID.intValue() + 1;
					} else {
						iRowIndex = 1;
					}
					tableForDataTemplateFields.addItem(arrayEmptyColumns, new Integer(iRowIndex));
					tableForDataTemplateFields.requestRepaint();
					tableForDataTemplateFields.setTableFieldFactory(new TableFieldFactory() {
						private static final long serialVersionUID = 1L;
						public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
							boolean rowIsEditable = true; // check itemId if row should be editable
							if(rowIsEditable) {
								return DefaultFieldFactory.get().createField(container, itemId, propertyId, uiContext);
							}
							return null;
						}
					});

				} else if (2 == iSelectedTab) {

					String[] arrayEmptyColumns = new String[listOfAdditionalGIDsColumnFieldsForDArT.size()];
					int size = tableForGIDsTemplateFieldsForDArTGenotype.size();
					int iRowIndex = size + 1;
					String strEmptyColumns = "" + iRowIndex;
					tableForGIDsTemplateFieldsForDArTGenotype.setEditable(true);
					arrayEmptyColumns[0] = strEmptyColumns;
					for (int i = 1; i < listOfAdditionalGIDsColumnFieldsForDArT.size(); i++){
						String strFieldName = listOfAdditionalGIDsColumnFieldsForDArT.get(i).getFieldName();
						tableForGIDsTemplateFieldsForDArTGenotype.addContainerProperty(strFieldName, String.class, "");
						arrayEmptyColumns[i] = new String("");
					}
					Object[] array = tableForGIDsTemplateFieldsForDArTGenotype.getItemIds().toArray();
					if (array.length >= 1) {
						Integer iLastRowID = (Integer) array[array.length - 1];
						iRowIndex = iLastRowID.intValue() + 1;
					} else {
						iRowIndex = 1;
					}
					tableForGIDsTemplateFieldsForDArTGenotype.addItem(arrayEmptyColumns, new Integer(iRowIndex));
					tableForGIDsTemplateFieldsForDArTGenotype.requestRepaint();
					tableForGIDsTemplateFieldsForDArTGenotype.setTableFieldFactory(new TableFieldFactory() {
						private static final long serialVersionUID = 1L;
						public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
							boolean rowIsEditable = true; // check itemId if row should be editable
							if(rowIsEditable) {
								return DefaultFieldFactory.get().createField(container, itemId, propertyId, uiContext);
							}
							return null;
						}
					});
				}
			}
		});


		btnDelete = new Button("Delete Row");
		btnDelete.setImmediate(true);
		btnDelete.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				Tab sourceTab = null;
				if (null != listOfSourceColumnFields && 0 != listOfSourceColumnFields.size()){
					sourceTab = tabsheetForMarkerTemplates.getTab(0);
				}
				
				Tab dataTab = null;
				if (null != listOfDataColumnFields && 0 != listOfDataColumnFields.size()){
					dataTab = tabsheetForMarkerTemplates.getTab(1);
				}
				Tab gidTab = null;
				if (null != listOfAdditionalGIDsColumnFieldsForDArT && 0 != listOfAdditionalGIDsColumnFieldsForDArT.size()){
					gidTab = tabsheetForMarkerTemplates.getTab(2);
				}
				Component selectedTab = tabsheetForMarkerTemplates.getSelectedTab();
				iSelectedTab = 0;
				if (selectedTab.getCaption().equals(sourceTab.getCaption())){
					iSelectedTab = 0;
				} else if (selectedTab.getCaption().equals(dataTab.getCaption())){
					iSelectedTab = 1;
				} else if (selectedTab.getCaption().equals(gidTab.getCaption())){
					iSelectedTab = 2;
				}

				Integer iRowIdFromSourceTable = 0;
				Integer iRowIdFromDataTable = 0;
				Integer iRowIdFromGIDsTableForDArT = 0;

				if (0 == iSelectedTab){
					if (0 < tableForSourceTemplateFields.size()){
						if (null != tableForSourceTemplateFields.getValue()){
							if (tableForSourceTemplateFields.getValue() instanceof Integer){
								iRowIdFromSourceTable = (Integer)tableForSourceTemplateFields.getValue();
							} else {
								iRowIdFromSourceTable = 0;
							}
						} else {
							iRowIdFromSourceTable = 0;
						}
					}
				} else if (1 == iSelectedTab){
					if (0 < tableForDataTemplateFields.size()){
						if (null !=  tableForDataTemplateFields.getValue()){
							if (tableForDataTemplateFields.getValue() instanceof Integer){
								iRowIdFromDataTable = (Integer)tableForDataTemplateFields.getValue();
							} else {
								iRowIdFromDataTable = 0;
							}
						} else {
							iRowIdFromDataTable = 0;
						}
					}
				} else if (2 == iSelectedTab){
					if (0 < tableForGIDsTemplateFieldsForDArTGenotype.size()){
						if (null !=  tableForGIDsTemplateFieldsForDArTGenotype.getValue()){
							if (tableForGIDsTemplateFieldsForDArTGenotype.getValue() instanceof Integer){
								iRowIdFromGIDsTableForDArT = (Integer) tableForGIDsTemplateFieldsForDArTGenotype.getValue();
							} else {
								iRowIdFromGIDsTableForDArT = 0;
							}
						} else {
							iRowIdFromGIDsTableForDArT = 0;
						}
					}
				}

				if (0 != iRowIdFromSourceTable) {

					Container containerDataSource = tableForSourceTemplateFields.getContainerDataSource();
					//int iTempRowcount = iRowCount;
					//iRowCount = 1;
					int iSNO = 1;
					Object[] array = tableForSourceTemplateFields.getItemIds().toArray();
					//for (int i = 0; i < iTempRowcount+1; i++){
					for (int i = 0; i < array.length; i++){
						Integer iRowID = (Integer) array[i];
						Item item2 = containerDataSource.getItem(iRowID.intValue());
						if (null != item2){
							Property itemProperty = item2.getItemProperty(UploadField.SNo.toString());
							if (null != itemProperty) {
								if (iRowID == iRowIdFromSourceTable){
									itemProperty.setValue(-1);
								} else {
									itemProperty.setValue(iSNO);
									iSNO += 1;
									//iRowCount += 1;
								}
							}
						}
					}
					containerDataSource.removeItem(iRowIdFromSourceTable);
					//boolean removeItem2 = containerDataSource.removeItem(iRowIdFromSourceTable);
					//System.out.println("Item-Deleted: " + removeItem2);
					tableForSourceTemplateFields.setContainerDataSource(containerDataSource);
					tableForSourceTemplateFields.requestRepaint();
					

				} else if (0 != iRowIdFromDataTable) {

					Container containerDataSource = tableForDataTemplateFields.getContainerDataSource();
					//containerDataSource.removeItem(iRowIdFromDataTable);
					//int iTempRowcount = iRowCount;
					//iRowCount = 1;
					int iSNO = 1;
					Object[] array = tableForDataTemplateFields.getItemIds().toArray();
					//for (int i = 0; i < iTempRowcount+1; i++){
					for (int i = 0; i < array.length; i++){
						Integer iRowID = (Integer) array[i];
						Item item2 = containerDataSource.getItem(iRowID.intValue());
						if (null != item2){
							Property itemProperty = item2.getItemProperty(UploadField.SNo.toString());
							if (null != itemProperty) {
								if (iRowID == iRowIdFromDataTable){
									itemProperty.setValue(-1);
								} else {
									itemProperty.setValue(iSNO);
									iSNO += 1;
									//iRowCount += 1;
								}
							}
						}
					}
					containerDataSource.removeItem(iRowIdFromDataTable);
					//boolean removeItem = containerDataSource.removeItem(iRowIdFromDataTable);
					//System.out.println("Item-Deleted: " + removeItem);
					tableForDataTemplateFields.setContainerDataSource(containerDataSource);
					tableForDataTemplateFields.requestRepaint();

				} else if (0 != iRowIdFromGIDsTableForDArT) {

					Container containerDataSource = tableForGIDsTemplateFieldsForDArTGenotype.getContainerDataSource();
					//containerDataSource.removeItem(iRowIdFromGIDsTableForDArT);
					//int iTempRowcount = iRowCount;
					//iRowCount = 1;
					int iSNO = 1;
					Object[] array = tableForGIDsTemplateFieldsForDArTGenotype.getItemIds().toArray();
					//for (int i = 0; i < iTempRowcount+1; i++){
					for (int i = 0; i < array.length; i++){
						Integer iRowID = (Integer) array[i];
						Item item2 = containerDataSource.getItem(iRowID.intValue());
						if (null != item2){
							Property itemProperty = item2.getItemProperty(UploadField.SNo.toString());
							if (null != itemProperty) {
								if (iRowID == iRowIdFromGIDsTableForDArT){
									itemProperty.setValue(-1);
								} else {
									itemProperty.setValue(iSNO);
									iSNO += 1;
									//iRowCount += 1;
								}
							}
						}
					}
					containerDataSource.removeItem(iRowIdFromGIDsTableForDArT);
					//boolean removeItem = containerDataSource.removeItem(iRowIdFromGIDsTableForDArT);
					//System.out.println("Item-Deleted: " + removeItem);
					tableForGIDsTemplateFieldsForDArTGenotype.setContainerDataSource(containerDataSource);
					tableForGIDsTemplateFieldsForDArTGenotype.requestRepaint();

				}
			}
		});


		btnUpload = new Button("Upload");
		btnUpload.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				buildListOfDataObjectsToBeUploaded();
			}
		});
		

		ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		excelButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				try {
					String strFileName = _strMarkerType.replaceAll(" ", "_") + ".xls";
					File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
					File absoluteFile = baseDirectory.getAbsoluteFile();

					File[] listFiles = absoluteFile.listFiles();
					File fileExport = baseDirectory;
					for (File file : listFiles) {
						if(file.getAbsolutePath().endsWith("FileExports")) {
							fileExport = file;
							break;
						}
					}
					String strFilePath = fileExport.getAbsolutePath();
					File generatedFile = new File(strFilePath + "\\" + strFileName);
					WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
					WritableSheet sheet1 = workbook.createSheet(strSourceSheetTitle, 0);
					
					//20130829: Fix for Issue No: 58
					if (1 == iNumOfTabs) {
						exportToExcel(workbook, sheet1, tableForSourceTemplateFields, listOfSourceColumnFields);
						//20130829: Fix for Issue No: 58
					} else if (1 < iNumOfTabs){
						
						exportToExcelSource(workbook, sheet1, tableForSourceTemplateFields, listOfSourceColumnFields);
						
						if (null != tableForDataTemplateFields){
							WritableSheet sheet2 = workbook.createSheet(strDataSheetTitle, 1);
							exportToExcel(workbook, sheet2, tableForDataTemplateFields, listOfDataColumnFields);
						}
						
						if (null != tableForGIDsTemplateFieldsForDArTGenotype){
							WritableSheet sheet3 = workbook.createSheet(strGIDsSheetTitleForDArT, 2);
							exportToExcel(workbook, sheet3, tableForGIDsTemplateFieldsForDArTGenotype, listOfAdditionalGIDsColumnFieldsForDArT);
						}
					}
					

					workbook.write();
					workbook.close();

					event.getButton().getWindow().open(new FileDownloadResource(
							generatedFile, _mainHomePage.getMainWindow().getWindow().getApplication()));

				} catch (WriteException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
				}
			}
			
			
					});

		GDMSFileChooser gdmsFileChooser = new GDMSFileChooser(_mainHomePage, false);
		gdmsFileChooser.registerListener(new FileUploadListener() {
			public void updateLocation(String absolutePath) {

				uploadMarker.setFileLocation(absolutePath);
				try {
					uploadMarker.readExcelFile();
					uploadMarker.validateDataInExcelSheet();
					uploadMarker.createObjectsToBeDisplayedOnGUI();

					sourceDataListToBeDisplayedInTheTable = uploadMarker.getDataFromSourceSheet();

					if (null != listOfDataColumnFields){
						dataListToBeDisplayedInTheTable = uploadMarker.getDataFromDataSheet();
					}

					if (null != listOfAdditionalGIDsColumnFieldsForDArT){
						dataListFromAdditionalGIDsSheetForDArTGenotype = uploadMarker.getDataFromAdditionalGIDsSheet();
					}

					Layout newMarkerTableComponent = buildTabbedComponentForTemplate();
					int iNumOfTabs = tabsheetForMarkerTemplates.getComponentCount();
					if (1 == iNumOfTabs) {
						btnAdd.setVisible(true);
					} else {
						btnAdd.setVisible(false);
					}
					tabsheetForMarkerTemplates.addListener(tabSheetListener);
					verticalLayout.replaceComponent(layoutForMarkerTableComponent, newMarkerTableComponent);
					verticalLayout.requestRepaint();
					layoutForMarkerTableComponent = newMarkerTableComponent;

				} catch (GDMSException e) {
					_mainHomePage.getMainWindow().showNotification(e.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});

		final HorizontalLayout horizontalLayoutForUploadComponent = new HorizontalLayout();
		horizontalLayoutForUploadComponent.setSpacing(true);
		horizontalLayoutForUploadComponent.setMargin(true, true, true, true);

		int iNumOfTabs = tabsheetForMarkerTemplates.getComponentCount();
		if (1 == iNumOfTabs) {
			btnAdd.setVisible(true);
		} else {
			btnAdd.setVisible(false);
		}
		
		
		btnAddColumns = new Button("Add Columns");
		btnAddColumns.setImmediate(true);
		btnAddColumns.setVisible(false);
		btnAddColumns.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			
			/** Add more columns to SNP, DArT, Allelic and ABH Uploads */
			@Override
			public void buttonClick(ClickEvent event) {
				String strSelectedDataType = _strMarkerType.replace(" ", "");
				UploadVariableFieldsDialog uploadVariableFieldsDialog = null;
				if (strSelectedDataType.equalsIgnoreCase("SNPGenotype") ||  strSelectedDataType.equalsIgnoreCase("AllelicData") || strSelectedDataType.equalsIgnoreCase("ABHData") ){
					uploadVariableFieldsDialog = new UploadVariableFieldsDialog(_mainHomePage, "Markers", true);
				} else if (strSelectedDataType.equalsIgnoreCase("DArTGenotype")){
					uploadVariableFieldsDialog = new UploadVariableFieldsDialog(_mainHomePage, "Germplasm-Names", true);
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
							listOfVariableDataColumns = listOfVariableColumns;
							if (null != listOfVariableColumns && 0 != listOfVariableColumns.size()){
								Layout newTabbedComponentForTemplate = buildTabbedComponentForTemplate();
								verticalLayout.replaceComponent(layoutForMarkerTableComponent, newTabbedComponentForTemplate);
								verticalLayout.requestRepaint();
								layoutForMarkerTableComponent = newTabbedComponentForTemplate;
							}
						}
					});
				}

			}
			
		});
		
		
		btnDeleteColumns = new Button("Delete Columns");
		btnDeleteColumns.setImmediate(true);
		btnDeleteColumns.setVisible(false);
		btnDeleteColumns.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				String strSelectedDataType = _strMarkerType.replace(" ", "");
				UploadVariableFieldsDialog uploadVariableFieldsDialog = null;
				if (strSelectedDataType.equalsIgnoreCase("SNPGenotype") ||  strSelectedDataType.equalsIgnoreCase("AllelicData") || strSelectedDataType.equalsIgnoreCase("ABHData") ){
					uploadVariableFieldsDialog = new UploadVariableFieldsDialog(_mainHomePage, "Markers", false);
				} else if (strSelectedDataType.equalsIgnoreCase("DArTGenotype")){
					uploadVariableFieldsDialog = new UploadVariableFieldsDialog(_mainHomePage, "Germplasm-Names", false);
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
							listOfVariableColumnsToBeDeleted = theListOfVariableFields;
							if (null != listOfVariableColumnsToBeDeleted && 0 != listOfVariableColumnsToBeDeleted.size()){
								Layout newTabbedComponentForTemplate = buildTabbedComponentForTemplate();
								verticalLayout.replaceComponent(layoutForMarkerTableComponent, newTabbedComponentForTemplate);
								verticalLayout.requestRepaint();
								layoutForMarkerTableComponent = newTabbedComponentForTemplate;
							}
							
							/*for (int i = 0; i < listOfVariableColumnsToBeDeleted.size(); i++) {
								String strColumn = listOfVariableColumnsToBeDeleted.get(i);
								String columnHeader = tableForDataTemplateFields.getColumnHeader(strColumn);
								tableForDataTemplateFields.removeContainerProperty(columnHeader);
								tableForDataTemplateFields.requestRepaint();
							}*/
							
						}
					});
				}
			}
		});

		
		/**
		 * 20130823: Fix for Issue No: 61
		 * 
		 * Not showing the Add button is the Source sheet is selected.
		 */
		tabsheetForMarkerTemplates.addListener(tabSheetListener);
		/*tabsheetForMarkerTemplates.addListener(new TabSheet.SelectedTabChangeListener() {
			private static final long serialVersionUID = 1L;
			public void selectedTabChange(SelectedTabChangeEvent event){
				int iNumOfTabs = tabsheetForMarkerTemplates.getComponentCount();
				if (1 < iNumOfTabs) {
					Component selectedTab = tabsheetForMarkerTemplates.getSelectedTab();
					Tab sourceTab = tabsheetForMarkerTemplates.getTab(0);
					iSelectedTab = 0;
					if (!selectedTab.getCaption().equals(sourceTab.getCaption())){
						btnAdd.setVisible(true);
					} else {
						btnAdd.setVisible(false);
					}
				} else {
					btnAdd.setVisible(true);
				}
		    }
		});*/
		/** 20130823: End of fix for Issue No: 61 */
		 
	
		/*HorizontalLayout horizontalLayout2 = new HorizontalLayout();
		horizontalLayout2.setSpacing(true);
		horizontalLayout2.setMargin(true, true, false, true);
		horizontalLayout2.addComponent(gdmsFileChooser);
		horizontalLayout2.addComponent(btnAdd);
		horizontalLayout2.addComponent(btnAddColumns);
		horizontalLayout2.addComponent(btnDelete);
		horizontalLayout2.addComponent(btnUpload);
		horizontalLayout2.addComponent(excelButton);*/
		
		

		//20131106: Instead adding a hyperlink to download the marker template
		btnDownloadMarker = new Button("Download Sample Template");
		btnDownloadMarker.setImmediate(true);
		btnDownloadMarker.setStyleName(Reindeer.BUTTON_LINK);
		btnDownloadMarker.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
                
				WebApplicationContext ctx = (WebApplicationContext) _mainHomePage.getContext();
                String strTemplateFolderPath = ctx.getHttpSession().getServletContext().getRealPath("\\VAADIN\\themes\\gdmstheme\\Templates");
                System.out.println("Folder-Path: " + strTemplateFolderPath);
                
				String strMarkerType = _strMarkerType.replace(" ", "");
				String strFileName = "";
				System.out.println("strMarkerType=:"+strMarkerType);
				if (strMarkerType.equals("SSRMarker")){
					strFileName = "SSR_Marker.xls";
				} else if (strMarkerType.equals("SNPMarker")){
					strFileName = "SNP_Marker.xls";
				} else if (strMarkerType.equals("CISRMarker")){
					strFileName = "CISRMarker.xls";
				} else if (strMarkerType.equals("CAPMarker")){
					strFileName = "CAPMarker.xls";
				} else if (strMarkerType.equals("SSRGenotype")){
					strFileName = "SSR_GenotypingTemplate.xls";
				} else if (strMarkerType.equals("SNPGenotype") || strMarkerType.equals("GenericSNP")){
					strFileName = "SNPGenotypingTemplate.txt";
				} else if (strMarkerType.equals("LGCGenomicsSNP")){
					strFileName = ""; //TODO: Tulasi --- Have to check with Kalyani
				} else if (strMarkerType.equals("DArtGenotype")){
					strFileName = "DArTGenotypingTemplate.xls";
				} else if (strMarkerType.equals("AllelicData") || strMarkerType.equals("ABHData") ){
					strFileName = "MappingTemplate.xls";
				} else if (strMarkerType.equals("QTL")){
					strFileName = "QTLTemplate.xls";
				} else if (strMarkerType.equals("Map")){
					strFileName = "MapTemplate.xls";
				} else if (strMarkerType.equals("MTA")){
					strFileName = "MTATemplate.xls";
				} else {
					strFileName = "SSR_Marker.xls";
				}
				
				File strFileLoc = new File(strTemplateFolderPath + "\\" + strFileName);
				FileResource fileResource = new FileResource(strFileLoc, _mainHomePage);
				//_mainHomePage.getMainWindow().getWindow().open(fileResource, "_blank", true);
				
				if (strFileName.endsWith(".xls")) {
					_mainHomePage.getMainWindow().getWindow().open(fileResource, "", true);
				} else if (strFileName.endsWith(".txt")) {
					//_mainHomePage.getMainWindow().getWindow().open(new ExternalResource(strTemplateFolderPath + "\\" + strFileName));
					_mainHomePage.getMainWindow().getWindow().open(fileResource, "SNP_Genotype", true);
				}
				
			}
			
		});
		//20131106: Instead adding a hyperlink to download the marker template
		
		HorizontalLayout horizontalLayout2 = new HorizontalLayout();
		horizontalLayout2.setSpacing(true);
		horizontalLayout2.setMargin(true, true, false, true);
		horizontalLayout2.addComponent(btnAdd);
		horizontalLayout2.addComponent(btnAddColumns);
		horizontalLayout2.addComponent(btnDelete);
		horizontalLayout2.addComponent(btnDeleteColumns);
		horizontalLayout2.addComponent(btnUpload);
		//horizontalLayout2.addComponent(excelButton); //20131106: Not displaying the excel button used to export the marker data from the table
		horizontalLayout2.addComponent(btnDownloadMarker);
		horizontalLayout2.setComponentAlignment(btnDownloadMarker, Alignment.BOTTOM_LEFT);
		
		
		optiongroup = new OptionGroup();
		optiongroup.setMultiSelect(false);
		optiongroup.addStyleName("horizontal");
		optiongroup.addItem("SSR");
		optiongroup.addItem("SNP");
		optiongroup.addItem("DArT");
		optiongroup.setImmediate(true);
		optiongroup.addListener(new Component.Listener() {
			private static final long serialVersionUID = 1L;
			public void componentEvent(Event event) {
				Object value = optiongroup.getValue();
				String strSelectedMarkerType = value.toString();
				String strMappingType = _strMarkerType.replace(" ", "");
				_gdmsModel.setMarkerForMap(strMappingType, strSelectedMarkerType);
			}
		});
		optiongroup.select(0);
		
		comboBoxForMap = new ComboBox();
		Object itemId = comboBoxForMap.addItem();
		comboBoxForMap.setItemCaption(itemId, "Select Map");
		comboBoxForMap.setValue(itemId);
		comboBoxForMap.setNullSelectionAllowed(false);
		comboBoxForMap.setImmediate(true);
		//20131206: Tulasi --- Added the condition to retrieve maps only for Mapping Data type
				if (_strMarkerType.equalsIgnoreCase("Allelic Data") || _strMarkerType.equalsIgnoreCase("ABH Data")) {
					final List<Map> listOfAllMaps = retrieveAllMaps();
					if (null != listOfAllMaps){
						for (int i = 0; i < listOfAllMaps.size(); i++){
							Map map = listOfAllMaps.get(i);
							comboBoxForMap.addItem(map.getMapName());
						}
					}
					
					comboBoxForMap.addListener(new Property.ValueChangeListener() {
						private static final long serialVersionUID = 1L;
						public void valueChange(ValueChangeEvent event) {
							String strSelectedMap = "";
							Object mapValue = comboBoxForMap.getValue();
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
							_gdmsModel.setMapSelected(strSelectedMap);
						}
					});
				} 
				//20131206: Tulasi --- Added the condition to retrieve maps only for Mapping Data type
				
				

		//20131106: Added code to display list of Maps for ABH Data type and to display list of Datasets for Map uploads
		comboBoxForDataset = new ComboBox();
		Object itemId1 = comboBoxForDataset.addItem();
		comboBoxForDataset.setItemCaption(itemId1, "Select Dataset");
		comboBoxForDataset.setValue(itemId1);
		comboBoxForDataset.setNullSelectionAllowed(false);
		comboBoxForDataset.setImmediate(true);
		//20131209: Tulasi --- Added condition to retrive Datasets for Map and LGC Genomics SNP uploads only
				if (_strMarkerType.equalsIgnoreCase("Map")) {
					final List<Dataset> listOfDatasets = retrieveAllDatasets();
					if (null != listOfDatasets){
						for (int i = 0; i < listOfDatasets.size(); i++){
							Dataset dataset = listOfDatasets.get(i);
							comboBoxForDataset.addItem(dataset.getDatasetName());
						}
					}
					comboBoxForDataset.addListener(new Property.ValueChangeListener() {
						private static final long serialVersionUID = 1L;
						public void valueChange(ValueChangeEvent event) {
							String strSelecteDataset = "";
							Object datasetValue = comboBoxForDataset.getValue();
							if (datasetValue instanceof Integer){
								Integer itemId = (Integer)datasetValue;
								if (itemId.equals(1)){
									strSelecteDataset = "";
								} 
							} else {
								String strDatasetSelected = datasetValue.toString();
								boolean bIsValidDataset = false;
								if (null != listOfDatasets){
									for (int i = 0; i < listOfDatasets.size(); i++){
										Dataset datasetName = listOfDatasets.get(i);
										if (datasetName.getDatasetName().equals(strDatasetSelected)){
											bIsValidDataset = true;
											break;
										}
									}
								}	
								if (bIsValidDataset){
									strSelecteDataset = strDatasetSelected;
								}
							}
							_gdmsModel.setDatasetSelected(strSelecteDataset);
						}
					});
				}
				//Label lblForDatasetName = new Label();
		//lblForDatasetName.setValue("Dataset Name: ");
		
		txtFieldForDatasetName = new TextField("Dataset Name ");
		//txtFieldForDatasetName.setRequired(true);
		txtFieldForDatasetName.setWidth("250px");
		txtFieldForDatasetName.setCursorPosition(0);
		txtFieldForDatasetName.setInputPrompt("Enter Dataset Name");
		
		comboBoxForGermplasm = new ComboBox("Germplasm List ");
		comboBoxForGermplasm.setWidth("150px");
		Object itemIdGermplasm = comboBoxForGermplasm.addItem();
		comboBoxForGermplasm.setItemCaption(itemIdGermplasm, "-- Select -- ");
		comboBoxForGermplasm.setValue(itemIdGermplasm);
		comboBoxForGermplasm.setNullSelectionAllowed(false);
		comboBoxForGermplasm.setImmediate(true);
		
		if (_strMarkerType.equalsIgnoreCase("LGC Genomics SNP")) {
			final ArrayList<String> listOfGermplasms = retrieveAllGermplasms();
			if (null != listOfGermplasms){
				for (int i = 0; i < listOfGermplasms.size(); i++){
					String strGermplasmName = listOfGermplasms.get(i).toString();
					comboBoxForGermplasm.addItem(strGermplasmName);
				}
			}
		}	
	
		//20131209: Tulasi --- Modified the following conditions to display datasets for KBioScienceSNPGenotype
		HorizontalLayout topHorizontalLayout = new HorizontalLayout();
		if (_strMarkerType.equalsIgnoreCase("Allelic Data")) {
			topHorizontalLayout.addComponent(optiongroup);
			topHorizontalLayout.addComponent(comboBoxForMap);
		} else if (_strMarkerType.equalsIgnoreCase("ABH Data")) {
			topHorizontalLayout.addComponent(comboBoxForMap);
		} else if (_strMarkerType.equalsIgnoreCase("Map")) {
			topHorizontalLayout.addComponent(comboBoxForDataset);
		} else if (_strMarkerType.equalsIgnoreCase("LGC Genomics SNP")) {
			txtFieldForDatasetName.setRequired(true);
			//topHorizontalLayout.addComponent(lblForDatasetName);
			topHorizontalLayout.addComponent(txtFieldForDatasetName);
			topHorizontalLayout.addComponent(comboBoxForGermplasm);
		} 
		topHorizontalLayout.setSpacing(true);
		topHorizontalLayout.setMargin(true, true, true, true);
		

		//VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(lblSelectedType);
		verticalLayout.setComponentAlignment(lblSelectedType, Alignment.TOP_CENTER);
		if (_strMarkerType.equalsIgnoreCase("Allelic Data") || _strMarkerType.equalsIgnoreCase("ABH Data") || 
				_strMarkerType.equalsIgnoreCase("Map") || _strMarkerType.equalsIgnoreCase("LGC Genomics SNP")) {
			verticalLayout.addComponent(topHorizontalLayout);
		}
		//20131106: Added code to display list of Maps for ABH Data type and to display list of Datasets for Map uploads
		
		verticalLayout.addComponent(layoutForMarkerTableComponent);
		verticalLayout.addComponent(gdmsFileChooser);
		verticalLayout.addComponent(horizontalLayout2);

		return verticalLayout;
	}
	private ArrayList<String> retrieveAllGermplasms() {
		String strQuerry="select distinct listname from listnms where listtype='LST'";
		ArrayList<String> listOfGermplasmLists = new ArrayList<String>();
		
		List newListL=new ArrayList();
		List newListC=new ArrayList();
		//try {	
		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;
		
		
		listOfGermplasmLists.clear();
		
		sessionC=centralSession.getSessionFactory().openSession();			
		SQLQuery queryC=sessionC.createSQLQuery(strQuerry);		
		queryC.addScalar("listname",Hibernate.STRING);	
		newListC=queryC.list();			
		itListC=newListC.iterator();			
		while(itListC.hasNext()){
			obj=itListC.next();
			if(obj!=null)				
				listOfGermplasmLists.add(obj.toString());	
		}
				

		sessionL=localSession.getSessionFactory().openSession();			
		SQLQuery queryL=sessionL.createSQLQuery(strQuerry);		
		queryL.addScalar("listname",Hibernate.STRING);	        
		newListL=queryL.list();
		itListL=newListL.iterator();			
		while(itListL.hasNext()){
			objL=itListL.next();
			if(objL!=null)				
				listOfGermplasmLists.add(objL.toString());	
		}
		
			
			/*
			
			System.out.println("^^^^^^^^^^^^^^^^^^^   :"+list.getGermplasmListTypes());
			
			System.out.println("%%%%%%%%%%%%%%%%%%%%   :"+list.getAllGermplasmLists(1, 20, Database.CENTRAL));
			
			int countL = (int) list.countAllGermplasmLists();
	        List<GermplasmList> listC = list.getAllGermplasmLists(0, countL, Database.CENTRAL);
	        for (GermplasmList germPListC : listC) {
	        	if(germPListC.getType().toString().equalsIgnoreCase("lst")){
	        		newList.add(germPListC.getName().toString());	        		
	        	}	           
	        }	
	        List<GermplasmList> listL = list.getAllGermplasmLists(0, countL, Database.LOCAL);
	        for (GermplasmList germPListL : listL) {
	        	if(germPListL.getType().toString().equalsIgnoreCase("lst")){
	        		if(!(newList.contains(germPListL.getName().toString())))
	        			newList.add(germPListL.getName().toString());	        		
	        	}	           
	        }
			*/
			
			
			//System.out.println("listOfGermplasmLists="+listOfGermplasmLists);
			
		/*} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		

		return listOfGermplasmLists;
	}
	
	private List<Map> retrieveAllMaps() throws GDMSException {
		
		MapDAO mapDAOLocal = new MapDAO();
		mapDAOLocal.setSession(localSession);

		MapDAO mapDAOCentral = new MapDAO();
		mapDAOCentral.setSession(centralSession);

		List<Map> listOfAllMaps = new ArrayList<Map>();
		try {
			List<Map> listOfAllMapsLocal = mapDAOLocal.getAll();
			List<Map> listOfAllMapsCentral = mapDAOCentral.getAll();

			if (null != listOfAllMapsLocal) {
				for (Map map : listOfAllMapsLocal){
					if (false == listOfAllMaps.contains(map)){
						listOfAllMaps.add(map);
					}
				}
			}
			
			if (null != listOfAllMapsCentral) {
				for (Map map : listOfAllMapsCentral){
					if (false == listOfAllMaps.contains(map)){
						listOfAllMaps.add(map);
					}
				}
			}

		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}

		return listOfAllMaps;
	}
	
	
	private List<Dataset> retrieveAllDatasets() throws GDMSException {
		
		DatasetDAO datasetDAOLocal = new DatasetDAO();
		datasetDAOLocal.setSession(localSession);
		
		DatasetDAO datasetDAOCentral = new DatasetDAO();
		datasetDAOCentral.setSession(centralSession);
		
		List<Dataset> listOfAllDatasets = new ArrayList<Dataset>();
		
		try {
			List<Dataset> listOfAllDatasetsLocal = datasetDAOLocal.getAll();
			List<Dataset>  listOfAllDatasetsCentral = datasetDAOCentral.getAll();

			if (null != listOfAllDatasetsLocal) {
				for (Dataset dataset : listOfAllDatasetsLocal){
					if (false == listOfAllDatasets.contains(dataset)){
						listOfAllDatasets.add(dataset);
					}
				}
			}
			
			if (null != listOfAllDatasetsCentral) {
				for (Dataset dataset : listOfAllDatasetsCentral){
					if (false == listOfAllDatasets.contains(dataset)){
						listOfAllDatasets.add(dataset);
					}
				}
			}

		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		return listOfAllDatasets;
	}

	

	private void createMarkerToBeUploaded(){
		uploadMarker = new SSRMarker();
		String strMarkerType = _strMarkerType.replace(" ", "");
		if (strMarkerType.equals("SSRMarker")){
			uploadMarker = new SSRMarker();
		} else if (strMarkerType.equals("SNPMarker")){
			uploadMarker = new SNPMarker();
		} else if (strMarkerType.equals("CISRMarker")){
			uploadMarker = new CISRMarker();
		} else if (strMarkerType.equals("CAPMarker")){
			uploadMarker = new CAPMarker();
		} else if (strMarkerType.equals("SSRGenotype")){
			uploadMarker = new SSRGenotype();
		} else if (strMarkerType.equals("SNPGenotype") || strMarkerType.equals("GenericSNP")){ //20131209: Tulasi: Adding condition to check for GenericSNP type too
			uploadMarker = new SNPGenotype();
		} else if (strMarkerType.equals("LGCGenomicsSNP")){ //20131209: Tulasi: Adding condition to check for KBioScienceSNP
			uploadMarker = new KBioScienceGenotype();
		} else if (strMarkerType.equals("DArtGenotype")){
			uploadMarker = new DARTGenotype();
		} else if (strMarkerType.equals("AllelicData")){
			uploadMarker = new MappingAllelic();
		} else if ( strMarkerType.equals("ABHData")){
			uploadMarker = new MappingABH();
		} else if (strMarkerType.equals("QTL")){
			uploadMarker = new QTLUpload();
		} else if (strMarkerType.equals("Map")){
			uploadMarker = new MapUpload();
		} else if (strMarkerType.equals("MTA")){
			uploadMarker = new MTAUpload();
		} else {
			//TODO
		}

	}
	
	private void exportToExcelSource(WritableWorkbook workbook, WritableSheet sheet1, Table tableForSourceTemplateFields, ArrayList<FieldProperties> listOfSourceColumnFields)
			throws WriteException, RowsExceededException {
			
			
		
			Container containerDataSource = tableForSourceTemplateFields.getContainerDataSource();
			int colIndex = 0;
			for (FieldProperties column : listOfSourceColumnFields) {
		        if (column != null) {
		            //setColumnHeader(column, header);
		        	WritableFont writableFont = new WritableFont(WritableFont.ARIAL);
					WritableCellFormat writableCellFormat = new WritableCellFormat(writableFont);
					if (false == column.getFieldName().equalsIgnoreCase(UploadField.SNo.toString())) {
						jxl.write.Label label = new jxl.write.Label(0, colIndex, column.getFieldName(), writableCellFormat);
						colIndex++;
						sheet1.addCell(label);
					}
		        }
		    }
			
			
			for (Object itemId : containerDataSource.getItemIds()) {
				colIndex = 0;
				for (Object propertyId : tableForSourceTemplateFields.getVisibleColumns()) {
					if (-1 != propertyId.toString().indexOf(UploadField.SNo.toString())){
						continue;
					}
					
					Property property = containerDataSource.getContainerProperty(itemId, propertyId);
					if (null != property){
		            	WritableFont writableFont = new WritableFont(WritableFont.ARIAL);
						WritableCellFormat writableCellFormat = new WritableCellFormat(writableFont);
						jxl.write.Label label = new jxl.write.Label(1, colIndex, property.getValue().toString(), writableCellFormat);
						sheet1.addCell(label);
		            }
					colIndex++;
				}
			}
	}

	private void exportToExcel(WritableWorkbook workbook, WritableSheet sheet1, Table tableForSourceTemplateFields, ArrayList<FieldProperties> listOfSourceColumnFields)
			throws WriteException, RowsExceededException {
			
			
		
			Container containerDataSource = tableForSourceTemplateFields.getContainerDataSource();
			int colIndex = 0;
			for (FieldProperties column : listOfSourceColumnFields) {
		        if (column != null) {
		            //setColumnHeader(column, header);
		        	WritableFont writableFont = new WritableFont(WritableFont.ARIAL);
					WritableCellFormat writableCellFormat = new WritableCellFormat(writableFont);
					if (false == column.getFieldName().equalsIgnoreCase(UploadField.SNo.toString())) {
						jxl.write.Label label = new jxl.write.Label(colIndex, 0, column.getFieldName(), writableCellFormat);
						colIndex++;
						sheet1.addCell(label);
					}
		        }
		    }
			
			int rowIndex = 1;
			
			for (Object itemId : containerDataSource.getItemIds()) {
				colIndex = 0;
				for (Object propertyId : tableForSourceTemplateFields.getVisibleColumns()) {
					Property property = containerDataSource.getContainerProperty(itemId,
							propertyId);
					if (-1 != propertyId.toString().indexOf(UploadField.SNo.toString())){
						continue;
					}
					
					if (null != property){
		            	WritableFont writableFont = new WritableFont(WritableFont.ARIAL);
						WritableCellFormat writableCellFormat = new WritableCellFormat(writableFont);
						Object value = property.getValue();
						if (null != value){
							jxl.write.Label label = new jxl.write.Label(colIndex, rowIndex, value.toString(), writableCellFormat);
							sheet1.addCell(label);
						}
		            }
					colIndex++;
				}
				rowIndex++;
			}
	}


	protected void buildListOfDataObjectsToBeUploaded() {

		int iRowCountInSourceTable = 0;

		if (strSourceSheetTitle.equalsIgnoreCase("SSRMarkers")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
		} else if (strSourceSheetTitle.equalsIgnoreCase("SNPMarkers")){
			strSourceSheetTitle = "SNPMarkers";
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
		} else if (strSourceSheetTitle.equalsIgnoreCase("CISRMarkers")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
		} else if (strSourceSheetTitle.equalsIgnoreCase("CAPMarkers")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
		} else if (strSourceSheetTitle.equalsIgnoreCase("SSR_Source")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
			//System.out.println("Num Of Rows in Source table: " + iRowCountInSourceTable);
			if (1 < iRowCountInSourceTable){
				_mainHomePage.getMainWindow().getWindow().showNotification("Source table should not have more than one row", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		} else if (strSourceSheetTitle.equalsIgnoreCase("SNPGenotype_Source")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
			//System.out.println("Num Of Rows in Source table: " + iRowCountInSourceTable);
			if (1 < iRowCountInSourceTable){
				_mainHomePage.getMainWindow().getWindow().showNotification("Source table should not have more than one row", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		} else if (strSourceSheetTitle.equalsIgnoreCase("LGCGenomicsSNPGenotype_Source")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
			if (1 < iRowCountInSourceTable){
				_mainHomePage.getMainWindow().getWindow().showNotification("Source table should not have more than one row", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
			
			
			if (null == txtFieldForDatasetName.getValue()) {
				_mainHomePage.getMainWindow().getWindow().showNotification("Please provide a valid value for Dataset Name", Notification.TYPE_ERROR_MESSAGE);
				return;
			} else if (txtFieldForDatasetName.getValue().toString().trim().equals("")) {
				_mainHomePage.getMainWindow().getWindow().showNotification("Please provide a valid value for Dataset Name", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
			
			Object valueGermplasm = comboBoxForGermplasm.getValue();
			if (valueGermplasm instanceof Integer){
				Integer itemId = (Integer)valueGermplasm;
				if (itemId.equals(1)){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the Germplasm required for LGC Genomics SNP upload.", Notification.TYPE_ERROR_MESSAGE);
					return;
				} 
			}
			
			_gdmsModel.setDatasetSelected(txtFieldForDatasetName.getValue().toString());
			_gdmsModel.setGermplasmSelected(valueGermplasm.toString());
		} else if (strSourceSheetTitle.equalsIgnoreCase("DArT_Source")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
			//System.out.println("Num Of Rows in Source table: " + iRowCountInSourceTable);
			if (1 < iRowCountInSourceTable){
				_mainHomePage.getMainWindow().getWindow().showNotification("Source table should not have more than one row", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		} else if (strSourceSheetTitle.equalsIgnoreCase("Mapping_Source")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
			//System.out.println("Num Of Rows in Source table: " + iRowCountInSourceTable);
			
			/**
			 * 20130813: Fix for Issue No. 57 on Trello
			 * 
			 * Added code to validate Marker Type and Map required for Allelic Data upload
			 * 
			 */
			String strMarkerType = _strMarkerType.replace(" ", "");
			if (strMarkerType.equals("AllelicData")) { //20131111: Tulasi --- Added a condition to check for Marker Type, which is required only for Allelic Mapping Upload
				Object valueMarkerType = optiongroup.getValue();
				if (null == valueMarkerType){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select a Marker Type", Notification.TYPE_ERROR_MESSAGE);
					return;
				} else {
					String strSelectedMarkerType = valueMarkerType.toString();
					String strMappingType = _strMarkerType.replace(" ", "");
					_gdmsModel.setMarkerForMap(strMappingType, strSelectedMarkerType);
				}
			} else {
				String strMappingType = _strMarkerType.replace(" ", "");
				_gdmsModel.setMarkerForMap(strMappingType, "");
			}
			
			/** 20130823: Fix for Issue No: 62  
			 * 
			 *  Map is not a mandaotory for Allelic Mapping upload --- Commented the code added previously
			 */
			/*Object valueMap = comboBoxForMap.getValue();
			if (valueMap instanceof Integer){
				Integer itemId = (Integer)valueMap;
				if (itemId.equals(1)){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for Allelic Data Upload.", Notification.TYPE_ERROR_MESSAGE);
					return;
				} 
			}*/ 
			//20130813: Fix for Issue No. 57 on Trello
			/** 20130823: Fis for Issue No: 62 Map is not required for Allelic Mapping upload */
			
			
			if (1 < iRowCountInSourceTable){
				_mainHomePage.getMainWindow().getWindow().showNotification("Source table should not have more than one row", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		} else if (strSourceSheetTitle.equalsIgnoreCase("QTL_Source")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
			//System.out.println("Num Of Rows in Source table: " + iRowCountInSourceTable);
			if (1 < iRowCountInSourceTable){
				_mainHomePage.getMainWindow().getWindow().showNotification("Source table should not have more than one row", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		} else if (strSourceSheetTitle.equalsIgnoreCase("Map_Source")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
			//System.out.println("Num Of Rows in Source table: " + iRowCountInSourceTable);
			if (1 < iRowCountInSourceTable){
				_mainHomePage.getMainWindow().getWindow().showNotification("Source table should not have more than one row", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		} else if (strSourceSheetTitle.equalsIgnoreCase("MTA_Source")){
			iRowCountInSourceTable = tableForSourceTemplateFields.size();
			//System.out.println("Num Of Rows in Source table: " + iRowCountInSourceTable);
			if (1 < iRowCountInSourceTable){
				_mainHomePage.getMainWindow().getWindow().showNotification("Source table should not have more than one row", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		}		


		/** Building data list of all rows from the Source table */
		ArrayList<HashMap<String, String>> listOfSourceDataRows = new ArrayList<HashMap<String,String>>();
		for (int i = 1; i <= iRowCountInSourceTable; i++){
			Item item = tableForSourceTemplateFields.getItem(i);
			if (null != item){
				HashMap<String, String> hmOfFieldNameAndValue = new HashMap<String, String>();
				for (int j = 0; j < listOfSourceColumnFields.size(); j++){
					FieldProperties fieldProperties = listOfSourceColumnFields.get(j);
					String strFieldName = fieldProperties.getFieldName();
					if (false == strFieldName.equalsIgnoreCase("SNO.")){
						Property itemProperty = item.getItemProperty(strFieldName);
						if (null != itemProperty.getValue()){
							String strValue = itemProperty.getValue().toString();
							hmOfFieldNameAndValue.put(strFieldName, strValue);
						}
					}
				}
				listOfSourceDataRows.add(hmOfFieldNameAndValue);
			}
		}


		/** Building data list of all rows from the Data table */
		ArrayList<HashMap<String, String>> listOfDataRows = new ArrayList<HashMap<String,String>>();
		if (null != listOfDataColumnFields && 0 != listOfDataColumnFields.size()){
			int iNumOfRowsInDataTable = tableForDataTemplateFields.size();
			for (int i = 1; i <= iNumOfRowsInDataTable; i++){
				Item item = tableForDataTemplateFields.getItem(i);
				if (null != item){
					HashMap<String, String> hmOfFieldNameAndValue = new HashMap<String, String>();
					for (int j = 0; j < listOfDataColumnFields.size(); j++){
						FieldProperties fieldProperties = listOfDataColumnFields.get(j);
						String strFieldName = fieldProperties.getFieldName();
						
						
						if (false == strFieldName.equalsIgnoreCase("SNO.")){
							Property itemProperty = item.getItemProperty(strFieldName);
							if (null != itemProperty.getValue()){
								String strValue = itemProperty.getValue().toString();
								hmOfFieldNameAndValue.put(strFieldName, strValue);
							}
						}
					}
					listOfDataRows.add(hmOfFieldNameAndValue);
				}
			}
		}
		
		

		/** Building data list of all rows from the GID table */
		ArrayList<HashMap<String, String>> listOfDataRowsFromGIDsTable = new ArrayList<HashMap<String,String>>();
		if (null != listOfAdditionalGIDsColumnFieldsForDArT && 0 != listOfAdditionalGIDsColumnFieldsForDArT.size()){
			int iNumOfRowsInDataTable = tableForGIDsTemplateFieldsForDArTGenotype.size();
			for (int i = 1; i <= iNumOfRowsInDataTable; i++){
				Item item = tableForGIDsTemplateFieldsForDArTGenotype.getItem(i);
				if (null != item){
					HashMap<String, String> hmOfFieldNameAndValue = new HashMap<String, String>();
					for (int j = 0; j < listOfAdditionalGIDsColumnFieldsForDArT.size(); j++){
						FieldProperties fieldProperties = listOfAdditionalGIDsColumnFieldsForDArT.get(j);
						String strFieldName = fieldProperties.getFieldName();
						if (false == strFieldName.equalsIgnoreCase("SNO.")){
							Property itemProperty = item.getItemProperty(strFieldName);
							if (null != itemProperty.getValue()){
								String strValue = itemProperty.getValue().toString();
								hmOfFieldNameAndValue.put(strFieldName, strValue);
							}
						}
					}
					listOfDataRowsFromGIDsTable.add(hmOfFieldNameAndValue);
				}
			}
		}


		if (listOfSourceDataRows.size() > 0){
			uploadMarker.setListOfColumns(listOfSourceColumnFields);
			uploadMarker.setDataToBeUploded(listOfSourceDataRows, listOfDataRows, listOfDataRowsFromGIDsTable);

			//2013112: Tulasi --- Modified the following code to display error messages for SNPGenotype uploads in a Window
			boolean bUploadSuccessful = false;
			
			try {
				
				uploadMarker.upload();
				bUploadSuccessful = true;
				
			} catch (GDMSException e) {
				
				bUploadSuccessful = false;
				
				String strMarkerType = _strMarkerType.replace(" ", "");
				if (strMarkerType.equals("SNPGenotype") || strMarkerType.equals("GenericSNP") || strMarkerType.equals("LGCGenomicsSNP")) {
					
					Window messageWindow = new Window("SNPGenotype Upload Error Message");
					
					GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage, "Error uploading SNPGenotype \r\n\n" + 
					                                                      e.getMessage());
					messageWindow.addComponent(gdmsMessageWindow);
					messageWindow.setWidth("500px");
					messageWindow.setBorder(Window.BORDER_NONE);
					messageWindow.setClosable(true);
					messageWindow.center();
					
					if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
						_mainHomePage.getMainWindow().addWindow(messageWindow);
					}
					
				} else {
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
			
			if (bUploadSuccessful) {
				String strDataUploaded = uploadMarker.getDataUploaded();

				Window messageWindow = new Window("Upload Message");
				GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage, "Data uploaded successfully \r\n\n" + strDataUploaded);
				messageWindow.addComponent(gdmsMessageWindow);
				messageWindow.setWidth("500px");
				messageWindow.setBorder(Window.BORDER_NONE);
				messageWindow.setClosable(true);
				messageWindow.center();

				if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
					_mainHomePage.getMainWindow().addWindow(messageWindow);
				} 
			}
			//2013112: Tulasi --- Modified the code to display error messages for SNPGenotype upload in a Window
		} else {
			_mainHomePage.getMainWindow().getWindow().showNotification("Please add data to be uploaded.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}

	}

	private void getMarkerFields() {
		//String strSourceFileName = "";
		//String strDataFileName = "";
		//String strAdditonalGIDsFileNameForDArt = "";

		String strMarkerType = _strMarkerType.replace(" ", "");
		
		FieldProperties[] listOfSourceFieldProperties = null;
		FieldProperties[] listOfDataFieldProperties = null;
		FieldProperties[] listOfGIDsFieldPropertiesForDArT = null;
		
		listOfSourceColumnFields = new ArrayList<FieldProperties>();
		listOfDataColumnFields = new ArrayList<FieldProperties>();
		listOfAdditionalGIDsColumnFieldsForDArT = new ArrayList<FieldProperties>();
		
		if (strMarkerType.equals("SSRMarker")){
			//strSourceFileName = "SSRMarker";
			listOfSourceFieldProperties = UploadTableFields.SSRMarker;
			strSourceSheetTitle = "SSRMarkers";
		} else if (strMarkerType.equals("SNPMarker")){
			//strSourceFileName = "SNPMarker";
			listOfSourceFieldProperties = UploadTableFields.SNPMarker;
			strSourceSheetTitle = "SNPMarkers";
		} else if (strMarkerType.equals("CISRMarker")){
			//strSourceFileName = "CISRMarker";
			listOfSourceFieldProperties = UploadTableFields.CISRMarker;
			strSourceSheetTitle = "CISRMarkers";
		} else if (strMarkerType.equals("CAPMarker")){
			//strSourceFileName = "CAPMarker";
			listOfSourceFieldProperties = UploadTableFields.CAPMarker;
			strSourceSheetTitle = "CAPMarkers";
		} else if (strMarkerType.equals("SSRGenotype")){
			//strSourceFileName = "SSRGenotype_Source";
			//strDataFileName = "SSRGenotype_Data";
			listOfSourceFieldProperties = UploadTableFields.SSRGenotype_Source;
			listOfDataFieldProperties = UploadTableFields.SSRGenotype_Data;
			strSourceSheetTitle = "SSR_Source";
			strDataSheetTitle = "SSR_Data List";
		} /*else if (strMarkerType.equals("SNPGenotype")){
			//strSourceFileName = "SNPGenotype_Source";
			//strDataFileName = "SNPGenotype_Data";
			listOfSourceFieldProperties = UploadTableFields.SNPGenotype_Source;
			listOfDataFieldProperties = UploadTableFields.SNPGenotype_Data;
			strSourceSheetTitle = "SNPGenotype_Source";
			strDataSheetTitle = "SNPGenotype_Data";
		} */
		else if (strMarkerType.equals("GenericSNP")){
			//"Generic SNP"
			//TODO: 20131206: Tulasi --- Implementation pending - to display the data from the template in the table 
			listOfSourceFieldProperties = UploadTableFields.SNPGenotype_Source;
			listOfDataFieldProperties = UploadTableFields.SNPGenotype_Data;
			strSourceSheetTitle = "SNPGenotype_Source";
			strDataSheetTitle = "SNPGenotype_Data";
		} else if (strMarkerType.equals("LGCGenomicsSNP")){
			//"LGC Genomics SNP"
			//TODO: 20131206: Tulasi --- Implementation pending - to display the data from the template in the table 
			listOfSourceFieldProperties = UploadTableFields.LGCGenomicsSNPGenotype_Source;
			listOfDataFieldProperties = UploadTableFields.LGCGenomicsSNPGenotype_Data;
			strSourceSheetTitle = "LGCGenomicsSNPGenotype_Source";
			strDataSheetTitle = "LGCGenomicsSNPGenotype_Data";
		} else if (strMarkerType.equals("DArtGenotype")){
			//strSourceFileName = "DArtGenotype_Source";
			//strDataFileName = "DArtGenotype_Data";
			//strAdditonalGIDsFileNameForDArt = "DArtGenotype_GID";
			listOfSourceFieldProperties = UploadTableFields.DArtGenotype_Source;
			listOfDataFieldProperties = UploadTableFields.DArtGenotype_Data;
			listOfGIDsFieldPropertiesForDArT = UploadTableFields.DArtGenotype_GID;
			strSourceSheetTitle = "DArT_Source";
			strDataSheetTitle = "DArT_Data";
			strGIDsSheetTitleForDArT = "DArT_GIDs";
		} else if (strMarkerType.equals("AllelicData") || strMarkerType.equals("ABHData") ){
			//strSourceFileName = "MappingGenotype_Source";
			//strDataFileName = "MappingGenotype_Data";
			listOfSourceFieldProperties = UploadTableFields.MappingGenotype_Source;
			listOfDataFieldProperties = UploadTableFields.MappingGenotype_Data;
			strSourceSheetTitle = "Mapping_Source";
			strDataSheetTitle = "Mapping_DataList";
		} else if (strMarkerType.equals("QTL")){
			//strSourceFileName = "QTL_Source";
			//strDataFileName = "QTL_Data";
			listOfSourceFieldProperties = UploadTableFields.QTL_Source;
			listOfDataFieldProperties = UploadTableFields.QTL_Data;
			strSourceSheetTitle = "QTL_Source";
			strDataSheetTitle = "QTL_Data";
		} else if (strMarkerType.equals("Map")){
			//strSourceFileName = "Map_Source";
			//strDataFileName = "Map_Data";
			listOfSourceFieldProperties = UploadTableFields.Map_Source;
			listOfDataFieldProperties = UploadTableFields.Map_Data;
			strSourceSheetTitle = "Map_Source";
			strDataSheetTitle = "Map_Data";
		}  else if (strMarkerType.equals("MTA")){
			//strSourceFileName = "Map_Source";
			//strDataFileName = "Map_Data";
			listOfSourceFieldProperties = UploadTableFields.MTA_Source;
			listOfDataFieldProperties = UploadTableFields.MTA_Data;
			strSourceSheetTitle = "Map_Source";
			strDataSheetTitle = "Map_Data";
		}else {
			listOfSourceFieldProperties = UploadTableFields.SSRMarker;
		}
		
		if (null != listOfSourceFieldProperties){
			for  (FieldProperties fieldProperties : listOfSourceFieldProperties) {
				listOfSourceColumnFields.add(fieldProperties);
			}
		}

		if (null != listOfDataFieldProperties){
			for  (FieldProperties fieldProperties : listOfDataFieldProperties) {
				listOfDataColumnFields.add(fieldProperties);
			}
			
			if (null != listOfVariableDataColumns){
				for (String columnTitle : listOfVariableDataColumns){
					FieldProperties fieldProperties = new FieldProperties(columnTitle, UploadField.REQUIRED.toString(), "", "");
					listOfDataColumnFields.add(fieldProperties);
				}
			}
		}
		
		if (null != listOfGIDsFieldPropertiesForDArT){
			for  (FieldProperties fieldProperties : listOfGIDsFieldPropertiesForDArT) {
				listOfAdditionalGIDsColumnFieldsForDArT.add(fieldProperties);
			}
		}		
		
	}


	private Layout buildTabbedComponentForTemplate(){
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		//verticalLayout.setWidth("700px");
		verticalLayout.addStyleName(Reindeer.LAYOUT_WHITE);

		CssLayout mainCSSLayout = new CssLayout();
		mainCSSLayout.setMargin(false, true, true, true);
		mainCSSLayout.setSizeFull();

		tabsheetForMarkerTemplates = new TabSheet();
		tabsheetForMarkerTemplates.setSizeFull();
		mainCSSLayout.addComponent(tabsheetForMarkerTemplates);
		tabsheetForMarkerTemplates.setSelectedTab(0);
		

		Layout buildSourceTableComponent = buildSourceTableComponent();
		tabsheetForMarkerTemplates.addComponent(buildSourceTableComponent);
		
		iNumOfTabs = 1;
		if (null != listOfDataColumnFields && listOfDataColumnFields.size() > 0){
			if (null != listOfVariableDataColumns) {
				Layout existingDataTableComponent = (Layout) tabsheetForMarkerTemplates.getTab(1);
				Layout newDataTableComponent = buildDataTableComponent();
				tabsheetForMarkerTemplates.replaceComponent(existingDataTableComponent, newDataTableComponent);
				tabsheetForMarkerTemplates.requestRepaint();
				tabsheetForMarkerTemplates.setSelectedTab(1);
				existingDataTableComponent = newDataTableComponent;
			} else {
				Layout buildDataTableComponent = buildDataTableComponent();
				tabsheetForMarkerTemplates.addComponent(buildDataTableComponent);
				iNumOfTabs += 1;
			}
			
		}
		
		/*Layout newMarkerTableComponent = buildTabbedComponentForTemplate();
		int iNumOfTabs = tabsheetForMarkerTemplates.getComponentCount();
		if (1 == iNumOfTabs) {
			btnAdd.setVisible(true);
		} else {
			btnAdd.setVisible(false);
		}
		tabsheetForMarkerTemplates.addListener(tabSheetListener);
		verticalLayout.replaceComponent(layoutForMarkerTableComponent, newMarkerTableComponent);
		verticalLayout.requestRepaint();
		layoutForMarkerTableComponent = newMarkerTableComponent;*/


		if (null != listOfAdditionalGIDsColumnFieldsForDArT && listOfAdditionalGIDsColumnFieldsForDArT.size() > 0){
			Layout buildAdditionalGIDsSheetForDArTComponent = buildAdditionalGIDsSheetForDArT();
			tabsheetForMarkerTemplates.addComponent(buildAdditionalGIDsSheetForDArTComponent);
			iNumOfTabs += 1;
		}

		verticalLayout.addComponent(tabsheetForMarkerTemplates);
		verticalLayout.setComponentAlignment(tabsheetForMarkerTemplates, Alignment.TOP_LEFT);

		return verticalLayout;
	}

	private Layout buildSourceTableComponent() {

		VerticalLayout verticalLayout = new VerticalLayout();
		//verticalLayout.setMargin(false, false, true, false);
		verticalLayout.setWidth("700px");
		verticalLayout.addStyleName(Reindeer.LAYOUT_WHITE);

		tableForSourceTemplateFields = new Table();
		tableForSourceTemplateFields.setWidth("100%");
		tableForSourceTemplateFields.setPageLength(10);
		tableForSourceTemplateFields.setSelectable(true);
		tableForSourceTemplateFields.setColumnCollapsingAllowed(true);
		tableForSourceTemplateFields.setColumnReorderingAllowed(true);
		tableForSourceTemplateFields.setStyleName("strong");
		tableForSourceTemplateFields.addListener(this);
		tableForSourceTemplateFields.setImmediate(true);
		//tableForSourceTemplateFields.setStyleName("reqcol");
		
		tableForSourceTemplateFields.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 1L;
			public void itemClick(ItemClickEvent event) {
				if (event.isDoubleClick()){
					tableForSourceTemplateFields.setEditable(true);
					return;
				}
				tableForSourceTemplateFields.setEditable(false);
			}
		});


		if (null == sourceDataListToBeDisplayedInTheTable){

			String[] arrayEmptyColumns = new String[listOfSourceColumnFields.size()];
			String strEmptyColumns = "1";
			arrayEmptyColumns[0] = strEmptyColumns;
			for (int i = 0; i < listOfSourceColumnFields.size(); i++){
				String strFieldName = listOfSourceColumnFields.get(i).getFieldName();
				String isReq = listOfSourceColumnFields.get(i).getIsReq();
				if (isReq.equalsIgnoreCase(UploadField.REQUIRED.toString())){
					//strFieldName = "<html><font color=red>" + strFieldName + "</font></html>";
					tableForSourceTemplateFields.addContainerProperty(strFieldName, String.class, "");
				} else {
					//strFieldName = "<html><font color=white>" + strFieldName + "</font>buil</html>";
					tableForSourceTemplateFields.addContainerProperty(strFieldName, String.class, "");
				}
				if (0 < i){
					arrayEmptyColumns[i] = new String("");
				}
			}

			tableForSourceTemplateFields.addItem(arrayEmptyColumns, new Integer(1));
			//iRowCount = 1;
			tableForSourceTemplateFields.setEditable(true);
		} else {

			if (sourceDataListToBeDisplayedInTheTable.size() > 0){
				
				for (int i = 0; i < listOfSourceColumnFields.size(); i++){
					String strFieldName = listOfSourceColumnFields.get(i).getFieldName();
					String isReq = listOfSourceColumnFields.get(i).getIsReq();
					if (isReq.equalsIgnoreCase(UploadField.REQUIRED.toString())){
						//strFieldName = "<html><font color=red>" + strFieldName + "</font></html>";
						tableForSourceTemplateFields.addContainerProperty(strFieldName, String.class, "");
					} else {
						//strFieldName = "<html><font color=white>" + strFieldName + "</font></html>";
						tableForSourceTemplateFields.addContainerProperty(strFieldName, String.class, "");
					}
				}
				
				for (int i = 0; i< sourceDataListToBeDisplayedInTheTable.size(); i++){

					HashMap<String,String> hashMap = sourceDataListToBeDisplayedInTheTable.get(i);

					String[] strItems = new String[listOfSourceColumnFields.size()];
					for (int j = 0; j < listOfSourceColumnFields.size(); j++){
						if (j == 0){
							strItems[j] = String.valueOf(i+1);
						} else {
							FieldProperties fieldProperties = listOfSourceColumnFields.get(j);
							String fieldName = fieldProperties.getFieldName();
							strItems[j] = hashMap.get(fieldName);
						}
					}
					tableForSourceTemplateFields.addItem(strItems, new Integer(i+1));
					//iRowCount = i + 1;
				}
			}
		}

		verticalLayout.addComponent(tableForSourceTemplateFields);
		verticalLayout.setComponentAlignment(tableForSourceTemplateFields, Alignment.MIDDLE_CENTER);
		verticalLayout.setCaption(strSourceSheetTitle);

		return verticalLayout;
	}


	private Layout buildDataTableComponent() {

		VerticalLayout verticalLayout = new VerticalLayout();
		//verticalLayout.setMargin(false, false, true, false);
		verticalLayout.setWidth("700px");
		verticalLayout.addStyleName(Reindeer.LAYOUT_WHITE);

		String strMarkerType = _strMarkerType.replace(" ", "");

		tableForDataTemplateFields = new Table();
		tableForDataTemplateFields.setWidth("100%");
		tableForDataTemplateFields.setPageLength(10);
		tableForDataTemplateFields.setSelectable(true);
		tableForDataTemplateFields.setColumnCollapsingAllowed(true);
		tableForDataTemplateFields.setColumnReorderingAllowed(true);
		tableForDataTemplateFields.setStyleName("strong");
		tableForDataTemplateFields.addListener(this);
		tableForDataTemplateFields.setImmediate(true);
		
		if (null != listOfVariableColumnsToBeDeleted) {
			if (null != listOfVariableDataColumns){
				ArrayList<String> tempListOfVariableDataColumns = listOfVariableDataColumns;
				for (int i = 0; i < listOfVariableColumnsToBeDeleted.size(); i++) {
					String strColToBeDeleted = listOfVariableColumnsToBeDeleted.get(i);
					
					for (int j = 0; j < listOfVariableDataColumns.size(); j++){
						String strNewCol = listOfVariableDataColumns.get(j);
						if (strNewCol.equalsIgnoreCase(strColToBeDeleted)){
							tempListOfVariableDataColumns.remove(j);
						}
					}
				}
				listOfVariableDataColumns = tempListOfVariableDataColumns;
			}
			
			if (null != listOfDataColumnFields) {
				ArrayList<FieldProperties> tempListOfDataColumnFields = listOfDataColumnFields;
				for (int j = 0; j < listOfDataColumnFields.size(); j++){
					String fieldName = listOfDataColumnFields.get(j).getFieldName();
					
					for (int i = 0; i < listOfVariableColumnsToBeDeleted.size(); i++){
						String strColToBeDeleted = listOfVariableColumnsToBeDeleted.get(i);
						if (strColToBeDeleted.equalsIgnoreCase(fieldName)){
							tempListOfDataColumnFields.remove(j);
							break;
						}
					}
				}
				listOfDataColumnFields = tempListOfDataColumnFields;
			}
			
			
			if (null != dataListToBeDisplayedInTheTable && 0 != dataListToBeDisplayedInTheTable.size()){
				for (int i = 0; i < dataListToBeDisplayedInTheTable.size(); i++){
					HashMap<String,String> hashMap = dataListToBeDisplayedInTheTable.get(i);
					for (int j = 0; j < listOfVariableColumnsToBeDeleted.size(); j++){
						String fieldName = listOfVariableColumnsToBeDeleted.get(j);
						if (hashMap.containsKey(fieldName) || hashMap.containsKey(fieldName.toUpperCase())) {
							hashMap.remove(fieldName);
						}
					}
				}	
			}	
			
		}
		

		if (null != listOfVariableDataColumns){
			if (null != listOfDataColumnFields){
				for (String columnTitle : listOfVariableDataColumns){
					boolean bFound = false;
					for (int j = 0; j < listOfDataColumnFields.size(); j++){
						String fieldName = listOfDataColumnFields.get(j).getFieldName();
						if (true == fieldName.equalsIgnoreCase(columnTitle)){
							bFound = true;
						}
					}
					if (false == bFound){
						FieldProperties fieldProperties = new FieldProperties(columnTitle, UploadField.REQUIRED.toString(), "", "");
						listOfDataColumnFields.add(fieldProperties);
					}
				}
			}
		}
		
		if (null != dataListToBeDisplayedInTheTable && 0 != dataListToBeDisplayedInTheTable.size()){
			for (int i = 0; i < dataListToBeDisplayedInTheTable.size(); i++){
				HashMap<String,String> hashMap = dataListToBeDisplayedInTheTable.get(i);
				Iterator<String> iterator = hashMap.keySet().iterator();
				while(iterator.hasNext()){
					String next = iterator.next(); 
					boolean bFound = false;
					for (int j = 0; j < listOfDataColumnFields.size(); j++){
						String fieldName = listOfDataColumnFields.get(j).getFieldName();
						if (true == fieldName.equalsIgnoreCase(next)){
							bFound = true;
						}
					}
					if (false == bFound){
						FieldProperties fieldProperties = new FieldProperties(next, UploadField.REQUIRED.toString(), "", "");
						listOfDataColumnFields.add(fieldProperties);
					}
				}
			}
		}
		
		
		tableForDataTemplateFields.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 1L;
			public void itemClick(ItemClickEvent event) {

				if (event.isDoubleClick()){
					tableForDataTemplateFields.setEditable(true);
					return;
				}
				tableForDataTemplateFields.setEditable(false);
			}
		});

		if (null == dataListToBeDisplayedInTheTable){

			String[] arrayEmptyColumns = new String[listOfDataColumnFields.size()];
			String strEmptyColumns = "1";
			//arrayEmptyColumns[0] = strEmptyColumns;
			for (int i = 0; i < listOfDataColumnFields.size(); i++){
				String strFieldName = listOfDataColumnFields.get(i).getFieldName();
				String isReq = listOfDataColumnFields.get(i).getIsReq();
				if (isReq.equalsIgnoreCase(UploadField.REQUIRED.toString())){
					//strFieldName = "<html><font color=red>" + strFieldName + "</font></html>";
					tableForDataTemplateFields.addContainerProperty(strFieldName, String.class, "");
				} else {
					//strFieldName = "<html><font color=white>" + strFieldName + "</font></html>";
					tableForDataTemplateFields.addContainerProperty(strFieldName, String.class, "");
				}
				if (0 < i){
					arrayEmptyColumns[i] = new String("");
				} else {
					arrayEmptyColumns[0] = strEmptyColumns;
				}
			}

			tableForDataTemplateFields.addItem(arrayEmptyColumns, new Integer(1));
			//iRowCount = 1;
			tableForDataTemplateFields.setEditable(true);
		} else {

			if (dataListToBeDisplayedInTheTable.size() > 0){
				
				for (int i = 0; i < listOfDataColumnFields.size(); i++){
					String strFieldName = listOfDataColumnFields.get(i).getFieldName();
					String isReq = listOfDataColumnFields.get(i).getIsReq();
					if (isReq.equalsIgnoreCase(UploadField.REQUIRED.toString())){
						//strFieldName = "<html><font color=red>" + strFieldName + "</font></html>";
						tableForDataTemplateFields.addContainerProperty(strFieldName, String.class, "");
					} else {
						//strFieldName = "<html><font color=white>" + strFieldName + "</font></html>";
						tableForDataTemplateFields.addContainerProperty(strFieldName, String.class, "");
					}
				}
				
				for (int i = 0; i< dataListToBeDisplayedInTheTable.size(); i++){

					HashMap<String,String> hashMap = dataListToBeDisplayedInTheTable.get(i);

					String[] strItems = new String[listOfDataColumnFields.size()];
					if (strMarkerType.equals("AllelicData") || strMarkerType.equals("ABHData") ){
						for (int j = 0; j < listOfDataColumnFields.size(); j++){
							if (j == 0){
								strItems[j] = String.valueOf(i+1);
							} else {
								FieldProperties fieldProperties = listOfDataColumnFields.get(j);
								String fieldName = fieldProperties.getFieldName();
								strItems[j] = hashMap.get(fieldName);
							}
						}
						
					} else {
						for (int j = 0; j < listOfDataColumnFields.size(); j++){
							if (j == 0){
								strItems[j] = String.valueOf(i+1);
							} else {
								FieldProperties fieldProperties = listOfDataColumnFields.get(j);
								String fieldName = fieldProperties.getFieldName();
								String strFieldValue = hashMap.get(fieldName);
								if (null == strFieldValue){
									strItems[j] = ""; 
								} else {
									strItems[j] = strFieldValue;
								}
							}
						}
					}
					tableForDataTemplateFields.addItem(strItems, new Integer(i+1));
					//iRowCount = i + 1;
				}
			}
		}

		verticalLayout.addComponent(tableForDataTemplateFields);
		verticalLayout.setComponentAlignment(tableForDataTemplateFields, Alignment.MIDDLE_CENTER);
		verticalLayout.setCaption(strDataSheetTitle);

		return verticalLayout;
	}



	private Layout buildAdditionalGIDsSheetForDArT() {

		VerticalLayout verticalLayout = new VerticalLayout();
		//verticalLayout.setMargin(false, false, true, false);
		verticalLayout.setWidth("700px");
		verticalLayout.addStyleName(Reindeer.LAYOUT_WHITE);

		tableForGIDsTemplateFieldsForDArTGenotype = new Table();
		tableForGIDsTemplateFieldsForDArTGenotype.setWidth("100%");
		tableForGIDsTemplateFieldsForDArTGenotype.setPageLength(10);
		tableForGIDsTemplateFieldsForDArTGenotype.setSelectable(true);
		tableForGIDsTemplateFieldsForDArTGenotype.setColumnCollapsingAllowed(true);
		tableForGIDsTemplateFieldsForDArTGenotype.setColumnReorderingAllowed(true);
		tableForGIDsTemplateFieldsForDArTGenotype.setStyleName("strong");
		tableForGIDsTemplateFieldsForDArTGenotype.addListener(this);
		tableForGIDsTemplateFieldsForDArTGenotype.setImmediate(true);


		tableForGIDsTemplateFieldsForDArTGenotype.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 1L;
			public void itemClick(ItemClickEvent event) {

				if (event.isDoubleClick()){
					tableForGIDsTemplateFieldsForDArTGenotype.setEditable(true);
					return;
				}
				tableForGIDsTemplateFieldsForDArTGenotype.setEditable(false);
			}
		});


		if (null == dataListFromAdditionalGIDsSheetForDArTGenotype){
			String[] arrayEmptyColumns = new String[listOfAdditionalGIDsColumnFieldsForDArT.size()];
			String strEmptyColumns = "1";
			//arrayEmptyColumns[0] = strEmptyColumns;
			for (int i = 0; i < listOfAdditionalGIDsColumnFieldsForDArT.size(); i++){
				String strFieldName = listOfAdditionalGIDsColumnFieldsForDArT.get(i).getFieldName();
				String isReq = listOfAdditionalGIDsColumnFieldsForDArT.get(i).getIsReq();
				if (isReq.equalsIgnoreCase(UploadField.REQUIRED.toString())){
					//strFieldName = "<html><font color=red>" + strFieldName + "</font></html>";
					tableForGIDsTemplateFieldsForDArTGenotype.addContainerProperty(strFieldName, String.class, "");
				} else {
					//strFieldName = "<html><font color=white>" + strFieldName + "</font></html>";
					tableForGIDsTemplateFieldsForDArTGenotype.addContainerProperty(strFieldName, String.class, "");
				}
				if (0 < i){
					arrayEmptyColumns[i] = new String("");
				} else {
					arrayEmptyColumns[0] = strEmptyColumns;
				}
			}
			tableForGIDsTemplateFieldsForDArTGenotype.addItem(arrayEmptyColumns, new Integer(1));
			//iRowCount = 1;
			tableForGIDsTemplateFieldsForDArTGenotype.setEditable(true);
		} else {

			if (dataListFromAdditionalGIDsSheetForDArTGenotype.size() > 0){
				
				for (int i = 0; i < listOfAdditionalGIDsColumnFieldsForDArT.size(); i++){
					String strFieldName = listOfAdditionalGIDsColumnFieldsForDArT.get(i).getFieldName();
					String isReq = listOfAdditionalGIDsColumnFieldsForDArT.get(i).getIsReq();
					if (isReq.equalsIgnoreCase(UploadField.REQUIRED.toString())){
						//strFieldName = "<html><font color=red>" + strFieldName + "</font></html>";
						tableForGIDsTemplateFieldsForDArTGenotype.addContainerProperty(strFieldName, String.class, "");
					} else {
						//strFieldName = "<html><font color=white>" + strFieldName + "</font></html>";
						tableForGIDsTemplateFieldsForDArTGenotype.addContainerProperty(strFieldName, String.class, "");
					}
				}
				
				for (int i = 0; i< dataListFromAdditionalGIDsSheetForDArTGenotype.size(); i++){
					HashMap<String,String> hashMap = dataListFromAdditionalGIDsSheetForDArTGenotype.get(i);
					String[] strItems = new String[listOfAdditionalGIDsColumnFieldsForDArT.size()];
					for (int j = 0; j < listOfAdditionalGIDsColumnFieldsForDArT.size(); j++){
						if (j == 0){
							strItems[j] = String.valueOf(i+1);
						} else {
							FieldProperties fieldProperties = listOfAdditionalGIDsColumnFieldsForDArT.get(j);
							String fieldName = fieldProperties.getFieldName();
							strItems[j] = hashMap.get(fieldName);
						}
					}
					tableForGIDsTemplateFieldsForDArTGenotype.addItem(strItems, new Integer(i+1));
					//iRowCount = i + 1;
				}
			}
		}

		verticalLayout.addComponent(tableForGIDsTemplateFieldsForDArTGenotype);
		verticalLayout.setComponentAlignment(tableForGIDsTemplateFieldsForDArTGenotype, Alignment.MIDDLE_CENTER);
		verticalLayout.setCaption(strGIDsSheetTitleForDArT);

		return verticalLayout;
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}

	/*public void setListOfVariableFields(ArrayList<String> theListOfvariableDataColumns) {
		if (null != listOfDataColumnFields){
			for (String columnTitle : theListOfvariableDataColumns){
				FieldProperties fieldProperties = new FieldProperties(columnTitle, UploadField.REQUIRED.toString(), "", "");
				listOfDataColumnFields.add(fieldProperties);
			}
		} else {
			if (0 < theListOfvariableDataColumns.size()){
				listOfVariableDataColumns = new ArrayList<String>();
				for (String strVarCol : theListOfvariableDataColumns){
					if (false == strVarCol.equals("")){
						listOfVariableDataColumns = theListOfvariableDataColumns;
					}
				}
			}
		}
	}*/
	
	class TabSheetListener implements SelectedTabChangeListener {
		private static final long serialVersionUID = 1L;

		@Override
		public void selectedTabChange(SelectedTabChangeEvent event) {

			int iNumOfTabs = tabsheetForMarkerTemplates.getComponentCount();
			if (1 < iNumOfTabs) {
				Component selectedTab = tabsheetForMarkerTemplates.getSelectedTab();
				Tab sourceTab = tabsheetForMarkerTemplates.getTab(0);
				iSelectedTab = 0;
				if (!selectedTab.getCaption().equals(sourceTab.getCaption())){
					btnAdd.setVisible(true);
				} else {
					btnAdd.setVisible(false);
				}
				
				if (selectedTab.getCaption().equals("SNPGenotype_Data") || selectedTab.getCaption().equals("DArT_Data") ||
						selectedTab.getCaption().equals("Mapping_DataList") || 
						selectedTab.getCaption().equals("LGCGenomicsSNP") || selectedTab.getCaption().equals("GenericSNP")){
					btnAddColumns.setVisible(true);
					btnDeleteColumns.setVisible(true);
				} else {
					btnAddColumns.setVisible(false);
					btnDeleteColumns.setVisible(false);
				}
				
			} else {
				btnAdd.setVisible(true);
				btnAddColumns.setVisible(false);
				btnDeleteColumns.setVisible(false);
			}
	    
		}
		
	}

}
