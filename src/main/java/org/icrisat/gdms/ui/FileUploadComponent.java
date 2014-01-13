package org.icrisat.gdms.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerAlias;
import org.generationcp.middleware.pojos.gdms.MarkerDetails;
import org.generationcp.middleware.pojos.gdms.MarkerUserInfo;
import org.icrisat.gdms.common.GDMSException;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

public class FileUploadComponent extends CustomComponent {

	private static final long serialVersionUID = 1L;

	public File file;
	private GDMSMain _mainHomePage;
	private String _strDataType;
	private Marker[] arrayOfMarkers;
	private MarkerAlias[] arrayOfMarkerAlias;
	private MarkerUserInfo[] arrayOfMarkerUserInfo;
	private MarkerDetails[] arrayOfMarkerDetails;
	//private List<MarkerNameElement> listOfMarkerNamesByGIds;
	public List<Integer> listOfGIDs;
	public ArrayList<String> listOfMarkers;

	private FileUploadListener _fileUploadListener;
	private TextField txtFieldLocation;
	private File dupFile;
/**
	 * 
	 * 20131205: Tulasi --- Modified this custom component which is being used to read GIDs and Markers from a text file, in
	 * 
	 * RetrieveGIDInformation and RetrieveMarkersComponent
	 * 
	 * @param theMainHomePage
	 * @param theDataType
	 */

	public FileUploadComponent(GDMSMain theMainHomePage, String theDataType) {
		_mainHomePage = theMainHomePage;
		_strDataType = theDataType;
		
		GridLayout gridLayout = new GridLayout(3, 1);
		gridLayout.setSpacing(true);
		gridLayout.setWidth("500px");

		txtFieldLocation = new TextField();
		//txtFieldLocation.setWidth("150px");
		txtFieldLocation.setWidth("270px");
		txtFieldLocation.setInputPrompt("Please provide the template location.");
		gridLayout.addComponent(txtFieldLocation);
		gridLayout.setComponentAlignment(txtFieldLocation, Alignment.BOTTOM_LEFT);

		Button submitButton = new Button("Submit");
		submitButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
		
			public void buttonClick(ClickEvent event) {
				//allowUpload = true;
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
					
					if (_strDataType.equals("GID")){
						//Trying to retrieve the Markers from the database based on the list of 
						//GIDs given in the text file
						try {
							
							listOfGIDs = obtainTheListOfGIDsFromTheTextFile(dupFile.getAbsolutePath());
							_fileUploadListener.updateLocation(dupFile.getAbsolutePath());
						} catch (GDMSException e1) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error reading the file. Please provide a text file with valid GIDs.", Notification.TYPE_ERROR_MESSAGE);
							return;
						} 
					} else if (_strDataType.equalsIgnoreCase("Marker Name")){
						try {
							listOfMarkers = obtainTheListOfMarkersFromTheTextFile(dupFile.getAbsolutePath());
							_fileUploadListener.updateLocation(dupFile.getAbsolutePath());
						} catch (GDMSException e1) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error reading the file. Please provide a text file with valid Marker-Names.", Notification.TYPE_ERROR_MESSAGE);
							return;
						} 
						
					}
				}
				
			}
		});
		
		gridLayout.addComponent(uploadComponent);
		/*if(bHaveSubmit){
			gridLayout.addComponent(submitButton);
			gridLayout.setComponentAlignment(submitButton, Alignment.BOTTOM_RIGHT);
		}*/

		setCompositionRoot(gridLayout);
	
	}

	public void init (String context) {
		VerticalLayout layout = new VerticalLayout();

		if ("basic".equals(context))
			basic(layout);
		else
			layout.addComponent(new Label("Invalid context: " + context));

		setCompositionRoot(layout);
	}

	void basic(VerticalLayout layout) {
		Upload upload = new Upload("", null);
		//upload.setButtonCaption("Start Upload");

		if (null == _strDataType){
			upload.setVisible(false);
		}
		
		if (_strDataType.equals("GID") || _strDataType.equalsIgnoreCase("Marker Name")){
			upload.setButtonCaption("Read File");
		} else {
			upload.setButtonCaption("View in Table");
		}

		class FileUploader implements Receiver, SucceededListener {
			private static final long serialVersionUID = 1L;
			private ArrayList<HashMap<String, String>> listOfMarkersToBeDisplayedInTheTable;
		
			public OutputStream receiveUpload(String filename, String mimeType) {
				FileOutputStream fos = null; // Output stream to write to
				try {
					file = new File(filename);
					fos = new FileOutputStream(file);

					if (null != _fileUploadListener){
						//TODO
						//_fileUploadListener.update(file);
					}
					
				} catch (final java.io.FileNotFoundException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Could not open file<br/>", e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
					return null;
				}
				return fos; // Return the output stream to write to
			}

			
			public void uploadSucceeded(SucceededEvent event) {

				String strAbsolutePath = file.getAbsolutePath();

				if (_strDataType.equals("GID")){
					//Trying to retrieve the Markers from the database based on the list of 
					//GIDs given in the text file
					try {
						
						listOfGIDs = obtainTheListOfGIDsFromTheTextFile(strAbsolutePath);
						_fileUploadListener.updateLocation(strAbsolutePath);
					} catch (GDMSException e1) {
						_mainHomePage.getMainWindow().getWindow().showNotification("Error reading the file. Please provide a text file with valid GIDs.", Notification.TYPE_ERROR_MESSAGE);
						return;
					} 
				} else if (_strDataType.equalsIgnoreCase("Marker Name")){
					try {
						listOfMarkers = obtainTheListOfMarkersFromTheTextFile(strAbsolutePath);
						_fileUploadListener.updateLocation(strAbsolutePath);
					} catch (GDMSException e1) {
						_mainHomePage.getMainWindow().getWindow().showNotification("Error reading the file. Please provide a text file with valid Marker-Names.", Notification.TYPE_ERROR_MESSAGE);
						return;
					} 
				} else {
				
				}
			}
		};

		final FileUploader uploader = new FileUploader(); 
		upload.setReceiver(uploader);
		upload.addListener(uploader);

		layout.addComponent(upload);
	}
	
	
	public List<Integer> obtainTheListOfGIDsFromTheTextFile(String theFileLocation) throws GDMSException {

		List<Integer> listOfGIDs = new ArrayList<Integer>();
		
		BufferedReader bufReader = null;

		try {

			bufReader = new BufferedReader(new FileReader(theFileLocation));

			String strLine = "";

			while ((strLine = bufReader.readLine()) != null) {
				if (false == strLine.equals(""))
					if (false == strLine.equals("GIDs"))

						try {
							String strGID = strLine;
							int iGID = Integer.parseInt(strGID);
							listOfGIDs.add(iGID);
						} catch (NumberFormatException nfe){
							throw new GDMSException(nfe.getMessage());
						}

			}
			
		} catch (FileNotFoundException e) {
			throw new GDMSException(e.getMessage());
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} 
		return listOfGIDs;
	}



	private ArrayList<String> obtainTheListOfMarkersFromTheTextFile(String strAbsolutePath) throws GDMSException  {
		ArrayList<String> listOfMarkers = new ArrayList<String>();
		BufferedReader bufReader = null;

		try {

			bufReader = new BufferedReader(new FileReader(strAbsolutePath));

			String strLine = "";

			while ((strLine = bufReader.readLine()) != null) {
				if (false == strLine.equals(""))
				if (false == strLine.equalsIgnoreCase("Marker Name"))
				listOfMarkers.add(strLine);
			}
			
		} catch (FileNotFoundException e) {
			throw new GDMSException(e.getMessage());
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} 
		return listOfMarkers;
	}

	
	public Marker[] getArrayOfMarkers() {
		return arrayOfMarkers;
	}

	public void setArrayOfMarkers(Marker[] arrayOfMarkers) {
		this.arrayOfMarkers = arrayOfMarkers;
	}

	public MarkerAlias[] getArrayOfMarkerAlias() {
		return arrayOfMarkerAlias;
	}

	public void setArrayOfMarkerAlias(MarkerAlias[] arrayOfMarkerAlias) {
		this.arrayOfMarkerAlias = arrayOfMarkerAlias;
	}

	public MarkerUserInfo[] getArrayOfMarkerUserInfo() {
		return arrayOfMarkerUserInfo;
	}

	public void setArrayOfMarkerUserInfo(MarkerUserInfo[] arrayOfMarkerUserInfo) {
		this.arrayOfMarkerUserInfo = arrayOfMarkerUserInfo;
	}

	public MarkerDetails[] getArrayOfMarkerDetails() {
		return arrayOfMarkerDetails;
	}

	public void setArrayOfMarkerDetails(MarkerDetails[] arrayOfMarkerDetails) {
		this.arrayOfMarkerDetails = arrayOfMarkerDetails;
	}


	public List<Integer> getListOfGIDs(){
		return listOfGIDs;
	}

	public ArrayList<String> getListOfMarkers() {
		return listOfMarkers;
	}

	public void registerListener(FileUploadListener fileUploadListener) {
		_fileUploadListener = fileUploadListener;
	}
}
