package at.terminplaner;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class User {
    static String IP_Address = "192.168.10.28";
    private static final OkHttpClient client = new OkHttpClient();

    public static void fetchUserId(Fragment fragment, String username, Runnable onSuccess) {
        String url = "http://" + IP_Address + ":3000/user/id/" + username;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                fragment.requireActivity().runOnUiThread(() ->
                        Toast.makeText(fragment.getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        int id = json.getInt("id");

                        // ID global speichern
                        ((MyApp) fragment.requireActivity().getApplication()).setUserId(id);

                        fragment.requireActivity().runOnUiThread(onSuccess);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    fragment.requireActivity().runOnUiThread(() ->
                            Toast.makeText(fragment.getContext(), "Serverfehler: " + responseBody, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    public interface ColorCallback {
        void onColorReceived(String color);
        void onError(String errorMessage);
    }

    public static void getColorById(int userId, ColorCallback callback) {
        String url = "http://" + IP_Address + ":3000/user/" + userId + "/color";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Serverfehler: " + response.code());
                    return;
                }

                String body = response.body().string();
                try {
                    JSONObject json = new JSONObject(body);
                    String color = json.getString("color");
                    callback.onColorReceived(color);
                } catch (Exception e) {
                    callback.onError("JSON Fehler: " + e.getMessage());
                }
            }
        });
    }
}
