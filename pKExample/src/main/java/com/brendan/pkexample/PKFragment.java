package com.brendan.pkexample;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.paymentkit.util.ToastUtils;
import com.paymentkit.util.ViewUtils;
import com.paymentkit.views.FieldHolder;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * 
 * @author Brendan Weinstein
 * http://www.brendanweinstein.me
 *
 */
public class PKFragment extends Fragment {

	private final static String TAG = PKFragment.class.getSimpleName();

	public static final float INPUT_WIDTH = 0.94f; // defined in terms of screen
    private ImageView mAcceptedCardsImg;
	private FieldHolder mFieldHolder;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View viewRoot = inflater.inflate(R.layout.add_credit_card, container, false);
		mAcceptedCardsImg = (ImageView) viewRoot.findViewById(R.id.accepted_cards);
		mFieldHolder = (FieldHolder) viewRoot.findViewById(R.id.field_holder);
        final Button saveBtn = (Button) viewRoot.findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(mSaveBtnListener);
        saveBtn.setEnabled(false);
        final TextView status = (TextView) viewRoot.findViewById(R.id.valid_message);
        CheckBox requireZip = (CheckBox) viewRoot.findViewById(R.id.check_require_zip);
        requireZip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFieldHolder.setRequirePostCode(isChecked);
            }
        });
        mFieldHolder.setRequirePostCode(requireZip.isChecked());
        mFieldHolder.setOnValidationEventListener(new FieldHolder.OnValidationEventListener() {
            @Override
            public void onValidationEvent(final boolean isValid) {
                saveBtn.setEnabled(isValid);
                status.setText(isValid ? "Valid credit card entry!" : "Credit card is invalid!");

                final float bigger = 1.25f;
                AnimatorSet shrink = new AnimatorSet();
                shrink.playTogether(
                        ObjectAnimator.ofFloat(status, "scaleX", bigger, 1f),
                        ObjectAnimator.ofFloat(status, "scaleY", bigger, 1f)
                );
                AnimatorSet grow = new AnimatorSet();
                grow.play(ObjectAnimator.ofFloat(status, "scaleX", 1f, bigger))
                        .with(ObjectAnimator.ofFloat(status, "scaleY", 1f, bigger))
                        .before(shrink);
                grow.start();
            }
        });
		return viewRoot;
	}

	private OnClickListener mSaveBtnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewUtils.hideSoftKeyboard(getActivity());
			if (mFieldHolder.isFieldsValid()) {
				ToastUtils.showToast(getActivity(), "Valid credit card entry!");
			} else {
                ToastUtils.showToast(getActivity(), getResources().getString(com.paymentkit.R.string.pk_error_invalid_card_no));
            }
		}
	};

	private void setupViews() {
		float marginLeft = 1.0f - INPUT_WIDTH;
		ViewUtils.setMarginLeft(mAcceptedCardsImg, (int) (marginLeft * ViewUtils.getScreenWidth(getActivity())));
		ViewUtils.setWidth(mFieldHolder, (int) (INPUT_WIDTH * ViewUtils.getScreenWidth(getActivity())));
	}

	/* After onCreateView is called */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupViews();
	}

}