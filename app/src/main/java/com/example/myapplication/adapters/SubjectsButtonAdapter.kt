    package com.example.myapplication.adapters
    
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.core.content.ContextCompat
    import androidx.recyclerview.widget.RecyclerView
    import com.example.myapplication.R
    import com.example.myapplication.models.Subjects
    
    class SubjectsButtonAdapter(
        private var disciplinas: List<Subjects>,
        private val onDisciplinaClick: (Subjects) -> Unit
    ) : RecyclerView.Adapter<SubjectsButtonAdapter.DisciplinaViewHolder>() {
    
        class DisciplinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNome: TextView = itemView.findViewById(R.id.tv_disciplina_nome)
            val tvSemestre: TextView = itemView.findViewById(R.id.tv_disciplina_semestre)
            val tvStatus: TextView = itemView.findViewById(R.id.tv_disciplina_status)
            val tvDescricao: TextView = itemView.findViewById(R.id.tv_disciplina_descricao)
            val tvCurso: TextView = itemView.findViewById(R.id.tv_disciplina_curso)
            val tvFormulas: TextView = itemView.findViewById(R.id.tv_disciplina_formulas)
        }
    
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisciplinaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_subject, parent, false)
            return DisciplinaViewHolder(view)
        }
    
        override fun onBindViewHolder(holder: DisciplinaViewHolder, position: Int) {
            val disciplina = disciplinas[position]
    
            // Nome da disciplina
            holder.tvNome.text = disciplina.name
    
            // Semestre
            holder.tvSemestre.text = "${disciplina.semmester}° semestre"
    
            // Status com cor
            holder.tvStatus.text = disciplina.status.uppercase()
            configureStatusBackground(holder.tvStatus, disciplina.status)
    
            // Descrição
            holder.tvDescricao.text = disciplina.description
    
            // Curso
            holder.tvCurso.text = disciplina.course.capitalize()
    
            // Contador de fórmulas
            val formulasCount = disciplina.formulas?.size
            holder.tvFormulas.text = if (formulasCount == 1) {
                "1 fórmula"
            } else {
                "$formulasCount fórmulas"
            }
    
            // Click listener
            holder.itemView.setOnClickListener {
                onDisciplinaClick(disciplina)
            }
        }
    
        override fun getItemCount(): Int = disciplinas.size
    
        private fun configureStatusBackground(textView: TextView, status: String) {
            val context = textView.context
    
            when (status.lowercase()) {
                "active", "ativo" -> {
                    textView.setBackgroundResource(R.drawable.status_active_background)
                    textView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                }
                "inactive", "inativo" -> {
                    textView.setBackgroundResource(R.drawable.status_inactive_background)
                    textView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                }
                else -> {
                    textView.setBackgroundResource(R.drawable.status_default_background)
                    textView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }
            }
        }
    
        /**
         * Atualiza a lista de disciplinas
         */
        fun updateDisciplinas(newDisciplinas: List<Subjects>) {
            disciplinas = newDisciplinas
            notifyDataSetChanged()
        }
    }