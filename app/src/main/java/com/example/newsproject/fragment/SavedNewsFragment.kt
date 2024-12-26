package com.example.newsproject.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsproject.NewsActivity
import com.example.newsproject.R
import com.example.newsproject.adapter.NewsAdapter
import com.example.newsproject.databinding.FragmentSavedNewsBinding
import com.example.newsproject.databinding.FragmentSearchNewsBinding
import com.example.newsproject.model.news_response.Article
import com.example.newsproject.viewmodel.activity.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SavedNewsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentSavedNewsBinding
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedNewsBinding.inflate(inflater, container, false)

        initViewModel()
        prepareNewsRV()
        requestSavedNews()
        setUpSwipingToDelete()

        return binding.root
    }

    private fun setUpSwipingToDelete() {
        val callback = object :ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val currentList = ArrayList(adapter.differ.currentList)
                val toDeleteItemPosition = viewHolder.adapterPosition
                val toDeleteItem = currentList[toDeleteItemPosition]
                viewModel.deleteSavedArticle(requireContext(), toDeleteItem)

                Snackbar.make(binding.rvNews, "Deleted successfully!", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.insertSavedArticle(requireContext(), toDeleteItem)
                            binding.rvNews.scrollToPosition(toDeleteItemPosition)
                        }
                    }.show()
            }
        }
        ItemTouchHelper(callback).apply {
            attachToRecyclerView(binding.rvNews)
        }
    }

    private fun requestSavedNews() {
        adapter.differ.apply {
            if (currentList.isEmpty())
                viewModel.getAllSavedNews(requireContext())
                    .observe(viewLifecycleOwner) { articles ->
                        submitList(articles)
                    }
        }
    }

    private fun prepareNewsRV() {
        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this@SavedNewsFragment.adapter = NewsAdapter(
                findNavController(),
                R.id.action_navigation_saved_news_to_articleFragment
            )
            adapter = this@SavedNewsFragment.adapter
        }
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as NewsActivity).viewModel
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SavedNewsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}