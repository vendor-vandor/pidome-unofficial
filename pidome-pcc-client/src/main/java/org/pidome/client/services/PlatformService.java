package org.pidome.client.services;

import static java.lang.String.format;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Logger;

public class PlatformService {

    private static final Logger LOG = Logger.getLogger(PlatformService.class.getName());

    private static PlatformService instance;
    
    public static synchronized PlatformService getInstance() throws RuntimeException {
        if (instance == null) {
            instance = new PlatformService();
        }
        return instance;
    }

    private final ServiceLoader<ServiceConnector> serviceLoader;
    private ServiceConnector provider;

    private PlatformService() {
        serviceLoader = ServiceLoader.load(ServiceConnector.class);

        Iterator<ServiceConnector> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            if (provider == null) {
                provider = iterator.next();
                LOG.info(format("ServiceConnection found is: %s", provider.getClass().getName()));
            } else {
                LOG.info(format("This ServiceConnection is ignored: %s", iterator.next().getClass().getName()));
            }
        }

        if (provider == null) {
            LOG.severe("No ServiceConnection implementation could be found!");
        }
    }

    public ServiceConnector getServiceConnector() {
        return provider;
    }

}
