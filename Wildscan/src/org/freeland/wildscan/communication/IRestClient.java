package org.freeland.wildscan.communication;

import org.freeland.wildscan.models.response.ContactsResponse;
import org.freeland.wildscan.models.response.RegionResponse;
import org.freeland.wildscan.models.response.SpeciesResponse;
import org.freeland.wildscan.models.response.StatsResponse;
import org.freeland.wildscan.util.AppConstants;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by nomankhan25dec on 3/6/2016.
 */
public interface IRestClient {

    /* @FormUrlEncoded
     @POST(AppConstants.CONTACTS_API)
     Call<ContactsResponse> getContacts(@Field("t") String apiToken,
                                        @Field("later_than") String laterThan);

     @FormUrlEncoded
     @POST(AppConstants.REGION_API)
     Call<RegionResponse> getRegions(@Field("t") String apiToken,
                                     @Field("later_than") String laterThan);

     @FormUrlEncoded
     @POST(AppConstants.SPECIES_API)
     Call<SpeciesResponse> getSpecies(@Field("t") String apiToken,
                                      @Field("region") String regionNo,
                                      @Field("later_than") String laterThan);
 */
    @FormUrlEncoded
    @POST(AppConstants.REGION_STATS_API)
    Call<StatsResponse> getRegionStats(@Field("t") String apiToken,
            @Field("later_than") String laterThan);


    @FormUrlEncoded
    @POST(AppConstants.CONTACTS_API)
    ContactsResponse getContacts(@Field("t") String apiToken,
            @Field("later_than") String laterThan);

    @FormUrlEncoded
    @POST(AppConstants.REGION_API)
    Call<RegionResponse> getRegions(@Field("t") String apiToken );

    @FormUrlEncoded
    @POST(AppConstants.SPECIES_API)
    SpeciesResponse getSpecies(@Field("t") String apiToken,
            @Field("region") String regionNo,
            @Field("later_than") String laterThan);


}
