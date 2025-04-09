#!/bin/sh
#JAVA_HOME='[PATH TO JAVA TO USE]'
echo 'JAVA_HOME used' = $JAVA_HOME
$JAVA_HOME/bin/java -Xms512m -Xmx1G -Djava.library.path="./lib" -jar eP-Back-@version@.jar
