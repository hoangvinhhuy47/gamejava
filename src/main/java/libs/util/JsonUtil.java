package libs.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class JsonUtil {
    // private static final Logger log;

    public static Object parseJson(final String jsonString, final TypeReference<?> typeReference, ObjectMapper mapper) throws IOException {
        JsonFactory jsonFactory = new JsonFactory(); // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory
        JsonParser jp = jsonFactory.createParser(jsonString);
        return mapper.readValue(jp, typeReference);
    }

    public static Object parseJson(final String jsonString, final TypeReference<?> typeReference) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonFactory = new JsonFactory(); // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory
        JsonParser jp = jsonFactory.createParser(jsonString);
        return mapper.readValue(jp, typeReference);
    }

    public static Object parseJsonIgnoreUnknown(final String jsonString, final TypeReference<?> typeReference) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        JsonFactory jsonFactory = new JsonFactory(); // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory
        JsonParser jp = jsonFactory.createParser(jsonString);
        return mapper.readValue(jp, typeReference);
    }

    public static Map<String, Object> convertJsonToMap(String json) throws IOException {
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {};
        return  (Map<String, Object>) JsonUtil.parseJson(json, typeReference);
    }

    public static String generateJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }
}
