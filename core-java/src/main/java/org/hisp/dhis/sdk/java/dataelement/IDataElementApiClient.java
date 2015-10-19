package org.hisp.dhis.sdk.java.dataelement;

import org.hisp.dhis.java.sdk.models.dataelement.DataElement;
import org.joda.time.DateTime;

import java.util.List;

public interface IDataElementApiClient {
    List<DataElement> getBasicDataElements(DateTime lastUpdated);

    List<DataElement> getFullDataElements(DateTime lastUpdated);
}
