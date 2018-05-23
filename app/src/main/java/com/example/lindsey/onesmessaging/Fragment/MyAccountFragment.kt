package com.example.lindsey.onesmessaging.Fragment


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.lindsey.onesmessaging.R
import com.example.lindsey.onesmessaging.R.id.name
import com.example.lindsey.onesmessaging.SignInActivity
import com.example.lindsey.onesmessaging.util.FirestoreUtil
import com.example.lindsey.onesmessaging.util.StorageUtil
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.fragment_my_account.*
import kotlinx.android.synthetic.main.fragment_my_account.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import java.io.ByteArrayOutputStream
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MyAccountFragment : Fragment() {
    private val RC_SELECT_IMAGE =2;
    private lateinit var selectedImageByte: ByteArray
    private var pictureHasChanged = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_account, container, false)

        view.apply{
            imageView_profile_picture.setOnClickListener{
                    val intent = Intent().apply {
                        type = "image/*"
                        action = Intent.ACTION_GET_CONTENT
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpeg", "image/gif"))
                    }
                startActivityForResult(Intent.createChooser(intent, "select Image"), RC_SELECT_IMAGE)
            }
            btn_save.setOnClickListener{
                if(::selectedImageByte.isInitialized)
                    StorageUtil.unploadProfilePhoto(selectedImageByte){ imagePath ->
                        FirestoreUtil.updateCurrentUser(editText_name.text.toString(),
                                editText_bio.text.toString(), imagePath)
                    }
                else
                    FirestoreUtil.updateCurrentUser(editText_name.text.toString(),
                            editText_bio.text.toString(), null)

            }

            btn_sign_out.setOnClickListener{
                AuthUI.getInstance()
                        .signOut(this@MyAccountFragment.context!!)
                        .addOnCompleteListener{
                            startActivity(intentFor<SignInActivity>().newTask().clearTask())

                        }
            }
        }
return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.data !=null){
            val selectedImagePath = data.data
            val selectedImageBitMap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImagePath)

            val outputSteam = ByteArrayOutputStream()
            selectedImageBitMap.compress(Bitmap.CompressFormat.JPEG, 90, outputSteam)
            selectedImageByte = outputSteam.toByteArray()

            //TODO: Load picutre

            pictureHasChanged = true
        }
    }
    override fun onStart(){
        super.onStart()
        FirestoreUtil.getCurrentUser { user ->
            if(this@MyAccountFragment.isVisible){
                editText_bio.setText(user.bio)
                editText_name.setText(user.name)
              //  if(!pictureHasChanged && user.profilePicturePath != null)
            }
        }
    }

}
