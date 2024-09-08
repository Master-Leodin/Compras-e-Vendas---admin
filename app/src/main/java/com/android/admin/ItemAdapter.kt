import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.admin.ItemVenda
import com.android.admin.R
import com.bumptech.glide.Glide

class ItemAdapter(
    private val itemList: MutableList<ItemVenda>,
    private val onEditClicked: (ItemVenda) -> Unit,
    private val onDeleteClicked: (ItemVenda) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomeProduto: TextView = itemView.findViewById(R.id.tvNomeProduto)
        val tvDescricaoProduto: TextView = itemView.findViewById(R.id.tvDescricaoProduto)
        val tvPrecoProduto: TextView = itemView.findViewById(R.id.tvPrecoProduto)
        val ivFotoProduto: ImageView = itemView.findViewById(R.id.ivFotoProduto)
        val btnEditar: Button = itemView.findViewById(R.id.btnEditar)
        val btnDeletar: Button = itemView.findViewById(R.id.btnDeletar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_produto, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.tvNomeProduto.text = currentItem.nome
        holder.tvDescricaoProduto.text = currentItem.descricao
        holder.tvPrecoProduto.text = "R$ ${currentItem.preco}"

        // Use uma biblioteca como Glide ou Picasso para carregar a imagem
        Glide.with(holder.itemView.context).load(currentItem.imagemUrl).into(holder.ivFotoProduto)

        holder.btnEditar.setOnClickListener {
            onEditClicked(currentItem)
        }

        holder.btnDeletar.setOnClickListener {
            onDeleteClicked(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
