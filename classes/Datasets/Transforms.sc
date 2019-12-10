/*****************************************
Data Transforms
(C) 2018 Jonathan Reus

Tools for analyzing and transforming datasets: scaling, normalization, standardization, PCA, etc..


******************************************/

// TO NORMALIZE:
// find min / max of features
// normalized_value = (val - min) / (max - min)


/*
T for Transform
Normalization:
1. find min/max of each feature
2. normalized_value = (val - min) / (max - min)
*/
TNormalizer {
	var <min, <max, <originalData, <normalizedData, dim;

	/*
	@param dataset a Matrix of rows as feature vectors
	*/
	*new {|dataset|
		^super.new.init(dataset);
	}

	init {|dataset|
		if(dataset.isKindOf(SequenceableCollection).not ) { "Dataset must be a Matrix".error;this.halt; };
		if(dataset.at(0).isKindOf(SequenceableCollection)) {
			dim = dataset.cols;
			min = Array.newClear(dim);
			max = Array.newClear(dim);
			normalizedData = Matrix.newClear(dataset.rows, dataset.cols);
			dim.do {|i|
				var col = dataset.getCol(i);
				min[i] = col.minItem;
				max[i] = col.maxItem;
				normalizedData.putCol(i, this.normalizeSample(col) );
			};
		} {
			min = dataset.minItem; max = dataset.maxItem;
			normalizedData = dataset.collect {|it,i| this.normalizeSample(it) };
		};
		originalData = dataset;
	}

	// normalize / denormalize a single sample
	normalizeSample {|samp| (samp - min) / (max - min) }
	denormalizeSample {|samp| (samp * (max-min)) + min }


	// normalize an entire dataset
	normalizeData {}

	// denormalize a point-slope form 2-dimensional line
	// of the form [slope, intercept]
	denormalizeLine {|line|
			var p1,p2, new_m, new_b, m=line[0], b=line[1];
			// calculate two normalized samples & denormalize them
			p1 = [-1,(-1 * m)+b]; p2 = [1,(1 * m)+b];
			p1 = this.denormalizeSample(p1);
			p2 = this.denormalizeSample(p2);
			// calculate denormalized decision boundary
			new_m = (p2[1]-p1[1]) / (p2[0]-p1[0]); // slope
			new_b = p1[1] - (new_m*p1[0]); // y-intercept
			^[new_m,new_b];
	}

}

/*
TO STANDARDIZE:
1. calculate mean and standard deviation of each feature
2. subtract mean from each feature
3. divide features by standard deviation

T is for Transform
*/
TStandardizer {
	var <mean, <stddev;

}

/*
T for Transform
*/
TPCA {

}



/*
( // feature scaling of reduced dataset
~dReduced = Matrix.newFrom(~dReduced);
dim = ~dReduced.cols;
mean = Array.newClear(dim);
stdev = Array.newClear(dim);

~standardizeSample = {|v,mean,stddev| (v-mean) / stddev };
~destandardizeSample = {|v,mean,stddev| (v*stddev) + mean };

~dStandard = Matrix.newClear(~dReduced.rows, dim);
dim.do {|i|
	var col = ~dReduced.getCol(i);
	mean[i] = col.mean;
	stdev[i] = col.stdDev(mean[i]);
	~dStandard.putCol(i, ~standardizeSample.(col, mean[i], stdev[i]));
};
"MEAN: %   STDDEV: %".format(mean, stdev).postln;
s1 = [-0.09, 1.45];
s2 = ~standardizeSample.(s1, mean, stdev);
s3 = ~destandardizeSample.(s2, mean, stdev);
"NEW SAMPLE: %   STANDARDIZED: %  DESTANDARDIZED: %".format(s1,s2,s3).postln;

); // END SCALING & STANDARDIZATION OF FEATURES

*/
