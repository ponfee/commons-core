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

/**
 * A lightweight Device implementation suitable for use as support code.
 * Typically used to hold the output of a device resolution invocation.
 * 
 * @author Keith Donald
 * @author Roy Clarkson
 * @author Scott Rossillo
 * @author Onur Kagan Ozcan
 * 
 * Modify from org.springframework.mobile.device.LiteDevice
 * @see org.springframework.mobile.device.LiteDevice
 */
public class LiteDevice {

    public static final LiteDevice NORMAL_INSTANCE = new LiteDevice(DeviceType.NORMAL, DevicePlatform.UNKNOWN);
    public static final LiteDevice MOBILE_INSTANCE = new LiteDevice(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
    public static final LiteDevice TABLET_INSTANCE = new LiteDevice(DeviceType.TABLET, DevicePlatform.UNKNOWN);

    public boolean isNormal() {
        return this.deviceType == DeviceType.NORMAL;
    }

    public boolean isMobile() {
        return this.deviceType == DeviceType.MOBILE;
    }

    public boolean isTablet() {
        return this.deviceType == DeviceType.TABLET;
    }

    public DevicePlatform getDevicePlatform() {
        return this.devicePlatform;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public static LiteDevice from(DeviceType deviceType, DevicePlatform devicePlatform) {
        return new LiteDevice(deviceType, devicePlatform);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[LiteDevice type=")
             .append(this.deviceType).append("]").toString();
    }

    private final DeviceType deviceType;

    private final DevicePlatform devicePlatform;

    /**
     * Creates a LiteDevice with DevicePlatform.
     */
    private LiteDevice(DeviceType deviceType, DevicePlatform devicePlatform) {
        this.deviceType = deviceType;
        this.devicePlatform = devicePlatform;
    }

}
