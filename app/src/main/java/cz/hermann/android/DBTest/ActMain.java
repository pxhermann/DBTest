package cz.hermann.android.DBTest;

import java.io.File;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ActMain extends ListActivity {
	private final int DLG_ABOUT = 1;
	private static int sortColID = R.id.col1;
	private static boolean sortAsc = true;
	private TextView tvColName;
	private TextView tvColPrice;
	
//*************************************************************************
// overrides 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
	        setContentView(R.layout.act_main);
			
	        findViewById(R.id.btnInsertSpeed).setOnClickListener(onBtnClick);
	        findViewById(R.id.btnInsertType).setOnClickListener(onBtnClick);
	        findViewById(R.id.btnDelete).setOnClickListener(onBtnClick);
	        findViewById(R.id.btnSqlTest).setOnClickListener(onBtnClick);
	        findViewById(R.id.btnDbInfo).setOnClickListener(onBtnClick);
	        tvColName = ((TextView)findViewById(R.id.col1)); tvColName.setBackgroundColor(Color.LTGRAY);  tvColName.setOnClickListener(onBtnClick);
	        tvColPrice= ((TextView)findViewById(R.id.col2)); tvColPrice.setBackgroundColor(Color.LTGRAY); tvColPrice.setOnClickListener(onBtnClick);
	         
	        updateColNames(); 
	        
	        registerForContextMenu(getListView()); // same as getListView().setOnCreateContextMenuListener(this); or getListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {menu.setHeaderTitle("ContextMenu");menu.add(0, R.id.miDel, 1, "Delete this item!");}});
	        
	        // ensure openned database
			if ( !DBase.isOpen() )
				DBase.open(this);  
			// open cursor
			updateList();
        }
        catch(Exception ex)
        {
        	GM.showError(this, ex, "Initialization failed!");
        }
	}
    @Override
    protected void onResume()
	{
    	super.onResume();

    	try
    	{
			if ( !DBase.isOpen() )	// this can happen if activity was paused for a 'long' time and system killed it 
			{
				DBase.open(this);  
				updateList();
			}
	    }
	    catch(Exception ex)
	    {
	    	GM.showError(this, ex, "Error occured while reopenning database!");
	    }
    }
	@Override
	protected void onDestroy() {
		super.onDestroy();

		// cursor is managed (see. startManagingCursor) so I don't have to close it explicitelly
		
		// close database
    	try { DBase.close(); }
    	catch(Exception ex)
		{
			GM.showError(this, ex, "Closing database failed!");
		}
    }
	@Override
	public void onBackPressed() 
	{
		finish();
	}
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
 
		final long prodID = id;

		DlgNumPad.OKListener onQtyEntered = new DlgNumPad.OKListener()
		{
			@Override
			public void onOK(double qty) 
			{
		        try
		        {
					ContentValues cv = new ContentValues();
					cv.put("price", qty);
					if ( DBase.db.update("ProdPrice", cv, "prod_id=?", new String[] {Long.toString(prodID)}) < 1 )
					{
						cv.put("prod_id", prodID);
						DBase.db.insert("ProdPrice", null, cv);
					}
				}
				catch(Exception ex)
				{
					Toast.makeText(getApplicationContext(), "Updating price failed: "+ex.getMessage(), Toast.LENGTH_LONG).show();
				}
				updateList();
			}
		};
   	
    	DlgNumPad dlg = new DlgNumPad(this, onQtyEntered);
    	Cursor set = null;
		try
		{
			String s = "SELECT P.name, PP.price FROM Product AS P"+
		 			   " LEFT OUTER JOIN ProdPrice AS PP ON (P._id = PP.prod_id)"+
		 			   " WHERE P._id="+id; 
        	set = DBase.db.rawQuery(s, null);
        	if ( set.getCount() > 0  )
        	{
        		set.moveToFirst();
        		dlg.name = set.getString(0);
        		dlg.qty  = set.getDouble(1);
        	}	    	
		}
		catch(Exception ex) 
		{
			GM.showError(this, ex, "Error occured when reading data!");
			return;
		}
		finally { try { if ( set != null && !set.isClosed() ) set.close(); } catch (Exception e) {} }
    	
    	dlg.show();
    }
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog retVal = null;
    	switch ( id )
    	{
    	case DLG_ABOUT: retVal = new DlgAbout(this); break;
    	}
    	if ( retVal == null )
    		retVal = super.onCreateDialog(id);
    	return retVal;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.mnuExit:
			finish();
			break;
		case R.id.mnuAbout:
			showDialog(DLG_ABOUT);
			break;
		default:
			return false;
		}

		return true;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) 
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.listitem, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo mi = (AdapterContextMenuInfo)item.getMenuInfo();
        
    	if ( mi.position >=0 && mi.position < getListView().getCount() )
			try
			{
				long prodID = mi.id; // == getListAdapter().getItemId(mi.position); // returns value in table column '_id' - standard android presumtion, that table contains _id column
									 // == ((Cursor)(getListView().getItemAtPosition(mi.position))).c.getLong(c.getColumnIndex("_id"));
		        switch ( item.getItemId() )
		        {
		        case R.id.miDel:
					DBase.db.execSQL(String.format("DELETE FROM ProdPrice WHERE prod_id = %d", prodID));
					DBase.db.execSQL(String.format("DELETE FROM Product WHERE _id = %d", prodID));
					updateList();
		        	return true;
				case R.id.miEdit:
					Intent i = new Intent(getApplicationContext(), ActDataEdit.class);
					i.putExtra(ActDataEdit.EXTRA_ID, prodID);
					startActivity(i);	// startActivityForResult(i, REQ_EDITDATA); ~ as managed cursor is used, its requery is called automatically in onResume, e.i. after closing this activity, so I don't have to ensure it explicitelly
					return true;
		        }
	        }
			catch(Exception ex)
			{
				GM.showError(ActMain.this, ex, "Error occured when editing record!");
				return false;
			}

    	return super.onContextItemSelected(item);
    }
    
 	private OnClickListener onBtnClick = new OnClickListener() 
	{
		public void onClick(View v)
		{
			switch (v.getId()) 
			{
			case R.id.btnInsertSpeed:
			{
				DlgInsertSpeed.OKListener onOK = new DlgInsertSpeed.OKListener() {
					public void onOK(int recCnt, int runType, boolean useTrn) {
						int nextNo = 1;
						Cursor set = null;
						try
						{
							set = DBase.db.rawQuery("SELECT MAX(_id) FROM Product", null);
							if ( set.getCount()>0 )
							{
								set.moveToFirst();
								nextNo = set.getInt(0)+1;
							}
						}
						catch(Exception ex) { GM.showError(ActMain.this, ex, "Error occured when collecting data from database!"); }
						finally { try { if ( set != null && !set.isClosed() ) set.close(); } catch (Exception e) {} }
						
						new TaskInsertSpeed(recCnt).execute(nextNo, runType, useTrn?1:0);
					}
				};
				
				(new DlgInsertSpeed(ActMain.this, onOK)).show();
				break;
			}
			case R.id.btnInsertType:
			{
				DlgInsertType.OKListener onOK = new DlgInsertType.OKListener() {
					public void onOK(int recCnt, int valueType) {
						new TaskInsertType(recCnt, valueType).execute();
					}
				};
				
				(new DlgInsertType(ActMain.this, onOK)).show();
				break;
			}
			case R.id.btnDelete:
				try
				{
					DBase.db.execSQL("DELETE FROM Product WHERE _id > 3");
					DBase.db.execSQL("DELETE FROM ProdPrice WHERE prod_id > 3");
					DBase.db.execSQL("DELETE FROM TestTable");
//					DBase.db.execSQL("VACUUM"); // compact database
					
					updateList();
				}
				catch(Exception ex)
				{
					GM.showError(ActMain.this, ex, "Error occured when running deleting data!");
				}
				break;
			case R.id.btnDbInfo:
		        File f = getDatabasePath(DBase.DATABASE_NAME);
		        GM.showInfo(ActMain.this, "Database location:\n"+f.getAbsolutePath()+"\n\nIsWritable: "+f.canWrite()+"\nSize: "+f.length()+" [B]");
				break;
			case R.id.btnSqlTest:
				startActivity(new Intent(v.getContext(), ActSqlTest.class)); 				
				break;
			case R.id.col1:
			case R.id.col2:
				if ( sortColID == v.getId() )
					sortAsc = !sortAsc;
				else
				{
					sortColID = v.getId();
					sortAsc = true;
				}
				updateColNames();
				updateList();
				break;
			}
		}
	};
	
	private void updateColNames()
	{
		String s;
		s = "Name";
		if ( sortColID == tvColName.getId() )
			s += " "+(sortAsc?"▲":"▼");//(sortAsc?"↑":"↓");
		tvColName.setText(s);
		//
		s = "Price";
		if ( sortColID == tvColPrice.getId() )
			s += " "+(sortAsc?"▲":"▼");
		tvColPrice.setText(s);
	}
    private void updateList()
    {
		SimpleCursorAdapter a = (getListAdapter() instanceof SimpleCursorAdapter)?((SimpleCursorAdapter)getListAdapter()):null;
		Cursor set = (a==null)?null:a.getCursor();

		if ( set != null && !set.isClosed()  )
		{
			stopManagingCursor(set);
			set.close();
//			set.requery();
		}
	//	else 
		{
    		String s =  String.format("SELECT P._id, P.name, PP.price FROM Product AS P"+
    								  " LEFT OUTER JOIN ProdPrice AS PP ON (P._id = PP.prod_id)"+
    								  " ORDER BY %s %s",
    								  (sortColID==R.id.col1)?"name":"price",
    								  sortAsc?"ASC":"DESC");
    		set = DBase.db.rawQuery(s,null);
    		startManagingCursor(set);	// if this wouldn't be used, cursor should be handled in onPause(deactivate), onResume (requery) and onDestroy (close)
			if ( a == null )
			{
				String[] colData = new String[] { "name", "price" }; 
				int[] colUI = new int[] { R.id.col1, R.id.col2 };
		
				a = new SimpleCursorAdapter(this,R.layout.list_row, set, colData, colUI);
				a.setViewBinder(new NumberViewBinder(set.getColumnIndex("price")));
				setListAdapter(a);
			}
			else
				a.changeCursor(set);
		}
    }

    // helper class realizing asynchronous inserting records to database - used for testing speed of various insert ways
	private class TaskInsertSpeed extends AsyncTask <Integer, Integer, Void> 
	{
		private ProgressDialog dlgProg = new ProgressDialog(ActMain.this);
		private long duration = 0;
		private int recCnt = 100;
		private String errMsg = ""; 
		
		public TaskInsertSpeed(int recCnt)
		{
			if ( recCnt < 1 )
				recCnt = 100;
			this.recCnt = recCnt;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		
			dlgProg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) { cancel(true); }
			});
			dlgProg.setTitle(String.format("Adding %d rows", recCnt));
			dlgProg.show();
		}
		
	 	@Override
	 	protected Void doInBackground(Integer... params) {
			int nextNo 		= (params!=null && params.length>0)?params[0]:0;
			int insertType 	= (params!=null && params.length>1)?params[1]:0;
			boolean runInTrn= (params!=null && params.length>2)?(params[2]==1):false;
			
			int progressStep = Math.max(1, recCnt/100);
			long startTime = System.currentTimeMillis();
			try
			{
				if ( runInTrn )
					DBase.db.beginTransaction();	// DBase.db.execSQL("BEGIN TRANSACTION");	// DBase.db.execSQL("PRAGMA synchronous=OFF");
				
				switch ( insertType )
				{
				case DlgInsertSpeed.TYPE_HELPER:
					InsertHelper ih = new InsertHelper(DBase.db, "Product");
			        // Get the numeric indexes for each of the columns that we're updating
			        final int colShort = ih.getColumnIndex("short");
			        final int colName = ih.getColumnIndex("name");
					for(int i = 0; i<recCnt && !isCancelled(); i++, nextNo++)
					{
			            ih.prepareForInsert();
			            // Add the data for each column
			            ih.bind(colShort, String.format("Kat%04d", nextNo));
			            ih.bind(colName, String.format("Product %04d", nextNo));
			            // Insert the row into the database.
			            ih.execute();
			            
			            if ( i % progressStep == 0 )
			            	publishProgress(i+1);
					}
					break;
				case DlgInsertSpeed.TYPE_DB:
					ContentValues cv = new ContentValues();
					for(int i = 0; i<recCnt && !isCancelled(); i++, nextNo++)
					{
						cv.put("short", String.format("Kat%04d", nextNo));
						cv.put("name", String.format("Product %04d", nextNo));
						DBase.db.insert("Product", null, cv);
						
			            if ( i % progressStep == 0 )
			            	publishProgress(i+1);
					}
					break;
				default:
					String s;
					for(int i = 0; i<recCnt && !isCancelled(); i++, nextNo++)
					{
						s = String.format("INSERT INTO Product (short, name) VALUES ('kat%04d', 'Product %04d')", nextNo, nextNo);
						DBase.db.execSQL(s);
						
			            if ( i % progressStep == 0 )
			            	publishProgress(i+1);
					}
					break;
				}
				if ( runInTrn )
					DBase.db.setTransactionSuccessful();
			}
			catch(Exception ex)
			{
				errMsg = ex.getMessage();
			}
			finally
			{
				if ( runInTrn )
					DBase.db.endTransaction(); // DBase.db.execSQL("COMMIT");	// DBase.db.execSQL("PRAGMA synchronous=FULL");
			}
			
			duration = System.currentTimeMillis()-startTime;
			
			return null;
		}
	 	@Override
	 	protected void onPostExecute(Void result) {
	 		
	 		if ( dlgProg != null && dlgProg.isShowing() )
	 			dlgProg.dismiss();
	 		
	 		updateList();

	 		if ( errMsg.length() > 0 )
	 			GM.showError(ActMain.this, String.format("Error occured!\n%s", errMsg) );
	 		else
	 			GM.showInfo(ActMain.this, String.format("%d records added.\nDuration: %d [ms]", recCnt, duration));
	 	}

	 	@Override
	 	protected void onCancelled() {
	 		super.onCancelled();
	 		
	 		Toast.makeText(ActMain.this, "Adding records interrupted!", Toast.LENGTH_SHORT).show();

	 		if ( dlgProg != null && dlgProg.isShowing() )
	 			dlgProg.dismiss();
	 		
	 		updateList();
	 	}
	 	
	 	@Override
	 	protected void onProgressUpdate(Integer... values) {
	 		if ( dlgProg != null && values != null && values.length > 0 )
	 			dlgProg.setMessage(String.format("%d records added", values[0]));
	 		else
	 			super.onProgressUpdate(values);
	 	}
	}
	//---------------------------------------------
    // helper class realizing asynchronous inserting records to database - used for testing space requirements of different types 
	private class TaskInsertType extends AsyncTask <Void, Integer, Void> 
	{
		private ProgressDialog dlgProg = new ProgressDialog(ActMain.this);

		private int recCnt = 100;
		private int valueType = DlgInsertType.TYPE_INT;
		private int initDbSize = 0;
		private String errMsg = ""; 
		
		public TaskInsertType(int recCnt, int valueType)
		{
			if ( recCnt < 1 )
				recCnt = 100;
			
			this.recCnt = recCnt;
			this.valueType = valueType;
			
	        File f = getDatabasePath(DBase.DATABASE_NAME);
	        initDbSize = (int)f.length();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		
	        String strType = "INT";
			switch ( valueType )
			{
			case DlgInsertType.TYPE_LONG:   strType = "LONG"; break;
			case DlgInsertType.TYPE_DOUBLE: strType = "DOUBLE"; break;
			case DlgInsertType.TYPE_TEXT:   strType = "TEXT"; break;
			}
			
			dlgProg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) { cancel(true); }
			});
			
			dlgProg.setTitle(String.format("Adding %d %s values", recCnt, strType));
			dlgProg.show();
		}
		
	 	@Override
	 	protected Void doInBackground(Void... params) 
	 	{
			int progressStep = Math.max(1, recCnt/100);
			try
			{
				DBase.db.beginTransaction();	// DBase.db.execSQL("BEGIN TRANSACTION");	// DBase.db.execSQL("PRAGMA synchronous=OFF");
				
				InsertHelper ih = new InsertHelper(DBase.db, "TestTable");
		        // Get the numeric indexes for each of the columns that we're updating
				final int colInt   = ih.getColumnIndex("intValue");
				final int colLong  = ih.getColumnIndex("longValue");
				final int colDouble= ih.getColumnIndex("doubleValue");
				final int colText  = ih.getColumnIndex("textValue");
				final int intIncrement = 10000;
				final double dblIncrement = 1.0/intIncrement;
				for(int i = 1; i<=recCnt && !isCancelled(); i++)
				{
		            ih.prepareForInsert();
		            // add the data
					switch ( valueType )
					{
					case DlgInsertType.TYPE_LONG:   ih.bind(colLong, (long)(i+intIncrement)); break;
					case DlgInsertType.TYPE_DOUBLE: ih.bind(colDouble, ((double)i+dblIncrement)); break;
					case DlgInsertType.TYPE_TEXT:   ih.bind(colText, String.format("%f", i+dblIncrement)); break;
					default: ih.bind(colInt, i+intIncrement); break;
					}
					// Insert the row into the database.
		            ih.execute();
		            
		            if ( i % progressStep == 0 )
		            	publishProgress(i+1);
				}
				
				DBase.db.setTransactionSuccessful();
			}
			catch(Exception ex) { errMsg = ex.getMessage(); } 
			finally				{ DBase.db.endTransaction(); }
			
			
			return null;
		}
	 	@Override
	 	protected void onPostExecute(Void result) 
	 	{
	 		if ( dlgProg != null && dlgProg.isShowing() )
	 			dlgProg.dismiss();
	 		
	 		updateList();

	 		if ( errMsg.length() > 0 )
	 			GM.showError(ActMain.this, String.format("Error occured!\n%s", errMsg) );
	 		else
	 		{
		        File f = getDatabasePath(DBase.DATABASE_NAME);
		        int endDbSize = (int)f.length();
		        String strType = "INT";
				switch ( valueType )
				{
				case DlgInsertType.TYPE_LONG:   strType = "LONG"; break;
				case DlgInsertType.TYPE_DOUBLE: strType = "DOUBLE"; break;
				case DlgInsertType.TYPE_TEXT:   strType = "TEXT"; break;
				}
	 			
	 			GM.showInfo(ActMain.this, String.format("%d %s values added.\nDB size before: %d [B]\nDB size after: %d [B]\nIncrement: %d [B]", recCnt, strType, initDbSize, endDbSize, endDbSize-initDbSize));
	 		}
	 	}

	 	@Override
	 	protected void onCancelled() 
	 	{
	 		super.onCancelled();

	 		if ( dlgProg != null && dlgProg.isShowing() )
	 			dlgProg.dismiss();
	 	}
	 	
	 	@Override
	 	protected void onProgressUpdate(Integer... values) 
	 	{
	 		if ( dlgProg != null && values != null && values.length > 0 )
	 			dlgProg.setMessage(String.format("%d / %d records added", values[0], recCnt));
	 		else
	 			super.onProgressUpdate(values);
	 	}
	}
}

class NumberViewBinder implements SimpleCursorAdapter.ViewBinder 
{
	private int colFormat = -1; 
	NumberViewBinder(int colDouble)
	{
		this.colFormat = colDouble;
	}
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if(columnIndex == colFormat) 
        {
        	if ( cursor.isNull(columnIndex) || cursor.getDouble(columnIndex) == 0 ) // testing isNull is not necessary as SQLite converts NULL to 0 when getDouble is called
        		((TextView)view).setText("-");
        	else
        		((TextView)view).setText(String.format("%.2f", cursor.getDouble(columnIndex)));
        	return true;
        }
        return false;
    }
}