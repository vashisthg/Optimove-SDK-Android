package com.optimove.sdk.optimove_sdk.optipush;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SdkMetadataEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptipushConfigs;
import com.optimove.sdk.optimove_sdk.main.tools.ApplicationHelper;
import com.optimove.sdk.optimove_sdk.main.tools.JsonUtils;
import com.optimove.sdk.optimove_sdk.main.tools.OptiUtils;
import com.optimove.sdk.optimove_sdk.main.tools.RequirementProvider;
import com.optimove.sdk.optimove_sdk.main.tools.networking.HttpClient;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInitializer;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationCreator;
import com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationData;
import com.optimove.sdk.optimove_sdk.optipush.messaging.OptipushMessageCommand;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushFcmTokenHandler;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptipushUserRegistrar;
import com.optimove.sdk.optimove_sdk.optipush.registration.RegistrationDao;
import com.optimove.sdk.optimove_sdk.optipush.registration.requests.Metadata;

public final class OptipushManager {

    @NonNull
    private RegistrationDao registrationDao;
    @NonNull
    private RequirementProvider requirementProvider;
    @NonNull
    private HttpClient httpClient;
    @NonNull
    private LifecycleObserver lifecycleObserver;
    @NonNull
    private Context context;

    @Nullable
    private OptipushUserRegistrar optipushUserRegistrar;

    public OptipushManager(@NonNull RegistrationDao registrationDao,@NonNull RequirementProvider requirementProvider,
                           @NonNull HttpClient httpClient,@NonNull LifecycleObserver lifecycleObserver,
                           @NonNull Context context) {
        this.registrationDao = registrationDao;
        this.requirementProvider = requirementProvider;
        this.httpClient = httpClient;
        this.lifecycleObserver = lifecycleObserver;
        this.context = context;
    }

    public void tokenWasChanged() {
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.userTokenChanged();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .save();
        }
    }

    public void userIdChanged() {
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.userIdChanged();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .save();
        }
    }

    public void optipushMessageCommand(RemoteMessage remoteMessage, int executionTimeLimitInMs) {
        if (registrationDao.isPushCampaignsDisabled()){
            return;
        }
        NotificationCreator notificationCreator = new NotificationCreator(context);
        new OptipushMessageCommand(context, Optimove.getInstance()
                .getEventHandlerProvider()
                .getEventHandler(),
                new RequirementProvider(context), notificationCreator)
                .processRemoteMessage(executionTimeLimitInMs, remoteMessage, JsonUtils.parseJsonMap(remoteMessage.getData(),
                        NotificationData.class));
    }

    public void disablePushCampaigns(){
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.disablePushCampaigns();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .disablePushCampaigns()
                    .save();
        }
    }

    public void enablePushCampaigns(){
        if (optipushUserRegistrar != null) {
            optipushUserRegistrar.enablePushCampaigns();
        } else {
            registrationDao.editFlags()
                    .markSetInstallationAsFailed()
                    .enablePushCampaigns()
                    .save();
        }
    }


    public void processConfigs(OptipushConfigs optipushConfigs, int tenantId, UserInfo userInfo) {
        if (!requirementProvider.isGooglePlayServicesAvailable()) {
            return;
        }

        boolean succeeded = new OptimoveFirebaseInitializer(context).setup(optipushConfigs);
        if (!succeeded) {
            return;
        }

        this.optipushUserRegistrar =
                OptipushUserRegistrar.create(optipushConfigs.getRegistrationServiceEndpoint(), httpClient,
                        context.getPackageName(), tenantId, requirementProvider, registrationDao, userInfo,
                        lifecycleObserver, getMetadata());

        new OptipushFcmTokenHandler().completeLastTokenRefreshIfFailed();
    }

    private Metadata getMetadata() {
        Object appVersionObject = OptiUtils.getBuildConfig(ApplicationHelper.getBasePackageName(context),
                "VERSION_NAME");

        String appVersion = String.valueOf(appVersionObject);

        return new Metadata(SdkMetadataEvent.NATIVE_SDK_VERSION, appVersion, Build.VERSION.RELEASE,
                Build.MODEL);
    }
}

