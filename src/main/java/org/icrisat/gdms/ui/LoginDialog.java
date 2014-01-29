package org.icrisat.gdms.ui;

import java.util.ArrayList;
import java.util.List;
import org.generationcp.middleware.dao.UserDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;


public class LoginDialog extends CustomComponent {

	private static final long serialVersionUID = 1L;
	protected String _strEnteredUserName;
	protected String _strEnteredPassword;
	private GDMSMain _mainHomePage;
	private GDMSModel _gdmsModel;
	private User _loggedInUser;
	private boolean _bLoginSuccessful;
	private Button _loginButton;
	private Button _clearButton;

	
	public LoginDialog(GDMSMain theMainHomePage) {
		_mainHomePage = theMainHomePage;
		_gdmsModel = GDMSModel.getGDMSModel();
		setCompositionRoot(buildLoginWindow());
	}

	public FormLayout buildLoginWindow() {

		final FormLayout loginFormLayout = new FormLayout();
		loginFormLayout.setCaption("Login Details");
		loginFormLayout.setMargin(true, true, true, true);
		
		final TextField txtFieldUserName = new TextField("User Name:");
		txtFieldUserName.setRequired(true);
		txtFieldUserName.setWidth("250px");
		txtFieldUserName.setCursorPosition(0);
		loginFormLayout.addComponent(txtFieldUserName);

		final PasswordField txtFieldPassword = new PasswordField("Password:");
		txtFieldPassword.setRequired(true);
		txtFieldPassword.setWidth("250px");
		loginFormLayout.addComponent(txtFieldPassword);

		
		
		_loginButton = new Button("Login");
		_loginButton.setDescription("Login");

		_loginButton.addListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				if (event.getButton().getDescription().equals("Login") && (_bLoginSuccessful == false)) {
					
					_strEnteredUserName = txtFieldUserName.getValue().toString();
					_strEnteredPassword = txtFieldPassword.getValue().toString();

					if (false == validateFields()){
						_bLoginSuccessful = false;
						_loggedInUser = null;
						return;
					}
					try{
					HibernateSessionProvider hibernateSessionProviderForLocal = _gdmsModel.getManagerFactory().getSessionProviderForLocal();
					UserDAO userDAOLocal = new UserDAO();
					userDAOLocal.setSession(hibernateSessionProviderForLocal.getSession());
					
					HibernateSessionProvider hibernateSessionProviderForCentral = _gdmsModel.getManagerFactory().getSessionProviderForCentral();
					UserDAO userDAOCentral = new UserDAO();
					userDAOCentral.setSession(hibernateSessionProviderForCentral.getSession());
					
					List<User> allUsers = new ArrayList<User>();
					
					try {
						List<User> listOfAllUsersFromLocal = userDAOLocal.getAll();
						
						List<User> listOfAllUsersFromCentral = userDAOCentral.getAll();
						
						for (User user : listOfAllUsersFromLocal){
							if (false == allUsers.contains(user)){
								allUsers.add(user);
							}
						}
						
						for (User user : listOfAllUsersFromCentral){
							if (false == allUsers.contains(user)){
								allUsers.add(user);
							}
						}
						
					} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving User's information from the Database.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					

					validateLoginDetails(allUsers);

					if (_bLoginSuccessful){
						_loginButton.setDescription("Close");
					}
					
					}catch (Exception e) {
						//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving User's information from the Database.", Notification.TYPE_ERROR_MESSAGE);
						e.printStackTrace();
						return;
					}
				} else if (event.getButton().getDescription().equals("Close")){
					_mainHomePage.getMainWindow().getWindow().removeWindow(getWindow());
					_mainHomePage.requestRepaintAfterLogin();
				}
			}
		});
		_loginButton.addShortcutListener(new Button.ClickShortcut(_loginButton, KeyCode.ENTER){});
		
		_clearButton = new Button("Clear");
		_clearButton.setDescription("Clear");
		_clearButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				txtFieldUserName.setValue("");
				txtFieldPassword.setValue("");
			}
		});
		
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.addComponent(_loginButton);
		horizontalLayout.addComponent(_clearButton);
		
		loginFormLayout.addComponent(horizontalLayout);

		return loginFormLayout;
	}
	


	protected boolean validateFields() {

		if (null == _strEnteredUserName || _strEnteredUserName.trim().toString().equals("")){
			_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a valid user name", Notification.TYPE_ERROR_MESSAGE);
			return false;
		}

		if (null == _strEnteredPassword || _strEnteredPassword.trim().toString().equals("")){
			_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a valid password.", Notification.TYPE_ERROR_MESSAGE);
			return false;
		}

		return true;
	}



	protected void validateLoginDetails(List<User> allUsers) {
		//Check if the entered user-details are present in the DB
		boolean bUserExists = false;
		boolean bCorrectPassword = false;

		for (User user : allUsers){
			
			String strExistingUserName = user.getName();
			String strExistingPassword = user.getPassword();
			Integer userid = user.getUserid();
			
			_loggedInUser = user;

			if (strExistingUserName.equals(_strEnteredUserName)){
				bUserExists = true;
				if (strExistingPassword.equals(_strEnteredPassword)){
					bCorrectPassword = true;
					_bLoginSuccessful = true;
					
					/*try {
						GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId()
						WorkbenchDataManagerImpl workbenchDataManagerImpl = new WorkbenchDataManagerImpl(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal());
						WorkbenchRuntimeData workbenchRuntimeData = workbenchDataManagerImpl.getWorkbenchRuntimeData();
						
						if (null != workbenchRuntimeData){
							workbenchRuntimeData.setUserId(userid);
						}
						
					} catch (Exception e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
						return;
					}*/
					
					break;
				} else {
					bCorrectPassword = false;
					_bLoginSuccessful = false;
				}
			} else {
				bUserExists = false;
			}
		}

		if (!_bLoginSuccessful){
			if (!bUserExists){
				_mainHomePage.getMainWindow().getWindow().showNotification("User does not exist.", Notification.TYPE_ERROR_MESSAGE);
				return;
			} else {
				if (!bCorrectPassword){
					_mainHomePage.getMainWindow().getWindow().showNotification("Password does not match.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		} else {
			_gdmsModel.setLoggedInUser(_loggedInUser);
			_loginButton.setDescription("Close");
			_loginButton.click();
		}
	}

	public boolean isLoginSuccessful(){
		return _bLoginSuccessful;
	}

	@Override
	public void detach() {
		super.detach();
	}	

}
