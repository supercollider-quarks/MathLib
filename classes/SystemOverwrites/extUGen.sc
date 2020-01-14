+ UGen {
	thresh2 { |function, adverb|
		^this * (this.abs >= function)
	}
}
