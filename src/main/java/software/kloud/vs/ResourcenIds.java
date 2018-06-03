package software.kloud.vs;

import java.nio.ByteBuffer;

public class ResourcenIds {
    public static void main(String[] args) {
        String[] ids = {
                "intro.pdf",
                "index.html",
                "d41d8cd98f00b204e9800998ecf8427e",
                "0cc175b9c0f1b6a831c399e269772661",
                "900150983cd24fb0d6963f7d28e17f72",
                "f96b697d7cb7938d525a2f31aaf161d0",
                "c3fcd3d76192e4007dfb496cca67e13b",
                "c0008dfc-b5c6-4ac8-9b96-c1780084109b",
                "fde20f54-6d1f-45a8-baf0-82d20b6a0c62",
                "10af8b96-bf4a-4567-9cf4-56646cfefb23",
                "cb4c2299-be2b-4673-ae76-0e98d1539833",
                "c01c429b-5a55-4930-be48-8b0e2ae4db5d"
        };
        String[] server = {
                "s1.example.com",
                "s2.example.com",
                "s3.example.com"
        };

        for (var id : ids) {
            int lid = ByteBuffer.wrap(id.getBytes()).getInt();
            int sid = lid % server.length;
            System.out.println(id + " -> " + server[sid]);
        }
    }
}
