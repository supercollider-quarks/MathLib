/*
This file is part of MathLib, a Quark for SuperCollider:

<https://github.com/supercollider-quarks/MathLib>

You should have received a copy of the GNU General Public License v3 along with
this distribution. If not, see:

<https://www.gnu.org/licenses/gpl-3.0.txt>.
*/


//---------------------------------------------------------------------
//	MathLib: Some mathematical extensions to SuperCollider.
//
// 	Extension: Integer
//
//---------------------------------------------------------------------


/* J Anderson, 2019 */
+ Integer {

	squareOf {
		var sum = 0;
		var res;

		^if(this.isNegative, {
			nil
		}, {
			res = 0;

			while({ sum < this }, {
				sum = Array.series(res + 1, 1, 2).sum;
				res = res + 1;
			});

			if(sum == this, {
				res
			}, {
				nil
			})
		})
	}
}
