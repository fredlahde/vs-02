import org.junit.Test;
import software.kloud.vs.PersonDTO;
import software.kloud.vs.Serializer;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class SerializerTest {
    @Test
    public void it_can_serialize_correctly() throws IOException {
        var cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        cal.set(1986, Calendar.APRIL, 32);
        var dtoInput = new PersonDTO("Funny Man", cal.getTime(), 0xFF);

        var output = new Serializer().serialize(dtoInput);

        var dtoOutput = new Serializer().deserialize(output);
        System.out.println(dtoOutput.toString());
    }
}
