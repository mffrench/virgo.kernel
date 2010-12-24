/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class RegionHookBaseTests extends AbstractRegionHookTest {

    TestRegionHook testRegionHook;
    
    @Before
    public void setUp() {
        this.testRegionHook = new TestRegionHook(getRegionMembership());
    }

    @Test
    public void testIsSystemBundleBundleContext() {
        Assert.assertTrue(RegionHookBase.isSystemBundle(getBundleContext(SYSTEM_BUNDLE_INDEX)));
        Assert.assertFalse(RegionHookBase.isSystemBundle(getBundleContext(KERNEL_BUNDLE_INDEX)));
    }

    @Test
    public void testIsSystemBundleBundle() {
        Assert.assertTrue(RegionHookBase.isSystemBundle(getBundle(SYSTEM_BUNDLE_INDEX)));
        Assert.assertFalse(RegionHookBase.isSystemBundle(getBundle(KERNEL_BUNDLE_INDEX)));
    }

    @Test
    public void testIsUserRegionBundleBundleContext() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundleContext(SYSTEM_BUNDLE_INDEX)));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(getBundleContext(KERNEL_BUNDLE_INDEX)));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundleContext(USER_REGION_BUNDLE_INDEX)));
    }

    @Test
    public void testIsUserRegionBundleBundle() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundle(SYSTEM_BUNDLE_INDEX)));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(getBundle(KERNEL_BUNDLE_INDEX)));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundle(USER_REGION_BUNDLE_INDEX)));
    }

    @Test
    public void testIsUserRegionBundleLong() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundleId(SYSTEM_BUNDLE_INDEX)));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(getBundleId(KERNEL_BUNDLE_INDEX)));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundleId(USER_REGION_BUNDLE_INDEX)));
    }

    class TestRegionHook extends RegionHookBase {

        private TestRegionHook(RegionMembership regionMembership) {
            super(regionMembership);
        }

    }

}
