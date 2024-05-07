package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class WinRegistry {

	public static final int HKEY_CLASSES_ROOT = 0x80000000;
	public static final int HKEY_CURRENT_USER = 0x80000001;
	public static final int HKEY_LOCAL_MACHINE = 0x80000002;
	private static final String CLASSES_ROOT = "HKEY_CLASSES_ROOT";
	private static final String CURRENT_USER = "HKEY_CURRENT_USER";
	private static final String LOCAL_MACHINE = "HKEY_LOCAL_MACHINE";
	private static Preferences userRoot = Preferences.userRoot();
	private static Preferences systemRoot = Preferences.systemRoot();

	/**
	 * Reads value for the key from given path
	 * 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @param key
	 * @return the value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static String valueForKey(int hkey, String path, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, InterruptedException {
		
		return queryValueForKey(hkey, path, key);
	}


	/**
	 * Read all the subkey(s) from a given path
	 * 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @return the subkey(s) list
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws BackingStoreException 
	 * @throws IOException 
	 */
	public static List<String> subKeysForPath(int hkey, String path) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, BackingStoreException, IOException {
		List<String> result = new ArrayList<String>();
		Map<String, String> resultsMap = queryValuesForPath(hkey, path);
		Set<String> keys = resultsMap.keySet();

		// Now 'keys' contains all the keys from the map
		for (String key : keys) {
			result.add(key);
		}
		return result;
	}

	/**
	 * Create a key
	 * 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void createKey(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// int[] ret;
		if (hkey == HKEY_LOCAL_MACHINE) {
			// Create the node corresponding to the specified path
			createKey(systemRoot, key);
			// ret = createKey(systemRoot, hkey, key);
			// regCloseKey.invoke(systemRoot, new Object[] { new Integer(ret[0]) });
		} else if (hkey == HKEY_CURRENT_USER) {
			createKey(userRoot, key);
			// ret = createKey(userRoot, hkey, key);
			// regCloseKey.invoke(userRoot, new Object[] { new Integer(ret[0]) });
		} else
			throw new IllegalArgumentException("hkey=" + hkey);
		// if (ret[1] != REG_SUCCESS)
		// 	throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
	}

	/**
	 * Write a value in a given key/value name
	 * 
	 * @param hkey
	 * @param key
	 * @param valueName
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws BackingStoreException 
	 */
	public static void writeStringValue(int hkey, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, BackingStoreException {
		if (hkey == HKEY_LOCAL_MACHINE)
			writeStringValue(systemRoot, key, valueName, value);
		else if (hkey == HKEY_CURRENT_USER)
			writeStringValue(userRoot, key, valueName, value);
		else
			throw new IllegalArgumentException("hkey=" + hkey);
	}

	/**
	 * Delete a given key
	 * 
	 * @param hkey
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws BackingStoreException 
	 */
	public static void deleteKey(int hkey, String key) throws BackingStoreException {
		if (hkey == HKEY_LOCAL_MACHINE)
			deleteKey(systemRoot, key);
		else if (hkey == HKEY_CURRENT_USER)
			deleteKey(userRoot,  key);
	}

	/**
	 * delete a value from a given key/value name
	 * 
	 * @param hkey
	 * @param key
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void deleteValue(int hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// int rc = -1;
		if (hkey == HKEY_LOCAL_MACHINE)
			deleteValue(systemRoot, key, value);
		else if (hkey == HKEY_CURRENT_USER)
			deleteValue(userRoot,key, value);
		// if (rc != REG_SUCCESS)
		//	throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
	}

	// =====================

	private static void deleteValue(Preferences root, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });
		//if (handles[1] != REG_SUCCESS)
		// 	return handles[1]; // can be REG_NOTFOUND, REG_ACCESSDENIED
		//int rc = ((Integer) regDeleteValue.invoke(root, new Object[] { new Integer(handles[0]), toCstr(value) }))
		//		.intValue();
		//regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		// return rc;
        Preferences node = root.node(key);
        node.remove(value);
		
	}

	private static void deleteKey(Preferences root, String key) throws BackingStoreException {
		// Java 8
		// int rc = ((Integer) regDeleteKey.invoke(root, new Object[] { new Integer(hkey), toCstr(key) })).intValue();
		// return rc; // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
		// Open the node corresponding to the specified path
        Preferences node = root.node(key);
        node.removeNode();
	}


	
	
	private static String queryValueForKey(int hkey, String path, String key) throws IOException {
		return queryValuesForPath(hkey, path).get(key);
	}


	private static void createKey(Preferences root, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		root.node(key);
	}

	private static void writeStringValue(Preferences root, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, BackingStoreException {
		// Java 8 
//		int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });
//		regSetValueEx.invoke(root, new Object[] { new Integer(handles[0]), toCstr(valueName), toCstr(value) });
//		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		// Java 11
		Preferences node = root.node(key);
		node.put(valueName, value);
		node.flush();
	}

	/**
	 * Makes cmd query for the given hkey and path then executes the query
	 * 
	 * Usage of "reg.exe" guarantee to work in all versions of Java, at least for as long as Microsoft includes reg.exe with Windows:
	 * @param hkey
	 * @param path
	 * @return the map containing all results in form of key(s) and value(s)
	 *         obtained by executing query
	 * @throws IOException
	 */
	private static Map<String, String> queryValuesForPath(int hkey, String path) throws IOException {
		String line;
		StringBuilder builder = new StringBuilder();
		Map<String, String> map = new HashMap<String, String>();
		Process process = Runtime.getRuntime().exec("reg query \"" + getParentKey(hkey) + "\\" + path + "\"");
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while ((line = reader.readLine()) != null) {
			if (!line.contains("REG_"))
				continue;
			StringTokenizer tokenizer = new StringTokenizer(line, " \t");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.startsWith("REG_"))
					builder.append("\t ");
				else
					builder.append(token).append(" ");
			}
			String[] arr = builder.toString().split("\t");
			map.put(arr[0].trim(), arr[1].trim());
			builder.setLength(0);
		}
		return map;
	}

	/**
	 * Determines the string equivalent of hkey
	 * 
	 * @param hkey
	 * @return string equivalent of hkey
	 */
	private static String getParentKey(int hkey) {
		if (hkey == HKEY_CLASSES_ROOT)
			return CLASSES_ROOT;
		else if (hkey == HKEY_CURRENT_USER)
			return CURRENT_USER;
		else if (hkey == HKEY_LOCAL_MACHINE)
			return LOCAL_MACHINE;
		return null;
	}
}