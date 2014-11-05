package com.paymentkit.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.paymentkit.CardType;
import com.paymentkit.R;
import com.paymentkit.ValidateCreditCard;
import com.paymentkit.util.ViewUtils;
import com.paymentkit.views.CardIcon.CardFace;

/**
 * 
 * @author Brendan Weinstein
 *
 */
public class FieldHolder extends RelativeLayout {

	public static final int AMEX_CARD_LENGTH = 17;
	public static final int NON_AMEX_CARD_LENGTH = 19;
    public static final int DINERS_CARD_LENGTH = 16;

	private static final int RE_ENTRY_ALPHA_OUT_DURATION = 100;
	private static final int RE_ENTRY_ALPHA_IN_DURATION = 500;
	private static final int RE_ENTRY_OVERSHOOT_DURATION = 500;
	
	private CardNumHolder mCardHolder;
	private ExpirationEditText mExpirationEditText;
	private CVVEditText mCVVEditText;
    private PostCodeEditText mPostCodeEditText;
    private CardIcon mCardIcon;
	private LinearLayout mExtraFields;

    private OnValidationEventListener mCardValidListener;
    private boolean requirePostCode;
    private boolean lastIsValid = false;

    public FieldHolder(Context context) {
		super(context);
		setup();
	}

	public FieldHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public CVVEditText getCVVEditText() {
		return mCVVEditText;
	}

	public CardIcon getCardIcon() {
		return mCardIcon;
	}

	public ExpirationEditText getExpirationEditText() {
		return mExpirationEditText;
	}

	public CardNumHolder getCardNumHolder() {
		return mCardHolder;
	}

    public PostCodeEditText getPostCodeEditText() {
        return mPostCodeEditText;
    }

    public String getCVV() {
        return mCVVEditText.getText().toString();
    }

    public String getExprMonth() {
        return mExpirationEditText.getMonth();
    }

    public String getExprYear() {
        return mExpirationEditText.getYear();
    }

    public String getExprYearAbv() {
        return mExpirationEditText.getYearAbv();
    }

    public void setRequirePostCode(boolean yesNo) {
        requirePostCode = yesNo;
        mPostCodeEditText.setVisibility(yesNo ? VISIBLE : GONE);
        findViewById(R.id.post_code_spacer).setVisibility(yesNo ? VISIBLE : GONE);
        int cvvNext = yesNo ? EditorInfo.IME_ACTION_NEXT : EditorInfo.IME_ACTION_DONE;
        mCVVEditText.setImeOptions(cvvNext | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    }

    public String getPostCode() {
        return mPostCodeEditText.getText().toString();
    }

    /** Returns a string with only numeric characters. **/
    public String getCardNumber() {
        String formattedNumber = mCardHolder.getCardField().getText().toString();
        return ValidateCreditCard.numericOnlyString(formattedNumber);
    }

    public String getCardType() {
        return mCardIcon.getCardType().getName();
    }

    public boolean isFieldsValid() {
        if (!mCardHolder.isCardNumValid()) {
            return false;
        } else if (!mExpirationEditText.isValid()) {
            return false;
        } else if (!mCVVEditText.isValid()) {
            return false;
        } else if (requirePostCode && !mPostCodeEditText.isValid()) {
            return false;
        }
        return true;
    }

    /** this listener is a great place to call:
     * ViewUtils.hideSoftKeyboard((Activity)getContext());
     * clearFocus();
     *
     * Or to send focus to your next view.
     */
    public void setOnCardValidListener(final OnCardValidListener listener) {
        mCardValidListener = new OnValidationEventListener(){
            @Override
            public void onValidationEvent(boolean isValid) {
                if (isValid)
                    listener.cardIsValid();
            }
        };
    }

    /**
     * Set listener for validation events. Listeners will be notified whenever a field value has
     * changed and whether the data as a whole is now valid or invalid.
     * @param listener
     */
    public void setOnValidationEventListener(final OnValidationEventListener listener) {
        mCardValidListener = listener;
    }

    public void lockCardNumField() {
		transitionToExtraFields();
	}
	
	private void setup() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.pk_field_holder, this, true);
        if (!v.isInEditMode()) {
            mCardHolder = (CardNumHolder) findViewById(R.id.card_num_holder);
            mCardIcon = (CardIcon) findViewById(R.id.card_icon);
            mExtraFields = (LinearLayout) findViewById(R.id.extra_fields);
            mExpirationEditText = (ExpirationEditText) findViewById(R.id.expiration);
            mCVVEditText = (CVVEditText) findViewById(R.id.security_code);
            mPostCodeEditText = (PostCodeEditText) findViewById(R.id.post_code);
            mCardHolder.setCardEntryListener(mCardEntryListener);
            setRequirePostCode(mPostCodeEditText.getVisibility() == VISIBLE);
            setupViews();
        }
	}
	
	private void setupViews() {
		setExtraFieldsAlpha();
		setCardEntryListeners();
		setNecessaryFields();
	}
	
	private void setNecessaryFields() {
		setClipChildren(false);
        setAddStatesFromChildren(true);
	}
	
	private void setExtraFieldsAlpha() {
		ObjectAnimator setAlphaZero = ObjectAnimator.ofFloat(mExtraFields, "alpha", 0.0f);
		setAlphaZero.setDuration(0);
		setAlphaZero.start();
		mExtraFields.setVisibility(View.GONE);
	}

	private void setCardEntryListeners() {
		mExpirationEditText.setCardEntryListener(mCardEntryListener);
		mCVVEditText.setCardEntryListener(mCardEntryListener);
        mPostCodeEditText.setCardEntryListener(mCardEntryListener);
    }

	private void validateCard() {
        String input = mCardHolder.getCardField().getText().toString().replaceAll("\\s", "");
		long cardNumber = input.length() > 0 ? Long.parseLong(input) : 0;
		if (ValidateCreditCard.isValid(cardNumber)) {
			CardType cardType = ValidateCreditCard.getCardType(cardNumber);
			mCardIcon.setCardType(cardType);
			transitionToExtraFields();
		} else {
			mCardHolder.indicateInvalidCardNum();
		}
	}

	private void transitionToExtraFields() {
		// CREATE LAST 4 DIGITS OVERLAY
		mCardHolder.createOverlay();

		// MOVE CARD NUMBER TO LEFT AND ALPHA OUT
		AnimatorSet set = new AnimatorSet();
		ViewUtils.setHardwareLayer(mCardHolder);
		ObjectAnimator translateAnim = ObjectAnimator.ofFloat(mCardHolder, "translationX", -mCardHolder.getLeftOffset());
		translateAnim.setDuration(500);

		ObjectAnimator alphaOut = ObjectAnimator.ofFloat(mCardHolder.getCardField(), "alpha", 0.0f);
		alphaOut.setDuration(500);
		alphaOut.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				mCardHolder.getCardField().setVisibility(View.GONE);
				ViewUtils.setLayerTypeNone(mCardHolder);
			}
		});

		// ALPHA IN OTHER FIELDS
		mExtraFields.setVisibility(View.VISIBLE);
		ObjectAnimator alphaIn = ObjectAnimator.ofFloat(mExtraFields, "alpha", 1.0f);
		alphaIn.setDuration(500);
		set.playTogether(translateAnim, alphaOut, alphaIn);
		set.start();

		mExpirationEditText.requestFocus();
	}

    /** @return true if it focuses on an invalid field. **/
    private boolean focusOnInvalidField() {
        if (!mCardHolder.isCardNumValid()) {
            transitionToExtraFields();
            return true;
        }
        if (!mExpirationEditText.isValid()) {
            mExpirationEditText.requestFocus();
            mExpirationEditText.indicateInvalidDate();
            return true;
        }
        if (!mCVVEditText.isValid()) {
            mCVVEditText.requestFocus();
            mCVVEditText.indicateInvalidCVV();
            return true;
        }
        if (!mPostCodeEditText.isValid()) {
            mPostCodeEditText.requestFocus();
            mPostCodeEditText.indicateInvalidPostCode();
            return true;
        }
        return false;
    }

    public interface OnCardValidListener {
        public void cardIsValid();
    }

    public interface OnValidationEventListener {
        public void onValidationEvent(boolean isValid);
    }

    /**
     * Notify any registered OnValidationEventListener if and only if the valid state of all the data
     * has changed.
     */
    private void notifyOnValidationEventListeners() {
        boolean valid = isFieldsValid();
        if (valid != lastIsValid && mCardValidListener != null)
            mCardValidListener.onValidationEvent(valid);
        lastIsValid = valid;
    }

	protected interface CardEntryListener {
		void onCardNumberInputComplete();

		void onEdit();

		void onCardNumberInputReEntry();

        void onExpirationEntry();

        void onExpirationEdit();

		void onCVVEntry();

        void onCVVEdit();

		void onCVVEntryComplete();

		void onBackFromCVV();

        void onPostCodeEntry();

        void onPostCodeEdit();

        void onPostCodeEntryComplete();

        void onBackFromPostCode();
	}

	CardEntryListener mCardEntryListener = new CardEntryListener() {
		@Override
		public void onCardNumberInputComplete() {
			validateCard();
		}

		@Override
		public void onEdit() {
            mCardHolder.resetTextColor(); // In case the text color is an an error state.
            CardType newCardType = ValidateCreditCard.getCardType(mCardHolder.getCardField().getText().toString());
            if (!mCardIcon.isCardType(newCardType)) {
                if (newCardType == CardType.AMERICAN_EXPRESS) {
                    mCardHolder.getCardField().setMaxCardLength(AMEX_CARD_LENGTH);
                    mCVVEditText.setCVVMaxLength(CVVEditText.CCV_AMEX_LENGTH);
                } else if (newCardType == CardType.DINERS_CLUB) {
                    mCardHolder.getCardField().setMaxCardLength(DINERS_CARD_LENGTH);
                    mCVVEditText.setCVVMaxLength(CVVEditText.CCV_LENGTH);
                } else {
                    mCardHolder.getCardField().setMaxCardLength(NON_AMEX_CARD_LENGTH);
                    mCVVEditText.setCVVMaxLength(CVVEditText.CCV_LENGTH);
                }
                mCardIcon.setCardType(newCardType);
            }
            notifyOnValidationEventListeners();
		}

		@Override
		public void onCardNumberInputReEntry() {
			mCardIcon.flipTo(CardFace.FRONT);
			AnimatorSet set = new AnimatorSet();

			mCardHolder.getCardField().setVisibility(View.VISIBLE);
			ObjectAnimator alphaOut = ObjectAnimator.ofFloat(mExtraFields, "alpha", 0.0f);
			alphaOut.setDuration(RE_ENTRY_ALPHA_OUT_DURATION);
			alphaOut.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator anim) {
					mExtraFields.setVisibility(View.GONE);
					mCardHolder.destroyOverlay();
					mCardHolder.getCardField().requestFocus();
					mCardHolder.getCardField().setSelection(mCardHolder.getCardField().length());
				}
			});

			ObjectAnimator alphaIn = ObjectAnimator.ofFloat(mCardHolder.getCardField(), "alpha", 0.5f, 1.0f);
			alphaIn.setDuration(RE_ENTRY_ALPHA_IN_DURATION);

			ObjectAnimator overShoot = ObjectAnimator.ofFloat(mCardHolder, "translationX", -mCardHolder.getLeftOffset(), 0.0f);
			overShoot.setInterpolator(new OvershootInterpolator());
			overShoot.setDuration(RE_ENTRY_OVERSHOOT_DURATION);

			set.playTogether(alphaOut, alphaIn, overShoot);
			set.start();
		}

        @Override
        public void onExpirationEntry() {
            notifyOnValidationEventListeners();
            mCardIcon.flipTo(CardFace.FRONT);
            mExpirationEditText.requestFocus();
        }

        @Override
        public void onExpirationEdit() {
            notifyOnValidationEventListeners();
        }

        @Override
		public void onCVVEntry() {
            notifyOnValidationEventListeners();
			mCardIcon.flipTo(CardFace.BACK);
			mCVVEditText.requestFocus();
		}

        @Override
        public void onCVVEdit() {
            notifyOnValidationEventListeners();
        }

		@Override
		public void onCVVEntryComplete() {
            if (!requirePostCode) {
                if (!focusOnInvalidField()) {
                    mCardIcon.flipTo(CardFace.FRONT);
                    notifyOnValidationEventListeners();
                    // complete
                }
            } else {
                mPostCodeEditText.requestFocus();
            }
		}

		@Override
		public void onBackFromCVV() {
			mExpirationEditText.requestFocus();
			mCardIcon.flipTo(CardFace.FRONT);
		}

        @Override
        public void onPostCodeEntry() {
            notifyOnValidationEventListeners();
            mCardIcon.flipTo(CardFace.FRONT);
            mPostCodeEditText.requestFocus();
        }

        @Override
        public void onPostCodeEdit() {
            notifyOnValidationEventListeners();
        }

        @Override
        public void onPostCodeEntryComplete() {
            if (!focusOnInvalidField()) {
                if (requirePostCode)
                   notifyOnValidationEventListeners();
            }
        }

        @Override
        public void onBackFromPostCode() {
            mCVVEditText.requestFocus();
        }
	};
}
