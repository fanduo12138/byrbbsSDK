package com.bupt.forum.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;


public class ConfirmDialog extends AlertDialog
{
    protected ConfirmDialog(Context context)
    {
        super(context);
    }

    protected ConfirmDialog(Context context, int theme)
    {
        super(context, theme);
    }

    protected ConfirmDialog(Context context, boolean cancelable, OnCancelListener cancelListener)
    {
        super(context, cancelable, cancelListener);
    }
}
