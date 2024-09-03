package org.freeland.wildscan.authenticator;

import org.freeland.wildscan.R;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class WildscanAuthenticator extends AbstractAccountAuthenticator {
	private final Context mContext;
    // Simple constructor
    public WildscanAuthenticator(Context context) {
        super(context);
        mContext = context;
    }
    // Editing properties is not supported
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
        throw new UnsupportedOperationException();
    }
    // Don't add additional accounts
    @Override
    public Bundle addAccount(
            AccountAuthenticatorResponse r,
            String s,
            String s2,
            String[] strings,
            Bundle bundle) throws NetworkErrorException {
        return null;
    }
    // Ignore attempts to confirm credentials
    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle) throws NetworkErrorException {
        return null;
    }
    // Getting an authentication token is not supported
    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse r,
            Account account,
            String s,
            Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
    // Getting a label for the auth token is not supported
    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }
    // Updating user credentials is not supported
    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            String s, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
    // Checking features for the account is not supported
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse r, Account account, String[] strings) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
    
    @Override
	public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
		if (mContext.getResources().getString(R.string.sync_account_type).equals(account.type)) {
			Bundle ret = new Bundle();
			ret.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
			return ret;
		}
		return super.getAccountRemovalAllowed(response, account);
	}

	public static class AuthenticatorService extends Service {
        // Instance field that stores the authenticator object
        private WildscanAuthenticator mAuthenticator;
        @Override
        public void onCreate() {
            // Create a new authenticator object
            mAuthenticator = new WildscanAuthenticator(this);
        }
        /*
         * When the system binds to this Service to make the RPC call
         * return the authenticator's IBinder.
         */
        @Override
        public IBinder onBind(Intent intent) {
            return mAuthenticator.getIBinder();
        }
    }
}
