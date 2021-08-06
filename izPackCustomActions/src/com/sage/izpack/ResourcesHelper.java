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
		} catch (Exception exception) {
			logger.log(Level.SEVERE, "ResourcesHelper Cannot get resource " + key + " : " + exception);
			exception.printStackTrace();
		}

		if (result != null && arg1 != null) {
			result = String.format(result, arg1);
		}

		return result;
	}
	

	public String getProjectString(String key) {

		String result = key;
		if (langpack == null) {
			// Load langpack. Do not stop uninstall if not found.
			try {
				Locales locales = new DefaultLocales(resources, installData.getLocale());
				langpack = new LocaleDatabase(ResourcesHelper.class.getResourceAsStream("/langpack.xml"), locales);
				return langpack.getOrDefault(key, key);
			} catch (Exception exception) {
				logger.log(Level.SEVERE, "ResourcesHelper Cannot get resource " + key + " : " + exception);
			}
		}

		return result;
	}

	/**
	 * Get message from SAGE izpack custom library
	 */
	public String getCustomString(String key) {
		 // customResourcesPath = "/com/sage/izpack/langpacks/" +
		//		 * installData.getLocaleISO3() + ".xml"; String result = null; try { result =
		String result = null;
		String customResourcesPath = "/com/sage/izpack/langpacks/" + this.installData.getLocaleISO3() + ".xml";
		try {

		if (this.customResources == null) {
			Locales locales = new DefaultLocales(this.resources, this.installData.getLocale());
			this.customResources = new LocaleDatabase(getClass().getResourceAsStream(customResourcesPath), locales);
		}

			result = this.customResources.get(key);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "ResourcesHelper Cannot get resource " + key + " " + customResourcesPath + " : " + ex);
			ex.printStackTrace();
		}

		return result;
	}
}
