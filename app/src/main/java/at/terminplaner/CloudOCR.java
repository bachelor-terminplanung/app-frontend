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
import android.widget.ImageView;
import android.widget.PopupWindow;
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
        Button button = findViewById(R.id.insertManually);

        button.setOnClickListener(v -> {
            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.manuall_insert_pop_up, null);

            // popup window
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.9);
            int height = (int) (displayMetrics.heightPixels * 0.7);

            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            View rootView = findViewById(android.R.id.content);
            rootView.setVisibility(View.GONE);

            popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
            popupWindow.setOnDismissListener(() -> {
                rootView.setVisibility(View.VISIBLE);
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent image) {
        super.onActivityResult(requestCode, resultCode, image);

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && image != null) {
            Uri imageUri = image.getData();
            imageView.setImageURI(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                VisionApiHelper.recognizeHandwriting(bitmap, apiKey, result -> runOnUiThread(() -> resultText.setText(result)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}