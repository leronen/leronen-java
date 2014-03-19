package util.coding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.CollectionUtils;
import util.IOUtils;
import util.StringUtils;
import util.collections.MultiMap;
import util.collections.Pair;
import util.converter.Converter;

public class CompileProblemAnnotator {
    private static final Pattern WARNING_PATTERN = Pattern.compile("^([^:]*):([^:]*):([^:]*): warning: (.*)$");
    private static final Pattern ERROR_PATTERN = Pattern.compile("^([^:]*):([^:]*):([^:]*): error: (.*)$");

    private Map<Key, Warning> warningsByKey;
    MultiMap<String, Warning> warningsByFile;
    MultiMap<String, Integer> linesWithWarningByFile;

    private Map<Key, Error> errorsByKey;
    MultiMap<String, Error> errorsByFile;
    MultiMap<String, Integer> linesWithErrorByFile;

    private static final String WARNING_SUFFIX = "// _WARNING_: ";
    private static final String ERROR_SUFFIX = "// _ERROR_: ";

    enum WarningType {
        IGNORED_SYSTEM_RETURN_VALUE(".*ignoring return value of.*", true);

        Pattern pattern;
        boolean fixable;

        WarningType(String pattern, boolean fixable) {
            this.pattern = Pattern.compile(pattern);
            this.fixable = fixable;
        }

        public boolean matches(String line) {
            return pattern.matcher(line).matches();
        }
    }

    /** @return first matching type, or null if no matching type */
    private static WarningType deduceWarningType(String warningLine) {
        for (WarningType wt: WarningType.values()) {
            if (wt.matches(warningLine)) {
                return wt;
            }
        }
        return null;
    }

    private static List<Problem> readProblems(String[] problemFiles) throws IOException {
        ArrayList<Problem> problems = new ArrayList<>();
        for (String problemFile: problemFiles) {
            List<String> problemLines = IOUtils.readLines(problemFile);

            for (String problemLine: problemLines) {
                Matcher matcher = ERROR_PATTERN.matcher(problemLine);
                if (matcher.matches()) {
                    String file = matcher.group(1);
                    String line  = matcher.group(2);
                    String column = matcher.group(3);
                    String problemText = matcher.group(4);
                    Problem error = new Error(file, line, column, problemText);
                    problems.add(error);
                    continue;
                }

                matcher = WARNING_PATTERN.matcher(problemLine);
                if (matcher.matches()) {
                    String file = matcher.group(1);
                    String line  = matcher.group(2);
                    String column = matcher.group(3);
                    String problemText = matcher.group(4);
                    Problem warning = new Warning(file, line, column, problemText);
                    problems.add(warning);
                    continue;
                }

                System.err.println("UNMATCHED LINE: "+problemFile);
            }
        }
        return problems;
    }

    private static void log(String msg) {
        System.err.println(msg);
    }

    private void init(List<Problem> problems) {
        warningsByKey = new HashMap<>();
        warningsByFile = new MultiMap<>();
        linesWithWarningByFile = new MultiMap<>();
        errorsByKey = new HashMap<>();
        errorsByFile = new MultiMap<>();
        linesWithErrorByFile = new MultiMap<>();
        for (Problem problem: problems) {
            if (problem instanceof Warning) {
                warningsByKey.put(problem.key, (Warning)problem);
            }
            else if (problem instanceof Error) {
                errorsByKey.put(problem.key, (Error)problem);
            }
        }

        warningsByFile = new MultiMap<>();
        for (Key key: warningsByKey.keySet()) {
            Warning warning = warningsByKey.get(key);
            warningsByFile.put(warning.file, warning);

            linesWithWarningByFile.put(warning.file, warning.line);
        }

        errorsByFile = new MultiMap<>();
        for (Key key: errorsByKey.keySet()) {
            Error error = errorsByKey.get(key);
            errorsByFile.put(error.file, error);

            linesWithWarningByFile.put(error.file, error.line);
        }
    }

    private void fixLines(List<Line> lines) {
        for (Line line: lines) {
            if (line.warning != null && line.warning.isFixable()) {
                String newText = line.warning.fixLine(line.text);
                line.text = newText;
                line.warning = null;
                line.errorText = null;
                line.type = LineType.HARMLESS;
                log("Fixed line: "+line);
            }
        }
    }

    private class LineFormatter implements Converter<Line, String> {

        @Override
        public String convert(Line line) {
            return line.format();
        }
    }

    private Set<String> problematicFiles() {
        return CollectionUtils.union(errorsByFile.keySet(), warningsByFile.keySet());
    }

    private void run() throws IOException {
        Set<String> problematicFiles = problematicFiles();
        log("Running with "+problematicFiles.size()+" problematic files...");
        for (String file: problematicFiles()) {
            List<Line> lines = readLines(file);
            fixLines(lines);
            String content = StringUtils.collectionToString(lines, "\n", new LineFormatter());
            IOUtils.writeToFile(file, content);
        }
        log("There were problems in following files:\n\t"+StringUtils.collectionToString(problematicFiles, "\n\t"));
    }


    @Override
    public String toString() {
        return StringUtils.multiMapToString(warningsByFile);
    }

    private static class Key extends Pair<String,Integer> {
        Key(String file, int line) {
            super(file, line);
        }
    }

    private static class Problem {
        String file;
        int line;
        int column;
        String problemText;
        Key key;

        protected Problem(String file, String line, String column, String problemText) {
            this.file = file;
            this.line = Integer.parseInt(line);
            this.column = Integer.parseInt(column);
            this.problemText = problemText;

            this.key = new Key(file, this.line);
        }

    }

    private static class Warning extends Problem {
        private final WarningType type;

        public Warning(String file, String line, String column, String warningText) {
            super(file, line, column, warningText);

            this.type = deduceWarningType(warningText);

        }

        private boolean isFixable() {
            if (type != null && type.fixable) {
                return true;
            }
            else {
                log("Warning not fixable: "+this+", type="+type);
                return false;
            }
        }

        /** Fix a warning (assume fix is doable by replacing the warning-generating line with another */
        private String fixLine(String line) {
            if (type == WarningType.IGNORED_SYSTEM_RETURN_VALUE) {
                String tmp = line.replace("bc_unsafe_system", "system"); // unfix first, to avoid multiple "fixes"
                tmp = tmp.replace("system(", "bc_unsafe_system(");
                tmp = tmp.replace("system (", "bc_unsafe_system (");
                return tmp;
            }
            else {
                throw new RuntimeException("Unfixable warning type: "+type);
            }

        }

        @Override
        public String toString() {
            return "Warning [file=" + file + ", line=" + line + ", column="
                    + column + ", warningText=" + problemText + "]";
        }
    }

    private static class Error extends Problem {

        public Error(String file, String line, String column, String errorText) {
            super(file, line, column, errorText);
        }


        @Override
        public String toString() {
            return "Error [file=" + file + ", line=" + line + ", column="
                    + column + ", errorText=" + problemText + "]";
        }
    }


    private static String removeWarningSuffix(String original) {
        int warningBegin = original.indexOf(WARNING_SUFFIX);
        if (warningBegin != -1) {
            return original.substring(0, warningBegin);
        }
        else {
            return original;
        }
    }

    private static String removeErrorSuffix(String original) {
        int warningBegin = original.indexOf(ERROR_SUFFIX);
        if (warningBegin != -1) {
            return original.substring(0, warningBegin);
        }
        else {
            return original;
        }
    }

    /** read lines of code */
    private List<Line> readLines(String file) throws IOException {

        List<String> rawLines = IOUtils.readLines(file);
        List<Line> lines = new ArrayList<Line>(rawLines.size());
        for (int i=0; i<rawLines.size(); i++) {
            String line = rawLines.get(i);
            int lineNum = i+1;
            Warning warning = warningsByKey.get(new Key(file,lineNum));
            Error error = errorsByKey.get(new Key(file,lineNum));
            String warningText = warning != null ? warning.problemText : null;
            String errorText = error != null ? error.problemText : null;
            LineType lineType;
            if (error != null) {
                lineType = LineType.ERROR;
            }
            else if (warning != null) {
                lineType = LineType.WARNING;
            }
            else {
                lineType = LineType.HARMLESS;
            }

            lines.add(new Line(line, lineNum, lineType, warningText, errorText, warning, error, file));
        }
        return lines;
    }


    private class Line {
        String text;
        int num;
        LineType type;
        String warningText;
        String errorText;
        Warning warning;
        Error error;
        String file;


        Line(String text, int num, LineType type, String warningText, String errorText, Warning warning, Error error, String file) {
            this.text = removeWarningSuffix(text); // get of rid previous warning annotations
            this.text = removeErrorSuffix(this.text);
            this.num = num;
            this.type = type;
            this.warningText = warningText;
            this.errorText = errorText;
            this.warning = warning;
            this.error = error;
            this.file = file;
        }

        public String format() {
            if (type == LineType.HARMLESS) {
                return text;
            }
            else if (type == LineType.ERROR) {
                return text + ERROR_SUFFIX +errorText;
            }
            else if (type == LineType.WARNING) {
                return text + WARNING_SUFFIX +warningText;
            }
            else {
                throw new RuntimeException("FOO");
            }
        }

        @Override
        public String toString() {
            return "Line [text=" + text + ", num=" + num + ", type=" + type
                    + ", warningText=" + warningText + ", errorText="
                    + errorText + ", warning=" + warning + ", error=" + error
                    + ", file=" + file + "]";
        }
    }

    private enum LineType {
        HARMLESS,
        WARNING,
        ERROR;
    }


    public static void main(String[] args) throws Exception {
        CompileProblemAnnotator annotator = new CompileProblemAnnotator();
        List<Problem> warnings = readProblems(args);
        annotator.init(warnings);
        annotator.run();
    }


}
