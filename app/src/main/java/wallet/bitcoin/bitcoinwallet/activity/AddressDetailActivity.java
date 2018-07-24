package wallet.bitcoin.bitcoinwallet.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import wallet.bitcoin.bitcoinwallet.adapter.DetailsAdapter;
import wallet.bitcoin.bitcoinwallet.helper.App;
import wallet.bitcoin.bitcoinwallet.helper.Constants;
import wallet.bitcoin.bitcoinwallet.helper.OnFinishCallback;
import wallet.bitcoin.bitcoinwallet.helper.Utility;
import wallet.bitcoin.bitcoinwallet.rest.request.GetTxsRequest;
import wallet.bitcoin.bitcoinwallet.rest.request.RemoveRequest;
import wallet.bitcoin.bitcoinwallet.rest.response.GetTxsResponse;
import wallet.bitcoin.bitcoinwallet.rest.response.RemoveResponse;
import wallet.bitcoin.bitcoinwallet.view.AppRecyclerView;

public class AddressDetailActivity extends AppCompatActivity {

    public static final String ADDRESS = "Address";
    public static final String BALANCE = "balance";
    public static final String CUR = "CUR";

    @BindView(R.id.rvAddresses)
    public AppRecyclerView rvAddresses;

    @BindView(R.id.tvBalance)
    public TextView tvBalance;

    @BindView(R.id.tvDetails)
    public TextView tvDetails;

    @BindView(R.id.tvRemove)
    public TextView tvRemove;

    @BindView(R.id.tvBalanceInCurrency)
    public TextView tvBalanceInCurrency;

    @BindView(R.id.abName)
    public TextView abName;

    @BindView(R.id.rlProgress)
    public SmoothProgressBar rlProgress;

    @BindView(R.id.emptyView)
    public LinearLayout emptyView;

    @BindView(R.id.abBack)
    protected ImageView abBack;

    @BindView(R.id.swiperefresh)
    public SwipeRefreshLayout mPullToRefreshView;

    private DetailsAdapter adapter;

    private String address;
    private String cur;
    private float balance;

    public int offsetIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setStatusBarColor(this);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        address = getIntent().getStringExtra(ADDRESS);
        cur = getIntent().getStringExtra(CUR);
        balance = getIntent().getFloatExtra(BALANCE, 0f);

        init();
    }

    private void init() {
        Utility.setDrawableColor(abBack, App.getContext().getResources().getColor(R.color.white));

        abName.setText(App.getContext().getResources().getString(R.string.address) + ":\n" + address);
        tvBalance.setText(Utility.getDoubleStringFormatNoSign(balance)+ " " + Constants.CURRENCY);
        float curRate = App.getCurrentUser().getRate();
        tvBalanceInCurrency.setText(Utility.getDoubleStringFormatForCurrency(curRate * balance) + " " + App.getCurrentUser().getRateName());

        LinearLayoutManager quickLinkLayoutManager = new LinearLayoutManager(rvAddresses.getContext(), LinearLayoutManager.VERTICAL, false);
        rvAddresses.setLayoutManager(quickLinkLayoutManager);
        rvAddresses.setHasFixedSize(true);
        rvAddresses.setEmptyView(emptyView);

        adapter = new DetailsAdapter(address);
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
                    getTransactions(null, offsetIndex);
                }
            }
        };
        rvAddresses.addOnScrollListener(mScrollListener);


        mPullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                offsetIndex = 0;

                getTransactions(new OnFinishCallback() {
                    @Override
                    public void onFinished() {
                        mPullToRefreshView.setRefreshing(false);
                    }
                }, offsetIndex);
            }
        });
        mPullToRefreshView.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        getTransactions(null, offsetIndex);
    }

    private void getTransactions(final OnFinishCallback callback, final int offset){
        if (offset == 0) {
            showProgress(true);
        }

        if (offset != 0) {
            adapter.showFooter();
        }

        Call<GetTxsResponse> getTxsRequestCall = App.getRestClient().getApiService().
                getTxs(new GetTxsRequest(App.getCurrentUser().accessToken, address, offset, offset + Constants.OFFSET));

        getTxsRequestCall.enqueue(new Callback<GetTxsResponse>() {
            @Override
            public void onResponse(Call<GetTxsResponse> call, Response<GetTxsResponse> response) {
                if (response.isSuccessful()) {

                    GetTxsResponse responseBody = response.body();
                    if (responseBody.success) {

                        List<GetTxsResponse.Result> results = responseBody.result;
                        if (offset == 0) {
                            adapter.setItems(results);
                        } else {
                            adapter.addItems(results);
                            adapter.hideFooter();
                        }
                    } else {
                        Toasty.error(AddressDetailActivity.this, responseBody.error_msg, Toast.LENGTH_LONG, true).show();
                    }

                    showProgress(false);
                } else {
                    Toasty.error(AddressDetailActivity.this, response.message(), Toast.LENGTH_LONG, true).show();
                    showProgress(false);
                }

                if (callback != null) {
                    callback.onFinished();
                }
            }

            @Override
            public void onFailure(Call<GetTxsResponse> call, Throwable t) {
                Toasty.error(AddressDetailActivity.this, t.toString(), Toast.LENGTH_LONG, true).show();
                showProgress(false);

                if (callback != null) {
                    callback.onFinished();
                }
            }
        });
    }

    private void showProgress(boolean progressShown) {
        if (progressShown) {
            rlProgress.setVisibility(View.VISIBLE);
        } else {
            rlProgress.setVisibility(View.GONE);
        }
    }


    @OnClick(R.id.llDetails)
    public void ontvDetailsCLicked(){
        Intent intent = new Intent(this, AddressKeyDetailsActivity.class);
        intent.putExtra(AddressDetailActivity.ADDRESS, address);
        intent.putExtra(AddressDetailActivity.BALANCE, balance);
        intent.putExtra(AddressDetailActivity.CUR, cur);

        startActivity(intent);
    }

    @OnClick(R.id.llRemove)
    public void ontvRemoveCLicked(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_address)
                .setMessage(R.string.remove_address_descr)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        showProgress(true);

                        Call<RemoveResponse> call = App.getRestClient().getApiService().
                                removeAddress(new RemoveRequest(App.getCurrentUser().accessToken, address));

                        call.enqueue(new Callback<RemoveResponse>() {
                            @Override
                            public void onResponse(Call<RemoveResponse> call, Response<RemoveResponse> response) {
                                if (response.isSuccessful()) {

                                    RemoveResponse responseBody = response.body();
                                    if (responseBody.success && responseBody.result) {
                                        setResult(RESULT_OK);
                                        finish();
                                    } else {
                                        Toasty.error(AddressDetailActivity.this, responseBody.error_msg, Toast.LENGTH_LONG, true).show();
                                    }

                                    showProgress(false);
                                } else {
                                    Toasty.error(AddressDetailActivity.this, response.message(), Toast.LENGTH_LONG, true).show();
                                    showProgress(false);
                                }
                            }

                            @Override
                            public void onFailure(Call<RemoveResponse> call, Throwable t) {
                                Toasty.error(AddressDetailActivity.this, t.toString(), Toast.LENGTH_LONG, true).show();
                                showProgress(false);
                            }
                        });
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }


    @OnClick(R.id.abBack)
    public void onBackClick(){
        onBackPressed();
    }
}
