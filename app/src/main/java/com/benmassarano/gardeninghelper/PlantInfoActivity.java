package com.benmassarano.gardeninghelper;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.benmassarano.gardeninghelper.MainActivity.INFO_MODE;

public class PlantInfoActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_info);

        setCameraLauncher();
        INFO_MODE action = (INFO_MODE) getIntent().getSerializableExtra("info-mode");
        switch (action) {
            case EDIT:
                Intent intent = getIntent();
                String name = intent.getStringExtra("name");
                String daysUntilString = intent.getStringExtra("daysUntilWatering");
                String remainingDays = intent.getStringExtra("remainingDays");
                Bitmap image = Utils.getBitmapFromString(intent.getStringExtra("image"));

                EditText etName = findViewById(R.id.etName);
                EditText etDaysUntil = findViewById(R.id.etDaysUntilWatering);
                EditText etRemainingDays = findViewById(R.id.etRemainingDays);
                ImageView imPlantImage = findViewById(R.id.imPlantImage);

                // resize the imageView
                imPlantImage.getLayoutParams().height = (int) (MainActivity.HEIGHT * MainActivity.IMAGE_HEIGHT_FACTOR);
                imPlantImage.getLayoutParams().width = (int) (MainActivity.WIDTH * MainActivity.IMAGE_WIDTH_FACTOR);
                imPlantImage.setVisibility(View.VISIBLE);
                imPlantImage.requestLayout();

                //make remaining days visible
                TextView tvRemainingDays = findViewById(R.id.tvRemainingDays);
                tvRemainingDays.setVisibility(View.VISIBLE);
                etRemainingDays.setVisibility(View.VISIBLE);

                // make delete button visible, re-position the buttons
                Button delete = findViewById(R.id.bDelete);
                Button save = findViewById(R.id.bSavePlant);
                delete.setVisibility(View.VISIBLE);

                LayoutParams saveParams = (LayoutParams) save.getLayoutParams();
                saveParams.startToStart = R.id.glVerLeft;
                saveParams.topToBottom = R.id.glButtons;
                saveParams.endToEnd = LayoutParams.UNSET;
                save.requestLayout();

                LayoutParams deleteParams = (LayoutParams) delete.getLayoutParams();
                deleteParams.endToEnd = R.id.glVerRight;
                deleteParams.topToBottom = R.id.glButtons;
                delete.requestLayout();

                // set the data
                etName.setText(name);
                etDaysUntil.setText(daysUntilString);
                etRemainingDays.setText(remainingDays);
                imPlantImage.setImageBitmap(image);
                break;
            case ADD:
                break;
            default:
                Log.i("debug", "Unrecognized mode");
        }

    }

    private void setCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.i("debug", "closed camera");

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Bitmap image = (Bitmap) data.getExtras().get("data");
                            String imageString = Utils.getStringFromBitmap(image);
                            Intent mainIntent = getIntent();
                            mainIntent.putExtra("image", imageString);

                            setResult(RESULT_OK, mainIntent);
                            finish();
                        }
                        else {
                            Log.i("debug", "Camera failed. result code: " + result.getResultCode());
                        }
                    }
                });
    }

    public void startCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
        Log.i("debug", "opened camera");
    }

    public void savePlantDetails(View view) {
        EditText name = findViewById(R.id.etName);
        EditText daysUntil = findViewById(R.id.etDaysUntilWatering);
        EditText remainingDays = findViewById(R.id.etRemainingDays);

        String daysUntilString = daysUntil.getText().toString();
        // check if it's an integer
        try {
            int daysUntilInt = Integer.parseInt(daysUntilString);
            if (daysUntilInt <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "\"Days Until Watering\" must be a positive integer!",
                           Toast.LENGTH_LONG).show();
            return;
        }

        Intent mainIntent = getIntent();
        mainIntent.putExtra("name", name.getText().toString());
        mainIntent.putExtra("daysUntilWatering", daysUntilString);

        INFO_MODE action = (INFO_MODE) mainIntent.getSerializableExtra("info-mode");
        switch (action) {
            case EDIT:
                Log.i("debug", "editing");

                String remainingDaysString = remainingDays.getText().toString();
                try {
                    int remainingDaysInt = Integer.parseInt(remainingDaysString);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "\"Remaining Days\" must be an integer!",
                                   Toast.LENGTH_LONG).show();
                    return;
                }
                mainIntent.putExtra("remainingDays", remainingDays.getText().toString());

                setResult(RESULT_OK, mainIntent);
                finish();
                break;
            case ADD:
                Log.i("debug", "adding");
                startCamera(null);
                break;
            default:
                Log.i("debug", "Unrecognized mode");
                break;
        }
    }

    public void deletePlantDetails(View view) {
        Intent mainIntent = getIntent();
        mainIntent.putExtra("info-mode", INFO_MODE.DELETE);
        setResult(RESULT_OK, mainIntent);
        finish();
    }
}