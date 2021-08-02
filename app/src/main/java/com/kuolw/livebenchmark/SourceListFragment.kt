package com.kuolw.livebenchmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuolw.livebenchmark.databinding.SourceListBinding

class SourceListFragment : Fragment() {
    private var _binding: SourceListBinding? = null
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SourceListBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.sources.observe(viewLifecycleOwner, { sources ->
            val sourcesAdapter = SourcesAdapter(sources)
            sourcesAdapter.setOnItemClickListener { _, _, position ->
                model.source.value = sources[position]
            }

            val sourceListView: RecyclerView = _binding!!.list
            sourceListView.adapter = sourcesAdapter
            sourceListView.layoutManager = LinearLayoutManager(context)
        })
    }
}