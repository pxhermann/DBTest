package cz.hermann.android.DBTest;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActSqlTest extends Activity {
	static final String[] SQL_ELM = new String[] {
		"",
		"CREATE ",
		"ALTER ",
		"DROP ",
		" TABLE ",
		" INDEX ",
		" ADD ",
		"SELECT ",
		"SELECT COUNT(*) FROM ",
		"SELECT * FROM ",
		" \nINNER JOIN ",
		" \nLEFT OUTER JOIN ",
		" \nRIGHT OUTER JOIN ",
		" \nON ",
		"DELETE FROM ",
		"INSERT INTO ",
		" VALUES (",
		"UPDATE ",
		" \nSET ",
		" \nWHERE ",
		" \nGROUP BY ",
		" \nORDER BY ",
		" \nHAVING ",
		" OR ",
		" AND ",
		" IS NULL ",
		" IS NOT NULL",
		" LIKE ",
		" IN ",
		" LEN(",
		" SUBSTRING(",
		" INT",
		" SMALLINT",
		" VARCHAR",
		" DATETIME",
		" FLOAT",
		" BIT",
		"VACUUM",
		"PRAGMA",
		" auto_vacuum",
		" automatic_index",
		" synchronous",
		" cache_size =",
		" case_sensitive_like =",
		" collation_list",
		" compile_options",
		" foreign_keys",
		" encoding",
		" = \"UTF-8\"",
		" = \"UTF-16\"",
		" = \"UTF-16le\"",
		" = \"UTF-16be\""
		};
	// controls
	EditText etCmd;
	Spinner spTbl, spFld;
    SeekBar seekRecPos;
	TextView tvRecPos;
	// database data
    private Cursor setData = null;
    // current record
    ListView listRecData;
    ArrayList<HashMap<String, String>> arrRecData;

//*************************************************************************
// overrides 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.act_sqltest);
		
		etCmd = (EditText)findViewById(R.id.etCmd);
		etCmd.setText("SELECT * FROM ");
		etCmd.setSelection(etCmd.getText().length(), etCmd.getText().length());
		
		tvRecPos = (TextView)findViewById(R.id.tvRecPos);
		
		seekRecPos = (SeekBar)findViewById(R.id.seekRecPos);
		seekRecPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				if ( fromUser && progress!=lastRecNo )	// onProgressChanged is called several times when moving to certain progress position,   
				{										// so i use progress+1!=lastRecNo condition to avoid multiple calling ChangeCurRec for the same position
					try { changeCurRec(progress); }
					catch(Exception ex) { GM.showError(ActSqlTest.this, ex, "Error occured when moving current record!"); }
				}
			}
		});
		//
		findViewById(R.id.btnPrevRec).setOnClickListener(onBtnClick);
		findViewById(R.id.btnNextRec).setOnClickListener(onBtnClick);
		findViewById(R.id.btnExec).setOnClickListener(onBtnClick);
		findViewById(R.id.btnClear).setOnClickListener(onBtnClick);
		//
	    Spinner spinner = (Spinner)findViewById(R.id.spElm);
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SQL_ELM);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    adapter.setDropDownViewResource(R.layout.spinner_row);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(onSpinnerSel);
		//
		spTbl = (Spinner) findViewById(R.id.spTbl);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>() );
		adapter.setNotifyOnChange(false);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    adapter.setDropDownViewResource(R.layout.spinner_row);
		spTbl.setAdapter(adapter);
		spTbl.setOnItemSelectedListener(onSpinnerSel);
		//
		spFld= (Spinner) findViewById(R.id.spFld);
	    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>() );
		adapter.setNotifyOnChange(false);
//	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    adapter.setDropDownViewResource(R.layout.spinner_row);
		spFld.setAdapter(adapter);
		spFld.setOnItemSelectedListener(onSpinnerSel);
		
		////
		listRecData = (ListView)findViewById(R.id.listRec); 	// view for current record - contains names of columns and their values in current record 
		arrRecData = new ArrayList<HashMap<String, String>>();
		SimpleAdapter sa = new SimpleAdapter(this, arrRecData, R.layout.list_row, 
									   new String[] {"name", "val"}, 
									   new int[] {R.id.col1, R.id.col2});
		listRecData.setAdapter(sa);
		
		int curRecNo = 0;
		Object[] dataRetain = (Object[])getLastNonConfigurationInstance();
		if ( dataRetain != null && dataRetain.length == 2 )
		{	// check whether configuration change was performed 
			setData = (Cursor)dataRetain[0];
			curRecNo = ((Integer)dataRetain[1]).intValue();
		}
	
		OnDataChanged(curRecNo);

		updateTableList();
	}
    @Override
    public Object onRetainNonConfigurationInstance() {
    	final Object[] dataRetain = new Object[2];
    	dataRetain[0] = setData; setData = null;	// disable closing cursor in onDestroy
    	dataRetain[1] = new Integer(seekRecPos.getProgress());
    	
    	return dataRetain;
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	// close cursor - I don't use managed cursor in this activity, so I have to handle its closing manually 
    	if ( setData != null && !setData.isClosed() )
    		setData.close();
    }

//*************************************************************************
// event handlers 
	private OnItemSelectedListener onSpinnerSel = new OnItemSelectedListener()
	{
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
	    {
	    	String text = parent.getItemAtPosition(pos).toString();
	    	if ( text == null || text.length() < 1 )
	    		return;
	    	
	    	int start = Math.max(0, etCmd.getSelectionStart());
	    	etCmd.getText().insert(start, text);
	    	etCmd.setSelection(start + text.length());

	    	if ( parent.getId() == R.id.spTbl )
	    		updateFieldList();
	    	
    		parent.setSelection(0);
	    }
	    public void onNothingSelected(android.widget.AdapterView<?> arg0) {};
    };
    private OnClickListener onBtnClick = new OnClickListener()
    {
    	public void onClick(View view) 
    	{
    		switch ( view.getId() )
    		{
    		case R.id.btnPrevRec:
    		case R.id.btnNextRec: 
    			try
    			{
    				
    				int i = seekRecPos.getProgress();
    				if ( view.getId() == R.id.btnPrevRec )
    					i--;
    				else
    					i++;
    				changeCurRec(i);
    			}
    			catch(Exception ex) { GM.showError(ActSqlTest.this, ex, "Error occured when moving record!"); }
    			break;
    		case R.id.btnExec:
    			try
    			{
    				String sql = etCmd.getText().toString();
    				if ( sql.contains("SELECT ") || (sql.contains("PRAGMA ") && !sql.contains("=")) )
    				{
    					if ( setData != null && !setData.isClosed() )
    						setData.close();
    					setData = DBase.db.rawQuery(sql, null);
    					OnDataChanged(0);
    				}
    				else
    				{
    					DBase.db.execSQL(sql);
        				Toast.makeText(ActSqlTest.this, "Command successfully executed!", Toast.LENGTH_SHORT).show();
    					
    					if ( sql.contains("TABLE ") )
    					{
    						if ( sql.contains("ALTER ") )
    							updateFieldList();
    						else
    							updateTableList();
    					}
    				}
    			}
    			catch(Exception ex) { GM.showError(ActSqlTest.this, ex, "Error occured when running SQL command!"); }
    			break;
    		case R.id.btnClear:
    			etCmd.getText().clear();
    			break;
    		}
    	}
    };
    
//*************************************************************************
// helper methods 
    private void updateTableList()
    {
    	Cursor set = null;
    	try
    	{
    	    @SuppressWarnings("unchecked")
    		ArrayAdapter<String> a = (ArrayAdapter<String>)spTbl.getAdapter();
    		a.clear();
    		a.add("");
    		
		    set = DBase.db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);// AND name NOT LIKE 'sqlite_%'");
	    	for(set.moveToFirst(); !set.isAfterLast(); set.moveToNext())
	    		a.add(set.getString(0));

			a.notifyDataSetChanged();
    	}
    	catch(Exception ex) { GM.showError(this,  ex, "Error occured when reading table list!"); }
		finally { try { if ( set != null && !set.isClosed() ) set.close(); } catch (Exception e) {} }
    }
    
	static String prevTblName = "";
    private void updateFieldList()
    {
    	updateFieldList(false);
    }
    private void updateFieldList(boolean updatePrevTbl)
    {
    	Cursor set = null;
    	try
    	{
    		String tblName = spTbl.getSelectedItem().toString();
    		if ( updatePrevTbl || tblName != null && tblName.length()>0 && !tblName.equalsIgnoreCase(prevTblName) )
    		{
    			if ( !updatePrevTbl )
    				prevTblName = tblName;
    			@SuppressWarnings("unchecked")
        		ArrayAdapter<String> a = (ArrayAdapter<String>)spFld.getAdapter();
        		a.clear();
        		a.add("");

			    set = DBase.db.rawQuery(String.format("SELECT * FROM %s WHERE 0", prevTblName), null);
			    for(int i = 0; i<set.getColumnCount(); i++)
			    	a.add(set.getColumnName(i));

			    a.notifyDataSetChanged();
    		}
    	}
    	catch(Exception ex) { GM.showError(this,  ex, "Error occured when reading field list!"); }
		finally { try { if ( set != null && !set.isClosed() ) set.close(); } catch (Exception e) {} }
    }

    private void OnDataChanged(int initRecNo)
    {
		arrRecData.clear();
		if ( setData == null || setData.isClosed() )
			seekRecPos.setMax(1);
		else
		{
			seekRecPos.setMax((setData.getCount()<=1)?1:(setData.getCount()-1));	// do not allow max value smaller than 1 
			
			HashMap<String, String> entry;
		    for(int i = 0; i<setData.getColumnCount(); i++)
		    {
				entry = new HashMap<String, String>();
				entry.put("name", setData.getColumnName(i));
				entry.put("val",  "");
		    	
		    	arrRecData.add(entry);
		    }
		}
		
	    changeCurRec(initRecNo);
    }

    static int lastRecNo = -1;
	private void changeCurRec(int recNo)
	{
		lastRecNo = recNo;
		
		if ( setData == null || setData.getCount() < 1 )
			recNo = -1;
		else if ( recNo >= setData.getCount() )
			recNo = setData.getCount()-1;
		else if ( recNo < 1 )
			recNo = 0;

		tvRecPos.setText(String.format("%d / %d ", recNo+1, ((setData == null || setData.isClosed())?0:setData.getCount())) );
		seekRecPos.setProgress((recNo<0)?0:recNo);
		
		if ( setData != null && recNo>=0 && recNo < setData.getCount() )
		{
			setData.moveToPosition(recNo);
		    for(int i = 0; i<setData.getColumnCount() && i<arrRecData.size(); i++)
		    	arrRecData.get(i).put("val", setData.getString(i));
			
			SimpleAdapter sa = (SimpleAdapter)listRecData.getAdapter();
			sa.notifyDataSetChanged();
		}
	}
}
