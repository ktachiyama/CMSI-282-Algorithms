package huffman;

import java.io.ByteArrayOutputStream;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * Huffman instances provide reusable Huffman Encoding Maps for compressing and
 * decompressing text corpi with comparable distributions of characters.
 */
public class Huffman {

	// -----------------------------------------------
	// Construction
	// -----------------------------------------------

	private HuffNode trieRoot;
	private TreeMap<Character, String> encodingMap;

	/**
	 * Creates the Huffman Trie and Encoding Map using the character distributions
	 * in the given text corpus
	 * 
	 * @param corpus A String representing a message / document corpus with
	 *               distributions over characters that are implicitly used
	 *               throughout the methods that follow. Note: this corpus ONLY
	 *               establishes the Encoding Map; later compressed corpi may
	 *               differ.
	 */
	Huffman(String corpus) {
		TreeMap<Character, Integer> charToFrequency = new TreeMap<Character, Integer>();
		PriorityQueue<HuffNode> huffNodePQ = new PriorityQueue<HuffNode>();
		encodingMap = new TreeMap<Character, String>();

		// store characters and mapped counts in TreeMap
		for (char c : corpus.toCharArray()) {
			Integer val = charToFrequency.get(c);
			charToFrequency.put(c, (val == null) ? 1 : (val + 1));
		}

		// add HuffNodes to a PriorityQueue by iterating over TreeMap
		charToFrequency.forEach((k, v) -> {
			huffNodePQ.add(new HuffNode(k, v));
		});

		// create Huffman Trie
		while (huffNodePQ.size() > 1) {
			HuffNode leftChild = huffNodePQ.poll();
			HuffNode rightChild = huffNodePQ.poll();
			HuffNode parent = new HuffNode('\0', leftChild.count + rightChild.count);
			parent.left = leftChild;
			parent.right = rightChild;

			huffNodePQ.add(parent);
		} // 1 HuffNode left in queue
		trieRoot = huffNodePQ.poll();

		trie_to_encodingMap(trieRoot, "");
	}

	void trie_to_encodingMap(HuffNode node, String code) {
		if (node.isLeaf()) {
			encodingMap.put(node.character, code);
			return;
		}
		trie_to_encodingMap(node.left, code + "0");
		trie_to_encodingMap(node.right, code + "1");
	}

	// -----------------------------------------------
	// Compression
	// -----------------------------------------------

	/**
	 * Compresses the given String message / text corpus into its Huffman coded
	 * bitstring, as represented by an array of bytes. Uses the encodingMap field
	 * generated during construction for this purpose.
	 * 
	 * @param message String representing the corpus to compress.
	 * @return {@code byte[]} representing the compressed corpus with the Huffman
	 *         coded bytecode. Formatted as 3 components: (1) the first byte
	 *         contains the number of characters in the message, (2) the bitstring
	 *         containing the message itself, (3) possible 0-padding on the final
	 *         byte.
	 */
	public byte[] compress(String message) {
		String content = "";
		ByteArrayOutputStream byteArrOS = new ByteArrayOutputStream();

		// first byte is the message length
		byteArrOS.write(message.length());

		// convert message to compressed corpus encoding
		for (char c : message.toCharArray()) {
			content += encodingMap.get(c);
		}

		// zerosToAppend is how many 0s we will append at the end of the byte sequence
		int zerosToAppend = 8 - (content.length() % 8);

		String byteStr = "";
		for (int i = 0; i < content.length(); i++) {
			/*
			 * Every char in content represents a bit in the corpus encoding. Every 8 bits
			 * creates a byte and gets written to the byteArrayOutStream. byteStr starts
			 * over as an empty string until 8 more bits get added
			 */
			if (i % 8 == 0 && byteStr != "") {
				byteArrOS.write(binaryString_to_decimalInt(byteStr));
				byteStr = "";
			}
			byteStr += content.charAt(i);
		}

		// The last byte is the rest of the corpus encoding plus the appended 0s
		for (; zerosToAppend > 0; zerosToAppend--) {
			byteStr += "0";
		}
		byteArrOS.write(binaryString_to_decimalInt(byteStr));

		return byteArrOS.toByteArray();
	}

	Integer binaryString_to_decimalInt(String binary) {
		return (binary.charAt(0) == '1') ? (-256 + Integer.parseInt(binary, 2)) : Integer.parseInt(binary, 2);
	}

	// -----------------------------------------------
	// Decompression
	// -----------------------------------------------

	/**
	 * Decompresses the given compressed array of bytes into their original, String
	 * representation. Uses the trieRoot field (the Huffman Trie) that generated the
	 * compressed message during decoding.
	 * 
	 * @param compressedMsg {@code byte[]} representing the compressed corpus with
	 *                      the Huffman coded bytecode. Formatted as 3 components:
	 *                      (1) the first byte contains the number of characters in
	 *                      the message, (2) the bitstring containing the message
	 *                      itself, (3) possible 0-padding on the final byte.
	 * @return Decompressed String representation of the compressed bytecode
	 *         message.
	 */
	public String decompress(byte[] compressedMsg) {
		int contentLength = compressedMsg[0];
		String code = "";

		for (int i = 1; i < compressedMsg.length; i++) {
			code += decimalInt_to_binaryString(compressedMsg[i]);
		}

		return decompressAux(trieRoot, code, contentLength, "");
	}

	String decompressAux(HuffNode node, String code, int length, String result) {
		if (length == 0) {
			return result;
		}
		if (node.isLeaf()) {
			result += node.character;
			length--;
			node = trieRoot;
		}

		return (code.charAt(0) == '1') ? decompressAux(node.right, code.substring(1), length, result)
				: decompressAux(node.left, code.substring(1), length, result);
	}

	String decimalInt_to_binaryString(int decimal) {
		String binary = Integer.toBinaryString((decimal < 0) ? (decimal + 256) : decimal);
		while (binary.length() < 8) { // if the binary string is not 8 chars
			binary = "0" + binary; // long, prepend 0s until is is.
		}

		return binary;
	}

	// -----------------------------------------------
	// Huffman Trie
	// -----------------------------------------------

	/**
	 * Huffman Trie Node class used in construction of the Huffman Trie. Each node
	 * is a binary (having at most a left and right child), contains a character
	 * field that it represents (in the case of a leaf, otherwise the null character
	 * \0), and a count field that holds the number of times the node's character
	 * (or those in its subtrees, in the case of inner nodes) appear in the corpus.
	 */
	private static class HuffNode implements Comparable<HuffNode> {

		HuffNode left, right;
		char character;
		int count;

		HuffNode(char character, int count) {
			this.count = count;
			this.character = character;
		}

		public boolean isLeaf() {
			return left == null && right == null;
		}

		public int compareTo(HuffNode other) {
			return this.count - other.count;
		}

	}

}
