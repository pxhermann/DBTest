package cz.hermann.android.DBTest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.TextView;

public class DlgAbout extends AlertDialog {

	public DlgAbout(Context context) 
	{
		super(context, android.R.style.Theme_Dialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		
		String s = getContext().getString(R.string.app_name);
	    try 
	    {
	    	PackageInfo pi = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
	    	s = String.format("%s, v %s", s, pi.versionName);
	    }
	    catch (Exception e) 
	    {
	    }
	    ((TextView)findViewById(R.id.tvTitle)).setText(s);
	    
//		getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}
}
