package com.hablapatabla.implingfinder;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.hablapatabla.implingfinder.model.ImplingFinderData;
import com.hablapatabla.implingfinder.model.ImplingFinderEnum;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;


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


    protected void postImplings() {
        try {
            // Oracle cloud only handles 1 JSON object to be posted at a time
            for (ImplingFinderData data : plugin.getImplingsToUpload()) {
                Request r = new Request.Builder()
                        .url(ImplingFinderPlugin.implingPostEndpoint)
                        .addHeader(CONTENT, JSON)
                        .post(RequestBody.create(JSONTYPE, getGson().toJson(data)))
                        .build();

                okHttpClient.newCall(r).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        logger.error("Failed to post implings", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!response.isSuccessful())
                                logger.error("On post response error ");
                        }
                        catch (Exception e) {
                            logger.error("POST responded unsuccessful ", e);
                        }
                        finally {
                            response.close();
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Outer catch block POST ", e);
        }
        plugin.getImplingsToUpload().clear();
    }

    protected List<ImplingFinderData> getData(List<Integer> ids) {
        List<Future<ImplingsWrapper>> futures = new ArrayList<>();
        List<ImplingFinderData> implings = new ArrayList<>();

        for (Integer id : ids) {
            String endpoint;
            if (id != ImplingFinderPlugin.RECENT_IMPLINGS_ID)
                endpoint = ImplingFinderPlugin.implingGetIdEndpoint + Integer.toString(id);
            else
                endpoint = ImplingFinderPlugin.implingGetAnyEndpoint;

            Future<ImplingsWrapper> f = fetchAndDeserializeSpecificImpling(endpoint, getGson(), new TypeToken<ImplingsWrapper>() {});
            futures.add(f);
        }

        try {
            for (Future<ImplingsWrapper> f : futures) {
                List<ImplingFinderData> l = f.get().implings;
                implings.addAll(l);
            }
        }
        catch (Exception e) {
            logger.error("Error opening futures", e);
        }
        finally {
            Collections.sort(implings, Collections.reverseOrder());
            if (implings.size() > 25)
                implings = implings.subList(0, 25);
            return implings;
        }
    }


    /**
     * Calls getSpecificImplingResponseAsync in order to get a CompletableFuture containing the
     * full response body from the api at url. Uses a TypeToken to deserialize body, see
     * ImplingsWrapper for only use case. Oracle api response is a full JSON array, with
     * impling data of interest in a JSON object called items.
     */
    private <T> Future<T> fetchAndDeserializeSpecificImpling(String url, Gson gson, TypeToken<T> typeToken) {
        CompletableFuture<String> future = getSpecificImplingResponseAsync(url);
        return future.thenApply(new Function<String, T>() {
            public T apply (String body) {
                return gson.fromJson(body, typeToken.getType());
            }
        });
    }


    private CompletableFuture<String> getSpecificImplingResponseAsync(String url) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Request r = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        future.complete(response.body().string());
                    }
                    else {
                        throw new IOException("Http error");
                    }
                }
                catch (Exception e) {
                    future.completeExceptionally(e);
                }
                finally {
                    response.close();
                }
            }
        });
        return future;
    }

    @Value
    private static class ImplingsWrapper {
        @SerializedName("items")
        List<ImplingFinderData> implings;
    }

    private Gson getGson() {
        return gsonBuilder.registerTypeAdapter(Instant.class, new InstantSecondsConverter()).create();
    }

    /**
     * Serializes/Deserializes {@link Instant} using {@link Instant#getEpochSecond()}/{@link Instant#ofEpochSecond(long)}
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
