package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.SwingWorker;

/**
 * A class for running magyarlánc and processing its output. It loads magyarlanc.jar 
 * dynamically from a specified path. Input and output of the process can be specified 
 * in the different constructors of this class. 
 * <p>
 * This class is also a subclass of {@link SwingWorker}, which means that the time consuming 
 * task of running magyarlánc is carried out on a separate thread. To receive information 
 * about the progress of the process, override the {@link #process(List)} method. 
 * @author Berci
 *
 */
public class MagyarlancRunner extends SwingWorker<WordCollection, String>{
	
	public enum Input{
		FromFileOrFolder, 
		FromMetadata;
	}
	
	public enum Output{
		Full,
	//	Basic, 
		None;
	}

	private List<String> inputList = new ArrayList<>();
	private File f;
	private List<List<List<String>>> parsedList = new ArrayList<List<List<String>>>();
	private volatile int status = 0;
//	private HashSet<Word> words = new HashSet<Word>();
//	private HashMap<Word, Word> words = new HashMap<Word, Word>();
	private WordCollection words = new WordCollection();
	private File wordCollection;
	private static boolean isInitialized = false;
	
	private String magyarlancURL;
	private URLClassLoader loader;
	@SuppressWarnings("rawtypes")
	private static Class magyarlanc;
	@SuppressWarnings("rawtypes")
	private static Class stringCleaner;
	@SuppressWarnings("rawtypes")
	private static Class splitter;
	private static Method morphParseSentence;
	private static Method splitToArray;
	private static Method cleanString;
	@SuppressWarnings("unused")
	private static Method init;
	private static Object mySplitterInst;
	private static Object stringCleanerInst;
	@SuppressWarnings("rawtypes")
	private static Class myPurePos;
	private static Method getInstance;
	@SuppressWarnings("rawtypes")
	private static Class resourceHolder;
	private static List<Method> initMethods = new ArrayList<>();
	
	
//	public static void main(String[] args) {
//		
//		try {
//			Descriptions d = new Descriptions(new File("voros_okor"), null, OutputFormat.None);
//			
//			MagyarlancRunner mr = new MagyarlancRunner("E:\\Dokumentumok\\IRUN\\magyarlánc\\magyarlanc-3.0.jar", new File("kosztolanyi_desc.txt"));
//			mr.execute();
//			WordCollection wc = mr.get();
//		}catch (Exception e){
//			
//		}
//		
//	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadMagyarlanc() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException{
		URL url = new File(magyarlancURL).toURI().toURL();
		loader = new URLClassLoader(new URL[]{url});
		magyarlanc = loader.loadClass("hu.u_szeged.magyarlanc.Magyarlanc");
		stringCleaner = loader.loadClass("splitter.archive.StringCleaner");
		splitter = loader.loadClass("splitter.MySplitter");
		myPurePos = loader.loadClass("hu.u_szeged.pos.purepos.MyPurePos");
		resourceHolder = loader.loadClass("hu.u_szeged.magyarlanc.resource.ResourceHolder");
		
		
		//init = magyarlanc.getMethod("init");
		
		Method getInst = splitter.getMethod("getInstance");
		mySplitterInst = getInst.invoke(null);
		splitToArray = splitter.getMethod("splitToArray", String.class);
		
		morphParseSentence = magyarlanc.getMethod("morphParseSentence", String[].class);
		
		Constructor c = stringCleaner.getConstructor();
		stringCleanerInst = c.newInstance();
		cleanString = stringCleaner.getMethod("cleanString", String.class);
		
		getInstance = myPurePos.getMethod("getInstance");
		
		initMethods.add(resourceHolder.getMethod("initTokenizer"));
		initMethods.add(resourceHolder.getMethod("initCorpus"));
		initMethods.add(resourceHolder.getMethod("initMSDReducer"));
		initMethods.add(resourceHolder.getMethod("initPunctations"));
		initMethods.add(resourceHolder.getMethod("initRFSA"));
		initMethods.add(resourceHolder.getMethod("initKRToMSD"));
		initMethods.add(resourceHolder.getMethod("initMSDToCoNLLFeatures"));
		initMethods.add(resourceHolder.getMethod("initCorrDic"));
		initMethods.add(resourceHolder.getMethod("initMorPhonDir"));
		
	}
	
	
//	{
//		addPropertyChangeListener(new PropertyChangeListener() {
//			
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				if (evt.getPropertyName().equals("progress")){
//					if (((Integer) evt.getNewValue()) == 0){
//						System.err.println("Initializing magyarlánc...");
//					}else {
//						System.err.println(getProgress()+"% ("+status+" of "+parsedList.size()+")");
//					}
//				}
//				
//			}
//		});
//	}

	public MagyarlancRunner(String magyarlancURL, List<String> inputList) throws Exception{
		this.inputList = inputList;
		this.magyarlancURL = magyarlancURL;
		loadMagyarlanc();
	}

	public MagyarlancRunner(String magyarlancURL, File f) throws FileNotFoundException, IOException, Exception {
		this.magyarlancURL = magyarlancURL;
		this.f = f;
		inputList = new ArrayList<String>();
		if (f.isDirectory()) {
			File[] fs = f.listFiles();
			Arrays.sort(fs,new AlfanumComparator());
			try {
				for (File file : fs) {
					publish(file.toString());
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
					while (br.ready())
						inputList.add(br.readLine());
					br.close();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else if (f.isFile()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			while (br.ready())
				inputList.add(br.readLine());
			br.close();
		} else
			throw new FileNotFoundException("Input file or folder does not exist.");
		loadMagyarlanc();

	}
	
	public  MagyarlancRunner(String magyarlancURL, File dir, File wordCollection) throws Exception {
		this.magyarlancURL = magyarlancURL;
		this.f = dir;
		this.wordCollection = wordCollection;
		loadMagyarlanc();
		if (wordCollection.isFile()){
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(wordCollection));
			Object o = ois.readObject();
			ois.close();
			if (o instanceof WordCollection) {
				WordCollection wc = (WordCollection) o;
				words = wc;
			}else {
				throw new Exception("Word collection file is wrong.");
			}
		}
		
	}
	
	public void processFiles(int start, int end) throws Exception{
		init();
		if (!f.isDirectory())
			throw new Exception("Given directory does not exist,");
		for (int n = start; n <= end; n++){
			File file = new File(f.getAbsolutePath(), "Description_"+n+".txt");
			publish(file.getName());
			if (file.isFile()){
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				String line = ""; 
				while (br.ready())
					line += br.readLine() + " ";
				br.close();
				List<List<String>> list = getParsedSentence(line);
				for (List<String> l : list) {
					words.addNew(l.get(1), l.get(0), l.get(2), n);
				}

			}else {
				publish("File does not exist: "+file.getName());
			}
		}
	}
	
	public void printWordCollection() throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(wordCollection));
		oos.writeObject(words);
		oos.flush();
		oos.close();
		publish("Word collection is written to file.");
	}

	/**
	 * This method parses a line and returns a two-dimensional array with the
	 * parsed, tokenized, lemmatized words.
	 * <p>
	 * This is the core method. All other methods have to invoke this one.
	 * <p>
	 * It uses the {@link Magyarlanc#morphParse(String[][])} method.
	 * 
	 * @param line
	 *            the input line
	 * @return a two dimensional array with the parsed words.
	 */
	private List<List<String>> getParsedSentence(String line) {
		List<List<String>> list = new ArrayList<List<String>>();
		try{
//		line = new StringCleaner().cleanString(line.trim());
//		line = cleanString(line.trim());
		line = (String) cleanString.invoke(stringCleanerInst, line);
//		String[][] sentences = MySplitter.getInstance().splitToArray(line);
//		String[][] sentences = splitToArray(line);
		String[][] sentences = (String[][]) splitToArray.invoke(mySplitterInst, line);
		for (String[] sentence : sentences) {
//			String[][] morphed = Magyarlanc.morphParseSentence(sentence);
//			String[][] morphed = morphParseSentence(sentence);
			String[][] morphed = (String[][]) morphParseSentence.invoke(null, (Object) sentence);
			for (int j = 0; j < morphed.length; j++) {
				list.add(Arrays.asList(morphed[j]));
			}
		}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public LinkedHashMap<String, Integer> getWordMap(){
		LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
		
		
		for (String lemma : words.map.keySet()){
			
			for (String lexForm : words.map.get(lemma).formsTable.keySet()){
				HashMap<Integer, Integer> freqMap = words.map.get(lemma).formsTable.get(lexForm);
				int count = 0;
				for (Integer i : freqMap.keySet())
					count+=freqMap.get(i);
				map.put(lexForm, count);
			}
		}
		
		return map;
	}

	@Override
	protected WordCollection doInBackground() throws Exception {
		if (isCancelled())
			return words;
		firePropertyChange("progress", 1, 0);
//		publish("Initializing magyarlánc...");	
//		if (!isInitialized){
//			isInitialized = true;
//			for (Method m : initMethods){
//				if (isCancelled())
//					return words;
//				m.invoke(null);
////				publish("...");
//				
//			}
//		}
//		getInstance.invoke(null);
//		publish("Initialization done.");
		init();
		int c = 1;
		for (String s : inputList){
			if (isCancelled())
				return words;
			status = c++;
			publish("Parsing "+status + " of " + inputList.size());
			parsedList.add(getParsedSentence(s));
			setProgress((status*100 ) / inputList.size());
		}
		publish("Doing word frequency analysis...");
		for (int objNum = 0; objNum < parsedList.size();objNum++){
			for (List<String> l : parsedList.get(objNum)){
				words.addNew(l.get(1), l.get(0), l.get(2), objNum);
			}
		}
		publish("NLP done.");
		
		return words;
	}
	

	
	@Override
	protected void process(List<String> chunks) {
		for (String s : chunks)
			System.err.println(s);
	}
	
	private void init() throws Exception{
		if (!isInitialized){
			publish("Initializing magyarlánc...");
			isInitialized = true;
			for (Method m : initMethods){
				m.invoke(null);
				
			}
			getInstance.invoke(null);
			publish("Initialization done.");
		}
	}


	public List<String> getBasicInfo() {
		WCManipulator wcm = new WCManipulator(words);
		return wcm.getBasicInfo();
	}


	public List<String> getFullInfo() {
		WCManipulator wcm = new WCManipulator(words);
		return wcm.getFullInfo();
	}
	
//	private static void initialize(){
//		if (!isInitialized){
//	//		Magyarlanc.init();
//			try {
//				Method m = magyarlanc.getMethod("init");
//				m.setAccessible(true);
//				m.invoke(null);
//			} catch (NoSuchMethodException e) {
//				e.printStackTrace();
//			} catch (SecurityException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			}
//			
//			isInitialized = true;
//		}
//			
//	}
	
	
//	private String[][] splitToArray(String line){
//		try {
//		//	Constructor c = splitter.getConstructor();
//			Method getInst = splitter.getMethod("getInstance");
//			Object mySplitterInst = getInst.invoke(null);
//			Method m = splitter.getMethod("splitToArray", String.class);
//			m.setAccessible(true);
//			return (String[][]) m.invoke(mySplitterInst, line);
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		}catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.getTargetException().printStackTrace();
//			e.printStackTrace();
//		}finally {
//		}
//		return null;
//	}
//
//	
//	private String[][] morphParseSentence(String[] sentence){
//		try {
//			Method morphParseSentence = magyarlanc.getMethod("morphParseSentence", String[].class);
//			return (String[][]) morphParseSentence.invoke(null, (Object) sentence);
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}finally{
//		}
//		return null;
//	}
//	
//	private String cleanString(String line){
//		try {
//			Constructor c = stringCleaner.getConstructor();
//			Object stringCleanerInst = c.newInstance();
//			Method cleanString = stringCleaner.getMethod("cleanString", String.class);
//			cleanString.setAccessible(true);
//			return (String) cleanString.invoke(stringCleanerInst, line);
//			
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		return null;
//		
//	}
//	

}
