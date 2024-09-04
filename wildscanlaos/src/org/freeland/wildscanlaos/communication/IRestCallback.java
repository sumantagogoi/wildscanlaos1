package org.freeland.wildscanlaos.communication;

/**
 * Created by nomankhan25dec on 3/7/2016.
 */

/*
* It is a Callback Interface which is implemented in RestClient class to handle response*/
public interface IRestCallback {
    public void onResponse(Object object);
    public void onError(String message);
}
