# Flexible immutability

When modelling my data classes, I often find myself combining [Lombok][1] and [Jackson][2]. 
However, these frameworks may bite when combining annotations. For example: 
* Some validations go on the constructor, some don't.
* Some annotations require a no-args constructor, but this breaks your immutability.
* Some annotations do not combine well with super classes

The following setup demonstrates the usage of both of these frameworks for a data model that is immutable, but remains flexible at runtime. 
My requirements for this data model are:

**1\. Must be immutable**

**2\. Must be able to distinguish between required and optional fields, regardless of whether we instantiate the object from code or from JSON**

**3\. Creation of the object must fail with an exception when required fields are missing**

Furthermore, I want to allow for fields to be added at runtime, because sometimes I don't know exactly what fields may be part of my data classes, outside of the ones I've explicitly modelled. 
This is often the case when working with Elasticsearch, which allows for either a strict or dynamic mapping of fields. 
Therefore I'm going to add one additional requirement:

**4\. Data model allows for flexible addition of properties at runtime**

This last requirement sounds like it's conflicting with the earlier requirement that the data model must be immutable, but I'll show how to achieve both in the next sections.

## Immutability

We will first implement an immutable data model with field validation. 
This validation will work when creating the object, but also when (de-)serializing it later on.
We start with the following example data class:

```
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_EMPTY)
public class PersonMessage extends FlexibleContentMessage {

    public enum Gender {
        MALE, FEMALE
    }

    @NonNull
    private final Instant dateOfBirth;

    @NonNull
    private final Gender gender;

    @NonNull
    private final String name;

    private List<String> children;

    @JsonCreator
    public PersonMessage(
        @JsonProperty(required = true) Instant dateOfBirth,
        @JsonProperty(required = true) Gender gender,
        @JsonProperty(required = true) String name) {
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.name = name;
    }

    @JsonIgnore
    public boolean isAdult() {
        return dateOfBirth.isBefore(Instant.now().minus(Period.ofYears(18)));
    }
}
```

Take note of the following things:
- I'm using `@NonNull` on fields to mark properties required for Lombok. These cannot go on the constructor.
- I'm using `@JsonProperty` on constructor parameters to mark properties required for Jackson. These cannot go on the fields.
- I'm using `@JsonCreator` on the constructor to indicate that this particular constructor needs to be used for deserialization.
- `@JsonIgnore` is required in order to avoid serializing the `isAdult()` method.
- I'm using `@Getter` only and do not need `@Setter`, `@NoArgsConstructor` or `@AllArgsConstructor`, which would take away from an immutable data model.
- I'm using `@SuperBuilder` to let Lombok generate a builder which will be the only way of instantiating my data class.
- I can use `myInstance.toBuilder()` if I want to create a copy of my immutable object because of the usage of `toBuilder = true` on my `@SuperBuilder` annotation.
- I'm using `@JsonInclude` to exclude empty fields when serializing (e.g. null fields, empty lists or strings and such).

This setup allows us to construct instances of our class with Lombok's builder pattern with validation that automatically fires for required fields:

```
PersonMessage.builder()
    .dateOfBirth(Instant.now())
    .gender(Gender.MALE)
    .name("John Doe")
    .build();
```

Also see [this unittest][3] for usage examples.

## Flexibility

This satisfies requirements 1 through 3. For requirement 4, I'm creating a super class for the above class to extend:

```
@Getter
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_EMPTY)
public abstract class FlexibleContentMessage {

    private final Map<String, Object> otherFields;

    public FlexibleContentMessage() {
        otherFields = new HashMap<>();
    }

    @JsonAnySetter
    private void setOtherFields(String key, Object value) {
        otherFields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getOtherFields() {
        return otherFields;
    }

    @JsonIgnore
    public Map<String, Object> toMap() {
        return ObjectMapperFactory.getInstance().convertValue(this, ObjectMapperFactory.MAP);
    }
}
```

Note the usage of `@JsonAnyGetter` and `@JsonAnySetter`. Furthermore, note that the `@JsonAnySetter` is private. 
This allows Jackson to set all unmapped fields when deserializing but doesn't expose the setter to any users of our data model.
I've also added a `toMap()` method for ease of use. When doing this, make sure that you [reuse your ObjectMapper][4].

## Dependencies

The Maven dependencies involved for this setup are as follows:
```
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>2.11.1</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.11.1</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.11.1</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.module</groupId>
            <artifactId>jackson-module-paranamer</artifactId>
            <version>2.11.1</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
        </dependency>
```

The first two dependencies pull in the Jackson annotations and ObjectMapper functionality, respectively. 
We use `jackson-datatype-jsr310` for proper serialization of the Instant class and use `jackson-module-paranamer` to help Jackson deserialize without us having to define an empty constructor (and thus taking away from our data model's immutability).

The implementation of all these examples and code snippets can be found on my Github repository [here][5]. 

[1]: https://projectlombok.org
[2]: https://github.com/FasterXML/jackson
[3]: https://github.com/markkrijgsman/flexible-immutability/blob/master/src/test/java/nl/luminis/articles/flexibility/model/PersonMessageTest.java
[4]: https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/ObjectMapper.html
[5]: https://github.com/markkrijgsman/flexible-immutability
