# JMU Math Summer 2021 REU on ODEs

These solvers currently use the Boost
[ODEint](https://www.boost.org/doc/libs/1_66_0/libs/numeric/odeint/doc/html/index.html)
library to solve an ODE numerically. The code is written such that both single-
and double-precision versions are compiled by default.

## Running a solver

To run an ODE solver, compile the corresponding executable with `make` and then
run the resulting executable, providing any needed parameters. Example:

```
./solve_p1_fp64 1 0 1 2 0.1
```

## Comparing floating-point results

To compare the results of running with 64-bit vs. 32-bit IEEE floating point,
use the provided `compare.sh` script. Example:

```
./compare.sh p1 1 0 1 2 0.1
```

## Running the graphical viewer

To run the grpahical ODE solution viewer (requires Java and Gnuplot), run `make
ODEViewer` and then launch the compiled viewer:

```
java ODEViewer
```

## Adding new solvers

To add a new ODE solver:

1. Copy one of the existing solvers to a new C++ source file.
2. Copy the build code in `Makefile` and adapt it to create targets for the new
   solver.
3. Modify the `TARGETS` definition in `Makefile` so that it is compiled by
   default (if desired).
4. Modify your new file to edit the parameter list and parsing code (in `main`)
   to include the parameters you need.
5. Modify the `ode()` function in your new file to calculate the desired ODE
   system.

## Contributors

* [Mike Lam](https://github.com/lam2mo) (James Madison University)
* Delaney MacDonald (Providence College)
* Kevin Rojas (Universidad de Costa Rica)

## Licensing

This software is made available under the MIT License. See `LICENSE` for
details.

