package org.review.csv;

/**
 * Container for parsed LAU CSV data.
 * 
 * Data cleaning is done in {@link LauCsvParser}.
 * 
 * Single items are maintained in {@link LauCsvCollection}.
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class LauCsvItem {

	public int lauSchema = -1;
	public int nutsSchema = -1;
	public String lauCode;
	public String lauCodeSecond; // only used until 2016
	public String relatedNutsCode;
	public String nameLatin;
	public String nameNational;
	public int population = -1;
	public int area = -1;

	public LauCsvItem(int lauSchema, int nutsSchema, String lauCode, String lauCodeSecond, String relatedNutsCode,
			String nameLatin, String nameNational, int population, int area) {
		this.lauSchema = lauSchema;
		this.nutsSchema = nutsSchema;
		this.lauCode = lauCode;
		this.lauCodeSecond = lauCodeSecond;
		this.relatedNutsCode = relatedNutsCode;
		this.nameLatin = nameLatin;
		this.nameNational = nameNational;
		this.population = population;
		this.area = area;
	}

	public String getCountryCode() {
		if (relatedNutsCode != null && relatedNutsCode.length() >= 2)
			return relatedNutsCode.substring(0, 2);
		else
			return "??";
	}

	@Override
	public String toString() {
		return lauCode + (lauCodeSecond == null ? "" : " " + lauCodeSecond) + " (" + getCountryCode() + ", " + nameLatin
				+ ", " + relatedNutsCode + ")";
	}

	// Check values

	public boolean hasLauSchema() {
		return lauSchema != -1;
	}

	public boolean hasNutsSchema() {
		return nutsSchema != -1;
	}

	public boolean hasLauCode() {
		return lauCode != null;
	}

	public boolean hasLauCodeSecond() {
		return lauCodeSecond != null;
	}

	public boolean hasNutsCode() {
		return relatedNutsCode != null;
	}

	public boolean hasNameLatin() {
		return nameLatin != null;
	}

	public boolean hasNameNational() {
		return nameNational != null;
	}

	public boolean hasPopulation() {
		return population != -1;
	}

	public boolean hasArea() {
		return area != -1;
	}

	// Get values

	public String lauCodeToString() {
		return hasLauCode() ? lauCode : "";
	}

	public String lauCodeSecondToString() {
		return hasLauCodeSecond() ? lauCodeSecond : "";
	}

	public String nutsCodeToString() {
		return hasNutsCode() ? relatedNutsCode : "";
	}

	public String nameLatinToString() {
		return hasNameLatin() ? nameLatin : "";
	}

	public String nameNationalToString() {
		return hasNameNational() ? nameNational : "";
	}

	public String populationToString() {
		return hasPopulation() ? Integer.toString(population) : "";
	}

	public String areaToString() {
		return hasArea() ? Integer.toString(area) : "";
	}
}