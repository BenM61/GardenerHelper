package com.benmassarano.gardeninghelper;

import android.graphics.Bitmap;

public class Plant {
    private String name;
    private int daysUntilWatering;
    private String imageString;
    private int remainingDays;

    public Plant(String name, int daysUntilWatering, Bitmap image) {
        this.name = name;
        this.daysUntilWatering = daysUntilWatering;
        this.imageString = Utils.getStringFromBitmap(image);
        this.remainingDays = daysUntilWatering;
    }

    protected String getName() {
        return name;
    }

    protected int getDaysUntilWatering() {
        return daysUntilWatering;
    }

    protected Bitmap getImage() {
        return Utils.getBitmapFromString(imageString);
    }

    protected int getRemainingDays() {
        return remainingDays;
    }

    protected void decreaseRemainingDays(int days) {
        this.remainingDays -= days;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setDaysUntilWatering(int daysUntilWatering) {
        this.daysUntilWatering = daysUntilWatering;
    }

    protected void setRemainingDays(int remainingDays) {
        this.remainingDays = remainingDays;
    }

    protected void setImage(Bitmap bitmapString) {
        this.imageString = Utils.getStringFromBitmap(bitmapString);
    }

    protected void water() {
        remainingDays = daysUntilWatering;
    }

}
