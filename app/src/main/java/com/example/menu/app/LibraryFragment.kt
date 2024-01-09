package com.example.menu.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menu.R
import com.example.menu.adapter.RvBooksAdapter
import com.example.menu.databinding.FragmentLibraryBinding
import com.example.menu.models.Books
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class LibraryFragment : Fragment() {
    private var _binding : FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var booksList: ArrayList<Books>
    private lateinit var firebaseRef: DatabaseReference

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)

        firebaseRef = FirebaseDatabase.getInstance().getReference("Books")
        booksList = arrayListOf()

        auth = FirebaseAuth.getInstance()

        binding.rvBooks.layoutManager = LinearLayoutManager(context)

        fetchData()

        if(auth.currentUser?.email == "admin1@gmail.com"){
            binding.btnToAdd.visibility = View.VISIBLE
        } else {
            binding.btnToAdd.visibility = View.INVISIBLE
        }


        binding.btnToAdd.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_addFragment)
        }

        binding.edtSerch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                filterBooks(s.toString())
            }
        })
        return binding.root
    }

    private fun fetchData(){
        firebaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                booksList.clear()
                if(snapshot.exists()){
                    for(bookSnap in snapshot.children){
                        val books = bookSnap.getValue(Books::class.java)
                        booksList.add(books!!)
                    }
                }
                val rvAdapter = RvBooksAdapter(booksList)
                binding.rvBooks.adapter = rvAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Ошибка: ${error.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun filterBooks(query: String) {
        if(query.isEmpty()){
            val rvAdapter = RvBooksAdapter(booksList)
            binding.rvBooks.adapter = rvAdapter
        } else {
            val filteredList = ArrayList<Books>()
            for (book in booksList) {
                if (book.title!!.toLowerCase().contains(query.toLowerCase()) || book.author!!.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(book)
                }
            }
            val rvAdapter = RvBooksAdapter(filteredList)
            binding.rvBooks.adapter = rvAdapter
        }
    }
}