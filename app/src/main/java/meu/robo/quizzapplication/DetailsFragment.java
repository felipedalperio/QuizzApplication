package meu.robo.quizzapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

//FRAGMENT RESPONSAVEL POR MOSTRAR OS DETALHES DE CADA QUESTÃO
public class DetailsFragment extends Fragment implements View.OnClickListener {

    private NavController navController;
    private QuizListViewModel quizListViewModel;
    private int position;

    private ImageView detailsImage;
    private TextView detailsTitle;
    private TextView detailsDesc;
    private TextView detailsDiff;
    private TextView detailsQuestions;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private TextView detailsScore;
    private TextView totalPerguntas;

    private Button detailsStartBtn;
    private String quizId;
    private long totalQuestions = 0;
    private String quizName;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Load Previous Results
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //pegando o id do Usuario atual:
        if(firebaseAuth.getCurrentUser() != null){
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }else{ //caso o uuario nao esteja logado
            //volta para o home:
        }


        navController = Navigation.findNavController(view);
        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();
        Log.d("APP_LOG","Position: " + position);

        //inciciando componentes:
        detailsStartBtn = view.findViewById(R.id.details_start_btn);
        detailsImage = view.findViewById(R.id.details_image);
        detailsTitle = view.findViewById(R.id.details_title);
        detailsDesc = view.findViewById(R.id.details_desc);
        detailsDiff = view.findViewById(R.id.details_difficulty_text);
        detailsQuestions = view.findViewById(R.id.details_questions);
        detailsScore = view.findViewById(R.id.details_score_text);
        totalPerguntas = view.findViewById(R.id.details_questions_text);



        //EVENTO DE CLICK PARA O BOTÃO:

        detailsStartBtn.setOnClickListener(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {

                Glide.with(getContext()) // context
                        .load(quizListModels.get(position).getImage()) //url da imagem
                        .centerCrop() //posição
                        .placeholder(R.drawable.placeholder_image) //caso ñ tenha img ele carregara uma imagem padrão
                        .into(detailsImage);//onde deseja carregar a image

                detailsTitle.setText(quizListModels.get(position).getName());
                detailsDesc.setText(quizListModels.get(position).getDesc());
                detailsDiff.setText(quizListModels.get(position).getLevel());

                totalPerguntas.setText( "5" );

               // detailsQuestions.setText(quizListModels.get(position).getQuestion() + "");


                quizId = quizListModels.get(position).getQuiz_id();
                quizName = quizListModels.get(position).getName();
                totalQuestions = quizListModels.get(position).getQuestion();


                //carregando o resultado:
                loadResultData();

            }
        });
    }

    private void loadResultData() {
        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Results")
                .document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                   if(document != null && document.exists()){
                       Long correct = document.getLong("correct");
                       Long wrong  = document.getLong("wrong");
                       Long missed = document.getLong("unanswered");

                        Long total = correct + wrong + missed;
                        Long percent = ( correct*100)/total;
                        detailsScore.setText(percent+ "%");
                   }
                }else{
                    //mostra um erro

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.details_start_btn:

                DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment
                        action = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
                action.setTotalQuestions(totalQuestions);
                action.setQuizId(quizId);
                action.setQuizName(quizName);
                navController.navigate(action);
                break;
        }
    }
}