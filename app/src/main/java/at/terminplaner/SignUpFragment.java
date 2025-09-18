package at.terminplaner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewbinding.ViewBinding;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

import at.terminplaner.databinding.FragmentSignUpBinding;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentSignUpBinding fragmentSignUpBinding;
    private static final String BASE_URL = "http://10.0.2.2:3000/register";
    private static final OkHttpClient client = new OkHttpClient();

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
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
        ViewBinding viewBinding;
        fragmentSignUpBinding = FragmentSignUpBinding.inflate(inflater, container, false);
        return fragmentSignUpBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentSignUpBinding = null;
    }

    private void registerUser(String firstName, String lastName, String color,
                              String address, String username, String password) {
        String json = "{"
                + "\"firstName\":\"" + firstName + "\","
                + "\"lastName\":\"" + lastName + "\","
                + "\"color\":\"" + color + "\","
                + "\"address\":\"" + address + "\","
                + "\"username\":\"" + username + "\","
                + "\"password\":\"" + password + "\""
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
                        Toast.makeText(getContext(), "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_signUpFragment_to_loginFragment);
                    } else {
                        Toast.makeText(getContext(), "Serverfehler: " + responseBody, Toast.LENGTH_LONG).show();
                        Log.d("Fehler", responseBody);
                    }
                });
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentSignUpBinding.buttonSignUp.setOnClickListener(v -> {
            String fullname = fragmentSignUpBinding.editTextFullName.getText().toString().trim();
            String[] parts = fullname.split(" ", 2);
            String firstName = parts.length > 0 ? parts[0] : "";
            String lastName = parts.length > 1 ? parts[1] : "";

            String username = fragmentSignUpBinding.editTextUsername.getText().toString().trim();
            String address = fragmentSignUpBinding.editTextEmail.getText().toString().trim();
            String password = fragmentSignUpBinding.editTextPassword.getText().toString();
            String confirmPassword = fragmentSignUpBinding.editTextConfirmPassword.getText().toString();
            String color = "green"; // Default-Farbe, kann angepasst werden

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwörter stimmen nicht überein", Toast.LENGTH_SHORT).show();
                return;
            }

            if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Bitte Vorname, Nachname und Passwort eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(firstName, lastName, color, address, username, password);
        });

        fragmentSignUpBinding.textViewToLogin.setOnClickListener(view1 ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_signUpFragment_to_loginFragment)
        );
    }
}