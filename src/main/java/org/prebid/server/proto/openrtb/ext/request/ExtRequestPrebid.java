package org.prebid.server.proto.openrtb.ext.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Defines the contract for bidrequest.ext.prebid
 */
@Builder(toBuilder = true)
@Value
public class ExtRequestPrebid {

    /**
     * Defines the contract for bidrequest.ext.prebid.debug
     */
    Integer debug;

    /**
     * Defines the contract for bidrequest.ext.prebid.aliases
     */
    Map<String, String> aliases;

    /**
     * Defines the contract for bidrequest.ext.prebid.aliasgvlids
     */
    Map<String, Integer> aliasgvlids;

    /**
     * Defines the contract for bidrequest.ext.prebid.bidadjustmentfactors
     */
    Map<String, BigDecimal> bidadjustmentfactors;

    /**
     * Defines the contract for bidrequest.ext.prebid.currency
     */
    ExtRequestCurrency currency;

    /**
     * Defines the contract for bidrequest.ext.prebid.targeting
     */
    ExtRequestTargeting targeting;

    /**
     * Defines the contract for bidrequest.ext.prebid.storedrequest
     */
    ExtStoredRequest storedrequest;

    /**
     * Defines the contract for bidrequest.ext.prebid.cache
     */
    ExtRequestPrebidCache cache;

    /**
     * Defines the contract for bidrequest.ext.prebid.data
     */
    ExtRequestPrebidData data;

    /**
     * Defines the contract for bidrequest.ext.prebid.bidderconfig
     */
    List<ExtRequestPrebidBidderConfig> bidderconfig;

    /**
     * Defines the contract for bidrequest.ext.prebid.events
     */
    ObjectNode events;

    /**
     * Defines the contract for bidrequest.ext.prebid.schains
     */
    List<ExtRequestPrebidSchain> schains;

    /**
     * Defines the contract for bidrequest.ext.prebid.nosale
     */
    List<String> nosale;

    /**
     * Defines the contract for bidrequest.ext.prebid.auctiontimestamp
     */
    Long auctiontimestamp;

    /**
     * Defines the contract for bidrequest.ext.prebid.bidders
     */
    ObjectNode bidders;

    /**
     * Defines the contract for bidrequest.ext.prebid.amp
     */
    ExtRequestPrebidAmp amp;

    /**
     * Defines the contract for bidrequest.ext.prebid.adservertargeting
     */
    List<ExtRequestPrebidAdservertargetingRule> adservertargeting;
}
