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
package org.eclipse.kapua.app.console.shared;

import java.util.MissingResourceException;

import org.eclipse.kapua.app.console.client.messages.ValidationMessages;

import com.google.gwt.core.client.GWT;

/**
 * The GwtKapuaException class is the superclass of all GWT errors and exceptions in the Kapua project. It extends the JDK Exception class by requesting its invokers to provide an error code
 * when
 * building its instances. The code is one value of KapuaErrorCode enum; the code is used to document the possible error conditions generated by the platform as well as to identify the localized
 * exception messages to be reported. Exceptions messages are stored in the KapuaExceptionMessagesBundle Properties Bundle and they are keyed on the exception code.
 *
 * @author mcarrer
 *
 */
public class GwtKapuaException extends Exception {

    private static final long serialVersionUID = -2843620194094802461L;

    protected GwtKapuaErrorCode m_errorCode;
    protected String[]        m_arguments;
    protected Integer         m_remainingLoginAttempts;

    @SuppressWarnings("unused")
    private GwtKapuaException() {
        super();
    }

    public GwtKapuaException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    private GwtKapuaException(String message, Throwable cause) {
        super(message, cause);
    }

    @SuppressWarnings("unused")
    private GwtKapuaException(Throwable t) {
        super(t);
    }

    /**
     * Builds a new GwtKapuaException instance based on the supplied GwtKapuaErrorCode, an optional Throwable cause.
     *
     * @param errorCode
     * @param cause
     * @param remainingLoginAttempts
     */
    public GwtKapuaException(GwtKapuaErrorCode errorCode, Throwable cause, Integer remainingLoginAttempts) {
        super(cause);
        this.m_errorCode = errorCode;
        this.m_remainingLoginAttempts = remainingLoginAttempts;
    }

    public GwtKapuaException(GwtKapuaErrorCode errorCode, Throwable cause, String... arguments) {
        super(cause);
        m_errorCode = errorCode;
        m_arguments = arguments;
    }

    /**
     * Factory method to build an GwtKapuaException with the GwtKapuaErrorCode.INTERNAL_ERROR code providing a cause and a message.
     *
     * @param cause
     * @param message
     * @return
     */
    public static GwtKapuaException internalError(Throwable cause, String message) {
        return new GwtKapuaException(GwtKapuaErrorCode.INTERNAL_ERROR, cause, message);
    }

    /**
     * Factory method to build an GwtKapuaException with the GwtKapuaErrorCode.INTERNAL_ERROR code providing only a message.
     *
     * @param message
     * @return
     */
    public static GwtKapuaException internalError(String message) {
        return new GwtKapuaException(GwtKapuaErrorCode.INTERNAL_ERROR, null);
    }

    public GwtKapuaErrorCode getCode() {
        return m_errorCode;
    }

    public String getMessage() {
        return getLocalizedMessage();
    }

    public String getLocalizedMessage() {

        String msg = m_errorCode.toString();
        try {
            ValidationMessages MSGS = GWT.create(ValidationMessages.class);
            String msgPattern = MSGS.getString(m_errorCode.name());
            if (msgPattern != null) {
                msg = format(msgPattern, (Object[]) m_arguments);
            }

            if (m_remainingLoginAttempts != null) {
                if (m_remainingLoginAttempts > 0) {
                    msgPattern = MSGS.getString(m_errorCode.name() + "_LOGIN_ATTEMPTS");
                    if (msgPattern != null) {
                        msg += " " + format(msgPattern, new Object[] { m_remainingLoginAttempts });
                    }
                } else {
                    msg += " " + MSGS.getString(m_errorCode.name() + "_USER_LOCKED");
                }
            }
        } catch (MissingResourceException e) {
            // ignore
        }
        return msg;
    }

    public String[] getArguments() {
        return m_arguments;
    }

    private String format(String s, Object[] arguments) {

        if (arguments == null) {
            return s;
        }

        // A very simple implementation of format
        int i = 0;
        while (i < arguments.length) {
            String delimiter = "{" + i + "}";
            while (s.contains(delimiter)) {
                s = s.replace(delimiter, String.valueOf(arguments[i]));
            }
            i++;
        }
        return s;
    }
}
