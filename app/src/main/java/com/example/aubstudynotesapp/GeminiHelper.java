package com.example.aubstudynotesapp;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiHelper {

    private static final String TAG = "GeminiHelper";
    private static final String API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    public interface GeminiCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    public static void summarize(
            String lectureText,
            String lectureTitle,
            GeminiCallback callback) {

        String apiKey = BuildConfig.GEMINI_API_KEY;

        if (apiKey == null || apiKey.isEmpty()) {
            callback.onFailure("API key not found");
            return;
        }

        Log.d(TAG, "Sending to Groq. Text length: "
                + lectureText.length());

        String prompt =
                "You are an academic assistant for university students " +
                        "at AUB (American University of Beirut).\n\n" +
                        "Summarize this lecture titled \"" + lectureTitle + "\".\n\n" +
                        "Provide:\n" +
                        "1. A 3-sentence summary\n" +
                        "2. 3-5 key points as bullet points\n" +
                        "3. One exam tip\n\n" +
                        "Lecture content:\n" + lectureText;

        try {
            // Build messages array
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(message);

            // Build request body
            JSONObject body = new JSONObject();
            body.put("model", "llama3-8b-8192");
            body.put("messages", messages);
            body.put("max_tokens", 1024);
            body.put("temperature", 0.7);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network failure: " + e.getMessage());
                    callback.onFailure("Network: " + e.getMessage());
                }

                @Override
                public void onResponse(
                        Call call,
                        Response response) throws IOException {

                    String responseBody =
                            response.body().string();

                    Log.d(TAG, "Response code: "
                            + response.code());
                    Log.d(TAG, "Response: " + responseBody);

                    if (!response.isSuccessful()) {
                        callback.onFailure(
                                response.code() + ": " + responseBody);
                        return;
                    }

                    try {
                        JSONObject json =
                                new JSONObject(responseBody);

                        String text = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        callback.onSuccess(text);

                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: "
                                + e.getMessage());
                        callback.onFailure(
                                "Parse error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Build error: " + e.getMessage());
            callback.onFailure("Error: " + e.getMessage());
        }
    }
}