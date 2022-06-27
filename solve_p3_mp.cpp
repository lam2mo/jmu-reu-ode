#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>

using namespace std;
using namespace std::chrono;

#include <boost/multiprecision/cpp_bin_float.hpp>
#include <boost/multiprecision/cpp_int.hpp>

using namespace boost::multiprecision;

typedef number<backends::cpp_bin_float<11, backends::digit_base_2, void, std::int16_t, -14,   15>, et_off> cpp_bin_float_half;
typedef number<backends::cpp_bin_float<8,  backends::digit_base_2, void, std::int16_t, -126, 127>, et_off> cpp_bin_float_bfloat;

ofstream out;

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
void updateNthCoefficient(vector<T> &x, vector<T> &xprime, vector<T> &xx, int n){
	/* This method asumes we have the n-th coefficient for x, it then updates the rest
	 * 
	 * This method runs in O(N)
	 */
	 if(n>0){ 
		xprime[n-1] = n*x[n];
	 }
	 xx[n] = nthCoefficientProduct(x,x,n);
	 if(n == 0){
		 xx[n]-=1;
	 }
 }

template <class T>
vector<T> coefficients(T a,T x0, T x00, int nn){
	/* Inputs
	 * a     : Parameters of ODE
	 * n     : order of the polynomial approximation
	 * x0,x00: initial conditions 
	 * Output:
	 * coefficients of the nth polynomial approximation in increasing order
	 * 
	 * Runs in O(n^2), memory O(n)
	 */
	 vector<T> x(nn,0);
	 vector<T> deriv(nn,0);
	 vector<T> secondDeriv(nn,0);
	 vector<T> square(nn,0); //This is actually x^2-1
	 
	 x[0] = x0; //The initial condition is x0 
	 x[1] = x00; //The initial condition for the derivative is x00
	 updateNthCoefficient(x,deriv,square,0);
	 updateNthCoefficient(x,deriv,square,1);
	 
	 for(int n = 0; n<nn-2; n++){
		 secondDeriv[n] = -a*nthCoefficientProduct(square,deriv,n)-x[n];
		 x[n+2] = secondDeriv[n]/((n+2)*(n+1));
		 updateNthCoefficient(x,deriv,square,n+2);
	 }
	 return x;
}

template <class T>
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

template <class T>
vector<T> derivative(vector<T> f){
	vector<T> deriv(f.size()-1,0);
	for(int i = 0; i< deriv.size(); i++){
		deriv[i] = f[i+1]*(i+1);
	}
	return deriv;
}

template <class T>
pair<T, T> computeNext(T a,T x0, T x00, T step,bool forward,int n){
	/*Inputs:
	 * a,x0,x00: parameters of the ODE
	 * step: Size of the step
	 * forward: if you want to go forward or backwards 
	 * Outputs:
	 * x(t) where t = x0+step and x solves the vander pol oscillator problem
	 */
	 vector<T> coeff = coefficients(a,x0,x00,n);
	 vector<T> deriv = derivative(coeff);
	 if(forward){
		return {eval(coeff,step), eval(deriv,step)};
	 }
	 else{
		return {eval(coeff,(T)(-step)), eval(deriv,(T)(-step))};
	}
}

template <class T>
pair<vector<T>,vector<T>> generateSolutionStepper(T a,T x0, T x00, T step, bool forward, T end, int n){
	/*Inputs:
	 * a,x0,x00: parameters of the ODE
	 * step: Size of the step
	 * end : End point to solve the solution (solves in [0,end])
	 * step: Size of the step, expecting a positive value
	 * n   : Degree of the approximation
	 * forward: if you want to go forward or backwards 
	 * Output:
	 * vector of solutions to the Vander Pol Oscillator equation
	 */
	 step = fabs(step);
	 vector<T> sol;
	 sol.push_back(x0);
	 vector<T> deriv;
	 deriv.push_back(x00);
	 T newx0 = x0;
	 T newx00 = x00;
	 for(int k = 1; step*k<=end; k++){
		//cout<<step*k<<" "<<computeNext(a,b,c,solution[k-1],step,forward,n)<<"\n";
		pair<T, T> cond = computeNext(a,newx0,newx00,step,forward,n);
		sol.push_back(cond.first);
		deriv.push_back(cond.second);
		newx0 = cond.first;
		newx00 = cond.second;		 
	 }
	 return {sol,deriv};
}

template <class T>
void solve_p3(T a, T x0, T x00, T end, T step, int n){
	vector<T> coeff = coefficients(a,x0,x00,n);

	vector<T> solutionStepper = generateSolutionStepper(a,x0,x00,step,1,end, n).first;
	vector<T> derivSolution = generateSolutionStepper(a,x0,x00,step,1,end, n).second;

    out.open("out.dat");
	//cout<<setw(15)<<"t"<<setw(15)<<"No Stepper"<<setw(15)<<"Stepper"<<"\n";
	for(int i = 0; i<solutionStepper.size(); i++){
		int dig = 1000;
		//out.precision(5);
		//out<<fixed<<setw(15)<<i*step<<" ";
		out<<i*step<<" ";
		T stepper = solutionStepper[i];
		T deriv = derivSolution[i];

		if( fabs(stepper)>dig || fabs(deriv)>dig ){
			//out<<scientific<<setw(15)<<stepper<<" "<<setw(15)<<deriv<<"\n";
			out<<stepper<<" "<<deriv<<"\n";
		}
		else{
			out<<stepper<<" "<<deriv<<"\n";
		}
	}
    out.close();

}

int main(int argc, const char* argv[]){

	// check parameters
    if (argc != 8) {
        cout << "Usage: " << argv[0] << " <a> <x0> <xp0> <tn> <dt> <n> <prec>" << endl;
        return EXIT_FAILURE;
    }

    // parse parameters
    double a  = stod(argv[1], NULL);
    double x0  = stod(argv[2], NULL);
    double x00  = stod(argv[3], NULL);
    double end = stod(argv[4], NULL);
    double step = stod(argv[5], NULL);
    int n = strtol(argv[6], NULL, 10);
    int prec = stod(argv[7], NULL);

    if (prec == 16) {
        //solve_p3((cpp_bin_float_half)a, (cpp_bin_float_half)x0, (cpp_bin_float_half)x00, (cpp_bin_float_half)end, (cpp_bin_float_half)step, n);
        solve_p3((cpp_bin_float_bfloat)a, (cpp_bin_float_bfloat)x0, (cpp_bin_float_bfloat)x00, (cpp_bin_float_bfloat)end, (cpp_bin_float_bfloat)step, n);
    } else if (prec <= 32) {
        solve_p3((float)a, (float)x0, (float)x00, (float)end, (float)step, n);
    } else if (prec <= 64) {
        solve_p3(a, x0, x00, end, step, n);
    } else if (prec <= 128) {
        solve_p3((cpp_bin_float_quad)a, (cpp_bin_float_quad)x0, (cpp_bin_float_quad)x00, (cpp_bin_float_quad)end, (cpp_bin_float_quad)step, n);
    } else {
        cout << "Error: invalid precision (" << prec << ")" << endl;
    }
}
