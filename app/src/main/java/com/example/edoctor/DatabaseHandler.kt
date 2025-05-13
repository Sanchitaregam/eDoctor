package com.example.edoctor

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

val DATABASE_NAME="eDoctor"
val TABLE_NAME="doctor"
val COL_ID="id"
val COL_NAME="name"
val COL_EMAIL="email"
val COL_PASSWORD="password"
val COL_CONFIRM_PASSWORD="confirm_password"
val COL_PHONE="phone"

class DatabaseHandler(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,1){
    override fun onCreate(db: SQLiteDatabase?) { // Renamed p0 to db for clarity
        val createTable="CREATE TABLE "+ TABLE_NAME+"("+ COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ COL_NAME+" VARCHAR(256),"+ COL_EMAIL+" VARCHAR(256),"+ COL_PASSWORD+" VARCHAR(256),"+ COL_CONFIRM_PASSWORD+" VARCHAR(256),"+ COL_PHONE+" VARCHAR(256))";
        db?.execSQL(createTable)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }
    fun insertData(name:String,email:String,password:String,confirm_password:String,phone:String){
        val db=this.writableDatabase
       val cv=android.content.ContentValues()
        cv.put(COL_NAME,name)
        cv.put(COL_EMAIL,email)
        cv.put(COL_PASSWORD,password)
        cv.put(COL_CONFIRM_PASSWORD,confirm_password)
        cv.put(COL_PHONE,phone)
        var result=db.insert(TABLE_NAME,null,cv)
        if(result==-1.toLong()){
        Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        }
    }

}
