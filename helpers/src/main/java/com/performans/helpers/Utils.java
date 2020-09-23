package com.performans.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");


    private static ProgressDialog dialog;
    private static Pattern pattern;
    private static Matcher matcher;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


    //#region VALIDATION
    public static boolean isValidEmail(String email) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidURL(String urlStr) {
        return Patterns.WEB_URL.matcher(urlStr).matches();
    }

    public static boolean isValidIP(String ip) {
        boolean validate = false;
        Matcher matcher = IP_ADDRESS.matcher(ip);
        if (matcher.matches()) {
            return true;
        }
        return validate;
    }

    public static boolean isValidJSON(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isRooted() {
        return findBinary("su");
    }
    //#endregion


    //#region HARDWARE
    public static double getBatteryPercentage(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        return level / (float) scale;
    }

    public static int getBatteryStatus(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);
        return batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getWifiIpAddress(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiMgr != null;
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ip);
    }

    public static boolean isGPSEnable(Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert manager != null;
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static Intent getLocationSettingsIntent() {
        return new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getDeviceIMEI(Context context) {
        String imei = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            assert telephonyManager != null;
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                imei = telephonyManager.getImei();
            } else {
                imei = telephonyManager.getDeviceId();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return imei;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getDeviceSerial(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        assert mWifi != null;
        return mWifi.isConnected();
    }
    //#endregion


    //#region FILE SYSTEM & BITMAP
    public static Bitmap getBitmapFromFile(String path) {
        File imageFile = new File(path);
        Bitmap bitmap = null;
        if (imageFile.exists()) {
            bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        }

        return bitmap;
    }

    public static Bitmap getBitmapFromURI(Uri uri, Context context) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static byte[] getByteFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] outBuffer = stream.toByteArray();
        return outBuffer;
    }

    public static Bitmap getBitmapFromByte(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static boolean findBinary(String binaryName) {
        boolean found = false;
        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/",
                "/data/local/xbin/", "/data/local/bin/",
                "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
        for (String where : places) {
            if (new File(where + binaryName).exists()) {
                found = true;

                break;
            }
        }
        return found;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // Resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
    //#endregion


    //#region VARIABLE AND DATA OPERATIONS
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || isWhitespace(s);
    }

    private static boolean isWhitespace(String s) {
        int length = s.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String clearNull(String s) {
        return s == null ? "" : (s.equals("null") ? "" : s);
    }

    public static boolean isObjectNull(Object object) {
        return (object == null);
    }

    public static void removeFromArrayListByName(ArrayList<String> arrayList, String name) {

        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).equalsIgnoreCase(name)) {
                arrayList.remove(i);
            }
        }

    }

    public static ArrayList<String> getValuesFromHashMap(HashMap<String, String> hashMap) {
        ArrayList<String> values = new ArrayList<>();
        for (String key : hashMap.keySet()) {
            values.add(hashMap.get(key));
        }
        return values;
    }

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    public static ArrayList<String> getTagList(ArrayList<String> tagList) {
        ArrayList<String> finalTagList = new ArrayList<>();
        for (int i = 0; i < tagList.size(); i++) {
            String[] tag = tagList.get(i).split(",");
            for (int j = 0; j < tag.length; j++) {
                if (!finalTagList.contains(tag[j])) {
                    finalTagList.add(tag[j]);
                }
            }
        }
        return finalTagList;
    }

    public static String getFormatFromIntegerValue(int value) {
        String amount = String.valueOf(value);
        amount = amount.replaceAll(",", "");
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(Double.valueOf(amount));
    }

    public static String getFormatFromDecimalValue(Double value) {
        String amount = String.valueOf(value);
        amount = amount.replaceAll(",", "");
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        return decimalFormat.format(Double.valueOf(amount));
    }

    public static List<String> convertToList(JSONArray jsonArray) {
        ArrayList<String> listdata = new ArrayList<String>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                listdata.add(jsonArray.optString(i));
            }
        }
        return listdata;
    }
    //#endregion


    //#region FEEDBACK AND VIEWS


    public static String formatPhone(String phoneNumber) {
        if (clearNull(phoneNumber).equals("")) return phoneNumber;
        if (phoneNumber.length() == 10) {
            return "0 (" + phoneNumber.substring(0, 3) + ") " + phoneNumber.substring(3, 6) + " " + phoneNumber.substring(6, 8) + " " + phoneNumber.substring(8, 10);
        } else {
            return phoneNumber;
        }
    }

    public static void shortToast(Context context, String message) {
        try {
            Toast.makeText(context,
                    message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void longToast(Context context, String message) {
        try {
            Toast.makeText(context,
                    message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String TAG, String message) {
        Log.d(TAG, message);
    }

    public static void log(String message) {
        Log.d(TAG, message);
    }
    //#endregion


    //#region DATETIME
    public static String getCurrentDateTime(String format) {
        return getTimeByFormat(format);
    }

    public static String getCurrentTime(String format) {
        return getTimeByFormat(format);
    }

    public static String getCurrentDateTime() {
        return getTimeByFormat("dd-MMM-yyyy hh:mm:ss");
    }

    public static String getDbDate() {
        return getTimeByFormat("yyyy-MM-dd");
    }

    public static String getDbTime() {
        return getTimeByFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
    }

    public static String getCurrentTime() {
        return getTimeByFormat("hh:mm");
    }

    @SuppressLint("SimpleDateFormat")
    private static String getTimeByFormat(String format) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
        return df.format(c.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    public static String getLocalFromUTC(String utcTime) {
        try {
            String newUtcTime = utcTime.substring(0, utcTime.indexOf("+"));
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            Date date = dateFormat.parse(newUtcTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            assert date != null;
            return simpleDateFormat.format(date);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("BST"));
                Date date = dateFormat.parse(utcTime);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                assert date != null;
                return simpleDateFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getLocalTimeFromUTCTime(String utcTime) {
        String tempTime = "";
        try {
            String time = getLocalFromUTC(utcTime);
            assert time != null;
            String[] timePice = time.split("T");
            tempTime = timePice[1].substring(0, 8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tempTime;
    }

    public static String getLocalDateFromUTCTime(String utcTime) {
        String dateString = "";
        try {
            String time = getLocalFromUTC(utcTime);
            assert time != null;
            String[] timePice = time.split("T");
            String tempDate = timePice[0];
            String tempTime = timePice[1].substring(0, 8);
            String[] date = tempDate.split("-");
            String year = date[0];
            String month = date[1];
            String day = date[2];
            dateString = day + "/" + month + "/" + year;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dateString;
    }

    public static long getCurrentTimeInMillis() {
        long currentTimeMillis;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        currentTimeMillis = calendar.getTimeInMillis();
        return currentTimeMillis;
    }

    public static long getHoursDifferent(long startTime, long currentTime) {
        long hours = 0;
        long diff = currentTime - startTime;
        hours = diff / (60 * 60 * 1000);
        return hours;
    }

    @SuppressLint("SimpleDateFormat")
    public static long getLongFromStringTime(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            Date date = sdf.parse(timeString);
            assert date != null;
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    //#endregion


    //#region MATH
    public static float getDistance(double lat1, double lng1, double lat2, double lng2) {
        Location loc1 = new Location("");
        Location loc2 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        return loc1.distanceTo(loc2);
    }

    public static String randomString(int length) {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(length);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public static boolean isVersionHigher(String baseVersion, String testVersion) {
        System.out.println("versionToComparable( baseVersion ) =" + versionToComparable(baseVersion));
        System.out.println("versionToComparable( testVersion ) =" + versionToComparable(testVersion) + " is this higher ?");
        return versionToComparable(testVersion).compareTo(versionToComparable(baseVersion)) >= 0;
    }

    private static String versionToComparable(String version) {
        String versionNum = version;
        int at = version.indexOf('-');
        if (at >= 0)
            versionNum = version.substring(0, at);

        String[] numAr = versionNum.split("\\.");
        String versionFormatted = "0";
        for (String tmp : numAr) {
            versionFormatted += String.format("%4s", tmp).replace(' ', '0');
        }
        while (versionFormatted.length() < 12)  // pad out to aaaa.bbbb.cccc
        {
            versionFormatted += "0000";
        }
        return versionFormatted + getVersionModifier(version, at);
    }

    private static String getVersionModifier(String version, int at) {
        String[] wordModsAr = {"-SNAPSHOT", "-ALPHA", "-BETA", "-RC", "-RELEASE"};
        if (at < 0)
            return "." + wordModsAr.length + "00";
        int i = 1;
        for (String word : wordModsAr) {
            if ((at = version.toUpperCase().indexOf(word)) > 0)
                return "." + i + getSecondVersionModifier(version.substring(at + word.length()));
            i++;
        }

        return ".000";
    }

    private static String getSecondVersionModifier(String version) {
        System.out.println("second modifier =" + version + "=");
        Matcher m = Pattern.compile("(.*?)(\\d+).*").matcher(version);
        return m.matches() ? String.format("%2s", m.group(2)).replace(' ', '0') : "00";
    }

    public static String getUploadedAndTotalByteText(long uploadedBytes, long totalBytes) {
        String res = uploadedBytes + " / " + totalBytes + " Bytes";
        // proceed with Byte
        if (totalBytes > 1024) {
            //proceed with KB
            res = String.format(Locale.ENGLISH, "%.2f / %.2f KB", ((float) uploadedBytes / 1024), ((float) totalBytes / 1024));
        }
        if (totalBytes > 1024 * 1024) {
            // proceed with MB
            res = String.format(Locale.ENGLISH, "%.2f / %.2f MB", ((float) uploadedBytes / (1024 * 1024)), ((float) totalBytes / (1024 * 1024)));
        }
        return res;
    }
    //#endregion


    //#region ENCODE-DECODE
    public static String encodeBase64(String text) throws UnsupportedEncodingException {
        assert text != null;
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        return base64.replace("\n", "");
    }

    public static String decodeBase64(String base64) throws UnsupportedEncodingException {
        assert base64 != null;
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return new String(data, StandardCharsets.UTF_8);
    }
    //#endregion
}
