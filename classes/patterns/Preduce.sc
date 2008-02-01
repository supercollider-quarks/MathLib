Preduce : Pattern {
	var <>selectorPattern, <>list;
	*new { arg selectorPattern ... patterns;
		^super.new.list_(patterns).selectorPattern_(selectorPattern)
	}
	embedInStream { arg inval;
		var selstr, selector, streams, outval, values;
		selstr = selectorPattern.asStream;
		streams = list.collect(_.asStream);
		while {
			selector = selstr.next(inval);
			selector.notNil
		} {
			values = streams.collect { |x| 
				var z = x.value(inval); 
				if(z.isNil) {
					^inval
				};
				z
			};
			outval = values.reduce(selector);
			inval = outval.yield;
		};
		^inval
	}	
}