/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.datastore.client.transport;

import java.io.IOException;

import org.elasticsearch.client.Client;

/**
 * Elasticsearch transport client wrapper definition.
 *
 * @since 1.0
 */
public interface EsClientProvider {

    /**
     * Get a new Elasticsearch client instance
     *
     * @return
     */
    public Client getClient();

    /**
     * Close the Elasticsearch client instance
     */
    public void close() throws IOException;

    /**
     * Check if the Elasticsearch client instance is alive
     * 
     * @return
     */
    public boolean isAlive();

}
