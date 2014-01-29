package org.icrisat.gdms.common;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableHyperlink;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionPerThreadProvider;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.hibernate.HibernateUtil;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.AllelicValueWithMarkerIdElement;
import org.generationcp.middleware.pojos.gdms.GermplasmMarkerElement;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.workbench.Project;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Table;


public class ExportFileFormats {

	//	ArrayList<AlleleValues> listOfAlleleValues = new ArrayList<AlleleValues>();
	//	ArrayList<MarkerNameElement> listOfMarkers = new ArrayList<MarkerNameElement>();
	private GDMSModel _gdmsModel;		
	 private static WorkbenchDataManager workbenchDataManager;
	 private static HibernateUtil hibernateUtil;
	 HashMap<Object, String> IBWFProjects= new HashMap<Object, String>();
	 
	 String bPath="";
     String opPath="";
    
     ////System.out.println(",,,,,,,,,,,,,  :"+bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1));
     String pathWB="";
     
	    String dbNameL="";
	    String instDir="";
	    int currWorkingProject=0;
	public ExportFileFormats(){
		_gdmsModel = GDMSModel.getGDMSModel();
		
		try{
			/*hibernateUtil = new HibernateUtil(GDMSModel.getGDMSModel().getWorkbenchParams());
			HibernateSessionProvider sessionProvider = new HibernateSessionPerThreadProvider(hibernateUtil.getSessionFactory());
			workbenchDataManager = new WorkbenchDataManagerImpl(sessionProvider);*/
			instDir=_gdmsModel.getWorkbenchDataManager().getWorkbenchSetting().getInstallationDirectory().toString();
			Project results = _gdmsModel.getWorkbenchDataManager().getLastOpenedProject(_gdmsModel.getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId());
			currWorkingProject=Integer.parseInt(results.getProjectId().toString());
			////System.out.println("..........currWorkingProject=:"+currWorkingProject);
		}catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
	}

	//This method is for Markers Retrieval under Genotyping
	//markList - The list of Markers provided initially
	//dataMap - Allele values for each marker selected
	public File MatrixDataSNP(GDMSMain theMainHomePage, List<Integer> listOfGIDs, List<String> markList, List<GermplasmMarkerElement> listOfGermplasmNames, List<AllelicValueElement> listOfAllelicValues, HashMap<Integer, String> hmOfGIDsAndGermplamsSelected){		

		try{
			
			long time = new Date().getTime();
			String strFileName = "Matrix" + String.valueOf(time);
			File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
			File absoluteFile = baseDirectory.getAbsoluteFile();

			File[] listFiles = absoluteFile.listFiles();
			File fileExport = baseDirectory;
			for (File file : listFiles) {
				if(file.getAbsolutePath().endsWith("FileExports")) {
					fileExport = file;
					break;
				}
			}
			String folderName="AnalysisFiles";
			String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
			
			if(!new File(strFilePath).exists())
		       	new File(strFilePath).mkdir();
			//String strFilePath = fileExport.getAbsolutePath();
			File generatedFile = new File(strFilePath + "\\" + strFileName + ".txt");

			FileWriter SNPdatstream = new FileWriter(generatedFile);
			BufferedWriter SNPMatrix = new BufferedWriter(SNPdatstream);
			SNPMatrix.write("Marker's Provided: ");
			for(int m1 = 0; m1 < markList.size(); m1++){
				SNPMatrix.write("\t" + markList.get(m1));
			}

			SNPMatrix.write("\n\n\n");
			SNPMatrix.write("GID " + "\t" + "Germplasm-Name");
			for (int i = 0; i < listOfGIDs.size(); i++){
				Integer iGID = listOfGIDs.get(i);
				if (hmOfGIDsAndGermplamsSelected.containsKey(iGID)){
					String strGName = hmOfGIDsAndGermplamsSelected.get(iGID);
					SNPMatrix.write("\n" + iGID + "\t" + strGName);	
				}
			}

			SNPMatrix.write("\n\n\n");	
			SNPMatrix.write("GID " + "\t" + "Marker-Name" + "\t" + "Data" + "\n");
			for (int i = 0; i < markList.size(); i++){
				String strMarker = markList.get(i);
				for (int j = 0; j < listOfAllelicValues.size(); j++){
					AllelicValueElement allelicValueElement = listOfAllelicValues.get(j);
					String alleleBinValue = allelicValueElement.getAlleleBinValue();
					String markerName = allelicValueElement.getMarkerName();
					Integer gid = allelicValueElement.getGid();
					String data = allelicValueElement.getData();
					if (markerName.equals(strMarker)){
						SNPMatrix.write(gid + "\t" + markerName + "\t" + data + "\n");
					}
				}

			}


			SNPMatrix.close();	

			return generatedFile;
		}catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

	public void CMTVTxt(ArrayList<String[]> sortMapListToBeDisplayed, String theFilePath, GDMSMain _mainHomePage, boolean shouldOpenByDefault) throws GDMSException {

		FileWriter fileWriter;
		File file;

		try {

			file = new File(theFilePath);
			fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write("\t" + "LINKAGE-GROUP" + "\t" +
					"MARKER-NAME" + "\t" + "COUNT" + "\t" + "DISTANCE" + "STARTING-POSITION" + "\n");

			for (int i = 0; i < sortMapListToBeDisplayed.size(); i++){


				String[] strArray = sortMapListToBeDisplayed.get(i);

				String strLG = strArray[0];
				String strMarkerName = strArray[1];
				String strCount = strArray[2];
				String strDistance = strArray[3];
				String strStartingPoint = strArray[4];


				bufferedWriter.write("\t" + strLG + "\t" + strMarkerName + "\t" + 
						strCount + "\t" + strDistance + "\t" + strStartingPoint + "\n");

			}

			bufferedWriter.close();

		} catch (IOException e1) {
			throw new GDMSException(e1.getMessage());
		}

		FileResource fileResource = new FileResource(file, _mainHomePage);
		if(shouldOpenByDefault) {
			_mainHomePage.getMainWindow().getWindow().open(fileResource, "_blank");
		}
	}

	public void CMTVTxt(ArrayList<String[]> sortMapListToBeDisplayed, String theFilePath, GDMSMain _mainHomePage) throws GDMSException {
		CMTVTxt(sortMapListToBeDisplayed, theFilePath, _mainHomePage, true);
	}


	/**	Writing genotyping .dat file for FlapJack */
	public void FlapjackDataFile(ArrayList a, String mapData, String filePath, ArrayList accList, ArrayList markList, ArrayList qtlData, String expOp, boolean qtlexists){

		try{
			boolean condition=false;
			/*//System.out.println("List="+a);
			//System.out.println(" gListExp="+ accList);
			//System.out.println(" mListExp="+ markList);
*/
			//int noOfAccs=accList.size();
			//int noOfMarkers=markList.size();			
			File fexists=new File(filePath+("//")+"/Flapjack.txt");
			if(fexists.exists()) { fexists.delete(); }
			//int accIndex=1,markerIndex=1;
			//int i;
			String chVal="";
			FileWriter flapjackdatstream = new FileWriter(filePath+("//")+"/Flapjack.dat");
			BufferedWriter fjackdat = new BufferedWriter(flapjackdatstream);

			for(int m1 = 0; m1< markList.size(); m1++){
				fjackdat.write("\t"+markList.get(m1));
			}

			int al=0;
			for (int j=0;j<accList.size();j++){ 
				String arrList6[]=new String[3];
				fjackdat.write("\n"+accList.get(j));		
				for (int k=0;k<markList.size();k++){
					if(al<a.size()){
						String strList5=a.get(al).toString();
						StringTokenizer stz = new StringTokenizer(strList5.toString(), ",");
						int i1=0;				  
						while(stz.hasMoreTokens()){
							arrList6[i1] = stz.nextToken();
							i1++;
						}
						if(expOp.equalsIgnoreCase("gids"))
							condition=((Integer.parseInt(arrList6[0])==Integer.parseInt(accList.get(j).toString())) && arrList6[1].equals(markList.get(k)));
						else
							condition=((arrList6[0].equalsIgnoreCase(accList.get(j).toString().toLowerCase())) && arrList6[1].equals(markList.get(k)));
						if(condition){
							if(arrList6[2].contains("/")){								
								String[] ChVal1=arrList6[2].split("/");
								if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
									chVal=ChVal1[0];
								}else{
									chVal=ChVal1[0]+"/"+ChVal1[1];
								}
							}else if(arrList6[2].contains(":")){								
								String[] ChVal1=arrList6[2].split(":");
								if(ChVal1[0].equalsIgnoreCase(ChVal1[1])){
									chVal=ChVal1[0];
								}else{
									chVal=ChVal1[0]+"/"+ChVal1[1];
								}
							}else{
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



			/**	writing tab delimited .map file for FlapJack  
			 * 	consisting of marker chromosome & position
			 * 
			 * **/
			FileWriter flapjackmapstream = new FileWriter(filePath+("//")+"/Flapjack.map");
			BufferedWriter fjackmap = new BufferedWriter(flapjackmapstream);
			String[] mData=mapData.split("~~!!~~");

			for(int m=0;m<mData.length;m++){		
				String[] strMData=mData[m].split("!~!");
				fjackmap.write(strMData[0]);
				fjackmap.write("\t");
				fjackmap.write(strMData[1]);
				fjackmap.write("\t");
				fjackmap.write(strMData[2]);
				fjackmap.write("\n");		
			}
			fjackmap.close();


			/**	writing tab delimited qtl file for FlapJack  
			 * 	consisting of marker chromosome & position
			 * 
			 * **/
			if(qtlexists){
				FileWriter flapjackQTLstream = new FileWriter(filePath+("//")+"/Flapjack.txt");
				BufferedWriter fjackQTL = new BufferedWriter(flapjackQTLstream);
				fjackQTL.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tfavallele\tFlanking markers in original publication\teffect");
				fjackQTL.write("\n");
				for(int q=0;q<qtlData.size();q++){					
					String[] strMData=qtlData.get(q).toString().split("!~!");
					fjackQTL.write(strMData[0]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[1]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[2]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[3]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[4]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[5]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[6]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[7]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[8]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[9]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[10]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[11]);
					fjackQTL.write("\t");
					fjackQTL.write(strMData[12]);
					fjackQTL.write("\n");		
				}
				fjackQTL.close();
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * this method is used in Germplasm Retrieval and GID Retrieval
	 * 
	 * @param a --- list of AllelicValueElements
	 * @param accList --- list of GIDs
	 * @param markList --- list of Markers selected from the UI
	 * @param gMap --- Hashmap of GIDs and Germplasm Names selected
	 * @throws GDMSException 
	 */
	public File Matrix(
			GDMSMain theMainHomePage,
			ArrayList<Integer> listOfGIDsSelected,
			ArrayList<String> listOfMarkersSelected,
			HashMap<Integer, String> hashMapOfGIDsAndGNamesSelected,
			HashMap listAlleleValueElementsForGIDsSelected) throws GDMSException {
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		
        pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
        //System.out.println("pathWB=:"+pathWB);
        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
        if(!new File(pathWB+"/"+folderName).exists())
	   		new File(pathWB+"/"+folderName).mkdir();
        
	
		
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "\\" + strFileName + ".txt");
		int noOfAccs=listOfGIDsSelected.size();
		int noOfMarkers=listOfMarkersSelected.size();			
		
		int accIndex=1,markerIndex=1;
		int i;String chVal="";
		HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
		try {
			FileWriter datastream = new FileWriter(generatedFile);
			BufferedWriter SNPMatrix = new BufferedWriter(datastream);
			SNPMatrix.write("\t");
			for(int m1 = 0; m1< listOfMarkersSelected.size(); m1++){
				SNPMatrix.write("\t"+listOfMarkersSelected.get(m1));
			}
			
			//int k=0;
			int gid=0;
			String gname="";		
			
			for (int j=0;j<listOfGIDsSelected.size();j++){ 
				Iterator iterator = hashMapOfGIDsAndGNamesSelected.keySet().iterator();
				String arrList6[];
				gid=Integer.parseInt(listOfGIDsSelected.get(j).toString());
				 while (iterator.hasNext()){
	        	   Object key = iterator.next();
	        	   if(key.equals(gid)){
	        		   gname=hashMapOfGIDsAndGNamesSelected.get(key).toString();
	        	   }
				 }
				SNPMatrix.write("\n"+listOfGIDsSelected.get(j)+"\t"+gname);		
			    for (int k=0;k<listOfMarkersSelected.size();k++){
			    	////System.out.println("**************************  :"+dataMap.get(Integer.parseInt(accList.get(j).toString())));
			    	markerAlleles=(HashMap)listAlleleValueElementsForGIDsSelected.get(Integer.parseInt(listOfGIDsSelected.get(j).toString()));
			    	List markerKey = new ArrayList();
					markerKey.addAll(markerAlleles.keySet());
					//for(int m=0; m<markerKey.size();m++){
						//markerAlleles.
						if(markerAlleles.containsKey(listOfGIDsSelected.get(j).toString()+"!~!"+listOfMarkersSelected.get(k).toString())){
				    		SNPMatrix.write("\t"+markerAlleles.get(listOfGIDsSelected.get(j).toString()+"!~!"+listOfMarkersSelected.get(k).toString()));
				    		
				    	}else{
				    		SNPMatrix.write("\t");	
				    	}	
						
					//}
					
			    }		    	
			}					
			SNPMatrix.close();	
			

			
			//String strFilePath = fileExport.getAbsolutePath();
			File generatedFileWF = new File(pathWB+"\\"+folderName + "\\" + strFileName + ".txt");
			/*int noOfAccsI=listOfGIDsSelected.size();
			int noOfMarkersI=listOfMarkersSelected.size();			
			*/
			/*int accIndex=1,markerIndex=1;
			int i;String chVal="";*/
			HashMap<String,Object> markerAllelesWF= new HashMap<String,Object>();
			//try {
				FileWriter datastreamWF = new FileWriter(generatedFileWF);
				BufferedWriter SNPMatrixWF = new BufferedWriter(datastreamWF);
				SNPMatrixWF.write("\t");
				for(int m1 = 0; m1< listOfMarkersSelected.size(); m1++){
					SNPMatrixWF.write("\t"+listOfMarkersSelected.get(m1));
				}
				
				//int k=0;
				gid=0;
				gname="";		
				
				for (int j=0;j<listOfGIDsSelected.size();j++){ 
					Iterator iterator = hashMapOfGIDsAndGNamesSelected.keySet().iterator();
					String arrList6[];
					gid=Integer.parseInt(listOfGIDsSelected.get(j).toString());
					 while (iterator.hasNext()){
		        	   Object key = iterator.next();
		        	   if(key.equals(gid)){
		        		   gname=hashMapOfGIDsAndGNamesSelected.get(key).toString();
		        	   }
					 }
					SNPMatrixWF.write("\n"+listOfGIDsSelected.get(j)+"\t"+gname);		
				    for (int k=0;k<listOfMarkersSelected.size();k++){
				    	////System.out.println("**************************  :"+dataMap.get(Integer.parseInt(accList.get(j).toString())));
				    	markerAllelesWF=(HashMap)listAlleleValueElementsForGIDsSelected.get(Integer.parseInt(listOfGIDsSelected.get(j).toString()));
				    	List markerKey = new ArrayList();
						markerKey.addAll(markerAllelesWF.keySet());
						//for(int m=0; m<markerKey.size();m++){
							//markerAlleles.
							if(markerAllelesWF.containsKey(listOfGIDsSelected.get(j).toString()+"!~!"+listOfMarkersSelected.get(k).toString())){
					    		SNPMatrixWF.write("\t"+markerAllelesWF.get(listOfGIDsSelected.get(j).toString()+"!~!"+listOfMarkersSelected.get(k).toString()));
					    		
					    	}else{
					    		SNPMatrixWF.write("\t");	
					    	}	
							
						//}
						
				    }		    	
				}					
				SNPMatrixWF.close();	
				

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} 
		return generatedFile;
	}
	
	
	public List<File> MatrixTxt(
			GDMSMain theMainHomePage,
			ArrayList<Integer> listOfGIDsSelected,
			ArrayList<String> listOfMarkersSelected,
			HashMap<Integer, String> hashMapOfGIDsAndGNamesSelected,
			ArrayList<AllelicValueElement> listAlleleValueElementsForGIDsSelected) throws GDMSException {

		PdfFileBuilder pdfFileBuilder = new PdfFileBuilder();
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "\\" + strFileName + ".txt");

		try {
			pdfFileBuilder.initTempFile();
			pdfFileBuilder.setVisibleColumnsLength(listOfMarkersSelected.size());
			pdfFileBuilder.resetContent();
		} catch (IOException e1) {
		}

		try {
			
			FileWriter ssrDatastream = new FileWriter(generatedFile);
			BufferedWriter bw = new BufferedWriter(ssrDatastream);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			bw.write("\t");
			bw.write("\t");
			pdfFileBuilder.buildCell("");
			pdfFileBuilder.buildCell("");
			//Writing the Markers from Column-2 onwards in the first row
			for (int j = 0; j < listOfMarkersSelected.size(); j++){
				String strMarker = listOfMarkersSelected.get(j);
				bw.write("\t");
				bw.write(strMarker);
				pdfFileBuilder.buildCell(strMarker);
			}
			
			for (int i = 0; i < listOfGIDsSelected.size(); i++){
				bw.write("\n");
			Integer iGID = listOfGIDsSelected.get(i);
			bw.write(String.valueOf(iGID));

			String strGName = hashMapOfGIDsAndGNamesSelected.get(iGID);
			bw.write("\t");
			bw.write(strGName);
			pdfFileBuilder.buildCell(strGName);
			//Next writing the AlleleValues for the Markers
			for (int k = 0; k < listAlleleValueElementsForGIDsSelected.size(); k++){

				AllelicValueElement allelicValueElement = listAlleleValueElementsForGIDsSelected.get(k);

				Integer gid = allelicValueElement.getGid();
				String markerName = allelicValueElement.getMarkerName();
				String strData = allelicValueElement.getData();

				if (listOfMarkersSelected.contains(markerName)){

					if (listOfGIDsSelected.contains(gid)){
						bw.write("\t");
						bw.write(strData);
						pdfFileBuilder.buildCell(strData);
					}
				}
			}
		}


			bw.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} 
		List<File> listOfFiles = new ArrayList<File>();
		listOfFiles.add(generatedFile);
		if(listOfMarkersSelected.size() < 20) {
			pdfFileBuilder.writeToFile();
			File file = pdfFileBuilder.file;
			listOfFiles.add(file);
		}
		return listOfFiles;
	}
	
	

	public File MatrixTextFileDataSSRDataset(
			GDMSMain theMainHomePage, ArrayList<AllelicValueWithMarkerIdElement> listOfAllelicValue,
			ArrayList<Integer> listOfNIDs, ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers,
			HashMap<Integer, String> hmOfNIDAndNVal, HashMap<Integer, String> hmOfMIdAndMarkerName) throws GDMSException {
		FileBuilder pdfFileBuilder = new PdfFileBuilder();
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		 String folderName="AnalysisFiles";
		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
        pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
        //System.out.println("pathWB=:"+pathWB);
        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
        if(!new File(pathWB+"/"+folderName).exists())
	   		new File(pathWB+"/"+folderName).mkdir();
        
	
		
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		//String strFilePath = fileExport.getAbsolutePath();
		/*//System.out.println("file path=:"+strFilePath);
		//System.out.println("**************:"+listOfNIDs);
		//System.out.println("%%%%%%%%%%%%%%%:"+hmOfNIDAndNVal);*/
		File generatedFile = new File(strFilePath + "\\" + strFileName + ".txt");

		int iNumOfCols = listOfAllMarkers.size() + 2;
		int iNumOfRows = listOfNIDs.size() + 1;
		try {
			FileWriter ssrDatastream = new FileWriter(generatedFile);
			BufferedWriter ssrMatrix = new BufferedWriter(ssrDatastream);


			String[][] strArrayOfData = new String[iNumOfRows][iNumOfCols];

			int iRow = 1;
			/*//System.out.println("listOfNIDs=:"+listOfNIDs);
			//System.out.println("hmOfNIDAndNVal:"+hmOfNIDAndNVal);*/
			HashMap<Integer, Integer> hmOfGIDAndIndex = new HashMap<Integer, Integer>(); 
			for (int i = 0; i < listOfNIDs.size(); i++){
				Integer iGID = listOfNIDs.get(i);
				strArrayOfData[iRow][0] = String.valueOf(iGID);
				String strNVal = hmOfNIDAndNVal.get(iGID);
				strArrayOfData[iRow][1] = strNVal;
				hmOfGIDAndIndex.put(iGID, iRow);
				iRow += 1;
			}
			////System.out.println("...........:"+hmOfGIDAndIndex);
			int iCol = 2;
			HashMap<String, Integer> hmOfMarkerNameAndIndex = new HashMap<String, Integer>();
			for (int j = 0; j < listOfAllMarkers.size(); j++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(j);
				String markerName = marker.getMarkerName();
				strArrayOfData[0][iCol] = markerName;
				hmOfMarkerNameAndIndex.put(markerName, iCol);
				iCol += 1;
			}
			////System.out.println("--------------------:"+hmOfMarkerNameAndIndex);
			for (int i = 0; i < listOfAllelicValue.size(); i++){
				AllelicValueWithMarkerIdElement allelicValueWithMarkerIdElement = listOfAllelicValue.get(i);
				Integer gid = allelicValueWithMarkerIdElement.getGid();
				String data = allelicValueWithMarkerIdElement.getData();
				Integer markerId = allelicValueWithMarkerIdElement.getMarkerId();
				String strMarkerName = hmOfMIdAndMarkerName.get(markerId);

				Integer iRowIndex = 0;
				Integer iColIndex = 0;
				////System.out.println("gid=:"+gid);
				////System.out.println(gid+"  ---  "+ hmOfNIDAndNVal.get(gid) + " --- " +markerId+"  ---  "+strMarkerName + " --- " + data);
				if (hmOfGIDAndIndex.containsKey(gid)){
					iRowIndex = hmOfGIDAndIndex.get(gid);
					if (hmOfMarkerNameAndIndex.containsKey(strMarkerName)){
						iColIndex = hmOfMarkerNameAndIndex.get(strMarkerName);
					}
					
					if (null == data){
						data = "0/0";
					}
					strArrayOfData[iRowIndex][iColIndex] = data;
				}
			}
			pdfFileBuilder.initTempFile();
			pdfFileBuilder.setVisibleColumnsLength(iNumOfCols);
			pdfFileBuilder.resetContent();
			////System.out.println("iNumOfCols=:"+iNumOfCols);
			//if(iNumOfCols < 20) {
				for (int i = 0; i < iNumOfRows; i++){
					for (int j = 0; j < iNumOfCols; j++){
						String strData = strArrayOfData[i][j];
						////System.out.println(strData);
						
						if ("0/0" == strData){
							////System.out.println("into condition 0/0");
							strData = " ";
						}
						ssrMatrix.write(strData + "\t");
						//pdfFileBuilder.buildCell(strData);
					}
					ssrMatrix.write("\n");
				}
			//}

			ssrMatrix.close();	
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		//* writing the file to IBWS path
		
		File generatedFileIBWS = new File(pathWB+"/"+folderName + "/" + strFileName + ".txt");

		int iNumOfColsI = listOfAllMarkers.size() + 2;
		int iNumOfRowsI = listOfNIDs.size() + 1;
		try {
			FileWriter ssrDatastream = new FileWriter(generatedFileIBWS);
			BufferedWriter ssrMatrix = new BufferedWriter(ssrDatastream);


			String[][] strArrayOfData = new String[iNumOfRowsI][iNumOfColsI];

			int iRow = 1;
			/*//System.out.println("listOfNIDs=:"+listOfNIDs);
			//System.out.println("hmOfNIDAndNVal:"+hmOfNIDAndNVal);*/
			HashMap<Integer, Integer> hmOfGIDAndIndex = new HashMap<Integer, Integer>(); 
			for (int i = 0; i < listOfNIDs.size(); i++){
				Integer iGID = listOfNIDs.get(i);
				strArrayOfData[iRow][0] = String.valueOf(iGID);
				String strNVal = hmOfNIDAndNVal.get(iGID);
				strArrayOfData[iRow][1] = strNVal;
				hmOfGIDAndIndex.put(iGID, iRow);
				iRow += 1;
			}
			////System.out.println("...........:"+hmOfGIDAndIndex);
			int iCol = 2;
			HashMap<String, Integer> hmOfMarkerNameAndIndex = new HashMap<String, Integer>();
			for (int j = 0; j < listOfAllMarkers.size(); j++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(j);
				String markerName = marker.getMarkerName();
				strArrayOfData[0][iCol] = markerName;
				hmOfMarkerNameAndIndex.put(markerName, iCol);
				iCol += 1;
			}
			////System.out.println("--------------------:"+hmOfMarkerNameAndIndex);
			for (int i = 0; i < listOfAllelicValue.size(); i++){
				AllelicValueWithMarkerIdElement allelicValueWithMarkerIdElement = listOfAllelicValue.get(i);
				Integer gid = allelicValueWithMarkerIdElement.getGid();
				String data = allelicValueWithMarkerIdElement.getData();
				Integer markerId = allelicValueWithMarkerIdElement.getMarkerId();
				String strMarkerName = hmOfMIdAndMarkerName.get(markerId);

				Integer iRowIndex = 0;
				Integer iColIndex = 0;
				////System.out.println("gid=:"+gid);
				////System.out.println(gid+"  ---  "+ hmOfNIDAndNVal.get(gid) + " --- " +markerId+"  ---  "+strMarkerName + " --- " + data);
				if (hmOfGIDAndIndex.containsKey(gid)){
					iRowIndex = hmOfGIDAndIndex.get(gid);
					if (hmOfMarkerNameAndIndex.containsKey(strMarkerName)){
						iColIndex = hmOfMarkerNameAndIndex.get(strMarkerName);
					}
					
					if (null == data){
						data = "0/0";
					}
					strArrayOfData[iRowIndex][iColIndex] = data;
				}
			}
			
				for (int i = 0; i < iNumOfRowsI; i++){
					for (int j = 0; j < iNumOfColsI; j++){
						String strData = strArrayOfData[i][j];
						////System.out.println(strData);
						
						if ("0/0" == strData){
							////System.out.println("into condition 0/0");
							strData = " ";
						}
						ssrMatrix.write(strData + "\t");
						//pdfFileBuilder.buildCell(strData);
					}
					ssrMatrix.write("\n");
				}
			//}

			ssrMatrix.close();	
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/*List<File> listOfFiles = new ArrayList<File>();
		listOfFiles.add(generatedFile);
		if(iNumOfCols < 20) {
			pdfFileBuilder.writeToFile();
			File file = pdfFileBuilder.file;
			listOfFiles.add(file);
		}*/
		return generatedFile;
	}

	public File exportMap(
			GDMSMain theMainHomePage, 
			List<String[]> listToExport, String theFileName) throws WriteException, IOException {

		long time = new Date().getTime();
		
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "\\" + strFileName + ".xls");

		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			for (int i = 0; i < listToExport.size(); i++){
				String[] strings = listToExport.get(i);
				for (int j = 0; j < strings.length; j++) {
					if(null == strings[j]) {
						Label lGID = new Label(j, i,  "");
						sheet.addCell(lGID);
						continue;
					} 
					if(strings[j].startsWith("http://")) {
						//Formula f = new Formula(j, i, "HYPERLINK("+ strings[j] + "," + strings[j]);
						sheet.addHyperlink(new WritableHyperlink(j, i, new URL(strings[j])));
					} else {
						Label lGID = new Label(j, i,  strings[j]);
						sheet.addCell(lGID);
					}
				}

			}

			workbook.write();			 
			workbook.close();	

			FileResource fileResource = new FileResource(generatedFile, theMainHomePage);
			theMainHomePage.getMainWindow().getWindow().open(fileResource, "_self");

		} catch (IOException e) {
			throw e;
		} catch (WriteException e) {
			throw e;
		} 

		// TODO Auto-generated method stub
		return generatedFile;
	}

	public File MatrixForSSRDataset(GDMSMain theMainHomePage,
			ArrayList<AllelicValueWithMarkerIdElement> listOfAllelicValue,
			ArrayList<Integer> listOfNIDs, ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers,
			HashMap<Integer, String> hmOfNIDAndNVal,
			HashMap<Integer, String> hmOfMIdAndMarkerName) throws GDMSException {

		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		
		 pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	        //System.out.println("pathWB=:"+pathWB);
	        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
	        if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
	        
		
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "\\" + strFileName + ".xls");

		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			HashMap<Integer, Integer> hashMapOfGIDsAndRowIndex = new HashMap<Integer, Integer>();
			for (int i = 0; i < listOfNIDs.size(); i++){

				Integer iGID = listOfNIDs.get(i);
				Label lGID = new Label(0, (i+1), iGID + "");
				sheet.addCell(lGID);
				hashMapOfGIDsAndRowIndex.put(iGID, (i+1));

				String strGName = hmOfNIDAndNVal.get(iGID);
				Label lGName = new Label(1, (i+1), strGName + "");
				sheet.addCell(lGName);

			}
			List allMarkers=new ArrayList();
			//Writing the Markers from Column-2 onwards in the first row
			HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
			for (int i = 0; i < listOfAllMarkers.size(); i++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
				String strMarker = marker.getMarkerName(); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);
				hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
				allMarkers.add(strMarker);
			}

			//Next writing the AlleleValues for the Markers
			for (int i = 0; i < listOfAllelicValue.size(); i++){

				AllelicValueWithMarkerIdElement allelicValueElement = listOfAllelicValue.get(i);

				Integer gid = allelicValueElement.getGid();
				Integer markerID = allelicValueElement.getMarkerId();
				String strData = allelicValueElement.getData();

				String strMarkerName = hmOfMIdAndMarkerName.get(markerID);
				////System.out.println(strMarkerName+"   "+listOfAllMarkers);
				if (allMarkers.contains(strMarkerName)){
					////System.out.println(strMarkerName+"   "+listOfAllMarkers);
					Integer colIndex = hashMapOfMakerNamesAndColIndex.get(strMarkerName);
					int iColIndex = colIndex.intValue();

					int iGIDRowIndex = 0;
					if (listOfNIDs.contains(gid)){
						Integer integer = hashMapOfGIDsAndRowIndex.get(gid);
						iGIDRowIndex = integer.intValue();
						/*if(strData=="0/0")
							strData=" ";*/
						Label lGName = new Label((iColIndex), iGIDRowIndex, strData + "");
						sheet.addCell(lGName);
						////System.out.println(gid + " --- " + strMarkerName + " --- " + strData);
					}
				}
			}

			workbook.write();			 
			workbook.close();	
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
			
			//String strFilePath = fileExport.getAbsolutePath();
			File generatedFileIBWS = new File(pathWB+"/"+folderName + "\\" + strFileName + ".xls");

			try {

				WritableWorkbook workbookIBWS = Workbook.createWorkbook(generatedFileIBWS);
				WritableSheet sheetIBWS = workbookIBWS.createSheet("DataSheet",0);

				//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
				HashMap<Integer, Integer> hashMapOfGIDsAndRowIndex = new HashMap<Integer, Integer>();
				for (int i = 0; i < listOfNIDs.size(); i++){

					Integer iGID = listOfNIDs.get(i);
					Label lGID = new Label(0, (i+1), iGID + "");
					sheetIBWS.addCell(lGID);
					hashMapOfGIDsAndRowIndex.put(iGID, (i+1));

					String strGName = hmOfNIDAndNVal.get(iGID);
					Label lGName = new Label(1, (i+1), strGName + "");
					sheetIBWS.addCell(lGName);

				}
				List allMarkers=new ArrayList();
				//Writing the Markers from Column-2 onwards in the first row
				HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
				for (int i = 0; i < listOfAllMarkers.size(); i++){
					MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
					String strMarker = marker.getMarkerName(); 
					Label lMarkerName = new Label((i+2), 0, strMarker + "");
					sheetIBWS.addCell(lMarkerName);
					hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
					allMarkers.add(strMarker);
				}

				//Next writing the AlleleValues for the Markers
				for (int i = 0; i < listOfAllelicValue.size(); i++){

					AllelicValueWithMarkerIdElement allelicValueElement = listOfAllelicValue.get(i);

					Integer gid = allelicValueElement.getGid();
					Integer markerID = allelicValueElement.getMarkerId();
					String strData = allelicValueElement.getData();

					String strMarkerName = hmOfMIdAndMarkerName.get(markerID);
					////System.out.println(strMarkerName+"   "+listOfAllMarkers);
					if (allMarkers.contains(strMarkerName)){
						////System.out.println(strMarkerName+"   "+listOfAllMarkers);
						Integer colIndex = hashMapOfMakerNamesAndColIndex.get(strMarkerName);
						int iColIndex = colIndex.intValue();

						int iGIDRowIndex = 0;
						if (listOfNIDs.contains(gid)){
							Integer integer = hashMapOfGIDsAndRowIndex.get(gid);
							iGIDRowIndex = integer.intValue();
							/*if(strData=="0/0")
								strData=" ";*/
							Label lGName = new Label((iColIndex), iGIDRowIndex, strData + "");
							sheetIBWS.addCell(lGName);
							////System.out.println(gid + " --- " + strMarkerName + " --- " + strData);
						}
					}
				}

				workbookIBWS.write();			 
				workbookIBWS.close();	
			
			

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		return generatedFile;
	}

	public File MatrixForDArtDataset(GDMSMain theMainHomePage,
			ArrayList<AllelicValueElement> listOfAllelicValueElements,
			ArrayList<Integer> listOfNIDs, ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers,
			HashMap<Integer, String> hmOfNIDAndNVal) throws GDMSException {
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "\\" + strFileName + ".xls");

		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			HashMap<Integer, Integer> hashMapOfGIDsAndRowIndex = new HashMap<Integer, Integer>();
			for (int i = 0; i < listOfNIDs.size(); i++){

				Integer iGID = listOfNIDs.get(i);
				Label lGID = new Label(0, (i+1), iGID + "");
				sheet.addCell(lGID);
				hashMapOfGIDsAndRowIndex.put(iGID, (i+1));

				String strGName = hmOfNIDAndNVal.get(iGID);
				Label lGName = new Label(1, (i+1), strGName + "");
				sheet.addCell(lGName);

			}

			//Writing the Markers from Column-2 onwards in the first row
			HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
			for (int i = 0; i < listOfAllMarkers.size(); i++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
				String strMarker = marker.getMarkerName(); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);
				hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
			}

			//Next writing the AlleleValues for the Markers
			for (int i = 0; i < listOfAllelicValueElements.size(); i++){
				AllelicValueElement allelicValueElement = listOfAllelicValueElements.get(i);
				Integer gid = allelicValueElement.getGid();
				String strMarkerName = allelicValueElement.getMarkerName();
				String strData = allelicValueElement.getData();
				if (hashMapOfMakerNamesAndColIndex.containsKey(strMarkerName)) {

					Integer colIndex = hashMapOfMakerNamesAndColIndex.get(strMarkerName);
					int iColIndex = colIndex.intValue();

					int iGIDRowIndex = 0;
					if (listOfNIDs.contains(gid)){
						Integer integer = hashMapOfGIDsAndRowIndex.get(gid);
						iGIDRowIndex = integer.intValue();

						Label lGName = new Label((iColIndex), iGIDRowIndex, strData + "");
						sheet.addCell(lGName);
					}
				}
			}
			workbook.write();			 
			workbook.close();	
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		return generatedFile;
	}

	public File MatrixForSNPDataset(
			GDMSMain theMainHomePage,
			ArrayList<AllelicValueWithMarkerIdElement> listOfAllelicValueWithMarkerIdElements,
			ArrayList<Integer> listOfNIDs, ArrayList<String> listofMarkerNamesForSNP,
			HashMap<Integer, String> hmOfNIDAndNVal,
			HashMap<Integer, String> hmOfMIdAndMarkerName) throws GDMSException {
		long time = new Date().getTime();
		
		////System.out.println("listofMarkerNamesForSNP=:"+listofMarkerNamesForSNP);
		
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		 pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	        //System.out.println("pathWB=:"+pathWB);
	        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
	        if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
	        
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "\\" + strFileName + ".xls");

		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			HashMap<Integer, Integer> hashMapOfGIDsAndRowIndex = new HashMap<Integer, Integer>();
			for (int i = 0; i < listOfNIDs.size(); i++){

				Integer iGID = listOfNIDs.get(i);
				Label lGID = new Label(0, (i+1), iGID + "");
				sheet.addCell(lGID);
				hashMapOfGIDsAndRowIndex.put(iGID, (i+1));

				String strGName = hmOfNIDAndNVal.get(iGID);
				Label lGName = new Label(1, (i+1), strGName + "");
				sheet.addCell(lGName);

			}
			
			//Writing the Markers from Column-2 onwards in the first row
			HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
			for (int i = 0; i < listofMarkerNamesForSNP.size(); i++){
				String strMarker = listofMarkerNamesForSNP.get(i); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);
				hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
			}

			//Next writing the AlleleValues for the Markers
			for (int i = 0; i < listOfAllelicValueWithMarkerIdElements.size(); i++){

				AllelicValueWithMarkerIdElement allelicValueElement = listOfAllelicValueWithMarkerIdElements.get(i);

				Integer gid = allelicValueElement.getGid();
				Integer markerId = allelicValueElement.getMarkerId();
				String strData = allelicValueElement.getData();

				if (hmOfMIdAndMarkerName.containsKey(markerId)){
					String strMarkerName = hmOfMIdAndMarkerName.get(markerId);

					if (hashMapOfMakerNamesAndColIndex.containsKey(strMarkerName)) {

						Integer colIndex = hashMapOfMakerNamesAndColIndex.get(strMarkerName);
						int iColIndex = colIndex.intValue();

						int iGIDRowIndex = 0;
						if (listOfNIDs.contains(gid)){
							Integer integer = hashMapOfGIDsAndRowIndex.get(gid);
							iGIDRowIndex = integer.intValue();

							Label lGName = new Label((iColIndex), iGIDRowIndex, strData + "");
							sheet.addCell(lGName);

							////System.out.println(gid + " --- " + strMarkerName + " --- " + strData);
						}


					}
				}

			}

			workbook.write();			 
			workbook.close();	

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		File generatedFileIBWS = new File(pathWB+"/"+folderName + "\\" + strFileName + ".xls");

		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFileIBWS);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			HashMap<Integer, Integer> hashMapOfGIDsAndRowIndex = new HashMap<Integer, Integer>();
			for (int i = 0; i < listOfNIDs.size(); i++){

				Integer iGID = listOfNIDs.get(i);
				Label lGID = new Label(0, (i+1), iGID + "");
				sheet.addCell(lGID);
				hashMapOfGIDsAndRowIndex.put(iGID, (i+1));

				String strGName = hmOfNIDAndNVal.get(iGID);
				Label lGName = new Label(1, (i+1), strGName + "");
				sheet.addCell(lGName);

			}
			
			//Writing the Markers from Column-2 onwards in the first row
			HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
			for (int i = 0; i < listofMarkerNamesForSNP.size(); i++){
				String strMarker = listofMarkerNamesForSNP.get(i); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);
				hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
			}

			//Next writing the AlleleValues for the Markers
			for (int i = 0; i < listOfAllelicValueWithMarkerIdElements.size(); i++){

				AllelicValueWithMarkerIdElement allelicValueElement = listOfAllelicValueWithMarkerIdElements.get(i);

				Integer gid = allelicValueElement.getGid();
				Integer markerId = allelicValueElement.getMarkerId();
				String strData = allelicValueElement.getData();

				if (hmOfMIdAndMarkerName.containsKey(markerId)){
					String strMarkerName = hmOfMIdAndMarkerName.get(markerId);

					if (hashMapOfMakerNamesAndColIndex.containsKey(strMarkerName)) {

						Integer colIndex = hashMapOfMakerNamesAndColIndex.get(strMarkerName);
						int iColIndex = colIndex.intValue();

						int iGIDRowIndex = 0;
						if (listOfNIDs.contains(gid)){
							Integer integer = hashMapOfGIDsAndRowIndex.get(gid);
							iGIDRowIndex = integer.intValue();

							Label lGName = new Label((iColIndex), iGIDRowIndex, strData + "");
							sheet.addCell(lGName);

							////System.out.println(gid + " --- " + strMarkerName + " --- " + strData);
						}


					}
				}

			}

			workbook.write();			 
			workbook.close();	

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		
		
		
		return generatedFile;
	}

	public File MatrixForMappingDataset(
			GDMSMain theMainHomePage,
			ArrayList a,
			String listOfParentGIDs,ArrayList<Integer> accList,
			ArrayList<String> markList,
			TreeMap<Integer, String> hmOfNIDAndNVal,
			HashMap<Integer, String> hmOfMIdAndMarkerName) throws GDMSException {
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();	
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "\\" + strFileName + ".xls");
		int columns=2;
		int row=0;
		String MarkernameId="";
		String previousMarkerId="";			
		//String MarkerIdNameList="";
		String allele2="";
		
		String markerId="";
		//System.out.println("a=:"+a);
		//System.out.println("markList:"+markList);
		//System.out.println("accList=:"+accList);
		try{
			////System.out.println("****************  EXPORT FORMATS CLASS  *****************");					
			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);			
			
			int accIndex=1,markerIndex=2;
			int PmarkerIndex=2;
			int i;			
			
			int gid=0;
			String gname="";
			////System.out.println(".........parents=:"+parentsList);
			////System.out.println(" data list=:"+a);
			////System.out.println("markList=:"+markList);
			int noOfAccs=accList.size();
			int noOfMarkers=markList.size();
			
			Label l=null;			
			
			for(i=0;i<noOfMarkers;i++){					
				l=new Label(markerIndex++,0,(String)markList.get(i));
				sheet.addCell(l);				
			}	
			
			
//			 To write accessions
			for(i=0;i<noOfAccs;i++){
				Iterator iterator = hmOfNIDAndNVal.keySet().iterator();
				gid=Integer.parseInt(accList.get(i).toString());
				while (iterator.hasNext()){
	        	   Object key = iterator.next();
	        	   if(key.equals(gid)){
	        		   gname=hmOfNIDAndNVal.get(key).toString();
	        	   }
				 }	
				l=new Label(0,accIndex++,gid+"");
				sheet.addCell(l);
				accIndex--;
				l=new Label(1,accIndex++,gname);
				sheet.addCell(l);
			}
					
			//MarkerIdNameList=MarkerNameIdList(markList);
			row=1;
			String[] AllelesList=null;
			for(int a1=0;a1<a.size();a1++){
				AllelesList=a.get(a1).toString().split("!~!");
				////System.out.println(AllelesList[1].toString()+ "="+previousMarkerId+", ");
				
				if((AllelesList[1].toString().toLowerCase()).equals(previousMarkerId.toString().toLowerCase())){
					row++;
					////System.out.println(AllelesList[1].toString()+ "="+previousMarkerId);
				}else{
					int totalRows=sheet.getRows();
					for(int ss=0;ss<totalRows;ss++){
						if(sheet.getCell(0,ss).getContents().equals(AllelesList[0].toString())){
							row=ss;
							break;
						}
					}
				}
				
				MarkernameId=hmOfMIdAndMarkerName.get(Integer.parseInt(AllelesList[1]));
				int totalcols=sheet.getColumns();
				////System.out.println(MarkernameId);
				////System.out.println("totalcols:"+totalcols);
				for(int ss=2;ss<totalcols;ss++){
					////System.out.println(".......      "+sheet.getCell(ss,0).getContents()+"           .................:"+MarkernameId);
					if(sheet.getCell(ss,0).getContents().equals(MarkernameId)){
						////System.out.println("inside if sheet");
						columns=ss;
						break;
					}
				}	
				String[] allele1=null;
				////System.out.println(AllelesList[0]+"      "+AllelesList[1]+"   "+AllelesList[2]);				
				if(AllelesList.length<3)
					allele2="-";
				else
					allele2=AllelesList[2];
				String allele="";
				if(allele2.contains(":")){
					allele1=allele2.split(":");
					if(allele1[0].equalsIgnoreCase(allele1[1])){
						allele=allele1[0];
					}else{
						allele=allele1[0]+"/"+allele1[1];
					}
						
				}else if(allele2.contains(",")){					
					allele1=allele.split(",");					
					if(allele1[0].equalsIgnoreCase(allele1[1])){						
						allele=allele1[0];
					}else{
						allele=allele1[0]+"/"+allele1[1];
					}
				}else{
					allele=allele2;
				}
				////System.out.println(columns+","+row);
				l=new Label(columns,row,allele+"");
				sheet.addCell(l);
				columns++;
					
				previousMarkerId=AllelesList[1].toString();	
				
			}
			
			workbook.write();			 
			workbook.close();		
			
		
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		
		/*try{
			bPath="C:\\IBWorkflowSystem\\infrastructure\\tomcat\\webapps\\GDMS";
		    opPath=bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1);
		       
		        ////System.out.println(",,,,,,,,,,,,,  :"+bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1));
		   
			dbNameL=GDMSModel.getGDMSModel().getLocalParams().getDbName();
			IBWFProjects= new HashMap<Object, String>();
	        List<Project> projects = workbenchDataManager.getProjects();
	        Long projectId = Long.valueOf(0);
	        ////System.out.println("testGetProjects(): ");
	        for (Project project : projects) {
	            ////System.out.println("  " + project.getLocalDbName());
	            projectId = project.getProjectId();
	            //IBWFProjects.put(project.getLocalDbName(),project.getProjectId()+"-"+project.getProjectName());
	            IBWFProjects.put(project.getLocalDbName(),project.getProjectId().toString());
	        }
	        //System.out.println(".........:"+IBWFProjects.get(dbNameL));
	        
		}catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
        pathWB=opPath+"/IBWorkflowSystem/workspace/"+IBWFProjects.get(dbNameL)+"/gdms/output";
        //System.out.println("pathWB=:"+pathWB);
        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
        if(!new File(pathWB+"/"+folderName).exists())
	   		new File(pathWB+"/"+folderName).mkdir();
		*/
		 pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	        //System.out.println("pathWB=:"+pathWB);
	        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
	        if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
	        
		
     
      		File generatedFileIBWS = new File(pathWB+"/"+folderName + "\\" + strFileName + ".xls");
      		columns=2;
      		row=0;
      		 MarkernameId="";
      		 previousMarkerId="";			
      		
      		allele2="";
      		
      		markerId="";
      		
      		try{
      			////System.out.println("****************  EXPORT FORMATS CLASS  *****************");					
      			WritableWorkbook workbook = Workbook.createWorkbook(generatedFileIBWS);
      			WritableSheet sheet = workbook.createSheet("DataSheet",0);			
      			
      			int accIndex=1,markerIndex=2;
      			int PmarkerIndex=2;
      			int i;			
      			
      			int gid=0;
      			String gname="";
      			////System.out.println(".........parents=:"+parentsList);
      			////System.out.println(" data list=:"+a);
      			////System.out.println("markList=:"+markList);
      			int noOfAccs=accList.size();
      			int noOfMarkers=markList.size();
      			
      			Label l=null;			
      			
      			for(i=0;i<noOfMarkers;i++){					
      				l=new Label(markerIndex++,0,(String)markList.get(i));
      				sheet.addCell(l);				
      			}	
      			
      			
//      			 To write accessions
      			for(i=0;i<noOfAccs;i++){
      				Iterator iterator = hmOfNIDAndNVal.keySet().iterator();
      				gid=Integer.parseInt(accList.get(i).toString());
      				while (iterator.hasNext()){
      	        	   Object key = iterator.next();
      	        	   if(key.equals(gid)){
      	        		   gname=hmOfNIDAndNVal.get(key).toString();
      	        	   }
      				 }	
      				l=new Label(0,accIndex++,gid+"");
      				sheet.addCell(l);
      				accIndex--;
      				l=new Label(1,accIndex++,gname);
      				sheet.addCell(l);
      			}
      					
      			//MarkerIdNameList=MarkerNameIdList(markList);
      			row=1;
      			String[] AllelesList=null;
      			for(int a1=0;a1<a.size();a1++){
      				AllelesList=a.get(a1).toString().split("!~!");
      				////System.out.println(AllelesList[1].toString()+ "="+previousMarkerId+", ");
      				
      				if((AllelesList[1].toString().toLowerCase()).equals(previousMarkerId.toString().toLowerCase())){
      					row++;
      					////System.out.println(AllelesList[1].toString()+ "="+previousMarkerId);
      				}else{
      					int totalRows=sheet.getRows();
      					for(int ss=0;ss<totalRows;ss++){
      						if(sheet.getCell(0,ss).getContents().equals(AllelesList[0].toString())){
      							row=ss;
      							break;
      						}
      					}
      				}
      				
      				MarkernameId=hmOfMIdAndMarkerName.get(Integer.parseInt(AllelesList[1]));
      				int totalcols=sheet.getColumns();
      				////System.out.println(MarkernameId);
      				////System.out.println("totalcols:"+totalcols);
      				for(int ss=2;ss<totalcols;ss++){
      					////System.out.println(".......      "+sheet.getCell(ss,0).getContents()+"           .................:"+MarkernameId);
      					if(sheet.getCell(ss,0).getContents().equals(MarkernameId)){
      						////System.out.println("inside if sheet");
      						columns=ss;
      						break;
      					}
      				}	
      				String[] allele1=null;
      				////System.out.println(AllelesList[0]+"      "+AllelesList[1]+"   "+AllelesList[2]);				
      				if(AllelesList.length<3)
      					allele2="-";
      				else
      					allele2=AllelesList[2];
      				String allele="";
      				if(allele2.contains(":")){
      					allele1=allele2.split(":");
      					if(allele1[0].equalsIgnoreCase(allele1[1])){
      						allele=allele1[0];
      					}else{
      						allele=allele1[0]+"/"+allele1[1];
      					}
      						
      				}else if(allele2.contains(",")){					
      					allele1=allele.split(",");					
      					if(allele1[0].equalsIgnoreCase(allele1[1])){						
      						allele=allele1[0];
      					}else{
      						allele=allele1[0]+"/"+allele1[1];
      					}
      				}else{
      					allele=allele2;
      				}
      				////System.out.println(columns+","+row);
      				l=new Label(columns,row,allele+"");
      				sheet.addCell(l);
      				columns++;
      					
      				previousMarkerId=AllelesList[1].toString();	
      				
      			}
      			
      			workbook.write();			 
      			workbook.close();		
      			
      		
      		} catch (IOException e) {
      			throw new GDMSException(e.getMessage());
      		} catch (RowsExceededException e) {
      			throw new GDMSException(e.getMessage());
      		} catch (WriteException e) {
      			throw new GDMSException(e.getMessage());
      		}
      		
		
		
		
		return generatedFile;
	}
	/*public String MarkerNameIdList(ArrayList markList){		
		String MarkerIdNameList="";
		for(int i=0;i<markList.size();i++){
			MarkerIdNameList=MarkerIdNameList+markList.get(i)+"!&&!";
		}
		////System.out.println("MarkerIdNameList=:"+MarkerIdNameList);
		return MarkerIdNameList;
	}*/

	public void exportToPdf(Table table, GDMSMain theMainHomePage) {
		PdfExporter pdfExporter = new PdfExporter();
		File exportToPDF = pdfExporter.exportToPDF(table);
		FileResource fileResource = new FileResource(exportToPDF, theMainHomePage);
		theMainHomePage.getMainWindow().getWindow().open(fileResource, "_blank");
	}

	public void exportToPdf(List<String[]> theData, GDMSMain theMainHomePage) {
		PdfExporter pdfExporter = new PdfExporter();
		if(null == theData || 0 == theData.size()) {
			return;
		}
		Table table = new Table();
		
		for (int i = 0; i < theData.get(0).length; i++){
			table.addContainerProperty(theData.get(0)[i], String.class, null);
		}
		for (int i = 1; i < theData.get(0).length; i++){
			table.addItem(theData.get(i), new Integer(i - 1));
		}
		File exportToPDF = pdfExporter.exportToPDF(table);
		FileResource fileResource = new FileResource(exportToPDF, theMainHomePage);
		theMainHomePage.getMainWindow().getWindow().open(fileResource, "_blank");
	}
	public File exportToKBio(ArrayList markersList, GDMSMain _mainHomePage) {
		// 20131512 : Kalyani added to create kbio order form
		
		String folderName="K-bioOrderForms";		
		WebApplicationContext ctx = (WebApplicationContext) _mainHomePage.getContext();
        String strTemplateFolderPath = ctx.getHttpSession().getServletContext().getRealPath("\\VAADIN\\themes\\gdmstheme\\Templates");
        ////System.out.println("Folder-Path: " + strTemplateFolderPath);
		
      //String strMarkerType = _strMarkerType.replace(" ", "");
		String strSrcFileName = strTemplateFolderPath+"\\snp_template.xls";
		
		
		long time = new Date().getTime();
		//String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String strFilePath = fileExport.getAbsolutePath()+"\\"+folderName;
		/*bPath="C:\\IBWorkflowSystem\\infrastructure\\tomcat\\webapps\\GDMS";
	    opPath=bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1);
	     */
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		////System.out.println("strFilePath=:"+strFilePath);
		
		String destFileWF=strFilePath+"/KBio"+String.valueOf(time)+".xls";
		File generatedFile = new File(destFileWF);
		
		
		 pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	        //System.out.println("pathWB=:"+pathWB);
	        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
	        if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
	        
			
	        String strFilePathIBWS=pathWB+"/"+folderName;
	        String destFileIBWFS=strFilePathIBWS+"/KBio"+String.valueOf(time)+".xls";
			File generatedFileWF = new File(destFileIBWFS);
		
		try{
		
			File strFileLoc = new File(strSrcFileName);
			FileResource fileResource = new FileResource(strFileLoc, _mainHomePage);
			
			InputStream oInStream = new FileInputStream(strSrcFileName);
	        OutputStream oOutStream = new FileOutputStream(destFileWF);
	
	        OutputStream oOutStreamWF = new FileOutputStream(destFileIBWFS);
	        
	        // Transfer bytes from in to out
	        byte[] oBytes = new byte[1024];
	        int nLength;
	        BufferedInputStream oBuffInputStream = 
	                        new BufferedInputStream( oInStream );
	        while ((nLength = oBuffInputStream.read(oBytes)) > 0) 
	        {
	            oOutStream.write(oBytes, 0, nLength);
	        }
	        oInStream.close();
	        oOutStream.close();
	        
	        FileInputStream file = new FileInputStream(generatedFile);
	        
	        HSSFWorkbook workbook = new HSSFWorkbook(file);
	        HSSFSheet sheet = workbook.getSheetAt(1);
	        Row row = null;
	        int rowNum=2;
	        Cell cell = null;
	       
	        for(int m1=0;m1<markersList.size();m1++){
		    	 int colnum = 0;
		    	 row = sheet.getRow(rowNum);	        
		    	 if(row == null){row = sheet.createRow(rowNum);}
	             cell = row.getCell(0);
	             if (cell == null)
		    		 cell = row.createCell(0);
	             cell.setCellValue(cell.getStringCellValue()+markersList.get(m1).toString());
		
		    	 rowNum++;
		     }
	      
	        file.close();
	         
	        FileOutputStream outFile =new FileOutputStream(destFileWF);
	        workbook.write(outFile);
	        outFile.close();
	        
	        
	        
	        
	       
	        
	        FileInputStream fileIBWS = new FileInputStream(destFileWF);
	        
	        HSSFWorkbook workbookIBWS = new HSSFWorkbook(fileIBWS);
	        HSSFSheet sheetIBWS = workbookIBWS.getSheetAt(1);
	        Row rowIBWS = null;
	        int rowNumIBWS=2;
	        Cell cellIBWS = null;
	       
	        for(int m1=0;m1<markersList.size();m1++){
		    	 int colnum = 0;
		    	 rowIBWS = sheetIBWS.getRow(rowNumIBWS);	        
		    	 if(rowIBWS == null){rowIBWS = sheetIBWS.createRow(rowNumIBWS);}
	             cellIBWS = rowIBWS.getCell(0);
	             if (cellIBWS == null)
		    		 cellIBWS = rowIBWS.createCell(0);
	             if(cellIBWS.getStringCellValue()==null)
	            	 cellIBWS.setCellValue(cellIBWS.getStringCellValue()+markersList.get(m1).toString());
		
		    	 rowNumIBWS++;
		     }
	      
	        fileIBWS.close();
	         
	        FileOutputStream outFileIWBS =new FileOutputStream(destFileIBWFS);
	        workbookIBWS.write(outFileIWBS);
	        outFileIWBS.close();
	        
	        
	        
		}catch(Exception e){
			e.printStackTrace();
		}
             
		return generatedFile;
		
		// TODO Auto-generated method stub
	}
	

}
