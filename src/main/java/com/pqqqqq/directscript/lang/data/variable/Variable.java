package com.pqqqqq.directscript.lang.data.variable;

import com.pqqqqq.directscript.lang.data.Literal;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Kevin on 2015-06-02.
 * Represents a memory section that contains a {@link Literal} and is read by a specific name
 */
public class Variable {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9]*$");

    @Nonnull private final String name;
    @Nonnull private Literal data;

    public Variable(String name) {
        this(name, Literal.empty());
    }

    public Variable(String name, Literal data) {
        this.name = name;
        setData(data);
    }

    public static Pattern namePattern() {
        return NAME_PATTERN;
    }

    public String getName() {
        return name;
    }

    public Literal getData() {
        return data;
    }

    public void setData(Literal data) {
        checkNotNull(data, "Data itself cannot be null. Use Literal#empty for null data");
        this.data = data;
    }
}
