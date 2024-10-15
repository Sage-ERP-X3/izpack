package com.sage.izpack;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.resource.DefaultLocales;

/*
 * @author Franck DEPOORTERE
 */
public class ResourcesHelper {

	private Resources resources = null;
	private LocaleDatabase langpack = null;
	private LocaleDatabase customResources = null;
	private String customResourcesPath = null;
	private InstallData installData;
	private static Logger logger = Logger.getLogger(ResourcesHelper.class.getName());
	private static String LogPrefix = "ResourcesHelper - ";

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

			logger.log(Level.FINE, LogPrefix + "getCustomPropString  get '" + key + "': '" + result + "'  ");

		} catch (Exception exception) {
			logger.log(Level.SEVERE, LogPrefix + "Cannot get resource " + key + " : " + exception);
			exception.printStackTrace();
		}

		if (result != null && arg1 != null) {
			result = String.format(result, arg1);
		}

		return result;
	}

	private String getLocaleIso3() {
		if (this.installData != null)
			return this.installData.getLocaleISO3();
		return getLocale().getISO3Language();
	}

	private Locale getLocale() {

		Locale result = null;
		if (this.installData != null)
			result = installData.getLocale();
		if (result == null)
			result = Locale.getDefault();
		if (result == null)
			result = Locale.ENGLISH;
		return result;

	}

	/*
	 * Merge customized resources with the standard resources
	 */
	public void mergeCustomMessages() {
		mergeCustomMessages(installData.getMessages());
	}

	public void mergeCustomMessages(Messages messages) {

		initializeResources();

		logger.log(Level.FINE, LogPrefix + "mergeCustomMessages  from " + this.customResourcesPath + "  GetLocale():"
				+ getLocaleIso3());

		Messages messagesM = messages;
		if (this.customResources != null)
			messagesM.add(customResources);

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
		try {

			result = this.customResources.get(key);
			if (result != null && arg1 != null) {
				result = String.format(result, arg1);
			}
			logger.log(Level.FINE, LogPrefix + "getCustomString  get '" + key + "': '" + result + "'  from "
					+ this.customResourcesPath + "  GetLocale():" + getLocaleIso3());

		} catch (Exception ex) {
			logger.log(Level.SEVERE, LogPrefix + "Cannot get resource " + key + " " + this.customResourcesPath
					+ "GetLocale(): " + getLocaleIso3() + " : " + ex);
			ex.printStackTrace();
		}

		return result;
	}

	private String getSafeLocale() {

		final String defaultLangISO3 = getLocale().getISO3Language(); // "eng";
		final String defaultLangISO2 = getLocale().getLanguage(); // "en";

		String lang = defaultLangISO3;
		if (this.installData != null)
			lang = this.installData.getLocaleISO3();

		if (lang == null) {
			if (this.installData != null && this.installData.getLocale() != null) {
				lang = this.installData.getLocale().getISO3Language();
			}
			if (lang == null && this.installData != null) {
				lang = this.installData.getVariable("ISO3_LANG");
			}
			if (lang == null) {
				lang = defaultLangISO3;
				logger.log(Level.FINE, LogPrefix + "getSafeLocale.  Force ISO3_LANG=" + lang);
			}
		}

		if (this.installData != null) {
			if (this.installData.getVariable("ISO3_LANG") == null)
				this.installData.setVariable("ISO3_LANG", lang);

			if (this.installData.getVariable("ISO2_LANG") == null)
				this.installData.setVariable("ISO2_LANG", defaultLangISO2);

			if (this.installData.getLocale() == null) {
				if (this.installData instanceof AutomatedInstallData) {
					((AutomatedInstallData) this.installData).setLocale(Locale.ENGLISH, lang);
				}

				// Locales locales = new DefaultLocales(resources, Locale.ENGLISH);
			}
		}

		return lang;
	}

	private void initializeResources() {

		String lang = getSafeLocale();// this.installData.getLocaleISO3();
		try {

			if (this.customResourcesPath == null) {
				this.customResourcesPath = "/com/sage/izpack/langpacks/" + lang + ".xml";
			}
			if (this.customResources == null) {
				Locales locales = new DefaultLocales(this.resources, getLocale());
				this.customResources = new LocaleDatabase(getClass().getResourceAsStream(customResourcesPath), locales);
			}

			logger.log(Level.FINE, LogPrefix + "getCustomString  initialized from " + customResourcesPath
					+ "  GetSafeLocale():" + lang);

		} catch (Exception ex) {
			logger.log(Level.SEVERE,
					LogPrefix + "Cannot be initialized " + customResourcesPath + "GetLocale(): " + lang + " : " + ex);
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
			logger.log(Level.FINE, LogPrefix + "getProjectString  get '" + key + "': '" + result + "'  ");

		} catch (Exception exception) {
			logger.log(Level.SEVERE, LogPrefix + "Cannot get resource " + key + " : " + exception);
		}

		return result;
	}
}
