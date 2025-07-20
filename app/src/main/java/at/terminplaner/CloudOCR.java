package at.terminplaner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class CloudOCR extends AppCompatActivity {

    private static final int SELECT_IMAGE = 1;
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
        insertManuallyButton.setOnClickListener(v -> showDetailedPopup("", "", ""));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent image) {
        super.onActivityResult(requestCode, resultCode, image);

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && image != null) {
            Uri imageUri = image.getData();
            imageView.setImageURI(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                VisionApiHelper.recognizeHandwriting(bitmap, apiKey, result -> runOnUiThread(() -> {
                    resultText.setText(result);
                }), CloudOCR.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showDetailedPopup(String description, String date, String time) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.manuall_insert_pop_up, null);

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
        EditText inputRepeatType = popupView.findViewById(R.id.inputRepeatType);
        EditText inputRepeatUntil = popupView.findViewById(R.id.inputRepeatUntil);
        Button submitButton = popupView.findViewById(R.id.buttonSubmit);

        // if OCR called, insert values
        inputEventDate.setText(date);
        inputStartTime.setText(time);
        inputDescription.setText(description);

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        submitButton.setOnClickListener(v -> {
            String descriptionInput = inputDescription.getText().toString();
            String dateInput = inputEventDate.getText().toString();
            String timeInput = inputStartTime.getText().toString();
            int duration = Integer.parseInt(inputDuration.getText().toString());
            boolean isRepeating = switchIsRepeating.isChecked();
            String repeatType = inputRepeatType.getText().toString();
            String repeatUntil = inputRepeatUntil.getText().toString();

            new Thread(() -> VisionApiHelper.sendEvent(descriptionInput, dateInput, timeInput, duration, isRepeating, repeatType, repeatUntil)).start();

            popupWindow.dismiss();
        });
    }


}