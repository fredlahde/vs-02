package software.kloud.vs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Serializer {
    private static final byte DIVIDER_BYTE = "|".getBytes()[0];

    public byte[] serialize(PersonDTO input) throws IOException {
        var baos = new ByteArrayOutputStream();

        try (baos) {
            baos.write(input.getName().length());
            baos.write(DIVIDER_BYTE);
            baos.write(input.getName().getBytes("UTF-8"));
            baos.write(DIVIDER_BYTE);

            var dateAsString = input.getBirthdate().toString();
            baos.write(dateAsString.length());
            baos.write(DIVIDER_BYTE);
            baos.write(dateAsString.getBytes("UTF-8"));
            baos.write(DIVIDER_BYTE);

            var buffer = ByteBuffer.allocate(32);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(input.getNumber());
            baos.write(buffer.array());
        }
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
