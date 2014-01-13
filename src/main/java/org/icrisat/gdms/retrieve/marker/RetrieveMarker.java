package org.icrisat.gdms.retrieve.marker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.MarkerDetailsDAO;
import org.generationcp.middleware.dao.gdms.MarkerUserInfoDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerDetails;
import org.generationcp.middleware.pojos.gdms.MarkerUserInfo;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;


public class RetrieveMarker {
	
	public LinkedList<Marker> retrieveMarker() throws GDMSException{
		
		ArrayList<Marker> listOfMarkers = new ArrayList<Marker>();
		
		LinkedList<Marker> listOfNewMarkers = new LinkedList<Marker>();
		
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		
		try {
			
			listOfMarkers = (ArrayList<Marker>) markerDAO.getAll();
			
			for (Marker marker : listOfMarkers){
				
				Integer markerId = marker.getMarkerId();
				
				if (0 > markerId){
					listOfNewMarkers.addLast(marker);
				}
			}
			
		
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}

		return listOfNewMarkers;
	}
	
	
	public LinkedList<MarkerUserInfo> retrieveMarkerUserInfo() throws GDMSException{
		
		LinkedList<MarkerUserInfo> listOfMarkerUserInfo = new LinkedList<MarkerUserInfo>();
		
		MarkerUserInfoDAO markerUserInfoDAO = new MarkerUserInfoDAO();
		markerUserInfoDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		
		try {
			
			
			List<MarkerUserInfo> listOfAllMarkerUserInfo = markerUserInfoDAO.getAll();
			
			for (MarkerUserInfo markerUserInfo : listOfAllMarkerUserInfo){
				
				listOfMarkerUserInfo.addFirst(markerUserInfo);
			}
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		return listOfMarkerUserInfo;
	}
	
	public LinkedList<MarkerDetails> retrieveMarkerDetails() throws GDMSException {
		
		LinkedList<MarkerDetails> listOfMarkerDetails = new LinkedList<MarkerDetails>();
		
		MarkerDetailsDAO markerDetailsDAO = new MarkerDetailsDAO();
		markerDetailsDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		
		try {
			
			List<MarkerDetails> listOfAllMarkerDetails = markerDetailsDAO.getAll();
			
			for (MarkerDetails markerUserInfo : listOfAllMarkerDetails){
				
				listOfMarkerDetails.addFirst(markerUserInfo);
			}
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		return listOfMarkerDetails;
		
	}
	
	public List<String> retrieveAllMarkerTypes() throws GDMSException {
		
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		
		List<String> listOfAllMarkerTypes;
		
		try {
			
			long countAllMarkerTypes = markerDAO.countAllMarkerTypes();
			
			listOfAllMarkerTypes = markerDAO.getAllMarkerTypes(0, (int)countAllMarkerTypes);
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		return listOfAllMarkerTypes;
	}
	
	
	public List<String> retrieveAllDBAccessionIDs() throws GDMSException {
		
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		
		List<String> listOfAllDBAccessionIDs;
		
		try {
			
			long countAllDBAccessionIDs = markerDAO.countAllDbAccessionIds();
			
			listOfAllDBAccessionIDs = markerDAO.getAllDbAccessionIds(0, (int)countAllDBAccessionIDs);
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		return listOfAllDBAccessionIDs;
	}

	public List<Marker> retrieveMarkers() throws MiddlewareQueryException {
		List<Marker> listOfMarker = new ArrayList<Marker>();
		List<Marker> localMarkers = getLocalMarkers();
		if(null != localMarkers) {
			listOfMarker.addAll(localMarkers);
		}
		List<Marker> centralMarkers = getCentralMarkers();
		if(null != centralMarkers) {
			listOfMarker.addAll(centralMarkers);
		}
		return listOfMarker;
	}


	private List<Marker> getLocalMarkers() throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		List<Marker> all = markerDAO.getAll();
		return all;
	}


	private List<Marker> getCentralMarkers() throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
		List<Marker> all = markerDAO.getAll();
		return all;
	}

}
