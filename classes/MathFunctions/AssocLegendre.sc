/* 
 * Original C++ code from: Numerical Recipes, 3rd edition, Cambridge Press 2007. Translation to 
 * SuperCollider by Michael Dzjaparidze, may 2010.
 */
AssocLegendre {
	var <>l, <>m;

	*new { arg l, m;
		^super.new.init(l, m);
	}
	
	init { arg l, m;
		if(l.isKindOf(Integer) or: { l == nil }, {
			this.l = l ? 0
		}, {
			Error("Argument l is not of type Integer").throw
		});
		if(m.isKindOf(Integer) or: { m == nil }, {
			this.m = m ? 0;
			if(this.m < 0 or: { this.m > this.l }, {
				this.m = nil;
				Error("Bad argument for m! Either m is negative or bigger than l.").throw
			})
		}, {
			Error("Argument m is not of type Integer").throw
		})
	}
	
	setL { arg l;
		if(l.isKindOf(Integer), {
			this.l = l
		}, {
			Error("Argument l is not of type Integer").throw
		})
	}
	
	getL { 
		^l;
	}
	
	setM { arg m;
		if(m.isKindOf(Integer) and: { m >= 0 } and: { m < l }, {
			this.m = m
		}, {
			Error("Argument m is either not of type Integer, is negative or is bigger than" ++ 				" l").throw
		})
	}
	
	getM { 
		^m;
	}
	
	setLM { arg l, m;
		if(l.isKindOf(Integer), {
			this.l = l
		}, {
			Error("Argument l is not of type Integer").throw
		});
		if(m.isKindOf(Integer) and: { m >= 0 } and: { m < this.l }, {
			this.m = m
		}, {
			Error("Argument m is either not of type Integer, is negative or is bigger than" ++ 				" l").throw
		})
	}
	
	getLM {
		^[l, m];
	}
	
	*calc { arg l, m, x;
		^this.new(l, m).calc(x)
	}
	
	calc { arg x; var ll, fact, oldFact, pll, pmm, pmmp1, omx2;
		if(x.isKindOf(SimpleNumber), {
			pmm = 1.0;
			if(m > 0, {
				omx2 = (1.0-x) * (1.0+x);
				fact = 1.0;
				m.do({ arg i; i = i + 1;
					pmm = pmm * ((omx2*fact) / (fact+1.0));
					fact = fact + 2.0
				})
			});
			pmm = sqrt(((2*m+1) * pmm) / 4pi);
			if((m & 1).booleanValue, {
				pmm = pmm.neg
			});
			if(l == m, {
				^pmm
			}, {
				pmmp1 = x * sqrt(2.0*m+3.0) * pmm;
				if(l == (m+1), {
					^pmmp1
				}, {
					oldFact = sqrt(2.0*m+3.0);
					ll = m + 2;
					while({ ll <= l }, {
						fact = sqrt((4.0*ll*ll-1.0) / ((ll*ll) - (m*m)));
						pll = ((x*pmmp1) - (pmm/oldFact)) * fact;
						oldFact = fact;
						pmm = pmmp1;
						pmmp1 = pll;
						ll = ll + 1
					});
					^pll
				})
			})
		}, {
			Error("Argument x is not of type SimpleNumber").throw
		})
	}
}