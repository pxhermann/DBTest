package cz.hermann.android.DBTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public final class GM 
{
/*	public static char DecimalSeparator = '.';
    private static DecimalFormat currencyFormatter = (DecimalFormat)DecimalFormat.getInstance();
    static 
    {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		DecimalSeparator = dfs.getDecimalSeparator();
    	
		currencyFormatter.setDecimalFormatSymbols(dfs);
		currencyFormatter.setGroupingUsed(false);
		currencyFormatter.setDecimalSeparatorAlwaysShown(false);
		
//		currencyFormatter.setMaximumFractionDigits(maxDecDigits);
    }
    public static double parseQty(String strQty)
    {
    	BigDecimal bd = new BigDecimal(strQty);
    	bd.setScale()
    	
    }
    public static String formatQty(double qty)
    {
    	return currencyFormatter.format(qty);
    }
*/    
	public static void showError(Context context, Exception ex, String msg)
	{
		showError(context, String.format("%s\n\n%s", msg, ex.getMessage()) );
	}
	public static void showError(Context context, String msg)
	{
		showError(context, msg, null);
	}
	public static void showError(Context context, String msg, OnClickListener onOkClick)
	{
		(new AlertDialog.Builder(context))
			.setTitle(R.string.app_name)
			.setMessage(msg)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setNeutralButton("OK", onOkClick)
			.create()
			.show();
	}
	public static void showErrorAndFinish(Context context, Exception ex, String msg)
	{
		showErrorAndFinish(context, String.format("%s\n\n%s", msg, ex.getMessage()));
	}
	
	public static void showErrorAndFinish(Context context, String msg)
	{
		final Activity act = ((Activity)context); 
		OnClickListener onFinish = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { act.finish(); }
			};	

		showError(context, msg, onFinish);
	}
	
	public static void showInfo(Context context, String msg)
	{
		(new AlertDialog.Builder(context))
			.setTitle(R.string.app_name)
			.setMessage(msg)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setNeutralButton("OK", null)
			.create().show();
	}
	public static void showQuestion(Context context, String msg, OnClickListener onYesClick, OnClickListener onNoClick)
	{
		(new AlertDialog.Builder(context))
			.setTitle(R.string.app_name)
			.setMessage(msg)
//			.setIcon(android.R.drawable.ic_menu_help)
			.setPositiveButton("Yes", onYesClick)
			.setNegativeButton("No", onNoClick)
			.create()
			.show();
	}
	
}
