package com.pqqqqq.directscript.lang.reader;

import com.pqqqqq.directscript.DirectScript;
import com.pqqqqq.directscript.lang.container.Script;
import com.pqqqqq.directscript.lang.container.ScriptInstance;
import com.pqqqqq.directscript.lang.container.ScriptsFile;
import com.pqqqqq.directscript.lang.statement.Result;
import com.pqqqqq.directscript.lang.statement.Statement;
import com.pqqqqq.directscript.lang.statement.internal.setters.ScriptDeclaration;
import com.pqqqqq.directscript.lang.statement.internal.setters.Termination;
import com.pqqqqq.directscript.lang.trigger.cause.Cause;
import com.pqqqqq.directscript.lang.trigger.cause.Causes;
import com.pqqqqq.directscript.lang.util.StringParser;
import com.pqqqqq.directscript.lang.util.Utilities;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by Kevin on 2015-06-02.
 * A reader for retrieving scripts from any directory
 */
public class Reader {
    private static Reader INSTANCE = new Reader();

    private Reader() {
    }

    public static Reader instance() {
        return INSTANCE;
    }

    public Set<ScriptsFile> load() {
        for (Cause cause : Causes.getRegistry()) {
            cause.getTriggers().clear(); // Clear all the triggers
        }

        return readInDir();
    }

    public Set<ScriptsFile> readInDir() { // Reads from ROOT/scripts
        File dir = new File("scripts/");
        dir.mkdir(); // Make /scripts if it doesn't exist

        return readInDir(dir);
    }

    public Set<ScriptsFile> readInDir(File dir) {
        return readInDir(dir, dir);
    }

    public Set<ScriptsFile> readInDir(File root, File dir) {
        checkNotNull(dir, "Directory cannot be null");
        checkState(dir.exists(), "The directory must exist");
        checkState(dir.isDirectory(), "Directory must actually be a directory");

        Set<ScriptsFile> scriptsFiles = new HashSet<ScriptsFile>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scriptsFiles.addAll(readInDir(root, file)); // I <3 recursion
            } else {
                if (file.getName().endsWith(".ds") || file.getName().endsWith(".dsc")) {
                    scriptsFiles.add(readScriptsFile(root, file));
                }
            }
        }

        return scriptsFiles;
    }

    public ScriptsFile readScriptsFile(File root, File file) {
        checkNotNull(file, "File cannot be null");
        checkState(file.exists(), "The file must exist");
        checkState(!file.isDirectory(), "The file cannot be a directory");
        checkState(file.getName().endsWith(".ds") || file.getName().endsWith(".dsc"), "File must have .ds or .dsc extension");

        ScriptsFile scriptsFile = new ScriptsFile(root, file);

        // Read and separate scripts
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int absoluteLine = 0, scriptLine = 0;
            boolean blockComment = false;
            Script currentScript = null;
            String line;

            List<Line> bracesLineList = new ArrayList<Line>();

            while ((line = bufferedReader.readLine()) != null) {
                try {
                    absoluteLine++; // Up the line #s

                    Pair<Boolean, String> comments = StringParser.instance().removeComments(blockComment, line);
                    blockComment = comments.getLeft();
                    line = comments.getRight();

                    String trim = Utilities.fullLineTrim(line);
                    if (trim.isEmpty()) {
                        continue;
                    }

                    Line lineInst = new Line(absoluteLine, scriptLine, line);
                    if (currentScript != null) {
                        scriptLine++;
                        if (lineInst.getStatement() instanceof Termination) { // Else is an instance of Termination
                            currentScript.getLinkedLines().put(bracesLineList.remove(0), lineInst);
                        }

                        if (lineInst.getStatement().getSuffix().equals("{") || lineInst.getTrimmedLine().endsWith("{")) { // Necessary for else and else if statements
                            bracesLineList.add(0, lineInst);
                        }
                    }

                    Statement statementOptional = lineInst.getStatement();
                    if (statementOptional instanceof ScriptDeclaration) { // Check if this is a script declaration
                        checkState(currentScript == null, "Please end a script declaration with an end brace (})");

                        Result<String> result = lineInst.toContex(ScriptInstance.compile(null)).run();
                        checkState(result.isSuccess() && result.getResult().isPresent(), String.format("File %s has an improper formatted script declaration", file.getName()));

                        currentScript = new Script(scriptsFile, result.getResult().get());
                        bracesLineList.add(0, lineInst); // Add braces here since it wasn't added above
                        continue;
                    } else if (statementOptional instanceof Termination) {
                        checkNotNull(currentScript, "No script is being declared line " + lineInst.getLine() + " " + lineInst.getAbsoluteNumber());

                        Line starting = currentScript.lookupStartingLine(lineInst);
                        if (starting.getStatement() instanceof ScriptDeclaration) {
                            scriptsFile.getScripts().add(currentScript);
                            ScriptInstance.compile(currentScript).run();

                            // Reset cache for the script
                            currentScript = null;
                            bracesLineList.clear();
                            scriptLine = 0;
                            continue;
                        }
                    }

                    if (currentScript != null) { // Otherwise, if there's an active script, add the line to the script line list
                        currentScript.getLines().add(lineInst);
                    }
                } catch (Throwable e1) {
                    DirectScript.instance().getErrorHandler().log(String.format("Error in compilation of %s at line %d", scriptsFile.getStringRepresentationNoExt() + (currentScript != null ? " -> " + currentScript.getName() : ""), absoluteLine));
                    DirectScript.instance().getErrorHandler().log(e1);
                    DirectScript.instance().getErrorHandler().flush();
                }
            }

            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scriptsFile;
    }
}
