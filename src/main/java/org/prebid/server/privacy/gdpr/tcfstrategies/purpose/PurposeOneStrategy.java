package org.prebid.server.privacy.gdpr.tcfstrategies.purpose;

import org.prebid.server.privacy.gdpr.model.PrivacyEnforcementAction;
import org.prebid.server.privacy.gdpr.tcfstrategies.purpose.typestrategies.BasicEnforcePurposeStrategy;
import org.prebid.server.privacy.gdpr.tcfstrategies.purpose.typestrategies.FullEnforcePurposeStrategy;
import org.prebid.server.privacy.gdpr.tcfstrategies.purpose.typestrategies.NoEnforcePurposeStrategy;

public class PurposeOneStrategy extends PurposeStrategy {

    private static final int PURPOSE_ID = 1;

    public PurposeOneStrategy(FullEnforcePurposeStrategy fullEnforcePurposeStrategy,
                              BasicEnforcePurposeStrategy basicEnforcePurposeStrategy,
                              NoEnforcePurposeStrategy noEnforcePurposeStrategy) {
        super(fullEnforcePurposeStrategy, basicEnforcePurposeStrategy, noEnforcePurposeStrategy);
    }

    @Override
    public void allow(PrivacyEnforcementAction privacyEnforcementAction) {
        privacyEnforcementAction.setBlockPixelSync(false);
    }

    @Override
    public void allowNaturally(PrivacyEnforcementAction privacyEnforcementAction) {
    }

    @Override
    public int getPurposeId() {
        return PURPOSE_ID;
    }
}

