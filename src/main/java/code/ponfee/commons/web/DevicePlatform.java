/*
 * Copyright 2002-2014 the original author or authors.
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
 * Enumeration for the platform of device that has been resolved
 * 
 * @author Onur Kagan Ozcan
 * 
 * Modify from org.springframework.mobile.device.DevicePlatform
 * @see org.springframework.mobile.device.DevicePlatform
 */
public enum DevicePlatform {

    /**
     * Represents an apple platform
     */
    IOS,

    /**
     * Represents an android platform
     */
    ANDROID,

    /**
     * Represents unknown platform
     */
    UNKNOWN
}
