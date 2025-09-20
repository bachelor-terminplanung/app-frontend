package at.terminplaner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        insertManuallyButton.setOnClickListener(v -> {
            Event event = new Event("", "","", 0, false, "", "");
            EventPopUp.showDetailedPopup(CloudOCR.this, event, false, false, inputEvent -> {
                checkAndHandleDuplicate(inputEvent);
            });
        });

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

    void checkAndHandleDuplicate(Event inputEvent) {

        DuplicateEventChecker.checkDuplicateEvent(this, inputEvent.date, inputEvent.time, inputEvent.description, new DuplicateEventChecker.DuplicateCallback() {
            @Override
            public void onResult(boolean isDuplicate, int count) {
                runOnUiThread(() -> {
                    if (isDuplicate) {
                        new androidx.appcompat.app.AlertDialog.Builder(CloudOCR.this)
                                .setTitle("Wiederholter Termin erkannt")
                                .setMessage("Dieses Event existiert bereits (" + count + " mal). Als Wiederholung speichern?")
                                .setPositiveButton("Ja", (dialog, which) -> {
                                    showRepeatingOptions(inputEvent);
                                })
                                .setNegativeButton("Nein", null)
                                .show();
                    } else {
                        new Thread(() -> VisionApiHelper.sendEvent(
                                CloudOCR.this, inputEvent.description, inputEvent.date, inputEvent.time,
                                inputEvent.duration, inputEvent.isRepeating, inputEvent.repeatType, inputEvent.repeatUntil
                        )).start();
                    }
                });
            }

            private void showRepeatingOptions(Event inputEvent) {
                String[] repeatOptions = {"Täglich", "Wöchentlich", "Monatlich", "Jährlich"};
                new androidx.appcompat.app.AlertDialog.Builder(CloudOCR.this)
                        .setTitle("Wiederholungstyp auswählen")
                        .setItems(repeatOptions, (dialogInterface, selectedIndex) -> {
                            String selectedRepeatType = repeatOptions[selectedIndex];

                            new Thread(() -> VisionApiHelper.sendEvent(
                                    CloudOCR.this, inputEvent.description, inputEvent.date, inputEvent.time,
                                    inputEvent.duration, true, selectedRepeatType, inputEvent.repeatUntil
                            )).start();
                        })
                        .setNegativeButton("Abbrechen", null)
                        .show();
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(CloudOCR.this, "Fehler beim Duplikat-Check: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}