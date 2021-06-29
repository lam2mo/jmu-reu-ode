


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
	int n = coeff.size();
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

vector<vector<double>> PSM::computeCoefficientsAdaptive(const vector<double> &parameters, const vector<double> &initialConditions){
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
    int maxDegree = 25; //This is arbitrary as of right now 
	int k = initialConditions.size();
	vector<vector<double>> coefficients(k,vector<double> (maxDegree,0));
	
	//Add initial conditions
	for(int i = 0; i<k; i++){
		coefficients[i][0] = initialConditions[i];
	}
	/*Calculate solution*/
	for(int n = 0; n<maxDegree-1; n++){
		//This function has to be defined for every problem
		updateNthCoefficient(parameters,coefficients,n);
		if(n>20 && approximateRadiusOfConvergence(coefficients)!= -1){
			//We've found an approximation to the radius of convergence
			for(int i = 0; i<k; i++){
				coefficients[i].resize(n);
			}
			break;
		}
	}
	return coefficients;
}

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
		vector<vector<double>> coeff = PSM::computeCoefficientsAdaptive(parameters,currentInitialConditions);
		step = approximateRadiusOfConvergence(coeff)/2.0; //This is an estimation to the radius of convergence
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
