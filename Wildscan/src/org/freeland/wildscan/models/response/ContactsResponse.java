package org.freeland.wildscan.models.response;

import org.freeland.wildscan.models.RegionContacts;

import java.util.ArrayList;

/**
 * Created by nomankhan25dec on 3/7/2016.
 */
public class ContactsResponse {
    private boolean success;
    private RegionContacts data;

    public ArrayList<RegionContacts> getData() {
        return data.getContacts();
    }

    public boolean isSuccess() {
        return success;
    }
}
