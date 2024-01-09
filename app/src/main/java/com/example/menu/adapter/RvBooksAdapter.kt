package com.example.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.menu.app.LibraryFragmentDirections
import com.example.menu.databinding.RvBooksItemBinding
import com.example.menu.models.Books
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class RvBooksAdapter(private val bookList: java.util.ArrayList<Books>) : RecyclerView.Adapter<RvBooksAdapter.ViewHolder>() {

    private var auth = FirebaseAuth.getInstance()

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

                rvContainer.setOnClickListener{
                    val action = LibraryFragmentDirections.actionLibraryFragmentToUpdateFragment(
                        currentItem.id.toString(),
                        currentItem.title.toString(),
                        currentItem.author.toString(),
                        currentItem.imageUri.toString(),
                        currentItem.genre.toString(),
                        currentItem.description.toString(),
                        currentItem.location.toString()
                    )
                    findNavController(holder.itemView).navigate(action)
                }

                if(auth.currentUser?.email == "admin1@gmail.com") {
                    rvContainer.setOnLongClickListener {
                        MaterialAlertDialogBuilder(holder.itemView.context)
                            .setTitle("Удаление книги")
                            .setMessage("Желаете удалить книгу")
                            .setPositiveButton("Да") { _, _ ->
                                val firebaseRef =
                                    FirebaseDatabase.getInstance().getReference("Books")
                                val storageRef =
                                    FirebaseStorage.getInstance().getReference("Images")
                                //storage database
                                storageRef.child(currentItem.id.toString()).delete()
                                //realtime database
                                firebaseRef.child(currentItem.id.toString()).removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            holder.itemView.context,
                                            "Книга успешно удалена",
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
}