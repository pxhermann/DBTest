package cz.hermann.android.DBTest;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBase 
{
	public static final String DATABASE_NAME = "TestDatabase";
	
    public static SQLiteDatabase db = null;

    public static boolean isOpen()
    {
		if ( db != null )
			return db.isOpen();
		
		return false;
    	
    }
	public static void open(Context context) throws NameNotFoundException
	{
		if ( isOpen() )
			return;

		Context appC = context.getApplicationContext();	// zajisti application context, aby se nestalo, context je napr activity context a nedoslo tak k pripadnemu memory leaku 
		
		PackageInfo pi = appC.getPackageManager().getPackageInfo(appC.getPackageName(), 0);

		DBEngine engine = new DBEngine(appC, DATABASE_NAME, pi.versionCode);

		db = engine.getWritableDatabase();
	}
	public static boolean close()
	{
		try
		{
		// close main connection
			if ( db != null )
			{
				if ( db.isOpen() )
					db.close();

				db = null;
			}
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}
}

class DBEngine extends SQLiteOpenHelper 
{
	public DBEngine(Context context, String dbName, int dbVersion) 
	{
		super(context, dbName, null, dbVersion);
	}
	@Override
	public void onCreate(SQLiteDatabase database) 
	{
	//-- create tables
		String sql = "CREATE TABLE Product (" +
					"_id integer primary key autoincrement,"+
					"short text,"+
					"name text,"+
					"note text"+
					");";
		database.execSQL(sql);
		//
		sql = "CREATE TABLE ProdPrice (" +
				"_id integer primary key autoincrement,"+
				"prod_id integer,"+
				"price real"+
				");";
		database.execSQL(sql);
		//
		sql = "CREATE TABLE TestTable (" +
				"textValue text,"+
				"doubleValue real,"+
				"longValue number,"+
				"intValue int"+
				");";
		database.execSQL(sql);
		
	//-- init some data
		sql = "INSERT INTO Product (short, name, note) VALUES ('kat0001', 'Product 0001', '');";
		database.execSQL(sql);
		sql = "INSERT INTO Product (short, name, note) VALUES ('kat0002', 'Product 0002', '');";
		database.execSQL(sql);
		sql = "INSERT INTO Product (short, name, note) VALUES ('kat0003', 'Product 0003', '');";
		database.execSQL(sql);
		sql = "INSERT INTO Product (short, name, note) VALUES ('kat0004', 'Product 0004', '');";
		database.execSQL(sql);
		//
		sql = "INSERT INTO ProdPrice (prod_id, price) VALUES (1, 11.1);";
		database.execSQL(sql);
		sql = "INSERT INTO ProdPrice (prod_id, price) VALUES (2, 22.2);";
		database.execSQL(sql);
		sql = "INSERT INTO ProdPrice (prod_id, price) VALUES (3, 33.33);";
		database.execSQL(sql);
	}

	// Method is called during an upgrade of the database, e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) 
	{
	//-- clear database first
		Cursor set = null;
		try
		{
			// ~ drop indexes			
			String s = "SELECT name FROM sqlite_master WHERE type='index'";
			set = database.rawQuery(s, null);
			for(set.moveToFirst(); !set.isAfterLast(); set.moveToNext())
			{
				s = String.format("DROP INDEX %s", set.getString(0));
				database.execSQL(s);
			}
			set.close();
			// drop tables 
			s = "SELECT name FROM sqlite_master WHERE (type='table' OR type='view') AND name NOT LIKE 'sqlite_%'";
			set = database.rawQuery(s, null);
			for(set.moveToFirst(); !set.isAfterLast(); set.moveToNext())
			{
				s = String.format("DROP TABLE %s", set.getString(0));
				database.execSQL(s);
			}
			set.close();
		}
		finally { try { if ( set != null && !set.isClosed() ) set.close(); } catch (Exception e) {} }
		
	//-- create new database
		onCreate(database);
	}
}	
