package com.yl.db;

import com.yl.utils.Constant;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaseDao extends SQLiteOpenHelper{
	private static final String tag = "BaseDao";
	public BaseDao(Context context) {
		super(context, Constant.NAME, null, Constant.VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuffer sql = new StringBuffer("CREATE TABLE IF NOT EXISTS alarm (id integer primary key AutoIncrement,zdKey varchar(20),mark varchar(100))");
		db.execSQL(sql.toString());
		Log.i(tag, "创建数据库完成");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

}
