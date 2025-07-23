package at.terminplaner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class CloudOCR extends AppCompatActivity {

    private static final int SELECT_IMAGE = 1;
    private static final int CAMERA_PIC_REQUEST = 2500;
    private ImageView imageView;
    private TextView resultText;
    private String apiKey = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloudocr);
        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);

        Button selectImage = findViewById(R.id.selectImage);
        selectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_IMAGE);
        });

        Button insertManuallyButton = findViewById(R.id.insertManually);
        insertManuallyButton.setOnClickListener(v -> showDetailedPopup("", "", "", false));

        Button camera = findViewById(R.id.takeImage);
        camera.setOnClickListener(v -> {
            Intent intent = new Intent(CloudOCR.this, Camera.class);
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent image) {
        super.onActivityResult(requestCode, resultCode, image);

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && image != null) {
            Uri imageUri = image.getData();
            imageView.setImageURI(imageUri);
            processImage(imageUri);
        }
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK && image != null) {
            Uri imageUri = image.getData();  // vom Camera-Intent
            imageView.setImageURI(imageUri);
            processImage(imageUri);
        }
    }
    private void processImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            VisionApiHelper.recognizeHandwriting(bitmap, apiKey, result -> runOnUiThread(() -> {
                resultText.setText(result);
            }), CloudOCR.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void showDetailedPopup(String description, String date, String time, boolean fromOCR) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.manuall_insert_pop_up, null);
        Button submitButton = popupView.findViewById(R.id.buttonSubmit);
        TextView inputTextOK = popupView.findViewById(R.id.textViewOcrStatus);

        if (fromOCR == true) {
            inputTextOK.setText("Erkannte Daten in Ordnung?");
            submitButton.setText("Daten in Ordnung");
        }
        ConstraintLayout popupRoot = popupView.findViewById(R.id.popup);
        int backgroundColor = isDarkMode() ? R.color.popupBackgroundDark : R.color.popupBackgroundLight;
        popupRoot.setBackgroundColor(ContextCompat.getColor(this, backgroundColor));

        // popup window
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.9);
        int height = (int) (displayMetrics.heightPixels * 0.7);

        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.setElevation(10);
        popupWindow.setOutsideTouchable(false);

        EditText inputEventDate = popupView.findViewById(R.id.inputEventDate);
        EditText inputStartTime = popupView.findViewById(R.id.inputStartTime);
        EditText inputDescription = popupView.findViewById(R.id.inputDescription);
        EditText inputDuration = popupView.findViewById(R.id.inputDuration);
        Switch switchIsRepeating = popupView.findViewById(R.id.switchIsRepeating);

        Spinner spinnerRepeatType = popupView.findViewById(R.id.spinnerRepeatType);
        spinnerRepeatType.setVisibility(View.GONE);
        switchIsRepeating.setOnCheckedChangeListener((buttonView, isChecked) -> {
            spinnerRepeatType.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.repeat_type_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeatType.setAdapter(adapter);

        EditText inputRepeatUntil = popupView.findViewById(R.id.inputRepeatUntil);

        // if OCR called, insert values
        inputEventDate.setText(date);
        inputStartTime.setText(time);
        inputDescription.setText(description);

        View rootView = findViewById(android.R.id.content);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            RenderEffect blur = RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP);
            rootView.setRenderEffect(blur);
        }
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rootView.setRenderEffect(null);
            }
        });

        submitButton.setOnClickListener(v -> {
            String descriptionInput = inputDescription.getText().toString();
            String dateInput = inputEventDate.getText().toString();
            String timeInput = inputStartTime.getText().toString();
            int duration = Integer.parseInt(inputDuration.getText().toString());
            boolean isRepeating = switchIsRepeating.isChecked();
            String repeatType = spinnerRepeatType.getSelectedItem().toString();
            String repeatUntil = inputRepeatUntil.getText().toString();

            new Thread(() -> VisionApiHelper.sendEvent(this, descriptionInput, dateInput, timeInput, duration, isRepeating, repeatType, repeatUntil)).start();

            popupWindow.dismiss();
        });
    }
    private boolean isDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

}