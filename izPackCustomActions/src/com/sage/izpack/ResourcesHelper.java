package com.sage.izpack;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.resource.DefaultLocales;

public class ResourcesHelper {

	private Resources resources = null;
	private LocaleDatabase langpack = null;
	private LocaleDatabase customResources = null;
	private String customResourcesPath = null;
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

	/*
	 * Merge customized resources with the standard resources
	 */
	public void mergeCustomMessages() {

		initializeResources();
		// if (this.customResourcesPath == null || this.customResources == null) {
		// getCustomString("TEST", true);
		// }
		logger.log(Level.FINE, "ResourcesHelper.mergeCustomMessages  from " + this.customResourcesPath
				+ "  GetLocale():" + this.installData.getLocaleISO3());

		Messages messagesM = installData.getMessages();
		if (this.customResources != null)
			messagesM.add(customResources);

		/*
		 * Map<String, String> newMessages = new HashMap<String, String>(); Map<String,
		 * String> messages = messagesM.getMessages(); messages.forEach((mesgKey,
		 * mesgValue) -> {
		 * 
		 * String customMesg = getCustomString(mesgKey, true); if (customMesg != null) {
		 * // messages.replace(mesgKey, customMesg); // newMessages.put(mesgKey,
		 * customMesg);
		 * 
		 * logger.log(Level.FINE,
		 * "ResourcesHelper.mergeCustomMessages  replace custom '" + mesgKey + "': '" +
		 * customMesg + "'  from " + this.customResourcesPath + "  GetLocale():" +
		 * this.installData.getLocaleISO3()); } else { newMessages.put(mesgKey,
		 * mesgValue);
		 * 
		 * logger.log(Level.FINE, "ResourcesHelper.mergeCustomMessages  keep '" +
		 * mesgKey + "': '" + mesgValue + "'  from " + this.customResourcesPath +
		 * "  GetLocale():" + this.installData.getLocaleISO3());
		 * 
		 * } });
		 * 
		 */

	}

	/**
	 * Get message from SAGE izpack custom library ex:
	 * izPackCustomActions\src\com\sage\izpack\langpacks\eng.xml
	 */
	public String getCustomString(String key) {
		return getCustomString(key, null);
	}

	public String getCustomString(String key, String arg1) {

		initializeResources();

		String result = null;

		// this.customResourcesPath = "/com/sage/izpack/langpacks/" +
		// this.installData.getLocaleISO3() + ".xml";
		try {

			// if (this.customResources == null) {
			// Locales locales = new DefaultLocales(this.resources,
			// this.installData.getLocale());
			// this.customResources = new
			// LocaleDatabase(getClass().getResourceAsStream(customResourcesPath), locales);
			// }

			result = this.customResources.get(key);
			if (result != null && arg1 != null) {
				result = String.format(result, arg1);
			}
			logger.log(Level.FINE, "ResourcesHelper.getCustomString  get '" + key + "': '" + result + "'  from "
					+ this.customResourcesPath + "  GetLocale():" + this.installData.getLocaleISO3());

		} catch (Exception ex) {
			logger.log(Level.SEVERE, "ResourcesHelper Cannot get resource " + key + " " + this.customResourcesPath
					+ "GetLocale(): " + this.installData.getLocaleISO3() + " : " + ex);
			ex.printStackTrace();
		}

		return result;
	}

	private void initializeResources() {

		String lang = this.installData.getLocaleISO3();
		try {

			if (this.customResourcesPath == null) {
				if (lang == null) {
					if (this.installData.getLocale() != null) {
						lang = this.installData.getLocale().getISO3Language();
					}
					if (lang == null) {
						lang = this.installData.getVariable("ISO3_LANG");
					}
					if (lang == null) {
						lang = "ENG";
					}
				}
				this.customResourcesPath = "/com/sage/izpack/langpacks/" + lang + ".xml";
			}
			if (this.customResources == null) {
				Locales locales = new DefaultLocales(this.resources, this.installData.getLocale());
				this.customResources = new LocaleDatabase(getClass().getResourceAsStream(customResourcesPath), locales);
			}

			logger.log(Level.FINE, "ResourcesHelper.getCustomString  initialized from " + customResourcesPath
					+ "  GetLocale():" + lang);

		} catch (Exception ex) {
			logger.log(Level.SEVERE, "ResourcesHelper Cannot be initialized " + customResourcesPath + "GetLocale(): "
					+ lang + " : " + ex);
			ex.printStackTrace();
		}
	}

	/**
	 * Message from the project ex:
	 * C:\Source\X3\print-server225\installers\izpack\X3Print\project\i18n\PacksLangEng.xml
	 * 
	 * @param key
	 * @return
	 */
	public String getProjectString(String key) {

		String result = null;
		// Load langpack. Do not stop uninstall if not found.
		try {
			if (langpack == null) {
				Locales locales = new DefaultLocales(resources, installData.getLocale());
				langpack = new LocaleDatabase(ResourcesHelper.class
						.getResourceAsStream("/langpack" + this.installData.getLocaleISO3() + ".xml"), locales);
			}
			result = this.langpack.get(key);
			logger.log(Level.FINE, "ResourcesHelper.getProjectString  get '" + key + "': '" + result + "'  ");

		} catch (Exception exception) {
			logger.log(Level.SEVERE, "ResourcesHelper Cannot get resource " + key + " : " + exception);
		}

		return result;
	}
}
