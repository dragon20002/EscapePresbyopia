package kr.or.hanium.chungbukhansung.escapepresbyopia.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Objects;

public class EPDialog extends DialogFragment {
    public interface DialogButtonListener {
        void onDialogPositive();

        void onDialogNegative();
    }

    private DialogButtonListener listener;
    private String message;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositive();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegative();
                    }
                });
        return builder.create();
    }

    public EPDialog setListener(DialogButtonListener listener) {
        this.listener = listener;
        return this;
    }

    public EPDialog setMessage(String message) {
        this.message = message;
        return this;
    }

}
