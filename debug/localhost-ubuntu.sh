if [ "$USER" != "root" ];then
    echo "You have to run this script as a root: sudo su"
    exit
else
   cd ../
   java -Djdk.tls.client.protocols=TLSv1.2 -agentlib:jdwp=transport=dt_socket,address=8889,server=y,suspend=n -jar target/vertx-codeone-conduit-1.0-SNAPSHOT-fat.jar
fi
