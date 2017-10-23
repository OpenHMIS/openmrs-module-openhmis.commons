package org.openmrs.module.openhmis.commons.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Created by Kevin on 7/11/2017.
 */
public class TestUtil {

	public static final String TEST_DATASETS_PROPERTIES_FILE = "test-datasets.properties";

	/**
	 * Determines the name of the proper test dataset based on what version of OpenMRS we are testing against
	 */
	public String getTestDatasetFilename(String testDatasetName) throws Exception {

		InputStream propertiesFileStream = null;

		// try to load the file if its a straight up path to the file or
		// if its a classpath path to the file
		if (new File(TEST_DATASETS_PROPERTIES_FILE).exists()) {
			propertiesFileStream = new FileInputStream(TEST_DATASETS_PROPERTIES_FILE);
		} else {
			propertiesFileStream = getClass().getClassLoader().getResourceAsStream(TEST_DATASETS_PROPERTIES_FILE);
			if (propertiesFileStream == null) {
				throw new FileNotFoundException("Unable to find '" + TEST_DATASETS_PROPERTIES_FILE + "' in the classpath");
			}
		}

		Properties props = new Properties();

		OpenmrsUtil.loadProperties(props, propertiesFileStream);

		if (props.getProperty(testDatasetName) == null) {
			throw new Exception("Test dataset named " + testDatasetName + " not found in properties file");
		}

		return props.getProperty(testDatasetName);
	}

	public String getVersionedFileIfExists(String datasetFileName) {
		String openMRSVersion = OpenmrsConstants.OPENMRS_VERSION_SHORT;
		int openMRSMajorVersionNumber = Integer.parseInt(openMRSVersion.split("\\.")[0]);
		int openMRSMinorVersionNumber = Integer.parseInt(openMRSVersion.split("\\.")[1]);

		File datasetFile = new File(datasetFileName);
		// try to load the file if its a straight up path to the file or
		// if its a classpath path to the file
		if (!datasetFile.exists()) {
			datasetFile = new File(getClass().getClassLoader().getResource(datasetFileName).getPath());
			// If it doesn't exist, we'll let the OpenMRS class throw the error
			if (!datasetFile.exists()) {
				return datasetFileName;
			}
		}

		// Split up the file path, file name, and file extension
		String datasetFileNameWithoutExtension = datasetFile.getName().substring(0, datasetFile.getName().lastIndexOf('.'));
		String datasetFileExtension = datasetFile.getName().substring(datasetFile.getName().lastIndexOf('.'));
		String datasetFilePath =
		        datasetFile.getPath().substring(0, datasetFile.getPath().lastIndexOf(datasetFileNameWithoutExtension));

		int j = openMRSMajorVersionNumber;
		// Cycle through version numbers to check for each file
		for (int i = openMRSMinorVersionNumber; i >= 0; i--) {
			String versionedDatasetFileName =
			        datasetFilePath + datasetFileNameWithoutExtension + "-" + j + "." + i + datasetFileExtension;
			File versionedDatasetFile = new File(versionedDatasetFileName);
			if (versionedDatasetFile.exists()) {
				return versionedDatasetFileName;
			}

			// Assume the minor version is never higher than 30 (arbitrary assumption)
			if (i == 0 && j > 1) {
				i = 30;
				j--;
			}
		}

		return datasetFileName;
	}
}