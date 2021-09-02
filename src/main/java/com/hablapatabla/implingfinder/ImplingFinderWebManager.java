package com.hablapatabla.implingfinder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class ImplingFinderWebManager {
    protected static final String implingGetEndpoint = "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/imp/implings";
    protected static final String implingPostEndpoint = "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/imp/implings";
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

    private List<ImplingFinderData> implingsToUpload = new LinkedList<>();


    public void storeImpling(ImplingFinderData impling) {
        synchronized (this) {
            implingsToUpload.add(impling);
        }
    }

    public void storeManyImplings(ArrayList<ImplingFinderData> implings) {
        synchronized (this) {
            for (ImplingFinderData i : implings)
                implingsToUpload.add(i);
        }
    }

    private ArrayList<ImplingFinderData> parseData(JsonArray j)
    {
        ArrayList<ImplingFinderData> l = new ArrayList<>();
        JsonElement je= j.get(0);
        for (JsonElement jsonElement : j)
        {
            JsonObject jObj = jsonElement.getAsJsonObject();
            ImplingFinderData d = new ImplingFinderData(jObj.get("npcid").getAsInt(),
                    jObj.get("npcindex").getAsInt(), jObj.get("world").getAsInt(), jObj.get("xcoord").getAsInt(),
                    jObj.get("ycoord").getAsInt(), jObj.get("plane").getAsInt(), jObj.get("discoveredtime").getAsString());
            l.add(d);
        }
        return l;
    }

    private ArrayList<ImplingFinderData> parseData(JsonObject o)
    {
        logger.debug("PARSE DATA O");
        JsonArray arr = o.get("items").getAsJsonArray();
        return parseData(arr);
    }


    protected void getData() {
        try {
            Request r = new Request.Builder()
                    .url(implingGetEndpoint)
                    .build();

            okHttpClient.newCall(r).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("error GETting data");
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            logger.debug(responseBody);
                            JsonObject j = new Gson().fromJson(responseBody, JsonObject.class);
                            plugin.setRemotelyFetchedNpcs(parseData(j));
                            response.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        logger.error("unsuccessful GET");
                        try {
                            logger.error(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
            logger.debug("Post Malone");
            List<Object> is = new ArrayList<Object>();
            for (ImplingFinderData imp : implingsToUpload)
                is.add(imp);

            for (Object o : is) {
                Request r = new Request.Builder()
                        .url(implingPostEndpoint)
                        .addHeader(CONTENT, JSON)
                        .post(RequestBody.create(JSONTYPE, gson.toJson(o)))
                        .build();
                String postStr = gson.toJson(o);
                //logger.debug("POST STR: " + postStr);
                //logger.error(r.toString());
                logger.error("made it past build");
                Buffer b = new Buffer();
                r.body().writeTo(b);
                logger.error(b.readUtf8());

                okHttpClient.newCall(r).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        logger.error("failed to post implings");
                        logger.debug(e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            logger.debug("helloo");
                            logger.debug(response.body().string());
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
        this.implingsToUpload.clear();
        plugin.clearImplingsToUpload();
    }
}
