package com.topjohnwu.magisk;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.channel.AbstractChannelListener;
import com.microsoft.appcenter.channel.Channel;
import com.microsoft.appcenter.crashes.AbstractCrashesListener;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.appcenter.crashes.model.ErrorReport;
import com.microsoft.appcenter.ingestion.models.Log;

import java.util.ArrayList;
import java.util.Map;

public class Telemetry {
    private static final Channel.Listener patchDeviceListener = new AbstractChannelListener() {
        @Override
        public void onPreparedLog(@NonNull Log log, @NonNull String groupName, int flags) {
            var device = log.getDevice();
            device.setAppVersion(BuildConfig.VERSION_NAME);
            device.setAppBuild(String.valueOf(BuildConfig.VERSION_CODE));
            device.setAppNamespace(BuildConfig.APPLICATION_ID);
        }
    };

    private static void addPatchDeviceListener() {
        try {
            var channelField = AppCenter.class.getDeclaredField("mChannel");
            channelField.setAccessible(true);
            var channel = (Channel) channelField.get(AppCenter.getInstance());
            assert channel != null;
            channel.addListener(patchDeviceListener);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void patchDevice() {
        try {
            var handlerField = AppCenter.class.getDeclaredField("mHandler");
            handlerField.setAccessible(true);
            var handler = ((Handler) handlerField.get(AppCenter.getInstance()));
            assert handler != null;
            handler.post(Telemetry::addPatchDeviceListener);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void start(Application app, String text, String fileName) {
        Crashes.setListener(new AbstractCrashesListener() {
            @Override
            public Iterable<ErrorAttachmentLog> getErrorAttachments(ErrorReport report) {
                var list = new ArrayList<ErrorAttachmentLog>(1);
                list.add(ErrorAttachmentLog.attachmentWithText(text, fileName));
                return list;
            }
        });
        AppCenter.start(app, "051a74ce-6ccb-4580-bed9-52d4437715af",
                Analytics.class, Crashes.class);
        patchDevice();
    }

    public static void trackEvent(String name, Map<String, String> properties) {
        Analytics.trackEvent(name, properties);
    }

    public static void trackError(Throwable throwable, Map<String, String> properties) {
        Crashes.trackError(throwable, properties, null);
    }
}
