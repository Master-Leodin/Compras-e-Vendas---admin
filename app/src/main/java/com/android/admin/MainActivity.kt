package com.android.admin

import ItemAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

data class ItemVenda(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val preco: Double = 0.0,
    val imagemUrl: String = ""
)

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemList: MutableList<ItemVenda>
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var database: DatabaseReference

    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etPreco: EditText
    private lateinit var ivFotoProduto: ImageView
    private lateinit var btnSelecionarImagem: Button
    private lateinit var btnAdicionar: Button
    private var imagemUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialização das views
        etNome = findViewById(R.id.etNome)
        etDescricao = findViewById(R.id.etDescricao)
        etPreco = findViewById(R.id.etPreco)
        ivFotoProduto = findViewById(R.id.ivFotoProduto)
        btnSelecionarImagem = findViewById(R.id.btnSelecionarImagem)
        btnAdicionar = findViewById(R.id.btnAdicionar)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemList = mutableListOf()
        itemAdapter = ItemAdapter(itemList, ::onEditClicked, ::onDeleteClicked)
        recyclerView.adapter = itemAdapter

        database = FirebaseDatabase.getInstance().getReference("itens")

        btnSelecionarImagem.setOnClickListener {
            selecionarImagem()
        }

        btnAdicionar.setOnClickListener {
            adicionarItem()
        }

        // Carregar itens do Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(ItemVenda::class.java)
                    item?.let { itemList.add(it) }
                }
                itemAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun selecionarImagem() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            imagemUri = data.data
            ivFotoProduto.setImageURI(imagemUri)
        }
    }

    private fun adicionarItem() {
        val nome = etNome.text.toString()
        val descricao = etDescricao.text.toString()
        val preco = etPreco.text.toString().toDoubleOrNull()

        if (nome.isEmpty() || descricao.isEmpty() || preco == null || imagemUri == null) {
            Toast.makeText(this, "Preencha todos os campos e selecione uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("itens")
        val itemId = database.push().key ?: return
        val storageRef = FirebaseStorage.getInstance().getReference("imagens/$itemId.jpg")

        storageRef.putFile(imagemUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val item = ItemVenda(itemId, nome, descricao, preco, uri.toString())

                    database.child(itemId).setValue(item)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item adicionado com sucesso", Toast.LENGTH_SHORT).show()
                            etNome.text.clear()
                            etDescricao.text.clear()
                            etPreco.text.clear()
                            ivFotoProduto.setImageResource(0)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao adicionar item", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onEditClicked(item: ItemVenda) {
        etNome.setText(item.nome)
        etDescricao.setText(item.descricao)
        etPreco.setText(item.preco.toString())

        Glide.with(this).load(item.imagemUrl).into(ivFotoProduto)
        imagemUri = null // Resetar imagemUri para evitar conflitos ao salvar

        btnAdicionar.setOnClickListener {
            val novoNome = etNome.text.toString()
            val novaDescricao = etDescricao.text.toString()
            val novoPreco = etPreco.text.toString().toDoubleOrNull()

            if (novoNome.isEmpty() || novaDescricao.isEmpty() || novoPreco == null) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novoItem = item.copy(nome = novoNome, descricao = novaDescricao, preco = novoPreco)

            database.child(item.id).setValue(novoItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Item editado com sucesso", Toast.LENGTH_SHORT).show()
                    etNome.text.clear()
                    etDescricao.text.clear()
                    etPreco.text.clear()
                    ivFotoProduto.setImageResource(0)
                    imagemUri = null
                    btnAdicionar.setText("Adicionar Item")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao editar item", Toast.LENGTH_SHORT).show()
                }
        }
        btnAdicionar.setText("Salvar Alterações")
    }

    private fun onDeleteClicked(item: ItemVenda) {
        database.child(item.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Item deletado com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao deletar item", Toast.LENGTH_SHORT).show()
            }
    }
}
