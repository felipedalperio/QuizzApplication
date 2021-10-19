package meu.robo.quizzapplication;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.PublicKey;
import java.util.List;

public class FirebaseRepository {
    //CHAMANDO A INTERFACE:
    private OnFirestoreTaskComplete onFirestoreTaskComplete;

    //INSANCIA PARA O FIREBASE STORE:
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    //REFRENCIA DA COLEÇÃO "TABELA" :
    private Query quizRef = firebaseFirestore.collection("QuizList")
            .whereEqualTo("visibility", "public");

    public FirebaseRepository(OnFirestoreTaskComplete onFirestoreTaskComplete){
        this.onFirestoreTaskComplete = onFirestoreTaskComplete;
    }

    //NOS PERMITE VISUALIZAR OS DADOS:
    public void getQuizData(){
        quizRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    onFirestoreTaskComplete.quizListDataAdded(task.getResult().toObjects(QuizListModel.class));
                }else{
                    onFirestoreTaskComplete.onError(task.getException());
                }
            }
        });
    }

    public interface OnFirestoreTaskComplete{
        //Lista de elementos model:
        void quizListDataAdded(List<QuizListModel> quizListModelList);
        //exeções:
        void onError(Exception e);

    }

}
