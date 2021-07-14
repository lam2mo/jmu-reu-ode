#include <vector>
#ifndef PSM_H
#define PSM_H

using namespace std;

class PSM{
	public:
		/**
		 * @struct Solution
		 * @brief This structre saves a standard PSM solution
		 * @var Solution::steps 
		 * Member 'steps' contains the steps taken by the algorithm
		 * @var Solution::solutions 
		 * Member 'solutions' contains the values of the solution at each step
		 */
		struct Solution{
			vector<double> steps;
			vector<vector<double>> solutions;		
		};
		
		
		/// Returns MaxDegree 
		int getMaxDegree();
		/// Sets MaxDegree
		void setMaxDegree(int);
		
		/**
		 * Calculates the nth coefficient of the product of two polynomials
		 * @param x First polynomial
		 * @param y Second polynomial
		 * @param n Desired coefficient
		 * @returns n-th coefficient of x*y
		 */
		double nthCoefficientProduct(const vector<double>& x, const vector<double>& y, int n);
		
		/**
		 *Evaluates a polynomial at a point using Horners algorithm
		 * @param coeff coefficients of the polynomial
		 * @param x     point to evaluate at
		 * @returns p(x) where p is the polynomial determined by coeff
		 */
		double eval(const vector<double>& coeff, double x);
		
		/**
		 * Evaluates several polynomials at a point
		 * @param coeff Vector of polynomials in coefficient form
		 * @param x     Point to evaluate at
		 * @returns vector of evaluations at every polynomial
		 */
		vector<double> evaluateAll(const vector<vector<double>>& coeff, double x);
		
		/**
		 * Calulates the derivative of a polynomial
		 * @param coeff coefficients of the polynomial
		 * @returns derivative of coeff
		 */ 
		vector<double> calculateDerivative(const vector<double>& coeff);
		
			
		/**
		 * Given initial conditions it computes a PSM solution around 0
		 * @param parameters any parameters that the ODE has
		 * @param initialConditions initial conditions of all variables
		 * @param nn degree of the approximation
		 * @returns coefficients of the solution around 0
		 */
		vector<vector<double>> computeCoefficients(const vector<double> &parameters, const vector<double> &initialConditions, int nn);
		
		/**
		 * Solves an ODE in a interval using fixed step
		 * @param parameters any parameters that the ODE has
		 * @param initialConditions initial conditions of all variables
		 * @param step step size
		 * @param end  end point of the interval
		 * @param n  degree of the approximation
		 * @param forward tells wheter we should solve forward or backwards (1 for forward, 0 otherwise)
		 * 
		 * @returns PSM::Solution with approximations to the solution 
		 */
		virtual Solution findSolution(const vector<double> &parameters, const vector<double> &initialConditions, double step, double end, int n, bool forward);
		
		/**
		 * Recursion relation for a problem, this function needs to be changed for every ODE
		 * @param parameters any parameters that the ODE has
		 * @param y vector of variables and auxiliary variables used in the recursion
		 * @param n coefficient to be updated
		 */
		virtual void updateNthCoefficient(const vector<double> &parameters, vector<vector<double>> &y, int n){};
			
			
		//Adaptive functions
		
		
		/**
		 * Returns 1/r where r is an approximation the radius of convergence.
		 * For n<8 it returns the root test, if n>=8 it uses Aitkens acceleration
		 * @param a coefficients of a power series
		 * @param EvenRadius tells us if we want to calculate the radius of convergence of even terms
		 * @returns 1/r with r an approximation to the radius of convergence
		 */
		double findRadiusOfConvergence(const vector<double> &a,bool EvenRadius);
		
		/**
		 * Returns 1/r where r is an approximation the radius of convergence.
		 * For n<8 it returns the root test, if n>=8 it uses Aitkens acceleration
		 * @param a coefficients of a power series
		 * @returns 1/r with r an approximation to the radius of convergence
		 */
		double approximateRadiusOfConvergence(const vector<double> &coeff);
		
		/**
		 * Computes the radius of convergence that applies to all auxiliary variables
		 * @param coeff vector of power series
		 * @returns r an approximation to the radius of convergence for all functions
		 */
		double approximateRadiusOfConvergence(const vector<vector<double>> &coeff);
		
		
		/**
		 * Solves an ODE by the PSM adaptive method using Aitkens acceleration
		 * @param parameters any parameters that the ODE might have
		 * @param initialConditions initialConditions of all variables
		 * @param end end point of the interval 
		 * @param forward if we should solve forward or backwards in time
		 * @param eps Tolerance
		 * 
		 * @returns PSM::Solution that we found through the adaptive method
		 */
		virtual Solution findSolutionAdaptive(const vector<double> &parameters,const vector<double> &initialConditions, double end, bool forward,double eps);
		
		/**
		 * Solves an ODE by the PSM adaptive method using Warne's method
		 * @param parameters any parameters that the ODE might have
		 * @param initialConditions initialConditions of all variables
		 * @param end end point of the interval 
		 * @param forward if we should solve forward or backwards in time
		 * @param eps Tolerance
		 * 
		 * @returns PSM::Solution that we found through the adaptive method
		 */
		virtual Solution findAdaptiveSolutionTruncation(const vector<double> &parameters,const vector<double> &initialConditions, double end, bool forward,double eps);
		
		/**
		 * Solves an ODE by the PSM adaptive method using Jorba and Zou 
		 * @param parameters any parameters that the ODE might have
		 * @param initialConditions initialConditions of all variables
		 * @param end end point of the interval 
		 * @param forward if we should solve forward or backwards in time
		 * @param eps Tolerance
		 * 
		 * @returns PSM::Solution that we found through the adaptive method
		 */
		virtual Solution findAdaptiveSolutionJorbaAndZou(const vector<double> &parameters,const vector<double> &initialConditions, double end, bool forward,double eps);
	
	private:
		//Degree that we'll use for the adaptive methods
		int maxDegree = 25;

};
#endif
