package org.icrisat.gdms.ui;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionPerThreadProvider;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.hibernate.HibernateUtil;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.HeadingOne;

import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;



public class GDMSMain extends Application implements Component.Listener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Window _main;
	private VerticalLayout _mainLayout;
	private TabSheet _tabsheet;
	private GDMSModel _gdmsModel;
	private MenuItem _homeMenu;
	//private MenuItem _uploadMenu;
	//private MenuItem _retrieveMenu;
	//private MenuItem _deleteMenu;
	private MenuItem _loginMenu;
	private Label _lblLoginMessage;
	private MenuItem _contactMenu;
	private UploadComponent buildUploadDataWindow;
	private RetrieveComponent buildRetrieveWindow;
	private DeleteComponent buildDeleteWindow;
	private VerticalLayout buildWelcomeScreen;
	private Window loginWindow;

	 WorkbenchDataManager  workbenchManager;
	 private static HibernateUtil hibernateUtil;
	@Override
	public void init() {
		
		/*try{			
			System.out.println(GDMSModel.getGDMSModel().getWorkbenchParams().getDbName()+"  "+GDMSModel.getGDMSModel().getWorkbenchParams().getHost()+"   "+GDMSModel.getGDMSModel().getWorkbenchParams().getPort()+"   "+GDMSModel.getGDMSModel().getWorkbenchParams().getUsername()+"   "+GDMSModel.getGDMSModel().getWorkbenchParams().getPassword());
			hibernateUtil = new HibernateUtil(GDMSModel.getGDMSModel().getWorkbenchParams());
			HibernateSessionProvider sessionProvider = new HibernateSessionPerThreadProvider(hibernateUtil.getSessionFactory());
			workbenchManager = new WorkbenchDataManagerImpl(sessionProvider);
		}catch (FileNotFoundException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}catch (IOException ei) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}catch (URISyntaxException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		*/
		
		try{
			int userId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
			User user1=	GDMSModel.getGDMSModel().getWorkbenchDataManager().getUserById(userId);
			
		_gdmsModel = GDMSModel.getGDMSModel();
		User user2 = new User();
		user2.setUserid(new Integer(user1.getUserid()));
		user2.setName(user1.getName());
		user2.setPassword(user1.getPassword());
		_gdmsModel.setLoggedInUser(user2);
		
		setTheme("gdmstheme");

		_main = new Window("Genotyping Data Management System (GDMS)");
		//GDMSModel.getGDMSModel().getWorkbenchParams().getHibernateSessionProviderForCentral().getSession();
		/*try{
			System.out.println(".............  workbenchloogedin user:"+workbenchManager.getWorkbenchRuntimeData().getUserId());
		}catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}*/
		_mainLayout = (VerticalLayout) _main.getContent();
		_mainLayout.setMargin(false);
		_mainLayout.setStyleName(Reindeer.LAYOUT_BLUE);
		_mainLayout.addListener(this);
		_mainLayout.setImmediate(true);
		
		_main.setImmediate(true);
		setMainWindow(_main);

		buildMainView();
		}catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}catch (ConfigException e) {
			e.printStackTrace();
			//_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			//return null;
		}

	}

	void buildMainView() {
		_mainLayout.setSizeFull();

		
		_lblLoginMessage = new Label("");
		_lblLoginMessage.setStyleName(Reindeer.LABEL_H2);
		
		HorizontalLayout topMenuLayout = getTopMenu();
		CssLayout headerImageLayout = getHeader();

		VerticalLayout topLayout = new VerticalLayout();
		topLayout.addComponent(headerImageLayout);
		topLayout.addComponent(topMenuLayout);
		topLayout.setComponentAlignment(topMenuLayout, Alignment.TOP_RIGHT);
		topLayout.addComponent(_lblLoginMessage);
		topLayout.setComponentAlignment(_lblLoginMessage, Alignment.TOP_RIGHT);
		topLayout.setMargin(false, true, true, true);
		_mainLayout.addComponent(topLayout);


		CssLayout mainCSSLayout = new CssLayout();
		mainCSSLayout.setMargin(false, true, true, true);
		mainCSSLayout.setSizeFull();

		_tabsheet = new TabSheet();
		_tabsheet.setSizeFull();
		
		mainCSSLayout.addComponent(_tabsheet);
		_mainLayout.addComponent(mainCSSLayout);
		_mainLayout.setExpandRatio(mainCSSLayout, 1);

		buildWelcomeScreen = buildWelcomeScreen();
		
		buildUploadDataWindow = new UploadComponent(this);
		buildUploadDataWindow.setImmediate(true);
		buildUploadDataWindow.setEnabled(false);
		buildUploadDataWindow.addListener(this);
		buildUploadDataWindow.setWidth("100%");

		buildRetrieveWindow = new RetrieveComponent(this);
		buildRetrieveWindow.setImmediate(true);
		buildRetrieveWindow.setEnabled(false);
		buildRetrieveWindow.addListener(this);
		buildRetrieveWindow.setWidth("100%");

		buildDeleteWindow = new DeleteComponent(this);
		buildDeleteWindow.setImmediate(true);
		buildDeleteWindow.setEnabled(false);
		buildDeleteWindow.addListener(this);
		buildRetrieveWindow.setWidth("100%");

		_tabsheet.addComponent(buildWelcomeScreen);
		_tabsheet.addComponent(buildUploadDataWindow);
		_tabsheet.addComponent(buildRetrieveWindow);
		_tabsheet.addComponent(buildDeleteWindow);

		/*if (null == _gdmsModel.getLoggedInUser()){
			buildUploadDataWindow.setEnabled(false);
			buildRetrieveWindow.setEnabled(false);
			buildDeleteWindow.setEnabled(false);
		} else {
			buildUploadDataWindow.setEnabled(true);
			buildRetrieveWindow.setEnabled(true);
			buildDeleteWindow.setEnabled(true);
		}*/
		
		if (null == _gdmsModel.getLoggedInUser()){
			_tabsheet.getTab(1).setEnabled(false);
			_tabsheet.getTab(2).setEnabled(false);
			_tabsheet.getTab(3).setEnabled(false);
		} else {
			_tabsheet.getTab(1).setEnabled(true);
			_tabsheet.getTab(2).setEnabled(true);
			_tabsheet.getTab(3).setEnabled(true);
		}
		
		_tabsheet.addListener(new SelectedTabChangeListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void selectedTabChange(SelectedTabChangeEvent event) {
				User loggedInUser = _gdmsModel.getLoggedInUser();
				//System.out.println("loggedInUser:"+loggedInUser);
				if (null == loggedInUser){
					if (!(buildWelcomeScreen == _tabsheet.getSelectedTab())){
						getMainWindow().showNotification("Please login inorder to Upload, Retrieve or Delete data.", Notification.TYPE_HUMANIZED_MESSAGE);
						return;
					} 
				} else {
					if(event.getTabSheet().getSelectedTab().getCaption() == "Upload") {
						updateUploadTabComponent();
					} else if (event.getTabSheet().getSelectedTab().getCaption() == "Retrieve"){
						updateRetrieveTabComponent();
					} else if (event.getTabSheet().getSelectedTab().getCaption() == "View"){
						updateDeleteTabComponent();
					} 
				}
				
			}

		});

		CssLayout bottomPanelLayout = getBottomPanelLayout();
		VerticalLayout bottomContactLayout = new VerticalLayout();
		bottomContactLayout.addComponent(bottomPanelLayout);
		bottomContactLayout.setComponentAlignment(bottomPanelLayout, Alignment.BOTTOM_LEFT);
		bottomContactLayout.setMargin(false, true, false, true);
	}

	VerticalLayout buildWelcomeScreen() {
		VerticalLayout layoutForWelcomeTab = new VerticalLayout();
		layoutForWelcomeTab.setMargin(true);
		layoutForWelcomeTab.setSpacing(true);
		layoutForWelcomeTab.setCaption("Welcome");
		layoutForWelcomeTab.setStyleName(Reindeer.LAYOUT_WHITE);

		CssLayout cssLayout = new CssLayout();
		cssLayout.setMargin(true);
		cssLayout.setWidth("100%");
		layoutForWelcomeTab.addComponent(cssLayout);

		HeadingOne title = new HeadingOne("Welcome to Genotyping Data Management");
		cssLayout.addComponent(title);

		HorizontalLayout horizLayoutForIntroPara = new HorizontalLayout();

		horizLayoutForIntroPara.setSpacing(true);
		horizLayoutForIntroPara.setWidth("100%");
		horizLayoutForIntroPara.setMargin(true, false, true, false);
		cssLayout.addComponent(horizLayoutForIntroPara);

		String strIntroPara1 = "<p>The Genotyping Data Management System aims to provide a comprehensive public repository " +
		"for genotype, linkage map and QTL data from crop species relevant in the semi-arid tropics.</p>";

		String strIntroPara2 = "<p>This system is developed in Java and the database is MySQL. The initial release record " +
		"details of current genotype datasets generated for GCP mandate crops along with details of " +
		"molecular markers and related metadata. The Retrieve tab is a good starting point to browse " +
		"or query the database contents. The datasets available for each crop species can be queried. " +
		"Access to data sets requires a login.</p>";

		String strIntroPara3 = "<p>Data may be currently exported to the following formats: 2x2 matrix and flapjack software formats. " +
		"Data submission is through templates; upload templates are available for genotype, QTL and " +
		"map data(type of markers - SSR, SNP and DArt). The templates are in the form of excel sheets with built-in " +
		"validation functions.</p>";

		Label lblPara = new Label(strIntroPara1 + strIntroPara2 + strIntroPara3, Label.CONTENT_XHTML);
		horizLayoutForIntroPara.addComponent(lblPara);
		horizLayoutForIntroPara.setExpandRatio(lblPara, 1);

		//Spacer
		lblPara = new Label("");
		lblPara.setWidth("20px");
		horizLayoutForIntroPara.addComponent(lblPara);

		/** Commented the following two on 11-02-2013: 12:44 PM and tried ThemeResource */
		ThemeResource themeResource = new ThemeResource("images/FlowChart.jpg");
		Embedded headerImage = new Embedded("", themeResource);

		headerImage.setWidth("500px");
		headerImage.setHeight("400px");
		horizLayoutForIntroPara.addComponent(headerImage);

		return layoutForWelcomeTab;
	}

	HorizontalLayout getTopMenu() {
		HorizontalLayout horizontalLayout = new HorizontalLayout();

		MenuBar menubar = new MenuBar();

		MenuBar.Command menuCommand = new MenuBar.Command() {
			private static final long serialVersionUID = 1L;

			public void menuSelected(MenuItem selectedItem) {

				if (selectedItem.getText().equals("Login")){
					openLoginWindow();
				} else if (selectedItem.getText().equals("Logout")){
					
					_gdmsModel.setLoggedInUser(null);
					_loginMenu.setText("Login");
					setUser(null);
					_lblLoginMessage.setValue("");
					
					//buildUploadDataWindow.setEnabled(false);
					//buildRetrieveWindow.setEnabled(false);
					//buildDeleteWindow.setEnabled(false);
					
					if (buildWelcomeScreen == _tabsheet.getSelectedTab()){
						_tabsheet.getSelectedTab().setEnabled(true);
					} else {
						_tabsheet.getSelectedTab().setEnabled(false);
					}
					
					_tabsheet.getTab(1).setEnabled(false);
					_tabsheet.getTab(2).setEnabled(false);
					_tabsheet.getTab(3).setEnabled(false);
					
					if (!getMainWindow().getChildWindows().contains(loginWindow)) {
						getMainWindow().removeWindow(loginWindow);
						loginWindow = null;
					}
					
				} else if (selectedItem.getText().equals("Contact")){ 
					getMainWindow().showNotification("Functionality for Contact is yet to be added.", Notification.TYPE_HUMANIZED_MESSAGE);
				} else if (selectedItem.getText().equals("Help")){ 
					getMainWindow().showNotification("Functionality for Help menu-item is yet to be added.", Notification.TYPE_HUMANIZED_MESSAGE);
				} else {
					_gdmsModel.setMenuItemSelected(null);
					updateAllTabComponents();
				}
			}
		};

		//_homeMenu = menubar.addItem("Home", null);
		_contactMenu = menubar.addItem("Contact", menuCommand);
		final MenuBar.MenuItem helpMenu = menubar.addItem("Help", menuCommand);
		Object user2 = getUser();
		if(null == user2) {
			_loginMenu = menubar.addItem("Login", menuCommand);
		} else {
			_loginMenu = menubar.addItem("Logout", menuCommand);
		}
		

		if (null != _gdmsModel.getLoggedInUser()){
			_loginMenu.setEnabled(false);
			uploadLoginDetailsOnMainWindow();
		}
		
		horizontalLayout.addComponent(menubar);

		return horizontalLayout;
	}


	protected void uploadLoginDetailsOnMainWindow() {
		
		User loggedInUser = _gdmsModel.getLoggedInUser();

		if (null != loggedInUser){
			String name = loggedInUser.getName();
			setUser(loggedInUser);
			_loginMenu.setText("Logout");
			_lblLoginMessage.setValue("Welcome " + name + "!");
		}
		//_mainLayout.addComponent(_lblLoginMessage);
		//_mainLayout.setComponentAlignment(_lblLoginMessage, Alignment.MIDDLE_LEFT);
		//_mainLayout.requestRepaintAll();
	}

	/** Following method updates the contents of the Upload tab */
	protected void updateUploadTabComponent() {
		Component newUploadTabComponent = (Component) new UploadComponent(this);
		newUploadTabComponent.setWidth("100%");
		Component currentUploadTabComponent = _tabsheet.getSelectedTab();
		_tabsheet.replaceComponent(currentUploadTabComponent, newUploadTabComponent);
		
		_tabsheet.requestRepaint();
	}

	/** Following method updates the contents of the Retrieve tab */
	protected void updateRetrieveTabComponent() {

		Component newRetrieveTabComponent = (Component) new RetrieveComponent(this);
		newRetrieveTabComponent.setWidth("100%");
		Component currentRetrieveTabComponent = _tabsheet.getSelectedTab();

		_tabsheet.replaceComponent(currentRetrieveTabComponent, newRetrieveTabComponent);
		_tabsheet.requestRepaint();
	}

	/** Following method updates the contents of the Delete tab */
	protected void updateDeleteTabComponent() {

		Component newDeleteTabComponent = (Component) new DeleteComponent(this);
		newDeleteTabComponent.setWidth("100%");
		Component currentDeleteTabComponent = _tabsheet.getSelectedTab();

		_tabsheet.replaceComponent(currentDeleteTabComponent, newDeleteTabComponent);
		_tabsheet.requestRepaint();
	}

	/** 
	 *  Following method updates the contents of Upload, Retrieve and Delete tabs in the TabSheet
	 *  to display the default data, if no MenuItem is selected
	 *  
	 */
	protected void updateAllTabComponents() {
		Component newUploadTabComponent = (Component) new UploadComponent(this);
		newUploadTabComponent.setWidth("100%");
		Component existingUploadTabcomponent = _tabsheet.getTab(1).getComponent();
		_tabsheet.replaceComponent(existingUploadTabcomponent, newUploadTabComponent);

		Component newRetrieveTabComponent = (Component) new RetrieveComponent(this);
		newRetrieveTabComponent.setWidth("100%");
		Component existingRetrieveTabcomponent = _tabsheet.getTab(2).getComponent();
		_tabsheet.replaceComponent(existingRetrieveTabcomponent, newRetrieveTabComponent);

		Component newDeleteTabComponent = (Component) new DeleteComponent(this);
		newDeleteTabComponent.setWidth("100%");
		Component existingDeleteTabcomponent = _tabsheet.getTab(3).getComponent();
		_tabsheet.replaceComponent(existingDeleteTabcomponent, newDeleteTabComponent);

		_tabsheet.requestRepaint();

	}

	CssLayout getHeader() {
		CssLayout cssLayoutForHeaderImage = new CssLayout();
		cssLayoutForHeaderImage.setWidth("100%");


		ThemeResource themeResource = new ThemeResource("images/GDMS.gif");
		//ThemeResource themeResource = new ThemeResource("images/Banner3.jpg");
		Embedded headerImage = new Embedded("", themeResource);

		headerImage.setSizeFull();
		cssLayoutForHeaderImage.setSizeFull();
		cssLayoutForHeaderImage.addComponent(headerImage);
		cssLayoutForHeaderImage.setMargin(false, false, false, false);

		return cssLayoutForHeaderImage;
	}


	CssLayout getBottomPanelLayout() {
		CssLayout cssLayoutForContactImage = new CssLayout();
		cssLayoutForContactImage.setWidth("100%");

		ThemeResource themeResource = new ThemeResource("images/GDMS_Footer.gif");
		Embedded contactImage = new Embedded("", themeResource);

		contactImage.setWidth("1000px");
		contactImage.setHeight("20px");
		cssLayoutForContactImage.addComponent(contactImage);

		return cssLayoutForContactImage;
	}


	void openLoginWindow() {
		
		if (null == loginWindow) {
			loginWindow = new Window("Login");
			LoginDialog loginBox = new LoginDialog(this);
			loginWindow.addComponent(loginBox);
			loginWindow.setWidth("400px");
			loginWindow.setBorder(Window.BORDER_NONE);
			loginWindow.setClosable(true);
			loginWindow.center();

		} 
	
		if (!getMainWindow().getChildWindows().contains(loginWindow)) {
			getMainWindow().addWindow(loginWindow);
		}
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}

	public void requestRepaintAfterLogin(){
		User loggedInUser = _gdmsModel.getLoggedInUser();

		if (null != loggedInUser){
			String name = loggedInUser.getName();
			_loginMenu.setText("Logout");
			setUser(loggedInUser);
			_lblLoginMessage.setValue("Welcome " + name + "!");
			_tabsheet.getSelectedTab().setEnabled(true);
			
			_tabsheet.getTab(1).setEnabled(true);
			_tabsheet.getTab(2).setEnabled(true);
			_tabsheet.getTab(3).setEnabled(true);
		}
		
	}
}
