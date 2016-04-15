package gui;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import gui.WordCollection.Word;

/**
 * A class for doing statistical analysis on a single {@link Word} object. 
 * Different statistical results can be printed to a given PrintStream. 
 * @author Berci
 *
 */
public class WordAnalyzer {
	private Word w;

	//Primary storage of objects
	private LinkedHashSet<Integer> objects = new LinkedHashSet<>();
	//The full frequency matrix
	private FreqMatrix freqMatrix = new FreqMatrix();
	//Primary storage of lexical forms (contains frequency in all objects too)
	private GorwableHashMap<String> lexFormFreq = new GorwableHashMap<String>();
	//The relevance of each lexical form, i.e. in how many percent of the objects contain it
	private HashMap<String, Double> lexFormRelevance = new HashMap<>();
	//Stores the object completeness for all objects in order (between 0 and 1, weighted average)
	private LinkedHashMap<Integer, Double> objectCompleteness = new LinkedHashMap<>();
	
	private GorwableHashMap<Integer> objectLexForms = new GorwableHashMap<>();
	//variables for local calculations
	private double sum, div;

	public WordAnalyzer(Word w) {
		this.w = w;
		if (w == null)
			return;
		if (w.formsTable == null)
			return;
		w.formsTable.forEach((k, v) -> {
			objects.addAll(v.keySet());
			v.forEach((kk, vv) -> {
				freqMatrix.add(k, kk, vv);
				lexFormFreq.add(k, vv);
			});
		});
		TreeSet<Integer> set = new TreeSet<>(objects);
//		set.forEach(System.out::println);
		objects.clear();
		objects.addAll(set);
//		System.out.println(" --- ");
//		objects.forEach(System.out::println);
		lexFormFreq.sortByValue();
		
		lexFormFreq.forEach((k,v) -> {
			lexFormRelevance.put(k, (double) v/objects.size());
		});
		
		objects.forEach(obj -> {
			sum = 0;
			div = 0;
			lexFormRelevance.forEach((lexF,rel) -> {
				if (freqMatrix.get(lexF, obj)!=0){
					sum += lexFormRelevance.get(lexF);
					objectLexForms.add(obj, 1);
				}
				div += lexFormRelevance.get(lexF);
			});
			objectCompleteness.put(obj, sum / div);
		});
	}
	

	
	public void printObjectLexFormsAndCompleteness(PrintStream ps) {
		objectLexForms.forEach((k, v) -> ps.printf(Locale.ENGLISH, "%d; %d; %.0f%%\n",k,v,objectCompleteness.get(k)*100));
	}
	
	public void printObjectLexForms(PrintStream ps) {
		objectLexForms.forEach((k, v) -> ps.println(k + " - " + v));
	}

	public void printLexForms(PrintStream ps) {
		lexFormFreq.forEach((k, v) -> ps.println(k + " - " + v));
	}
	
	public void printLexFormRelevance(PrintStream ps) {
		lexFormRelevance.forEach((k, v) -> ps.printf(Locale.ENGLISH, "%s - %d - %.0f%%\n", k ,lexFormFreq.get(k), v*100));
	}
	
	public void printObjectCompleteness(PrintStream ps) {
		objectCompleteness.forEach((k, v) -> ps.printf(Locale.ENGLISH, "%d - %.0f%%\n", k , v*100));
	}
	
	public void printFullTable(PrintStream ps){
		ps.print("Number of objects:; ");
		ps.println(getNumberOfObjects());
		
		ps.print("Number of lexical forms:; ");
		ps.println(getNumberOfLexForms());
		
		
		ps.print("Object num; ");
		ps.print("Lex. forms; ");
		ps.print("Completeness; ");
		lexFormFreq.keySet().forEach(e -> ps.print(e+"; "));
		ps.println();
		ps.print(";;occurence->; ");
		lexFormFreq.forEach((k,v) -> ps.printf("%d ;",v));
		ps.println();
		ps.print(";;relevance->; ");
		lexFormFreq.forEach((k,v) -> ps.printf(Locale.ENGLISH, "%.2f%%;",lexFormRelevance.get(k)*100));
		ps.println();
		
		
		objects.forEach(obj -> {

			ps.printf("%d; %d; %.0f%%; ",obj,objectLexForms.get(obj),objectCompleteness.get(obj)*100);
			lexFormFreq.keySet().forEach(lexF -> {
				ps.print(freqMatrix.get(lexF, obj));
				ps.print("; ");
			});
			ps.println();
		});
		
	}

	public void printFreqMatrix(PrintStream ps) {
		
		ps.print("; ");
		lexFormFreq.keySet().forEach(e -> ps.print(e+"; "));
		ps.println();
		objects.forEach(obj -> {
			ps.print(obj);
			ps.print("; ");
			lexFormFreq.keySet().forEach(lexF -> {
				ps.print(freqMatrix.get(lexF, obj));
				ps.print("; ");
			});
			ps.println();
		});
	}
	
	public int getNumberOfLexForms(){
		return lexFormFreq.size();
	}
	
	public int getNumberOfObjects(){
		return objects.size();
	}

	public Word getW() {
		return w;
	}

	public class GorwableHashMap<K> extends LinkedHashMap<K, Integer> {

		private static final long serialVersionUID = 5230580916517060301L;

		public void add(K k, Integer num) {
			if (containsKey(k))
				put(k, get(k) + num);
			else
				put(k, num);
		}

		public void sortByValue() {
			List<Map.Entry<K, Integer>> l = new ArrayList<>(entrySet());
			l.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
			clear();
			l.forEach(e -> put(e.getKey(), e.getValue()));
		}

	}

	public class FreqMatrix extends HashMap<String, Integer> {

		private static final long serialVersionUID = 8519145549018818944L;

		public void add(String lexForm, Integer obj, Integer freq) {
			put(lexForm + obj, freq);
		}

		public Integer get(String lexForm, Integer obj) {
			return get(lexForm + obj) == null ? 0 : get(lexForm + obj);
		}

	}

}
