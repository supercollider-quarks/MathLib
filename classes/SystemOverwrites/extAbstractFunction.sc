+ AbstractFunction {

	// TODO: migrate thresh2 methods to SC common lib
	thresh2 { |function|
		^this * (this.abs >= function).asInteger
	}
}
