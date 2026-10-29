package com.Kelzuni2.Vorqet.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.core.di.ServiceLocator
import com.Kelzuni2.Vorqet.databinding.DialogHistoryBinding
import com.Kelzuni2.Vorqet.domain.model.SessionRecord

class HistoryDialog : DialogFragment() {

    private var _binding: DialogHistoryBinding? = null

    private val adapter = SessionHistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ChickenDash_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogHistoryBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        bound.historyList.layoutManager = LinearLayoutManager(requireContext())
        bound.historyList.adapter = adapter
        val records = loadRecords()
        adapter.submit(records)
        val empty = records.isEmpty()
        bound.historyEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        bound.historyScroll.visibility = if (empty) View.GONE else View.VISIBLE
        bound.historyClose.setOnClickListener {
            try {
                dismissAllowingStateLoss()
            } catch (e: Exception) {
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        } catch (e: Exception) {
        }
    }

    private fun loadRecords(): List<SessionRecord> = try {
        if (ServiceLocator.isReady) {
            ServiceLocator.history.invoke()
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    override fun onDestroyView() {
        val bound = _binding
        if (bound != null) {
            bound.historyList.adapter = null
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "history_dialog"
    }
}
