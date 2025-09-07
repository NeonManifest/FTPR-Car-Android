package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapitest.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private val viewModel: CarViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CarAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()
        auth = FirebaseAuth.getInstance()
        recyclerView = binding.recyclerView
        swipeRefreshLayout = binding.swipeRefreshLayout
        setupActionBar()
        setupSwipeRefresh()
        setupView()
        observeViewModel()
        fetchItems()


        // 1- Criar tela de Login com algum provedor do Firebase (Telefone, Google)
        //      Cadastrar o Seguinte celular para login de test: +5511912345678
        //      Código de verificação: 101010

        // 2- Criar Opção de Logout no aplicativo

        // 3- Integrar API REST /car no aplicativo
        //      API será disponibilida no Github
        //      JSON Necessário para salvar e exibir no aplicativo
        //      O Image Url deve ser uma foto armazenada no Firebase Storage
        //      { "id": "001", "imageUrl":"https://image", "year":"2020/2020", "name":"Gaspar", "licence":"ABC-1234", "place": {"lat": 0, "long": 0} }

        // Opcionalmente trabalhar com o Google Maps ara enviar o place
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(false)
        supportActionBar?.title = getString(R.string.app_name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_custom_button -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupView() {
        adapter = CarAdapter { car ->
            val intent = Intent(this, CarDetails::class.java).apply {
                putExtra("CAR_ID", car.id)
                putExtra("CAR_IMAGE_URL", car.imageUrl)
                putExtra("CAR_YEAR", car.year)
                putExtra("CAR_NAME", car.name)
                putExtra("CAR_LICENCE", car.licence)
                putExtra("CAR_LAT", car.place.getLat())
                putExtra("CAR_LONG", car.place.getLong())
            }
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun requestLocationPermission() {
        // TODO
    }

    private fun fetchItems() {
        viewModel.fetchCars()
    }

    private fun observeViewModel() {
        viewModel.cars.observe(this) { cars ->
            cars?.let {
                adapter.submitList(it)
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSwipeRefresh(){
        swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
            swipeRefreshLayout.isRefreshing = false
        }
    }

}
