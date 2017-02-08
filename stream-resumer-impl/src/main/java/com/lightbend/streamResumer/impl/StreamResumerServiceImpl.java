/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.streamResumer.impl;

import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import com.lightbend.streamResumer.api.StreamResumerService;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import sample.chirper.chirp.api.Chirp;
import sample.chirper.chirp.api.ChirpService;
import sample.chirper.chirp.api.LiveChirpsRequest;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.Arrays.asList;

/**
 * Implementation of the StreamResumerService.
 */
public class StreamResumerServiceImpl implements StreamResumerService {

    @Inject
    public StreamResumerServiceImpl(Materializer materializer, ChirpService chirpService) {
        PSequence<String> userIds = TreePVector.from(asList("user1", "user2"));
        LiveChirpsRequest chirpsReq =  new LiveChirpsRequest(userIds);
        // Note that this stream will not include changes to friend associates,
        // e.g. adding a new friend.
        CompletionStage<Source<Chirp, ?>> result = chirpService.getLiveChirps().invoke(chirpsReq);
        result.thenAccept(chirps -> {
            chirps.runForeach(this::printChirp, materializer);
        });
    }

    private void printChirp(Chirp chirp) {
        System.err.println(chirp.toString());
    }

}
