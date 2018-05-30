package software.kloud.vs;

import java.beans.Transient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class Serializer {

    private static final String DIVIDER_STRING = "\0";
    private static final byte DIVIDER_BYTE = 0; //DIVIDER_STRING.getBytes()[0];

    public byte[] serialize(PersonDTO input) throws IOException {
        var baos = new ByteArrayOutputStream();
        for (var f : input.getClass().getDeclaredFields()) {
            if (!f.isAnnotationPresent(Transient.class)) {
                try {
                    var getterName = "get" + f.getName().toUpperCase().charAt(0) + f.getName().substring(1);
                    var getter = input.getClass().getMethod(getterName);
                    var value = getter.invoke(input);
                    if (value.getClass().isAssignableFrom(Integer.class)) {
                        var buffer = ByteBuffer.allocate(32);
                        buffer.order(ByteOrder.BIG_ENDIAN);
                        buffer.putInt((Integer) value);
                        baos.write(buffer.array());
                    } else if (value.getClass().isAssignableFrom(String.class)) {
                        baos.write(f.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        baos.write(value.toString().getBytes());
                    } else if (value.getClass().isAssignableFrom(Date.class)) {
                        Long millis = ((Date) value).getTime();
                        baos.write(f.getName().getBytes());
                        baos.write(DIVIDER_BYTE);
                        baos.write(millis.byteValue());
                    } else {
                        continue;
                    }
                    baos.write(DIVIDER_BYTE);

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

    public PersonDTO deserialize(byte[] input) {
        var index = 0;

        while (input[index] != DIVIDER_BYTE) {
            index++;
        }

        byte[] sizeOfStringArr = new byte[index];
        System.arraycopy(input, 0, sizeOfStringArr, 0, index);
        var sizeOfString = ByteBuffer.wrap(sizeOfStringArr).getInt();

        byte[] stringArr = new byte[sizeOfString + 1];
        System.arraycopy(input, index + 1, stringArr, 0, sizeOfString);
        var name = new String(stringArr);

        return null;

    }
}
