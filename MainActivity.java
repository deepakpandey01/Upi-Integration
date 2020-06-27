package com.deepak.gpay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    int GOOGLE_PAY_REQUEST_CODE=123;
    EditText name, upivirtualid,amount,note;
    Button send;
    String TAG="Main";
    final int UPI_PAYMENT= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.name);
        upivirtualid = findViewById(R.id.upi_id);
        amount = findViewById(R.id.amount_et);
        note = findViewById(R.id.note);

//        msg = findViewById(R.id.status);
        send = findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(name.getText().toString().trim())){
                    Toast.makeText(MainActivity.this,"Name is invalid!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(upivirtualid.getText().toString().trim())){
                    Toast.makeText(MainActivity.this,"UPI is invalid!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(note.getText().toString().trim())){
                    Toast.makeText(MainActivity.this,"Note is Empty", Toast.LENGTH_SHORT).show();
                } else {
                    payUsingUpi("pavan n", "pavan.n.sap@okaxis",note.getText().toString(), amount.getText().toString());
                }

            }
        });

    }

    void payUsingUpi(String name,String upiId, String note, String amount){
        Log.e("main", "name"+ name + "--upiid--" + upiId + "--" + note + "--" + amount);
        Uri uri= Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa",upiId)
                .appendQueryParameter("pn",name)
                .appendQueryParameter("tn",note)
                .appendQueryParameter("am",amount)
                .appendQueryParameter("cu","INR")
                .build();

        Intent upiPayintent = new Intent(Intent.ACTION_VIEW);
        upiPayintent.setData(uri);

        //will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayintent,"pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())){
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(MainActivity.this,"Gpay not found", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode, data);
        Log.e("main","response"+resultCode);

        switch ((requestCode)){
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)){
                    if(data != null){
                        String trxt = data.getStringExtra("response");
                        Log.e("UPI","onActivityResult: "+ trxt);
                        ArrayList <String> datalist = new ArrayList<>();
                        datalist.add(trxt);
                        upiPaymentDataOperation(datalist);
                    }
                    else{
                        Log.e("UPI","onActivityResult: "+ "Result is null");
                        ArrayList <String> datalist = new ArrayList<>();
                        datalist.add("nothing");
                        upiPaymentDataOperation(datalist);
                    }
                }
                else{
                    Log.e("UPI","onActivityResult: "+ "Result is null");
                    ArrayList <String> datalist = new ArrayList<>();
                    datalist.add("nothing");
                    upiPaymentDataOperation(datalist);
                }
                break;
        }
    }
    private void upiPaymentDataOperation(ArrayList <String> data){
        if(isConnectionAvailable(MainActivity.this)){
            String str = data.get(0);
            Log.e("UPIPAY","upiPaymentDataOperation"+str);
            String paymentCancel = "";
            if(str == null) str="discard";
            String status = "";
            String approvalRefNo = "";
            String[] response = str.split("&");
            for(int i=0;i<response.length; i++){
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if(equalStr[0].toLowerCase().equals("Status".toLowerCase())){
                        status = equalStr[1].toLowerCase();
                    }else if(equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase())){
                        approvalRefNo = equalStr[1];
                    }else{
                        paymentCancel = "Cancel by user";
                    }
                }
                if(status.equals("success")){
                    Toast.makeText(MainActivity.this,"Transaction Successfull", Toast.LENGTH_SHORT).show();
                    Log.e("UPI","payment successfull"+approvalRefNo);
                } else if("Cancel by user".equals(paymentCancel)){
                    Toast.makeText(MainActivity.this,"Cancel by user", Toast.LENGTH_SHORT).show();
                    Log.e("UPI","payment cancel"+approvalRefNo);
                }else{
                    Toast.makeText(MainActivity.this,"Transaction failed", Toast.LENGTH_SHORT).show();
                    Log.e("UPI","failed payment"+approvalRefNo);
                }
            }
        }else {
            Toast.makeText(MainActivity.this,"Internet issue", Toast.LENGTH_SHORT).show();
            Log.e("UPI","Internet issue");
        }
    }
    public static boolean isConnectionAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMetered = connectivityManager.isActiveNetworkMetered();
        if(isMetered){
            return true;
        }
        else{
            return false;
        }
    }
}
