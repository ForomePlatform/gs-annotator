package com.annotator.utils.fam_file;

public interface FamFileConstants {
	// General:
	String FAMILY_ID_KEY = "familyId";
	String WITHIN_FAMILY_ID_KEY = "withinFamilyId";
	String WITHIN_FAMILY_ID_OF_FATHER_KEY = "withinFamilyIdOfFather";
	String WITHIN_FAMILY_ID_OF_MOTHER_KEY = "withinFamilyIdOfMother";
	String SEX_CODE_KEY = "genderCode";
	String PHENOTYPE_VALUE_KEY = "phenotypeValue";

	// Phenotype key/values:
	Integer CONTROL_KEY = 1;
	Integer CASE_KEY = 2;
	String CONTROL_VALUE = "control";
	String CASE_VALUE = "case";
	String MISSING_DATA = "missingData";

	// Error messages:
	String FAM_PARSING_ERROR = "Error occurred during FAM file parsing...";
}
