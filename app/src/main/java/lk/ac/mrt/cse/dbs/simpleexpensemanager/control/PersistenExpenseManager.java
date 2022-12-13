package lk.ac.mrt.cse.dbs.simpleexpensemanager.control;

import android.content.Context;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.PersistentAccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.PersistentTransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;

public class PersistentExpenseManager extends ExpenseManager {

    private final ExpenseManagerDBHelper EMdb;

    public PersistentExpenseManager(Context context) {
        EMdb = new ExpenseManagerDBHelper(context);
        setup();
    }

    public void setup() {
        // Begin generating dummy data for persistent storage implementation

        TransactionDAO persistentTransactionDAO = new PersistentTransactionDAO(EMdb);
        setTransactionsDAO(persistentTransactionDAO);

        AccountDAO persistentAccountDAO = new PersistentAccountDAO(EMdb);
        setAccountsDAO(persistentAccountDAO);

        // dummy data

        Account dummyAcct1 = new Account("12345A", "Yoda Bank", "Anakin Skywalker", 10000.0);
        Account dummyAcct2 = new Account("78945Z", "Clone BC", "Obi-Wan Kenobi", 80000.0);
        if(!persistentAccountDAO.getAccountsList().contains(dummyAcct1)){
            getAccountsDAO().addAccount(dummyAcct1);
        }

        if(!persistentAccountDAO.getAccountsList().contains(dummyAcct2)){
            getAccountsDAO().addAccount(dummyAcct2);
        }

        // End
    }
}
