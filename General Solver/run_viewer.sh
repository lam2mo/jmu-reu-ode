#!/bin/bash

make -C .. ODEViewer.class

java -cp .. ODEViewer $@

