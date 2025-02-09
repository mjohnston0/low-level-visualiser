package editor;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Editor extends JFrame implements ActionListener {
	
	//Editor text area
	private JTextArea textArea;
	private JTextArea console;
	private JTextArea codeDisplay;
	private JFileChooser fileChooser;
	private JLabel progCounterLabel;
	
	//Path to save location for faster saving
	private File activeFile;
	
	private VirtualMachine virtualM;

	public Editor() {
		
		//Main Window Settings
		setTitle("Code Editor");
		setSize(800, 600);
		
		//Style to match system
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		fileChooser = new JFileChooser();
		
		//Set up top menu options
		JMenuBar menuBar = new JMenuBar();
		
		JMenu file = new JMenu("File");
		
		JMenuItem optionNew = new JMenuItem("New");
		optionNew.setIcon(UIManager.getIcon("FileView.fileIcon"));
		JMenuItem optionOpen = new JMenuItem("Open");
		optionOpen.setIcon(UIManager.getIcon("FileView.directoryIcon"));
		JMenuItem optionSave = new JMenuItem("Save");
		optionSave.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
		JMenuItem optionSaveAs = new JMenuItem("Save As...");
		optionSave.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
		
		optionNew.addActionListener(this);
		optionOpen.addActionListener(this);
		optionSave.addActionListener(this);
		optionSaveAs.addActionListener(this);
		
		file.add(optionNew);
		file.add(optionOpen);
		file.add(optionSave);
		file.add(optionSaveAs);
		
		JMenu code = new JMenu("Code");
		
		JMenuItem quickRun = new JMenuItem("Quick Run");
		quickRun.addActionListener(this);
		code.add(quickRun);
		
		JMenuItem loadCode = new JMenuItem("Load Code");
		loadCode.addActionListener(this);
		code.add(loadCode);
		
		JMenuItem executeLine = new JMenuItem("Execute Next Line");
		executeLine.addActionListener(this);
		code.add(executeLine);
		
		menuBar.add(file);
		menuBar.add(code);
		setJMenuBar(menuBar);
		
		//Set up main areas
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new GridLayout(1,2));
		
		textArea = new JTextArea();
		textArea.setFont(new Font("Courier new", Font.PLAIN, 16));
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new GridLayout(2,1));
		
		console = new JTextArea();
		console.setEditable(false);
		JScrollPane consoleScroll = new JScrollPane(console);
		
		codeDisplay = new JTextArea();
		codeDisplay.setEditable(false);
		JScrollPane codeDisplayScroll = new JScrollPane(codeDisplay);
		
		rightPanel.add(codeDisplayScroll);
		rightPanel.add(consoleScroll);
		
		JScrollPane textAreaScroll = new JScrollPane(textArea);
		
		wrapper.add(textAreaScroll);
		wrapper.add(rightPanel);
		add(wrapper, BorderLayout.CENTER);
		
		//Top Button Panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JButton openButton = new JButton("", UIManager.getIcon("FileView.directoryIcon"));
		openButton.setPreferredSize(new Dimension(40,40));
		openButton.setToolTipText("Open File");
		openButton.setActionCommand("Open");
		openButton.addActionListener(this);
		
		JButton saveButton = new JButton("", UIManager.getIcon("FileView.floppyDriveIcon"));
		saveButton.setPreferredSize(new Dimension(40,40));
		saveButton.setToolTipText("Save File");
		saveButton.setActionCommand("Save");
		saveButton.addActionListener(this);
		
		JButton loadCodeBtn = new JButton("Load Code");
		loadCodeBtn.setActionCommand("Load Code");
		loadCodeBtn.addActionListener(this);
		
		JButton executeLineBtn = new JButton("Execute Line");
		executeLineBtn.setActionCommand("Execute Next Line");
		executeLineBtn.addActionListener(this);
		
		JButton runAllBtn = new JButton("Execute All");
		runAllBtn.setActionCommand("Quick Run");
		runAllBtn.addActionListener(this);
		
		buttonPanel.add(openButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(loadCodeBtn);
		buttonPanel.add(executeLineBtn);
		buttonPanel.add(runAllBtn);
		
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		progCounterLabel = new JLabel("Program Counter: 0");
		
		statusPanel.add(progCounterLabel);

		add(buttonPanel, BorderLayout.NORTH);
		bottomPanel.add(statusPanel);
		
		add(bottomPanel, BorderLayout.SOUTH);
		
		virtualM = new VirtualMachine(128, console);
		
		setVisible(true);
		
	}
	
	private void updateTitle() {
		if (activeFile != null) {
			setTitle("Code Editor (" + activeFile.getName() + ")");
		} else {
			setTitle("Code Editor");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		int success;
		
		switch (command) {
		case "New":
			this.textArea.setText("");
			activeFile = null;
			updateTitle();
			break;
		case "Save":
			if (activeFile == null) {
				success = fileChooser.showSaveDialog(null);
				
				if (success == JFileChooser.APPROVE_OPTION) {
					activeFile = fileChooser.getSelectedFile();
				} else {
					break;
				}
			}
				
			try {
				FileWriter fileWriter = new FileWriter(activeFile, false);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				
				bufferedWriter.write(textArea.getText());
				bufferedWriter.close();
				fileWriter.close();
			} catch (IOException err) {
				err.printStackTrace();
			}
			
			updateTitle();
			break;
			
		case "Save As...":
			success = fileChooser.showSaveDialog(null);
			
			if (success == JFileChooser.APPROVE_OPTION) {
				activeFile = fileChooser.getSelectedFile();
				
				try {
					FileWriter fileWriter = new FileWriter(activeFile, false);
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
					
					bufferedWriter.write(textArea.getText());
					bufferedWriter.close();
					fileWriter.close();
				} catch (IOException err) {
					err.printStackTrace();
				}
				
			}
			
			updateTitle();
			break;
		case "Open":
			success = fileChooser.showOpenDialog(null);
			
			if (success == JFileChooser.APPROVE_OPTION) {
				activeFile = fileChooser.getSelectedFile();
				Path targetPath = activeFile.toPath();
				
				try {
					String data = Files.readString(targetPath);
					this.textArea.setText(data);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, "Error opening this file.");
				}
			}
			updateTitle();
			break;
		case "Quick Run":
			loadCode();
			while (virtualM.running()) {
				virtualM.executeNextLine();
				progCounterLabel.setText("Program Counter: "+ virtualM.getProgramCounter());
			}
			console.append("Execution Complete.\n");
			break;
			
		case "Load Code":
			loadCode();
			break;
			
		case "Execute Next Line":
			if (virtualM.running()) {
				virtualM.executeNextLine();
				progCounterLabel.setText("Program Counter: "+ virtualM.getProgramCounter());
			} else {
				console.append("Error: No code to execute\n");
			}
			
			break;
			
		}
		
	}
	
	private void loadCode() {
		codeDisplay.setText("");
		progCounterLabel.setText("Program Counter: 0");
		int lineCounter = 0;
		String[] codeLines = this.textArea.getText().split("\n");
		ArrayList<String> validLines = new ArrayList<String>();
		
		for(String line : codeLines) {
			if(line.length() > 0) {
				if(line.startsWith("//")) {
					continue;
				}
				validLines.add(line);
				if (!line.startsWith("#")) {
				    codeDisplay.append(lineCounter+"   "+line+"\n");
				    lineCounter++;
				} else {
					codeDisplay.append(line+"\n");
				}
				
			}
		}
		String[] code = new String[validLines.size()];
		for (int i=0; i < validLines.size(); i++) {
			code[i] = validLines.get(i);
		}
		virtualM.loadCode(code);
		
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		Editor edit = new Editor();

	}

}
