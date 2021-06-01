/**
 * JMU REU 2021
 *
 * Cauchy Product coefficients
 *
 * @author Mike Lam
 */

#include <cstdlib>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

class PerfStats
{
    long loads, adds, muls;

public:
    PerfStats() : loads(0), adds(0), muls(0) { }
    PerfStats(long l, long a, long m) : loads(l), adds(a), muls(m) { }

    PerfStats operator+(const PerfStats& rhs)
    {
        return PerfStats(loads + rhs.loads, adds + rhs.adds, muls + rhs.muls);
    }

    PerfStats& operator+=(const PerfStats& rhs)
    {
        loads += rhs.loads;
        adds  += rhs.adds;
        muls  += rhs.muls;
        return *this;
    }

    friend ostream& operator<<(ostream&, const PerfStats&);
};

class Coeff
{
    char var;
    int idx;

public:
    Coeff(char v, int i) : var(v), idx(i)  { }
    PerfStats simulate() { return PerfStats(1, 0, 0); }

    friend ostream& operator<<(ostream&, const Coeff&);
};

class Term
{
    Coeff left, right;

public:
    Term(Coeff l, Coeff r) : left(l), right(r)  { }

    PerfStats simulate()
    {
        PerfStats stats(0, 0, 1);
        stats += left.simulate();
        stats += right.simulate();
        return stats;
    }

    friend ostream& operator<<(ostream&, const Term&);
};

class AA : public Term
{
public:
    AA(int i, int j) : Term(Coeff('a',i), Coeff('a',j)) { }
};

class AB : public Term
{
public:
    AB(int i, int j) : Term(Coeff('a',i), Coeff('b',j)) { }
};

class Expr
{
    vector<Term> terms;

public:
    Expr() { }
    void add(const Term& t) { terms.push_back(t); }

    PerfStats simulate()
    {
        PerfStats stats(0, 0, 0);
        bool first = true;
        for (auto t : terms) {
            if (first) {
                first = false;
            } else {
                stats += PerfStats(0, 1, 0);
            }
            stats += t.simulate();
        }
        return stats;
    }

    friend ostream& operator<<(ostream&, const Expr&);
};

ostream& operator<<(ostream& os, const PerfStats& stats)
{
    os << "Memory loads:    " << stats.loads << endl;
    os << "Additions:       " << stats.adds << endl;
    os << "Multiplications: " << stats.muls << endl;
    return os;
}

ostream& operator<<(ostream& os, const Coeff& coeff)
{
    os << coeff.var << "_" << coeff.idx;
    return os;
}

ostream& operator<<(ostream& os, const Term& term)
{
    os << term.left << "*" << term.right;
    return os;
}

ostream& operator<<(ostream& os, const Expr& expr)
{
    bool first = true;
    for (auto t : expr.terms) {
        if (first) {
            first = false;
        } else {
            os << " + ";
        }
        os << t;
    }
    return os;
}

int main(int argc, const char* argv[])
{
    // check and parse parameter(s)
    if (argc != 2) {
        cout << "Usage: " << argv[0] << " <n>" << endl;
        return EXIT_FAILURE;
    }
    int n = stod(argv[1], NULL);

    // computational statistics
    PerfStats all_stats;

    // calculate b coefficients
    vector<Expr> b_exprs;
    for (int b = 0; b < n; b++) {
        Expr e;
        for (int i = 0; i <= b; i++) {
            e.add(AA(i, b-i));
        }
        b_exprs.push_back(e);
        cout << "b" << b << " = " << e << endl;
        all_stats += e.simulate();
    }
    cout << endl;

    // calculate c coefficients
    vector<Expr> c_exprs;
    for (int c = 0; c < n; c++) {
        Expr e;
        for (int i = 0; i <= c; i++) {
            e.add(AB(i, c-i));
        }
        cout << "c" << c << " = " << e << endl;
        all_stats += e.simulate();
    }
    cout << endl;

    // print computational stats
    cout << all_stats << endl;

    return EXIT_SUCCESS;
}
