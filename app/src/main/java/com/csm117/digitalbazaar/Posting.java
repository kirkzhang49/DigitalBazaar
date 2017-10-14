package com.csm117.digitalbazaar;

import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class Posting extends AppCompatActivity implements View.OnClickListener{

    private static final int Result_load_image = 1;
    private ImageView imageToupload;
    Button bUploadImage;
    private StorageReference mStorage;
    private static String currentUserId;
    public String latitude;
    public String longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);
        imageToupload =(ImageView)  findViewById(R.id.imageToupload);
        //DownloadedImage =(ImageView)  findViewById(R.id.resultimage);
        bUploadImage =(Button) findViewById(R.id.bUploadImage);
        mStorage = FirebaseStorage.getInstance().getReference();
        //imageToupload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
    }
    private void showMessage(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void Setdata(View view) {
        // Get form info
        EditText et = (EditText) findViewById(R.id.topic);
        String topic = et.getText().toString();
        EditText et1 = (EditText) findViewById(R.id.price);
        String price = et1.getText().toString();
        EditText et2 = (EditText) findViewById(R.id.description);
        String description = et2.getText().toString();
        //imageToupload =(ImageView)  findViewById(R.id.imageToupload);
        imageToupload.buildDrawingCache();
        Bitmap bitmap = imageToupload.getDrawingCache();
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        byte[] image=stream.toByteArray();
       // System.out.println("byte array:"+image);
        String Imaged = Base64.encodeToString(image, 0);

        // Save token to database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference Posts = database.getReference();

        // Add something to database
        //PostInfo p = new PostInfo(topic, "someone");
        currentUserId = getIntent().getExtras().getString("userID");
        PostInfo p = new PostInfo(topic,price,description,Imaged, currentUserId,latitude,longitude);
        Posts.child("posts").child(topic).setValue(p);

        showMessage("Done Posting");
        finish();
    }

    @Override
    public void onClick(View v) {//ctrl+i
        switch(v.getId())
        {
            case R.id.bUploadImage:
                //Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                //startActivityForResult(galleryIntent,Result_load_image);
                startActivityForResult(intent,Result_load_image);
                break;

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//ctrl + o
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Result_load_image&& resultCode==RESULT_OK&&data!=null)
        {
            Uri uri = data.getData();
            StorageReference filepath = mStorage.child("Photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(Posting.this,"UpLoad Done",Toast.LENGTH_LONG).show();
                }
            });
            Uri selectedImage =data.getData();
            imageToupload.setImageURI(selectedImage);
        }
    }

}