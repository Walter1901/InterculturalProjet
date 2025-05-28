package Finance.data;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Custom Gson adapter for serializing and deserializing LocalDate objects.
 * Enables proper JSON conversion of Java 8 LocalDate instances.
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    // Formatter for ISO-8601 date format (yyyy-MM-dd)
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Serializes a LocalDate object to JSON format.
     * @param date The LocalDate to serialize
     * @param typeOfSrc The type of the source object
     * @param context The JSON serialization context
     * @return JSON element representing the date in ISO-8601 format
     */
    @Override
    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        // Convert LocalDate to String using ISO format
        return new JsonPrimitive(date.format(formatter));
    }

    /**
     * Deserializes a JSON element back to a LocalDate object.
     * @param json The JSON element containing the date string
     * @param typeOfT The target type (LocalDate)
     * @param context The JSON deserialization context
     * @return LocalDate object parsed from the JSON string
     * @throws JsonParseException if the date string is invalid
     */
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // Parse ISO-8601 formatted string back to LocalDate
        return LocalDate.parse(json.getAsString(), formatter);
    }
}