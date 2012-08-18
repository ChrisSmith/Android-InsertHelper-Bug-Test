package com.example.testbulkinsert;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView tv;
	private DatabaseHelper dbHelper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dbHelper = new DatabaseHelper(this);
        
        tv = (TextView) findViewById(R.id.text);
    }

    public void onClick(View v){
    	tv.setText("");
    	
    	if(v.getId() == R.id.button0){
    		startTest(0);
    	}else if(v.getId() == R.id.button1){
    		startTest(1);
    	}else if(v.getId() == R.id.button2){
    		displayContents();
    	}else if(v.getId() == R.id.button3){
    		deleteContents();
    	}
    }
   
    private static final String TABLE = "testtableone";
    private static final String FIRST = "first";
    private static final String SECOND = "second";
    
    private void Log(String msg){
    	tv.append(msg);
    	tv.append("\n");
    }
    
    private void deleteContents(){
    	Log("Delete existing content");
        
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	db.delete(TABLE, null, null);
    	db.close();
    	
    	Log("Done");
        
    }
    
    private void displayContents(){
    	tv.setText("");
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	
    	displayContents(db, TABLE);
    	
    	db.close();
    }
    
    private void displayContents(SQLiteDatabase db, String table){
    	Cursor c = null;
    	try{
        	c = db.query(false, table, null, null, null, null, null, null, null);
        	int col1 = c.getColumnIndexOrThrow(FIRST), col2 = c.getColumnIndexOrThrow(SECOND);
    		if(c.moveToFirst()){
    			do{
    				int one = c.getInt(col1);
    				int two = c.getInt(col2);
    				Log(one+", "+two);
    				
    			}while(c.moveToNext());
    		}
    	}finally{
    		if(c!=null) c.close();
    	}
    }
    
    
    private ContentValues[] getValues(){
    	
		Log("creating values");
    	
    	final int size = 10;
    	ContentValues[] bulkvalues = new ContentValues[size*size];
        for(int x=0; x < size; x++){
        	for(int y=0; y < size; y++){

        		ContentValues values = new ContentValues();
        		values.put(FIRST, x);
        		values.put(SECOND,y);
        		bulkvalues[x*size + y] = values;
        	}
        }
        
        return bulkvalues;
    }
    
    private void startTest(int testid){
    
    	ContentValues[] bulkvalues = getValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Log("\n******* DELETING VALUES *******");
        db.delete(TABLE, null, null);

        testTable(db, testid, bulkvalues);
        
    	db.close();
    }
 
    private void testTable(SQLiteDatabase db, int testid, ContentValues[] bulkvalues){
        long error = 0;
        
        if(testid == 0){
        	Log("\n******* TESTING BINDING *******");
        }else{
        	Log("\n******* TESTING INSERT *******");
        }
        
        InsertHelper ih = new InsertHelper(db, TABLE);
		int count = -1;
		
        try {        	
			db.beginTransaction();
			
			int col1 = ih.getColumnIndex(FIRST), col2 = ih.getColumnIndex(SECOND);
			
		    for (ContentValues value : bulkvalues) {
	        	count++;
	        	
		    	ih.prepareForInsert();
		    	
		    	if(testid == 0){
			    	ih.bind(col1, value.getAsInteger(FIRST));
			    	ih.bind(col2, value.getAsInteger(SECOND));
		    		
		    	}else if(testid == 1){
		    		ih.insert(value);
		    	}
		    	
		    	error = ih.execute(); 

		    	if(error == -1){
		    		Log("error while executing insert: "+value.getAsInteger(FIRST) + ", " + value.getAsInteger(SECOND));
		    	}
		    }
		    db.setTransactionSuccessful();
		    
		} finally {
			db.endTransaction(); 
			ih.close();
			
			Log("\n******* DONE *******\n");
		}
    }
    
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, "testingdatabase", null, 1);
		}
		
    	private static final String CREATE_TWO = 
    		"create table testtableone (first integer, second integer, primary key (first, second) );";
    	
    	@Override
		public void onCreate(SQLiteDatabase db) {
    		db.execSQL(CREATE_TWO);
    	}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			//unused
		}
    }
}
