#include "psmGeneralFunctions.h"
#include <vector>
#include <cmath>
#include <iostream>

using namespace std;

bool debug = 0;

int PSM::getMaxDegree(){
	return maxDegree;
}

void PSM::setMaxDegree(int maxDegree){
	this->maxDegree = maxDegree;
} 		
double PSM::nthCoefficientProduct(const vector<double> &x, const vector<double> &y, int n){
	double sum = 0;
	for(int i  = 0; i<=n; i++){
		sum+=x[i]*y[n-i];
	}
	return sum;
}

double PSM::eval(const vector<double> &coeff, double x){
	double val = 0;
	for(int i = coeff.size()-1; i>=0; i--){
		val = val*x+coeff[i];
	}
	return val;
}

vector<double> PSM::evaluateAll(const vector<vector<double>> &coeff, double x){

	int n = coeff.size();
	vector<double> sol(n);
	for(int i = 0; i<n; i++){
		sol[i] = PSM::eval(coeff[i],x);
	}
	return sol;
}

vector<double> PSM::calculateDerivative(const vector<double> &coeff){
	vector<double> deriv(coeff.size()-1,0);
	for(unsigned int i = 0; i< deriv.size(); i++){
		deriv[i] = coeff[i+1]*(i+1);
	}
	return deriv;
}



vector<vector<double>> PSM::computeCoefficients(const vector<double> &parameters, const vector<double> &initialConditions, int nn){

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
	int n = a.size();
	if(n<8){
		// Return rooth test
		double lastNonZeroCoefficient = max(fabs(a[n-1]),fabs(a[n-2]));
		double radius = pow(lastNonZeroCoefficient,1.0/(n-1)); //This is an estimation to the radius of convergence
		
		return radius;
	}
	//Simplify calculations by calculating the even or odd radius
	if((n%2 == 0 && EvenRadius) || (n%2 != 0 && !EvenRadius)){
		n-=2;
	}
	else{
		n--;
	}
	//Aitkens accelaration
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



//The following are 3 adaptive methods



PSM::Solution PSM::findSolutionAdaptive(const vector<double> &parameters,
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
		step = pow(eps,1.0/(coeff[0].size()-1))/approximateRadiusOfConvergence(coeff[0]); //This is an estimation to the radius of convergence
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
		
		step = pow(abs(2*coeff[0][maxDegree-2]/eps),1.0/(maxDegree-2));
		step = max(pow(abs(2*coeff[0][maxDegree-1]/eps),1.0/(maxDegree-1)),step);
		step = 1.0/step;
		
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



PSM::Solution PSM::findAdaptiveSolutionJorbaAndZou(const vector<double> &parameters,
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
	double infNorm = 1;
	
	int degree = ceil(-log(eps)/2.0 +1);
	while(cur< end){
		vector<vector<double>> coeff = PSM::computeCoefficients(parameters,currentInitialConditions,degree);

		step = pow(abs(coeff[0][degree-2]/infNorm),1.0/(degree-2));
		step = max(pow(abs(coeff[0][degree-1]/infNorm),1.0/(degree-1)),step);
		
		step = 1.0/step;
		step = step/exp(2)*exp(-.7/(degree-1));
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
		if(cur != 0){
			//This part needs to be changed for systems of differential equations, as we should ignore auxiliary variables when calculating the max
			infNorm = max(infNorm,abs(currentInitialConditions[0]));
		}	
	}
	Solution sol {steps, solution};
	return sol;		
	
}

