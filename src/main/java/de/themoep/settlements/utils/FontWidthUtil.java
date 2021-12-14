package de.themoep.settlements.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * DO WHAT YOU WANT TO PUBLIC LICENSE
 * Credit to angelsl for the original code.
 */
@SuppressWarnings("all")
public class FontWidthUtil {

	private static String weirdHardcodedStuffIFoundInMinecraft = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~¦ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×áíóúñÑªº¿®¬½¼¡«»";
	private static int[] charWidth = {1,9,9,8,8,8,8,7,9,8,9,9,8,9,9,9,8,8,8,8,9,9,8,9,8,8,8,8,8,9,9,9,4,2,5,6,6,6,6,3,5,5,5,6,2,6,2,6,6,6,6,6,6,6,6,6,6,6,2,2,5,6,5,6,7,6,6,6,6,6,6,6,6,4,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,4,6,4,6,6,3,6,6,6,6,6,5,6,6,2,6,5,3,6,6,6,6,6,6,6,4,6,6,6,6,6,6,5,2,5,7,6,6,6,6,6,6,6,6,6,6,6,6,4,6,3,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,4,6,6,3,6,6,6,6,6,6,6,7,6,6,6,2,6,6,8,9,9,6,6,6,8,8,6,8,8,8,8,8,6,6,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,6,9,9,9,5,9,9,8,7,7,8,7,8,8,8,7,8,8,7,9,9,6,7,7,7,7,7,9,6,7,8,7,6,6,9,7,6,7,1};

	// Custom function to get the maximum width of the string.
	public static int getMaxChatWidth()
	{
		return getWidth("---------------------------------------------------");
	}

	// Custom function to get the maximum width of the string.
	public static int getMaxBookWidth()
	{
		return getWidth("-------------------");
	}


	// taken directly from notchcode. enjoy
	public static int getWidth(String s) {
		if (s == null) {
			return 0;
		}
		int i = 0;
		for (int j = 0; j < s.length(); j++) {
			if (s.charAt(j) == '\247') {
				j++;
				continue;
			}
			i += getWidth(s.charAt(j));
		}

		return i;
	}

	public static int getWidth(char c) {
		int k = weirdHardcodedStuffIFoundInMinecraft.indexOf(c);
		if (k >= 0) {
			return charWidth[k + 32];
		}
		return 0;
	}

	public static String shorten(String text, int width) {
		if (getWidth(text) <= width) {
			return text;
		}
		String s = split(text, width - getWidth("...")).get(0);
		if (s.endsWith(" ")) {
			s = s.substring(0, s.length() - 1);
		}
		return s + "...";
	}

	private static List<String> split(String text, int width) {
		return split(text, width, 0);
	}

	private static List<String> split(String text, int width, int amount) {
		List<String> parts = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		int currentWidth = 0;
		for (char c : text.toCharArray()) {
			int charWidth = getWidth(c);
			if (currentWidth + charWidth > width && (amount < 1 || parts.size() + 1 < amount)) {
				currentWidth = 0;
				parts.add(sb.toString());
				sb = new StringBuilder();
			}
			currentWidth += charWidth;
			sb.append(c);
		}
		parts.add(sb.toString());
		return parts;
	}

	public static String wrap(String text, int width, boolean splitLongWords) {
		if (getWidth(text) <= width) {
			return text;
		}
		List<String> lines = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		int spaceWidth = getWidth(' ');
		int dashWidth = getWidth('-');
		int currentWidth = 0;
		for (String word : text.split(" ")) {
			int wordWidth = getWidth(word);
			if (currentWidth + wordWidth > width) {
				if (splitLongWords && wordWidth > width) {
					List<String> parts = split(word, wordWidth - width - dashWidth, 2);
					sb.append(parts.get(0)).append('-');
					lines.add(sb.toString());

					List<String> restParts = split(parts.get(1), width - dashWidth);
					for (int i = 0; i + 1 < restParts.size(); i++) {
						restParts.add(restParts.get(i) + "-");
					}

					sb = new StringBuilder();
					currentWidth = 0;
					word = restParts.get(restParts.size() - 1);
				} else {
					lines.add(sb.toString());
					sb = new StringBuilder();
					currentWidth = 0;
				}
			} else if (currentWidth > 0) {
				sb.append(" ");
				currentWidth += spaceWidth;
			}

			currentWidth += getWidth(word);
			sb.append(word);
		}
		lines.add(sb.toString());
		return String.join("\n", lines);
	}
}