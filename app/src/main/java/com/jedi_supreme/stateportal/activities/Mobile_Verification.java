package com.jedi_supreme.stateportal.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.jedi_supreme.stateportal.R;
import com.jedi_supreme.stateportal.common;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Mobile_Verification extends AppCompatActivity {

    final static int CODE_TIMEOUT = 20; // timeout in seconds

    TextInputEditText et_mobile_number, et_verify_code;
    ConstraintLayout constraint_code_layout;
    LinearLayout linear_mobile_layout;
    ProgressBar pro_Bar_verify;
    TextSwitcher ts_verify_progress;
    Button bt_resend;

    int arr_index = 0;
    Timer verify_timer;

    String verification_id;

    WeakReference<Mobile_Verification> weak_verify_context;

    //====================================================ON CREATE=================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile__verification);

        et_mobile_number = findViewById(R.id.et_mobilenumber);
        et_verify_code = findViewById(R.id.et_verify_code);
        constraint_code_layout = findViewById(R.id.constraint_confirm_code);
        linear_mobile_layout = findViewById(R.id.linear_mobile_number);
        pro_Bar_verify = findViewById(R.id.pro_bar_verify);
        ts_verify_progress = findViewById(R.id.ts_verify_progress);
        bt_resend = findViewById(R.id.bt_resend_code);

        verify_timer = new Timer();
        weak_verify_context = new WeakReference<>(this);

    }
    //====================================================ON CREATE=================================


    //------------------------------------------BUTTON CLICK LISTENERS------------------------------
    public void send_Verification_code(View view) {
        test_number_inpput();
    }

    public void verify_code(View view) {

        if (!et_verify_code.getText().toString().equals("") || !et_verify_code.getText().toString().isEmpty()){
            verifyVerificationCode(et_verify_code.getText().toString());
        }
    }

    public void resend_code(View view) {
    }
    //------------------------------------------BUTTON CLICK LISTENERS------------------------------

    @Override
    protected void onStop(){
        super.onStop();

        pro_Bar_verify.setVisibility(View.GONE);
        pro_Bar_verify.clearAnimation();
        ts_verify_progress.setVisibility(View.GONE);
        verify_timer.cancel();
    }

    //===========================================METHODS============================================
    //test input -> send verification code -> verify code -> sign in

    void test_number_inpput(){
        String user_number ;
        int mobile_number;

        //if user number is not empty
        if (!et_mobile_number.getText().toString().isEmpty() || !et_mobile_number.getText().toString().equals("")){

            if ( et_mobile_number.getText().toString().length() == 10){

                try {

                    mobile_number = Integer.parseInt(et_mobile_number.getText().toString());
                    user_number = common.GHANA_CODE+String.valueOf(mobile_number);
                    send_Code_Method(user_number);

                }catch (Exception ignored){
                    common.snackbar(findViewById(R.id.mobile_verification_parent),
                            "Invalid Number Entered, Please Check and try Again.").show();
                }

            }else {
                common.snackbar(findViewById(R.id.mobile_verification_parent),
                        "Enter Valid Mobile Number").show();
            }


        }else {
            common.snackbar(findViewById(R.id.mobile_verification_parent),
                    "Enter Valid Mobile Number").show();
        }
    }

    //send verification code
    void send_Code_Method(String mobile_number){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile_number,
                CODE_TIMEOUT,
                TimeUnit.SECONDS,
                weak_verify_context.get(),
                verificationCallbacks);
        linear_mobile_layout.setVisibility(View.GONE);
        constraint_code_layout.setVisibility(View.VISIBLE);
    }

    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                String code;

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                    //Getting the code sent by SMS
                    code = phoneAuthCredential.getSmsCode();

                    //sometimes the code is not detected automatically
                    //in this case the code will be null
                    //so user has to manually enter the code
                    if (code != null) {

                        linear_mobile_layout.setVisibility(View.GONE);

                        //verify the code
                        verifyVerificationCode(code);
                    }else {
                        linear_mobile_layout.setVisibility(View.GONE);
                        constraint_code_layout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCodeAutoRetrievalTimeOut(String s) {
                    super.onCodeAutoRetrievalTimeOut(s);
                    bt_resend.setEnabled(true);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(getApplicationContext(),"Verification failed with error: "
                            + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);

                    //storing the verification id that is sent to the user
                     verification_id = s;
                }

            };

    //verify code
    private void verifyVerificationCode(String code) {

        if (constraint_code_layout.getVisibility() == View.VISIBLE){
            constraint_code_layout.setVisibility(View.GONE);
        }

        if (linear_mobile_layout.getVisibility() == View.VISIBLE ){
            linear_mobile_layout.setVisibility(View.GONE);
        }

        //show progress bar
        pro_Bar_verify.setVisibility(View.VISIBLE);
        pro_Bar_verify.animate();

        ts_verify_progress.setVisibility(View.VISIBLE);
        ts_verify_progress.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView tv_verify = new TextView(getApplicationContext());
                tv_verify.setTextSize(16);
                tv_verify.setGravity(Gravity.CENTER);
                tv_verify.setTextColor(getResources().getColor(R.color.report_text));
                return tv_verify;
            }
        });

        verify_timer.schedule(new TimerTask() {
            @Override
            public void run() {

                handler.sendEmptyMessage(arr_index);
                arr_index++;
            }
        },1000,1000);

        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification_id, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    //sign in
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful ,move on to the second step
                            if (mAuth.getCurrentUser() != null){
                                second_Step();
                            }

                        } else {

                            //verification unsuccessful.. display an error message
                            String message = "Verification Unsuccessful, Please try again...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                                pro_Bar_verify.setVisibility(View.GONE);
                                pro_Bar_verify.clearAnimation();
                                ts_verify_progress.setVisibility(View.GONE);
                            }

                            common.snackbar(findViewById(R.id.mobile_verification_parent),message).show();
                        }
                    }
                });

    }

    Handler handler = new Handler(new handlerCallback());

    private class handlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            String[] verify_Arr = new String[]{"Verifying","Verifying.","Verifying..","Verifying..."};
            int x = msg.what;

            if (x < verify_Arr.length){
                ts_verify_progress.setText(verify_Arr[x]);
            }else {
                arr_index = 0;
                ts_verify_progress.setText(verify_Arr[arr_index]);
            }
            return true;
        }
    }

    //===========================================METHODS============================================

    //-------------------------------------------INTENTS--------------------------------------------
    void second_Step(){
        SharedPreferences state_pref = getSharedPreferences(common.PREFERENCE_KEY,MODE_PRIVATE);
        SharedPreferences.Editor pref_editor = state_pref.edit();
        pref_editor.putInt(common.REGISTRATION_STEP_KEY,common.STEP_FILL_DATA).apply();
        Intent userDetails_intent = new Intent(weak_verify_context.get(),UserDetails.class);
        startActivity(userDetails_intent);
        super.finish();
    }
    //-------------------------------------------INTENTS--------------------------------------------
}
