/*
	You should have received a copy of the GNU General Public License along with the
	SuperCollider3 version of the Ambisonic Toolkit (ATK). If not, see
	<http://www.gnu.org/licenses/>.
*/

//---------------------------------------------------------------------
//	MathLib library
//	https://github.com/supercollider-quarks/MathLib
//
// 	Extension: Integer
//---------------------------------------------------------------------


+ Integer {

	/*
	Perfect Power algorithms adapted from those found in:

	Title: Detecting perfect powers in essentially linear time
	Author: Daniel J. Bernstein
	Journal: Math. Comp. 67 (1998), 1253-1283

	https://www.ams.org/journals/mcom/1998-67-223/S0025-5718-98-00952-1/home.html

	See also:

	Title: Primality Testing - Theory, Complexity, and Applications
	Author: Riley Worthington

	https://www.whitman.edu/documents/Academics/Mathematics/2018/Worthington.pdf


	NOTE:
	It is expected the below algorithms may be further optimized by refactoring
	in terms of Bernstein's 2-adic variant primitives (see: PART VI. PRACTICAL
	IMPROVEMENTS). Doing so requires implementation of b-bit approximate kth
	power of r (see: 6. Approximate powers) and related truncated arithmetic.
	*/

	// perfect power: detection - unknown base, exponent
	isPerfectPower {
		if(this < 4) { ^false };
		^this.isPowerOfTwo or: { this.perfectPower.notNil }
	}

	// perfect power: decomposition - known base
	// TBD

	// (perfect) power: decomposition - known exponent
	perfectSqrt {
		^this.perfectRootOf(2)
	}

	perfectCbrt {
		^this.perfectRootOf(3)
	}

	perfectRootOf { |aNumber|
		var b;
		var a, c;
		var m, p;
		var cp;

		switch( aNumber,
			0, { ^inf },
			1, { ^this },
			{
				if( this == 0 or: { this == 1 }, {
						^this
					}, {
						// (this >= 2) && (aNumber >= 2)
						b = aNumber.asInteger;  // coerce arg
						cp = 2.pow(b);

						if( cp <= this, {
							a = 1;
							c = this;

							while({
								(c - a) >= 2
							}, {
								m = (a + c).div(2);
								p = m.pow(b).min(this + 1).asInteger;

								// found perfect power?
								if( p == this, {
									^m  // found! return base (root)
								}, {
									if( p < this, {
										a = m
									}, {
										c = m
									})
								})
							})
						});

						^nil  // not found! return nil
					}
				)
			}
		)
	}


	// perfect power: decomposition - unknown base, exponent
	// -> minimal exponent
	// maximal exponent, TBD
	perfectPower {
		var b;
		var a, c;
		var m, p;
		var cp;

		// start exponent and base at 2
		b = 2;
		cp = 2.pow(b);

		// outer loop
		while({
			cp <= this
		}, {
			a = 1;
			c = this;

			// inner loop
			while({
				(c - a) >= 2
			}, {
				m = (a + c).div(2);
				p = m.pow(b).min(this + 1).asInteger;

				// found perfect power?
				if( p == this, {
					// found!
					^[ m, b ]  // return: [ base, power (min exponent) ]
				}, {
					if( p < this, {
						a = m
					}, {
						c = m
					})
				})
			});

			// increment exponent
			b = b + 1;
			cp = cp * 2;
		});

		^nil  // not found! return nil
	}

}
