package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.OsVersionConstants;

public class OsVersionHelper {

	private static final Logger logger = Logger.getLogger(OsVersionHelper.class.getName());

	public OsVersionHelper() {

	}

	/**
	 * Oracle = "Oracle"
	 */
	public final static String ORACLE = "Oracle";

	/**
	 * EL = "Entreprise Linux"
	 */
	public final static String EL = "Entreprise Linux";

	/**
	 * CENTOS = "CentOS"
	 */
	public final static String CENTOS = "CentOS";

    /**
     * AMI = "Amazon Linux AMI"
     */
    public final static String AMI = "Amazon Linux AMI";

    
	/**
	 * True if Oracle Linux was detected
	 */
	public static final boolean IS_ORACLE_LINUX = OsVersion.IS_LINUX
			&& ((fileContains("/etc/oracle-release", ORACLE) || fileContains("/etc/oracle-release", EL)));

    /**
     * True if CentOS Linux was detected
     */
	public static final boolean IS_CENTOS_LINUX = OsVersion.IS_LINUX && fileContains("/etc/centos-release", CENTOS);

	// No such field
    /**
     * True if Amazon AMI Linux was detected
     */
    public static final boolean IS_AMI_LINUX = OsVersion.IS_LINUX && fileContains(getReleaseFileName(), AMI);
	
    
	// OsVersion osVersion = new OsVersion();
	// osVersion.getOsDetails();

	private static Boolean fileContains(String filename, String str) {
		Boolean result = false;

		try {
			List<String> contentList = getFileContent(filename);
			return contentList.contains(str);
		} catch (IOException e) {

			logger.log(Level.WARNING,
					"OsVersionHelper fileContains(" + filename + ", " + str + ") error: " + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	private static List<String> getFileContent(String fileName) throws IOException {
		List<String> result = new ArrayList<String>();

		File aFile = new File(fileName);
		if (!aFile.isFile()) {
			return result; // None
		}

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(aFile));
		} catch (FileNotFoundException e) {
			return result;
		}

		String aLine;
		while ((aLine = reader.readLine()) != null) {
			result.add(aLine + "\n");
		}
		reader.close();

		return result;
	}


    /**
     * Gets the etc Release Filename
     *
     * @return name of the file the release info is stored in for Linux distributions
     */
    private static String getReleaseFileName() {
        String result = "";
        File[] etcList = new File("/etc").listFiles();
        if (etcList != null) {
            for (File etcEntry : etcList) {
                if (etcEntry.isFile()) {
                    if (etcEntry.getName().endsWith("-release")) {
                        // match :-)
                        return result = etcEntry.toString();
                    }
                }
            }
        }
        return result;
    }

    
}
