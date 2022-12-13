package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.ExpenseManagerDBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO implements TransactionDAO {
    private ExpenseManagerDBHelper EMdbHelper;

    public PersistentTransactionDAO(ExpenseManagerDBHelper EMdbHelper) {
        this.EMdbHelper = EMdbHelper;
    }

    /***
     * Log the transaction requested by the user.
     *
     * @param date        - date of the transaction
     * @param accountNo   - account number involved
     * @param expenseType - type of the expense
     * @param amount      - amount involved
     */
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount){
        SQLiteDatabase dbWrite = EMdbHelper.getWritableDatabase();
        dbWrite.beginTransaction();
        try{
            ContentValues values = new ContentValues();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
            String stringDate = formatter.format(date);
            values.put("date", stringDate);
            values.put("accountNo", accountNo);
            values.put("expenseType", expenseType.equals(ExpenseType.EXPENSE) ? "EXPENSE": "INCOME");
            values.put("amount", amount);
            long currentRow = dbWrite.insert("Transactions", null, values);
            if (  currentRow>0) {
                Log.d("PTransactionDAO", "Transaction logged successfully");
            }
            else {
                Log.d("PTransactionDAO", "Failed to log transaction ");
            }
        }
        catch (Exception e){
            Log.d("PTransactionDAO", "Error while logging transaction ");
        }
        finally {
            dbWrite.endTransaction();
            dbWrite.close();
        }
    }

    /***
     * Return all the transactions logged.
     *
     * @return - a list of all the transactions
     */
    public List<Transaction> getAllTransactionLogs(){
        SQLiteDatabase dbRead = EMdbHelper.getReadableDatabase();
        String selectQuery =String.format("select * from %s order by %s desc ",ExpenseManagerDBHelper.tblTransaction,ExpenseManagerDBHelper.keyTransactionID);
        Cursor cr = dbRead.rawQuery(selectQuery, null);
        List<Transaction> transactions = new LinkedList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
        try{
            if (cr.moveToFirst())  {
                do{
                    Date date = formatter.parse(cr.getString(cr.getColumnIndex("date")));
                    String accountNo = cr.getString(cr.getColumnIndex("accountNo"));
                    String expenseSType = cr.getString(cr.getColumnIndex("expenseType"));
                    double amount = cr.getDouble(cr.getColumnIndex("amount"));
                    ExpenseType expenseType;
                    if(expenseSType.equals("EXPENSE") ){
                        expenseType = ExpenseType.EXPENSE;
                    }else {
                        expenseType = ExpenseType.INCOME;
                    }
                    Transaction tempTransaction= new Transaction(date, accountNo, expenseType, amount);
                    transactions.add(tempTransaction);
                } while (cr.moveToNext());
            }
        }
        catch (ParseException e) {
            Log.d("PTransactionDAO","Error while getting all transactions");
        }
        finally {
            if (cr != null && !cr.isClosed()) {
                cr.close();
            }
            dbRead.close();
        }

        return transactions;

    }

    /***
     * Return a limited amount of transactions logged.
     *
     * @param limit - number of transactions to be returned
     * @return - a list of requested number of transactions
     */
    public List<Transaction> getPaginatedTransactionLogs(int limit){
        SQLiteDatabase dbRead = EMdbHelper.getReadableDatabase();
        String selectQuery =String.format("select * from %s order by %s desc %d ",ExpenseManagerDBHelper.tblTransaction,ExpenseManagerDBHelper.keyTransactionID,limit);
        Cursor cr = dbRead.rawQuery(selectQuery, null);
        List<Transaction> transactions = new LinkedList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");

        try {
            if (cr.moveToFirst()) {
                 do{
                    String date = cr.getString(cr.getColumnIndex("date"));
                    String accountNo = cr.getString(cr.getColumnIndex("accountNo"));
                    double amount = cr.getDouble(cr.getColumnIndex("amount"));
                    String expenseType = cr.getString(cr.getColumnIndex("expenseType"));
                    Date dateObj = formatter.parse(date);
                    ExpenseType expenseTypeObj;
                    if (expenseType.equals("INCOME")) {
                        expenseTypeObj = ExpenseType.INCOME;
                    } else {
                        expenseTypeObj = ExpenseType.EXPENSE;
                    }

                    Transaction tempTransaction = new Transaction(dateObj, accountNo, expenseTypeObj, amount);
                    transactions.add(tempTransaction);
                }while (cr.moveToNext());
            }
        }
        catch (ParseException e) {
           Log.d("PTransactionDAO","Error while getting paginated transactions");
        }
        finally{
            if (cr != null && !cr.isClosed()) {
                cr.close();
            }
            dbRead.close();
        }
        return transactions;

    }
}
