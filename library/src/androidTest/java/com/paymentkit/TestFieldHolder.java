package com.paymentkit;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import com.paymentkit.views.CardNumHolder;
import com.paymentkit.views.FieldHolder;

public class TestFieldHolder extends ActivityInstrumentationTestCase2<FieldHolderTestActivity> {

    private FieldHolderTestActivity testActivity;
    private FieldHolder fieldHolder;
    private CardNumHolder cardNumHolder;

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
    }

    public void testPreconditions() {
        assertNotNull("Test Activity is null", testActivity);
        assertNotNull("FieldHolder is null", fieldHolder);
        assertNotNull("CardNumHolder is null", cardNumHolder);
    }

    @UiThreadTest
    public void testCardNumberEntry() {
        cardNumHolder.getCardField().setText(invalidMC);
        assertFalse("Card number expected to be invalid " + invalidMC, cardNumHolder.isCardNumValid());

        // Note: We must change length of text before all text watching events get handled properly
        cardNumHolder.getCardField().setText("");
        cardNumHolder.getCardField().setText(validMC);
        assertTrue("Card number expected to be valid " + validMC, cardNumHolder.isCardNumValid());
        assertTrue("Card expected to be Master Card", fieldHolder.getCardIcon().isCardType(CardType.MASTERCARD));

        assertTrue("Last 4 digits overlay expected to be shown", cardNumHolder.isOverlayShown());
        String last4 = validMC.substring(12);
        assertTrue("Last 4 digits expected to be "+last4, last4.equals(cardNumHolder.getLastFourDigits()));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
