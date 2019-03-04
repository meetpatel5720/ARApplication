package com.mp.android.myarapp.Adapter;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.mp.android.myarapp.Data.Model;
import com.mp.android.myarapp.Misc.GlideApp;
import com.mp.android.myarapp.R;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.mp.android.myarapp.Misc.Constant.isAnySelected;

public class ModelListAdapter extends RecyclerView.Adapter<ModelListAdapter.ModelListViewHolder> {
    Context context;
    ArrayList<Model> modelList;
    Model selectedModel = new Model();

    public ModelListAdapter(@NonNull Context context, ArrayList<Model> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ModelListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.model_list_item, viewGroup, false);
        return (new ModelListViewHolder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull ModelListViewHolder modelListViewHolder, int position) {
        Model model = modelList.get(position);
        if (selectedModel.name == null) {
            selectedModel = modelList.get(0);
        }
        GlideApp
                .with(context)
                .load(model.getPreviewId())
                .transform(new RoundedCorners(5))
                .into(modelListViewHolder.modelPreview);

        if (model == selectedModel) {
            modelListViewHolder.selected.setVisibility(View.VISIBLE);
        } else {
            modelListViewHolder.selected.setVisibility(View.INVISIBLE);
        }

        modelListViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedModel = model;
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public Model getSelectedModel() {
        if (selectedModel.getName() == null) {
            return modelList.get(0);
        } else {
            return selectedModel;
        }
    }

    public class ModelListViewHolder extends RecyclerView.ViewHolder {
        ImageView modelPreview;
        ImageView selected;
        RelativeLayout relativeLayout;

        public ModelListViewHolder(@NonNull View itemView) {
            super(itemView);
            modelPreview = itemView.findViewById(R.id.model_preview);
            selected = itemView.findViewById(R.id.is_selected_check);
            relativeLayout = itemView.findViewById(R.id.model_preview_layout);
        }
    }
}
