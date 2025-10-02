package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.SubjectsButtonAdapter
import com.example.myapplication.models.Subjects
import com.example.myapplication.utils.DisciplinaJsonReader

class SubjectsTab : Fragment() {

    // Views do layout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var progressLoading: ProgressBar

    // Componentes para gerenciar disciplinas
    private lateinit var disciplinaReader: DisciplinaJsonReader
    private lateinit var adapter: SubjectsButtonAdapter
    private var disciplinas: List<Subjects> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout XML para este fragmento
        return inflater.inflate(R.layout.tab_subjects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar componentes
        initializeViews(view)
        setupRecyclerView()
        setupDisciplinaReader()

        // Carregar disciplinas
        loadDisciplinas()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_disciplinas)
        emptyStateLayout = view.findViewById(R.id.layout_empty_state)
        progressLoading = view.findViewById(R.id.progress_loading)
    }

    private fun setupRecyclerView() {
        // Configurar adapter com callback para cliques nos botões
        adapter = SubjectsButtonAdapter(disciplinas) { disciplina ->
            onDisciplinaButtonClick(disciplina)
        }

        // Configurar RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupDisciplinaReader() {
        disciplinaReader = DisciplinaJsonReader()
    }

    private fun loadDisciplinas() {
        Log.d("SUBJECTS_TAB", "Iniciando carregamento de disciplinas...")

        // Mostrar loading
        showLoading(true)

        try {
            // Lista com todos os arquivos JSON de disciplinas na pasta assets
            val jsonFiles = listOf(
                "analise-de-circuitos-i.json",
                "analise-de-circuitos-ii.json",
                "analise-de-circuitos-iii.json",
                "analise-de-circuitos-iv.json",
                "eletricidade-i.json",
                "eletricidade-ii.json",
                "eletronica-digital-iv.json",
                "eletronica-digital-v.json",
                "eletronica-geral-i.json",
                "eletronica-geral-iii.json",
                "eletronica-geral-v.json",
                // Adicione aqui outros arquivos conforme você tiver:
                // "eletronica-geral-iv.json",
            )

            Log.d("SUBJECTS_TAB", "Tentando carregar ${jsonFiles.size} arquivo(s)...")

            // Carregar disciplinas dos JSONs
            disciplinas = disciplinaReader.loadAllDisciplinas(requireContext(), jsonFiles)

            Log.d("SUBJECTS_TAB", "Carregadas ${disciplinas.size} disciplina(s) com sucesso!")

            // Atualizar UI
            showLoading(false)

            if (disciplinas.isNotEmpty()) {
                // Mostrar lista de disciplinas
                adapter.updateDisciplinas(disciplinas)
                showDisciplinas()

                // Log detalhado das disciplinas carregadas
                disciplinas.forEachIndexed { index, disciplina ->
                    Log.d("SUBJECTS_TAB",
                        "${index + 1}. ${disciplina.name} | " +
                                "${disciplina.semmester}° sem | " +
                                "${disciplina.status} | " +
                                "${disciplina.formulas?.size} fórmulas")
                }

            } else {
                // Mostrar estado vazio
                showEmptyState()
                Log.w("SUBJECTS_TAB", "Nenhuma disciplina foi carregada")
            }

        } catch (e: Exception) {
            Log.e("SUBJECTS_TAB", "Erro ao carregar disciplinas: ${e.message}", e)
            showLoading(false)
            showError("Erro ao carregar disciplinas: ${e.message}")
        }
    }

    private fun onDisciplinaButtonClick(disciplina: Subjects) {
        Log.d("DISCIPLINA_CLICK", "Clicou na disciplina: ${disciplina.name}")

        try {
            // --- A CORREÇÃO ESTÁ AQUI ---
            // Constrói o nome do arquivo a partir do slug (ex: "analise-de-circuitos-i" -> "analise-de-circuitos-i.json")
            val fileName = "${disciplina.slug}.json"

            // Cria o intent explícito para a FormulasActivity
            val intent = Intent(requireContext(), FormulasActivity::class.java).apply {
                // Passa o nome do arquivo JSON, que é o método unificado de carregamento
                putExtra("disciplina_arquivo_json", fileName)
                // Passa o nome da disciplina para ser usado como título na próxima tela
                putExtra("disciplina_nome", disciplina.name)
            }
            startActivity(intent)

        } catch (e: Exception) {
            Log.e("DISCIPLINA_CLICK", "Erro ao abrir FormulasActivity: ${e.message}", e)
            Toast.makeText(requireContext(), "Erro ao abrir fórmulas: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showLoading(show: Boolean) {
        progressLoading.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }

    private fun showDisciplinas() {
        recyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        progressLoading.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        progressLoading.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        showEmptyState()
    }

    /**
     * Função pública para recarregar disciplinas (pode ser chamada de zfora)
     */
    fun refreshDisciplinas() {
        loadDisciplinas()
    }

    /**
     * Função para obter disciplina por slug (útil para outras partes do app)
     */
    fun getDisciplinaBySlug(slug: String): Subjects? {
        return disciplinas.find { it.slug == slug }
    }
}