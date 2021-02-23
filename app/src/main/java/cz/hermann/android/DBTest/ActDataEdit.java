package cz.hermann.android.DBTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ActDataEdit extends Activity 
{
	public static final String EXTRA_ID = "id";
	
	private boolean bModified = false;
	private EditText etShort, etName, etNote, etPrice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		if ( !DBase.isOpen() )
		{
			GM.showErrorAndFinish(this, "Aplication was closed by system and will be restarted!");
			return;
		}
		
		setContentView(R.layout.act_dataedit);
		
		etShort = (EditText)findViewById(R.id.etShort);
		etName  = (EditText)findViewById(R.id.etName);
		etNote = (EditText)findViewById(R.id.etNote);
		etPrice = (EditText)findViewById(R.id.etPrice);
		
		findViewById(R.id.btnOK).setOnClickListener(onBtnClick);
		findViewById(R.id.btnCancel).setOnClickListener(onBtnClick);
		
		Cursor set = null;
        try
        {
        	String s = String.format("SELECT P.*, PP.price FROM Product AS P"+
					 " LEFT OUTER JOIN ProdPrice AS PP ON (P._id = PP.prod_id)"+
 					 " WHERE P._id = %d",
 					 getIntent().getLongExtra(EXTRA_ID, 0)); 
        	set = DBase.db.rawQuery(s, null);
        	if ( set.getCount() > 0  )
        	{
        		set.moveToFirst();
       			etShort.setText(set.getString(set.getColumnIndex("short")));
				etName.setText(set.getString(set.getColumnIndex("name")));
				etNote.setText(set.getString(set.getColumnIndex("note")));
				etPrice.setText(set.getString(set.getColumnIndex("price")));
        	}
		}
		catch(Exception ex) { GM.showError(getApplicationContext(), ex, "Error occured when loading product data!"); }
		finally { try { if ( set != null && !set.isClosed() ) set.close(); } catch (Exception e) {} }
		
		etShort.addTextChangedListener(onTextModified); etShort.setOnEditorActionListener(onEnterPress);	// .setOnKeyListener(onKeyPress);
		etName.addTextChangedListener(onTextModified);  etName.setOnEditorActionListener(onEnterPress);	// .setOnKeyListener(onKeyPress);
		etNote.addTextChangedListener(onTextModified);  etNote.setOnEditorActionListener(onEnterPress);	// .setOnKeyListener(onKeyPress);
		etPrice.addTextChangedListener(onTextModified); etPrice.setOnEditorActionListener(onEnterPress);	// .setOnKeyListener(onKeyPress);
	}
	@Override
	public void onBackPressed() 
	{
		onFinish(false);
	}
	
    private TextWatcher onTextModified = new TextWatcher()
    {
    	public void afterTextChanged(android.text.Editable arg0) {};
    	public void beforeTextChanged(CharSequence s, int start, int count, int after) {};
    	public void onTextChanged(CharSequence s, int start, int before, int count) {bModified = true;};
    };
	private OnEditorActionListener onEnterPress = new OnEditorActionListener()
	{
		public boolean onEditorAction(android.widget.TextView c, int actionID, android.view.KeyEvent event) 
		{
			if ( event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN )
			{
				onFinish(true);
				return true;
			}
			return false;
		}
	};
	private OnClickListener onBtnClick = new OnClickListener()
	{
		public void onClick(android.view.View view)
		{
			switch ( view.getId() )
			{
			case R.id.btnOK:
				onFinish(true);
				break;
			case R.id.btnCancel:
				onFinish(false);
				break;
			}
		}
	};
	private void onFinish(boolean resultOk)
	{
		if ( resultOk )
		{
			Intent i = getIntent();
			
			Float price = 0F;
			String name  = etName.getText().toString();
			if ( name == null || name.length() < 1)
			{
				GM.showError(this, "Enter name!");
				return;
			}
			try { price = Float.parseFloat(etPrice.getText().toString()); }
			catch(Exception ex)
			{
				GM.showError(this, "Enter price!");
				return;
			}

	        try
	        {
	        	long prodID = getIntent().getLongExtra(EXTRA_ID, 0);
	        	
	        	////
				ContentValues cv = new ContentValues();
				cv.put("name", name);
				cv.put("short", etShort.getText().toString());
				cv.put("note", etNote.getText().toString());
				
				if ( DBase.db.update("Product", cv, "_id=?", new String[] {Long.toString(prodID)}) < 1 )
					DBase.db.insert("Product", null, cv);
					
				////
				cv.clear();
				cv.put("price", price);
				if ( DBase.db.update("ProdPrice", cv, "prod_id=?", new String[] {Long.toString(prodID)}) < 1 )
				{
					cv.put("prod_id", prodID);
					DBase.db.insert("ProdPrice", null, cv);
				}
	        	
				setResult(RESULT_OK, i);
				finish();
			}
			catch(Exception ex) { GM.showError(this, ex, "Saving to database failed!"); }
		}
		else if ( bModified )
			GM.showQuestion(this, "Close and cancel changes?", new DialogInterface.OnClickListener() { 
					public void onClick(DialogInterface dialog, int which) {
						setResult(RESULT_CANCELED);
						finish(); } }, null);
		else
			finish();
	}
}
