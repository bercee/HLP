package gui;

import java.util.List;

import org.json.simple.JSONArray;

/**
 * A convenience class that completes the {@link GUI} class. Each GUI instance
 * contain one {@link GUIEngine} as a field. 
 * Furthermore, to each {@link GUIEngine} instance belong one {@link WorkerThread} instance
 * that contains the functionalities of the GUI and carries out the time consuming tasks. 
 * @author Berci
 *
 */
public class GUIEngine {
	final GUI gui;
	Downloader downloader;
	Descriptions descriptions;
	MagyarlancRunner magyarlanc;
	List<String> descriptionResult;
	JSONArray downloadResult;
	WordCollection wordCollection;

	WorkerThread worker;
	public GUIEngine(GUI gui) {
		this.gui = gui;
	}

	void start() {
		// gui.start.setText("STOP");
		gui.start.setEnabled(false);
		gui.stop.setEnabled(true);
		gui.progressArea.setText("");
		worker = new WorkerThread(this);
		worker.start();

	}

	void stop() {
//		System.out.println("stop clicked");
//		System.out.println(worker == null);
		if (worker != null){
//			System.out.println("stopping worker...");
			worker.cancel();
			worker = null;
		}

	}

	public void append(String line) {
		gui.progressArea.append(line);
		gui.progressArea.append("\n");
	}

	public void appendResult(String line) {
		gui.resultsArea.append(line);
		gui.resultsArea.append("\n");
	}

}
