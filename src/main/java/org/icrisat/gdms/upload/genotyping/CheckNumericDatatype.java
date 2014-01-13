package org.icrisat.gdms.upload.genotyping;

/**
 * @author tpraveenreddy
 *
 */

public class CheckNumericDatatype {
//	Check the given value is whether Integer or String
	public boolean isInteger(String strFlt){
		
		boolean result = true;
		try{
	
			Integer.parseInt(strFlt);
			
		}catch(NumberFormatException e){
			result = false;
		}
		return result;
	}
//	Check the given value is whether Float or String
	public boolean isFloat(String strFlt){
			
			boolean result = true;
			try{
				
				Float.parseFloat(strFlt);
								
			}catch(NumberFormatException e){
				result = false;
			}
			return result;
		}
// Check the given value is whether Numeric or not
	public boolean isNumeric(String strNum){
			boolean result = false;
			CheckNumericDatatype utResult = new CheckNumericDatatype();
			result = utResult.isInteger(strNum);
				if(!result){
					result = utResult.isFloat(strNum);
				}
		return result;
	}
	
//	Check the given value is whether String or Numeric 
	public boolean isString(String strFlt){
			
			boolean result = false;
			try{
				
				Integer.parseInt(strFlt);
				
								
			}catch(NumberFormatException e){
				result = true;
			}
			return result;
		}
//	Check the given value is whether Char or String
	public boolean isChar(String strValue){
			
			boolean result = false;
			try{
				
				int chData = strValue.length();
				
				if(chData==1)
					result = true;
				
								
			}catch(NumberFormatException e){
				result = false;
			}
			return result;
		}
}
