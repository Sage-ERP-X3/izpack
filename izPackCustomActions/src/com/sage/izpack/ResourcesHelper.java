package com.sage.izpack;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.resource.DefaultLocales;

public class ResourcesHelper {

	private Resources resources;
	private LocaleDatabase langpack = null;
	private LocaleDatabase customResources = null;
	private InstallData installData;

	private static Logger logger = Logger.getLogger(ResourcesHelper.class.getName());

	public ResourcesHelper(InstallData installData, Resources resources) {
		this.installData = installData;
		this.resources = resources;
	}

	public static String getCustomPropString(String key) {

		return getCustomPropString(key, null);
	}

	public static String getCustomPropString(String key, String arg1) {

		String result = null;
		try {

			result = ResourceBundle.getBundle("/com/sage/izpack/messages").getString(key);

			logger.log(Level.FINE, "ResourcesHelper.getCustomPropString  get '" + key + "': '" + result + "'  ");

		} catch (Exception exception) {
			logger.log(Level.SEVERE, "ResourcesHelper Cannot get resource " + key + " : " + exception);
			exception.printStackTrace();
		}

		if (result != null && arg1 != null) {
			result = String.format(result, arg1);
		}

		return result;
	}

	/**
	 * Get message from SAGE izpack custom library ex:
	 * izPackCustomActions\src\com\sage\izpack\langpacks\eng.xml
	 */
	public String getCustomString(String key, boolean searchInProject) {
		// customResourcesPath = "/com/sage/izpack/langpacks/" +
		// * installData.getLocaleISO3() + ".xml"; String result = null; try { result =
		String result = null;
		if (searchInProject) {
			// result = getProjectString(key);
			// if (result != null && result!= key) {
			//	return result;
			// }
		}

		String customResourcesPath = "/com/sage/izpack/langpacks/" + this.installData.getLocaleISO3() + ".xml";
		try {

			if (this.customResources == null) {
				Locales locales = new DefaultLocales(this.resources, this.installData.getLocale());
				this.customResources = new LocaleDatabase(getClass().getResourceAsStream(customResourcesPath), locales);
			}

			result = this.customResources.get(key);
			logger.log(Level.FINE, "ResourcesHelper.getCustomString  get '" + key + "': '" + result + "'  from "
					+ customResourcesPath);

		} catch (Exception ex) {
			logger.log(Level.SEVERE,
					"ResourcesHelper Cannot get resource " + key + " " + customResourcesPath + " : " + ex);
			ex.printStackTrace();
		}

		return result;
	}

	/**
	 * Message from the project
	 * ex: C:\Source\X3\print-server225\installers\izpack\X3Print\project\i18n\PacksLangEng.xml
	 * @param key
	 * @return
	 */
	public String getProjectString(String key) {

		String result = null;
		// Load langpack. Do not stop uninstall if not found.
		try {
			if (langpack == null) {
				Locales locales = new DefaultLocales(resources, installData.getLocale());
				langpack = new LocaleDatabase(ResourcesHelper.class.getResourceAsStream("/langpack" + this.installData.getLocaleISO3() + ".xml"), locales);
			}
			result = this.langpack.get(key);
			logger.log(Level.FINE, "ResourcesHelper.getProjectString  get '" + key + "': '" + result + "'  ");

		} catch (Exception exception) {
			logger.log(Level.SEVERE, "ResourcesHelper Cannot get resource " + key + " : " + exception);
		}

		return result;
	}
}
