package at.terminplaner;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UpdateEventFragment extends Fragment {

    private static final String ARG_EVENT = "event";
    private static final String BASE_URL = "http://192.168.10.28:3000/event";
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private Event event;
    private ProgressBar progressBar;
    private Context context;

    public static UpdateEventFragment newInstance(Event event) {
        UpdateEventFragment fragment = new UpdateEventFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_screen, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            event = getArguments().getParcelable(ARG_EVENT);
        }

        if (event == null) {
            Toast.makeText(context, "Kein Event gefunden", Toast.LENGTH_SHORT).show();
            closeFragment();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        event.getEventID(new EventIdCallback() {
            @Override
            public void onEventIdReceived(int eventId) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        EventPopUp.showDetailedPopup(getActivity(), event, false, true, inputEvent -> {
                            updateEvent(inputEvent, eventId, new Callback() {
                                @Override
                                public void onFailure(okhttp3.Call call, IOException e) {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(context, "Update fehlgeschlagen", Toast.LENGTH_SHORT).show();
                                            closeFragment();
                                        });
                                    }
                                }

                                @Override
                                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(context, "Event aktualisiert", Toast.LENGTH_SHORT).show();
                                            closeFragment();
                                        });
                                    }
                                }
                            });
                        });
                    });
                }
            }


            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, "Fehler beim Finden des Events: " + errorMessage, Toast.LENGTH_LONG).show();
                        closeFragment();
                    });
                }
            }
        });
    }

    private void closeFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    public static void updateEvent(Event event, int eventId, Callback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("event_id", eventId);
            jsonBody.put("user_id", 1);
            jsonBody.put("event_date", event.getDate());
            jsonBody.put("start_time", event.getTime());
            jsonBody.put("description", event.getDescription());
            jsonBody.put("duration", event.getDuration());
            jsonBody.put("is_repeating", event.isRepeating());
            jsonBody.put("repeat_type", event.getRepeatType());
            if (event.getRepeatUntil() == null || event.getRepeatUntil().isEmpty()) {
                jsonBody.put("repeat_until", JSONObject.NULL);
            } else {
                jsonBody.put("repeat_until", event.getRepeatUntil());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/" + eventId)
                .put(body)
                .build();

        Log.d("UPDATE", "updateEvent: " + request);
        Log.d("UPDATE", "body: " + jsonBody.toString());

        client.newCall(request).enqueue(callback);
    }
}
