package com.pqqqqq.directscript.lang.data;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.pqqqqq.directscript.DirectScript;
import com.pqqqqq.directscript.lang.data.container.DataContainer;
import com.pqqqqq.directscript.lang.script.ScriptInstance;
import com.pqqqqq.directscript.lang.util.ICopyable;
import com.pqqqqq.directscript.lang.util.Utilities;
import org.apache.commons.lang3.StringEscapeUtils;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by Kevin on 2015-06-02.
 * A literal is an immutable value that is not dependent on any environment; a constant
 *
 * @param <T> the literal type
 */
public class Literal<T> implements DataContainer<T>, ICopyable<Literal<T>> {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");

    private final Optional<T> value;

    Literal() {
        this(null);
    }

    Literal(T value) {
        this.value = Optional.fromNullable(value);
    }

    /**
     * Creates a {@link Literal} without parsing through the {@link Sequencer} and using the plain object
     *
     * @param value the value for the literal
     * @return the new literal instance
     */
    public static Literal fromObject(Object value) {
        if (value == null) {
            return Literals.EMPTY;
        }

        if (value instanceof Literal) {
            return (Literal) value;
        }

        ObjectiveLiteral objectiveLiteral = ObjectiveLiteral.of(value); // If it's an objective literal, return it
        if (objectiveLiteral != null) {
            return objectiveLiteral;
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? Literals.TRUE : Literals.FALSE; // No need to create more immutable instances
        }

        if (value instanceof Integer || value instanceof Long || value instanceof Float) {
            return new Literal<Double>(Double.parseDouble(value.toString()));
        }

        if (value.getClass().isArray()) { // Special for arrays
            List<LiteralHolder> array = new ArrayList<LiteralHolder>();
            for (Object obj : (Object[]) value) {
                array.add(obj instanceof LiteralHolder ? (LiteralHolder) obj : new LiteralHolder(Literal.fromObject(obj)));
            }
            return new Literal<List<LiteralHolder>>(array);
        }

        if (value instanceof Collection) { // Special for collections
            List<LiteralHolder> array = new ArrayList<LiteralHolder>();
            for (Object obj : (Collection) value) {
                array.add(obj instanceof LiteralHolder ? (LiteralHolder) obj : new LiteralHolder(Literal.fromObject(obj)));
            }
            return new Literal<List<LiteralHolder>>(array);
        }

        return new Literal(value);
    }

    protected static Optional<Literal> fromSequence(String literal) { // Only Sequencer should use this
        if (literal == null || literal.isEmpty() || literal.equals("null")) { // Null or empty values return an empty parse
            return Optional.of(Literals.EMPTY);
        }

        // If there's quotes, it's a string
        if (literal.startsWith("\"") && literal.endsWith("\"")) {
            return Optional.of(Literal.fromObject(Utilities.formatColour(StringEscapeUtils.unescapeJava(literal.substring(1, literal.length() - 1)))));
        }

        // Literal booleans are only true or false
        if (literal.equals("true")) {
            return Optional.<Literal>of(Literals.TRUE);
        }

        if (literal.equals("false")) {
            return Optional.<Literal>of(Literals.FALSE);
        }

        // All numbers are doubles, just make them all doubles
        Double doubleVal = Utilities.getDouble(literal);
        if (doubleVal != null) {
            return Optional.of(Literal.fromObject(doubleVal));
        }

        return Optional.absent();
    }

    /**
     * Gets whether this literal is empty ({@link #getValue()} = {@link Optional#absent()})
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return !getValue().isPresent();
    }

    /**
     * Gets the {@link Optional} generic value for this {@link Literal}
     *
     * @return the value
     */
    public Optional<T> getValue() {
        return value;
    }

    /**
     * Gets whether this {@link Literal}'s value is an instance of a String
     *
     * @return true if a String
     */
    public boolean isString() {
        checkState(getValue().isPresent(), "This literal must be present to check this");
        return getValue().get() instanceof String;
    }

    /**
     * Gets whether this {@link Literal}'s value is an instance of a Boolean
     *
     * @return true if a Boolean
     */
    public boolean isBoolean() {
        checkState(getValue().isPresent(), "This literal must be present to check this");
        return getValue().get() instanceof Boolean;
    }

    /**
     * Gets whether this {@link Literal}'s value is an instance of a Number
     *
     * @return true if a Number
     */
    public boolean isNumber() {
        checkState(getValue().isPresent(), "This literal must be present to check this");
        return getValue().get() instanceof Double;
    }

    /**
     * Gets whether this {@link Literal}'s value is an instance of an Array
     *
     * @return true if an Array
     */
    public boolean isArray() {
        checkState(getValue().isPresent(), "This literal must be present to check this");
        return getValue().get() instanceof List;
    }

    /**
     * Gets the String value of this {@link Literal}
     *
     * @return the String value
     */
    public String getString() {
        return isString() ? (String) getValue().get() : parseString().getString();
    }

    /**
     * Gets the Boolean value of this {@link Literal}
     *
     * @return the Boolean value
     */
    public Boolean getBoolean() {
        return isBoolean() ? (Boolean) getValue().get() : parseBoolean().getBoolean();
    }

    /**
     * Gets the number (Double) value of this {@link Literal}
     *
     * @return the number value
     */
    public Double getNumber() {
        return isNumber() ? (Double) getValue().get() : parseNumber().getNumber();
    }

    /**
     * Gets the array ({@link LiteralHolder} {@link List}) value of this {@link Literal}
     *
     * @return the array value
     */
    public List<LiteralHolder> getArray() {
        return isArray() ? (List<LiteralHolder>) getValue().get() : parseArray().getArray();
    }

    /**
     * Gets the specific {@link LiteralHolder} array data at the index
     *
     * @param index the index for the array (base 1)
     * @return the variable at this index
     */
    public LiteralHolder getArrayValue(int index) {
        checkState(isArray(), "This literal is not an array");

        List<LiteralHolder> literalHolders = getArray();
        Utilities.buildToIndex(literalHolders, --index, new LiteralHolder()); // Base 1
        return literalHolders.get(index);
    }

    // Sponge casting

    /**
     * Gets the {@link Literal} as a specific given type
     * @param type the type
     * @param <T> the generic type parameter
     * @return the optional value of the type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAs(Class<T> type) {
        if (type.equals(Player.class)) {
            try {
                Optional<Player> playerOptional = DirectScript.instance().getGame().getServer().getPlayer(getString()); // Check name first
                if (playerOptional.isPresent()) {
                    return (Optional<T>) playerOptional;
                }

                return (Optional<T>) DirectScript.instance().getGame().getServer().getPlayer(UUID.fromString(getString())); // Check uuid now
            } catch (IllegalArgumentException e) {
                return Optional.absent();
            }
        } else if (type.equals(World.class)) {
            return (Optional<T>) DirectScript.instance().getGame().getServer().getWorld(getString());
        } else if (type.equals(Vector3d.class)) {
            List<LiteralHolder> array = getArray();
            return (Optional<T>) Optional.of(new Vector3d(array.get(0).getData().getNumber(), array.get(1).getData().getNumber(), array.get(2).getData().getNumber()));
        } else if (type.equals(Location.class)) {
            List<LiteralHolder> array = getArray();

            World world = DirectScript.instance().getGame().getServer().getWorld(array.get(0).getData().getString()).get();
            Vector3d vec = new Vector3d(array.get(1).getData().getNumber(), array.get(2).getData().getNumber(), array.get(3).getData().getNumber());

            return (Optional<T>) Optional.of(new Location(world, vec));
        } else if (type.equals(ItemStack.class)) {
            List<LiteralHolder> array = getArray();
            ItemType itemType = Utilities.getType(ItemType.class, array.get(0).getData().getString()).get();
            int quantity = array.size() >= 2 ? array.get(1).getData().getNumber().intValue() : 1;

            return (Optional<T>) Optional.of(DirectScript.instance().getGame().getRegistry().getItemBuilder().itemType(itemType).quantity(quantity).build());
        }
        return Optional.absent();
    }

    // Arithmetic

    /**
     * Adds a {@link Literal} to this one, either by string (concatenation) or numerically
     *
     * @param other the other literal
     * @return the sum literal
     */
    public Literal add(Literal other) {
        if (isNumber() && other.isNumber()) {
            return Literal.fromObject(getNumber() + other.getNumber());
        }

        return Literal.fromObject(getString() + other.getString()); // Everything can be a string
    }

    /**
     * Subtracts a {@link Literal} from this one numerically
     *
     * @param other the other literal
     * @return the difference literal
     */
    public Literal sub(Literal other) {
        return Literal.fromObject(getNumber() - other.getNumber());
    }

    /**
     * Multiplies a {@link Literal} by this one numerically
     *
     * @param other the other literal
     * @return the product literal
     */
    public Literal mult(Literal other) {
        return Literal.fromObject(getNumber() * other.getNumber());
    }

    /**
     * Divides a {@link Literal} by this one numerically
     *
     * @param other the other literal
     * @return the quotient literal
     */
    public Literal div(Literal other) {
        return Literal.fromObject(getNumber() / other.getNumber());
    }

    /**
     * Raises a {@link Literal} to the power of this one numerically
     *
     * @param other the other literal
     * @return the resultant literal
     */
    public Literal pow(Literal other) {
        return Literal.fromObject(Math.pow(getNumber(), other.getNumber()));
    }

    /**
     * Takes the nth root as per a {@link Literal} of this one numerically
     *
     * @param other the other literal
     * @return the resultant literal
     */
    public Literal root(Literal other) {
        return Literal.fromObject(Math.pow(getNumber(), (1D / other.getNumber())));
    }

    /**
     * Negates this {@link Literal} by switching the boolean value (true -> false, false -> true)
     *
     * @return the negative boolean literal
     */
    public Literal<Boolean> negative() {
        checkState(isBoolean(), "Negation can only be done to booleans (" + getString() + ")");
        return Literal.fromObject(!getBoolean());
    }

    /**
     * Gets a {@link Literal} with the specified new value if this literal is {@link Literals#EMPTY}, or otherwise this literal
     *
     * @param newvalue the new value
     * @return this literal if not empty, or a literal with newvalue
     */
    public Literal or(Object newvalue) {
        if (isEmpty()) {
            return Literal.fromObject(newvalue);
        }
        return this;
    }

    @Override
    public Literal<T> resolve(ScriptInstance scriptInstance) { // Override for DataContainer
        return this;
    }

    @Override
    public Literal<T> copy() {
        if (!isEmpty() && isArray()) {
            List<LiteralHolder> newarray = new ArrayList<LiteralHolder>();
            for (LiteralHolder literalHolder : getArray()) {
                newarray.add(literalHolder.copy());
            }

            return Literal.fromObject(newarray);
        }

        return this;
    }

    // Object overrides

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Literal && getValue().equals(((Literal) obj).getValue());
    }

    // Parsing stuff for conversions
    private Literal<String> parseString() {
        if (!getValue().isPresent()) {
            return Literals.EMPTY;
        }

        // Make integers not have the .0
        if (isNumber()) {
            return Literal.fromObject(decimalFormat.format(getNumber()));
        }

        // Format arrays
        if (isArray()) {
            String string = "{";
            List<LiteralHolder> array = getArray();

            for (LiteralHolder literalHolder : array) {
                string += literalHolder.getData().getString() + ", ";
            }

            return Literal.fromObject((array.isEmpty() ? string : string.substring(0, string.length() - 2)) + "}");
        }

        return Literal.fromObject(getValue().get().toString());
    }

    private Literal<Boolean> parseBoolean() {
        checkState(isString(), "The value must be a string to use this method");
        return Literal.fromObject(Boolean.parseBoolean(getString()));
    }

    private Literal<Double> parseNumber() {
        checkState(isString(), "The value must be a string to use this method");
        return Literal.fromObject(Double.parseDouble(getString()));
    }

    private Literal<List<LiteralHolder>> parseArray() {

        checkState(getValue().isPresent(), "This parse must be present to do this");
        return Literal.fromObject(new Literal[]{this}); // Create a singleton of the data
    }

    /**
     * Created by Kevin on 2015-06-18.
     * A class of common {@link Literal}s
     */
    public static class Literals {
        /**
         * An empty {@link Literal}, where the value is absent
         */
        public static final Literal EMPTY = new Literal();

        /**
         * A true {@link Literal}, where the value is true (or 1)
         */
        public static final Literal<Boolean> TRUE = new Literal(true);

        /**
         * A false {@link Literal}, where the value is false (or 0)
         */
        public static final Literal<Boolean> FALSE = new Literal(false);

        /**
         * A {@link Literal} where whose value is a number equal to 0
         */
        public static final Literal<Double> ZERO = new Literal(0D);

        /**
         * A {@link Literal} where whose value is a number equal to 1
         */
        public static final Literal<Double> ONE = new Literal(1D);
    }
}
