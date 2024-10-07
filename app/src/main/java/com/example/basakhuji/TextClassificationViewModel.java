package com.example.basakhuji;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SuppressLint("StaticFieldLeak")
public class TextClassificationViewModel extends ViewModel {
    private int _currentDelegate = DELEGATE_CPU;
    private String _currentModel = MOBILEBERT;

    private BertNLClassifier bertClassifier;

    private ScheduledExecutorService executor;

    public TextClassificationViewModel(Context context) {
        initClassifier(context);
    }

    public List<Float> classify(String text) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        List<Float> results = new ArrayList<>();
        try {
            results = executor.submit(new Callable<List<Float>>() {
                @Override
                public List<Float> call() throws Exception {
                    List<Float> classificationResults = new ArrayList<>();

                    for (Category category : bertClassifier.classify(text)) {
                        classificationResults.add(category.getScore());
                    }
                    return classificationResults;
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        return results;
    }

    private void initClassifier(Context context) {
        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder();
        switch (_currentDelegate) {
            case DELEGATE_CPU:
                break;
            case DELEGATE_NNAPI:
                baseOptionsBuilder.useNnapi();
                break;
        }
        BaseOptions baseOptions = baseOptionsBuilder.build();

        if (_currentModel.equals(MOBILEBERT)) {
            try {
                BertNLClassifier.BertNLClassifierOptions options = BertNLClassifier.BertNLClassifierOptions.builder()
                        .setBaseOptions(baseOptions)
                        .build();
                bertClassifier = BertNLClassifier.createFromFileAndOptions(context, MOBILEBERT, options);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_NNAPI = 1;
    public static final String MOBILEBERT = "mobilebert.tflite";
}
