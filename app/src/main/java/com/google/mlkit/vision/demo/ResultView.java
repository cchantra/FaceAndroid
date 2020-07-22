package com.google.mlkit.vision.demo;



import java.util.List;
import com.google.mlkit.vision.demo.tflite.Classifier;



public interface ResultView {
    public void setResults(final List<Classifier.Recognition> results);
}

