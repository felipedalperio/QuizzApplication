package meu.robo.quizzapplication;

import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//FRAGMENT RESPONSAVEL POR CONTER TODAS AS PERGUNTAS
public class QuizFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "QUIZ_FRAGMENT_LOG";
    private NavController navController;

    private FirebaseFirestore firebaseFirestore;
    private String quizId;
    private String quizName;

    private TextView quizTitle;
    private Button optionOneBtn;
    private Button optionTwoBtn;
    private Button optionThreeBtn;
    private Button nextBtn;
    private ImageButton closeBtn;
    private TextView questionFeedback;
    private TextView questionText;
    private TextView questionTime;
    private ProgressBar questionProgress;
    private TextView questionNumber;
    private CountDownTimer countDownTimer;
    private boolean canAnswer = false;
    private int currentQuestion = 0;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int notAnswered = 0;

    private String currentUserId;
    private FirebaseAuth firebaseAuth;

    private List<QuestionModel> allQuestionsList = new ArrayList<>();
    private long totalQuestionsToAnswer = 10;
    private List<QuestionModel> questionToAnswer = new ArrayList<>();

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        //instanciando o FirebaseAuth
        firebaseAuth = firebaseAuth.getInstance();
        //pegando o id do Usuario atual:
        if(firebaseAuth.getCurrentUser() != null){
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }else{ //caso o uuario nao esteja logado
            //volta para o home:
        }

        firebaseFirestore = FirebaseFirestore.getInstance();

        quizTitle = view.findViewById(R.id.quiz_title);
        optionOneBtn = view.findViewById(R.id.quiz_option_one);
        optionTwoBtn = view.findViewById(R.id.quiz_option_two);
        optionThreeBtn = view.findViewById(R.id.quiz_option_three);
        nextBtn = view.findViewById(R.id.quiz_next_btn);
        questionFeedback = view.findViewById(R.id.quiz_question_feedback);
        questionText = view.findViewById(R.id.quiz_question);
        questionTime = view.findViewById(R.id.quiz_question_time);
        questionProgress = view.findViewById(R.id.quiz_question_progress);
        questionNumber = view.findViewById(R.id.quiz_question_number);


        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();
        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();

        totalQuestionsToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();


        //pegando todas as questões de uma quiz :
        firebaseFirestore.collection("QuizList");

        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Questions")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    allQuestionsList = task.getResult().toObjects(QuestionModel.class);
                    totalQuestionsToAnswer = allQuestionsList.size() ;

                    pickQuestions();
                    loadUI();

                }else{
                    quizTitle.setText("Error Loading Data");
                }
            }
        });

        //evento de click nas opções:
        optionOneBtn.setOnClickListener(this);
        optionTwoBtn.setOnClickListener(this);
        optionThreeBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }

    private void loadUI() {
        //SETANDO AS INFORMAÇÕES:
        quizTitle.setText(quizName);
        questionText.setText("Load first Question");
        //Visibilidade questões:
        enableOptions();

        //carregar first Question:
        loadQuestion(1);
    }

    private void loadQuestion(int questionNum) {
        questionNumber.setText(questionNum + "");
        //carrregando a qestão:
        questionText.setText(questionToAnswer.get(questionNum-1).getQuestion());
        //carregando as opções:
        optionOneBtn.setText(questionToAnswer.get(questionNum-1).getOption_a());
        optionTwoBtn.setText(questionToAnswer.get(questionNum-1).getOption_b());
        optionThreeBtn.setText(questionToAnswer.get(questionNum-1).getOption_c());

        //setando a possibilidade da resposta:
        canAnswer = true;
        currentQuestion = questionNum;

        //Timer:
        startTimer(questionNum);

    }

    private void startTimer(final int questionNumber) {
        final Long timeToAnswer = questionToAnswer.get(questionNumber-1).getTimer();
        questionTime.setText(timeToAnswer.toString());

        //mostrar o progressBar:
        questionProgress.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(timeToAnswer*1000,1){

            @Override
            public void onTick(long millisUntilFinished) {
                //atualiza o timer:
                questionTime.setText(millisUntilFinished/1000 + "");

                //Progress in percent:
                Long percent = millisUntilFinished/(timeToAnswer*10);
                questionProgress.setProgress(percent.intValue());
            }

            @Override
            public void onFinish() {
                canAnswer = false;
                questionFeedback.setText("Ops.. Nenhuma resposta foi escolhida");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary));
                notAnswered++;
                showNextBtn(); // mostrar o botão proximo
            }
        };
        countDownTimer.start();
    }


    private void enableOptions() {
        optionOneBtn.setVisibility(View.VISIBLE);
        optionTwoBtn.setVisibility(View.VISIBLE);
        optionThreeBtn.setVisibility(View.VISIBLE);

        optionOneBtn.setEnabled(true);
        optionTwoBtn.setEnabled(true);
        optionThreeBtn.setEnabled(true);

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void pickQuestions() {

        for (int i=0; i<totalQuestionsToAnswer; i++){
            int randomNumber = getRandomInteger(allQuestionsList.size(), 0);
            questionToAnswer.add(allQuestionsList.get(randomNumber));
            allQuestionsList.remove(randomNumber); //casp tover
            Log.d(TAG, "Question "+ i +": " + questionToAnswer.get(i).getQuestion());
        }
    }

    public static int getRandomInteger(int maxinum, int mininum){
        return ((int) (Math.random() * (maxinum - mininum))) + mininum;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.quiz_option_one:
                answerSelected(optionOneBtn);
                break;
            case R.id.quiz_option_two:
                answerSelected(optionTwoBtn);
                break;
            case R.id.quiz_option_three:
                answerSelected(optionThreeBtn);
                break;
            case R.id.quiz_next_btn:
                if(currentQuestion == totalQuestionsToAnswer) {
                    //Carregar resultados:
                    submitResult();
                }else {
                    currentQuestion++;
                    loadQuestion(currentQuestion);
                    resetOptions();
                }
                break;
        }
    }

    private void submitResult() {
        HashMap<String,Object> resultMap = new HashMap<>();
        resultMap.put("correct",correctAnswers);
        resultMap.put("wrong",wrongAnswers);
        resultMap.put("unanswered",notAnswered);



        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Results")
                .document(currentUserId).set(resultMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //vai para a pagina de resultados
                    QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                    action.setQuizId(quizId);
                    navController.navigate(action);
                }else{
                    //mostra um erro
                    quizTitle.setText(task.getException().getMessage());
                }
            }
        });
    }

    private void resetOptions() {
        optionOneBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));
        optionTwoBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));
        optionThreeBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));

        optionOneBtn.setTextColor(getResources().getColor(R.color.colorLightText));
        optionTwoBtn.setTextColor(getResources().getColor(R.color.colorLightText));
        optionThreeBtn.setTextColor(getResources().getColor(R.color.colorLightText));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);


    }

    private void answerSelected(Button selectAnswerBtn) {
        if(canAnswer){ //verifica se pode responder a questão
            //texto do botão para preto:
            selectAnswerBtn.setTextColor(getResources().getColor(R.color.colorDark));
            //resposta correta:
            if(questionToAnswer.get(currentQuestion-1).getAnswer().equals(selectAnswerBtn.getText())){
                //Log.d(TAG,"Resposta Correta");
                correctAnswers ++;
                selectAnswerBtn.setBackground(getResources().getDrawable(R.drawable.correct_answer_btn_bg, null));

                //FeedBack:
                questionFeedback.setText("Resposta Correta");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary));

            }else{ // resposta errada
                //Log.d(TAG,"Resposta Errada");
                wrongAnswers ++;
                selectAnswerBtn.setBackground(getResources().getDrawable(R.drawable.wrong_answer_btn_bg, null));

                //FeedBack:
                questionFeedback.setText("Resposta Errada, A resposta correta é  "+questionToAnswer.get(currentQuestion-1).getAnswer());
                questionFeedback.setTextColor(getResources().getColor(R.color.colorAccent));

            }
            canAnswer = false;

            //parar o timer:
            countDownTimer.cancel();

            //mostrar para ir para o proximo :
            showNextBtn();
        }
    }

    private void showNextBtn() {
        if(currentQuestion == totalQuestionsToAnswer) {
            nextBtn.setText("Ver resultado");
        }
        questionFeedback.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setEnabled(true);

    }
}