package org.review.rdf;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.review.Utils;
import org.review.sources.Source;
import org.review.sources.SourceType;
import org.review.sources.Sources;

/**
 * Reads NUTS RDF.
 *
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class NutsRdfReader {

	class PropertyComparator implements Comparator<Property> {
		@Override
		public int compare(Property o1, Property o2) {
			return o1.getURI().compareTo(o2.getURI());
		}
	}

	private Model model;

	private File getRdfFile() {
		try {
			for (Source source : new Sources().getSources()) {
				if (source.sourceType.equals(SourceType.NUTSRDF))
					return source.getDownloadFile();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public NutsRdfReader read() {
		model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, getRdfFile().toURI().toString());
		return this;
	}

	public Model getModelCopy() {
		Model modelCopy = ModelFactory.createDefaultModel();
		modelCopy.add(this.model);
		return modelCopy;
	}

	/**
	 * @param scheme 2010 | 2013 | 2016
	 * @param level  0 | 1 | 2 (level 3 not directly encoded)
	 */
	public SortedSet<String> getResourceUrisInSchemeAndLevel(int scheme, int level) {
		SortedSet<String> uris = new TreeSet<>();
		for (String uri : getResourceUrisInScheme(scheme)) {
			Resource res = model.getResource(uri);
			if (model.containsLiteral(res, VocEu.EU_level, level)) {
				uris.add(res.getURI());
			}
		}
		return uris;
	}

	/**
	 * @param scheme 2010 | 2013 | 2016
	 */
	public SortedSet<String> getResourceUrisInScheme(int scheme) {
		SortedSet<String> uris = new TreeSet<>();
		Resource resScheme = model.getResource(VocEu.getNutsSchemeUri(scheme));
		ResIterator resIt = model.listResourcesWithProperty(VocEu.SKOS_inScheme, resScheme);
		while (resIt.hasNext()) {
			uris.add(resIt.next().getURI());
		}
		return uris;
	}

	public SortedSet<String> getAllPredicateUris() {
		SortedSet<String> predicateUris = new TreeSet<>();
		StmtIterator stmtIt = model.listStatements();
		while (stmtIt.hasNext()) {
			predicateUris.add(stmtIt.next().getPredicate().getURI());
		}
		return predicateUris;
	}

	public void printPredicateUrisOfTypes(String uri) {
		ResIterator it = model.listSubjectsWithProperty(RDF.type, ResourceFactory.createResource(uri));
		Set<RDFNode> resourcesOfType = new HashSet<>();
		while (it.hasNext()) {
			resourcesOfType.add(it.next());
		}
		SortedSet<Property> asSub = new TreeSet<Property>(new PropertyComparator());
		SortedSet<Property> asObj = new TreeSet<Property>(new PropertyComparator());
		StmtIterator stmtIt = model.listStatements();
		while (stmtIt.hasNext()) {
			Statement stmt = stmtIt.next();
			if (resourcesOfType.contains(stmt.getSubject())) {
				asSub.add(stmt.getPredicate());
			}
			if (resourcesOfType.contains(stmt.getObject())) {
				asObj.add(stmt.getPredicate());
			}
		}
		System.out.println("Predicates where subject is of type " + uri);
		for (Property property : asSub) {
			System.out.println(property);
		}
		System.out.println();
		System.out.println("Predicates where object is of type " + uri);
		for (Property property : asObj) {
			System.out.println(property);
		}
	}

	public SortedSet<String> getAllResourceUris() {
		SortedSet<String> resourceUris = new TreeSet<>();

		ResIterator resIt = model.listSubjects();
		while (resIt.hasNext()) {
			resourceUris.add(resIt.next().getURI());
		}

		NodeIterator nodeIt = model.listObjects();
		while (nodeIt.hasNext()) {
			RDFNode node = nodeIt.next();
			if (node.isURIResource())
				resourceUris.add(node.asResource().getURI());
		}

		return resourceUris;
	}

	public Map<String, Integer> getMostUsedUriResources() {
		Map<String, Integer> counts = new HashMap<>();
		StmtIterator it = model.listStatements();
		RDFNode res;
		String uri;
		while (it.hasNext()) {
			Statement statement = it.next();
			res = statement.getSubject();
			if (res.isURIResource()) {
				uri = res.asResource().getURI();
				if (counts.containsKey(uri)) {
					counts.put(uri, counts.get(uri) + 1);
				} else {
					counts.put(uri, 1);
				}
			}
			res = statement.getObject();
			if (res.isURIResource()) {
				uri = res.asResource().getURI();
				if (counts.containsKey(uri)) {
					counts.put(uri, counts.get(uri) + 1);
				} else {
					counts.put(uri, 1);
				}
			}
		}
		return Utils.sortByValue(counts, true);
	}

	public SortedSet<String> getAllObjects(String predicateUri) {
		SortedSet<String> objects = new TreeSet<>();
		NodeIterator it = model.listObjectsOfProperty(ResourceFactory.createProperty(predicateUri));
		while (it.hasNext()) {
			objects.add(it.next().toString());
		}
		return objects;
	}

	public SortedSet<String> getAllSubjects(String predicateUri) {
		SortedSet<String> objects = new TreeSet<>();
		ResIterator it = model.listSubjectsWithProperty(ResourceFactory.createProperty(predicateUri));
		while (it.hasNext()) {
			objects.add(it.next().toString());
		}
		return objects;
	}

	public void printStats() {
		NutsRdfReader reader = read();

		System.out.println("Triples: " + model.size());

		System.out.println("Predicate URIs");
		for (String uri : reader.getAllPredicateUris()) {
			System.out.println(uri);
		}
		System.out.println();

		System.out.println("Resource URIs");
		for (int scheme : new Integer[] { 2010, 2013, 2016 }) {
			for (int level : new Integer[] { 0, 1, 2 }) {
				System.out.println(
						scheme + " " + level + "  " + reader.getResourceUrisInSchemeAndLevel(scheme, level).size());
			}
			System.out.println("Overall " + reader.getResourceUrisInScheme(scheme).size());
			System.out.println();
		}

		System.out.println("Resource URIs");
		System.out.println(reader.getAllResourceUris().size());

		System.out.println();
		System.out.println("Most used resource URIs");
		int i = 10;
		for (Entry<String, Integer> entry : reader.getMostUsedUriResources().entrySet()) {
			i--;
			System.out.println(entry);
			if (i == 0)
				break;
		}

		System.out.println();
		System.out.println("Subjects of http://purl.org/dc/terms/issued                "
				+ getAllSubjects("http://purl.org/dc/terms/issued"));
		System.out.println("Objects of http://www.w3.org/2004/02/skos/core#inScheme    "
				+ getAllObjects("http://www.w3.org/2004/02/skos/core#inScheme"));
		System.out.println("Objects of http://data.europa.eu/nuts/level                "
				+ getAllObjects("http://data.europa.eu/nuts/level"));
		System.out.println("Objects of http://www.w3.org/1999/02/22-rdf-syntax-ns#type "
				+ getAllObjects("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		System.out.println("Objects of http://www.w3.org/ns/adms#status                "
				+ getAllObjects("http://www.w3.org/ns/adms#status"));

		System.out.println();
		printPredicateUrisOfTypes("http://www.w3.org/2004/02/skos/core#Concept");

		System.out.println();
		printPredicateUrisOfTypes("http://www.w3.org/2004/02/skos/core#ConceptScheme");
	}

}