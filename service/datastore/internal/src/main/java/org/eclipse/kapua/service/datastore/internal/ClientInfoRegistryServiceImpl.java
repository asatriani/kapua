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
package org.eclipse.kapua.service.datastore.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.AbstractKapuaConfigurableService;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.domain.Domain;
import org.eclipse.kapua.service.authorization.permission.Actions;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.datastore.ClientInfoRegistryService;
import org.eclipse.kapua.service.datastore.DatastoreDomain;
import org.eclipse.kapua.service.datastore.DatastoreObjectFactory;
import org.eclipse.kapua.service.datastore.MessageStoreService;
import org.eclipse.kapua.service.datastore.internal.elasticsearch.DatastoreMediator;
import org.eclipse.kapua.service.datastore.internal.elasticsearch.EsSchema;
import org.eclipse.kapua.service.datastore.internal.elasticsearch.MessageField;
import org.eclipse.kapua.service.datastore.internal.model.query.AndPredicateImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.MessageQueryImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.RangePredicateImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.SortFieldImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.StorableFieldImpl;
import org.eclipse.kapua.service.datastore.model.ClientInfo;
import org.eclipse.kapua.service.datastore.model.ClientInfoListResult;
import org.eclipse.kapua.service.datastore.model.MessageListResult;
import org.eclipse.kapua.service.datastore.model.StorableId;
import org.eclipse.kapua.service.datastore.model.query.AndPredicate;
import org.eclipse.kapua.service.datastore.model.query.ClientInfoQuery;
import org.eclipse.kapua.service.datastore.model.query.MessageQuery;
import org.eclipse.kapua.service.datastore.model.query.RangePredicate;
import org.eclipse.kapua.service.datastore.model.query.SortDirection;
import org.eclipse.kapua.service.datastore.model.query.SortField;
import org.eclipse.kapua.service.datastore.model.query.StorableFetchStyle;
import org.eclipse.kapua.service.datastore.model.query.TermPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client information registry implementation.
 * 
 * @since 1.0.0
 */
@KapuaProvider
public class ClientInfoRegistryServiceImpl extends AbstractKapuaConfigurableService implements ClientInfoRegistryService {

    private static final Domain datastoreDomain = new DatastoreDomain();

    private static final Logger logger = LoggerFactory.getLogger(ClientInfoRegistryServiceImpl.class);

    private final AccountService accountService;
    private final AuthorizationService authorizationService;
    private final PermissionFactory permissionFactory;
    private final ClientInfoRegistryFacade clientInfoFacade;
    private final MessageStoreService messageStoreService;
    private final DatastoreObjectFactory datastoreObjectFactory;

    /**
     * Default constructor.
     * 
     * @since 1.0.0
     */
    public ClientInfoRegistryServiceImpl() {
        super(ClientInfoRegistryService.class.getName(), datastoreDomain, DatastoreEntityManagerFactory.getInstance());

        KapuaLocator locator = KapuaLocator.getInstance();
        accountService = locator.getService(AccountService.class);
        authorizationService = locator.getService(AuthorizationService.class);
        permissionFactory = locator.getFactory(PermissionFactory.class);
        messageStoreService = locator.getService(MessageStoreService.class);
        datastoreObjectFactory = KapuaLocator.getInstance().getFactory(DatastoreObjectFactory.class);

        MessageStoreService messageStoreService = KapuaLocator.getInstance().getService(MessageStoreService.class);
        ConfigurationProviderImpl configurationProvider = new ConfigurationProviderImpl(messageStoreService, accountService);
        this.clientInfoFacade = new ClientInfoRegistryFacade(configurationProvider, DatastoreMediator.getInstance());
        DatastoreMediator.getInstance().setClientInfoStoreFacade(this.clientInfoFacade);
    }

    @Override
    public void delete(KapuaId scopeId, StorableId id)
            throws KapuaException {
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(id, "id");

        checkAccess(scopeId, Actions.delete);

        try {
            clientInfoFacade.delete(scopeId, id);
        } catch (Exception e) {
            throw KapuaException.internalError(e);
        }
    }

    @Override
    public ClientInfo find(KapuaId scopeId, StorableId id)
            throws KapuaException {
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(id, "id");

        checkAccess(scopeId, Actions.read);

        try {
            ClientInfo clientInfo = clientInfoFacade.find(scopeId, id);

            // populate the lastMessageTimestamp
            updateLastPublishedFields(clientInfo);

            return clientInfo;
        } catch (Exception e) {
            throw KapuaException.internalError(e);
        }
    }

    @Override
    public ClientInfoListResult query(ClientInfoQuery query)
            throws KapuaException {
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        checkAccess(query.getScopeId(), Actions.read);

        try {
            ClientInfoListResult result = clientInfoFacade.query(query);

            // populate the lastMessageTimestamp
            for (ClientInfo clientInfo : result.getItems()) {
                updateLastPublishedFields(clientInfo);
            }

            return result;
        } catch (Exception e) {
            throw KapuaException.internalError(e);
        }
    }

    @Override
    public long count(ClientInfoQuery query)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        checkAccess(query.getScopeId(), Actions.read);

        try {
            return clientInfoFacade.count(query);
        } catch (Exception e) {
            throw KapuaException.internalError(e);
        }
    }

    @Override
    public void delete(ClientInfoQuery query)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        checkAccess(query.getScopeId(), Actions.delete);

        try {
            clientInfoFacade.delete(query);
        } catch (Exception e) {
            throw KapuaException.internalError(e);
        }
    }

    private void checkAccess(KapuaId scopeId, Actions action)
            throws KapuaException {
        //
        // Check Access
        Permission permission = permissionFactory.newPermission(datastoreDomain, action, scopeId);
        authorizationService.checkPermission(permission);
    }

    /**
     * Update the last published date and last published message identifier for the specified client info, so it gets the timestamp and the message identifier of the last published message for the
     * account/clientId in the client info
     * 
     * @param scopeId
     * @param channelInfo
     * 
     * @throws KapuaException
     * 
     * @since 1.0.0
     */
    private void updateLastPublishedFields(ClientInfo clientInfo) throws KapuaException {
        List<SortField> sort = new ArrayList<>();
        SortField sortTimestamp = new SortFieldImpl();
        sortTimestamp.setField(EsSchema.MESSAGE_TIMESTAMP);
        sortTimestamp.setSortDirection(SortDirection.DESC);
        sort.add(sortTimestamp);

        MessageQuery messageQuery = new MessageQueryImpl(clientInfo.getScopeId());
        messageQuery.setAskTotalCount(true);
        messageQuery.setFetchStyle(StorableFetchStyle.SOURCE_SELECT);
        messageQuery.setLimit(1);
        messageQuery.setOffset(0);
        messageQuery.setSortFields(sort);

        RangePredicate messageIdPredicate = new RangePredicateImpl(new StorableFieldImpl(EsSchema.CLIENT_TIMESTAMP), clientInfo.getFirstMessageOn(), null);
        TermPredicate clientIdPredicate = datastoreObjectFactory.newTermPredicate(MessageField.CLIENT_ID, clientInfo.getClientId());

        AndPredicate andPredicate = new AndPredicateImpl();
        andPredicate.getPredicates().add(messageIdPredicate);
        andPredicate.getPredicates().add(clientIdPredicate);
        messageQuery.setPredicate(andPredicate);

        MessageListResult messageList = messageStoreService.query(messageQuery);

        StorableId lastPublishedMessageId = null;
        Date lastPublishedMessageTimestamp = null;
        if (messageList.getSize() == 1) {
            lastPublishedMessageId = messageList.getFirstItem().getDatastoreId();
            lastPublishedMessageTimestamp = messageList.getFirstItem().getTimestamp();
        } else if (messageList.isEmpty()) {
            // this condition could happens due to the ttl of the messages (so if it happens, it does not necessarily mean there has been an error!)
            logger.warn("Cannot find last timestamp for the specified client id '{}' - account '{}'", new Object[] { clientInfo.getScopeId(), clientInfo.getClientId() });
        } else {
            // this condition shouldn't never happens since the query has a limit 1
            // if happens it means than an elasticsearch internal error happens and/or our driver didn't set it correctly!
            logger.error("Cannot find last timestamp for the specified client id '{}' - account '{}'. More than one result returned by the query!",
                    new Object[] { clientInfo.getScopeId(), clientInfo.getClientId() });
        }
        clientInfo.setLastMessageId(lastPublishedMessageId);
        clientInfo.setLastMessageOn(lastPublishedMessageTimestamp);
    }
}
