/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.broker.core.pool;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.kapua.KapuaErrorCodes;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.broker.core.message.CamelKapuaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jms session wrapper.<BR>
 * This class wrap a single session per connection and manage the close operation of connection on session close.<BR>
 * The connection is taken from a connection pool ({@link org.apache.activemq.ActiveMQConnectionFactory})
 * 
 * @since 1.0
 */
public abstract class JmsProducerWrapper
{

    private static Logger s_logger = LoggerFactory.getLogger(JmsProducerWrapper.class);

    protected String          destination;
    protected Connection      connection;
    protected Session         session;
    protected MessageProducer producer;

    /**
     * 
     * @param vmconnFactory
     * @param destination if it's null the producer will not be bound to any destination so it can sends messages to the whole topic space.<BR>
     *            Otherwise if it isn't null the producer will be bound to a queue destination as specified by the parameter.
     * @param transacted
     * @param start start activeMQ connection
     * @throws JMSException
     */
    protected JmsProducerWrapper(ActiveMQConnectionFactory vmconnFactory, String destination, boolean transacted, boolean start) throws JMSException
    {
        connection = vmconnFactory.createConnection();
        if (start == true) {
            connection.start();
        }
        session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
        // for virtual topic support we need to send connect/disconnect messages to the topics instead of queue then the destination will be dynamic ($KAPUA.{0}.{1}.MQTT.CONNECT)
        if (destination != null && destination.trim().length() > 0) {
            producer = session.createProducer(session.createQueue(destination));
        }
        else {
            producer = session.createProducer(null);
        }
        this.destination = destination;
    }

    public void close()
    {
        try {
            connection.close();
        }
        catch (JMSException e) {
            s_logger.error("Exception on connection close close {}", e.getMessage(), e);
        }
    }

    public String getDestination()
    {
        return destination;
    }

    @Override
    protected void finalize() throws Throwable
    {
        close();
        super.finalize();
    }

    /**
     * Send a message {@link CamelKapuaMessage}
     * 
     * @param message
     * @throws JMSException
     * @throws KapuaException
     */
    public void sendRawMessage(CamelKapuaMessage<?> message) throws JMSException, KapuaException
    {
        s_logger.error("Feature not implemented yet!");
        throw new KapuaException(KapuaErrorCodes.INTERNAL_ERROR);
    }

}
