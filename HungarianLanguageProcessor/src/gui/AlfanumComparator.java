package gui;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A handy inner class to make order between files that are named the same, but the digit is 
 * different at the end. By default, the ordering happens according to a number right before
 * the extension (e.g.: .txt), and after an underscore (_). For example: "Something12_44.txt"
 * will consider 44 "_33.dssp" will consider 33.
 * If you want to change the rule, you can use the {@link #AlfanumComparator(String)} constructor. 
 * <p>
 * It is possible to use this class only for extracting the number of a given string, by using the {@link #extractInt(String)} method.
 * 
 * @author Berci
 *
 */
public class AlfanumComparator implements Comparator<File> {
	
	private Pattern pattern = Pattern.compile(".*_(\\d+)\\..*");
	
	/**
	 * Creates a new AlfanumComparator with the default pattern.
	 */
	public AlfanumComparator() {
	}
	
	/**
	 * Creates a new AlfanumComparator with a given rule. In the 
	 * given regex, you must group the substring where the number is expected, 
	 * i.e. put in parenthesis. E.g.: ".*_(\\d+)\\..*"
	 * @param pattern the new regex pattern
	 */
	public AlfanumComparator(String pattern){
		this.pattern = Pattern.compile(pattern);
	}

	@Override
	public int compare(File o1, File o2) {
		return extractInt(o1.getName()) - extractInt(o2.getName());
	}

	/**
	 * Extracts a number from the  given string according to the given pattern.
	 * @param s the given string
	 * @return the extracted number
	 */
	public int extractInt(String s) {
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()){
			String g = matcher.group(1);
			return Integer.parseInt(g);
		}else {
			return 0;
		}
	}
	
	

}
