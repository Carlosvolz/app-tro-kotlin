package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.FormulasAdapter
import com.example.myapplication.models.FormulaX
import com.example.myapplication.utils.DisciplinaJsonReader

class FormulasActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTituloFormulas: TextView
    private lateinit var tvSubtituloFormulas: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var formulasAdapter: FormulasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulas)

        // Inicializar views
        btnBack = findViewById(R.id.btn_back)
        tvTituloFormulas = findViewById(R.id.tv_titulo_formulas)
        tvSubtituloFormulas = findViewById(R.id.tv_subtitulo_formulas)
        recyclerView = findViewById(R.id.rv_formulas)

        // --- LÓGICA DE RECEBIMENTO DE DADOS ATUALIZADA ---
        // Pega os dados que a tela anterior enviou (nome do arquivo e nome da disciplina)
        val nomeDisciplina = intent.getStringExtra("disciplina_nome") ?: "Fórmulas"
        val nomeArquivoJson = intent.getStringExtra("disciplina_arquivo_json")

        // Define o título da tela
        tvTituloFormulas.text = nomeDisciplina

        // Configurar o clique no botão de voltar
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Carrega as fórmulas se o nome do arquivo foi recebido com sucesso
        if (nomeArquivoJson != null) {
            carregarFormulasDoArquivo(nomeArquivoJson)
        } else {
            // Se, por algum motivo, o nome do arquivo não foi passado, mostra um erro e fecha a tela.
            Toast.makeText(this, "Erro: Arquivo da disciplina não encontrado.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * Carrega os dados de uma disciplina a partir de um nome de arquivo JSON específico
     * e configura o RecyclerView para exibi-los.
     */
    private fun carregarFormulasDoArquivo(fileName: String) {
        val disciplinaJsonReader = DisciplinaJsonReader()

        // Usa o reader para carregar o objeto completo da disciplina a partir do nome do arquivo
        val disciplina = disciplinaJsonReader.loadDisciplina(this, fileName)

        // Pega a lista de fórmulas da disciplina. Se for nula, usa uma lista vazia para evitar crashes.
        val formulas = disciplina?.formulas ?: emptyList()

        // Atualizar o subtítulo com o número de fórmulas
        val numFormulas = formulas.size
        tvSubtituloFormulas.text = if (numFormulas == 1) "1 fórmula disponível" else "$numFormulas fórmulas disponíveis"

        // Configurar o adapter com as fórmulas
        // **** MODIFICAÇÃO APLICADA AQUI ****
        formulasAdapter = FormulasAdapter(this, formulas) { formula ->
            // TODO: Implementar o que acontece quando uma fórmula é clicada
            // Por exemplo, abrir um Dialog ou uma nova Activity com os detalhes da fórmula
            // Exemplo de log para testar o clique:
            android.util.Log.d("FormulasActivity", "Fórmula clicada: ${formula.name}")
        }
        recyclerView.adapter = formulasAdapter
    }
}