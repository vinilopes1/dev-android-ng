package com.example.devandroidng.app

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.devandroidng.R
import com.example.devandroidng.infra.APIService
import com.example.devandroidng.infra.endpoints.PokemonEndPoint
import com.example.devandroidng.infra.endpoints.ResultEndPoint
import com.example.devandroidng.models.ItemResult
import com.example.devandroidng.models.Pokemon
import com.example.devandroidng.models.Result
import com.example.devandroidng.adapters.PokedexAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PokedexActivity : AppCompatActivity() , PokedexAdapter.ItemClickListener{
    private lateinit var pokedexAdapter: PokedexAdapter
    private lateinit var itemSelected: Pokemon
    var listPokedex: MutableList<Pokemon> = ArrayList()
    private lateinit var progressDialog: ProgressDialog


    val db = Firebase.firestore
    val user = Firebase.auth.currentUser



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokedex)
        initComponents()
    }

    private fun initComponents(){
        progressDialog = initProgressDialog()
        progressDialog.show()
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.my_toolbar)
        val color = resources.getColor(R.color.white)
        val upArrow = getResources().getDrawable(R.drawable.ic_arrow_back);
        toolbar.setTitleTextColor(color)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(upArrow)
        setTitle("Pokedex")
        getPokemons()

    }

    fun bindingComponents(){
        val tvName: TextView = findViewById(R.id.tv_name_pokedex)
        val tvWeight: TextView = findViewById(R.id.tv_weight_pokedex)
        val tvHeight: TextView = findViewById(R.id.tv_height_pokedex)
        val tvExperience: TextView = findViewById(R.id.tv_experience_pokedex)
        val imageView: ImageView = findViewById(R.id.img_poke_detail)
        Picasso.get()
            .load(itemSelected.images.avatar_image)
            .into(imageView)
        tvName.text = itemSelected.name[0].toUpperCase()+itemSelected.name.substring(1)
        tvWeight.text = itemSelected.weight.toString()
        tvHeight.text = itemSelected.height.toString()
        tvExperience.text = itemSelected.experience.toString()
        val btnDrop: Button = findViewById(R.id.btn_drop_pokedex)
        btnDrop.setOnClickListener{
            val pokeRef = db.collection("pokemons").document(itemSelected.id.toString())
            pokeRef
                .update("captured", false, "owner", "")
                .addOnSuccessListener {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    Toast.makeText(baseContext, "Pokemon dropped!", Toast.LENGTH_LONG).show()
                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
        }
    }

    fun bindingComponentsNone(){
        val tvName: TextView = findViewById(R.id.tv_name_pokedex)
        val btnDrop: Button = findViewById(R.id.btn_drop_pokedex)
        val tvWeight: TextView = findViewById(R.id.tv_weight_label)
        val tvHeight: TextView = findViewById(R.id.tv_height_label)
        val tvExperience: TextView = findViewById(R.id.tv_experience_label)
        tvHeight.text = "Sem pokemons por aqui."
        tvName.visibility = View.INVISIBLE
        btnDrop.visibility = View.INVISIBLE
        tvWeight.visibility = View.INVISIBLE
        tvExperience.visibility = View.INVISIBLE
    }

    private fun showListPokedex() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_my_pokemons)
        pokedexAdapter = PokedexAdapter(applicationContext, listPokedex, this)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = pokedexAdapter
    }

    fun getPokemons() {
        val list: MutableList<ItemResult> = ArrayList()
        val retrofitClient = APIService
            .getRetrofitInstance("https://pokeapi.co/api/v2/")

        val endpoint = retrofitClient.create(ResultEndPoint::class.java)
        val callback = endpoint.getPokemonResults()

        callback.enqueue(object : Callback<Result> {
            override fun onFailure(call: Call<Result>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                db.collection("pokemons")
                    .whereEqualTo("owner", user!!.email)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            response.body()!!.results.forEach {
                                if((document.data["name"] == it.name)){
                                    list.add((it))
                                }
                            }
                        }
                        getPokemonDetail(list)
                    }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "Error getting documents: ", exception)
                    }
            }
        })
    }

    fun getPokemonDetail(listResults: MutableList<ItemResult>){
        if(listResults.isEmpty()){
            bindingComponentsNone()
        }else {
            listResults.forEach {

                val strs = it.url.split("https://pokeapi.co/api/v2/").toTypedArray()
                val retrofitClient = APIService
                    .getRetrofitInstance("https://pokeapi.co/api/v2/")

                val endpoint = retrofitClient.create(PokemonEndPoint::class.java)
                val callback = endpoint.getPokemon(strs[1])

                callback.enqueue(object : Callback<Pokemon> {
                    override fun onFailure(call: Call<Pokemon>, t: Throwable) {
                        Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                        createListPokedex(response.body()!!)

                    }
                })
            }
        }
        progressDialog.hide()

    }

    fun createListPokedex(pokemon: Pokemon){
        listPokedex.add(pokemon)
        itemSelected = listPokedex[0]
        showListPokedex()
        bindingComponents()
    }

    private fun initProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Aguarde...")
        return progressDialog
    }

    override fun onItemClick(position: Int) {
        itemSelected = listPokedex[position]
        showListPokedex()
        pokedexAdapter.notifyDataSetChanged()
        bindingComponents()
    }

    override fun onLongClick(position: Int) {
        TODO("Not yet implemented")
    }
}