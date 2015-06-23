package com.pqqqqq.directscript.lang.statement.generic.setters;

import com.google.common.base.Optional;
import com.pqqqqq.directscript.lang.data.Literal;
import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;

/**
 * Created by Kevin on 2015-06-09.
 * A statement that returns, and ceases execution, of the script
 */
public class ReturnStatement extends Statement {

    public ReturnStatement() {
        super(Syntax.builder()
                .identifiers("return", "exit")
                .arguments(Arguments.empty(), Arguments.of(Argument.from("ReturnValue")))
                .build());
    }

    @Override
    public Result run(Context ctx) {
        Literal returnValue = ctx.getLiteral("ReturnValue");
        ctx.getScriptInstance().setReturnValue(Optional.of(returnValue));
        return Result.success();
    }
}
