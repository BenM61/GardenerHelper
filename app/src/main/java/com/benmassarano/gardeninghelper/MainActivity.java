package com.benmassarano.gardeninghelper;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CustomRecyclerViewAdapter.ItemClickListener {

    protected final static double IMAGE_HEIGHT_FACTOR = 1f / 3;
    protected final static double IMAGE_WIDTH_FACTOR = 1f / 3.1;
    protected static double HEIGHT = -1;
    protected static double WIDTH = -1;

    private SharedPreferences sharedPreferences;
    private static final String plantListMemoryName = "plantList";

    protected enum INFO_MODE {ADD, EDIT, DELETE}

    private CustomRecyclerViewAdapter adapter;
    private ArrayList<Plant> plants;
    private ActivityResultLauncher<Intent> plantInfoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPlantInfoLauncher();

        sharedPreferences = getSharedPreferences("com.benmassarano.gardeninghelper", MODE_PRIVATE);
        ArrayList<Plant> retrievedList = loadList(sharedPreferences);
        plants = (retrievedList == null) ? new ArrayList<>() : retrievedList;

        RecyclerView recyclerView = findViewById(R.id.rv);
        int columnAmount = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, columnAmount) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                // force width/ height of viewHolder here, this will override layout_height from xml
                HEIGHT = getHeight();
                WIDTH = getWidth();
                lp.height = (int) (getHeight() * IMAGE_HEIGHT_FACTOR);
                lp.width = (int) (getWidth() * IMAGE_WIDTH_FACTOR);
                return true;
            }
        });
        adapter = new CustomRecyclerViewAdapter(this, plants);
        adapter.setClickListener(this);

        recyclerView.setAdapter(adapter);
        updateDate(null);
        updateUI();
    }

    public void displayHelp(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void addPlant(View view) {
        Intent intent = new Intent(this, PlantInfoActivity.class);
        intent.putExtra("info-mode", INFO_MODE.ADD);
        plantInfoLauncher.launch(intent);
    }

    public void updateDate(View view) {
        String currentDate = Utils.getCurrentDay();
        String savedDate = sharedPreferences.getString("date", "");

        if (savedDate.equals("")) {
            sharedPreferences.edit().putString("date", currentDate).apply();
            return;
        }

        int difference = Utils.differenceBetweenDays(savedDate, currentDate);
        if (difference > 0) {
            sharedPreferences.edit().putString("date", currentDate).apply();
            decreaseDay(difference);
            Log.i("debug", "Updated date " + savedDate + " to " + currentDate);
        }
    }

    private void decreaseDay(int days) {
        Log.i("debug", "Decreasing day");
        for (Plant plant : plants) {
            plant.decreaseRemainingDays(days);
        }

        processPlantsChange();
    }

    private void processPlantsChange() {
        savePlantList(sharedPreferences, plants);
        updateUI();
    }

    private void setPlantInfoLauncher() {
        plantInfoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.i("debug", "PlantInfoActivity returned");

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            String name = data.getStringExtra("name");
                            int daysUntilWatering = Integer.parseInt(data.getStringExtra("daysUntilWatering"));
                            Bitmap image = Utils.getBitmapFromString(data.getStringExtra("image"));

                            INFO_MODE action = (INFO_MODE) data.getSerializableExtra("info-mode");
                            Plant plant;
                            int position;
                            switch (action) {
                                case EDIT:
                                    position = Integer.parseInt(data.getStringExtra("position"));
                                    int remainingDays = Integer.parseInt(data.getStringExtra("remainingDays"));
                                    plant = plants.get(position);
                                    plant.setName(name);
                                    plant.setRemainingDays(remainingDays);
                                    plant.setImage(image);
                                    plant.setDaysUntilWatering(daysUntilWatering);
                                    Log.i("debug", "Edited " + name);

                                    break;
                                case ADD:
                                    plant = new Plant(name, daysUntilWatering, image);
                                    plants.add(plant);
                                    Log.i("debug", "Added " + name);
                                    break;
                                case DELETE:
                                    position = Integer.parseInt(data.getStringExtra("position"));
                                    plants.remove(position);
                                    Log.i("debug", "Deleted " + name);
                                default:
                                    Log.i("debug", "Unrecognized mode, added null plant");
                                    break;
                            }

                            syncPlantsChange();
                        }
                    }
                });
    }

    private void syncPlantsChange() {
        savePlantList(sharedPreferences, plants);
        updateUI();
    }

    private void updateUI() {
        TextView plantAmount = findViewById(R.id.tvPlantAmount);
        plantAmount.setText("TOTAL PLANTS: " + plants.size());
        adapter.notifyDataSetChanged();
    }

    private ArrayList<Plant> loadList(SharedPreferences preferences) {
        Gson gson = new Gson();
        String json = preferences.getString(plantListMemoryName, "");
        ArrayList<Plant> retrievedList = gson.fromJson(json, new TypeToken<ArrayList<Plant>>() {}.getType());

        if (retrievedList == null) {
            Log.i("debug", "Couldn't retrieve list");
        }
        Log.i("debug", "load finish");

        return retrievedList;
    }

    private void savePlantList(SharedPreferences preferences, ArrayList<Plant> dataList) {
        Gson gson = new Gson();
        String json = gson.toJson(dataList);
        boolean saved = preferences.edit().putString(plantListMemoryName, json).commit();
        if (!saved) {
            Log.i("debug", "Couldn't save list");
        }
        else {
            Log.i("debug", "Saved list");
        }
    }

    /*********************************  recyclerview adapter's methods   **********************************************/
    @Override
    public void onItemClick(View view, int position) {
        plants.get(position).water();
        //make water sound
        MediaPlayer.create(this, R.raw.water).start();

        syncPlantsChange();
    }

    @Override
    public void onLongItemClick(View view, int position) {
        Intent intent = new Intent(this, PlantInfoActivity.class);
        intent.putExtra("info-mode", INFO_MODE.EDIT);
        intent.putExtra("position", String.valueOf(position));
        intent.putExtra("name", plants.get(position).getName());
        intent.putExtra("daysUntilWatering", String.valueOf(plants.get(position).getDaysUntilWatering()));
        intent.putExtra("remainingDays", String.valueOf(plants.get(position).getRemainingDays()));
        intent.putExtra("image", Utils.getStringFromBitmap(plants.get(position).getImage()));
        plantInfoLauncher.launch(intent);
    }
}