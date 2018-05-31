package software.kloud.vs;

import java.beans.Transient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Serializer {

    public static final Map<Class<?>, Class<?>> primitiveTypesMap;

    private static final byte DIVIDER_BYTE = 0; //DIVIDER_STRING.getBytes()[0];

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
        baos.write(input.getClass().getName().getBytes());
        baos.write(DIVIDER_BYTE);
        for (var f : input.getClass().getDeclaredFields()) {
            if (!f.isAnnotationPresent(Transient.class)) {
                try {
                    var getterName = "get" + f.getName().toUpperCase().charAt(0) + f.getName().substring(1);
                    var getter = input.getClass().getMethod(getterName);
                    var value = getter.invoke(input);
                    if (value.getClass().isAssignableFrom(Integer.class)) {
                        // field name
                        baos.write(f.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        // field type
                        baos.write(Integer.class.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        // field value
                        baos.write(((Integer) value).byteValue());
                        var buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                        buffer.order(ByteOrder.BIG_ENDIAN);
                        buffer.putInt((Integer) value);
                        baos.write(DIVIDER_BYTE);
                    } else if (value.getClass().isAssignableFrom(String.class)) {
                        // field name
                        baos.write(f.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        // field type
                        baos.write(String.class.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        // field value
                        baos.write(value.toString().getBytes());
                        baos.write(DIVIDER_BYTE);
                    } else if (value.getClass().isAssignableFrom(Date.class)) {
                        // field name
                        baos.write(f.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        // field type
                        baos.write(Date.class.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        // field value
                        Long millis = ((Date) value).getTime();
                        baos.write(millis.byteValue());
                        baos.write(DIVIDER_BYTE);
                    }
                } catch (NoSuchMethodException e) {
                    System.out.println("NoSuchMethodException: No getter found for field " + f.getName());
                } catch (IllegalAccessException e) {
                    System.out.println("IllegalAccessException: Cannot retrieve value for field " + f.getName() + ". Getter not public?");
                } catch (InvocationTargetException e) {
                    System.out.println("InvocationTargetException: Cannot retrieve value for field " + f.getName() + ".");
                }
            }
        }
        for (byte b : baos.toByteArray()) {
            System.out.print(b + " ");
        }
        System.out.println();
        return baos.toByteArray();
    }

    public Object deserialize(byte[] input) {
        String className = null;
        var values = new HashMap<String, byte[]>();
        var types = new HashMap<String, Class<?>>();

        String currentField = null;
        String currentType = null;
        var from = 0;
        var to = indexOf(input, 0, DIVIDER_BYTE);
        while (to != -1) {
            byte[] buffer = new byte[to - from];
            System.arraycopy(input, from, buffer, 0, to - from);
            if (className == null) {
                className = new String(buffer);
            } else if (currentField == null) {
                currentField = new String(buffer);
            } else if (currentType == null) {
                currentType = new String(buffer);
            } else {
                try {
                    types.put(currentField, Class.forName(currentType));
                    values.put(currentField, buffer);
                } catch (ClassNotFoundException e) {
                    System.out.println("Invalid type: " + currentType);
                }
                currentField = null;
                currentType = null;
            }
            from = to + 1;
            to = indexOf(input, from, DIVIDER_BYTE);
        }

        for (var k : values.keySet()) {
            System.out.print(k + " (" + types.get(k) + ") -> ");
            for (var b : values.get(k)) {
                System.out.print(b + " ");
            }
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
                    // TODO date from byte array
                }
            }
            return obj;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

//        var index = 0;
//
//        while (input[index] != DIVIDER_BYTE) {
//            index++;
//        }
//
//        byte[] sizeOfStringArr = new byte[index];
//        System.arraycopy(input, 0, sizeOfStringArr, 0, index);
//        var sizeOfString = ByteBuffer.wrap(sizeOfStringArr).getInt();
//
//        byte[] stringArr = new byte[sizeOfString + 1];
//        System.arraycopy(input, index + 1, stringArr, 0, sizeOfString);
//        var name = new String(stringArr);
//
//        return null;
    }

    private int indexOf(byte[] array, int fromIndex, byte searchfor) {
        for (int i = fromIndex; i < array.length; i++) {
            if (array[i] == searchfor) {
                return i;
            }
        }
        return -1;
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
        biggerBuffer.put(buffer.array());

        biggerBuffer.rewind();
        return biggerBuffer.getInt();
    }
}