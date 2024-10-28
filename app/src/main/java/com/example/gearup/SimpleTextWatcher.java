package com.example.gearup;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleTextWatcher implements TextWatcher {
    private final Runnable onTextChanged;

    public SimpleTextWatcher(Runnable onTextChanged) {
        this.onTextChanged = onTextChanged;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No action needed
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Notify when text has changed
        onTextChanged.run();
    }

    @Override
    public void afterTextChanged(Editable s) {
        // No action needed
    }
}
