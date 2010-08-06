HydrogenWaveFunction { 
	var <>n, <>l, <>m, genLaguerre, spherHarmonic;
		
	*new { arg n, l, m;
		^super.new.init(n, l, m);
	}
	
	init { arg n, l, m;
		this.n = n ? 1;
		this.l = l ? 0;
		this.m = m ? 0;
		
		//Generalized Laguerre polynomial of degree n-l-1 and order 2*l+1
		genLaguerre = GenLaguerre.new(this.n - this.l - 1, 2 * this.l + 1);
		//Spherical harmonicn of degree l and order m
		spherHarmonic = SpherHarmonic.new(this.l, this.m)
	}
	
	setN { arg n;
		this.n = n;
		genLaguerre.setN(this.n - l - 1)
	}
	
	getN {
		^n
	}
	
	setL { arg l;
		this.l = l;
		genLaguerre.setNAlpha(n - this.l - 1, 2 * this.l + 1);
		spherHarmonic.setL(this.l)
	}
	
	getL {
		^l
	}
	
	setM { arg m;
		this.m = m;
		spherHarmonic.setM(this.m)
	}
	
	getM {
		^m
	}
	
	setNLM { arg n, l, m;
		this.n = n;
		this.l = l;
		this.m = m;
		genLaguerre.setNAlpha(this.n - this.l - 1, 2 * this.l + 1);
		spherHarmonic.setLM(this.l, this.m)
	}
	
	getNLM {
		^[n, l, m]
	}
	
	*calc { arg n, l, m, r, theta, phi;
		^this.new(n, l, m).calc(r, theta, phi)
	}

	calc { arg r, theta, phi; var a_0, norm, rho;
		if(r.isKindOf(SimpleNumber) and: { theta.isKindOf(SimpleNumber) } and: 		{ phi.isKindOf(SimpleNumber) }, {
			a_0 = 0.529;					//Approximate bohr radius of hydrogen in picometers
			rho = (2.0 * r) / (n * a_0);
			norm = sqrt(pow(2 / (n * a_0), 3) * (n - l - 1).factorial / 
				(2 * n * pow((n + l).factorial, 3)));
			norm = norm * exp(rho * -1 * 0.5) * pow(rho, l) * genLaguerre.calc(rho);
		
			^(spherHarmonic.calc(theta, phi) * norm);
		}, {
			Error("Argument(s) r, theta and/or phi are not of type SimpleNumber").throw
		})
	}
	
	*calcAbsSquare { arg n, l, m, r, theta, phi;
		^this.new(n, l, m).calcAbsSquare(r, theta, phi)
	}
	
	calcAbsSquare { arg r, theta, phi; var wavefunc;
		wavefunc = this.calc(r, theta, phi);
		^(wavefunc * wavefunc.conjugate).real
	}
}
		
		
		