package gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import gui.WordCollection.Word;

/**
 * A supplementary class for the {@link WordCollection} class that provides 
 * further functionalities. 
 * @author Berci
 *
 */
public class WCManipulator {
	
	private final WordCollection wc;
	
	public WordCollection getWC(){
		return wc;
	}
	
	
	public WCManipulator(WordCollection wc) {
		this.wc = wc;
	}
	
	public List<String> getBasicInfo(){
		List<String> l = new ArrayList<>();
		l.add("Number of words: "+wc.getSize());
		l.add("Number of words with at least 2 different lexical forms: "+wc.getWordsWithDiffLexForm());
		l.add("Number of words with at least 2 different lexical forms that occur in different objects: "+wc.getWordsWihtAmbigousOccurrence());
		return l;
	}
	
	public List<String> getFullInfo(){
		List<String> l = new ArrayList<>();
		l.add("[lemma] (number of all occurrences) - word type");
		l.add("\t[lexical form]");
		l.add("\t\t[contanied in in object] - [how many times]");
		l.add("");
		
		wc.map.entrySet().stream()
		.sorted(new Comparator<Entry<String, Word>>() {

			@Override
			public int compare(Entry<String, Word> o1, Entry<String, Word> o2) {
				return Integer.compare(o2.getValue().getAllOccurrenceNum(), o1.getValue().getAllOccurrenceNum());
			}
		}).forEach(new Consumer<Entry<String,Word>>() {

			@Override
			public void accept(Entry<String, Word> t) {
				String lemma = t.getKey();
				Word word = wc.map.get(lemma);
				if (word.type.equals("PUNCT"))
					return;
				l.add(lemma + "  (" + t.getValue().getAllOccurrenceNum()+")" + "  "+word.type);
				HashMap<String, HashMap<Integer, Integer>> lexForms = word.formsTable;
				for (String form : lexForms.keySet()){
					l.add(new StringBuilder("\t").append(form).toString());
					for (Integer objNum : lexForms.get(form).keySet()){
						l.add(new StringBuilder("\t\t").append(objNum).append(" - ").append(lexForms.get(form).get(objNum)).toString());
					}
				}
			}
		});
		return l;
	}
	
	public List<String> getFullInfoDisordered(){
		List<String> l = new ArrayList<>();
		l.add("[lemma] (number of all occurrences) - word type");
		l.add("\t[lexical form]");
		l.add("\t\t[contanied in in object] - [how many times]");
		l.add("");
		
		wc.map.entrySet().stream()
		.forEach(new Consumer<Entry<String,Word>>() {

			@Override
			public void accept(Entry<String, Word> t) {
				String lemma = t.getKey();
				Word word = wc.map.get(lemma);
				if (word.type.equals("PUNCT"))
					return;
				l.add(lemma + "  (" + t.getValue().getAllOccurrenceNum()+")" + "  "+word.type);
				HashMap<String, HashMap<Integer, Integer>> lexForms = word.formsTable;
				for (String form : lexForms.keySet()){
					l.add(new StringBuilder("\t").append(form).toString());
					for (Integer objNum : lexForms.get(form).keySet()){
						l.add(new StringBuilder("\t\t").append(objNum).append(" - ").append(lexForms.get(form).get(objNum)).toString());
					}
				}
			}
		});
		return l;
	}
	
	
	
	public void add(WordCollection other){
		other.map.entrySet().stream().forEach(e -> {
			e.getValue().formsTable.entrySet().stream().forEach(ee -> {
				ee.getValue().entrySet().stream().forEach(eee -> {
					wc.addNew(e.getValue().lemma, ee.getKey(), e.getValue().type, eee.getKey());
				});
			});
		});
	}
	

	//////NOT FINISHED!!!!!/////
//	public static void add(HashMap<String, Word> map2){
//		for (String lemma : map2.keySet()){
//			if (map1.containsKey(lemma)){
//				for (String lexForm : map2.get(lemma).formsTable.keySet()){
//					
//				}
//			}else {
//				map1.put(lemma, map2.get(lemma));
//			}
//		}
//	}
}
