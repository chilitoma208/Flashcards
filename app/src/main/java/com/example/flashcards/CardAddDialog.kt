package com.example.flashcards

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class CardAddDialog :DialogFragment() {

    public interface DialogListener{
        public fun onDialogTextRecieve(dialog: DialogFragment, text: String)//Activity側へStringを渡します。
    }
    var listener:DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val s_View = inflater?.inflate(R.layout.dialog_add_card, null)

        builder.setView(s_View)
            .setTitle("card name?")
            // Add action buttons
            .setPositiveButton(R.string.add,
                DialogInterface.OnClickListener { _, _ ->
                    val text = s_View?.findViewById<EditText>(R.id.c_name)?.text
                    if(!text.isNullOrEmpty()) {
                        listener?.onDialogTextRecieve(this,text.toString())
                    }
                })
            .setNegativeButton(
                R.string.cancel,
                DialogInterface.OnClickListener { _, _ ->
                    dialog?.cancel()
                },
            )
        return builder.create()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DialogListener
        }catch (e: Exception){
            Log.e("ERROR","CANNOT FIND LISTENER")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
