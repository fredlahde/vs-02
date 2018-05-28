package software.kloud.vs;

import java.io.Serializable;

public class Serializer {
    public <T extends Serializable> byte[] serialize(T input) {
        return new byte[]{0};
    }
}
