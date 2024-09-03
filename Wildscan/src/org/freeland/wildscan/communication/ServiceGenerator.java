package org.freeland.wildscan.communication;

import com.squareup.okhttp.OkHttpClient;

import org.freeland.wildscan.util.AppConstants;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by nomankhan25dec on 3/7/2016.
 */

/* This ServiceGenerator class is created to get Retrofit client for network calls*/
public class ServiceGenerator {

    private static OkHttpClient httpClient = new OkHttpClient();
    /* Create Retrofit object which we use to create our service and return*/

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(AppConstants.REMOTE_SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    /* this method user retrofit object to create rest client interface which we use for network request calls
    * this method is used in RestClient class*/
    public static IRestClient createService() {
        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(IRestClient.class);
    }
}
