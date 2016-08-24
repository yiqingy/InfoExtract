package com.sf.mlp.ie.ds;

/**
 * Created by Joms on 4/19/2016.
 */
public class Range {
    private int start;
    private int end;
    private double confidence;

    public Range(int start, int end, double confidence) {
        this.start = start;
        this.end = end;
        this.confidence = confidence;
    }

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
        this.confidence = 0.0;
    }


    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public boolean isConfident(double threshold) {
        return this.confidence>threshold;
    }

    public boolean isValid(String str) {
        return start>=0 && start!=str.length() && end<=str.length() && end>=start;
    }

    public int getLength() {
        return end - start;
    }

    public boolean overlap(Range r) {
        return r.getStart()>=start && r.getStart()<end;
    }

    public boolean contains(Range r) {
        return (r.getStart()>=start && r.getEnd()<=end);
    }

    public void addOffset(int offset) {
        this.start += offset;
        this.end += offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        return start == range.start && end == range.end;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    @Override
    public String toString() {
        return start + " " + end;
    }
}
