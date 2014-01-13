package org.icrisat.gdms.ui;

import java.util.ArrayList;
import org.icrisat.gdms.ui.common.UploadVariableFieldsListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class UploadVariableFieldsDialog extends CustomComponent {

	private static final long serialVersionUID = 1L;
	private GDMSMain _mainHomePage;
	private Button _confirmButton;
	private String _strRequiredDataType;
	private TextArea textArea;
	private ArrayList<String> listOfTemplateDataColumns;
	private UploadVariableFieldsListener _uploadVariableFieldsListener;
	private boolean _bToAddColumns;
	
	
	public UploadVariableFieldsDialog(GDMSMain theMainHomePage, String theDataTypeRequired, boolean bToAddColumns) {
		_mainHomePage = theMainHomePage;
		_strRequiredDataType = theDataTypeRequired;
		_bToAddColumns = bToAddColumns;
		setCompositionRoot(buildMessageWindow());
	}


	public VerticalLayout buildMessageWindow() {
		
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setCaption("Upload Variable Fields");
		verticalLayout.setSpacing(true);
		
		String strTextAreaCaption = "Please provide the " +  _strRequiredDataType + "(separated by commas) to be added." + "\n";
		
		if (false == _bToAddColumns) {
			strTextAreaCaption = "Please provide the " +  _strRequiredDataType + "(separated by commas) to be deleted." + "\n";
		}
		
		textArea = new TextArea();
		textArea.setCaption(strTextAreaCaption);
		textArea.setWidth("400px");
		textArea.setHeight("150px");
		
		
		_confirmButton = new Button("Confirm");
		_confirmButton.setDescription("Confirm");
		_confirmButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				obtainListOfGIDsInTextArea();
				_mainHomePage.getMainWindow().getWindow().removeWindow(getWindow());
			}
		});
		
		verticalLayout.addComponent(textArea);
		
		verticalLayout.addComponent(_confirmButton);
		verticalLayout.setComponentAlignment(_confirmButton, Alignment.BOTTOM_CENTER);
		
		return verticalLayout;
	}

	public void obtainListOfGIDsInTextArea(){
		listOfTemplateDataColumns = new ArrayList<String>();
		String strTextWithDataFields = textArea.getValue().toString();
		String[] arrayOfDataFields = strTextWithDataFields.split(",");
		for (int i = 0; i < arrayOfDataFields.length; i++){
			listOfTemplateDataColumns.add(arrayOfDataFields[i]);
		}
		_uploadVariableFieldsListener.uploadVariableFields(listOfTemplateDataColumns);
	}
	
	public ArrayList<String> getlistOfTemplateDataColumns(){
		return listOfTemplateDataColumns;
	}
	
	@Override
	public void detach() {
		super.detach();
	}


	public void addListener(UploadVariableFieldsListener uploadVariableFieldsListener) {
			_uploadVariableFieldsListener = uploadVariableFieldsListener;
	}
}
