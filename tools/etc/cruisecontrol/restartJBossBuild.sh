#!/bin/bash

echo kill old cruisecontrol jobs

killall -9 java
killall -9 cvs


echo sleep a bit before we start again
sleep 30

nohup /home/build/cc-cvs/cruisecontrol/main/bin/cruisecontrol.sh -configfile /home/build/cruisecontrol/work/scripts/cc-config-all.xml &

#nohup /usr/local/cruisecontrol/cruisecontrol-2.1.5/main/bin/cruisecontrol.sh -projectname jboss-head -configfile /home/build/cruisecontrol/work/config.xml &
