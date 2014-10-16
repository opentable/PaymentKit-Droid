package com.paymentkit;

import junit.framework.TestCase;

import static com.paymentkit.CardType.AMERICAN_EXPRESS;
import static com.paymentkit.CardType.DINERS_CLUB;
import static com.paymentkit.CardType.DISCOVER;
import static com.paymentkit.CardType.JCB;
import static com.paymentkit.CardType.MASTERCARD;
import static com.paymentkit.CardType.NOT_ENOUGH_DIGITS;
import static com.paymentkit.CardType.TOO_MANY_DIGITS;
import static com.paymentkit.CardType.UNKNOWN_CARD;
import static com.paymentkit.CardType.VISA;
import static com.paymentkit.ValidateCreditCard.getCardType;
import static com.paymentkit.ValidateCreditCard.isValid;
import static com.paymentkit.ValidateCreditCard.matchCardType;

public class TestValidateCreditCard extends TestCase {

    private static long validVisa[] = { 4111111111111111L, 4012888888881881L, };
    private static long validMC[] = { 5588888888888838L, 5555555555554444L, };
    private static long validAmex[] = { 378888888888858L, 378282246310005L, };
    private static long validDiscover[] = { 6011222233334444L, 6011000990139424L, };
    private static long validDiners[] = { 30569309025904L, 38520000023237L, };
    private static long validJCB[] = { 3530111333300000L, 3566002020360505L, };

    private static long invalidNumbers[] = { 6010222233334444L, 4000000000009L, 4999999999999L, };
    private static long tooShort[] = { 4123456789012L, 37000000000002L, 601100000000012L, };
    private static long tooLong[] = { 51327698001231327L, 380000000000006L, 35660020203605005L  };

    public void testGetCardType() {
        assertTrue("Expected UNKNOWN_CARD due to too few digits", getCardType(0) == UNKNOWN_CARD);
        assertTrue("Expected Discover Card", getCardType(6011222233334444L) == DISCOVER);
        assertTrue("Expect unknown card type", getCardType(6010222233334444L) == UNKNOWN_CARD);
        assertTrue("Expected Visa Card", getCardType(validVisa[0]) == VISA);
        assertTrue("Expected Master Card", getCardType(validMC[0]) == MASTERCARD);
        assertTrue("Expected American Express Card", getCardType(validAmex[0]) == AMERICAN_EXPRESS);
        assertTrue("Expected Discover Card", getCardType(validDiscover[0]) == DISCOVER);
        assertTrue("Expected Diners Club Card", getCardType(validDiners[0]) == DINERS_CLUB);
        assertTrue("Expected JCB Card", getCardType(validJCB[0]) == JCB);
    }

    public void testIsValid() {
        for (long num : validVisa)
            assertTrue("Expected valid number "+num, isValid(num));
        for (long num : validMC)
            assertTrue("Expected valid number "+num, isValid(num));
        for (long num : validAmex)
            assertTrue("Expected valid number "+num, isValid(num));
        for (long num : validDiscover)
            assertTrue("Expected valid number "+num, isValid(num));
        for (long num : validDiners)
            assertTrue("Expected valid number "+num, isValid(num));
        for (long num : validJCB)
            assertTrue("Expected valid number "+num, isValid(num));
        for (long num : invalidNumbers)
            assertFalse("Expected invalid number "+num, isValid(num));
    }

    public void testMatchCardType() {
        // Note: testGetCardType already checks everything but number of digits
        assertTrue("Expected Visa Card", matchCardType(validVisa[1]) == VISA);
        assertTrue("Expected Master Card", matchCardType(validMC[1]) == MASTERCARD);
        assertTrue("Expected American Express Card", matchCardType(validAmex[1]) == AMERICAN_EXPRESS);
        assertTrue("Expected Discover Card", matchCardType(validDiscover[1]) == DISCOVER);
        assertTrue("Expected Diners Club Card", matchCardType(validDiners[1]) == DINERS_CLUB);
        assertTrue("Expected JCB Card", matchCardType(validJCB[1]) == JCB);

        for (long num : tooShort)
            assertTrue("Expected 'not enough digits' card number "+num, matchCardType(num) == NOT_ENOUGH_DIGITS);
        for (long num : tooLong)
            assertTrue("Expected 'too many digits' card number "+num, matchCardType(num) == TOO_MANY_DIGITS);
    }
}
