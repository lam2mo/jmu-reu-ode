/**
 * ODE solver - JMU REU 2021
 *
 * @author Mike Lam
 */

// standard headers
#include <cstdlib>
#include <iostream>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <string>
#include <cmath>
#include <functional>
using namespace std;

// Boost ODEint headers (individual files compile faster than the catch-all)
#include <boost/numeric/odeint.hpp>
//#include <boost/numeric/odeint/integrate/integrate_const.hpp>
//#include <boost/numeric/odeint/stepper/runge_kutta4.hpp>
using namespace boost::numeric::odeint;

// floating-point precision control (control via compiler parameter -Dreal_t)
//typedef double real_t;      // 64 bits
//typedef float real_t;       // 32 bits

// solver state
typedef std::vector<real_t> state_t;

// parameters (most read from command line)
real_t a, b, c, x0;
double t0 = 0.0;
double tn, dt;
const int N = 10;
real_t ac[N+1];

// output stream
ofstream out;

// ODE system
void ode(const state_t &x, state_t &dxdt, const real_t /*t*/)
{
    dxdt[0] = a*pow(x[0],2.0) + b*x[0] + c;
}

// power series solution at x
void calc_coefficients(real_t x)
{
    ac[0] = x;
    ac[1] = a * ac[0]*ac[0] + b * ac[0] + c;

    for (int i = 2; i <= N; i++) {
        ac[i] = 0.0;
        for (int k = 0; k < i; k++) {
            ac[i] += ac[k] * ac[i-1-k];
        }
        ac[i] = (a * ac[i] + b * ac[i-1]) / i;
    }
}

// power series solution using Horner's method
real_t psm(const real_t t)
{
    real_t x = t * ac[N];
    for (int j = N-1; j > 0; j--) {
        x = t * (x + ac[j]);
    }
    x += ac[0];
    return x;
}

// x \left( t \right) =1/2\,{\frac {1}{a} \left( \tan \left( 1/2\,t\sqrt
// {4\,ac-{b}^{2}}+\arctan \left( {\frac {2\,{\it x0}\,a+b}{\sqrt {4\,ac-
// {b}^{2}}}} \right)  \right) \sqrt {4\,ac-{b}^{2}}-b \right) }
//
// @author Kevin Rojas
real_t solution(real_t a,real_t b, real_t c, real_t x0, real_t t)
{
    /* Inputs x:
     * a,b,c: Parameters of the ODE
     * x0   : Initial condition
     * x    : Point to evaluate the exact solution on
     * Output:
     * x(t) where x solves x' = ax+bx+c, x(0) = x0
     */

    real_t disc = b*b-4.0*a*c;
    //cout << "discriminant: " << disc << endl;
    real_t val = 0.0;

    if (disc > 0.0) {
       real_t x1 = (-b+sqrt(disc))/(2.0*a);
       real_t x2 = (-b-sqrt(disc))/(2.0*a);
       real_t A = abs((x0-x2)/(x0-x1))*exp((x2-x1)*a*t);
       real_t val1 = (A*x1-x2)/(A-1);

       real_t B = -A;
       real_t val2 = (B*x1-x2)/(B-1);

       if((val1 >x2 && val1> x1) || (val1<x2 && val1<x1)){
           val = val1;
       }
       else{
           val = val2;
       }
       val = val2;
       // TODO: deal with the absolute value?

    } else if (disc < 0.0) {
       real_t A = b/(2.0*sqrt(a));
       real_t D = -disc/(4.0*a*a);
       real_t c0 = atan((x0+A)/sqrt(D));
       val = sqrt(D)*tan(a*sqrt(D)*t+c0)-A;

    } else {
       real_t x1 = -b/(2.0*a);
       real_t c0 = -1.0/(x0-x1);
       val = (x1-1.0/(a*t+c0));
    }
    return val;
}

// debug output helper
void observe(const state_t &x, const real_t t)
{
    real_t sol = solution(a, b, c, x0, t);
    real_t psm_sol = psm(0);

    // re-center power series
    calc_coefficients(psm(dt));

    // write output
    out << setw(8) << t;
    out << setw(16) << sol;
    out << setw(16) << x[0] << setw(16) << fabs(sol-x[0]);
    out << setw(16) << psm_sol << setw(16) << fabs(sol-psm_sol);
    out << endl;
}

int main(int argc, const char* argv[])
{
    // check parameters
    if (argc != 7) {
        cout << "Usage: " << argv[0] << " <a> <b> <c> <x0> <tn> <dt>" << endl;
        return EXIT_FAILURE;
    }

    // parse parameters
    a  = stod(argv[1], NULL);
    b  = stod(argv[2], NULL);
    c  = stod(argv[3], NULL);
    x0 = stod(argv[4], NULL);
    tn = stod(argv[5], NULL);
    dt = stod(argv[6], NULL);
    // TODO: add stepper and t0?

    // initialize state
    state_t x(1);
    x[0] = x0;

    // precalculate coefficients
    calc_coefficients(x0);

    // show floating-point width
    //cout << "sizeof(real_t)=" << sizeof(real_t) << endl;

    // stepper
    runge_kutta4<state_t> stp;

    // integrate w/ debug output
    out.open("out.dat");
    /*size_t steps =*/ integrate_const(stp, ode, x, t0, tn, dt, observe);
    out.close();

    // show final output
    //cout << "steps=" << steps << " x=" << x[0] << endl;

    return EXIT_SUCCESS;
}
