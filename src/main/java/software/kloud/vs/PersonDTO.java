package software.kloud.vs;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

public class PersonDTO implements Serializable {

    private transient String transientField;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "PersonDTO{" +
                "name='" + name + '\'' +
                ", birthdate=" + birthdate +
                ", number=" + number +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonDTO personDTO = (PersonDTO) o;
        return number == personDTO.number &&
                Objects.equals(name, personDTO.name) &&
                Objects.equals(birthdate, personDTO.birthdate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, birthdate, number);
    }
}
