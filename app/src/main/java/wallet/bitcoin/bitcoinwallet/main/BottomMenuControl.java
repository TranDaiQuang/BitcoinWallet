package wallet.bitcoin.bitcoinwallet.main;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wallet.bitcoin.bitcoinwallet.R;
import wallet.bitcoin.bitcoinwallet.activity.MainActivity;
import wallet.bitcoin.bitcoinwallet.helper.App;
import wallet.bitcoin.bitcoinwallet.helper.MagicDrawable;

public class BottomMenuControl {

    private static final float SCALE = 1.2f;

    public View activity_tutorial;

    @BindView(R.id.llSend)
    public LinearLayout llSend;

    @BindView(R.id.llMain)
    public LinearLayout llMain;

    @BindView(R.id.llReceive)
    public LinearLayout llReceive;

    @BindView(R.id.llTransactions)
    public LinearLayout llTransactions;

    @BindView(R.id.ivSend)
    public ImageView ivSend;

    @BindView(R.id.ivHome)
    public ImageView ivHome;

    @BindView(R.id.ivTransactions)
    public ImageView ivTransactions;

    @BindView(R.id.ivReceive)
    public ImageView ivReceive;

    public interface MenuCallback{
        void onSetCurrentViewPagerItem(int pos);
    }

    private MenuCallback callback;

    public BottomMenuControl(View activity_tutorial, MenuCallback callback) {
        ButterKnife.bind(this, activity_tutorial);
        this.activity_tutorial = activity_tutorial;
        this.callback = callback;

        ivSend.setImageDrawable(MagicDrawable.createSelected(ivSend.getDrawable(),
                App.getContext().getResources().getColor(R.color.white_nonselected),
                App.getContext().getResources().getColor(R.color.white)));

        ivHome.setImageDrawable(MagicDrawable.createSelected(ivHome.getDrawable(),
                App.getContext().getResources().getColor(R.color.white_nonselected),
                App.getContext().getResources().getColor(R.color.white)));

        ivTransactions.setImageDrawable(MagicDrawable.createSelected(ivTransactions.getDrawable(),
                App.getContext().getResources().getColor(R.color.white_nonselected),
                App.getContext().getResources().getColor(R.color.white)));

        ivReceive.setImageDrawable(MagicDrawable.createSelected(ivReceive.getDrawable(),
                App.getContext().getResources().getColor(R.color.white_nonselected),
                App.getContext().getResources().getColor(R.color.white)));
    }

    public void onPageScrolled(float positionOffset, int position) {
        llSend.setSelected(false);
        llMain.setSelected(false);
        llTransactions.setSelected(false);
        llReceive.setSelected(false);

        switch (position) {
            case 0: {
                float scale = (1 - SCALE) * positionOffset + SCALE;
                float scale2 = (SCALE - 1) * positionOffset + 1;

                llSend.setScaleX(scale);
                llSend.setScaleY(scale);

                llMain.setScaleX(scale2);
                llMain.setScaleY(scale2);

                llTransactions.setScaleX(1);
                llTransactions.setScaleY(1);

                llReceive.setScaleX(1);
                llReceive.setScaleY(1);

                if (positionOffset == 0) {
                    llSend.setSelected(true);
                }
                break;
            }
            case 1: {
                float scale = (1 - SCALE) * positionOffset + SCALE;
                float scale2 = (SCALE - 1) * positionOffset + 1;

                llSend.setScaleX(1);
                llSend.setScaleY(1);

                llMain.setScaleX(scale);
                llMain.setScaleY(scale);

                llTransactions.setScaleX(scale2);
                llTransactions.setScaleY(scale2);

                llReceive.setScaleX(1);
                llReceive.setScaleY(1);

                if (positionOffset == 0) {
                    llMain.setSelected(true);
                }
                break;
            }

            case 2: {
                float scale = (1 - SCALE) * positionOffset + SCALE;
                float scale2 = (SCALE - 1) * positionOffset + 1;

                llSend.setScaleX(1);
                llSend.setScaleY(1);

                llMain.setScaleX(1);
                llMain.setScaleY(1);


                llTransactions.setScaleX(scale);
                llTransactions.setScaleY(scale);

                llReceive.setScaleX(scale2);
                llReceive.setScaleY(scale2);

                if (positionOffset == 0) {
                    llTransactions.setSelected(true);
                }
                break;
            }
            default: {//pos == 3
                llSend.setScaleX(1);
                llSend.setScaleY(1);

                llMain.setScaleX(1);
                llMain.setScaleY(1);

                llTransactions.setScaleX(1);
                llTransactions.setScaleY(1);

                llReceive.setScaleX(SCALE);
                llReceive.setScaleY(SCALE);

                if (positionOffset == 0) {
                    llReceive.setSelected(true);
                }
            }
            break;
        }
    }

    @OnClick(R.id.llSend)
    public void llSendClicked(){
        callback.onSetCurrentViewPagerItem(MainActivity.FIRST_TAB_NUM);
    }

    @OnClick(R.id.llMain)
    public void llMainClicked(){
        callback.onSetCurrentViewPagerItem(MainActivity.SECOND_TAB_NUM);
    }

    @OnClick(R.id.llTransactions)
    public void llTransactionsClicked(){
        callback.onSetCurrentViewPagerItem(MainActivity.THIRD_TAB_NUM);
    }

    @OnClick(R.id.llReceive)
    public void llReceiveClicked(){
        callback.onSetCurrentViewPagerItem(MainActivity.FOURTH_TAB_NUM);
    }

}
