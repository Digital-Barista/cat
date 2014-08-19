/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.util;

import java.util.List;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.hibernate.HibernateException;
import org.hibernate.transaction.BTMTransactionManagerLookup;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 *
 * @author Falken
 */
public class CompositeTransactionManagerLookup implements TransactionManagerLookup {

  private static List<TransactionManagerLookup> lookupClasses;
  
  public TransactionManager getTransactionManager(Properties props) throws HibernateException {
    try
    {
      InitialContext ic = new InitialContext();
      TransactionManager tm = (TransactionManager)ic.lookup("java:/TransactionManager");
      return tm;
    }catch(NamingException ex)
    {}
    return new BTMTransactionManagerLookup().getTransactionManager(props);
  }

  public String getUserTransactionName() {
    try
    {
      InitialContext ic = new InitialContext();
      Object obj = ic.lookup("UserTransaction");
      if(obj!=null)
        return "UserTransaction";
    }catch(NamingException ex)
    {
    }
    return "java:comp/UserTransaction";
  }

  public Object getTransactionIdentifier(Transaction transaction) {
    return transaction;
  }
  
}
