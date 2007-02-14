LaTeX {
	
	*lineEnd { ^"  \\\\  \n" }
	*hline { ^"  \\hline  " }
	*cline { arg which; ^which.collect { |x|Ê"\\cline{" ++ x ++ "-" ++ x ++ "}" }.join(" ") }
	*tabletab { ^"\t&\t" }
	
	*math { arg str, flag=true;
		^if(flag) {Ê"$" ++ str ++ "$" }Ê{ str };
	}
	
	*row { arg obj, align="l", n=1;
		if(n > 1) {
			^ "\\multicolumn{" ++ n ++ "}{" ++ align ++ "}{" ++ obj ++ "}";
		};
		^obj.asString
	}
	
	*asTable { arg dict, func;
		var res;
		if(dict.isKindOf(Dictionary)) {
			dict.keysValuesDo { |key, val|
				res = res.add([key, val]);
			}
		} {
			^dict
		};
		^res
	}
	
	*tabularFooter { ^"\\end{tabular}\n" }
	*tableHeader { arg n, align="l", separator=" ";
		^ "\\begin{tabular}{" ++ (align).dup(n).join(separator) ++ "}" ++ this.lineEnd;
	}
	*tableDict { arg dict,  hlines=#[], align="l", separator=" ", math=true;
		var str, headline;
		dict = this.asTable(dict);
		str = this.tableHeader(dict.shape.at(1), align, separator);
		dict.do {|x, i|
			if(hlines.includes(i)) {Êstr = str ++ this.hline };
			if(math) {Êx = x.collect(this.math(_)) };
			str = str ++ x.collect(this.row(_), align).join(this.tabletab);
			str = str ++ this.lineEnd;
		};
		str = str ++ this.tabularFooter;
		^str
	}
	
	*numericalDict { arg dict, keyName, valName, math=true;
		var str;
		str = "\\begin{tabular}{c r @{.} l}";
		if(keyName.notNil or: {ÊvalName.notNil }) {
			str = str + this.math(keyName, math) + "&" 
					++ this.row(this.math(valName, math), "c", 2) ++ this.lineEnd;
			str = str ++ this.hline 
		} {
			str = str ++ this.lineEnd;
		};
		dict.pairsDo {|key, val|
			var valStr = if(val.isNumber) {
				 	val.asString.split($.).join($&);
				 } {
					this.row(this.math(val, math), "l",  2);
			};
			str = str ++ this.row(this.math(key, math), "l", 1) ++ this.tabletab ++ valStr;
			str = str ++ this.lineEnd;
			
		};
		str = str ++ this.tabularFooter;
		^str
	}
}

/*
cline

\begin{tabular}{l l l l }  \\  $0$	&$1$	&$2$	&$3$  \\    \hline  $4$	&$5$ 	&$6$	&$7$  \\  
\cline{1-1} \cline{3-4}$8$	&$9$	&$10$	&$11$  \\    \hline  $12$	&$13$	&$14$	&$15$  \\  \end{tabular}
*/

