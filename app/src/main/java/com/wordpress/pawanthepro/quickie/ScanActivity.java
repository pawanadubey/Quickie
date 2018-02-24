package com.wordpress.pawanthepro.quickie;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanActivity extends AppCompatActivity {

    private TextView outputData;
    private Button scanButton;
    private ImageButton copyButton, useButton;
    private IntentIntegrator qrScan;

    private String scanResult, scanType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        getSupportActionBar().setTitle("Scan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        scanResult = "";
        scanType = "";

        qrScan = new IntentIntegrator(this);

        outputData = (TextView) findViewById(R.id.outputData);
        scanButton = (Button) findViewById(R.id.scanButton);
        copyButton = (ImageButton) findViewById(R.id.copyClip);
        useButton = (ImageButton) findViewById(R.id.useButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.setOrientationLocked(false);
                qrScan.setTimeout(20000);
                qrScan.initiateScan();
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", outputData.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ScanActivity.this, "Copied on clip board.", Toast.LENGTH_SHORT).show();
            }
        });

        useButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (scanType) {
                    case "Phone":
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + outputData.getText().toString()));
                        if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.CALL_PHONE},100);
                        }
                        else
                        {
                            startActivity(intent);
                        }
                        break;
                    case "Email":
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",outputData.getText().toString(), null));
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        break;
                    case "Web":
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        String url=outputData.getText().toString();
                        if(!(url.startsWith("http://") || url.startsWith("https://"))){
                            url="http://"+url;
                        }
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        break;
                    case "IP":
                        Intent i1 = new Intent(Intent.ACTION_VIEW);
                        i1.setData(Uri.parse(outputData.getText().toString()));
                        startActivity(i1);
                        break;
                    default:
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                scanResult = result.getContents();
                outputData.setText(scanResult);
                if(Patterns.PHONE.matcher(scanResult).matches()){
                    useButton.setImageResource(R.drawable.ic_phone);
                    scanType="Phone";
                    useButton.setVisibility(View.VISIBLE);
                }else if(Patterns.EMAIL_ADDRESS.matcher(scanResult).matches()){
                    useButton.setImageResource(R.drawable.ic_email);
                    scanType="Email";
                    useButton.setVisibility(View.VISIBLE);
                }else if(Patterns.WEB_URL.matcher(scanResult).matches()){
                    useButton.setImageResource(R.drawable.ic_web);
                    scanType="Web";
                    useButton.setVisibility(View.VISIBLE);
                }else if(Patterns.IP_ADDRESS.matcher(scanResult).matches()){
                    useButton.setImageResource(R.drawable.ic_web);
                    scanType="IP";
                    useButton.setVisibility(View.VISIBLE);
                }else{
                    useButton.setVisibility(View.INVISIBLE);
                }
                copyButton.setVisibility(View.VISIBLE);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getParentActivityIntent() == null) {
                    Intent intent=new Intent(ScanActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_right,
                            R.anim.anim_slide_out_right);
                    finish();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
