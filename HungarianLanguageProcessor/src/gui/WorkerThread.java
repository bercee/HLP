package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import org.json.simple.parser.ParseException;

/**
 * An additional class for a {@link GUI} and {@link GUIEngine} class. 
 * This class is responsible for organizing the different time consuming tasks 
 * (e.g. downloading metadata, executing magyarlánc) and reporting the progress 
 * to the GUI. 
 * @author Berci
 *
 */
public class WorkerThread extends Thread {
	
	final GUIEngine engine;
	final GUI gui;


	public WorkerThread(GUIEngine engine) {
		this.engine = engine;
		this.gui = engine.gui;
		
		
	}
	
	@Override
	public void run() {

		try {
			if (gui.downloadBox.isSelected()) {
				if (gui.queryF.getText().equals(""))
					gui.queryF.setText("*");
				String query = gui.queryF.getText();
				try {
					query = URLEncoder.encode(query, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (gui.apiF.getText().equals("")) {
					JOptionPane.showMessageDialog(gui.frame, "API key is missing.", "Download",
							JOptionPane.ERROR_MESSAGE);
					throw new Exception("API key is missing.");
				}
				String[] qfs = new String[0];
				if (!gui.qfF1.getText().equals("") && !gui.qfF2.getText().equals("")) {
					qfs = new String[1];
					qfs[0] = "qf=" + gui.qfF1.getText() + ":" + gui.qfF2.getText();
				}
				File downOut = null;
				if (!gui.downloadSelect.combo.getSelectedItem().equals(Downloader.OutputFormat.None)) {
					downOut = new File(gui.downloadSelect.field.getText());
				}

				try {
					int start = 0;
					if (!gui.startF.getText().trim().equals("")){
						start = Integer.parseInt(engine.gui.startF.getText().trim());
					}
					String cursor = "*";
					if (!gui.cursorF.getText().trim().equals("")){
						cursor = gui.cursorF.getText().trim();
					}
					engine.downloader = new Downloader(gui.apiF.getText(), query, qfs, downOut,
							(Downloader.OutputFormat) gui.downloadSelect.combo.getSelectedItem(), cursor, start) {
						@Override
						protected void process(List<String> chunks) {
							for (String s : chunks) {
								engine.append(s);
							}
						}
					};
				}catch (NumberFormatException e1){
					JOptionPane.showMessageDialog(gui.frame,
							"Invalid start number.", "Download",
							JOptionPane.ERROR_MESSAGE);
					throw e1;
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(gui.frame,
							"IO error while dowloading. Check input and output files.", "Download",
							JOptionPane.ERROR_MESSAGE);
					throw e1;
				}

				engine.downloader.addPropertyChangeListener(e -> {
					if (e.getPropertyName().equals("progress"))
						gui.progressBar.setValue((Integer) e.getNewValue());
				});
				try {
					engine.downloader.execute();
					engine.downloadResult = engine.downloader.get();
				} catch (InterruptedException e1) {
					throw e1;
				} catch (ExecutionException e1) {
					if (!e1.getMessage().contains("HTTP response code: 403")) {
						JOptionPane.showMessageDialog(gui.frame, "Unkown error while downloading.", "Download",
								JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(gui.frame,
								"Error while downloading. HTTP response: 403. Check API key.", "Download",
								JOptionPane.ERROR_MESSAGE);
					}
					throw e1;
				}

			}

			if (gui.descriptionBox.isSelected()) {
				File out = null;
				if (!gui.descrSelectOut.combo.getSelectedItem().equals(Descriptions.OutputFormat.None)) {
					out = new File(gui.descrSelectOut.field.getText());
				}

				if (gui.descrSelectIn.combo.getSelectedItem().equals(Descriptions.InputSource.FromDownload)) {
					// if (downloadResult == null){
					// throw new IOException("Download did not
					// finish
					// properly.");
					// }
					try {
						engine.descriptions = new Descriptions(engine.downloadResult, out,
								(Descriptions.OutputFormat) gui.descrSelectOut.combo.getSelectedItem()) {
							@Override
							protected void process(List<String> chunks) {
								for (String s : chunks)
									engine.append(s);
							}
						};
					} catch (FileNotFoundException e) {
						throw e;
					}
				} else if (gui.descrSelectIn.combo.getSelectedItem()
						.equals(Descriptions.InputSource.FromFileOrFolder)) {
					if (gui.descrSelectIn.field.getText().equals("")) {
						JOptionPane.showMessageDialog(gui.frame, "Input file is missing.", "Parsing",
								JOptionPane.ERROR_MESSAGE);
						throw new FileNotFoundException("Input file is missing in parsing.");
					}
					try {
						engine.descriptions = new Descriptions(new File(gui.descrSelectIn.field.getText()), out,
								(Descriptions.OutputFormat) gui.descrSelectOut.combo.getSelectedItem()) {
							@Override
							protected void process(List<String> chunks) {
								for (String s : chunks)
									engine.append(s);
							}
						};
					} catch (IOException e) {
						JOptionPane.showMessageDialog(gui.frame,
								"IO exception in parsing. Check input/output files.", "Parsing",
								JOptionPane.ERROR_MESSAGE);
						throw e;
					} catch (ParseException e) {
						JOptionPane.showMessageDialog(gui.frame,
								"IO exception in parsing. Input file may be corrupt.", "Parsing",
								JOptionPane.ERROR_MESSAGE);
						throw e;
					}
				}

				try {
					engine.descriptions.execute();
					engine.descriptionResult = engine.descriptions.get();
				} catch (InterruptedException e) {
					throw e;
				} catch (ExecutionException e) {
					JOptionPane.showMessageDialog(gui.frame, "Unknown error while parsing metadata.", "Parsing",
							JOptionPane.ERROR_MESSAGE);
					throw e;
				}
			}

			if (gui.nlpBox.isSelected()) {

				try {
					if (gui.nlpSelectionIn.combo.getSelectedItem()
							.equals(MagyarlancRunner.Input.FromMetadata)) {
						// if (descriptionResult == null)
						// throw new IOException("Metadata parsing did
						// not
						// finish properly.");
						engine.magyarlanc = new MagyarlancRunner(gui.magyarlancF.getText(), engine.descriptionResult) {
							@Override
							protected void process(List<String> chunks) {
								for (String s : chunks)
									engine.append(s);
							}
						};
					} else if (gui.nlpSelectionIn.combo.getSelectedItem()
							.equals(MagyarlancRunner.Input.FromFileOrFolder)) {
						if (gui.nlpSelectionIn.field.getText().equals(""))
							throw new FileNotFoundException("Input file or folder is missing.");
						engine.magyarlanc = new MagyarlancRunner(gui.magyarlancF.getText(),
								new File(gui.nlpSelectionIn.field.getText())) {
							@Override
							protected void process(List<String> chunks) {
								for (String s : chunks)
									engine.append(s);
							}
						};
					}
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(gui.frame, "Error during NLP. Check input and output files.",
							"NLP", JOptionPane.ERROR_MESSAGE);
					throw e1;
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(gui.frame, "Error during NLP. Check input and output files.",
							"NLP", JOptionPane.ERROR_MESSAGE);
					throw e1;
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(gui.frame, "Error during NLP. Check magyarlánc's path.",
							"NLP", JOptionPane.ERROR_MESSAGE);
					throw e1;
				} 

				engine.magyarlanc.addPropertyChangeListener(e -> {
					if (e.getPropertyName().equals("progress"))
						gui.progressBar.setValue((Integer) e.getNewValue());
				});

				try {
					engine.magyarlanc.execute();
					engine.wordCollection = engine.magyarlanc.get();
				} catch (OutOfMemoryError e1){
					JOptionPane.showMessageDialog(gui.frame, "Out of memory. You need more heap space.", "NLP",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				
				} catch (InterruptedException e1) {
					throw e1;
				} catch (ExecutionException e1) {
					JOptionPane.showMessageDialog(gui.frame, "Unknown error during NLP.", "NLP",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}

				for (String s : engine.magyarlanc.getBasicInfo())
					engine.appendResult(s);
				engine.appendResult("");
				engine.appendResult("");
				for (String s : engine.magyarlanc.getFullInfo())
					engine.appendResult(s);
				
				
				
				if (engine.gui.nlpSelectionOut.combo.getSelectedItem().equals(MagyarlancRunner.Output.Full)){
					File out = new File(engine.gui.nlpSelectionOut.field.getText());
					PrintWriter pw = new PrintWriter(out);
					for (String s : engine.magyarlanc.getBasicInfo())
						pw.println(s);
					for (String s : engine.magyarlanc.getFullInfo())
						pw.println(s);
					pw.flush();
					pw.close();
					
				}
//				
//				LinkedHashMap<String, Integer> wordMap = engine.magyarlanc.getWordMap();
//				wordMap.entrySet().stream()
//				.sorted(Map.Entry.comparingByValue((i1,i2)->Integer.compare(i2, i1)))
//				.forEach(e -> engine.appendResult(e.getKey() + "   ---   " + e.getValue()));
				
			}
			
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}finally{
						engine.append("Normal termination.");
					}
				}
			}).start();
			

		} catch (InterruptedException | CancellationException e) {
			JOptionPane.showMessageDialog(gui.frame, "Process is canceled.", "Cancel",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			engine.append("Abnormal termination: "+e.getMessage());
			e.printStackTrace();
		} finally {
			gui.stop.setEnabled(false);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}finally{
						gui.start.setEnabled(true);
					}
					
				}
			}).start();
		}
		
		

	}

	void cancel(){
		engine.append("Task canceled.");
		if (engine.downloader != null) {
			engine.downloader.cancel(true);
			engine.downloader = null;
		}
		if (engine.descriptions != null) {
			engine.descriptions = null;
		}
		if (engine.magyarlanc != null) {
			engine.magyarlanc.cancel(true);
			engine.magyarlanc = null;
		}
		interrupt();
		gui.stop.setEnabled(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}finally{
					gui.start.setEnabled(true);
				}
				
			}
		}).start();
	}

}
