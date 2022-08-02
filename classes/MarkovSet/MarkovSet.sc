	/*
__________________________________________________________________________________________
	n-th Order Markov Set: a set of a finite number of elements in which each element
	is associated with a number of possible transition probabilities to another
	element of the set.

	MarkovSet is a Dictionary that contains keys pointing to WeighBags
	(that contain objects and their probabilities).
	By parsing in a stream the Set 'learns' what element can possibly follow another.
	for reference see: http://www.taygeta.com/rwalks/rwalks.html
______________________________________________________________________________________	version 3.5, Julian Rohrhuber, 12/2004, 3/2007
version 3.5.1, contribution Yann Ics, Summer 2022
*/


MarkovSet  {

	var <seeds, <>updateSeeds, <dict, remanence;


	*new { arg args, updateSeeds=false, remanence=3;
		^super.new.init(remanence).updateSeeds_(updateSeeds).putAll(args);
	}

	*fill { arg length, stream;
		   ^this.new.parse(stream, length);
	}



	////////////////////////initializing

	init { arg argRemanence;
		remanence = argRemanence;
		dict = Dictionary.new;
		this.makeSeeds;
	}

	remanence { ^remanence }
	nodeClass { ^WeighBag }
	order { ^1 }
	size { ^dict.size }

	dmc { ^seeds.select{|seed| seed.asArray.size == 1}.collect{|key| [key.asArray.first, dict.at(key).counts.sum]} }
	// dmc returns [[seed1, countSeed1], [seed2, countSeed2] ... [seedN, countSeedN]]

	makeSeeds {
			seeds = List.newFrom(dict.keys);
	}

	seeds_ { arg collection;
		seeds = collection.asArray.reject({ arg item; dict.at(item).isNil })
	}

	addExits { arg terminators;
		if( terminators.notNil, {
			terminators.asArray.do({ arg item; this.read(item, nil) });
		})

	}




	//////////////////////////parsing


	read { arg prevkey, nextkey;
			var bag;

			bag = dict.at(prevkey);
			if(bag.isNil, {
				bag = this.nodeClass.new;
				dict.put(prevkey, bag);
				if(updateSeeds, { seeds.add(prevkey) });
			});
			bag.add(nextkey);
	}

	put { arg prevkey, nextkeys, weights, precision=0.01;
			var bag;
			bag = this.nodeClass.with(nextkeys, weights, precision);
			dict.put(prevkey, bag);
	}

	putAll { arg args;
		args.do({ arg item;
			var key, next, weights;
			#key, next, weights = item;
			this.put(key, next, weights)
		});
	}

	next { arg prevKey;
		var bag;
		if(prevKey.isNil, {
			"chose a seed at random".postln;
			^seeds.choose });
		^this.get(prevKey)
	}

	choose {
		^this.next(nil)
	}

	get { arg key;
		var bag;
		bag = dict.at(key);
		if(bag.isNil, { ^nil }, { ^bag.wchoose });
	}

	remove { arg key, nextKey;
		var bag;
		bag = dict.at(key);
		if(bag.isNil, { ^nil }, { ^bag.remove(nextKey) });
	}

	parse { arg pattern, length=inf;
			var val, stream;
			stream = Pspy(this, pattern, length).asStream;
				loop {
					val = stream.next;
				if(val.isNil) { ^this }
				};
	}

	embedSpyInStream { arg pattern, repeats=inf, inval;
			var stream, prev, val;
				stream = pattern.asStream;
					val = prev = stream.next(inval);
					inval = val.yield;
				val = stream.next(inval);
					inval = val.yield;
					repeats.do {
						if((prev.notNil) and: {val.notNil})
					{
						this.read(prev, val)
					};
						prev = val;

						val = stream.next(inval);
						inval = val.yield;
					}
					^inval

	}

	asStream { ^Routine.new { arg inval; this.embedInStream(inval) } }

	// possible: embed item in stream each time.
	// refactor.
	embedInStream { arg inval, repeats=inf;
				var item, lastItem;
				repeats.do {
						item = this.choose;
						while {
							lastItem = item;
							item = this.next(lastItem);
							lastItem.embedInStream(inval);
							item.notNil;
						}
				};
				^inval

	}

	parseFile { arg path, delimiter=$ , blockSize=0, completionFunc;
		var source, stream, isRichText, doc, string;
		isRichText = path.endsWith(".rtf")
		or: { path.endsWith(".html") } or: {path.endsWith(".htm") };
		protect {
			if(isRichText){
				/*
				source = Document.open(path);
				stream = CollStream.new;
				if(source.isNil) { Error("wrong path" ++ path).throw };
				stream << source.text;
				stream.pos = 0;
				this.parseIO(stream, delimiter, blockSize, completionFunc, inf);
				*/
				"rich text parsing is currently not supported anymore.".error;
			} {
				source = File(path, "r");
				this.parseIO(source, delimiter, blockSize, completionFunc, inf);
			};
			source.close;
		};
	}

	parseIO { arg iostream, delimiter=$ , blockSize=0, completionFunc, maxLength=32;
				var pat;

				pat = if(blockSize == 0) {
						Prout.new {
							var item;
							while {
								item = iostream.readUpTo(delimiter);
								item.notNil and: { item != "" }
							} {
								(item ++ delimiter).asSymbol.yield;
							}
						}
					} {
						Prout.new {
								var item;
								while {
									item = iostream.nextN(blockSize);
									item.notNil and: { item != "" }
								} {
									item.asSymbol.yield;
								}
							}
						};
				this.parse(pat, maxLength);
				completionFunc.value;
	}


	////////////////modification

	normalize { dict.do({ arg node; node.weigh }) }
	equalize { dict.do({ arg node; node.equalize }) }



	manipulate { arg func;
				dict.do({ arg node, i;
					node.manipulate(func, i)
				})
	}



	storeOn { arg stream;
			stream << this.class.name << "[ " ;
			stream << Char.nl;
			this.storeItemsOn(stream);
			stream << " ]" ;
	}

	storeItemsOn { arg stream, itemsPerLine = 1;
		^dict.storeItemsOn(stream, itemsPerLine);
	}

}



//this is faster in many cases, but less flexible.
//only objects that can act as keys in an IdentityDictionary can be read


IdentityMarkovSet : MarkovSet {

	init {
			dict = IdentityDictionary.new;
			this.makeSeeds;
	}

	nodeClass { ^IdentityWeighBag }

}





LookupMarkovSet : IdentityMarkovSet {
	var <lookUpDict;



	//needed because IdentityDictionary cannot be used with Arrays as keys
	//this is mainly a measure for speed.

	init {
		lookUpDict = IdentityDictionary.new;
		super.init;
	}

	register { arg obj;
		//^aSymbol. returns the key to the lookUpDict needed for objects
		//that can't be used as keys in a IdentityDictionary, such as Arrays.
		//anything that implements .asSymbol to return a key can thus be used as element

		var key;
		key = obj.asSymbol;
		if(lookUpDict.at(key).isNil,
		 { lookUpDict.put(key, obj) });
		^key
	}


	registerAsNext { arg obj;
		^this.register(obj)
	}


	read { arg prevObj, nextObj;
			var bag, prevKey, nextKey;

			// generate the key to look for. might be generated from an array of objects
			prevKey = this.register(prevObj);
			nextKey = this.registerAsNext(nextObj);

			bag = dict.at(prevKey);

			if(bag.isNil, {
				bag = this.nodeClass.new;
				dict.put(prevKey, bag);
				if(updateSeeds, { seeds.add(prevKey) });
			});
			nextKey.asCollection.do({ arg item; bag.add(item);	});

	}

	put { arg prevObj, nextObj, weights;
			var bag, key, nextKey;
			key = this.register(prevObj);
			nextKey = this.registerAsNext(nextObj);
			nextKey.asCompileString.debug;
			bag = this.nodeClass.with(nextKey, weights);
			if(updateSeeds, { seeds.add(key) });
			dict.put(key, bag);
	}



	next { arg prevObj;
		var bag, nextKey;
		nextKey = this.nextKey(prevObj.asSymbol);
		^lookUpDict.at(nextKey)
	}


	get { arg prevObj;
		var bag, nextKey;
		nextKey = this.getKey(prevObj.asSymbol);
		^lookUpDict.at(nextKey)

	}

	choose {
		^lookUpDict.at(seeds.choose)
	}


	nextKey { arg prevKey;
		^this.getKey(prevKey) ?? {
			"chose a seed at random".postln;
			seeds.choose;
		};

	}

	getKey { arg prevKey;
		var bag;
		bag = dict.at(prevKey);
		if(bag.isNil, { ^nil }, { ^bag.wchoose });
	}



}

//nth order markov set

MarkovSetN : LookupMarkovSet {

	var order;

	*new { arg args, order=1, updateSeeds=false;
		^super.new.init(order).updateSeeds_(updateSeeds).putAll(args);

	}

	*fill { arg n, stream, order=1;
		   ^this.new(nil, order).parse(stream, n);
	}

	init { arg argOrder;
			order = argOrder;
			super.init;
	}

	order { ^order }

	registerAsNext { arg obj;
		^if(obj.isSequenceableCollection)
		{ obj.collect({ arg item; this.register(item) }).debug }
		{ this.register(obj) }
	}

	embedSpyInStream { arg pattern, repeats=inf, inval;
		var stream, item, buffer;

 		stream = pattern.asStream;

		// fill up the buffer incrementally on the first few incoming values
		buffer = Array.fill(order, {
			item = stream.next(inval);
			if(item.isNil) { ^inval };
			inval = item.yield;
			item
		});

		// now we have enough info to begin populating the set
		repeats.value.do {
			item = stream.next(inval);
			if(item.notNil) {
				this.read(buffer, item);
				(buffer = buffer.copy).removeAt(0);
				buffer.add(item);
			} {
				^inval
			};
			inval = item.yield;
 		};

	}

	embedInStream { arg inval, repeats=inf;

				var item,  buffer, key;
				repeats.value.do {
						key = seeds.choose; //get a	 seed name
						"chose a seed at random".postln;
						buffer = lookUpDict.at(key).asArray.copy; //find the array
						buffer.do { arg item; inval = item.yield };

						while {
							item = this.get(key);
							item.notNil
						} {
							buffer.removeAt(0);
							buffer = buffer.add(item);
							key = buffer.asSymbol;

						item.yield;
						};
				};
				nil.yield;
				^inval

	 }
}

//--------------------------------------------------
// Dynamic Markoc Chain
//  Allows n-order Markov chain avoiding 100%
//  if so returns NIL or a random key according to updateSeeds
//  This supposes a MarkovSet understood as a no constant order set
//  and satisfying with 1-order, 2-order, ... n-order
//  with n as some kind of remanence or short term memory.
//  /!\ currently works only if each element of the Markov chain is an Array
//--------------------------------------------------
+ MarkovSet	{
	nextProb { arg prevKey, prob;
		var pk, bag;
		pk = if(prevKey.isNil.not) { prevKey.reverse.copyFromStart(this.remanence-1).reverse };
		bag = this.dict.at(pk);
		case
		{ bag.isNil && (pk.size < 2) } { if (this.updateSeeds && prob.asBoolean.not) { ^this.next.choose } { ^nil} }
		{ bag.isNil && (pk.size > 1) } { ^this.nextProb(pk[1..], prob) }
		{ bag.weights.size == 1 } { ^this.nextProb(pk[1..], prob) }
		{ true }
		{
			if (prob.asBoolean)
			{ ^[bag.items, bag.counts] }
			{ ^bag.wchoose }
		}
	}

	nextIncludes { arg prevKey, includes;
		var ins, tmp;
		var prob = this.nextProb(prevKey, true);
		if (prob.isNil.not)
		{
			// [TODO] when includes.size > 1
			// (it.asArray.asSet & includes.asArray.asSet)
			// should respect their respective order as indices
			// and be optimised ...
			// e.g. [0, 7, 0, 5] should match only with [0, 7], [0, 0], [0, 5], [7, 0], [7, 5], [0, 7, 0], [0, 7, 5], [0, 0, 5], [7, 0, 5] or [0, 7, 0, 5] as includes
			ins = prob[0].selectIndices{|it| (it.asArray.asSet & includes.asArray.asSet) == includes.asArray.asSet};
			if (ins.isEmpty && this.updateSeeds)
			{
				ins = prob[0].selectIndices{|it| (it.asArray.asSet & includes.asArray.asSet).isEmpty.not};
				if (ins.size > 0) { "partial match".postln }
			}
		};
		if (ins.size == 0)
		{
			if (this.updateSeeds)
			{
				tmp = this.dmc.select{|it| (it[0].asArray.asSet & includes.asArray.asSet) == includes.asArray.asSet};
				if (tmp.isEmpty)
				{
					tmp = this.dmc.select{|it| (it[0].asArray.asSet & includes.asArray.asSet).isEmpty.not};
					if (tmp.isEmpty)
					{ ^this.nextProb }
					{ ^tmp.flopChoose }
				}
				{ ^tmp.flopChoose }
			}
			{ ^nil }
		}
		{
			^ins.collect{|ind| [prob[0][ind], prob[1][ind]]}.flopChoose;
		}
	}

	parseSeq { arg data, completionFunc;
		// data is a list of sequence(s)
		// e.g. Array.with(seq1.asArray, seq2.asArray...seqN.asArray)
		data.do{|seq|
			seq.windloop(this.remanence).do{|len|
				len.do{|it| this.read(it[..it.size-2], it.last)}}};
		this.makeSeeds;
		completionFunc.value;
	}
}

// get weighted array for method nextProb in MarkovSet
+ ArrayedCollection {
	windloop { arg order;
		var ord;
		// order is an integer strictly superior to zero
		if (order > this.size) { order = this.size };
		^(2..order+1).collect
		{
			|k|
			(this.size-k+1).collect
			{
				|i|
				this[i..i+k-1]
			}
		}
	}

	flopChoose {
		// this = [[item1, countItem1], [item2, countItem2] ... [itemN, countItemN]]
		var tmp = this.flop;
		^tmp[0].wchoose(tmp[1].normalizeSum)
	}
}
//--------------------------------------------------
// ref: https://yannics.github.io/
//  - Journal of Generative Sonic Art
//  - Neuromuse3
//--------------------------------------------------


