package com.pqqqqq.directscript.lang.statement.generic.getters;

import com.flowpowered.math.GenericMath;
import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;

/**
 * Created by Kevin on 2015-06-10.
 * A statement that rounds a number (0.5 up)
 */
public class RoundStatement extends Statement<Double> {
    public static final Syntax SYNTAX = Syntax.builder()
            .identifiers("round")
            .arguments(Arguments.of(GenericArguments.withName("Number")))
            .build();

    @Override
    public Syntax getSyntax() {
        return SYNTAX;
    }

    @Override
    public Result<Double> run(Context ctx) {
        double rounded = GenericMath.round(ctx.getLiteral("Number").getNumber(), 0);
        return Result.<Double>builder().success().result(rounded).build();
    }
}
