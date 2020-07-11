package nl.luminis.articles.flexibility.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.time.Instant;
import java.util.Map;
import nl.luminis.articles.flexibility.model.PersonMessage.Gender;
import nl.luminis.articles.flexibility.util.ObjectMapperFactory;
import org.junit.Test;

public class PersonMessageTest {

    private static final Instant NOW = Instant.now();

    @Test
    public void testSerialize() throws JsonProcessingException {
        PersonMessage person = PersonMessage.builder()
            .dateOfBirth(NOW)
            .gender(Gender.FEMALE)
            .name("Jane Doe")
            .nationality("Dutch")
            .otherFields(Map.of("foo", "bar"))
            .build();

        String json = ObjectMapperFactory.getInstance().writeValueAsString(person);

        assertThat(json)
            .isEqualTo("{\"dateOfBirth\":\"" + NOW + "\",\"gender\":\"FEMALE\",\"name\":\"Jane Doe\",\"nationality\":\"Dutch\",\"foo\":\"bar\"}");
    }

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String json = "{\"dateOfBirth\":\"" + NOW + "\",\"gender\":\"FEMALE\",\"name\":\"Jane Doe\",\"nationality\":\"Dutch\",\"foo\":\"bar\"}";

        PersonMessage person = ObjectMapperFactory.getInstance().readValue(json, PersonMessage.class);

        assertThat(person.getDateOfBirth()).isEqualTo(NOW);
        assertThat(person.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(person.getName()).isEqualTo("Jane Doe");
        assertThat(person.getNationality()).isEqualTo("Dutch");
        assertThat(person.getOtherFields()).isEqualTo(Map.of("foo", "bar"));
    }

    @Test
    public void testDeserializeFailsOnMissingRequiredProperty() {
        String json = "{\"gender\":\"FEMALE\",\"name\":\"Jane Doe\",\"nationality\":\"Dutch\",\"foo\":\"bar\"}";

        assertThatThrownBy(() -> ObjectMapperFactory.getInstance().readValue(json, PersonMessage.class))
            .isInstanceOf(MismatchedInputException.class)
            .hasMessageContaining("Missing required creator property 'dateOfBirth'");
    }

    @Test
    public void testValidationFailsOnMissingRequiredProperty() {
        assertThatThrownBy(() -> PersonMessage.builder()
            .gender(Gender.MALE)
            .name("John Doe")
            .build()
        ).isInstanceOf(NullPointerException.class)
            .hasMessage("dateOfBirth is marked non-null but is null");
    }

    @Test
    public void testValidationSucceedsOnMissingOptionalProperty() {
        assertThatCode(() -> PersonMessage.builder()
            .dateOfBirth(Instant.now())
            .gender(Gender.MALE)
            .name("John Doe")
            .build()
        ).doesNotThrowAnyException();
    }

    @Test
    public void testToMap() {
        PersonMessage person = PersonMessage.builder()
            .dateOfBirth(NOW)
            .gender(Gender.FEMALE)
            .name("Jane Doe")
            .nationality("Dutch")
            .otherFields(Map.of("foo", "bar"))
            .build();

        assertThat(person.toMap()).isEqualTo(Map.of(
            "dateOfBirth", NOW.toString(),
            "gender", Gender.FEMALE.toString(),
            "name", "Jane Doe",
            "nationality", "Dutch",
            "foo", "bar"
        ));
    }

    @Test
    public void testCopyImmutableObject() {
        PersonMessage person = PersonMessage.builder()
            .dateOfBirth(NOW)
            .gender(Gender.MALE)
            .name("John Doe")
            .nationality("Dutch")
            .otherFields(Map.of("foo", "bar"))
            .build();

        PersonMessage newPerson = person.toBuilder().name("Richard Doe").build();

        assertThat(newPerson).isNotEqualTo(person);
        assertThat(newPerson.getDateOfBirth()).isEqualTo(NOW);
        assertThat(newPerson.getGender()).isEqualTo(Gender.MALE);
        assertThat(newPerson.getName()).isEqualTo("Richard Doe");
        assertThat(newPerson.getNationality()).isEqualTo("Dutch");
        assertThat(newPerson.getOtherFields()).isEqualTo(Map.of("foo", "bar"));
    }
}
