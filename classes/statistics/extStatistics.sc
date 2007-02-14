// some statistics, adc 2005.

+ Collection {
			// same as sum, but sum initialized as float 0.0 to avoid numerical
			// errors for sums of large integer arrays.
	sumF { | function |			
		var sum = 0.0;			
		if (function.isNil) { 		// optimized version if no function
			this.do { | elem | sum = sum + elem; }
		}{
			this.do {|elem, i| sum = sum + function.value(elem, i); }
		};
		^sum
	}
	
	meanF { ^this.sumF / this.size }
	
	geoMean { 
		// 	this.product ** (this.size.reciprocal);	// fails for big arrays, 
		^2 ** this.mean({ |el| el.log2 })			// log2 method is slower but safer.
	}
	
	harmMean { 
		^this.size / this.sumF({ |el| el.reciprocal })
	}
	
	variance { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };
		
		^(this.sumF { |el| (el - mean).abs.squared } / (this.size - 1).max(1))
	}
	
	stdDev { arg mean; ^this.variance(mean).sqrt; }
	
	skew { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };		
	
		^this.sumF({ |el| (el - mean).cubed }) / (this.size * this.stdDev(mean).cubed)
	}
	
		// kurtosis is tails size : kurtosis > 0 is leptokurtic, i.e. large tails; 
		// 0 is normal distribution, < 1 is platykurtic = small tails.
		// not quite sure how to test that this formula is correct. 
	kurtosis { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };		
		
		^(this.sumF({ |el| (el - mean).squared.squared })
		/ ((this.size - 1)  * this.variance.squared)) - 3
	}
		
						// standard normal distribution form:
						// avg = 0, stdDev = 1.
	zTable { arg mean, stdDev;	// supply mean and stdDev if you can
						// compact formula is:
	//	^this - this.meanF / this.stdDev; 

		mean = mean ?? { this.meanF };
		stdDev = stdDev ?? { this.stdDev(mean) };	
		^this - mean / stdDev
	}
	
					// typically assumes this is sorted! 
	atPercent { |index=0.5, interpol=true| 
		if (interpol) 
			{ ^this.blendAt(index * (this.size - 1)) }
			{ ^this.at( index * (this.size - 1).round.asInteger) }
	}

			// expensive on large arrays! 
			// better cache sorted copies and use atPercent.
	percentile { |percent=#[0.25, 0.5, 0.75], interpol=true| 
		^this.copy.sort.atPercent(percent, true)
	}
			// median exists already, median2 interpolates.
	median2 {
		^this.percentile(0.5, true);
	}
	
	trimedian { |interpol=true|
		^this.percentile([0.25, 0.5, 0.5, 0.75], interpol).meanF;
	}

			// utility.
	histo { arg steps=100, min, max; 
		var freqs, freqIndex, lastIndex, range, outliers = 0; 
		
		min = min ?? { this.minItem };
		max = max ?? { this.maxItem };
		
		freqs = Array.fill(steps, 0); 
		lastIndex = steps - 1;
		range = max - min; 
		this.do({ arg el; 
			freqIndex = ((el - min) / range * steps).trunc.asInteger;

			if (freqIndex.inclusivelyBetween(0, lastIndex), { 
				freqs[freqIndex] = freqs[freqIndex] + 1;
			}, { 
						// if max is derived from maxItem, count it in:
				if (el == max) { 
					freqs[steps-1] = freqs[steps-1] + 1;
				} { 		// else it is an outlier.
					outliers =  outliers + 1; 
				//	("out :" + el).postln;
				};
			});
		});
		
		if (outliers > 0, { 
			("histo :" + outliers + "out of (histo) range values in collection.").inform; 
		});

		^freqs;
	}
}


+ SequenceableCollection { 
			// Pearson correlation.
	corr { arg that; 
		var num, denom, thisSum, thatSum; 
		
		if (this.size != that.size, { 
			"No correlation between colls of unequal size.".error; 
			^nil 
		}); 
		 
		thisSum = this.sumF;
		thatSum = that.sumF;
		
		num = this.sumF({ |el, i| el * that[i] }) - (thisSum * thatSum / this.size); 
		
		denom = sqrt( 
			(this.sumF({ |el| el.squared }) - (thisSum.squared / this.size)) 
			* (that.sumF({ |el| el.squared }) - (thatSum.squared / that.size))
		);
		^num / denom
	}
	
		// return n sorted indices and values for a given sort function
	nSorted { |n, func| 
		var sorted, indexedArr;
		func = func ? { arg a, b; a < b }; 
		n = n ? this.size; 
		
		sorted = SortedList(n, { |a, b| func.value(a[1], b[1]) }); 
		
		this.do { |el, i| sorted.add([i, el]); 
			if(sorted.size > n) { sorted.removeAt(n) } 
		};
		^sorted.array
	}

}