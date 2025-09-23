package at.terminplaner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeleteEventFragment extends Fragment {
    static String IP_Address = "192.168.10.28";
    private static final String BASE_URL = "http://" + IP_Address + ":3000/event";
    private static final OkHttpClient client = new OkHttpClient();
    private Event event;
    private ProgressBar progressBar;

    public DeleteEventFragment() {
    }

    public static DeleteEventFragment newInstance(Event event) {
        DeleteEventFragment fragment = new DeleteEventFragment();
        Bundle args = new Bundle();
        args.putParcelable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_screen, container, false);
        progressBar = view.findViewById(R.id.progressBar);

        if (getArguments() != null) {
            event = getArguments().getParcelable("event");
        }

        if (event != null) {
            Log.d("EVENT", "event: " + event.toString());
            event.getEventID(requireContext(), new EventIdCallback() {
                @Override
                public void onEventIdReceived(int eventId) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.VISIBLE);
                        deleteEvent(eventId, new Callback() {

                            @Override
                            public void onFailure(Call call, IOException e) {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Löschen fehlgeschlagen", Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager().popBackStack(); // Fragment schließen
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Event gelöscht", Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager().popBackStack(); // Fragment schließen
                                });
                            }
                        });
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Fehler beim Finden des Events: " + errorMessage, Toast.LENGTH_LONG).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
                }
            });
        }

        return view;
    }

    private void deleteEvent(int eventId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + eventId)
                .delete()
                .build();

        client.newCall(request).enqueue(callback);
    }
}
