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
		
		int getMaxDegree();
		void setMaxDegree(int);
		
		//Basic functions
		double nthCoefficientProduct(const vector<double>&, const vector<double>&, int );
		double eval(const vector<double>&, double);
		vector<double> evaluateAll(const vector<vector<double>>&, double);
		vector<double> calculateDerivative(const vector<double>&);
		vector<vector<double>> computeCoefficients(const vector<double>&, const vector<double>&,int);

		virtual Solution findSolution(const vector<double>&, const vector<double>&, double, double, int, bool);
		virtual void updateNthCoefficient(const vector<double> &, vector<vector<double>> &, int n){};
			
		//Adaptive functions
		double findRadiusOfConvergence(const vector<double> & ,bool );
		double approximateRadiusOfConvergence(const vector<double> &);
		double approximateRadiusOfConvergence(const vector<vector<double>> &);
		
		virtual Solution findSolutionAdaptive(const vector<double> &,const vector<double> &, double end, bool, double );
		virtual Solution findAdaptiveSolutionTruncation(const vector<double> &,const vector<double> &, double , bool ,double);
		virtual Solution findAdaptiveSolutionJorbaAndZou(const vector<double> &,const vector<double> &, double , bool ,double);
	
	private:
		int maxDegree = 25;

};
#endif
