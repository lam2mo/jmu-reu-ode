#include<bits/stdc++.h>

using namespace std;
using namespace std::chrono;


double nthCoefficientProduct(vector<double> &x, vector<double> &y, int n){
	/* This method computes the nth coefficient of x*y, runs in O(n)
	 */
	double sum = 0;
	for(int i  = 0; i<=n; i++){
		sum+=x[i]*y[n-i];
	}
	return sum;
}

void updateNthCoefficient(vector<double> &x, vector<double> &xprime, vector<double> &xx, int n){
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


vector<double> coefficients(double a,double x0, double x00, int nn){
	/* Inputs
	 * a     : Parameters of ODE
	 * n     : order of the polynomial approximation
	 * x0,x00: initial conditions 
	 * Output:
	 * coefficients of the nth polynomial approximation in increasing order
	 * 
	 * Runs in O(n^2)!!
	 */
	 vector<double> x(nn,0);
	 vector<double> deriv(nn,0);
	 vector<double> secondDeriv(nn,0);
	 vector<double> square(nn,0); //This is actually x^2-1
	 
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

double eval(vector<double> coeff, double x){
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

vector<double> derivative(vector<double> f){
	vector<double> deriv(f.size()-1,0);
	for(int i = 0; i< deriv.size(); i++){
		deriv[i] = f[i+1]*(i+1);
	}
	return deriv;
}

pair<double, double> computeNext(double a,double x0, double x00, double step,bool forward,int n){
	/*Inputs:
	 * a,x0,x00: parameters of the ODE
	 * step: value to be found
	 * Outputs:
	 * x(t) where t = x0+step and 
	 * x(t) = ax(t)^2+bx(t)+c
	 */
	 vector<double> coeff = coefficients(a,x0,x00,n);
	 vector<double> deriv = derivative(coeff);
	 if(forward){
		return {eval(coeff,step), eval(deriv,step)};
	 }
	 else{
		return {eval(coeff,-step), eval(deriv,-step)};
	}
}


vector<double> generateSolutionStepper(double a,double x0, double x00, double step, bool forward, double end, int n){
	/*Inputs:
	 * a,b,c,x0: parameters of the ODE
	 * step: value to be found
	 * (beg,end): interval to find the solution
	 * step: size of the step, expecting a positive value
	 * forward: if you want to go forward or backwards 
	 * Output:
	 * vector of solutions to the Vander Pol Oscillator equation
	 */
	 step = abs(step);
	 vector<double> sol;
	 sol.push_back(x0);
	 double newx0 = x0;
	 double newx00 = x00;
	 for(int k = 1; step*k<=end; k++){
		//cout<<step*k<<" "<<computeNext(a,b,c,solution[k-1],step,forward,n)<<"\n";
		pair<double, double> cond = computeNext(a,newx0,newx00,step,forward,n);
		sol.push_back(cond.first);
		newx0 = cond.first;
		newx00 = cond.second;		 
	 }
	 return sol;
}

vector<double> generateSolutionNoStepper(double a, double x0, double x00, double step, double end, int n){
	vector<double> coeff = coefficients(a,x0,x00,n);
	vector<double> sol;
	sol.push_back(x0);
	 for(int k = 1; step*k<=end; k++){		 
		 sol.push_back(eval(coeff,step*k));
	 }
	 return sol;
}



int main(){
	double a  = -1;
	double x0 = 1;
	double x00 = 1;
	int n = 10;
	double end = 2;
	double step = 1/8.0;
	
	vector<double> coeff = coefficients(a,x0,x00,n);	
	
	vector<double> solutionStepper = generateSolutionStepper(a,x0,x00,step,1,end, n);
	vector<double> solutionNoStepper = generateSolutionNoStepper(a,x0,x00,step,end, n);
	
	cout<<setw(15)<<"t"<<setw(15)<<"No Stepper"<<setw(15)<<"Stepper"<<"\n";
	for(int i = 0; i<solutionStepper.size(); i++){
		int dig = 1000;
		cout.precision(5);
		cout<<fixed<<setw(15)<<i*step<<" ";
		double stepper = solutionStepper[i];
		double noStepper = solutionNoStepper[i];
		
		if( abs(stepper)>dig || abs(noStepper)>dig ){
			cout<<scientific<<setw(15)<<noStepper<<" "<<setw(15)<<stepper<<"\n";
		}
		else{					
			cout<<fixed<<setw(15)<<noStepper<<" "<<setw(15)<<stepper<<"\n";
		}
	}
}
