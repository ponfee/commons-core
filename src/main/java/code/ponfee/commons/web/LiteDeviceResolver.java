/*
 * Copyright 2010-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package code.ponfee.commons.web;

import static com.google.common.collect.ImmutableList.of;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;

/**
 * A "lightweight" device resolver algorithm based on Wordpress's Mobile pack. Detects the
 * presence of a mobile device and works for a large percentage of mobile browsers. Does
 * not perform any device capability mapping, if you need that consider WURFL.
 * 
 * The code is based primarily on a list of approximately 90 well-known mobile browser UA
 * string snippets, with a couple of special cases for Opera Mini, the W3C default
 * delivery context and certain other Windows browsers. The code also looks to see if the
 * browser advertises WAP capabilities as a hint.
 * 
 * Tablet resolution is also performed based on known tablet browser UA strings. Android
 * tablets are detected based on <a href=
 * "http://googlewebmastercentral.blogspot.com/2011/03/mo-better-to-also-detect-mobile-user.html"
 * >Google's recommendations</a>.
 * 
 * @author Keith Donald
 * @author Roy Clarkson
 * @author Scott Rossillo
 * @author Yuri Mednikov
 * @author Onur Kagan Ozcan
 * 
 * Modify from org.springframework.mobile.device.LiteDeviceResolver
 * @see org.springframework.mobile.device.LiteDeviceResolver
 */
public class LiteDeviceResolver {

    private final List<String> normalUserAgentKeywords = new ArrayList<>();

    public LiteDeviceResolver() {
        this(null);
    }

    public LiteDeviceResolver(List<String> normalUserAgentKeywords) {
        if (CollectionUtils.isNotEmpty(normalUserAgentKeywords)) {
            this.normalUserAgentKeywords.addAll(normalUserAgentKeywords);
        }
    }

    public LiteDevice resolveDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        // UserAgent keyword detection of Normal devices
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            for (String keyword : normalUserAgentKeywords) {
                if (userAgent.contains(keyword)) {
                    return resolveFallback(request);
                }
            }
        }
        // UserAgent keyword detection of Tablet devices
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            // Android special case
            if (userAgent.contains("android") && !userAgent.contains("mobile")) {
                return resolveWithPlatform(DeviceType.TABLET, DevicePlatform.ANDROID);
            }
            // Apple special case
            if (userAgent.contains("ipad")) {
                return resolveWithPlatform(DeviceType.TABLET, DevicePlatform.IOS);
            }
            // Kindle Fire special case
            if (userAgent.contains("silk") && !userAgent.contains("mobile")) {
                return resolveWithPlatform(DeviceType.TABLET, DevicePlatform.UNKNOWN);
            }
            for (String keyword : KNOWN_TABLET_USER_AGENT_KEYWORDS) {
                if (userAgent.contains(keyword)) {
                    return resolveWithPlatform(DeviceType.TABLET, DevicePlatform.UNKNOWN);
                }
            }
        }
        // UAProf detection
        if (request.getHeader("x-wap-profile") != null || request.getHeader("Profile") != null) {
            if (userAgent != null) {
                // Android special case
                if (userAgent.contains("android")) {
                    return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.ANDROID);
                }
                // Apple special case
                if (userAgent.contains("iphone") || userAgent.contains("ipod") || userAgent.contains("ipad")) {
                    return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.IOS);
                }
            }
            return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
        }
        // User-Agent prefix detection
        if (userAgent != null && userAgent.length() >= 4) {
            String prefix = userAgent.substring(0, 4).toLowerCase();
            if (KNOWN_MOBILE_USER_AGENT_PREFIXES.contains(prefix)) {
                return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
            }
        }
        // Accept-header based detection
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("wap")) {
            return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
        }
        // UserAgent keyword detection for Mobile devices
        if (userAgent != null) {
            // Android special case
            if (userAgent.contains("android")) {
                return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.ANDROID);
            }
            // Apple special case
            if (userAgent.contains("iphone") || userAgent.contains("ipod") || userAgent.contains("ipad")) {
                return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.IOS);
            }
            for (String keyword : KNOWN_MOBILE_USER_AGENT_KEYWORDS) {
                if (userAgent.contains(keyword)) {
                    return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
                }
            }
        }
        // OperaMini special case
        @SuppressWarnings("rawtypes") Enumeration headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            if (header.contains("OperaMini")) {
                /*return LiteDevice.MOBILE_INSTANCE;*/
                return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
            }
        }
        return resolveFallback(request);
    }

    // subclassing hooks

    /**
     * Wrapper method for allow subclassing platform based resolution
     */
    protected LiteDevice resolveWithPlatform(DeviceType deviceType, DevicePlatform devicePlatform) {
        return LiteDevice.from(deviceType, devicePlatform);
    }

    /**
     * Fallback called if no mobile device is matched by this resolver. The default
     * implementation of this method returns a "normal" {@link Device} that is neither
     * mobile or a tablet. Subclasses may override to try additional mobile or tablet
     * device matching before falling back to a "normal" device.
     */
    protected LiteDevice resolveFallback(HttpServletRequest request) {
        return LiteDevice.NORMAL_INSTANCE;
    }

    // internal helpers
    private static final List<String> KNOWN_MOBILE_USER_AGENT_PREFIXES = of(
        "w3c ", "w3c-", "acs-", "alav", "alca", "amoi", "audi", "avan", "benq",
        "bird", "blac", "blaz", "brew", "cell", "cldc", "cmd-", "dang", "doco",
        "eric", "hipt", "htc_", "inno", "ipaq", "ipod", "jigs", "kddi", "keji",
        "leno", "lg-c", "lg-d", "lg-g", "lge-", "lg/u", "maui", "maxo", "midp",
        "mits", "mmef", "mobi", "mot-", "moto", "mwbp", "nec-", "newt", "noki",
        "palm", "pana", "pant", "phil", "play", "port", "prox", "qwap", "sage",
        "sams", "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-",
        "siem", "smal", "smar", "sony", "sph-", "symb", "t-mo", "teli", "tim-",
        "tosh", "tsm-", "upg1", "upsi", "vk-v", "voda", "wap-", "wapa", "wapi",
        "wapp", "wapr", "webc", "winw", "winw", "xda ", "xda-" 
    );

    private static final List<String> KNOWN_MOBILE_USER_AGENT_KEYWORDS = of(
        "blackberry", "webos", "ipod", "lge vx", "midp", "maemo", "mmp", "mobile",
        "netfront", "hiptop", "nintendo DS", "novarra", "openweb", "opera mobi",
        "opera mini", "palm", "psp", "phone", "smartphone", "symbian", "up.browser",
        "up.link", "wap", "windows ce" 
    );

    private static final List<String> KNOWN_TABLET_USER_AGENT_KEYWORDS = of(
        "ipad", "playbook", "hp-tablet", "kindle" 
    );

}
