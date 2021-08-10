package com.izforge.izpack.util.sage;

import static com.izforge.izpack.util.sage.CTextLineUtils.toInsecable;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * 
 * 
 * @author ogattaz
 *
 */
public class CWordList {

	/**
	 * @author ogattaz
	 *
	 */
	public enum EKindOfFinding {
		//
		AT_THE_BEGINING_OF_A_LINE,
		//
		IN_ALL_THE_TEXT;
	}

	private final String pKindOfWord;

	private final CReport pReport;

	private final String[] pWordList;

	/**
	 * @param aReport
	 * @param aKindOfWord
	 * @param aWordList
	 */
	public CWordList(CReport aReport, final String aKindOfWord,
			final String[] aWordList) {
		super();
		pReport = aReport;
		pKindOfWord = aKindOfWord;
		pWordList = aWordList;

	}

	/**
	 * @param aStrings
	 * @param aLabel
	 * @return
	 */
	public String dumpAsNumberedList() {
		StringBuilder wDump = new StringBuilder();
		int wIdx = 0;
		for (String wWord : pWordList) {
			wIdx++;
			wDump.append(toInsecable(String.format("\n- %s(%2d)=[%s]",
					pKindOfWord, wIdx, wWord)));
		}
		return wDump.toString();
	}

	/**
	 * @param aLines
	 * @return
	 */
	public boolean isAllWordsIn(final String aLines,
			final EKindOfFinding aKindOfFinding) {

		if (aLines == null || aLines.isEmpty()) {
			throw new RuntimeException(String.format(
					"Unable to find a '%s' in a null or empty set of Lines",
					pKindOfWord));
		}
		// hypothesis
		boolean wAllFound = true;
		for (String wWord : pWordList) {

			if (EKindOfFinding.AT_THE_BEGINING_OF_A_LINE == aKindOfFinding) {

				if (!oneOfTheLinesStartsWith(aLines, wWord)) {
					pReport.appendError(String.format(
							"The %s [%s] is not present at the begining of one of the lines",
							pKindOfWord, wWord));
					// if at least one is not found
					wAllFound = false;
				} else {
					pReport.appendSuccess(String.format(
							"The %s [%s] is present at the begining of one of the lines",

							pKindOfWord, wWord));
				}
			}
			//
			else {
				if (!aLines.contains(wWord)) {
					pReport.appendError(String.format(
							"The %s [%s] is not present", pKindOfWord, wWord));
					// if at least one is not found
					wAllFound = false;
				} else {
					pReport.appendSuccess(String.format(
							"The %s [%s] is present", pKindOfWord, wWord));
				}
			}

		}

		return wAllFound;
	}

	/**
	 * @param aText
	 * @param aWord
	 * @return
	 */
	private boolean oneOfTheLinesStartsWith(final String aText,
			final String aWord) {
		for (String wLines : aText.split(CReport.REGEX_SPLIT_LINES)) {
			if (wLines != null && !wLines.isEmpty()
					&& wLines.startsWith(aWord)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public int size() {
		return pWordList.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("KindOfWord=[%s]:%s", pKindOfWord,
				dumpAsNumberedList());
	}
}
