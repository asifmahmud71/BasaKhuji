package com.example.basakhuji;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier;
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TextClassificationViewModel extends ViewModel {
    private static final int DELEGATE_CPU = 0;
    private static final int DELEGATE_NNAPI = 1;
    private static final String WORD_VEC = "wordvec.tflite";
    private static final String MOBILEBERT = "mobilebert.tflite";

    private Context context;
    private int currentDelegate = DELEGATE_CPU;
    private String currentModel = MOBILEBERT;
    private NLClassifier nlClassifier;
    private BertNLClassifier bertClassifier;
    private Executor executor;

    public TextClassificationViewModel(Context context) {
        this.context = context;
        initClassifier();
        executor = Executors.newSingleThreadExecutor();
    }

    public List<Float> classify(String text) {
        List<Float> results = new ArrayList<>();
        try {
            switch (currentModel) {
                case MOBILEBERT:
                    List<Category> categoriesMobileBERT = bertClassifier.classify(text);
                    for (Category category : categoriesMobileBERT) {
                        results.add(category.getScore());
                    }
                    break;
                case WORD_VEC:
                    List<Category> categoriesWordVec = nlClassifier.classify(text);
                    for (Category category : categoriesWordVec) {
                        results.add(category.getScore());
                    }
                    break;
                default:
                    // Handle unknown models
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }



    private void initClassifier() {
        BaseOptions baseOptions = BaseOptions.builder().build();
        switch (currentDelegate) {
            case DELEGATE_CPU:
                // Default
                break;
            case DELEGATE_NNAPI:
                // Fallback to CPU
                currentDelegate = DELEGATE_CPU;
                Toast.makeText(context, "NNAPI is not supported on this device. Falling back to CPU.", Toast.LENGTH_SHORT).show();
                break;
        }
        try {
            if (currentModel.equals(MOBILEBERT)) {
                BertNLClassifier.BertNLClassifierOptions options = BertNLClassifier.BertNLClassifierOptions.builder()
                        .setBaseOptions(baseOptions)
                        .build();
                bertClassifier = BertNLClassifier.createFromFileAndOptions(context, MOBILEBERT, options);
            } else if (currentModel.equals(WORD_VEC)) {
                NLClassifier.NLClassifierOptions options = NLClassifier.NLClassifierOptions.builder()
                        .setBaseOptions(baseOptions)
                        .build();
                nlClassifier = NLClassifier.createFromFileAndOptions(context, WORD_VEC, options);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the IOException as per your application's requirements
        }
    }




}
