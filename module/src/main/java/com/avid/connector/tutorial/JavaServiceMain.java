package com.avid.connector.tutorial;

import com.avid.acs.bus.*;
import com.avid.acs.bus.connection.ConnectEvent;
import com.avid.acs.bus.connection.ConnectionListener;
import com.avid.acs.bus.connection.DisconnectEvent;
import com.avid.acs.bus.service.context.ServiceContext;
import com.avid.acs.service.IpcBusAccessFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class JavaServiceMain {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaServiceMain.class);
    private static BusAccess bus;

    public static void main(String[] args) {
        try {
            bus = new IpcBusAccessFactory().createBusAccess(null, false);
            bus.connect(new ConnectionListener() {
                @Override
                public void onConnect(ConnectEvent event) {
                    LOG.info("Connected to Avid Platform");
                }

                @Override
                public void onDisconnect(DisconnectEvent event) {
                    LOG.info("Disconnected from Avid Platform");
                }
            });
            JavaService javaService = new JavaServiceImpl(bus);
            AsyncCallback<ServiceContext> asyncCallback = new AsyncCallback<ServiceContext>() {
                @Override
                public void onSuccess(ServiceContext serviceContext) {
                    LOG.info("Service registered with id={}", serviceContext.getServiceInfo().getId());
                }

                @Override
                public void onError(CallbackError callbackError) {
                    LOG.info("Failed to register service: {}", callbackError.getMessage());
                }
            };
            bus.registerService(javaService, null, asyncCallback);
        } catch (BusAccessException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}