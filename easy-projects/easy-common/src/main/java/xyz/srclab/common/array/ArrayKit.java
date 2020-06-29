package xyz.srclab.common.array;

import kotlin.collections.ArraysKt;
import org.apache.commons.lang3.ArrayUtils;
import xyz.srclab.annotation.Nullable;
import xyz.srclab.annotation.OutReturn;
import xyz.srclab.common.base.Cast;
import xyz.srclab.common.base.Check;
import xyz.srclab.common.base.Equals;
import xyz.srclab.common.base.Hash;
import xyz.srclab.common.cache.Cache;
import xyz.srclab.common.collection.IterableKit;
import xyz.srclab.common.lang.finder.Finder;
import xyz.srclab.common.reflect.TypeKit;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Util for array.
 */
public class ArrayKit {

    public static final Object[] EMPTY_OBJECT_ARRAY = ArrayUtils.EMPTY_OBJECT_ARRAY;

    public static final Class<?>[] EMPTY_CLASS_ARRAY = ArrayUtils.EMPTY_CLASS_ARRAY;

    public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    public static final String[] EMPTY_STRING_ARRAY = ArrayUtils.EMPTY_STRING_ARRAY;

    public static <A> A newArray(Class<?> componentType, int length) {
        Object array = Array.newInstance(componentType, length);
        return Cast.as(array);
    }

    public static <T> List<T> asList(Object array) {
        if (array instanceof Object[]) {
            return Cast.as(Arrays.asList((Object[]) array));
        }
        if (array instanceof boolean[]) {
            return Cast.as(asList((boolean[]) array));
        }
        if (array instanceof byte[]) {
            return Cast.as(asList((byte[]) array));
        }
        if (array instanceof short[]) {
            return Cast.as(asList((short[]) array));
        }
        if (array instanceof char[]) {
            return Cast.as(asList((char[]) array));
        }
        if (array instanceof int[]) {
            return Cast.as(asList((int[]) array));
        }
        if (array instanceof long[]) {
            return Cast.as(asList((long[]) array));
        }
        if (array instanceof float[]) {
            return Cast.as(asList((float[]) array));
        }
        if (array instanceof double[]) {
            return Cast.as(asList((double[]) array));
        }
        throw new IllegalArgumentException("Unknown array object: " + array);
    }

    public static List<Boolean> asList(boolean[] array) {
        return ArraysKt.asList(array);
    }

    public static List<Byte> asList(byte[] array) {
        return ArraysKt.asList(array);
    }

    public static List<Short> asList(short[] array) {
        return ArraysKt.asList(array);
    }

    public static List<Character> asList(char[] array) {
        return ArraysKt.asList(array);
    }

    public static List<Integer> asList(int[] array) {
        return ArraysKt.asList(array);
    }

    public static List<Long> asList(long[] array) {
        return ArraysKt.asList(array);
    }

    public static List<Float> asList(float[] array) {
        return ArraysKt.asList(array);
    }

    public static List<Double> asList(double[] array) {
        return ArraysKt.asList(array);
    }

    public static <N, O> N[] map(O[] old, Class<?> newComponentType, Function<? super O, ? extends N> mapper) {
        N[] newArray = newArray(newComponentType, old.length);
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = mapper.apply(old[i]);
        }
        return newArray;
    }

    public static <E> E[] toArray(Iterable<? extends E> iterable, Class<?> componentType) {
        return IterableKit.toArray(iterable, componentType);
    }

    public static <T> int find(T[] array, Predicate<? super T> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int find(boolean[] array, Predicate<? super Boolean> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int find(byte[] array, Predicate<? super Byte> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int find(short[] array, Predicate<? super Short> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int find(char[] array, Predicate<? super Character> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int find(int[] array, Predicate<? super Integer> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int find(long[] array, Predicate<? super Long> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int find(float[] array, Predicate<? super Float> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int find(double[] array, Predicate<? super Double> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static <T> T[] buildArray(@OutReturn T[] array, ObjectSupplier<T> supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static boolean[] buildArray(@OutReturn boolean[] array, BooleanSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static byte[] buildArray(@OutReturn byte[] array, ByteSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static short[] buildArray(@OutReturn short[] array, ShortSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static char[] buildArray(@OutReturn char[] array, CharSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static int[] buildArray(@OutReturn int[] array, IntSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static long[] buildArray(@OutReturn long[] array, LongSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static float[] buildArray(@OutReturn float[] array, FloatSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static double[] buildArray(@OutReturn double[] array, DoubleSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static <T> T[] buildArray(@OutReturn T[] array, int from, int to, ObjectSupplier<T> supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static boolean[] buildArray(@OutReturn boolean[] array, int from, int to, BooleanSupplier supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static byte[] buildArray(@OutReturn byte[] array, int from, int to, ByteSupplier supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static short[] buildArray(@OutReturn short[] array, int from, int to, ShortSupplier supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static char[] buildArray(@OutReturn char[] array, int from, int to, CharSupplier supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static int[] buildArray(@OutReturn int[] array, int from, int to, IntSupplier supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static long[] buildArray(@OutReturn long[] array, int from, int to, LongSupplier supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static float[] buildArray(@OutReturn float[] array, int from, int to, FloatSupplier supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static double[] buildArray(@OutReturn double[] array, int from, int to, DoubleSupplier supplier) {
        Check.checkIndexRange(from, to, array.length);
        for (int i = from; i < to; i++) {
            array[i] = supplier.get(i);
        }
        return array;
    }

    public static Type getComponentType(Type type) {
        if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        }
        Class<?> rawType = TypeKit.getRawType(type);
        if (!rawType.isArray()) {
            throw new IllegalArgumentException("Type is not array: " + type);
        }
        return rawType.getComponentType();
    }

    public static Type getArrayType(Type componentType) {
        return ArrayTypeFinder.find(componentType);
    }

    public interface ObjectSupplier<E> {
        @Nullable
        E get(int index);
    }

    public interface BooleanSupplier {
        boolean get(int index);
    }

    public interface ByteSupplier {
        byte get(int index);
    }

    public interface ShortSupplier {
        short get(int index);
    }

    public interface CharSupplier {
        char get(int index);
    }

    public interface IntSupplier {
        int get(int index);
    }

    public interface LongSupplier {
        long get(int index);
    }

    public interface FloatSupplier {
        float get(int index);
    }

    public interface DoubleSupplier {
        double get(int index);
    }

    private static final class ArrayTypeFinder {

        // Key: component type, value: array type
        private static final Cache<Type, Type> cache = Cache.newL2();

        private static final Finder<Type, Type> finder = Finder.newMapFinder(ArrayTypeTable.TABLE);

        public static Type find(Type componentType) {
            @Nullable Type arrayType = finder.find(componentType);
            if (arrayType != null) {
                return arrayType;
            }
            return cache.getNonNull(componentType, ArrayTypeFinder::make);
        }

        private static Type make(Type componentType) {

            if (componentType instanceof Class) {
                return newArray((Class<?>) componentType, 0).getClass();
            }

            class GenericArrayTypeImpl implements GenericArrayType {

                public Type getGenericComponentType() {
                    return componentType;
                }

                public String toString() {
                    Type componentType = this.getGenericComponentType();
                    StringBuilder buffer = new StringBuilder();
                    if (componentType instanceof Class) {
                        buffer.append(((Class<?>) componentType).getName());
                    } else {
                        buffer.append(componentType.toString());
                    }

                    buffer.append("[]");
                    return buffer.toString();
                }

                public boolean equals(Object any) {
                    if (any instanceof GenericArrayType) {
                        GenericArrayType that = (GenericArrayType) any;
                        return Equals.equals(componentType, that.getGenericComponentType());
                    } else {
                        return false;
                    }
                }

                public int hashCode() {
                    return Hash.hashCode(componentType);
                }
            }

            return new GenericArrayTypeImpl();
        }

        private static final class ArrayTypeTable {

            private static final Type[] TABLE = {
                    Object.class, Object[].class,
                    String.class, String[].class,
                    boolean.class, boolean[].class,
                    byte.class, byte[].class,
                    short.class, short[].class,
                    char.class, char[].class,
                    int.class, int[].class,
                    long.class, long[].class,
                    float.class, float[].class,
                    double.class, double[].class,
                    Boolean.class, Boolean[].class,
                    Byte.class, Byte[].class,
                    Short.class, Short[].class,
                    Character.class, Character[].class,
                    Integer.class, Integer[].class,
                    Long.class, Long[].class,
                    Float.class, Float[].class,
                    Double.class, Double[].class,
            };
        }
    }
}