package com.paymentkit.views;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import com.nineoldandroids.animation.ObjectAnimator;
import com.paymentkit.util.AnimUtils;
import com.paymentkit.views.FieldHolder.CardEntryListener;

public class PostCodeEditText extends EditText {

    public static final int US_POST_CODE_LENGTH = 5;
    private int postCodeMaxLength = US_POST_CODE_LENGTH;

	private static final String TAG = PostCodeEditText.class.getSimpleName();

	private CardEntryListener mListener;

    private ObjectAnimator shakeAnim;

    public PostCodeEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public PostCodeEditText(Context context) {
		super(context);
		setup();
	}
	
	private void setup() {
		addTextChangedListener(mTextWatcher);
		setOnFocusChangeListener(mFocusListener);
	}

    public boolean isValid() {
        return postCodeMaxLength == getText().toString().length();
    }
	
	public void setCardEntryListener(CardEntryListener listener) {
		mListener = listener;
	}

    public void setPostCodeMaxLength(int val) {
        postCodeMaxLength = val;
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(val);
        setFilters(filters);
    }
	
	private OnFocusChangeListener mFocusListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				mListener.onPostCodeEntry();
			}
		}
	};
	
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		return new ZanyInputConnection(super.onCreateInputConnection(outAttrs),
				true);
	}

    public void indicateInvalidPostCode() {
        if (shakeAnim != null) shakeAnim.end();
        shakeAnim = AnimUtils.getShakeAnimation(this, true);
        shakeAnim.start();
    }

    /*
     * See "android EditText delete(backspace) key event" on stackoverflow
     */
	private class ZanyInputConnection extends InputConnectionWrapper {

		public ZanyInputConnection(InputConnection target, boolean mutable) {
			super(target, mutable);
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event) {
            boolean shouldConsume = false;
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DEL:
                        if (getSelectionStart() == 0) {
                            mListener.onBackFromPostCode();
                            shouldConsume = true;
                        }
                        break;
                    case KeyEvent.KEYCODE_ENTER:
                        shouldConsume = true;
                        mListener.onPostCodeEntryComplete(); break;
                }
            }
			return shouldConsume ? true : super.sendKeyEvent(event);
		}

        @Override
        public boolean performEditorAction(int editorAction) {
                boolean shouldConsume = false;
            switch (editorAction) {
                case EditorInfo.IME_ACTION_DONE:
                    shouldConsume = true;
                    mListener.onPostCodeEntryComplete();
            }
            return shouldConsume ? true : super.performEditorAction(editorAction);
        }

        @Override
		public boolean deleteSurroundingText(int beforeLength, int afterLength) {
			// magic: in latest Android, deleteSurroundingText(1, 0) will be
			// called for backspace
			if (beforeLength == 1 && afterLength == 0) {
				// backspace
				return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_DEL))
						&& sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
								KeyEvent.KEYCODE_DEL));
			}

			return super.deleteSurroundingText(beforeLength, afterLength);
		}
	}
	
	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() == postCodeMaxLength) {
				mListener.onPostCodeEntryComplete();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	};

}
