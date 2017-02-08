/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.streamResumer.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.streamResumer.api.StreamResumerService;
import sample.chirper.chirp.api.ChirpService;

/**
 * The module that binds the StreamResumerService so that it can be served.
 */
public class StreamResumerModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(serviceBinding(StreamResumerService.class, StreamResumerServiceImpl.class));
        bindClient(ChirpService.class);
    }
}
