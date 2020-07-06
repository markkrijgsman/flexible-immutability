package nl.luminis.articles.flexibility.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.util.Map;
import nl.luminis.articles.flexibility.model.PersonMessage.Gender;
import nl.luminis.articles.flexibility.util.ObjectMapperFactory;
import org.junit.Test;

public class PersonMessageTest {

    private static final Instant NOW = Instant.now();

    @Test
    public void testSerialize() throws JsonProcessingException {
        PersonMessage personMessage = PersonMessage.builder()
            .dateOfBirth(NOW)
            .gender(Gender.FEMALE)
            .name("Jane Doe")
            .nationality("Dutch")
            .otherFields(Map.of("foo", "bar"))
            .build();

        String json = ObjectMapperFactory.getInstance().writeValueAsString(personMessage);

        assertThat(json)
            .isEqualTo("{\"dateOfBirth\":\"" + NOW + "\",\"gender\":\"FEMALE\",\"name\":\"Jane Doe\",\"nationality\":\"Dutch\",\"foo\":\"bar\"}");
    }

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String json = "{\"dateOfBirth\":\"" + NOW + "\",\"gender\":\"FEMALE\",\"name\":\"Jane Doe\",\"nationality\":\"Dutch\",\"foo\":\"bar\"}";

        PersonMessage personMessage = ObjectMapperFactory.getInstance().readValue(json, PersonMessage.class);

        assertThat(personMessage.getDateOfBirth()).isEqualTo(NOW);
        assertThat(personMessage.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(personMessage.getName()).isEqualTo("Jane Doe");
        assertThat(personMessage.getNationality()).isEqualTo("Dutch");
        assertThat(personMessage.getOtherFields()).isEqualTo(Map.of("foo", "bar"));
    }

    @Test
    public void testToMap() {
        PersonMessage personMessage = PersonMessage.builder()
            .dateOfBirth(NOW)
            .gender(Gender.FEMALE)
            .name("Jane Doe")
            .nationality("Dutch")
            .otherFields(Map.of("foo", "bar"))
            .build();

        assertThat(personMessage.toMap()).isEqualTo(Map.of(
            "dateOfBirth", NOW.toString(),
            "gender", Gender.FEMALE.toString(),
            "name", "Jane Doe",
            "nationality", "Dutch",
            "foo", "bar"
        ));
    }

    @Test
    public void testFailsOnMissingRequiredProperty() {
        assertThatThrownBy(() -> PersonMessage.builder()
            .gender(Gender.MALE)
            .name("John Doe")
            .build()
        ).isInstanceOf(NullPointerException.class)
            .hasMessage("dateOfBirth is marked non-null but is null");
    }

    @Test
    public void testSucceedsOnMissingOptionalProperty() {
        assertThatCode(() -> PersonMessage.builder()
            .dateOfBirth(Instant.now())
            .gender(Gender.MALE)
            .name("John Doe")
            .build()
        ).doesNotThrowAnyException();
    }
}
