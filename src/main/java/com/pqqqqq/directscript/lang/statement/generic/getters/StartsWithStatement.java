package com.pqqqqq.directscript.lang.statement.generic.getters;

import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;

/**
 * Created by Kevin on 2015-06-16.
 * A statement that checks if a string starts with another string
 */
public class StartsWithStatement extends Statement<Boolean> {
    public static final Syntax SYNTAX = Syntax.builder()
            .identifiers("startswith")
            .arguments(Arguments.of(GenericArguments.withName("String"), ",", GenericArguments.withName("StartsWithString")))
            .build();

    @Override
    public Syntax getSyntax() {
        return SYNTAX;
    }

    @Override
    public Result<Boolean> run(Context ctx) {
        String string = ctx.getLiteral("String").getString();
        String contains = ctx.getLiteral("StartsWithString").getString();
        boolean check = string.startsWith(contains);

        return Result.<Boolean>builder().success().result(check).build();
    }
}
