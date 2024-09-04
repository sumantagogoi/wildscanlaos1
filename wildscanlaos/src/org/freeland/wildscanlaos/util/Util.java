package org.freeland.wildscanlaos.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.provider.DocumentsContract;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.freeland.wildscanlaos.App;
import org.freeland.wildscanlaos.BuildConfig;
import org.freeland.wildscanlaos.data.WildscanDataManager;
import org.freeland.wildscanlaos.data.contract.Species;
import org.freeland.wildscanlaos.data.contract.StaticContent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Util {

    public static final int MAX_IMAGE_FILE_SIZE = 200 * 1024;
    private static int MAX_TRIES = 3;
    private static HttpClient sHttpClient = null;
    private static File[] mTmp = new File[1];

    public static String addLangToSelection(Context c, String selection, String fieldName) {
        String lang = WildscanDataManager.getInstance(c).getLanguage();
        String out = "";
        if (lang.contains("en"))  //StaticContent.LANG_CODE_ENGLISH)
            return selection;
        if (!TextUtils.isEmpty(selection))
            out = selection + " AND ";
        out += fieldName + " IS " + DatabaseUtils.sqlEscapeString(lang);

        return out;
    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, boolean rotated,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int inHeight = rotated ? options.outWidth : options.outHeight;
        final int inWidth = rotated ? options.outHeight : options.outWidth;
        int inSampleSize = 1;

        if (inHeight > reqHeight || inWidth > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = reqHeight > 0 ? Math
                    .round((float) inHeight / (float) reqHeight) : Integer.MAX_VALUE;
            final int widthRatio = reqWidth > 0 ? Math
                    .round((float) inWidth / (float) reqWidth) : Integer.MAX_VALUE;

            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static void compressPhoto(String inFileName, String outFileName) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        File outFile = new File(outFileName);
        long size;

        // rotate if needed
        Matrix m = new Matrix();
        m.postRotate(getImageRotation(inFileName));

        File dir = outFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Bitmap bm;
        do {
            try {
                bm = BitmapFactory.decodeFile(inFileName, opts);
                FileOutputStream out = new FileOutputStream(outFile);
                Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, false)
                        .compress(Bitmap.CompressFormat.JPEG, 100, out);
                size = outFile.length();
            } catch (OutOfMemoryError e) {
                size = MAX_IMAGE_FILE_SIZE + 1;
            }
            opts.inSampleSize <<= 1;
        } while (size > MAX_IMAGE_FILE_SIZE);

    }

    public static float getImageRotation(String path) {
        float rotation = 0f;
        if (path == null)
            return rotation;
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270f;
                    break;
            }

        } catch (IOException ioe) {

        }
        return rotation;
    }

    public static void compressPhoto(Bitmap bitmap, String outFileName) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        File outFile = new File(outFileName);
        long size;

        File dir = outFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Bitmap bm = bitmap;

        do {
            try {
                FileOutputStream out = new FileOutputStream(outFile);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                size = outFile.length();
                if (size > MAX_IMAGE_FILE_SIZE) {
                    bm = BitmapFactory.decodeFile(outFileName, opts);
                }
            } catch (OutOfMemoryError e) {
                size = MAX_IMAGE_FILE_SIZE + 1;
            }
            opts.inSampleSize <<= 1;
        } while (size > MAX_IMAGE_FILE_SIZE);

    }

    public static CharSequence formatString(String text) {
        CharacterStyle bold = new StyleSpan(Typeface.BOLD);
        CharacterStyle undeline = new android.text.style.UnderlineSpan();
        CharacterStyle italic = new StyleSpan(Typeface.ITALIC);

        CharSequence newText = setSpanBetweenTokens(text, "<b>", bold);
        ;
        newText = setSpanBetweenTokens(newText, "<ub>", bold, undeline);
        newText = setSpanBetweenTokens(newText, "<i>", italic);
        return newText;
    }

    public static CharSequence setSpanBetweenTokens(CharSequence text,
                                                    String token, CharacterStyle... cs) {
        while (true) {
            // Start and end refer to the points where the span will apply
            int tokenLen = token.length();
            int start = text.toString().indexOf(token) + tokenLen;
            int end = text.toString().indexOf(token, start);

            if (start > -1 && end > -1) {
                // Copy the spannable string to a mutable spannable string
                SpannableStringBuilder ssb = new SpannableStringBuilder(text);
                for (CharacterStyle c : cs)
                    ssb.setSpan(c, start, end, 0);

                // Delete the tokens before and after the span
                ssb.delete(end, end + tokenLen);
                ssb.delete(start - tokenLen, start);

                text = ssb;
            } else
                break;
        }

        return text;
    }

    @SuppressLint("NewApi")
    public static String escapeHtml(String in) {
        if (TextUtils.isEmpty(in))
            return in;
        if (Build.VERSION.SDK_INT >= 16) {
            return Html.escapeHtml(in);
        } else {
            return TextUtils.htmlEncode(in);
        }
    }

    @SuppressLint("NewApi")
    public static String escapeHtmlOrNull(String in) {
        if (TextUtils.isEmpty(in))
            return null;
        if (Build.VERSION.SDK_INT >= 16) {
            return Html.escapeHtml(in);
        } else {
            return TextUtils.htmlEncode(in);
        }
    }

    public static String nullIfEmpty(String in) {
        return TextUtils.isEmpty(in) ? null : in;
    }

    public static boolean rotateImageFile(String path) {
        boolean ok = false;
        try {
            float rotation = getImageRotation(path);

            Bitmap src = BitmapFactory.decodeFile(path);

            int h = src.getHeight(), w = src.getWidth();
            Bitmap out = Bitmap.createBitmap(h, w, src.getConfig());
            Canvas c = new Canvas(out);
            c.rotate(rotation, h / 2, w / 2);
            c.drawBitmap(src, 0, 0, null);
            FileOutputStream fos = new FileOutputStream(path);

            out.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            ok = true;
        } catch (IOException ioe) {

        }

        return ok;
    }

    public static boolean httpDownloadFile(String remote, String local) {
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        FileOutputStream out = null;
        int retry = 0;
        while (retry < MAX_TRIES) {
            try {
                Log.i("ImageUrl: ", remote);
                URL url = new URL(remote);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
//                urlConnection.setDoOutput(true);

                urlConnection.connect();

                in = urlConnection.getInputStream();

                out = new FileOutputStream(local);

                int total = urlConnection.getContentLength();
                int done = 0;

                byte[] buffer = new byte[1024];
                int read = 0;

                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                    done += read;
                }
                out.flush();
                out.close();
                in.close();
                return done == total;
            } catch (MalformedURLException | FileNotFoundException e) {
                retry = MAX_TRIES;
                e.printStackTrace();
            } catch (IOException e) {
                retry++;
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
        }
        return false;
    }
    /*
    public static String addRegionSelection(Context mAppContext, String selection) {
        if (selection == null) {
            selection = "";
        }
        String[] regions = AppPreferences.getSelectedRegions(mAppContext).split(",");
        for (int i = 0; i < regions.length; i++) {
            if (selection.length() != 0) {
                if (selection.contains(Species._S_REGION))
                    selection += " OR ";
                else
                    selection += " AND ";

            }
            selection += Species._S_REGION + "='" + regions[i] + "'";

        }
        return selection;
    }
    */
    public static boolean checkLoginResponseJson(String jsonResponse, Context context) throws
            InvalidCrcException {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            String status = response.getString("status");
            long dex_crc = WildscanDataManager.getInstance(context).dexCrc();
            long crc = response.getLong("crc");
            if (crc != dex_crc) {
                Log.e("CheckLoginResponse",
                        "Error: CRC check failed. val=" + dex_crc + " remote=" + crc);
                if (crc != 0L && !BuildConfig.DEBUG) {
                    throw new InvalidCrcException("Error: CRC check failed. val=" + dex_crc);
                }
            }
            return "OK".equals(status);
        } catch (JSONException e) {
            e.printStackTrace();
            //showMessage("Error: " + e.getMessage() + "\n" + jsonResponse);
        }
        return false;
    }

    public static boolean uploadReportJson(Context ctx, String json) {
        boolean ok = false;
        Util.logInfo("SubmittingReports", json);
        try {
            WildscanDataManager mng = WildscanDataManager.getInstance(ctx);
            // open connection to backend and send JSON version
            String url = Uri
                    .parse(AppConstants.REMOTE_SERVER_URL + WildscanDataManager
                            .REMOTE_PHP_SUBMIT_REPORT)
                    .toString();
            String auth = mng.generateAuthString();
          /*  File file = new File(Environment.getExternalStorageDirectory() + File.separator +
          "encodedstring.txt");
            FileWriter writer = new FileWriter(file);
            BufferedWriter bufferWrite = new BufferedWriter(writer);
            bufferWrite.write(URLEncoder.encode(json, "UTF-8"));*/


            List<NameValuePair> l = new ArrayList<NameValuePair>(1);
//            l.add(new BasicNameValuePair("authorization", auth));
            l.add(new BasicNameValuePair("json", URLEncoder.encode(json, "UTF-8")));
            HttpClient httpClient = getHttpClient(ctx);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(l));
            httpPost.addHeader("Cache-Control", "no-cache");

            HttpResponse response = httpClient.execute(httpPost);
            InputStream is = response.getEntity().getContent();
            String res = readResponse(is);
            Log.e("NOMAN", res);
            is.close();
            StringReader isr = new StringReader(res);

            JsonReader reader = new JsonReader(isr);
            boolean isArray = reader.peek() == JsonToken.BEGIN_ARRAY;
            if (isArray)
                reader.beginArray();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName(), value = null;
                if (reader.peek() == JsonToken.NULL)
                    reader.nextNull();
                else
                    value = reader.nextString();
                if ("result".equalsIgnoreCase(name) && "OK".equalsIgnoreCase(value)) {
                    ok = true;
                    break;
                }
                //Log.d("Sumbit Report Response:", "name: " + name + "; value: " + value);
            }
            reader.endObject();
            if (isArray)
                reader.endArray();

            reader.close();
            isr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok;
    }

    public static void logInfo(String tag, String value) {
        if (BuildConfig.DEBUG)
            Log.i(tag, value);
    }

    public static HttpClient getHttpClient(Context ctx) {
        if (sHttpClient == null) {
            sHttpClient = AndroidHttpClient.newInstance("wildscanapp-app", ctx);
//			sHttpClient = new DefaultHttpClient();
//			InputStream in = null;
//			KeyStore trustStore;
//			try {
//				trustStore = KeyStore.getInstance("BKS");//, keyStore = KeyStore.getInstance
// ("BKS");
//				in = ctx.getApplicationContext().getAssets().open("truststore.bks");
//				trustStore.load(in, ctx.getApplicationContext().getString(R.string.pass1)
// .toCharArray());
//	
//				SchemeRegistry schemeRegistry = new SchemeRegistry();
//				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(),
// 80));
//				SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
// trustStore);
//				sslSocketFactory.setHostnameVerifier(new X509HostnameVerifier() {
//					@Override
//					public void verify(String host, String[] cns, String[] subjectAlts)	throws
// SSLException {
//					}
//					
//					@Override
//					public void verify(String host, X509Certificate cert) throws SSLException {
//					}
//					
//					@Override
//					public void verify(String host, SSLSocket ssl) throws IOException {
//					}
//					
//					@Override
//					public boolean verify(String host, SSLSession session) {
//						return WildscanDataManager.REMOTE_BASE.contains(host);
//					}
//				});
//				schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
//				HttpParams params = new BasicHttpParams();
//				sHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
// schemeRegistry), params);
//	
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				try { if (in!=null) in.close(); } catch (IOException e) {}
//			}
        }
        return sHttpClient;
    }

    public static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    public static File[] listFiles(String path, FilenameFilter filter) {
        return listFilesRecursive(new File(path), filter).toArray(mTmp);
    }

    private static ArrayList<File> listFilesRecursive(File root, FilenameFilter filter) {
        ArrayList<File> files = new ArrayList<File>();
        if (root.isDirectory()) {
            for (File f : root.listFiles(filter)) {
                files.addAll(listFilesRecursive(f, filter));
            }
        } else {
            files.add(root);
        }
        return files;
    }

    public static byte[] readBytesFromResource(Context context, int resourceID) {
        return readTextFromResource(context, resourceID).getBytes();
    }

    public static String readTextFromResource(Context context, int resourceID) {
        InputStream raw = context.getResources().openRawResource(resourceID);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int i;
        try {
            i = raw.read();
            while (i != -1) {
                stream.write(i);
                i = raw.read();
            }
            raw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toString();
    }

    @SuppressLint("NewApi")
    public static String getSelectedImagePath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String id = docId.substring(docId.lastIndexOf(':') + 1);
                Uri contentUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                final String selection = "_id=" + id;

                return getDataColumn(context, contentUri, selection);
            }
        }
//        // Google gallery3d
//        else if ("com.android.gallery3d.provider".equals(uri.getAuthority()) || "com.android
// .gallery3d.provider".equals(uri.getAuthority())) {
//        	File outF = new File(tempFileName);
//        	outF.getParentFile().mkdirs();
//        }
        // Pre-KitKat/MediaStore/General
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Google photos
            if ("com.google.android.apps.photos.content".equals(uri.getAuthority()))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null);
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String colorToHtmlString(int color) {
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        return String.format("#%X%X%X", r, g, b);
    }

    public static String lastSyncTime(long time) {
        if (!AppPreferences.isCallFromActivity(App.getInstance())) {
            Log.i("LastSyncTime: ", time + " ");
            if (time <= 0) return "";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss");//2016-02-28%2021:07:57
            Log.i("LastSyncDate: ", simpleDateFormat.format(new Date(time)));
            return simpleDateFormat.format(new Date(time)).replace(" ", "%20");
        } else {
            return "";
        }
    }

    public static String getRegionId(String region) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("global", "2");
        hashMap.put("se_asia", "4");
        hashMap.put("africa", "1");
        hashMap.put("s_america", "3");

        String r = hashMap.get(region);
        return r == null ? "2" : r;
    }

    public static boolean hasSoftKeys(WindowManager windowManager) {
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();

        int realHeight = 0;
        int realWidth = 0;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);

            realHeight = realDisplayMetrics.heightPixels;
            realWidth = realDisplayMetrics.widthPixels;
        } else {
            Point screenSize = new Point();
            d.getSize(screenSize);
            realHeight = screenSize.y;
            realWidth = screenSize.x;
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    public static class InvalidCrcException extends Exception {
        private static final long serialVersionUID = -5007636274065572301L;

        public InvalidCrcException(String detailMessage) {
            super(detailMessage);
        }
    }
}
