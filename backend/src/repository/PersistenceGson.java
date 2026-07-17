package repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

public class PersistenceGson {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                @Override
                public void write(JsonWriter out, LocalDateTime value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                        return;
                    }
                    out.beginArray();
                    out.value(value.getYear());
                    out.value(value.getMonthValue());
                    out.value(value.getDayOfMonth());
                    out.value(value.getHour());
                    out.value(value.getMinute());
                    out.value(value.getSecond());
                    out.value(value.getNano());
                    out.endArray();
                }

                @Override
                public LocalDateTime read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }
                    in.beginArray();
                    int year = in.nextInt();
                    int month = in.nextInt();
                    int day = in.nextInt();
                    int hour = in.nextInt();
                    int minute = in.nextInt();
                    int second = in.nextInt();
                    int nano = in.nextInt();
                    in.endArray();
                    return LocalDateTime.of(year, month, day, hour, minute, second, nano);
                }
            })
            .create();

    public static Gson getGson() {
        return gson;
    }
}