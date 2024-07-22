package com.sage.izpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.StringTool;

public class OsVersionHelper {

	private static final Logger logger = Logger.getLogger(OsVersionHelper.class.getName());

	public OsVersionHelper() {

	}

    /**
     * OS_VERSION = System.getProperty("os.aversion")
     */
	public static final String OS_VERSION = (OsVersion.IS_LINUX) ? getLinuxversion() : System.getProperty(OsVersion.OSVERSION);

	public static float getOsVersionFl() {
		String version = System.getProperty(OsVersion.OSVERSION);
		if (OsVersion.IS_LINUX) 
			version = getLinuxversion();
	 float result =  Float.parseFloat(version);
	 logger.log(Level.FINE, "OsVersionHelper getOsVersionFl():" + result);
	 return result;
	}
	
    public static final boolean IS_REDHAT_7_MIN = OsVersion.IS_LINUX && OsVersion.IS_REDHAT_LINUX && (getOsVersionFl() >= 7.0);
    public static final boolean IS_REDHAT_8_MIN = OsVersion.IS_LINUX && OsVersion.IS_REDHAT_LINUX && (getOsVersionFl() >= 8.0);
    public static final boolean IS_REDHAT_9_MIN = OsVersion.IS_LINUX && OsVersion.IS_REDHAT_LINUX && (getOsVersionFl() >= 9.0);
    public static final boolean IS_REDHAT_10_MIN = OsVersion.IS_LINUX && OsVersion.IS_REDHAT_LINUX && (getOsVersionFl() >= 10.0);

    public static final boolean IS_UBUNTU_16_MIN = OsVersion.IS_LINUX && OsVersion.IS_UBUNTU_LINUX && (getOsVersionFl() >= 16.0);

    public static final boolean IS_LINUX_8_MIN = OsVersion.IS_LINUX  && (getOsVersionFl() >= 8.0);
    public static final boolean IS_LINUX_9_MIN = OsVersion.IS_LINUX  && (getOsVersionFl() >= 9.0);
    public static final boolean IS_LINUX_10_MIN = OsVersion.IS_LINUX  && (getOsVersionFl() >= 10.0);

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
     * MACHINE_ID = /etc/machine_id || /var/lib/dbus/machine_id
     */
    public static final String MACHINE_ID = getLinuxMachineId();

    /**
     * MACHINE_ID_REGISTERED = Machine_ID != null
     */
    public static final boolean MACHINE_ID_REGISTERED = OsVersion.IS_LINUX && (MACHINE_ID != null);
    
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
	
    

	private static Boolean fileContains(String filename, String str) {
		Boolean result = false;

		try {
			List<String> contentList = getFileContent(filename);			
			// return contentList.contains(str);
			for (String strItem : contentList) {
	            if (strItem.indexOf(str) >=0 ) {
	            	result = true;
	                break;
	            }
	        }
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


    /*
     * Samples: 
     * Red Hat/older CentOS: 
     * $ cat /etc/redhat-release 
     * CentOS release 5.3 (Final)
     * 
     * newer CentOS: 
     * $ cat /etc/centos-release 
     * CentOS Linux release 7.1.1503 (Core)
     * 
     * $ more /etc/lsb-release 
     * DISTRIB_ID=Ubuntu 
     * DISTRIB_RELEASE=16.04
     * DISTRIB_CODENAME=xenial 
     * DISTRIB_DESCRIPTION="Ubuntu 16.04.2 LTS"
     * 
     * @return version number with the pattern $MAJOR.$MINOR.$SECURITY. 
     * Ex: "16.04.2", "5.3", "7.1.1503", etc ... 
     */
    private static String getLinuxVersionFromFile(String strReleaseFile) {
        final String regex = "^(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)$";
        String result = null;

        try {
            List<String> lstLines = getFileContent(strReleaseFile);

            Iterator linesIter = lstLines.iterator();
            while (linesIter.hasNext()) {
                String strline = (String) linesIter.next();

                String[] strPattern = strline.trim().split(" |=");

                for (int i = 0; i < strPattern.length; i++) {
                    // result = Float.valueOf(strPattern[i]).toString();
                    // Improvement to manage version string like $MAJOR.$MINOR.$SECURITY like "16.04.09"
                    // Full match   0-8 `16.04.09`  Group 1.    0-2 `16` Group 2.   3-5 `04` Group 3.   6-8 `09`
                    if (strPattern[i].matches(regex)) {
                        result = strPattern[i];
                        break;
                    }
                }
            }
        } catch (Exception e1) {
            // TODO handle Exception
            e1.printStackTrace();

        }

        return result;
    }

    private static String _linuxVersion = null;
    private static String getLinuxversion() {
		
    	if (_linuxVersion != null)
			return _linuxVersion;

		String result = null;
	
        if (OsVersion.IS_SUSE_LINUX) {
            result = getLinuxVersionFromFile("/etc/sles-release");
            if (result == null)
                result = getLinuxVersionFromFile("/etc/novell-release");
            if (result == null)
                result = getLinuxVersionFromFile("/etc/SuSE-release");
        } else if (OsVersion.IS_UBUNTU_LINUX) {
            // $ more /etc/lsb-release
            // DISTRIB_ID=Ubuntu
            // DISTRIB_RELEASE=16.04
            // DISTRIB_CODENAME=xenial
            // DISTRIB_DESCRIPTION="Ubuntu 16.04.2 LTS"

            result = getLinuxVersionFromFile("/etc/lsb-release");
        } else if (OsVersion.IS_REDHAT_LINUX) {
            result = getLinuxVersionFromFile("/etc/redhat-release");
        } else if (IS_CENTOS_LINUX) {
            // Red Hat/older CentOS: $ cat /etc/redhat-release
            // CentOS release 5.3 (Final)
            // newer CentOS: $ cat /etc/centos-release
            // CentOS Linux release 7.1.1503 (Core)
            result = getLinuxVersionFromFile("/etc/centos-release");
        } else if (IS_ORACLE_LINUX) {
            result = getLinuxVersionFromFile("/etc/oracle-release");
        } else if (OsVersion.IS_FEDORA_LINUX) {
            // Fedora: $ cat /etc/fedora-release
            // Fedora release 10 (Cambridge)
            result = getLinuxVersionFromFile("/etc/fedora-release");
        } else if (OsVersion.IS_MANDRAKE_LINUX) {
            result = getLinuxVersionFromFile("/etc/mandrake-release");
        } else if (OsVersion.IS_MANDRIVA_LINUX) {
            result = getLinuxVersionFromFile("/etc/mandriva-release");
            if (result == null)
                result = getLinuxVersionFromFile("/etc/mandrake-release");
            if (result == null)
                result = getLinuxVersionFromFile("/etc/mandrakelinux-release");
        } else if (OsVersion.IS_DEBIAN_LINUX) {
            // $ cat /etc/debian_version
            // 5.0.2
            result = getLinuxVersionFromFile("/etc/debian_version");
        } else {
            result = getLinuxVersionFromFile(getReleaseFileName());
        }
        
        logger.log(Level.FINE, "getLinuxversion result: "+ result);

        _linuxVersion = result;
        
        return result;
    }


    /**
     * Gets the Details of a Linux Distribution
     *
     * @return description string of the Linux distribution 
     * Ex: Red Hat 7.2, SuSE
     *         Linux 15.3
     */
    public static String getLinuxDistribution() {
        String result = null;

        if (OsVersion.IS_SUSE_LINUX) {
            try {
                result = OsVersion.SUSE + OsVersion.SP + OsVersion.LINUX + OsVersion.NL
                        + StringTool.listToString(getFileContent(getReleaseFileName()));
            } catch (IOException e) {
                // TODO ignore
            }
        } else if (OsVersion.IS_UBUNTU_LINUX) {
            try {
                result = OsVersion.UBUNTU + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + getLinuxversion();
                // NOOK:  java.lang.ArrayIndexOutOfBoundsException: 
                // result = (String)FileUtil.getFileContent(getReleaseFileName()).get(3);
                // result = result.split("\"")[1];
            } catch (Exception e) {
                // TODO ignore
            }
        } else if (OsVersion.IS_REDHAT_LINUX) {
            try {
                // result = OsVersion.REDHAT + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + StringTool.listToString(getFileContent("/etc/redhat-release"));
                result = OsVersion.REDHAT + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + StringTool.listToString(getFileContent("/etc/redhat-release"));
            } catch (IOException e) {
                // TODO ignore
            }
        } else if (IS_CENTOS_LINUX) {
            try {
                result = CENTOS + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + StringTool.listToString(getFileContent("/etc/centos-release"));
            } catch (IOException e) {
                // TODO ignore
            }
        } else if (IS_ORACLE_LINUX) {
            try {
                result = ORACLE + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + StringTool.listToString(getFileContent("/etc/oracle-release"));
            } catch (IOException e) {
                // TODO ignore
            }
        } else if (OsVersion.IS_FEDORA_LINUX) {
            try {
                result = OsVersion.FEDORA + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + StringTool.listToString(getFileContent(getReleaseFileName()));
            } catch (IOException e) {
                // TODO ignore
            }
        } else if (OsVersion.IS_MANDRAKE_LINUX) {
            try {
                result = OsVersion.MANDRAKE + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + StringTool.listToString(getFileContent(getReleaseFileName()));
            } catch (IOException e) {
                // TODO ignore
            }
        } else if (OsVersion.IS_MANDRIVA_LINUX) {
            try {
                result = OsVersion.MANDRIVA + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + StringTool.listToString(getFileContent(getReleaseFileName()));
            } catch (IOException e) {
                // TODO ignore
            }
        } else if (OsVersion.IS_DEBIAN_LINUX) {
            try {
                result = OsVersion.DEBIAN + OsVersion.SP + OsVersion.LINUX + OsVersion.NL + StringTool.listToString(getFileContent("/etc/debian_version"));
            } catch (IOException e) {
                // TODO ignore
            }
        } else {
            try {
                result = "Unknown Linux Distribution\n"
                        + StringTool.listToString(getFileContent(getReleaseFileName()));
            } catch (IOException e) {
                // TODO ignore
            }
        }
        logger.log(Level.FINE, "getLinuxDistribution result: "+ result);
        return result;
    }
    
    /**
     * returns a String which contains details of known OSs
     *
     * @return the details
     */
    public static String getOsDetails() {
        StringBuffer result = new StringBuffer();
        result.append("OS_NAME=").append(OsVersion.OS_NAME).append(OsVersion.NL);

        if (OsVersion.IS_UNIX) {
            if (OsVersion.IS_LINUX) {
                result.append(getLinuxDistribution()).append(OsVersion.NL);
            } else {
                try {
                    result.append(getFileContent(getReleaseFileName())).append(OsVersion.NL);
                } catch (IOException e) {
                	logger.log(Level.FINE, "Unable to get release file contents in 'getOsDetails'.");
                }
            }
        }

        if (OsVersion.IS_WINDOWS) {
            result.append(System.getProperty(OsVersion.OSNAME)).append(OsVersion.SP).append(System.getProperty("sun.os.patch.level", "")).append(OsVersion.NL);
        }
        logger.log(Level.FINE, "getOsDetails result: "+ result.toString());

        return result.toString();
    }
    
    private static String getLinuxMachineId() {
        String result = null;

        File machineIdFile = new File("/var/lib/dbus/machine-id");

        try {
            if (!machineIdFile.exists())
                machineIdFile = new File("/etc/machine-id");
            if (machineIdFile.exists())
                result = (String) FileUtil.getFileContent(machineIdFile.getAbsolutePath()).get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

}
