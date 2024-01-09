package com.example.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.menu.app.CollectionsFragmentDirections
import com.example.menu.databinding.RvCollectionsItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RvCollectionsAdapter(private val collectionsList: java.util.ArrayList<String>) : RecyclerView.Adapter<RvCollectionsAdapter.ViewHolder>(){

    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(val binding: RvCollectionsItemBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvCollectionsItemBinding.inflate(LayoutInflater.from(parent.context), parent,false))
    }

    override fun getItemCount(): Int {
        return collectionsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = collectionsList[position]
        holder.apply {
            binding.apply {
                tvTitleItem.text = currentItem

                rvContainer.setOnClickListener{
                    if (currentItem != null) {
                        val action = CollectionsFragmentDirections.actionCollectionsFragmentToShowCollectionFragment(currentItem)
                        Navigation.findNavController(holder.itemView).navigate(action)
                    } else {
                        Toast.makeText(holder.itemView.context, "Ошибка: элемент не найден", Toast.LENGTH_SHORT).show()
                    }
                }

                rvContainer.setOnLongClickListener {
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Удаление коллекции")
                        .setMessage("Желаете удалить коллекцию")
                        .setPositiveButton("Да") { _, _ ->
                            val firebaseRef =
                                FirebaseDatabase.getInstance().getReference("Collections").child(auth.currentUser!!.uid)
                            //realtime database
                            firebaseRef.child(currentItem).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Коллекция успешно удалена",
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