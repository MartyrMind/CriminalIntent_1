package com.hfad.criminalintent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import java.io.File

private const val ARG_FILE = "file"

class DialogFragment : Fragment() {

    private lateinit var image : ImageView
    private lateinit var photoFile : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       photoFile  = arguments?.getSerializable(ARG_FILE) as File
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_fragment, container, false)
        image = view.findViewById(R.id.image)
        updatePhotoView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            image.setImageBitmap(bitmap)
            image.rotation = 90f


        } else {
            image.setImageDrawable(null)
        }
    }

   companion object {
       fun newInstance(file : File) : DialogFragment {
           val args = Bundle().apply {
               putSerializable(ARG_FILE, file)
           }
           return DialogFragment().apply {
               arguments = args
           }
       }
   }
}