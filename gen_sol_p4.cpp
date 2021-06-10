#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>
#include <cstdlib>
#include <functional>
#include <sstream>


using namespace std;
using namespace std::chrono;

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
T updateNthCoefficient(vector<T> &x, vector<T> &xx, vector<T> &xxx, int n){
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
	 updateNthCoefficient(x,xx,xxx,0);
	 
	 for(int n = 0; n<nn-1; n++){
		 xprime[n] = xx[n]-xxx[n];
		 x[n+1] = xprime[n]/(n+1);
		 updateNthCoefficient(x,xx,xxx,n+1);
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
pair<T, T> computeNext(T x0, T step,bool forward,int n){
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
		return {eval(coeff,step), eval(deriv,step)};
	 }
	 else{
		return {eval(coeff,-step), eval(deriv,-step)};
	}
}

template<class T>
pair<vector<T>, vector<T> > generateSolutionStepper(T x0,T step, bool forward, T end, int n){
	/*Inputs:
	 * x0  : parameters of the ODE
	 * step: Size of the step
	 * end : End point to solve the solution (solves in [0,end])
	 * n   : Degree of the approximation
	 * forward: if you want to go forward or backwards 
	 * Output:
	 * vector of solutions to the equation x' = x^2-x^3
	 */
	 step = fabs(step);
	 vector<T> sol;
	 sol.push_back(x0);
	 vector<T> deriv;
	 deriv.push_back(pow(x0,2.0)-pow(x0,3.0));
	 double newx0 = x0;
	 for(int k = 1; step*k<=end; k++){
		pair<T, T> cond = computeNext(newx0,step,forward,n);
		sol.push_back(cond.first);
		deriv.push_back(cond.second);
		newx0 = cond.first; 
	 }
	 return {sol,deriv};
}


int main(int argc, const char* argv[]){
	
	// check parameters
    if (argc != 4) {
        cout << "Usage: " << argv[0] << "<x0> <tn> <dt>" << endl;
        return EXIT_FAILURE;
    }

    // parse parameters
    double x0  = stod(argv[1], NULL);
    double end = stod(argv[2], NULL);
    double step = stod(argv[3], NULL);
	int n = 10;
	vector<double> coeff = coefficients(x0,n);	
	bool forward = 0;
	vector<double> solutionStepperB = generateSolutionStepper(x0,step,forward,end, n).first;
	vector<double> derivSolutionB = generateSolutionStepper(x0,step,forward,end, n).second;
	forward = 1;
	vector<double> solutionStepperF = generateSolutionStepper(x0,step,forward,end, n).first;
	vector<double> derivSolutionF = generateSolutionStepper(x0,step,forward,end, n).second;
    out.open("out.dat");
	for(int i = solutionStepperB.size()-1; i>=0; i--){
		int dig = 1000;
		out.precision(5);
		out<<fixed<<setw(15)<<-i*step<<" ";
		double stepper = solutionStepperB[i];
		double deriv = derivSolutionB[i];
		
		if( fabs(stepper)>dig || fabs(deriv)>dig ){
			out<<scientific<<setw(15)<<stepper<<" "<<setw(15)<<deriv<<"\n";
		}
		else{					
			out<<fixed<<setw(15)<<stepper<<" "<<setw(15)<<deriv<<"\n";
		}
	}
	for(int i = 1; i<solutionStepperF.size(); i++){
		int dig = 1000;
		out.precision(5);
		out<<fixed<<setw(15)<<i*step<<" ";
		double stepper = solutionStepperF[i];
		double deriv = derivSolutionF[i];
		
		if( fabs(stepper)>dig || fabs(deriv)>dig ){
			out<<scientific<<setw(15)<<stepper<<" "<<setw(15)<<deriv<<"\n";
		}
		else{					
			out<<fixed<<setw(15)<<stepper<<" "<<setw(15)<<deriv<<"\n";
		}
	}
    out.close();
}
