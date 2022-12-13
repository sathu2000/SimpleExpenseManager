package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;



import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.ExpenseManagerDBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO implements AccountDAO {
    private final ExpenseManagerDBHelper EMdbHelper;


    public PersistentAccountDAO(ExpenseManagerDBHelper EMdbHelper) {
        this.EMdbHelper=EMdbHelper;
    }
    /***
     * Get a list of account numbers.
     *
     * @return - list of account numbers as String
     */
    public List<String> getAccountNumbersList(){
        SQLiteDatabase dbRead =EMdbHelper.getReadableDatabase();
        String selectQuery= String.format("select %s from %s",ExpenseManagerDBHelper.keyAccountNo,ExpenseManagerDBHelper.tblAccount);
        Cursor cr = dbRead.rawQuery(selectQuery,null);
        List<String> accNum = new LinkedList<>();
        try{
            if(cr.moveToFirst())
            {
                do{
                    String accNo = cr.getString(cr.getColumnIndex("accountNo"));
                    accNum.add(accNo);
                } while(cr.moveToNext());
            }
        }
        catch (Exception e) {
            Log.d("PAccountDAO", "Error while trying to get account numbers list from database");
        }
        finally {
            if (cr != null && !cr.isClosed()) {
                cr.close();
            }
            dbRead.close();
        }
        return accNum;
    }
    /***
     * Get a list of accounts.
     *
     * @return - list of Account objects.
     */
    public List<Account> getAccountsList(){
        SQLiteDatabase dbRead =EMdbHelper.getReadableDatabase();
        String selectQuery= String.format("select * from %s",ExpenseManagerDBHelper.tblAccount);
        Cursor cr = dbRead.rawQuery(selectQuery,null);
        List<Account> accList = new LinkedList<>();
        try {
            if (cr.moveToFirst()) {
                do {
                    String accountNo = cr.getString(cr.getColumnIndex("accountNo"));
                    String bankName = cr.getString(cr.getColumnIndex("bankName"));
                    String accountHolderName = cr.getString(cr.getColumnIndex("accountHolderName"));
                    double balance = cr.getDouble(cr.getColumnIndex("balance"));
                    Account tempAcc = new Account(accountNo, bankName, accountHolderName, balance);
                    accList.add(tempAcc);
                } while (cr.moveToNext());
            }
        }
        catch (Exception e) {
            Log.d("PAccountDAO", "Error while trying to get accounts list from database");
        }
        finally {
            if (cr != null && !cr.isClosed()) {
                cr.close();
            }
            dbRead.close();
        }
        return accList;

    }

    /***
     * Get the account given the account number.
     *
     * @param accountNo as String
     * @return - the corresponding Account
     * @throws InvalidAccountException - if the account number is invalid
     */
    public Account getAccount(String accountNo) throws InvalidAccountException{
        SQLiteDatabase dbRead = EMdbHelper.getReadableDatabase();
        String select_query = "select * from Accounts" + " where accountNo='" + accountNo + "'";
        Cursor cr = dbRead.rawQuery(select_query, null);
        try {
            if (cr.moveToFirst()) {
                String accountNum = cr.getString(cr.getColumnIndex("accountNo"));
                String bankName = cr.getString(cr.getColumnIndex("bankName"));
                String accountHolderName = cr.getString(cr.getColumnIndex("accountHolderName"));
                double balance = cr.getDouble(cr.getColumnIndex("balance"));
                Account tempAcc = new Account(accountNum, bankName, accountHolderName, balance);
                return tempAcc;
            }
        }
        catch (Exception e) {
            Log.d("PAccountDAO", "Error while trying to get account from database");
        }
        finally {
            if (cr != null && !cr.isClosed()) {
                cr.close();
            }
            dbRead.close();
        }
        String msg = String.format("Account %s is invalid.",ExpenseManagerDBHelper.keyAccountNo);
        throw new InvalidAccountException(msg);
    }

    /***
     * Add an account to the accounts collection.
     *
     * @param account - the account to be added.
     */
    public void addAccount(Account account){
        SQLiteDatabase dbAdd = EMdbHelper.getWritableDatabase();
        dbAdd.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("accountNo", account.getAccountNo());
            values.put("bankName", account.getBankName());
            values.put("accountHolderName", account.getAccountHolderName());
            values.put("balance", account.getBalance());
            dbAdd.insert("Accounts", null, values);
            Log.d("PAccountDAO", "Account added successfully");
        }
        catch (Exception e) {
            Log.d("PAccountDAO", "Failed to add account");
        }
        finally {
            dbAdd.endTransaction();
            dbAdd.close();
        }
    }

    /***
     * Remove an account from the accounts collection.
     *
     * @param accountNo - of the account to be removed.
     * @throws InvalidAccountException - if the account number is invalid
     */
    public void removeAccount(String accountNo) throws InvalidAccountException{
        SQLiteDatabase dbRemove = EMdbHelper.getWritableDatabase();
        dbRemove.beginTransaction();
        try{
            int result=dbRemove.delete("Accounts", "accountNo=?", new String[]{accountNo});
            if ( result != -1) {
                Log.d("PAccountDAO", "Account removed successfully");
            }
            else {
                Log.d("PAccountDAO", "Failed to remove account");
                String msg = String.format("Account %s is invalid.",ExpenseManagerDBHelper.keyAccountNo);
                throw new InvalidAccountException(msg);
            }
        }
        catch(Exception e){
            Log.d("PAccountDAO", "Error while removing account");
        }
        finally {
            dbRemove.endTransaction();
            dbRemove.close();
        }

    }

    /***
     * Update the balance of the given account. The type of the expense is specified in order to determine which
     * action to be performed.
     * <p/>
     * The implementation has the flexibility to figure out how the updating operation is committed based on the type
     * of the transaction.
     *
     * @param accountNo   - account number of the respective account
     * @param expenseType - the type of the transaction
     * @param amount      - amount involved
     * @throws InvalidAccountException - if the account number is invalid
     */
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException{
        double newBalance = getAccount(accountNo).getBalance();
        switch (expenseType){
            case INCOME:
                newBalance +=amount;
                break;
            case EXPENSE:
                newBalance -=amount;
        }

        SQLiteDatabase dbUpdate = EMdbHelper.getWritableDatabase();
        dbUpdate.beginTransaction();
        try{
            ContentValues values = new ContentValues();
            values.put("balance", newBalance);
            int result= dbUpdate.update("Accounts", values, "accountNo=?", new String[]{accountNo});
            if ( result!= -1) {
                Log.d("PAccountDAO", "Account balance updated successfully");
            }
            else {
                Log.d("PAccountDAO", "Failed to update account balance");
                String msg = String.format("Account %s is invalid.",ExpenseManagerDBHelper.keyAccountNo);
                throw new InvalidAccountException(msg);
            }
        }
        catch (Exception e){
            Log.d("PAccountDAO", "Error while updating account balance");
        }
        finally {
            dbUpdate.endTransaction();
            dbUpdate.close();
        }
    }

}
