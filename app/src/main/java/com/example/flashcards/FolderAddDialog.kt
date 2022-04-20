package com.example.flashcards


import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class FolderAddDialog: DialogFragment() {

    public interface DialogListener{
        //public fun onDialogPositive(dialog: DialogFragment)//今回は使わない。色んなダイアログで使いまわす際には使います。
        //public fun onDialogNegative(dialog: DialogFragment)
        public fun onDialogTextRecieve(dialog: DialogFragment,text: String)//Activity側へStringを渡します。
    }
    var listener:DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        // Get the layout inflater
        val inflater = activity?.layoutInflater
        val s_View = inflater?.inflate(R.layout.dialog_add_folder, null)

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(s_View)
            .setTitle("folder name?")
            // Add action buttons
            .setPositiveButton(R.string.add,
                DialogInterface.OnClickListener { dialog, id ->
                    val text = s_View?.findViewById<EditText>(R.id.f_name)?.text
                    if(!text.isNullOrEmpty()) {
                        listener?.onDialogTextRecieve(this,text.toString())
                    }
                })
            .setNegativeButton(
                R.string.cancel,
                DialogInterface.OnClickListener { _, _ ->
                    this.dialog?.cancel()
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