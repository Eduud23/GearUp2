package com.example.gearup;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class CardHelper {

    // Identifies the card type based on the card number
    public static String identifyCardType(String cardNum) {
        String cleanCardNum = cardNum.replaceAll("\\s", ""); // Remove spaces for validation

        if (cleanCardNum.startsWith("4")) {
            return "Visa";
        } else if (cleanCardNum.matches("^5[1-5].*") || cleanCardNum.matches("^222[1-9].*") ||
                cleanCardNum.matches("^22[3-9].*") || cleanCardNum.matches("^2[3-6].*") ||
                cleanCardNum.matches("^27[01].*") || cleanCardNum.matches("^2720.*")) {
            return "MasterCard";
        } else if (cleanCardNum.startsWith("34") || cleanCardNum.startsWith("37")) {
            return "American Express";
        } else if (cleanCardNum.startsWith("6011") || cleanCardNum.matches("^622(12[6-9]|1[3-9]\\d|[2-8]\\d\\d|9[01]\\d|92[0-5]).*") ||
                cleanCardNum.matches("^64[4-9].*") || cleanCardNum.startsWith("65")) {
            return "Discover";
        } else {
            return "Unknown";
        }
    }

    // Formats the card number with spaces every 4 digits
    public static void setupCardNumberFormatting(final EditText cardNumber) {
        cardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int beforeLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String input = s.toString().replaceAll("\\s", ""); // Remove existing spaces
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" "); // Insert space after every 4 digits
                    }
                    formatted.append(input.charAt(i));
                }

                cardNumber.removeTextChangedListener(this);
                cardNumber.setText(formatted.toString());
                cardNumber.setSelection(formatted.length()); // Move cursor to the end
                cardNumber.addTextChangedListener(this);

                isFormatting = false;
            }
        });
    }

    // Formats the expiry date in MM/YY format
    public static void setupExpiryDateFormatting(final EditText expiryDate) {
        expiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("[^\\d]", ""); // Remove non-numeric characters
                StringBuilder formatted = new StringBuilder();

                if (input.length() > 2) {
                    formatted.append(input.substring(0, 2)).append("/");
                    if (input.length() > 4) {
                        formatted.append(input.substring(2, 4)); // MM/YY
                    } else {
                        formatted.append(input.substring(2));
                    }
                } else {
                    formatted.append(input);
                }

                expiryDate.removeTextChangedListener(this);
                expiryDate.setText(formatted.toString());
                expiryDate.setSelection(formatted.length());
                expiryDate.addTextChangedListener(this);

                isFormatting = false;
            }
        });
    }

    // Formats the CVV based on the card type (3 digits for regular cards, 4 digits for Amex)
    public static void setupCvvFormatting(final EditText cvv, final EditText cardNumber) {
        cvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cardType = identifyCardType(cardNumber.getText().toString());
                int maxLength = cardType.equals("American Express") ? 4 : 3;

                if (s.length() > maxLength) {
                    cvv.setText(s.subSequence(0, maxLength));
                    cvv.setSelection(maxLength);
                }
            }
        });
    }
}
