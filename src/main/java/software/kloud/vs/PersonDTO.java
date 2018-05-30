package software.kloud.vs;

import java.io.Serializable;
import java.util.Date;

public class PersonDTO implements Serializable {
    private String name;
    private Date birthdate;
    private int number;

    public PersonDTO() {

    }

    public PersonDTO(String name, Date birthdate, int number) {
        this.name = name;
        this.birthdate = birthdate;
        this.number = Integer.parseUnsignedInt(String.valueOf(number), 10);
    }

    public String getName() {
        return name;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "PersonDTO{" +
                "name='" + name + '\'' +
                ", birthdate=" + birthdate +
                ", number=" + number +
                '}';
    }
}
