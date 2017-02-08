/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.streamResumer.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

/**
 * The streamResumer service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the StreamResumerService.
 */
public interface StreamResumerService extends Service {
    @Override
    default Descriptor descriptor() {
        return named("streamResumer");
    }
}
