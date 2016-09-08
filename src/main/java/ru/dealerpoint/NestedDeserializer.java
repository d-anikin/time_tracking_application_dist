package ru.dealerpoint;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by dmitry on 06.09.16.
 */
public class NestedDeserializer<T> implements JsonDeserializer<T> {
    private String contentKey;

    public NestedDeserializer(String contentKey) {
        this.contentKey = contentKey;
    }

    public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException
    {
        // Get the "content" element from the parsed JSON
        JsonElement content = je.getAsJsonObject().get(this.contentKey);

        // Deserialize it. You use a new instance of Gson to avoid infinite recursion
        // to this deserializer
        return new Gson().fromJson(content, type);

    }
}
