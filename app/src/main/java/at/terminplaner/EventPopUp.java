package at.terminplaner;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.function.Consumer;

public class EventPopUp {
    public static void showDetailedPopup(Activity activity, Event prefillEvent, boolean fromOCR, boolean forUpdate, Consumer<Event> onSubmit) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.manuall_insert_pop_up, null);
        Button submitButton = popupView.findViewById(R.id.buttonSubmit);
        TextView inputTextOK = popupView.findViewById(R.id.textViewOcrStatus);

        if (fromOCR == true) {
            inputTextOK.setText("Erkannte Daten in Ordnung?");
            submitButton.setText("Daten in Ordnung");
        }
        if (forUpdate == true) {
            inputTextOK.setText("Bitte bearbeiten Sie nun Ihren Termin:");
            submitButton.setText("Daten aktualisieren");
        }
        ConstraintLayout popupRoot = popupView.findViewById(R.id.popup);
        int backgroundColor = isDarkMode(activity) ? R.color.popupBackgroundDark : R.color.popupBackgroundLight;
        popupRoot.setBackgroundColor(ContextCompat.getColor(activity, backgroundColor));

        // popup window
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
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
        switchIsRepeating.setChecked(prefillEvent.isRepeating);
        switchIsRepeating.setOnCheckedChangeListener((buttonView, isChecked) -> {
            spinnerRepeatType.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                activity,
                R.array.repeat_type_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeatType.setAdapter(adapter);

        EditText inputRepeatUntil = popupView.findViewById(R.id.inputRepeatUntil);

        // if OCR called, insert values
        inputEventDate.setText(prefillEvent.date);
        inputStartTime.setText(prefillEvent.time);
        inputDescription.setText(prefillEvent.description);
        inputDuration.setText(String.valueOf(prefillEvent.duration));
        inputRepeatUntil.setText(prefillEvent.repeatUntil);

        View rootView = activity.findViewById(android.R.id.content);
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

            Event inputEvent = new Event(descriptionInput, dateInput, timeInput, duration, isRepeating, repeatType, repeatUntil);
            onSubmit.accept(inputEvent);
            popupWindow.dismiss();
        });
    }
    private static boolean isDarkMode(Activity activity) {
        int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}
