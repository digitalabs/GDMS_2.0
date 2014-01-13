package org.icrisat.gdms.common;

import com.vaadin.data.Container;
import com.vaadin.ui.Table;

public class PdfExporter extends Exporter {
	private static final long serialVersionUID = 1L;

	public PdfExporter() {
        super();
    }

    public PdfExporter(Table table) {
        super(table);
    }

	@Override
    protected FileBuilder createFileBuilder(Container container) {
        return new PdfFileBuilder(container);
    }

    @Override
    protected String getDownloadFileName() {
    	if(downloadFileName == null){
    		return "exported-pdf.pdf";
        }
    	if(downloadFileName.endsWith(".pdf")){
    		return downloadFileName;
    	}else{
    		return downloadFileName + ".pdf";
    	}
    }

    public void setWithBorder(boolean withBorder) {
        ((PdfFileBuilder) fileBuilder).setWithBorder(withBorder);
    }
    
}
