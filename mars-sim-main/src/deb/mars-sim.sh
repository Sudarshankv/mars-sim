#!/bin/sh

java -Xms256m -Xmx512m -Djava.util.logging.config.file=logging.properties -cp /usr/lib/mars-sim-main/* org.mars_sim.msp.MarsProject
