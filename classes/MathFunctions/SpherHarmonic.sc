SpherHarmonic {
	var <>l, <>m, assocLegendre;
	
	*new { arg l, m;
		^super.new.init(l, m);
	}
	
	init { arg l, m;
		this.l = l ? 0;
		this.m = m ? 0;
		assocLegendre = AssocLegendre.new(this.l, this.m);
	}
	
	setL { arg l;
		this.l = l;
		assocLegendre.setL(l);
	}
	
	getL {
		^l;
	}
	
	setM { arg m;
		this.m = m;
		assocLegendre.setM(m);
	}
	
	getM {
		^m;
	}
	
	setLM { arg l, m;
		this.l = l;
		this.m = m;
		assocLegendre.setLM(l, m);
	}

	getLM {
		^[l, m];
	}
	
	*calc { arg l, m, theta, phi;
		^this.new(l, m).calc(theta, phi)
	}
	
	calc { arg theta, phi; var legendre;
		if(theta.isKindOf(SimpleNumber) and: { phi.isKindOf(SimpleNumber) }, {
			legendre = assocLegendre.calc(cos(theta));
			^Complex.new(legendre * cos(m * phi), legendre * sin(m * phi))
		}, {
			Error("Argument(s) theta and/or phi are/is not of type SimpleNumber").throw
		})
	}
}