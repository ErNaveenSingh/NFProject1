package nav.com.nfproject1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.payuui.PayUBaseActivity;

public class MobilePaymentActivity extends AppCompatActivity implements View.OnClickListener {

    String merchantTestKey = "gtKFFx";
    String merchantTestSalt = "eCwWELxi";
    private PayuConfig payuConfig;
    private PaymentParams mPaymentParams;
    PayUChecksum checksum;
    private Intent intent;
    private String var1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_payment);
        if (getSupportActionBar()!=null)
            getSupportActionBar().setTitle("Mobile Payment Demo");
        findViewById(R.id.payment_pay_button).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if(data != null ) {
                Toast.makeText(this, "Payment was successful!", Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(data.getStringExtra("result"))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).show();
            }else{
                Toast.makeText(this, "Could not receive data", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.payment_pay_button){
            startPaymentProcess();
        }
    }

    private void startPaymentProcess(){
        mPaymentParams = new PaymentParams();
        payuConfig = new PayuConfig();
        payuConfig.setEnvironment(PayuConstants.MOBILE_STAGING_ENV);

        mPaymentParams.setKey("gtKFFx");
        mPaymentParams.setAmount("25.0");
        mPaymentParams.setProductInfo("Groceries");
        mPaymentParams.setFirstName("Naveen");
        mPaymentParams.setEmail("er.nav.singh@gmail.com");
        mPaymentParams.setTxnId(Utils.getRandomNumberString(10));
        mPaymentParams.setSurl("https://payu.herokuapp.com/success");
        mPaymentParams.setFurl("https://payu.herokuapp.com/failure");
        mPaymentParams.setUdf1("");
        mPaymentParams.setUdf2("");
        mPaymentParams.setUdf3("");
        mPaymentParams.setUdf4("");
        mPaymentParams.setUdf5("");
//        mPaymentParams.setUserCredentials(merchantTestKey+":payutest@payu.in");
        generateHashFromSDK(mPaymentParams);
    }

    /****************************** Client hash generation ***********************************/
    // Do not use this, you may use this only for testing.
    // lets generate hashes.
    // This should be done from server side..
    // Do not keep salt anywhere in app.
    public void generateHashFromSDK(PaymentParams mPaymentParams){
        PayuHashes payuHashes = new PayuHashes();
        PostData postData = new PostData();

        // payment Hash;
        checksum = null;
        checksum = new PayUChecksum();
        checksum.setAmount(mPaymentParams.getAmount());
        checksum.setKey(mPaymentParams.getKey());
        checksum.setTxnid(mPaymentParams.getTxnId());
        checksum.setEmail(mPaymentParams.getEmail());
        checksum.setSalt(merchantTestSalt);
        checksum.setProductinfo(mPaymentParams.getProductInfo());
        checksum.setFirstname(mPaymentParams.getFirstName());
        checksum.setUdf1(mPaymentParams.getUdf1());
        checksum.setUdf2(mPaymentParams.getUdf2());
        checksum.setUdf3(mPaymentParams.getUdf3());
        checksum.setUdf4(mPaymentParams.getUdf4());
        checksum.setUdf5(mPaymentParams.getUdf5());

         postData = checksum.getHash();

        if(postData.getCode() == PayuErrors.NO_ERROR){
            payuHashes.setPaymentHash(postData.getResult());
        }

        // checksum for payemnt related details
        // var1 should be either user credentials or default
        var1 = var1 == null ? PayuConstants.DEFAULT : var1 ;

        if((postData = calculateHash(merchantTestKey, PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, var1, merchantTestSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // Assign post data first then check for success
            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(postData.getResult());
        //vas
        if((postData = calculateHash(merchantTestKey, PayuConstants.VAS_FOR_MOBILE_SDK, PayuConstants.DEFAULT, merchantTestSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setVasForMobileSdkHash(postData.getResult());

        // getIbibocodes
        if((postData = calculateHash(merchantTestKey, PayuConstants.GET_MERCHANT_IBIBO_CODES, PayuConstants.DEFAULT, merchantTestSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setMerchantIbiboCodesHash(postData.getResult());

        if(!var1.contentEquals(PayuConstants.DEFAULT)){
            // get user card
            if((postData = calculateHash(merchantTestKey, PayuConstants.GET_USER_CARDS, var1, merchantTestSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // todo rename storedc ard
                payuHashes.setStoredCardsHash(postData.getResult());
            // save user card
            if((postData = calculateHash(merchantTestKey, PayuConstants.SAVE_USER_CARD, var1, merchantTestSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setSaveCardHash(postData.getResult());
            // delete user card
            if((postData = calculateHash(merchantTestKey, PayuConstants.DELETE_USER_CARD, var1, merchantTestSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setDeleteCardHash(postData.getResult());
            // edit user card
            if((postData = calculateHash(merchantTestKey, PayuConstants.EDIT_USER_CARD, var1, merchantTestSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setEditCardHash(postData.getResult());
        }

        if(mPaymentParams.getOfferKey() != null ){
            postData = calculateHash(merchantTestKey, PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey(), merchantTestSalt);
            if(postData.getCode() == PayuErrors.NO_ERROR){
                payuHashes.setCheckOfferStatusHash(postData.getResult());
            }
        }

        if(mPaymentParams.getOfferKey() != null && (postData = calculateHash(merchantTestKey, PayuConstants.CHECK_OFFER_STATUS, mPaymentParams.getOfferKey(), merchantTestSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR ){
            payuHashes.setCheckOfferStatusHash(postData.getResult());
        }

        // we have generated all the hases now lest launch sdk's ui
        launchSdkUI(payuHashes);
    }

    // deprecated, should be used only for testing.
    private PostData calculateHash(String key, String command, String var1, String salt) {
        checksum = null;
        checksum = new PayUChecksum();
        checksum.setKey(key);
        checksum.setCommand(command);
        checksum.setVar1(var1);
        checksum.setSalt(salt);
        return checksum.getHash();
    }

    public void launchSdkUI(PayuHashes payuHashes){
        // let me add the other params which i might use from other activity
        intent = new Intent(this, PayUBaseActivity.class);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
//        intent.putExtra(PayuConstants.PAYMENT_DEFAULT_PARAMS, mPaymentParams);
//        mPaymentParams.setHash(payuHashes);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);

        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);


        /**
         *  just for testing, dont do this in production.
         *  i need to generate hash for {@link com.payu.india.Tasks.GetTransactionInfoTask} since it requires transaction id, i don't generate hash from my server
         *  merchant should generate the hash from his server.
         *
         */
        intent.putExtra(PayuConstants.SALT, merchantTestSalt);

        startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);

    }

}
