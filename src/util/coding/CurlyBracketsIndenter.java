package util.coding;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import util.IOUtils;
import util.StringUtils;

/**
 * CMD LINE class Suitable for indenting curly-bracket-blocky code, including, but not limited to c, java and awk
 *   Usage: java util.coding.CurlyBracketIndenter infile > outfile
 */
public class CurlyBracketsIndenter {

    private final int n_spaces;

    private CurlyBracketsIndenter(int n_spaces) {
        this.n_spaces = n_spaces;
    }

	private List<Line> readLines(InputStream is) throws IOException {
		List<String> rawLines = IOUtils.readLines(is);
		List<Line> lines = new ArrayList<Line>(rawLines.size());
		for (int i=0; i<rawLines.size(); i++) {
			String text = rawLines.get(i);
			boolean commented = CodingUtils.isComment(text);
			boolean empty = StringUtils.isEmpty(text);
			LineType type;
			if (commented) {
				type = LineType.COMMENT;
			}
			else if (empty) {
				type = LineType.EMPTY;
			}
			else {
				int numOpening = StringUtils.countOccurences(text, '{');
				int numClosing= StringUtils.countOccurences(text, '}');
				if (numOpening == 0 && numClosing == 0) {
					type = LineType.NORMAL;
				}
				else if (numOpening == 1 && numClosing == 0) {
					type = LineType.OPENING;
				}
				else if (numOpening == 0 && numClosing == 1) {
					type = LineType.CLOSING;
				}
				else if (numOpening == numClosing) {
					// balanced
					type = LineType.NORMAL;
				}
				else {
					type = LineType.INVALID;
					throw new RuntimeException("More than one curly bracket on line "+(i+1)+": "+text);
				}
			}
			lines.add(new Line(text, type, i+1));
		}
		return lines;
	}

	private void run(InputStream is) throws IOException {
		List<Line> lines = readLines(is);
		Stack<Line> stack = new Stack<Line>();
		for (Line l: lines) {
			if (l.type == LineType.CLOSING) {
				if (stack.isEmpty()) {
					System.err.println("No opening bracket for line "+l.num);
				}
				else {
					Line beginLine = stack.pop();
					beginLine.closingLine = l;
					l.openingLine = beginLine;
				}
			}
			l.indentLevel = stack.size();
			if (l.type == LineType.OPENING) {
				stack.add(l);
			}

		}

		for (Line l: lines) {
			System.out.println(l);
		}
	}


	private class Line {
        String text;
        LineType type;
        int num;
        /** only for lines beginning a block */
        @SuppressWarnings("unused")
        Line closingLine;
        /** only for lines ending a block */
        @SuppressWarnings("unused")
        Line openingLine;
        /** indent level, according to curly braces */
        int indentLevel;

        Line(String text,
             LineType type,
             int num) {
            this.text = text;
            this.type = type;
            this.num = num;
        }

        @Override
        public String toString() {
            Pattern AND_PATTERN = Pattern.compile("^ *&&.*");
            Pattern OR_PATTERN = Pattern.compile("^ *\\|\\|.*");
        	String tmp = StringUtils.removeLeadingWhiteSpaces(text);
        	String indent = StringUtils.stringMultiply(n_spaces*indentLevel, " ");
        	if (type == LineType.EMPTY) {
        		return "";
        	}
        	else if (type == LineType.COMMENT && text.startsWith(" *")) {
        		return text;
        	}
        	else if (type == LineType.COMMENT && text.startsWith(" *")) {
        		return text;
        	}
            else if (AND_PATTERN.matcher(text).matches()) {
                return text;
            }
            else if (OR_PATTERN.matcher(text).matches()) {
                return text;
            }
        	else {
        		return indent+tmp;
        	}
        }
    }


	public static void main(String[] args) throws Exception {
	    String n_spaces_str = System.getenv("INDENT");
	    if (n_spaces_str == null) {
	        n_spaces_str = "4";
	    }

	    int n_spaces = Integer.parseInt(n_spaces_str);

		CurlyBracketsIndenter cba = new CurlyBracketsIndenter(n_spaces);
		if (args.length == 0) {
			cba.run(System.in);
		}
		else {
			cba.run(new FileInputStream(args[0]));
		}
	}

	private enum LineType {
		COMMENT,
		OPENING,
		CLOSING,
		NORMAL,
		EMPTY,
		INVALID;
	}


}
