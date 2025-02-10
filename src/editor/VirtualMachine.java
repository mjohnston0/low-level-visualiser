package editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.Color;

public class VirtualMachine {
	private int memorySize;
	private int[] memory;
	private HashMap<String, Integer> varTable; //Table maps varName -> index of value in memory
	private int programCounter;
	private int lagCounter; //Points to previous line executed
	private int memPointer;
	private String[] codeLines;
	private HashMap<String, Integer> gotoLabels;
	private int[] lineIndex; // Where x[i] -> index of line start of ith line
	private int[] lineLengths;
	
	private JTextArea output;
	private JTextPane cDisplay;
	
	private Style regular;
	private Style toExe;
	private Style executed;
	
	
	
	public VirtualMachine(int memSize, JTextArea out, JTextPane cDisp) {
		memorySize = memSize;
		output = out;
		cDisplay = cDisp;
		
		this.regular = cDisplay.addStyle("regular", null);
		StyleConstants.setForeground(regular, Color.black);
		StyleConstants.setItalic(regular, false);
		StyleConstants.setBold(regular, false);
		
		this.toExe = cDisplay.addStyle("red", null);
		StyleConstants.setForeground(toExe, Color.red);
		StyleConstants.setBold(toExe, true);
		
		this.executed = cDisplay.addStyle("done", null);
		StyleConstants.setItalic(executed, true);
		StyleConstants.setForeground(executed, Color.blue);
		
		clearState();
		
	}
	
	//Prepare code from text into String Array, register goto labels
	public void loadCode(String[] codeArray) {
		//this.codeLines = codeArray;
		ArrayList<String> codeBuffer = new ArrayList<String>();
		ArrayList<Integer> lineIndices = new ArrayList<Integer>();
		ArrayList<Integer> lineLens = new ArrayList<Integer>();
		int lindexCntr = 0;
		if (codeArray == null) {
			return;
		}
		clearState();
		int gotoAddr = 0;
		for (int i = 0; i<codeArray.length; i++) {
			if (codeArray[i].length() == 0) {
				continue;
			}
			
			if (codeArray[i].charAt(0) == '#') {
				gotoLabels.put(codeArray[i].substring(1), gotoAddr);
				output.append("Address " + gotoAddr + " stored as " + codeArray[i].substring(1) + "\n");
				lindexCntr += codeArray[i].length()+1;
				continue;
			}
			lineIndices.add(lindexCntr);
			codeBuffer.add(codeArray[i]);
			lineLens.add(codeArray[i].length() + 5 + String.valueOf(gotoAddr).length());
			lindexCntr += codeArray[i].length() + 5 + String.valueOf(gotoAddr).length();
			gotoAddr++;
		}
		lineIndices.add(lindexCntr);
		this.codeLines = new String[codeBuffer.size()];
		this.lineIndex = new int[lineIndices.size()];
		this.lineLengths = new int[lineLens.size()];
		for (int i=0;i<codeLines.length;i++) {
			codeLines[i] = codeBuffer.get(i);
			lineLengths[i] = lineLens.get(i);
		}
		for (int i=0;i<lineIndices.size();i++) {
			this.lineIndex[i] = lineIndices.get(i);
			System.out.print(lineIndex[i] + " ");
		}
		output.append("Code loaded successfully.\n");
		
		
	}
	
	public void executeNextLine() {
		String line;
		int pcNext = programCounter;
		
		try {
			line = codeLines[programCounter];
		} catch (Exception ex) {
			System.out.println("ProgramCounter out of bounds.");
			output.append("Program Counter out of bounds.\n");
			return;
		}
		
		// SYNTAX PATTERNS
		Pattern assnPattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9]*) *= *(-?[a-zA-Z0-9]+)$");
		Matcher assnMatcher = assnPattern.matcher(line);
		Pattern gotoPattern = Pattern.compile("^goto (-?[a-zA-Z0-9]+)$");
		Matcher gotoMatcher = gotoPattern.matcher(line);
		Pattern ifGotoPattern = Pattern.compile("^if (-?[a-zA-Z0-9]+) *(==|<|>|<=|>=|!=) *(-?[a-zA-Z0-9]+) goto (-?[a-zA-Z0-9]+)$");
		Matcher ifGotoMatcher = ifGotoPattern.matcher(line);
		Pattern compPattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9]*) *= *(-?[a-zA-Z0-9]+) *(\\+|-|\\*|/) *(-?[a-zA-Z0-9]+)$");
		Matcher compMatcher = compPattern.matcher(line);
		
		//PATTERN MATCHING AND PROCESS
		//ASSIGNMENT
		if (assnMatcher.find()) {
	
			if (assnMatcher.group(1) != null && assnMatcher.group(2) != null) {
				
				try {
					int val = evalVarName(assnMatcher.group(2));
					assignVar(assnMatcher.group(1), val);
					pcNext++;
				} catch (Exception ex) {
					output.append("Error with assignment: "+ex+"\n");
					pcNext++;
				}
			}
		}
		//GOTO
		else if (gotoMatcher.find()) {
			if (gotoMatcher.group(1) != null) {
				String dest = gotoMatcher.group(1);
				if (gotoLabels.containsKey(dest)) {
					pcNext = gotoLabels.get(dest);
				} else {
					try {
						pcNext = Integer.valueOf(dest);
					} catch (Exception ex) {
						output.append("Goto argument invalid. Unrecognised label and not a number.");
						pcNext++;
					}
				}
				
			}
		}
		
		//IF COND GOTO
		else if (ifGotoMatcher.find()) {
			System.out.println(ifGotoMatcher.group(1) + "|" + ifGotoMatcher.group(2) + "|" + ifGotoMatcher.group(3)+ "|" + ifGotoMatcher.group(4));
			String operator = ifGotoMatcher.group(2);
			String cond1 = ifGotoMatcher.group(1);
			String cond2 = ifGotoMatcher.group(3);
			String dest = ifGotoMatcher.group(4);
			boolean result = false;
			try {
				result = evaluateBoolExpression(cond1, cond2, operator);
			} catch (Exception ex) {
				output.append("Error: "+ex);
				pcNext++;
			}
			
			if (result) {
				if (gotoLabels.containsKey(dest)) {
					pcNext = gotoLabels.get(dest);
					
				} else {
					try {
						pcNext = Integer.valueOf(dest);
						
					} catch (Exception ex) {
						output.append("Goto argument invalid. Unrecognised label and not a number.");
						pcNext++;
					}
				}
				
			} else {
				pcNext++;
			}
			
		}
		
		//COMPUTATION
		else if (compMatcher.find()) {
			String arg1 = compMatcher.group(2);
			String arg2 = compMatcher.group(4);
			String op = compMatcher.group(3);
			String assnName = compMatcher.group(1);
			int result;
			try {
				result = evaluateExpression(arg1, arg2, op);
				
				try {
					assignVar(assnName, result);
					pcNext++;
				} catch (Exception ex) {
					output.append("Failed to assign to variable with error: "+ex+"\n");
					pcNext++;
				}
				
			} catch (Exception ex) {
				output.append("Computation failed with error: "+ex);
				pcNext++;
			}
			
		} else {
			output.append("Failed to recognise command on line " + programCounter + ".\n");
			pcNext++;
		}
		
		StyledDocument doc = cDisplay.getStyledDocument();
		
		//System.out.println(lineIndex[pcNext]);
		//System.out.println(lineIndex[programCounter]);
		// lagcounter -> default
		doc.setCharacterAttributes(lineIndex[lagCounter], lineLengths[lagCounter], regular, true);
		// pc -> executed
		doc.setCharacterAttributes(lineIndex[programCounter], lineLengths[programCounter], executed, true);
		// pcNext -> to be executed
		if (pcNext < lineIndex.length-1) {
			doc.setCharacterAttributes(lineIndex[pcNext], lineLengths[pcNext], toExe, true);
		}
		// lagcounter = pc
		lagCounter = programCounter;
		// pc = pcNext
		programCounter = pcNext;
		
		
		
	}
	
	public boolean running() {
		if (codeLines == null) return false;
		
		return programCounter < codeLines.length && 
				programCounter != -1 &&
				codeLines != null;
	}
	
	public int getProgramCounter() {
		return programCounter;
	}
	
	
	//Reset internal state of language
	private void clearState() {
		memory = new int[memorySize];
		varTable = new HashMap<String, Integer>();
		gotoLabels = new HashMap<String, Integer>();
		programCounter = 0;
		lagCounter = 0;
		memPointer = 0;
	}
	
	private boolean evaluateBoolExpression(String cond1, String cond2, String op) throws Exception {
		int val1, val2;
		val1 = evalVarName(cond1);
		val2 = evalVarName(cond2);
		
		//==|<|>|<=|>=|!=
		switch (op) {
		case "==":
			if (val1 == val2) {
				return true;
			} else {
				return false;
			}
		case "<":
			if (val1 < val2) {
				return true;
			} else {
				return false;
			}
		case ">":
			if (val1 > val2) {
				return true;
			} else {
				return false;
			}
		case "<=":
			if (val1 <= val2) {
				return true;
			} else {
				return false;
			}
		case ">=":
			if (val1 >= val2) {
				return true;
			} else {
				return false;
			}
		case "!=":
			if (val1 != val2) {
				return true;
			} else {
				return false;
			}
		}
		
		return false;
	}
	
	private int evaluateExpression(String arg1, String arg2, String op) throws Exception {
		int val1, val2;
		val1 = evalVarName(arg1);
		val2 = evalVarName(arg2);
		
		switch (op) {
		case "+":
			return val1 + val2;
		case "-":
			return val1 - val2;
		case "*":
			return val1 * val2;
		case "/":
			return val1 / val2;
		}
		
		throw new Exception("Invalid operator: "+op);
	}
	
	private int evalVarName(String name) throws Exception {
		int positive = 1;
		if (name.startsWith("-")) {
			positive = -1;
			name = name.substring(1);
		}
		if (varTable.containsKey(name)) {
			return positive * memory[varTable.get(name)];
		} else {
			try {
				return positive * Integer.valueOf(name);
			} catch (Exception ex) {
				System.out.println(name + " is an invalid var name or argument.");
				throw new Exception(name + " is an invalid var name or argument.");
			}
		}
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
