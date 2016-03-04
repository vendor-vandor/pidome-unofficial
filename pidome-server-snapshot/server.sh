#!/bin/bash
PIDFILE=/var/run/pidome.pid
APP_HOME="`pwd -P`"
JAVA_LOC=$APP_HOME/jre/bin/java

if [ -f initial.log ]; then
    rm initial.log
fi

if [ -f $PIDFILE ]; then
    CurPID=$(<"$PIDFILE")

        case $1 in  
            kill)
                echo -n "Stopping PiDome server, please wait... "
                if ps -p $CurPID > /dev/null
                then
                    sudo kill -9 $CurPID
                fi
                echo -n "removing PID file $PIDFILE, "
                sudo rm $PIDFILE
                echo "done."
                exit 0
            ;;
            *)
                echo "PiDome already running: $PIDFILE, pid number: $CurPID"
                echo "If the server is running shut it down with the web interface or use 'sudo ./server.sh kill' to force a stop and removing $PIDFILE"
                exit 1
            ;;
        esac

fi
case $1 in  
    kill)
        exit 0
    ;;
    *)
        chmod 755 $JAVA_LOC
        echo -n "Initializing PiDome server, please wait for server start.."
        sudo nohup $JAVA_LOC -XshowSettings:properties -Djava.security.debug=access,failure -Dlog4j.configurationFile=config/log4j2.xml -Djava.awt.headless=true -Dfile.encoding="UTF-8" -jar pidome-server.jar $1 $2 $3 >initial.log 2>&1 &
        while [ ! -f $PIDFILE ]
        do
              sleep 1
              echo -n "."
        done
        echo " starting up."
esac