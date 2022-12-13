package lk.ac.mrt.cse.dbs.simpleexpensemanager.control;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class ExpenseManagerDBHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String dbName = "200591M.db";
    private static final int dbVersion = 1;
    // Table Names
    public static final String tblAccount = "accounts";
    public static final String tblTransaction = "transactions";

    // columns in accounts table
    public static final String keyAccountNo = "accountNo";
    public static final String keyBankNAme = "bankName";
    public static final String keyAccountHolderName = "accountHolderName";
    public static final String keyBalance ="balance";

    // columns in transactions table
    public static final String keyTransactionID = "transaction_id";
    public static final String keyDate = "date";
    public static final String getKeyAccountNoFK = "accountNo";
    public static final String keyExpenseType = "expenseType";
    public static final String keyAmount = "amount";

    public ExpenseManagerDBHelper(@Nullable Context context){
        super(context,dbName , null,dbVersion);
    }
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CreateAccountTable = String.format("create table %s(%s text primary key,%s text not null,%s text not null,%s real not null)", tblAccount, keyAccountNo,keyBankNAme,keyAccountHolderName,keyBalance);
        sqLiteDatabase.execSQL(CreateAccountTable);
        String CreateTransactionTable = String.format("create table %s (%s integer primary key autoincrement, %s text not null, %s text references %s, %s text not null, %s real not null)",tblTransaction,keyTransactionID,keyDate,getKeyAccountNoFK,tblAccount ,keyExpenseType,keyAmount);
        sqLiteDatabase.execSQL(CreateTransactionTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(String.format("drop table if exists %s",tblAccount));
        sqLiteDatabase.execSQL(String.format("drop table if exists %s",tblTransaction));
        onCreate(sqLiteDatabase);
    }
}
