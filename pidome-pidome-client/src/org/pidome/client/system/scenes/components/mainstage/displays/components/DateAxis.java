/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 * 
 * 
 */
package org.pidome.client.system.scenes.components.mainstage.displays.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Dimension2D;
import javafx.geometry.Side;
import javafx.util.StringConverter;

import com.sun.javafx.charts.ChartLayoutAnimator;
import com.sun.javafx.css.converters.SizeConverter;
import java.text.SimpleDateFormat;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.scene.chart.ValueAxis;
import javafx.util.Duration;

/**
 * A axis class that plots a range of dates with major tick marks every "tickUnit".
 * 
 * The default is from the oracle class NumberAxis, it is modified so it can use
 * a various of date labels based on the time scale. Where the time is the "Number".
 * 
 * At this moment it uses the following scales:
 * Hour: Major tick 5 minutes, minor tick every minute
 * Day : Major tick every hour, minor tick every 5 minutes.
 * Week: Major tick every day, minor tick every hour
 * Month: Major tick every day, minor tick every 8 hours.
 * Quarter: Major tick Every whole week, minor tick every day of the week.
 * 
 * @todo: create a correct animator, the default one comes from a private function and is commented out at the moment.
 * @todo: Better implementation of the tick formats.
 * 
 * You can use any Legit date format, the range of the minimal and maximum date sets the major tick date format
 */
public final class DateAxis extends ValueAxis<Number> {

    /** These defaults are for calculating the length if a major tick and then applying the format **/
    private static final Double[] TICK_UNIT_DEFAULTS_LENGTH = {300.0, // 5 Minutes (when showing hour)
                                                              3600.0, // 1 hour (when showing day)
                                                             86400.0, // 1 day (when showing week)
                                                             86400.0, // 1 day (when showing month)
                                                            604800.0, // 1 week (everything between month and year)
                                                           2592000.0  // 1 month (when showing year)
                                                               };
    
    /** These are matching decimal formatter strings */
    private static final String[] TICK_UNIT_FORMATTER_DEFAULTS = {"HH:mm", "HH", "E", "dd", "w", "M"};

    private Object currentAnimationID;
    private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);
    private IntegerProperty currentRangeIndexProperty = new SimpleIntegerProperty(this, "currentRangeIndex", -1);
    private DefaultFormatter defaultFormatter = new DefaultFormatter(this);

    // -------------- PUBLIC PROPERTIES --------------------------------------------------------------------------------

    /** When true zero is always included in the visible range. This only has effect if auto-ranging is on. */
    private BooleanProperty forceZeroInRange = new BooleanPropertyBase(true) {
        @Override protected void invalidated() {
            // This will effect layout if we are auto ranging
            if(isAutoRanging()) requestAxisLayout();
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "forceZeroInRange";
        }
    };
    public final boolean isForceZeroInRange() { return forceZeroInRange.getValue(); }
    public final void setForceZeroInRange(boolean value) { forceZeroInRange.setValue(value); }
    public final BooleanProperty forceZeroInRangeProperty() { return forceZeroInRange; }

  /**  The value between each major tick mark in data units. This is automatically set if we are auto-ranging. */
    private DoubleProperty tickUnit = new StyleableDoubleProperty(5) {
        @Override protected void invalidated() {
            if(!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }
        
        @Override
        public CssMetaData<DateAxis,Number> getCssMetaData() {
            return StyleableProperties.TICK_UNIT;
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "tickUnit";
        }
    };
    public final double getTickUnit() { return tickUnit.get(); }
    public final void setTickUnit(double value) { tickUnit.set(value); }
    public final DoubleProperty tickUnitProperty() { return tickUnit; }

    /** The scale factor from data units to visual units */
    private ReadOnlyDoubleWrapper scale = new ReadOnlyDoubleWrapper(this, "scale", 0);
    ReadOnlyDoubleWrapper scalePropertyImpl() { return scale; }
    
    // -------------- CONSTRUCTORS -------------------------------------------------------------------------------------

    /**
     * Create a auto-ranging NumberAxis
     */
    public DateAxis() {}
    
    /**
     * Create a non-auto-ranging NumberAxis with the given upper bound, lower bound and tick unit
     *
     * @param lowerBound The lower bound for this axis, ie min plottable value
     * @param upperBound The upper bound for this axis, ie max plottable value
     * @param tickUnit The tick unit, ie space between tickmarks
     */
    public DateAxis(double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
    }

    /**
     * Create a non-auto-ranging NumberAxis with the given upper bound, lower bound and tick unit
     *
     * @param axisLabel The name to display for this axis
     * @param lowerBound The lower bound for this axis, ie min plottable value
     * @param upperBound The upper bound for this axis, ie max plottable value
     * @param tickUnit The tick unit, ie space between tickmarks
     */
    public DateAxis(String axisLabel, double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
        setLabel(axisLabel);
    }

    // -------------- PROTECTED METHODS --------------------------------------------------------------------------------

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    @Override protected String getTickMarkLabel(Number value) {
        DefaultFormatter formatter = defaultFormatter;
        return formatter.toString(value.longValue()*1000);
    }
    
    /**
     * Called to get the current axis range.
     *
     * @return A range object that can be passed to setRange() and calculateTickValues()
     */
    @Override protected Object getRange() {
        return new double[]{
            getLowerBound(),
            getUpperBound(),
            getTickUnit(),
            getScale(),
            currentRangeIndexProperty.get()
        };
    }

    /**
     * Called to set the current axis range to the given range. If isAnimating() is true then this method should
     * animate the range to the new range.
     *
     * @param range A range object returned from autoRange()
     * @param animate If true animate the change in range
     */
    @Override protected void setRange(Object range, boolean animate) {
        final double[] rangeProps = (double[]) range;
        final double lowerBound = rangeProps[0];
        final double upperBound = rangeProps[1];
        final double tickUnit = rangeProps[2];
        final double scale = rangeProps[3];
        final double rangeIndex = rangeProps[4];
        currentRangeIndexProperty.set((int)rangeIndex);
        final double oldLowerBound = getLowerBound();
        setLowerBound(lowerBound);
        setUpperBound(upperBound);
        setTickUnit(tickUnit);
        if(animate) {
            animator.stop(currentAnimationID);
            currentAnimationID = animator.animate(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(currentLowerBound, oldLowerBound),
                        new KeyValue(scalePropertyImpl(), getScale())
                ),
                new KeyFrame(Duration.millis(700),
                        new KeyValue(currentLowerBound, lowerBound),
                        new KeyValue(scalePropertyImpl(), scale)
                )
            );
        } else {
            currentLowerBound.set(lowerBound);
            setScale(scale);
        }
    }

    /**
     * Calculate a list of all the data values for each tick mark in range
     *
     * @param length The length of the axis in display units
     * @param range A range object returned from autoRange()
     * @return A list of tick marks that fit along the axis if it was the given length
     */
    @Override protected List<Number> calculateTickValues(double length, Object range) {
        final double[] rangeProps = (double[]) range;
        final double lowerBound = rangeProps[0];
        final double upperBound = rangeProps[1];
        double tickUnit = rangeProps[2];
        List<Number> tickValues =  new ArrayList<>();
        if (tickUnit <= 0 || lowerBound == upperBound) {
            tickValues.add(lowerBound);
        } else if (getTickUnit() > 0) {
            for (double major = lowerBound; major <= upperBound; major += tickUnit)  {
                tickValues.add(major);
                if(tickValues.size()>2000) {
                    // This is a ridiculous amount of major tick marks, something has probably gone wrong
                    System.err.println("Warning we tried to create more than 2000 major tick marks on a NumberAxis. " +
                            "Lower Bound=" + lowerBound + ", Upper Bound=" + upperBound + ", Tick Unit=" + tickUnit);
                    break;
                }
            }
        } 
        return tickValues;
    }

    /**
     * Calculate a list of the data values for every minor tick mark
     *
     * @return List of data values where to draw minor tick marks
     */
    @Override
    protected List<Number> calculateMinorTickMarks() {
        final List<Number> minorTickMarks = new ArrayList<>();
        final double lowerBound = getLowerBound();
        final double upperBound = getUpperBound();
        final double tickUnit = getTickUnit();
        final double minorUnit = tickUnit/getMinorTickCount();
        if (getTickUnit() > 0) {
            for (double major = lowerBound; major < upperBound; major += tickUnit)  {
                for (double minor=major+minorUnit; minor < (major+tickUnit); minor += minorUnit) {
                    minorTickMarks.add(minor);
                    if(minorTickMarks.size()>10000) {
                        // This is a ridiculous amount of major tick marks, something has probably gone wrong
                        System.err.println("Warning we tried to create more than 10000 minor tick marks on a NumberAxis. " +
                                "Lower Bound=" + getLowerBound() + ", Upper Bound=" + getUpperBound() + ", Tick Unit=" + tickUnit);
                        break;
                    }
                }
            }
        }
        return minorTickMarks;
    }

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value tick mark value
     * @param range range to use during calculations
     * @return size of tick mark label for given value
     */
    @Override protected Dimension2D measureTickMarkSize(Number value, Object range) {
        final double[] rangeProps = (double[]) range;
        final double rangeIndex = rangeProps[4];
        return measureTickMarkSize(value, getTickLabelRotation(), (int)rangeIndex);
    }

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value     tick mark value
     * @param rotation  The text rotation
     * @param rangeIndex The index of the tick unit range
     * @return size of tick mark label for given value
     */
    private Dimension2D measureTickMarkSize(Number value, double rotation, int rangeIndex) {
        String labelText;
        DefaultFormatter formatter = defaultFormatter;
        if(formatter instanceof DefaultFormatter) {
            labelText = ((DefaultFormatter)formatter).toString(value, rangeIndex);
        } else {
            labelText = formatter.toString(value);
        }
        return measureTickMarkLabelSize(labelText, rotation);
    }

    /**
     * Called to set the upper and lower bound and anything else that needs to be auto-ranged
     *
     * @param minValue The min data value that needs to be plotted on this axis
     * @param maxValue The max data value that needs to be plotted on this axis
     * @param length The length of the axis in display coordinates
     * @param labelSize The approximate average size a label takes along the axis
     * @return The calculated range
     */
    @Override protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {
        final Side side = getSide();
        final boolean vertical = Side.LEFT.equals(side) || Side.RIGHT.equals(side);
        // check if we need to force zero into range
        if (isForceZeroInRange()) {
            if (maxValue < 0) {
                maxValue = 0;
            } else if (minValue > 0) {
                minValue = 0;
            }
        }
        final double range = maxValue-minValue;
        // pad min and max by 2%, checking if the range is zero
        final double paddedRange = (range==0) ? 2 : Math.abs(range)*1.02;
        final double padding = (paddedRange - range) / 2;
        // if min and max are not zero then add padding to them
        double paddedMin = minValue - padding;
        double paddedMax = maxValue + padding;
        // check padding has not pushed min or max over zero line
        if ((paddedMin < 0 && minValue >= 0) || (paddedMin > 0 && minValue <= 0)) {
            // padding pushed min above or below zero so clamp to 0
            paddedMin = 0;
        }
        if ((paddedMax < 0 && maxValue >= 0) || (paddedMax > 0 && maxValue <= 0)) {
            // padding pushed min above or below zero so clamp to 0
            paddedMax = 0;
        }
        // calculate the number of tick-marks we can fit in the given length
        int numOfTickMarks = (int)Math.floor(Math.abs(length)/labelSize);
        // can never have less than 2 tick marks one for each end
        numOfTickMarks = Math.max(numOfTickMarks, 2);
        // calculate tick unit for the number of ticks can have in the given data range
        double tickUnit = paddedRange/(double)numOfTickMarks;
        // search for the best tick unit that fits
        double tickUnitRounded = 0;
        double minRounded = 0;
        double maxRounded = 0;
        int rangeIndex = 0;
            
        double difValue = maxValue - minValue;

        if (difValue < 3660.0) { /// When showing an hour + 1 minute diff max
            rangeIndex = 0;
            setMinorTickVisible(true); /// no minor ticks, too many major ticks
            setMinorTickCount(5); // Minor tick at every minute

        } else if (difValue < 87300.0) { /// when showing a day + 15 minutes diff max
            rangeIndex = 1;
            setMinorTickVisible(true);
            setMinorTickCount(4); // Minor tick at every 15 minutes

        } else if (difValue < 608200.0) { /// When showing a week + 1 hour diff
            rangeIndex = 2;
            setMinorTickVisible(true);
            setMinorTickCount(24); // Minor tick at every hour

        } else if (difValue < 18777600.0) { /// When showing a month + 8 hour diff
            rangeIndex = 3;
            setMinorTickVisible(true);
            setMinorTickCount(8); // Minor tick at every 8 hours

        } else if (difValue < 56332800.0) { /// When showing 3 months + 1 day diff 
            rangeIndex = 2;
            setMinorTickVisible(true);
            setMinorTickCount(7); // minor tick at every day
        }
        tickUnitRounded = TICK_UNIT_DEFAULTS_LENGTH[rangeIndex];

        // move min and max to nearest tick mark
        minRounded = Math.floor(paddedMin / tickUnitRounded) * tickUnitRounded;
        maxRounded = Math.ceil(paddedMax / tickUnitRounded) * tickUnitRounded;
        // calculate the required length to display the chosen tick marks for real, this will handle if there are
        // huge numbers involved etc or special formatting of the tick mark label text
        double maxReqTickGap = 0;
        double last = 0;
        int count = 0;
        for (double major = minRounded; major <= maxRounded; major += tickUnitRounded, count++) {
            double size = (vertical) ? measureTickMarkSize((Double) major, getTickLabelRotation(), rangeIndex).getHeight()
                    : measureTickMarkSize((Double) major, getTickLabelRotation(), rangeIndex).getWidth();
            if (major == minRounded) { // first
                last = size / 2;
            } else {
                maxReqTickGap = Math.max(maxReqTickGap, last + 6 + (size / 2));
            }
        }
        tickUnit = tickUnitRounded;
        // calculate new scale
        final double newScale = calculateNewScale(length, minRounded, maxRounded);
        // return new range
        return new double[]{minRounded, maxRounded, tickUnitRounded, newScale, rangeIndex};
    }

    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------

     /** @treatAsPrivate implementation detail */
    private static class StyleableProperties {
        private static final CssMetaData<DateAxis,Number> TICK_UNIT =
            new CssMetaData<DateAxis,Number>("-fx-tick-unit",
                SizeConverter.getInstance(), 5.0) {

            @Override
            public boolean isSettable(DateAxis n) {
                return n.tickUnit == null || !n.tickUnit.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(DateAxis n) {
                return (StyleableProperty<Number>)n.tickUnitProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
           final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ValueAxis.getClassCssMetaData());
           styleables.add(TICK_UNIT);
           STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    // -------------- INNER CLASSES ------------------------------------------------------------------------------------

    /**
     * Default number formatter for NumberAxis, this stays in sync with auto-ranging and formats values appropriately.
     * You can wrap this formatter to add prefixes or suffixes;
     */
    public static class DefaultFormatter extends SimpleDateFormat {
        private SimpleDateFormat formatter;
        private String prefix = null;
        private String suffix = null;

        /** used internally */
        private DefaultFormatter() {}

        /**
         * Construct a DefaultFormatter for the given NumberAxis
         *
         * @param axis The axis to format tick marks for
         */
        public DefaultFormatter(final DateAxis axis) {
            formatter = getFormatter(axis.isAutoRanging()? axis.currentRangeIndexProperty.get() : -1);
            final ChangeListener axisListener = (ObservableValue observable, Object oldValue, Object newValue) -> {
                formatter = getFormatter(axis.isAutoRanging()? axis.currentRangeIndexProperty.get() : -1);
            };
            axis.currentRangeIndexProperty.addListener(axisListener);
            axis.autoRangingProperty().addListener(axisListener);
        }

        /**
         * Construct a DefaultFormatter for the given NumberAxis with a prefix and/or suffix.
         *
         * @param axis The axis to format tick marks for
         * @param prefix The prefix to append to the start of formatted number, can be null if not needed
         * @param suffix The suffix to append to the end of formatted number, can be null if not needed
         */
        public DefaultFormatter(DateAxis axis, String prefix, String suffix) {
            this(axis);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        private static SimpleDateFormat getFormatter(int rangeIndex) {
            if (rangeIndex < 0) {
                return new SimpleDateFormat();
            } else if(rangeIndex >= TICK_UNIT_FORMATTER_DEFAULTS.length) {
                return new SimpleDateFormat(TICK_UNIT_FORMATTER_DEFAULTS[TICK_UNIT_FORMATTER_DEFAULTS.length-1]);
            } else {
                return new SimpleDateFormat(TICK_UNIT_FORMATTER_DEFAULTS[rangeIndex]);
            }
        }

        /**
        * Converts the object provided into its string form.
        * Format of the returned string is defined by this converter.
        * @return a string representation of the object passed in.
        * @see StringConverter#toString
        */
        public String toString(Number object) {
            return toString(object, formatter);
        }

        private String toString(Number object, int rangeIndex) {
            return toString(object, getFormatter(rangeIndex));
        }

        private String toString(Number object, SimpleDateFormat formatter) {
            if (prefix != null && suffix != null) {
                return prefix + formatter.format(object) + suffix;
            } else if (prefix != null) {
                return prefix + formatter.format(object);
            } else if (suffix != null) {
                return formatter.format(object) + suffix;
            } else {
                return formatter.format(object);
            }
        }

        /**
        * Converts the string provided into a Number defined by the this converter.
        * Format of the string and type of the resulting object is defined by this converter.
        * @return a Number representation of the string passed in.
        * @see StringConverter#toString
        */
        public Number fromString(String string) {
            int prefixLength = (prefix == null)? 0: prefix.length();
            int suffixLength = (suffix == null)? 0: suffix.length();
            return string.length() - (suffixLength+prefixLength);
        }
    }

}