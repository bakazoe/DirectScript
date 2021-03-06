package com.pqqqqq.directscript.lang.statement.generic.getters;

import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;

/**
 * Created by Kevin on 2015-06-15.
 * A statement that returns the boolean value of a literal
 */
public class BooleanStatement extends Statement<Boolean> {
    public static final Syntax SYNTAX = Syntax.builder()
            .identifiers("boolean", "bool")
            .arguments(Arguments.of(GenericArguments.withName("Literal")))
            .build();

    @Override
    public Result<Boolean> run(Context ctx) {
        Boolean booleanValue = ctx.getLiteral("Literal").getBoolean();
        return Result.<Boolean>builder().success().result(booleanValue).build();
    }

    @Override
    public Syntax getSyntax() {
        return SYNTAX;
    }
}
