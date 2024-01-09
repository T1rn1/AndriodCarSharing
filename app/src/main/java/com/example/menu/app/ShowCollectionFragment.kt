package com.example.menu.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menu.R
import com.example.menu.adapter.RvShowCollectionsAdapter
import com.example.menu.databinding.FragmentShowCollectionBinding
import com.example.menu.models.Books
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowCollectionFragment : Fragment() {

    private var _binding : FragmentShowCollectionBinding? = null
    private val binding get() = _binding!!

    private val args: ShowCollectionFragmentArgs by navArgs()
    private val auth = FirebaseAuth.getInstance()
    private val firebaseRef = FirebaseDatabase.getInstance().getReference("Collections").child(auth.currentUser!!.uid)
    private lateinit var booksList: ArrayList<Books>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowCollectionBinding.inflate(inflater, container, false)

        binding.rvShow.layoutManager = LinearLayoutManager(context)
        booksList = arrayListOf()

        fetchData()

        binding.btnToUpdateCollection.setOnClickListener {
            val action =
                ShowCollectionFragmentDirections.actionShowCollectionFragmentToCollectionUpdateFragment(
                    args.title
                )
            findNavController().navigate(action)
        }

        binding.btnShowToBack.setOnClickListener {
            findNavController().navigate(R.id.collectionsFragment)
        }

        return binding.root
    }

    private fun fetchData(){
        firebaseRef.child(args.title).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                booksList.clear()
                if(snapshot.exists()){
                    for(bookSnap in snapshot.children){
                        val books = bookSnap.getValue(Books::class.java)
                        booksList.add(books!!)
                    }
                }
                val rvAdapter = RvShowCollectionsAdapter(booksList, args.title)
                binding.rvShow.adapter = rvAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Ошибка: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}