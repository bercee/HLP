package gui;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * A convenience class for reading and parsing a file that contains JSON data. 
 * This class encapsulates functionalities of the simple json parser package. 
 * @author Berci
 *
 */
public class JSONObjectReader{
	

	public final static String APIKEY = "apikey";
	public final static String ITEMS_COUNT = "itemsCount";
	public final static String TOTAL_RESULTS = "totalResults";
	public final static String NEXT_CURSOR = "nextCursor";
	public final static String ITEMS = "items";
	public final static String ID = "id";
	public final static String SUCCESS = "success";
	public final static String DESCRIPTION_LANG_AWARE = "dcDescriptionLangAware";
	public final static String DESCRIPTION = "dcDescription";

	@SuppressWarnings("unused")
	private String line;
	JSONObject jsonObj;

	public JSONObjectReader(String line) throws Exception {
		super();
		this.line = line;
		JSONParser parser = new JSONParser();
		Object o = parser.parse(line);
		if (o instanceof JSONObject) {
			jsonObj = (JSONObject) o;
		} else {
			throw new Exception("The given string is wrong.");
		}
	}

	public JSONArray getItems() {
		for (Object o : jsonObj.keySet()) {
			if (o.equals(ITEMS))
				if (jsonObj.get(o) instanceof JSONArray)
					return (JSONArray) jsonObj.get(o);
		}
		return new JSONArray();
	}
	
	public Object get(Object key){
		return jsonObj.get(key);
	}

	public int getTotalResults() {
		if (!jsonObj.containsKey(TOTAL_RESULTS))
			return 0;
		if (jsonObj.get(TOTAL_RESULTS) instanceof Long)
			return ((Long) jsonObj.get(TOTAL_RESULTS)).intValue();
		else 
			return 0;
	}
	
	public JSONObject getJSONObj(){
		return jsonObj;
	}
	


}
