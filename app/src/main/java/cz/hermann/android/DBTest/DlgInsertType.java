package cz.hermann.android.DBTest;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView.OnEditorActionListener;

public class DlgInsertType extends Dialog 
{
    public interface OKListener {
        void onOK(int recCnt, int valueType);
    }
	
	public static final int TYPE_INT = 0;
	public static final int TYPE_LONG = 1;
	public static final int TYPE_DOUBLE = 2;
	public static final int TYPE_TEXT = 3;

	private static int recCnt = 1000;
	private static int selType = TYPE_INT;

	//*************************************************
	private EditText etRecCnt;
	private RadioButton rbLong;
	private RadioButton rbDouble;
	private RadioButton rbText;
	
	private OKListener okListener;
	
	public DlgInsertType(Context context, OKListener listener) 
	{
		super(context); 
		
		okListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.inserttype);
		
		setTitle("Insert data type test");
		setCancelable(true);
		
		etRecCnt = (EditText)findViewById(R.id.etRecCnt);
		rbLong   = (RadioButton)findViewById(R.id.rbLong);
		rbDouble = (RadioButton)findViewById(R.id.rbDouble);
		rbText   = (RadioButton)findViewById(R.id.rbText);
		
		// restore saved values
		if ( recCnt < 1 )
			recCnt = 100;
		etRecCnt.setText(String.format("%d", recCnt));
		switch ( selType )
		{
		case TYPE_LONG:   rbLong.setChecked(true); break;
		case TYPE_DOUBLE: rbDouble.setChecked(true); break;
		case TYPE_TEXT:   rbText.setChecked(true); break;
		default:		  ((RadioButton)findViewById(R.id.rbInt)).setChecked(true); break;
		}
		
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
			GM.showError(getContext(), "Enter positive number of records to add!");
			return;
		}
		// check type
		selType = TYPE_INT;
		if ( rbLong.isChecked() )		 selType = TYPE_LONG;
		else if ( rbDouble.isChecked() ) selType = TYPE_DOUBLE;
		else if ( rbText.isChecked() ) 	 selType = TYPE_TEXT;
		
		// invoke listener 
		if ( okListener != null )
			okListener.onOK(recCnt, selType);
		
		dismiss();
	}
}
