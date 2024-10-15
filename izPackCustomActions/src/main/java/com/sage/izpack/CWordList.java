package com.sage.izpack;

import static com.sage.izpack.CTextLineUtils.toInsecable;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 */
public class CWordList {

	public enum EKindOfFinding {
		AT_THE_BEGINING_OF_A_LINE,
		IN_ALL_THE_TEXT;
	}

	private final String pKindOfWord;

	private final CReport pReport;

	private final String[] pWordList;

	private String friendlySuccessMesg;
	private String friendlyWarningMesg;

	/**
	 * @param aReport
	 * @param aKindOfWord
	 * @param aWordList
	 */
	public CWordList(CReport aReport, final String aKindOfWord, final String[] aWordList) {
		super();
		pReport = aReport;
		pKindOfWord = aKindOfWord;
		pWordList = aWordList;

	}

	public void SetFriendlySuccessMsg(String friendlyMsg) {
		this.friendlySuccessMesg = friendlyMsg;
	}

	public void SetFriendlyWarningMsg(String friendlyMsg) {
		this.friendlyWarningMesg = friendlyMsg;
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
			wDump.append(toInsecable(String.format("\n- %s(%2d)=[%s]", pKindOfWord, wIdx, wWord)));
		}
		return wDump.toString();
	}

	/**
	 * @param aLines
	 * @return
	 */
	/*
	public String[] missingWordsIn(final String aLines, final EKindOfFinding aKindOfFinding) {

		if (aLines == null || aLines.isEmpty()) {
			throw new RuntimeException(
					String.format("Unable to find a '%s' in a null or empty set of Lines", pKindOfWord));
		}
		List<String> result = new ArrayList<String>();
		boolean wAllFound = true;
		for (String wWord : pWordList) {

			if (EKindOfFinding.AT_THE_BEGINING_OF_A_LINE == aKindOfFinding) {

				if (!oneOfTheLinesStartsWith(aLines, wWord)) {
					if (this.friendlyWarningMesg != null && !this.friendlyWarningMesg.isEmpty()) {
						pReport.appendError(String.format(this.friendlyWarningMesg, wWord));
					} else {
						pReport.appendError(String.format("The %s [%s] is not present", pKindOfWord, wWord));
					}
					// if at least one is not found
					wAllFound = false;
				} else {
					if (this.friendlySuccessMesg != null && !this.friendlySuccessMesg.isEmpty()) {
						pReport.appendSuccess(String.format(this.friendlySuccessMesg, wWord));
					} else {
						pReport.appendSuccess(String.format("The %s [%s] is present", pKindOfWord, wWord));
					}
				}
			}
			//
			else {
				if (!aLines.contains(wWord)) {
					result.add(wWord);
					pReport.appendError(String.format("The %s [%s] is not present", pKindOfWord, wWord));
					// if at least one is not found
					wAllFound = false;
				} else {
					pReport.appendSuccess(String.format("The %s [%s] is present", pKindOfWord, wWord));
				}
			}
		}

		return (String[]) result.toArray();
	}
*/
	/**
	 * @param aLines
	 * @return
	 */
	public boolean isAllWordsIn(final String aLines, final EKindOfFinding aKindOfFinding) {

		if (aLines == null || aLines.isEmpty()) {
			throw new RuntimeException(
					String.format("Unable to find a '%s' in a null or empty set of Lines", pKindOfWord));
		}
		// hypothesis
		boolean wAllFound = true;
		for (String wWord : pWordList) {

			if (EKindOfFinding.AT_THE_BEGINING_OF_A_LINE == aKindOfFinding) {

				if (!oneOfTheLinesStartsWith(aLines, wWord)) {
					if (this.friendlyWarningMesg != null && !this.friendlyWarningMesg.isEmpty()) {
						pReport.appendError(String.format(this.friendlyWarningMesg, wWord));
					} else {
						pReport.appendError(String.format("The %s [%s] has NOT been found.", pKindOfWord, wWord));
					}
					// if at least one is not found
					wAllFound = false;
				} else {
					if (this.friendlySuccessMesg != null && !this.friendlySuccessMesg.isEmpty()) {
						pReport.appendSuccess(String.format(this.friendlySuccessMesg, wWord));
					} else {
						pReport.appendSuccess(String.format("The %s [%s] has been found.", pKindOfWord, wWord));
					}
				}
			}
			//
			else {
				if (!aLines.contains(wWord)) {
					pReport.appendError(String.format("The %s [%s] is not present", pKindOfWord, wWord));
					// if at least one is not found
					wAllFound = false;
				} else {
					pReport.appendSuccess(String.format("The %s [%s] is present", pKindOfWord, wWord));
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
	private boolean oneOfTheLinesStartsWith(final String aText, final String aWord) {
		for (String wLines : aText.split(CReport.REGEX_SPLIT_LINES)) {
			if (wLines != null && !wLines.isEmpty() && wLines.trim().startsWith(aWord)) {
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
		return String.format("KindOfWord=[%s]:%s", pKindOfWord, dumpAsNumberedList());
	}
}
