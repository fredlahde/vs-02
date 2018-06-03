package software.kloud.vs.hrw;

import java.security.SecureRandom;
import java.util.Random;

public class RandomString {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static char[] validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456879".toCharArray();

    public String generate(int length) {
        Random random = new Random();
        char[] buffer = new char[length];
        for (int i = 0; i < length; ++i) {
            if ((i % 10) == 0) {
                random.setSeed(secureRandom.nextLong());
            }
            buffer[i] = validChars[random.nextInt(validChars.length)];
        }
        return new String(buffer);
    }
}
