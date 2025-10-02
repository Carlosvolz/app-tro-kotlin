package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Constants
import com.example.myapplication.models.FormulaX
import java.io.BufferedReader
import java.io.InputStreamReader

class FormulasAdapter(
    private val context: Context,
    private val formulas: List<FormulaX>,
    private val onFormulaClick: (FormulaX) -> Unit
) : RecyclerView.Adapter<FormulasAdapter.FormulaViewHolder>() {

    private val htmlKatexTemplate: String by lazy {
        loadHtmlFromAssets(context, "katex_renderer.html")
    }

    private fun loadHtmlFromAssets(context: Context, fileName: String): String {
        return try {
            val inputStream = context.assets.open(fileName)
            BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            "<html><body>Erro ao carregar template KaTeX: ${e.message}</body></html>"
        }
    }

    private fun getTextColorPrimaryHex(): String {
        return try {
            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
            val colorInt = if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT
            ) {
                typedValue.data
            } else {
                ContextCompat.getColor(context, typedValue.resourceId)
            }
            String.format("#%06X", 0xFFFFFF and colorInt)
        } catch (e: Exception) {
            Log.e("FormulasAdapter", "Erro ao obter textColorPrimary: ${e.message}")
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (nightMode) {
                Configuration.UI_MODE_NIGHT_YES -> "#FFFFFF" // Branco para modo noturno
                Configuration.UI_MODE_NIGHT_NO -> "#000000"  // Preto para modo claro
                else -> "#000000"
            }
        }
    }

    inner class FormulaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val formulaName: TextView = view.findViewById(R.id.tv_formula_name)
        val formulaDescription: TextView = view.findViewById(R.id.tv_formula_description)
        val formulaWebView: WebView = view.findViewById(R.id.webview_latex_formula)
        val container: View = itemView

        private val expandableContentLayout: LinearLayout = view.findViewById(R.id.layout_expandable_content)
        private val expandStatusTextView: TextView = view.findViewById(R.id.tv_expand_status)

        // Novas Views para Variáveis e Constantes
        private val variablesHeaderTextView: TextView = view.findViewById(R.id.tv_variables_header)
        private val variablesListTextView: TextView = view.findViewById(R.id.tv_variables_list)
        private val separatorView: View = view.findViewById(R.id.separator_variables_constants)
        private val constantsHeaderTextView: TextView = view.findViewById(R.id.tv_constants_header)
        private val constantsListTextView: TextView = view.findViewById(R.id.tv_constants_list)
        // Removido: val formulaVariablesDisplayTextView

        private var isExpanded = false
        private var isFormulaRendered = false
        private var isWebViewSetupDone = false
        private lateinit var currentFormula: FormulaX


        init {
            // A inicialização do WebView agora é feita condicionalmente no bind
            // para garantir que currentFormula esteja disponível.
        }

        @SuppressLint("SetJavaScriptEnabled")
        private fun setupWebViewDefaults(webView: WebView) {
            if (isWebViewSetupDone) return
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webView.setBackgroundColor(0x00000000) // Transparente

            webView.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d(
                            "KaTeX_WebView_JS",
                            "JS Console: \"${it.message()}\" -- (Source: ${it.sourceId()}, Line: ${it.lineNumber()})"
                        )
                    }
                    return true
                }
            }
            isWebViewSetupDone = true
        }

        fun bind(formula: FormulaX) {
            currentFormula = formula // Armazena a fórmula atual para uso no listener
            formulaName.text = formula.name
            formulaDescription.text = formula.description

            // Inicializar e configurar o WebView aqui, pois precisamos de 'formula' para renderizar
            if (!isWebViewSetupDone) { // Configura apenas uma vez
                setupWebViewDefaults(formulaWebView)
            }

            // Lógica para Variáveis
            val variablesText = formatTermsForDisplay(formula.variables)
            if (variablesText.isNotBlank()) {
                variablesListTextView.text = variablesText
            } else {
                variablesListTextView.text = "" // Limpar para garantir
            }

            // Lógica para Constantes
            val constantsText = formatTermsForDisplay(formula.constants)
            if (constantsText.isNotBlank()) {
                constantsListTextView.text = constantsText
            } else {
                constantsListTextView.text = "" // Limpar para garantir
            }

            isFormulaRendered = false // Resetar o status de renderização para cada bind
            updateExpandCollapseUI() // Atualiza a UI com base no estado de expansão e conteúdo

            container.setOnClickListener {
                isExpanded = !isExpanded
                updateExpandCollapseUI() // Atualiza a UI imediatamente ao clicar
                onFormulaClick(formula) // Notifica o click
            }
        }

        private fun updateExpandCollapseUI() {
            Log.d("DIAGNOSTICO_FORMULA", "2. Update UI. isExpanded agora é: $isExpanded")
            if (isExpanded) {

                Log.d("DIAGNOSTICO_FORMULA", "3. Verificando WebView. Latex está presente: ${currentFormula.latex.isNotEmpty()}.")
                Log.d("DIAGNOSTICO_FORMULA", "--> Conteúdo do LaTeX: ${currentFormula.latex}")
                expandableContentLayout.visibility = View.VISIBLE
                expandStatusTextView.text = context.getString(R.string.collapse)
                expandStatusTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_up,
                    0
                )

                // Visibilidade do WebView
                if (currentFormula.latex.isNotEmpty()) {
                    formulaWebView.visibility = View.VISIBLE
                    if (!isFormulaRendered) {
                        Log.d("DIAGNOSTICO_FORMULA", "4. CHAMANDO renderFormulaInWebView!")
                        renderFormulaInWebView(currentFormula)
                    }
                } else {
                    formulaWebView.visibility = View.GONE
                }

                // Visibilidade das Variáveis
                val variablesPresent = variablesListTextView.text.isNotBlank()
                variablesHeaderTextView.visibility = if (variablesPresent) View.VISIBLE else View.GONE
                variablesListTextView.visibility = if (variablesPresent) View.VISIBLE else View.GONE

                // Visibilidade das Constantes
                val constantsPresent = constantsListTextView.text.isNotBlank()
                constantsHeaderTextView.visibility = if (constantsPresent) View.VISIBLE else View.GONE
                constantsListTextView.visibility = if (constantsPresent) View.VISIBLE else View.GONE

                // Visibilidade do Separador
                separatorView.visibility = if (variablesPresent && constantsPresent) View.VISIBLE else View.GONE

            } else {
                expandableContentLayout.visibility = View.GONE
                expandStatusTextView.text = context.getString(R.string.expand)
                expandStatusTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_down,
                    0
                )
                // Não é estritamente necessário esconder os filhos aqui, pois o pai já está GONE,
                // mas para garantir o estado, especialmente do WebView:
                formulaWebView.visibility = View.GONE
                variablesHeaderTextView.visibility = View.GONE
                variablesListTextView.visibility = View.GONE
                separatorView.visibility = View.GONE
                constantsHeaderTextView.visibility = View.GONE
                constantsListTextView.visibility = View.GONE
            }
        }

        private fun formatTermsForDisplay(termsMap: Map<String, String>?): SpannableStringBuilder {
            val builder = SpannableStringBuilder()
            termsMap?.forEach { (symbol, description) ->
                if (builder.isNotEmpty()) {
                    builder.append("\n")
                }
                val start = builder.length
                builder.append("$symbol: ")
                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    builder.length,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                builder.append(description)
            }
            return builder
        }

        private fun renderFormulaInWebView(formula: FormulaX) {
            formulaWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (formula.latex.isNotEmpty()) {
                        val latexToRender = formula.latex.joinToString(" \\\\\\\\ ")
                        val escapedLatex = latexToRender
                            .replace("\\", "\\\\")
                            .replace("'", "\\'")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                        val formulaTextColor = this@FormulasAdapter.getTextColorPrimaryHex()
                        val jsCommand =
                            "javascript:clearFormula(); setPageTextColor('${formulaTextColor}'); displayFormula('${escapedLatex}');"
                        Log.d("KaTeX_JS_Command", "Executando JS: $jsCommand")
                        view?.evaluateJavascript(jsCommand, null)
                        isFormulaRendered = true
                    } else {
                        view?.evaluateJavascript("javascript:clearFormula();", null)
                        isFormulaRendered = false
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    val errorDescription = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        "${error?.errorCode} ${error?.description}"
                    } else {
                        error?.description ?: "Unknown error"
                    }
                    val urlString = request?.url?.toString() ?: "Unknown URL"
                    Log.e("KaTeX_WebView_Error", "Erro ao carregar WebView ($urlString): $errorDescription")
                    isFormulaRendered = false
                }
            }
            formulaWebView.loadDataWithBaseURL(
                "file:///android_asset/",
                htmlKatexTemplate,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormulaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_formula, parent, false)
        return FormulaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormulaViewHolder, position: Int) {
        val formula = formulas[position]
        holder.bind(formula)
    }

    override fun getItemCount() = formulas.size
}

