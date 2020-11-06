package spellex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class SpellEx {

	// Note: Not quite as space-conscious as a Bloom Filter,
	// nor a Trie, but since those aren't in the JCF, this map
	// will get the job done for simplicity of the assignment
	private Map<String, Integer> dict;

	// For your convenience, you might need this array of the
	// alphabet's letters for a method
	private static final char[] LETTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

	/**
	 * Constructs a new SpellEx spelling corrector from a given "dictionary" of
	 * words mapped to their frequencies found in some corpus (with the higher
	 * counts being the more prevalent, and thus, the more likely to be suggested)
	 * 
	 * @param words The map of words to their frequencies
	 */
	SpellEx(Map<String, Integer> words) {
		dict = new HashMap<>(words);
	}

	/**
	 * Returns the edit distance between the two input Strings s0 and s1 based on
	 * the minimal number of insertions, deletions, replacements, and transpositions
	 * required to transform s0 into s1
	 * 
	 * @param s0 A "start" String
	 * @param s1 A "destination" String
	 * @return The minimal edit distance between s0 and s1
	 */
	public static int editDistance(String s0, String s1) {

		// create bottom up table
		int[][] distanceTable = new int[s0.length() + 1][s1.length() + 1];

		// initialize gutters
		for (int row = 0; row < distanceTable.length; row++) {
			distanceTable[row][0] = row;
		}
		for (int col = 0; col < distanceTable[0].length; col++) {
			distanceTable[0][col] = col;
		}

		// Fill in rest of table using bottom-up dynamic-programming approach
		for (int row = 1; row < distanceTable.length; row++) {
			for (int col = 1; col < distanceTable[row].length; col++) {

				// if transposition is possible, find the min between
				// insertion, deletion, replacement, and transposition
				if (row >= 2 && col >= 2 && s0.charAt(row - 1) == s1.charAt(col - 2)
						&& s0.charAt(row - 2) == s1.charAt(col - 1)) {
					distanceTable[row][col] = min(distanceTable[row][col - 1] + 1, // insertion
							distanceTable[row - 1][col] + 1, // deletion
							distanceTable[row - 1][col - 1] + ((s0.charAt(row - 1) == s1.charAt(col - 1)) ? 0 : 1), // replacement
							distanceTable[row - 2][col - 2] + 1); // transposition
				}
				// if transposition is not possible, then just find the
				// min between insertion, deletion, and replacement
				else {
					distanceTable[row][col] = min(distanceTable[row][col - 1] + 1, // insertion
							distanceTable[row - 1][col] + 1, // deletion
							distanceTable[row - 1][col - 1] + ((s0.charAt(row - 1) == s1.charAt(col - 1)) ? 0 : 1));// replacement
				}
			}
		}
		// return final value in table
		return distanceTable[distanceTable.length - 1][distanceTable[0].length - 1];
	}

	/**
	 * Takes in an unspecified number of arguments and returns their minimum value
	 * 
	 * @param nums A list of integers
	 * @return The minimum value out of the list nums
	 */
	static int min(int... nums) {
		int min_val = nums[1];
		for (int var : nums) {
			min_val = (var < min_val) ? var : min_val;
		}
		return min_val;
	}

	/**
	 * Returns all of the possible strings that are 1 edit distance away via
	 * insertion, deletion, replacement, and transposition
	 * 
	 * @return An ArrayList of every string 1 edit distance away from word
	 */
	static ArrayList<String> words1EditDistanceAway(String word) {
		ArrayList<String> wordsList = new ArrayList<String>();

		///////// insertion //////////
		int new_length = word.length() + 1;

		for (int index = 0; index < new_length; index++) {
			for (char c : LETTERS) {
				StringBuffer temp = new StringBuffer(word);
				temp.insert(index, c);
				wordsList.add(temp.toString());
			}
		}

		///////// deletion //////////
		for (int index = 0; index < word.length(); index++) {
			StringBuffer temp = new StringBuffer(word);
			temp.deleteCharAt(index);
			wordsList.add(temp.toString());
		}

		///////// replacement //////////
		for (int index = 0; index < word.length(); index++) {
			for (char c : LETTERS) {
				StringBuffer temp = new StringBuffer(word);
				temp.replace(index, index + 1, String.valueOf(c));
				wordsList.add(temp.toString());
			}
		}

		///////// transposition //////////
		for (int index = 0; index < word.length() - 1; index++) {
			char[] charArray = word.toCharArray();
			char[] temp_str = charArray;
			char temp_char = temp_str[index];
			temp_str[index] = temp_str[index + 1];
			temp_str[index + 1] = temp_char;

			wordsList.add(String.copyValueOf(temp_str));
		}

		return wordsList;
	}

	/**
	 * Returns the n closest words in the dictionary to the given word, where
	 * "closest" is defined by:
	 * <ul>
	 * <li>Minimal edit distance (with ties broken by:)
	 * <li>Largest count / frequency in the dictionary (with ties broken by:)
	 * <li>Ascending alphabetic order
	 * </ul>
	 * 
	 * @param word The word we are comparing against the closest in the dictionary
	 * @param n    The number of least-distant suggestions desired
	 * @return A set of up to n suggestions closest to the given word
	 */
	public Set<String> getNLeastDistant(String word, int n) {
		Set<String> results = new HashSet<String>();

		PriorityQueue<SpellCheckTriplet> distancePriorityQ = new PriorityQueue<SpellCheckTriplet>();

		for (Map.Entry<String, Integer> dict_entry : dict.entrySet()) {
			String dict_word = dict_entry.getKey();
			int frequency = dict_entry.getValue();
			int distance = editDistance(word, dict_entry.getKey());

			distancePriorityQ.add(new SpellCheckTriplet(dict_word, frequency, distance));
		}

		for (int i = 0; i < n; i++) {
			results.add(distancePriorityQ.poll().getWord());
		}

		return results;
	}

	/**
	 * Returns the set of n most frequent words in the dictionary to occur with edit
	 * distance distMax or less compared to the given word. Ties in max frequency
	 * are broken with ascending alphabetic order.
	 * 
	 * @param word    The word to compare to those in the dictionary
	 * @param n       The number of suggested words to return
	 * @param distMax The maximum edit distance (inclusive) that suggested /
	 *                returned words from the dictionary can stray from the given
	 *                word
	 * @return The set of n suggested words from the dictionary with edit distance
	 *         distMax or less that have the highest frequency.
	 */
	public Set<String> getNBestUnderDistance(String word, int n, int distMax) {
		Set<String> results = new HashSet<String>();
		ArrayList<String> wordsArray = new ArrayList<String>();
		PriorityQueue<SpellCheckPair> frequencyPriorityQ = new PriorityQueue<SpellCheckPair>();

		wordsArray.add(word);

		/**
		 * words1EditDistanceAway() takes a string and returns all possible strings that
		 * are 1 edit distance away. When this set of strings is passed through the same
		 * function, we get the set of strings that is 2 edit distances away from our
		 * original parameter. This process continues until all strings @distMax edit
		 * distances away are found
		 */

		for (int iterations = 0; iterations < distMax; iterations++) {
			ArrayList<String> temp = new ArrayList<String>();
			for (String s : wordsArray) {
				temp.addAll(words1EditDistanceAway(s));
			}
			wordsArray.addAll(temp);
		}

		for (String s : wordsArray) {
			// If the key s exists in the map, frequency will be set to its corresponding
			// value. If s does not exist, frequency will be defaulted to -1 instead
			int frequency = dict.getOrDefault(s, -1);
			if (frequency != -1) {
				frequencyPriorityQ.add(new SpellCheckPair(s, frequency));
			}
		}

		while (results.size() < n && !frequencyPriorityQ.isEmpty()) {
			results.add(frequencyPriorityQ.poll().getWord());
		}

		return results;
	}

}

/**
 * SpellCheckTriplet contains 3 fields: word, frequency, and distance. The class
 * implements Comparable and is sorted by minimal distance with ties broken by
 * largest frequency, with ties broken by alphabetical order
 */
class SpellCheckTriplet implements Comparable<SpellCheckTriplet> {

	final String word;
	final int frequency;
	final int distance;

	public SpellCheckTriplet(String word, int frequency, int distance) {
		this.word = word;
		this.frequency = frequency;
		this.distance = distance;
	}

	public String getWord() {
		return word;
	}

	public int compareTo(SpellCheckTriplet other) {
		if (this.distance == other.distance && this.frequency == other.frequency) {
			return this.word.compareTo(other.word);
		} else if (this.distance == other.distance) {
			return other.frequency - this.frequency;
		} else
			return this.distance - other.distance;
	}
}

/**
 * SpellCheckPair contains 2 fields: word, frequency. The class implements
 * Comparable and is sorted by largest frequency with ties broken by
 * alphabetical order
 */
class SpellCheckPair implements Comparable<SpellCheckPair> {
	final String word;
	final int frequency;

	public SpellCheckPair(String word, int frequency) {
		this.word = word;
		this.frequency = frequency;
	}

	public String getWord() {
		return word;
	}

	public int compareTo(SpellCheckPair other) {
		if (this.frequency == other.frequency) {
			return this.word.compareTo(other.word);
		} else
			return other.frequency - this.frequency;
	}
}
