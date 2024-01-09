package com.example.menu.app

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.menu.R
import com.example.menu.adapter.MySpinnerAdapter
import com.example.menu.databinding.FragmentAddBinding
import com.example.menu.models.Books
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {
    private var _binding : FragmentAddBinding? = null
    private val binding get() = _binding!!

    private var firebaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Books")
    private var storageRef: StorageReference = FirebaseStorage.getInstance().getReference("Images")

    private var uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        binding.btnAdd.setOnClickListener{
            addData()
        }

        val myAdapter = MySpinnerAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(
            R.array.book_genres
        ))
        binding.spinner.adapter = myAdapter

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            binding.imgAdd.setImageURI(it)
            if(it != null) {
                uri = it
            }
        }

        binding.btnAddBack.setOnClickListener {
            findNavController().navigate(R.id.libraryFragment)
        }

        binding.btnPickImage.setOnClickListener{
            pickImage.launch("image/*")
        }

        return binding.root
    }

    private fun addData() {
        val title = binding.edtAddTitle.text.toString()
        val author = binding.edtAddAuthor.text.toString()
        val description = binding.edtAddDescription.text.toString()
        val location = binding.edtAddLocation.text.toString()
        val genre = binding.spinner.selectedItem.toString()

        if(title.isEmpty()) binding.edtAddTitle.error = "Введите название"
        if(author.isEmpty()) binding.edtAddAuthor.error = "Введите Автора"
        if(description.isEmpty()) binding.edtAddDescription.error = "Введите описание"
        if(location.isEmpty()) binding.edtAddLocation.error = "Введите место"
        if(genre == resources.getStringArray(R.array.book_genres)[0]) Toast.makeText(context,"Выберите жанр", Toast.LENGTH_SHORT).show()

        if(title.isNotEmpty() && author.isNotEmpty() && description.isNotEmpty() && location.isNotEmpty()) {
            val bookId = firebaseRef.push().key!!

            if(uri == null){
                Toast.makeText(context, "Необходимо выбрать фото", Toast.LENGTH_SHORT).show()
            }else {
                uri?.let {
                    storageRef.child(bookId).putFile(it)
                        .addOnSuccessListener { task ->
                            task.metadata!!.reference!!.downloadUrl
                                .addOnSuccessListener { uri ->
                                    val imgUri = uri.toString()

                                    val book = Books(bookId, title, author, imgUri, genre, description, location)

                                    firebaseRef.child(bookId).setValue(book)
                                        .addOnCompleteListener {
                                            Toast.makeText(
                                                context,
                                                "Книга успешно сохранена",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            findNavController().navigate(R.id.libraryFragment)
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Ошибка ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                        }
                }
            }
        }
    }
}