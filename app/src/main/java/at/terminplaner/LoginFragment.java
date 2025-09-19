package at.terminplaner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;

import at.terminplaner.databinding.FragmentLoginBinding;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentLoginBinding fragmentLoginBinding;
    private static final String BASE_URL = "http://10.0.2.2:3000/login";
    private static final OkHttpClient client = new OkHttpClient();

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false);
        return fragmentLoginBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentLoginBinding = null;
    }

    private void loginUser(String username, String passwordHash) {
        String json = "{"
                + "\"username\":\"" + username + "\","
                + "\"password\":\"" + passwordHash + "\""
                + "}";

        RequestBody body = RequestBody.create(
                json,
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.d("Fehler", e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Login erfolgreich!", Toast.LENGTH_SHORT).show();

                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_loginFragment_to_calendarFragment);
                    } else {
                        Toast.makeText(getContext(), "Login fehlgeschlagen: " + responseBody, Toast.LENGTH_LONG).show();
                        Log.d("Fehler", responseBody);
                    }
                });
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentLoginBinding.buttonLogin.setOnClickListener(v -> {
            String username = fragmentLoginBinding.editTextUsernameLogin.getText().toString();
            String password = fragmentLoginBinding.editTextPasswordLogin.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Bitte Benutzername und Passwort eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(username, password);
        });

        fragmentLoginBinding.textViewToSignUp.setOnClickListener(view1 ->
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_signUpFragment)
        );
    }

}