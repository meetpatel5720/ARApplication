package com.mp.android.myarapp.Data;

import android.content.Context;
import android.net.Uri;

import com.google.ar.sceneform.rendering.ModelRenderable;
import com.mp.android.myarapp.Activity.MainActivity;

import java.util.concurrent.CompletableFuture;

public class Model {
    public int previewId;
    public String name;

    public Model(){}

    public Model(int previewId, String name) {
        this.previewId = previewId;
        this.name = name;
    }

    public int getPreviewId() {
        return previewId;
    }

    public void setPreviewId(int previewId) {
        this.previewId = previewId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
