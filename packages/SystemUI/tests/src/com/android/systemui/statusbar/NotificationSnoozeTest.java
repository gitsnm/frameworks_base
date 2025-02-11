/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.systemui.statusbar;

import android.provider.Settings;
import android.test.suitebuilder.annotation.SmallTest;
import android.testing.AndroidTestingRunner;
import android.testing.TestableResources;
import android.testing.UiThreadTest;
import android.util.KeyValueListParser;

import com.android.systemui.R;
import com.android.systemui.SysuiTestCase;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper.SnoozeOption;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SmallTest
@RunWith(AndroidTestingRunner.class)
@UiThreadTest
public class NotificationSnoozeTest extends SysuiTestCase {
    private static final int RES_DEFAULT = 2;
    private static final int[] RES_OPTIONS = {1, 2, 3};
    private NotificationSnooze mNotificationSnooze;
    private KeyValueListParser mMockParser;

    @Before
    public void setUp() throws Exception {
        Settings.Global.putString(mContext.getContentResolver(),
                Settings.Global.NOTIFICATION_SNOOZE_OPTIONS, null);
        TestableResources resources = mContext.getOrCreateTestableResources();
        resources.addOverride(R.integer.config_notification_snooze_time_default, RES_DEFAULT);
        resources.addOverride(R.array.custom_config_notification_snooze_times, RES_OPTIONS);
        mNotificationSnooze = new NotificationSnooze(mContext, null);
        mMockParser = mock(KeyValueListParser.class);
    }

    @Test
    public void testParseIntArrayNull() throws Exception {
        when(mMockParser.getString(anyString(), isNull())).thenReturn(null);
        mNotificationSnooze.setKeyValueListParser(mMockParser);

        int[] result = mNotificationSnooze.parseIntArray("foo", RES_OPTIONS);
        assertEquals(RES_OPTIONS, result);
    }

    @Test
    public void testParseIntArrayLeadingSep() throws Exception {
        when(mMockParser.getString(anyString(), isNull())).thenReturn(":4:5:6");
        mNotificationSnooze.setKeyValueListParser(mMockParser);

        int[] result = mNotificationSnooze.parseIntArray("foo", RES_OPTIONS);
        assertEquals(RES_OPTIONS, result);
    }

    @Test
    public void testParseIntArrayEmptyItem() throws Exception {
        when(mMockParser.getString(anyString(), isNull())).thenReturn("4::6");
        mNotificationSnooze.setKeyValueListParser(mMockParser);

        int[] result = mNotificationSnooze.parseIntArray("foo", RES_OPTIONS);
        assertEquals(RES_OPTIONS, result);
    }

    @Test
    public void testParseIntArrayTrailingSep() throws Exception {
        when(mMockParser.getString(anyString(), isNull())).thenReturn("4:5:6:");
        mNotificationSnooze.setKeyValueListParser(mMockParser);

        int[] result = mNotificationSnooze.parseIntArray("foo", RES_OPTIONS);
        assertEquals(3, result.length);
        assertEquals(4, result[0]);  // respect order
        assertEquals(5, result[1]);
        assertEquals(6, result[2]);
    }

    @Test
    public void testParseIntArrayGoodData() throws Exception {
        when(mMockParser.getString(anyString(), isNull())).thenReturn("4:5:6");
        mNotificationSnooze.setKeyValueListParser(mMockParser);

        int[] result = mNotificationSnooze.parseIntArray("foo", RES_OPTIONS);
        assertEquals(3, result.length);
        assertEquals(4, result[0]);  // respect order
        assertEquals(5, result[1]);
        assertEquals(6, result[2]);
    }

    @Test
    public void testGetOptionsWithNoConfig() throws Exception {
        ArrayList<SnoozeOption> result = mNotificationSnooze.getDefaultSnoozeOptions();
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getMinutesToSnoozeFor());  // respect order
        assertEquals(2, result.get(1).getMinutesToSnoozeFor());
        assertEquals(3, result.get(2).getMinutesToSnoozeFor());
        assertEquals(2, mNotificationSnooze.getDefaultOption().getMinutesToSnoozeFor());
    }

    @Test
    public void testGetOptionsWithInvalidConfig() throws Exception {
        Settings.Global.putString(mContext.getContentResolver(),
                Settings.Global.NOTIFICATION_SNOOZE_OPTIONS,
                "this is garbage");
        ArrayList<SnoozeOption> result = mNotificationSnooze.getDefaultSnoozeOptions();
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getMinutesToSnoozeFor());  // respect order
        assertEquals(2, result.get(1).getMinutesToSnoozeFor());
        assertEquals(3, result.get(2).getMinutesToSnoozeFor());
        assertEquals(2, mNotificationSnooze.getDefaultOption().getMinutesToSnoozeFor());
    }

    @Test
    public void testGetOptionsWithValidDefault() throws Exception {
        Settings.Global.putString(mContext.getContentResolver(),
                Settings.Global.NOTIFICATION_SNOOZE_OPTIONS,
                "default=10,options_array=4:5:6:7");
        ArrayList<SnoozeOption> result = mNotificationSnooze.getDefaultSnoozeOptions();
        assertNotNull(mNotificationSnooze.getDefaultOption());  // pick one
    }

    @Test
    public void testGetOptionsWithValidConfig() throws Exception {
        Settings.Global.putString(mContext.getContentResolver(),
                Settings.Global.NOTIFICATION_SNOOZE_OPTIONS,
                "default=6,options_array=4:5:6:7");
        ArrayList<SnoozeOption> result = mNotificationSnooze.getDefaultSnoozeOptions();
        assertEquals(4, result.size());
        assertEquals(4, result.get(0).getMinutesToSnoozeFor());  // respect order
        assertEquals(5, result.get(1).getMinutesToSnoozeFor());
        assertEquals(6, result.get(2).getMinutesToSnoozeFor());
        assertEquals(7, result.get(3).getMinutesToSnoozeFor());
        assertEquals(6, mNotificationSnooze.getDefaultOption().getMinutesToSnoozeFor());
    }

    @Test
    public void testGetOptionsWithLongConfig() throws Exception {
        Settings.Global.putString(mContext.getContentResolver(),
                Settings.Global.NOTIFICATION_SNOOZE_OPTIONS,
                "default=6,options_array=4:5:6:7:8:9:10:11:12:13:14:15:16:17");
        ArrayList<SnoozeOption> result = mNotificationSnooze.getDefaultSnoozeOptions();
        assertTrue(result.size() > 3);
        assertEquals(4, result.get(0).getMinutesToSnoozeFor());  // respect order
        assertEquals(5, result.get(1).getMinutesToSnoozeFor());
        assertEquals(6, result.get(2).getMinutesToSnoozeFor());
    }
}
