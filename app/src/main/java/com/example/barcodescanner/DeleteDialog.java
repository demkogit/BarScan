package com.example.barcodescanner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

public class DeleteDialog extends AppCompatDialogFragment {

    private IDeleteItem datable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        datable = (IDeleteItem) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int position = getArguments().getInt("position");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Удалить выбранный товар?");

        builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                datable.deleteItem(position);
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    public interface IDeleteItem {
        void deleteItem(int position);
    }
}

