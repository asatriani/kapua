/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.datastore.internal.client;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.assertj.core.api.Assertions;
import org.eclipse.kapua.service.datastore.internal.mediator.DatastoreUtils;
import org.junit.Assert;
import org.junit.Test;

public class ClientUtilsConvertDateTest {

    @Test
    public void convertNull1() {
        Assert.assertNull(DatastoreUtils.convertToCorrectType(DatastoreUtils.CLIENT_METRIC_TYPE_DATE_ACRONYM, null));
    }

    @Test
    public void convertWithMillis1() {
        Assertions.assertThat(DatastoreUtils.convertToCorrectType(DatastoreUtils.CLIENT_METRIC_TYPE_DATE_ACRONYM, "2017-01-02T12:34:56.123Z"))
                .isInstanceOf(Date.class)
                .isEqualTo(Date.from(ZonedDateTime.of(2017, 1, 2, 12, 34, 56, 123_000_000, ZoneOffset.UTC).toInstant()));
    }
    
    @Test
    public void convertWithMillis2() {
        Assertions.assertThat(DatastoreUtils.convertToCorrectType(DatastoreUtils.CLIENT_METRIC_TYPE_DATE_ACRONYM, "2017-01-02T12:34:56.123"))
                .isInstanceOf(Date.class)
                .isEqualTo(Date.from(ZonedDateTime.of(2017, 1, 2, 12, 34, 56, 123_000_000, ZoneOffset.UTC).toInstant()));
    }

    @Test
    public void convertNoMillis1() {
        Assertions.assertThat(DatastoreUtils.convertToCorrectType(DatastoreUtils.CLIENT_METRIC_TYPE_DATE_ACRONYM, "2017-01-02T13:34:56Z"))
                .isInstanceOf(Date.class)
                .isEqualTo(Date.from(ZonedDateTime.of(2017, 1, 2, 13, 34, 56, 0, ZoneOffset.UTC).toInstant()));
    }
    
    @Test
    public void convertNoMillis2() {
        Assertions.assertThat(DatastoreUtils.convertToCorrectType(DatastoreUtils.CLIENT_METRIC_TYPE_DATE_ACRONYM, "2017-01-02T13:34:56"))
                .isInstanceOf(Date.class)
                .isEqualTo(Date.from(ZonedDateTime.of(2017, 1, 2, 13, 34, 56, 0, ZoneOffset.UTC).toInstant()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertWrongFormat() {
        DatastoreUtils.convertToCorrectType(DatastoreUtils.CLIENT_METRIC_TYPE_DATE_ACRONYM, "");
    }
}
