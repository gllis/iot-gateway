package com.gllis.gateway.server.lwm2m;

import com.gllis.gateway.server.conf.Lwm2mConf;
import com.gllis.gateway.server.core.listener.Listener;
import com.gllis.gateway.server.core.service.BaseServiceImpl;
import com.gllis.gateway.server.enums.ServerStateEnum;
import com.gllis.gateway.server.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.observation.CompositeObservation;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.observation.SingleObservation;
import org.eclipse.leshan.core.response.ObserveCompositeResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.server.LeshanServer;
import org.eclipse.leshan.server.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.endpoint.CaliforniumServerEndpointsProvider;
import org.eclipse.leshan.server.californium.endpoint.CaliforniumServerEndpointsProvider.Builder;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * lwm2m服务
 *
 * @author glli
 * @date 2023/8/27
 */
@Slf4j
@Component
public class Lwm2mServer extends BaseServiceImpl {

    @Autowired
    private Lwm2mConf lwm2mConf;
    private LeshanServer server;

    private final AtomicReference<ServerStateEnum> serverState = new AtomicReference<>(ServerStateEnum.Created);

    @Override
    public void init() {
        if (!serverState.compareAndSet(ServerStateEnum.Created, ServerStateEnum.Initialized)) {
            throw new ServiceException("Server already init");
        }
    }

    @Override
    public boolean isRunning() {
        return serverState.get() == ServerStateEnum.Started;
    }

    @Override
    public void start(Listener listener) {
        if (!serverState.compareAndSet(ServerStateEnum.Initialized, ServerStateEnum.Starting)) {
            throw new ServiceException("Server already started or have not init");
        }
        Builder EndpointProviderbuilder = new CaliforniumServerEndpointsProvider.Builder();
        EndpointProviderbuilder.addEndpoint(new InetSocketAddress(lwm2mConf.getCoapPort()), Protocol.COAP);
        EndpointProviderbuilder.addEndpoint(new InetSocketAddress(lwm2mConf.getCoapsPort()), Protocol.COAPS);
        LeshanServerBuilder builder = new LeshanServerBuilder();
        builder.setEndpointsProvider(EndpointProviderbuilder.build());
        server = builder.build();
        server.getRegistrationService().addListener(getRegistrationListener());
        server.getObservationService().addListener(getObservationListener());
        server.start();
        serverState.set(ServerStateEnum.Started);
        log.info("server start success on:{}", lwm2mConf.getCoapPort());
        if (listener != null) {
            listener.onSuccess(lwm2mConf.getCoapPort());
        }
    }

    @Override
    public void stop(Listener listener) {
        if (serverState.compareAndSet(ServerStateEnum.Started, ServerStateEnum.Shutdown)) {
            if (listener != null) {
                listener.onFailure(new ServiceException("server was already shutdown."));
            }
            log.info("{} was already shutdown.", this.getClass().getSimpleName());
            return;
        }
        log.info("try shutdown {}...", this.getClass().getSimpleName());
        if (server != null) {
           server.stop();
           server.destroy();
        }
        log.info("{} shutdown success.", this.getClass().getSimpleName());
        listener.onSuccess(lwm2mConf.getCoapPort());
    }

    public RegistrationListener getRegistrationListener() {
        return new RegistrationListener() {
            @Override
            public void registered(Registration registration, Registration registration1, Collection<Observation> collection) {

            }

            @Override
            public void updated(RegistrationUpdate registrationUpdate, Registration registration, Registration registration1) {

            }

            @Override
            public void unregistered(Registration registration, Collection<Observation> collection, boolean b, Registration registration1) {

            }
        };
    }

    public ObservationListener getObservationListener() {
        return new ObservationListener() {

            @Override
            public void newObservation(Observation observation, Registration registration) {

            }

            @Override
            public void cancelled(Observation observation) {

            }

            @Override
            public void onResponse(SingleObservation singleObservation, Registration registration, ObserveResponse observeResponse) {

            }

            @Override
            public void onResponse(CompositeObservation compositeObservation, Registration registration, ObserveCompositeResponse observeCompositeResponse) {

            }

            @Override
            public void onError(Observation observation, Registration registration, Exception e) {

            }
        };
    }

}
