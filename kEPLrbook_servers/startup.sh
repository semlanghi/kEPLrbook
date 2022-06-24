#!/bin/bash

# Start the first process
java -cp ./target/kEPLrbook-jar-with-dependencies.jar server_distributed.RuntimeServer.RuntimeServer &> runtime.out

sleep 5
# Start the second process
java -cp ./target/kEPLrbook-jar-with-dependencies.jar server_distributed.InputManager.InputManager &> input.out

sleep 5
java -cp ./target/kEPLrbook-jar-with-dependencies.jar server_distributed.OutputManager.OutputManager &> output.out
