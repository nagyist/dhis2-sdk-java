/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.java.sdk.common.persistence;

import org.hisp.dhis.java.sdk.common.IFailedItemStore;
import org.hisp.dhis.java.sdk.common.IStateStore;
import org.hisp.dhis.java.sdk.dashboard.IDashboardItemContentStore;
import org.hisp.dhis.java.sdk.dashboard.IDashboardItemStore;
import org.hisp.dhis.java.sdk.dashboard.IDashboardStore;
import org.hisp.dhis.java.sdk.dashboard.IDashboardElementStore;
import org.hisp.dhis.java.sdk.dataset.IDataSetStore;
import org.hisp.dhis.java.sdk.enrollment.IEnrollmentStore;
import org.hisp.dhis.java.sdk.event.IEventStore;
import org.hisp.dhis.java.sdk.interpretation.IInterpretationCommentStore;
import org.hisp.dhis.java.sdk.interpretation.IInterpretationElementStore;
import org.hisp.dhis.java.sdk.models.constant.Constant;
import org.hisp.dhis.java.sdk.models.dataelement.DataElement;
import org.hisp.dhis.java.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.java.sdk.models.optionset.OptionSet;
import org.hisp.dhis.java.sdk.models.relationship.RelationshipType;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntity;
import org.hisp.dhis.java.sdk.models.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.java.sdk.optionset.IOptionStore;
import org.hisp.dhis.java.sdk.organisationunit.IOrganisationUnitStore;
import org.hisp.dhis.java.sdk.program.*;
import org.hisp.dhis.java.sdk.relationship.IRelationshipStore;
import org.hisp.dhis.java.sdk.trackedentity.ITrackedEntityAttributeValueStore;
import org.hisp.dhis.java.sdk.trackedentity.ITrackedEntityDataValueStore;
import org.hisp.dhis.java.sdk.trackedentity.ITrackedEntityInstanceStore;
import org.hisp.dhis.java.sdk.user.IUserAccountStore;
import org.hisp.dhis.java.sdk.user.IUserStore;

public interface IPersistenceModule {
    ITransactionManager getTransactionManager();

    IStateStore getStateStore();

    IDashboardStore getDashboardStore();

    IDashboardItemStore getDashboardItemStore();

    IDashboardElementStore getDashboardElementStore();

    IDashboardItemContentStore getDashboardContentStore();

    IIdentifiableObjectStore<Constant> getConstantStore();

    IIdentifiableObjectStore<DataElement> getDataElementStore();

    IOptionStore getOptionStore();

    IIdentifiableObjectStore<OptionSet> getOptionSetStore();

    IOrganisationUnitStore getOrganisationUnitStore();

    IProgramStore getProgramStore();

    IIdentifiableObjectStore<TrackedEntity> getTrackedEntityStore();

    IIdentifiableObjectStore<TrackedEntityAttribute> getTrackedEntityAttributeStore();

    IProgramTrackedEntityAttributeStore getProgramTrackedEntityAttributeStore();

    IProgramStageDataElementStore getProgramStageDataElementStore();

    IProgramIndicatorStore getProgramIndicatorStore();

    IProgramStageSectionStore getProgramStageSectionStore();

    IProgramStageStore getProgramStageStore();

    IProgramRuleStore getProgramRuleStore();

    IProgramRuleActionStore getProgramRuleActionStore();

    IProgramRuleVariableStore getProgramRuleVariableStore();

    IIdentifiableObjectStore<RelationshipType> getRelationshipTypeStore();

    IDataSetStore getDataStore();

    //Tracker store objects
    ITrackedEntityAttributeValueStore getTrackedEntityAttributeValueStore();

    IRelationshipStore getRelationshipStore();

    ITrackedEntityInstanceStore getTrackedEntityInstanceStore();

    ITrackedEntityDataValueStore getTrackedEntityDataValueStore();

    IEventStore getEventStore();

    IEnrollmentStore getEnrollmentStore();

    // Interpretation store objects
    IIdentifiableObjectStore<Interpretation> getInterpretationStore();

    IInterpretationCommentStore getInterpretationCommentStore();

    IInterpretationElementStore getInterpretationElementStore();

    // User store object
    IUserAccountStore getUserAccountStore();

    IUserStore getUserStore();

    IFailedItemStore getFailedItemStore();

}
