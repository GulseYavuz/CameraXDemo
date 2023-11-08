package com.yavuz.cameraxdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yavuz.cameraxdemo.databinding.FragmentGalleryBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.io.File

class GalleryFragment internal constructor(): Fragment() {
    private var _fragmentGalleryBinding: FragmentGalleryBinding? = null
    private val fragmentGalleryBinding get() = _fragmentGalleryBinding!!
    private var mediaList: MutableList<MediaStoreFile> = mutableListOf()
    private var hasMediaItems = CompletableDeferred<Boolean>()

    inner class MediaPagerAdapter(
        fm: FragmentManager,
        private var mediaList: MutableList<MediaStoreFile>
    ) :
        FragmentStateAdapter(fm, lifecycle) {
        override fun getItemCount(): Int = mediaList.size
        override fun createFragment(position: Int): Fragment =
            PhotoFragment.create(mediaList[position])

        override fun getItemId(position: Int): Long {
            return mediaList[position].id
        }

        override fun containsItem(itemId: Long): Boolean {
            return null != mediaList.firstOrNull { it.id == itemId }
        }

        fun setMediaListAndNotify(mediaList: MutableList<MediaStoreFile>) {
            this.mediaList = mediaList
            notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            // Get images this app has access to from MediaStore
            mediaList = MediaStoreUtils(requireContext()).getImages()
            (fragmentGalleryBinding.viewPager.adapter as MediaPagerAdapter)
                .setMediaListAndNotify(mediaList)
            hasMediaItems.complete(mediaList.isNotEmpty())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _fragmentGalleryBinding = FragmentGalleryBinding.inflate(inflater, container, false)
        val imgFile = File("/storage/self/primary/Android/media/com.yavuz.cameraxdemo.png")
        val imageView = view?.findViewById<ImageView>(R.id.imageView)

        if(imgFile.exists()){
            val imgBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)

            imageView?.setImageBitmap(imgBitmap)
        }
        return fragmentGalleryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate the ViewPager and implement a cache of two media items
        fragmentGalleryBinding.viewPager.apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager, mediaList)
        }
    }

    override fun onDestroyView() {
        _fragmentGalleryBinding = null
        super.onDestroyView()
    }
}