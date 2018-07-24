package wallet.bitcoin.bitcoinwallet.helper;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import io.fabric.sdk.android.Fabric;
import org.greenrobot.greendao.database.Database;

import rateusdialoghelper.RateDialogHelper;
import serverconfig.great.app.serverconfig.AwsApp;
import serverconfig.great.app.serverconfig.LibConfigs;
import serverconfig.great.app.serverconfig.helper.AwsLogger;
import wallet.bitcoin.bitcoinwallet.BuildConfig;
import wallet.bitcoin.bitcoinwallet.R;
import wallet.bitcoin.bitcoinwallet.model.DaoMaster;
import wallet.bitcoin.bitcoinwallet.model.DaoSession;
import wallet.bitcoin.bitcoinwallet.model.User;
import wallet.bitcoin.bitcoinwallet.rest.RestClient;

public class App extends Application {

    private static Handler handler;

    private static DaoSession daoSession;

    private static Context appContext;

    private static User currentUser;

    private static RestClient restClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        handler = new Handler();
        appContext = getApplicationContext();

        initGreenDao();

        UIHelper.init(appContext);

        RateDialogHelper.onNewSession(appContext);

        restClient = new RestClient();
    }

    private static void initGreenDao(){
        DaoMaster.OpenHelper helper = new UpgradeDb(appContext, "wallet-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public static DaoSession getDaoSession(){
        return daoSession;
    }

    public static Handler getHandler(){
        return handler;
    }

    public static Context getContext(){
        return appContext;
    }

    public static User getCurrentUser() {
        if (currentUser == null){
            currentUser = new User();
            currentUser.load();
        }

        return currentUser;
    }

    public static User forceLoadCurrentUser() {
        currentUser = new User();
        currentUser.load();

        return currentUser;
    }

    public static void setCurrentUser(User _currentUser) {
        currentUser = _currentUser;
        currentUser.save();
    }

    public static void updateUser() {
        currentUser.save();
    }

    public static RestClient getRestClient()
    {
        return restClient;
    }

}
