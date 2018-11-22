/*********************************************************************
(C) 2005 Till Bovermann / Bielefeld University

and later...

(C) 2018 Jonathan Reus / IEM Graz

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

For more details see: https://www.gnu.org/licenses/
**********************************************************************/


/*
@class ScatterView

A simple 2-dimensional scatterplot with coordinate axes.

@usage

(
n = 100;
x = Array.series(n, 0, 100 / n) + Array.fill(n, {rrand(-15.0, 15.0)});
y = Array.series(n, 0, 100 / n) + Array.fill(n, {rrand(-15.0, 15.0)});
p = x.collect {|item,i| [item, y[i]] };

w = Window.new("Linear Scatter", 800@600);
j = ScatterView.new(w, 800@600, p, [0,100].asSpec,[0,100].asSpec);
j.drawAxes_(true).drawGrid_(true).drawMethod_(\fillOval);
j.symbolSize_(5@5).symbolColor_(Color.blue);
w.front;
);

// Show axis, grid and value labels
j.drawAxes_(true).drawGrid_(true).drawValues_(true).refresh;

// Visual style
j.axisColor_(Color.gray(0.3)).gridColor_(Color.red).valuesColor_(Color.gray(0.7)).backgroundColor_(Color.black).refresh;


// Adjust axes & scale
j.setAxes([-50, 150, \lin].asSpec, [-50,150,\lin].asSpec).symbolSize_(3@3).drawValues_(false).refresh;
j.setAxes([1, 150, \exp].asSpec, [0, 100,\lin].asSpec).symbolSize_(5@5).drawGridValues_(true).refresh;


*/
ScatterView : View {
	var <plot, <>backgroundColor;
	var <>highlightColor, <highlightItem, <>highlightSize, <>isHighlight;
	var <>drawAxes, <>drawGrid, <>drawGridValues, <>drawValues;
	var <>gridResolution;
	var <>axisColor,<>gridColor, <>valuesColor;
	var <>xAxisName, <>yAxisName;
	var <symbolSize, <>symbolColor, <>drawMethod = \lineTo;

	var <normalizedData; // data normalized to 0-1 for projection on to the view
	var <originalData, <specX, <specY;

	*new {|parent, bounds, data, specX, specY|
		^super.new(parent, bounds).initPlot(data, specX, specY);
	}

	*drawMethods { ^[\fillRect, \fillOval, \strokeOval, \strokeRect] }

	setAxes {|xspec,yspec|
		specX = xspec ? specX;
		specY = yspec ? specY;
		this.normalizeData;
	}

	highlightItem_{|item|
		highlightItem = item.isKindOf(Collection).if({item}, {[item]});
		//this.plot.refresh;
	}

	highlightItemRel_{|val|
		this.highlightItem = (val * (normalizedData.size-1)).asInteger;
	}

	highlightRange{|start, end|
		this.highlightItem = (start .. end);
	}

	highlightRangeRel{|start, end|
		var startIndex, endIndex;
		startIndex = (start * (normalizedData.size-1)).asInteger;
		endIndex = (end * (normalizedData.size-1)).asInteger;
		this.highlightItem = (startIndex .. endIndex);
	}

	refresh {
		plot.refresh;
	}

	data_{|data|
		originalData = data.shallowCopy;
		this.normalizeData;
		highlightItem = min(highlightItem, (normalizedData.size-1));
	}

	normalizeData {
		normalizedData = originalData.collect {|item|
			[specX.unmap(item[0]), specY.unmap(item[1])];
		};
	}

	symbolSize_{|point|
		if(point.isKindOf(Point), {symbolSize = point}, {symbolSize = (point@point)});
	}
	resize{
		^plot.resize;
	}
	resize_{arg resize;
		plot.resize_(resize)
	}
	initPlot {arg data, argSpecX, argSpecY;

		var widthSpec, heightSpec, cross;
		var possibleMethods;

		specX = argSpecX ? [0,1].asSpec;
		specY = argSpecY ? specX.copy;
		possibleMethods = ScatterView.drawMethods;

		this.symbolSize = 1;
		symbolColor = symbolColor ? Color.black;
		axisColor = axisColor ? Color.black;
		valuesColor = valuesColor ? Color.black;
		gridColor = gridColor ? Color.gray(0.7);

		backgroundColor = Color.white;
		highlightColor = Color.red;
		highlightItem = 0;
		highlightSize = (2@2);
		isHighlight = false;
		drawAxes = false;
		drawGrid = false;
		drawGridValues = true;
		drawValues = false;
		xAxisName= "X";
		yAxisName= "Y";
		gridResolution = 5@5;

		this.data_(data);

		cross = {|rect, width, color|
			var dx, dy, extX, extY;
			dx = rect.left;
			dy = rect.top;
			extX = rect.width;
			extY = rect.height;
			Pen.use{
				Pen.color = color;
				Pen.translate(dx, dy);
				Pen.moveTo((0)@(extY*0.5));
				Pen.lineTo(extX@(extY*0.5));
				Pen.moveTo((extX*0.5)@(0));
				Pen.lineTo((extX*0.5)@(extY));
				Pen.stroke;
			};
		};

		plot = UserView.new(this, this.bounds).drawFunc_({|w|
			var width, height, left, top, rect, pad = 10;
			if (drawAxes) { pad = 60 };

			width =  w.bounds.width  - pad;
			height = w.bounds.height - pad;
			left = w.bounds.left + (pad/2);
			top = w.bounds.top + (pad/2);


			Pen.use{
				// clipping into the boundingbox
				Pen.moveTo((w.bounds.left)@(w.bounds.top));
				Pen.lineTo(((w.bounds.left)@(w.bounds.top))
					+ (w.bounds.width@0));
				Pen.lineTo(((w.bounds.left)@(w.bounds.top))
					+ (w.bounds.width@w.bounds.height));
				Pen.lineTo(((w.bounds.left)@(w.bounds.top))
					+ (0@w.bounds.height));
				Pen.lineTo((w.bounds.left)@(w.bounds.top));
				Pen.clip;

				// draw Background
				Pen.color = backgroundColor;
				Pen.addRect(w.bounds);
				Pen.fill;

				// draw data
				Pen.color = symbolColor;
				if (possibleMethods.includes(drawMethod), {
					Pen.beginPath;
					normalizedData.do{|item, i|
						rect = Rect(
							(   item[0]  * width)  - (symbolSize.x/2) + (pad/2),
							((1-item[1]) * height) - (symbolSize.y/2) + (pad/2),
							symbolSize.x, symbolSize.y
						);
						Pen.perform(
							drawMethod,
							(Rect(w.bounds.left, w.bounds.top, 0, 0) + rect)
						);
					}
				},{ // else draw lines
					Pen.width = symbolSize.x;
					// move to first position;
					if (normalizedData.notNil) {
						Pen.moveTo(
							((normalizedData[0][0]  * width) + (pad/2))
							@
							(((1-normalizedData[0][1]) * height) + (pad/2))
							+
							(w.bounds.left@w.bounds.top)
						);
					};
					// draw lines
					normalizedData.do{|item, i|
						Pen.lineTo(
							((item[0]  * width) + (pad/2))
							@
							(((1-item[1]) * height) + (pad/2))
							+
							(w.bounds.left@w.bounds.top)
						)
					};
					if(drawMethod == \fill, {Pen.fill},{Pen.stroke});
				});

				// highlight datapoint
				if(isHighlight) {
					highlightItem.do{|item|
						cross.value((Rect(
							(normalizedData[item][0]  * width)
							-
							(highlightSize.x/2) + (pad/2),
							((1-normalizedData[item][1]) * height)
							-
							(highlightSize.y/2)
							+
							(pad/2),
							highlightSize.x, highlightSize.y)
						+
						Rect(w.bounds.left, w.bounds.top, 0, 0)),
						symbolSize,
						highlightColor
						);
					}; // od
				};

				// draw grid
				if (drawGrid) {
					var xticks = List.new, yticks = List.new;
					var xlabels = List.new, ylabels = List.new;


					gridResolution.x.do {|i|
						var val, pos;
						val = specX.minval + (((specX.maxval - specX.minval) / gridResolution.x) * (i+1));
						pos = (specX.unmap(val) * width) + (pad/2);
						xticks.add(pos);
						xlabels.add(val.round(0.01).asString);
					};

					gridResolution.y.do {|i|
						var val, pos;
						val = specY.minval + (((specY.maxval - specY.minval) / gridResolution.y) * (i+1));
						pos = ((1.0-specY.unmap(val)) * height) + (pad/2);
						yticks.add(pos);
						ylabels.add(val.round(0.01).asString);
					};

					Pen.color = gridColor;
					xticks.do {|pos| Pen.line(pos@top, pos@(top+height)) };
					yticks.do {|pos| Pen.line(left@pos, (left+width)@pos) };
					Pen.stroke;

					// Draw Labels
					if(drawGridValues) {
						xticks.do {|pos, i|
							if(i != (gridResolution.x-1)) {
								xlabels[i].drawAtPoint((pos-8)@(top+height+2));
							};
						};
						yticks.do {|pos, i|
							if(i != (gridResolution.y-1)) {
								Pen.rotate(-pi/2);
								ylabels[i].drawAtPoint((pos.neg - 8)@(left-17));
								Pen.rotate(pi/2);
							};
						};

					};
				};

				// draw axis
				if (drawAxes) {
					Pen.color = axisColor;
					Pen.moveTo((w.bounds.left+(pad/2))@(w.bounds.top+(pad/2)));
					Pen.lineTo((w.bounds.left+(pad/2))@(w.bounds.top+w.bounds.height-(pad/2)));
					Pen.lineTo(
						(w.bounds.left-(pad/2)+w.bounds.width)@(w.bounds.top+w.bounds.height-(pad/2)));
					specX.minval.round(0.001).asString
					.drawAtPoint(
						(w.bounds.left+(pad/2)+5)@
						(w.bounds.height-(pad/2)+10));
					xAxisName
					.drawAtPoint(
						(w.bounds.left+(w.bounds.width/2))@
						(w.bounds.height-(pad/2)+10));
					specX.maxval.round(0.001).asString
					.drawAtPoint(
						(w.bounds.left+10+w.bounds.width-20-(pad/2))@
						(w.bounds.height-(pad/2)+10));



					Pen.rotate(-pi/2);
					Pen.translate(w.bounds.height.neg, 0);
					specY.minval.round(0.001).asString
					.drawAtPoint((pad/2)@(w.bounds.left+(pad/2) -20));
					yAxisName.
					drawAtPoint((w.bounds.height/2)@(w.bounds.left+(pad/2) -20));
					specY.maxval.round(0.001).asString
					.drawAtPoint((w.bounds.height - (pad/2))@(w.bounds.left+(pad/2) -20));
					Pen.translate(w.bounds.height, 0);
					Pen.rotate(pi/2);
					Pen.stroke;
				};
				// draw values
				if (drawValues) {
					Pen.color = valuesColor;
					normalizedData.do{|item, i|
						data.at(i).round(0.001).asString.drawAtPoint(
							((item[0]  * width) + (pad/2))
							@
							(((1-item[1]) * height) + (pad/2) + 10)
						)
					}
				}
			}; // end Pen.use
		});
	}
	canFocus_ { arg state = false;
		plot.canFocus_(state);
	}
	canFocus {
		^plot.canFocus;
	}
	visible_ { arg bool;
		plot.visible_(bool)
	}
	visible {
		^plot.visible
	}
}

/*
@class ScatterView3d

A simple 3-dimensional scatterplot with rotation.


@usage

// Generate some toy data with a linear relationship
n = 100;
x = Array.series(n, 0.0, 100 / n) + Array.fill(n, {rrand(-10.0, 10.0)}).round(0.01);
y = Array.series(n, 1.0, 100 / n) + Array.fill(n, {rrand(-10.0, 10.0)}).round(0.01);
z = Array.series(n, 1.0, 100 / n) + Array.fill(n, {rrand(-10.0, 10.0)}).round(0.01);
p = x.collect {|item,i| [item, y[i], z[i]] }; // as points

// Plot
b = Rect(0,0, 800, 600);
w = Window.new(bounds: b);
j = ScatterView3d.new(w, b, p, [0,100].asSpec, [0,100].asSpec, [0,100].asSpec);
j.symbolColor_(Color.black).drawMethod_(\fillRect).symbolSize_(5@5);
w.front;

Tdef('animate', {
var i = 0;
inf.do {
j.rot(i, i, 0).refresh;
i = i + 0.03;
0.05.wait;
};
}).play(AppClock);

*/
ScatterView3d : View {
	var scatterView;
	var rotX, rotY, rotZ;
	var specX, specY, specZ;
	var data3d;

	*new {|parent, bounds, data, specX, specY, specZ, rotX = 0, rotY = 0, rotZ = 0|
		^super.new.init3d(parent, bounds, data, specX, specY, specZ, rotX, rotY, rotZ);
	}

	init3d {|parent, bounds, data, argspecX, argspecY, argspecZ, argrotX, argrotY, argrotZ|
		specX = argspecX ? [0, 1].asSpec;
		specY = argspecY ? specX.copy;
		specZ = argspecZ ? specX.copy;

		rotX = argrotX;
		rotY = argrotY;
		rotZ = argrotZ;

		// [rotX, rotY, rotZ].postln;


		// [-2.sqrt, 2.sqrt].asSpec approx. [-1.42, 1.42].asSpec
		scatterView = ScatterView(parent, bounds, [[1, 1, 1]], [-1.42, 1.42].asSpec);
		this.data = data;
		this.refresh
	}

	data_{|data|
		data3d = data.collect {|item|
			Matrix.withFlatArray(3, 1, [
				specX.unmap(item[0]),
				specY.unmap(item[1]),
				specZ.unmap(item[2])
			] * 2 - 1);
		};
		scatterView.data = this.pr_project;

		//		highlightItem = min(highlightItem, (normalizedData.size-1));
	}

	refresh {
		scatterView.refresh;
	}

	rotX_{|val|
		this.rot(val, rotY, rotZ);
	}

	rotY_{|val|
		this.rot(rotX, val, rotZ);
	}

	rotZ_{|val|
		this.rot(rotX, rotY, val);
	}

	/*
	@method
	Rotation in radians
	*/
	rot {|rX, rY, rZ|
		rotX = rX;
		rotY = rY;
		rotZ = rZ;
		scatterView.data = this.pr_project;
	}

	drawMethod_{|method|
		scatterView.drawMethod = method;
	}

	symbolSize_{|val|
		scatterView.symbolSize = val;
	}

	symbolColor_{|val|
		scatterView.symbolColor = val;
	}

	isHighlight_{|val|
		scatterView.isHighlight_(val);
	}

	highlightItem_{|item|
		scatterView.highlightItem_(item);
	}

	highlightRange{|start, end|
		scatterView.highlightRange(start, end);
	}

	highlightColor_{|color|
		scatterView.highlightColor_(color);
	}

	highlightSize_{|size|
		scatterView.highlightSize_(size);
	}

	backgroundColor_{|val|
		scatterView.backgroundColor_(val);
	}

	resize{
		^scatterView.resize
	}

	resize_{|resize|
		scatterView.resize_(resize)
	}


	pr_project {
		var cx, cy, sx, sy, sz, cz, projectionMatrix;
		sx = sin(rotX);
		sy = sin(rotY);
		sz = sin(rotZ);

		cx = cos(rotX);
		cy = cos(rotY);
		cz = cos(rotZ);

		projectionMatrix = Matrix.with([
			[( (cy * cz) - (sx * sy * sz)), (sz.neg * cx), ((cz * sy) + (sz * sx * cy))],
			[( (cy * sz) + (cz * sx * sy)), ( cz * cx),    ((sz * sy) - (sx * cy * cz))]
		]);


		^data3d.collect{|row|
			(projectionMatrix * row).getCol(0)
		};
	}
}

/*
@class ScatterPlotter

A higher level view abstraction that manages multiple scatterplots on a single graph.
All scatterplots share a single axis specification and commands for drawing / showing axes.

@usage

n = 100;
x = Array.series(n, 0, 100 / n) + Array.fill(n, {rrand(-15.0, 15.0)});
y = Array.series(n, 0, 100 / n) + Array.fill(n, {rrand(-15.0, 15.0)});
p = x.collect {|item,i| [item, y[i]] };
x = Array.series(n, 0, 100 / n) + Array.fill(n, {rrand(-15.0, 15.0)});
y = Array.series(n, 0, 100 / n).reverse + Array.fill(n, {rrand(-15.0, 15.0)});
q = x.collect {|item,i| [item, y[i]] };

w = Window.new("X Scatter", 800@600);
j = ScatterPlotter.new(w, 800@600, p, [0,100].asSpec, [0,100].asSpec);
j.drawAxes_(true).drawGrid_(true).drawMethod_(\fillOval).symbolSize_(5@5);
w.front;

j.addPlot(q);
j.addPlot([[0,0],[100,100]]);
j.backgroundColor_(Color.black).axisColor_(Color.gray);
j.symbolColor_(Color.blue,0);
j.symbolColor_(Color.green,1);
j.symbolColor_(Color.red,2).drawMethod_(\lineTo,2).symbolSize_(1,2);
j.setAxes([1,100,\exp].asSpec, [1,100,\exp].asSpec);

*/
ScatterPlotter : CompositeView {
	var <scatterviews; // list of plots, plot 0 is where axes and grids are drawn.

	*new {|parent, bounds, data, specX, specY|
		^super.new(parent, bounds).init(data, specX, specY);
	}

	init {|data, specX, specY|
		var firstplot;
		scatterviews = List.new;
		firstplot = ScatterView.new(this, this.bounds, data, specX, specY);
		scatterviews.add(firstplot);
		^this
	}

	drawAxes_ {|bool| scatterviews.do {|plot| plot.drawAxes_(bool).refresh }; ^this; }
	drawAxes { ^scatterviews[0].drawAxes; }

	drawGrid_ {|bool| scatterviews[0].drawGrid_(bool).refresh; ^this; }
	drawGrid { ^scatterviews[0].drawGrid; }

	gridResolution_ {|dim| scatterviews[0].gridResolution_(dim).refresh; ^this; }
	gridResolution { ^scatterviews[0].gridResolution; }

	axisColor_ {|color| scatterviews[0].axisColor_(color).refresh; ^this; }
	axisColor { ^scatterviews[0].axisColor; }


	gridColor_ {|color| scatterviews[0].gridColor_(color).refresh; ^this; }
	gridColor { ^scatterviews[0].gridColor; }

	xAxisName_ {|name| scatterviews[0].xAxisName_(name).refresh; ^this; }
	xAxisName { ^scatterviews[0].xAxisName; }

	yAxisName_ {|name| scatterviews[0].yAxisName_(name).refresh; ^this; }
	yAxisName { ^scatterviews[0].yAxisName; }


	drawGridValues_ {|bool| scatterviews[0].drawGridValues_(bool).refresh; ^this; }
	drawGridValues { ^scatterviews[0].drawGridValues; }

	backgroundColor_ {|bgcolor| scatterviews[0].backgroundColor_(bgcolor).refresh; ^this; }
	backgroundColor { ^scatterviews[0].backgroundColor; }

	getPlot {|idx=0| ^scatterviews.at(idx); }
	deletePlot {|idx| if(idx != 0) { scatterviews.remove(idx); } }

	addPlot {|data|
		var newplot;
		newplot = ScatterView.new(this, this.bounds, data, scatterviews[0].specX, scatterviews[0].specY);
		newplot.backgroundColor_(Color.clear).drawMethod_(scatterviews[0].drawMethod);
		newplot.symbolColor_(Color.rand(0.0, 0.95)).symbolSize_(scatterviews[0].symbolSize);
		newplot.drawAxes_(scatterviews[0].drawAxes).axisColor_(Color.clear);
		scatterviews.add(newplot);
		this.refresh;
		^newplot;
	}

	setAxes {|xspec, yspec|
		scatterviews.do {|plot, idx|
			plot.setAxes(xspec, yspec);
			plot.refresh;
		};
	}

	drawValues_ {|bool, idx=0| scatterviews[idx].drawValues_(bool).refresh; ^this; }
	valuesColor_ {|color, idx=0| scatterviews[idx].valuesColor_(color).refresh; ^this; }
	symbolColor_ {|color, idx=0| scatterviews[idx].symbolColor_(color).refresh; ^this; }
	symbolSize_ {|dim, idx=0| scatterviews[idx].symbolSize_(dim).refresh; ^this; }
	drawMethod_ {|method, idx=0| scatterviews[idx].drawMethod_(method).refresh; ^this; }
}




