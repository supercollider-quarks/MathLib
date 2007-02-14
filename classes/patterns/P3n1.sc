P3n1 : FilterPattern {
	embedInStream { arg inval;
		var str, x;
		str = pattern.asStream;
		while { 
			x = str.next(inval); 
			x.notNil 
		} {
			x = x.asInteger;
			inval = x.yield;
			while { x > 1 } {
				x = if(x.even) { x div: 2 } { x * 3 + 1 };
				inval = x.yield;
			};
		};
		^inval
	}
}