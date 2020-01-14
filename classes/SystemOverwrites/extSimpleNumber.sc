+ SimpleNumber {

	// TODO: migrate thresh2 methods to SC common lib
	thresh2 { |thresh, adverb|
		^this * (this.abs >= thresh).asInteger
	}
}
