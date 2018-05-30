package software.kloud.vs;

import java.beans.Transient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Serializer {

    private static final byte DIVIDER_BYTE = 0; //DIVIDER_STRING.getBytes()[0];

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
                        baos.write(f.getName().getBytes());
                        baos.write(DIVIDER_BYTE);

                        // var buffer = ByteBuffer.allocate(32);
                        // buffer.order(ByteOrder.BIG_ENDIAN);
                        // buffer.putInt((Integer) value);
                        // baos.write(buffer.array());
                        baos.write(((Integer) value).byteValue());
                        baos.write(DIVIDER_BYTE);
                    } else if (value.getClass().isAssignableFrom(String.class)) {
                        baos.write(f.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        baos.write(value.toString().getBytes());
                        baos.write(DIVIDER_BYTE);
                    } else if (value.getClass().isAssignableFrom(Date.class)) {
                        Long millis = ((Date) value).getTime();
                        baos.write(f.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
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
        var properties = new HashMap<String, byte[]>();

        String current = null;
        var from = 0;
        var to = indexOf(input, 0, DIVIDER_BYTE);
        while (to != -1) {
            byte[] buffer = new byte[to - from];
            System.arraycopy(input, from, buffer, 0, to - from);
            if (className == null) {
                className = new String(buffer);
            } else if (current == null) {
                current = new String(buffer);
            } else {
                properties.put(current, buffer);
                current = null;
            }
            from = to + 1;
            to = indexOf(input, from, DIVIDER_BYTE);
        }

        for (var k : properties.keySet()) {
            System.out.print(k + " -> ");
            for (var b : properties.get(k)) {
                System.out.print(b + " ");
            }
            System.out.println();
        }

        try {
            Object obj = Class.forName(className).getConstructor().newInstance();

            return obj;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
//
//
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
}
