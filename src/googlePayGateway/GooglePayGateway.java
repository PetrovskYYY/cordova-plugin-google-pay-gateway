package GooglePayGateway;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardInfo;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.IsReadyToPayRequest;
// import com.stripe.android.model.Token;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class GooglePayGateway extends CordovaPlugin {
  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 42;

  private PaymentsClient paymentsClient = null;
  private CallbackContext callback;
  private int environment;
 
  private JSONObject TokenizationSpecification;
  private JSONArray AllowedCardNetworks;
  private JSONArray AllowedCardAuthMethods;
  private String MerchantName;
  
  private static JSONObject getBaseRequest()  throws JSONException {
    return new JSONObject()
            .put("apiVersion", 2)
            .put("apiVersionMinor", 0);
  }
  
  private void echo(String message, CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
        callbackContext.success(message);
    } else {
        callbackContext.error("Expected one non-empty string argument.");
    }
}

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

  @Override
  public boolean execute(final String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    this.callback = callbackContext;

    if (action.equals("configure")) {
      this.configure(new JSONObject( data.getString(0) ) );
	  this.initPaymentsClient();
    }

    // These actions require the key to be already set
    if (this.isInitialised()) {
      this.callback.error("googlePayGW not initialised. Please run googlePayGW.configure().");
    }

    if (action.equals("isReadyToPay")) {
      this.isReadyToPay();
    }
    else if (action.equals("requestPayment")) {
      this.requestPayment2( new JSONObject( data.getString(0) ));
    }
    else {
      return false;
    }
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.v("CDEBUG","requestCode:" + requestCode);
    Log.v("CDEBUG","resultCode:" + resultCode);
    switch (requestCode) {
      case LOAD_PAYMENT_DATA_REQUEST_CODE:
        switch (resultCode) {
          case Activity.RESULT_OK:
            PaymentData paymentData = PaymentData.getFromIntent(data);
            // You can get some data on the user's card, such as the brand and last 4 digits
            //CardInfo info = paymentData.getCardInfo();

            // You can also pull the user address from the PaymentData object.
            //UserAddress address = paymentData.getShippingAddress();
            // This is the raw JSON string version of your Stripe token.
            //String rawToken = paymentData.getPaymentMethodToken().getToken();
            String result = paymentData.toJson().toString();
			this.callback.success(result);
            // Now that you have a Stripe token object, charge that by using the id
            // Token gatewayToken = Token.fromString(rawToken);
            // if (gatewayToken != null) {
              // This chargeToken function is a call to your own server, which should then connect
              // to Stripe's API to finish the charge.
              //chargeToken(stripeToken.getId());
              // this.callback.success(stripeToken.getId());
            // } else {
              // this.callback.error("An error occurred");
            // }
            break;
          case Activity.RESULT_CANCELED:
            this.callback.error("Payment cancelled");
            break;
          case AutoResolveHelper.RESULT_ERROR:
            Status status = AutoResolveHelper.getStatusFromIntent(data);

            switch(status.getStatusCode()){
              case WalletConstants.ERROR_CODE_DEVELOPER_ERROR:{
                this.callback.error(status.getStatusCode() + " ERROR_CODE_DEVELOPER_ERROR");
              }
              case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:{
                // https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants
                this.callback.error(status.getStatusCode() + " ERROR_CODE_BUYER_ACCOUNT_ERROR");
              } break;
              default:{
                Log.v("CDEBUG","StatusCode:" + status.getStatusCode());
                Log.v("CDEBUG","StatusMessage:" + status.getStatusMessage());
                this.callback.error(status.getStatusCode());
              }
            }

            // Log the status for debugging
            // Generally there is no need to show an error to
            // the user as the Google Payment API will do that
            break;
          default:
            // Do nothing.
        }
        break; // Breaks the case LOAD_PAYMENT_DATA_REQUEST_CODE
      // Handle any other startActivityForResult calls you may have made.
      default:
        // Do nothing.
    }
  }

  private boolean isInitialised() {
    return this.paymentsClient == null;
  }
  
  private void configure( JSONObject data ) throws  JSONException {
	  this.setTokenizationSpecification(data.getJSONObject("tokenizationSpecification"));
	  this.setAllowedCardNetworks(data.getJSONArray("allowedCardNetworks"));
	  this.setAllowedCardAuthMethods(data.getJSONArray("allowedCardAuthMethods"));
	  this.setMerchantName(data.getString("merchantName"));
  }
  
  private void setTokenizationSpecification(JSONObject data){
	  this.TokenizationSpecification = data;
  }
  
  private JSONObject getTokenizationSpecification(){
    return this.TokenizationSpecification;
  }
  
  private void setAllowedCardNetworks(JSONArray data){
	  this.AllowedCardNetworks = data;
  }
  
  private JSONArray getAllowedCardNetworks(){
    return this.AllowedCardNetworks;
  }

  private void setAllowedCardAuthMethods(JSONArray data){
	  this.AllowedCardNetworks = data;
  }
  
  private JSONArray getAllowedCardAuthMethods(){
    return this.AllowedCardNetworks;
  }
  
  private void setMerchantName(String name){
	  this.MerchantName = name;
  }
  
  private JSONObject getMerchantInfo()  throws JSONException {
    return new JSONObject()
    .put("merchantName", this.MerchantName);
  }
  
  private JSONObject getBaseCardPaymentMethod()  throws JSONException {
  JSONObject cardPaymentMethod = new JSONObject();
  cardPaymentMethod.put("type", "CARD");
  cardPaymentMethod.put(
      "parameters",
      new JSONObject()
          .put("allowedAuthMethods", this.getAllowedCardAuthMethods())
          .put("allowedCardNetworks", this.getAllowedCardNetworks()));

  return cardPaymentMethod;
  }
  
  private JSONObject getCardPaymentMethod()  throws JSONException {
    JSONObject cardPaymentMethod = this.getBaseCardPaymentMethod();
    cardPaymentMethod.put("tokenizationSpecification", this.getTokenizationSpecification());

    return cardPaymentMethod;
  }
  
  public JSONObject getIsReadyToPayRequest()  throws JSONException {
  JSONObject isReadyToPayRequest = getBaseRequest();
  isReadyToPayRequest.put(
    "allowedPaymentMethods",
    new JSONArray()
      .put(this.getBaseCardPaymentMethod()));

  return isReadyToPayRequest;
}
	

  private void initPaymentsClient() {
    Wallet.WalletOptions walletOptions =
            new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
    this.paymentsClient = Wallet.getPaymentsClient(
        this.cordova.getActivity().getApplicationContext(),
        walletOptions
    );
    this.callback.success();
  }

  private void isReadyToPay()  throws JSONException {
    // IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(this.getIsReadyToPayRequest().toString());
	IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
      .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
      .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
      .build();

    Task<Boolean> task = this.paymentsClient.isReadyToPay(request);
    final CallbackContext callbackContext = this.callback;
    task.addOnCompleteListener(
      new OnCompleteListener<Boolean>() {
        public void onComplete(Task<Boolean> task) {
          try {
            boolean result = task.getResult(ApiException.class);
            if (!result) callbackContext.error("Not supported");
            else callbackContext.success();

          } catch (ApiException exception) {
            callbackContext.error("Exception:" + exception.getStatusCode() + exception.getMessage()  + exception.toString());
          }
        }
      });
  }

  private void requestPayment (String totalPrice, String currency) {
    PaymentDataRequest request = this.createPaymentDataRequest(totalPrice, currency);
    Activity activity = this.cordova.getActivity();
    Log.v("CDEBUG","PaymentDataRequest:" + request.toJson());
    if (request != null) {
      cordova.setActivityResultCallback(this);
      AutoResolveHelper.resolveTask(
          this.paymentsClient.loadPaymentData(request),
          activity,
          LOAD_PAYMENT_DATA_REQUEST_CODE);
    }
  }

  private void requestPayment2 (JSONObject requestData) throws JSONException{

//    PaymentDataRequest request = createPaymentDataRequest2(requestData);

    PaymentDataRequest request = PaymentDataRequest.fromJson(requestData.toString());

    Activity activity = this.cordova.getActivity();
    Log.v("CDEBUG","request:" + requestData);
    Log.v("CDEBUG","PaymentDataRequest:" + request.toString());
    Log.v("CDEBUG","PaymentDataRequest:" + request.toJson());
    if (request != null) {
      cordova.setActivityResultCallback(this);
      AutoResolveHelper.resolveTask(
              this.paymentsClient.loadPaymentData(request),
              activity,
              LOAD_PAYMENT_DATA_REQUEST_CODE);
    }
  }

  private PaymentMethodTokenizationParameters createTokenisationParameters() {
    return PaymentMethodTokenizationParameters.newBuilder()
        .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
        .addParameter("gateway", "example")
        .addParameter("gatewayMerchantId", "01234567890123456789")
        .build();
  }

  private PaymentDataRequest createPaymentDataRequest(String totalPrice, String currency) {
    PaymentDataRequest.Builder request =
        PaymentDataRequest.newBuilder()
            .setTransactionInfo(
                TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setTotalPrice(totalPrice)
                    .setCurrencyCode(currency)
                    .build())
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(Arrays.asList(
                        WalletConstants.CARD_NETWORK_AMEX,
                        WalletConstants.CARD_NETWORK_DISCOVER,
                        WalletConstants.CARD_NETWORK_VISA,
                        WalletConstants.CARD_NETWORK_MASTERCARD))
                    .build());

    request.setPaymentMethodTokenizationParameters(this.createTokenisationParameters());
    return request.build();
  }

  private PaymentDataRequest createPaymentDataRequest2(JSONObject requestData) throws JSONException {
    JSONObject transactionInfo = requestData.getJSONObject("transactionInfo");
    String totalPrice = transactionInfo.getString("totalPrice");
    String currency = transactionInfo.getString("currencyCode");

//    PaymentDataRequest request =
//            PaymentDataRequest.fromJson(requestData.toString());

    PaymentDataRequest.Builder request =
            PaymentDataRequest.newBuilder()
                    .setTransactionInfo(
                            TransactionInfo.newBuilder()
                                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                    .setTotalPrice(totalPrice)
                                    .setCurrencyCode(currency)
                                    .build())
                    .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
//                    .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                    .setCardRequirements(
                            CardRequirements.newBuilder()
                                    .addAllowedCardNetworks(Arrays.asList(
//                                            WalletConstants.CARD_NETWORK_AMEX,
//                                            WalletConstants.CARD_NETWORK_DISCOVER,
                                            WalletConstants.CARD_NETWORK_VISA,
                                            WalletConstants.CARD_NETWORK_MASTERCARD
                                    ))
                                    .build());

    request.setPaymentMethodTokenizationParameters(this.createTokenisationParameters());
    return request.build();
  }
}
