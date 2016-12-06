package com.knightriders.medicinewatch.adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class History {

    private String number, image, ndc, status;

    public History() {
    }

    public History(String number, String image, String ndc, String status) {
        this.number = number;
        this.image = image;
        this.ndc = ndc;
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public String getNdc() {
        return ndc;
    }

    public String getNumber() {
        return number;
    }

    public String getStatus() {
        return status;
    }

}