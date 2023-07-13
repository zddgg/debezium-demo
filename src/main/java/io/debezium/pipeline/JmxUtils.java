package io.debezium.pipeline;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.util.Clock;
import io.debezium.util.Metronome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.time.Duration;

public class JmxUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmxUtils.class);
    private static final String JMX_OBJECT_NAME_FORMAT = "debezium.%s:type=%s, context=%s, server=%s";
    private static final int REGISTRATION_RETRIES = 12;
    private static final Duration REGISTRATION_RETRY_DELAY = Duration.ofSeconds(5L);

    public static void registerMXBean(ObjectName objectName, Object mxBean) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            if (mBeanServer == null) {
                LOGGER.info("JMX not supported, bean '{}' not registered", objectName);
            } else {
                int attempt = 1;

                while (attempt <= 12) {
                    try {
                        mBeanServer.registerMBean(mxBean, objectName);
                        break;
                    } catch (InstanceAlreadyExistsException var6) {
                        if (attempt < 12) {
                            LOGGER.warn("Unable to register metrics as an old set with the same name exists, retrying in {} (attempt {} out of {})", new Object[]{REGISTRATION_RETRY_DELAY, attempt, 12});
                            Metronome metronome = Metronome.sleeper(REGISTRATION_RETRY_DELAY, Clock.system());
                            metronome.pause();
                        } else {
                            LOGGER.error("Failed to register metrics MBean, metrics will not be available");
                        }

                        ++attempt;
                    }
                }

            }
        } catch (InterruptedException | JMException var7) {
            throw new RuntimeException("Unable to register the MBean '" + objectName + "'", var7);
        }
    }

    public static void registerMXBean(Object mxBean, CommonConnectorConfig connectorConfig, String type, String context) {
        String jmxObjectName = getManagementJmxObjectName(type, context, connectorConfig);

        try {
            ObjectName objectName = new ObjectName(jmxObjectName);
            registerMXBean(objectName, mxBean);
        } catch (MalformedObjectNameException var6) {
            throw new RuntimeException("Unable to register the MBean '" + jmxObjectName + "'", var6);
        }
    }

    public static void unregisterMXBean(ObjectName objectName) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            if (mBeanServer == null) {
                LOGGER.debug("JMX not supported, bean '{}' not registered", objectName);
            } else {
                try {
                    mBeanServer.unregisterMBean(objectName);
                } catch (InstanceNotFoundException var3) {
                    LOGGER.info("Unable to unregister metrics MBean '{}' as it was not found", objectName);
                }

            }
        } catch (JMException var4) {
            throw new RuntimeException("Unable to unregister the MBean '" + objectName + "'", var4);
        }
    }

    public static void unregisterMXBean(CommonConnectorConfig connectorConfig, String type, String context) {
        String jmxObjectName = getManagementJmxObjectName(type, context, connectorConfig);

        try {
            ObjectName objectName = new ObjectName(jmxObjectName);
            unregisterMXBean(objectName);
        } catch (MalformedObjectNameException var5) {
            LOGGER.info("Unable to unregister metrics MBean '{}' as it was not found", jmxObjectName);
        }

    }

    private static String getManagementJmxObjectName(String type, String context, CommonConnectorConfig connectorConfig) {
        return String.format("debezium.%s:type=%s, context=%s, server=%s", connectorConfig.getContextName().toLowerCase(), type, context, connectorConfig.getLogicalName());
    }
}
