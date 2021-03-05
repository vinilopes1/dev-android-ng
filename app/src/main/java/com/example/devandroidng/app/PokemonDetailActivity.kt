package com.example.devandroidng.app

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.devandroidng.R
import com.example.devandroidng.infra.APIService
import com.example.devandroidng.infra.endpoints.PokemonEndPoint
import com.example.devandroidng.models.Pokemon
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PokemonDetailActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_detail)
        initComponents()
    }

    private fun initComponents(){
        progressDialog = initProgressDialog()
        progressDialog.show()
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.my_toolbar)
        val color = resources.getColor(R.color.white)
        val upArrow = getResources().getDrawable(R.drawable.ic_arrow_back);
        toolbar.setTitleTextColor(color)
        setTitle("Pokemon Detail")
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(upArrow)
        getPokemonDetail()

    }

    fun getPokemonDetail(){

        val retrofitClient = APIService
            .getRetrofitInstance("https://pokeapi.co/api/v2/")

        val endpoint = retrofitClient.create(PokemonEndPoint::class.java)
        val callback = endpoint.getPokemonDetail(intent.getStringExtra("pokemonId")!!)

        callback.enqueue(object : Callback<Pokemon> {
            override fun onFailure(call: Call<Pokemon>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                showPokemon(response.body()!!)
            }
        })
    }

    fun showPokemon(pokemon: Pokemon){
        val db = Firebase.firestore
        val user = Firebase.auth.currentUser
        val imageView: ImageView = findViewById(R.id.img_poke_detail)
        Picasso.get()
            .load(pokemon.images.avatar_image)
            .into(imageView)
        val btnCapture: Button = findViewById(R.id.btn_capture_detail)
        btnCapture.setOnClickListener {
            val pokeRef = db.collection("pokemons").document(intent.getStringExtra("pokemonId")!!)
            pokeRef
                .update("captured", true, "owner", user!!.email)
                .addOnSuccessListener {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    Toast.makeText(baseContext, "Pokemon captured!", Toast.LENGTH_LONG).show()
                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
        }
        bindComponents(pokemon)
        progressDialog.hide()
    }

    private fun bindComponents(pokemon: Pokemon){
        val tvName: TextView = findViewById(R.id.tv_name_detail)
        val tvWeight: TextView = findViewById(R.id.tv_weight_detail)
        val tvHeight: TextView = findViewById(R.id.tv_height_detail)
        val tvExperience: TextView = findViewById(R.id.tv_experience_detail)
        tvName.text = pokemon.name[0].toUpperCase()+pokemon.name.substring(1)
        tvWeight.text = pokemon.weight.toString()
        tvHeight.text = pokemon.height.toString()
        tvExperience.text = pokemon.experience.toString()
    }

    private fun initProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Aguarde...")
        return progressDialog
    }
}