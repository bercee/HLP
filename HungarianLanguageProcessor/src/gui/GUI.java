package gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A class for displaying the GUI components. For initializing the GUI, 
 * one simply has to instantiate this class from the Event Dispatch Thread. 
 * @author Berci
 *
 */
public class GUI {

	public class Row extends JPanel{
		private static final long serialVersionUID = 1L;
		
		public Row(Component... c) {
			setAlignmentX(Component.LEFT_ALIGNMENT);
			FlowLayout l = new FlowLayout();
			l.setAlignment(FlowLayout.LEFT);
			l.setHgap(10);
			l.setVgap(5);
			setLayout(l);
			for (Component cc : c){
				add(cc);
			}
		}
	}
	
	
	
	public class Selection extends Row implements ActionListener{
		private static final long serialVersionUID = 1L;

		JLabel label1 = new JLabel("Output:");
		JComboBox<Object> combo = new JComboBox<Object>();
		JLabel label2 = new JLabel("to");
		JTextField field = new JTextField();
		JButton select = new JButton("Select");
		public Selection() {
			super();
			add(label1);
			add(combo);
			add(label2);
			add(field);
			add(select);
			setPrefWidth(field, 250);
			select.addActionListener(this);
		}
		
		public Selection(String label1, Object[] elements){
			this();
			this.label1.setText(label1);
			for (Object o : elements)
				combo.addItem(o);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (jfc.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION){
				field.setText(jfc.getSelectedFile().getAbsolutePath());
			}
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			combo.setEnabled(enabled);
			field.setEnabled(enabled);
			select.setEnabled(enabled);
			if (combo.isEnabled())
				combo.setSelectedIndex(combo.getSelectedIndex());
		}
		
		void setDeactivator(Object item){
			combo.addActionListener(e -> {
				boolean b = combo.getSelectedItem().equals(item);
				field.setEnabled(!b);
				select.setEnabled(!b);
			});
							
		}
	}
	
	
	JFileChooser jfc = new JFileChooser();
	{
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		jfc.setCurrentDirectory(new File("."));
	}
	
	final GUIEngine engine = new GUIEngine(this);

	//Dowload panel and stuff
	
	JCheckBox downloadBox = new JCheckBox("Download metadata");
	
	JLabel queryL = new JLabel("Search query:");
	JTextField queryF = new JTextField();
	
	JLabel apiL = new JLabel("API key:");
	JTextField apiF = new JTextField();
	
	JLabel qfL = new JLabel("Query refinements:");
	

	JTextField qfF1 = new JTextField();
	JLabel qfL2 = new JLabel(":");
	JTextField qfF2 = new JTextField();
	
	Selection downloadSelect = new Selection("Output:", Downloader.OutputFormat.values());
	JLabel cursorL = new JLabel("First cursor:");
	JTextField cursorF = new JTextField("*");
	JLabel startL = new JLabel("Start at:");
	JTextField startF = new JTextField("0");
	
	JPanel downloadPanel = new JPanel();
	
	ArrayList<Component> downloadComponents = new ArrayList<>();
	{
		downloadComponents.add(queryF);
		downloadComponents.add(apiF);
		downloadComponents.add(qfF1);
		downloadComponents.add(qfF2);
		downloadComponents.add(downloadSelect);
		downloadComponents.add(cursorF);
		downloadComponents.add(startF);
	}
	
	
	
	//Description panel
	JCheckBox descriptionBox = new JCheckBox("Parse metadata");
	Selection descrSelectIn = new Selection("Metadata input:", Descriptions.InputSource.values());
	{
		descrSelectIn.label2.setText("from");
	}
	Selection descrSelectOut = new Selection("Description output:", Descriptions.OutputFormat.values());
	
	JPanel descriptionPanel = new JPanel();

	ArrayList<Component> descriptionComponents = new ArrayList<>();
	{
		descriptionComponents.add(descrSelectIn);
		descriptionComponents.add(descrSelectOut);
	}
	
	//NLP box
	JCheckBox nlpBox = new JCheckBox("Perform NLP on description");
	
	Selection nlpSelectionIn = new Selection("Descriptions input:", MagyarlancRunner.Input.values());
	{
		nlpSelectionIn.label2.setText("from");
	}
	Selection nlpSelectionOut = new Selection("Output:                                       ", MagyarlancRunner.Output.values());
	JTextField magyarlancF = new JTextField();
	JLabel magyarlancL = new JLabel("Magyarlánc path:");
	JButton magyarlancB = new JButton("Select");
	
	JPanel nlpPanel = new JPanel();	
	
	ArrayList<Component> nlpComponents = new ArrayList<>();
	
	{
		nlpComponents.add(nlpSelectionIn);
		nlpComponents.add(nlpSelectionOut);
		nlpComponents.add(magyarlancF);
		nlpComponents.add(magyarlancB);
	}

	
	
	//frame
	JButton start = new JButton("START");
	JButton stop = new JButton("STOP");
	JProgressBar progressBar = new JProgressBar();
	JTextArea progressArea = new JTextArea();
	JScrollPane progressPane = new JScrollPane(progressArea);
	JTextArea resultsArea = new JTextArea();
	JScrollPane resultsPane = new JScrollPane(resultsArea);

	

	JFrame frame = new JFrame("Europeana Language Processor");
	JScrollPane fullPane;
	
	{
		
		downloadSelect.setDeactivator(Downloader.OutputFormat.None);
		descrSelectIn.setDeactivator(Descriptions.InputSource.FromDownload);
		descrSelectOut.setDeactivator(Descriptions.OutputFormat.None);
		nlpSelectionIn.setDeactivator(MagyarlancRunner.Input.FromMetadata);
		nlpSelectionOut.setDeactivator(MagyarlancRunner.Output.None);
		
		//Download panel
		setPrefWidth(queryF, 150);
		setPrefWidth(apiF, 150);
		setPrefWidth(qfF1, 150);
		setPrefWidth(qfF2, 50);
		setPrefWidth(cursorF, 300);
		setPrefWidth(startF, 50);
		
		downloadBox.setSelected(true);
		downloadBox.addActionListener(e -> {boolean b = downloadBox.isSelected(); 
					downloadComponents.forEach(x -> x.setEnabled(b));});
		downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.Y_AXIS));
		downloadPanel.add(new Row(downloadBox));
		downloadPanel.add(new Row(apiL, apiF));
		downloadPanel.add(new Row(queryL,queryF));
		downloadPanel.add(new Row(qfL, qfF1, qfL2, qfF2));
		downloadPanel.add(new Row(cursorL,cursorF, startL, startF));
		downloadPanel.add(downloadSelect);
		downloadSelect.combo.setSelectedItem(Downloader.OutputFormat.None);
		downloadPanel.setBorder(BorderFactory.createTitledBorder("Download"));
		
		
		//Description panel
		descriptionBox.setSelected(true);
		descriptionBox.addActionListener(e -> {descrSelectIn.setEnabled(descriptionBox.isSelected());
		descrSelectOut.setEnabled(descriptionBox.isSelected());});
		descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));
		descriptionPanel.add(new Row(descriptionBox));
		descriptionPanel.add(descrSelectIn);
		descriptionPanel.add(descrSelectOut);
		descrSelectIn.combo.setSelectedItem(Descriptions.InputSource.FromDownload);
		descrSelectOut.combo.setSelectedItem(Descriptions.OutputFormat.None);
		descriptionPanel.setBorder(BorderFactory.createTitledBorder("Get Description"));
		
		//NLP panel
		setPrefWidth(magyarlancF, 300);
		nlpBox.setSelected(true);
		nlpBox.addActionListener(e -> nlpComponents.forEach(c -> c.setEnabled(nlpBox.isSelected())));
		magyarlancB.addActionListener(e -> {if (jfc.showOpenDialog(frame)== JFileChooser.APPROVE_OPTION) magyarlancF.setText(jfc.getSelectedFile().getAbsolutePath());});
		nlpPanel.setLayout(new BoxLayout(nlpPanel, BoxLayout.Y_AXIS));
		nlpPanel.add(new Row(nlpBox));
		nlpPanel.add(new Row(magyarlancL, magyarlancF, magyarlancB));
		nlpPanel.add(nlpSelectionIn);
		nlpPanel.add(nlpSelectionOut);
		nlpSelectionIn.combo.setSelectedItem(MagyarlancRunner.Input.FromMetadata);
		nlpSelectionOut.combo.setSelectedItem(MagyarlancRunner.Output.None);
		
		
		//frame
		Container fullPanel = frame.getContentPane();
		fullPane = new JScrollPane(fullPanel);
		frame.setContentPane(fullPane);
		
		
		start.addActionListener(e -> engine.start());
		stop.addActionListener(e -> engine.stop());
		
		
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		nlpPanel.setBorder(BorderFactory.createTitledBorder("NLP"));
		fullPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 10, 10, 0);
		fullPanel.add(downloadPanel,c);
		
		c.gridy = 1;
		fullPanel.add(descriptionPanel, c);
		
		c.gridy = 2;
		fullPanel.add(nlpPanel, c);
		
		setPrefWidth(progressBar, 450);
		progressBar.setStringPainted(true);
		c.gridy = 3;
		fullPanel.add(new Row(start, stop, progressBar),c);
		
		progressArea.setFont(progressArea.getFont().deriveFont(11.5f));
		resultsArea.setFont(progressArea.getFont().deriveFont(11.5f));
			
		c.gridy = 4;
		c.weighty = 1;
		progressPane.setPreferredSize(new Dimension(600, 100));
		fullPanel.add(progressPane, c);
		
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridheight = 5;
		resultsPane.setPreferredSize(new Dimension(600, 600));
		fullPanel.add(resultsPane,c);
	}
	
	
	public GUI() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		

		
		frame.pack();
		frame.setVisible(true);
	}
	
	private static void setPrefWidth(Component c, int newWidth){
		c.setPreferredSize(new Dimension(newWidth, c.getPreferredSize().height));
	}
	
	
}
