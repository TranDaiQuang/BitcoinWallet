package wallet.bitcoin.bitcoinwallet.fragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

    protected ViewGroup mRootView;

    public View getRootView() {
        return mRootView;
    }

    public abstract void showProgress(boolean progressShown);

    public void onVisibleOnScreen(){}

    public void onInvisibleOnScreen(){}

}
