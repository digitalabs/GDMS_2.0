package org.icrisat.gdms.ui.common;

import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

public class HeadingOne extends Label {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HeadingOne(String caption) {
        super(caption);
        setSizeUndefined();
        setStyleName(Reindeer.LABEL_H1);
    }
	
}