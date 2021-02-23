package cz.hermann.android.DBTest;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView.OnEditorActionListener;

public class DlgInsertSpeed extends Dialog
{
    public interface OKListener {
        void onOK(int recCnt, int runType, boolean useTrn);
    }
	
	public static final int TYPE_SQL = 0;
	public static final int TYPE_DB = 1;
	public static final int TYPE_HELPER = 2;

	private static int recCnt = 100;
	private static int selType = TYPE_SQL;
	private static boolean runInTrn = false;
	
//*************************************************
	private EditText etRecCnt;
	private RadioButton rbDB;
	private RadioButton rbHelper;
    private CheckBox chbInTrn;
	
	private OKListener okListener;
	
	public DlgInsertSpeed(Context context, OKListener listener) 
	{
		super(context);
		
		okListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.insertspeed);

		setTitle("Insert speed test");
		setCancelable(true);
		
		etRecCnt = (EditText)findViewById(R.id.etRecCnt);
		rbDB     = (RadioButton)findViewById(R.id.rbDB);
		rbHelper = (RadioButton)findViewById(R.id.rbHelper);
		chbInTrn = (CheckBox)findViewById(R.id.chbInTrn);
		
		// restore saved values
		if ( recCnt < 1 )
			recCnt = 100;
		etRecCnt.setText(String.format("%d", recCnt));
		switch ( selType )
		{
		case TYPE_DB: 	  rbDB.setChecked(true); break;
		case TYPE_HELPER: rbHelper.setChecked(true); break;
		default:		  ((RadioButton)findViewById(R.id.rbSql)).setChecked(true); break;
		}
		chbInTrn.setChecked(runInTrn);
		
		// set listeners
		findViewById(R.id.btnOK).setOnClickListener(onBtnClick);
		findViewById(R.id.btnCancel).setOnClickListener(onBtnClick);
	}
	private View.OnClickListener onBtnClick = new View.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			switch( v.getId() )
			{
			case R.id.btnCancel:
				dismiss();
				break;
			case R.id.btnOK:
				saveAndFinish();
				break;
			}
		}
	};
	private void saveAndFinish()
	{
		// check count
		try { recCnt = Integer.parseInt(etRecCnt.getText().toString()); }
		catch(Exception ex) { recCnt = 0; }
		
		if ( recCnt < 1 )
		{
			GM.showError(DlgInsertSpeed.this.getContext(), "Enter positive number of records to add!");
			return;
		}
		// check type
		selType = TYPE_SQL;
		if ( rbDB.isChecked() )	selType = TYPE_DB;
		else if ( rbHelper.isChecked() ) selType = TYPE_HELPER;
		
		runInTrn = chbInTrn.isChecked();
		
		// invoke listener 
		if ( okListener != null )
			okListener.onOK(recCnt, selType, runInTrn);
		dismiss();
	}
}
