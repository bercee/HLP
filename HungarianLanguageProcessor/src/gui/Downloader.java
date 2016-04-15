package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import javax.swing.SwingWorker;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A fancy class to download metadata from Europeana's REST-API. 
 * <p>
 * It is a subclass of {@link SwingWorker} in order to make it invokable from EDT. 
 * <p>
 * By default, it log to the standard error. If you want to change that, change the its PropertyChangeListener.
 * <p>
 * As an input, you have to give an API key and a search query. Optionally, you can also give refinement queries, for example:
 * qf=LANGUAGE:hu. The query is going to be converted into URL encoded string, so you can pass queries like "vörös ökör". However, the 
 * refinement queries will be left as they are passed.
 * <p> 
 * The {@link #get()} method returns with a {@link JSONArray}, that contains each europeana object that corresponds to the query, represented
 * in a {@link JSONObject}. The {@link #process(List)} method receives those {@link JSONObject} references to the europeana objects.
 * <p>
 * If you want to export the metadata, you can give a file and a folder in the constructor, and you also have to specify the output format as a 
 * field of the inner class {@link OutputFormat}.
 * @author Berci
 *
 */
public class Downloader extends SwingWorker<JSONArray, String>{

	//setup...
	private String apikey;
	private String query;
	private String[] refinement = new String[0];
	private String link = "http://www.europeana.eu/api/v2/search.json?wskey=uLzAwOTA&query=hcmNoaXZlTgFF54s&rows=100&profile=rich&cursor=hlbmFfVX";
	private File outfile;
	private OutputFormat format = OutputFormat.None;
	private PrintWriter outputWriter;
	
	//constants for URL manipulation
	private final String API = "uLzAwOTA";
	private final String QUERY = "hcmNoaXZ";
	private final String CURSOR = "hlbmFfVX";
	private final String REFINEMENTS = "lTgFF54s";
		
	//results
	private volatile int totalResults;
	private volatile int currentRes = 0;
	private JSONArray result = new JSONArray();
	private int count;
	private String firstCursor = "*";
	
//	{
//		addPropertyChangeListener(new PropertyChangeListener() {
//			
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				if (evt.getPropertyName().equals("progress")){
//					if (totalResults!=0)
//						System.err.printf(Locale.ENGLISH, "%d of %d (%.2f%%)\n", getCurrentResults(),totalResults,(double) getCurrentResults() * 100 / totalResults);
//					else 
//						System.err.println("Downloading...");
//					if (getProgress() == 100)
//						System.err.println("Ready.");
//				}
//			}
//		});
//	}
	
	public enum OutputFormat{
		
		/**
		 * All europeana objects will be written in a single file in JSON format.
		 */
		JSON_OneFile, 
		/**
		 * The downloaded json texts are going to be directly written to the different file.
		 * One output file is created for each download.
		 */
		JSON_SeparateFiles, 
		/**
		 * Each europeana object will be written in one line, and after each line a comma (,) is put.
		 */
		LineByLine_OneFile, 
		/**
		 * Each europeana object will be written in one line in a separate file.
		 */
		LineByLine_SeparateFiles, 
		None;
		
	}
	
	
	/**
	 * Creates a new Downloader object with the given setup. If format is set to {@link OutputFormat#JSON_OneFile} or {@link OutputFormat#LineByLine_OneFile}, 
	 * than the given outfile parameter will be taken as a file. In case of {@link OutputFormat#JSON_SeparateFiles} or {@link OutputFormat#LineByLine_SeparateFiles}, 
	 * the given parameter will be taken as a folder.
	 * @param apikey the api key
	 * @param query the search query
	 * @param refinement refinement terms to the search query
	 * @param outfile the output file (new file will be created) or folder (new folder will be 
	 * created but possible old content will not be erased.) 
	 * @param format the output format
	 * @throws IOException in case of IO problem (e.g. file or folder cannot be created.)
	 */
	public Downloader(String apikey, String query, String[] refinement, File outfile, OutputFormat format) throws IOException {
		super();
		this.apikey = apikey;
		this.query = query;
		this.refinement = refinement;
		this.outfile = outfile;
		this.format = format;
		switch (format) {
		case JSON_OneFile:
		case LineByLine_OneFile:
			//create new file and output stream;
//			outfile.delete();
//			outfile.createNewFile();
			outputWriter = new PrintWriter(new FileOutputStream(outfile, false));
			break;
		case JSON_SeparateFiles:
		case LineByLine_SeparateFiles:
			//create folder;
			if (!outfile.isDirectory())
				outfile.mkdirs();
			break;
		default:
			break;
		}
		
	}
	

	
	/**
	 * Creates a new Downloader object with the given setup. If format is set to {@link OutputFormat#JSON_OneFile} or {@link OutputFormat#LineByLine_OneFile}, 
	 * than the given outfile parameter will be taken as a file. In case of {@link OutputFormat#JSON_SeparateFiles} or {@link OutputFormat#LineByLine_SeparateFiles}, 
	 * the given parameter will be taken as a folder.
	 * @param apikey the apikey
	 * @param query the search query
	 * @param outfile the output file (new file will be created) or folder (new folder will be 
	 * created but possible old content will not be erased.) 
	 * @param format the output format
	 * @throws IOException in case of IO error (e.g. file or folder cannot be created.)
	 */
	public Downloader(String apikey, String query, File outfile, OutputFormat format) throws IOException {
		this(apikey, query, new String[0], outfile, format);
	}
	
	/**
	 * Creates a new dowloader with the given setup. The results of the search will not be written to output file or folder.
	 * @param apikey the api key
	 * @param query the research query
	 */
	public Downloader(String apikey, String query) {
		this(apikey, query, new String[0]);
	}
	
	/**
	 * Creates a new dowloader with the given setup. The results of the search will not be written to output file or folder.
	 * @param apikey the api key
	 * @param query the search query
	 * @param refinement refinement terms to the search query
	 */
	public Downloader(String apikey, String query, String[] refinement){
		this.apikey = apikey;
		this.query = query;
		this.refinement = refinement;
	}
	
	public Downloader(String apikey, String query, String[] refinement, File outfile, OutputFormat format, String nextCursor, int start) throws IOException {
		this(apikey, query, refinement, outfile, format);
		currentRes = start;
		firstCursor = nextCursor;
		
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	protected JSONArray doInBackground() throws Exception {
		firePropertyChange("progress", 1, 0);
		
		String urlString = link.replaceAll(API, apikey);
//		urlString = urlString.replaceAll(QUERY, URLEncoder.encode(query, "UTF-8"));
		urlString = urlString.replaceAll(QUERY,query);
		
		StringBuilder sb = new StringBuilder();
		for (String s : refinement){
			sb.append("&").append(s);
		}
		urlString = urlString.replaceAll(REFINEMENTS, sb.toString());
		urlString = urlString.replaceAll(CURSOR, firstCursor);
		String nextCursor = "";
		
		publish("Start download...");
		publish(urlString);
		
		switch (format) {
		case JSON_OneFile:
			outputWriter.print("{");
			break;
		case JSON_SeparateFiles:

			break;
		case LineByLine_OneFile:

			break;
		case LineByLine_SeparateFiles:

			break;

		default:
			break;
		}

		int count = currentRes+1;
		do{
			URL url = new URL(urlString);
			URLConnection c = url.openConnection();
			int i = 1;
			BufferedReader br = null;
			while (i <= 3){
				try{
					InputStream is = null;
					is = c.getInputStream();
					br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					 i = 4;
//				}catch(IOException e){
//					if (e.getMessage().contains("HTTP response code: 403")){
//						publish(e.getMessage());
//						throw new Exception("Check API key", e);
//					}
				}
				catch (Exception e){
					if (i == 3){
						publish(urlString);
						publish(url.toString());
						throw e;
					}
					Thread.sleep(5000);
					i++;
					e.printStackTrace();
					publish(e.getMessage());
					publish("Trying again...");
				}
			}
			// String line = br.readLine();
			// if (line == null)
			// break;
			sb = new StringBuilder();
			char[] buffer = new char[1024];
			int charsRead;
			while ((charsRead = br.read(buffer, 0, 1024)) != -1) {
				sb.append(buffer, 0, charsRead);
			}
			String line = sb.toString();
			JSONObjectReader r = new JSONObjectReader(line);
			totalResults = r.getTotalResults();
			if (isCancelled())
				break;
			synchronized (result) {
				result.addAll(r.getItems());
			}
			currentRes+=r.getItems().size();
//			System.out.println(currentRes);
			if (format != OutputFormat.None){
				printResults(r);
				result.clear();
			}
			for (Object o : r.getItems()) {
				if (o instanceof JSONObject) {
					JSONObject e = (JSONObject) o;
					publish((count++) + " of " + totalResults + ": " + e.get(JSONObjectReader.ID));
				}
			}
			if (totalResults != 0) {
				// if (result.size()*100 / totalResults == getProgress() &&
				// getProgress() != 100)
				// firePropertyChange("progress", getProgress()+1,
				// result.size()*100 / totalResults);
				setProgress(currentRes * 100 / totalResults); // set the new
																	// progress
			}else {
				setProgress(100);
				publish("No items found.");
				
			}
			

			nextCursor = (String) r.get(JSONObjectReader.NEXT_CURSOR);
			if (nextCursor == null)
				nextCursor = "";
			urlString = urlString.replaceAll("cursor=.*", "cursor=" + URLEncoder.encode(nextCursor, "UTF-8"));
		}while (!nextCursor.equals("")&&!isCancelled());
		
		switch (format) {
		case None:
			break;
		case JSON_OneFile:
			outputWriter.print("}");
		default:
			if (outputWriter != null)
				outputWriter.close();
			publish("Downloaded metadata is written to the output.");
			break;
		}
		
		return result;
	}
	
	
	private void printResults(JSONObjectReader r) {
		switch (format) {
		case JSON_OneFile:
			for (Object o : r.getItems()){
				if (o instanceof JSONObject) {
					JSONObject e = (JSONObject) o;
					outputWriter.print(e);
					outputWriter.print(",");
				}
			}
			break;
		case JSON_SeparateFiles:
			PrintWriter pw = null;
			try {
				File f = new File(outfile, "results_"+getCurrentResults()+".json");
				pw = new PrintWriter(f);
				pw.println(r.getJSONObj());
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}finally {
				if (pw != null)
					pw.close();
			}
			break;
		case LineByLine_OneFile:
			for (Object o : r.getItems()){
				if (o instanceof JSONObject) {
					JSONObject e = (JSONObject) o;
					outputWriter.println(e);
				}
			}
			break;
		case LineByLine_SeparateFiles:
			for (Object o : r.getItems()){
				if (o instanceof JSONObject) {
					JSONObject e = (JSONObject) o;
					PrintWriter pw2  = null;
					try {
						File f = new File(outfile,  "Result_"+(count++)+".json");
						pw2 = new PrintWriter(f);
						pw2.println(e);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}finally {
						if (pw2 != null){
							pw2.close();
						}
					}
				}
			}
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * Return the number of the expected total results. 
	 * @return total results
	 */
	public int getTotalRes(){
		return totalResults;
	}
	
	/**
	 * Returns the number of downloaded objects by the time 
	 * the method is invoked.
	 * @return current number of results
	 */
	public int getCurrentResults(){
		return currentRes;
	}

	@Override
	protected void process(List<String> chunks) {
		for (String s : chunks)
			System.err.println(s);
	}


	
	

}
