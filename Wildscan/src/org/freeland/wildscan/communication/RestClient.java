package org.freeland.wildscan.communication;

import android.util.Log;

import org.freeland.wildscan.models.response.ContactsResponse;
import org.freeland.wildscan.models.response.RegionResponse;
import org.freeland.wildscan.models.response.SpeciesResponse;
import org.freeland.wildscan.models.response.StatsResponse;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by nomankhan25dec on 3/6/2016.
 */
public class RestClient {


   /* public void regionRequest(final IRestCallback callback, String timespan) {
        IRestClient restClient = ServiceGenerator.createService();
        Call<RegionResponse> call = restClient.getRegions(AppConstants.API_SECRET_KEY, timespan);
        call.enqueue(new Callback<RegionResponse>() {
            @Override
            public void onResponse(Response<RegionResponse> response, Retrofit retrofit) {
                callback.onResponse(response.body());
                Log.i("Region: ", response.body().getData().get(0).getName()+"empty");
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t.getMessage() + " ");
                Log.i("Region: ", t.getMessage() + "empty");
            }
        });
    }

    public void contactsRequest(final IRestCallback callback, String timespan) {
        IRestClient restClient = ServiceGenerator.createService();
        Call<ContactsResponse> call = restClient.getContacts(AppConstants.API_SECRET_KEY, timespan);
        call.enqueue(new Callback<ContactsResponse>() {
            @Override
            public void onResponse(Response<ContactsResponse> response, Retrofit retrofit) {
                callback.onResponse(response.body());
                Log.i("Contacts: ", response.body().getData().get(0).getName() + "empty");
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t.getMessage() + " ");
                Log.i("Contacts: ", t.getMessage() + "empty");
            }
        });

    }

    public void speciesRequest(final IRestCallback callback, String regionNo, String timespan) {
        IRestClient restClient = ServiceGenerator.createService();
        Call<SpeciesResponse> call = restClient.getSpecies(AppConstants.API_SECRET_KEY, regionNo, timespan);
        call.enqueue(new Callback<SpeciesResponse>() {
            @Override
            public void onResponse(Response<SpeciesResponse> response, Retrofit retrofit) {
                callback.onResponse(response.body());
                Log.i("Species: ", response.body().getData().get(0).getCommon_name() + "empty");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i("Species: ", t.getMessage() + "empty");
                callback.onError(t.getMessage() + " ");
            }
        });
    }

    public void regionStatsRequest(final IRestCallback callback, String timespan) {
        IRestClient restClient = ServiceGenerator.createService();
        Call<StatsResponse> call = restClient.getRegionStats(AppConstants.API_SECRET_KEY, timespan);
        call.enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(Response<StatsResponse> response, Retrofit retrofit) {
                callback.onResponse(response.body());
                Log.i("Region Stats: ", response.body().getData().get(0).getRegion() + "empty");
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t.getMessage() + " ");
                Log.i("Region Stats: ", t.getMessage() + "empty");
            }
        });
    }*/

}