package wallet.bitcoin.bitcoinwallet.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallet.bitcoin.bitcoinwallet.R;
import wallet.bitcoin.bitcoinwallet.helper.App;
import wallet.bitcoin.bitcoinwallet.helper.Utility;
import wallet.bitcoin.bitcoinwallet.rest.request.GetKeysRequest;
import wallet.bitcoin.bitcoinwallet.rest.request.GetTxsRequest;
import wallet.bitcoin.bitcoinwallet.rest.response.KeysResponse;

public class AddressKeyDetailsActivity extends AppCompatActivity {

    @BindView(R.id.tvPublicKeyValue)
    public TextView tvPublicKeyValue;

    @BindView(R.id.tvPrivateKeyValue)
    public TextView tvPrivateKeyValue;

    @BindView(R.id.ivCopyPrivate)
    public ImageView ivCopyPrivate;

    @BindView(R.id.ivCopyPublic)
    public ImageView ivCopyPublic;

    @BindView(R.id.tvBalance)
    public TextView tvBalance;

    @BindView(R.id.abBack)
    public ImageView abBack;

    @BindView(R.id.abName)
    public TextView abName;

    @BindView(R.id.tvBalanceInCurrency)
    public TextView tvBalanceInCurrency;

    @BindView(R.id.rlProgress)
    public SmoothProgressBar rlProgress;

    private String address;
    private String cur;
    private float balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setStatusBarColor(this);
        setContentView(R.layout.activity_key_detail);
        ButterKnife.bind(this);

        address = getIntent().getStringExtra(AddressDetailActivity.ADDRESS);
        cur = getIntent().getStringExtra(AddressDetailActivity.CUR);
        balance = getIntent().getFloatExtra(AddressDetailActivity.BALANCE, 0f);

        init();
    }

    private void init() {
        Utility.setDrawableColor(ivCopyPrivate, App.getContext().getResources().getColor(R.color.text_color));
        Utility.setDrawableColor(ivCopyPublic, App.getContext().getResources().getColor(R.color.text_color));
        Utility.setDrawableColor(abBack, App.getContext().getResources().getColor(R.color.white));

        abName.setText(App.getContext().getResources().getString(R.string.address) + ":\n" + address);
        tvBalance.setText(Utility.getDoubleStringFormatNoSign(balance) + " " + cur);
        float curRate = App.getCurrentUser().getRate();
        tvBalanceInCurrency.setText(Utility.getDoubleStringFormatForCurrency(curRate * balance) + " " + App.getCurrentUser().getRateName());

        requestKeys();
    }

    private void requestKeys() {
        showProgress(true);

        Call<KeysResponse> balanceRequestCall = App.getRestClient().getApiService().
                getKeys(new GetKeysRequest(App.getCurrentUser().accessToken, address));

        balanceRequestCall.enqueue(new Callback<KeysResponse>() {
            @Override
            public void onResponse(Call<KeysResponse> call, Response<KeysResponse> response) {
                if (response.isSuccessful()) {

                    KeysResponse responseBody = response.body();
                    if (responseBody.success && responseBody.result != null) {
                        onReceivedKeys(responseBody.result.publicKey, responseBody.result.privateKey);
                    } else {
                        Toasty.error(AddressKeyDetailsActivity.this, responseBody.error_msg, Toast.LENGTH_LONG, true).show();
                    }

                    showProgress(false);
                } else {
                    Toasty.error(AddressKeyDetailsActivity.this, response.message(), Toast.LENGTH_LONG, true).show();
                    showProgress(false);
                }
            }

            @Override
            public void onFailure(Call<KeysResponse> call, Throwable t) {
                Toasty.error(AddressKeyDetailsActivity.this, t.toString(), Toast.LENGTH_LONG, true).show();
                showProgress(false);
            }
        });
    }

    private void onReceivedKeys(String pubKey, String privKey) {
        tvPublicKeyValue.setText(pubKey);
        tvPrivateKeyValue.setText(privKey);
    }


    @OnClick(R.id.abBack)
    public void onBackClick() {
        onBackPressed();
    }

    @OnClick(R.id.ivCopyPublic)
    public void onCopyivCopyPublicClicked(){
        Utility.copy(tvPublicKeyValue.getText().toString());
        Toasty.success(this, getResources().getString(R.string.copied)).show();
    }

    @OnClick(R.id.ivCopyPrivate)
    public void onCopyivCopyPrivateClicked(){
        Utility.copy(tvPrivateKeyValue.getText().toString());
        Toasty.success(this, getResources().getString(R.string.copied)).show();
    }

    private void showProgress(boolean progressShown) {
        if (progressShown) {
            rlProgress.setVisibility(View.VISIBLE);
        } else {
            rlProgress.setVisibility(View.GONE);
        }
    }
}
