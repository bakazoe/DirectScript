package com.pqqqqq.directscript.lang.data.variable;

import com.google.common.base.Objects;
import com.pqqqqq.directscript.lang.data.Literal;
import com.pqqqqq.directscript.lang.util.ICopyable;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by Kevin on 2015-06-02.
 * Represents a memory section that contains a {@link Literal} and is read by a specific name
 */
public class Variable implements ICopyable<Variable> {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]([A-Za-z0-9]|\\.)*$");

    private final String name;
    private final boolean isFinal;
    private Literal data;

    /**
     * Creates a new variable with the corresponding name that has a value of {@link Literal#empty()} and is not final
     *
     * @param name the name
     */
    public Variable(String name) {
        this(name, Literal.empty());
    }

    /**
     * Creates a new variable with the corresponding name and {@link Literal} data, and is not final
     *
     * @param name the name
     * @param data the data
     */
    public Variable(String name, Literal data) {
        this(name, data, false);
    }

    /**
     * Creates a new variable with the corresponding name, {@link Literal} data and finality
     *
     * @param name    the name
     * @param data    the data
     * @param isFinal whether this variable is final
     */
    public Variable(String name, Literal data, boolean isFinal) {
        this.name = name;
        forceSetData(data);
        this.isFinal = isFinal;
    }

    /**
     * Creates new empty variable (with null name). This is analogous to <code>new Variable(null)</code>
     *
     * @return an empty variable
     */
    public static Variable empty() {
        return new Variable(null);
    }

    /**
     * Gets the name matching {@link Pattern} that all {@link Variable}s names must match to be created
     *
     * @return the name matching pattern
     */
    public static Pattern namePattern() {
        return NAME_PATTERN;
    }

    /**
     * Gets the name of this variable
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the {@link Literal} data for this variable
     * @return the data
     */
    public Literal getData() {
        return data;
    }

    /**
     * Sets the {@link Literal} data for this variaboe
     * @param data the data
     * @see #isFinal()
     */
    public void setData(Literal data) {
        checkState(!isFinal, "You cannot change the value of a finalized vaiable");
        forceSetData(data);
    }

    private void forceSetData(Literal data) {
        checkNotNull(data, "Data itself cannot be null. Use Literal#empty for null data");
        this.data = data;
    }

    /**
     * Gets whether this variable's {@link Literal} data is final, or cannot be changed
     * @return true if constant
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Copies this variable into a new instance
     * @return the copied variable
     */
    public Variable copy() {
        return new Variable(name, data, isFinal);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("data", data)
                .add("isFinal", isFinal).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, data, isFinal);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Variable) {
            return hashCode() == obj.hashCode();
        }
        return false;
    }
}
