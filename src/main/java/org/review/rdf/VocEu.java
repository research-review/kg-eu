package org.review.rdf;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

/**
 * Vocabulary used by Eurostat.
 * 
 * Example URI: http://data.europa.eu/nuts/code/DEA47
 * 
 * Example URI: http://data.europa.eu/nuts/scheme/2016
 * 
 * @see https://ec.europa.eu/eurostat/web/nuts/linked-open-data
 *
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public abstract class VocEu {

	public static final String EU_PREFIX_NUTS = "http://data.europa.eu/nuts/";
	public static final String EU_NS_NUTS_CODE = EU_PREFIX_NUTS + "code/";
	public static final String EU_NS_NUTS_SCHEME = EU_PREFIX_NUTS + "scheme/";
	public static final String EU_NS_STATUS = "http://publications.europa.eu/resource/authority/concept-status/";
	public static final String DCT_NS = DCTerms.NS;
	public static final String RDF_NS = RDF.uri;
	public static final String SKOS_NS = SKOS.uri;
	public static final String ADMS_NS = "http://www.w3.org/ns/adms";

	private static final Model model = ModelFactory.createDefaultModel();

	// --- RESOURCES ---

	// publications.europa.eu

	public static final Resource EU_Current = model.createResource(EU_NS_STATUS + "CURRENT");
	public static final Resource EU_Deprecated = model.createResource(EU_NS_STATUS + "DEPRECATED");

	// SKOS

	public static final Resource SKOS_Concept = SKOS.Concept;
	public static final Resource SKOS_ConceptScheme = SKOS.ConceptScheme;

	// --- PROPERTIES ---

	// data.europa.eu

	public static final Property EU_level = model.createProperty(EU_PREFIX_NUTS + "level");
	public static final Property EU_mergedFrom = model.createProperty(EU_PREFIX_NUTS + "mergedFrom");
	public static final Property EU_mergedInto = model.createProperty(EU_PREFIX_NUTS + "mergedInto");
	public static final Property EU_splitFrom = model.createProperty(EU_PREFIX_NUTS + "splitFrom");
	public static final Property EU_splitInto = model.createProperty(EU_PREFIX_NUTS + "splitInto");

	// Dublin Core

	public static final Property DCT_isReplacedBy = DCTerms.isReplacedBy;
	public static final Property DCT_issued = DCTerms.issued;
	public static final Property DCT_replaces = DCTerms.replaces;

	// RDF

	public static final Property RDF_type = RDF.type;

	// SKOS

	public static final Property SKOS_broader = SKOS.broader;
	public static final Property SKOS_inScheme = SKOS.inScheme;
	public static final Property SKOS_narrower = SKOS.narrower;
	public static final Property SKOS_notation = SKOS.notation;
	public static final Property SKOS_prefLabel = SKOS.prefLabel;

	/**
	 * Note: Error in EU RDF source file. Use {@value #DCT_isReplacedBy} instead.
	 */
	public static final Property SKOS_ERR_isReplacedBy = model.createProperty(SKOS_NS + "isReplacedBy");

	/**
	 * Note: Error in EU RDF source file. Use {@value #DCT_replaces} instead.
	 */
	public static final Property SKOS_ERR_replaces = model.createProperty(SKOS_NS + "replaces");

	// Asset Description Metadata Schema (ADMS)

	public static final Property ADMS_status = model.createProperty(ADMS_NS + "status");

	// --- CONSTRUCTION METHODS ---

	/**
	 * Constructs sorted map with RDF prefixes / namespaces.
	 * 
	 * @return Sorted map containing prefixes
	 */
	public static final SortedMap<String, String> getPrefixes() {
		SortedMap<String, String> map = new TreeMap<>();
		map.put("nuts", EU_NS_NUTS_CODE);
		map.put("scheme", EU_NS_NUTS_SCHEME);
		map.put("status", EU_NS_STATUS);
		map.put("dct", DCT_NS);
		map.put("rdf", RDF_NS);
		map.put("skos", SKOS_NS);
		map.put("adms", ADMS_NS);
		return map;
	}

	/**
	 * Constructs URI of NUTS scheme.
	 * 
	 * @param scheme 2010 | 2013 | 2016
	 * 
	 * @return URI of NUTS scheme
	 */
	public static String getNutsSchemeUri(Integer scheme) {
		return EU_NS_NUTS_SCHEME + scheme;
	}

	/**
	 * Constructs URI of NUTS code.
	 * 
	 * @param nutsCode, e.g. "FR101" (Paris) or "DE" (Germany)
	 * 
	 * @return URI of URI of NUTS code
	 */
	public static String getNutsCodeUri(String nutsCode) {
		return EU_NS_NUTS_CODE + nutsCode;
	}
}