package com.example.menu.app

import  android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.menu.R
import com.example.menu.databinding.ActivityMainBinding
import com.example.menu.databinding.NavHeaderBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var headerBinding: NavHeaderBinding
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
        headerBinding.userPhoto.setImageURI(it)
        if(it != null) {
            uri = it
            savePhoto()
        }
    }

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().getReference("Avatars")
        databaseRef = FirebaseDatabase.getInstance().getReference("Avatars")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        headerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0))

        drawerLayout = binding.drawerLayout

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        navController = findNavController(R.id.fragmentContainerView)

        val navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, 0, 0)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if(savedInstanceState == null){
            navController.navigate(R.id.libraryFragment)
            navigationView.setCheckedItem(R.id.nav_library)
        }
    }

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_library -> navController.navigate(R.id.libraryFragment)
            R.id.nav_collections -> navController.navigate(R.id.collectionsFragment)
            R.id.nav_info -> navController.navigate(R.id.infoFragment)
            R.id.nav_logout -> logout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        headerBinding.userEmail.text = updateData()

        loadUserAvatar()

        headerBinding.newImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        headerBinding.userPhoto.setOnLongClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun updateData(): String{
        return "${auth.currentUser?.email}"
    }

    private fun savePhoto(){
        uri?.let {
            storageRef.child(auth.currentUser!!.uid).putFile(it)
                .addOnSuccessListener { task ->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            val imgUri = uri.toString()
                            databaseRef.child(auth.currentUser!!.uid).setValue(imgUri)
                                .addOnCompleteListener {
                                    Toast.makeText(
                                        this,
                                        "Фото успешно добавлено",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Ошибка ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                }
        }

    }

    private fun loadUserAvatar() {
        if (auth.currentUser != null) {
            databaseRef.child(auth.currentUser!!.uid).get().addOnSuccessListener { snapshot ->
                val avatarUrl = snapshot.getValue(String::class.java)
                if (avatarUrl != null) {
                    Picasso.get()
                        .load(avatarUrl)
                        .into(headerBinding.userPhoto)
                }
            }
        }
    }

    private fun deletePhotoAndReset(): Boolean {
        val user = auth.currentUser
        if (user != null) {
            // Удаление изображения из Firebase Storage
            storageRef.child(user.uid).delete()
                .addOnSuccessListener {
                    // Удаление ссылки на изображение из Realtime Database
                    databaseRef.child(user.uid).removeValue()
                        .addOnSuccessListener {
                            // Установка дефолтного изображения
                            headerBinding.userPhoto.setImageResource(R.drawable.baseline_account_circle_24)
                            Toast.makeText(this, "Фото успешно удалено", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Ошибка удаления из базы данных: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Ошибка удаления из хранилища: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
        return true
    }

    private fun showDeleteConfirmationDialog(): Boolean {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Удалить фотографию")
        builder.setMessage("Вы уверены, что хотите удалить эту фотографию?")

        builder.setPositiveButton("Удалить") { dialog, which ->
            // Вызовите здесь ваш метод для удаления фотографии
            deletePhotoAndReset()
        }

        builder.setNegativeButton("Отмена") { dialog, which ->
            // Закрыть диалоговое окно
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
        return true
    }
}