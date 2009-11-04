/*
UnitTest.gui

These tests are for "miscellaneous" mathlib functions that don't have their own class.
*/
TestMathLib : UnitTest {
	
	test_correlation {
		var x, y, n, p;
		
		n = 1000000;
		
		// Uncorrelated
		x = {10.0.sum3rand}.dup(n);
		y = {10.0.sum3rand}.dup(n);
		p  = corr(x, y);
		this.assert(p.abs < 0.002, "corr() on uncorrelated data should give near-zero value (this test may [rarely] fail by chance)");
		// Correlated
		x = {10.0.sum3rand}.dup(n);
		y = x.deepCopy;
		p  = corr(x, y);
		this.assert(p == 1.0, "corr() on perfectly correlated data == 1");
		// Anticorrelated
		x = {10.0.sum3rand}.dup(n);
		y = x.collect(0.0 - _);
		p  = corr(x, y);
		this.assert(p == -1.0, "corr() on perfectly correlated data == -1");
		
		
		n = 50000; // spearmanRho is much slower than corr so let's use slightly smaller data
		
		// Uncorrelated
		x = {10.0.sum3rand}.dup(n);
		y = {10.0.sum3rand}.dup(n);
		p  = spearmanRho(x, y);
		this.assert(p.abs < 0.012, "spearmanRho() on uncorrelated data should give near-zero value (this test may [rarely] fail by chance)");
		// Correlated
		x = {10.0.sum3rand}.dup(n);
		y = x.deepCopy;
		p  = spearmanRho(x, y);
		this.assert(p == 1.0, "spearmanRho() on perfectly correlated data == 1");
		// Anticorrelated
		x = {10.0.sum3rand}.dup(n);
		y = x.collect(0.0 - _);
		p  = spearmanRho(x, y);
		this.assert(p == -1.0, "spearmanRho() on perfectly correlated data == -1");
		
		
		
	}
	
}
