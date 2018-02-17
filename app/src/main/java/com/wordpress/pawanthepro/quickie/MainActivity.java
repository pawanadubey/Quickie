package com.wordpress.pawanthepro.quickie;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private EditText inputData;
    private Button processButton;
    private ImageView qrImageView;
    private ImageButton saveButton, shareButton;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputData = (EditText) findViewById(R.id.inputData);
        processButton = (Button) findViewById(R.id.processButton);
        qrImageView = (ImageView) findViewById(R.id.qrImageView);
        saveButton = (ImageButton) findViewById(R.id.saveButton);
        shareButton = (ImageButton) findViewById(R.id.shareButton);

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = inputData.getText().toString().trim();
                if (input.equals("")) {
                    inputData.setError("Please enter text.");
                } else {
                    new CreateQRCode().execute(input);
                }
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(v);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                try {
                    shareImage(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveImage(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);

        alert.setTitle("File name");
        alert.setMessage("Please give file name for the QR image");
        alert.setView(edittext, 50 ,0, 50 , 0);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String fileName = edittext.getText().toString();
                        if (fileName.equals("")) {
                            fileName = "Quickie";
                        }
                        fileName = fileName + ".jpg";
                        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            final File quickieFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Quickie");
                            File outFile = null;
                            try {
                                if (!quickieFolder.exists()) {
                                    boolean rv = quickieFolder.mkdir();
                                    if (rv) {
                                        BitmapDrawable draw = (BitmapDrawable) qrImageView.getDrawable();
                                        Bitmap bitmap = draw.getBitmap();
                                        FileOutputStream outStream = null;
                                        outFile = new File(quickieFolder, fileName);
                                        outStream = new FileOutputStream(outFile);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                        Toast.makeText(MainActivity.this, "QR Code saved at: " + outFile.toString(), Toast.LENGTH_LONG).show();
                                        outStream.flush();
                                        outStream.close();
                                    }
                                } else {
                                    FileOutputStream outStream = null;
                                    outFile = new File(quickieFolder, fileName);
                                    outStream = new FileOutputStream(outFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                    Toast.makeText(MainActivity.this, "QR Code saved at: " + outFile.toString(), Toast.LENGTH_LONG).show();
                                    outStream.flush();
                                    outStream.close();
                                }
                            } catch (IOException e) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                                builder1.setMessage(e.getMessage());
                                builder1.setCancelable(true);
                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                            }
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
                        }
                    }
                }
        );
        alert.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void shareImage(View v) throws IOException {
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            final File quickieFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Quickie");
            File outFile = null;
            if (!quickieFolder.exists()) {
                boolean rv = quickieFolder.mkdir();
                if (rv) {
                    BitmapDrawable draw = (BitmapDrawable) qrImageView.getDrawable();
                    Bitmap bitmap = draw.getBitmap();

                    FileOutputStream outStream = null;
                    String fileName = String.format("%d.jpg", System.currentTimeMillis());
                    outFile = new File(quickieFolder, fileName);
                    outStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                }
            } else {
                FileOutputStream outStream = null;
                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                outFile = new File(quickieFolder, fileName);
                outStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            }
            if (outFile.equals(null)) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            } else {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(outFile));
                startActivity(Intent.createChooser(share, "Share via"));
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
    }

    Bitmap textToImageEncode(String Value) throws WriterException {

        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    500, 500, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.colorBlack) : getResources().getColor(R.color.colorWhite);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);

        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan, menu); // set your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left,
                R.anim.anim_slide_out_left);
        return super.onOptionsItemSelected(item);
    }

    class CreateQRCode extends AsyncTask<String, Intent, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            processButton.setText("Processing...");
            qrImageView.setVisibility(View.INVISIBLE);
            saveButton.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);
            processButton.setClickable(false);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("Success")){
                qrImageView.setImageBitmap(bitmap);
                saveButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);
                processButton.setText("Process");
            }else {
                Toast.makeText(MainActivity.this, "Something went wrong: "+s, Toast.LENGTH_SHORT).show();
            }
            qrImageView.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);
            processButton.setClickable(true);
        }

        @Override
        protected String doInBackground(String... params) {
            String input = params[0];
            try {
                bitmap = textToImageEncode(input);
                return "Success";
            } catch (WriterException e) {
                return e.getMessage();
            }

        }
    }
}
