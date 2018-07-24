package wallet.bitcoin.bitcoinwallet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wallet.bitcoin.bitcoinwallet.R;
import wallet.bitcoin.bitcoinwallet.events.RatesUpdateEvent;
import wallet.bitcoin.bitcoinwallet.fragment.BaseFragment;
import wallet.bitcoin.bitcoinwallet.fragment.MainFragment;
import wallet.bitcoin.bitcoinwallet.fragment.ReceiveFragment;
import wallet.bitcoin.bitcoinwallet.fragment.SendFragment;
import wallet.bitcoin.bitcoinwallet.fragment.TransactionFragment;
import wallet.bitcoin.bitcoinwallet.helper.App;
import wallet.bitcoin.bitcoinwallet.helper.CustomViewPager;
import wallet.bitcoin.bitcoinwallet.helper.OnFinishCallback;
import wallet.bitcoin.bitcoinwallet.helper.RatesHelper;
import wallet.bitcoin.bitcoinwallet.helper.Utility;
import wallet.bitcoin.bitcoinwallet.main.BottomMenuControl;
import wallet.bitcoin.bitcoinwallet.model.Fee;
import wallet.bitcoin.bitcoinwallet.model.WalletAddress;

public class MainActivity extends AppCompatActivity implements BottomMenuControl.MenuCallback {

    public static final int FIRST_TAB_NUM = 0;
    public static final int SECOND_TAB_NUM = 1;
    public static final int THIRD_TAB_NUM = 2;
    public static final int FOURTH_TAB_NUM = 3;
    private static final int INITIAL_TAB_INDEX = SECOND_TAB_NUM;

    private static final int CODE_SETTINGS_ACTIVITY = 32;


    @BindView(R.id.llSend)
    public LinearLayout llSend;

    @BindView(R.id.llMain)
    public LinearLayout llMain;

    @BindView(R.id.llReceive)
    public LinearLayout llReceive;

    @BindView(R.id.viewpager)
    public CustomViewPager mViewPager;

    @BindView(R.id.bottomBar)
    public LinearLayout bottomBar;

    @BindView(R.id.rlFee)
    public RelativeLayout rlFee;

    @BindView(R.id.abSettings)
    public ImageView abSettings;

    @BindView(R.id.ivSendBtn)
    public ImageView ivSendBtn;

    @BindView(R.id.swiperefresh)
    public SwipeRefreshLayout mPullToRefreshView;

    @BindView(R.id.abName)
    public TextView abName;

    @BindView(R.id.confirmSend)
    public TextView confirmSend;

    @BindView(R.id.tvFee)
    public TextView tvFee;

    @BindView(R.id.low)
    public Button low;

    @BindView(R.id.optimal)
    public Button optimal;

    @BindView(R.id.fast)
    public Button fast;

    private float lastBalance = 0;

    private BottomMenuControl bottomMenuControl;

    private MainAdapter adapter;

    private BaseFragment lastFragment;

    private List<WalletAddress> results = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setStatusBarColor(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void setBalance(float balance){
        lastBalance = balance;
    }

    public float getBalance(){
        return lastBalance;
    }

    private void init(){
        Utility.setDrawableColor(abSettings, App.getContext().getResources().getColor(R.color.white));
        Utility.setDrawableColor(ivSendBtn, App.getContext().getResources().getColor(R.color.white));

        RatesHelper.getRates();

        mPullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RatesHelper.getRates();

                if (mViewPager.getCurrentItem() == THIRD_TAB_NUM){
                    ((TransactionFragment) adapter.getItem(THIRD_TAB_NUM)).getAllTxs(new OnFinishCallback() {
                        @Override
                        public void onFinished() {
                            mPullToRefreshView.setRefreshing(false);
                        }
                    });
                } else {
                    ((MainFragment) adapter.getItem(SECOND_TAB_NUM)).resetOffset();
                    ((MainFragment) adapter.getItem(SECOND_TAB_NUM)).getBalance(new OnFinishCallback() {
                        @Override
                        public void onFinished() {
                            mPullToRefreshView.setRefreshing(false);
                        }
                    });
                }
            }
        });
        mPullToRefreshView.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        bottomMenuControl = new BottomMenuControl(bottomBar, this);

        adapter = new MainAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                bottomMenuControl.onPageScrolled(positionOffset, position);
            }

            @Override
            public void onPageSelected(int position) {
                onOpenedFragment(position);

                switch (position){
                    case FIRST_TAB_NUM:
                        abName.setText(R.string.send);
                        ivSendBtn.setVisibility(View.VISIBLE);
                        abSettings.setVisibility(View.GONE);
                        break;
                    case SECOND_TAB_NUM:
                        abName.setText(R.string.main);
                        ivSendBtn.setVisibility(View.GONE);
                        abSettings.setVisibility(View.VISIBLE);
                        break;
                    case THIRD_TAB_NUM:
                        abName.setText(R.string.transactions);
                        ivSendBtn.setVisibility(View.GONE);
                        abSettings.setVisibility(View.VISIBLE);

                        ((TransactionFragment) adapter.getItem(THIRD_TAB_NUM)).onRequestAllTxs();
                        break;
                    case FOURTH_TAB_NUM:
                        abName.setText(R.string.receive);
                        ivSendBtn.setVisibility(View.GONE);
                        abSettings.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                enableDisableSwipeRefresh( state == ViewPager.SCROLL_STATE_IDLE );
            }
        });

        mViewPager.setCurrentItem(INITIAL_TAB_INDEX);
        ivSendBtn.setVisibility(View.GONE);
        abSettings.setVisibility(View.VISIBLE);

    }

    private void onOpenedFragment(int pos){
        if (lastFragment != adapter.getItem(pos)) {
            if (lastFragment != null) {
                lastFragment.onInvisibleOnScreen();
            }
            lastFragment = adapter.getItem(pos);
            lastFragment.onVisibleOnScreen();
        }
    }

    public void getBalance(final OnFinishCallback callback){
        ((MainFragment) adapter.getItem(SECOND_TAB_NUM)).getBalance(callback);
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        if (mPullToRefreshView != null) {
            mPullToRefreshView.setEnabled(enable);
        }
    }

    @OnClick(R.id.abSettings)
    public void onSettingsClicked(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, CODE_SETTINGS_ACTIVITY);
    }

    public void setAddresses(List<WalletAddress> results, float balance){
        this.results = results;

        if (results != null && results.size() > 0) {
            ((ReceiveFragment) adapter.getItem(FOURTH_TAB_NUM)).setCurrentAddress(this.results);
            ((SendFragment) adapter.getItem(FIRST_TAB_NUM)).setCurrentAddress(this.results, balance);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CODE_SETTINGS_ACTIVITY:
                if (resultCode == RESULT_OK){

                    ((MainFragment) adapter.getItem(SECOND_TAB_NUM)).onRatesUpdated();

                    if (data != null && data.getBooleanExtra(SettingsActivity.LOGOUT, false)){
                        Intent intent = new Intent(MainActivity.this, RootActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in_at_once, R.anim.fade_out_at_once);
                        finish();
                    }
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRatesUpdated(RatesUpdateEvent event) {
        ((MainFragment) adapter.getItem(SECOND_TAB_NUM)).onRatesUpdated();
    }

    @Override
    public void onSetCurrentViewPagerItem(int pos) {
        mViewPager.setCurrentItem(pos, true);
    }

    public static class MainAdapter extends FragmentStatePagerAdapter {

        private static final int SIZE = 4;

        private SparseArray<BaseFragment> list = new SparseArray<>(SIZE);


        public MainAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BaseFragment fragment = (BaseFragment) super.instantiateItem(container, position);
            list.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            list.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return SIZE;
        }

        @Override
        public BaseFragment getItem(int position) {
            switch (position) {
                case 0:
                    if (list.get(0) == null) {
                        list.put(0, SendFragment.newInstance());
                    }
                    return list.get(0);

                case 1:
                    if (list.get(1) == null) {
                        list.put(1, MainFragment.newInstance());
                    }
                    return list.get(1);

                case 2:
                    if (list.get(2) == null) {
                        list.put(2, TransactionFragment.newInstance());
                    }
                    return list.get(2);

                case 3:
                    if (list.get(3) == null) {
                        list.put(3, ReceiveFragment.newInstance());
                    }
                    return list.get(3);
            }

            return null;
        }
    }

    @OnClick(R.id.ivSendBtn)
    public void ivSendBtnCLicked(){
        confirmSend();
    }

    @OnClick(R.id.confirmSend)
    public void confirmSendCLicked(){
        confirmSend();
    }

    private void confirmSend(){
        ((SendFragment) adapter.getItem(FIRST_TAB_NUM)).onSendBtnClicked(rlFee.getVisibility() == View.VISIBLE);
    }


    public void setFeeVisibility(String feeMode){
        rlFee.setVisibility(View.VISIBLE);
        confirmSend.setVisibility(View.VISIBLE);

        selectFeeBtn(feeMode);
    }

    public void setFeeInVisibility(){
        rlFee.setVisibility(View.GONE);
        confirmSend.setVisibility(View.GONE);
    }

    public boolean isFeeVisible(){
        if (rlFee.getVisibility() == View.VISIBLE){
            return true;
        }

        return false;
    }

    @OnClick(R.id.rlFee)
    public void onFeeClicked(){
        onBackPressed();
    }

    public void selectFeeBtn(String mode){
        fast.setSelected(false);
        optimal.setSelected(false);
        low.setSelected(false);

        StringBuilder feeValue = new StringBuilder();

        if (mode.equalsIgnoreCase(Fee.FAST_FEE)){
            fast.setSelected(true);
            feeValue.append(App.getCurrentUser().fastFee)
                    .append(" ")
                    .append(App.getContext().getResources().getString(R.string.satoshi_per_byte));
        } else    if (mode.equalsIgnoreCase(Fee.SLOW_FEE)){
            low.setSelected(true);
            feeValue.append(App.getCurrentUser().slowFee)
                    .append(" ")
                    .append(App.getContext().getResources().getString(R.string.satoshi_per_byte));
        } else {
            optimal.setSelected(true);
            feeValue.append(App.getCurrentUser().optimalFee)
                    .append(" ")
                    .append(App.getContext().getResources().getString(R.string.satoshi_per_byte));
        }

        tvFee.setText(App.getContext().getResources().getString(R.string.fee) + " " + feeValue.toString());
    }

    @OnClick(R.id.low)
    public void onLowFeeClicked(){
        ((SendFragment) adapter.getItem(FIRST_TAB_NUM)).onFeeChosen(Fee.SLOW_FEE);
        setFeeVisibility(Fee.SLOW_FEE);
    }

    @OnClick(R.id.optimal)
    public void onOptimalFeeClicked(){
        ((SendFragment) adapter.getItem(FIRST_TAB_NUM)).onFeeChosen(Fee.OPTIMAL_FEE);
        setFeeVisibility(Fee.OPTIMAL_FEE);
    }

    @OnClick(R.id.tvFee)
    public void ontvFeelicked(){}

    @OnClick(R.id.bottom)
    public void onbottomlicked(){}

    @OnClick(R.id.fast)
    public void onFastFeeClicked(){
        ((SendFragment) adapter.getItem(FIRST_TAB_NUM)).onFeeChosen(Fee.FAST_FEE);
        setFeeVisibility(Fee.FAST_FEE);
    }

    @Override
    public void onBackPressed() {
        if (rlFee.getVisibility() == View.VISIBLE){
            rlFee.setVisibility(View.GONE);
            confirmSend.setVisibility(View.GONE);
            return;
        }

        super.onBackPressed();
    }
}
