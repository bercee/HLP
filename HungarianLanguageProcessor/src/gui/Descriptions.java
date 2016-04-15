package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingWorker;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is made to extract descriptions from Europeana's metadata. 
 * First, you have to instantiate the class and specify the output location and format. Optionally, 
 * you can given an input source as a file or folder, or you can just 
 * add a list a of {@link JSONObject}s. 
 * With the {@link #writeInfo()} method the descriptions will be written to a file or folder.
 * 
 * @author Berci
 *
 */
public class Descriptions extends SwingWorker<List<String>, String>{
	
	public enum InputSource{
		FromDownload,
		FromFileOrFolder;
	}

	public enum OutputFormat {
		/**
		 * The descriptions will be written into a single file, line-by-line.
		 */
		OneFile, 
		/**
		 * To each object a new file will be made in the given folder.
		 */
		SeparateFiles, 
		/**
		 * No output.
		 */
		None;
		
	}

	@SuppressWarnings("unused")
	private File in;
	private File[] infiles;
	private File out;
	private JSONArray objects = new JSONArray();
	private List<String> desciptions = new ArrayList<String>();
	private int outCount = 0;


	private OutputFormat format = OutputFormat.OneFile;

//	/**
//	 * A constructor to create a new, empty {@link Descriptions} object.
//	 * <p>
//	 * If the output format is set to <code>OneFile</code>, than the given <code>File</code> object will be
//	 * taken as a file. In case of <code>SeparateFiles</code>, the output is taken as a directory.
//	 * @param out The ouptut file or folder.
//	 * @param format The output format.
//	 * @throws IOException in case of IO error
//	 * @throws ParseException if the metadata input is wrong
//	 */
//	public Descriptions(File out, OutputFormat format) throws IOException, ParseException {
//		this(null, out, format);
//	}

	/**
	 * A constructor the create a new {@link Descriptions} object with a specified input.
	 * <p>
	 * The input can be a folder or a file. In both cases, the file(s) can be the result of the 
	 * direct download (the original JSON response), or just one object. It will be recognized and 
	 * information will be extracted from it as a list of objects.
	 * <p>
	 * If the output format is set to <code>OneFile</code>, than the given <code>File</code> object will be
	 * taken as a file. In case of <code>SeparateFiles</code>, the output is taken as a directory.
	 * @param in the input source
	 * @param out the output file or folder
	 * @param format the output format
	 * @throws IOException in case of IO error
	 * @throws ParseException if the input metadata is wrong
	 */
	public Descriptions(File in, File out, OutputFormat format) throws IOException, ParseException {
		this.in = in;
		this.out = out;
		this.format = format;
		if (format == OutputFormat.SeparateFiles) {
			if (out == null)
				throw new FileNotFoundException("Output folder does not exist!");
			if (!out.exists())
				out.mkdirs();
		}

		if (in != null) {
			if (!in.exists()){
				throw new FileNotFoundException("Given input does not exist.");
			}
			if (in.isDirectory()) {
				infiles = in.listFiles();
				Arrays.sort(infiles, new AlfanumComparator());
//				List<File> l = Arrays.asList(infiles);
//				l.sort(new AlfanumComparator());
//				l.toArray(infiles);
				
			
				
				if (infiles.length == 0)
					throw new FileNotFoundException("The given folder is empty.");
				fillObjects(infiles);
			}else {
				File[] ff = new File[1];
				ff[0] = in;
				fillObjects(ff);
				
			}
		}
		
	}
	
	public Descriptions(JSONArray objects, File out, OutputFormat format) throws FileNotFoundException {
		this.objects = objects;
		this.out = out;
		this.format = format;
		if (format == OutputFormat.SeparateFiles) {
			if (out == null)
				throw new FileNotFoundException("Output folder does not exist!");
			if (!out.exists())
				out.mkdirs();
		}
		
	}

	@SuppressWarnings("unchecked")
	private int fillObjects(File[] files) throws FileNotFoundException, IOException, ParseException {
		for (File f : files) {
			JSONParser p = new JSONParser();
			Object o = p.parse(new FileReader(f));
			if (o instanceof JSONObject) {
				JSONObject obj = (JSONObject) o;
				if (obj.containsKey(JSONObjectReader.ITEMS)) {
					objects.addAll((ArrayList<Object>) obj.get(JSONObjectReader.ITEMS));
				}
				if (obj.containsKey(JSONObjectReader.ID)) {
					objects.add(obj);
				}
			}
		}
		return objects.size();
	}

	
//	/**
//	 * Adds a list of Europeana objects to this {@link Descriptions} object. It is important
//	 * that the list contains valid Europeana objects. Returns the number of the objects as a result. 
//	 * @param list the list of objects to add
//	 * @return the number objects after adding this list
//	 */
//	@SuppressWarnings("unchecked")
//	public int addObjects(ArrayList<Object> list) {
//		objects.addAll(list);
//		return objects.size();
//	}
	
	private void extractDescriptions() throws IOException   {
		for (Object o : objects) {
			StringBuilder sb = new StringBuilder();
			if (o instanceof JSONObject) {
				Object oo = getInfo((JSONObject) o);
				if (oo instanceof JSONArray) {
					for (Object ooo : (JSONArray) oo) {
						sb.append(ooo);
						sb.append(" ");
					}
				} else {
					if (oo != null) {
						sb.append(oo);
					} else {
					}
				}
			}
			desciptions.add(sb.toString());
			if (!format.equals(OutputFormat.None)){
				writeInfo();
				desciptions.clear();
			}
		}
	}
	@Override
	protected List<String> doInBackground() throws Exception {
		publish("I am working.");
		if (isCancelled())
			return desciptions;
		publish("Parsing metadata...");
		extractDescriptions();
		publish("Parsing done.");
		
		
		return desciptions;
	}
	
	@Override
	protected void process(List<String> chunks) {
		for (String s :chunks)
			System.err.println(s);
	}

	
	private Object getInfo(JSONObject o) {
		
		Object oo = o.get(JSONObjectReader.DESCRIPTION);
		if (oo == null) {
			Object o2 = o.get(JSONObjectReader.DESCRIPTION_LANG_AWARE);
			if (o2 instanceof JSONObject) {
				oo = ((JSONObject) o2).get("def");
			}
		}
		
		return oo;
	}

	/**
	 * Writes the description of each object to the specified output.
	 * <p>
	 * To each object, first its dcDescription will be looked for. If not found, than its
	 * dcDescriptionLangAware. If its not found either, than an empty string will be written.
	 * @throws FileNotFoundException if it is not possible to write to the output
	 */
	public void writeInfo() throws IOException {
		if (format == OutputFormat.OneFile) {
			
			PrintWriter pw = new PrintWriter(new FileWriter(out, true));
			
			for (String s : desciptions){
				pw.println(s);
			}
			
//			for (Object o : objects) {
//				StringBuilder sb = new StringBuilder();
//				if (o instanceof JSONObject) {
//					Object oo = getInfo((JSONObject) o);
//					if (oo instanceof JSONArray) {
//						for (Object ooo : (JSONArray) oo) {
//							pw.print(ooo);
//							pw.print(" ");
//						}
//						pw.println();
//					} else {
//						if (oo != null){
//							pw.println(oo);
//						}
//						else
//							pw.println();
//					}
//				}
//				desciptions.add(sb.toString());
//			}
			pw.close();
		} else if (format == OutputFormat.SeparateFiles) {
			
			for (String s : desciptions){
				PrintWriter pw = new PrintWriter(new File(out, "Desciption_" + (outCount++) + ".txt"));
				pw.println(s);
				pw.close();
			}
//			for (Object o : objects) {
//				PrintWriter pw = new PrintWriter(new File(out, "Desciption_" + (count++) + ".txt"));
//				if (o instanceof JSONObject) {
//					Object oo = getInfo((JSONObject) o);
//					if (oo instanceof JSONArray) {
//						// pw.print(oo.getClass());
//						pw.print(" - ");
//						for (Object ooo : (JSONArray) oo) {
//							pw.print(ooo);
//							pw.print(" ");
//						}
//						pw.println();
//					} else {
//						if (oo != null)
//							pw.println(oo);
//						else
//							pw.println();
//					}
//				}
//				pw.close();
//			}
		}
		
		if (!format.equals(OutputFormat.None))
			publish("Description is written to the output.");

	}



}
