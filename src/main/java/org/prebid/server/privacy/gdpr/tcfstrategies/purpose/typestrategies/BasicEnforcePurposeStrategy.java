package org.prebid.server.privacy.gdpr.tcfstrategies.purpose.typestrategies;

import com.iabtcf.decoder.TCString;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.prebid.server.privacy.gdpr.model.VendorPermission;
import org.prebid.server.privacy.gdpr.model.VendorPermissionWithGvl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BasicEnforcePurposeStrategy extends EnforcePurposeStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BasicEnforcePurposeStrategy.class);

    public Collection<VendorPermission> allowedByTypeStrategy(int purposeId,
                                                              TCString vendorConsent,
                                                              Collection<VendorPermissionWithGvl> vendorsForPurpose,
                                                              Collection<VendorPermissionWithGvl> excludedVendors,
                                                              boolean isEnforceVendors) {

        logger.debug("Basic strategy used fo purpose {0}", purposeId);
        final List<VendorPermission> allowedVendorPermissions = vendorsForPurpose.stream()
                .map(VendorPermissionWithGvl::getVendorPermission)
                .filter(vendorPermission -> vendorPermission.getVendorId() != null)
                .filter(vendorPermission -> isAllowedBySimpleConsent(purposeId,
                        vendorPermission.getVendorId(), isEnforceVendors, vendorConsent))
                .collect(Collectors.toList());

        return CollectionUtils.union(allowedVendorPermissions, toVendorPermissions(excludedVendors));
    }
}
