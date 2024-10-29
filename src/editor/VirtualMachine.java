package editor;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;

public class VirtualMachine {
	private int memorySize;
	private int[] memory;
	private HashMap<String, Integer> varTable; //Table maps varName -> index of value in memory
	private int programCounter;
	private int memPointer;
	private String[] codeLines;
	private HashMap<String, Integer> gotoLabels;
	
	private JTextArea output;
	
	public VirtualMachine(int memSize, JTextArea out) {
		memorySize = memSize;
		output = out;
		clearState();
		
	}
	
	//Prepare code from text into String Array, register goto labels
	public void loadCode(String code) {
		if (code == null) {
			return;
		}
		codeLines = code.split("\n");
		clearState();
		for (int i = 0; i<codeLines.length; i++) {
			if (codeLines[i].charAt(0) == '#') {
				gotoLabels.put(codeLines[i].substring(1), i);
				output.append("Address " + i + " stored as " + codeLines[i].substring(1) + "\n");
			}
		}
		output.append("Code loaded successfully.\n");
		
	}
	
	public void executeNextLine() {
		String line;
		try {
			line = codeLines[programCounter];
		} catch (Exception ex) {
			System.out.println("ProgramCounter out of bounds.");
			return;
		}
		
		// SYNTAX PATTERNS
		Pattern assnPattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9]*) = ([a-zA-Z0-9]+)$");
		Matcher assnMatcher = assnPattern.matcher(line);
		Pattern gotoPattern = Pattern.compile("^goto ([a-zA-Z0-9]+)$");
		Matcher gotoMatcher = gotoPattern.matcher(line);
		
		//PATTERN MATCHING AND PROCESS
		//ASSIGNMENT
		if (assnMatcher.find()) {
	
			if (assnMatcher.group(1) != null && assnMatcher.group(2) != null) {
				
				if (varTable.containsKey(assnMatcher.group(2))) {
					try {
						assignVar(assnMatcher.group(1), memory[varTable.get(assnMatcher.group(2))]);
					} catch(Exception ex) {
						ex.printStackTrace();
						
					}
				} else {
				
					try {
						int val = Integer.valueOf( assnMatcher.group(2));
						assignVar(assnMatcher.group(1), val);
					} catch (Exception ex) {
						System.out.println(ex);
						ex.printStackTrace();
					
					}
				}
			}
		}
		
		//GOTO
		if (gotoMatcher.find()) {
			if (gotoMatcher.group(1) != null) {
				String dest = gotoMatcher.group(1);
				if (gotoLabels.containsKey(dest)) {
					programCounter = gotoLabels.get(dest);
					return;
				} else {
					try {
						programCounter = Integer.valueOf(dest);
					} catch (Exception ex) {
						output.append("Goto argument invalid. Unrecognised label and not a number.");
					}
				}
				
			}
		}
		
		
		
		programCounter++;
	}
	
	public boolean running() {
		return programCounter < codeLines.length && programCounter != -1;
	}
	
	//Reset internal state of language
	private void clearState() {
		memory = new int[memorySize];
		varTable = new HashMap<String, Integer>();
		gotoLabels = new HashMap<String, Integer>();
		programCounter = 0;
		memPointer = 0;
	}
	
	private void assignVar(String varName, int value) throws Exception {
		if (varTable.containsKey(varName)) {
			memory[varTable.get(varName)] = value;
			System.out.println("Variable " + varName + " has changed to " + value);
			output.append(varName + " set to " + value+"\n");
		} else {
			
			if (memPointer >= memorySize) {
				throw new Exception("MEMORY ERROR: All memory allocated.");
			}
			
			memory[memPointer] = value;
			varTable.put(varName, memPointer);
			memPointer++;
			System.out.println("Variable: " + varName + " = " + value + " in memory space " + (memPointer-1));
			output.append("Variable: " + varName + " = " + value + " in memory space " + (memPointer-1)+"\n");
		}
	}

}
