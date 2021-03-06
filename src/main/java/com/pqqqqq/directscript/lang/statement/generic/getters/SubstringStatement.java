package com.pqqqqq.directscript.lang.statement.generic.getters;

import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;

/**
 * Created by Kevin on 2015-06-16.
 * A statement that takes the substring of a literal
 */
public class SubstringStatement extends Statement<String> {
    public static final Syntax SYNTAX = Syntax.builder()
            .identifiers("substring")
            .arguments(Arguments.of(GenericArguments.withName("String"), ",", GenericArguments.withName("Start")))
            .arguments(Arguments.of(GenericArguments.withName("String"), ",", GenericArguments.withName("Start"), ",", GenericArguments.withName("End")))
            .build();

    @Override
    public Syntax getSyntax() {
        return SYNTAX;
    }

    @Override
    public Result<String> run(Context ctx) {
        String string = ctx.getLiteral("String").getString();
        int start = ctx.getLiteral("Start").getNumber().intValue();
        int end = ctx.getLiteral("End", string.length()).getNumber().intValue();
        String sub = string.substring(start, end);

        return Result.<String>builder().success().result(sub).build();
    }
}
