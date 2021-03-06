package com.optimove.sdk.optimove_sdk.event_handler_tests;

import com.optimove.sdk.optimove_sdk.main.OptistreamEventBuilder;
import com.optimove.sdk.optimove_sdk.main.event_handlers.DestinationDecider;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamHandler;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.SimpleCustomEvent;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;
import com.optimove.sdk.optimove_sdk.realtime.RealtimeManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DestinationDeciderTests {

    @Mock
    private Map<String, EventConfigs> eventConfigsMap;
    @Mock
    private OptistreamHandler optistreamHandler;
    @Mock
    private RealtimeManager realtimeManager;
    @Mock
    private OptistreamEventBuilder optistreamEventBuilder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void eventShouldntBeReportedToRealtimeIfNotSupportedOnRealtime() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(false);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        DestinationDecider destinationDecider = new DestinationDecider(eventConfigsMap, optistreamHandler,
                realtimeManager,
                optistreamEventBuilder, true, false);
        destinationDecider.reportEvent(Collections.singletonList(optimoveEvent));
        verifyZeroInteractions(realtimeManager);

    }

    @Test
    public void eventShouldntBeReportedToRealtimeIfRealtimeDisabled() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        DestinationDecider destinationDecider = new DestinationDecider(eventConfigsMap, optistreamHandler,
                realtimeManager,
                optistreamEventBuilder, false, false);
        destinationDecider.reportEvent(Collections.singletonList(optimoveEvent));
        verifyZeroInteractions(realtimeManager);
    }
    @Test
    public void eventShouldntBeReportedToRealtimeIfRealtimeEnabledThroughOptistream() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        DestinationDecider destinationDecider = new DestinationDecider(eventConfigsMap, optistreamHandler,
                realtimeManager,
                optistreamEventBuilder, true, true);
        destinationDecider.reportEvent(Collections.singletonList(optimoveEvent));
        verifyZeroInteractions(realtimeManager);
    }

    @Test
    public void eventShouldBeReportedToRealtimeIfRealtimeEnabledThroughOptistreamDisabled() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());

        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);
        OptistreamEvent optistreamEvent = mock(OptistreamEvent.class);
        when(optistreamEventBuilder.convertOptimoveToOptistreamEvent(eq(optimoveEvent), anyBoolean())).thenReturn(optistreamEvent);

        when(optistreamEvent.getName()).thenReturn(eventName);

        DestinationDecider destinationDecider = new DestinationDecider(eventConfigsMap, optistreamHandler,
                realtimeManager,
                optistreamEventBuilder, true, false);
        destinationDecider.reportEvent(Collections.singletonList(optimoveEvent));
        verify(realtimeManager).reportEvents(assertArg(arg -> Assert.assertTrue(arg.get(0).getName().equals(
                eventName))));
    }

    @Test
    public void eventShouldntBeReportedToRealtimeIfThereAreValidationIssuesEvenIfRtEnabledThroughOptistream() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        List<OptimoveEvent.ValidationIssue> validationIssues = new ArrayList<>();
        validationIssues.add(new OptimoveEvent.ValidationIssue(555, "some_message", true));
        optimoveEvent.setValidationIssues(validationIssues);
        DestinationDecider destinationDecider = new DestinationDecider(eventConfigsMap, optistreamHandler, realtimeManager,
                optistreamEventBuilder, true, true);
        destinationDecider.reportEvent(Collections.singletonList(optimoveEvent));
        verifyZeroInteractions(realtimeManager);
    }
    @Test
    public void eventShouldntBeReportedToRealtimeIfThereAreValidationErrors() {
        String eventName = "some_event_name";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);

        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        List<OptimoveEvent.ValidationIssue> validationIssues = new ArrayList<>();
        validationIssues.add(new OptimoveEvent.ValidationIssue(555, "some_message", true));
        optimoveEvent.setValidationIssues(validationIssues);
        DestinationDecider destinationDecider = new DestinationDecider(eventConfigsMap, optistreamHandler, realtimeManager,
                optistreamEventBuilder, true, false);
        destinationDecider.reportEvent(Collections.singletonList(optimoveEvent));
        verifyZeroInteractions(realtimeManager);
    }
    @Test
    public void eventShouldBeReportedToRealtimeIfThereAreValidationWarningsOnly() {
        String eventName = "some_event_name1";
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(eventConfigsMap.get(eventName)).thenReturn(eventConfigs);
        when(eventConfigs.isSupportedOnRealtime()).thenReturn(true);


        OptimoveEvent optimoveEvent = new SimpleCustomEvent(eventName, new HashMap<>());
        OptistreamEvent optistreamEvent = mock(OptistreamEvent.class);
        when(optistreamEventBuilder.convertOptimoveToOptistreamEvent(eq(optimoveEvent), anyBoolean())).thenReturn(optistreamEvent);

        when(optistreamEvent.getName()).thenReturn(eventName);
        List<OptimoveEvent.ValidationIssue> validationIssues = new ArrayList<>();
        validationIssues.add(new OptimoveEvent.ValidationIssue(555, "some_message", false));
        optimoveEvent.setValidationIssues(validationIssues);
        DestinationDecider destinationDecider = new DestinationDecider(eventConfigsMap, optistreamHandler, realtimeManager,
                optistreamEventBuilder, true, false);
        destinationDecider.reportEvent(Collections.singletonList(optimoveEvent));
        verify(realtimeManager).reportEvents(assertArg(arg -> Assert.assertTrue(arg.get(0).getName().equals(
                eventName))));
    }
}
