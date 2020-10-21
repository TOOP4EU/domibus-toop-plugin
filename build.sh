#!/bin/bash

mvn -DskipTests=true clean package && \
    cp target/domibus-toop-plugin-4.1.1.jar ../Domibus/domibus-toop-dockerfiles/domibus/blue/conf/plugins/lib && \
    cp target/domibus-toop-plugin-4.1.1.jar ../Domibus/domibus-toop-dockerfiles/domibus/red/conf/plugins/lib/

