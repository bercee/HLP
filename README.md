# HLP - Hungarian Language Processor for Europeana
## Nataural Language Processing of Europeana's metadata in Hungarian

### Running program 
Execute runnable HungarianLanguageProcessor_GUI.jar desktop application. 
Attached source code can be directly imported into eclipse. To initialize GUI, executed Main.main().
Depending on the quantity of downloaded metadata, it can be safer to execute HLP with a bigger heap memory. E.g.: java -Xmx1G -jar HungarianLanguageProcessor_GUI.jar

### Software requirements: 
- Java 8 
- magyarlánc 3.0 (download from http://www.inf.u-szeged.hu/rgai/magyarlanc)
- API key for Europeana's REST API. (Get an API key here: http://labs.europeana.eu/api/registration)
- For source code only: json-simple (https://github.com/fangyidong/json-simple)


### Program Usage
#### Download metadata
- API key: use your valid API key
- Search query: do not use special characters (e.g. kosztolanyi)
- Query refinements: e.g. LANGUAGE:hu (if you only want to download Hungarian metadata)
- First cursor: in case you want to continue a previously started download, paste here the cursor field of the last valid JSON response. You also have to specify the index of the last downloaded object. By default, these fields are set to * and 0, which mean that the download will start from the beginning. 
- Output: 
 * None: metadata is not saved, but directly submitted to parsing instead.
 * JSON_OneFile: all downloaded metadata is saved in a single file in JSON format.
 * JSON_SeparateFiles: each JSON response page is saved in a separate file in the specified directory.
 * LineByLine_OneFile: all downloaded objects are saved in a single file, one object in one line.
 * LineByLine_SeparateFile: each downloaded object is saved in a separate file in the specified directory.

 
#### Get Description
- Metadata input:
 * FromDownload: previously downloaded metadata is taken as input
 * FromFileOrFolder: metadata is taken from a single file or from a directory by reading all files in it. File format can be JSON or LineByLine.
- Description output:
 * None: descriptions are directly submitted to natural language processing.
 * OneFile: all descriptions are saved in a single file, one object in one line. 
 * SeparateFiles: each desciption is saved in a separate file in the specified format. 
 
 

#### NLP (Natural Langauge Processing)
- Magyarlánc path: the full path to magyarlanc-3.0.jar file
- Descriptions input: 
 * FromMetadata: previuosly downloaded and parsed metadata is taken as input
 * FromFileOrFolder: descriptions are taken from a single file or from a directory by reading all files in it.
- Output
 * None: output is only shown in the GUI window
 * Full: all the output is printed to the given file

