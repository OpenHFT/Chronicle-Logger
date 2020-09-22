#!/usr/bin/env bash

# flatc has to be installed from command line, i.e. `snap install flatc`

flatc --java --gen-mutable -o src/main/java schema/entry.fbs