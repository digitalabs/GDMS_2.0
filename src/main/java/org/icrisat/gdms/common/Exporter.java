package org.icrisat.gdms.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;

import com.vaadin.data.Container;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Table;

public abstract class Exporter implements StreamSource {

	private static final long serialVersionUID = 1L;
	
    protected FileBuilder fileBuilder;
    private Locale locale;
    private String dateFormatString;
    protected String downloadFileName;

	private File file;

    public Exporter() {
    }

    public Exporter(Table table) {
        setTableToBeExported(table);
    	file = fileBuilder.getFile();
    }

    public File exportToPDF(Table table) {
        setTableToBeExported(table);
    	file = fileBuilder.getFile();
    	return file;
    }
    
	public void setTableToBeExported(Table table) {
        setContainerToBeExported(table.getContainerDataSource());
        setVisibleColumns(table.getVisibleColumns());
        setHeader(table.getCaption());
        for (Object column : table.getVisibleColumns()) {
            String header = table.getColumnHeader(column);
            if (header != null) {
                setColumnHeader(column, header);
            }
        }
    }

	public void setContainerToBeExported(Container container) {
		fileBuilder = createFileBuilder(container);
		if (locale != null) {
			fileBuilder.setLocale(locale);
		}
		if (dateFormatString != null) {
			fileBuilder.setDateFormat(dateFormatString);
		}
	}

    public void setVisibleColumns(Object[] visibleColumns) {
        fileBuilder.setVisibleColumns(visibleColumns);
    }

    public void setColumnHeader(Object propertyId, String header) {
        fileBuilder.setColumnHeader(propertyId, header);
    }

    public void setHeader(String header) {
        fileBuilder.setHeader(header);
    }
    
    public void setLocale(Locale locale){
    	this.locale = locale;
    }
    
    public void setDateFormat(String dateFormat){
    	this.dateFormatString = dateFormat;
    }
    protected abstract FileBuilder createFileBuilder(Container container);

    protected abstract String getDownloadFileName();

    public void setDownloadFileName(String fileName){
//    	downloadFileName = fileName;
//    	((StreamResource)fileDownloader.getFileDownloadResource()).setFilename(getDownloadFileName());
    }

	@Override
	public InputStream getStream() {
		 try {
	            return new FileInputStream(fileBuilder.getFile());
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
	        return null;
	}

}
