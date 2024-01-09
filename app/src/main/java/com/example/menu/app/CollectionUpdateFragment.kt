package com.example.menu.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menu.R
import com.example.menu.adapter.RvCollectionChooseAdapter
import com.example.menu.databinding.FragmentCollectionUpdateBinding
import com.example.menu.models.Books
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class CollectionUpdateFragment : Fragment() {

    private var _binding : FragmentCollectionUpdateBinding? = null
    private val binding get() = _binding!!

    private lateinit var booksList: ArrayList<Books>
    private lateinit var firebaseRefBooks: DatabaseReference
    private lateinit var firebaseRefCollections: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var rvAdapter: RvCollectionChooseAdapter
    private val args: CollectionUpdateFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCollectionUpdateBinding.inflate(inflater, container, false)

        firebaseRefBooks = FirebaseDatabase.getInstance().getReference("Books")
        firebaseRefCollections = FirebaseDatabase.getInstance().getReference("Collections")

        auth = FirebaseAuth.getInstance()
        booksList = arrayListOf()
        rvAdapter = RvCollectionChooseAdapter(booksList)

        binding.rvBooks.layoutManager = LinearLayoutManager(context)
        binding.rvBooks.adapter = rvAdapter

        fetchData()

        binding.btnCreateBack.setOnClickListener {
            findNavController().navigate(R.id.collectionsFragment)
        }

        binding.btnCreateCollection.setOnClickListener {
            createCollection()
        }

        return binding.root
    }

    private fun createCollection() {
        val checkedBooks = rvAdapter.getCheckedBooks()
        for (book in checkedBooks) {
            firebaseRefCollections.child(auth.currentUser!!.uid).child(args.title)
                .child(book.id.toString()).setValue(book)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Коллекция успешно создана",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.collectionsFragment)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun fetchData(){
        firebaseRefBooks.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                booksList.clear()
                if(snapshot.exists()){
                    for(bookSnap in snapshot.children){
                        val books = bookSnap.getValue(Books::class.java)
                        if (books != null) {
                            booksList.add(books)
                        }
                    }
                }
                rvAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Ошибка: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}