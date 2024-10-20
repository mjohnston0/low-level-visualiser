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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Editor extends JFrame implements ActionListener {
	
	//Editor text area
	JTextArea textArea;
	JFileChooser fileChooser;

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
		
		menuBar.add(file);
		
		setJMenuBar(menuBar);
		
		//Set up main areas
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new GridLayout(1,2));
		
		textArea = new JTextArea();
		textArea.setFont(new Font("Courier new", Font.PLAIN, 16));
		
		JPanel executionPanel = new JPanel();
		
		JScrollPane textAreaScroll = new JScrollPane(textArea);
		
		wrapper.add(textAreaScroll);
		wrapper.add(executionPanel);
		add(wrapper, BorderLayout.CENTER);
		setSize(800,600);
		setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		switch (command) {
		case "New":
			this.textArea.setText("");
			break;
		case "Save":
			
			break;
		case "Open":
			int success = fileChooser.showOpenDialog(null);
			
			if (success == JFileChooser.APPROVE_OPTION) {
				Path targetPath = fileChooser.getSelectedFile().toPath();
				
				try {
					String data = Files.readString(targetPath);
					this.textArea.setText(data);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, "Error opening this file.");
				}
			}
		}
		
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		Editor edit = new Editor();

	}


}
