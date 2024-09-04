package org.freeland.wildscanlaos.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.impl.DownloaderService;

public class WildscanDownloaderService extends DownloaderService {
	private static final String PUBLIC_KEY = 
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkruFiggy1i70iw"
			+ "RTz8z4b/geYx6ADm2RVXHUN0mfyvvYuZC82vSi6ikMIoB5sSKUYBefwa"
			+ "HbMB4Mb3QUA21AFLNkxYi2M9/6kPIL+CK3idGu/XoTVhFJBryCIVNQm1/2g"
			+ "RWMyLmjBtqfPibQ5CaoUm8Zh7ptcH1cOy26jbXP5ltATW7yyLNn7qW4hzA/P"
			+ "ECwZB9o4nHcPmyn98quEaS15gGKJMIYQei/7x907uOQXaH8q/SGkSeVmK2"
			+ "Was91HcmLEoBiCkv/RUlyQGtNaUD3limk2IaBssAUgntRav8wVi7hIBWT"
			+ "JTWEzb6wtc4pdqRhfxWIIIcWOf1kTuA2EynzdwIDAQAB";
	private static final byte[] SALT = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

	@Override
	public String getPublicKey() {
		return PUBLIC_KEY;
	}

	@Override
	public byte[] getSALT() {
		return SALT;
	}

	@Override
	public String getAlarmReceiverClassName() {
		return AlarmReceiver.class.getName();
	}
	
	public static class AlarmReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        try {
	            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, WildscanDownloaderService.class);
	        } catch (NameNotFoundException e) {
	            e.printStackTrace();
	        }       
	    }		
	}

}
