package org.icrisat.gdms.retrieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//import org.generationcp.middleware.dao.TraitDAO;
import org.generationcp.middleware.dao.gdms.QtlDAO;
import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
//import org.generationcp.middleware.pojos.Trait;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
import org.hibernate.Session;
import org.icrisat.gdms.ui.common.GDMSModel;

public class RetrieveQTL {

	private Session localSession;
	private Session centralSession;
	
	ManagerFactory factory=null;
	OntologyDataManager om;
	
	public RetrieveQTL() {
		localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		om=factory.getOntologyDataManager();
		
	}
	

	private List<QtlDetailElement> getCentralQTLByName(String strQTLName) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		long countQtlDetailsByName = qtlDAO.getAll().size();
		List<QtlDetailElement> listOfQTLDetailElementsByName = qtlDAO.getQtlDetailsByName(strQTLName, 0, (int)countQtlDetailsByName);
		return listOfQTLDetailElementsByName;
	}

	private List<QtlDetailElement> getLocalQTLByName(String strQTLName) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		long countQtlDetailsByName = qtlDAO.getAll().size();
		List<QtlDetailElement> listOfQTLDetailElementsByName = qtlDAO.getQtlDetailsByName(strQTLName, 0, (int)countQtlDetailsByName);
		return listOfQTLDetailElementsByName;
	}

	public List<QtlDetailElement> retrieveQTLByName(String strQTLName) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		List<QtlDetailElement> localQTLByName = getLocalQTLByName(strQTLName);
		if(null != localQTLByName) {
			listToReturn.addAll(localQTLByName);
		}

		List<QtlDetailElement> centralQTLByName = getCentralQTLByName(strQTLName);
		if(null != centralQTLByName) {
			listToReturn.addAll(centralQTLByName);
		}
		return listToReturn;
	}
	
	public List<QtlDetailElement> retrieveQTLDetails() throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		List<QtlDetailElement> localQTL = getLocalQTLDetails();
		if(null != localQTL) {
			listToReturn.addAll(localQTL);
		}
		List<QtlDetailElement> getcentrailQTL = getcentralQTLDetails();
		if(null != getcentrailQTL) {
			listToReturn.addAll(getcentrailQTL);
		}
		return listToReturn;
	}

	private List<QtlDetailElement> getcentralQTLDetails() throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			String qtlName = qtl.getQtlName();
			boolean bFound = false;
			for (QtlDetailElement qtlDetailElement : listToReturn) {
				if(qtlDetailElement.getQtlName().equals(qtlName)) {
					bFound = true;
					break;
				}
			}
			if(bFound) {
				continue;
			}
			List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlDetailsByName(qtlName, 0, all.size());
			if(null != qtlDetailsByName) {
				listToReturn.addAll(qtlDetailsByName);
			}
		}
		return listToReturn;
	}

	private List<QtlDetailElement> getLocalQTLDetails() throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			String qtlName = qtl.getQtlName();
			boolean bFound = false;
			for (QtlDetailElement qtlDetailElement : listToReturn) {
				if(qtlDetailElement.getQtlName().equals(qtlName)) {
					bFound = true;
					break;
				}
			}
			if(bFound) {
				continue;
			}
			List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlDetailsByName(qtlName, 0, all.size());
			if(null != qtlDetailsByName) {
				listToReturn.addAll(qtlDetailsByName);
			}
		}
		return listToReturn;
	}

	public List<QtlDetailElement> retrieveQTLDetailsStartsWith(String theStartWith) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		List<QtlDetailElement> localQTL = getLocalQTLDetailsStartsWith(theStartWith);
		if(null != localQTL) {
			listToReturn.addAll(localQTL);
		}
		List<QtlDetailElement> getcentrailQTL = getcentralQTLDetailsStartsWith(theStartWith);
		if(null != getcentrailQTL) {
			listToReturn.addAll(getcentrailQTL);
		}
		return listToReturn;
	}

	private List<QtlDetailElement> getcentralQTLDetailsStartsWith(String theStartWith) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			String qtlName = qtl.getQtlName();
			boolean bFound = false;
			for (QtlDetailElement qtlDetailElement : listToReturn) {
				if(qtlDetailElement.getQtlName().equals(qtlName)) {
					bFound = true;
					break;
				}
			}
			if(bFound) {
				continue;
			}
			if(qtlName.startsWith(theStartWith)) {
				List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlDetailsByName(qtlName, 0, all.size());
				if(null != qtlDetailsByName) {
					listToReturn.addAll(qtlDetailsByName);
				}
			}
		}
		return listToReturn;
	}

	private List<QtlDetailElement> getLocalQTLDetailsStartsWith(String theStartWith) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			String qtlName = qtl.getQtlName();
			boolean bFound = false;
			for (QtlDetailElement qtlDetailElement : listToReturn) {
				if(qtlDetailElement.getQtlName().equals(qtlName)) {
					bFound = true;
					break;
				}
			}
			if(bFound) {
				continue;
			}
			if(qtlName.startsWith(theStartWith)) {
				List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlDetailsByName(qtlName, 0, all.size());
				if(null != qtlDetailsByName) {
					listToReturn.addAll(qtlDetailsByName);
				}
			}
		}
		return listToReturn;
	}

	public List<String> retrieveQTLNames() throws MiddlewareQueryException {
		List<String> listOfQTLNames = new ArrayList<String>();
		List<Qtl> localQTL = getLocalQTL();
		for (Qtl qtl : localQTL) {
			if(false == listOfQTLNames.contains(qtl.getQtlName())) {
				listOfQTLNames.add(qtl.getQtlName());
			}
		}
		List<Qtl> centralQTL = getCentralQTL();
		for (Qtl qtl : centralQTL) {
			if(false == listOfQTLNames.contains(qtl.getQtlName())) {
				listOfQTLNames.add(qtl.getQtlName());
			}
		}
		
		return listOfQTLNames;
	}

	private List<Qtl> getCentralQTL() throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		List<Qtl> all = qtlDAO.getAll();
		return all;
	}

	private List<Qtl> getLocalQTL() throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		List<Qtl> all = qtlDAO.getAll();
		return all;
	}

	/*public List<QtlDetailElement> retrieveTrait(String strTraitName) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		List<QtlDetailElement> localTrait = getLocalTrait(strTraitName);
		if(null != localTrait) {
			listToReturn.addAll(localTrait);
		}
		List<QtlDetailElement> centralTrait = getCentralTrait(strTraitName);
		if(null != centralTrait) {
			listToReturn.addAll(centralTrait);
		}
		return listToReturn;
	}
*/
	public List<QtlDetailElement> retrieveTrait(String strTraitName) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		
		Integer traitIdByTraitName = getTraitIdByTraitName(strTraitName);
		
		if (null == traitIdByTraitName){
			return listToReturn;
		}
		
		List<QtlDetailElement> localTrait = getLocalTrait(traitIdByTraitName);
		if(null != localTrait) {
			listToReturn.addAll(localTrait);
		}
		List<QtlDetailElement> centralTrait = getCentralTrait(traitIdByTraitName);
		if(null != centralTrait) {
			listToReturn.addAll(centralTrait);
		}
		return listToReturn;
	}

	/*private List<QtlDetailElement> getCentralTrait(String strTraitName) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		int size = qtlDAO.getAll().size();
		List<Integer> listOfQTLIdsByTrait = qtlDAO.getQtlByTrait(strTraitName, 0, size);
		if(null == listOfQTLIdsByTrait || 0 == listOfQTLIdsByTrait.size()) {
			return new ArrayList<QtlDetailElement>();
		}
		return qtlDAO.getQtlDetailsByQTLIDs(listOfQTLIdsByTrait, 0, size);
	}*/
	
	private Integer getTraitIdByTraitName(String strTraitName) throws MiddlewareQueryException {

		Integer localTraitId = getLocalTrait(strTraitName);
		if (null != localTraitId){
			return localTraitId;
		} else {
			Integer centralTraitId = getCentralTrait(strTraitName);
			return centralTraitId;
		}
	}
	


	private Integer getLocalTrait(String strTraitName) throws MiddlewareQueryException {
		
		Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(strTraitName);
		//assertTrue(standardVariables.size() == 1);
		for (StandardVariable stdVar : standardVariables) {
			System.out.println(stdVar.getId()+"   "+stdVar.getNameSynonyms()+"   "+stdVar.getName());
			
			return stdVar.getId();
		}		
		/*TraitDAO traitDAO = new TraitDAO();
		traitDAO.setSession(localSession);
		List<Trait> listOfAllTraits = traitDAO.getAll();
		if (null != listOfAllTraits){
			for (Trait trait : listOfAllTraits){
				String strAbbr = trait.getAbbreviation();
				if (strAbbr.equals(strTraitName)){
					return trait.getTraitId();
				}
			}
		}*/
		return null;
	}


	private Integer getCentralTrait(String strTraitName) throws MiddlewareQueryException {
		Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(strTraitName);
		//assertTrue(standardVariables.size() == 1);
		for (StandardVariable stdVar : standardVariables) {
			System.out.println(stdVar.getId()+"   "+stdVar.getNameSynonyms()+"   "+stdVar.getName());
			
			return stdVar.getId();
		}	
		/*TraitDAO traitDAO = new TraitDAO();
		traitDAO.setSession(centralSession);
		List<Trait> listOfAllTraits = traitDAO.getAll();
		if (null != listOfAllTraits){
			for (Trait trait : listOfAllTraits){
				String strAbbr = trait.getAbbreviation();
				if (strAbbr.equals(strTraitName)){
					return trait.getTraitId();
				}
			}
		}*/
		return null;
	}


	private List<QtlDetailElement> getCentralTrait(Integer iTraitId) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		int size = qtlDAO.getAll().size();
		List<Integer> listOfQTLIdsByTrait = qtlDAO.getQtlByTrait(iTraitId, 0, size);
		if(null == listOfQTLIdsByTrait || 0 == listOfQTLIdsByTrait.size()) {
			return new ArrayList<QtlDetailElement>();
		}
		return qtlDAO.getQtlDetailsByQTLIDs(listOfQTLIdsByTrait, 0, size);
	}

/*	private List<QtlDetailElement> getLocalTrait(String strTraitName) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		int size = qtlDAO.getAll().size();
		List<Integer> listOfQTLIdsByTrait = qtlDAO.getQtlByTrait(strTraitName, 0, size);
		if(null == listOfQTLIdsByTrait || 0 == listOfQTLIdsByTrait.size()) {
			return new ArrayList<QtlDetailElement>();
		}
		return qtlDAO.getQtlDetailsByQTLIDs(listOfQTLIdsByTrait, 0, size);

	}*/
	
	private List<QtlDetailElement> getLocalTrait(Integer iTraitId) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		int size = qtlDAO.getAll().size();
		List<Integer> listOfQTLIdsByTrait = qtlDAO.getQtlByTrait(iTraitId, 0, size);
		if(null == listOfQTLIdsByTrait || 0 == listOfQTLIdsByTrait.size()) {
			return new ArrayList<QtlDetailElement>();
		}
		return qtlDAO.getQtlDetailsByQTLIDs(listOfQTLIdsByTrait, 0, size);
	}
	

	public QtlDetailElement retrieveTraitNameStartWith(String strTraitSearchName) throws MiddlewareQueryException {
		List<QtlDetailElement> retrieveQTLDetails = retrieveQTLDetails();
		for (QtlDetailElement qtlDetailElement : retrieveQTLDetails) {
			if(qtlDetailElement.getTRName().startsWith(strTraitSearchName)) {
				return qtlDetailElement;
			}
		}
		return null;
	}

	public List<QtlDetails> retrieveQTLDetailsWithQTLDetailsPK() throws MiddlewareQueryException {
		List<QtlDetails> listOfQTLDetails = new ArrayList<QtlDetails>();
		List<QtlDetails> localQTLDetailsWithQTLDetailsPK = getLocalQTLDetailsWithQTLDetailsPK();
		if(null != localQTLDetailsWithQTLDetailsPK) {
			listOfQTLDetails.addAll(localQTLDetailsWithQTLDetailsPK);
		}
		List<QtlDetails> centralQTLDetailsWithQTLDetailsPK = getCentralQTLDetailsWithQTLDetailsPK();
		if(null != centralQTLDetailsWithQTLDetailsPK) {
			listOfQTLDetails.addAll(centralQTLDetailsWithQTLDetailsPK);
		}
		return listOfQTLDetails;
	}

	private List<QtlDetails> getLocalQTLDetailsWithQTLDetailsPK() throws MiddlewareQueryException {
		QtlDetailsDAO dao = new QtlDetailsDAO();
		dao.setSession(localSession);
		return dao.getAll();
	}

	private List<QtlDetails> getCentralQTLDetailsWithQTLDetailsPK() throws MiddlewareQueryException {
		QtlDetailsDAO dao = new QtlDetailsDAO();
		dao.setSession(centralSession);
		return dao.getAll();
	}

}
