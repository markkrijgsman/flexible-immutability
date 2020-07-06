package nl.luminis.articles.flexibility.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.Period;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_EMPTY)
public class PersonMessage extends FlexibleContentMessage {

    public enum Gender {
        MALE, FEMALE
    }

    @NonNull
    @JsonProperty(required = true)
    private final Instant dateOfBirth;

    @NonNull
    @JsonProperty(required = true)
    private final Gender gender;

    @NonNull
    @JsonProperty(required = true)
    private final String name;

    private String nationality;

    public PersonMessage(Instant dateOfBirth, Gender gender, String name) {
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.name = name;
    }

    @JsonIgnore
    public boolean isAdult() {
        return dateOfBirth.isBefore(Instant.now().minus(Period.ofYears(18)));
    }
}
