package com.example.devandroidng.app

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.devandroidng.R
import com.example.devandroidng.infra.APIService
import com.example.devandroidng.infra.endpoints.PokemonEndPoint
import com.example.devandroidng.infra.endpoints.ResultEndPoint
import com.example.devandroidng.models.*
import com.example.devandroidng.adapters.PokemonAdapter
import com.example.devandroidng.adapters.TypeAdapter
import com.firebase.ui.auth.AuthUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity(), TypeAdapter.ItemClickListener{

    private lateinit var pokemonAdapter: PokemonAdapter
    private lateinit var typeAdapter: TypeAdapter
    private lateinit var progressDialog: ProgressDialog
    private var orderAlphabetic: Boolean = true
    private var itemSelected: String = "all"
    var listFiltered : MutableList<Pokemon> = ArrayList()
    var listAllPokemons : MutableList<Pokemon> = ArrayList()
    var listTypeSelected: MutableList<Type> = ArrayList()
    var listPokemonStatus: MutableList<String> = ArrayList()
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initComponents()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        val menuItem = menu!!.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchPokemons(newText)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initComponents(){
        progressDialog = initProgressDialog()
        progressDialog.show()
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(this, PokedexActivity::class.java)
            startActivity(intent)
        }
        val llOrder: LinearLayout = findViewById(R.id.ll_order)
        llOrder.setOnClickListener {
            if(orderAlphabetic){
                listFiltered.sortWith(compareByDescending { it.name })
                orderAlphabetic = false
                pokemonAdapter.notifyDataSetChanged()
            }else{
                listFiltered.sortWith(compareBy { it.name })
                orderAlphabetic = true
                pokemonAdapter.notifyDataSetChanged()
            }

        }
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.my_toolbar)
        val color = resources.getColor(R.color.white)
        toolbar.setTitleTextColor(color)
        setSupportActionBar(toolbar)
        setTitle("Poke APP")
        getTypes()
        getPokemons()

    }

    private fun showListType() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        for(i in 0 until listTypeSelected.size-1){
            if(listTypeSelected.get(i).name == itemSelected){
                val item = listTypeSelected.get(i)
                listTypeSelected.removeAt(i)
                listTypeSelected.add(0,item)
            }
        }
        typeAdapter = TypeAdapter(applicationContext, listTypeSelected, this)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = typeAdapter
    }

    private fun showListPokemon(list: MutableList<Pokemon>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_pokemon)

        listFiltered = list
        listFiltered.sortWith(compareBy({it.name}))

        pokemonAdapter = PokemonAdapter(applicationContext, listFiltered, this)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = pokemonAdapter

    }

    fun changeListPokemon(){
        val listAux: MutableList<Pokemon> = ArrayList()

        if(itemSelected == "all"){
            listFiltered = listAllPokemons
        }else{
            listAllPokemons.forEach {
                it.types.forEach {element ->
                    if(element.type.name == itemSelected){
                        listAux.add(it)
                    }
                }
            }
            listFiltered = listAux
        }
        if(listFiltered.size == 0){
            val llWarn: LinearLayout = findViewById(R.id.ll_warn)
            llWarn.visibility = View.VISIBLE
        }

        listFiltered.sortWith(compareBy({it.name}))
        orderAlphabetic = true
        pokemonAdapter.notifyDataSetChanged()
        showListType()
        showListPokemon(listFiltered)
        typeAdapter.notifyDataSetChanged()

    }

    fun searchPokemons(text: String){
        listFiltered.removeAll(listFiltered)
        listAllPokemons.forEach {
            it.types.forEach { element ->
                if(element.type.name == itemSelected && it.name.toLowerCase().contains(text.toLowerCase())) {
                    listFiltered.add(it)
                }
            }
        }
        listFiltered.sortWith(compareBy({it.name}))
        orderAlphabetic = true
        pokemonAdapter.notifyDataSetChanged()
        showListType()
        typeAdapter.notifyDataSetChanged()
    }

    fun getTypes() {
        val retrofitClient = APIService
            .getRetrofitInstance("https://pokeapi.co/api/v2/")

        val endpoint = retrofitClient.create(ResultEndPoint::class.java)
        val callback = endpoint.getTypeResults()

        callback.enqueue(object : Callback<Result> {
            override fun onFailure(call: Call<Result>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                createListType(response.body()!!.results.toMutableList())
            }
        })
        createTypeAll(Type(name = "all"))
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
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            response.body()!!.results.forEach {
                                if((document.data["name"] == it.name) and (document.data["captured"] == false)){
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
                    createListPokemons(response.body()!!)

                }
            })
        }
        progressDialog.hide()
    }

    fun createListPokemons(poke: Pokemon){
        this.listAllPokemons.add(poke);
        showListPokemon(listAllPokemons)
    }

    fun createListType(listResults: MutableList<ItemResult>){
        listResults.forEach {
            var type = Type(name = it.name)
            this.listTypeSelected.add(type)
        }
        showListType()
    }

    fun createTypeAll(type: Type){
       this.listTypeSelected.add(type)
    }

    private fun initProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Aguarde...")
        return progressDialog
    }

    override fun onItemClick(position: Int) {
        itemSelected = listTypeSelected[position].name
        changeListPokemon()
    }

    override fun onLongClick(position: Int) {
        TODO("Not yet implemented")
    }

}