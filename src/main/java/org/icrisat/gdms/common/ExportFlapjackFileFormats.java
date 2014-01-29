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
import org.generationcp.middleware.pojos.gdms.MapInfo;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.ui.Window.Notification;
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;

public class ExportFlapjackFileFormats {
	ManagerFactory factory =null;
	private File generatedTextFile;
	private File generatedDatFile;
	private File generatedMapFile;

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
			ArrayList listOfAllAllelicValues,			
			ArrayList listOfAllMapInfo,
			ArrayList<Integer> listOfGIDsToBeExported,
			ArrayList<String> listOfMarkerNames,
			TreeMap<Integer, String> hmOfMIDandMNames, ArrayList<QtlDetailElement> listOfAllQTLDetails,
			HashMap<Integer, String> hmOfQtlPosition, HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName, String strSelectedExportType, boolean bQTLExists, String datasetType) {
		
			
		
		
		try {
			writeTextFile(theMainHomePage, listOfAllQTLDetails, hmOfQtlPosition,  hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType,  bQTLExists);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.txt file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeDatFile(theMainHomePage, listOfAllAllelicValues, listOfGIDsToBeExported,
					listOfMarkerNames, hmOfMIDandMNames, datasetType);
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
			
			/*
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			
			OntologyDataManager ontManager=factory.getOntologyDataManager();
			*/
			FileWriter flapjackTextWriter = new FileWriter(generatedTextFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			//getAllelicValuesByGidsAndMarkerNames
			//genoManager.getAlle
			//			  fjackQTL.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tFlanking markers in original publication");
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
	
	private void writeDatFile(
			GDMSMain theMainHomePage,
			ArrayList a,
			ArrayList<Integer> listOfGIDsToBeExported,
			ArrayList<String> listOfMarkerNames,
			TreeMap<Integer, String> hmOfMIDandMNames, String dType) throws GDMSException {
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
		
		
		/*//System.out.println("listOfAllAllelicValues=:"+a);
		//System.out.println("listOfMarkerNames=:"+listOfMarkerNames);
		//System.out.println("listOfGIDsToBeExported=:"+listOfGIDsToBeExported);*/
		/*//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		//System.out.println(strFilePathGids);*/
		try {
			FileWriter flapjackTextWriterGIDs = new FileWriter(generatedDatFile);
			BufferedWriter fjackdatGids = new BufferedWriter(flapjackTextWriterGIDs);
			
			for(int m1 = 0; m1< listOfMarkerNames.size(); m1++){
				////System.out.println("m1=:"+m1);
				fjackdatGids.write("\t"+listOfMarkerNames.get(m1));
			}
			
			int al=0;
			for (int j=0;j<listOfGIDsToBeExported.size();j++){ 
				////System.out.println("jdfhgjkdfghkjdfhgkdfh          &&&&&&&&&&&&&&&&&&&&&&&&&&:");
				String arrList6[]=new String[3];
				fjackdatGids.write("\n"+listOfGIDsToBeExported.get(j));		
			    for (int k=0;k<listOfMarkerNames.size();k++){
			    	if(al<a.size()){
			    		////System.out.println("al="+al+"    "+a.get(al));
			    		String strList5=a.get(al).toString();
			    		// String[] arrList6=strList5.split(",");
			    		// //System.out.println(k+":"+strList5);
			    		StringTokenizer stz = new StringTokenizer(strList5.toString(), "!~!");
			    		//arrList6 = new String[stz.countTokens()];
			    		int i1=0;				  
			    		while(stz.hasMoreTokens()){
			    			arrList6[i1] = stz.nextToken();
			    			i1++;
			    		}
			    		////System.out.println(arrList6[0]+"==("+listOfGIDsToBeExported.get(j).toString()+")) && "+hmOfMIDandMNames.get(Integer.parseInt(arrList6[1]))+".equals("+listOfMarkerNames.get(k));
			    		condition=((Integer.parseInt(arrList6[0])==Integer.parseInt(listOfGIDsToBeExported.get(j).toString())) && hmOfMIDandMNames.get(Integer.parseInt(arrList6[1])).equals(listOfMarkerNames.get(k)));
			    		if(condition){
			    			if(arrList6[2].contains("/")){
			    				////System.out.println("if \\");
								String[] ChVal1=arrList6[2].split("/");
								if(dType.equalsIgnoreCase("ssr")){
									if(arrList6[2].length()==3){
										chVal="";
									}else{
										if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
											chVal=ChVal1[0];
										}else{
											chVal=ChVal1[0]+"/"+ChVal1[1];
										}
									}
								}else{
									if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
										chVal=ChVal1[0];
									}else{
										chVal=ChVal1[0]+"/"+ChVal1[1];
									}
								}
			    			}else if(arrList6[2].contains(":")){								
								String[] ChVal1=arrList6[2].split(":");
								if(dType.equalsIgnoreCase("ssr")){
									if(arrList6[2].length()==3){
										chVal="";
									}else{
										if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
											chVal=ChVal1[0];
										}else{
											chVal=ChVal1[0]+"/"+ChVal1[1];
										}
									}
								}else{
									if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
										chVal=ChVal1[0];
									}else{
										chVal=ChVal1[0]+"/"+ChVal1[1];
									}
								}
							}else if(arrList6[2].contains("?")){
								chVal="";
							}else{
								////System.out.println("else ?, :, /"+arrList6[2]);
								chVal=arrList6[2];
							}
			    			fjackdatGids.write("\t"+chVal);	
							
						   
						}else{
							fjackdatGids.write("\t");
						}
					   al++;
					   
			    	}
			      }
		    	
		     }
						
			fjackdatGids.close();			
			
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

	public void generateFlapjackDataFilesByGermplasmNames(
			GDMSMain theMainHomePage,
			ArrayList listOfAllelicValuesForMappingType,
			ArrayList listOfAllMappingData,
			ArrayList<String> listOfGNamesToBeExported,
			ArrayList<String> listOfMarkerNames,
			TreeMap<Integer, String> hmOfMIDandMNames,
			TreeMap<Integer, String> hmOfGIdsAndNval,
			ArrayList<QtlDetailElement> listOfAllQTLDetails,			
			HashMap<Integer, String> hmOfQtlPosition, 
			HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName,
			String strSelectedExportType, boolean bQTLExists, String datasetType) {
		try {
			writeTextFile(theMainHomePage, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType,  bQTLExists);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.txt file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeDatFileGermplasmNames(theMainHomePage, listOfAllelicValuesForMappingType, listOfGNamesToBeExported, hmOfGIdsAndNval,
					listOfMarkerNames, hmOfMIDandMNames, datasetType);
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
			ArrayList a,
			ArrayList<String> listOfGNamesToBeExported,
			TreeMap<Integer, String> hmOfGIdsAndNval, ArrayList<String> listOfMarkerNames,
			TreeMap<Integer, String> hmOfMIDandMNames, String dType) throws GDMSException {
		boolean condition=false;
		int noOfAccs=listOfGNamesToBeExported.size();
		int noOfMarkers=listOfMarkerNames.size();			
		
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
		/*File fexists=new File(filePath+("//")+"/Flapjack.txt");
		if(fexists.exists()) { fexists.delete(); }*/
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter fjackdat = new BufferedWriter(flapjackTextWriter);
			//fjackdat.write("\n");
			
			for(int m = 0; m< listOfMarkerNames.size(); m++){
				fjackdat.write("\t"+listOfMarkerNames.get(m));
			}
			
			int al=0;
			
			for (int j=0;j<listOfGNamesToBeExported.size();j++){ 
				String arrList6[]=new String[3];
				fjackdat.write("\n"+listOfGNamesToBeExported.get(j));		
			    for (int k=0;k<listOfMarkerNames.size();k++){
			    	if(al<a.size()){
			    		////System.out.println("al="+al+"    "+a.get(al));
			    		String strList5=a.get(al).toString();
			    		// String[] arrList6=strList5.split(",");
			    		 ////System.out.println(k+":"+strList5);
			    		StringTokenizer stz = new StringTokenizer(strList5.toString(), "!~!");
			    		//arrList6 = new String[stz.countTokens()];
			    		int i1=0;				  
			    		while(stz.hasMoreTokens()){
			    			arrList6[i1] = stz.nextToken();
			    			i1++;
			    		}
			    		////System.out.println(hmOfGIdsAndNval.get(Integer.parseInt(arrList6[0]))+"  .equalsIgnoreCase(   "+listOfGNamesToBeExported.get(j).toString()+")) &&  "+ hmOfMIDandMNames.get(Integer.parseInt(arrList6[1]))+"  .equals(   "+listOfMarkerNames.get(k));	
			    		condition=((hmOfGIdsAndNval.get(Integer.parseInt(arrList6[0])).equalsIgnoreCase(listOfGNamesToBeExported.get(j).toString())) && hmOfMIDandMNames.get(Integer.parseInt(arrList6[1])).equals(listOfMarkerNames.get(k)));
			    			
					   
			    		if(condition){
			    			if(arrList6[2].contains("/")){
			    				////System.out.println("if \\");
								String[] ChVal1=arrList6[2].split("/");
								if(dType.equalsIgnoreCase("ssr")){
									if(arrList6[2].length()==3){
										chVal="";
									}else{
										if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
											chVal=ChVal1[0];
										}else{
											chVal=ChVal1[0]+"/"+ChVal1[1];
										}
									}
								}else{
									if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
										chVal=ChVal1[0];
									}else{
										chVal=ChVal1[0]+"/"+ChVal1[1];
									}
								}
			    			}else if(arrList6[2].contains(":")){								
								String[] ChVal1=arrList6[2].split(":");
								if(dType.equalsIgnoreCase("ssr")){
									if(arrList6[2].length()==3){
										chVal="";
									}else{
										if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
											chVal=ChVal1[0];
										}else{
											chVal=ChVal1[0]+"/"+ChVal1[1];
										}
									}
								}else{
									if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
										chVal=ChVal1[0];
									}else{
										chVal=ChVal1[0]+"/"+ChVal1[1];
									}
								}
							}else if(arrList6[2].contains("?")){
								chVal="";
							}else{
								////System.out.println("else ?, :, /"+arrList6[2]);
								chVal=arrList6[2];
							}
							fjackdat.write("\t"+chVal);	
							
						   
						}else{
							fjackdat.write("\t");
						}
					   al++;
			    	}
			      }
		    	
		     }
					
			fjackdat.close();			
		
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}


}
