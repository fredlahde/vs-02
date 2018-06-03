package software.kloud.vs;

import java.beans.Transient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Checksum;

public class Serializer {

    public static final Map<Class<?>, Class<?>> primitiveTypesMap;

    static {
        primitiveTypesMap = new HashMap<>();
        primitiveTypesMap.put(Integer.class, int.class);
        primitiveTypesMap.put(Long.class, long.class);
        primitiveTypesMap.put(Float.class, float.class);
        primitiveTypesMap.put(Double.class, double.class);
        primitiveTypesMap.put(Byte.class, byte.class);
        primitiveTypesMap.put(Boolean.class, boolean.class);
        primitiveTypesMap.put(Short.class, short.class);
    }

    public byte[] serialize(Object input) throws IOException {
        var baos = new ByteArrayOutputStream();
        System.out.print("Class -> ");
        baos.write(serializeProperty(input.getClass().getName().getBytes()));
        System.out.println();
        for (var f : input.getClass().getDeclaredFields()) {
            if (!Modifier.isTransient(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())) {
                try {
                    var getterName = "get" + f.getName().toUpperCase().charAt(0) + f.getName().substring(1);
                    var getter = input.getClass().getMethod(getterName);
                    var value = getter.invoke(input);
                    if (value.getClass().isAssignableFrom(Integer.class)) {
                        System.out.print(f.getName() + " -> ");
                        baos.write(serializeProperty(f.getName().getBytes()));
                        System.out.print(f.getType().getName() + " -> ");
                        baos.write(serializeProperty(Integer.class.getName().getBytes()));

                        var buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                        buffer.order(ByteOrder.BIG_ENDIAN);
                        buffer.putInt((Integer) value);
                        System.out.print(value.toString() + " -> ");
                        baos.write(serializeProperty(buffer.array()));

                        baos.write(serializeProperty(serializeChecksum(value)));
                        System.out.println();

                    } else if (value.getClass().isAssignableFrom(String.class)) {
                        System.out.print(f.getName() + " -> ");
                        baos.write(serializeProperty(f.getName().getBytes()));
                        System.out.print(f.getType().getName() + " -> ");
                        baos.write(serializeProperty(f.getType().getName().getBytes()));
                        System.out.print(value.toString() + " -> ");
                        baos.write(serializeProperty(value.toString().getBytes()));

                        baos.write(serializeProperty(serializeChecksum(value)));
                        System.out.println();

                    } else if (value.getClass().isAssignableFrom(Date.class)) {
                        System.out.print(f.getName() + " -> ");
                        baos.write(serializeProperty(f.getName().getBytes()));
                        System.out.print(f.getType().getName() + " -> ");
                        baos.write(serializeProperty(f.getType().getName().getBytes()));

                        Long millis = ((Date) value).getTime();
                        var buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
                        buffer.order(ByteOrder.BIG_ENDIAN);
                        buffer.putLong(millis);
                        System.out.print(value.toString() + " -> ");
                        baos.write(serializeProperty(buffer.array()));

                        baos.write(serializeProperty(serializeChecksum(value)));
                        System.out.println();
                    } else {
                        System.out.println("Unsupported type: " + value.getClass().getName());
                    }
                } catch (NoSuchMethodException e) {
                    System.out.println("NoSuchMethodException: No getter found for field " + f.getName());
                } catch (IllegalAccessException e) {
                    System.out.println("IllegalAccessException: Cannot retrieve value for field " + f.getName() + ". Getter not public?");
                } catch (InvocationTargetException e) {
                    System.out.println("InvocationTargetException: Cannot retrieve value for field " + f.getName() + ".");
                }
            } else {
                System.out.println(f.getName() + " is transient or static and will be skipped in serialization.");
            }
        }
        return baos.toByteArray();
    }

    private byte[] serializeChecksum(Object value) {
        var buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value.hashCode());
        return buffer.array();
    }

    private byte[] serializeProperty(byte[] value) throws IOException {
        var baos = new ByteArrayOutputStream();

        // byte count of this value
        var buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value.length);
        baos.write(buffer.array());
        baos.write(value);

        printByteArray(buffer.array());
        System.out.print(" - ");
        printByteArray(value);
        System.out.println();

        return baos.toByteArray();
    }

    public Object deserialize(byte[] input) {
        String className = null;
        var values = new HashMap<String, byte[]>();
        var types = new HashMap<String, Class<?>>();
        var checksums = new HashMap<String, Integer>();

        String currentField = null;
        String currentType = null;

        var from = 0;
        while (from < input.length) {
            // get value length
            byte[] temp = new byte[4];
            System.arraycopy(input, from, temp, 0, 4);
            var bb = ByteBuffer.wrap(temp);
            bb.order(ByteOrder.BIG_ENDIAN);
            final int len = getIntFromBuffer(bb);

            // get value
            from += 4;
            temp = new byte[len];
            System.arraycopy(input, from, temp, 0, len);

            if (className == null) {
                className = new String(temp);
            } else if (currentField == null) {
                currentField = new String(temp);
            } else if (currentType == null) {
                currentType = new String(temp);
            } else {
                from += len;

                byte[] checksum = new byte[4];
                System.arraycopy(input, from, checksum, 0, 4);
                var bb2 = ByteBuffer.wrap(input, from, 4);
                bb2.order(ByteOrder.BIG_ENDIAN);
                final int len = getIntFromBuffer(bb);
                from += 4;

                try {
                    types.put(currentField, Class.forName(currentType));
                    values.put(currentField, temp);
                } catch (ClassNotFoundException e) {
                    System.out.println("Unsupported type: " + currentType);
                }
                currentField = null;
                currentType = null;
            }
        }

        for (var k : values.keySet()) {
            System.out.print(k + " (" + types.get(k) + ") -> ");
            printByteArray(values.get(k));
            System.out.println();
        }

        try {
            Class<?> clazz = Class.forName(className);
            Object obj = clazz.getConstructor().newInstance();
            for (var k : values.keySet()) {
                var setterName = "set" + k.toUpperCase().charAt(0) + k.substring(1);
                var setter = getMethodPrimitiveSafe(clazz, setterName, types.get(k));
                if (types.get(k).equals(Integer.class)) {
                    var buffer = ByteBuffer.wrap(values.get(k));
                    buffer.order(ByteOrder.BIG_ENDIAN);
                    final int intFromBuffer = getIntFromBuffer(buffer);
                    setter.invoke(obj, intFromBuffer);
                } else if (types.get(k).equals(String.class)) {
                    setter.invoke(obj, new String(values.get(k)));
                } else if (types.get(k).equals(Date.class)) {
                    var buffer = ByteBuffer.wrap(values.get(k));
                    buffer.order(ByteOrder.BIG_ENDIAN);
                    final long longFromBuffer = getLongFromBuffer(buffer);
                    setter.invoke(obj, new Date(longFromBuffer));
                }
            }
            return obj;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Method getMethodPrimitiveSafe(Class<?> clazz, final String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ignored) {
        }

        return clazz.getMethod(methodName, Arrays.stream(parameterTypes)
                .map(type -> {
                    if (primitiveTypesMap.containsKey(type)) {
                        return primitiveTypesMap.get(type);
                    }
                    return type;
                }).toArray(Class<?>[]::new));
    }

    private int getIntFromBuffer(ByteBuffer buffer) {
        try {
            return buffer.getInt();
        } catch (BufferUnderflowException ignored) {
        }

        var biggerBuffer = ByteBuffer.allocate(4);
        biggerBuffer.put(3, buffer.array()[0]);

        biggerBuffer.rewind();
        return biggerBuffer.getInt();
    }

    private long getLongFromBuffer(ByteBuffer buffer) {
        try {
            return buffer.getLong();
        } catch (BufferUnderflowException ignored) {
        }

        var biggerBuffer = ByteBuffer.allocate(8);
        biggerBuffer.put(7, buffer.array()[0]);

        biggerBuffer.rewind();
        return biggerBuffer.getLong();
    }

    private void printByteArray(byte[] array) {
        for (var b : array) {
            System.out.print(b + " ");
        }
    }
}