package org.hisp.dhis.sdk.java.interpretation;

import org.hisp.dhis.java.sdk.models.common.Access;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.java.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.java.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.java.sdk.models.interpretation.InterpretationElement;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InterpretationElementServiceTest {
    private Interpretation interpretation;
    private DashboardElement dashboardElement;
    private InterpretationElement interpretationElement;
    private IInterpretationElementService interpretationElementService;

    @Before
    public void setUp() {
        interpretation = new Interpretation();
        dashboardElement = new DashboardElement();
        interpretationElement = new InterpretationElement();
        interpretationElementService = new InterpretationElementService();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationElementShouldThrowExceptionOnNullInterpretation() {
        interpretationElementService.create(null, dashboardElement, "SomeType");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationElementShouldThrowExceptionOnNullDashboardElement() {
        interpretationElementService.create(interpretation, null, "SomeType");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationElementShouldThrowExceptionOnNullMimeType() {
        interpretationElementService.create(interpretation, dashboardElement, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInterpretationElementShouldThrowExceptionOnUnsupportedMimeType() {
        interpretationElementService.create(interpretation, dashboardElement, "UnsupportedType");
    }

    @Test
    public void testCreateInterpretationElementShouldSupportGivenMimeTypes() {
        interpretationElementService.create(interpretation, dashboardElement, DashboardContent.TYPE_CHART);
        interpretationElementService.create(interpretation, dashboardElement, DashboardContent.TYPE_MAP);
        interpretationElementService.create(interpretation, dashboardElement, DashboardContent.TYPE_REPORT_TABLE);
    }

    @Test
    public void testCreateInterpretationElementShouldReturnValidObject() {
        DateTime dateTime = DateTime.now();
        Access access = Access.createDefaultAccess();

        dashboardElement.setUId("omsdfjadf");
        dashboardElement.setName("SomeName");
        dashboardElement.setDisplayName("SomeDisplayName");
        dashboardElement.setCreated(dateTime);
        dashboardElement.setLastUpdated(dateTime);
        dashboardElement.setAccess(access);

        InterpretationElement interpretationElement = interpretationElementService.create(
                interpretation, dashboardElement, DashboardContent.TYPE_CHART);

        assertEquals(interpretationElement.getUId(), dashboardElement.getUId());
        assertEquals(interpretationElement.getName(), dashboardElement.getName());
        assertEquals(interpretationElement.getDisplayName(), dashboardElement.getDisplayName());
        assertEquals(interpretationElement.getCreated(), dashboardElement.getCreated());
        assertEquals(interpretationElement.getLastUpdated(), dashboardElement.getLastUpdated());
        assertEquals(interpretationElement.getAccess(), dashboardElement.getAccess());
        assertEquals(interpretationElement.getType(), DashboardContent.TYPE_CHART);
        assertEquals(interpretationElement.getInterpretation(), interpretation);
    }
}
