package com.example.criminalintent.dialogFragment

import android.app.AlertDialog
import android.app.Dialog
import android.app.PictureInPictureParams
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import com.example.criminalintent.R
import com.example.criminalintent.utils.getScaledBitmap
import java.io.File

class PictureDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.picture_dialog, null)

            builder.setView(view)

            val crimePicture = view.findViewById<ImageView>(R.id.crimePicture)

            val photoFileName = arguments?.getString("photoFileName")
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            crimePicture.doOnLayout { measuredView ->
                val scaledBitmap = getScaledBitmap(
                    photoFile!!.path,
                    measuredView.width,
                    measuredView.height
                )
                crimePicture.setImageBitmap(scaledBitmap)
            }

            builder.setTitle("R.string.crime_photo")
                .setNegativeButton("R.string.Dismiss", DialogInterface.OnClickListener{ _, _ -> dialog?.cancel() } )

            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }

}