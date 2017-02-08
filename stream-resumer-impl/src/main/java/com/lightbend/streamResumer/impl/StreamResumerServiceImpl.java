/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.streamResumer.impl;

import akka.Done;
import akka.actor.*;
import akka.pattern.BackoffSupervisor;
import akka.pattern.PatternsCS;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import com.lightbend.streamResumer.api.StreamResumerService;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import sample.chirper.chirp.api.Chirp;
import sample.chirper.chirp.api.ChirpService;
import sample.chirper.chirp.api.LiveChirpsRequest;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

/**
 * Implementation of the StreamResumerService.
 */
public class StreamResumerServiceImpl implements StreamResumerService {

    @Inject
    public StreamResumerServiceImpl(ActorSystem actorSystem, Materializer materializer, ChirpService chirpService) {
        Props subscriberProps = SubscriberActor.props(chirpService, materializer);
        Props subscriberWithBackoffProps = BackoffSupervisor.propsWithSupervisorStrategy(
                subscriberProps,
                "ChirpSubscriber",
                FiniteDuration.create(3, TimeUnit.SECONDS),  // in a real app
                FiniteDuration.create(30, TimeUnit.SECONDS), // these three settings
                0.2,                                         // would be configurable
                SupervisorStrategy.stoppingStrategy()
        );

        actorSystem.actorOf(subscriberWithBackoffProps, "ChirpBackoffSubscriber");
    }

    static class SubscriberActor extends UntypedActor {
        private final ChirpService chirpService;
        private final Materializer materializer;

        static Props props(ChirpService chirpService, Materializer materializer) {
            return Props.create(SubscriberActor.class, chirpService, materializer);
        }

        public SubscriberActor(ChirpService chirpService, Materializer materializer) {
            this.chirpService = chirpService;
            this.materializer = materializer;
        }

        @Override
        public void preStart() throws Exception {
            System.err.println("Fetching chirps from chirpService");
            PSequence<String> userIds = TreePVector.from(asList("user1", "user2"));
            LiveChirpsRequest chirpsReq = new LiveChirpsRequest(userIds);

            CompletionStage<Source<Chirp, ?>> result = chirpService.getLiveChirps().invoke(chirpsReq);
            result.thenAccept(chirps -> {
                CompletionStage<Done> doneCompletionStage = chirps.runForeach(this::printChirp, materializer);
                PatternsCS.pipe(doneCompletionStage, getContext().dispatcher()).to(getSelf());
            });
        }

        private void printChirp(Chirp chirp) {
            System.err.println(chirp.toString());
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            System.err.println("Received " + message);
            if (message instanceof Status.Failure) {
                Status.Failure failure = (Status.Failure) message;
                throw failure.cause();
            } else if (message instanceof Done) {
                System.err.println("Terminating");
                getContext().stop(getSelf());
            }
        }
    }
}
