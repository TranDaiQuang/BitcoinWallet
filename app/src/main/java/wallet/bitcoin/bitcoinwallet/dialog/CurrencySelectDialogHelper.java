package wallet.bitcoin.bitcoinwallet.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import wallet.bitcoin.bitcoinwallet.R;
import wallet.bitcoin.bitcoinwallet.helper.App;
import wallet.bitcoin.bitcoinwallet.helper.Utility;
import wallet.bitcoin.bitcoinwallet.model.User;

public class CurrencySelectDialogHelper extends BaseDialogHelper {

    @BindView(R.id.cbUSD)
    public CheckBox cbUSD;

    @BindView(R.id.cbEUR)
    public CheckBox cbEUR;

    @BindView(R.id.cbRUB)
    public CheckBox cbRUB;

    private CurrencyDialogCallback callback;

    public interface CurrencyDialogCallback {
        void onSelectedCurrency(int currencyCode);
    }

    public static void show(Context context, CurrencyDialogCallback onPrivateKeyImported) {
        new CurrencySelectDialogHelper(context, onPrivateKeyImported);
    }

    private CurrencySelectDialogHelper(Context context, CurrencyDialogCallback callback) {
        super(context);
        this.callback = callback;

        init();
    }

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.currency_select, null);
        ButterKnife.bind(this, view);

        return view;
    }

    private void init() {
        cbUSD.setButtonDrawable(Utility.getCheckBoxDrawable());
        cbEUR.setButtonDrawable(Utility.getCheckBoxDrawable());
        cbRUB.setButtonDrawable(Utility.getCheckBoxDrawable());

        switch (App.getCurrentUser().localCurrency){
            case User.USD_CURRENCY:
                cbUSD.setChecked(true);
                break;
            case User.EUR_CURRENCY:
                cbEUR.setChecked(true);
                break;
            case User.RUR_CURRENCY:
                cbRUB.setChecked(true);
                break;
        }

        cbUSD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbUSD.isChecked()) {
                    callback.onSelectedCurrency(User.USD_CURRENCY);
                }
                dismiss();
            }
        });

        cbEUR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbEUR.isChecked()) {
                    callback.onSelectedCurrency(User.EUR_CURRENCY);
                }
                dismiss();
            }
        });

        cbRUB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbRUB.isChecked()) {
                    callback.onSelectedCurrency(User.RUR_CURRENCY);
                }
                dismiss();
            }
        });
    }

}

