package org.icrisat.gdms.upload.genotyping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;

public class KBioScienceGenotype implements  UploadMarker {
	
	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();
	private HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();
	private BufferedReader bReader;
	private HashMap<Integer, String> hmOfColIndexAndGermplasmName;
	private HashMap<String, String> hmOfData;
	private int iDataRowIndex;

	
	//20131209: Tulasi --- Implemented data to be displayed from the template file and display on the GUI
	
	@Override
	public void readExcelFile() throws GDMSException {
		/*try {
			workbook = Workbook.getWorkbook(new File(strFileLocation));
			strSheetNames = workbook.getSheetNames();
		} catch (BiffException e) {
			throw new GDMSException("Error Reading KBio Science SNP Genotype Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading KBio Science SNP Sheet - " + e.getMessage());
		}*/
		try {
			bReader = new BufferedReader(new FileReader(strFileLocation));
		} catch (FileNotFoundException e) {
			throw new GDMSException(e.getMessage());
		} 
	}

	@Override
	public void validateDataInExcelSheet() throws GDMSException {
		
		
		String strLine = "";
		boolean bDataStarts = false;
		
		try {
			while ((strLine = bReader.readLine()) != null) {
				
				if (strLine.startsWith("Project number")) {
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");	
					
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.ProjectNumber.toString(), strArrayOfTokens[1]);
					}
				} else if (strLine.startsWith("Order number")) {
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.OrderNumber.toString(), strArrayOfTokens[1]);
					}
				} else if (strLine.startsWith("Plates")) {
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.Plates.toString(), strArrayOfTokens[1]);
					}
				}
				
				if(strLine.startsWith("DNA\\Assay") || strLine.startsWith("DNA \\ Assay") || 
						strLine.startsWith("Sample Name")) {
					
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");	
					
					hmOfColIndexAndGermplasmName = new HashMap<Integer, String>();
					for(int iColIndex = 0; iColIndex < strArrayOfTokens.length; iColIndex++){
						String strMarkerName = strArrayOfTokens[iColIndex];
						hmOfColIndexAndGermplasmName.put(iColIndex, strMarkerName);
					}
					
					bDataStarts = true;
					
				} else {
					
					if (null == hmOfData) {
						hmOfData = new HashMap<String, String>();
						listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();
					}
					
					if (bDataStarts) {
						//System.out.println(strLine);
						String[] strArrayOfTokens = strLine.split(",");
						
						HashMap<String, String> hmOfData = new HashMap<String, String>();
						String strDNAName = strArrayOfTokens[0]; 
						hmOfData.put(UploadField.DNA.toString(), strDNAName);
						
						for(int iColIndex = 1; iColIndex < strArrayOfTokens.length; iColIndex++){
							String strValue = strArrayOfTokens[iColIndex];
							String strDNA = hmOfColIndexAndGermplasmName.get(iColIndex);
							hmOfData.put(strDNA, strValue);
						}
						
						listOfDataInDataSheet.add(hmOfData);
					}
				}
			}
			
			listOfDataInSourceSheet.add(hmOfDataInSourceSheet);
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}	
		
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI() throws GDMSException {
		
		/*Sheet kbioScienceSheet = workbook.getSheet(0);
		listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();
		
		HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();

		String strKBioScienceGridReport = kbioScienceSheet.getCell(1, 0).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.KBioSciencesGridReport.toString(), strKBioScienceGridReport);

		String strGridVersion = kbioScienceSheet.getCell(1, 1).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.GridVersion.toString(), strGridVersion);

		String strProjectNumber = kbioScienceSheet.getCell(1, 3).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ProjectNumber.toString(), strProjectNumber);

		String strOrderNumber = kbioScienceSheet.getCell(1, 4).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.OrderNumber.toString(), strOrderNumber);

		String strPlates = kbioScienceSheet.getCell(1, 5).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Plates.toString(), strPlates);
		
		listOfDataInSourceSheet.add(hmOfDataInSourceSheet);
		
		int iNumOfColumnsInDataSheet = kbioScienceSheet.getColumns();
		int iNumOfRowsInDataSheet = kbioScienceSheet.getRows();
		HashMap<Integer, String> hmOfColIndexAndGermplasmName = new HashMap<Integer, String>();
		int colIndex = 1;
		for(colIndex = 1; colIndex < iNumOfColumnsInDataSheet; colIndex++){
			String strMarkerName = kbioScienceSheet.getCell(colIndex, 7).getContents().toString();
			hmOfColIndexAndGermplasmName.put(colIndex, strMarkerName);
		}
		
		int rowIndex = 8;
		HashMap<Integer, String> hmOfRowIndexAndDNAName = new HashMap<Integer, String>();
		for(rowIndex = 8; rowIndex < iNumOfRowsInDataSheet; rowIndex++){
			String strDNAName = kbioScienceSheet.getCell(0, rowIndex).getContents().toString();
			hmOfRowIndexAndDNAName.put(rowIndex, strDNAName);
		}

		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();

		for (int rIndex = 8; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strDNAName = kbioScienceSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.DNA.toString(), strDNAName);
			
			//Inserting the Marker-Names and Marker-Values
			for (int cIndex = 1; cIndex < iNumOfColumnsInDataSheet; cIndex++){
				String strMName = hmOfColIndexAndGermplasmName.get(cIndex);
				String strMValue = kbioScienceSheet.getCell(cIndex, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(strMName, strMValue);
			}

			listOfDataInDataSheet.add(hmOfDataInDataSheet);
		}*/
		
	}

	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(
			ArrayList<HashMap<String, String>> theListOfSourceDataRows,
			ArrayList<HashMap<String, String>> listOfDataRows,
			ArrayList<HashMap<String, String>> listOfGIDRows) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void upload() throws GDMSException {
		validateData();
		createObjectsToBeSavedToDB();
	}

	@Override
	public void validateData() throws GDMSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		
		String strDatasetSelected = GDMSModel.getGDMSModel().getDatasetSelected();
		String strGermplasmSelected = GDMSModel.getGDMSModel().getGermplasmSelected();
		
		// TODO Auto-generated method stub
	}

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDataUploaded() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		return listOfDataInDataSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
	}

}
