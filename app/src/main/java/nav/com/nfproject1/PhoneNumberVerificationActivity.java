package nav.com.nfproject1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PhoneNumberVerificationActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int CODE_LENGTH = 5;

//    @BindView(R.id.verification_send_code_linearLayout)
    LinearLayout sendCodeLayout;
//    @BindView(R.id.verification_verify_linearLayout)
    LinearLayout verificationLayout;
//    @BindView(R.id.verification_phone_number_editText)
    EditText phoneNumberEditText;
//    @BindView(R.id.verification_verify_editText)
    EditText verifyEditText;
//    @BindView(R.id.verification_send_message_button)
    Button verificationSendMessageButton;
//    @BindView(R.id.verification_verify_button)
    Button verifyButton;
//    @BindView(R.id.moveToPayment_button)
    Button moveToPaymentButton;

    private ProgressDialog mProgressDialog;

    private String mCurrentCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_verification);
//        ButterKnife.bind(this);
        if (getSupportActionBar()!=null)
            getSupportActionBar().setTitle("Phone Number Verification Demo");
        initViews();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setTitle("Sending Message");

        verificationSendMessageButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
        moveToPaymentButton.setOnClickListener(this);
    }

    void initViews(){
        sendCodeLayout = (LinearLayout)findViewById(R.id.verification_send_code_linearLayout);
        verificationLayout = (LinearLayout)findViewById(R.id.verification_verify_linearLayout);
        phoneNumberEditText = (EditText)findViewById(R.id.verification_phone_number_editText);
        verifyEditText = (EditText)findViewById(R.id.verification_verify_editText);
        verificationSendMessageButton = (Button) findViewById(R.id.verification_send_message_button);
        verifyButton = (Button) findViewById(R.id.verification_verify_button);
        moveToPaymentButton = (Button)findViewById(R.id.moveToPayment_button);
    }

//    @OnClick(R.id.verification_send_message_Button)
    void sendMessage(){
        if (phoneNumberEditText.getText().length()==10) {
            sendMessageUsingURL(phoneNumberEditText.getText().toString());
//            (new SendMessageTaskTwilio()).execute(phoneNumberEditText.getText().toString());
        }else {
            Toast.makeText(this, "Invalid Number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.verification_send_message_button){
            sendMessage();
        }else if (v.getId()==R.id.verification_verify_button){
            verifyCode();
        }else if (v.getId()==R.id.moveToPayment_button){
            startActivity(new Intent(this, MobilePaymentActivity.class));
        }
    }

    private void sendMessageUsingURL(final String pNumber){
        String ACCOUNT_SID = "ACedbc51467f9fa98638dcf32eb177220a";
        String AUTH_TOKEN = "7b53bec5288c603bc228649c0bed779b";
        mCurrentCode = Utils.getRandomNumberString(CODE_LENGTH);

        String postURL = "https://api.twilio.com/2010-04-01/Accounts/"+ACCOUNT_SID+"/Messages";
        try {
            final String base64EncodedCredentials = "Basic "
                    + Base64.encodeToString(
                    (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(),
                    Base64.NO_WRAP);
            StringRequest sr = new StringRequest(Request.Method.POST, postURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    mProgressDialog.hide();
                    Log.d("TAG", "onResponse: "+response);
                    verificationLayout.setVisibility(View.VISIBLE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressDialog.hide();
                    Log.d("TAG", "onErrorResponse: "+error.getLocalizedMessage());
                }
            }){
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Map<String, String> params = new HashMap<>();
//                    params.put("From", "+919873358793");
//                    params.put("To", "+91" + pNumber);
//                    params.put("Body", "Hi, your verification code is "+mCurrentCode);
//                    return super.getParams();
//                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authentication", base64EncodedCredentials);
                    return super.getHeaders();
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    StringBuilder builder = new StringBuilder();
                    builder.append("From=");
                    builder.append("+919873358793");
                    builder.append("&To=");
                    builder.append("+91" + pNumber);
                    builder.append("&Body=");
                    builder.append("Hi, your verification code is "+mCurrentCode);

                    return builder.toString().getBytes();
                }
            };
            mProgressDialog.show();
            VolleySingleton.getInstance(this).getRequestQueue().add(sr);
        }catch (Exception exp){
            mProgressDialog.hide();
        }


    }


//    private class SendMessageTaskTwilio extends AsyncTask<String,Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            mProgressDialog.show();
//            sendCodeLayout.setEnabled(false);
//        }
//
//        @Override
//        protected String doInBackground(String[] params) {
//            String ACCOUNT_SID = "ACedbc51467f9fa98638dcf32eb177220a";
//            String AUTH_TOKEN = "7b53bec5288c603bc228649c0bed779b";
//            TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
//            Account account = client.getAccount();
//            mCurrentCode = Utils.getRandomNumberString(CODE_LENGTH);
//
//            try {
//                MessageFactory messageFactory = account.getMessageFactory();
//                List<NameValuePair> data = new ArrayList<>();
//                data.add(new BasicNameValuePair("To", "+91" + params[0])); // Replace with a valid phone number for your account.
//                data.add(new BasicNameValuePair("From", "+919873358793")); // Replace with a valid phone number for your account.
//                data.add(new BasicNameValuePair("Body", "Hi, your verification code is "+mCurrentCode));
//                Message sms = messageFactory.create(data);
//
//            }catch (TwilioRestException exp){
//
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String o) {
//            mProgressDialog.hide();
//            verificationLayout.setVisibility(View.VISIBLE);
//        }
//    }

    /*
    private class SendMessageTask extends AsyncTask<String,Void, String> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
            sendCodeLayout.setEnabled(false);
        }

        @Override
        protected String doInBackground(String[] params) {
            String authId = "SANMU1MWRJYTY0OGQZMZ";
            String authToken = "NjAxMzUxMzZhM2YyODgxMmE3OTRkNDQzYTBhNWJh";
            RestAPI api = new RestAPI(authId, authToken, "v1");
            mCurrentCode = Utils.getRandomNumberString(CODE_LENGTH);

            LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
            parameters.put("src", "917838535038"); // Sender's phone number with country code
            parameters.put("dst", "91"+params[0]); // Receiver's phone number with country code
            parameters.put("text", "Hi, your verification code is "+mCurrentCode); // Your SMS text message
            parameters.put("url", "http://example.com/report/"); // The URL to which with the status of the message is sent
            parameters.put("method", "GET"); // The method used to call the url

            try {
                // Send the message
//                String response =  api.request("POST", "/Message/", parameters);
//                System.out.println(response);
                MessageResponse msgResponse = api.sendMessage(parameters);

                // Print the response
                System.out.println(msgResponse);
                // Print the Api ID
                System.out.println("Api ID : " + msgResponse.apiId);
                // Print the Response Message
                System.out.println("Message : " + msgResponse.message);

                if (msgResponse.serverCode == 202) {
                    // Print the Message UUID
                    System.out.println("Message UUID : " + msgResponse.messageUuids.get(0).toString());
                    return msgResponse.messageUuids.get(0).toString();
                } else {
                    System.out.println(msgResponse.error);
                }
            } catch (PlivoException e) {
                System.out.println(e.getLocalizedMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String o) {
            mProgressDialog.hide();
            verificationLayout.setVisibility(View.VISIBLE);
        }
    }
*/
//    @OnClick(R.id.verification_verify_Button)
    void verifyCode(){
        if (verifyEditText.getText().length()==CODE_LENGTH && verifyEditText.getText().toString().equalsIgnoreCase(mCurrentCode)) {
            Toast.makeText(this, "Verification Complete", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Invalid Code", Toast.LENGTH_SHORT).show();
        }
    }



}
