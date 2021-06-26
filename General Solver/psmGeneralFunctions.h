#include <vector>
#ifndef PSM_H
#define PSM_H

using namespace std;

class PSM{
	public:
		struct Solution{
			vector<double> steps;
			vector<vector<double>> solutions;		
		};
		
		double nthCoefficientProduct(const vector<double>&, const vector<double>&, int );
		double eval(const vector<double>&, double);
		vector<double> evaluateAll(const vector<vector<double>>&, double);
		vector<double> calculateDerivative(const vector<double>&);
		
		vector<vector<double>> computeCoefficients(const vector<double>&, const vector<double>&,int);
		
		
		virtual Solution findSolution(const vector<double>&, const vector<double>&, double, double, int, bool);
		virtual void updateNthCoefficient(const vector<double> &parameters, vector<vector<double>> &y, int n){};
		


};
#endif
