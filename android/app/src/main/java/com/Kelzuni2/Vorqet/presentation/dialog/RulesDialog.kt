package com.Kelzuni2.Vorqet.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.data.sample.SampleData
import com.Kelzuni2.Vorqet.databinding.DialogRulesBinding
import java.util.Calendar

class RulesDialog : DialogFragment() {

    private var _binding: DialogRulesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ChickenDash_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogRulesBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        bound.rulesTip.text = SampleData.tipFor(tipIndex())
        bound.rulesOk.setOnClickListener {
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

    private fun tipIndex(): Int {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return day % SampleData.TIPS.size
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "rules_dialog"
    }
}
