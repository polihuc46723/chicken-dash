package com.Kelzuni2.Vorqet.presentation.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.databinding.DialogPauseBinding

class PauseDialog : DialogFragment() {

    private var _binding: DialogPauseBinding? = null

    private var decided = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ChickenDash_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogPauseBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        bound.pauseResume.setOnClickListener {
            publish(false)
            dismissSafely()
        }
        bound.pauseQuit.setOnClickListener {
            publish(true)
            dismissSafely()
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

    override fun onCancel(dialog: DialogInterface) {
        publish(false)
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        publish(false)
        super.onDismiss(dialog)
    }

    private fun publish(quit: Boolean) {
        if (decided) {
            return
        }
        decided = true
        try {
            if (!isAdded || parentFragmentManager.isStateSaved) {
                return
            }
            val payload = Bundle()
            payload.putBoolean(KEY_QUIT, quit)
            setFragmentResult(REQUEST_KEY, payload)
        } catch (e: Exception) {
        }
    }

    private fun dismissSafely() {
        try {
            dismissAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "pause_dialog"
        const val REQUEST_KEY = "pause_request"
        const val KEY_QUIT = "pause_quit"
    }
}
