package org.telegram.tabgram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CompactSponsoredPillTest {

    @Test
    public void rowIsTallEnoughForTheImpressionCheck() {
        assertTrue(CompactSponsoredPill.ROW_HEIGHT_DP > 25);
    }

    @Test
    public void pillFitsInsideTheRow() {
        assertTrue(CompactSponsoredPill.PILL_HEIGHT_DP < CompactSponsoredPill.ROW_HEIGHT_DP);
    }

    @Test
    public void labelIsAtTheStartInLtr() {
        assertTrue(CompactSponsoredPill.isOnLabel(false, 15, 0, 100, 20));
        assertFalse(CompactSponsoredPill.isOnLabel(false, 30, 0, 100, 20));
    }

    @Test
    public void labelIsAtTheEndInRtl() {
        assertTrue(CompactSponsoredPill.isOnLabel(true, 85, 0, 100, 20));
        assertFalse(CompactSponsoredPill.isOnLabel(true, 70, 0, 100, 20));
    }

    @Test
    public void touchAcceptsVerticalSlop() {
        assertTrue(CompactSponsoredPill.isInsidePill(10, 20, 110, 40, 50, 17, 3));
        assertTrue(CompactSponsoredPill.isInsidePill(10, 20, 110, 40, 50, 43, 3));
        assertFalse(CompactSponsoredPill.isInsidePill(10, 20, 110, 40, 50, 16, 3));
    }

    @Test
    public void touchRejectsOutsideHorizontally() {
        assertFalse(CompactSponsoredPill.isInsidePill(10, 20, 110, 40, 9, 30, 3));
        assertFalse(CompactSponsoredPill.isInsidePill(10, 20, 110, 40, 111, 30, 3));
        assertTrue(CompactSponsoredPill.isInsidePill(10, 20, 110, 40, 110, 30, 3));
    }
}
