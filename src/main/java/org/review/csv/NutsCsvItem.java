package org.review.csv;

/**
 * Container for parsed NUTS CSV data.
 * 
 * Data cleaning is done in {@link NutsCsvParser}.
 * 
 * Single items are maintained in {@link NutsCsvCollection}.
 * 
 * @author 33a1cc8d616a72f953d8e15274194bcd5aac2b78fbe6b4a4d1a911e0f2ef00cd
 */
public class NutsCsvItem {

	public int nutsSchema = -1;
	public String nutsCode;
	public String name;

	public NutsCsvItem(int nutsSchema, String nutsCode, String name) {
		this.nutsSchema = nutsSchema;
		this.nutsCode = nutsCode;
		this.name = name;
	}

	@Override
	public String toString() {
		return nutsCode + " (" + name + ", " + nutsSchema + " )";
	}

	public int getLevel() {
		return nutsCode.length() - 2;
	}

	// Check values

	public boolean hasNutsSchema() {
		return nutsSchema != -1;
	}

	public boolean hasNutsCode() {
		return nutsCode != null;
	}

	public boolean hasName() {
		return name != null;
	}

}