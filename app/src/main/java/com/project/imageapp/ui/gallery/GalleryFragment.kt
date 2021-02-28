package com.project.imageapp.ui.gallery

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.project.imageapp.R
import com.project.imageapp.data.UnsplashPhoto
import com.project.imageapp.databinding.FragmentGalleryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_gallery.*

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery), UnsplashPhotoAdapter.OnItemClickListener {
    private val viewModel by viewModels<GalleryViewModel>()

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentGalleryBinding.bind(view)
        val adapter = UnsplashPhotoAdapter(this)
        binding.apply {
            recView.setHasFixedSize(true)
            recView.itemAnimator = null
            recView.adapter = adapter.withLoadStateHeaderAndFooter(
                header = UnsplashPhotoLoadAdapter { adapter.retry() },
                footer = UnsplashPhotoLoadAdapter { adapter.retry() }
            )
            buttonRetry.setOnClickListener {
                adapter.retry()
            }
        }

        viewModel.photos.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        adapter.addLoadStateListener { loadState ->
            progress_bar.isVisible = loadState.source.refresh is LoadState.Loading
            rec_view.isVisible = loadState.source.refresh is LoadState.NotLoading
            button_retry.isVisible = loadState.source.refresh is LoadState.Error
            text_view_error.isVisible = loadState.source.refresh is LoadState.Error

            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount < 1) {
                rec_view.isVisible = false
                text_view_empty.isVisible = true
            } else {
                text_view_empty.isVisible = false
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_gallery, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    binding.recView.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(photo: UnsplashPhoto) {
        val action = GalleryFragmentDirections.actionGalleryFragmentToDetailsFragment(photo)
        findNavController().navigate(action)
    }
}