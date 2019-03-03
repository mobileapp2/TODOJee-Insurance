package com.insurance.todojee.utilities;

public class ApplicationConstants {

    //USER SESSION MANAGER CONSTANTS DON'T CHANGE THESE CONSTANTS

    public static final String PREFER_NAME = "Insurance";
    public static final String KEY_APPOPENEDFORFIRST = "KEY_APPOPENEDFORFIRST";
    public static final String IS_USER_LOGIN = "IS_USER_LOGIN";
    public static final String KEY_LOGIN_INFO = "KEY_LOGIN_INFO";
    public static final String KEY_ANDROIDTOKETID = "KEY_ANDROIDTOKETID";

    // BETA API LINK 
//    public static final String API_LINK = "https://gstkhata.com/QA/todojee_insurance/serverfiles/LIC_Apis/";

    //LIVE API LINK 
    private static final String API_LINK = "https://todojeeinsurance.in/serverfiles/LIC_Apis/";


    //CC AVENUE CONSTANTS
    public static final String ACCESS_CODE = "AVHT83GB59BP46THPB";
    public static final String MERCHANT_ID = "153211";
    public static final String CURRENCY = "INR";
    public static final String REDIRECT_URL = "https://www.todojeeinsurance.in/includes/ccavResponseHandler.php";
    public static final String CANCEL_URL = "https://www.todojeeinsurance.in/includes/ccavResponseHandler.php";
    public static final String RSA_KEY_URL = "https://www.todojeeinsurance.in/includes/GetRSA.php";
    public static final String TRANS_URL = "https://secure.ccavenue.com/transaction/initTrans";




    // API's LIST

    public static final String CLIENTAPI = API_LINK + "lic_client.php";
    public static final String INSURANCEAPI = API_LINK + "lic.php";
    public static final String LOGINAPI = API_LINK + "dologin.php";
    public static final String PROFILEAPI = API_LINK + "profile.php";
    public static final String BIRTHDAYANNIVERSARYAPI = API_LINK + "todays_birthday_anniversary.php";
    public static final String OTPAPI = API_LINK + "sendotp.php";
    public static final String REGISTERAPI = API_LINK + "dosignup.php";
    public static final String TODOLISTAPI = API_LINK + "list.php";
    public static final String SETTINGSAPI = API_LINK + "settings.php";
    public static final String PRODUCTINFOAPI = API_LINK + "product_info.php";
    public static final String EVENTSAPI = API_LINK + "event.php";
    public static final String UPLOADFILEAPI = API_LINK + "upload.php";
    public static final String NOTIFICATIONAPI = API_LINK + "notification.php";
    public static final String FAQAPI = API_LINK + "faq.php";
    public static final String SIGNATURE = API_LINK + "signature.php";
    public static final String PLANLISTAPI = API_LINK + "buy_plan.php";
    
}