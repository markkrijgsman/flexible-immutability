package nl.luminis.articles.flexibility.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;

public class ObjectMapperFactory {

    public static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .findAndRegisterModules();

    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }
}
