package com.performans.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import java.util.ArrayList;
import java.util.Arrays;

public class DialogUtils {

    public static AlertDialog getDialogProgressBar(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.str_please_wait);
        final ProgressBar progressBar = new ProgressBar(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(30, 30,30,30);
        progressBar.setLayoutParams(lp);
        builder.setView(progressBar);
        return builder.create();
    }

    public static void showConfirm(Context context, String title, String message, DialogInterface.OnClickListener okHandler) {
        showDialog(context,
                title,
                message,
                context.getResources().getString(android.R.string.ok),
                okHandler,
                context.getResources().getString(android.R.string.cancel),
                null);
    }



    public static void showAlert(Context context, String title, String message, DialogInterface.OnClickListener okHandler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, okHandler);
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public static void showDialog(Context context, String title, String message,
                                  String positiveLabel,
                                  DialogInterface.OnClickListener positiveHandler,
                                  String negativeLabel,
                                  DialogInterface.OnClickListener negativeHandler) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveLabel, positiveHandler);
        builder.setNegativeButton(negativeLabel, negativeHandler);
        AlertDialog dialog = builder.create();
        dialog.show();
    }








    public static ProgressDialog showProgressDialog(Activity activity) {
        return showProgressDialog(activity, activity.getResources().getString(R.string.str_loading));
    }

    public static ProgressDialog showProgressDialog(Activity activity, String message) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(android.R.style.Theme_Holo_Dialog);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        return progressDialog;
    }

    public static void hideProgressDialog(ProgressDialog progressDialog){
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }







    public static AlertDialog showAlertDialog(Context context, String message) {
        return showAlertDialog(context, null, message);
    }

    public static AlertDialog showAlertDialog(Context context, String title, String message) {
        return showAlertDialog(context, title, message, -1);
    }

    public static AlertDialog showAlertDialog(Context context, String message, int icon) {
        return showAlertDialog(context, null, message, icon);
    }

    public static AlertDialog showAlertDialog(Context context, String title, String message, int icon) {
        return showAlertDialog(context, title, message, icon, null, false);
    }

    public static AlertDialog showAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener onOkClicked) {
        return showAlertDialog(context, title, message, -1, new DialogButton[]{new DialogButton("OK", DialogButton.ButtonTypes.POSITIVE, onOkClicked)}, false);
    }

    public static AlertDialog showAlertDialog(final Context context, String title, String message, int icon, DialogButton[] buttons, boolean buttonsAsItems) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        if (buttonsAsItems && buttons != null && buttons.length > 0) {
            ArrayList<String> items = new ArrayList<>();

            for (DialogButton button : buttons) {
                items.add(button.getText());
            }

            String[] aItems = items.toArray(new String[items.size()]);

            final ArrayList<DialogButton> dButtons = new ArrayList<DialogButton>(Arrays.asList(buttons));

            dialogBuilder.setItems(aItems, (dialog, which) -> dButtons.get(which).getOnClickListener().onClick(dialog, which));
        } else {
            if (buttons == null || buttons.length == 0) {
                buttons = new DialogButton[]{new DialogButton("OK")};
            }

            for (DialogButton button : buttons) {
                switch (button.getButtonType()) {
                    case NEUTRAL:
                        dialogBuilder.setNeutralButton(button.getText(), button.getOnClickListener());
                        break;
                    case NEGATIVE:
                        dialogBuilder.setNegativeButton(button.getText(), button.getOnClickListener());
                        break;
                    case POSITIVE:
                        dialogBuilder.setPositiveButton(button.getText(), button.getOnClickListener());
                        break;
                }
            }
        }


        if (title != null && !title.equals("")) dialogBuilder.setTitle(title);
        if (message != null && !message.equals("")) dialogBuilder.setMessage(message);
        if (icon != -1) dialogBuilder.setIcon(icon);

        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setOnShowListener(dialog -> {
            int[] buttonTypes = new int[]{Dialog.BUTTON_POSITIVE, Dialog.BUTTON_NEGATIVE, Dialog.BUTTON_NEUTRAL};

            for (int btnType : buttonTypes) {
                Button btn = alertDialog.getButton(btnType);

                if (btn != null) {
                    btn.setTextColor(alertDialog.getContext().getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        });

        alertDialog.show();

        return alertDialog;
    }
}
