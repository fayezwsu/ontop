package it.unibz.krdb.obda.io;

import java.util.HashMap;
import java.util.Set;


/**
 * The prefix manager is administrating the prefixes for ontolgyie. It allows to
 * register and unregister prefixes for ontolgies and to query them.
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class SimplePrefixManager extends AbstractPrefixManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2394283946895814071L;
	/**
	 * A simple map containing for each ontolgoy URI the correpsonding prefix
	 */
	private HashMap<String, String>	uriToPrefixMap		= null;
	/**
	 * A simple map containing for each prefix the correpsonding onotlogy URI
	 */
	private HashMap<String, String>	prefixToURIMap		= null;



	/**
	 * The constructor. It creates a new instance of the prefix manager
	 */
	public SimplePrefixManager() {
		uriToPrefixMap = new HashMap<String, String>();
		prefixToURIMap = new HashMap<String, String>();
	}

	/**
	 * Adds the given prefix together with the corresponding ontolgoy URI to the
	 * manager
	 * 
	 * @param uri
	 *            the ontolgy URI
	 * @param prefix
	 *            the prefix
	 */
	public void addUri(String uri, String prefix) {
		uriToPrefixMap.put(uri, prefix);
		prefixToURIMap.put(prefix, uri);
	}

	/**
	 * Returns the corresponding ontology URI for the given prefix
	 * 
	 * @param prefix
	 *            the prefix
	 * @return the corresponding ontology URI or null if the prefix is not
	 *         registered
	 */
	public String getURIForPrefix(String prefix) {
		return prefixToURIMap.get(prefix);
	}

	/**
	 * Returns the corresponding prefix for the given ontology URI
	 * 
	 * @param prefix
	 *            the prefix
	 * @return the corresponding prefix or null if the ontology URI is not
	 *         registered
	 */
	public String getPrefixForURI(String uri) {
		return uriToPrefixMap.get(uri);
	}

	/**
	 * Returns a map with all registered prefixes and the corresponding ontology
	 * URI's
	 * 
	 * @return a hash map
	 */
	public HashMap<String, String> getPrefixMap() {
		return prefixToURIMap;
	}
	
	/**
	 * Checks if the prefix manager stores the prefix name.
	 * 
	 * @param prefix
	 * 				The prefix name to check.
	 */
	public boolean contains(String prefix) {
		Set<String> prefixes = prefixToURIMap.keySet();
		return prefixes.contains(prefix);
	}
}