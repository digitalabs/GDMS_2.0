package org.icrisat.gdms.upload;

import java.util.ArrayList;
import java.util.List;
import org.icrisat.gdms.common.GDMSException;
import jxl.Sheet;
import jxl.Workbook;


public class ExcelSheetValidations {

	String strValid = "valid";

	public String validation(Workbook workbook, String type) throws GDMSException{

		ExcelSheetColumnName escn =  new ExcelSheetColumnName();
		String[] strSheetNames = workbook.getSheetNames();

		//Sheet Names display
		List<String> lSN = new ArrayList<String>();
		List<String> lstSheetNames = new ArrayList<String>();

		if(type.equalsIgnoreCase("SSRG")){

			lstSheetNames.add("ssr_source");
			lstSheetNames.add("ssr_data list");

			for (int i = 0; i < strSheetNames.length; i++){
				String strSN = strSheetNames[i];
				if(lstSheetNames.contains(strSN.toLowerCase())){
					if(!lSN.contains(strSN))
						lSN.add(strSN);
				}	
			}

			if(lstSheetNames.size()!=lSN.size()){
				throw new GDMSException("Sheet Name Not Found");
			}

			//check the template fields
			for(int i = 0; i < strSheetNames.length; i++){

				String strSheetName = strSheetNames[i].toString();

				if(strSheetName.equalsIgnoreCase("SSR_Source")){

					Sheet sName = workbook.getSheet(strSheetName);

					String strTempColumnNames[] = {"Institute", " Principle investigator", "Dataset Name", "Dataset description", "Genus", "Species", "Missing Data", "Remark"};

					for(int j = 0; j < strTempColumnNames.length; j++){
						String strColFromSheet = (String)sName.getCell(0, j).getContents().trim();

						if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
							throw new GDMSException("Column Name Not Found");
						}

						if(strColFromSheet == null || strColFromSheet == ""){
							throw new GDMSException("Delete Empty Rows");
						}
					}															
				}



				//SSR_DataList fields validation
				if(strSheetName.equalsIgnoreCase("SSR_Data List")){

					Sheet sName = workbook.getSheet(strSheetName);

					String strTempColumnNames[] = {"GID", "Accession", "Marker", "Gel/Run", "Dye", "Called Allele", "Raw Data", "Quality",
							"Height", "Volume", "Amount"};

					for(int j = 0; j < strTempColumnNames.length; j++){

						String strColFromSheet = (String)sName.getCell(j, 0).getContents().trim();
						if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
							throw new GDMSException("Column Name Not Found");
						}

						if(strColFromSheet == null || strColFromSheet == ""){
							throw new GDMSException("Delete Empty Columns");
						}
					}
				}
			}

			//check the required fields in SSR_Source;
			for(int i = 0; i < strSheetNames.length; i++){

				if(strSheetNames[i].equalsIgnoreCase("SSR_Source")){

					Sheet sName = workbook.getSheet(i);
					int intNoOfRows = sName.getRows();

					for(int j = 0; j < intNoOfRows; j++){

						String strFieldsName = sName.getCell(0, j).getContents().trim();

						if(strFieldsName.equalsIgnoreCase("institute") || strFieldsName.equalsIgnoreCase("Dataset Name") || strFieldsName.equalsIgnoreCase("Dataset description") || strFieldsName.equalsIgnoreCase("genus") || strFieldsName.equalsIgnoreCase("missing data")){
							String strFieldValue = sName.getCell(1, j).getContents().trim();
							if(strFieldValue == null || strFieldValue == ""){
								throw new GDMSException("Please provide values for Required Fields");
							}
						}
					}
				}

				//Accession, Marker and Amount fields from ssr_data list.
				if(strSheetNames[i].equalsIgnoreCase("SSR_Data List")){

					Sheet sName = workbook.getSheet(i);
					int intNoOfRows = sName.getRows();
					int intNoOfCols = sName.getColumns();
					String strFieldsName = "";
					String strFieldsName1 = "";
					int iAcc = 0, iMar = 0, iGr = 0, iDye = 0, iAlle = 0, iSize = 0, iQua = 0, 
					iHei = 0, iVol = 0, iAmo = 0, iGID = 0;

					for(int c = 0; c < intNoOfCols; c++){

						String strFieldsN = sName.getCell(c, 0).getContents().trim();

						if(strFieldsN.equalsIgnoreCase("GID"))
							iGID = c;
						if(strFieldsN.equalsIgnoreCase("Accession"))
							iAcc = c;
						if(strFieldsN.equalsIgnoreCase("Marker"))
							iMar = c;
						if(strFieldsN.equalsIgnoreCase("Gel/Run"))
							iGr = c;
						if(strFieldsN.equalsIgnoreCase("Dye"))
							iDye = c;
						if(strFieldsN.equalsIgnoreCase("Called Allele"))
							iAlle = c;
						if(strFieldsN.equalsIgnoreCase("Raw Data"))
							iSize = c;
						if(strFieldsN.equalsIgnoreCase("Quality"))
							iQua = c;
						if(strFieldsN.equalsIgnoreCase("Height"))
							iHei = c;
						if(strFieldsN.equalsIgnoreCase("Volume"))
							iVol = c;
						if(strFieldsN.equalsIgnoreCase("Amount"))
							iAmo = c;

						strFieldsName1 = sName.getCell(1, 0).getContents().trim();
						strFieldsName = sName.getCell(0, 0).getContents().trim();
					}

					///Accession sets validation
					//getting the germplasm names and count of germplasm from Data List of Template
					int intColcount = 0;
					for(int t = 0; t < intNoOfRows; t++){
						String strAccessionFieldValue = sName.getCell(0, t).getContents().trim();
						String strMarkerFieldValue = sName.getCell(1, t).getContents().trim();
						String strAmountFieldValue = sName.getCell(iAmo, t).getContents().trim();

						//skip the row which contains all the null values
						if(!strAccessionFieldValue.equals("") && !strMarkerFieldValue.equals("") && !strAmountFieldValue.equals("")){
							intColcount++;
						}
					}

					for(int j = 0; j < intNoOfRows; j++){
						String strAFieldValue = sName.getCell(0, j).getContents().trim();
						String strMFieldValue = sName.getCell(1, j).getContents().trim();
						String strAmount = sName.getCell(iAmo, 0).getContents().trim();

						//skip the row which contains all the null values
						if(j < intColcount){
							//GIDs
							if(strFieldsName.equalsIgnoreCase("GID")){
								if(strAFieldValue.equals("") && !strMFieldValue.equals("")){
									String strColName = escn.getColumnName(sName.getCell(0, j).getColumn());
									throw new GDMSException(strColName + " is requried field.");
								}
							}

							//Accessions
							if(strFieldsName.equalsIgnoreCase("Accession")){
								if(strAFieldValue.equals("") && !strMFieldValue.equals("")){
									String strColName = escn.getColumnName(sName.getCell(1, j).getColumn());
									throw new GDMSException(strColName + " is requried field.");
								}
							}

							//Markers
							if(strFieldsName1.equalsIgnoreCase("Marker")){
								if(!strAFieldValue.equals("") && strMFieldValue.equals("")){
									String strColName = escn.getColumnName(sName.getCell(2, j).getColumn());
									throw new GDMSException(strColName + " is requried field.");
								}
							}

							//Both the Accession and Marker is either null or not
							if(strAFieldValue.equals("") && strMFieldValue.equals("")){
								String strRowNumber = String.valueOf(sName.getCell(1, j).getRow()+1);
								String strErrMsg = "Accession and Marker values should not be null in SSR_Data List sheet.\n            Please delete it if not required.\n            The row position is "+strRowNumber;
								throw new GDMSException(strErrMsg);
							}

							//Amount field should contain the value when the fields (Allele,Size and Volume) are not null;
							if(strAmount.equalsIgnoreCase("Amount")){
								if(j != 0){
									boolean bIntASize = Boolean.getBoolean(sName.getCell(10,j).getContents().trim());

									if(bIntASize){
										String stra = (String)sName.getCell(10, j).getContents().trim();
										Double dbA = Double.valueOf(stra);

										if(dbA>=0.0 && dbA<=1.00){


										}else{
											String strRowNumber = String.valueOf(sName.getCell(10, j).getRow()+1);
											String strErrMsg = "The value under Amount in the SSR_Data List sheet should be between 0 and 1.\n The row position is " + strRowNumber;
											throw new GDMSException(strErrMsg);
										}

									}else{
										String strRowNumber = String.valueOf(sName.getCell(10, j).getRow()+1);
										String strErrMsg = "The value under Amount in the SSR_Data List sheet should be between 0 and 1.\n The row position is " + strRowNumber;
										throw new GDMSException(strErrMsg);
									}
								}
							}
						}else{
						}
					}

					//get the Markers from the Data List sheet
					List<String> lstMarNames = new ArrayList<String>();

					for(int a = 1; a < intNoOfRows; a++){
						String strMarker = (String)sName.getCell(2, a).getContents().trim();
						String strAccession = (String)sName.getCell(0, a).getContents().trim();
						String strAmount = (String)sName.getCell(10, a).getContents().trim();

						if(!lstMarNames.contains(strMarker) && !strMarker.equals("") && !strAccession.equals("") && !strAmount.equals("")){
							lstMarNames.add(strMarker);
						}
					}
					int firstAccCount = 0;
					int rowcount = 1;

					List<String> lstFirstAccSet = new ArrayList<String>();

					for(int m = 0; m < lstMarNames.size(); m++){

						List<String> listGNames = new ArrayList<String>();
						String strMarCheck = (String) lstMarNames.get(m);

						for(int ac = rowcount; ac < intNoOfRows; ac++){

							String strAmount = (String)sName.getCell(10,ac).getContents().trim();

							//skip the row which contains all the null values
							if(ac < intColcount){
								float fltAmount = Float.parseFloat(strAmount);

								if(strMarCheck == (String)sName.getCell(2,ac).getContents().trim()){
									if((fltAmount == 0.0) || (fltAmount == 1.0)){
										listGNames.add(sName.getCell(0,ac).getContents().trim());
									}else{/*
										 listGNames.add(sName.getCell(0,ac).getContents().trim());
										 int fltA=0;
										 for(int r=1;r<25;r++){
											 double f = fltAmount*r;
											 MaxIdValue rt = new MaxIdValue();
											 double fltRB=rt.roundThree(f);
											 if((fltRB>=0.900 && fltRB<=0.999))
												 fltRB=Math.round(f);
											 if(fltRB==1.000){
												 fltA=r;
												 r=25;
											 }
										 }
										 if(fltA!=0){
											 ac=ac+fltA-1;
											 rowcount=rowcount+fltA-1;
										 }
									 */}
								}else{
									break;
								}
								rowcount++;
							}
						}

					}				 
					//end of the Accession set validation
				} // end Data List validation
			}	

		} else if(type.equalsIgnoreCase("QTL")){

			lstSheetNames.add("qtl_source");
			lstSheetNames.add("qtl_data");

			for (int i = 0; i < strSheetNames.length; i++){
				String strSN = strSheetNames[i];
				if(lstSheetNames.contains(strSN.toLowerCase())){
					if(!lSN.contains(strSN))
						lSN.add(strSN);
				}	
			}

			if(lstSheetNames.size() != lSN.size()){
				throw new GDMSException("Sheet Name not found");
			}

			//check the template fields
			for(int i = 0; i < strSheetNames.length; i++){

				String strSName = strSheetNames[i].toString();

				if(strSName.equalsIgnoreCase("QTL_Source")){

					Sheet qtlSourceSheet = workbook.getSheet(strSName);
					String strArrayOfReqColNames[] = {"Institute", "Principle investigator", "Dataset Name", "Dataset description", "Genus", "Species", "Remark"};

					for(int j = 0; j < strArrayOfReqColNames.length; j++){

						String strColNameFromTemplate = (String)qtlSourceSheet.getCell(0, j).getContents().trim();

						if(!strArrayOfReqColNames[j].toLowerCase().contains(strColNameFromTemplate.toLowerCase())){
							throw new GDMSException("Column " + strArrayOfReqColNames[j].toLowerCase() + " not found");
						}

						if(strColNameFromTemplate == null || strColNameFromTemplate == ""){
							String strColName = escn.getColumnName(qtlSourceSheet.getCell(0, j).getColumn());
							throw new GDMSException("Delete empty column " + strColName);
						}
					}	

					//After checking for the required columns, have  to verify if the values have been provided for
					//all the required columns.
					//That is value at cell positon(1, n) should not be null or empty

					//Checking for value at Row#:0 Institute
					String strInstitue = qtlSourceSheet.getCell(1, 0).getContents().trim().toString();
					if (null == strInstitue){
						throw new GDMSException("Please provide the value for Institute at position (1, 0) in QTL_Source sheet of the template.");
					} else if (strInstitue.equals("")){
						throw new GDMSException("Please provide the value for Institute at position (1, 0) in QTL_Source sheet of the template.");
					}

					//Checking for value at Row#:2 Dataset Name
					String strDatasetName = qtlSourceSheet.getCell(1, 2).getContents().trim().toString();
					if (null == strDatasetName){
						throw new GDMSException("Please provide the value for Dataset Name at position (1, 2) in QTL_Source sheet of the template.");
					} else if (strDatasetName.equals("")){
						throw new GDMSException("Please provide the value for Dataset Name at position (1, 2) in QTL_Source sheet of the template.");
					}

					//Checking for value at Row#:3 Dataset Description
					String strDatasetDescription = qtlSourceSheet.getCell(1, 3).getContents().trim().toString();
					if (null == strDatasetDescription){
						throw new GDMSException("Please provide the value for Dataset Description at position (1, 3) in QTL_Source sheet of the template.");
					} else if (strDatasetDescription.equals("")){
						throw new GDMSException("Please provide the value for Dataset Description at position (1, 3) in QTL_Source sheet of the template.");
					}

					//Checking for value at Row#:4 Genus
					String strGenus = qtlSourceSheet.getCell(1, 4).getContents().trim().toString();
					if (null == strGenus){
						throw new GDMSException("Please provide the value for Genus at position (1, 4) in QTL_Source sheet of the template.");
					} else if (strGenus.equals("")){
						throw new GDMSException("Please provide the value for Genus at position (1, 4) in QTL_Source sheet of the template.");
					}

				}

				//SSR_DataList fields validation
				if(strSName.equalsIgnoreCase("QTL_Data")){

					Sheet sheetQTLData = workbook.getSheet(strSName);

					String strArrayOfRequiredColumnNames[] = {"Name", "Chromosome", "Map-Name", "Position", "Pos-Min",
							"Pos-Max", "Trait", "Experiment", "CLEN", "LFM",
							"RFM", "Effect", "LOD", "R2", "Interactions"};

					for(int j = 0; j < strArrayOfRequiredColumnNames.length; j++){

						String strColNamesFromDataSheet = (String)sheetQTLData.getCell(j, 0).getContents().trim();

						if(!strArrayOfRequiredColumnNames[j].toLowerCase().contains(strColNamesFromDataSheet.toLowerCase())){
							throw new GDMSException("column " + strColNamesFromDataSheet + " not found.");
						}

						if(strColNamesFromDataSheet == null || strColNamesFromDataSheet == ""){
							String strColName = escn.getColumnName(sheetQTLData.getCell(j, 0).getColumn());
							throw new GDMSException("Delete column " + strColName);
						}
					}


					int iNumOfRows = sheetQTLData.getRows();

					for (int r = 1; r < iNumOfRows; r++){

						//0 --- Name	
						String strName = sheetQTLData.getCell(0, r).getContents().trim().toString();
						if (strName.equals("")){
							String strErrMsg = "Please provide value in Name column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//1 --- Chromosome	
						String strChromosome = sheetQTLData.getCell(1, r).getContents().trim().toString();
						if (strChromosome.equals("")){
							String strErrMsg = "Please provide value in Chromosome column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//2 --- Map-Name	
						String strMapName = sheetQTLData.getCell(2, r).getContents().trim().toString();
						if (strMapName.equals("")){
							String strErrMsg = "Please provide value in Map-Name column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//3 --- Position	
						String strPosition = sheetQTLData.getCell(3, r).getContents().trim().toString();
						if (strPosition.equals("")){
							String strErrMsg = "Please provide value in Position column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//4 --- Pos-Min	
						String strMinPos = sheetQTLData.getCell(4, r).getContents().trim().toString();
						if (strMinPos.equals("")){
							String strErrMsg = "Please provide value in Pos-Min column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//5 --- Pos-Max	
						String strMaxPos = sheetQTLData.getCell(5, r).getContents().trim().toString();
						if (strMaxPos.equals("")){
							String strErrMsg = "Please provide value in Pos-Max column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//6 --- Trait	
						String strTrait = sheetQTLData.getCell(6, r).getContents().trim().toString();
						if (strTrait.equals("")){
							String strErrMsg = "Please provide value in Trait column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//7 --- Experiment	
						String strExperiment = sheetQTLData.getCell(7, r).getContents().trim().toString();
						if (strExperiment.equals("")){
							String strErrMsg = "Please provide value in Experiment column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//9 --- LFM	
						String strLFM = sheetQTLData.getCell(9, r).getContents().trim().toString();
						if (strLFM.equals("")){
							String strErrMsg = "Please provide value in LFM column at row:" + r;
							throw new GDMSException(strErrMsg);
						}


						//10 --- RFM	
						String strRFM = sheetQTLData.getCell(10, r).getContents().trim().toString();
						if (strRFM.equals("")){
							String strErrMsg = "Please provide value in RFM column at row:" + r;
							throw new GDMSException(strErrMsg);
						}


						//11 --- Effect	
						String strEffect = sheetQTLData.getCell(11, r).getContents().trim().toString();
						if (strEffect.equals("")){
							String strErrMsg = "Please provide value in Effect column at row:" + r;
							throw new GDMSException(strErrMsg);
						}

						//12 --- LOD	
						String strLOD = sheetQTLData.getCell(12, r).getContents().trim().toString();
						if (strLOD.equals("")){
							String strErrMsg = "Please provide value in LOD column at row:" + r;
							throw new GDMSException(strErrMsg);
						}


						//13 --- R2
						String strR2 = sheetQTLData.getCell(13, r).getContents().trim().toString();
						if (strR2.equals("")){
							String strErrMsg = "Please provide value in R2 column at row:" + r;
							throw new GDMSException(strErrMsg);
						}
					}
				}
			}

		} else if(type.equalsIgnoreCase("Map")){

			lstSheetNames.add("map");

			for (int i = 0; i < strSheetNames.length; i++){
				String strSN = strSheetNames[i];

				if(lstSheetNames.contains(strSN.toLowerCase())){
					if(!lSN.contains(strSN))
						lSN.add(strSN);
				}	
			}

			if(lstSheetNames.size()!=lSN.size()){
				throw new GDMSException("Sheet Name not found.");
			}

			//check the template fields
			for(int i = 0 ; i < strSheetNames.length; i++){

				String strSName = strSheetNames[i].toString();

				if(strSName.equalsIgnoreCase("Map")){

					Sheet sName = workbook.getSheet(strSName);
					String strArrayOfReqColNames[] = {"Map Name", "Map Description", "Crop", "Map Unit"};


					for(int j = 0; j < strArrayOfReqColNames.length; j++){

						String strColFromSheet = (String)sName.getCell(0, j).getContents().trim();

						if(!strArrayOfReqColNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
							throw new GDMSException(strColFromSheet + " column not found");
						}
						if(strColFromSheet == null || strColFromSheet == ""){
							String strColName = escn.getColumnName(sName.getCell(0, j).getColumn());
							throw new GDMSException("Delete empty column " + strColName);
						}
					}	

					String strArrayOfReqCols2[] = {"Marker Name", "Linkage Group", "Position"};

					Sheet sNameMap = workbook.getSheet(i);
					int intNoOfRows = sNameMap.getRows();
					int intNoOfCols = sNameMap.getColumns();

					for(int j = 0; j < strArrayOfReqCols2.length; j++){

						String strColNameFromSheet = (String)sName.getCell(j, 5).getContents().trim();

						if(!strArrayOfReqCols2[j].toLowerCase().contains(strColNameFromSheet.toLowerCase())){
							throw new GDMSException(strColNameFromSheet + " column name not found.");
						}

						if(strColNameFromSheet==null || strColNameFromSheet==""){
							String strColName = escn.getColumnName(sName.getCell(j, 5).getColumn());
							throw new GDMSException(strColName + " information required.");
						}
					}

					String strFieldsName = "";
					String strFieldsName1 = "";
					int iMar = 0, iLG = 0, iPos = 0;

					for(int c = 0; c < intNoOfCols; c++){

						String strFieldsN = sName.getCell(c, 0).getContents().trim();

						if(strFieldsN.equalsIgnoreCase("Marker Name"))
							iMar = c;
						if(strFieldsN.equalsIgnoreCase("Linkage Group"))
							iLG = c;
						if(strFieldsN.equalsIgnoreCase("Position"))
							iPos = c;

						strFieldsName1 = sName.getCell(1, 0).getContents().trim();
						strFieldsName = sName.getCell(0, 0).getContents().trim();
					}


					for(int j = 7; j < intNoOfRows; j++){

						String strMFieldValue = sName.getCell(0, j).getContents().trim();
						String strLGFieldValue = sName.getCell(1, j).getContents().trim();
						String strPosition = sName.getCell(2, j).getContents().trim();

						if(strMFieldValue.equals("") && !strLGFieldValue.equals("")){
							String strColName = escn.getColumnName(sName.getCell(0, j).getColumn());
							throw new GDMSException(strColName + " is required field");
						}

						if(strMFieldValue.equals("") && !strLGFieldValue.equals("")){
							String strColName = escn.getColumnName(sName.getCell(0, j).getColumn());
							throw new GDMSException(strColName + " is required field");
						}

						if(!strMFieldValue.equals("") && strLGFieldValue.equals("")){
							String strColName = escn.getColumnName(sName.getCell(1, j).getColumn());
							throw new GDMSException(strColName + " is required field");
						}

						if(!strMFieldValue.equals("") && strPosition.equals("")){
							String strColName = escn.getColumnName(sName.getCell(2, j).getColumn());
							throw new GDMSException(strColName + " is required field");
						}

						if(strMFieldValue.equals("") && strLGFieldValue.equals("") && strPosition.equals("")){
							String strRowNumber = String.valueOf(sName.getCell(1, j).getRow()+1);								 
							String strErrMsg = "There is an empty row at position " + strRowNumber + ".\nPlease delete it.";
							throw new GDMSException(strErrMsg);
						}							 
					}
				}
			}
		} else if(type.equalsIgnoreCase("DArt")){

			lstSheetNames.add("dart_source");
			lstSheetNames.add("dart_data");
			lstSheetNames.add("dart_gids");

			for (int i = 0; i < strSheetNames.length; i++){
				String strSN = strSheetNames[i];
				if(lstSheetNames.contains(strSN.toLowerCase())){
					if(!lSN.contains(strSN))
						lSN.add(strSN);
				}	
			}

			if(lstSheetNames.size() != lSN.size()){
				throw new GDMSException("Sheet Name Not Found");
			}

			//check the template fields
			for(int i = 0; i < strSheetNames.length; i++){

				String strSName = strSheetNames[i].toString();

				if(strSName.equalsIgnoreCase("DArT_Source")) {

					Sheet sName = workbook.getSheet(strSName);

					String strTempColumnNames[] = {"Institute", "Principle investigator", "Dataset Name", "Dataset description", "Genus",
							"Species", "Remark"};

					for(int j = 0; j < strTempColumnNames.length; j++){
						String strColFromSheet = (String)sName.getCell(0, j).getContents().trim();

						if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
							throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
						}

						if(strColFromSheet == null || strColFromSheet == ""){
							throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
						}
					}															
				}

				if(strSName.equalsIgnoreCase("DArT_Data")){

					Sheet sName = workbook.getSheet(strSName);
					int intNoOfRows = sName.getRows();
					int intNoOfCols = sName.getColumns();
					String strTempColumnNames[] = {"CloneID", "MarkerName", "Q", "Reproducibility",
							"Call Rate", "PIC", "Discordance"};					 

					for(int j = 0; j < strTempColumnNames.length; j++){

						String strColFromSheet = (String)sName.getCell(j, 0).getContents().trim();

						if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
							throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
						}

						if(strColFromSheet == null || strColFromSheet == ""){
							String strColName = escn.getColumnName(sName.getCell(j, 0).getColumn());
							throw new GDMSException("Delete Empty Column " + strColName);
						}
					}

					for(int c = 0; c < 6; c++){

						for(int r = 1; r < intNoOfRows; r++){
							String value = (String)sName.getCell(c, r).getContents().trim();
							if(value == null || value == ""){
								String strRowNumber = String.valueOf(sName.getCell(c, r).getRow()+1);	
								String strColumnName = escn.getColumnName(sName.getCell(c, r).getColumn());	
								String strErrMsg = "This cell is empty at position "+strColumnName+strRowNumber+".";
								throw new GDMSException(strErrMsg);
							}
						}
					}

					for(int c = 7; c < intNoOfCols; c++){

						for(int r = 0; r < intNoOfRows; r++){

							String value = (String)sName.getCell(c, r).getContents().trim();

							if(value == null || value == ""){
								String strRowNumber = String.valueOf(sName.getCell(c, r).getRow()+1);	
								String strColumnName = escn.getColumnName(sName.getCell(c, r).getColumn());	
								String strErrMsg = "This cell is empty at position "+strColumnName+strRowNumber+".";
								throw new GDMSException(strErrMsg);
							}
						}
					}				 
				}
			}
		} else if(type.equalsIgnoreCase("Mapping")){

			lstSheetNames.add("mapping_source");
			lstSheetNames.add("mapping_datalist");

			for (int i = 0; i < strSheetNames.length; i++){
				String strSN = strSheetNames[i];
				if(lstSheetNames.contains(strSN.toLowerCase())){
					if(!lSN.contains(strSN))
						lSN.add(strSN);
				} else {
					throw new GDMSException(lstSheetNames.get(i).toString() + " Sheet Name Not Found");	
				}	
			}

			/*if(lstSheetNames.size() != lSN.size()){
				throw new GDMSException("Sheet Name Not Found");
			}*/

			//check the template fields
			for(int i = 0; i < strSheetNames.length; i++){

				String strSName = strSheetNames[i].toString();

				if(strSName.equalsIgnoreCase("Mapping_Source")) {

					Sheet sName = workbook.getSheet(strSName);					 

					String strArrayOfRequiredColumns[] = {"Institute", "Principle investigator", "Email contact", "Dataset Name", "Dataset description", 
							"Genus", "Species", "Population ID", "Parent A GID", "Parent A", 
							"Parent B GID", "Parent B", "Population Size", "Population Type",
							"Purpose of the study", "Scoring Scheme",
							"Missing Data", "Creation Date", "Remark"};

					for(int j = 0; j < strArrayOfRequiredColumns.length; j++){
						String strColFromSheet = (String)sName.getCell(0, j).getContents().trim();

						if(!strArrayOfRequiredColumns[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
							throw new GDMSException(strArrayOfRequiredColumns[j] + " Column Name Not Found");
						}

						if(strColFromSheet == null || strColFromSheet == ""){
							throw new GDMSException(strArrayOfRequiredColumns[j] + " Column Name Not Found");
						}
					}	

					//Checking if values have been provided for the following mandatory fields
					//0 --- Institute
					String strInstitute = (String)sName.getCell(1, 0).getContents().trim();
					if(strInstitute == null || strInstitute == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 0).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 0).getColumn());	
						String strErrMsg = "Please provide a value for Institute at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:1 --- Principle investigator
					String strPrincipleInvestigator = (String)sName.getCell(1, 1).getContents().trim();
					if(strPrincipleInvestigator == null || strPrincipleInvestigator == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 1).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 1).getColumn());	
						String strErrMsg = "Please provide a value for Principle investigator at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}


					//Row#:3 --- Dataset Name
					String strDatasetName = (String)sName.getCell(1, 3).getContents().trim();
					if(strDatasetName == null || strDatasetName == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 3).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 3).getColumn());	
						String strErrMsg = "Please provide a value for Dataset Name at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:4 --- Dataset description
					String strDatasetDescription = (String)sName.getCell(1, 4).getContents().trim();
					if(strDatasetDescription == null || strDatasetDescription == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 4).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 4).getColumn());	
						String strErrMsg = "Please provide a value for Dataset description at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:5 --- Genus
					String strGenus = (String)sName.getCell(1, 5).getContents().trim();
					if(strGenus == null || strGenus == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 5).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 5).getColumn());
						String strErrMsg = "Please provide a value for Genus at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:6 --- Species
					String strSpecies = (String)sName.getCell(1, 6).getContents().trim();
					if(strSpecies == null || strSpecies == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 6).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 6).getColumn());
						String strErrMsg = "Please provide a value for Species at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:7 --- Population ID
					String strPopulationID = (String)sName.getCell(1, 7).getContents().trim();
					if(strPopulationID == null || strPopulationID == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 7).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 7).getColumn());
						String strErrMsg = "Please provide a value for Population ID at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:8 --- Parent A GID
					String strParentAGID = (String)sName.getCell(1, 8).getContents().trim();
					if(strParentAGID == null || strParentAGID == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 8).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 8).getColumn());
						String strErrMsg = "Please provide a value for Parent A GID ID at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:9 --- Parent A
					String strParentA = (String)sName.getCell(1, 9).getContents().trim();
					if(strParentA == null || strParentA == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 9).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 9).getColumn());
						String strErrMsg = "Please provide a value for Parent A at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:10 --- Parent B GID
					String strParentBIGD = (String)sName.getCell(1, 10).getContents().trim();
					if(strParentBIGD == null || strParentBIGD == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 10).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 10).getColumn());
						String strErrMsg = "Please provide a value for Parent B GID at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:11 --- Parent B
					String strParentB = (String)sName.getCell(1, 11).getContents().trim();
					if(strParentB == null || strParentB == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 11).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 11).getColumn());
						String strErrMsg = "Please provide a value for Parent B at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:14 --- Purpose of the study
					String strPurposeOfTheStudy = (String)sName.getCell(1, 14).getContents().trim();
					if(strPurposeOfTheStudy == null || strPurposeOfTheStudy == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 14).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 14).getColumn());
						String strErrMsg = "Please provide a value for Purpose of the study at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:16 --- Missing Data
					String strMissingData = (String)sName.getCell(1, 16).getContents().trim();
					if(strMissingData == null || strMissingData == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 16).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 16).getColumn());
						String strErrMsg = "Please provide a value for Missing Data at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}

					//Row#:17 --- Creation Date
					String strCreationDate = (String)sName.getCell(1, 17).getContents().trim();
					if(strCreationDate == null || strCreationDate == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, 17).getRow()+1);	
						String strColumnName = escn.getColumnName(sName.getCell(1, 17).getColumn());
						String strErrMsg = "Please provide a value for Creation Date at (" + strColumnName + ", " + strRowNumber + ") in Mapping_Source sheet.";
						throw new GDMSException(strErrMsg);
					}
				}

				if(strSName.equalsIgnoreCase("Mapping_DataList")){

					Sheet sName = workbook.getSheet(strSName);
					int intNoOfRows = sName.getRows();
					int intNoOfCols = sName.getColumns();
					String strArrayOfReqColumnNames[] = {"Alias", "GID", "Line"};					 

					for(int j = 0; j < strArrayOfReqColumnNames.length; j++){

						String strColFromSheet = (String)sName.getCell(j, 0).getContents().trim();

						if(!strArrayOfReqColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
							throw new GDMSException(strArrayOfReqColumnNames[j] + " Column Name Not Found");
						}

						if(strColFromSheet == null || strColFromSheet == ""){
							String strColName = escn.getColumnName(sName.getCell(j, 0).getColumn());
							throw new GDMSException("Delete Empty Column " + strColName);
						}
					}


					for(int r = 1; r < intNoOfRows; r++){
						String strGIDalue = (String)sName.getCell(1, r).getContents().trim();
						if(strGIDalue == null || strGIDalue == ""){
							String strRowNumber = String.valueOf(sName.getCell(1, r).getRow()+1);	
							String strColumnName = escn.getColumnName(sName.getCell(1, r).getColumn());	
							String strErrMsg = "Please provide a value at cell position " + "(" + strColumnName + ", " + strRowNumber + ") in Mapping_DataList sheet.";
							throw new GDMSException(strErrMsg);
						}
						
						String strLine = (String)sName.getCell(2, r).getContents().trim();
						if(strLine == null || strLine == ""){
							String strRowNumber = String.valueOf(sName.getCell(2, r).getRow()+1);	
							String strColumnName = escn.getColumnName(sName.getCell(2, r).getColumn());	
							String strErrMsg = "Please provide a value at cell position " + "(" + strColumnName + ", " + strRowNumber + ") in Mapping_DataList sheet.";
							throw new GDMSException(strErrMsg);
						}
					}

					//Checking for values for Line-Markers
					for(int c = 3; c < intNoOfCols; c++){
						for(int r = 0; r < intNoOfRows; r++){

							String strCellValue = (String)sName.getCell(c, r).getContents().trim();

							if(strCellValue == null || strCellValue == ""){
								String strRowNumber = String.valueOf(sName.getCell(c, r).getRow()+1);	
								String strColumnName = escn.getColumnName(sName.getCell(c, r).getColumn());	
								String strErrMsg = "Please provide a value at cell position " + "(" + strColumnName + ", " + strRowNumber + ") in Mapping_DataList sheet.";
								throw new GDMSException(strErrMsg);
							}
						}
					}				 
				}
			}
		}
		return strValid;
	}

}
