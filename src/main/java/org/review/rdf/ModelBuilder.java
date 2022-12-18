package org.review.rdf;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.review.csv.LauCsvCollection;
import org.review.csv.LauCsvItem;
import org.review.csv.NutsCsvCollection;
import org.review.csv.NutsCsvItem;
import org.review.linking.WikipediaLinking;

/**
 * Creates Knowledge Graph.
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class ModelBuilder {

	public List<LauCsvCollection> lauCsvCollections = new LinkedList<>();
	public List<NutsCsvCollection> nutsCsvCollections = new LinkedList<>();
	public Statistics statistics = new Statistics();

	public Model build() {
		Model model = ModelFactory.createDefaultModel();

		for (LauCsvCollection lauCsvCollection : lauCsvCollections) {
			addLauCsvCollection(model, lauCsvCollection);
		}

		for (NutsCsvCollection nutsCsvCollection : nutsCsvCollections) {
			addNutsCsvCollection(model, nutsCsvCollection);
		}

		addRelated(model);

		System.out.println(statistics);

		return model;
	}

	private void addLauCsvCollection(Model model, LauCsvCollection lauCsvCollection) {
		for (LauCsvItem item : lauCsvCollection.lauCsvItems) {

			// Mandatory values
			if (!item.hasLauSchema() || !item.hasNutsSchema() || !item.hasLauCode() || !item.hasNutsCode()) {
				System.err.println("Warning: Value missing " + item.toString() + " " + getClass().getSimpleName());
				continue;
			}

			// LAU to NUTS
			Resource resUniqueNuts = ResourceFactory
					.createResource(Voc.getUniqueNutsUri(item.nutsSchema, item.nutsCodeToString()));
			Resource resUniqueLau = ResourceFactory.createResource(Voc.getUniqueLauUri(item.lauSchema,
					item.getCountryCode(), item.lauCodeToString(), item.lauCodeSecondToString()));
			model.add(resUniqueLau, Voc.SKOS_broader, resUniqueNuts);

			// Schema
			Resource resLauSchema = ResourceFactory.createResource(Voc.getLauSchemeUri(item.lauSchema));
			if (!model.containsResource(resLauSchema)) {
				Literal litDate = ResourceFactory.createTypedLiteral(item.lauSchema + "-01-01", XSDDatatype.XSDdate);
				model.add(resLauSchema, Voc.DCT_issued, litDate);
				model.add(resUniqueLau, Voc.SKOS_inScheme, resLauSchema);
			}

			// General LAU
			Resource resLau = ResourceFactory.createResource(
					Voc.getLauUri(item.getCountryCode(), item.lauCodeToString(), item.lauCodeSecondToString()));
			if (!model.containsResource(resLau)) {
				model.add(resLau, Voc.SKOS_inScheme, resLauSchema);
				model.add(resUniqueLau, Voc.SKOS_hasTopConcept, resLau);
				model.addLiteral(resLau, Voc.SKOS_notation, ResourceFactory.createPlainLiteral(item.lauCode));
			}

			// Literals: Names
			if (item.hasNameLatin())
				model.addLiteral(resUniqueLau, Voc.SKOS_prefLabel, ResourceFactory.createPlainLiteral(item.nameLatin));
			if (item.hasNameNational())
				// National names are only added if not already addded in latin
				if (!item.nameNational.equals(item.nameLatin))
					model.addLiteral(resUniqueLau, Voc.SKOS_altLabel,
							ResourceFactory.createPlainLiteral(item.nameNational));

			// Literals: Numbers
			if (item.hasArea())
				model.addLiteral(resUniqueLau, Voc.DBO_area, ResourceFactory.createTypedLiteral(item.area));
			if (item.hasPopulation())
				model.addLiteral(resUniqueLau, Voc.DBO_populationTotal,
						ResourceFactory.createTypedLiteral(item.population));

			statistics.countLauCsvItem(item);
		}
	}

	private void addNutsCsvCollection(Model model, NutsCsvCollection nutsCsvCollection) {
		for (NutsCsvItem item : nutsCsvCollection.getAll()) {

			// Mandatory values
			if (!item.hasNutsSchema() || !item.hasName() || !item.hasNutsCode()) {
				System.err.println("Warning: Value missing " + item.toString() + " " + getClass().getSimpleName());
				continue;
			}

			Resource resUniqueNuts = ResourceFactory
					.createResource(Voc.getUniqueNutsUri(item.nutsSchema, item.nutsCode));

			// Eurostat NUTS scheme
			Resource resEurostatNutsScheme = ResourceFactory.createResource(VocEu.getNutsSchemeUri(item.nutsSchema));
			if (!model.containsResource(resEurostatNutsScheme)) {
				Literal litDate = ResourceFactory.createTypedLiteral(item.nutsSchema + "-01-01", XSDDatatype.XSDdate);
				model.add(resEurostatNutsScheme, Voc.DCT_issued, litDate);
			}
			model.add(resUniqueNuts, Voc.SKOS_inScheme, resEurostatNutsScheme);

			// Eurostat NUTS
			Resource resEurostatNuts = ResourceFactory.createResource(VocEu.getNutsCodeUri(item.nutsCode));
			if (!model.containsResource(resEurostatNuts)) {
				model.addLiteral(resEurostatNuts, Voc.SKOS_notation, ResourceFactory.createPlainLiteral(item.nutsCode));
				model.add(resEurostatNuts, Voc.SKOS_inScheme, resEurostatNutsScheme);
			}
			model.add(resUniqueNuts, Voc.SKOS_hasTopConcept, resEurostatNuts);

			// Add NUTS label/name
			model.addLiteral(resUniqueNuts, Voc.SKOS_prefLabel, ResourceFactory.createPlainLiteral(item.name));

			// Add hierarchy
			if (item.getLevel() > 0) {
				Resource resbroaderLaunutsNuts = ResourceFactory.createResource(
						Voc.getUniqueNutsUri(item.nutsSchema, item.nutsCode.substring(0, item.nutsCode.length() - 1)));
				model.add(resUniqueNuts, Voc.SKOS_broader, resbroaderLaunutsNuts);
			}

			statistics.countNutsCsvItem(item);
		}
	}

	private void addRelated(Model model) {
		WikipediaLinking wikipediaLinking = new WikipediaLinking();
		String markdown = wikipediaLinking.downloadWpNuts1().getWpNuts1Markdown();
		Map<String, String> codesToUris = wikipediaLinking.getWpNuts1CodesToUris(markdown);
		for (Entry<String, String> codeToUri : codesToUris.entrySet()) {
			String uniqueNutsUri = Voc.getUniqueNutsUri(2021, codeToUri.getKey());
			Resource nutsRes = ResourceFactory.createResource(uniqueNutsUri);
			if (model.containsResource(nutsRes)) {
				Resource wpRes = ResourceFactory.createResource(codeToUri.getValue());
				model.add(nutsRes, Voc.SKOS_related, wpRes);
				statistics.countLinked();
			} else
				System.err.println("Info: Not linked: " + uniqueNutsUri + " " + getClass().getSimpleName());
		}
	}
}