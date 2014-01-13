package org.icrisat.gdms.ui.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import org.icrisat.gdms.ui.FileUploadListener;
import org.icrisat.gdms.ui.GDMSMain;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Window.Notification;


public class GDMSFileChooser extends CustomComponent {

	private static final long serialVersionUID = 1L;
	boolean allowUpload = false;
	private TextField txtFieldLocation;
	private GDMSMain _mainHomePage;
	protected File file;
private FileUploadListener _fileUploadListener;
protected File dupFile;

	public GDMSFileChooser(GDMSMain theMainHomePage, boolean bHaveSubmit){

		_mainHomePage = theMainHomePage;

		GridLayout gridLayout = new GridLayout(3, 1);
		gridLayout.setSpacing(true);

		txtFieldLocation = new TextField();
		txtFieldLocation.setWidth("350px");
		txtFieldLocation.setInputPrompt("Please provide the template location.");
		gridLayout.addComponent(txtFieldLocation);
		gridLayout.setComponentAlignment(txtFieldLocation, Alignment.BOTTOM_LEFT);

		Button submitButton = new Button("Submit");
		submitButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				allowUpload = true;
				_mainHomePage.getMainWindow().getWindow().showNotification("Marker details saved to the database.");
			}
		});


		Upload uploadComponent = new Upload("", new Upload.Receiver() {
			private static final long serialVersionUID = 1L;
			public OutputStream receiveUpload(String filename, String mimeType) {
				FileOutputStream fos = null; // Output stream to write to
				try {
					file = new File(filename);
					
					FileResource fileResource = new FileResource(file, _mainHomePage.getMainWindow().getApplication());
					String absolutePath = fileResource.getSourceFile().getAbsolutePath();
					
					dupFile = new File(absolutePath);
					dupFile.createNewFile();
					
					
					
					fos = new FileOutputStream(file);
					txtFieldLocation.setValue(dupFile.getName());

					if (null != _fileUploadListener){
						//System.out.println("Dup file's location: " + dupFile.getAbsolutePath());
						//_fileUploadListener.updateLocation(dupFile.getAbsolutePath());
					}

				} catch (final java.io.FileNotFoundException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Could not open file<br/>", e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
					return null;
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Could not open file<br/>", e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
					return null;
				}
				return fos; // Return the output stream to write to
			}
		});
		uploadComponent.setImmediate(true);
		uploadComponent.setButtonCaption("Browse");
		uploadComponent.addListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadFinished(FinishedEvent event) {
				
				if (null == dupFile){
					return;
				}
				
				if (null != _fileUploadListener){
					//System.out.println("Dup file's location: " + dupFile.getAbsolutePath());
					_fileUploadListener.updateLocation(dupFile.getAbsolutePath());
				}
				
			}
		});
		
		gridLayout.addComponent(uploadComponent);
		if(bHaveSubmit){
			gridLayout.addComponent(submitButton);
			gridLayout.setComponentAlignment(submitButton, Alignment.BOTTOM_RIGHT);
		}

		setCompositionRoot(gridLayout);
	}

	public void registerListener(FileUploadListener fileUploadListener) {
		_fileUploadListener = fileUploadListener;
	}

}
