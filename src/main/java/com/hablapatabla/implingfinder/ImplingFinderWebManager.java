package com.hablapatabla.implingfinder;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import lombok.Value;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ImplingFinderWebManager {
    protected static final String CONTENT = "Content-Type";
    protected static final String JSON = "application/json";
    private static final MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private ImplingFinderPlugin plugin;

    @Inject
    private GsonBuilder gsonBuilder;

    private Logger logger = LoggerFactory.getLogger(ImplingFinderWebManager.class);

    private Gson getGson() {
        return gsonBuilder.registerTypeAdapter(Instant.class, new InstantSecondsConverter()).create();
    }

    protected void getData(Integer id) {
        try {
            //logger.error("GET DATA");
            Request r;
            if (id == -1) {
                r = new Request.Builder()
                        .url(plugin.getImplingGetAnyEndpoint())
                        .build();
            } else {
                String endpoint = plugin.getImplingGetIdEndpoint();
                endpoint += Integer.toString(id);
                r = new Request.Builder()
                        .url(endpoint)
                        .build();
            }

            okHttpClient.newCall(r).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("Get failed " + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            ImplingsWrapper w = getGson().fromJson(responseBody, ImplingsWrapper.class);
                            ArrayList<ImplingFinderData> ifd = new ArrayList<>(w.implings);
                            plugin.setRemotelyFetchedImplings(ifd);
                            plugin.updatePanels();
                        }
                    }
                    catch (Exception e) {
                        logger.error("GET unsuccessful" + e.toString());
                    }
                    finally {
                        response.close();
                    }
                }
            });
        }
        catch (Exception e) {
            logger.error("Outer catch block GET " + e.toString());
        }
    }

    protected void postImplings() {
        try {
            logger.error("Post Malone");
            List<Object> is = new ArrayList<>();
            is.addAll(plugin.getImplingsToUpload());

            // Oracle cloud only handles 1 JSON object to be posted at a time
            for (ImplingFinderData data : plugin.getImplingsToUpload()) {
                Request r = new Request.Builder()
                        .url(plugin.getImplingPostEndpoint())
                        .addHeader(CONTENT, JSON)
                        .post(RequestBody.create(JSONTYPE, getGson().toJson(data)))
                        .build();
                okHttpClient.newCall(r).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        logger.error("Failed to post implings");
                        logger.debug(e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!response.isSuccessful())
                                logger.error("On post response error" + response.body().string());
                        }
                        catch (Exception e) {
                            logger.error("POST responded unsuccessful  " + e.toString());
                        }
                        finally {
                            response.close();
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Outer catch block POST " + e.toString());
        }
        plugin.getImplingsToUpload().clear();
    }

    @Value
    private static class ImplingsWrapper {
        @SerializedName("items")
        List<ImplingFinderData> implings;
    }

    /**
     * Serializes/Unserializes {@link Instant} using {@link Instant#getEpochSecond()}/{@link Instant#ofEpochSecond(long)}
     */
    private static class InstantSecondsConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant>
    {
        @Override
        public JsonElement serialize(Instant src, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(src.getEpochSecond());
        }

        @Override
        public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            return Instant.ofEpochSecond(json.getAsLong());
        }
    }
}
