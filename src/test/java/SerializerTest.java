import org.junit.Assert;
import org.junit.Test;
import software.kloud.vs.PersonDTO;
import software.kloud.vs.Serializer;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("Duplicates")
public class SerializerTest {
    @Test
    public void it_can_serialize_correctly() throws IOException {
        var cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        cal.set(1986, Calendar.APRIL, 32);
        var dtoInput = new PersonDTO("Funny Man", cal.getTime(), 0xFF);

        var output = new Serializer().serialize(dtoInput);

        var dtoOutput = new Serializer().deserialize(output, PersonDTO.class);
        System.out.println(dtoOutput.toString());
        Assert.assertEquals(0xFF, dtoOutput.getNumber());
        Assert.assertEquals("Funny Man", dtoOutput.getName());
        Assert.assertEquals(cal.getTime(), dtoOutput.getBirthdate());

    }

    @Test
    public void it_can_serialize_correctly_with_large_ints() throws IOException {
        var cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        cal.set(1986, Calendar.APRIL, 32);
        var dtoInput = new PersonDTO("Funny Man", cal.getTime(), 0xFFFF);

        var output = new Serializer().serialize(dtoInput);

        var dtoOutput = new Serializer().deserialize(output, PersonDTO.class);
        Assert.assertEquals(0xFFFF, dtoOutput.getNumber());
    }
}
