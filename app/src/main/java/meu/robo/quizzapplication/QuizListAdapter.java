package meu.robo.quizzapplication;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuizViewHolder> {

    private List<QuizListModel> quizListModels;
    private OnQuizListItemClicked onQuizListItemClicked;

    public QuizListAdapter(OnQuizListItemClicked onQuizListItemClicked){
        this.onQuizListItemClicked = onQuizListItemClicked;;
    }

    public void setQuizListModels(List<QuizListModel> quizListModels) {
        this.quizListModels = quizListModels;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //serve para para inflar o layout do item.
        // Basicamente é chamado quando é necessário criar um novo item.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        //tem a finalidade de definir os atributos de
        // exibição com base nos dados. Basicamente é
        // invocado quando um item precisa ser exibido para o usuário.
        holder.listTitle.setText(quizListModels.get(position).getName());

        //PEGANDO A URL DA IMAGEM:
        String  imageUrl = quizListModels.get(position).getImage();

        Glide.with(holder.itemView.getContext()) // context
                .load(imageUrl) //url da imagem
                .centerCrop() //posição
                .placeholder(R.drawable.placeholder_image) //caso ñ tenha img ele carregara uma imagem padrão
                .into(holder.listImage);//onde deseja carregar a image

        String listDescription = quizListModels.get(position).getDesc();

        if(listDescription.length() >  140){
            listDescription = listDescription.substring(0,140);
        }

        holder.listDesc.setText(listDescription + "...");
        holder.listLevel.setText(quizListModels.get(position).getLevel());
        holder.listBtn.setText("Visualizar Quiz");

    }

    @Override
    public int getItemCount() {
        //exibe o tamanho da lista
        if(quizListModels == null){
            return 0;
        }else{
            return quizListModels.size();
        }

    }

    public class QuizViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView listImage;
        private TextView listTitle;
        private TextView listDesc;
        private TextView listLevel;
        private Button listBtn;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);

            listImage = itemView.findViewById(R.id.list_image);
            listTitle = itemView.findViewById(R.id.list_title);
            listDesc = itemView.findViewById(R.id.list_desc);
            listLevel = itemView.findViewById(R.id.list_difficulty);
            listBtn = itemView.findViewById(R.id.list_btn);


            listBtn.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            //chamando a interface e acessando o metodo dentro dela:
            //no parametro estamos recuperando a posição do item que o usuario clicou
            onQuizListItemClicked.onItemClicked(getAdapterPosition());
        }
    }

    public interface OnQuizListItemClicked{
        void onItemClicked(int position);
    }
}
