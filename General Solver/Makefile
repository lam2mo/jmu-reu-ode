TARGETS=DoublePendulum FlameEquation VanDerPolEquation

CXXFLAGS=-g -O2 -std=c++11 -Wall
LDFLAGS=

all: $(TARGETS)

FlameEquation: psmGeneralFunctions.o FlameEquation.o
	$(CXX) -o $@ $(LDFLAGS) $^

VanDerPolEquation: psmGeneralFunctions.o VanDerPolEquation.o
	$(CXX) -o $@ $(LDFLAGS) $^

DoublePendulum: psmGeneralFunctions.o DoublePendulum.o
	$(CXX) -o $@ $(LDFLAGS) $^

%.o: %.cpp psmGeneralFunctions.h
	$(CXX) -c $(CXXFLAGS) $<

clean:
	rm -f *.o $(TARGETS)

