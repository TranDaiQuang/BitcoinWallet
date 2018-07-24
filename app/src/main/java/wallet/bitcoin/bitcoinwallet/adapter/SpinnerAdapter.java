package wallet.bitcoin.bitcoinwallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import wallet.bitcoin.bitcoinwallet.R;
import wallet.bitcoin.bitcoinwallet.helper.App;
import wallet.bitcoin.bitcoinwallet.helper.UIHelper;
import wallet.bitcoin.bitcoinwallet.helper.Utility;
import wallet.bitcoin.bitcoinwallet.model.WalletAddress;

public class SpinnerAdapter extends ArrayAdapter<WalletAddress> {

    private static final int textSize = 12;
    private static final int padding = UIHelper.getPixel(8);

    private Context context;
    private List<WalletAddress> contents;
    private boolean isSend;
    private float balance;

    public SpinnerAdapter(boolean isSend, float balance, Context context, int textViewResourceId, List<WalletAddress> values) {
        super(context, textViewResourceId, values);
        this.isSend = isSend;
        this.balance = balance;

        this.context = context;
        this.contents = values;
    }

    @Override
    public int getCount() {
        if (isSend && contents == null){
            return 1;
        }

        if (!isSend && contents == null) {
            return 0;
        }

        return isSend ? 1 + contents.size() : contents.size();
    }

    @Override
    public WalletAddress getItem(int position) {
        return contents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        String text;
        if (isSend) {
            position -= 1;
        }

        if (position < 0){
            text = getTotalWalletString(balance).toString();
        } else {
            text = getWalletString(contents.get(position)).toString();
        }

        TextView txt = new TextView(context);
        txt.setPadding(padding, padding, padding, padding);
        txt.setTextSize(textSize);
        txt.setBackgroundColor(Color.WHITE);
        txt.setTextColor(App.getContext().getResources().getColor(R.color.text_color));

        if (isSend && position < 0){
            txt.setTypeface(txt.getTypeface(), Typeface.BOLD);
        }

        txt.setText(text);
        return txt;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewgroup) {
        String text;
        if (isSend) {
            position -= 1;
        }

        if (position < 0){
            text = getTotalWalletString(balance).toString();
        } else {
            text = getWalletString(contents.get(position)).toString();
        }


        TextView txt = new TextView(context);
        txt.setGravity(Gravity.CENTER);
        if (isSend){
            txt.setBackgroundColor(Color.WHITE);
            txt.setTextColor(App.getContext().getResources().getColor(R.color.text_color));
            txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_36dp, 0);
        } else {
            txt.setBackgroundColor(App.getContext().getResources().getColor(R.color.action_bar_color));
            txt.setTextColor(Color.WHITE);
            txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_36dp, 0);
        }

        if (isSend && position < 0 || !isSend){
            txt.setTypeface(txt.getTypeface(), Typeface.BOLD);
        }

        txt.setPadding(padding, padding, padding, padding);
        txt.setTextSize(textSize);

        txt.setText(text);
        return txt;
    }

    private StringBuilder getWalletString(WalletAddress preset){
        StringBuilder poolAppearance = new StringBuilder()
                .append(preset.id)
                .append("\n")
                .append(App.getContext().getResources().getString(R.string.balance))
                .append(": ")
                .append(Utility.getDoubleStringFormatNoSign(preset.balance));

        return poolAppearance;
    }

    private StringBuilder getTotalWalletString(float balance){
        StringBuilder poolAppearance = new StringBuilder()
                .append(App.getContext().getResources().getString(R.string.send_from_wallet))
                .append("\n")
                .append(App.getContext().getResources().getString(R.string.balance))
                .append(": ")
                .append(Utility.getDoubleStringFormatNoSign(balance));

        return poolAppearance;
    }
}