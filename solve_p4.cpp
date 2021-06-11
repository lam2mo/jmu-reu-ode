#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>
#include <cstdlib>
#include <functional>
#include <sstream>

#include <boost/multiprecision/cpp_bin_float.hpp>
#include <boost/multiprecision/cpp_int.hpp>

using namespace std;
using namespace std::chrono;
using namespace boost::multiprecision;

typedef number<backends::cpp_bin_float<11, backends::digit_base_2, void, std::int16_t, -14,   15>, et_off> cpp_bin_float_half;
typedef number<backends::cpp_bin_float<8,  backends::digit_base_2, void, std::int16_t, -126, 127>, et_off> cpp_bin_float_bfloat;

ofstream out;

template <class T>
T absval(T v) {
    return (v < T(0)) ? -v : v;
}

template <class T>
T nthCoefficientProduct(vector<T> &x, vector<T> &y, int n){
    /* This method computes the nth coefficient of x*y, runs in O(n)
     */
    T sum = 0;
    for(int i  = 0; i<=n; i++){
        sum+=x[i]*y[n-i];
    }
    return sum;
}

template <class T>
void updateNthCoefficient(vector<T> &x, vector<T> &xx, vector<T> &xxx, int n){
    /* This method asumes we have the n-th coefficient for x, it then updates the rest
     *
     * This method runs in O(N)
     */
    xx[n] = nthCoefficientProduct(x,x,n);
    xxx[n] = nthCoefficientProduct(xx,x,n);
 }

template <class T>
vector<T> coefficients(T x0, int nn){
    /* Inputs
     * n : order of the polynomial approximation
     * x0: initial conditions
     * Output:
     * coefficients of the nth polynomial approximation in increasing order
     *
     * Runs in O(n^2), memory O(n)
     */
    vector<T> x(nn,0);
    vector<T> xx(nn,0);
    vector<T> xxx(nn,0);
    vector<T> xprime(nn,0);
    x[0] = x0; //The initial condition is x0
    updateNthCoefficient<T>(x,xx,xxx,0);

    for(int n = 0; n<nn-1; n++){
        xprime[n] = xx[n]-xxx[n];
        x[n+1] = xprime[n]/(n+1);
        updateNthCoefficient<T>(x,xx,xxx,n+1);
    }
    return x;
}

template<class T>
T eval(vector<T> coeff, T x){
    /* Evaluates a Polynomial given its coefficients
     * Inputs:
     * coeff: coefficients of the polynomial to be evaluated in increasing order
     * x    : number to be evaluated
     * Output:
     * p(x)
     */

    //Horners Algorithm
    T val = 0;
    for(int i = coeff.size()-1; i>=0; i--){
        val = val*x+coeff[i];
    }
    return val;
}

template<class T>
vector<T> derivative(vector<T> f){
    vector<T> deriv(f.size()-1,0);
    for(int i = 0; i< deriv.size(); i++){
        deriv[i] = f[i+1]*(i+1);
    }
    return deriv;
}

template<class T>
pair<T, T> computeNext(T x0, T step, bool forward, int n){
    /*Inputs:
     * x0  : parameters of the ODE
     * step: Size of the step
     * forward: if you want to go forward or backwards
     * Outputs:
     * x(t) where t = x0+step and x solves the equation x' = x^2-x^3
     */
    vector<T> coeff = coefficients(x0,n);
    vector<T> deriv = derivative(coeff);
    if(forward){
        return make_pair(eval<T>(coeff,step), eval<T>(deriv,step));
    }
    else{
        return make_pair(eval<T>(coeff,-step), eval<T>(deriv,-step));
    }
}

template<class T>
pair<vector<T>, vector<T> > generateSolutionStepper(T x0, T step, bool forward, T end, int n){
    /*Inputs:
     * x0  : parameters of the ODE
     * step: Size of the step
     * end : End point to solve the solution (solves in [0,end])
     * n   : Degree of the approximation
     * forward: if you want to go forward or backwards
     * Output:
     * vector of solutions to the equation x' = x^2-x^3
     */
    step = absval(step);
    vector<T> sol;
    sol.push_back(x0);
    vector<T> deriv;
    T xx = x0 * x0;
    deriv.push_back(xx-x0*xx);
    T newx0 = x0;
    for(int k = 1; step*k<=end; k++){
        //cout << "step " << k << endl;
        pair<T, T> cond = computeNext<T>(newx0,step,forward,n);
        sol.push_back(cond.first);
        deriv.push_back(cond.second);
        newx0 = cond.first;
    }
    return make_pair(sol,deriv);
}

template<class T>
void solve_p4(T x0, T end, T step, int n, string fn)
{
    // generate solutions
    //cout << "size = " << sizeof(x0) << endl;
    vector<T> coeff = coefficients(x0,n);
    auto stepperB = generateSolutionStepper<T>(x0, step, 0, end, n);
    vector<T> solutionStepperB = stepperB.first;
    vector<T> derivSolutionB = stepperB.second;
    auto stepperF = generateSolutionStepper<T>(x0, step, 1, end, n);
    vector<T> solutionStepperF = stepperF.first;
    vector<T> derivSolutionF = stepperF.second;

    // print backward solution
    out.open(fn);
    for(int i = solutionStepperB.size()-1; i>=0; i--) {
        int dig = 1000;
        out.precision(5);
        out << fixed << setw(15) << -i*step << " ";
        T stepper = solutionStepperB[i];
        T deriv = derivSolutionB[i];
        if (absval(stepper) > dig || absval(deriv) > dig) {
            out << scientific << setw(15) << stepper << " " << setw(15) << deriv << "\n";
        }
        else{
            out << fixed << setw(15) << stepper << " " << setw(15) << deriv << "\n";
        }
    }

    // print forward solution
    for(int i = 1; i<solutionStepperF.size(); i++) {
        int dig = 1000;
        out.precision(5);
        out << fixed << setw(15) << i*step << " ";
        T stepper = solutionStepperF[i];
        T deriv = derivSolutionF[i];
        if (absval(stepper) > dig || absval(deriv) > dig) {
            out << scientific << setw(15) << stepper << " " << setw(15) << deriv << "\n";
        }
        else{
            out << fixed << setw(15) << stepper << " " << setw(15) << deriv << "\n";
        }
    }
    out.close();
}

int main(int argc, const char* argv[])
{
    // check parameters
    if (argc != 5) {
        cout << "Usage: " << argv[0] << " <x0> <tn> <dt> <rtype>" << endl;
        return EXIT_FAILURE;
    }

    // parse parameters
    double x0  = stod(argv[1], NULL);
    double end = stod(argv[2], NULL);
    double step = stod(argv[3], NULL);
    string rtype = string(argv[4]);

    if (rtype == "float") {
        solve_p4((float)x0, (float)end, (float)step, 10, "out.dat");
    } else if (rtype == "quad") {
        solve_p4(cpp_bin_float_quad(x0), cpp_bin_float_quad(end), cpp_bin_float_quad(step), 10, "out.dat");
    } else if (rtype == "half") {
        solve_p4(cpp_bin_float_half(x0), cpp_bin_float_half(end), cpp_bin_float_half(step), 10, "out.dat");
    } else if (rtype == "bfloat") {
        solve_p4(cpp_bin_float_bfloat(x0), cpp_bin_float_bfloat(end), cpp_bin_float_bfloat(step), 10, "out.dat");
    } else if (rtype == "rational") {
        solve_p4(cpp_rational(x0), cpp_rational(end), cpp_rational(step), 10, "out.dat");
    } else {
        solve_p4(x0, end, step, 10, "out.dat");
    }

    return EXIT_SUCCESS;
}
