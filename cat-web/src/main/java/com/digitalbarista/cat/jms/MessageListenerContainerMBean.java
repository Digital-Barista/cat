/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.digitalbarista.cat.jms;

import javax.jms.ConnectionFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 *
 * @author Falken
 */
public interface MessageListenerContainerMBean {
    public void setCacheLevelName(String constantName) throws IllegalArgumentException;
    public int getCacheLevel();
    public void setConcurrency(String concurrency);
    public int getConcurrentConsumers();
    public void setMaxConcurrentConsumers(int maxConcurrentConsumers);
    public int getMaxConcurrentConsumers();
    public void setMaxMessagesPerTask(int maxMessagesPerTask);
    public int getMaxMessagesPerTask();
    public void setIdleConsumerLimit(int idleConsumerLimit);
    public int getIdleConsumerLimit();
    public void setIdleTaskExecutionLimit(int idleTaskExecutionLimit);
    public int getIdleTaskExecutionLimit();
    public void start() throws JmsException;
    public int getScheduledConsumerCount();
    public int getActiveConsumerCount();
    public String getDestinationName();
    public boolean isAcceptMessagesWhileStopping();
    public boolean isAutoStartup();
    public int getPhase();
    public boolean isActive();
    public void stop() throws JmsException;
    public boolean isRunning();
    public int getPausedTaskCount();
    public DestinationResolver getDestinationResolver();
    public ConnectionFactory getConnectionFactory();
    public boolean isSessionTransacted();
    public int getSessionAcknowledgeMode();
}
