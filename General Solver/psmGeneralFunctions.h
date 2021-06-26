#include <vector>
#ifndef PSM_H
#define PSM_H

using namespace std;

class PSM{
	public:
		double nthCoefficientProduct(vector<double>, vector<double>, int n);
		double eval(vector<double>, double);
		vector<double> evaluateAll(vector<vector<double>>, double);
		vector<double> calculateDerivative(vector<double>);
		
		vector<vector<double>> computeCoefficients(vector<double>,vector<double>,int);
		vector<vector<double>> findSolution(vector<double>, vector<double> ,double ,double ,int , bool);
		
		
		virtual void updateNthCoefficient(vector<double> parameters, vector<vector<double>> &y, int n) = 0;
};
#endif
