package com.sage.izpack;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;

/**
 * In izpack4 {@link InstallData#MODIFY_INSTALLATION} variable was in upper case, in version 5 it got changed to lower case.
 * This is util is for backward compatibility. It reads the variable with a fallback to izpack4.
 */
public abstract class ModifyInstallationUtil {
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	public static void set(InstallData data, boolean value) {
		ModifyInstallationUtil.set(data, value ? TRUE : FALSE);
	}

	public static void set(InstallData data, String value) {
		assert (TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value)) : "Only true|false value is allowed.";
		data.setVariable(InstallData.MODIFY_INSTALLATION, value.toLowerCase());
	}
	
	public static Boolean get(IXMLElement panelRoot) {
		IXMLElement oldVal = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION.toUpperCase());
		if (oldVal != null && oldVal.getContent() != null && TRUE.equalsIgnoreCase(oldVal.getContent().trim())) {
			return Boolean.TRUE;
		}
		IXMLElement newVal = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION.toLowerCase());
		if (newVal != null && newVal.getContent() != null && TRUE.equalsIgnoreCase(newVal.getContent().trim())) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public static Boolean get(InstallData data) {
		return TRUE.equalsIgnoreCase(data.getVariable(InstallData.MODIFY_INSTALLATION.toUpperCase()))
				|| TRUE.equalsIgnoreCase(data.getVariable(InstallData.MODIFY_INSTALLATION.toLowerCase()));
	}
}
