#!/bin/bash
IPADDR=$(ifconfig eth0 | sed -n '/inet /{s/.*addr://;s/ .*//;p}')
java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9000 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=$IPADDR -XshowSettings:properties -Djava.security.debug=access,failure -Dlog4j.configurationFile=config/log4j2.xml -Djava.awt.headless=true -Dfile.encoding="UTF-8" -jar pidome-server.jar $1 $2 $3
