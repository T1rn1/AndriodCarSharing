package com.example.menu.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.menu.databinding.RvCollectionsItemChooseBinding
import com.example.menu.models.Books
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.ArrayList

class RvCollectionChooseAdapter(private val bookList: java.util.ArrayList<Books>) : RecyclerView.Adapter<RvCollectionChooseAdapter.ViewHolder>() {

    private var checkedBooks = ArrayList<Books>()

    class ViewHolder(val binding: RvCollectionsItemChooseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvCollectionsItemChooseBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
                cbChoose.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        checkedBooks.add(currentItem)
                    } else {
                        checkedBooks.remove(currentItem)
                    }
                }
            }
        }
    }

    fun getCheckedBooks() : ArrayList<Books>{
        return checkedBooks
    }
}