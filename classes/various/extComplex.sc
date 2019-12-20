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
// 	Extension: Complex
//
//---------------------------------------------------------------------


/* J Anderson, 2019 */
+ Complex {
	sqrt {
		^this.pow(0.5)
	}
}
