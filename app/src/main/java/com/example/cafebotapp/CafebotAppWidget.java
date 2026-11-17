package com.example.cafebotapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.Calendar;
import java.util.Date;

public class CafebotAppWidget extends AppWidgetProvider {

    // Reuse a single OkHttpClient instance across widget updates
    private static final OkHttpClient client = new OkHttpClient();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, BroadcastReceiver.PendingResult pendingResult) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.cafebot_app_widget);
        Log.d("JSON", "Cteasdasd.");
        views.setImageViewResource(R.id.imageView, R.drawable.square);
        String credential = Credentials.basic("cafebotbot", "8AF278A7-2C23-405D-A20D-8F215738C0DA");
        Request request = new Request.Builder()
                .url("https://ftc-api.firstinspires.org/v2.0/2026/leagues/rankings/USMOKS/STLS/")
                .header("Authorization", credential)
                .build();

        // Run network request asynchronously on a background thread
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (pendingResult != null) {
                    pendingResult.finish();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (response) {
                    Log.d("JSON", String.valueOf(response.code()));
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray head = json.getJSONArray("rankings");

                        JSONObject cafebot = null;

                        for (int i = 0; i < head.length(); i++) {
                            if (head.getJSONObject(i).getInt("teamNumber") == 4982) {
                                cafebot = head.getJSONObject(i);
                                break;
                            }
                        }


                        // Guard against null if team 4982 is not in the rankings yet
                        if (cafebot != null) {
                            views.setTextViewText(R.id.teamNumber, "#" + cafebot.getString("displayTeamNumber"));
                            views.setTextViewText(R.id.teamName, cafebot.getString("teamName"));
                            views.setTextViewText(R.id.teamLocation, "www.cafebot.org");
                            views.setTextViewText(R.id.leagueRank, "League rank: " + cafebot.getInt("rank"));
                            views.setTextViewText(R.id.meetWins, "Wins: " + cafebot.getInt("wins"));
                            views.setTextViewText(R.id.meetLosses, "Losses: " + cafebot.getInt("losses"));

                        } else {
                            Log.d("JSON", "rahhh");
                        }

                        // Update the widget once data is ready
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (pendingResult != null) {
                        pendingResult.finish();
                    }
                }
            }
        });
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Keeps the BroadcastReceiver alive while async network calls finish
        final PendingResult pendingResult = goAsync();
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, pendingResult);
        }
    }
}
