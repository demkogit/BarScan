package com.example.barcodescanner;

import android.app.Activity;
import android.content.Context;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.w3c.dom.Text;

import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder>  {

    private Context mContext;
    private List<ProductItem> productItemList;
    private OnItemClickListener onItemClickListener;
    private Activity mActivity;
    private TextView mTextView;
    private ProductItem currentProduct;
    private EditText mEditText;

    public ProductAdapter(final Context mContext, List<ProductItem> productItemList,
                          OnItemClickListener onItemClickListener, final Activity  mActivity) {
        this.mContext = mContext;
        this.productItemList = productItemList;
        this.onItemClickListener = onItemClickListener;
        this.mActivity = mActivity;

    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = (View) layoutInflater.inflate(R.layout.list_layout, null);
        ProductViewHolder holder = new ProductViewHolder(view, onItemClickListener);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ProductViewHolder productViewHolder, int i) {
        final ProductItem productItem = productItemList.get(i);

        productViewHolder.editText.setText(productItem.getCount());
        productViewHolder.textViewName.setText(productItem.getName());
        productViewHolder.textViewParty.setText(productItem.getParty());
        productViewHolder.textViewCode.setText(productItem.getCode());


        KeyboardVisibilityEvent.setEventListener(mActivity, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if(!isOpen) {
                    productViewHolder.editText.clearFocus();
                    productItem.setCount(productViewHolder.editText.getText().toString());
                }
            }
        });

        productViewHolder.editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    productItem.setCount(v.getText().toString());
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    productViewHolder.editText.clearFocus();
                    return true; // consume.

                }

                Toast.makeText(mContext, "Bad(", Toast.LENGTH_SHORT).show();
                return false; // pass on to other listeners
            }

        });

    }


    @Override
    public int getItemCount() {
        return productItemList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        EditText editText;
        TextView textViewName, textViewParty, textViewCode;
        OnItemClickListener listener;

        public ProductViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            editText = itemView.findViewById(R.id.itemEditText);
            textViewName = itemView.findViewById(R.id.itemName);
            textViewParty = itemView.findViewById(R.id.itemParty);
            textViewCode = itemView.findViewById(R.id.itemCode);
            this.listener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(getAdapterPosition());
        }
    }
}
