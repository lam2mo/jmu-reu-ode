CXX=g++
CXXFLAGS=-g -O2 -Wall -std=c++11
LDFLAGS=

TARGETS=ODEViewer.class VanderPolOscillatorGeneral

all: $(TARGETS)

ODEViewer.class: ODEViewer.java
	javac $<

VanderPolOscillatorGeneral: VanderPolOscillatorGeneral.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

clean:
	rm -rf $(TARGETS) *.dSYM *.class

