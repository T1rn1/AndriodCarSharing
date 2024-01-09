package com.example.menu.app

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.menu.R
import com.example.menu.adapter.MySpinnerAdapter
import com.example.menu.databinding.FragmentLibraryBinding
import com.example.menu.databinding.FragmentUpdateBinding
import com.example.menu.models.Books
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class UpdateFragment : Fragment() {
    private var _binding : FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val args: UpdateFragmentArgs by navArgs()

    private var uri: Uri? = null
    private var imageUri: String? = null

    private lateinit var firebaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)

        firebaseRef = FirebaseDatabase.getInstance().getReference("Books")
        storageRef = FirebaseStorage.getInstance().getReference("Images")

        val myAdapter = MySpinnerAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(
            R.array.book_genres
        ))
        binding.spinner.adapter = myAdapter

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser?.email == "admin1@gmail.com"){
            binding.btnPickImage.visibility = View.VISIBLE
            binding.btnUpdate.visibility = View.VISIBLE
        } else {
            binding.btnPickImage.visibility = View.INVISIBLE
            binding.btnUpdate.visibility = View.INVISIBLE
        }


        binding.btnUpdateBack.setOnClickListener {
            findNavController().navigate(R.id.libraryFragment)
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            binding.imgUpdate.setImageURI(it)
            if(it != null) {
                uri = it
            }
        }

        binding.apply {
            edtUpdateTitle.setText(args.title)
            edtUpdateAuthor.setText(args.author)
            edtUpdateDescription.setText(args.description)
            edtUpdateLocation.setText(args.location)
            spinner.setSelection(resources.getStringArray(R.array.book_genres).indexOf(args.genre))
            Picasso.get().load(args.imageUri).into(imgUpdate)


            btnUpdate.setOnClickListener{
                updatedata()
            }

            btnPickImage.setOnClickListener{
                context?.let { context ->
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Изменение фото")
                        .setMessage("Желаете изменить фото?")
                        .setPositiveButton("Изменить фото"){_,_ ->
                            pickImage.launch("image/*")
                        }
                        .setNegativeButton("Отмена"){_,_ ->

                        }.show()
                }
            }
        }

        return binding.root
    }

    private fun updatedata(){
        val title = binding.edtUpdateTitle.text.toString()
        val author = binding.edtUpdateAuthor.text.toString()
        val description = binding.edtUpdateDescription.text.toString()
        val location = binding.edtUpdateLocation.text.toString()
        val genre = binding.spinner.selectedItem.toString()

        if(title.isEmpty()) binding.edtUpdateTitle.error = "Введите название"
        if(author.isEmpty()) binding.edtUpdateAuthor.error = "Введите автора"
        if(description.isEmpty()) binding.edtUpdateDescription.error = "Введите описание"
        if(location.isEmpty()) binding.edtUpdateLocation.error = "Введите место"
        if(genre == resources.getStringArray(R.array.book_genres)[0]) Toast.makeText(context,"Выберите жанр", Toast.LENGTH_SHORT).show()

        if(title.isNotEmpty() && author.isNotEmpty() && description.isNotEmpty() && location.isNotEmpty() && genre != resources.getStringArray(
                R.array.book_genres
            )[0]) {

            if(uri != null){
                storageRef.child(args.id).putFile(uri!!)
                    .addOnSuccessListener {task ->
                        task.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri ->
                                imageUri = uri.toString()
                                    val book = Books(args.id,title,author,imageUri,genre,description,location)
                                    firebaseRef.child(args.id).setValue(book)
                                    .addOnCompleteListener {
                                        Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.libraryFragment)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Ошибка ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                    }
            }else if(uri == null){
                val book = Books(args.id,title,author,args.imageUri,genre, description,location)
                firebaseRef.child(args.id).setValue(book)
                    .addOnCompleteListener {
                        Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.libraryFragment)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Ошибка ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}