package com.pqqqqq.directscript.lang.statement.generic.getters;

import com.pqqqqq.directscript.lang.reader.Context;
import com.pqqqqq.directscript.lang.statement.Statement;

/**
 * Created by Kevin on 2015-06-16.
 * A statement that gets the character by its ASCII ordinal
 */
public class CharacterStatement extends Statement<String> {

    @Override
    public String[] getIdentifiers() {
        return new String[]{"character", "chr"};
    }

    @Override
    public Argument[] getArguments() {
        return new Argument[]{
                Argument.builder().name("Ordinal").build()
        };
    }

    @Override
    public Result<String> run(Context ctx) {
        int ordinal = ctx.getLiteral(0).getNumber().intValue();
        char character = (char) ordinal;
        String result = Character.toString(character);

        return Result.<String>builder().success().result(result).literal(result).build();
    }
}
