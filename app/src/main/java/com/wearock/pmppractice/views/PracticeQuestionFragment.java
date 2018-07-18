package com.wearock.pmppractice.views;


import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.wearock.pmppractice.R;
import com.wearock.pmppractice.models.Answer;
import com.wearock.pmppractice.models.Question;
import com.wearock.pmppractice.models.QuestionBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A simple {@link Fragment} subclass.
 */
public class PracticeQuestionFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private Question question;
    private int answer;
    private boolean editable;

    private TextView tvDescription;
    private ImageView imgQuestionImage;
    private RadioButton rbChoiceA;
    private RadioButton rbChoiceB;
    private RadioButton rbChoiceC;
    private RadioButton rbChoiceD;
    private LinearLayout pQuestionResult;
    private TextView tvCorrectAnswer;

    public PracticeQuestionFragment() {
        // Required empty public constructor
    }

    public static PracticeQuestionFragment newInstance(Question question, int answer, boolean editable) {
        PracticeQuestionFragment instance = new PracticeQuestionFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("question", question);
        bundle.putInt("answer", answer);
        bundle.putBoolean("editable", editable);
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            question = (Question) args.getSerializable("question");
            answer = args.getInt("answer");
            editable = args.getBoolean("editable");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragRoot = inflater.inflate(R.layout.fragment_practice_question, container, false);

        tvDescription = fragRoot.findViewById(R.id.tvQuestionDescription);
        imgQuestionImage = fragRoot.findViewById(R.id.imgQuestionImage);
        rbChoiceA = fragRoot.findViewById(R.id.rbQuestionChoiceA);
        rbChoiceA.setOnCheckedChangeListener(this);
        rbChoiceA.setEnabled(editable);
        rbChoiceB = fragRoot.findViewById(R.id.rbQuestionChoiceB);
        rbChoiceB.setOnCheckedChangeListener(this);
        rbChoiceB.setEnabled(editable);
        rbChoiceC = fragRoot.findViewById(R.id.rbQuestionChoiceC);
        rbChoiceC.setOnCheckedChangeListener(this);
        rbChoiceC.setEnabled(editable);
        rbChoiceD = fragRoot.findViewById(R.id.rbQuestionChoiceD);
        rbChoiceD.setOnCheckedChangeListener(this);
        rbChoiceD.setEnabled(editable);

        updateQuestionBody();

        pQuestionResult = fragRoot.findViewById(R.id.pQuestionResult);
        pQuestionResult.setVisibility(editable ? View.INVISIBLE : View.VISIBLE);
        pQuestionResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTips();
            }
        });

        tvCorrectAnswer = fragRoot.findViewById(R.id.tvCorrectAnswer);
        tvCorrectAnswer.setText((new String[] {"A", "B", "C", "D"})[question.getAnswer()]);

        return fragRoot;
    }

    public void updateQuestionBody() {
        PracticeActivity curActivity = (PracticeActivity) getActivity();
        QuestionBody body = question.getBodyByLanguage((curActivity == null) ?
                QuestionBody.Language.Chinese : curActivity.getCurLanguage());
        tvDescription.setText(Html.fromHtml(body.getDescription()));
        if (question.getImage() == null) {
            imgQuestionImage.setVisibility(View.INVISIBLE);
        } else {
            imgQuestionImage.setVisibility(View.VISIBLE);

            InputStream is = null;
            try {
                is = getActivity().getResources().getAssets().open("questions/" + question.getImage());
                imgQuestionImage.setImageDrawable(Drawable.createFromStream(is, null));
            } catch (IOException ioe) {
                imgQuestionImage.setVisibility(View.INVISIBLE);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // Ignore here
                    }
                }
            }
        }
        rbChoiceA.setText("A: " + body.getChoiceA());
        rbChoiceB.setText("B: " + body.getChoiceB());
        rbChoiceC.setText("C: " + body.getChoiceC());
        rbChoiceD.setText("D: " + body.getChoiceD());

        switch (answer) {
            case 0:
                rbChoiceA.setChecked(true);
                rbChoiceB.setChecked(false);
                rbChoiceC.setChecked(false);
                rbChoiceD.setChecked(false);
                break;
            case 1:
                rbChoiceA.setChecked(false);
                rbChoiceB.setChecked(true);
                rbChoiceC.setChecked(false);
                rbChoiceD.setChecked(false);
                break;
            case 2:
                rbChoiceA.setChecked(false);
                rbChoiceB.setChecked(false);
                rbChoiceC.setChecked(true);
                rbChoiceD.setChecked(false);
                break;
            case 3:
                rbChoiceA.setChecked(false);
                rbChoiceB.setChecked(false);
                rbChoiceC.setChecked(false);
                rbChoiceD.setChecked(true);
                break;
            default:
                rbChoiceA.setChecked(false);
                rbChoiceB.setChecked(false);
                rbChoiceC.setChecked(false);
                rbChoiceD.setChecked(false);
                break;
        }
    }

    private void showTips() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View tipsRoot = LayoutInflater.from(getActivity()).inflate(
                R.layout.layout_question_tips, null);

        String[] answers = new String[] { "A", "B", "C", "D" };
        ((TextView) tipsRoot.findViewById(R.id.tvTipsAnswer)).setText(answers[question.getAnswer()]);
        ((TextView) tipsRoot.findViewById(R.id.tvTipsDomain)).setText(question.getDomain());
        ((TextView) tipsRoot.findViewById(R.id.tvTipsProcess)).setText(question.getProcess());
        ((TextView) tipsRoot.findViewById(R.id.tvTipsSubproc)).setText(question.getSubProcess());
        ((TextView) tipsRoot.findViewById(R.id.tvTipsKnowledgePoint)).setText(question.getKnowledgePoint());
        ((TextView) tipsRoot.findViewById(R.id.tvTipsExplain)).setText(question.getExplanation());

        builder.setTitle(R.string.practice_menu_tips);
        builder.setView(tipsRoot);
        builder.setNegativeButton(R.string.dialog_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int answer = -1;
        switch (compoundButton.getId()) {
            case R.id.rbQuestionChoiceA:
                answer = 0;
                break;
            case R.id.rbQuestionChoiceB:
                answer = 1;
                break;
            case R.id.rbQuestionChoiceC:
                answer = 2;
                break;
            case R.id.rbQuestionChoiceD:
                answer = 3;
                break;
        }

        Answer iAnswer = new Answer();
        iAnswer.setQuestionId(question.getId());
        iAnswer.setAnswer(answer);
        ((PracticeActivity)getActivity()).receiveAnswer(iAnswer);
    }

}
