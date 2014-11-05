package com.paymentkit;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.EditText;

import com.paymentkit.views.CVVEditText;
import com.paymentkit.views.CardNumEditText;
import com.paymentkit.views.CardNumHolder;
import com.paymentkit.views.ExpirationEditText;
import com.paymentkit.views.FieldHolder;
import com.paymentkit.views.PostCodeEditText;

public class TestFieldHolder extends ActivityInstrumentationTestCase2<FieldHolderTestActivity> {

    private FieldHolderTestActivity testActivity;
    private FieldHolder fieldHolder;
    private CardNumHolder cardNumHolder;
    private CardNumEditText cardNumEdit;
    private ExpirationEditText expEdit;
    private CVVEditText cvvEdit;
    private PostCodeEditText postCodeEdit;

    private static final String validMC   = "5555555555554444";
    private static final String invalidMC = "5555555555554044";

    public TestFieldHolder() {
        super(FieldHolderTestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        testActivity = getActivity();
        fieldHolder = (FieldHolder) testActivity.findViewById(com.paymentkit.test.R.id.field_holder);
        cardNumHolder = fieldHolder.getCardNumHolder();
        cardNumEdit = cardNumHolder.getCardField();
        expEdit = fieldHolder.getExpirationEditText();
        cvvEdit = fieldHolder.getCVVEditText();
        postCodeEdit = fieldHolder.getPostCodeEditText();
    }

    public void testPreconditions() {
        assertNotNull("Test Activity is null", testActivity);
        assertNotNull("FieldHolder is null", fieldHolder);
        assertNotNull("CardNumHolder is null", cardNumHolder);
    }

    @UiThreadTest
    public void testCardNumberEntry() {
        cardNumEdit.setText(invalidMC);
        assertFalse("Card number expected to be invalid " + invalidMC, cardNumHolder.isCardNumValid());

        // Note: We must change length of text before all text watching events get handled properly
        cardNumEdit.setText("");
        cardNumEdit.setText(validMC);
        assertTrue("Card number expected to be valid " + validMC, cardNumHolder.isCardNumValid());
        assertTrue("Card expected to be Master Card", fieldHolder.getCardIcon().isCardType(CardType.MASTERCARD));

        assertTrue("Last 4 digits overlay expected to be shown", cardNumHolder.isOverlayShown());
        String last4 = validMC.substring(12);
        assertTrue("Last 4 digits expected to be "+last4, last4.equals(cardNumHolder.getLastFourDigits()));
    }

    @UiThreadTest
    public void testOnValidationEvent() {
        final String validExpDate = "9/17";
        final String validCVV = "123";
        final String validPostCode = "90210";

        ValidationListener listener = new ValidationListener();
        fieldHolder.setOnValidationEventListener(listener);
        fieldHolder.setRequirePostCode(true);

        long lastEventTime = listener.getEventTime();
        // Card Number
        // Note: We must change length of text before all text watching events get handled properly
        cardNumEdit.setText("");
        assertEquals("Expected no validation event when clearing card number text", listener.getEventTime(), lastEventTime);
        cardNumEdit.setText(validMC);
        assertEquals("Expected no validation event when setting card number text", listener.getEventTime(), lastEventTime);
        // Expiration
        expEdit.setText(validExpDate);
        assertEquals("Expected no validation event when setting expiration text", listener.getEventTime(), lastEventTime);
        // CVV
        cvvEdit.setText(validCVV);
        assertEquals("Expected no validation event when setting CVV text", listener.getEventTime(), lastEventTime);
        // Postal Code
        postCodeEdit.setText(validPostCode);
        assertFalse("Expected a validation event when setting post code text", listener.getEventTime() == lastEventTime);
        int lastEventCount = listener.getCount();
        assertTrue("Expected 1 validation event when finished setting all fields", lastEventCount == 1);
        assertTrue("Expected card data to be valid", listener.isValid());

        // Invalidate each individual field in turn and verify that 1 validation event is fired and
        // it's that the data has become invalid
        invalidateField(cardNumEdit, listener, "card number");
        cardNumEdit.setText(validMC); // reset, make data set valid again
        invalidateField(expEdit, listener, "exp date");
        expEdit.setText(validExpDate); // reset, make data set valid again
        invalidateField(cvvEdit, listener, "cvv");
        cvvEdit.setText(validCVV); // reset, make data set valid again
        invalidateField(postCodeEdit, listener, "post code");
        postCodeEdit.setText(validPostCode);

        // Invalidate 2 fields, then reset each value to valid testing that the 1 invalidation event
        // then the 1 validation event happen properly -- and run it for every pair of fields
        EditText[] fields = {cardNumEdit, expEdit, cvvEdit, postCodeEdit};
        String[] validValues = {validMC, validExpDate, validCVV, validPostCode};
        combinatorialInvalidate2Fields(fields, validValues, 0, listener);
    }

    /**
     * Invalidate the data in the given EditText field and assert() that the appropriate events
     * were fired on the ValidationListener.
     *
     * Assumptions:
     * The card data set up is valid when this method is called.
     */
    private void invalidateField(EditText field, ValidationListener listener, String fieldDesc) {
        // Edit field value, make data invalid
        long lastEventTime = listener.getEventTime();
        int lastEventCount = listener.getCount();
        field.setText(field.getText().subSequence(0, field.length() - 2));
        assertTrue("Expected validation event [time] when invalidating "+fieldDesc, listener.getEventTime() != lastEventTime);
        assertEquals("Expected validation event [count] when invalidating "+fieldDesc, listener.getCount(), lastEventCount + 1);
        assertFalse("Expected validation event [invalid] when invalidating "+fieldDesc, listener.isValid());
    }

    /**
     * Run invalidate2Fields on all permutations of the given fields (taken 2 at a time).
     * Note: If there are N fields then this will run N!/(N-2)! tests. Beware.
     *
     * @param fields All the fields that should be tested with invalidate2Fields()
     * @param validValues Valid values for each field in {@code fields} _in order_
     * @param startIndex The index into {@code fields} to start choosing fields to test
     * @param listener The ValidationListener to use during the test
     */
    private void combinatorialInvalidate2Fields(EditText[] fields, String[] validValues, int startIndex, ValidationListener listener) {
        int end = fields.length;
        for (int i = startIndex+1; i < end; i++) {
            invalidate2Fields(fields[startIndex], fields[i], validValues[startIndex], validValues[i], listener);
            invalidate2Fields(fields[i], fields[startIndex], validValues[i], validValues[startIndex], listener);
        }
        if (startIndex < (fields.length-2))
            combinatorialInvalidate2Fields(fields, validValues, startIndex + 1, listener);
    }

    /**
     * Test that, starting from a valid data set, that 1) invalidating {@code fieldA} invalidates the
     * data; 2) then invalidating {@code fieldB} does NOT fire invalidation events; 3) resetting
     * {@code fieldA} to a valid value does NOT fire invalidation events; 4) resetting {@code fieldB}
     * to a valid value DOES fire 1 validation event.
     */
    private void invalidate2Fields(EditText fieldA, EditText fieldB, String validValueA, String validValueB, ValidationListener listener) {
        assertTrue(listener.isValid());
        invalidateField(fieldA, listener, fieldA.toString()); // will fire 1 invalidation event
        long lastEventTime = listener.getEventTime();
        int lastEventCount = listener.getCount();
        // Note: Can't call invalidateField() a second time because we expect that no events
        // should be fired. So we invalidate the field manually.
        fieldB.setText(fieldB.getText().subSequence(0, fieldB.length()-2));
        fieldB.setText(validValueB);
        assertEquals("Expected no validation event [time] when fixing 1st of 2 invalid fields "+fieldB,
                listener.getEventTime(), lastEventTime);
        assertEquals("Expected no validation event [count] when fixing 1st of 2 invalid fields "+fieldB,
                listener.getCount(), lastEventCount);
        assertFalse("Expected no validation event [invalid] when fixing 1st of 2 invalid fields "+fieldB,
                listener.isValid());
        fieldA.setText(validValueA);
        assertTrue("Expected validation event [time] when fixing 2nd of 2 invalid fields "+fieldA,
                listener.getEventTime() != lastEventTime);
        assertEquals("Expected validation event [count] when fixing 2nd of 2 invalid fields "+fieldA,
                listener.getCount(), lastEventCount+1);
        assertTrue("Expected validation event [invalid] when fixing 2nd of 2 invalid fields "+fieldA,
                listener.isValid());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private class ValidationListener implements FieldHolder.OnValidationEventListener {
        boolean valid = false;
        long eventTime = 0;
        int count = 0;
        @Override
        public void onValidationEvent(boolean isValid) {
            synchronized (this) {
                eventTime = System.nanoTime();
                valid = isValid;
                count++;
            }
        }
        public synchronized boolean isValid() { return valid; }
        public synchronized long getEventTime() { return eventTime; }
        public synchronized int getCount() { return count; }
    }
}
