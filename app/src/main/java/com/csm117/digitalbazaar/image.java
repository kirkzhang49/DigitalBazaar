package com.csm117.digitalbazaar;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class image extends AppCompatActivity implements View.OnClickListener{
    private static final int Result_load_image = 1;
    ImageView imageToupload,DownloadedImage;
    Button bUploadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageToupload =(ImageView)  findViewById(R.id.imagetoupload);
        DownloadedImage =(ImageView)  findViewById(R.id.resultimage);
        bUploadImage =(Button) findViewById(R.id.upload);

        imageToupload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {//ctrl+i
        switch(v.getId())
        {
            case R.id.imagetoupload:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,Result_load_image);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//ctrl + o
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Result_load_image&& resultCode==RESULT_OK&&data!=null)
        {
            Uri selectedImage =data.getData();
            imageToupload.setImageURI(selectedImage);
        }
    }
}

