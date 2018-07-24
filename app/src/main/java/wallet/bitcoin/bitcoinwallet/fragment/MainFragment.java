package wallet.bitcoin.bitcoinwallet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallet.bitcoin.bitcoinwallet.R;
import wallet.bitcoin.bitcoinwallet.activity.AddressDetailActivity;
import wallet.bitcoin.bitcoinwallet.activity.MainActivity;
import wallet.bitcoin.bitcoinwallet.adapter.AddressAdapter;
import wallet.bitcoin.bitcoinwallet.dialog.ImportAddressDialog;
import wallet.bitcoin.bitcoinwallet.dialog.ImportTutorialDialog;
import wallet.bitcoin.bitcoinwallet.helper.App;
import wallet.bitcoin.bitcoinwallet.helper.Constants;
import wallet.bitcoin.bitcoinwallet.helper.OnFinishCallback;
import wallet.bitcoin.bitcoinwallet.helper.Utility;
import wallet.bitcoin.bitcoinwallet.rest.request.AccessTokenRequest;
import wallet.bitcoin.bitcoinwallet.rest.request.GetBalanceRequest;
import wallet.bitcoin.bitcoinwallet.rest.request.GetNewAddressRequest;
import wallet.bitcoin.bitcoinwallet.rest.request.ImportAddressRequest;
import wallet.bitcoin.bitcoinwallet.rest.response.BalanceResponse;
import wallet.bitcoin.bitcoinwallet.rest.response.GetNewAddressResponse;
import wallet.bitcoin.bitcoinwallet.rest.response.TotalBalanceResponse;

import static android.app.Activity.RESULT_OK;

public class MainFragment extends BaseFragment implements AddressAdapter.AddressAdapterCallback, ImportTutorialDialog.ImportTutorialDialogCallback {

    public static final int REQUEST_CODE_DETAILS = 313;

    @BindView(R.id.ivAdd)
    public ImageView ivAdd;

    @BindView(R.id.rvAddresses)
    public RecyclerView rvAddresses;

    @BindView(R.id.rlProgress)
    public SmoothProgressBar rlProgress;

    @BindView(R.id.tvBalance)
    public TextView tvBalance;

    @BindView(R.id.tvBalanceInCurrency)
    public TextView tvBalanceInCurrency;

    private AddressAdapter adapter;

    private float lastBalance;

    private int offsetIndex = 0;

    public static MainFragment newInstance() {
        MainFragment instance = new MainFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = (ViewGroup) LayoutInflater.from(App.getContext()).inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, mRootView);

        init();

        return mRootView;
    }

    private void init() {
        LinearLayoutManager quickLinkLayoutManager = new LinearLayoutManager(rvAddresses.getContext(), LinearLayoutManager.VERTICAL, false);
        rvAddresses.setLayoutManager(quickLinkLayoutManager);
        rvAddresses.setHasFixedSize(true);

        adapter = new AddressAdapter(this);
        rvAddresses.setAdapter(adapter);

        RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {}

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager mg = (LinearLayoutManager) rvAddresses.getLayoutManager();

                int totalCount = mg.getItemCount();
                int lastVisible = mg.findLastVisibleItemPosition();

                if (lastVisible < 0) {
                    return;
                }

                boolean loadMore = (lastVisible == (totalCount - 1));

                if (adapter.getItemCount() < Constants.OFFSET + offsetIndex){
                    loadMore = false;
                }

                if (loadMore) {
                    offsetIndex += Constants.OFFSET;
                    getBalance(null);
                }
            }
        };
        rvAddresses.addOnScrollListener(mScrollListener);

        getBalance(null);

        showTipsDlg();
    }

    public void resetOffset(){
        offsetIndex = 0;
    }

    private void showTipsDlg(){
        if (!App.getCurrentUser().tip1Shown) {
            if (getActivity() != null) {
                ImportTutorialDialog.show(getActivity(), this);
            }

            App.getCurrentUser().tip1Shown = true;
            App.updateUser();
        }
    }

    @OnClick(R.id.llAdd)
    public void onAddClick() {
        createNewAddress();
    }

    @OnClick(R.id.llImport)
    public void onImportClick() {
        importNewAddress();
    }

    private void getAddressesBalance(final OnFinishCallback callback){
        Call<BalanceResponse> balanceRequestCall = App.getRestClient().getApiService().getBalance(new GetBalanceRequest(App.getCurrentUser().accessToken, offsetIndex));
        balanceRequestCall.enqueue(new Callback<BalanceResponse>() {
            @Override
            public void onResponse(Call<BalanceResponse> call, Response<BalanceResponse> response) {
                if (response.isSuccessful()) {

                    BalanceResponse responseBody = response.body();
                    if (responseBody.success) {

                        List<BalanceResponse.Result> results = responseBody.result;

                        if (results == null || (offsetIndex == 0 && results.size() == 0)) {
                            createNewAddress();
                        } else {

                            if (offsetIndex == 0) {
                                adapter.setItems(results);
                            } else {
                                adapter.hideFooter();
                                adapter.addItems(results);
                            }

                            if (getActivity() != null) {
                                ((MainActivity) getActivity()).setAddresses(adapter.getAddresses(), lastBalance);
                            }
                        }
                    } else {
                        if (getActivity() != null) {
                            Toasty.error(getActivity(), responseBody.error_msg, Toast.LENGTH_LONG, true).show();
                        }
                    }

                    showProgress(false);
                } else {
                    if (getActivity() != null) {
                        Toasty.error(getActivity(), response.message(), Toast.LENGTH_LONG, true).show();
                    }
                    showProgress(false);
                }

                if (callback != null) {
                    callback.onFinished();
                }
            }

            @Override
            public void onFailure(Call<BalanceResponse> call, Throwable t) {
                if (getActivity() != null) {
                    Toasty.error(getActivity(), t.toString(), Toast.LENGTH_LONG, true).show();
                }
                showProgress(false);

                if (callback != null) {
                    callback.onFinished();
                }
            }
        });
    }


    public void getBalance(final OnFinishCallback callback) {
        getTotalBalance(callback);
    }

    private void getTotalBalance(final OnFinishCallback callback){
        if (offsetIndex == 0) {
            showProgress(true);
        }

        if (offsetIndex != 0) {
            adapter.showFooter();
        }

        Call<TotalBalanceResponse> totalBalanceRequestCall = App.getRestClient().getApiService().getTotalBalance(new AccessTokenRequest(App.getCurrentUser().accessToken));
        totalBalanceRequestCall.enqueue(new Callback<TotalBalanceResponse>() {
            @Override
            public void onResponse(Call<TotalBalanceResponse> call, Response<TotalBalanceResponse> response) {
                if (response.isSuccessful()) {

                    TotalBalanceResponse responseBody = response.body();
                    if (responseBody.success) {

                        if (responseBody.result == null || responseBody.result.size() == 0){
                            updateBalance(0f);
                        } else {
                            float balance = responseBody.result.get(0).balance;
                            updateBalance(balance);
                        }

                        getAddressesBalance(callback);

                    } else {
                        if (getActivity() != null) {
                            Toasty.error(getActivity(), responseBody.error_msg, Toast.LENGTH_LONG, true).show();
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        Toasty.error(getActivity(), response.message(), Toast.LENGTH_LONG, true).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TotalBalanceResponse> call, Throwable t) {
                if (getActivity() != null) {
                    Toasty.error(getActivity(), t.toString(), Toast.LENGTH_LONG, true).show();
                }
            }
        });
    }

    public void onRatesUpdated(){
        updateBalance(lastBalance);
    }

    @Override
    public void importNewAddress(){
        ImportAddressDialog.show(MainFragment.this.getContext(), new ImportAddressDialog.ImportAddressDialogCallback() {
            @Override
            public void onPrivateKeyImported(String privateKey) {
                importNewAddressWithPrivateKey(privateKey);
            }
        });
    }

    private void importNewAddressWithPrivateKey(String privateKey){
        showProgress(true);

        Call<GetNewAddressResponse> balanceRequestCall = App.getRestClient().getApiService().
                importAddress(new ImportAddressRequest(App.getCurrentUser().accessToken, Constants.CURRENCY, privateKey));
        balanceRequestCall.enqueue(new Callback<GetNewAddressResponse>() {
            @Override
            public void onResponse(Call<GetNewAddressResponse> call, Response<GetNewAddressResponse> response) {
                if (response.isSuccessful()) {

                    GetNewAddressResponse responseBody = response.body();
                    if (responseBody.success) {

                        GetNewAddressResponse.Result result = responseBody.result;
                        adapter.addItem(result);
                        rvAddresses.scrollToPosition(0);

                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).setAddresses(adapter.getAddresses(), lastBalance);
                        }

                        getBalance(null);

                    } else {
                        Toasty.error(MainFragment.this.getContext(), responseBody.error_msg, Toast.LENGTH_LONG, true).show();
                    }

                    showProgress(false);
                } else {
                    Toasty.error(MainFragment.this.getContext(), response.message(), Toast.LENGTH_LONG, true).show();
                    showProgress(false);
                }
            }

            @Override
            public void onFailure(Call<GetNewAddressResponse> call, Throwable t) {
                Toasty.error(MainFragment.this.getContext(), t.toString(), Toast.LENGTH_LONG, true).show();
                showProgress(false);
            }
        });
    }

    private void createNewAddress() {
        showProgress(true);

        Call<GetNewAddressResponse> balanceRequestCall = App.getRestClient().getApiService().
                getNewAddress(new GetNewAddressRequest(App.getCurrentUser().accessToken, Constants.CURRENCY));
        balanceRequestCall.enqueue(new Callback<GetNewAddressResponse>() {
            @Override
            public void onResponse(Call<GetNewAddressResponse> call, Response<GetNewAddressResponse> response) {
                if (response.isSuccessful()) {

                    GetNewAddressResponse responseBody = response.body();
                    if (responseBody.success) {

                        GetNewAddressResponse.Result result = responseBody.result;
                        adapter.addItem(result);
                        rvAddresses.scrollToPosition(0);

                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).setAddresses(adapter.getAddresses(), lastBalance);
                        }

                    } else {
                        Toasty.error(MainFragment.this.getContext(), responseBody.error_msg, Toast.LENGTH_LONG, true).show();
                    }

                    showProgress(false);
                } else {
                    Toasty.error(MainFragment.this.getContext(), response.message(), Toast.LENGTH_LONG, true).show();
                    showProgress(false);
                }
            }

            @Override
            public void onFailure(Call<GetNewAddressResponse> call, Throwable t) {
                Toasty.error(MainFragment.this.getContext(), t.toString(), Toast.LENGTH_LONG, true).show();
                showProgress(false);
            }
        });
    }

//    private void updateBalance(List<BalanceResponse.Result> results) {
//        float res = 0f;
//        for (BalanceResponse.Result result : results) {
//            if (result.currency.equalsIgnoreCase(Constants.CURRENCY)) {
//                res += result.balance;
//            }
//        }
//
//        updateBalance(res);
//    }

    private void updateBalance(float res){
        lastBalance = res;

        if (getActivity() != null) {
            ((MainActivity) getActivity()).setBalance(res);
        }

        float curRate = App.getCurrentUser().getRate();
        tvBalance.setText(Utility.getDoubleStringFormatNoSign(res)+ " " + Constants.CURRENCY);
        tvBalanceInCurrency.setText(Utility.getDoubleStringFormatForCurrency(curRate * res) + " " + App.getCurrentUser().getRateName());
    }

    @Override
    public void showProgress(boolean progressShown) {
        if (progressShown) {
            rlProgress.setVisibility(View.VISIBLE);
        } else {
            rlProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAddressClicked(String id, float balance, String cur) {
        if (getActivity() == null) {
            return;
        }

        Intent intent = new Intent(getActivity(), AddressDetailActivity.class);
        intent.putExtra(AddressDetailActivity.ADDRESS, id);
        intent.putExtra(AddressDetailActivity.BALANCE, balance);
        intent.putExtra(AddressDetailActivity.CUR, cur);

        startActivityForResult(intent, REQUEST_CODE_DETAILS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_DETAILS:
                if (resultCode == RESULT_OK){
                    getBalance(null);
                }
                break;
        }
    }
}
