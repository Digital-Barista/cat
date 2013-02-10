/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.jms;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RunAs;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 *
 * @author Falken
 */
public abstract class DBTransactionalJMSListener  implements MessageListener {
    private static final Logger logger = Logger.getLogger(DBTransactionalJMSListener.class);
    
    @Autowired
    private SessionFactory sessionFactory;
    
    protected void setUpAuthorization() {
        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_admin"));      
        
        TestingAuthenticationToken token = new TestingAuthenticationToken("JMS Listener","pw",grantedAuths);
        token.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(token);       
    }
  
    protected void tearDownAuthorization() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
    
    
    protected boolean openSessionTransaction() {
        boolean participate = false;
        if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
            // Do not modify the Session: just set the participate flag.
            participate = true;
            logger.debug("listener session already in transaction.");
        } else {
            logger.debug("Opening Hibernate Session for JMS listener.");
            Session session = openSession(sessionFactory);
            SessionHolder sessionHolder = new SessionHolder(session);
            TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);         
        }
        return participate;
    }
    
    protected Session openSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
        try {
            Session session = SessionFactoryUtils.getNewSession(sessionFactory);
            session.setFlushMode(FlushMode.AUTO);
            return session;
        } catch (HibernateException ex) {
            throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
        }
    }    

    protected void closeSession() {
        SessionHolder sessionHolder =
                    (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.closeSession(sessionHolder.getSession());
    }
    
    @Override
    public void onMessage(Message msg) {
        preProcess();
        setUpAuthorization();
        boolean participate = false;
        try {
            participate = openSessionTransaction();           
            // let implementor process the message.
            processMessage(msg);
        } finally {
            if (!participate) {
                closeSession();
            }
        }       
        tearDownAuthorization();
        postProcess();
    }
    
    public void preProcess() {}

    public abstract void processMessage(Message msg);
    
    public void postProcess() {}

}