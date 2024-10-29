package editor;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("serial")
public class Editor extends JFrame implements ActionListener {
	
	//Editor text area
	private JTextArea textArea;
	private JTextArea console;
	private JTextArea codeDisplay;
	private JFileChooser fileChooser;
	
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
		
		virtualM = new VirtualMachine(8, console);
		
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
			virtualM.loadCode(this.textArea.getText());
			codeDisplay.setText(this.textArea.getText());
			while (virtualM.running()) {
				virtualM.executeNextLine();
			}
			break;
			
		case "Load Code":
			virtualM.loadCode(this.textArea.getText());
			codeDisplay.setText(this.textArea.getText());
			break;
			
		case "Execute Next Line":
			if (virtualM.running()) {
				virtualM.executeNextLine();
			} else {
				console.append("Error: No code to execute\n");
			}
			
			break;
			
		}
		
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		Editor edit = new Editor();

	}

}
