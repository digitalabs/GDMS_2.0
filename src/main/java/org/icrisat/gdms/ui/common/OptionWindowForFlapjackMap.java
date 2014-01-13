package org.icrisat.gdms.ui.common;

import org.icrisat.gdms.ui.MapOptionsListener;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout.MarginInfo;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;

public class OptionWindowForFlapjackMap extends CustomComponent {

	private static final long serialVersionUID = 1L;
	private Label lblErrorMessage;
	private MapOptionsListener mapOptionsListener;
	
	public OptionWindowForFlapjackMap() {
		
		setCompositionRoot(buildMapOptionLayout());
	}

	
	private VerticalLayout buildMapOptionLayout() {
		
		//setStyleName("gdmscustomcomponent");
		
		VerticalLayout mapOptionLayout = new VerticalLayout();
		mapOptionLayout.setCaption("Map");
		mapOptionLayout.setMargin(new MarginInfo(true, true, true, true));
		
		String strSubTitle1 = "Create Flapjack without Map?";
		Label lblSubTitle1 = new Label();
		lblSubTitle1.setValue(strSubTitle1);
		lblSubTitle1.setStyleName("subtitlelabel");
		
		
		final OptionGroup optiongroup = new OptionGroup();
		optiongroup.setNullSelectionAllowed(false);
		optiongroup.setMultiSelect(false);
		optiongroup.addItem("Yes"); 
		optiongroup.addItem("No");
		optiongroup.setValue("No");//Default selection is No
		optiongroup.setImmediate(true);

		lblErrorMessage = new Label();
		lblErrorMessage.setStyleName("errmessage");
		lblErrorMessage.setValue("");
	    lblErrorMessage.setVisible(false);
		
		Button btnConfirm = new Button("Confirm");
		btnConfirm.setDescription("Click this to confirm selection.");
		btnConfirm.setWidth("180px");
		btnConfirm.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				
				Object value = optiongroup.getValue();
				String strValue = value.toString();
				
				if (strValue.equals("Yes")) {
					mapOptionsListener.isMapRequiredOption(false); //Implies to create Flapjack without Map
				} else {
					mapOptionsListener.isMapRequiredOption(true); //Implies to create Flapjack with Map 
				}
			}
		});
		
		
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.addComponent(lblSubTitle1);
		verticalLayout.addComponent(optiongroup);
		verticalLayout.addComponent(lblErrorMessage);
		verticalLayout.addComponent(btnConfirm);
		verticalLayout.setSizeUndefined();
		verticalLayout.setMargin(new MarginInfo(true, true, true, true));
		verticalLayout.setWidth("90%");

		
		VerticalLayout finalVerticalLayout = new VerticalLayout();
		finalVerticalLayout.addComponent(verticalLayout);
		finalVerticalLayout.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);
		//finalVerticalLayout.setStyleName("verticalLayoutMapOptionWindow");
		finalVerticalLayout.setWidth("90%");
		
		return finalVerticalLayout;
	}

	
	@Override
	public void detach() {
		super.detach();
	}


	public void addMapOptionListener(MapOptionsListener theMapOptionsListener) {
		mapOptionsListener = theMapOptionsListener;
	}
}
