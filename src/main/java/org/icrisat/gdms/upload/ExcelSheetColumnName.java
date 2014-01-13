package org.icrisat.gdms.upload;

public class ExcelSheetColumnName {
	//	this method returns the Excel sheet column name
	public String getColumnName(int noofcell){
		String result = "";
		String[] strAlp = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		
		int ab= noofcell/26;
		int abb = ab * 26;
		if(ab<1){
			result =strAlp[noofcell];				
		}else{
			ab--;			
			int count = 0;
			for(int i=abb;i<noofcell;i++){
				count++;
			}
			result = strAlp[ab]+strAlp[count];			
		}
		return result;
	}
}
