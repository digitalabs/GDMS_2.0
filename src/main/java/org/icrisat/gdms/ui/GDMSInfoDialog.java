package org.icrisat.gdms.ui;


import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class GDMSInfoDialog extends CustomComponent {

	private static final long serialVersionUID = 1L;
	private GDMSMain _mainHomePage;
	private String _strMessage;
	private Button _okButton;
	
	
	public GDMSInfoDialog(GDMSMain theMainHomePage, String theMessage) {
		_mainHomePage = theMainHomePage;
		_strMessage = theMessage;
		setCompositionRoot(buildMessageWindow());
	}


	public VerticalLayout buildMessageWindow() {
		
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setCaption("Upload Message");
		
		
		TextArea textArea = new TextArea();
		textArea.setValue(_strMessage);
		textArea.setReadOnly(true);
		textArea.setWidth("400px");
		textArea.setHeight("150px");
		
		
		_okButton = new Button("Ok");
		_okButton.setDescription("Ok");
		_okButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				_mainHomePage.getMainWindow().getWindow().removeWindow(getWindow());
			}
		});
		
		verticalLayout.addComponent(textArea);
		
		verticalLayout.addComponent(_okButton);
		verticalLayout.setComponentAlignment(_okButton, Alignment.BOTTOM_CENTER);
		
		return verticalLayout;
	}

	
	@Override
	public void detach() {
		super.detach();
	}
}
