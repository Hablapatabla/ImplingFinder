package com.hablapatabla.implingfinder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
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
    private Gson gson;

    @Inject
    private ImplingFinderPlugin plugin;

    private Logger logger = LoggerFactory.getLogger(ImplingFinderWebManager.class);

    private ArrayList<ImplingFinderData> parseData(JsonArray j) {
        ArrayList<ImplingFinderData> l = new ArrayList<>();
        if (j.size() == 0) {
            return l;
        }

        JsonElement je = j.get(0);
        for (JsonElement jsonElement : j) {
            JsonObject jObj = jsonElement.getAsJsonObject();
            ImplingFinderData d = new ImplingFinderData(jObj.get("npcid").getAsInt(),
                    jObj.get("npcindex").getAsInt(), jObj.get("world").getAsInt(), jObj.get("xcoord").getAsInt(),
                    jObj.get("ycoord").getAsInt(), jObj.get("plane").getAsInt(), jObj.get("discoveredtime").getAsString());
            l.add(d);
        }
        return l;
    }

    private ArrayList<ImplingFinderData> parseData(JsonObject o) {
        JsonArray arr = o.get("items").getAsJsonArray();
        return parseData(arr);
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
                    logger.error("Get failed");
                    logger.error(e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JsonObject j = new Gson().fromJson(responseBody, JsonObject.class);
                            plugin.setRemotelyFetchedImplings(parseData(j));
                            response.close();
                            plugin.updatePanels();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        logger.error("GET responsed unsuccessful");
                    }
                }
            });
        }
        catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void postImplings() {
        try {
            logger.error("Post Malone");
            List<Object> is = new ArrayList<>();
            is.addAll(plugin.getImplingsToUpload());

            // Oracle cloud only handles 1 JSON object to be posted at a time
            for (Object o : is) {
                Request r = new Request.Builder()
                        .url(plugin.getImplingPostEndpoint())
                        .addHeader(CONTENT, JSON)
                        .post(RequestBody.create(JSONTYPE, gson.toJson(o)))
                        .build();

                okHttpClient.newCall(r).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        logger.error("Failed to post implings");
                        logger.debug(e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            logger.error(response.body().string());
                            response.close();
                        } else {
                            logger.error("On response error" + response.body().string());
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        plugin.getImplingsToUpload().clear();
    }
}
