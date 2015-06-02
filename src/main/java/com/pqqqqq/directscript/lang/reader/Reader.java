package com.pqqqqq.directscript.lang.reader;

import com.pqqqqq.directscript.lang.container.Script;
import com.pqqqqq.directscript.lang.container.ScriptsFile;
import com.pqqqqq.directscript.lang.statement.StatementResult;
import com.pqqqqq.directscript.lang.statement.Statements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
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

    public Set<ScriptsFile> readInDir() { // Reads from ROOT/scripts
        File dir = new File("scripts/");
        dir.mkdir(); // Make /scripts if it doesn't exist

        return readInDir(dir);
    }

    public Set<ScriptsFile> readInDir(File dir) {
        checkNotNull(dir, "Directory cannot be null");
        checkState(dir.exists(), "The directory must exist");
        checkState(dir.isDirectory(), "Directory must actually be a directory");

        Set<ScriptsFile> scriptsFiles = new HashSet<ScriptsFile>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scriptsFiles.addAll(readInDir(file)); // I <3 recursion
            } else {
                if (file.getName().endsWith(".ds") || file.getName().endsWith(".dsc")) {
                    scriptsFiles.add(readScriptsFile(file));
                }
            }
        }

        return scriptsFiles;
    }

    public ScriptsFile readScriptsFile(File file) {
        checkNotNull(file, "File cannot be null");
        checkState(file.exists(), "The file must exist");
        checkState(!file.isDirectory(), "The file cannot be a directory");
        checkState(file.getName().endsWith(".ds") || file.getName().endsWith(".dsc"), "File must have .ds or .dsc extension");

        ScriptsFile scriptsFile = new ScriptsFile(file);

        // Read and separate scripts
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int absoluteLine = 0;
            Script currentScript = null;
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                absoluteLine++;
                Line lineInst = new Line(absoluteLine, (currentScript != null ? currentScript.getLines().size() + 1 : -1), line);

                if (lineInst.getLine().isEmpty() || lineInst.getLine().startsWith("//")) {
                    continue;
                }
                // TODO: Continue empty lines?

                if (Statements.isApplicableToLine(Statements.SCRIPT_DECLARATION, lineInst)) { // Check if this is a script declaration
                    checkState(currentScript == null, "Please end a script declaration with #endscript");

                    StatementResult<String> result = Statements.SCRIPT_DECLARATION.run(lineInst);
                    checkState(result.isSuccess() && result.getResult().isPresent(), String.format("File %s has an improper formatted script declaration", file.getName()));

                    currentScript = new Script(result.getResult().get());
                } else if (Statements.isApplicableToLine(Statements.SCRIPT_TERMINATION, lineInst)) {
                    checkNotNull(currentScript, "No script is being declared");

                    scriptsFile.getScripts().add(currentScript);
                    currentScript = null;
                    // TODO: Interpret here?
                } else if (currentScript != null) {
                    currentScript.getLines().add(lineInst);
                }
            }

            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scriptsFile;
    }
}
