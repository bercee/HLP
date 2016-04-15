package gui;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Once having obtained a serialized version of a {@link WordCollection} object, 
 * this class is capable of doing statistical analysis. 
 * <p>
 * The following parameters can be set below: input file (a serialized {@link WordCollection} 
 * object file), output folder, and a list of words (necessarily lemmas) for which statistics 
 * can be done.
 * @author Berci
 *
 */
public class WordStatistics {
	
	static File f = new File("C:\\IRUN\\words_all.ws");
	static File outputFolder = new File("C:\\IRUN\\word_anal");
	static ArrayList<String> lemmas = new ArrayList<>();
	static {
		lemmas.add("magyar");
		lemmas.add("cím");
		lemmas.add("dal");
		lemmas.add("élet");
		lemmas.add("ember");
		lemmas.add("emlék");
		lemmas.add("első");
		lemmas.add("falu");
		lemmas.add("irodalom");
		lemmas.add("kávé");
		lemmas.add("király");
		lemmas.add("költő");
		lemmas.add("lány");
		lemmas.add("mező");
		lemmas.add("szív");
		lemmas.add("van");
		lemmas.add("vers");
		lemmas.add("virág");
	}
	
	public static void main(String[] args) {
		try {
			System.err.println("Start reading...");
			long l = System.currentTimeMillis();
			
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			WordCollection wc = (WordCollection) ois.readObject();
			ois.close();
			System.err.println("Done. " + (System.currentTimeMillis()-l));
//			for (String lemma : wc.map.keySet()){
//			}
//			wc.map.get("magyar").formsTable.forEach((k,v)->System.out.println(k));
		

			lemmas.forEach(lemma -> {
				try {
					System.err.println("Start analyzing "+lemma);
					long ll = System.currentTimeMillis();
					WordAnalyzer wa = new WordAnalyzer(wc.map.get(lemma));
					System.err.println("Done. " + (System.currentTimeMillis()-ll));
					
//				wa.printLexForms(System.out);
//				wa.printFreqMatrix(System.out);
//				wa.printLexFormRelevance(System.out);
//				wa.printObjectCompleteness(System.out);
//				wa.printObjectLexForms(System.out);
//				wa.printObjectLexFormsAndCompleteness(System.out);
//					wa.printFullTable(System.out);
//				System.out.println(wa.getNumberOfObjects());
					System.err.println("Start printing...");
					ll = System.currentTimeMillis();
					File objCompl = new File(outputFolder,lemma+".csv");
					
					PrintStream ps = new PrintStream(objCompl, "ISO-8859-1");
					wa.printFullTable(ps);
					ps.flush();
					ps.close();
					System.err.println("Done. " + (System.currentTimeMillis()-ll));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
			
//			System.err.println("Start analyzing...");
//			l = System.currentTimeMillis();
//			WordAnalyzer wa = new WordAnalyzer(wc.map.get("magyar"));
//			System.err.println("Done. " + (System.currentTimeMillis()-l));
//			wa.printLexForms(System.out);
//			wa.printFreqMatrix(System.out);
//			wa.printLexFormRelevance(System.out);
//			wa.printObjectCompleteness(System.out);
//			wa.printObjectLexForms(System.out);
//			wa.printObjectLexFormsAndCompleteness(System.out);
//			wa.printFullTable(System.out);
//			System.err.println("Start printing...");
//			l = System.currentTimeMillis();
//			File objCompl = new File("C:\\IRUN\\object_compl.txt");
//			PrintStream ps = new PrintStream(objCompl);
//			wa.printObjectCompleteness(ps);
//			ps.flush();
//			ps.close();
//			System.err.println("Done. " + (System.currentTimeMillis()-l));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
}
