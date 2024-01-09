package com.example.menu.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menu.R
import com.example.menu.adapter.RvCollectionsAdapter
import com.example.menu.databinding.FragmentCollectionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class CollectionsFragment : Fragment() {
    private var _binding : FragmentCollectionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseRef: DatabaseReference
    private lateinit var collectionsList: ArrayList<String>

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCollectionsBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firebaseRef = FirebaseDatabase.getInstance().getReference("Collections").child(auth.currentUser!!.uid)
        collectionsList = arrayListOf()

        binding.rvCollections.layoutManager = LinearLayoutManager(context)

        fetchData()

        binding.btnToCreate.setOnClickListener {
            findNavController().navigate(R.id.createCallectionFragment)
        }

        return binding.root
    }

    private fun fetchData(){
        firebaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                collectionsList.clear()
                if(snapshot.exists()){
                    for(collectionSnap in snapshot.children){
                        val collectionName = collectionSnap.key
                        collectionsList.add(collectionName!!)
                    }
                }
                val rvAdapter = RvCollectionsAdapter(collectionsList)
                binding.rvCollections.adapter = rvAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Ошибка: ${error.message}",Toast.LENGTH_LONG).show()
            }
        })
    }
}