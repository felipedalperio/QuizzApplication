package meu.robo.quizzapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

//FRAGMENT RESPONSAVEL POR MOSTRAR O RESULTADO
public class ResultFragment extends Fragment {

    private NavController navController;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String quizId;

    private TextView resultCorrect;
    private TextView resultWrong;
    private TextView resultMissed;

    private TextView resultPercent;
    private ProgressBar resultProgress;
    private Button resultHomeBtn;
    private String currentUserId;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);

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

        firebaseFirestore = firebaseFirestore.getInstance();
        quizId = ResultFragmentArgs.fromBundle(getArguments()).getQuizId();

        //Inicializando componentes:
        resultCorrect = view.findViewById(R.id.results_correct_text);
        resultWrong = view.findViewById(R.id.results_wrong_text);
        resultMissed = view.findViewById(R.id.results_missed_text);

        resultHomeBtn = view.findViewById(R.id.results_home_btn);
        resultPercent = view.findViewById(R.id.results_percent);
        resultProgress = view.findViewById(R.id.results_progress);

        resultHomeBtn = view.findViewById(R.id.results_home_btn);
        resultHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_resultFragment_to_listFragment);
            }
        });

        //Pegando resultado:
        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Results")
                .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();

                    Long correct = result.getLong("correct");
                    Long wrong  = result.getLong("wrong");
                    Long missed = result.getLong("unanswered");

                    resultCorrect.setText(correct.toString());
                    resultWrong.setText(wrong.toString());
                    resultMissed.setText(missed.toString());

                    //CALCULANDO O PROGRESS BAR:
                    Long total = correct + wrong + missed;
                    Long percent = (correct*100)/total;
                    resultPercent.setText(percent + "%");
                    resultProgress.setProgress(percent.intValue());

                }
            }
        });
    }
}