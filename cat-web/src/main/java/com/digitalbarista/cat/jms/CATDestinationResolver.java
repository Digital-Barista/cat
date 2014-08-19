/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.jms;

import java.util.List;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.log4j.Logger;
import org.springframework.jms.support.destination.JndiDestinationResolver;


/**
 *
 * @author Falken
 */
public class CATDestinationResolver extends JndiDestinationResolver {
    private static final Logger logger = Logger.getLogger(CATDestinationResolver.class);
    private List<String> jndiPrefixes;

    public List<String> getJndiPrefixes() {
        return jndiPrefixes;
    }

    public void setJndiPrefixes(List<String> jndiPrefixes) {
        this.jndiPrefixes = jndiPrefixes;
    }

    @Override
    public Destination resolveDestinationName(Session session, String destinationName, boolean pubSubDomain) throws JMSException {
        Destination dest = super.resolveDestinationName(session, destinationName, pubSubDomain);
        if (dest != null) {
            logger.debug("JMS name: '" + dest + "'");
            return dest;
        }
        if (jndiPrefixes != null) {
            for (String prefix : jndiPrefixes) {
                dest = super.resolveDestinationName(session, prefix + destinationName, pubSubDomain);
                if (dest != null) {
                    logger.debug("JMS name: '" + dest + "'");
                    return dest;
                }
            }
        }
        logger.debug("JMS destination name not resolved.");
        return null;
    }
}
