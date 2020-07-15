package org.prebid.server.bidder.beintoo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iab.openrtb.request.Banner;
import com.iab.openrtb.request.BidRequest;
import com.iab.openrtb.request.Device;
import com.iab.openrtb.request.Format;
import com.iab.openrtb.request.Imp;
import com.iab.openrtb.request.Site;
import com.iab.openrtb.response.Bid;
import com.iab.openrtb.response.BidResponse;
import com.iab.openrtb.response.SeatBid;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.prebid.server.bidder.Bidder;
import org.prebid.server.bidder.model.BidderBid;
import org.prebid.server.bidder.model.BidderError;
import org.prebid.server.bidder.model.HttpCall;
import org.prebid.server.bidder.model.HttpRequest;
import org.prebid.server.bidder.model.Result;
import org.prebid.server.exception.PreBidException;
import org.prebid.server.json.DecodeException;
import org.prebid.server.json.JacksonMapper;
import org.prebid.server.proto.openrtb.ext.ExtPrebid;
import org.prebid.server.proto.openrtb.ext.request.beintoo.ExtImpBeintoo;
import org.prebid.server.proto.openrtb.ext.response.BidType;
import org.prebid.server.util.HttpUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BeintooBidder implements Bidder<BidRequest> {

    private static final TypeReference<ExtPrebid<?, ExtImpBeintoo>> BEINTOO_EXT_TYPE_REFERENCE =
            new TypeReference<ExtPrebid<?, ExtImpBeintoo>>() {
            };

    private static final String DEFAULT_BID_CURRENCY = "USD";

    private final String endpointUrl;
    private final JacksonMapper mapper;

    public BeintooBidder(String endpointUrl, JacksonMapper mapper) {
        this.endpointUrl = HttpUtil.validateUrl(Objects.requireNonNull(endpointUrl));
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public Result<List<HttpRequest<BidRequest>>> makeHttpRequests(BidRequest request) {
        final List<BidderError> errors = new ArrayList<>();
        if (CollectionUtils.isEmpty(request.getImp())) {
            errors.add(BidderError.badInput("No valid impressions in the bid request"));
            return Result.of(Collections.emptyList(), errors);
        }

        final BidRequest updatedBidRequest;
        try {
            updatedBidRequest = updateBidRequest(request);
        } catch (PreBidException e) {
            return Result.emptyWithError(BidderError.badInput(e.getMessage()));
        }

        final String body = mapper.encode(updatedBidRequest);
        final MultiMap headers = makeHeaders(request);

        return Result.of(Collections.singletonList(
                HttpRequest.<BidRequest>builder()
                        .method(HttpMethod.POST)
                        .uri(endpointUrl)
                        .body(body)
                        .headers(headers)
                        .payload(request)
                        .build()), errors);
    }

    private BidRequest updateBidRequest(BidRequest request) {
        final boolean isSecure = isSecure(request.getSite());

        final List<Imp> modifiedImps = request.getImp().stream()
                .map(imp -> modifyImp(imp, isSecure, parseAndValidateImpExt(imp)))
                .collect(Collectors.toList());

        return request.toBuilder()
                .imp(modifiedImps)
                .build();
    }

    private static boolean isSecure(Site site) {
        return site != null && StringUtils.isNotBlank(site.getPage()) && site.getPage()
                .startsWith("https");
    }

    private ExtImpBeintoo parseAndValidateImpExt(Imp imp) {
        final ExtImpBeintoo extImpBeintoo;
        try {
            extImpBeintoo = mapper.mapper().convertValue(imp.getExt(), BEINTOO_EXT_TYPE_REFERENCE).getBidder();
        } catch (IllegalArgumentException e) {
            throw new PreBidException(e.getMessage(), e);
        }

        final int tagidNumber;
        try {
            tagidNumber = Integer.parseInt(extImpBeintoo.getTagId());
        } catch (NumberFormatException e) {
            throw new PreBidException(
                    String.format("tagid must be a String of numbers, ignoring imp id=%s",
                            imp.getId()), e);
        }

        if (tagidNumber == 0) {
            throw new PreBidException(String.format("tagid cant be 0, ignoring imp id=%s",
                    imp.getId()));
        }

        return extImpBeintoo;
    }

    private static Imp modifyImp(Imp imp, boolean isSecure, ExtImpBeintoo extImpBeintoo) {
        final Banner banner = modifyImpBanner(imp.getBanner());

        final Imp.ImpBuilder impBuilder = imp.toBuilder()
                .tagid(extImpBeintoo.getTagId())
                .secure(BooleanUtils.toInteger(isSecure))
                .banner(banner)
                .ext(null);

        final String stringBidfloor = extImpBeintoo.getBidFloor();
        if (StringUtils.isBlank(stringBidfloor)) {
            return impBuilder.build();
        }

        final BigDecimal bidfloor;
        try {
            bidfloor = new BigDecimal(stringBidfloor);
        } catch (NumberFormatException e) {
            return impBuilder.build();
        }

        return impBuilder
                .bidfloor(bidfloor)
                .build();
    }

    private static Banner modifyImpBanner(Banner banner) {
        if (banner == null) {
            throw new PreBidException("Request needs to include a Banner object");
        }

        if (banner.getW() == null && banner.getH() == null) {
            final Banner.BannerBuilder bannerBuilder = banner.toBuilder();
            final List<Format> originalFormat = banner.getFormat();

            if (originalFormat == null || originalFormat.isEmpty()) {
                throw new PreBidException("Need at least one size to build request");
            }

            final List<Format> formatSkipFirst = originalFormat.subList(1, originalFormat.size());
            bannerBuilder.format(formatSkipFirst);

            Format firstFormat = originalFormat.get(0);
            bannerBuilder.w(firstFormat.getW());
            bannerBuilder.h(firstFormat.getH());

            return bannerBuilder.build();
        }

        return banner;
    }

    private static MultiMap makeHeaders(BidRequest request) {
        final MultiMap headers = HttpUtil.headers();

        final Device device = request.getDevice();
        if (device != null) {
            HttpUtil.addHeaderIfValueIsNotEmpty(headers, HttpUtil.USER_AGENT_HEADER,
                    device.getUa());
            HttpUtil.addHeaderIfValueIsNotEmpty(headers, HttpUtil.X_FORWARDED_FOR_HEADER,
                    device.getIp());
            HttpUtil.addHeaderIfValueIsNotEmpty(headers, HttpUtil.ACCEPT_LANGUAGE_HEADER,
                    device.getLanguage());
            if (device.getDnt() != null) {
                HttpUtil.addHeaderIfValueIsNotEmpty(headers, HttpUtil.DNT_HEADER,
                        String.valueOf(device.getDnt()));
            }
        }

        final Site site = request.getSite();
        if (site != null && StringUtils.isNotBlank(site.getPage())) {
            HttpUtil.addHeaderIfValueIsNotEmpty(headers, HttpUtil.REFERER_HEADER, site.getPage());
        }

        return headers;
    }

    @Override
    public Result<List<BidderBid>> makeBids(HttpCall<BidRequest> httpCall, BidRequest bidRequest) {
        try {
            final BidResponse bidResponse = mapper.decodeValue(httpCall.getResponse().getBody(), BidResponse.class);
            return Result.of(extractBids(bidResponse), Collections.emptyList());
        } catch (DecodeException | PreBidException e) {
            return Result.emptyWithError(BidderError.badServerResponse(e.getMessage()));
        }
    }

    private static List<BidderBid> extractBids(BidResponse bidResponse) {
        return bidResponse == null || bidResponse.getSeatbid() == null
                ? Collections.emptyList()
                : bidsFromResponse(bidResponse);
    }

    private static List<BidderBid> bidsFromResponse(BidResponse bidResponse) {
        return bidResponse.getSeatbid().stream()
                .filter(Objects::nonNull)
                .map(SeatBid::getBid)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(bid -> BidderBid.of(modifyBid(bid), BidType.banner, DEFAULT_BID_CURRENCY))
                .collect(Collectors.toList());
    }

    private static Bid modifyBid(Bid bid) {
        return bid.toBuilder().impid(bid.getId()).build();
    }

    @Override
    public Map<String, String> extractTargeting(ObjectNode ext) {
        return Collections.emptyMap();
    }
}
