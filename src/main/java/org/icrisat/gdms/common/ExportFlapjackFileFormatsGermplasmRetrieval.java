package org.icrisat.gdms.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.MapInfo;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.MappingPopValues;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.ui.Window.Notification;
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;


public class ExportFlapjackFileFormatsGermplasmRetrieval {
	
	private File generatedTextFile;
	private File generatedDatFile;
	private File generatedMapFile;
	ManagerFactory factory=null;
	public File getGeneratedTextFile() {
		return generatedTextFile;
	}

	public File getGeneratedDatFile() {
		return generatedDatFile;
	}

	public File getGeneratedMapFile() {
		return generatedMapFile;
	}


	public void generateFlapjackDataFilesByGIDs(
			GDMSMain theMainHomePage,						
			ArrayList listOfAllMapInfo,
			ArrayList<Integer> listOfGIDsToBeExported,
			ArrayList<String> listOfMarkerNames,
			TreeMap<Integer, String> hmOfMIDandMNames, ArrayList<QtlDetailElement> listOfAllQTLDetails,
			HashMap<Integer, String> hmOfQtlPosition, HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName, String strSelectedExportType, boolean bQTLExists, HashMap dataMap) {
		
			
		
		
		try {
			writeTextFile(theMainHomePage, listOfAllQTLDetails, hmOfQtlPosition,  hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType,  bQTLExists);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.txt file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeDatFile(theMainHomePage, dataMap, listOfGIDsToBeExported,
					listOfMarkerNames, hmOfMIDandMNames);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.dat file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeMapFile(theMainHomePage, listOfAllMapInfo);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.map file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
	}

	
	
	public void generateFlapjackDataFilesByGermplasmNames(
			GDMSMain theMainHomePage,			
			ArrayList listOfAllMappingData,
			ArrayList<String> listOfGNamesToBeExported,
			ArrayList<String> listOfMarkerNames,
			TreeMap<Integer, String> hmOfMIDandMNames,
			TreeMap<String, Integer> hmOfNvalAndGIds,
			ArrayList<QtlDetailElement> listOfAllQTLDetails,			
			HashMap<Integer, String> hmOfQtlPosition, 
			HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName,
			String strSelectedExportType, boolean bQTLExists, HashMap dataMap) {
		try {
			writeTextFile(theMainHomePage, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType,  bQTLExists);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.txt file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeDatFileGermplasmNames(theMainHomePage, dataMap, listOfGNamesToBeExported, hmOfNvalAndGIds,
					listOfMarkerNames, hmOfMIDandMNames);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.dat file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeMapFile(theMainHomePage, listOfAllMappingData);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.map file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
	}
	
	private void writeDatFileGermplasmNames(
			GDMSMain theMainHomePage,
			HashMap dataMap,
			ArrayList<String> accList,
			TreeMap<String, Integer> hmOfNvalAndGIds, ArrayList<String> markList,
			TreeMap<Integer, String> hmOfMIDandMNames) throws GDMSException {
		HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
		boolean condition=false;
		int noOfAccs=accList.size();
		int noOfMarkers=markList.size();			
		
		int accIndex=1,markerIndex=1;
		int i;String chVal="";
		//long time = new Date().getTime();
		//String strFlapjackDatFile = "Flapjack" + String.valueOf(time);
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePath + "\\" + strFlapjackDatFile + ".dat");
		//System.out.println("strFilePath=:"+strFilePath);
		//System.out.println("hmOfNvalAndGIds:"+hmOfNvalAndGIds);
		String finalData="";	
		/*File fexists=new File(filePath+("//")+"/Flapjack.txt");
		if(fexists.exists()) { fexists.delete(); }*/
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter fjackdat = new BufferedWriter(flapjackTextWriter);
			//fjackdat.write("\n");
			
			for(int m = 0; m< markList.size(); m++){
				fjackdat.write("\t"+markList.get(m));
			}
			
			int al=0;
			//System.out.println("dataMap:"+dataMap);
			for (int j=0;j<accList.size();j++){ 
				String arrList6[]=new String[3];
				fjackdat.write("\n"+accList.get(j));		
			    for (int k=0;k<markList.size();k++){
			    	markerAlleles=(HashMap)dataMap.get(Integer.parseInt(hmOfNvalAndGIds.get(accList.get(j).toString()).toString()));
			    	if(markerAlleles.containsKey(hmOfNvalAndGIds.get(accList.get(j).toString())+"!~!"+markList.get(k).toString())){
						//fjackdat.write("\t"+markerAlleles.get(gList.get(accList.get(j).toString()).toString()+"!~!"+markList.get(k).toString()));
						String alleleValue=markerAlleles.get(hmOfNvalAndGIds.get(accList.get(j).toString())+"!~!"+markList.get(k).toString()).toString();
						////System.out.println("k=:"+k +"   "+alleleValue);
						if(alleleValue.contains("/")){
							if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
								finalData="";
							}else{
								String[] strAllele=alleleValue.split("/");
								////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
								if(strAllele[0].equalsIgnoreCase(strAllele[1]))
									finalData=strAllele[0];
								else
									finalData=strAllele[0]+"/"+strAllele[1];
							}
						}else if(alleleValue.contains(":")){
							if((alleleValue.length()==3 && alleleValue.matches("0:0"))||(alleleValue.equals("?"))){									
								finalData="";
							}else{
								String[] strAllele=alleleValue.split(":");
								////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
								if(strAllele[0].equalsIgnoreCase(strAllele[1]))
									finalData=strAllele[0];
								else
									finalData=strAllele[0]+"/"+strAllele[1];
							}
						}else{
							if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){													
								finalData="";
							}else{
								finalData=alleleValue;
							}
						}
						fjackdat.write("\t"+finalData);
			    		
			    	}else{
			    		fjackdat.write("\t");	
			    	}
			      }
		    	
		     }
					
			fjackdat.close();			
		
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}
	private void writeDatFile(
			GDMSMain theMainHomePage,
			HashMap dataMap, 
			ArrayList<Integer> accList,
			ArrayList<String> markList,
			TreeMap<Integer, String> hmOfMIDandMNames) throws GDMSException {
		HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
		boolean condition=false;
		//long time = new Date().getTime();
		//String strFlapjackDatFile = "Flapjack" + String.valueOf(time);
		int accIndex=1,markerIndex=1;
		int i;String chVal="";
		//long time = new Date().getTime();
		//String strFlapjackDatFile = "Flapjack" + String.valueOf(time);
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePathGids = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePathGids + "\\" + strFlapjackDatFile + ".dat");
		int al=0;
		String finalData="";	
		
		/*//System.out.println("listOfAllAllelicValues=:"+a);
		//System.out.println("listOfMarkerNames=:"+listOfMarkerNames);
		//System.out.println("listOfGIDsToBeExported=:"+listOfGIDsToBeExported);*/
		/*//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		//System.out.println(strFilePathGids);*/
		try {
			FileWriter flapjackTextWriterGIDs = new FileWriter(generatedDatFile);
			BufferedWriter fjackdatGids = new BufferedWriter(flapjackTextWriterGIDs);
			
			for(int m1 = 0; m1< markList.size(); m1++){
				////System.out.println("m1=:"+m1);
				fjackdatGids.write("\t"+markList.get(m1));
			}
			
			
			for (int j=0;j<accList.size();j++){ 
				////System.out.println("jdfhgjkdfghkjdfhgkdfh          &&&&&&&&&&&&&&&&&&&&&&&&&&:");
				String arrList6[]=new String[3];
				fjackdatGids.write("\n"+accList.get(j));		
			    for (int k=0;k<markList.size();k++){
			    	markerAlleles=(HashMap)dataMap.get(Integer.parseInt(accList.get(j).toString()));
			    	if(markerAlleles.containsKey(accList.get(j).toString()+"!~!"+markList.get(k).toString())){
						//fjackdat.write("\t"+markerAlleles.get(gList.get(accList.get(j).toString()).toString()+"!~!"+markList.get(k).toString()));
						String alleleValue=markerAlleles.get(accList.get(j).toString()+"!~!"+markList.get(k).toString()).toString();
						////System.out.println("k=:"+k +"   "+alleleValue);
						if(alleleValue.contains("/")){
							if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){	
								finalData="";
							}else{
								String[] strAllele=alleleValue.split("/");
								////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
								if(strAllele[0].equalsIgnoreCase(strAllele[1]))
									finalData=strAllele[0];
								else
									finalData=strAllele[0]+"/"+strAllele[1];
							}
						}else if(alleleValue.contains(":")){
							if((alleleValue.length()==3 && alleleValue.matches("0:0"))||(alleleValue.equals("?"))){									
								finalData="";
							}else{
								String[] strAllele=alleleValue.split(":");
								////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
								if(strAllele[0].equalsIgnoreCase(strAllele[1]))
									finalData=strAllele[0];
								else
									finalData=strAllele[0]+"/"+strAllele[1];
							}
						}else{
							if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
								finalData="";
							}else{
								finalData=alleleValue;
							}
						}
						fjackdatGids.write("\t"+finalData);
			    		
			    	}else{
			    		fjackdatGids.write("\t");	
			    	}	
		    	}
		    }
			   
						
			fjackdatGids.close();			
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	private void writeTextFile(
			GDMSMain theMainHomePage,
			ArrayList<QtlDetailElement> listOfAllQTLDetails, HashMap<Integer, String> hmOfQtlPosition, HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName, String strSelectedExportType, boolean bQTLExists) throws GDMSException {
		
		//long time = new Date().getTime();
		//String strFlapjackTextFile = "Flapjack" + String.valueOf(time);
		
		//System.out.println("######################   listOfAllQTLDetails:"+listOfAllQTLDetails);
		//System.out.println("######################   hmOfQtlIdandName:"+hmOfQtlIdandName);
		//System.out.println("######################   strSelectedExportType=:"+strSelectedExportType);
		//System.out.println("######################   bQTLExists=:"+bQTLExists);
		
		
		String strFlapjackTextFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		//System.out.println("strFilePath=:"+strFilePath);
		generatedTextFile = new File(strFilePath + "\\" + strFlapjackTextFile + ".txt");

		/**	writing tab delimited qtl file for FlapJack  
		 * 	consisting of marker chromosome & position
		 * 
		 * **/
		try {
			
			FileWriter flapjackTextWriter = new FileWriter(generatedTextFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			
			//fjackQTL.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tFlanking markers in original publication");
			flapjackBufferedWriter.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tFlanking markers in original publication\teffect");
			flapjackBufferedWriter.write("\n");
			for (int i = 0 ; i < listOfAllQTLDetails.size(); i++){
				//System.out.println(listOfAllQTLDetails.get(i));
				QtlDetailElement qtlDetails = listOfAllQTLDetails.get(i);
				
				/*QtlDetailsPK id = qtlDetails.getQtlName().get.getId();
				Integer qtlId = id.getQtlId();*/
				//String strQtlName = hmOfQtlIdandName.get(qtlId);
				String strQtlName =qtlDetails.getQtlName();
				int qtlId=hmOfQtlNameId.get(strQtlName);
				//qtlDetails.get
				//Float clen = qtlDetails.getClen();
				//Float fEffect = qtlDetails.getEffect();
				int fEffect = qtlDetails.getEffect();
				Float fMaxPosition = qtlDetails.getMaxPosition();
				Float fMinPosition = qtlDetails.getMinPosition();
				//Float fPosition = qtlDetails.getPosition();
				String fPosition = hmOfQtlPosition.get(qtlId);
				Float frSquare = qtlDetails.getRSquare();
				Float fScoreValue = qtlDetails.getScoreValue();
				String strExperiment = qtlDetails.getExperiment();
				//String strHvAllele = qtlDetails..getHvAllele();
				//String strHvParent = qtlDetails.getHvParent();
				//String strInteractions = qtlDetails.getInteractions();
				String strLeftFlankingMarker = qtlDetails.getLeftFlankingMarker();
				String strLinkageGroup = qtlDetails.getChromosome();
				//String strLvAllele = qtlDetails.getLvAllele();
				//String strLvParent = qtlDetails.getLvParent();
				String strRightFM = qtlDetails.getRightFlankingMarker();
				//String strSeAdditive = qtlDetails.getSeAdditive();
				
				//String strTrait = qtlDetails.getTrait();
				String strTrait = qtlDetails.getTRName();
							
				
				flapjackBufferedWriter.write(strQtlName + "\t" + strLinkageGroup + "\t" + fPosition + "\t" + fMinPosition + "\t" + fMaxPosition + "\t" +
						strTrait + "\t" + strExperiment + "\t \t" + fScoreValue + "\t" + frSquare+
	                     "\t" + strLeftFlankingMarker+"/"+strRightFM + "\t" + fEffect);
				
				flapjackBufferedWriter.write("\n");
				
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
	}
	
	

	private void writeMapFile(
			GDMSMain theMainHomePage,
			ArrayList listOfAllMapInfo) throws GDMSException {
	
		//long time = new Date().getTime();
		//String strFlapjackMapFile = "Flapjack" + String.valueOf(time);
		String strFlapjackMapFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedMapFile = new File(strFilePath + "\\" + strFlapjackMapFile + ".map");
		
		/**	writing tab delimited .map file for FlapJack  
		 * 	consisting of marker chromosome & position
		 * 
		 * **/
		//System.out.println("............:"+listOfAllMapInfo);
		try {
			FileWriter flapjackMapFileWriter = new FileWriter(generatedMapFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackMapFileWriter);
			//flapjackBufferedWriter.write("\n");
			//flapjackBufferedWriter.write("Marker-Name\tLinkage-Group\tStarting-Position\n");
			
			for (int m=0; m<listOfAllMapInfo.size();m++){
				String[] MapInfo=listOfAllMapInfo.get(m).toString().split("!~!");			
				
				flapjackBufferedWriter.write(MapInfo[0]);
				flapjackBufferedWriter.write("\t");
				flapjackBufferedWriter.write(MapInfo[1]);
				flapjackBufferedWriter.write("\t");
				flapjackBufferedWriter.write(MapInfo[2]);
				flapjackBufferedWriter.write("\n");
				
			}
			
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}
	
	
	/*public void generateFlapjackDataFilesByGIDs(GDMSMain theMainHomePage,
			HashMap listOfAllCharValuesForGermplasmRetrieval,
			 ArrayList<AllelicValueElement>   listOfAllelicValueElementsForGermplasmRetrievals,
			 ArrayList<MappingPopValues> listOfAllMappingPopValuesForGermplasmRetrieval,
			ArrayList listOfAllMapInfo,
			ArrayList<Integer> listOfGIDsToBeExported,
			ArrayList<String> listOfMarkerNames,
			HashMap<Integer, String> hmOfMIDandMNames,
			ArrayList<QtlDetailElement> listOfAllQTLDetails,HashMap<Integer, String> hmOfQtlPosition, HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName,
			String strSelectedExportType, boolean bQTLExists, String datasetType) {
		//System.out.println("listOfAllQTLDetails=:"+listOfAllQTLDetails);
		//System.out.println("hmOfQtlIdandName=:"+hmOfQtlIdandName);
		//System.out.println("strSelectedExportType=:"+strSelectedExportType);
		//System.out.println("bQTLExists:"+bQTLExists);
		try {
			writeTextFile(theMainHomePage, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId,hmOfQtlIdandName, strSelectedExportType,  bQTLExists);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.txt file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			
			if (null != listOfAllCharValuesForGermplasmRetrieval){
				writeDatFileBasedOnGIDs1(theMainHomePage, listOfAllCharValuesForGermplasmRetrieval, listOfGIDsToBeExported,
						listOfMarkerNames, hmOfMIDandMNames);
			} else if (null != listOfAllelicValueElementsForGermplasmRetrievals){
				writeDatFileBasedOnGIDs2(theMainHomePage, listOfAllelicValueElementsForGermplasmRetrievals, listOfGIDsToBeExported,
						listOfMarkerNames, hmOfMIDandMNames);
			} else if (null != listOfAllMappingPopValuesForGermplasmRetrieval){
				writeDatFileBasedOnGIDs3(theMainHomePage, listOfAllMappingPopValuesForGermplasmRetrieval, listOfGIDsToBeExported,
						listOfMarkerNames, hmOfMIDandMNames);
			}
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.dat file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeMapFile(theMainHomePage, listOfAllMapInfo);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.map file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
	}

	
	private void writeDatFileBasedOnGIDs3(
			GDMSMain theMainHomePage,
			ArrayList<MappingPopValues> listOfAllMappingPopValuesForGermplasmRetrieval,
			ArrayList<Integer> listOfGIDsToBeExported,
			ArrayList<String> listOfMarkerNames,
			HashMap<Integer, String> hmOfMIDandMNames) throws GDMSException {
		
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePath + "\\" + strFlapjackDatFile + ".dat");
		
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			flapjackBufferedWriter.write("\n");
			
			int iNumOfCols = listOfMarkerNames.size() + 2;
			int iNumOfRows = listOfGIDsToBeExported.size() + 1;

			String[][] strArrayOfData = new String[iNumOfRows][iNumOfCols];

			//Writing all GIDs in Column-0, from Row-1 onwards
			HashMap<Integer, Integer> hmOfGIDAndIndex = new HashMap<Integer, Integer>(); 
			for (int i = 0; i < listOfGIDsToBeExported.size(); i++){
				Integer iGID = listOfGIDsToBeExported.get(i);
				strArrayOfData[i+1][0] = String.valueOf(iGID);
				hmOfGIDAndIndex.put(iGID, i+1);
			}

			//Writing all Markers in Row-0 from Column-1 onwards
			HashMap<String, Integer> hmOfMarkerNameAndIndex = new HashMap<String, Integer>();
			for (int j = 0; j < listOfMarkerNames.size(); j++){
				String markerName = listOfMarkerNames.get(j);
				strArrayOfData[0][j+1] = markerName;
				hmOfMarkerNameAndIndex.put(markerName, j+1);
			}
			
			
			//Writing the Allelic Data
			for (int i = 0; i < listOfAllMappingPopValuesForGermplasmRetrieval.size(); i++){
				MappingPopValues mappingPopValues = listOfAllMappingPopValuesForGermplasmRetrieval.get(i);
				
				Integer gid = mappingPopValues.getGid();
				Integer markerId = mappingPopValues.getMarkerId();
				String strMarkerName  = hmOfMIDandMNames.get(markerId);
				String data = mappingPopValues.getMapCharValue();
				
				Integer iRowIndex = 0;
				Integer iColIndex = 0;
				if (hmOfGIDAndIndex.containsKey(gid)){
					iRowIndex = hmOfGIDAndIndex.get(gid);
					if (hmOfMarkerNameAndIndex.containsKey(strMarkerName)){
						iColIndex = hmOfMarkerNameAndIndex.get(strMarkerName);
					}
					//System.out.println(gid + " --- " + strMarkerName + " --- " + iRowIndex + " --- " + iColIndex + " --- " + data);
					if (null == data){
						data = "0:0";
					}
					strArrayOfData[iRowIndex][iColIndex] = data;
				}
			}
			
			for (int i = 0; i < iNumOfRows; i++){
				for (int j = 0; j < iNumOfCols; j++){
					String strData = strArrayOfData[i][j];
					if (null == strData){
						strData = "";
					}
					flapjackBufferedWriter.write(strData + "\t");
				}
				flapjackBufferedWriter.write("\n");
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	private void writeDatFileBasedOnGIDs1(GDMSMain theMainHomePage,
			HashMap listOfAllCharValuesForGermplasmRetrieval,
			ArrayList<Integer> listOfGIDsToBeExported,
			ArrayList<String> listOfMarkerNames,
			HashMap<Integer, String> hmOfMIDandMNames) throws GDMSException {
		
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePath + "\\" + strFlapjackDatFile + ".dat");
		
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			flapjackBufferedWriter.write("\n");
			
			int iNumOfCols = listOfMarkerNames.size() + 2;
			int iNumOfRows = listOfGIDsToBeExported.size() + 1;

			String[][] strArrayOfData = new String[iNumOfRows][iNumOfCols];

			//Writing all GIDs in Column-0, from Row-1 onwards
			HashMap<Integer, Integer> hmOfGIDAndIndex = new HashMap<Integer, Integer>(); 
			for (int i = 0; i < listOfGIDsToBeExported.size(); i++){
				Integer iGID = listOfGIDsToBeExported.get(i);
				strArrayOfData[i+1][0] = String.valueOf(iGID);
				hmOfGIDAndIndex.put(iGID, i+1);
			}

			//Writing all Markers in Row-0 from Column-1 onwards
			HashMap<String, Integer> hmOfMarkerNameAndIndex = new HashMap<String, Integer>();
			for (int j = 0; j < listOfMarkerNames.size(); j++){
				String markerName = listOfMarkerNames.get(j);
				strArrayOfData[0][j+1] = markerName;
				hmOfMarkerNameAndIndex.put(markerName, j+1);
			}
			
			
			//Writing the Allelic Data
			for (int i = 0; i < listOfAllCharValuesForGermplasmRetrieval.size(); i++){
				CharValues charValues = listOfAllCharValuesForGermplasmRetrieval.get(i);
				
				Integer gid = charValues.getgId();
				Integer markerId = charValues.getMarkerId();
				String strMarkerName  = hmOfMIDandMNames.get(markerId);
				String data = charValues.getCharValue();
				
				Integer iRowIndex = 0;
				Integer iColIndex = 0;
				if (hmOfGIDAndIndex.containsKey(gid)){
					iRowIndex = hmOfGIDAndIndex.get(gid);
					if (hmOfMarkerNameAndIndex.containsKey(strMarkerName)){
						iColIndex = hmOfMarkerNameAndIndex.get(strMarkerName);
					}
					//System.out.println(gid + " --- " + strMarkerName + " --- " + iRowIndex + " --- " + iColIndex + " --- " + data);
					if (null == data){
						data = "0:0";
					}
					strArrayOfData[iRowIndex][iColIndex] = data;
				}
			}
			
			for (int i = 0; i < iNumOfRows; i++){
				for (int j = 0; j < iNumOfCols; j++){
					String strData = strArrayOfData[i][j];
					if (null == strData){
						strData = "";
					}
					flapjackBufferedWriter.write(strData + "\t");
				}
				flapjackBufferedWriter.write("\n");
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	private void writeDatFileBasedOnGIDs2(
			GDMSMain theMainHomePage,
			ArrayList<AllelicValueElement> listOfAllelicValueElementsForGermplasmRetrievals,
			ArrayList<Integer> listOfGIDsToBeExported,
			ArrayList<String> listOfMarkerNames,
			HashMap<Integer, String> hmOfMIDandMNames) throws GDMSException {
		
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePath + "\\" + strFlapjackDatFile + ".dat");
		
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			flapjackBufferedWriter.write("\n");
			
			int iNumOfCols = listOfMarkerNames.size() + 2;
			int iNumOfRows = listOfGIDsToBeExported.size() + 1;

			String[][] strArrayOfData = new String[iNumOfRows][iNumOfCols];

			//Writing all GIDs in Column-0, from Row-1 onwards
			HashMap<Integer, Integer> hmOfGIDAndIndex = new HashMap<Integer, Integer>(); 
			for (int i = 0; i < listOfGIDsToBeExported.size(); i++){
				Integer iGID = listOfGIDsToBeExported.get(i);
				strArrayOfData[i+1][0] = String.valueOf(iGID);
				hmOfGIDAndIndex.put(iGID, i+1);
			}

			//Writing all Markers in Row-0 from Column-1 onwards
			HashMap<String, Integer> hmOfMarkerNameAndIndex = new HashMap<String, Integer>();
			for (int j = 0; j < listOfMarkerNames.size(); j++){
				String markerName = listOfMarkerNames.get(j);
				strArrayOfData[0][j+1] = markerName;
				hmOfMarkerNameAndIndex.put(markerName, j+1);
			}
			
			
			//Writing the Allelic Data
			for (int i = 0; i < listOfAllelicValueElementsForGermplasmRetrievals.size(); i++){
				AllelicValueElement allelicValueWithMarkerIdElement = listOfAllelicValueElementsForGermplasmRetrievals.get(i);
				
				Integer gid = allelicValueWithMarkerIdElement.getGid();
				String strMarkerName  = allelicValueWithMarkerIdElement.getMarkerName();
				
				String data = allelicValueWithMarkerIdElement.getData();
				
				Integer iRowIndex = 0;
				Integer iColIndex = 0;
				if (hmOfGIDAndIndex.containsKey(gid)){
					iRowIndex = hmOfGIDAndIndex.get(gid);
					if (hmOfMarkerNameAndIndex.containsKey(strMarkerName)){
						iColIndex = hmOfMarkerNameAndIndex.get(strMarkerName);
					}
					//System.out.println(gid + " --- " + strMarkerName + " --- " + iRowIndex + " --- " + iColIndex + " --- " + data);
					if (null == data){
						data = "0:0";
					}
					strArrayOfData[iRowIndex][iColIndex] = data;
				}
			}
			
			for (int i = 0; i < iNumOfRows; i++){
				for (int j = 0; j < iNumOfCols; j++){
					String strData = strArrayOfData[i][j];
					if (null == strData){
						strData = "";
					}
					flapjackBufferedWriter.write(strData + "\t");
				}
				flapjackBufferedWriter.write("\n");
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	public void generateFlapjackDataFilesByGermplasmNames(
			GDMSMain theMainHomePage,
			ArrayList<CharValues> listOfAllCharValuesForGermplasmRetrieval,
			ArrayList<AllelicValueElement>   listOfAllelicValueElementsForGermplasmRetrievals,
			 ArrayList<MappingPopValues> listOfAllMappingPopValuesForGermplasmRetrieval,
			ArrayList listOfAllMappingData,
			ArrayList<String> listOfGNamesToBeExported,
			ArrayList<String> listOfMarkerNames,
			HashMap<Integer, String> hmOfMIDandMNames,
			HashMap<Integer, String> hmOfGIdsAndNval,
			ArrayList<QtlDetailElement> listOfAllQTLDetails,HashMap<Integer, String> hmOfQtlPosition, HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName,
			String strSelectedExportType, boolean bQTLExists, String datasetType) {
		
		try {
			writeTextFile(theMainHomePage, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType,  bQTLExists);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.txt file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			
			if (null != listOfAllCharValuesForGermplasmRetrieval){
				writeDatFileBasedOnGermplasmNames1(theMainHomePage, listOfAllCharValuesForGermplasmRetrieval, listOfGNamesToBeExported, hmOfGIdsAndNval,
						listOfMarkerNames, hmOfMIDandMNames);
			} else if (null != listOfAllelicValueElementsForGermplasmRetrievals){
				writeDatFileBasedOnGermplasmNames2(theMainHomePage, listOfAllelicValueElementsForGermplasmRetrievals, listOfGNamesToBeExported, hmOfGIdsAndNval,
						listOfMarkerNames, hmOfMIDandMNames);
			} else if (null != listOfAllMappingPopValuesForGermplasmRetrieval){
				writeDatFileBasedOnGermplasmNames3(theMainHomePage, listOfAllMappingPopValuesForGermplasmRetrieval, listOfGNamesToBeExported, hmOfGIdsAndNval,
						listOfMarkerNames, hmOfMIDandMNames);
			}
			
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.dat file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeMapFile(theMainHomePage, listOfAllMappingData);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.map file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
	}

	private void writeDatFileBasedOnGermplasmNames1(GDMSMain theMainHomePage,
			ArrayList<CharValues> listOfAllCharValuesForGermplasmRetrieval,
			ArrayList<String> listOfGNamesToBeExported,
			HashMap<Integer, String> hmOfGIdsAndNval,
			ArrayList<String> listOfMarkerNames,
			HashMap<Integer, String> hmOfMIDandMNames) throws GDMSException {
		
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePath + "\\" + strFlapjackDatFile + ".dat");
		
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			flapjackBufferedWriter.write("\n");
			
			int iNumOfCols = listOfMarkerNames.size() + 2;
			int iNumOfRows = listOfGNamesToBeExported.size() + 1;

			String[][] strArrayOfData = new String[iNumOfRows][iNumOfCols];

			//Writing all GNames in Column-0, from Row-1 onwards
			HashMap<String, Integer> hmOfGNameAndIndex = new HashMap<String, Integer>(); 
			for (int i = 0; i < listOfGNamesToBeExported.size(); i++){
				String strGName = listOfGNamesToBeExported.get(i);
				strArrayOfData[i+1][0] = strGName;
				hmOfGNameAndIndex.put(strGName, i+1);
			}

			//Writing all Markers in Row-0 from Column-1 onwards
			HashMap<String, Integer> hmOfMarkerNameAndIndex = new HashMap<String, Integer>();
			for (int j = 0; j < listOfMarkerNames.size(); j++){
				String markerName = listOfMarkerNames.get(j);
				strArrayOfData[0][j+1] = markerName;
				hmOfMarkerNameAndIndex.put(markerName, j+1);
			}
			
			
			//Writing the Allelic Data
			for (int i = 0; i < listOfAllCharValuesForGermplasmRetrieval.size(); i++){
				CharValues charValueElement = listOfAllCharValuesForGermplasmRetrieval.get(i);
				
				Integer gid = charValueElement.getgId();
				String strGName = hmOfGIdsAndNval.get(gid);
				
				Integer markerId = charValueElement.getMarkerId();
				String strMarkerName = hmOfMIDandMNames.get(markerId);
				
				String data = charValueElement.getCharValue();
				
				Integer iRowIndex = 0;
				Integer iColIndex = 0;
				if (hmOfGNameAndIndex.containsKey(strGName)){
					iRowIndex = hmOfGNameAndIndex.get(strGName);
					if (hmOfMarkerNameAndIndex.containsKey(strMarkerName)){
						iColIndex = hmOfMarkerNameAndIndex.get(strMarkerName);
					}
					//System.out.println(gid + " --- " + strMarkerName + " --- " + iRowIndex + " --- " + iColIndex + " --- " + data);
					if (null == data){
						data = "0:0";
					}
					strArrayOfData[iRowIndex][iColIndex] = data;
				}
			}
			
			for (int i = 0; i < iNumOfRows; i++){
				for (int j = 0; j < iNumOfCols; j++){
					String strData = strArrayOfData[i][j];
					if (null == strData){
						strData = "";
					}
					flapjackBufferedWriter.write(strData + "\t");
				}
				flapjackBufferedWriter.write("\n");
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	private void writeDatFileBasedOnGermplasmNames2(
			GDMSMain theMainHomePage,
			ArrayList<AllelicValueElement> listOfAllelicValueElementsForGermplasmRetrievals,
			ArrayList<String> listOfGNamesToBeExported,
			HashMap<Integer, String> hmOfGIdsAndNval,
			ArrayList<String> listOfMarkerNames,
			HashMap<Integer, String> hmOfMIDandMNames) throws GDMSException {
		
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePath + "\\" + strFlapjackDatFile + ".dat");
		
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			flapjackBufferedWriter.write("\n");
			
			int iNumOfCols = listOfMarkerNames.size() + 2;
			int iNumOfRows = listOfGNamesToBeExported.size() + 1;

			String[][] strArrayOfData = new String[iNumOfRows][iNumOfCols];

			//Writing all GNames in Column-0, from Row-1 onwards
			HashMap<String, Integer> hmOfGNameAndIndex = new HashMap<String, Integer>(); 
			for (int i = 0; i < listOfGNamesToBeExported.size(); i++){
				String strGName = listOfGNamesToBeExported.get(i);
				strArrayOfData[i+1][0] = strGName;
				hmOfGNameAndIndex.put(strGName, i+1);
			}

			//Writing all Markers in Row-0 from Column-1 onwards
			HashMap<String, Integer> hmOfMarkerNameAndIndex = new HashMap<String, Integer>();
			for (int j = 0; j < listOfMarkerNames.size(); j++){
				String markerName = listOfMarkerNames.get(j);
				strArrayOfData[0][j+1] = markerName;
				hmOfMarkerNameAndIndex.put(markerName, j+1);
			}
			
			
			//Writing the Allelic Data
			for (int i = 0; i < listOfAllelicValueElementsForGermplasmRetrievals.size(); i++){
				AllelicValueElement allelicValueElement = listOfAllelicValueElementsForGermplasmRetrievals.get(i);
				
				Integer gid = allelicValueElement.getGid();
				String strGName = hmOfGIdsAndNval.get(gid);
				
				String strMarkerName = allelicValueElement.getMarkerName();
				
				String data = allelicValueElement.getData();
				
				String alleleBinValue = allelicValueElement.getAlleleBinValue();
				
				Integer iRowIndex = 0;
				Integer iColIndex = 0;
				if (hmOfGNameAndIndex.containsKey(strGName)){
					iRowIndex = hmOfGNameAndIndex.get(strGName);
					if (hmOfMarkerNameAndIndex.containsKey(strMarkerName)){
						iColIndex = hmOfMarkerNameAndIndex.get(strMarkerName);
					}
					//System.out.println(strGName + " --- " + strMarkerName + " --- " + iRowIndex + " --- " + iColIndex + " --- " + data + " --- " + alleleBinValue);
					if (null == data){
						data = "0:0";
					}
					strArrayOfData[iRowIndex][iColIndex] = data;
				}
			}
			
			for (int i = 0; i < iNumOfRows; i++){
				for (int j = 0; j < iNumOfCols; j++){
					String strData = strArrayOfData[i][j];
					if (null == strData){
						strData = "";
					}
					flapjackBufferedWriter.write(strData + "\t");
				}
				flapjackBufferedWriter.write("\n");
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	private void writeDatFileBasedOnGermplasmNames3(
			GDMSMain theMainHomePage,
			ArrayList<MappingPopValues> listOfAllMappingPopValuesForGermplasmRetrieval,
			ArrayList<String> listOfGNamesToBeExported,
			HashMap<Integer, String> hmOfGIdsAndNval,
			ArrayList<String> listOfMarkerNames,
			HashMap<Integer, String> hmOfMIDandMNames) throws GDMSException {
		
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePath + "\\" + strFlapjackDatFile + ".dat");
		
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			flapjackBufferedWriter.write("\n");
			
			for(int m = 0; m< listOfMarkerNames.size(); m++){
				flapjackBufferedWriter.write("\t"+listOfMarkerNames.get(m));
			}
			
			int iNumOfCols = listOfMarkerNames.size() + 2;
			int iNumOfRows = listOfGNamesToBeExported.size() + 1;

			String[][] strArrayOfData = new String[iNumOfRows][iNumOfCols];

			//Writing all GNames in Column-0, from Row-1 onwards
			HashMap<String, Integer> hmOfGNameAndIndex = new HashMap<String, Integer>(); 
			for (int i = 0; i < listOfGNamesToBeExported.size(); i++){
				String strGName = listOfGNamesToBeExported.get(i);
				strArrayOfData[i+1][0] = strGName;
				hmOfGNameAndIndex.put(strGName, i+1);
			}

			//Writing all Markers in Row-0 from Column-1 onwards
			HashMap<String, Integer> hmOfMarkerNameAndIndex = new HashMap<String, Integer>();
			for (int j = 0; j < listOfMarkerNames.size(); j++){
				String markerName = listOfMarkerNames.get(j);
				strArrayOfData[0][j+1] = markerName;
				hmOfMarkerNameAndIndex.put(markerName, j+1);
			}
			
			
			//Writing the Allelic Data
			for (int i = 0; i < listOfAllMappingPopValuesForGermplasmRetrieval.size(); i++){
				MappingPopValues mappingPopValue = listOfAllMappingPopValuesForGermplasmRetrieval.get(i);
				
				Integer gid = mappingPopValue.getGid();
				String strGName = hmOfGIdsAndNval.get(gid);
				
				Integer markerId = mappingPopValue.getMarkerId();
				String strMarkerName = hmOfMIDandMNames.get(markerId);
				
				String data = mappingPopValue.getMapCharValue();
				
				Integer iRowIndex = 0;
				Integer iColIndex = 0;
				if (hmOfGNameAndIndex.containsKey(strGName)){
					iRowIndex = hmOfGNameAndIndex.get(strGName);
					if (hmOfMarkerNameAndIndex.containsKey(strMarkerName)){
						iColIndex = hmOfMarkerNameAndIndex.get(strMarkerName);
					}
					//System.out.println(gid + " --- " + strMarkerName + " --- " + iRowIndex + " --- " + iColIndex + " --- " + data);
					if (null == data){
						data = "0:0";
					}
					strArrayOfData[iRowIndex][iColIndex] = data;
				}
			}
			
			for (int i = 0; i < iNumOfRows; i++){
				for (int j = 0; j < iNumOfCols; j++){
					String strData = strArrayOfData[i][j];
					if (null == strData){
						strData = "";
					}
					flapjackBufferedWriter.write(strData + "\t");
				}
				flapjackBufferedWriter.write("\n");
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	
	private void writeMapFile(GDMSMain theMainHomePage,
			ArrayList listOfAllMappingData) throws GDMSException {
	
		String strFlapjackMapFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedMapFile = new File(strFilePath + "\\" + strFlapjackMapFile + ".map");
		
		*//**	writing tab delimited .map file for FlapJack  
		 * 	consisting of marker chromosome & position
		 * 
		 * **//*
		
		try {
			FileWriter flapjackMapFileWriter = new FileWriter(generatedMapFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackMapFileWriter);
			
			for (int m=0; m<listOfAllMappingData.size();m++){
				String[] MapInfo=listOfAllMappingData.get(m).toString().split("!~!");	
			
			
				
				flapjackBufferedWriter.write(MapInfo[0]);
				flapjackBufferedWriter.write("\t");
				flapjackBufferedWriter.write(MapInfo[1]);
				flapjackBufferedWriter.write("\t");
				flapjackBufferedWriter.write(MapInfo[2]);
				flapjackBufferedWriter.write("\n");
				
			}
			
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}

	private void writeTextFile(GDMSMain theMainHomePage,
			ArrayList<QtlDetailElement> listOfAllQTLDetails,HashMap<Integer, String> hmOfQtlPosition, HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName,
			String strSelectedExportType, boolean bQTLExists) throws GDMSException {
		
		String strFlapjackTextFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		//System.out.println("strFilePath=:"+strFilePath);
		generatedTextFile = new File(strFilePath + "\\" + strFlapjackTextFile + ".txt");

		*//**	writing tab delimited qtl file for FlapJack  
		 * 	consisting of marker chromosome & position
		 * 
		 * **//*
		try {
			factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			OntologyDataManager om=factory.getOntologyDataManager();
			FileWriter flapjackTextWriter = new FileWriter(generatedTextFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			
			//flapjackBufferedWriter.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tfavallele\tFlanking markers in original publication\teffect");
			//flapjackBufferedWriter.write("\n");
			for (int i = 0 ; i < listOfAllQTLDetails.size(); i++){
				//System.out.println(listOfAllQTLDetails.get(i));
				QtlDetailElement qtlDetails = listOfAllQTLDetails.get(i);
				
				QtlDetailsPK id = qtlDetails.getQtlName().get.getId();
				Integer qtlId = id.getQtlId();
				//String strQtlName = hmOfQtlIdandName.get(qtlId);
				String strQtlName =qtlDetails.getQtlName();
				int qtlId=hmOfQtlNameId.get(strQtlName);
				//qtlDetails.get
				//Float clen = qtlDetails.getClen();
				//Float fEffect = qtlDetails.getEffect();
				int fEffect = qtlDetails.getEffect();
				Float fMaxPosition = qtlDetails.getMaxPosition();
				Float fMinPosition = qtlDetails.getMinPosition();
				//Float fPosition = qtlDetails.getPosition();
				String fPosition = hmOfQtlPosition.get(qtlId);
				Float frSquare = qtlDetails.getRSquare();
				Float fScoreValue = qtlDetails.getScoreValue();
				String strExperiment = qtlDetails.getExperiment();
				//String strHvAllele = qtlDetails..getHvAllele();
				//String strHvParent = qtlDetails.getHvParent();
				//String strInteractions = qtlDetails.getInteractions();
				String strLeftFlankingMarker = qtlDetails.getLeftFlankingMarker();
				String strLinkageGroup = qtlDetails.getChromosome();
				//String strLvAllele = qtlDetails.getLvAllele();
				//String strLvParent = qtlDetails.getLvParent();
				String strRightFM = qtlDetails.getRightFlankingMarker();
				//String strSeAdditive = qtlDetails.getSeAdditive();
				
				//String strTrait = qtlDetails.getTrait();
				String strTrait = qtlDetails.getTRName();
							
				
				flapjackBufferedWriter.write(strQtlName + "\t" + strLinkageGroup + "\t" + fPosition + "\t" + fMinPosition + "\t" + fMaxPosition + "\t" +
						strTrait + "\t" + strExperiment + "\t \t" + fScoreValue + "\t" + frSquare+
	                     "\t" + strLeftFlankingMarker+"/"+strRightFM + "\t" + fEffect);
				
				flapjackBufferedWriter.write("\n");
				
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} 
		
	}*/


}
