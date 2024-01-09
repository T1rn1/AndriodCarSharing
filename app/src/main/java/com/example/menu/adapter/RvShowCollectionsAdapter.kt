package com.example.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.menu.databinding.RvBooksItemBinding
import com.example.menu.models.Books
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class RvShowCollectionsAdapter(private val bookList: java.util.ArrayList<Books>, private val title: String) : RecyclerView.Adapter<RvShowCollectionsAdapter.ViewHolder>() {

    class ViewHolder(val binding: RvBooksItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(RvBooksItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = bookList[position]
        holder.apply {
            binding.apply {
                tvTitleItem.text = currentItem.title
                tvAuthorItem.text = currentItem.author
                Picasso.get().load(currentItem.imageUri).into(imageItem)

                rvContainer.setOnLongClickListener {
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Удаленbt книги из коллекции")
                        .setMessage("Желаете удалить книгу")
                        .setPositiveButton("Да") { _, _ ->
                            val auth = FirebaseAuth.getInstance()
                            val firebaseRef = FirebaseDatabase.getInstance().getReference("Collections").child(auth.currentUser!!.uid).child(title)
                            //realtime database
                            firebaseRef.child(currentItem.id.toString()).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "книга успешно удалена",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Ошибка${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .setNegativeButton("Нет") { _, _ ->

                        }.show()
                    return@setOnLongClickListener true
                }
            }
        }
    }
}