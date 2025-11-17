package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.FavoritesCarouselAdapter
import com.example.myapplication.adapters.SearchAdapter
import com.example.myapplication.models.FormulaX
import com.example.myapplication.models.SearchableItem
import com.example.myapplication.utils.DisciplinaJsonReader
import com.example.myapplication.utils.FavoritesManager
import com.example.myapplication.utils.RecentFormulasManager
import java.util.Locale

class HomeTab : Fragment() {
    data class PhrasePair(val phrase: String, val emoji: String)
    private val phrasePairs = listOf(
        PhrasePair("Pronto para aprender algo novo hoje?", "💡"),
        PhrasePair("Que tal uma dose de eletrônica?", "🔋"),
        PhrasePair("Seu próximo conhecimento te espera aqui.", "🚀"),
        PhrasePair("O mundo da eletrônica te chama!", "🤖"),
        PhrasePair("Vamos desvendar um mistério da eletrônica?", "📡"),
        PhrasePair("Qual conceito vamos dominar hoje?", "🧠"),
        PhrasePair("Mantenha a mente ligada na eletrônica!", "🔌"),
        PhrasePair("Hora de energizar seus conhecimentos!", "⚡"),
        PhrasePair("Conecte-se com o saber da eletrônica.", "📖"),
        PhrasePair("O tempo passa rápido quando a gente se diverte...", "🤣"),
        PhrasePair("Quem tem mais, tem 15!", "🏆"),
        PhrasePair("Preparado para acender ideias hoje?", "✨"),
        PhrasePair("Cada detalhe aprendido é uma nova conquista.", "🎯"),
        PhrasePair("A aventura da eletrônica nunca para!", "💪"),
        PhrasePair("Qual desafio técnico vamos superar agora?", "📝"),
        PhrasePair("Nunca desligue sua curiosidade!", "🕹️"),
        PhrasePair("A teoria conecta com a prática aqui.", "📚"),
        PhrasePair("Aprender também pode ser diversão!", "😄"),
    )
    private val searchableList = mutableListOf<SearchableItem>()
    private lateinit var searchAdapter: SearchAdapter
    private val disciplinaReader = DisciplinaJsonReader()
    private lateinit var rvFavoritesCarousel: RecyclerView
    private lateinit var tvFavoritesTitle: TextView
    private var allFormulas: List<FormulaX>? = null
    private lateinit var rvRecentsCarousel: RecyclerView
    private lateinit var tvRecentsTitle: TextView
    private lateinit var cardWelcomePrompt: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAllContentFromAssets()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGreetingMessage(view)

        setupSearch(view)
        setupFavoritesCarousel(view)
        setupRecentsCarousel(view)
        cardWelcomePrompt = view.findViewById(R.id.card_welcome_prompt)
    }

    override fun onResume() {
        super.onResume()
        view?.let { setupGreetingMessage(it) }
        displayFavorites()
        displayRecents()
    }
    private fun showEditUserNameDialog() {
        if (!isAdded) return

        // MODIFICAÇÃO: Aplicando o tema com bordas arredondadas
        val builder = AlertDialog.Builder(requireContext(), R.style.MyRounded_AlertDialog)

        builder.setTitle("Alterar nome de usuário")
        val input = EditText(requireContext())
        input.hint = "Digite seu nome"
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentName = sharedPreferences.getString("user_name", "")
        input.setText(currentName)
        builder.setView(input)
        builder.setPositiveButton("Salvar") { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                // Salva o novo nome
                sharedPreferences.edit().putString("user_name", newName).apply()
                // Atualiza a UI imediatamente
                view?.let { setupGreetingMessage(it) }
                Toast.makeText(requireContext(), "Nome atualizado!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        // Configura o botão "Cancelar"
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()

        // Desabilita o botão Salvar se o campo estiver vazio
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = input.text.toString().trim().isNotEmpty()

            input.addTextChangedListener { text ->
                positiveButton.isEnabled = text.toString().trim().isNotEmpty()
            }
        }

        dialog.show()
    }
    private fun updateEmptyStateUI() {
        val areFavoritesVisible = rvFavoritesCarousel.visibility == View.VISIBLE
        val areRecentsVisible = rvRecentsCarousel.visibility == View.VISIBLE

        if (!areFavoritesVisible && !areRecentsVisible) {
            cardWelcomePrompt.visibility = View.VISIBLE
        } else {
            cardWelcomePrompt.visibility = View.GONE
        }
    }
    private fun setupGreetingMessage(view: View) {
        val welcomeTextView = view.findViewById<TextView>(R.id.welcome_home_textview)
        val btnEditName = view.findViewById<ImageButton>(R.id.btn_edit_user_name) // Pega a referência do botão

        val sharedPreferences =
            requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "Usuário") ?: "Usuário"
        val randomPair = phrasePairs.random()
        val greetingPart = "Olá, $userName! ${randomPair.emoji}\n"
        val fullText = greetingPart + randomPair.phrase
        val spannableString = SpannableString(fullText)
        val sizeSpan = RelativeSizeSpan(0.8f)
        val startIndex = greetingPart.length
        val endIndex = fullText.length
        spannableString.setSpan(sizeSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        welcomeTextView.text = spannableString

        // Configura o clique do botão para chamar o diálogo
        btnEditName.setOnClickListener {
            showEditUserNameDialog()
        }
    }
    private fun setupSearch(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_results)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchAdapter = SearchAdapter(emptyList()) { clickedItem ->
            navigateToFormulas(clickedItem)
        }
        recyclerView.adapter = searchAdapter

        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterContent(newText)
                return true
            }
        })
    }

    private fun setupFavoritesCarousel(view: View) {
        tvFavoritesTitle = view.findViewById(R.id.tv_favorites_title)
        rvFavoritesCarousel = view.findViewById(R.id.rv_favorites_carousel)
        rvFavoritesCarousel.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupRecentsCarousel(view: View) {
        tvRecentsTitle = view.findViewById(R.id.tv_recents_title)
        rvRecentsCarousel = view.findViewById(R.id.rv_recents_carousel)
        rvRecentsCarousel.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun displayRecents() {
        if (!isAdded) return

        val recentFormulaIds = RecentFormulasManager.getRecentFormulas(requireContext())

        if (recentFormulaIds.isEmpty() || allFormulas == null) {
            tvRecentsTitle.visibility = View.GONE
            rvRecentsCarousel.visibility = View.GONE
            updateEmptyStateUI()
            return
        }

        val recentFormulas = recentFormulaIds.mapNotNull { formulaId ->
            allFormulas!!.find { it.getUniqueId() == formulaId }
        }

        if (recentFormulas.isNotEmpty()) {
            tvRecentsTitle.visibility = View.VISIBLE
            rvRecentsCarousel.visibility = View.VISIBLE

            val recentsAdapter = FavoritesCarouselAdapter(requireContext(), recentFormulas)
            rvRecentsCarousel.adapter = recentsAdapter
        } else {
            tvRecentsTitle.visibility = View.GONE
            rvRecentsCarousel.visibility = View.GONE
        }

        updateEmptyStateUI()
    }

    private fun displayFavorites() {
        if (!isAdded) return

        val favoriteIds = FavoritesManager.getFormulaFavorites(requireContext())

        Log.d("HomeTab_Favorites", "=== DEBUG FAVORITOS ===")
        Log.d("HomeTab_Favorites", "IDs salvos no SharedPreferences: $favoriteIds")

        if (favoriteIds.isEmpty()) {
            tvFavoritesTitle.visibility = View.GONE
            rvFavoritesCarousel.visibility = View.GONE
            Log.d("HomeTab_Favorites", "Nenhum favorito encontrado")
            updateEmptyStateUI()
            return
        }

        if (allFormulas == null) {
            Log.e("HomeTab_Display", "A lista 'allFormulas' ainda está nula.")
            updateEmptyStateUI()
            return
        }

        Log.d("HomeTab_Favorites", "--- Fórmulas Carregadas (primeiras 5) ---")
        allFormulas!!.take(5).forEach { formula ->
            Log.d("HomeTab_Favorites", "Nome: '${formula.name}', ID Único: '${formula.getUniqueId()}'")
        }

        val favoriteFormulaObjects = favoriteIds.mapNotNull { favoriteId ->
            allFormulas!!.find { formula ->
                formula.getUniqueId() == favoriteId
            }
        }.also { formulas ->
            Log.d("HomeTab_Favorites", "Total de favoritos filtrados (sem duplicatas): ${formulas.size}")
            formulas.forEach { formula ->
                Log.d("HomeTab_Favorites", "✓ MATCH: '${formula.name}' (ID: '${formula.getUniqueId()}')")
            }
        }

        Log.d("HomeTab_Favorites", "Total de favoritos filtrados: ${favoriteFormulaObjects.size}")

        if (favoriteFormulaObjects.isEmpty()) {
            tvFavoritesTitle.visibility = View.GONE
            rvFavoritesCarousel.visibility = View.GONE
            Log.w("HomeTab_Favorites", "Nenhuma fórmula correspondeu aos IDs salvos!")
            updateEmptyStateUI()
            return
        }

        tvFavoritesTitle.visibility = View.VISIBLE
        rvFavoritesCarousel.visibility = View.VISIBLE

        val carouselAdapter = FavoritesCarouselAdapter(requireContext(), favoriteFormulaObjects)
        rvFavoritesCarousel.adapter = carouselAdapter
        updateEmptyStateUI()
    }

    private fun loadAllContentFromAssets() {
        if (allFormulas != null) return

        val tempSearchableList = mutableListOf<SearchableItem>()
        val tempAllFormulas = mutableListOf<FormulaX>()

        try {
            val fileNames = requireContext().assets.list("")?.filter { it.endsWith(".json") }
            fileNames?.forEach { fileName ->
                val subject = disciplinaReader.loadDisciplina(requireContext(), fileName)
                if (subject?.formulas != null) {
                    subject.formulas.forEachIndexed { index, formula ->
                        formula.disciplinaOrigem = subject.name
                        formula.arquivoJsonOrigem = fileName
                        formula.indiceNoArray = index

                        tempAllFormulas.add(formula)

                        val searchText = (
                                (subject.name ?: "") + " " +
                                        formula.name + " " +
                                        (formula.description ?: "") + " " +
                                        (subject.tags?.joinToString(" ") ?: "") + " " +
                                        (subject.alias?.joinToString(" ") ?: "")
                                ).lowercase(Locale.ROOT)

                        tempSearchableList.add(
                            SearchableItem(
                                title = formula.name,
                                description = subject.name ?: "Disciplina Desconhecida",
                                searchText = searchText,
                                sourceFile = fileName
                            )
                        )
                    }
                }
            }

            allFormulas = tempAllFormulas
            searchableList.clear()
            searchableList.addAll(tempSearchableList)

            Log.d("HomeTab_Loader", "Carregou com sucesso ${allFormulas?.size} fórmulas.")

            allFormulas?.take(3)?.forEach { formula ->
                Log.d("HomeTab_Loader",
                    "Fórmula: '${formula.name}', Arquivo: '${formula.arquivoJsonOrigem}', " +
                            "Índice: ${formula.indiceNoArray}, ID: '${formula.getUniqueId()}'")
            }

        } catch (e: Exception) {
            Log.e("HomeTab_Loader", "Erro CRÍTICO ao carregar assets.", e)
        }
    }

    private fun filterContent(query: String?) {
        val resultsOverlay = view?.findViewById<CardView>(R.id.results_overlay_container)

        if (query.isNullOrBlank()) {
            searchAdapter.updateList(emptyList())
            resultsOverlay?.visibility = View.GONE
            return
        }

        val filteredList = searchableList.filter { item ->
            item.searchText.contains(query.lowercase(Locale.ROOT))
        }

        searchAdapter.updateList(filteredList)
        resultsOverlay?.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun navigateToFormulas(item: SearchableItem) {
        try {

            val intent = Intent(requireContext(), FormulasActivity::class.java).apply {
                putExtra("disciplina_arquivo_json", item.sourceFile)
                putExtra("disciplina_nome", item.description)
                putExtra("formula_nome_foco", item.title)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Não foi possível abrir a disciplina.", Toast.LENGTH_SHORT).show()
        }
    }
}
