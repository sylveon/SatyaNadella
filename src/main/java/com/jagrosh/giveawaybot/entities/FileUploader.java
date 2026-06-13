/*
 * Copyright 2022 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.giveawaybot.entities;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jagrosh.interactions.requests.Route;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class FileUploader
{
    private final OkHttpClient client = new OkHttpClient.Builder().build();
    private final String authorization;
    private final long channelId;

    public FileUploader(String authorization, long channelId)
    {
        this.authorization = authorization;
        this.channelId = channelId;
    }
    
    public String uploadFile(String contents, String filename)
    {
        try
        {
            long msgId = sendFile(contents, filename).get().getLong("id");
            return String.format("%d", msgId);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    private CompletableFuture<JSONObject> sendFile(String contents, String filename)
    {
        return CompletableFuture.supplyAsync(() -> 
        {
            try
            {
                RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("files[0]", filename, RequestBody.create(contents.getBytes()))
                    .build();
                Response res = client.newCall(new Request.Builder()
                    .url(Route.POST_MESSAGE.format(channelId).getURL())
                    .post(body)
                    .header("Authorization", "Bot " + authorization)
                    .header("User-Agent", "DiscordBot (DiscordInteractions, 0.1)").build()).execute();
                return bodyToJson(res.body());
            }
            catch(IOException ex)
            {
                return new JSONObject();
            }
        });
    }

    private static JSONObject bodyToJson(ResponseBody body) throws IOException
    {
        if(body == null)
            return new JSONObject();
        String str = body.string();
        if(str.isEmpty())
            return new JSONObject();
        return str.startsWith("[") ? new JSONObject().put("_", new JSONArray(str)) : new JSONObject(str);
    }
}
