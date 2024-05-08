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

public class WinRegistry {

	public static final int HKEY_CLASSES_ROOT = 0x80000000;
	public static final int HKEY_CURRENT_USER = 0x80000001;
	public static final int HKEY_LOCAL_MACHINE = 0x80000002;
	private static final String CLASSES_ROOT = "HKEY_CLASSES_ROOT";
	private static final String CURRENT_USER = "HKEY_CURRENT_USER";
	private static final String LOCAL_MACHINE = "HKEY_LOCAL_MACHINE";

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
	
	private static String queryValueForKey(int hkey, String path, String key) throws IOException {
		return queryValuesForPath(hkey, path).get(key);
	}



	/**
	 * Makes cmd query for the given hkey and path then executes the query
	 * 
	 * Java 17: Usage of "reg.exe" guarantee to work in all versions of Java, at least for as long as Microsoft includes reg.exe with Windows:
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