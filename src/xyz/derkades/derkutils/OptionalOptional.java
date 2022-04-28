package xyz.derkades.derkutils;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class OptionalOptional<T> {

	private static final OptionalOptional<Object> UNKNOWN = new OptionalOptional<>(false, null);
	private static final OptionalOptional<Object> KNOWN_EMPTY = new OptionalOptional<>(true, null);

	private final boolean known;
	private final @Nullable T value;

	public OptionalOptional(final boolean known, final @Nullable T value) {
		this.known = known;
		this.value = value;

		if (!known) {
			Preconditions.checkArgument(value == null, "When known == false, value must be null.");
		}
	}

	public boolean isKnown() {
		return this.known;
	}

	public boolean isPresent() {
		if (!this.known) {
			throw new IllegalStateException("Cannot know if present, value is not known");
		}
		return this.value != null;
	}

	public @NonNull T get() {
		return Objects.requireNonNull(this.getNullable(), "Value is not present");
	}

	public @Nullable T getNullable() {
		if (!this.known) {
			throw new IllegalStateException("Value is not known");
		}
		return Objects.requireNonNull(this.value, "Value is not present");
	}

	public static <T> OptionalOptional<T> unknown() {
		return (OptionalOptional<T>) UNKNOWN;
	}

	public static <T> OptionalOptional<T> knownEmpty() {
		return (OptionalOptional<T>) KNOWN_EMPTY;
	}

	public static <T> OptionalOptional<T> knownPresent(@NonNull T value) {
		return new OptionalOptional<>(true, Objects.requireNonNull(value));
	}

}
