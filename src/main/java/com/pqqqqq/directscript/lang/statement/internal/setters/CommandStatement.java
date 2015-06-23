package com.pqqqqq.directscript.lang.statement.internal.setters;

import com.pqqqqq.directscript.lang.data.LiteralHolder;
import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 2015-06-16.
 * Represents a command specification statement
 */
public class CommandStatement extends Statement {

    public CommandStatement() {
        super(Syntax.builder()
                .identifiers("command")
                .executionTime(ExecutionTime.COMPILE)
                .arguments(Arguments.of(Argument.from("CommandAliases")))
                .build());
    }

    @Override
    public Result run(Context ctx) {
        List<String> aliases = new ArrayList<String>();
        List<LiteralHolder> array = ctx.getLiteral("CommandAliases").getArray();

        for (LiteralHolder alias : array) {
            aliases.add(alias.getData().getString());
        }

        ctx.getScript().getCauseData().setCommandAliases(aliases.toArray(new String[aliases.size()]));
        return Result.success();
    }
}
