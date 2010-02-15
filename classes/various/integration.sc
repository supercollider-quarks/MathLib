

+ Function {
	
	integrate { |a, b, d = 1|
		^(a, a+d .. b).mean(this.value(_))
	}

	
	integrateSimp { |a, b|
		^absdif(a, b) / 6.0 * (this.(a) + this.(b) + (4.0 * this.(a + b / 2)))
	}
	
	integrateSimpA { |a, b, eps = 1e-10, sum|
		var diff;
		var c = (a + b) * 0.5;
		var left = this.integrateSimp(a, c);
		var right = this.integrateSimp(c, b);
		sum = sum ?? { this.integrateSimp(a, b) };
		diff = left + right - sum;
		^if(abs(diff) <= (15.0 * eps)) {
			left + right + (diff / 15.0)
		} {
			eps = eps * 0.5;
			this.integrateSimpAdap(a, c, eps, left)
			+
			this.integrateSimpAdap(c, b, eps, right)
		}
	}
	
}
	