package nl.luminis.articles.flexibility.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import nl.luminis.articles.flexibility.util.ObjectMapperFactory;

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
