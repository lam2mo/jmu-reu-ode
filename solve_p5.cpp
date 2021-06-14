#include <iostream>
#include <cmath>
#include <vector>
#include <iomanip>
#include <fstream>

using namespace std;

ofstream out;

double C, M;

//Cauchy Product
double nthCoefficientProduct(vector<double> &x, vector<double> &y, int n){
	/* This method computes the nth coefficient of x*y, runs in O(n)
	 */
	double sum = 0;
	for(int i  = 0; i<=n; i++){
		sum+=x[i]*y[n-i];
	}
	return sum;
}

void updateNthCoefficient(vector<double> &y, vector<double> &u, vector<double> &w, int n){
    y[n+1] = u[n]/(n+1);
    u[n+1] = (nthCoefficientProduct(u,w,n))/(n+1);
    w[n+1] = -nthCoefficientProduct(u,u,n)/(n+1);
}

//nn means degree
vector<double> coefficients(double x0, int nn){
	/* Inputs
	 * nn : order of the polynomial approximation
	 * x0: initial conditions 
	 * Output:
	 * coefficients of the nth polynomial approximation in increasing order
	 * 
	 * Runs in O(n^2), memory O(n)
	 */
	 vector<double> y(nn,0);
	 vector<double> u(nn,0);
	 vector<double> w(nn,0);
     
     //Initial Conditions
	 y[0] = x0;  
	 u[0] = sin(x0);
     w[0] = cos(x0);

	 for(int n = 0; n<nn-1; n++){
        updateNthCoefficient(y,u,w,n);
	 }
	 return y;
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

vector<double> solve(double x0,double step, double end, int n, bool forward){
    vector<double> solution;
    solution.push_back(x0);
    double newx0 = x0;
    
    for(int i = 1; step*i<= end; i++){
        vector<double> coeff = coefficients(newx0,n);
        if(forward){
            newx0 = eval(coeff,step);
        }
        else{
            newx0 = eval(coeff,-step);
        }
        solution.push_back(newx0);
    }
    return solution;
}

vector<double> findDerivative(double x0,double step, double end, int n, bool forward){
    vector<double> deriv; //Keeps track of the derivative 
    deriv.push_back(sin(x0));
    double newx0 = x0;
    
    for(int i = 1; step*i<= end; i++){
        vector<double> coeff = coefficients(newx0,n);
        if(forward){
            newx0 = eval(coeff, step);
        }
        else{
            newx0 = eval(coeff, -step);
        }
        double dv = eval(derivative(coeff),newx0);
        deriv.push_back(dv);
    }
    return deriv;
}

double acot(double x) {
  return (2*atan(1)) - atan(x);
}

double cot(double x){
    return 1/tan(x);
}

double evaluateFunction(double t,double x0){
    double res = 2*acot(exp(-t)*cot(x0/2));
    return res;
}

int main(int argc, const char* argv[]){

    if (argc != 5) {
        cout << "Usage: " << argv[0] << " <x0> <tn> <dt> <n>" << endl;
        return EXIT_FAILURE;
    }
    double x0 = stod(argv[1]);
    double step = stod(argv[2]);
    double end = stod(argv[3]);
    int n = stod(argv[4]);
    
    vector<double> solF = solve(x0, step,end,n, 1);
    vector<double> dSolF = findDerivative(x0,step,end,n,1);
    vector<double> solB = solve(x0, step,end,n, 0);
    vector<double> dsolB = findDerivative(x0,step,end,n,0);
    
    double Terror = 0;
    double error = 0;   
    double difference = 0;
    out.open("out.dat");
    for(int i = solB.size()-1; i>0; i--){
        C = M = 1;
        if(fabs(solB[i-1])>1){
            C = M = fabs(solB[i-1]);
        }
        Terror = (C*pow(M*step,n+1))/(1-fabs(M*step));
        difference = fabs(sin(solB[i])-dsolB[i]); //|sin(y)-y'|
        error = abs(evaluateFunction(-i*step,x0)-solB[i]);
        out<<setw(15)<<-i*step<<setw(15)<<solB[i]<<setw(15)<<error<<setw(15)<<difference<<setw(15)<<error<<"\n";       
    }
    out<<setw(15)<<0<<setw(15)<<solF[0]<<setw(15)<<0<<"\n";       
    for(unsigned int i = 1; i<solF.size();i++){
        C = M = 1;
        if(fabs(solF[i-1])>1){
            C = M = fabs(solF[i-1]);
        }
        Terror = (C*pow(M*step,n+1))/(1-fabs(M*step));
        difference = fabs(sin(solF[i])-dSolF[i]); //|sin(y)-y'|
        error = abs(evaluateFunction(i*step,x0)-solF[i]);
        out<<setw(15)<<i*step<<setw(15)<<solF[i]<<setw(15)<<Terror<<setw(15)<<difference<<setw(15)<<error<<"\n";
    }
    out.close();

}
