package com.pqqqqq.directscript.lang.statement.generic.getters;

import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;

/**
 * Created by Kevin on 2015-11-14.
 */
public class ScriptStatement extends Statement<Object> {
    public static final Syntax SYNTAX = Syntax.builder()
            .identifiers("script")
            .arguments(Arguments.of(GenericArguments.withName("Getter")))
            .build();

    @Override
    public Syntax getSyntax() {
        return SYNTAX;
    }

    @Override
    public Result<Object> run(Context ctx) {
        String getter = ctx.getLiteral("Getter").getString();

        if (getter.equalsIgnoreCase("line")) {
            return Result.builder().success().result(ctx.getLine().getAbsoluteNumber()).build();
        } else if (getter.equalsIgnoreCase("scriptLine")) {
            return Result.builder().success().result(ctx.getLine().getScriptNumber()).build();
        }

        return Result.failure();
    }
}
