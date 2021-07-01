#include "psmGeneralFunctions.h"
#include <vector>
#include <cmath>
#include <iostream>

using namespace std;

bool debug = 0;


		
double PSM::nthCoefficientProduct(const vector<double> &x, const vector<double> &y, int n){
	/* This method computes the nth coefficient of x*y, runs in O(n)
	 */
	double sum = 0;
	for(int i  = 0; i<=n; i++){
		sum+=x[i]*y[n-i];
	}
	return sum;
}

double PSM::eval(const vector<double> &coeff, double x){
	/* Evaluates a Polynomial given its coefficients
	 * Inputs:
	 * coeff: coefficients of the polynomial to be evaluated in increasing order
	 * x    : number to be evaluated
	 * Output:
	 * p(x) 
	 */
	
	//Horners Algorithm
	double val = 0;
	for(int i = coeff.size()-1; i>=0; i--){
		val = val*x+coeff[i];
	}
	return val;
}

vector<double> PSM::evaluateAll(const vector<vector<double>> &coeff, double x){
	/* Evaluates a set of polynomials at x
	 */
	int n = coeff.size();
	vector<double> sol(n);
	for(int i = 0; i<n; i++){
		sol[i] = PSM::eval(coeff[i],x);
	}
	return sol;
}

vector<double> PSM::calculateDerivative(const vector<double> &f){
	/*This method calculates the derivative of a polynomial*/
	vector<double> deriv(f.size()-1,0);
	for(unsigned int i = 0; i< deriv.size(); i++){
		deriv[i] = f[i+1]*(i+1);
	}
	return deriv;
}



//nn means degree
vector<vector<double>> PSM::computeCoefficients(const vector<double> &parameters, const vector<double> &initialConditions, int nn){
	/* Inputs
	 * parameters: parameters of the equation
	 * initialConditions: initial conditions
	 * nn : order of the polynomial approximation

	 * Output:
	 * coefficients of the nth polynomial approximation in increasing order
	 * 
	 * Runs in O(n^2), memory O(n)
	 */
     //Initial Conditions
	int k = initialConditions.size();
	vector<vector<double>> coefficients(k,vector<double> (nn,0));
	
	//Add initial conditions
	for(int i = 0; i<k; i++){
		coefficients[i][0] = initialConditions[i];
	}
	/*Calculate solution*/
	for(int n = 0; n<nn-1; n++){
		//This function has to be defined for every problem
		updateNthCoefficient(parameters,coefficients,n);
	}
	return coefficients;
}



PSM::Solution PSM::findSolution(const vector<double> &parameters,
        const vector<double> &initialConditions, double step, double end, int n, bool forward){
	/*This function is the general PSM  solver
	 * Input:
	 * parameters: vector of parameters of the ODE
	 * initialConditions: vector of initial conditions, it's a vector for systems of ODEs 
	 * step: size of the step
	 * end : end of the interval
	 * forward: direction in which we should move
	 */
	int numberOfEquations = initialConditions.size();
	vector<vector<double>> solution(numberOfEquations); //We might want to include any auxiliary variables in here as well
	vector<double> steps;
	steps.push_back(0);
	//Initialize initial conditions
	vector<double> currentInitialConditions(numberOfEquations);
	for(int i = 0; i<numberOfEquations; i++){
		solution[i].push_back(initialConditions[i]);
		currentInitialConditions[i] = initialConditions[i];
	}
	//Start Stepping
	for(int i = 1; step*i<= end; i++){
		vector<vector<double>> coeff = PSM::computeCoefficients(parameters,currentInitialConditions,n);
		if(forward){
			steps.push_back(step*i);
			currentInitialConditions = PSM::evaluateAll(coeff,step);
		}
		else{
			steps.push_back(-step*i);
			currentInitialConditions = PSM::evaluateAll(coeff,-step);
		}
		for(int j = 0; j<numberOfEquations; j++){
			solution[j].push_back(currentInitialConditions[j]);
		}	
	}
	Solution sol {steps, solution};
	return sol;
} 


//Adaptive Stuff




double PSM::findRadiusOfConvergence(const vector<double> &a,bool EvenRadius){
	// This method returns 1/r, where r is an approximation to the radius of convergence
	// returns -1 if n<8
	int n = a.size();
	if(n<8){
		return -1;
	}
	if((n%2 == 0 && EvenRadius) || (n%2 != 0 && !EvenRadius)){
		n-=2;
	}
	else{
		n--;
	}
	double xn= pow(fabs(a[n]),1.0/n);
	double xn1 = pow(fabs(a[n-2]),1.0/(n-2));
	double xn2 = pow(fabs(a[n-4]),1.0/(n-4));
	double bn = xn-pow(xn-xn1,2)/((xn-xn1)-(xn1-xn2));
	return bn;
}


  
double PSM::approximateRadiusOfConvergence(const vector<double> &coeff){
	return max(findRadiusOfConvergence(coeff,1),findRadiusOfConvergence(coeff,0));	

}  

double PSM::approximateRadiusOfConvergence(const vector<vector<double>> &coeff){
	int n = coeff.size();
	double invR = approximateRadiusOfConvergence(coeff[0]);
	for(int i = 1; i<n; i++){
		invR = max(invR,approximateRadiusOfConvergence(coeff[i]));
	}
	return 1.0/invR;
}  
  
double PSM::findMaximumTimeStep(int k,double eps){
	double a = 0; double b = 1;
	while(fabs(b-a)> eps){
		double c = a+(b-a)/2.0;		
		double val = pow(c,k)/(1-c);
		if(val<eps){
			a = c;
		}
		else{
			b = c;
		}
	}
	return b;
}

/*As for now I'll just have this as a global variable
I should change it later on
*/

int maxDegree = 25;

//The following are 3 adaptive methods



PSM::Solution PSM::findSolutionAdaptive(const vector<double> &parameters,
        const vector<double> &initialConditions, double end, bool forward){
			
	int numberOfEquations = initialConditions.size();
	vector<vector<double>> solution(numberOfEquations); //We might want to include any auxiliary variables in here as well
	vector<double> steps;
	steps.push_back(0);
	//Initialize initial conditions
	vector<double> currentInitialConditions(numberOfEquations);
	for(int i = 0; i<numberOfEquations; i++){
		solution[i].push_back(initialConditions[i]);
		currentInitialConditions[i] = initialConditions[i];
	}
	
	//Start Stepping
	double cur = 0;
	double step = 0;
	while(cur< end){
		vector<vector<double>> coeff = PSM::computeCoefficients(parameters,currentInitialConditions,maxDegree);
		step = approximateRadiusOfConvergence(coeff)/3.0; //This is an estimation to the radius of convergence
		cur+= step;
		if(forward){
			steps.push_back(cur);
			currentInitialConditions = PSM::evaluateAll(coeff,step);
		}
		else{
			steps.push_back(-cur);
			currentInitialConditions = PSM::evaluateAll(coeff,-step);
		}
		for(int j = 0; j<numberOfEquations; j++){
			solution[j].push_back(currentInitialConditions[j]);
		}	
	}
	Solution sol {steps, solution};
	return sol;				
}


PSM::Solution PSM::findAdaptiveSolutionTruncation(const vector<double> &parameters,
        const vector<double> &initialConditions, double end, bool forward,double eps){
	
	int numberOfEquations = initialConditions.size();
	vector<vector<double>> solution(numberOfEquations); //We might want to include any auxiliary variables in here as well
	vector<double> steps;
	steps.push_back(0);
	//Initialize initial conditions
	vector<double> currentInitialConditions(numberOfEquations);
	for(int i = 0; i<numberOfEquations; i++){
		solution[i].push_back(initialConditions[i]);
		currentInitialConditions[i] = initialConditions[i];
	}
	
	//Start Stepping
	double cur = 0;
	double step = 0;
	while(cur< end){
		vector<vector<double>> coeff = PSM::computeCoefficients(parameters,currentInitialConditions,maxDegree);
		double lastNonZeroCoefficient = max(fabs(coeff[0][maxDegree-1]),fabs(coeff[0][maxDegree-2]));
		step = pow(fabs(eps/(2*lastNonZeroCoefficient)),1.0/(maxDegree-1)); //This is an estimation to the radius of convergence
		cur+= step;
		if(forward){
			steps.push_back(cur);
			currentInitialConditions = PSM::evaluateAll(coeff,step);
		}
		else{
			steps.push_back(-cur);
			currentInitialConditions = PSM::evaluateAll(coeff,-step);
		}
		for(int j = 0; j<numberOfEquations; j++){
			solution[j].push_back(currentInitialConditions[j]);
		}	
	}
	Solution sol {steps, solution};
	return sol;		
	
}



