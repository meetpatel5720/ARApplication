package com.mp.android.myarapp.Data;

import android.content.Context;
import android.net.Uri;

import com.google.ar.sceneform.rendering.ModelRenderable;
import com.mp.android.myarapp.Activity.MainActivity;

import java.util.concurrent.CompletableFuture;

public class Model {
    public int previewId;
    public String name;
    public boolean isSelected;

    public Model(){}

    public Model(int previewId, String name, boolean isSelected) {
        this.previewId = previewId;
        this.name = name;
        this.isSelected = isSelected;
    }

    public int getPreviewId() {
        return previewId;
    }

    public void setPreviewId(int previewId) {
        this.previewId = previewId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
