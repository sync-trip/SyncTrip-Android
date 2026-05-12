package com.example.synctrip.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.adapter.PhotoAdapter
import com.example.synctrip.R

class PhotoFragment : Fragment() {

    private lateinit var photoAdapter: PhotoAdapter

    // 갤러리에서 사진 선택 결과 받기
    private val getPhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let { photoAdapter.addPhoto(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정 (3열 그리드)
        photoAdapter = PhotoAdapter(mutableListOf()) { uri ->
            // 사진 클릭 시 (나중에 확대 보기 등 추가)
        }

        val rvPhotoList = view.findViewById<RecyclerView>(R.id.rvPhotoList)
        rvPhotoList.layoutManager = GridLayoutManager(requireContext(), 3)
        rvPhotoList.adapter = photoAdapter

        // 사진 추가 버튼
        val btnAddPhoto = view.findViewById<Button>(R.id.btnAddPhoto)
        btnAddPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getPhoto.launch(intent)
        }
    }
}