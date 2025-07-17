package at.terminplaner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageView imageView;
    private Button buttonSelectImage, buttonRecognizeText;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonRecognizeText = findViewById(R.id.buttonRecognizeText);
        textViewResult = findViewById(R.id.textViewResult);


        buttonSelectImage.setOnClickListener(v -> selectImage());
        buttonRecognizeText.setOnClickListener(v -> recognizeText());

        String dataPath = getFilesDir().getAbsolutePath() + "/tesseract/";

        Log.d(TAG, "Tesseract data path: " + dataPath);

        File tessdataDir = new File(dataPath + "tessdata/");
        if (!tessdataDir.exists() && tessdataDir.mkdirs()) {
            Log.d(TAG, "tessdata directory created.");
        }

        try {
            copyTrainedData(dataPath);
        } catch (IOException e) {
            Log.e(TAG, "Error copying traineddata file: ", e);
        }

        tessBaseAPI = new TessBaseAPI();
        if (tessBaseAPI.init(dataPath, "deu")) { // Deutsch
            Log.d(TAG, "Tesseract erfolgreich initialisiert.");
        } else {
            Log.e(TAG, "Fehler bei der Initialisierung von Tesseract.");
        }
    }

    private void copyTrainedData(String dataPath) throws IOException {
        InputStream inputStream = getAssets().open("tessdata/deu.traineddata");
        File outFile = new File(dataPath + "tessdata/deu.traineddata");

        // Datei nur kopieren, wenn sie noch nicht existiert
        if (!outFile.exists()) {
            Log.d(TAG, "Copying deu.traineddata to tessdata directory.");
            FileOutputStream outputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } else {
            Log.d(TAG, "deu.traineddata already exists, no need to copy.");
        }
    }


    private static final int REQUEST_IMAGE_SELECT = 1;
    private Bitmap selectedBitmap;
    private TessBaseAPI tessBaseAPI;

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_SELECT);
        } else {
            Toast.makeText(this, "Keine Galerie-App gefunden", Toast.LENGTH_SHORT).show();
        }
    }

    private void recognizeText() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "Bitte zuerst ein Bild auswählen", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tessBaseAPI == null) {
            Log.d(TAG, "Tesseract nicht initialisiert wenn man recognizeTExt().");

            Toast.makeText(this, "Tesseract nicht initialisiert", Toast.LENGTH_SHORT).show();
            return;
        }
        tessBaseAPI.setImage(selectedBitmap);
        String recognizedText = tessBaseAPI.getUTF8Text();
        textViewResult.setText("Erkannt: " + recognizedText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(selectedBitmap);
                textViewResult.setText("Erkannt: ");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Bild konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
